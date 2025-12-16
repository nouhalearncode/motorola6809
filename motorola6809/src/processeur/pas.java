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
            int currentRomAddress = rom.getCurrentAddressInt();
            reg.setPC(currentRomAddress);

        } else if (currentLine.get(0).equals("END")) {
            // Store END instruction in ROM
            storeInstructionInROM("END", "", "3F", "");

            // Update RAM to show END opcode
            updateRAMWithInstruction("END", "3F", "");

            // Update PC to point to the address after END
            int currentRomAddress = rom.getCurrentAddressInt();
            reg.setPC(currentRomAddress - 1);
        }
    }

    private void debugStackMemory() {
        System.out.println("\n=== STACK MEMORY DEBUG (00F0-0100) ===");
        for (int addr = 0x00F0; addr <= 0x0100; addr++) {
            String addrStr = String.format("%04X", addr);
            String value = ram.getram().get(addrStr);
            System.out.println(addrStr + ": " + (value != null ? value : "null"));
        }
    }

    // NEW METHOD: Smart RAM update based on instruction mode
    private void updateRAMWithInstruction(String instruction, String opcode, String operand) {
        // Clear RAM first - DISABLED TO PRESERVE STATE
        /*
         * for (int i = 0; i < 65536; i++) {
         * String addr = String.format("%04X", i);
         * ram.getram().put(addr, "00");
         * }
         */

        // Check mode by looking at the original instruction
        if (instruction.equals("ABX") || instruction.equals("DECA") || instruction.equals("INCB") ||
                instruction.equals("CLRA") || instruction.equals("COMB") ||
                instruction.equals("DAA") || instruction.equals("MUL") ||
                instruction.equals("NEGA") || instruction.equals("NEGB") || instruction.equals("NOP")) {
            // INHERENT MODE: Store opcode at 0000
            if (!opcode.equals("00")) {
                ram.getram().put("0000", opcode);
            }
        } else if (instruction.startsWith("LD") || instruction.startsWith("ADD") ||
                instruction.startsWith("SUB") || instruction.startsWith("AND") ||
                instruction.startsWith("OR") || instruction.startsWith("EOR") ||
                instruction.startsWith("CMP")) {
            // IMMEDIATE MODE: Store operand (data value) at 0000
            if (!operand.isEmpty()) {
                ram.getram().put("0000", operand);
            }
        } else if (instruction.equals("END")) {
            // END: Store opcode at 0000
            ram.getram().put("0000", opcode);
        }
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

    private void updateRAMWithData(String instruction, String data) {
        // Clear RAM first - DISABLED TO PRESERVE STATE
        /*
         * for (int i = 0; i < 65536; i++) {
         * String addr = String.format("%04X", i);
         * ram.getram().put(addr, "00");
         * }
         */

        // If no data, just show cleared RAM
        if (data.isEmpty()) {
            return;
        }

        // Store data in 2-character chunks starting from address 0000
        int address = 0;

        // For arithmetic/logical operations, store both result and operand
        if (instruction.startsWith("ADD") || instruction.startsWith("SUB") ||
                instruction.startsWith("AND") || instruction.startsWith("OR") ||
                instruction.startsWith("EOR") || instruction.startsWith("CMP")) {

            // Store result in address 0000
            if (instruction.endsWith("A")) {
                storeDataChunk(reg.getA(), address++);
            } else if (instruction.endsWith("B")) {
                storeDataChunk(reg.getB(), address++);
            } else if (instruction.endsWith("D")) {
                // For 16-bit registers, split into two bytes
                String regValue = reg.getD();
                storeDataChunk(regValue.substring(0, 2), address++); // High byte
                storeDataChunk(regValue.substring(2), address++); // Low byte
            }

            // Store operand in next address
            storeDataChunk(data, address);

        } else {
            // For load instructions, just store the data
            storeDataChunk(data, address);
        }
    }

    private void storeDataChunk(String data, int address) {
        if (data.isEmpty())
            return;

        // Ensure we have at least 2 characters
        String chunk = data;
        if (chunk.length() < 2) {
            chunk = "0" + chunk; // Pad with leading zero
        } else if (chunk.length() > 2) {
            // For values longer than 2 chars, take first 2 chars
            chunk = chunk.substring(0, 2);
        }

        String addr = String.format("%04X", address);
        ram.getram().put(addr, chunk);
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

    private int calculateROMEndAddress(int stepIndex) {
        // Calculate how many bytes have been written to ROM so far
        int bytesUsed = 0;
        for (int i = 0; i <= stepIndex; i++) {
            ArrayList<String> line = assemblyLines.get(i);
            if (line.size() >= 1) {
                String firstWord = line.get(0);
                if (firstWord.equals("END")) {
                    bytesUsed += 1; // END takes 1 byte
                } else {
                    // Count bytes for this instruction
                    String secondWord = (line.size() > 1) ? line.get(1) : "";
                    if (secondWord.contains("#")) {
                        bytesUsed += 2; // Instruction + operand
                    } else {
                        bytesUsed += 1; // Instruction only
                    }
                }
            }
        }
        return bytesUsed > 0 ? bytesUsed - 1 : 0;
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

        for (int i = 0; i < assemblyLines.size(); i++) {
            executeSingleStep(i);
        }

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