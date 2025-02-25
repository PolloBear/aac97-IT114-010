package M3;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Random;

/*
Challenge 3: Mad Libs Generator (Randomized Stories)
-----------------------------------------------------
- Load a **random** story from the "stories" folder
- Extract **each line** into a collection (i.e., ArrayList)
- Prompts user for each placeholder (i.e., <adjective>) 
    - Any word the user types is acceptable, no need to verify if it matches the placeholder type
    - Any placeholder with underscores should display with spaces instead
- Replace placeholders with user input (assign back to original slot in collection)
*/

public class MadLibsGenerator extends BaseClass {
    private static final String STORIES_FOLDER = "M3/stories";
    private static String ucid = "Aac97"; // <-- change to your ucid

    public static void main(String[] args) {
        printHeader(ucid, 3,
                "Objective: Implement a Mad Libs generator that replaces placeholders dynamically.");

        Scanner scanner = new Scanner(System.in);
        File folder = new File(STORIES_FOLDER);

        if (!folder.exists() || !folder.isDirectory() || folder.listFiles().length == 0) {
            System.out.println("Error: No stories found in the 'stories' folder.");
            printFooter(ucid, 3);
            scanner.close();
            return;
        }
        List<String> lines = new ArrayList<>();
        // Start edits
        
        File[] files = folder.listFiles();
        Random random = new Random();
        File chosenFile = files[random.nextInt(files.length)];

        try (Scanner fileScanner = new Scanner(chosenFile)) {
            while (fileScanner.hasNextLine()) {
                lines.add(fileScanner.nextLine());
            }
        } catch (Exception e) {
            System.out.println("Error reading file: " );
            printFooter(ucid, 3);
            scanner.close();
            return;
        }

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            int startIndex = line.indexOf('<');
            while (startIndex != -1) {
                int endIndex = line.indexOf('>', startIndex);


                if (endIndex == -1) {
                    break;
                }


                String placeholder = line.substring(startIndex + 1, endIndex); // e.g., "adjective" or "my_sillyword"

                // Convert underscores to spaces for the prompt
                String promptPlaceholder = placeholder.replace("_", " ");

                // Prompt user
                System.out.print("Enter a " + promptPlaceholder + ": ");
                String userInput = scanner.nextLine();

                // Rebuild the line with the user's input replacing the placeholder
                // Everything before < + user input + everything after >
                line = line.substring(0, startIndex) + userInput + line.substring(endIndex + 1);

                // Look for the next '<' in the updated line
                startIndex = line.indexOf('<');
            }

            // Update the collection with the modified line
            lines.set(i, line);
        }

        
        // load a random story file

        // parse the story lines

        // iterate through the lines

        // prompt the user for each placeholder (note: there may be more than one
        // placeholder in a line)

        // apply the update to the same collection slot

        // End edits
        System.out.println("\nYour Completed Mad Libs Story:\n");
        StringBuilder finalStory = new StringBuilder();
        for (String line : lines) {
            finalStory.append(line).append("\n");
        }
        System.out.println(finalStory.toString());

        printFooter(ucid, 3);
        scanner.close();
    }
}
