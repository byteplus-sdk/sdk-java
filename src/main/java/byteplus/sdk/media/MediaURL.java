package byteplus.sdk.media;

import byteplus.sdk.common.CommonURL;
import byteplus.sdk.core.Context;
import byteplus.sdk.core.URLCenter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public final class MediaURL extends CommonURL implements URLCenter {
    // The URL format of data uploading
    // Example: https://tob.sgsnssdk.com/data/api/media/media_demo/user?method=write
    private final static String UPLOAD_URL_FORMAT = "%s://%s/data/api/media/%s/%s?method=%s";

    // The URL of uploading real-time user data
    // Example: https://tob.sgsnssdk.com/data/api/media/media_demo/user?method=write
    private volatile String writeUsersUrl;

    // The URL of uploading real-time content data
    // Example: https://tob.sgsnssdk.com/data/api/media/media_demo/content?method=write
    private volatile String writeContentsUrl;

    // The URL of uploading real-time user event data
    // Example: https://tob.sgsnssdk.com/data/api/media/media_demo/user_event?method=write
    private volatile String writeUserEventsUrl;

    public MediaURL(Context context) {
        super(context);
        refresh(context.getHosts().get(0));
    }

    @Override
    public void refresh(String host) {
        super.refresh(host);
        writeUsersUrl = String.format(UPLOAD_URL_FORMAT, schema, host, tenant, "user", "write");
        writeContentsUrl = String.format(UPLOAD_URL_FORMAT, schema, host, tenant, "content", "write");
        writeUserEventsUrl = String.format(UPLOAD_URL_FORMAT, schema, host, tenant, "user_event", "write");
    }
}
