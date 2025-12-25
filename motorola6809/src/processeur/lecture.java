package processeur;

import java.util.ArrayList;
import java.util.Scanner;
import java.lang.String;

public class lecture {

    private ArrayList<ArrayList<String>> lines;
    private static int compteurdelignee = 0;
    private ROM rom;
    private ram ram;
    private registre reg;
    private mode modeDetector;

    public lecture(ArrayList<ArrayList<String>> lines, ram ram, ROM rom, registre reg, mode modeDetector) {

        this.lines = lines;
        this.rom = rom; // INITIALIZE THE RAM FIELD
        this.ram = ram;
        this.reg = reg;
        this.modeDetector = modeDetector;

        Scanner sc = new Scanner(System.in);
        System.out.println("Enter your assembly code (type END to finish):");

        while (true) {

            String inputLine = sc.nextLine();

            if (inputLine.equalsIgnoreCase("END")) {
                // Just add END to the list, don't execute it yet
                ArrayList<String> endLine = new ArrayList<>();
                endLine.add("END");
                lines.add(endLine);
                break;
            }

            // // Stop reading when user types "END"
            // if (inputLine.equalsIgnoreCase("END")) {

            // String[] converted = modeDetector.processAndConvertInstruction("END", "",
            // reg);

            // this.ram.writeAndClear(converted[0]);
            // this.rom.writeData(converted[0]);
            // break;

            // }
            compteurdelignee++;

            // Split the line into words and create a new ArrayList for this line
            String[] words = inputLine.split("\\s+"); // Split by spaces
            ArrayList<String> lineWords = new ArrayList<>();

            // Keep original words for display
            for (String word : words) {
                if (!word.isEmpty()) {
                    lineWords.add(word);
                }
            }
            lines.add(lineWords);
        }
    }

    // Get the first word of a specific line
    public String getFirstWord(int compteurdelignee) {
        // Convert to 0-based indexing and check bounds
        int index = compteurdelignee - 1;
        if (index >= 0 && index < lines.size() && !lines.get(index).isEmpty()) {
            return lines.get(index).get(0);
        } else {
            return "Line not found or empty";
        }
    }

    // Get the first character of the second word from a specific line
    public char getSecondWordFirstChar(int compteurdelignee) {
        int index = compteurdelignee - 1;
        if (index >= 0 && index < lines.size() && lines.get(index).size() > 1) {
            String secondWord = lines.get(index).get(1);
            if (!secondWord.isEmpty()) {
                return secondWord.charAt(0);
            }
        }
        return ' ';
    }

    public ArrayList<ArrayList<String>> getLines() {
        return lines;
    }

}