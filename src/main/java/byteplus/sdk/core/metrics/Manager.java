package byteplus.sdk.core.metrics;

import com.codahale.metrics.Reservoir;
import com.codahale.metrics.SlidingWindowReservoir;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.UniformSnapshot;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static byteplus.sdk.core.metrics.Constant.DEFAULT_TIDY_INTERVAL;

@Slf4j
public class Manager {
    private static final Map<String, Manager> managerCache = new HashMap<>();
    private static final int DEFAULT_FLUSH_MS = 15 * 1000;

    private final String prefix;
    // interval of clean expired metrics
    private final long tidyInterval;
    //interval of flushing all cache metrics
    private final int flushInterval;

    private final ConcurrentHashMap<Constant.MetricsType, ConcurrentHashMap<String, Metrics>> metricsMaps;

    private final ScheduledExecutorService flushExecutor;

    private final ScheduledExecutorService tidyExecutor;


    public static Manager GetManager(String prefix) {
        if (managerCache.containsKey(prefix)) {
            return managerCache.get(prefix);
        }
        synchronized (Manager.class) {
            if (managerCache.containsKey(prefix)) {
                return managerCache.get(prefix);
            }
            Manager client = new Manager(prefix, DEFAULT_TIDY_INTERVAL, DEFAULT_FLUSH_MS);
            managerCache.put(prefix, client);
            return client;
        }
    }

    private Manager() {
        this(Constant.DEFAULT_METRICS_PREFIX, DEFAULT_TIDY_INTERVAL, DEFAULT_FLUSH_MS);
    }

    public Manager(String prefix, long tidyInterval, int flushInterval) {
        this.prefix = prefix;
        this.tidyInterval = tidyInterval <= 0 ? DEFAULT_TIDY_INTERVAL : tidyInterval;
        this.flushInterval = flushInterval <= 0 ? DEFAULT_FLUSH_MS : flushInterval;
        this.metricsMaps = new ConcurrentHashMap<>();
        this.metricsMaps.put(Constant.MetricsType.metricsTypeCounter, new ConcurrentHashMap<>(256));
        this.metricsMaps.put(Constant.MetricsType.metricsTypeStore, new ConcurrentHashMap<>(256));
        this.metricsMaps.put(Constant.MetricsType.metricsTypeTimer, new ConcurrentHashMap<>(256));
        this.flushExecutor = Executors.newSingleThreadScheduledExecutor(new Helper.NamedThreadFactory("metric-flush"));
        this.flushExecutor.scheduleAtFixedRate(this::reportMetrics, this.flushInterval, this.flushInterval, TimeUnit.MILLISECONDS);
        this.tidyExecutor = Executors.newSingleThreadScheduledExecutor(new Helper.NamedThreadFactory("metric-expire"));
        this.tidyExecutor.scheduleAtFixedRate(this::tidy, this.tidyInterval, this.tidyInterval, TimeUnit.MILLISECONDS);
    }

    private void reportMetrics() {
        metricsMaps.forEach((metricsType, metricsMap) ->
                metricsMap.forEach((name, metric) -> metric.flush()));
    }

    private void tidy() {
        metricsMaps.forEach((metricsType, metricsMap) -> {
                    List<String> expiredMetrics = metricsMap.entrySet().parallelStream().filter(metrics ->
                            metrics.getValue().isExpired()).map(Map.Entry::getKey).collect(Collectors.toList());
                    if (!expiredMetrics.isEmpty()) {
                        expiredMetrics.parallelStream().forEach(key -> {
                            metricsMap.remove(key);
                            if (Config.isEnablePrintLog()) {
                                log.info("remove expired store metrics {}", key);
                            }
                        });
                    }
                }
        );
    }

    public void emitCounter(String name, double value, TreeMap<String, String> tags) {
        getOrAddMetrics(Constant.MetricsType.metricsTypeCounter, prefix + "." + name, null).
                emit(value, tags);
    }

    public void emitStore(String name, double value, TreeMap<String, String> tags) {
        getOrAddMetrics(Constant.MetricsType.metricsTypeStore, prefix + "." + name, null).
                emit(value, tags);
    }

    public void emitTimer(String name, double value, TreeMap<String, String> tags) {
        getOrAddMetrics(Constant.MetricsType.metricsTypeTimer, prefix + "." + name, tags).
                emit(value, null);
    }


    private Metrics getOrAddMetrics(Constant.MetricsType metricsType, String name, TreeMap<String, String> tags) {
        String tagString = "";
        if (tags != null && !tags.isEmpty()) {
            tagString = Helper.processTags(tags);
        }
        String metricsKey = name + tagString;
        if (metricsMaps.get(metricsType).get(metricsKey) == null) {
            synchronized (this) {
                if (metricsMaps.get(metricsType).get(metricsKey) == null) {
                    Metrics metrics = buildMetrics(metricsType, name, tagString);
                    if (metrics != null) {
                        metrics.updateExpireTime(this.tidyInterval);
                    }
                    metricsMaps.get(metricsType).put(metricsKey, metrics);
                    return metrics;
                }
            }
        }
        return metricsMaps.get(metricsType).get(metricsKey);
    }

    private Metrics buildMetrics(Constant.MetricsType metricsType, String name, String tagString) {
        switch (metricsType) {
            case metricsTypeStore:
                return new Store(name, flushInterval);
            case metricsTypeCounter:
                return new Counter(name, flushInterval);
            case metricsTypeTimer:
                return new Timer(name, tagString, new LockFreeSlidingWindowReservoir(), flushInterval);
        }
        return null;
    }

    public void stop() {
        tidy();
        this.flushExecutor.shutdownNow();
        this.tidyExecutor.shutdownNow();
    }

    static class LockFreeSlidingWindowReservoir implements Reservoir {
        private final List<Long> measurements;

        private long count;

        private static final int DEFAULT_MAX_WINDOW_SIZE = 65536;

        /**
         * Creates a new {@link SlidingWindowReservoir} which stores the last {@code size} measurements.
         */
        public LockFreeSlidingWindowReservoir() {
            this.measurements = new ArrayList<>();
            this.count = 0L;
        }

        @Override
        public int size() {
            return this.measurements.size();
        }

        @Override
        public void update(long value) {
            int index = (int) (count++ & 0x7FFFFFFFL) % DEFAULT_MAX_WINDOW_SIZE;
            if (this.measurements.size() == DEFAULT_MAX_WINDOW_SIZE) {
                this.measurements.set(index, value);
                return;
            }
            this.measurements.add(value);
        }

        @Override
        public Snapshot getSnapshot() {
            long[] values = new long[measurements.size()];
            synchronized (this) {
                for (int i = 0; i < measurements.size(); i++) {
                    values[i] = measurements.get(i);
                }
                this.measurements.clear(); // 窗口滑动机制
            }

            return new UniformSnapshot(values);
        }
    }

}
