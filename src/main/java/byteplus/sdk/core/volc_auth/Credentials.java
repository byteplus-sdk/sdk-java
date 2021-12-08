package byteplus.sdk.core.volc_auth;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class Credentials {
    private String accessKeyID;
    private String secretAccessKey;
    private String service;
    private String region;
    private String sessionToken;

    public Credentials(String ak, String sk, String service, String region) {
        this.accessKeyID = ak;
        this.secretAccessKey = sk;
        this.region = region;
        this.service = service;
    }
}
