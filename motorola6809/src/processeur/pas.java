package processeur;

import java.util.ArrayList;
import java.util.Scanner;

public class pas {
    private ArrayList<ArrayList<String>> assemblyLines;
    private ROM rom;
    private ram ram;
    private registre reg;
    private mode modeDetector;
    private int currentStep = 0;

    public pas(ArrayList<ArrayList<String>> lines, ram ram, ROM rom, registre reg, mode modeDetector) {
        this.assemblyLines = lines;
        this.rom = rom;
        this.ram = ram;
        this.reg = reg;
        this.modeDetector = modeDetector;
        resetMemory();
    }

    // In pas.java, add:
public void updateDisplayRAM(String value, int address) {
    if (value.length() == 4) {
        // 16-bit value
        ram.getram().put(String.format("%04X", address), value.substring(0, 2));
        ram.getram().put(String.format("%04X", address + 1), value.substring(2, 4));
    } else if (value.length() == 2) {
        // 8-bit value
        ram.getram().put(String.format("%04X", address), value);
    }
}

    private void executeSingleStep(int stepIndex) {
        if (stepIndex >= assemblyLines.size())
            return;

        ArrayList<String> currentLine = assemblyLines.get(stepIndex);

        if (currentLine.size() >= 1 && !currentLine.get(0).equals("END")) {
            String firstWord = currentLine.get(0);
            String secondWord = (currentLine.size() > 1) ? currentLine.get(1) : "";

            // Execute this instruction
            String[] converted = modeDetector.processAndConvertInstruction(firstWord, secondWord, reg);
            String opcode = converted[0];
            String cleanedOperand = converted[1];

            // Store instruction in ROM
            storeInstructionInROM(firstWord, secondWord, opcode, cleanedOperand);

            // Update RAM based on mode
            updateRAMWithInstruction(firstWord, opcode, cleanedOperand);

            // Update PC to current ROM address (pointing to next instruction)
            // BUT: Skip if PC was manually changed by control flow instructions (JMP, JSR, RTS, RTI)
            boolean shouldSkip = modeDetector.shouldSkipPCUpdate();
            if (!shouldSkip) {
                int currentRomAddress = rom.getCurrentAddressInt();
                reg.setPC(currentRomAddress);
                // Only reset flag if we actually updated the PC (normal instruction)
                modeDetector.resetSkipPCIncrementFlag();
            } else {
                // DO NOT reset flag here - it needs to persist for END instruction check
            }

        } else if (currentLine.get(0).equals("END")) {
            // Store END instruction in ROM
            storeInstructionInROM("END", "", "3F", "");

            // Update RAM to show END opcode
            updateRAMWithInstruction("END", "3F", "");

            // Update PC to point to the address after END
            // BUT: Skip if PC was manually changed by control flow instructions (JMP, JSR, RTS, RTI)
            if (!modeDetector.shouldSkipPCUpdate()) {
                int currentRomAddress = rom.getCurrentAddressInt();
                reg.setPC(currentRomAddress - 1);
            }
            // Reset the flag AFTER checking it
            modeDetector.resetSkipPCIncrementFlag();
        }
    }

    // NEW METHOD: Smart RAM update based on instruction mode
    private void updateRAMWithInstruction(String instruction, String opcode, String operand) {
    // DO NOT clear RAM - preserve existing data
    
    // This method should ONLY update RAM for visualization purposes
    // It doesn't affect actual execution
    
    if (instruction.equals("END")) {
        // Show END opcode at 0000
        ram.getram().put("0000", opcode); // "3F"
        return;
    }
    
    // For immediate mode (#$XX), show the data
    if (operand.length() == 2) {
        // This is a single byte value (like #$22)
        ram.getram().put("0000", operand);
    } 
    else if (operand.length() == 4) {
        // This is a 16-bit value (like #$3344)
        // Split into two bytes
        String highByte = operand.substring(0, 2);
        String lowByte = operand.substring(2, 4);
        ram.getram().put("0000", highByte);
        ram.getram().put("0001", lowByte);
    }
    // For extended mode addresses, DO NOT store them in RAM display
    // They are addresses, not data values
}

    private void resetMemory() {
        // Reset ROM completely - fill with zeros
        rom.resetWritePointer();

        // Reset RAM completely
        for (int i = 0; i < 65536; i++) {
            String addr = String.format("%04X", i);
            ram.getram().put(addr, "00");
        }

        // DO NOT pre-load any instructions into ROM
        // Instructions will be loaded one by one during execution
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

        resetToInitialState();
        currentStep = 0;

        while (true) {
            System.out.print("\nEnter command (1/-1/0): ");
            String command = scanner.nextLine().trim();

            if (command.equals("0")) {
                executeAllRemaining();
                showFinalState();
                break;
            } else if (command.equals("1")) {
                if (currentStep < assemblyLines.size()) {
                    executeSingleStep(currentStep);
                    currentStep++;
                    showCurrentState(currentStep - 1);
                } else {
                    System.out.println("Already at the last line!");
                }
            } else if (command.equals("-1")) {
                if (currentStep > 0) {
                    currentStep--;
                    resetAndExecuteUpTo(currentStep);
                } else {
                    System.out.println("Already at the first line!");
                }
            } else {
                System.out.println("Invalid command! Use: 1 (Next), -1 (Previous), 0 (Final)");
            }
        }
    }

