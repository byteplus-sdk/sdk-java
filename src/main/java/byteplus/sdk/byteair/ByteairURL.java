package byteplus.sdk.byteair;

import byteplus.sdk.common.CommonURL;
import byteplus.sdk.core.Context;
import byteplus.sdk.core.URLCenter;
import lombok.Getter;

@Getter
public class ByteairURL extends CommonURL implements URLCenter {
    // The URL template of "predict" request, which need fill with "scene" info when use
    // Example: https://byteair-api-cn1.snssdk.com/predict/api/20013144/home
    private final static String PREDICT_URL_FORMAT = "%s://%s/predict/api/%s/{}";

    // The URL format of reporting the real exposure list
    // Example: https://byteair-api-cn1.snssdk.com/predict/api/20013144/callback
    private final static String CALLBACK_URL_FORMAT = "%s://%s/predict/api/%s/callback";

    // The URL format of data uploading
    // Example: https://byteair-api-cn1.snssdk.com/data/api/20013144/user?method=write
    private final static String UPLOAD_URL_FORMAT = "%s://%s/data/api/%s/{}?method=%s";

    // The URL format of marking a whole day data has been imported completely
    // Example: https://byteair-api-cn1.snssdk.com/data/api/20013144/done?topic=user
    private final static String DONE_URL_FORMAT = "%s://%s/data/api/%s/done?topic={}";

    // The URL template of "predict" request, which need fill with "scene" info when use
    // Example: https://byteair-api-cn1.snssdk.com/predict/api/20013144/home
    private volatile String predictUrlFormat;

    // The URL of reporting the real exposure list
    // Example: https://byteair-api-cn1.snssdk.com/predict/api/20013144/callback
    private volatile String callbackUrl;

    // The URL of uploading real-time user data
    // Example: https://byteair-api-cn1.snssdk.com/data/api/20013144/user?method=write
    private volatile String writeDataUrlFormat;

    // The URL of importing batch offline user data
    // Example: https://byteair-api-cn1.snssdk.com/data/api/20013144/user?method=import
    private volatile String importDataUrlFormat;

    // The URL format of marking a whole day data has been imported completely
    // Example: https://byteair-api-cn1.snssdk.com/data/api/20013144/done?topic=user
    private volatile String doneUrlFormat;

    public ByteairURL(Context context) {
        super(context);
        refresh(context.getHosts().get(0));
    }

    @Override
    public void refresh(String host) {
        super.refresh(host);
        predictUrlFormat = String.format(PREDICT_URL_FORMAT, schema, host, tenant);
        callbackUrl = String.format(CALLBACK_URL_FORMAT, schema, host, tenant);
        writeDataUrlFormat = String.format(UPLOAD_URL_FORMAT, schema, host, tenant, "write");
        importDataUrlFormat = String.format(UPLOAD_URL_FORMAT, schema, host, tenant, "import");
        doneUrlFormat = String.format(DONE_URL_FORMAT, schema, host, tenant);
    }
}
