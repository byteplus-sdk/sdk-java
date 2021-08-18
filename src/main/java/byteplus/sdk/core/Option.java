package byteplus.sdk.core;

import okhttp3.Headers;

import java.time.Duration;
import java.time.LocalDate;
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
        return options -> options.setHeaders(headers);
    }

    static Option withDataDate(LocalDate date) {
        return options -> options.setDataDate(date);
    }

    static Option withDataEnd(Boolean isEnd) {
        return options -> options.setDataIsEnd(isEnd);
    }

    static Option withServerTimeout(Duration timeout) {
        return options -> options.setServerTimeout(timeout);
    }

    static Option withQueries(Map<String, String> queries) {
        return options -> options.setQueries(queries);
    }

    static Option withStage(String stage) {
        return options -> options.setStage(stage);
    }
}
