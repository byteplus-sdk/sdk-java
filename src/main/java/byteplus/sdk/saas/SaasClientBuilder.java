package byteplus.sdk.saas;

import byteplus.sdk.core.Context.Param;
import byteplus.sdk.core.Region;

import java.util.List;
import java.util.Map;

public final class SaasClientBuilder {
    private final Param param;

    public SaasClientBuilder() {
        this.param = new Param();
    }

    public SaasClientBuilder ak(String ak) {
        this.param.setAk(ak);
        return this;
    }

    public SaasClientBuilder sk(String sk) {
        this.param.setSk(sk);
        return this;
    }

    public SaasClientBuilder tenantId(String tenantId) {
        this.param.setTenantId(tenantId);
        return this;
    }

    public SaasClientBuilder token(String token) {
        this.param.setToken(token);
        return this;
    }

    public SaasClientBuilder schema(String schema) {
        this.param.setSchema(schema);
        return this;
    }

    public SaasClientBuilder hosts(List<String> hosts) {
        this.param.setHosts(hosts);
        return this;
    }

    public SaasClientBuilder headers(Map<String, String> headers) {
        this.param.setHeaders(headers);
        return this;
    }

    public SaasClientBuilder region(Region region) {
        this.param.setRegion(region);
        return this;
    }

    public SaasClient build() {
        return new SaasClientImpl(this.param);
    }
}
