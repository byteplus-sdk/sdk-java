package byteplus.sdk.core;

import byteplus.sdk.core.metrics.MetricsLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import static byteplus.sdk.core.Constant.METRICS_KEY_COMMON_ERROR;

@Slf4j
public class HostAvailabler {
    private static final float FAILURE_RATE_THRESHOLD = (float) 0.1;

    private static final int DEFAULT_WINDOW_SIZE = 60;

    private static final String DEFAULT_PING_URL_FORMAT = "%s://%s/predict/api/ping";

    private static final Duration DEFAULT_PING_TIMEOUT = Duration.ofMillis(300);

    private static final Duration DEFAULT_PING_INTERVAL = Duration.ofSeconds(1);

    private final OkHttpClient httpCli;

    private Map<String, Window> hostWindowMap;

    private String currentHost;

    private List<String> availableHosts;

    private final Config config;

    private final URLCenter urlCenter;

    private final Context context;

    private final String REAL_PING_URL_FORMAT;

    private ScheduledExecutorService executor;

    public HostAvailabler(Context context, URLCenter urlCenter) {
        this.urlCenter = urlCenter;
        this.context = context;
        this.config = fillDefaultConfig(context.getHostAvailablerConfig());
        this.REAL_PING_URL_FORMAT = config.getPingURLFormat().replace("{}", context.getSchema());
        this.httpCli = new OkHttpClient()
                .newBuilder()
                .callTimeout(config.getPingTimeout())
                .build();
        if (context.getHosts().size() <= 1) {
            return;
        }
        availableHosts = context.getHosts();
        currentHost = context.getHosts().get(0);
        hostWindowMap = new HashMap<>(context.getHosts().size());
        for (String host : context.getHosts()) {
            hostWindowMap.put(host, new Window(config.getWindowSize()));
        }
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::checkHost, 0, config.pingInterval.toMillis(), TimeUnit.MILLISECONDS);
    }

    private Config fillDefaultConfig(Config config) {
        config = config.toBuilder().build();
        if (Objects.isNull(config.pingURLFormat)) {
            config.pingURLFormat = DEFAULT_PING_URL_FORMAT;
        }
        if (Objects.isNull(config.pingTimeout) || config.pingTimeout.isZero()) {
            config.pingTimeout = DEFAULT_PING_TIMEOUT;
        }
        if (config.windowSize <= 0) {
            config.windowSize = DEFAULT_WINDOW_SIZE;
        }
        if (Objects.isNull(config.pingInterval) || config.pingInterval.isZero()) {
            config.pingInterval = DEFAULT_PING_INTERVAL;
        }
        return config;
    }

    public void shutdown() {
        if (Objects.isNull(executor)) {
            return;
        }
        executor.shutdown();
    }

    private void checkHost() {
        try {
            doCheckHost();
            switchHost();
        } catch (Throwable e) {
            log.error("[ByteplusSDK] ping find unexpected err, {}", e.getMessage());
        }
    }

    private void doCheckHost() {
        availableHosts = new ArrayList<>(context.getHosts().size());
        for (String host : context.getHosts()) {
            Window window = hostWindowMap.get(host);
            window.put(doPing(host));
            if (window.failureRate() < FAILURE_RATE_THRESHOLD) {
                availableHosts.add(host);
            }
        }
        if (availableHosts.size() <= 1) {
            return;
        }
        availableHosts.sort((host1, host2) -> {
            float host1FailureRate = hostWindowMap.get(host1).failureRate();
            float host2FailureRate = hostWindowMap.get(host2).failureRate();
            float delta = host1FailureRate - host2FailureRate;
            if (delta > 0.0001) {
                return 1;
            } else if (delta < -0.0001) {
                return -1;
            }
            return 0;
        });
    }

    private boolean doPing(String host) {
        String url = String.format(REAL_PING_URL_FORMAT, host);
        String reqID = "ping_" + UUID.randomUUID().toString();
        Headers headers = customerHeaders().newBuilder()
                .set("Request-Id", reqID).build();
        Request httpReq = new Request.Builder()
                .url(url)
                .headers(headers)
                .get()
                .build();
        Call httpCall = httpCli.newCall(httpReq);
        long start = System.currentTimeMillis();
        try (Response httpRsp = httpCall.execute()) {
            long cost = System.currentTimeMillis() - start;
            if (httpRsp.code() != 200) {
                MetricsLog.warn(reqID, "[ByteplusSDK] ping fail, tenant:%s, host:%s, cost:%dms, status:%d",
                        context.getTenant() ,Helper.escapeMetricsTagValue(host), cost, httpRsp.code());
            } else {
                MetricsLog.info(reqID, "[ByteplusSDK] ping success, tenant:%s, host:%s, cost:%dms",
                        context.getTenant(), Helper.escapeMetricsTagValue(host), cost);
            }
            return httpRsp.code() == 200;
        } catch (Throwable e) {
            MetricsLog.warn(reqID, "[ByteplusSDK] ping find err, tenant:%s, host:%s, err:%s",
                    context.getTenant(), Helper.escapeMetricsTagValue(host), e.getMessage());
            log.warn("[ByteplusSDK] ping find err, host:{} err:{}", host, e.getMessage());
            return false;
        } finally {
            long cost = System.currentTimeMillis() - start;
            log.debug("[ByteplusSDK] ping host:'{}' cost:'{}ms'", host, cost);
        }
    }

    private Headers customerHeaders() {
        Headers.Builder builder = new Headers.Builder();
        context.getCustomerHeaders().forEach(builder::set);
        return builder.build();
    }

    private void switchHost() {
        String newHost;
        if (availableHosts.isEmpty()) {
            newHost = context.getHosts().get(0);
        } else {
            newHost = availableHosts.get(0);
        }
        if (!currentHost.equals(newHost)) {
            log.warn("[ByteplusSDK] switch host to {}, origin is {}", newHost, currentHost);
            currentHost = newHost;
            urlCenter.refresh(currentHost);
        }
    }

    public String getHost() {
        return currentHost;
    }

    @Getter
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Config {
        private String pingURLFormat;

        private Duration pingTimeout;

        private Duration pingInterval;

        private int windowSize;
    }

    private static class Window {
        private final int size;
        private final boolean[] items;
        private int head;
        private int tail = 0;
        private float failureCount = 0;

        private Window(int size) {
            this.size = size;
            this.head = size - 1;
            items = new boolean[size];
            Arrays.fill(items, true);
        }

        void put(boolean success) {
            if (!success) {
                failureCount++;
            }
            head = (head + 1) % size;
            items[head] = success;
            tail = (tail + 1) % size;
            boolean removingItem = items[tail];
            if (!removingItem) {
                failureCount--;
            }
        }

        float failureRate() {
            return failureCount / (float) size;
        }
    }
}
