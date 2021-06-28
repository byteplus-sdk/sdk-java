package byteplus.sdk.core;

import okhttp3.Headers;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public interface Option {
    void fill(Options options);

    static Options conv2Options(Option[] opts) {
        Options options = new Options();
        if (Objects.isNull(opts) || opts.length == 0) {
            return options;
        }
        for (Option opt : opts) {
            opt.fill((options));
        }
        return options;
    }

    static Option withTimeout(Duration timeout) {
        return options -> {
            if (timeout.toMillis() <= 0) {
                return;
            }
            options.setTimeout(timeout);
        };
    }

    static Option withRequestId(String requestId) {
        return options -> options.setRequestId(requestId);
    }

    static Option withHeaders(Map<String, String> headers) {
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
