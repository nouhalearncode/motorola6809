package processeur;

import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe de validation complète pour les instructions Motorola 6809
 * Vérifie la syntaxe, les modes d'adressage et la validité des instructions
 */
public class InstructionValidator {

    // All supported instructions in the Motorola 6809
    private static final Set<String> VALID_INSTRUCTIONS = new HashSet<>();

    // Map of instructions to their supported addressing modes
    private static final Map<String, Set<String>> INSTRUCTION_MODES = new HashMap<>();

    static {
        initializeInstructions();
        initializeSupportedModes();
    }

    private static void initializeInstructions() {
        // Load/Store instructions
        VALID_INSTRUCTIONS.add("LDA");
        VALID_INSTRUCTIONS.add("LDAA");
        VALID_INSTRUCTIONS.add("LDB");
        VALID_INSTRUCTIONS.add("LDAB");
        VALID_INSTRUCTIONS.add("LDD");
        VALID_INSTRUCTIONS.add("LDX");
        VALID_INSTRUCTIONS.add("LDY");
        VALID_INSTRUCTIONS.add("LDS");
        VALID_INSTRUCTIONS.add("LDU");

        VALID_INSTRUCTIONS.add("STA");
        VALID_INSTRUCTIONS.add("STAA");
        VALID_INSTRUCTIONS.add("STB");
        VALID_INSTRUCTIONS.add("STAB");
        VALID_INSTRUCTIONS.add("STD");
        VALID_INSTRUCTIONS.add("STX");
        VALID_INSTRUCTIONS.add("STY");
        VALID_INSTRUCTIONS.add("STS");
        VALID_INSTRUCTIONS.add("STU");

        // Arithmetic instructions
        VALID_INSTRUCTIONS.add("ADDA");
        VALID_INSTRUCTIONS.add("ADDB");
        VALID_INSTRUCTIONS.add("ADDD");
        VALID_INSTRUCTIONS.add("ADCA");
        VALID_INSTRUCTIONS.add("ADCB");

        VALID_INSTRUCTIONS.add("SUBA");
        VALID_INSTRUCTIONS.add("SUBB");
        VALID_INSTRUCTIONS.add("SUBD");
        VALID_INSTRUCTIONS.add("SBCA");
        VALID_INSTRUCTIONS.add("SBCB");

        // Logical instructions
        VALID_INSTRUCTIONS.add("ANDA");
        VALID_INSTRUCTIONS.add("ANDB");
        VALID_INSTRUCTIONS.add("ANDCC");
        VALID_INSTRUCTIONS.add("ORA");
        VALID_INSTRUCTIONS.add("ORB");
        VALID_INSTRUCTIONS.add("ORCC");
        VALID_INSTRUCTIONS.add("EORA");
        VALID_INSTRUCTIONS.add("EORB");

        // Compare instructions
        VALID_INSTRUCTIONS.add("CMPA");
        VALID_INSTRUCTIONS.add("CMPB");
        VALID_INSTRUCTIONS.add("CMPD");
        VALID_INSTRUCTIONS.add("CMPX");
        VALID_INSTRUCTIONS.add("CMPY");
        VALID_INSTRUCTIONS.add("CMPS");
        VALID_INSTRUCTIONS.add("CMPU");

        // Bit test instructions
        VALID_INSTRUCTIONS.add("BITA");
        VALID_INSTRUCTIONS.add("BITB");

        // Shift/Rotate instructions
        VALID_INSTRUCTIONS.add("ASLA");
        VALID_INSTRUCTIONS.add("ASLB");
        VALID_INSTRUCTIONS.add("ASL");
        VALID_INSTRUCTIONS.add("ASRA");
        VALID_INSTRUCTIONS.add("ASRB");
        VALID_INSTRUCTIONS.add("ASR");
        VALID_INSTRUCTIONS.add("LSLA");
        VALID_INSTRUCTIONS.add("LSLB");
        VALID_INSTRUCTIONS.add("LSL");
        VALID_INSTRUCTIONS.add("LSRA");
        VALID_INSTRUCTIONS.add("LSRB");
        VALID_INSTRUCTIONS.add("LSR");
        VALID_INSTRUCTIONS.add("ROLA");
        VALID_INSTRUCTIONS.add("ROLB");
        VALID_INSTRUCTIONS.add("ROL");
        VALID_INSTRUCTIONS.add("RORA");
        VALID_INSTRUCTIONS.add("RORB");
        VALID_INSTRUCTIONS.add("ROR");

        // Inc/Dec instructions
        VALID_INSTRUCTIONS.add("INCA");
        VALID_INSTRUCTIONS.add("INCB");
        VALID_INSTRUCTIONS.add("INC");
        VALID_INSTRUCTIONS.add("DECA");
        VALID_INSTRUCTIONS.add("DECB");
        VALID_INSTRUCTIONS.add("DEC");

        // Clear/Complement/Negate instructions
        VALID_INSTRUCTIONS.add("CLRA");
        VALID_INSTRUCTIONS.add("CLRB");
        VALID_INSTRUCTIONS.add("CLR");
        VALID_INSTRUCTIONS.add("COMA");
        VALID_INSTRUCTIONS.add("COMB");
        VALID_INSTRUCTIONS.add("COM");
        VALID_INSTRUCTIONS.add("NEGA");
        VALID_INSTRUCTIONS.add("NEGB");
        VALID_INSTRUCTIONS.add("NEG");

        // Test instructions
        VALID_INSTRUCTIONS.add("TSTA");
        VALID_INSTRUCTIONS.add("TSTB");
        VALID_INSTRUCTIONS.add("TST");

        // Branch instructions
        VALID_INSTRUCTIONS.add("BRA");
        VALID_INSTRUCTIONS.add("BRN");
        VALID_INSTRUCTIONS.add("BHI");
        VALID_INSTRUCTIONS.add("BLS");
        VALID_INSTRUCTIONS.add("BCC");
        VALID_INSTRUCTIONS.add("BCS");
        VALID_INSTRUCTIONS.add("BNE");
        VALID_INSTRUCTIONS.add("BEQ");
        VALID_INSTRUCTIONS.add("BVC");
        VALID_INSTRUCTIONS.add("BVS");
        VALID_INSTRUCTIONS.add("BPL");
        VALID_INSTRUCTIONS.add("BMI");
        VALID_INSTRUCTIONS.add("BGE");
        VALID_INSTRUCTIONS.add("BLT");
        VALID_INSTRUCTIONS.add("BGT");
        VALID_INSTRUCTIONS.add("BLE");

        // Jump/Subroutine instructions
        VALID_INSTRUCTIONS.add("JMP");
        VALID_INSTRUCTIONS.add("JSR");
        VALID_INSTRUCTIONS.add("RTS");
        VALID_INSTRUCTIONS.add("RTI");

        // LEA instructions
        VALID_INSTRUCTIONS.add("LEAX");
        VALID_INSTRUCTIONS.add("LEAY");
        VALID_INSTRUCTIONS.add("LEAS");
        VALID_INSTRUCTIONS.add("LEAU");

        // Stack instructions
        VALID_INSTRUCTIONS.add("PSHS");
        VALID_INSTRUCTIONS.add("PULS");
        VALID_INSTRUCTIONS.add("PSHU");
        VALID_INSTRUCTIONS.add("PULU");

        // Transfer/Exchange instructions
        VALID_INSTRUCTIONS.add("TFR");
        VALID_INSTRUCTIONS.add("EXG");

        // Special instructions
        VALID_INSTRUCTIONS.add("ABX");
        VALID_INSTRUCTIONS.add("DAA");
        VALID_INSTRUCTIONS.add("MUL");
        VALID_INSTRUCTIONS.add("SEX");
        VALID_INSTRUCTIONS.add("NOP");
        VALID_INSTRUCTIONS.add("SWI");
        VALID_INSTRUCTIONS.add("SWI2");
        VALID_INSTRUCTIONS.add("SWI3");
        VALID_INSTRUCTIONS.add("SYNC");
        VALID_INSTRUCTIONS.add("CWAI");

        // END instruction
        VALID_INSTRUCTIONS.add("END");
    }

