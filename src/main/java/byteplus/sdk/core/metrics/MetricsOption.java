package byteplus.sdk.core.metrics;

import java.time.Duration;
import java.util.Objects;

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

    static MetricsOption withMetricsHttpSchema(String schema) {
        return options -> {
            // only support "http" and "https"
            if ("http".equals(schema) || "https".equals(schema))
                options.setHttpSchema(schema);
        };
    }

    //if not set, will not report metrics.
    static MetricsOption enableMetrics() {
        return options -> {
            options.setEnableMetrics(true);
        };
    }

    //if not set, will not report metrics logs.
    static MetricsOption enableMetricsLog() {
        return options -> {
            options.setEnableMetricsLog(true);
        };
    }

    //set the interval of reporting metrics
    static MetricsOption withReportInterval(Duration reportInterval) {
        return options -> {
            if (reportInterval.toMillis() > 1000) { // reportInterval should not be too small
                options.setReportInterval(reportInterval);
            }
        };
    }

    static MetricsOption withMetricsTimeout(Duration timeout) {
        return options -> {
            options.setHttpTimeout(timeout);
        };
    }
}
