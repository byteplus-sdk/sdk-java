package byteplus.sdk.core;

import okhttp3.Headers;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public class Option {
    public static Options.Filler withTimeout(Duration timeout) {
        return options -> {
            if (timeout.toMillis() <= 0) {
                return;
            }
            options.setTimeout(timeout);
        };
    }

    public static Options.Filler withRequestId(String requestId) {
        return options -> options.setRequestId(requestId);
    }

    public static Options.Filler withHeaders(Map<String, String> headers) {
        return options -> {
            if (Objects.isNull(headers) || headers.isEmpty()) {
                return;
            }
            Headers.Builder headersBuilder = new Headers.Builder();
            headers.forEach(headersBuilder::set);
            options.setHeaders(headersBuilder.build());
        };
    }
}
