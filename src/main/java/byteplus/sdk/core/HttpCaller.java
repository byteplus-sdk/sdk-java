package byteplus.sdk.core;

import byteplus.sdk.core.volcAuth.VoclAuth;
import com.alibaba.fastjson.JSON;
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
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    public <Rsp extends Message, Req extends Message> Rsp doPbRequest(
            String url,
            Req request,
            Parser<Rsp> rspParser,
            Options options) throws NetException, BizException {
        byte[] reqBytes = request.toByteArray();
        String contentType = "application/x-protobuf";
        return doRequest(url, reqBytes, rspParser, contentType, options);
    }

    public <Rsp extends Message> Rsp doJsonRequest(
            String url,
            Object request,
            Parser<Rsp> rspParser,
            Options options) throws NetException, BizException {
        byte[] reqBytes = JSON.toJSONBytes(request);
        String contentType = "application/json";
        return doRequest(url, reqBytes, rspParser, contentType, options);
    }

    private <Rsp extends Message> Rsp doRequest(String url,
                                                byte[] reqBytes,
                                                Parser<Rsp> rspParser,
                                                String contentType,
                                                Options options) throws NetException, BizException {
        reqBytes = gzipCompress(reqBytes);
        Headers headers = buildHeaders(options, contentType);
        url = buildUrlWithQueries(options, url);
        byte[] rspBytes = doHttpRequest(url, headers, reqBytes, options.getTimeout());

        try {
            return rspParser.parseFrom(rspBytes);
        } catch (InvalidProtocolBufferException e) {
            log.error("[ByteplusSDK]parse response fail, url:{} err:{} ", url, e.getMessage());
            throw new BizException("parse response fail");
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
            return bodyBytes;
        }
        return out.toByteArray();
    }

    private Headers buildHeaders(Options options, String contentType) {
        Headers.Builder builder = new Headers.Builder();
        builder.set("Content-Encoding", "gzip");
        builder.set("Accept-Encoding", "gzip");
        builder.set("Content-Type", contentType);
        builder.set("Accept", "application/x-protobuf"); //response parser only accept pb format
        builder.set("Tenant-Id", context.getTenantId());
        withOptionHeaders(builder, options);
        return builder.build();
    }

    private String buildUrlWithQueries(Options options, String url) {
        Map<String, String> queries = new HashMap<>();
        if (Objects.nonNull(options.getStage())) {
            queries.put("stage", options.getStage());
        }
        if (Objects.nonNull(options.getQueries())) {
            queries.putAll(options.getQueries());
        }
        if (queries.isEmpty()) {
            return url;
        }
        ArrayList<String> queryParts = new ArrayList<>();
        queries.forEach((queryName, queryValue) ->
                queryParts.add(queryName + "=" + queryValue));
        String queryString = String.join("&", queryParts);
        if (url.contains("?")) { //already contains queries
            return url + "&" + queryString;
        } else {
            return url + "?" + queryString;
        }
    }

    private void withOptionHeaders(Headers.Builder builder, Options options) {
        if (Objects.nonNull(options.getHeaders())) {
            options.getHeaders().forEach(builder::set);
        }
        if (Objects.isNull(options.getRequestId())) {
            String requestId = UUID.randomUUID().toString();
            log.info("[ByteplusSDK] use requestId generated by sdk: '{}' ", requestId);
            builder.set("Request-Id", requestId);
        } else {
            builder.set("Request-Id", options.getRequestId());
        }
        if (Objects.nonNull(options.getDataDate())) {
            builder.set("Content-Date", options.getDataDate().format(DateTimeFormatter.ISO_DATE));
        }
        if (Objects.nonNull(options.getDataIsEnd())) {
            builder.set("Content-End", options.getDataIsEnd().toString());
        }
        if (Objects.nonNull(options.getServerTimeout())) {
            builder.set("Timeout-Millis", options.getServerTimeout().toMillis() + "");
        }
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

        return Helper.bytes2Hex(digest.digest());
    }


    private byte[] doHttpRequest(String url,
                                 Headers headers,
                                 byte[] bodyBytes,
                                 Duration timeout) throws NetException, BizException {
        Request request = new Request.Builder()
                .url(url)
                .headers(headers)
                .post(RequestBody.create(bodyBytes))
                .build();
        // append auth headers
        headers = withAuthHeaders(request, bodyBytes);
        log.debug("[ByteplusSDK][HTTPCaller] URL:{} Request Headers:\n{}", url, headers);
        request = request.newBuilder().headers(headers).build();
        Call call = selectHttpClient(timeout).newCall(request);
        LocalDateTime startTime = LocalDateTime.now();
        try {
            Response response = call.execute();
            ResponseBody rspBody = response.body();
            if (response.code() != SUCCESS_HTTP_CODE) {
                logHttpResponse(url, response);
                throw new BizException(response.message());
            }
            if (Objects.isNull(rspBody)) {
                return null;
            }
            String rspEncoding = response.header("Content-Encoding");
            if (Objects.isNull(rspEncoding) || !rspEncoding.contains("gzip")) {
                return rspBody.bytes();
            }
            return gzipDecompress(rspBody.bytes(), url);
        } catch (IOException e) {
            if (e.getMessage().toLowerCase().contains("timeout")) {
                log.error("[ByteplusSDK] do http request timeout, cost:{} msg:{} url:{}",
                        Duration.between(startTime, LocalDateTime.now()).toMillis(), e, url);
                throw new NetException(e.toString());
            }
            log.error("[ByteplusSDK] do http request occur exception, msg:{} url:{}", e, url);
            throw new BizException(e.toString());
        } finally {
            log.debug("[ByteplusSDK] http url:{}, cost:{}ms",
                    url, Duration.between(startTime, LocalDateTime.now()).toMillis());
        }
    }

    private Headers withAuthHeaders(Request request, byte[] bodyBytes) throws BizException {
        //air_auth
        if (context.isUseAirAuth()) {
            Headers originHeaders = request.headers();
            return withAirAuthHeaders(originHeaders, bodyBytes);
        }
        //volc_auth
        try {
            return VoclAuth.sign(request, bodyBytes, context.getVolcCredential());
        } catch (Exception e) {
            throw new BizException(e.getMessage());
        }
    }

    private Headers withAirAuthHeaders(Headers originHeaders, byte[] reqBytes) {
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
        String signature = calSignature(reqBytes, ts, nonce);

        return originHeaders.newBuilder()
                .set("Tenant-Id", context.getTenantId())
                .set("Tenant-Ts", ts)
                .set("Tenant-Nonce", nonce)
                .set("Tenant-Signature", signature)
                .build();
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

    private void logHttpResponse(String url, Response response) throws IOException {
        ResponseBody rspBody = response.body();
        if (Objects.nonNull(rspBody)) {
            log.error("[ByteplusSDK] http status not 200, url:{} code:{} msg:{} headers:\n{} body:\n{}",
                    url, response.code(), response.message(), response.headers(), rspBody.string());
        } else {
            log.error("[ByteplusSDK] http status not 200, url:{} code:{} msg:{} headers:\n{}",
                    url, response.code(), response.message(), response.headers());
        }
    }

    private byte[] gzipDecompress(byte[] bodyBytes, String url) {
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
            log.error("[ByteplusSDK] gzip decompress http response error, msg:{} url:{}",
                    e.getMessage(), url);
        }
        return out.toByteArray();
    }
}
