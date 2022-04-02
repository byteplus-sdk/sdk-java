package byteplus.sdk.core.metrics;

import static byteplus.sdk.core.metrics.Constant.*;

import byteplus.sdk.core.BizException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.Request;

import java.util.Objects;


@Slf4j
public class MetricsRequest {

    static void send(Metrics.MetricMessage metricRequests, String url) throws BizException {
        Request request = buildMetricsRequest(metricRequests, url);
        Response response = null;
        for (int i = 0; i < MAX_TRY_TIMES; i++) {
            try {
                response = MetricsCollector.getHttpCli().newCall(request).execute();
                if (response.code() == SUCCESS_HTTP_CODE) {
                    return;
                }
                ResponseBody rspBody = response.body();
                if (Objects.isNull(rspBody)) {
                    throw new BizException("rsp body is null");
                }
                throw new BizException(String.format("do http request fail, code:%d, rsp:%s", response.code(), rspBody));
            } catch (Throwable e) {
                String msg = e.getMessage().toLowerCase();
                // timeout exception need retry
                if (msg.contains("timeout") || msg.contains("time_out") || msg.contains("timed out")) {
                    // not the last try time, retry
                    if (i < MAX_TRY_TIMES - 1) {
                        continue;
                    }
                }
                // other exception or reach max retry time, dont retry
                throw new BizException(msg);
            } finally {
                if (response != null) response.close();
            }
        }
    }


    // batch send request
    private static Request buildMetricsRequest(Metrics.MetricMessage metricRequests, String url) {
        Request.Builder builder = new Request.Builder();
        builder.addHeader("Content-Type", "application/protobuf");
        builder.addHeader("Accept", "application/json");
        builder.url(url);
        builder.post(RequestBody.create(metricRequests.toByteArray()));
        return builder.build();
    }


}
