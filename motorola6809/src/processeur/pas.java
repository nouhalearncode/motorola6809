package processeur;

import java.util.ArrayList;
import java.util.Scanner;

public class pas {
    private ArrayList<ArrayList<String>> assemblyLines;
    private ROM rom;
    private ram ram;
    private registre reg;
    private mode modeDetector;
    private int currentLineIndex = -1;
    
    // Store execution history for step-by-step
    private ArrayList<registre> registerHistory;
    private ArrayList<ROM> romHistory;
    private ArrayList<ram> ramHistory;
    
    public pas(ArrayList<ArrayList<String>> lines, ram ram, ROM rom, registre reg, mode modeDetector) {
        this.assemblyLines = lines;
        this.rom = rom;
        this.ram = ram;
        this.reg = reg;
        this.modeDetector = modeDetector;
        this.registerHistory = new ArrayList<>();
        this.romHistory = new ArrayList<>();
        this.ramHistory = new ArrayList<>();
    }
    
    public void executeStepByStep() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\n=== EXECUTION MODE SELECTION ===");
        System.out.println("0 - Show final result (default)");
        System.out.println("1 - Step-by-step execution");
        System.out.print("Choose execution mode: ");
        
        String choice = scanner.nextLine().trim();
        
        if (choice.equals("1")) {
            executeStepMode(scanner);
        } else {
            executeFinalResult();
        }
    }
    
    private void executeStepMode(Scanner scanner) {
        System.out.println("\n=== STEP-BY-STEP EXECUTION ===");
        System.out.println("Commands: 1=Next, -1=Previous, 0=Final Result");
        
        // First, execute all lines and store history
        executeAllAndStoreHistory();
        
        currentLineIndex = -1;
        
        while (true) {
            System.out.print("\nEnter command (1/-1/0): ");
            String command = scanner.nextLine().trim();
            
            if (command.equals("0")) {
                showFinalFromHistory();
                break;
            } else if (command.equals("1")) {
                if (currentLineIndex < assemblyLines.size() - 1) {
                    currentLineIndex++;
                    showCurrentStateFromHistory();
                } else {
                    System.out.println("Already at the last line!");
                }
            } else if (command.equals("-1")) {
                if (currentLineIndex > 0) {
                    currentLineIndex--;
                    showCurrentStateFromHistory();
                } else {
                    System.out.println("Already at the first line!");
                }
            } else {
                System.out.println("Invalid command! Use: 1 (Next), -1 (Previous), 0 (Final)");
            }
        }
    }
    
    private void executeAllAndStoreHistory() {
        // Save initial state
        saveCurrentState();
        
        // Execute each line and save state after each line
        for (int i = 0; i < assemblyLines.size(); i++) {
            ArrayList<String> currentLine = assemblyLines.get(i);
            
            if (currentLine.size() >= 1) {
                String firstWord = currentLine.get(0);
                String secondWord = (currentLine.size() > 1) ? currentLine.get(1) : "";
                
                // Process instruction
                modeDetector.processAndConvertInstruction(firstWord, secondWord, reg);
                
                // Save state after this instruction
                saveCurrentState();
            }
        }
    }
    
    private void saveCurrentState() {
        try {
            // Save register state
            registre regCopy = new registre();
            regCopy.setA(reg.getA());
            regCopy.setB(reg.getB());
            regCopy.setD(reg.getD());
            regCopy.setX(reg.getX());
            regCopy.setY(reg.getY());
            regCopy.setS(reg.getS());
            regCopy.setU(reg.getU());
            registerHistory.add(regCopy);
            
            // Note: For simplicity, we're not copying ROM/RAM history
            // In a real implementation, you'd want to deep copy these too
        } catch (Exception e) {
            System.out.println("Error saving state: " + e.getMessage());
        }
    }
    
    private void showCurrentStateFromHistory() {
        if (currentLineIndex < 0 || currentLineIndex >= registerHistory.size()) {
            return;
        }
        
        ArrayList<String> currentLine = assemblyLines.get(currentLineIndex);
        System.out.println("\n=== STATE AFTER LINE " + (currentLineIndex + 1) + " ===");
        
        // Display the current line being executed
        System.out.print("Line " + (currentLineIndex + 1) + ": ");
        for (String word : currentLine) {
            System.out.print(word + " ");
        }
        System.out.println();
        
        // Show registers from history
        registre currentReg = registerHistory.get(currentLineIndex);
        currentReg.displayRegisters();
        
        // Show ROM and RAM (these are the final states since we don't store history)
        ram.displayRange("0000", "000F");
        rom.displayRange("0000", "0010");
        
        // Calculate flags for current instruction if it's not END
        if (currentLine.size() > 0 && !currentLine.get(0).equals("END")) {
            modeDetector.calculateLastInstructionFlags();
        }
    }
    
    private void showFinalFromHistory() {
        System.out.println("\n=== FINAL RESULT ===");
        
        // Show final registers
        if (!registerHistory.isEmpty()) {
            registre finalReg = registerHistory.get(registerHistory.size() - 1);
            finalReg.displayRegisters();
        }
        
        // Show final ROM and RAM
        ram.displayRange("0000", "000F");
        rom.displayRange("0000", "0010");
        
        // Calculate flags for last non-END instruction
        modeDetector.calculateLastInstructionFlags();
    }
    
    private void executeFinalResult() {
        System.out.println("\n=== EXECUTING ALL INSTRUCTIONS ===");
        
        // Execute all instructions once
        for (int i = 0; i < assemblyLines.size(); i++) {
            ArrayList<String> currentLine = assemblyLines.get(i);
            
            if (currentLine.size() >= 1) {
                String firstWord = currentLine.get(0);
                String secondWord = (currentLine.size() > 1) ? currentLine.get(1) : "";
                
                // Process instruction
                modeDetector.processAndConvertInstruction(firstWord, secondWord, reg);
            }
        }
        
        // Show clean final result
        System.out.println("\n=== FINAL RESULT ===");
        displayCleanFinalResult();
    }
    
    private void displayCleanFinalResult() {
        // Show final registers
        reg.displayRegisters();
        
        // Show RAM (should contain only END opcode)
        System.out.println("\n=== RAM CONTENT ===");
        ram.displayRange("0000", "000F");
        
        // Show ROM (contains all instructions in opcode)
        System.out.println("\n=== ROM CONTENT (ALL INSTRUCTIONS) ===");
        rom.displayRange("0000", "0010");
        
        // Calculate flags for last non-END instruction
        modeDetector.calculateLastInstructionFlags();
    }
    
    private void resetComponents() {
        // Reset components to clean state
        // Note: This creates new instances to avoid contamination
    }
}