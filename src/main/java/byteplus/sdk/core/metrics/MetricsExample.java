package byteplus.sdk.core.metrics;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static byteplus.sdk.core.metrics.Helper.*;

public class MetricsExample {
    static int times = 150000;

    static {
        metricsInit();
    }

    public static void metricsInit() {
        MetricsCollector.Init(
                MetricsOption.withMetricsLog(),
                MetricsOption.withFlushIntervalMs(5 * 1000),
                MetricsOption.withMetricsTimeout(1000), //metrics http request timeout
//                MetricsOption.withMetricsDomain("rec-us-east-1.byteplusapi.com") //us metrics domain
//                MetricsOption.withMetricsDomain("rec-ap-singapore-1.byteplusapi.com") //sg metrics domain
                MetricsOption.withMetricsDomain("bot.snssdk.com") //cn metrics domain
        );
    }

    // test demo for store report
    public static void StoreReport() {
        System.out.println("start store reporting...");
        for (int i = 0; i < times; i++) {
            Store("java.request.store", 200, "type:test_metrics1","url:https://asfwe.sds.com/test?qu1=xxx&qu2=yyy","error:");
            Store("java.request.store", 100, "type:test_metrics2","url:https://asfwe.sds.com/test?qu1=xxx&qu2=yyy","error");
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("stop store reporting");
    }

    // test demo for counter report
    public static void CounterReport() {
        System.out.println("start counter reporting...");
        for (int i = 0; i < times; i++) {
            Counter("java.request.counter", 1, "type:test_metrics1");
            Counter("java.request.counter", 1, "type:test_metrics2");
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("stop counter reporting");
    }

    // test demo for timer report
    public static void TimerReport() {
        System.out.println("start timer reporting...");
        for (int i = 0; i < times; i++) {
            long begin = System.currentTimeMillis();
            try {
                TimeUnit.MILLISECONDS.sleep(100);
                Latency("java.request.timer", begin, "type:test_metrics1");
                begin = System.currentTimeMillis();
                TimeUnit.MILLISECONDS.sleep(150);
                Latency("java.request.timer", begin, "type:test_metrics2");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        System.out.println("stop timer reporting");
    }

    public static void main(String[] args) {
//        ExecutorService executorService = Executors.newFixedThreadPool(3);
//        executorService.submit(MetricsExample::StoreReport);
//        executorService.submit(MetricsExample::CounterReport);
//        executorService.submit(MetricsExample::TimerReport);

//        CounterReport();
        StoreReport();
//        TimerReport();
    }
}
