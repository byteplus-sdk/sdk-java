package byteplus.sdk.byteair;

import byteplus.sdk.core.Context;
import byteplus.sdk.core.Region;

import java.util.List;
import java.util.Map;

public class ByteairClientBuilder {
    private final Context.Param param;

    public ByteairClientBuilder() {
        this.param = new Context.Param();
    }

    public ByteairClientBuilder projectId(String projectId) {
        this.param.setTenant(projectId);
        return this;
    }

    public ByteairClientBuilder tenantId(String tenantId) {
        this.param.setTenantId(tenantId);
        return this;
    }

    public ByteairClientBuilder token(String token) {
        this.param.setToken(token);
        return this;
    }

    public ByteairClientBuilder schema(String schema) {
        this.param.setSchema(schema);
        return this;
    }

    public ByteairClientBuilder hosts(List<String> hosts) {
        this.param.setHosts(hosts);
        return this;
    }

    public ByteairClientBuilder headers(Map<String, String> headers) {
        this.param.setHeaders(headers);
        return this;
    }

    public ByteairClientBuilder region(Region region) {
        this.param.setRegion(region);
        return this;
    }

    public ByteairClientBuilder ak(String ak) {
        this.param.setAk(ak);
        return this;
    }

    public ByteairClientBuilder sk(String sk) {
        this.param.setSk(sk);
        return this;
    }

    public ByteairClientBuilder useAirAuth() {
        this.param.setUseAirAuth(true);
        return this;
    }

    public ByteairClient build() {
        return new ByteairClientImpl(this.param);
    }
}
