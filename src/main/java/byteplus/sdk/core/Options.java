package byteplus.sdk.core;

import lombok.Data;
import okhttp3.Headers;

import java.time.Duration;

@Data
class Options {
    private Duration timeout;

    private String RequestId;

    private Headers headers;
}
