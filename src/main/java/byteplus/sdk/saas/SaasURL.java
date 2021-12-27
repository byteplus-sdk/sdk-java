package byteplus.sdk.saas;

import byteplus.sdk.common.CommonURL;
import byteplus.sdk.core.Context;
import byteplus.sdk.core.URLCenter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public final class SaasURL extends CommonURL implements URLCenter {
    // The URL template of "predict" request.
    // Example: https://rec-api-sg1.recplusapi.com/RetailSaaS/Predict
    private final static String PREDICT_URL_FORMAT = "%s://%s/RetailSaaS/Predict";

    // The URL format of reporting the real exposure list
    // Example: https://rec-api-sg1.recplusapi.com/RetailSaaS/AckServerImpressions
    private final static String ACK_IMPRESSION_URL_FORMAT = "%s://%s/RetailSaaS/AckServerImpressions";

    // The URL format of data uploading
    // Example: https://rec-api-sg1.recplusapi.com/RetailSaaS/WriteUsers
    private final static String UPLOAD_URL_FORMAT = "%s://%s/RetailSaaS/%s";

    // The URL of "predict" request
    // Example: https://rec-api-sg1.recplusapi.com/RetailSaaS/Predict
    private volatile String predictUrl;

    // The URL of reporting the real exposure list
    // Example: https://rec-api-sg1.recplusapi.com/RetailSaaS/AckServerImpressions
    private volatile String ackImpressionUrl;

    // The URL of uploading real-time user data
    // Example: https://rec-api-sg1.recplusapi.com/RetailSaaS/WriteUsers
    private volatile String writeUsersUrl;

    // The URL of uploading real-time product data
    // Example: https://rec-api-sg1.recplusapi.com/RetailSaaS/WriteProducts
    private volatile String writeProductsUrl;

    // The URL of uploading real-time user event data
    // Example: https://rec-api-sg1.recplusapi.com/RetailSaaS/WriteUserEvents
    private volatile String writeUserEventUrl;


    public SaasURL(Context context) {
        super(context);
        refresh(context.getHosts().get(0));
    }

    @Override
    public void refresh(String host) {
        super.refresh(host);
        predictUrl = String.format(PREDICT_URL_FORMAT, schema, host);
        ackImpressionUrl = String.format(ACK_IMPRESSION_URL_FORMAT, schema, host);
        writeUsersUrl = String.format(UPLOAD_URL_FORMAT, schema, host, "WriteUsers");
        writeProductsUrl = String.format(UPLOAD_URL_FORMAT, schema, host, "WriteProducts");
        writeUserEventUrl = String.format(UPLOAD_URL_FORMAT, schema, host, "WriteUserEvents");
    }
}
