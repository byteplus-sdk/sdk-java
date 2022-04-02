package byteplus.sdk.core;

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
        String urlTag = "url:" + url;
        Latency(buildLatencyKey(metricsPrefix), begin, urlTag);
        Counter(buildCountKey(metricsPrefix), 1, urlTag);
    }

    //report fail request
    public static void reportRequestError(String metricsPrefix, String url, long begin, int code, String message) {
        String[] tagKvs = {"url:" + url, "code:" + code, "message:" + message};
        Latency(buildLatencyKey(metricsPrefix), begin, tagKvs);
        Counter(buildCountKey(metricsPrefix), 1, tagKvs);
    }

    // report exception
    public static void reportRequestException(String metricsPrefix, String url, long begin, Throwable e) {
        String urlTag = "url:" + url;
        exception(metricsPrefix, begin, e, urlTag);
    }

    public static void exception(String metricsPrefix, long begin, Throwable e, String... tagKvs) {
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
        String[] newTagKvs = new String[tagKvs.length + 1];
        System.arraycopy(tagKvs, 0, newTagKvs, 0, tagKvs.length);
        newTagKvs[tagKvs.length] = msgTag;
        Latency(buildLatencyKey(metricsPrefix), begin, newTagKvs);
        Counter(buildCountKey(metricsPrefix), 1, newTagKvs);
    }

    public static String buildCountKey(String metricsPrefix) {
        return metricsPrefix + "." + "count";
    }

    public static String buildLatencyKey(String metricsPrefix) {
        return metricsPrefix + "." + "latency";
    }
}
