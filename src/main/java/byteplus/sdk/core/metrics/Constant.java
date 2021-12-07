package byteplus.sdk.core.metrics;

import okhttp3.Protocol;

import java.util.Arrays;
import java.util.List;

public class Constant {
    // metrics default domain and prefix
    public static final String DEFAULT_METRICS_DOMAIN = "bot.snssdk.com";
    public static final String DEFAULT_METRICS_PREFIX = "byteplus.rec.sdk";

    public static final String COUNTER_URL_FORMAT = "http://{}/api/counter";
    public static final String OTHER_URL_FORMAT = "http://{}/api/put";

    // metric max flush size
    public static final int MAX_FLUSH_SIZE = 65536 * 2;
    // metrics expire time
    public static final long DEFAULT_METRICS_EXPIRE_TIME_MS = 12 * 60 * 60 * 1000L;
    //default tidy interval
    public static final int DEFAULT_TIDY_INTERVAL = 100 * 1000;
    // metrics flush interval
    public static final int DEFAULT_FLUSH_MS = 15 * 1000;

    // metric http protocList
    public static final List<Protocol> PROTOCOL_LIST = Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1);

    enum MetricsType {
        metricsTypeCounter, metricsTypeTimer, metricsTypeStore
    }
}
