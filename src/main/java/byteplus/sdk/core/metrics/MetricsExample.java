package byteplus.sdk.core.metrics;


import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static byteplus.sdk.core.metrics.Metrics.*;

public class MetricsExample {
    static int times = 1500;

    static {
        metricsInit();
    }

    public static void metricsInit() {
        MetricsCollector.Init(
                MetricsOption.withMetricsHttpSchema("http"),
                MetricsOption.enableMetrics(),
                MetricsOption.enableMetricsLog(),
                MetricsOption.withReportInterval(Duration.ofSeconds(5)),
                MetricsOption.withMetricsPrefix("test.byteplus.sdk"),
                MetricsOption.withMetricsTimeout(Duration.ofSeconds(1)), //metrics http request timeout
                MetricsOption.withMetricsDomain("10.244.245.79:9235") //sg metrics domain
        );
    }

    // test demo for store report
    public static void storeReport() {
        System.out.println("start store reporting...");
        for (int i = 0; i < times; i++) {
            store("java.request.store", 200, "type:test_metrics1","url:https://asfwe.sds.com/test?qu1=xxx&qu2=yyy","error:");
            store("java.request.store", 100, "type:test_metrics2","url:https://asfwe.sds.com/test?qu1=xxx&qu2=yyy","error");
            store("java.request.store", 200, "type:test_metrics3","url:https://asfwe.sds.com/test?qu1=xxx&qu2=yyy","error:");
            store("java.request.store", 100, "type:test_metrics4","url:https://asfwe.sds.com/test?qu1<eq>xxx&qu2<eq>yyy","error");
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("stop store reporting");
    }

    // test demo for counter report
    public static void counterReport() {
        System.out.println("start counter reporting...");
        for (int i = 0; i < times; i++) {
            counter("java.request.counter", 1, "type:test_counter1");
            counter("java.request.counter", 1, "type:test_counter2");
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("stop counter reporting");
    }

    // test demo for timer report
    public static void timerReport() {
        System.out.println("start timer reporting...");
        for (int i = 0; i < times; i++) {
            long begin = System.currentTimeMillis();
            try {
                TimeUnit.MILLISECONDS.sleep(100);
                latency("java.request.timer", begin, "type:test_timer1");
                begin = System.currentTimeMillis();
                TimeUnit.MILLISECONDS.sleep(150);
                latency("java.request.timer", begin, "type:test_timer2");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        System.out.println("stop timer reporting");
    }

    public static void main(String[] args) {
//        counterReport();
//        storeReport();
//        timerReport();
        MetricsLog.info(String.valueOf(UUID.randomUUID()), "this is a test log: %d", 2);
    }
}
