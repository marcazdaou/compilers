import java.lang.System;

// long and double types: literals, arithmetic, comparisons, locals, fields, parameters and return values.
public class LongDoubleTest {
    static long counter = 5L;
    double rate;

    static long cube(long n) {
        return n * n * n;
    }

    static double average(double a, double b) {
        return (a + b) / 2.0;
    }

    public static void main(String[] args) {
        long big = 3000000000L;
        long sum = big + big * 2L;
        System.out.println(sum);
        System.out.println(sum - 1L);
        System.out.println(sum / 7L);
        System.out.println(sum % 7L);
        System.out.println(cube(2000L));
        System.out.println(big > 2147483647L);

        double d = 3.5;
        double e = d * 2.0 - 1.25 / 0.5;
        System.out.println(e);
        System.out.println(average(1.5, 2.25));
        System.out.println(d >= 3.5);
        System.out.println(1e3 + 0.25);
        System.out.println("total = " + sum + ", mean = " + average(d, e));

        // All six comparisons on long and double, including NaN (every comparison with NaN is false).
        long p = 7L;
        long q = 3L;
        System.out.println((p < q) + " " + (p <= q) + " " + (p > q) + " " + (p >= q) + " " + (p == q) + " " + (p != q));
        System.out.println((q < p) + " " + (p == p) + " " + (p != p));
        double x = 2.5;
        double y = 2.5;
        System.out.println((x < y) + " " + (x <= y) + " " + (x > y) + " " + (x >= y) + " " + (x == y) + " " + (x != y));
        double nan = 0.0 / 0.0;
        System.out.println((nan < x) + " " + (nan <= x) + " " + (nan > x) + " " + (nan >= x) + " " + (nan == nan) + " " + (nan != nan));
        long steps = 0L;
        for (long n = 1L; n < 1000000L; n *= 10L) {
            steps += 1L;
        }
        System.out.println(steps);

        // long and double fields (static and instance)
        LongDoubleTest t = new LongDoubleTest();
        t.rate = 2.5;
        counter = counter * 3L;
        System.out.println(counter + " " + t.rate * 2.0);
    }
}
