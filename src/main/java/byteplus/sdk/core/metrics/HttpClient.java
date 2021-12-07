package byteplus.sdk.core.metrics;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HttpClient {
    private static final Map<String, HttpClient> clientCache = new HashMap<>();
    private final static String CONTENT_TYPE_JSON = "application/json";
    private final static int DEFAULT_HTTP_TIMEOUT_MS = 800;
    private final static int MAX_REQUEST_SIZE = 5000; // in case memory explosion caused by too many requests
    private final static int MAX_TRY_TIMES = 2;
    private final LinkedBlockingQueue<List<Request>> queue;
    private final ExecutorService executor;
    private final String url;

    private static class ClientHolder {
        private static final OkHttpClient client = new OkHttpClient.Builder()
                .protocols(Constant.PROTOCOL_LIST)
                .connectTimeout(DEFAULT_HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .writeTimeout(DEFAULT_HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .readTimeout(DEFAULT_HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .build();

        private static OkHttpClient getClient() {
            return ClientHolder.client;
        }
    }

    public static HttpClient getClient(String url) {
        if (clientCache.containsKey(url)) {
            return clientCache.get(url);
        }
        synchronized (HttpClient.class) {
            if (clientCache.containsKey(url)) {
                return clientCache.get(url);
            }
            HttpClient client = new HttpClient(url);
            clientCache.put(url, client);
            return client;
        }
    }

    public HttpClient(String url) {
        this.url = url;
        this.queue = new LinkedBlockingQueue<>(MAX_REQUEST_SIZE);
        this.executor = Executors.newSingleThreadExecutor();
        executor.submit((Runnable) () -> {
            while (true) {
                try {
                    List<Request> requests = queue.take();
                    if (!send(buildMetricsRequest(requests))) {
                        log.error("exec metrics fail, url:{}", url);
                    }
                } catch (InterruptedException e) {
                    log.error("poll metrics requests fail: {} \n {}", e.getMessage(), Helper.ExceptionUtil.getTrace(e));
                }
            }
        });
    }

    public void put(List<Request> requests) {
        if (!this.queue.offer(requests)) {
            if (Config.isEnablePrintLog()) {
                log.warn("metrics requests emit too fast, exceed max queue size({})", MAX_REQUEST_SIZE);
            }
        }
    }

    // todo:换成pb格式请求
    public boolean send(Request request) {
        Response response = null;
        for (int i = 0; i < MAX_TRY_TIMES; i++) {
            try {
                response = ClientHolder.getClient().newCall(request).execute();
                if (response.isSuccessful()) {
                    if (Config.isEnablePrintLog()) {
                        log.debug("success reporting metrics request:\n{}", response);
                    }
                    return true;
                }
                // if not success and no exception, print log
                if (Config.isEnablePrintLog()) {
                    log.error("do http request fail, url:{}\n response:{}\n", url, response);
                }
            } catch (Throwable e) {
                log.error("do http request exception: {} \n {}", e.getMessage(), Helper.ExceptionUtil.getTrace(e));
            } finally {
                if (response != null) response.close();
            }
        }
        return false;
    }

    public boolean emit(List<Request> requests) {
        Request request = buildMetricsRequest(requests);
        return send(request);
    }

    // batch send request
    private Request buildMetricsRequest(List<Request> requests) {
        Request.Builder builder = new Request.Builder();
        for (Map.Entry<String, String> entry : generateMetricsHeader().entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
        builder.url(url);
        RequestBody body = RequestBody.create(JSON.toJSONString(requests), MediaType.parse(CONTENT_TYPE_JSON));
        builder.post(body);
        return builder.build();
    }

    private Map<String, String> generateMetricsHeader() {
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", CONTENT_TYPE_JSON);
        header.put("Accept", CONTENT_TYPE_JSON);
        return header;
    }

    // todo：如何关闭，可以做成过期自动关闭
    public void stop() {
        executor.shutdownNow();
    }
}
