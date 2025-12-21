package processeur;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.Map;
import java.util.LinkedHashMap;

public class pas {
    private ArrayList<ArrayList<String>> assemblyLines;
    private ROM rom;
    private ram ram;
    private registre reg;
    private mode modeDetector;
    private int currentStep = 0;
    
    // StateManager instance
    private StateManager stateManager = new StateManager();

    public pas(ArrayList<ArrayList<String>> lines, ram ram, ROM rom, registre reg, mode modeDetector) {
        this.assemblyLines = lines;
        this.rom = rom;
        this.ram = ram;
        this.reg = reg;
        this.modeDetector = modeDetector;
        resetMemory();
    }

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

            // Update PC
            boolean shouldSkip = modeDetector.shouldSkipPCUpdate();
            if (!shouldSkip) {
                int currentRomAddress = rom.getCurrentAddressInt();
                reg.setPC(currentRomAddress);
                modeDetector.resetSkipPCIncrementFlag();
            }

        } else if (currentLine.get(0).equals("END")) {
            // Store END instruction in ROM
            storeInstructionInROM("END", "", "3F", "");

            // Update RAM to show END opcode
            updateRAMWithInstruction("END", "3F", "");

            if (!modeDetector.shouldSkipPCUpdate()) {
                int currentRomAddress = rom.getCurrentAddressInt();
                reg.setPC(currentRomAddress - 1);
            }
            modeDetector.resetSkipPCIncrementFlag();
        }
    }

    private void updateRAMWithInstruction(String instruction, String opcode, String operand) {
        if (instruction.equals("END")) {
            ram.getram().put("0000", opcode);
            return;
        }
        
        if (operand.length() == 2) {
            ram.getram().put("0000", operand);
        } 
        else if (operand.length() == 4) {
            String highByte = operand.substring(0, 2);
            String lowByte = operand.substring(2, 4);
            ram.getram().put("0000", highByte);
            ram.getram().put("0001", lowByte);
        }
    }

    private void resetMemory() {
        rom.resetWritePointer();
        for (int i = 0; i < 65536; i++) {
            String addr = String.format("%04X", i);
            ram.getram().put(addr, "00");
        }
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

    private void storeInstructionInROM(String instruction, String operand, String opcode, String data) {
        if (!opcode.equals("00")) {
            rom.writeData(opcode);
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

        reg.displayRegisters();

        System.out.println("\n=== CURRENT RAM ===");
        ram.displayRange("0000", "000F");

        System.out.println("\n=== CURRENT ROM (EXECUTED INSTRUCTIONS) ===");
        rom.displayRange("0000", reg.getPC());

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

        System.out.println("\n=== FINAL RAM ===");
        for (int i = 0; i < 65536; i++) {
            String addr = String.format("%04X", i);
            ram.getram().put(addr, "00");
        }
        ram.getram().put("0000", "3F");
        ram.displayRange("0000", "000F");

        System.out.println("\n=== FINAL ROM (COMPLETE PROGRAM) ===");
        int finalRomEnd = rom.getCurrentAddressInt() > 0 ? rom.getCurrentAddressInt() - 1 : 0;
        rom.displayRange("0000", String.format("%04X", finalRomEnd));

        if (!assemblyLines.isEmpty()) {
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
        modeDetector.resetSkipPCIncrementFlag();

        for (int i = 0; i < assemblyLines.size(); i++) {
            executeSingleStep(i);
        }

        modeDetector.resetSkipPCIncrementFlag();
        showFinalState();
    }

    private void resetToInitialState() {
        reg.setA("00");
        reg.setB("00");
        reg.setD("0000");
        reg.setX("0000");
        reg.setY("0000");
        reg.setS("0000");
        reg.setU("0000");
        reg.setCCR("00");
        reg.setPC(0);
        resetMemory();
    }

    // ================== GUI EXECUTION METHODS ==================

    public boolean executeGUISingleStep() {
        if (currentStep >= assemblyLines.size()) {
            return false;
        }
        
        saveCurrentState("Before step " + (currentStep + 1));
        executeSingleStep(currentStep);
        currentStep++;
        
        System.out.println("[GUI] Executed step " + currentStep);
        return currentStep < assemblyLines.size();
    }

    public void executeGUIAllSteps() {
        if (currentStep >= assemblyLines.size()) {
            return;
        }
        
        saveCurrentState("Before executing all from step " + (currentStep + 1));
        System.out.println("[GUI] Executing all " + (assemblyLines.size() - currentStep) + " remaining steps");
        
        while (currentStep < assemblyLines.size()) {
            executeSingleStep(currentStep);
            currentStep++;
        }
        
        System.out.println("[GUI] All steps executed");
    }

    public void resetGUIExecution() {
        resetToInitialState();
        currentStep = 0;
        stateManager.clear();
        System.out.println("[GUI] Execution reset");
    }

    public int getCurrentStepNumber() {
        return currentStep;
    }

    public int getTotalSteps() {
        return assemblyLines.size();
    }

    public ArrayList<String> getCurrentLineForDisplay() {
        if (currentStep > 0 && currentStep <= assemblyLines.size()) {
            return assemblyLines.get(currentStep - 1);
        }
        return new ArrayList<>();
    }

    public ArrayList<String> getLineAtStep(int step) {
        if (step >= 0 && step < assemblyLines.size()) {
            return assemblyLines.get(step);
        }
        return new ArrayList<>();
    }

    public void executeGUIFinalResult() {
        resetGUIExecution();
        executeGUIAllSteps();
        System.out.println("[GUI] Final result executed");
    }

    // ================== STATE MANAGEMENT METHODS ==================

    private void saveCurrentState(String description) {
        Map<String, String> registers = new LinkedHashMap<>();
        registers.put("A", reg.getA());
        registers.put("B", reg.getB());
        registers.put("D", reg.getD());
        registers.put("X", reg.getX());
        registers.put("Y", reg.getY());
        registers.put("S", reg.getS());
        registers.put("U", reg.getU());
        registers.put("CCR", reg.getCCR());
        registers.put("PC", reg.getPC());
        
        int romPtr = rom.getCurrentAddressInt();
        stateManager.saveState(ram.getram(), rom.getMemory(), registers, romPtr, currentStep, description);
    }

    public boolean goBackOneStep() {
        StateManager.ExecutionState previousState = stateManager.goBack();
        if (previousState == null) {
            return false;
        }
        
        // Restore RAM
        ram.getram().clear();
        ram.getram().putAll(previousState.ramState);
        
        // Restore ROM
        Map<String, String> romMemory = rom.getMemory();
        romMemory.clear();
        romMemory.putAll(previousState.romState);
        
        // Restore ROM write pointer
        rom.setWritePointer(previousState.romWritePointer);
        
        // Restore registers - INCLUDING PC!
        reg.setA(previousState.registerState.get("A"));
        reg.setB(previousState.registerState.get("B"));
        reg.setD(previousState.registerState.get("D"));
        reg.setX(previousState.registerState.get("X"));
        reg.setY(previousState.registerState.get("Y"));
        reg.setS(previousState.registerState.get("S"));
        reg.setU(previousState.registerState.get("U"));
        reg.setCCR(previousState.registerState.get("CCR"));
        
        // CRITICAL FIX: Restore PC properly
        String pcValue = previousState.registerState.get("PC");
        try {
            int pcInt = Integer.parseInt(pcValue, 16);
            reg.setPC(pcInt);
        } catch (Exception e) {
            reg.setPC(0);
        }
        
        // Restore current step
        currentStep = previousState.currentStep;
        
        System.out.println("[GUI] Went back to step " + currentStep + " (PC=" + reg.getPC() + ")");
        return true;
    }

    public boolean goForwardOneStep() {
        StateManager.ExecutionState nextState = stateManager.goForward();
        if (nextState == null) {
            return false;
        }
        
        // Restore RAM
        ram.getram().clear();
        ram.getram().putAll(nextState.ramState);
        
        // Restore ROM
        Map<String, String> romMemory = rom.getMemory();
        romMemory.clear();
        romMemory.putAll(nextState.romState);
        
        // Restore ROM write pointer
        rom.setWritePointer(nextState.romWritePointer);
        
        // Restore registers - INCLUDING PC!
        reg.setA(nextState.registerState.get("A"));
        reg.setB(nextState.registerState.get("B"));
        reg.setD(nextState.registerState.get("D"));
        reg.setX(nextState.registerState.get("X"));
        reg.setY(nextState.registerState.get("Y"));
        reg.setS(nextState.registerState.get("S"));
        reg.setU(nextState.registerState.get("U"));
        reg.setCCR(nextState.registerState.get("CCR"));
        
        // CRITICAL FIX: Restore PC properly
        String pcValue = nextState.registerState.get("PC");
        try {
            int pcInt = Integer.parseInt(pcValue, 16);
            reg.setPC(pcInt);
        } catch (Exception e) {
            reg.setPC(0);
        }
        
        // Restore current step
        currentStep = nextState.currentStep;
        
        System.out.println("[GUI] Went forward to step " + currentStep + " (PC=" + reg.getPC() + ")");
        return true;
    }

    public boolean canGoBack() {
        return stateManager.canGoBack();
    }

    public boolean canGoForward() {
        return stateManager.canGoForward();
    }

    public String getStateInfo() {
        return "State " + (stateManager.getCurrentStateIndex() + 1) + "/" + 
               stateManager.getTotalStates() + ": " + 
               stateManager.getCurrentStateDescription();
    }
}