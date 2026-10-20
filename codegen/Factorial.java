import java.lang.Integer;
import java.lang.System;

public class Factorial {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);

        int i = 1;
        long result = 1L;
        while (i <= n) {
            result = result * (long) i;
            i++;
        }

        System.out.println(result);
    }
}
