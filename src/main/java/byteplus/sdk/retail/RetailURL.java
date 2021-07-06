package byteplus.sdk.retail;

import byteplus.sdk.common.CommonURL;
import byteplus.sdk.core.Context;
import byteplus.sdk.core.URLCenter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public final class RetailURL extends CommonURL implements URLCenter {
    // The URL template of "predict" request, which need fill with "scene" info when use
    // Example: https://tob.sgsnssdk.com/predict/api/retail/demo/home
    private final static String PREDICT_URL_FORMAT = "%s://%s/predict/api/retail/%s/{}";

    // The URL format of reporting the real exposure list
    // Example: https://tob.sgsnssdk.com/predict/api/retail/demo/ack_server_impressions
    private final static String ACK_IMPRESSION_URL_FORMAT = "%s://%s/predict/api/retail/%s/ack_server_impressions";

    // The URL format of data uploading
    // Example: https://tob.sgsnssdk.com/data/api/retail/retail_demo/user?method=write
    private final static String UPLOAD_URL_FORMAT = "%s://%s/data/api/retail/%s/%s?method=%s";

    // The URL template of "predict" request, which need fill with "scene" info when use
    // Example: https://tob.sgsnssdk.com/predict/api/retail/demo/home
    private String predictUrlFormat;

    // The URL of reporting the real exposure list
    // Example: https://tob.sgsnssdk.com/predict/api/retail/demo/ack_server_impression
    private String ackImpressionUrl;

    // The URL of uploading real-time user data
    // Example: https://tob.sgsnssdk.com/data/api/retail/retail_demo/user?method=write
    private String writeUsersUrl;
    // The URL of importing daily offline user data
    // Example: https://tob.sgsnssdk.com/data/api/retail/retail_demo/user?method=import
    private String importUsersUrl;

    // The URL of uploading real-time product data
    // Example: https://tob.sgsnssdk.com/data/api/retail/retail_demo/product?method=write
    private String writeProductsUrl;
    // The URL of importing daily offline product data
    // Example: https://tob.sgsnssdk.com/data/api/retail/retail_demo/product?method=import
    private String importProductsUrl;

    // The URL of uploading real-time user event data
    // Example: https://tob.sgsnssdk.com/data/api/retail/retail_demo/user_event?method=write
    private String writeUserEventsUrl;
    // The URL of importing daily offline product data
    // Example: https://tob.sgsnssdk.com/data/api/retail/retail_demo/user_event?method=import
    private String importUserEventsUrl;

    public RetailURL(Context context) {
        super(context);
        refresh(context.getHosts().get(0));
    }

    @Override
    public void refresh(String host) {
        super.refresh(host);
        predictUrlFormat = String.format(PREDICT_URL_FORMAT, schema, host, tenant);
        ackImpressionUrl = String.format(ACK_IMPRESSION_URL_FORMAT, schema, host, tenant);
        writeUsersUrl = String.format(UPLOAD_URL_FORMAT, schema, host, tenant, "user", "write");
        importUsersUrl = String.format(UPLOAD_URL_FORMAT, schema, host, tenant, "user", "import");
        writeProductsUrl = String.format(UPLOAD_URL_FORMAT, schema, host, tenant, "product", "write");
        importProductsUrl = String.format(UPLOAD_URL_FORMAT, schema, host, tenant, "product", "import");
        writeUserEventsUrl = String.format(UPLOAD_URL_FORMAT, schema, host, tenant, "user_event", "write");
        importUserEventsUrl = String.format(UPLOAD_URL_FORMAT, schema, host, tenant, "user_event", "import");
    }
}
