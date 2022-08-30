package byteplus.sdk.retailv2;

import byteplus.sdk.core.HostAvailabler;
import byteplus.sdk.core.Region;
import byteplus.sdk.core.Context.Param;
import byteplus.sdk.core.metrics.MetricsCollector.MetricsCfg;

import java.util.List;
import java.util.Map;

public final class RetailClientBuilder {
    private final Param param;

    public RetailClientBuilder() {
        this.param = new Param();
    }

    public RetailClientBuilder tenant(String tenantName) {
        this.param.setTenant(tenantName);
        return this;
    }

    public RetailClientBuilder tenantId(String tenantId) {
        this.param.setTenantId(tenantId);
        return this;
    }

    public RetailClientBuilder token(String token) {
        this.param.setToken(token);
        return this;
    }

    public RetailClientBuilder schema(String schema) {
        this.param.setSchema(schema);
        return this;
    }

    public RetailClientBuilder hosts(List<String> hosts) {
        this.param.setHosts(hosts);
        return this;
    }

    public RetailClientBuilder headers(Map<String, String> headers) {
        this.param.setHeaders(headers);
        return this;
    }

    public RetailClientBuilder region(Region region) {
        this.param.setRegion(region);
        return this;
    }

    public RetailClientBuilder metricsConfig(MetricsCfg metricsCfg) {
        this.param.setMetricsCfg(metricsCfg);
        return this;
    }

    public RetailClientBuilder hostAvailablerConfig(HostAvailabler.Config config) {
        this.param.setHostAvailablerConfig(config);
        return this;
    }

    public RetailClient build() {
        // Except for air, all other environments default to airAuth
        this.param.setUseAirAuth(true);
        return new RetailClientImpl(this.param);
    }
}
