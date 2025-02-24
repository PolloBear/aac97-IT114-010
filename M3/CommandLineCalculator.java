package M3;
import java.util.Scanner;
/*
Challenge 1: Command-Line Calculator
------------------------------------
- Accept two numbers and an operator as command-line arguments
- Supports addition (+) and subtraction (-)
- Allow integer and floating-point numbers
- Ensures correct decimal places in output based on input (e.g., 0.1 + 0.2 → 1 decimal place)
- Display an error for invalid inputs or unsupported operators
- Capture 5 variations of tests
*/

public class CommandLineCalculator extends BaseClass {
    private static String ucid = "aac97"; // <-- change to your ucid

    public static void main(String[] args) {
        printHeader(ucid, 1, "Objective: Implement a calculator using command-line arguments.");

        

        if (args.length != 3) {
            System.out.println("Usage: java M3.CommandLineCalculator <num1> <operator> <num2>");
            printFooter(ucid, 1);
            return;
        }

        try {
            System.out.println("Calculating result...");
            // extract the equation (format is <num1> <operator> <num2>)
            String num1 = args[0];
            String operator = args[1];
            String num2 = args[2];

            float Num1 = Float.parseFloat(num1);
            float Num2 = Float.parseFloat(num2);
            // check if operator is addition or subtraction
            
            // check the type of each number and choose appropriate parsing

            float result;
            if (operator.equals("+")) {
                result = Num1 + Num2;
            } else if (operator.equals("-")) {
                result = Num1 - Num2;
            }
            else{
                System.out.println("Error wrong input type");
                return;

            }

            // generate the equation result (Important: ensure decimals display as the longest decimal passed)
        
            int numT1 = num1.contains(".") ? num1.split("\\.")[1].length() : 0;
            int numT2 = num2.contains(".") ? num2.split("\\.")[1].length() : 0;
            int maxDecimalPlace=Math.max(numT1, numT2);
            // i.e., 0.1 + 0.2 would show as one decimal place (0.3), 0.11 + 0.2 would shows as two (0.31), etc
            System.out.printf("Result : %."+ maxDecimalPlace + "f\n",result);

        } catch (Exception e) {
            System.out.println("Invalid input. Please ensure correct format and valid numbers.");
        }

        printFooter(ucid, 1);
    }
}
