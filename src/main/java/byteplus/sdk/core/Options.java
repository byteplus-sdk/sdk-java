package byteplus.sdk.core;

import lombok.Data;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;

@Data
class Options {
    private Duration timeout;

    private String RequestId;

    private Map<String, String> headers;

    private LocalDate dataDate;

    private Boolean dataIsEnd;

    private Duration serverTimeout;
}
