package byteplus.sdk.core;

import lombok.Data;
import okhttp3.Headers;

import java.time.Duration;

@Data
public class Options {
    public interface Filler {
        void Fill(Options options);
    }

    private Duration timeout;

    private String RequestId;

    private Headers headers;
}
