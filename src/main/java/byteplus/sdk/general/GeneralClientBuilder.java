package byteplus.sdk.general;

import byteplus.sdk.core.Context;
import byteplus.sdk.core.Region;

import java.util.List;
import java.util.Map;

public class GeneralClientBuilder {
    private final Context.Param param;

    public GeneralClientBuilder() {
        this.param = new Context.Param();
    }

    public GeneralClientBuilder tenant(String tenantName) {
        this.param.setTenant(tenantName);
        return this;
    }

    public GeneralClientBuilder tenantId(String tenantId) {
        this.param.setTenantId(tenantId);
        return this;
    }

    public GeneralClientBuilder token(String token) {
        this.param.setToken(token);
        return this;
    }

    public GeneralClientBuilder schema(String schema) {
        this.param.setSchema(schema);
        return this;
    }

    public GeneralClientBuilder hosts(List<String> hosts) {
        this.param.setHosts(hosts);
        return this;
    }

    public GeneralClientBuilder headers(Map<String, String> headers) {
        this.param.setHeaders(headers);
        return this;
    }

    public GeneralClientBuilder region(Region region) {
        this.param.setRegion(region);
        return this;
    }

    public GeneralClient build() {
        // Except for air, all other environments default to airAuth
        this.param.setUseAirAuth(true);
        return new GeneralClientImpl(this.param);
    }
}
