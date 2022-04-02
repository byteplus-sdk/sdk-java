package byteplus.sdk.core;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import static byteplus.sdk.core.Constant.METRICS_KEY_PING_SUCCESS;
import static byteplus.sdk.core.Constant.METRICS_KEY_PING_ERROR;

@Slf4j
public class HostAvailabler {
    private static final Duration INTERVAL = Duration.ofMillis(1000);

    private static final int WINDOW_SIZE = 60;

    private static final float FAILURE_RATE_THRESHOLD = (float) 0.1;

    private static final String PING_URL_FORMAT = "{}://%s/predict/api/ping";

    private static final Duration PING_TIMEOUT = Duration.ofMillis(200);

    private static final OkHttpClient httpCli = new OkHttpClient()
            .newBuilder()
            .callTimeout(PING_TIMEOUT)
            .build();

    private Map<String, Window> hostWindowMap;

    private String currentHost;

    private List<String> availableHosts;

    private final URLCenter urlCenter;

    private final Context context;

    private final String REAL_PING_URL_FORMAT;

    private ScheduledExecutorService executor;

    public HostAvailabler(Context context, URLCenter urlCenter) {
        this.urlCenter = urlCenter;
        this.context = context;
        this.REAL_PING_URL_FORMAT = PING_URL_FORMAT.replace("{}", context.getSchema());
        if (context.getHosts().size() <= 1) {
            return;
        }
        availableHosts = context.getHosts();
        currentHost = context.getHosts().get(0);
        hostWindowMap = new HashMap<>(context.getHosts().size());
        for (String host : context.getHosts()) {
            hostWindowMap.put(host, new Window(WINDOW_SIZE));
        }
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::checkHost, 0, INTERVAL.toMillis(), TimeUnit.MILLISECONDS);
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
        Request httpReq = new Request.Builder()
                .url(url)
                .headers(customerHeaders())
                .get()
                .build();
        Call httpCall = httpCli.newCall(httpReq);
        long start = System.currentTimeMillis();
        try (Response httpRsp = httpCall.execute()) {
            if (httpRsp.code() != 200) {
                Helper.reportRequestError(METRICS_KEY_PING_ERROR, url, start, httpRsp.code(), "ping-fail");
            } else {
                Helper.reportRequestSuccess(METRICS_KEY_PING_SUCCESS, url, start);
            }
            return httpRsp.code() == 200;
        } catch (Throwable e) {
            Helper.reportRequestException(METRICS_KEY_PING_ERROR, url, start, e);
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
