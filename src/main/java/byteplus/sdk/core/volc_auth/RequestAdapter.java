package byteplus.sdk.core.volc_auth;

import lombok.Getter;
import okhttp3.Headers;
import okhttp3.Request;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ByteArrayEntity;

@Getter
public class RequestAdapter {
    private final HttpUriRequest authRequest;

    public RequestAdapter(String url, Headers headers, byte[] bodyBytes) {
        this.authRequest = RequestBuilder.post().setUri(url)
                .setEntity(new ByteArrayEntity(bodyBytes)).build();
        for (int i=0;i<headers.size();i++) {
            String headerName = headers.name(i);
            String headerValue = headers.get(headerName);
            this.authRequest.addHeader(headerName, headerValue);
        }
    }

    // copy apache httpRequest headers to OkHttpRequest
    public void copyHeaders(Request.Builder request) {
        HeaderIterator headers = this.authRequest.headerIterator();
        while (headers.hasNext()) {
            Header header = headers.nextHeader();
            request.header(header.getName(), header.getValue());
        }
    }
}
