import java.lang.Double;
import java.lang.Math;
import java.lang.System;

public class Quadratic {
    public static void main(String[] args) {
        double a = Double.parseDouble(args[0]);
        double b = Double.parseDouble(args[1]);
        double c = Double.parseDouble(args[2]);

        double discriminant = b * b - (double) 4 * a * c;
        double root1 = (-b + Math.sqrt(discriminant)) / ((double) 2 * a);
        double root2 = (-b - Math.sqrt(discriminant)) / ((double) 2 * a);

        System.out.println(root1 + " " + root2);
    }
}
