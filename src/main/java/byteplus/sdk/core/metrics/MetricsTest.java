package byteplus.sdk.core.metrics;


import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static byteplus.sdk.core.metrics.Helper.*;

public class MetricsTest {
    static int times = 150000;

    static {
        metricsInit();
    }

    public static void metricsInit() {
        Collector.Init(
                MetricsOption.withMetricsLog(),
                MetricsOption.withFlushIntervalMs(10 * 1000)
        );
    }

    // test demo for store report
    public static void StoreReport() {
        for (int i = 0; i < times; i++) {
            Store("java.request.store", 200, "type:test_metrics1");
            Store("java.request.store", 100, "type:test_metrics2");
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
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        executorService.submit(MetricsTest::StoreReport);
        executorService.submit(MetricsTest::CounterReport);
        executorService.submit(MetricsTest::TimerReport);

//        CounterReport();
//        StoreReport();
//        TimerReport();
    }
}
