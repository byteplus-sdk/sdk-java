package byteplus.sdk.core;

import java.util.Objects;

import static byteplus.sdk.core.metrics.Helper.Counter;
import static byteplus.sdk.core.metrics.Helper.Latency;


public final class Helper {
    public static String bytes2Hex(byte[] bts) {
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

    //report success request
    public static void reportRequestSuccess(String metricsPrefix, String url, long begin) {
        String[] urlTag = buildUrlTags(url);
        Latency(buildLatencyKey(metricsPrefix), begin, urlTag);
        Counter(buildCountKey(metricsPrefix), 1, urlTag);
    }

    //report fail request
    public static void reportRequestError(String metricsPrefix, String url, long begin, int code, String message) {
        String[] urlTag = buildUrlTags(url);
        String[] tagKvs = appendTags(urlTag, "code:" + code, "message:" + message);
        Latency(buildLatencyKey(metricsPrefix), begin, tagKvs);
        Counter(buildCountKey(metricsPrefix), 1, tagKvs);
    }

    // report exception
    public static void reportRequestException(String metricsPrefix, String url, long begin, Throwable e) {
        String[] tagKvs = withExceptionTags(buildUrlTags(url), e);
        Latency(buildLatencyKey(metricsPrefix), begin, tagKvs);
        Counter(buildCountKey(metricsPrefix), 1, tagKvs);
    }

    public static String[] withExceptionTags(String[] tagKvs, Throwable e) {
        String msgTag;
        String msg = e.getMessage().toLowerCase();
        if (msg.contains("time") && msg.contains("out")) {
            if (msg.contains("connect")) {
                msgTag = "message:connect-timeout";
            } else if (msg.contains("read")) {
                msgTag = "message:read-timeout";
            } else {
                msgTag = "message:timeout";
            }
        } else {
            msgTag = "message:other";
        }
        return appendTags(tagKvs, msgTag);
    }


    private static String[] buildUrlTags(String url) {
        return new String[]{"url:" + adjustUrlTag(url), "req_type:" + parseReqType(url)};
    }

    /**
     * Parsing the url tag, replace the "=" in the url with "_is_",
     * because "=" is a special delimiter in metrics server
     * For example, url=http://xxxx?query=yyy, the direct report will fail,
     * instead, using url=http://xxxx?query_is_yyy.
     *
     * @param url full request url
     */
    private static String adjustUrlTag(String url) {
        return url.replaceAll("=", "_is_");
    }

    private static String[] appendTags(String[] oldTags, String... tags) {
        if (Objects.isNull(tags) || tags.length == 0) {
            return oldTags;
        }
        String[] newTags = new String[oldTags.length + tags.length];
        System.arraycopy(oldTags, 0, newTags, 0, oldTags.length);
        System.arraycopy(tags, 0, newTags, oldTags.length, tags.length);
        return newTags;
    }

    private static String parseReqType(String url) {
        if (url.contains("ping")) {
            return "ping";
        }
        if (url.contains("data/api")) {
            return "data-api";
        }
        if (url.contains("predict/api")) {
            return "predict-api";
        }
        return "unknown";
    }

    public static String buildCountKey(String metricsPrefix) {
        return metricsPrefix + "." + "count";
    }

    public static String buildLatencyKey(String metricsPrefix) {
        return metricsPrefix + "." + "latency";
    }
}
