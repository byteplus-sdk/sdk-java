package byteplus.sdk.core;

import byteplus.sdk.retail.RetailURL;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
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

@Slf4j
public class HostAvailabler {
    private static final Duration interval = Duration.ofMillis(1000);

    private static final int windowSize = 60;

    private static final float failureRateThreshold = (float) 0.1;

    private static final String URL_FORMAT = "https://%s/air/api/ping";

    private static final OkHttpClient httpCli = new OkHttpClient()
            .newBuilder()
            .callTimeout(Duration.ofMillis(300))
            .build();

    private Map<String, Window> hostWindowMap;

    private String currentHost;

    private List<String> availableHosts;

    private final RetailURL retailUrl;

    private final Context context;

    private ScheduledExecutorService executor;

    public HostAvailabler(RetailURL retailUrl, Context context) {
        this.retailUrl = retailUrl;
        this.context = context;
        if (context.getHosts().size() <= 1) {
            return;
        }
        hostWindowMap = new HashMap<>(context.getHosts().size());
        currentHost = context.getHosts().get(0);
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::checkHost, 0, interval.toMillis(), TimeUnit.MILLISECONDS);
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
            boolean success = doPing(host);
            Window window = hostWindowMap.get(host);
            if (Objects.isNull(window)) {
                window = new Window(windowSize);
                hostWindowMap.put(host, window);
            }
            window.put(success);
            if (window.getFailureRate() < failureRateThreshold) {
                availableHosts.add(host);
            }
        }
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
            retailUrl.refreshUrl(currentHost);
        }
    }

    private boolean doPing(String host) {
        try {
            String url = String.format(URL_FORMAT, host);
            Request httpReq = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            Call httpCall = httpCli.newCall(httpReq);
            Response httpRsp = httpCall.execute();
            httpRsp.close();
            return httpRsp.code() == 200;
        } catch (Throwable e) {
            log.debug("[ByteplusSDK] ping find err, host:{} err:{}", host, e.getMessage());
            return false;
        }
    }

    private static class Window {
        private final int size;
        private final boolean[] array;
        private int head = -1;
        private int tail = 0;
        private float successCount;
        private float failureCount = 0;

        private Window(int size) {
            this.size = size;
            array = new boolean[size];
            Arrays.fill(array, true);
            successCount = size;
        }

        void put(boolean success) {
            if (success) {
                successCount++;
            } else {
                failureCount++;
            }
            head = (head + 1) % size;
            array[head] = success;
            tail = (tail + 1) & size;
            boolean removedValue = array[tail];
            if (removedValue) {
                successCount--;
            } else {
                failureCount--;
            }
        }

        float getFailureRate() {
            return failureCount / (failureCount + successCount);
        }
    }
}
