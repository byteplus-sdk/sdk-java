package byteplus.sdk.core.metrics;


import com.codahale.metrics.Metric;

import java.util.Map;

public interface Metrics extends Metric {

    String getName();

    boolean isExpired();

    void updateExpireTime(long ttlInMs);

    void flush();

    void emit(Double value, Map<String, String> tags);
}

