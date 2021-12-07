package byteplus.sdk.core.metrics;

import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MetricsTest {
    static {
        metricsInit();
    }

    public static void metricsInit() {
        Config.setPrintLog(false);
    }

    // test demo for store report
    public static void TestStoreReport() {
        TreeMap<String, String> baseTags = new TreeMap<>();
        baseTags.put("tenant", "metrics_demo");
        Reporter reporter = new Reporter.ReporterBuilder().
                enableMetrics(true).
                baseTags(baseTags).build();
        for (int i = 0; i < 100000; i++) {
            reporter.store("request.store", 200, "type:test_metrics1");
            reporter.store("request.store", 100, "type:test_metrics2");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // test demo for store report
    public static void TestCounterReport() {
        TreeMap<String, String> baseTags = new TreeMap<>();
        baseTags.put("tenant", "metrics_demo");
        Reporter reporter = new Reporter.ReporterBuilder().
                enableMetrics(true).
                baseTags(baseTags).build();
        for (int i = 0; i < 100000; i++) {
            reporter.counter("request.qps", 1, "type:test_metrics1");
            reporter.counter("request.qps", 1, "type:test_metrics2");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    // test demo for timer report
    public static void TestTimerReport() {
        TreeMap<String, String> baseTags = new TreeMap<>();
        baseTags.put("tenant", "metrics_demo");
        Reporter reporter = new Reporter.ReporterBuilder().
                enableMetrics(true).
                baseTags(baseTags).build();
        for (int i = 0; i < 100000; i++) {
            long begin = System.currentTimeMillis();
            try {
                Thread.sleep(100);
                reporter.latency("request.latency", begin, "type:test_metrics1");
                begin = System.currentTimeMillis();
                Thread.sleep(150);
                reporter.latency("request.latency", begin, "type:test_metrics2");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        executorService.submit(MetricsTest::TestStoreReport);
        executorService.submit(MetricsTest::TestCounterReport);
        executorService.submit(MetricsTest::TestTimerReport);

//        TestCounterReport();
//        TestStoreReport();
//        TestTimerReport();
    }
}
