package processeur;

public class mode {
    
    private String immediat;
    private String direct;
    private String indexe;
    private String etendu;
    private String inherent;

    private ram ramMemory;

    private String op ="00";
    private int cycle =0;

    private String lastInstructionHex = "";
    private int lastInstructionResult = 0;
    private String[] lastInstructionFlags = {};

    private registre reg; // Add this field

    // Constructor to initialize modes
    public mode() {

        this.immediat = "Immediate";
        this.direct = "Direct";
        this.indexe = "Indexed";
        this.etendu = "Extended";
        this.inherent = "Inherent";
    }

// Add a setter method for the register
    public void setRegistre(registre reg) {
        this.reg = reg;
    }

    public String determineMode(String secondWord) {

        // MINIMAL FIX: Check if string is empty before charAt(0)
        if (secondWord == null || secondWord.isEmpty()) {
            return "Unknown mode";
        }
        
    if (secondWord.charAt(0) == '#'){

        // immediat
        return immediat;

    }

    if (secondWord.charAt(0) == '>'){

        // etendu
        return etendu;
        
    }

    if (secondWord.charAt(0) == '<'){

        // direct
        return direct;
    }

    if (secondWord.charAt(0) == '$'){

        // inherent
        return inherent;

    }
    return "Unknown mode";
}   

// Process instruction and set opcode/cycle
     // COMBINED METHOD: Process instruction, convert to opcode, update registers
    public String[] processAndConvertInstruction(String firstWord, String secondWord, registre reg) {
        String mode = determineMode(secondWord);
        String opcode = "00";
        String cleanedOperand = secondWord;
        this.reg = reg; // STORE THE REGISTER FOR LATER USE


        
        if (firstWord.equals("LDA")) {
            if (mode.equals(immediat)) {
                opcode = "86";
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                reg.setA(cleanedOperand);

                 // Store data for later flag calculation (don't calculate now)
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = Integer.parseInt(cleanedOperand, 16);
                lastInstructionFlags = new String[]{"Z", "N", "V"};

            }
        } else if (firstWord.equals("LDB")) {
            if (mode.equals(immediat)) {
                opcode = "C6";
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                reg.setB(cleanedOperand);

                // Store data for later flag calculation (don't calculate now)
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = Integer.parseInt(cleanedOperand, 16);
                lastInstructionFlags = new String[]{"Z", "N","V"};
            
            }
        } else if (firstWord.equals("LDD")) {
            if (mode.equals(immediat)) {
                opcode = "CC";
                cycle = 3;
                cleanedOperand = secondWord.replace("#$", "");
                reg.setD(cleanedOperand);

                // Store data for later flag calculation (don't calculate now)
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = Integer.parseInt(cleanedOperand, 16);
                lastInstructionFlags = new String[]{"Z", "N","V"};
            
            }
        }
        else if (firstWord.equals("LDS")) {
            if (mode.equals(immediat)) {
                opcode = "10CE";
                cycle = 4;
                cleanedOperand = secondWord.replace("#$", "");
                reg.setS(cleanedOperand);

                // Store data for later flag calculation (don't calculate now)
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = Integer.parseInt(cleanedOperand, 16);
                lastInstructionFlags = new String[]{"Z", "N","V"};
            
            }
        }
        else if (firstWord.equals("LDU")) {
            if (mode.equals(immediat)) {
                opcode = "CE";
                cycle = 3;
                cleanedOperand = secondWord.replace("#$", "");
                reg.setU(cleanedOperand);

                // Store data for later flag calculation (don't calculate now)
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = Integer.parseInt(cleanedOperand, 16);
                lastInstructionFlags = new String[]{"Z", "N","V"};
            
            }
        }else if (firstWord.equals("LDX")) {
            if (mode.equals(immediat)) {
                opcode = "8E";
                cycle = 3;
                cleanedOperand = secondWord.replace("#$", "");
                reg.setX(cleanedOperand);

                // Store data for later flag calculation (don't calculate now)
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = Integer.parseInt(cleanedOperand, 16);
                lastInstructionFlags = new String[]{"Z", "N","V"};
            
            }
        } 
        else if (firstWord.equals("LDY")) {
            if (mode.equals(immediat)) {
                opcode = "108E";
                cycle = 4;
                cleanedOperand = secondWord.replace("#$", "");
                reg.setY(cleanedOperand);

                // Store data for later flag calculation (don't calculate now)
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = Integer.parseInt(cleanedOperand, 16);
                lastInstructionFlags = new String[]{"Z", "N","V"};
            
            }
        }else if (firstWord.equals("SUBA")) {
            if (mode.equals(immediat)) {
                opcode = "80";
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performSubtractionA(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[]{"Z", "N", "V", "C"};
            }
        } else if (firstWord.equals("SUBB")) {
            if (mode.equals(immediat)) {
                opcode = "C0";
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performSubtractionB(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[]{"Z", "N", "V", "C"};
            }
        } else if (firstWord.equals("SUBD")) {
            if (mode.equals(immediat)) {
                opcode = "83";
                cycle = 4;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performSubtractionD(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[]{"Z", "N", "V", "C"};
            }
        } // NEW COMPARE INSTRUCTIONS
        else if (firstWord.equals("CMPA")) {
            if (mode.equals(immediat)) {
                opcode = "81";
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performCompareA(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[]{"Z", "N", "V", "C"};
            }
        } else if (firstWord.equals("CMPB")) {
            if (mode.equals(immediat)) {
                opcode = "C1";
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performCompareB(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[]{"Z", "N", "V", "C"};
            }
        } else if (firstWord.equals("CMPD")) {
            if (mode.equals(immediat)) {
                opcode = "1083";
                cycle = 5;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performCompareD(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[]{"Z", "N", "V", "C"};
            }
        } else if (firstWord.equals("CMPS")) {
            if (mode.equals(immediat)) {
                opcode = "118C";
                cycle = 5;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performCompareS(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[]{"Z", "N", "V", "C"};
            }
        } else if (firstWord.equals("CMPU")) {
            if (mode.equals(immediat)) {
                opcode = "1183";
                cycle = 5;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performCompareU(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[]{"Z", "N", "V", "C"};
            }
        } else if (firstWord.equals("CMPX")) {
            if (mode.equals(immediat)) {
                opcode = "8C";
                cycle = 4;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performCompareX(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[]{"Z", "N", "V", "C"};
            }
        } else if (firstWord.equals("CMPY")) {
            if (mode.equals(immediat)) {
                opcode = "108C";
                cycle = 5;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performCompareY(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[]{"Z", "N", "V", "C"};
            }
        }// === ADD INSTRUCTIONS ===
    else if (firstWord.equals("ADDA")) {
        if (mode.equals(immediat)) {
            opcode = "8B";
            cycle = 2;
            cleanedOperand = secondWord.replace("#$", "");
            int result = performAdditionA(cleanedOperand, reg);
            lastInstructionHex = cleanedOperand;
            lastInstructionResult = result;
            lastInstructionFlags = new String[]{"Z", "N", "V", "C"};
        }
    } else if (firstWord.equals("ADDB")) {
        if (mode.equals(immediat)) {
            opcode = "CB";
            cycle = 2;
            cleanedOperand = secondWord.replace("#$", "");
            int result = performAdditionB(cleanedOperand, reg);
            lastInstructionHex = cleanedOperand;
            lastInstructionResult = result;
            lastInstructionFlags = new String[]{"Z", "N", "V", "C"};
        }
    } else if (firstWord.equals("ADDD")) {
        if (mode.equals(immediat)) {
            opcode = "C3";
            cycle = 4;
            cleanedOperand = secondWord.replace("#$", "");
            int result = performAdditionD(cleanedOperand, reg);
            lastInstructionHex = cleanedOperand;
            lastInstructionResult = result;
            lastInstructionFlags = new String[]{"Z", "N", "V", "C"};
        }
    }
    
    // === AND INSTRUCTIONS ===
    else if (firstWord.equals("ANDA")) {
        if (mode.equals(immediat)) {
            opcode = "84";
            cycle = 2;
            cleanedOperand = secondWord.replace("#$", "");
            int result = performAndA(cleanedOperand, reg);
            lastInstructionHex = cleanedOperand;
            lastInstructionResult = result;
            lastInstructionFlags = new String[]{"Z", "N", "V"};
        }
    } else if (firstWord.equals("ANDB")) {
        if (mode.equals(immediat)) {
            opcode = "C4";
            cycle = 2;
            cleanedOperand = secondWord.replace("#$", "");
            int result = performAndB(cleanedOperand, reg);
            lastInstructionHex = cleanedOperand;
            lastInstructionResult = result;
            lastInstructionFlags = new String[]{"Z", "N", "V"};
        }
    } else if (firstWord.equals("ANDCC")) {
        if (mode.equals(immediat)) {
            opcode = "1C";
            cycle = 3;
            cleanedOperand = secondWord.replace("#$", "");
            int result = performAndCC(cleanedOperand, reg);
            lastInstructionHex = cleanedOperand;
            lastInstructionResult = result;
            lastInstructionFlags = new String[]{"Z", "N", "V"};
        }
    }
    
    // === OR INSTRUCTIONS ===
    else if (firstWord.equals("ORA")) {
        if (mode.equals(immediat)) {
            opcode = "8A";
            cycle = 2;
            cleanedOperand = secondWord.replace("#$", "");
            int result = performOrA(cleanedOperand, reg);
            lastInstructionHex = cleanedOperand;
            lastInstructionResult = result;
            lastInstructionFlags = new String[]{"Z", "N", "V"};
        }
    } else if (firstWord.equals("ORB")) {
        if (mode.equals(immediat)) {
            opcode = "CA";
            cycle = 2;
            cleanedOperand = secondWord.replace("#$", "");
            int result = performOrB(cleanedOperand, reg);
            lastInstructionHex = cleanedOperand;
            lastInstructionResult = result;
            lastInstructionFlags = new String[]{"Z", "N", "V"};
        }
    } else if (firstWord.equals("ORCC")) {
        if (mode.equals(immediat)) {
            opcode = "1A";
            cycle = 3;
            cleanedOperand = secondWord.replace("#$", "");
            int result = performOrCC(cleanedOperand, reg);
            lastInstructionHex = cleanedOperand;
            lastInstructionResult = result;
            lastInstructionFlags = new String[]{"Z", "N", "V"};
        }
    }
    
    // === EOR INSTRUCTIONS ===
    else if (firstWord.equals("EORA")) {
        if (mode.equals(immediat)) {
            opcode = "88";
            cycle = 2;
            cleanedOperand = secondWord.replace("#$", "");
            int result = performEorA(cleanedOperand, reg);
            lastInstructionHex = cleanedOperand;
            lastInstructionResult = result;
            lastInstructionFlags = new String[]{"Z", "N", "V"};
        }
    } else if (firstWord.equals("EORB")) {
        if (mode.equals(immediat)) {
            opcode = "C8";
            cycle = 2;
            cleanedOperand = secondWord.replace("#$", "");
            int result = performEorB(cleanedOperand, reg);
            lastInstructionHex = cleanedOperand;
            lastInstructionResult = result;
            lastInstructionFlags = new String[]{"Z", "N", "V"};
        }
    }

    // === ADD WITH CARRY INSTRUCTIONS ===
else if (firstWord.equals("ADCA")) {
    if (mode.equals(immediat)) {
        opcode = "89";  // ADCA immediate opcode
        cycle = 2;
        cleanedOperand = secondWord.replace("#$", "");
        int result = performAddWithCarryA(cleanedOperand, reg);
        lastInstructionHex = cleanedOperand;
        lastInstructionResult = result;
        lastInstructionFlags = new String[]{"Z", "N", "V", "C"};
    }
} else if (firstWord.equals("ADCB")) {
    if (mode.equals(immediat)) {
        opcode = "C9";  // ADCB immediate opcode
        cycle = 2;
        cleanedOperand = secondWord.replace("#$", "");
        int result = performAddWithCarryB(cleanedOperand, reg);
        lastInstructionHex = cleanedOperand;
        lastInstructionResult = result;
        lastInstructionFlags = new String[]{"Z", "N", "V", "C"};
    }
}
// === SUBTRACT WITH CARRY INSTRUCTIONS ===
else if (firstWord.equals("SBCA")) {
    if (mode.equals(immediat)) {
        opcode = "82";  // SBCA immediate opcode
        cycle = 2;
        cleanedOperand = secondWord.replace("#$", "");
        int result = performSubtractWithCarryA(cleanedOperand, reg);
        lastInstructionHex = cleanedOperand;
        lastInstructionResult = result;
        lastInstructionFlags = new String[]{"Z", "N", "V", "C"};
    }
} else if (firstWord.equals("SBCB")) {
    if (mode.equals(immediat)) {
        opcode = "C2";  // SBCB immediate opcode
        cycle = 2;
        cleanedOperand = secondWord.replace("#$", "");
        int result = performSubtractWithCarryB(cleanedOperand, reg);
        lastInstructionHex = cleanedOperand;
        lastInstructionResult = result;
        lastInstructionFlags = new String[]{"Z", "N", "V", "C"};
    }
}
    // === BIT TEST INSTRUCTIONS ===
else if (firstWord.equals("BITA")) {
    if (mode.equals(immediat)) {
        opcode = "85";  // BITA immediate opcode
        cycle = 2;
        cleanedOperand = secondWord.replace("#$", "");
        int result = performBitTestA(cleanedOperand, reg);
        lastInstructionHex = cleanedOperand;
        lastInstructionResult = result;
        lastInstructionFlags = new String[]{"Z", "N", "V"};
    }
} else if (firstWord.equals("BITB")) {
    if (mode.equals(immediat)) {
        opcode = "C5";  // BITB immediate opcode
        cycle = 2;
        cleanedOperand = secondWord.replace("#$", "");
        int result = performBitTestB(cleanedOperand, reg);
        lastInstructionHex = cleanedOperand;
        lastInstructionResult = result;
        lastInstructionFlags = new String[]{"Z", "N", "V"};
    }
}
    // === STACK OPERATIONS ===
else if (firstWord.equals("PSHS")) {
    if (mode.equals(immediat)) {
        opcode = "34";  // PSHS opcode
        cycle = 5;
        cleanedOperand = secondWord.replace("#$", "");
        int result = performPushS(cleanedOperand, reg);
        lastInstructionHex = cleanedOperand;
        lastInstructionResult = result;
        lastInstructionFlags = new String[]{}; // PSHS doesn't affect flags
    }
} else if (firstWord.equals("PSHU")) {
    if (mode.equals(immediat)) {
        opcode = "36";  // PSHU opcode
        cycle = 5;
        cleanedOperand = secondWord.replace("#$", "");
        int result = performPushU(cleanedOperand, reg);
        lastInstructionHex = cleanedOperand;
        lastInstructionResult = result;
        lastInstructionFlags = new String[]{}; // PSHU doesn't affect flags
    }
} else if (firstWord.equals("PULS")) {
    if (mode.equals(immediat)) {
        opcode = "35";  // PULS opcode
        cycle = 5;
        cleanedOperand = secondWord.replace("#$", "");
        int result = performPullS(cleanedOperand, reg);
        lastInstructionHex = cleanedOperand;
        lastInstructionResult = result;
        lastInstructionFlags = new String[]{}; // PULS may affect flags if pulling CCR
    }
} else if (firstWord.equals("PULU")) {
    if (mode.equals(immediat)) {
        opcode = "37";  // PULU opcode
        cycle = 5;
        cleanedOperand = secondWord.replace("#$", "");
        int result = performPullU(cleanedOperand, reg);
        lastInstructionHex = cleanedOperand;
        lastInstructionResult = result;
        lastInstructionFlags = new String[]{}; // PULU may affect flags if pulling CCR
    }
}

        else if (firstWord.equals("END")) {
            opcode = "3F";  // SWI instruction as END marker
            cleanedOperand = "";
        }
        // Add more instructions as needed
        
        // Update the instance variables
        this.op = opcode;
        
        
        
        return new String[]{opcode, cleanedOperand};
    }

    // === STACK OPERATION METHODS ===

// Simple stack simulation in memory (using RAM)
private void pushToMemory(String address, String value, ram memory) {
    // Convert address to integer
    int addr = Integer.parseInt(address, 16);
    
    // For 8-bit values
    if (value.length() == 2) {
        String addrStr = String.format("%04X", addr);
        memory.getram().put(addrStr, value);
    } 
    // For 16-bit values, store as two bytes
    else if (value.length() == 4) {
        String highByte = value.substring(0, 2);
        String lowByte = value.substring(2);
        
        String addrHigh = String.format("%04X", addr);
        String addrLow = String.format("%04X", addr + 1);
        
        memory.getram().put(addrHigh, highByte);
        memory.getram().put(addrLow, lowByte);
    }
}

private String pullFromMemory(String address, int bytes, ram memory) {
    int addr = Integer.parseInt(address, 16);
    
    if (bytes == 1) {
        String addrStr = String.format("%04X", addr);
        return memory.getram().get(addrStr);
    } else if (bytes == 2) {
        String addrHigh = String.format("%04X", addr);
        String addrLow = String.format("%04X", addr + 1);
        return memory.getram().get(addrHigh) + memory.getram().get(addrLow);
    }
    return "00";
}

// PSHS - Push onto S stack

// PSHU - Push onto U stack
private int performPushU(String operand, registre reg) {
    // Similar to PSHS but uses U stack pointer
    String registerSpec = operand.toUpperCase();
    String stackAddr = reg.getU();
    
    int uValue = Integer.parseInt(stackAddr, 16);
    
    switch (registerSpec) {
        case "A":
            pushToMemory(stackAddr, reg.getA(), this.ramMemory);
            uValue -= 1;
            break;
        case "B":
            pushToMemory(stackAddr, reg.getB(), this.ramMemory);
            uValue -= 1;
            break;
        case "CC":
            pushToMemory(stackAddr, reg.getCCR(), this.ramMemory);
            uValue -= 1;
            break;
        case "D":
            pushToMemory(stackAddr, reg.getD(), this.ramMemory);
            uValue -= 2;
            break;
        case "X":
            pushToMemory(stackAddr, reg.getX(), this.ramMemory);
            uValue -= 2;
            break;
        case "Y":
            pushToMemory(stackAddr, reg.getY(), this.ramMemory);
            uValue -= 2;
            break;
    }
    
    reg.setU(String.format("%04X", uValue));
    return uValue;
}

// PULS - Pull from S stack
private int performPullS(String operand, registre reg) {
    String registerSpec = operand.toUpperCase();
    
    // Current stack pointer (points to next free location)
    int sValue = Integer.parseInt(reg.getS(), 16);
    String stackAddr = String.format("%04X", sValue);
    
    switch (registerSpec) {
        case "A":
            String valueA = loadFromStack(stackAddr, 1); // ← CHANGE TO loadFromStack
            reg.setA(valueA);
            sValue += 1; // Increment AFTER loading
            break;
        case "B":
            String valueB = loadFromStack(stackAddr, 1); // ← CHANGE TO loadFromStack
            reg.setB(valueB);
            sValue += 1;
            break;
        case "CC":
            String valueCC = loadFromStack(stackAddr, 1); // ← CHANGE TO loadFromStack
            reg.setCCR(valueCC);
            sValue += 1;
            lastInstructionFlags = new String[]{"Z", "N", "V", "C"};
            break;
        case "D":
            String valueD = loadFromStack(stackAddr, 2); // ← CHANGE TO loadFromStack
            reg.setD(valueD);
            sValue += 2;
            break;
        case "X":
            String valueX = loadFromStack(stackAddr, 2); // ← CHANGE TO loadFromStack
            reg.setX(valueX);
            sValue += 2;
            break;
        case "Y":
            String valueY = loadFromStack(stackAddr, 2); // ← CHANGE TO loadFromStack
            reg.setY(valueY);
            sValue += 2;
            break;
    }
    
    reg.setS(String.format("%04X", sValue));
    return sValue;
}

// PULU - Pull from U stack
private int performPullU(String operand, registre reg) {
    String registerSpec = operand.toUpperCase();
    int uValue = Integer.parseInt(reg.getU(), 16);
    
    switch (registerSpec) {
        case "A":
            uValue += 1;
            String newAddr = String.format("%04X", uValue);
            String value = pullFromMemory(newAddr, 1, this.ramMemory);
            reg.setA(value);
            break;
        case "B":
            uValue += 1;
            newAddr = String.format("%04X", uValue);
            value = pullFromMemory(newAddr, 1, this.ramMemory);
            reg.setB(value);
            break;
        case "CC":
            uValue += 1;
            newAddr = String.format("%04X", uValue);
            value = pullFromMemory(newAddr, 1, this.ramMemory);
            reg.setCCR(value);
            lastInstructionFlags = new String[]{"Z", "N", "V", "C"};
            break;
        case "D":
            uValue += 2;
            newAddr = String.format("%04X", uValue);
            value = pullFromMemory(newAddr, 2, this.ramMemory);
            reg.setD(value);
            break;
        case "X":
            uValue += 2;
            newAddr = String.format("%04X", uValue);
            value = pullFromMemory(newAddr, 2, this.ramMemory);
            reg.setX(value);
            break;
        case "Y":
            uValue += 2;
            newAddr = String.format("%04X", uValue);
            value = pullFromMemory(newAddr, 2, this.ramMemory);
            reg.setY(value);
            break;
    }
    
    reg.setU(String.format("%04X", uValue));
    return uValue;
}


// Simple stack storage (using your existing RAM)
private void saveToStack(String stackPointer, String value) {
    // Convert hex address to integer
    int addr = Integer.parseInt(stackPointer, 16);
    
    // For 8-bit values
    if (value.length() == 2) {
        String addrStr = String.format("%04X", addr);
        ramMemory.getram().put(addrStr, value); // ← USE RAM
    }
    // For 16-bit values (2 bytes)
    else if (value.length() == 4) {
        String highByte = value.substring(0, 2);
        String lowByte = value.substring(2);
        
        String addrHigh = String.format("%04X", addr);
        String addrLow = String.format("%04X", addr + 1);
        
        // Store both bytes
        ramMemory.getram().put(addrHigh, highByte); // ← USE RAM
        ramMemory.getram().put(addrLow, lowByte);   // ← USE RAM
    }
}

private String loadFromStack(String stackPointer, int bytes) {
    int addr = Integer.parseInt(stackPointer, 16);
    
    if (bytes == 1) {
        String addrStr = String.format("%04X", addr);
        return ramMemory.getram().get(addrStr); // ← USE RAM
    } else if (bytes == 2) {
        String addrHigh = String.format("%04X", addr);
        String addrLow = String.format("%04X", addr + 1);
        String highByte = ramMemory.getram().get(addrHigh); // ← USE RAM
        String lowByte = ramMemory.getram().get(addrLow);   // ← USE RAM
        return highByte + lowByte;
    }
    return "00";
}

// In mode.java, add this field and setter:


public void setRam(ram ramMemory) {
    this.ramMemory = ramMemory;
}

// PSHS - Push onto S stack
private int performPushS(String operand, registre reg) {
    String registerSpec = operand.toUpperCase();
    String stackAddr = reg.getS(); // Current stack pointer
    
    // Decrement stack pointer FIRST (stack grows downward)
    int sValue = Integer.parseInt(stackAddr, 16);
    
    switch (registerSpec) {
        case "A":
            sValue -= 1; // 8-bit value
            String newAddr = String.format("%04X", sValue);
            saveToStack(newAddr, reg.getA()); // ← CHANGE TO saveToStack
            break;
        case "B":
            sValue -= 1;
            newAddr = String.format("%04X", sValue);
            saveToStack(newAddr, reg.getB()); // ← CHANGE TO saveToStack
            break;
        case "CC":
            sValue -= 1;
            newAddr = String.format("%04X", sValue);
            saveToStack(newAddr, reg.getCCR()); // ← CHANGE TO saveToStack
            break;
        case "D":
            sValue -= 2; // 16-bit value
            newAddr = String.format("%04X", sValue);
            saveToStack(newAddr, reg.getD()); // ← CHANGE TO saveToStack
            break;
        case "X":
            sValue -= 2;
            newAddr = String.format("%04X", sValue);
            saveToStack(newAddr, reg.getX()); // ← CHANGE TO saveToStack
            break;
        case "Y":
            sValue -= 2;
            newAddr = String.format("%04X", sValue);
            saveToStack(newAddr, reg.getY()); // ← CHANGE TO saveToStack
            break;
    }
    
    // Update stack pointer
    reg.setS(String.format("%04X", sValue));
    return sValue;
}
    // === BIT TEST METHODS ===
private int performBitTestA(String operand, registre reg) {
    int currentA = Integer.parseInt(reg.getA(), 16);
    int operandValue = Integer.parseInt(operand, 16);
    int result = currentA & operandValue; // Bitwise AND
    
    // IMPORTANT: BIT does NOT change the register!
    // Only sets flags based on the AND operation
    // Register A remains unchanged
    
    // Store for flag calculation
    return result; // This is the AND result for flags
}

private int performBitTestB(String operand, registre reg) {
    int currentB = Integer.parseInt(reg.getB(), 16);
    int operandValue = Integer.parseInt(operand, 16);
    int result = currentB & operandValue; // Bitwise AND
    
    // Register B remains unchanged
    return result;
}

    // === ADD WITH CARRY METHODS ===
private int performAddWithCarryA(String operand, registre reg) {
    int currentA = Integer.parseInt(reg.getA(), 16);
    int operandValue = Integer.parseInt(operand, 16);
    int carry = getCarryFlag(reg); // Need to get carry from CCR
    int result = currentA + operandValue + carry;
    
    // Handle 8-bit overflow
    if (result > 255) {
        result = result & 0xFF; // Keep only lower 8 bits
        setCarryFlag(reg, 1);   // Set carry flag
    } else {
        setCarryFlag(reg, 0);   // Clear carry flag
    }
    
    String resultHex = String.format("%02X", result);
    reg.setA(resultHex);
    return result;
}

private int performAddWithCarryB(String operand, registre reg) {
    int currentB = Integer.parseInt(reg.getB(), 16);
    int operandValue = Integer.parseInt(operand, 16);
    int carry = getCarryFlag(reg);
    int result = currentB + operandValue + carry;
    
    if (result > 255) {
        result = result & 0xFF;
        setCarryFlag(reg, 1);
    } else {
        setCarryFlag(reg, 0);
    }
    
    String resultHex = String.format("%02X", result);
    reg.setB(resultHex);
    return result;
}

// === SUBTRACT WITH CARRY METHODS ===
private int performSubtractWithCarryA(String operand, registre reg) {
    int currentA = Integer.parseInt(reg.getA(), 16);
    int operandValue = Integer.parseInt(operand, 16);
    int carry = getCarryFlag(reg);
    int result = currentA - operandValue - carry;
    
    // Handle 8-bit underflow (two's complement wrap-around)
    if (result < 0) {
        result += 256;
        setCarryFlag(reg, 1);  // Set carry flag (borrow occurred)
    } else {
        setCarryFlag(reg, 0);  // Clear carry flag
    }
    
    String resultHex = String.format("%02X", result & 0xFF);
    reg.setA(resultHex);
    return result;
}

private int performSubtractWithCarryB(String operand, registre reg) {
    int currentB = Integer.parseInt(reg.getB(), 16);
    int operandValue = Integer.parseInt(operand, 16);
    int carry = getCarryFlag(reg);
    int result = currentB - operandValue - carry;
    
    if (result < 0) {
        result += 256;
        setCarryFlag(reg, 1);
    } else {
        setCarryFlag(reg, 0);
    }
    
    String resultHex = String.format("%02X", result & 0xFF);
    reg.setB(resultHex);
    return result;
}

// === HELPER METHODS TO GET/SET CARRY FLAG ===
private int getCarryFlag(registre reg) {
    String ccr = reg.getCCR();
    // CCR bits: E F H I N Z V C (C is bit 0)
    // For now, if CCR is "00", carry is 0
    if (ccr.equals("00")) {
        return 0;
    }
    // Simple implementation: last character of CCR is carry
    // In real 6809, C is bit 0, but this is a simplification
    try {
        return Integer.parseInt(ccr.substring(ccr.length() - 1)) & 1;
    } catch (Exception e) {
        return 0;
    }
}

private void setCarryFlag(registre reg, int carry) {
    // Simplified: Just set CCR to carry value (0 or 1)
    // In real implementation, you'd only set bit 0
    if (carry == 1) {
        // Set CCR to "01" (carry flag set)
        reg.setCCR("01");
    } else {
        // Set CCR to "00" (carry flag clear)
        reg.setCCR("00");
    }
}

    private int performAdditionA(String operand, registre reg) {
    int currentA = Integer.parseInt(reg.getA(), 16);
    int operandValue = Integer.parseInt(operand, 16);
    int result = currentA + operandValue;
    
    // Handle 8-bit overflow
    if (result > 255) {
        result = result & 0xFF; // Keep only lower 8 bits
    }
    
    String resultHex = String.format("%02X", result);
    reg.setA(resultHex);
    return result;
}

private int performAdditionB(String operand, registre reg) {
    int currentB = Integer.parseInt(reg.getB(), 16);
    int operandValue = Integer.parseInt(operand, 16);
    int result = currentB + operandValue;
    
    if (result > 255) {
        result = result & 0xFF;
    }
    
    String resultHex = String.format("%02X", result);
    reg.setB(resultHex);
    return result;
}

private int performAdditionD(String operand, registre reg) {
    int currentD = Integer.parseInt(reg.getD(), 16);
    int operandValue = Integer.parseInt(operand, 16);
    int result = currentD + operandValue;
    
    // Handle 16-bit overflow
    if (result > 65535) {
        result = result & 0xFFFF;
    }
    
    String resultHex = String.format("%04X", result);
    reg.setD(resultHex);
    return result;
}

// === AND METHODS ===
private int performAndA(String operand, registre reg) {
    int currentA = Integer.parseInt(reg.getA(), 16);
    int operandValue = Integer.parseInt(operand, 16);
    int result = currentA & operandValue; // Bitwise AND
    
    String resultHex = String.format("%02X", result);
    reg.setA(resultHex);
    return result;
}

private int performAndB(String operand, registre reg) {
    int currentB = Integer.parseInt(reg.getB(), 16);
    int operandValue = Integer.parseInt(operand, 16);
    int result = currentB & operandValue;
    
    String resultHex = String.format("%02X", result);
    reg.setB(resultHex);
    return result;
}

private int performAndCC(String operand, registre reg) {
    int currentCC = Integer.parseInt(reg.getCCR(), 16);
    int operandValue = Integer.parseInt(operand, 16);
    int result = currentCC & operandValue;
    
    String resultHex = String.format("%02X", result);
    reg.setCCR(resultHex);
    return result;
}

// === OR METHODS ===
private int performOrA(String operand, registre reg) {
    int currentA = Integer.parseInt(reg.getA(), 16);
    int operandValue = Integer.parseInt(operand, 16);
    int result = currentA | operandValue; // Bitwise OR
    
    String resultHex = String.format("%02X", result);
    reg.setA(resultHex);
    return result;
}

private int performOrB(String operand, registre reg) {
    int currentB = Integer.parseInt(reg.getB(), 16);
    int operandValue = Integer.parseInt(operand, 16);
    int result = currentB | operandValue;
    
    String resultHex = String.format("%02X", result);
    reg.setB(resultHex);
    return result;
}

private int performOrCC(String operand, registre reg) {
    int currentCC = Integer.parseInt(reg.getCCR(), 16);
    int operandValue = Integer.parseInt(operand, 16);
    int result = currentCC | operandValue;
    
    String resultHex = String.format("%02X", result);
    reg.setCCR(resultHex);
    return result;
}

// === EOR (XOR) METHODS ===
private int performEorA(String operand, registre reg) {
    int currentA = Integer.parseInt(reg.getA(), 16);
    int operandValue = Integer.parseInt(operand, 16);
    int result = currentA ^ operandValue; // Bitwise XOR
    
    String resultHex = String.format("%02X", result);
    reg.setA(resultHex);
    return result;
}

private int performEorB(String operand, registre reg) {
    int currentB = Integer.parseInt(reg.getB(), 16);
    int operandValue = Integer.parseInt(operand, 16);
    int result = currentB ^ operandValue;
    
    String resultHex = String.format("%02X", result);
    reg.setB(resultHex);
    return result;
}

    private int performSubtractionA(String operand, registre reg) {
        int currentA = Integer.parseInt(reg.getA(), 16);
        int operandValue = Integer.parseInt(operand, 16);
        int result = currentA - operandValue;
        
        // Handle 8-bit overflow (keep result in 0-255 range)
        if (result < 0) {
            result += 256; // Two's complement wrap-around
        }
        
        String resultHex = String.format("%02X", result & 0xFF);
        reg.setA(resultHex);
        return result;
    }
    
    private int performSubtractionB(String operand, registre reg) {
        int currentB = Integer.parseInt(reg.getB(), 16);
        int operandValue = Integer.parseInt(operand, 16);
        int result = currentB - operandValue;
        
        // Handle 8-bit overflow
        if (result < 0) {
            result += 256;
        }
        
        String resultHex = String.format("%02X", result & 0xFF);
        reg.setB(resultHex);
        return result;
    }
    
    private int performSubtractionD(String operand, registre reg) {
        int currentD = Integer.parseInt(reg.getD(), 16);
        int operandValue = Integer.parseInt(operand, 16);
        int result = currentD - operandValue;
        
        // Handle 16-bit overflow
        if (result < 0) {
            result += 65536;
        }
        
        String resultHex = String.format("%04X", result & 0xFFFF);
        reg.setD(resultHex);
        return result;
    }

    // NEW COMPARE METHODS - These DON'T change registers, only calculate flags
    private int performCompareA(String operand, registre reg) {
        int currentA = Integer.parseInt(reg.getA(), 16);
        int operandValue = Integer.parseInt(operand, 16);
        int result = currentA - operandValue;
        
        // Note: Register A is NOT changed - only flags are set
        return result;
    }
    
    private int performCompareB(String operand, registre reg) {
        int currentB = Integer.parseInt(reg.getB(), 16);
        int operandValue = Integer.parseInt(operand, 16);
        int result = currentB - operandValue;
        
        // Register B is NOT changed
        return result;
    }
    
    private int performCompareD(String operand, registre reg) {
        int currentD = Integer.parseInt(reg.getD(), 16);
        int operandValue = Integer.parseInt(operand, 16);
        int result = currentD - operandValue;
        
        // Register D is NOT changed
        return result;
    }
    
    private int performCompareS(String operand, registre reg) {
        int currentS = Integer.parseInt(reg.getS(), 16);
        int operandValue = Integer.parseInt(operand, 16);
        int result = currentS - operandValue;
        
        // Register S is NOT changed
        return result;
    }
    
    private int performCompareU(String operand, registre reg) {
        int currentU = Integer.parseInt(reg.getU(), 16);
        int operandValue = Integer.parseInt(operand, 16);
        int result = currentU - operandValue;
        
        // Register U is NOT changed
        return result;
    }
    
    private int performCompareX(String operand, registre reg) {
        int currentX = Integer.parseInt(reg.getX(), 16);
        int operandValue = Integer.parseInt(operand, 16);
        int result = currentX - operandValue;
        
        // Register X is NOT changed
        return result;
    }
    
    private int performCompareY(String operand, registre reg) {
        int currentY = Integer.parseInt(reg.getY(), 16);
        int operandValue = Integer.parseInt(operand, 16);
        int result = currentY - operandValue;
        
        // Register Y is NOT changed
        return result;
    }

    
    
    // === ONLY ONE calculateSelectedFlags METHOD ===
    public void calculateSelectedFlags(String hexOperand, int result, String[] flagsToCalculate, registre reg) {
        System.out.println("\n=== CALCULATING SELECTED FLAGS ===");
        
        // FOR CMP INSTRUCTIONS: Show correct comparison flags
        if (flagsToCalculate.length == 4 && flagsToCalculate[3].equals("C")) {
            // This is a CMP instruction - get register value and compare
            int registerValue = 0;
            int operandValue = Integer.parseInt(hexOperand, 16);
            
            // Determine which register based on the opcode
            if (this.op.equals("81")) { // CMPA
                registerValue = Integer.parseInt(reg.getA(), 16);
            } else if (this.op.equals("C1")) { // CMPB
                registerValue = Integer.parseInt(reg.getB(), 16);
            } else if (this.op.equals("1083")) { // CMPD
                registerValue = Integer.parseInt(reg.getD(), 16);
            } else if (this.op.equals("118C")) { // CMPS
                registerValue = Integer.parseInt(reg.getS(), 16);
            } else if (this.op.equals("1183")) { // CMPU
                registerValue = Integer.parseInt(reg.getU(), 16);
            } else if (this.op.equals("8C")) { // CMPX
                registerValue = Integer.parseInt(reg.getX(), 16);
            } else if (this.op.equals("108C")) { // CMPY
                registerValue = Integer.parseInt(reg.getY(), 16);
            } else {
                // Default to A if unknown
                registerValue = Integer.parseInt(reg.getA(), 16);
            }
            
            // SHOW ONLY FLAGS
            System.out.print("Flags calculated: ");
            for (String flag : flagsToCalculate) {
                switch (flag) {
                    case "Z":
                        int zFlag = (registerValue == operandValue) ? 1 : 0;
                        System.out.print("Z:" + zFlag + " ");
                        break;
                    case "N":
                        int nFlag = (registerValue < operandValue) ? 1 : 0;
                        System.out.print("N:" + nFlag + " ");
                        break;
                    case "C":
                        int cFlag = (registerValue < operandValue) ? 1 : 0;
                        System.out.print("C:" + cFlag + " ");
                        break;
                    case "V":
                        int vFlag = 0;
                        System.out.print("V:" + vFlag + " ");
                        break;
                }
            }
            System.out.println();
            
        } else {
        // For non-CMP instructions - USE ACTUAL REGISTER VALUE FOR FLAGS
        int actualValue = 0;
        
        // Determine which register was affected based on opcode
        if (this.op.equals("86") || this.op.equals("8B") || this.op.equals("84") || 
            this.op.equals("8A") || this.op.equals("88") || this.op.equals("80")) { // A register operations
            actualValue = Integer.parseInt(reg.getA(), 16);
        } else if (this.op.equals("C6") || this.op.equals("CB") || this.op.equals("C4") || 
                   this.op.equals("CA") || this.op.equals("C8") || this.op.equals("C0")) { // B register operations
            actualValue = Integer.parseInt(reg.getB(), 16);
        } else if (this.op.equals("CC") || this.op.equals("C3") || this.op.equals("83")) { // D register operations
            actualValue = Integer.parseInt(reg.getD(), 16);
        } else {
            // Fallback to using the result parameter
            actualValue = result;
        }
        
        System.out.print("Flags calculated: ");
        for (String flag : flagsToCalculate) {
            switch (flag) {
                case "Z":
                    int zFlag = (actualValue == 0) ? 1 : 0;  // Zero if result is 0
                    System.out.print("Z:" + zFlag + " ");
                    break;
                case "N":
                    // For 8-bit operations, check if bit 7 is set (0x80)
                    if (actualValue <= 0xFF) {
                        int nFlag = ((actualValue & 0x80) != 0) ? 1 : 0;  // Negative if bit 7 is 1
                        System.out.print("N:" + nFlag + " ");
                    } else {
                        // For 16-bit operations, check if bit 15 is set (0x8000)
                        int nFlag = ((actualValue & 0x8000) != 0) ? 1 : 0;
                        System.out.print("N:" + nFlag + " ");
                    }
                    break;
                case "C":
                    // Carry flag - for now, set to 0 for logical operations
                    // For arithmetic operations, this would be different
                    int cFlag = 0;
                    System.out.print("C:" + cFlag + " ");
                    break;
                case "V":
                    // Overflow flag - set to 0 for most operations
                    int vFlag = 0;
                    System.out.print("V:" + vFlag + " ");
                    break;
            }
        }
        System.out.println();
    }
}
    
    // === ONLY ONE calculateLastInstructionFlags METHOD ===
    public void calculateLastInstructionFlags() {
        if (!lastInstructionHex.isEmpty() && this.reg != null) {
            calculateSelectedFlags(lastInstructionHex, lastInstructionResult, lastInstructionFlags, reg);
        } else {
            System.out.println("\n=== NO INSTRUCTIONS TO CALCULATE FLAGS ===");
        }
    }

    

    // Getters for the modes
    public String getImmediat() { return immediat; }
    public String getDirect() { return direct; }
    public String getIndexe() { return indexe; }
    public String getEtendu() { return etendu; }
    public String getInherent() { return inherent; }
}