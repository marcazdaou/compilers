import java.lang.System;

// Arithmetic, comparison, logical, increment/decrement and compound-assignment operators added to j--.
public class OperatorsTest {
    public static void main(String[] args) {
        int a = 17;
        int b = 5;
        System.out.println(a - b);
        System.out.println(a / b);
        System.out.println(a % b);
        System.out.println(-a / b);
        System.out.println(-a % b);
        System.out.println(+a);

        System.out.println(a != b);
        System.out.println(a >= b);
        System.out.println(b >= b);
        System.out.println(a < b);
        System.out.println(a < b || a > b);
        System.out.println(a < b || b > a);

        int x = 10;
        x -= 3;
        System.out.println(x);
        x *= 4;
        System.out.println(x);
        x /= 3;
        System.out.println(x);
        x %= 5;
        System.out.println(x);

        int i = 5;
        System.out.println(i++);
        System.out.println(i);
        System.out.println(++i);
        System.out.println(i--);
        System.out.println(--i);
        System.out.println("i = " + i + ", x = " + x);
    }
}
