package processeur;

import java.util.ArrayList;
import java.util.Scanner;
import java.lang.String;


public class lecture {

    private ArrayList<ArrayList<String>> lines;
    private static int compteurdelignee  = 0;
    private ROM rom;
    private ram ram;

    public lecture(ArrayList<ArrayList<String>> lines,ram ram, ROM rom){ 

        this.lines = lines;
        this.rom = rom; // INITIALIZE THE RAM FIELD
        this.ram = ram;
        
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter your assembly code (type END to finish):");

        while (true) {
            
            
            String inputLine = sc.nextLine();
            
            // Stop reading when user types "END"
            if (inputLine.equalsIgnoreCase("END")) {
                
                 
                this.ram.writeAndClear("END");
                this.rom.writeData("END");
                break;
                
            }
            compteurdelignee++;

    



            
            // Split the line into words and create a new ArrayList for this line
            String[] words = inputLine.split("\\s+"); // Split by spaces
            ArrayList<String> lineWords = new ArrayList<>();
            
            // Add each word to the line's ArrayList
            for (String word : words) {
                if (!word.isEmpty()) { // Skip empty strings
                    lineWords.add(word);
                    this.rom.writeData(word); 
                    this.ram.writeAndClear(word);
                    
                }
            }
            
            // Add this line to our main lines ArrayList
            lines.add(lineWords);
        }
    }
        public ArrayList<ArrayList<String>> getLines() {
        return lines;
    }

}
    

///////////////////////////////////////////////////////////////////////////////////////////////////////0/////////////
/// 
/// 
/// 
/* this is what i should use is i want to have the seccond word or the first */    

// // Print only the first word from each line
// System.out.println("\n--- First Words Only ---");
// for (int line = 0; line < lines.size(); line++) {
//     if (lines.get(line).size() > 0) { // Check if line has at least 1 word
//         System.out.println(lines.get(line).get(0));
// }
// }

// // // Now you can access second word!
//         System.out.println("\n--- Access Individual Words ---");
//         System.out.println("\nAll words individually:");
//         for (int line = 0; line < lines.size(); line++) {
//             if (lines.get(line).size() > 1) {
//                 System.out.println(lines.get(line).get(1));
//             }
//         }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// Simple method to get all first words as a list
// public ArrayList<String> getAllFirstWords() {
//     ArrayList<String> firstWords = new ArrayList<>();
//     for (ArrayList<String> line : lines) {
//         if (!line.isEmpty()) {
//             firstWords.add(line.get(0));
//         }
//     }
//     return firstWords;
// }

// // Simple method to get all first letters of second words
// public ArrayList<Character> getAllSecondWordFirstLetters() {
//     ArrayList<Character> firstLetters = new ArrayList<>();
//     for (ArrayList<String> line : lines) {
//         if (line.size() > 1 && !line.get(1).isEmpty()) {
//             firstLetters.add(line.get(1).charAt(0));
//         }
//     }
//     return firstLetters;
// }

    

    // Get the first word of a specific line
//     public String getFirstWord(int compteurdelignee) {
//         // Convert to 0-based indexing and check bounds
//         int index = compteurdelignee - 1;
//         if (index >= 0 && index < lines.size() && !lines.get(index).isEmpty()) {
//             return lines.get(index).get(0);
//         } else {
//             return "Line not found or empty";
//         }
//     }


//     // Get the first character of the second word from a specific line
//     public char getSecondWordFirstChar(int compteurdelignee) {
//         // Convert to 0-based indexing and check bounds
//         int index = compteurdelignee - 1;
//         if (index >= 0 && index < lines.size() && lines.get(index).size() > 1) {
//             String secondWord = lines.get(index).get(1);
//             if (!secondWord.isEmpty()) {
                 
//                 return secondWord.charAt(0);
//             }
//         }
//         return ' '; // Return space if not available
//     }
//         public ArrayList<ArrayList<String>> getLines() {
//         return lines;
//     }
// }
       
        

    

