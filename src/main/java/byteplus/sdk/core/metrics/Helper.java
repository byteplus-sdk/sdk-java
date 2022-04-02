package byteplus.sdk.core.metrics;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static byteplus.sdk.core.metrics.Constant.*;

@Slf4j
public class Helper {

    /**
     * description: Store tagKvs should be formatted as "key:value"
     * example: Store("goroutine.count", 400, "ip:127.0.0.1")
     */
    public static void Store(String key, long value, String... tagKvs) {
        MetricsCollector.emitStore(key, value, tagKvs);
    }

    /**
     * description: Store tagKvs should be formatted as "key:value"
     * example: Counter("request.qps", 1, "method:user", "type:upload")
     */
    public static void Counter(String key, long value, String... tagKvs) {
        MetricsCollector.emitCounter(key, value, tagKvs);
    }

    /**
     * @param value :The unit of `value` is milliseconds
     *              example: Timer("request.cost", 100, "method:user", "type:upload")
     *              description: Store tagKvs should be formatted as "key:value"
     */
    public static void Timer(String key, long value, String... tagKvs) {
        MetricsCollector.emitTimer(key, value, tagKvs);
    }

    /**
     * @param begin :The unit of `begin` is milliseconds
     *              example: Latency("request.latency", startTime, "method:user", "type:upload")
     *              description: Store tagKvs should be formatted as "key:value"
     */
    public static void Latency(String key, long begin, String... tagKvs) {
        MetricsCollector.emitTimer(key, System.currentTimeMillis() - begin, tagKvs);
    }


    public static String buildCollectKey(String name, String... tags) {
        return name + DELIMITER + tags2String(tags);
    }

    public static String tags2String(String... tags) {
        Arrays.sort(tags); //todo:测试是否可用
        return String.join("|", Arrays.asList(tags));
    }

    public static List<String> parseNameAndTags(String src) {
        int index = src.indexOf(DELIMITER);
        if (index == -1) {
            return null;
        }
        ArrayList<String> res = new ArrayList<>();
        res.add(src.substring(0, index)); //metrics name as first element
        res.add(src.substring(index + DELIMITER.length()));  //tagString as second element
        return res;
    }

    // recover tagString to origin Tags map
    public static Map<String, String> recoverTags(String tagString) {
        Map<String, String> tags = new HashMap<>();
        for (String entry : tagString.split("\\|")) {
            String[] keyValue = entry.split(":");
            if (keyValue.length != 2) {
                continue;
            }
            tags.put(keyValue[0], keyValue[1]);
        }
        return tags;
    }

}
