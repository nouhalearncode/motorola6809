package processeur;

public class mode {
    
    private String immediat;
    private String direct;
    private String indexe;
    private String etendu;
    private String inherent;

    private String op ="00";
    private int cycle =0;

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
            }
        } else if (firstWord.equals("LDX")) {
            if (mode.equals(immediat)) {
                opcode = "CE";
                cycle = 3;
                cleanedOperand = secondWord.replace("#$", "");
                reg.setX(cleanedOperand);
            }
        } else if (firstWord.equals("END")) {
            opcode = "3F";  // SWI instruction as END marker
            cleanedOperand = "";
        }
        // Add more instructions as needed
        
        // Update the instance variables
        this.op = opcode;
        
        
        
        return new String[]{opcode, cleanedOperand};
    }

 


    // Getters for the modes
    public String getImmediat() { return immediat; }
    public String getDirect() { return direct; }
    public String getIndexe() { return indexe; }
    public String getEtendu() { return etendu; }
    public String getInherent() { return inherent; }
}










        
        

