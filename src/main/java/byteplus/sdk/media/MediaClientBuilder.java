package byteplus.sdk.media;

import byteplus.sdk.core.Context;
import byteplus.sdk.core.HostAvailabler;
import byteplus.sdk.core.Region;
import byteplus.sdk.core.metrics.MetricsCollector;

import java.util.List;
import java.util.Map;

public class MediaClientBuilder {
    private final Context.Param param;

    public MediaClientBuilder() {
        this.param = new Context.Param();
    }

    public MediaClientBuilder tenant(String tenantName) {
        this.param.setTenant(tenantName);
        return this;
    }

    public MediaClientBuilder tenantId(String tenantId) {
        this.param.setTenantId(tenantId);
        return this;
    }

    public MediaClientBuilder token(String token) {
        this.param.setToken(token);
        return this;
    }

    public MediaClientBuilder schema(String schema) {
        this.param.setSchema(schema);
        return this;
    }

    public MediaClientBuilder hosts(List<String> hosts) {
        this.param.setHosts(hosts);
        return this;
    }

    public MediaClientBuilder headers(Map<String, String> headers) {
        this.param.setHeaders(headers);
        return this;
    }

    public MediaClientBuilder region(Region region) {
        this.param.setRegion(region);
        return this;
    }

    public MediaClientBuilder metricsConfig(MetricsCollector.MetricsCfg metricsCfg) {
        this.param.setMetricsCfg(metricsCfg);
        return this;
    }

    public MediaClientBuilder hostAvailablerConfig(HostAvailabler.Config config) {
        this.param.setHostAvailablerConfig(config);
        return this;
    }

    public MediaClient build() {
        // Except for air, all other environments default to airAuth
        this.param.setUseAirAuth(true);
        return new MediaClientImpl(this.param);
    }
}
