package byteplus.sdk.core.metrics;

import byteplus.sdk.core.BizException;
import byteplus.sdk.core.metrics.protocol.SdkMetrics.MetricMessage;
import byteplus.sdk.core.metrics.protocol.SdkMetrics.MetricLogMessage;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import byteplus.sdk.core.metrics.MetricsCollector.MetricsCfg;
import static byteplus.sdk.core.metrics.Constant.*;


@Slf4j
public class MetricsReporter {
    private final OkHttpClient httpCli;

    protected MetricsReporter(MetricsCfg metricsCfg) {
        httpCli = new OkHttpClient.Builder()
                .connectTimeout(metricsCfg.getHttpTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .writeTimeout(metricsCfg.getHttpTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .readTimeout(metricsCfg.getHttpTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    protected void report(MetricMessage metricMessage, String url) throws BizException {
        byte[] reqBodyBytes = metricMessage.toByteArray();
        Headers headers = buildMetricsHeaders();
        doRequest(url, reqBodyBytes, headers);
    }

    protected void report(MetricLogMessage metricLogMessage, String url) throws BizException {
        byte[] reqBodyBytes = metricLogMessage.toByteArray();
        Headers headers = buildMetricsHeaders();
        doRequest(url, reqBodyBytes, headers);
    }

    private void doRequest(String url, byte[] reqBodyBytes, Headers headers) throws BizException {
        Request request = new Request.Builder()
                .url(url)
                .headers(headers)
                .post(RequestBody.create(reqBodyBytes))
                .build();
        Response response = null;
        for (int i = 0;; i++) {
            try {
                response = httpCli.newCall(request).execute();
                if (response.code() == SUCCESS_HTTP_CODE) {
                    return;
                }
                ResponseBody rspBody = response.body();
                if (Objects.isNull(rspBody)) {
                    throw new BizException("rsp body is null");
                }
                throw new BizException(String.format("do http request fail, code:%d, rsp:%s",
                        response.code(), new String(rspBody.bytes())));
            } catch (IOException e) {
                if (!e.getMessage().toLowerCase().contains("timeout")) {
                    throw new BizException(e.toString());
                }
                // timeout exception need retry
                // not the last try time, retry
                if (i == MAX_TRY_TIMES - 1) {
                    throw new BizException(e.toString());
                }
            } finally {
                if (response != null) response.close();
            }
        }
    }

    private Headers buildMetricsHeaders() {
        return new Headers.Builder()
                .set("Content-Type", "application/x-protobuf")
                .set("Accept", "application/json")
                .build();
    }
}
