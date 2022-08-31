package byteplus.sdk.core;

import java.util.Objects;


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

    // The recommended platform only supports the following strings.
    // ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.-_/:
    // If there are ?, & and = in the query, replace them all
    public static String escapeMetricsTagValue(String value) {
        value = value.replace("?", "-qu-");
        value = value.replace("&", "-and-");
        value = value.replace("=", "-eq-");
        return value;
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