    private static void initializeSupportedModes() {
        // LDA - All modes except inherent
        Set<String> ldaModes = new HashSet<>();
        ldaModes.add("IMMEDIATE");
        ldaModes.add("DIRECT");
        ldaModes.add("EXTENDED");
        ldaModes.add("INDEXED");
        INSTRUCTION_MODES.put("LDA", ldaModes);
        INSTRUCTION_MODES.put("LDAA", ldaModes);

        // LDB - All modes except inherent
        Set<String> ldbModes = new HashSet<>();
        ldbModes.add("IMMEDIATE");
        ldbModes.add("DIRECT");
        ldbModes.add("EXTENDED");
        ldbModes.add("INDEXED");
        INSTRUCTION_MODES.put("LDB", ldbModes);
        INSTRUCTION_MODES.put("LDAB", ldbModes);

        // LDD, LDX, LDY, LDS, LDU - Similar modes
        Set<String> ld16Modes = new HashSet<>();
        ld16Modes.add("IMMEDIATE");
        ld16Modes.add("DIRECT");
        ld16Modes.add("EXTENDED");
        ld16Modes.add("INDEXED");
        INSTRUCTION_MODES.put("LDD", ld16Modes);
        INSTRUCTION_MODES.put("LDX", ld16Modes);
        INSTRUCTION_MODES.put("LDY", ld16Modes);
        INSTRUCTION_MODES.put("LDS", ld16Modes);
        INSTRUCTION_MODES.put("LDU", ld16Modes);

        // Store instructions - No immediate
        Set<String> storeModes = new HashSet<>();
        storeModes.add("DIRECT");
        storeModes.add("EXTENDED");
        storeModes.add("INDEXED");
        INSTRUCTION_MODES.put("STA", storeModes);
        INSTRUCTION_MODES.put("STAA", storeModes);
        INSTRUCTION_MODES.put("STB", storeModes);
        INSTRUCTION_MODES.put("STAB", storeModes);
        INSTRUCTION_MODES.put("STD", storeModes);
        INSTRUCTION_MODES.put("STX", storeModes);
        INSTRUCTION_MODES.put("STY", storeModes);
        INSTRUCTION_MODES.put("STS", storeModes);
        INSTRUCTION_MODES.put("STU", storeModes);

        // Arithmetic - All modes
        Set<String> aluModes = new HashSet<>();
        aluModes.add("IMMEDIATE");
        aluModes.add("DIRECT");
        aluModes.add("EXTENDED");
        aluModes.add("INDEXED");
        INSTRUCTION_MODES.put("ADDA", aluModes);
        INSTRUCTION_MODES.put("ADDB", aluModes);
        INSTRUCTION_MODES.put("ADDD", aluModes);
        INSTRUCTION_MODES.put("SUBA", aluModes);
        INSTRUCTION_MODES.put("SUBB", aluModes);
        INSTRUCTION_MODES.put("SUBD", aluModes);
        INSTRUCTION_MODES.put("ANDA", aluModes);
        INSTRUCTION_MODES.put("ANDB", aluModes);
        INSTRUCTION_MODES.put("ORA", aluModes);
        INSTRUCTION_MODES.put("ORB", aluModes);
        INSTRUCTION_MODES.put("EORA", aluModes);
        INSTRUCTION_MODES.put("EORB", aluModes);
        INSTRUCTION_MODES.put("CMPA", aluModes);
        INSTRUCTION_MODES.put("CMPB", aluModes);
        INSTRUCTION_MODES.put("CMPD", aluModes);
        INSTRUCTION_MODES.put("CMPX", aluModes);
        INSTRUCTION_MODES.put("CMPY", aluModes);
        INSTRUCTION_MODES.put("BITA", aluModes);
        INSTRUCTION_MODES.put("BITB", aluModes);

        // Inherent mode only
        Set<String> inherentOnly = new HashSet<>();
        inherentOnly.add("INHERENT");
        INSTRUCTION_MODES.put("CLRA", inherentOnly);
        INSTRUCTION_MODES.put("CLRB", inherentOnly);
        INSTRUCTION_MODES.put("INCA", inherentOnly);
        INSTRUCTION_MODES.put("INCB", inherentOnly);
        INSTRUCTION_MODES.put("DECA", inherentOnly);
        INSTRUCTION_MODES.put("DECB", inherentOnly);
        INSTRUCTION_MODES.put("ASLA", inherentOnly);
        INSTRUCTION_MODES.put("ASLB", inherentOnly);
        INSTRUCTION_MODES.put("ASRA", inherentOnly);
        INSTRUCTION_MODES.put("ASRB", inherentOnly);
        INSTRUCTION_MODES.put("LSLA", inherentOnly);
        INSTRUCTION_MODES.put("LSLB", inherentOnly);
        INSTRUCTION_MODES.put("LSRA", inherentOnly);
        INSTRUCTION_MODES.put("LSRB", inherentOnly);
        INSTRUCTION_MODES.put("ROLA", inherentOnly);
        INSTRUCTION_MODES.put("ROLB", inherentOnly);
        INSTRUCTION_MODES.put("RORA", inherentOnly);
        INSTRUCTION_MODES.put("RORB", inherentOnly);
        INSTRUCTION_MODES.put("COMA", inherentOnly);
        INSTRUCTION_MODES.put("COMB", inherentOnly);
        INSTRUCTION_MODES.put("NEGA", inherentOnly);
        INSTRUCTION_MODES.put("NEGB", inherentOnly);
        INSTRUCTION_MODES.put("TSTA", inherentOnly);
        INSTRUCTION_MODES.put("TSTB", inherentOnly);
        INSTRUCTION_MODES.put("ABX", inherentOnly);
        INSTRUCTION_MODES.put("DAA", inherentOnly);
        INSTRUCTION_MODES.put("MUL", inherentOnly);
        INSTRUCTION_MODES.put("SEX", inherentOnly);
        INSTRUCTION_MODES.put("NOP", inherentOnly);
        INSTRUCTION_MODES.put("RTS", inherentOnly);
        INSTRUCTION_MODES.put("RTI", inherentOnly);

        // LEA - Indexed only
        Set<String> leaModes = new HashSet<>();
        leaModes.add("INDEXED");
        INSTRUCTION_MODES.put("LEAX", leaModes);
        INSTRUCTION_MODES.put("LEAY", leaModes);
        INSTRUCTION_MODES.put("LEAS", leaModes);
        INSTRUCTION_MODES.put("LEAU", leaModes);

        // JMP/JSR - Extended and Indexed
        Set<String> jmpModes = new HashSet<>();
        jmpModes.add("DIRECT");
        jmpModes.add("EXTENDED");
        jmpModes.add("INDEXED");
        INSTRUCTION_MODES.put("JMP", jmpModes);
        INSTRUCTION_MODES.put("JSR", jmpModes);
        // TFR/EXG - Transfer mode
        Set<String> transferModes = new HashSet<>();
        transferModes.add("TRANSFER");
        INSTRUCTION_MODES.put("TFR", transferModes);
        INSTRUCTION_MODES.put("EXG", transferModes);
    }

