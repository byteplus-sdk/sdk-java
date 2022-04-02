package byteplus.sdk.core.metrics;

import byteplus.sdk.core.BizException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static byteplus.sdk.core.metrics.Constant.*;
import static byteplus.sdk.core.metrics.Helper.*;


@Slf4j
public class MetricsCollector {
    private static MetricsCfg metricsCfg;
    private static Map<MetricsType, Map<String, MetricValue>> metricsCollector;
    private static OkHttpClient httpCli;
    // init func can only exec once
    private static final AtomicBoolean initialed = new AtomicBoolean(false);
    private static ScheduledExecutorService executor;
    // timer stat names to be reported
    private static final String[] timerStatMetrics = new String[]{
            "max", "min", "avg", "pct75", "pct90", "pct95", "pct99", "pct999"};


    public static void Init(MetricsOption... opts) {
        metricsCfg = new MetricsCfg();
        // apply options
        for (MetricsOption opt : opts) {
            opt.fill((metricsCfg));
        }

        metricsCollector = new HashMap<>();
        metricsCollector.put(MetricsType.metricsTypeStore, new HashMap<>());
        metricsCollector.put(MetricsType.metricsTypeCounter, new HashMap<>());
        metricsCollector.put(MetricsType.metricsTypeTimer, new HashMap<>());

        httpCli = new OkHttpClient.Builder()
                .connectTimeout(metricsCfg.httpTimeoutMs, TimeUnit.MILLISECONDS)
                .writeTimeout(metricsCfg.httpTimeoutMs, TimeUnit.MILLISECONDS)
                .readTimeout(metricsCfg.httpTimeoutMs, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .build();

        if (!initialed.get()) {
            initialed.set(true);
            executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(MetricsCollector::report, DEFAULT_FLUSH_INTERVAL_MS,
                    DEFAULT_FLUSH_INTERVAL_MS, TimeUnit.MILLISECONDS);
        }
    }

    public static OkHttpClient getHttpCli() {
        return httpCli;
    }

    static boolean isEnableMetrics() {
        if (Objects.isNull(metricsCfg)) {
            return false;
        }
        return metricsCfg.isEnableMetrics();
    }

    static boolean isEnablePrintLog() {
        if (Objects.isNull(metricsCfg)) {
            return false;
        }
        return metricsCfg.isPrintLog();
    }

    public static void emitStore(String name, long value, String... tagKvs) {
        if (!isEnableMetrics()) {
            return;
        }
        String collectKey = buildCollectKey(name, tagKvs);
        updateMetric(MetricsType.metricsTypeStore, collectKey, value);
    }

    public static void emitCounter(String name, long value, String... tagKvs) {
        if (!isEnableMetrics()) {
            return;
        }
        String collectKey = buildCollectKey(name, tagKvs);
        updateMetric(MetricsType.metricsTypeCounter, collectKey, value);
    }

    public static void emitTimer(String name, long value, String... tagKvs) {
        if (!isEnableMetrics()) {
            return;
        }
        String collectKey = buildCollectKey(name, tagKvs);
        updateMetric(MetricsType.metricsTypeTimer, collectKey, value);
    }

    private static void updateMetric(MetricsType metricsType, String collectKey, long value) {
        MetricValue metric = getOrCreateMetric(metricsType, collectKey);
        switch (metricsType) {
            case metricsTypeStore:
                metric.value = value;
                break;
            case metricsTypeCounter:
                ((AtomicLong) metric.value).addAndGet(value);
                break;
            case metricsTypeTimer:
                ((Sample) metric.value).update(value);
                break;
        }
        metric.updated = true;
    }

    private static MetricValue getOrCreateMetric(MetricsType metricsType, String collectKey) {
        if (Objects.nonNull(metricsCollector.get(metricsType).get(collectKey))) {
            return metricsCollector.get(metricsType).get(collectKey);
        }
        synchronized (metricsType) {
            if (Objects.nonNull(metricsCollector.get(metricsType).get(collectKey))) {
                return metricsCollector.get(metricsType).get(collectKey);
            }
            metricsCollector.get(metricsType).put(collectKey, buildDefaultMetric(metricsType));
            return metricsCollector.get(metricsType).get(collectKey);
        }
    }

    private static MetricValue buildDefaultMetric(MetricsType metricsType) {
        switch (metricsType) {
            case metricsTypeTimer:
                return new MetricValue(new Sample(RESERVOIR_SIZE), null);
            case metricsTypeCounter:
                return new MetricValue(new AtomicLong(0), new AtomicLong(0));
        }
        return new MetricValue((long) 0, (long) 0);
    }

    private static void report() {
        if (!isEnableMetrics()) {
            return;
        }
        flushStore();
        flushCounter();
        flushTimer();
    }

    private static void flushStore() {
        ArrayList<Metrics.Metric> metricsRequests = new ArrayList<>();
        metricsCollector.get(MetricsType.metricsTypeStore).forEach((collectKey, metric) -> {
            if (metric.updated) {
                metric.updated = false;
                List<String> nameAndTags = parseNameAndTags(collectKey);
                if (Objects.isNull(nameAndTags)) {
                    return;
                }
                String name = nameAndTags.get(0);
                Map<String, String> tagKvs = recoverTags(nameAndTags.get(1));
                Metrics.Metric metricsRequest = Metrics.Metric.newBuilder().
                        setMetric(metricsCfg.getPrefix() + "." + name).
                        putAllTags(tagKvs).
                        setValue((long) metric.value).
                        setTimestamp(System.currentTimeMillis() / 1000).
                        build();
                metricsRequests.add(metricsRequest);
            }
        });
        if (metricsRequests.size() > 0) {
            String url = OTHER_URL_FORMAT.replace("{}", metricsCfg.getDomain());
            sendMetrics(metricsRequests, url);
        }
    }

    private static void flushCounter() {
        ArrayList<Metrics.Metric> metricsRequests = new ArrayList<>();
        metricsCollector.get(MetricsType.metricsTypeCounter).forEach((collectKey, metric) -> {
            if (metric.updated) {
                metric.updated = false;
                List<String> nameAndTags = parseNameAndTags(collectKey);
                if (Objects.isNull(nameAndTags)) {
                    return;
                }
                String name = nameAndTags.get(0);
                Map<String, String> tagKvs = recoverTags(nameAndTags.get(1));
                double valueCopy = ((AtomicLong) metric.value).doubleValue();
                Metrics.Metric metricsRequest = Metrics.Metric.newBuilder().
                        setMetric(metricsCfg.getPrefix() + "." + name).
                        putAllTags(tagKvs).
                        setValue(valueCopy - ((AtomicLong) (metric.flushedValue)).doubleValue()).
                        setTimestamp(System.currentTimeMillis() / 1000).
                        build();
                metricsRequests.add(metricsRequest);
                // after each flushInterval of the counter is reported, the accumulated metric needs to be cleared
                ((AtomicLong) metric.flushedValue).set((long) valueCopy);
                // if the value is too large, reset it
                if ((long) valueCopy >= Long.MAX_VALUE / 2) {
                    ((AtomicLong) metric.value).set(0);
                    ((AtomicLong) metric.flushedValue).set(0);
                }
            }
        });
        if (metricsRequests.size() > 0) {
            String url = COUNTER_URL_FORMAT.replace("{}", metricsCfg.getDomain());
            sendMetrics(metricsRequests, url);
        }
    }

    private static void flushTimer() {
        ArrayList<Metrics.Metric> metricsRequests = new ArrayList<>();
        metricsCollector.get(MetricsType.metricsTypeTimer).forEach((collectKey, metric) -> {
            if (metric.updated) {
                metric.updated = false;
                Sample.SampleSnapshot snapshot = ((Sample) metric.value).getSnapshot();
                // clear sample every sample period
                ((Sample) metric.value).clear();
                List<String> nameAndTags = parseNameAndTags(collectKey);
                if (Objects.isNull(nameAndTags)) {
                    return;
                }
                String name = nameAndTags.get(0);
                Map<String, String> tagKvs = recoverTags(nameAndTags.get(1));
                metricsRequests.addAll(buildStatMetrics(snapshot, name, tagKvs));
            }
        });
        if (metricsRequests.size() > 0) {
            String url = OTHER_URL_FORMAT.replace("{}", metricsCfg.getDomain());
            sendMetrics(metricsRequests, url);
        }
    }

    private static void sendMetrics(ArrayList<Metrics.Metric> metricsRequests, String url) {
        Metrics.MetricMessage request = Metrics.MetricMessage.newBuilder().addAllMetrics(metricsRequests).build();
        try {
            MetricsRequest.send(request, url);
            if (isEnablePrintLog()) {
                log.debug("[BytePlusSDK][Metrics] send metrics success, url:{}, metricsRequests:{}", url, metricsRequests);
            }
        } catch (BizException e) {
            log.error("[BytePlusSDK][Metrics] send metrics exception, msg:{}, url:{}, metricsRequests:{}", e.getMessage(),
                    url, metricsRequests);
        }
    }

    private static List<Metrics.Metric> buildStatMetrics(Sample.SampleSnapshot snapshot,
                                                         String name, Map<String, String> tagKvs) {
        long timestamp = System.currentTimeMillis() / 1000;
        ArrayList<Metrics.Metric> metricsRequests = new ArrayList<>();
        for (String statName : timerStatMetrics) {
            double value = getStatValue(statName, snapshot);
            if (value < 0) {
                continue;
            }
            metricsRequests.add(Metrics.Metric.newBuilder().
                    setMetric(metricsCfg.getPrefix() + "." + name + "." + statName).
                    putAllTags(tagKvs).
                    setValue(value).
                    setTimestamp(timestamp).
                    build());
        }
        return metricsRequests;
    }

    private static double getStatValue(String statName, Sample.SampleSnapshot snapshot) {
        switch (statName) {
            case "max":
                return snapshot.getMax();
            case "min":
                return snapshot.getMin();
            case "avg":
                return snapshot.getMean();
            case "pct75":
                return snapshot.getValue(0.75D);
            case "pct90":
                return snapshot.getValue(0.90D);
            case "pct95":
                return snapshot.getValue(0.95D);
            case "pct99":
                return snapshot.getValue(0.99D);
            case "pct999":
                return snapshot.getValue(0.999D);
        }
        return -1;
    }

    @Data
    static class MetricsCfg {
        private boolean enableMetrics;
        private String domain;
        private String prefix;
        private boolean printLog;
        private long flushIntervalMs;
        private long httpTimeoutMs;

        // build default metricsCfg
        public MetricsCfg() {
            this.setEnableMetrics(true);
            this.setDomain(DEFAULT_METRICS_DOMAIN);
            this.setPrefix(DEFAULT_METRICS_PREFIX);
            this.setFlushIntervalMs(DEFAULT_FLUSH_INTERVAL_MS);
            this.setHttpTimeoutMs(DEFAULT_HTTP_TIMEOUT_MS);
        }
    }

    @Data
    static class MetricValue {
        private Object value;
        private Object flushedValue;
        private boolean updated;

        MetricValue(Object value, Object flushedValue) {
            if (Objects.nonNull(value)) {
                this.value = value;
            }
            if (Objects.nonNull(flushedValue)) {
                this.flushedValue = flushedValue;
            }
        }
    }
}
