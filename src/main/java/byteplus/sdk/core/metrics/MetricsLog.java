package byteplus.sdk.core.metrics;

public class MetricsLog {
    public static void trace(String logID, String format, Object... args) {
        String message = String.format(format, args);
        MetricsCollector.emitLog(logID, message, Constant.LOG_LEVEL_TRACE, System.currentTimeMillis());
    }

    public static void debug(String logID, String format, Object... args) {
        String message = String.format(format, args);
        MetricsCollector.emitLog(logID, message, Constant.LOG_LEVEL_DEBUG, System.currentTimeMillis());
    }

    public static void info(String logID, String format, Object... args) {
        String message = String.format(format, args);
        MetricsCollector.emitLog(logID, message, Constant.LOG_LEVEL_INFO, System.currentTimeMillis());
    }

    public static void notice(String logID, String format, Object... args) {
        String message = String.format(format, args);
        MetricsCollector.emitLog(logID, message, Constant.LOG_LEVEL_NOTICE, System.currentTimeMillis());
    }

    public static void warn(String logID, String format, Object... args) {
        String message = String.format(format, args);
        MetricsCollector.emitLog(logID, message, Constant.LOG_LEVEL_WARN, System.currentTimeMillis());
    }

    public static void error(String logID, String format, Object... args) {
        String message = String.format(format, args);
        MetricsCollector.emitLog(logID, message, Constant.LOG_LEVEL_ERROR, System.currentTimeMillis());
    }

    public static void fatal(String logID, String format, Object... args) {
        String message = String.format(format, args);
        MetricsCollector.emitLog(logID, message, Constant.LOG_LEVEL_FATAL, System.currentTimeMillis());
    }
}