    /**
     * Main validation method
     * 
     * @param instruction The instruction mnemonic (e.g., "LDA", "STAA")
     * @param operand     The operand (e.g., "#$FF", "$2000", ",X")
     * @return null if valid, error message string if invalid
     */
    public static String validateInstruction(String instruction, String operand) {
        // Special case: END instruction
        if (instruction.toUpperCase().equals("END")) {
            return null; // Always valid
        }

        // 1. Check if instruction is uppercase
        if (!instruction.equals(instruction.toUpperCase())) {
            return "❌ ERREUR: L'instruction '" + instruction + "' doit être en MAJUSCULES.\n" +
                    "➡️ Correction suggérée: '" + instruction.toUpperCase() + "'";
        }

        // 2. Check if instruction exists
        if (!VALID_INSTRUCTIONS.contains(instruction)) {
            String suggestion = findSimilarInstruction(instruction);
            if (suggestion != null) {
                return "❌ ERREUR: Instruction inconnue '" + instruction + "'.\n" +
                        "➡️ Vouliez-vous dire '" + suggestion + "' ?";
            }
            return "❌ ERREUR: Instruction '" + instruction + "' non supportée.\n" +
                    "Cette instruction n'existe pas dans le Motorola 6809.";
        }

        // 3. Determine addressing mode
        String mode = determineAddressingMode(instruction, operand);

        if (mode.equals("UNKNOWN")) {
            return "❌ ERREUR: Mode d'adressage invalide pour l'opérande '" + operand + "'.\n" +
                    "Formats valides: #$XX (immédiat), $XX (direct), $XXXX (étendu), ,X (indexé)";
        }

        // 4. Check if this instruction supports this mode
        Set<String> supportedModes = INSTRUCTION_MODES.get(instruction);
        if (supportedModes == null) {
            return null; // If not in map, assume all modes are valid
        }

        if (!supportedModes.contains(mode)) {
            return "❌ ERREUR: L'instruction '" + instruction + "' ne supporte pas le mode " +
                    getModeDisplayName(mode) + ".\n" +
                    "➡️ Modes supportés: " + formatSupportedModes(supportedModes);
        }

        // 5. Validate operand syntax
        String syntaxError = validateOperandSyntax(instruction, operand, mode);
        if (syntaxError != null) {
            return syntaxError;
        }

        return null; // Valid
    }

