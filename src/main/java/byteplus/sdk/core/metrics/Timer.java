package byteplus.sdk.core.metrics;

import com.codahale.metrics.Reservoir;
import com.codahale.metrics.Snapshot;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadFactory;

import static byteplus.sdk.core.metrics.Constant.DEFAULT_METRICS_EXPIRE_TIME_MS;

@Slf4j
public class Timer implements Metrics{
    private final HttpClient httpCli;

    private final String name;

    private final Map<String, String> tagMap;

    private final Queue<Double> queue;

    private final Reservoir reservoir;

    private static final ThreadFactory TIMER_THREAD_FACTORY;

    private long expireTime;

    public Timer(String name, String tags, Reservoir reservoir, int flushTimeMs) {
        this.name = name;
        this.expireTime = System.currentTimeMillis() + DEFAULT_METRICS_EXPIRE_TIME_MS;
        this.tagMap = Helper.recoverTags(tags);
        this.reservoir = reservoir;
        this.httpCli = HttpClient.getClient(Constant.OTHER_URL_FORMAT.replace("{}", Config.getMetricsDomain()));
        this.queue = new ConcurrentLinkedQueue<>();
    }

    static {
        TIMER_THREAD_FACTORY = new Helper.NamedThreadFactory("metric-timer-flush");
    }

    public String getName() {
        return this.name;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > this.expireTime;
    }

    public void updateExpireTime(long ttlInMs) {
        if (ttlInMs > 0) {
            this.expireTime = System.currentTimeMillis() + ttlInMs;
        }
    }

    @Override
    public void emit(Double value, Map<String, String> tags) {
        this.queue.offer(value);
    }

    public void flush() {
        try {
            int size = 0;
            while (size < Constant.MAX_FLUSH_SIZE && !this.queue.isEmpty()) {
                long item = this.queue.poll().longValue();
                this.reservoir.update(item);
                size++;
            }
            Snapshot snapshot = this.reservoir.getSnapshot();
            List<Request> requests = buildMetricList(snapshot, size);
            this.httpCli.put(requests);
            if ((Config.isEnablePrintLog())) {
                log.info("remove : {}", requests);
            }
        } catch (Throwable e) {
            log.error("flush timer exception: {} \n {}", e.getMessage(), Helper.ExceptionUtil.getTrace(e));
        }
    }



    public List<Request> buildMetricList(Snapshot shot, int size) {
        List<Request> data = new ArrayList<>();
        long timestamp = System.currentTimeMillis() / 1000L;

        //count
        Request<Long> countRequest = new Request<>();
        countRequest.setMetric(name + "." + "count");
        countRequest.setTimestamp(timestamp);
        countRequest.setTags(new HashMap<>(this.tagMap));
        countRequest.setValue((long) size);
        data.add(countRequest);

        //max
        Request<Long> maxRequest = new Request<>();
        maxRequest.setMetric(name + "." + "max");
        maxRequest.setTimestamp(timestamp);
        maxRequest.setTags(new HashMap<>(this.tagMap));
        maxRequest.setValue(shot.getMax());
        data.add(maxRequest);

        //min
        Request<Long> minRequest = new Request<>();
        minRequest.setMetric(name + "." + "min");
        minRequest.setTimestamp(timestamp);
        minRequest.setTags(new HashMap<>(this.tagMap));
        minRequest.setValue(shot.getMin());
        data.add(minRequest);

        //avg
        Request<Double> avgRequest = new Request<>();
        avgRequest.setMetric(name + "." + "avg");
        avgRequest.setTimestamp(timestamp);
        avgRequest.setTags(new HashMap<>(this.tagMap));
        avgRequest.setValue(shot.getMean());
        data.add(avgRequest);

        // median
        Request<Double> medianRequest = new Request<>();
        medianRequest.setMetric(name + "." + "median");
        medianRequest.setTimestamp(timestamp);
        medianRequest.setTags(new HashMap<>(this.tagMap));
        medianRequest.setValue(shot.getMedian());
        data.add(medianRequest);

        //pc75
        Request<Double> pc75Request = new Request<>();
        pc75Request.setMetric(name + "." + "pct75");
        pc75Request.setTimestamp(timestamp);
        pc75Request.setTags(new HashMap<>(this.tagMap));
        pc75Request.setValue(shot.get75thPercentile());
        data.add(pc75Request);

        //pc90
        Request<Double> pc90Request = new Request<>();
        pc90Request.setMetric(name + "." + "pct90");
        pc90Request.setTimestamp(timestamp);
        pc90Request.setTags(new HashMap<>(this.tagMap));
        pc90Request.setValue(shot.getValue(0.90D));
        data.add(pc90Request);

        //pc95
        Request<Double> pc95Request = new Request<>();
        pc95Request.setMetric(name + "." + "pct95");
        pc95Request.setTimestamp(timestamp);
        pc95Request.setTags(new HashMap<>(this.tagMap));
        pc95Request.setValue(shot.get95thPercentile());
        data.add(pc95Request);

        //pc99
        Request<Double> pc99Request = new Request<>();
        pc99Request.setMetric(name + "." + "pct99");
        pc99Request.setTimestamp(timestamp);
        pc99Request.setTags(new HashMap<>(this.tagMap));
        pc99Request.setValue(shot.get99thPercentile());
        data.add(pc99Request);

        //pc999
        Request<Double> pc999Request = new Request<>();
        pc999Request.setMetric(name + "." + "pct999");
        pc999Request.setTimestamp(timestamp);
        pc999Request.setTags(new HashMap<>(this.tagMap));
        pc999Request.setValue(shot.get999thPercentile());
        data.add(pc999Request);
        return data;
    }

}
