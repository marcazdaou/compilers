import java.lang.System;

// for, do-while, break and continue (including nested loops), and the ternary conditional.
public class LoopsTest {
    public static void main(String[] args) {
        int sum = 0;
        for (int i = 1; i <= 10; i++) {
            sum += i;
        }
        System.out.println(sum);

        int n = 0;
        do {
            n += 3;
        } while (n < 20);
        System.out.println(n);

        int ran = 0;
        do {
            ran++;
        } while (false);
        System.out.println(ran);

        int evens = 0;
        for (int i = 0; i < 20; i++) {
            if (i % 2 == 1) {
                continue;
            }
            if (i > 12) {
                break;
            }
            evens += i;
        }
        System.out.println(evens);

        int pairs = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (j > i) {
                    break;
                }
                pairs++;
            }
        }
        System.out.println(pairs);

        int k = 0;
        while (true) {
            k++;
            if (k == 7) {
                break;
            }
        }
        System.out.println(k);

        for (int i = 0; i < 4; i++) {
            System.out.println(i % 2 == 0 ? "even" : "odd");
        }
        int big = sum > 50 ? sum : -1;
        System.out.println(big);
    }
}
