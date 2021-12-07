package byteplus.sdk.core.metrics;

import lombok.Data;

import java.util.Map;

@Data
public class Request<T> {
    private String metric;

    private long timestamp;

    private T value;

    private Map<String, String> tags;
}
