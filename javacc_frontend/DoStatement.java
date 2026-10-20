import java.lang.Integer;
import java.lang.System;

public class DoStatement {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int i = 0;
        int sum = 0;
        do {
            sum += i;
            i += 1;
        } while(i <= n);
        System.out.println(sum);
    }
}

