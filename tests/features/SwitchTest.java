import java.lang.System;

// switch statements: dense cases (TABLESWITCH), sparse cases (LOOKUPSWITCH), fall-through and default.
public class SwitchTest {
    static String dense(int day) {
        String name = "";
        switch (day) {
            case 1:
                name = "Mon";
                break;
            case 2:
                name = "Tue";
                break;
            case 3:
                name = "Wed";
                break;
            case 4:
                name = "Thu";
                break;
            case 5:
                name = "Fri";
                break;
            default:
                name = "Weekend";
        }
        return name;
    }

    static int sparse(int code) {
        int result = 0;
        switch (code) {
            case 1:
                result = 10;
                break;
            case 1000:
                result = 20;
                break;
            case 1000000:
                result = 30;
                break;
            default:
                result = -1;
        }
        return result;
    }

    static int fallThrough(int n) {
        int count = 0;
        switch (n) {
            case 3:
                count++;
            case 2:
                count++;
            case 1:
                count++;
                break;
            default:
                count = 100;
        }
        return count;
    }

    public static void main(String[] args) {
        for (int d = 0; d <= 7; d++) {
            System.out.println(dense(d));
        }
        System.out.println(sparse(1));
        System.out.println(sparse(1000));
        System.out.println(sparse(1000000));
        System.out.println(sparse(7));
        System.out.println(fallThrough(3));
        System.out.println(fallThrough(1));
        System.out.println(fallThrough(9));
    }
}
