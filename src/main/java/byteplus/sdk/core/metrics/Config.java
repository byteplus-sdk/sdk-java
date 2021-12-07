package byteplus.sdk.core.metrics;


public class Config {
    private static String metricsDomain = Constant.DEFAULT_METRICS_DOMAIN;
    private static boolean printLog;

    public static void setMetricsDomain(String domain) {
        metricsDomain = domain;
    }

    public static void setPrintLog(boolean enableLog) {
        printLog = enableLog;
    }

    public static String getMetricsDomain() {
        return metricsDomain;
    }

    public static boolean isEnablePrintLog() {
        return printLog;
    }
}

