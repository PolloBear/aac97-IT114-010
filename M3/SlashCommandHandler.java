package M3;

/*
Challenge 2: Simple Slash Command Handler
-----------------------------------------
- Accept user input as slash commands
  - "/greet <name>" → Prints "Hello, <name>!"
  - "/roll <num>d<sides>" → Roll <num> dice with <sides> and returns a single outcome as "Rolled <num>d<sides> and got <result>!"
  - "/echo <message>" → Prints the message back
  - "/quit" → Exits the program
- Commands are case-insensitive
- Print an error for unrecognized commands
- Print errors for invalid command formats (when applicable)
- Capture 3 variations of each command except "/quit"
*/

import java.util.Scanner;
import java.util.Random;
public class SlashCommandHandler extends BaseClass {
    private static String ucid = "Aac97"; // <-- change to your UCID

    public static void main(String[] args) {
        printHeader(ucid, 2, "Objective: Implement a simple slash command parser.");

        Scanner scanner = new Scanner(System.in);
        Random random = new Random();
        // Can define any variables needed here


        //"/greet <name>" → Prints "Hello, <name>!"
        //"/roll <num>d<sides>" → Roll <num> dice with <sides> and returns a single outcome as "Rolled <num>d<sides> and got <result>!"
        //"/echo <message>" → Prints the message back
        //"/quit" → Exits the program

        while (true) {
            System.out.print("Enter command: ");
            String ask = scanner.nextLine();
            

            // check if quit
            //// process quit
            if (ask.equalsIgnoreCase("/quit")) 
            {
                break;
            }
            
            String[] test = ask.split(" ",2);
            String command =test[0].toLowerCase();
            String msg = test.length > 1 ? test[1] : "";
            

            switch (command) 
            {
            // check if greet
            //// process greet
                case ("/greet"):
                    if (msg.isEmpty()) 
                    {
                        System.out.println("Error: Missing name for /greet command.");
                    } 
                    else 
                    {
                        System.out.println("Hello, " + msg + "!");
                    }
                        continue;

            // check if roll
            //// process roll
            //// handle invalid formats
                case("/roll"):
                if (msg.isEmpty()) {
                    System.out.println("Error: Missing dice specification for /roll command.");
                } else {
                    try {
                        String[] diceParts = msg.split("d");
                        if (diceParts.length != 2) {
                            System.out.println("Error: Invalid dice format. Expected <num>d<sides>.");
                        } else {
                            int num = Integer.parseInt(diceParts[0]);
                            int sides = Integer.parseInt(diceParts[1]);
                            if (num <= 0 || sides <= 0) {
                                System.out.println("Error: Number of dice and sides must be positive integers.");
                            } else {
                                Random rand = new Random();
                                int result = rand.nextInt(num * sides - num + 1) + num;
                                System.out.println("Rolled " + num + "d" + sides + " and got " + result + "!");
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Invalid number format in /roll command.");
                    }
                }
                continue;
            // check if echo
            //// process echo

                case("/echo"):

                if (msg.isEmpty()) {
                    System.out.println("Error: Missing name for /echo command.");
                } else {
                    System.out.println( msg );
                }
                  continue;
                                
            }

            // handle invalid commnads
            
        }

        printFooter(ucid, 2);
        scanner.close();
    }
}
