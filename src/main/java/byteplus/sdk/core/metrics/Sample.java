package byteplus.sdk.core.metrics;

/**
 * @from: com.codahale.metrics.UniformSnapshot
 * @link: https://mvnrepository.com/artifact/io.dropwizard.metrics/metrics-core/4.0.1
 * @copyright: Copyright by Coda Hale, Ryan Tenney, Artem Prigoda
 * @license: Apache 2.0
 */


import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;


public class Sample {
    private static final int DEFAULT_SIZE = 1028;
    private final AtomicLong count;
    private final AtomicLongArray values;

    public Sample() {
        this(1028);
    }

    public Sample(int size) {
        this.count = new AtomicLong();
        this.values = new AtomicLongArray(size);

        for (int i = 0; i < this.values.length(); ++i) {
            this.values.set(i, 0L);
        }

        this.count.set(0L);
    }

    public int size() {
        long c = this.count.get();
        return c > (long) this.values.length() ? this.values.length() : (int) c;
    }

    public void update(long value) {
        long c = this.count.incrementAndGet();
        if (c <= (long) this.values.length()) {
            this.values.set((int) c - 1, value);
        } else {
            long r = ThreadLocalRandom.current().nextLong(c);
            if (r < (long) this.values.length()) {
                this.values.set((int) r, value);
            }
        }

    }

    public SampleSnapshot getSnapshot() {
        int s = this.size();
        long[] copy = new long[s];

        for (int i = 0; i < s; ++i) {
            copy[i] = this.values.get(i);
        }

        return new SampleSnapshot(copy);
    }


    public void clear() {
        for (int i = 0; i < this.values.length(); ++i) {
            this.values.set(i, 0L);
        }
        this.count.set(0L);
    }

    public static class SampleSnapshot {
        private final long[] values;

        public SampleSnapshot(Collection<Long> values) {
            Object[] copy = values.toArray();
            this.values = new long[copy.length];

            for (int i = 0; i < copy.length; ++i) {
                this.values[i] = (Long) copy[i];
            }

            Arrays.sort(this.values);
        }

        public SampleSnapshot(long[] values) {
            this.values = Arrays.copyOf(values, values.length);
            Arrays.sort(this.values);
        }

        public double getValue(double quantile) {
            if (quantile >= 0.0D && quantile <= 1.0D && !Double.isNaN(quantile)) {
                if (this.values.length == 0) {
                    return 0.0D;
                } else {
                    double pos = quantile * (double) (this.values.length + 1);
                    int index = (int) pos;
                    if (index < 1) {
                        return (double) this.values[0];
                    } else if (index >= this.values.length) {
                        return (double) this.values[this.values.length - 1];
                    } else {
                        double lower = (double) this.values[index - 1];
                        double upper = (double) this.values[index];
                        return lower + (pos - Math.floor(pos)) * (upper - lower);
                    }
                }
            } else {
                throw new IllegalArgumentException(quantile + " is not in [0..1]");
            }
        }

        public int size() {
            return this.values.length;
        }

        public long[] getValues() {
            return Arrays.copyOf(this.values, this.values.length);
        }

        public long getMax() {
            return this.values.length == 0 ? 0L : this.values[this.values.length - 1];
        }

        public long getMin() {
            return this.values.length == 0 ? 0L : this.values[0];
        }

        public double getMean() {
            if (this.values.length == 0) {
                return 0.0D;
            } else {
                double sum = 0.0D;
                long[] var3 = this.values;
                int var4 = var3.length;

                for (int var5 = 0; var5 < var4; ++var5) {
                    long value = var3[var5];
                    sum += (double) value;
                }

                return sum / (double) this.values.length;
            }
        }

        public double getStdDev() {
            if (this.values.length <= 1) {
                return 0.0D;
            } else {
                double mean = this.getMean();
                double sum = 0.0D;
                long[] var5 = this.values;
                int var6 = var5.length;

                for (int var7 = 0; var7 < var6; ++var7) {
                    long value = var5[var7];
                    double diff = (double) value - mean;
                    sum += diff * diff;
                }

                double variance = sum / (double) (this.values.length - 1);
                return Math.sqrt(variance);
            }
        }

        public void dump(OutputStream output) {
            PrintWriter out = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
            Throwable var3 = null;

            try {
                long[] var4 = this.values;
                int var5 = var4.length;

                for (int var6 = 0; var6 < var5; ++var6) {
                    long value = var4[var6];
                    out.printf("%d%n", value);
                }
            } catch (Throwable var16) {
                var3 = var16;
                throw var16;
            } finally {
                if (var3 != null) {
                    try {
                        out.close();
                    } catch (Throwable var15) {
                        var3.addSuppressed(var15);
                    }
                } else {
                    out.close();
                }

            }

        }
    }
}