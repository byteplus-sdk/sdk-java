package byteplus.sdk.core;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Slf4j
public class HttpCaller {
    // The http request was executed successfully without any net exception
    private final static int SUCCESS_HTTP_CODE = 200;

    private final static OkHttpClient defaultHttpCli = new OkHttpClient.Builder().build();

    private volatile static Map<Duration, OkHttpClient> timeoutHttpCliMap = new HashMap<>();

    private final Context context;

    public HttpCaller(Context context) {
        this.context = context;
    }

    public <Rsp extends Message, Req extends Message> Rsp doRequest(
            String url,
            Req request,
            Parser<Rsp> rspParser,
            Options.Filler... optFillers) throws NetException, BizException {

        byte[] bodyBytes = gzipCompress(request.toByteArray());
        Options options = conv2Options(optFillers);
        Headers headers = buildHeaders(options, bodyBytes);
        byte[] rspBytes = doHttpRequest(url, headers, bodyBytes, options.getTimeout());
        try {
            return rspParser.parseFrom(rspBytes);
        } catch (InvalidProtocolBufferException e) {
            log.error("[ByteplusSDK]parse response bytes fail, parser:{} err:{}", rspParser, e.getMessage());
            throw new BizException("parse response fail");
        }
    }

    private Options conv2Options(Options.Filler[] fillers) {
        Options options = new Options();
        if (Objects.isNull(fillers) || fillers.length == 0) {
            return options;
        }
        for (Options.Filler filler : fillers) {
            filler.Fill(options);
        }
        return options;
    }

    private byte[] doHttpRequest(String url,
                         Headers headers,
                         byte[] bodyBytes,
                         Duration timeout) throws NetException {

//        log.debug("[ByteplusSDK][HTTPCaller] URL:{} Request Headers:\n{}", url, headers);
        Request request = new Request.Builder()
                .url(url)
                .headers(headers)
                .post(RequestBody.create(bodyBytes))
                .build();
        Call call = selectHttpClient(timeout).newCall(request);
        LocalDateTime startTime = LocalDateTime.now();
        try {
            Response response = call.execute();
            ResponseBody rspBody = response.body();
            if (response.code() != SUCCESS_HTTP_CODE) {
                if (Objects.nonNull(rspBody)) {
                    log.error("[ByteplusSDK][HTTPCaller] http status not 200, url:{} code:{} msg:{} body:\n{}",
                            url, response.code(), response.message(), rspBody.string());
                } else {
                    log.error("[ByteplusSDK][HTTPCaller] http status not 200, url:{} code:{} msg:{}",
                            url, response.code(), response.message());
                }
                throw new NetException(response.code());
            }
//            log.debug("[ByteplusSDK][HTTPCaller] URL:{} Response Headers:\n{}", url, response.headers());
            if (Objects.isNull(rspBody)) {
                return null;
            }
            if (!Objects.equals(response.header("Content-Encoding"), "gzip")) {
                return rspBody.bytes();
            }
            return gzipDecompress(rspBody.bytes());
        } catch (IOException e) {
            log.error("[ByteplusSDK][HTTPCaller] do http request occur io exception, msg:{}", e.getMessage());
            throw new NetException(e.getMessage());
        } finally {
            log.debug("[ByteplusSDK][HTTPCaller] path:{}, cost:{}ms",
                    url, Duration.between(startTime, LocalDateTime.now()).toMillis());
        }
    }

    private byte[] gzipCompress(byte[] bodyBytes) {
        if (bodyBytes == null || bodyBytes.length == 0) {
            return new byte[0];
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(bodyBytes);
            gzip.finish();
            gzip.close();
        } catch (IOException e) {
            log.error("[ByteplusSDK] gzip compress http request bytes error {}", e.getMessage());
        }
        return out.toByteArray();
    }

