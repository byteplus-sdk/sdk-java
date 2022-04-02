package byteplus.sdk.core.metrics;

import java.util.Objects;
import static byteplus.sdk.core.metrics.Constant.DEFAULT_HTTP_TIMEOUT_MS;

public interface MetricsOption {
    void fill(MetricsCollector.MetricsCfg options);

    static MetricsOption withMetricsDomain(String domain) {
        return options -> {
            if (Objects.nonNull(domain) && !domain.equals(""))
                options.setDomain(domain);
        };
    }

    static MetricsOption withMetricsPrefix(String prefix) {
        return options -> {
            if (Objects.nonNull(prefix) && !prefix.equals(""))
                options.setPrefix(prefix);
        };
    }

    //if not set, will not print metrics log
    static MetricsOption withMetricsLog() {
        return options -> {
            options.setPrintLog(true);
        };
    }

    //set the interval of reporting metrics
    static MetricsOption withFlushIntervalMs(long flushIntervalMs) {
        return options -> {
            if (flushIntervalMs > 500) { // flushInterval should not be too small
                options.setFlushIntervalMs(flushIntervalMs);
            }
        };
    }

    static MetricsOption withMetricsTimeout(long timeoutMs) {
        return options -> {
            if (timeoutMs > DEFAULT_HTTP_TIMEOUT_MS)
                options.setHttpTimeoutMs(timeoutMs);
        };
    }

}