    // private void executeSingleStep(int stepIndex) {
    // if (stepIndex >= assemblyLines.size()) return;

    // ArrayList<String> currentLine = assemblyLines.get(stepIndex);

    // if (currentLine.size() >= 1 && !currentLine.get(0).equals("END")) {
    // String firstWord = currentLine.get(0);
    // String secondWord = (currentLine.size() > 1) ? currentLine.get(1) : "";

    // // Execute this instruction
    // String[] converted = modeDetector.processAndConvertInstruction(firstWord,
    // secondWord, reg);

    // // Store instruction in ROM (only executed ones)
    // storeInstructionInROM(firstWord, secondWord, converted[0], converted[1]);

    // // Update RAM with current data
    // updateRAMWithData(firstWord, converted[1]);
    // } else if (currentLine.get(0).equals("END")) {
    // // Store END instruction in ROM
    // storeInstructionInROM("END", "", "3F", "");
    // }
    // }

    // private void storeInstructionInROM(String instruction, String operand, String
    // opcode, String data) {
    // // Only store if we have a valid opcode
    // if (!opcode.equals("00")) {
    // rom.writeData(opcode);

    // // Store operand/data if it exists (for immediate mode)
    // if (!data.isEmpty() && operand.contains("#")) {
    // rom.writeData(data);
    // }
    // }
    // }

    private void storeInstructionInROM(String instruction, String operand, String opcode, String data) {
        // Only store if we have a valid opcode
        if (!opcode.equals("00")) {
            rom.writeData(opcode);

            // Store operand/data if it exists
            // - Pour mode immédiat (#): data contient la valeur
            // - Pour mode indexé (,): data contient le post-byte
            if (!data.isEmpty()) {
                rom.writeData(data);
            }
        }
    }

    private void showCurrentState(int stepIndex) {
        ArrayList<String> currentLine = assemblyLines.get(stepIndex);
        System.out.println("\n=== STATE AFTER LINE " + (stepIndex + 1) + " ===");

        System.out.print("Line " + (stepIndex + 1) + ": ");
        for (String word : currentLine) {
            System.out.print(word + " ");
        }
        System.out.println();

        // Show registers
        reg.displayRegisters();

        // Show RAM (current data)
        System.out.println("\n=== CURRENT RAM ===");
        ram.displayRange("0000", "000F");

        // DEBUG: Check what's actually in ROM
        // rom.debugMemory();

        // Show ROM (only executed instructions so far)
        System.out.println("\n=== CURRENT ROM (EXECUTED INSTRUCTIONS) ===");
        rom.displayRange("0000", reg.getPC());

        // Calculate flags
        if (!currentLine.get(0).equals("END")) {
            modeDetector.calculateLastInstructionFlags();
        }
    }

    private void resetAndExecuteUpTo(int step) {
        resetToInitialState();
        for (int i = 0; i <= step; i++) {
            executeSingleStep(i);
        }
        showCurrentState(step);
    }

    private void executeAllRemaining() {
        for (int i = currentStep; i < assemblyLines.size(); i++) {
            executeSingleStep(i);
        }
    }

    private void showFinalState() {
        System.out.println("\n=== FINAL RESULT ===");
        reg.displayRegisters();

        // Final RAM shows END opcode
        System.out.println("\n=== FINAL RAM ===");
        // Clear and show END opcode in RAM
        for (int i = 0; i < 65536; i++) {
            String addr = String.format("%04X", i);
            ram.getram().put(addr, "00");
        }
        ram.getram().put("0000", "3F");
        ram.displayRange("0000", "000F");

        // Final ROM shows complete executed program
        System.out.println("\n=== FINAL ROM (COMPLETE PROGRAM) ===");
        // Use the actual current address from ROM tracker
        int finalRomEnd = rom.getCurrentAddressInt() > 0 ? rom.getCurrentAddressInt() - 1 : 0;
        rom.displayRange("0000", String.format("%04X", finalRomEnd));

        // Calculate flags for last non-END instruction
        if (!assemblyLines.isEmpty()) {
            // Find the last non-END instruction
            for (int i = assemblyLines.size() - 1; i >= 0; i--) {
                ArrayList<String> line = assemblyLines.get(i);
                if (line.size() > 0 && !line.get(0).equals("END")) {
                    modeDetector.calculateLastInstructionFlags();
                    break;
                }
            }
        }
    }

    private void executeFinalResult() {
        System.out.println("\n=== EXECUTING ALL INSTRUCTIONS ===");
        resetToInitialState();
        // Reset the skipPCIncrement flag at the start of execution
        modeDetector.resetSkipPCIncrementFlag();

        for (int i = 0; i < assemblyLines.size(); i++) {
            executeSingleStep(i);
        }

        // Reset the flag after all instructions are executed
        modeDetector.resetSkipPCIncrementFlag();
        showFinalState();
    }

    private void resetToInitialState() {
        // Reset registers
        reg.setA("00");
        reg.setB("00");
        reg.setD("0000");
        reg.setX("0000");
        reg.setY("0000");
        reg.setS("0000");
        reg.setU("0000");
        reg.setCCR("00");

        // Reset memory
        resetMemory();
    }

    // private void resetMemory() {
    // // Reset ROM completely
    // rom.resetWritePointer();

    // // Reset RAM completely
    // for (int i = 0; i < 65536; i++) {
    // String addr = String.format("%04X", i);
    // ram.getram().put(addr, "00");
    // }
    // }
}