    private byte[] gzipDecompress(byte[] bodyBytes) {
        if (bodyBytes == null || bodyBytes.length == 0) {
            return new byte[0];
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bodyBytes);
        try {
            GZIPInputStream ungzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = ungzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
        } catch (Exception e) {
            log.error("[ByteplusSDK] gzip decompress http response bytes error, msg: {}", e.getMessage());
        }

        return out.toByteArray();
    }

    private Headers buildHeaders(Options options, byte[] bodyBytes) {
        Headers.Builder builder;
        if (Objects.isNull(options.getHeaders())) {
            builder = new Headers.Builder();
        } else {
            builder = options.getHeaders().newBuilder();
        }
        builder.set("Content-Encoding", "gzip");
        builder.set("Accept-Encoding", "gzip");
        builder.set("Content-Type", "application/x-protobuf");
        builder.set("Accept", "application/x-protobuf");
        if (Objects.isNull(options.getRequestId())) {
            builder.set("Request-Id", UUID.randomUUID().toString());
        } else {
            builder.set("Request-Id", options.getRequestId());
        }
        withAuthHeader(builder, bodyBytes);
        return builder.build();
    }

    private void withAuthHeader(Headers.Builder headerBuilder, byte[] httpBody) {
        // Gets the second-level timestamp of the current time.
        // The server only supports the second-level timestamp.
        // The 'ts' must be the current time.
        // When current time exceeds a certain time, such as 5 seconds, of 'ts',
        // the signature will be invalid and cannot pass authentication
        String ts = "" + (System.currentTimeMillis() / 1000);
        // Use sub string of UUID as "nonce",  too long will be wasted.
        // You can also use 'ts' as' nonce'
        String nonce = UUID.randomUUID().toString().substring(0, 8);
        // calculate the authentication signature
        String signature = calSignature(httpBody, ts, nonce);

        headerBuilder.add("Tenant-Id", context.getTenantId());
        headerBuilder.add("Tenant-Ts", ts);
        headerBuilder.add("Tenant-Nonce", nonce);
        headerBuilder.add("Tenant-Signature", signature);
    }

    private String calSignature(byte[] httpBody, String ts, String nonce) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ignored) {
            return "";
        }
        // Splice in the order of "token", "HttpBody", "tenant_id", "ts", and "nonce".
        // The order must not be mistaken.
        // String need to be encoded as byte arrays by UTF-8
        digest.update(context.getToken().getBytes(StandardCharsets.UTF_8));
        digest.update(httpBody);
        digest.update(context.getTenantId().getBytes(StandardCharsets.UTF_8));
        digest.update(ts.getBytes(StandardCharsets.UTF_8));
        digest.update(nonce.getBytes(StandardCharsets.UTF_8));

        return bytes2Hex(digest.digest());
    }

    private String bytes2Hex(byte[] bts) {
        StringBuilder sb = new StringBuilder();
        String hex;
        for (byte bt : bts) {
            hex = (Integer.toHexString(bt & 0xff));
            if (hex.length() == 1) {
                sb.append("0");
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    private OkHttpClient selectHttpClient(Duration timeout) {
        if (Objects.isNull(timeout) || timeout.isZero()) {
            return defaultHttpCli;
        }
        OkHttpClient httpClient = timeoutHttpCliMap.get(timeout);
        if (Objects.nonNull(httpClient)) {
            return httpClient;
        }
        synchronized (HttpCaller.class) {
            // 二次检查，防止并发导致重复进入
            httpClient = timeoutHttpCliMap.get(timeout);
            if (Objects.nonNull(httpClient)) {
                return httpClient;
            }
            httpClient = new OkHttpClient.Builder()
                    .callTimeout(timeout)
                    .build();
            // 使用ab替换，减少加锁操作
            Map<Duration, OkHttpClient> timeoutHttpCliMapTemp = new HashMap<>(timeoutHttpCliMap.size());
            timeoutHttpCliMapTemp.putAll(timeoutHttpCliMap);
            timeoutHttpCliMapTemp.put(timeout, httpClient);
            timeoutHttpCliMap = timeoutHttpCliMapTemp;
            return httpClient;
        }
    }
}
