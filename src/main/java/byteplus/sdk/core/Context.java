package byteplus.sdk.core;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
public class Context {

    // A unique token assigned by bytedance, which is used to
    // generate an authenticated signature when building a request.
    // It is sometimes called "secret".
    private final String token;

    // A unique token assigned by bytedance, which is used to
    // generate an authenticated signature when building a request.
    // It is sometimes called "secret".
    private final String tenantId;

    // A unique identity assigned by Bytedance, which is need to fill in URL.
    // It is sometimes called "company".
    private final String tenant;

    // Schema of URL, server supports both "HTTPS" and "HTTP",
    // in order to ensure communication security, please use "HTTPS"
    private String schema = "https";

    // Server address, china use "rec-b.volcengineapi.com",
    // other area use "tob.sgsnssdk.com" in default
    private List<String> hosts;

    // Customer-defined http headers, all requests will include these headers
    private Map<String, String> customerHeaders = Collections.emptyMap();

    @Slf4j
    @Accessors(chain = true)
    @Setter
    public static class Param {
        private String tenant;

        private String tenantId;

        private String token;

        private String schema;

        private List<String> hosts;

        private Map<String, String> headers;

        private Region region;
    }

    public Context(Param param) {
        checkRequiredField(param);
        this.tenant = param.tenant;
        this.tenantId = param.tenantId;
        this.token = param.token;
        fillHosts(param);

        if (Objects.nonNull(param.schema)) {
            this.schema = param.schema;
        }
        if (Objects.nonNull(param.headers)) {
            this.customerHeaders = param.headers;
        }
    }

    private void checkRequiredField(Param param) {
        if (Objects.isNull(param.tenant)) {
            throw new RuntimeException("Tenant is null");
        }
        if (Objects.isNull(param.tenantId)) {
            throw new RuntimeException("Tenant id is null");
        }
        if (Objects.isNull(param.token)) {
            throw new RuntimeException("Token is null");
        }
        if (Objects.isNull(param.region)) {
            throw new RuntimeException("Region is null");
        }
    }

    private void fillHosts(Param param) {
        if (Objects.nonNull(param.hosts) && !param.hosts.isEmpty()) {
            this.hosts = param.hosts;
            return;
        }
        if (param.region == Region.CN) {
            hosts = Constant.CN_HOSTS;
            return;
        }
        if (param.region == Region.US) {
            hosts = Constant.US_HOSTS;
            return;
        }
        if (param.region == Region.SG) {
            hosts = Constant.AIR_HOSTS;
            return;
        }
        if (param.region == Region.AIR) {
            hosts = Constant.AIR_HOSTS;
        }
    }
}
