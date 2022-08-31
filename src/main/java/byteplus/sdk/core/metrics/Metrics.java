package byteplus.sdk.core.metrics;


public class Metrics {
  /**
   * description: Store tagKvs should be formatted as "key:value"
   * example: store("goroutine.count", 400, "ip:127.0.0.1")
   */
  public static void store(String key, long value, String... tagKvs) {
    MetricsCollector.emitMetric(Constant.METRICS_TYPE_STORE, key, value, tagKvs);
  }

  /**
   * description: Store tagKvs should be formatted as "key:value"
   * example: counter("request.count", 1, "method:user", "type:upload")
   */
  public static void counter(String key, long value, String... tagKvs) {
    MetricsCollector.emitMetric(Constant.METRICS_TYPE_COUNTER, key, value, tagKvs);
  }

  /**
   * @param value :The unit of `value` is milliseconds
   *              example: timer("request.cost", 100, "method:user", "type:upload")
   *              description: Store tagKvs should be formatted as "key:value"
   */
  public static void timer(String key, long value, String... tagKvs) {
    MetricsCollector.emitMetric(Constant.METRICS_TYPE_TIMER, key, value, tagKvs);
  }

  /**
   * @param begin :The unit of `begin` is milliseconds
   *              example: latency("request.latency", startTime, "method:user", "type:upload")
   *              description: Store tagKvs should be formatted as "key:value"
   */
  public static void latency(String key, long begin, String... tagKvs) {
    MetricsCollector.emitMetric(Constant.METRICS_TYPE_TIMER, key, System.currentTimeMillis() - begin, tagKvs);
  }

  /**
   * description: Store tagKvs should be formatted as "key:value"
   * example: rateCounter("request.count", 1, "method:user", "type:upload")
   */
  public static void rateCounter(String key, long value, String... tagKvs) {
    MetricsCollector.emitMetric(Constant.METRICS_TYPE_RATE_COUNTER, key, value, tagKvs);
  }

  /**
   * description:
   *  - meter(xx) = counter(xx) + rateCounter(xx.rate)
   *  - Store tagKvs should be formatted as "key:value"
   * example: rateCounter("request.count", 1, "method:user", "type:upload")
   */
  public static void meter(String key, long value, String... tagKvs) {
    MetricsCollector.emitMetric(Constant.METRICS_TYPE_METER, key, value, tagKvs);
  }
}
