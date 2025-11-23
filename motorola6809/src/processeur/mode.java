package processeur;

public class mode {
    
    private String immediat;
    private String direct;
    private String indexe;
    private String etendu;
    private String inherent;

    private String op ="00";
    private int cycle =0;

    private String lastInstructionHex = "";
    private int lastInstructionResult = 0;
    private String[] lastInstructionFlags = {};

    // Constructor to initialize modes
    public mode() {
        this.immediat = "Immediate";
        this.direct = "Direct";
        this.indexe = "Indexed";
        this.etendu = "Extended";
        this.inherent = "Inherent";
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
        }else if (firstWord.equals("END")) {
            opcode = "3F";  // SWI instruction as END marker
            cleanedOperand = "";
        }
        // Add more instructions as needed
        
        // Update the instance variables
        this.op = opcode;
        
        
        
        return new String[]{opcode, cleanedOperand};
    }


     public void calculateLastInstructionFlags() {
        if (!lastInstructionHex.isEmpty()) {
            calculateSelectedFlags(lastInstructionHex, lastInstructionResult, lastInstructionFlags);
        } else {
            System.out.println("\n=== NO INSTRUCTIONS TO CALCULATE FLAGS ===");
        }
    }
 
     public void calculateSelectedFlags(String hexOperand, int result, String[] flagsToCalculate) {
        System.out.println("\n=== CALCULATING SELECTED FLAGS ===");
        
        // Convert hex operand to binary and store it
        flagv1.convertAndStoreBinary(hexOperand);
        String paddedBinary = flagv1.getPaddedBinary();
        
        
        
        // Calculate only the requested flags
        char msb = paddedBinary.charAt(0); // Most significant bit
        
        System.out.print("Flags calculated: ");
        for (String flag : flagsToCalculate) {
            switch (flag) {
                case "Z":
                    int zFlag = flagv1.flag_Z(result);
                    System.out.print("Z:" + zFlag + " ");
                    break;
                case "N":
                    int nFlag = flagv1.flag_S(msb);
                    System.out.print("N:" + nFlag + " ");
                    break;
                case "C":
                    // For C flag, we need the actual bits - using simplified version
                    int cFlag = flagv1.flag_C(0, 0, 0); // You'll need to pass actual bit values
                    System.out.print("C:" + cFlag + " ");
                    break;
                case "V":
                    int vFlag = flagv1.flag_O(flagv1.getBinaryLength());
                    System.out.print("V:" + vFlag + " ");
                    break;
            }
        }
        // System.out.println("\nBinary length stored as int: " + flagv1.getBinaryLength());
    }

    // Getters for the modes
    public String getImmediat() { return immediat; }
    public String getDirect() { return direct; }
    public String getIndexe() { return indexe; }
    public String getEtendu() { return etendu; }
    public String getInherent() { return inherent; }
}










        
        

