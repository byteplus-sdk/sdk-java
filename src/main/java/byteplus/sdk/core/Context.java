package byteplus.sdk.core;

import lombok.Data;
import lombok.Getter;
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

    // A unique ID assigned by Bytedance, which is used to
    // generate an authenticated signature when building a request
    // It is sometimes called "appkey".
    private final String tenantId;

    // A unique identity assigned by Bytedance, which is need to fill in URL.
    // It is sometimes called "company".
    private final String tenant;

    // Schema of URL, server supports both "HTTPS" and "HTTP",
    // in order to ensure communication security, please use "HTTPS"
    private String schema = "https";

    // Server address, china use "rec-b.volcengineapi.com",
    // other area use "tob.sgsnssdk.com"
    private List<String> hosts;

    private Map<String, String> customerHeaders = Collections.emptyMap();

    @Slf4j
    @Accessors(chain = true)
    @Data
    public static class Param {
        private String tenant;

        private String tenantId;

        private String token;

        private int retryTimes;

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
        initHosts(param.region);
        if (Objects.nonNull(param.schema)) {
            this.schema = param.schema;
        }
        if (Objects.nonNull(param.hosts) && !param.hosts.isEmpty()) {
            this.hosts = param.hosts;
        }
        if (Objects.nonNull(param.headers) && !param.headers.isEmpty()) {
            this.customerHeaders = param.headers;
        }
    }

    private void checkRequiredField(Param param) {
        if (Objects.isNull(param.getTenant())) {
            throw new RuntimeException("Tenant is null");
        }
        if (Objects.isNull(param.getTenantId())) {
            throw new RuntimeException("Tenant id is null");
        }
        if (Objects.isNull((param.getToken()))) {
            throw new RuntimeException("Token is null");
        }
        if (Objects.isNull(param.region)) {
            throw new RuntimeException("Area is null");
        }
    }

    private void initHosts(Region region) {
        if (region == Region.CN) {
            hosts = Constant.CNHosts;
        } else {
            hosts = Constant.SGHosts;
        }
    }
}