    private static String determineAddressingMode(String instruction, String operand) {
        if (operand == null || operand.isEmpty()) {
            return "INHERENT";
        }

        operand = operand.trim();

        if (instruction.equals("TFR") || instruction.equals("EXG")) {
            return "TRANSFER";
        }

        if (operand.startsWith("#")) {
            return "IMMEDIATE";
        }

        if (operand.contains(",") || (operand.startsWith("[") && operand.endsWith("]"))) {
            return "INDEXED";
        }

        if (operand.startsWith("<") || (operand.startsWith("$") && operand.length() <= 3)) {
            return "DIRECT";
        }

        if (operand.startsWith(">") || (operand.startsWith("$") && operand.length() > 3)) {
            return "EXTENDED";
        }

        // Check for hex values
        if (operand.startsWith("$")) {
            int len = operand.substring(1).length();
            if (len <= 2)
                return "DIRECT";
            if (len <= 4)
                return "EXTENDED";
        }

        return "UNKNOWN";
    }

    private static String validateOperandSyntax(String instruction, String operand, String mode) {
        if (mode.equals("INHERENT")) {
            if (!operand.isEmpty()) {
                return "❌ ERREUR: L'instruction '" + instruction + "' (mode inhérent) ne doit pas avoir d'opérande.";
            }
            return null;
        }

        if (mode.equals("IMMEDIATE")) {
            if (!operand.startsWith("#$")) {
                return "❌ ERREUR: Le mode immédiat doit commencer par #$\n" +
                        "➡️ Exemple: #$FF pour la valeur FF";
            }
            String hexPart = operand.substring(2);
            if (!isValidHex(hexPart)) {
                return "❌ ERREUR: Valeur hexadécimale invalide: " + operand + "\n" +
                        "Les valeurs hexadécimales doivent contenir uniquement: 0-9, A-F";
            }
            return null;
        }

        if (mode.equals("DIRECT")) {
            String addr = operand.replace("<", "").replace("$", "");
            if (!isValidHex(addr)) {
                return "❌ ERREUR: Adresse hexadécimale invalide en mode direct: " + operand;
            }
            if (addr.length() > 2) {
                return "❌ ERREUR: Le mode direct n'accepte que des adresses de 2 chiffres hex (00-FF).\n" +
                        "➡️ Pour des adresses plus grandes, utilisez le mode étendu: $" + addr;
            }
            return null;
        }

        if (mode.equals("EXTENDED")) {
            String addr = operand.replace(">", "").replace("$", "");
            if (!isValidHex(addr)) {
                return "❌ ERREUR: Adresse hexadécimale invalide en mode étendu: " + operand;
            }
            if (addr.length() > 4) {
                return "❌ ERREUR: L'adresse en mode étendu ne peut pas dépasser 4 chiffres hex (0000-FFFF).";
            }
            return null;
        }

        if (mode.equals("INDEXED")) {
            String workingOperand = operand.trim();

            // Gérer les crochets d'indirection [ ... ]
            if (workingOperand.startsWith("[") && workingOperand.endsWith("]")) {
                workingOperand = workingOperand.substring(1, workingOperand.length() - 1).trim();
            }

            // Cas spécial : Indirection étendue [ $XXXX ] sans virgule
            if (!workingOperand.contains(",")) {
                if (workingOperand.startsWith("$")) {
                    String addr = workingOperand.substring(1);
                    if (!isValidHex(addr) || addr.length() > 4) {
                        return "❌ ERREUR: Adresse indirecte invalide: " + workingOperand;
                    }
                    return null;
                }
                return "❌ ERREUR: Le mode indexé doit contenir une virgule ou être une indirection étendue [$XXXX].\n" +
                        "➡️ Exemples: ,X  ou  5,Y  ou  A,X  ou  [,X++]  ou  [$1000]";
            }

            String[] parts = workingOperand.split(",");
            if (parts.length != 2) {
                return "❌ ERREUR: Syntaxe invalide pour le mode indexé: " + operand;
            }

            String register = parts[1].trim().toUpperCase();

            // Autorise: X, X+, X++, -X, --X, etc. (et PC/PCR)
            if (!register.matches("^-{0,2}[XYUS]\\+{0,2}$|^PC$|^PCR$")) {
                return "❌ ERREUR: Registre d'index ou mode auto-inc/dec invalide '" + register + "'.\n" +
                        "➡️ Formats valides: X, Y, U, S, PC, PCR";
            }
            return null;
        }

        if (mode.equals("TRANSFER")) {
            if (!operand.contains(",")) {
                return "❌ ERREUR: L'instruction " + instruction + " nécessite deux registres séparés par une virgule.\n"
                        +
                        "➡️ Exemple: " + instruction + " A,B";
            }
            String[] regs = operand.split(",");
            if (regs.length != 2) {
                return "❌ ERREUR: L'instruction " + instruction + " nécessite deux registres séparés par une virgule.\n"
                        +
                        "➡️ Exemple: " + instruction + " A,B";
            }
            String r1 = regs[0].trim().toUpperCase();
            String r2 = regs[1].trim().toUpperCase();

            String validRegs = "A|B|D|X|Y|U|S|PC|CCR|DP";
            if (!r1.matches(validRegs) || !r2.matches(validRegs)) {
                return "❌ ERREUR: Registre(s) invalide(s) pour " + instruction + ": " + operand + "\n" +
                        "➡️ Registres valides: A, B, D, X, Y, U, S, PC, CC, DP";
            }
            return null;
        }

        return null;
    }

