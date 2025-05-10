package M2;

public class Problem3 extends BaseClass {
    private static Integer[] array1 = {42, -17, 89, -256, 1024, -4096, 50000, -123456};
    private static Double[] array2 = {3.14159265358979, -2.718281828459, 1.61803398875, -0.5772156649, 0.0000001, -1000000.0};
    private static Float[] array3 = {1.1f, -2.2f, 3.3f, -4.4f, 5.5f, -6.6f, 7.7f, -8.8f};
    private static String[] array4 = {"123", "-456", "789.01", "-234.56", "0.00001", "-99999999"};
    private static Object[] array5 = {-1, 1, 2.0f, -2.0d, "3", "-3.0"};
    private static void bePositive(Object[] arr, int arrayNumber) {
        // Only make edits between the designated "Start" and "End" comments
        printArrayInfo(arr, arrayNumber);

        // Challenge 1: Make each value positive
        // Challenge 2: Convert the values back to their original data type and assign it to the proper slot of the `output` array
        // Step 1: sketch out plan using comments (include ucid and date)
        //Challenge 1-Plan is to go through the array index and check if there are negative or not with math.abs this make the int a postive no matter what and use if and else if to figure out the obj type
        //Challenge 2- This might be a challange but i think i would just leave it in the array already with out 
        //aac97 2/19/2025
        // Step 2: Add/commit your outline of comments (required for full credit)
        // Step 3: Add code to solve the problem (add/commit as needed)
        Object[] output = new Object[arr.length];
        // Start Solution Edits
        for (int i = 0; i < arr.length; i++)// Goes through the whole 
        {
            Object val = arr[i];
        
            if (val instanceof Integer) //instanceof is a thing i found that will figure out the obj type
            {
                output[i] = Math.abs((Integer) val); //changes it to a postive 
            } 
            else if (val instanceof Double) //checks if its a double
            {
                output[i] = Math.abs((Double) val);//changes it to a postive 
            } 
            else if (val instanceof Float) //checks if its a float 
            {
                output[i] = Math.abs((Float) val);//changes it to a postive 
            } 
            else if (val instanceof String) // checks if its a string 
            {
                try 
                {
                    double num = Double.parseDouble((String) val); // this converts the string to a number
                    output[i] = String.valueOf(Math.abs(num)); //  this convert back to a string after taking the value value
                } 
                catch (NumberFormatException e) 
                {
                    output[i] = val; // Leave as-is if not a valid number
                }
            } 
            else 
            {
                output[i] = val; // Leave unchanged types
            }
        }

        // End Solution Edits
        System.out.println("Output: ");
        printOutputWithType(output);
        System.out.println("");
        System.out.println("______________________________________");
    }

    public static void main(String[] args) {
        final String ucid = "aac97"; // <-- change to your UCID
        // no edits below this line
        printHeader(ucid, 3);
        bePositive(array1, 1);
        bePositive(array2, 2);
        bePositive(array3, 3);
        bePositive(array4, 4);
        bePositive(array5, 5);
        printFooter(ucid, 3);

    }
}
