package byteplus.sdk.core;

import lombok.Data;
import okhttp3.Headers;

import java.time.Duration;
import java.time.LocalDate;

@Data
class Options {
    private Duration timeout;

    private String RequestId;

    private Headers headers;

    private LocalDate dataDate;

    private Boolean dataIsEnd;

    private Duration serverTimeout;
}
