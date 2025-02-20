package M2;

public class Problem4 extends BaseClass {
    private static String[] array1 = { "hello world!", "java programming", "special@#$%^&characters", "numbers 123 456",
            "mIxEd CaSe InPut!" };
    private static String[] array2 = { "hello world", "java programming", "this is a title case test",
            "capitalize every word", "mixEd CASE input" };
    private static String[] array3 = { "  hello   world  ", "java    programming  ",
            "  extra    spaces  between   words   ",
            "      leading and trailing spaces      ", "multiple      spaces" };
    private static String[] array4 = { "hello world", "java programming", "short", "a", "even" };

    private static void transformText(String[] arr, int arrayNumber) {
        // Only make edits between the designated "Start" and "End" comments
        printArrayInfoBasic(arr, arrayNumber);

        // Challenge 1: Remove non-alphanumeric characters except spaces
        // Challenge 2: Convert text to Title Case
        // Challenge 3: Trim leading/trailing spaces and remove duplicate spaces
        // Result 1-3: Assign final phrase to `placeholderForModifiedPhrase`
        // Challenge 4 (extra credit): Extract middle 3 characters (beginning starts at middle of phrase),
        // assign to 'placeholderForMiddleCharacters'
        // if not enough characters assign "Not enough characters"
 
        // Step 1: sketch out plan using comments (include ucid and date)
        //Challenge 1 we can use the replaceall feature to remove non-alphanumeric 
        //Challenge 2 as of right now i dont know any thing from my past knowledge and classes so i will be using the internet 
        //Challenge 3 use replace all again to remove the spaces and triming 
        //Challenge 4 we find the lenght of the phrase and divide it by 2 to get in the middle and subracting by 1 and than finding the 3 letter around it 
        //aac97 2/19/2025
        // Step 2: Add/commit your outline of comments (required for full credit)
        // Step 3: Add code to solve the problem (add/commit as needed)
        String placeholderForModifiedPhrase = "";
        String placeholderForMiddleCharacters = "";
        
        for(int i = 0; i <arr.length; i++){
            // Start Solution Edits

                String phrase = arr[i]; // Gives the prashe string the index of the array
                phrase = phrase.replaceAll("[^a-zA-Z0-9\\s]", ""); //This removes the non-alphanumeric charsters with the replace all feature
            
                
                String[] words = phrase.split("\\s+"); // \\+ handles cases and the mutiple spaces between the words 
                StringBuilder titleCasePhrase = new StringBuilder(); // Stringbuilder is used for the final title cases string 
                for (String word : words) // loops ins every array 
                {
                    if (word.length() > 0) //checks if the word is not empty 
                    {
                        titleCasePhrase.append(Character.toUpperCase(word.charAt(0))) // this makes the first letter upercase 
                                       .append(word.substring(1).toLowerCase())//makes the rest into lower case 
                                       .append(" ");//sepreates each word with a space 
                    }
                }
                
                phrase = titleCasePhrase.toString().trim();// tostring converts it into string and trim is remoivng the spaces at the end 

                phrase = phrase.replaceAll("\\s+", " ").trim();//removing duplicate spaces
                placeholderForModifiedPhrase = phrase;// Assign final phrase to placeholderForModifiedPhrase
                
                int len = placeholderForModifiedPhrase.length();//This checks the leangth of the phrase that in the array 
                if (len >= 3)
                {
                    int mid = len / 2 - 1;// dividing by 2 give the middle index and subtracting it by 1 to start one caracther before the middle
                    placeholderForMiddleCharacters = placeholderForModifiedPhrase.substring(mid, mid + 3);//extracts 3 characters starting from the middle 

                } 
                else 
                {
                    placeholderForMiddleCharacters = "Not enough characters"; // this is for when the prahse isnt big engough to pull the 3 letter

                }
            
             // End Solution Edits
            System.out.println(String.format("Index[%d] \"%s\" | Middle: \"%s\"",i, placeholderForModifiedPhrase, placeholderForMiddleCharacters));
        }

       

        
        System.out.println("\n______________________________________");
    }
    public static void main(String[] args) {
        final String ucid = "aac97"; // <-- change to your UCID
        // No edits below this line
        printHeader(ucid, 4);

        transformText(array1, 1);
        transformText(array2, 2);
        transformText(array3, 3);
        transformText(array4, 4);
        printFooter(ucid, 4);
    }

}