    private static boolean isValidHex(String hex) {
        if (hex == null || hex.isEmpty()) {
            return false;
        }
        return hex.matches("[0-9A-Fa-f]+");
    }

    private static String findSimilarInstruction(String instruction) {
        int minDistance = Integer.MAX_VALUE;
        String bestMatch = null;

        for (String valid : VALID_INSTRUCTIONS) {
            int distance = levenshteinDistance(instruction.toUpperCase(), valid);
            if (distance < minDistance && distance <= 2) {
                minDistance = distance;
                bestMatch = valid;
            }
        }

        return bestMatch;
    }

    private static int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost);
            }
        }

        return dp[a.length()][b.length()];
    }

    private static String getModeDisplayName(String mode) {
        switch (mode) {
            case "IMMEDIATE":
                return "IMMÉDIAT";
            case "DIRECT":
                return "DIRECT";
            case "EXTENDED":
                return "ÉTENDU";
            case "INDEXED":
                return "INDEXÉ";
            case "INHERENT":
                return "INHÉRENT";
            case "TRANSFER":
                return "TRANSFERT";
            default:
                return mode;
        }
    }

    private static String formatSupportedModes(Set<String> modes) {
        StringBuilder sb = new StringBuilder();
        for (String m : modes) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(getModeDisplayName(m));
        }
        return sb.toString();
    }
}