package byteplus.sdk.common;

import byteplus.sdk.core.Context;
import byteplus.sdk.core.URLCenter;
import lombok.Getter;

@Getter
public class CommonURL implements URLCenter {
    // The URL format of operation information
    // Example: https://tob.sgsnssdk.com/data/api/retail/retail_demo/operation?method=get
    private final static String OPERATION_URL_FORMAT = "%s://%s/data/api/%s/operation?method=%s";

    // The URL of getting operation information which is real-time
    // Example: https://tob.sgsnssdk.com/data/api/retail_demo/operation?method=get
    private String getOperationUrl;

    // The URL of query operations information which is non-real-time
    // Example: https://tob.sgsnssdk.com/data/api/retail_demo/operation?method=list
    private String listOperationsUrl;

    protected String schema;

    protected String tenant;

    protected CommonURL(Context context) {
        this.schema = context.getSchema();
        this.tenant = context.getTenant();
        this.refresh(context.getHosts().get(0));
    }

    @Override
    public void refresh(String host) {
        getOperationUrl = String.format(OPERATION_URL_FORMAT, schema, host, tenant, "get");
        listOperationsUrl = String.format(OPERATION_URL_FORMAT, schema, host, tenant, "list");
    }
}
