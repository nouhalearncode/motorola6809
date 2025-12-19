package processeur;

public class mode {

    private String immediat;
    private String direct;
    private String indexe;
    private String etendu;
    private String inherent;

    private ram ramMemory;

    private String op = "00";
    private int cycle = 0;

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
            return inherent;
        }
        // PHASE 1: Détecter le mode indexé AVANT les autres modes
        // Le mode indexé se reconnaît par la présence d'une virgule ","
        if (secondWord.contains(",")) {
            return indexe; // Retourne "Indexed"
        }

        if (secondWord.charAt(0) == '#') {

            // immediat
            return immediat;

        }

        if (secondWord.charAt(0) == '>') {

            // etendu
            return etendu;

        }

        if (secondWord.charAt(0) == '<') {

            // direct
            return direct;
        }

        // PHASE 2: Détecter $XXXX (4 hex digits) comme mode Extended
        if (secondWord.startsWith("$") && secondWord.replace("$", "").length() == 4) {
            return etendu;
        }

        // Check for $XX (2 hex digits) = direct
    if (secondWord.startsWith("$") && secondWord.replace("$", "").length() == 2) {
        return direct;
    }

        return "Unknown mode";
    }

    // Process instruction and set opcode/cycle
    // COMBINED METHOD: Process instruction, convert to opcode, update registers
    public String[] processAndConvertInstruction(String firstWord, String secondWord, registre reg) {
        // Note: skipPCIncrement flag is NOT reset here - it's reset by pas.java after checking it
        // This ensures the flag persists until pas.java has a chance to check it
        
        String mode = determineMode(secondWord);
        String opcode = "00";
        String cleanedOperand = secondWord;
        this.reg = reg; // STORE THE REGISTER FOR LATER USE
        boolean pcChangedManually = false; // Flag to prevent automatic PC increment for control flow instructions

        if (firstWord.equals("LDA")) {
            // In the LDA section, add this after the indexed mode handling:
if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
        && secondWord.replace("$", "").length() == 4)) {
    opcode = "B6";  // LDA extended opcode
    cycle = 5;      // LDA extended takes 5 cycles
    String addr = secondWord.replace(">", "").replace("$", "");
    int address = Integer.parseInt(addr, 16);
    
    // Read from RAM
    String val = readFromRAM(address);
    
    // Load into A
    reg.setA(val);
    
    cleanedOperand = addr;

    // Store for flag calculation
    lastInstructionHex = val;
    lastInstructionResult = Integer.parseInt(val, 16);
    lastInstructionFlags = new String[] { "Z", "N", "V" };
    //ramMemory.getram().put("0000", val);
}

// In LDA section, add direct mode:
if (mode.equals(direct)) {
    opcode = "96";  // LDA direct opcode
    cycle = 3;      // LDA direct takes 3 cycles
    
    // Get address (remove < and $)
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF; // Force to page 0
    
    // Read from RAM at page 0 address
    String val = readFromRAM(address);
    
    // Load into A
    reg.setA(val);
    
    cleanedOperand = String.format("%02X", address);
    
    // Store for flag calculation
    lastInstructionHex = val;
    lastInstructionResult = Integer.parseInt(val, 16);
    lastInstructionFlags = new String[] { "Z", "N", "V" };
}
            if (mode.equals(immediat)) {
                opcode = "86";
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                reg.setA(cleanedOperand);

                // Store data for later flag calculation (don't calculate now)
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = Integer.parseInt(cleanedOperand, 16);
                lastInstructionFlags = new String[] { "Z", "N", "V" };

            }
            // PHASE 2/3: Support du mode indexé pour LDA (Upgraded)
            else if (mode.equals(indexe)) {
                String parseResult = parseIndexedMode(secondWord);
                String[] parts = parseResult.split(":");
                String type = parts[0];

                // PHASE 6: Indirect Flag Extraction (Fixed)
                boolean isIndirect = type.contains("_INDIRECT");
                String cleanType = isIndirect ? type.replace("_INDIRECT", "") : type;

                int effectiveAddr = 0;

                if (cleanType.equals("ACC_OFFSET")) { // Use cleanType
                    String acc = parts[1]; // A, B, ou D
                    String indexReg = parts[2]; // X, Y, U, S
                    // Pass isIndirect
                    effectiveAddr = calculateAccumulatorIndexed(acc, indexReg, isIndirect);
                    // Generate post-byte (Pass isIndirect)
                    cleanedOperand = generatePostByteAccOffset(acc, indexReg, isIndirect);
                } else if (!cleanType.equals("UNKNOWN")) {
                    // PHASE 2: Autres types (offsets, auto-inc/dec)
                    String register = parts[1];
                    int value = Integer.parseInt(parts[2]);
                    // Pass dirty type to calculateIndexedAddress (updated to handle it)
                    effectiveAddr = calculateIndexedAddress(type, register, value);
                    // Pass dirty type to generatePostByte (updated to handle it)
                    cleanedOperand = generatePostByte(type, register, value);

                    // PHASE 4 & 5: Ajouter les octets d'offset au post-byte
                    // Use cleanType for specific checks
                    if (cleanType.equals("OFFSET_8_BIT") || cleanType.equals("PC_REL_8_BIT")) {
                        String offsetHex = String.format("%02X", value & 0xFF);
                        cleanedOperand += offsetHex;
                    } else if (cleanType.equals("OFFSET_16_BIT") || cleanType.equals("PC_REL_16_BIT")) {
                        String offsetHex = String.format("%04X", value & 0xFFFF);
                        cleanedOperand += offsetHex;
                    }
                } else {
                    System.out.println("Mode indexé non supporté ou invalide: " + secondWord);
                    cleanedOperand = "";
                }

                if (!cleanType.equals("UNKNOWN")) {
                    opcode = "A6"; // LDA indexed opcode
                    cycle = 4;

                    // Lire la valeur en RAM
                    String val = readFromRAM(effectiveAddr);

                    // Charger dans A
                    reg.setA(val);

                    // Flags
                    lastInstructionHex = val;
                    lastInstructionResult = Integer.parseInt(val, 16);
                    lastInstructionFlags = new String[] { "Z", "N", "V" };
                }
            }
        } else if (firstWord.equals("LDB")) {
            // In the LDB section, add this after the indexed mode handling:
 if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
        && secondWord.replace("$", "").length() == 4)) {
    opcode = "F6";  // LDB extended opcode
    cycle = 5;      // LDB extended takes 5 cycles
    String addr = secondWord.replace(">", "").replace("$", "");
    int address = Integer.parseInt(addr, 16);
    
    // Read from RAM
    String val = readFromRAM(address);
    
    // Load into B
    reg.setB(val);
    
    cleanedOperand = addr;

    // Store for flag calculation
    lastInstructionHex = val;
    lastInstructionResult = Integer.parseInt(val, 16);
    lastInstructionFlags = new String[] { "Z", "N", "V" };
    //ramMemory.getram().put("0000", val);
}
// In LDB section, add direct mode:
if (mode.equals(direct)) {
    opcode = "D6";  // LDB direct opcode
    cycle = 3;      // LDB direct takes 3 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    String val = readFromRAM(address);
    reg.setB(val);
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = val;
    lastInstructionResult = Integer.parseInt(val, 16);
    lastInstructionFlags = new String[] { "Z", "N", "V" };
}
            if (mode.equals(immediat)) {
                opcode = "C6";
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                reg.setB(cleanedOperand);

                // Store data for later flag calculation (don't calculate now)
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = Integer.parseInt(cleanedOperand, 16);
                lastInstructionFlags = new String[] { "Z", "N", "V" };

            }
            // PHASE 2/3: Support du mode indexé pour LDB (Upgraded)
            else if (mode.equals(indexe)) {
                String parseResult = parseIndexedMode(secondWord);
                String[] parts = parseResult.split(":");
                String type = parts[0];

                // PHASE 6: Indirect Flag Extraction
                boolean isIndirect = type.contains("_INDIRECT");
                String cleanType = isIndirect ? type.replace("_INDIRECT", "") : type;

                int effectiveAddr = 0;

                if (cleanType.equals("ACC_OFFSET")) {
                    // PHASE 3: Offset accumulateur
                    String acc = parts[1]; // A, B, ou D
                    String indexReg = parts[2]; // X, Y, U, S
                    // Pass isIndirect
                    effectiveAddr = calculateAccumulatorIndexed(acc, indexReg, isIndirect);
                    // Generate post-byte (Pass isIndirect)
                    cleanedOperand = generatePostByteAccOffset(acc, indexReg, isIndirect);
                } else if (!cleanType.equals("UNKNOWN")) {
                    // PHASE 2: Autres types (offsets, auto-inc/dec)
                    String register = parts[1];
                    int value = Integer.parseInt(parts[2]);
                    // Pass dirty type (calculateIndexedAddress handles it)
                    effectiveAddr = calculateIndexedAddress(type, register, value);
                    // Pass dirty type (generatePostByte handles it)
                    cleanedOperand = generatePostByte(type, register, value);

                    // PHASE 4 & 5: Ajouter les octets d'offset au post-byte
                    if (cleanType.equals("OFFSET_8_BIT") || cleanType.equals("PC_REL_8_BIT")) {
                        String offsetHex = String.format("%02X", value & 0xFF);
                        cleanedOperand += offsetHex;
                    } else if (cleanType.equals("OFFSET_16_BIT") || cleanType.equals("PC_REL_16_BIT")) {
                        String offsetHex = String.format("%04X", value & 0xFFFF);
                        cleanedOperand += offsetHex;
                    }
                } else {
                    System.out.println("Mode indexé non supporté ou invalide: " + secondWord);
                    cleanedOperand = "";
                }

                if (!cleanType.equals("UNKNOWN")) {
                    opcode = "E6"; // LDB indexed opcode
                    cycle = 4;

                    String val = readFromRAM(effectiveAddr);

                    reg.setB(val);

                    lastInstructionHex = val;
                    lastInstructionResult = Integer.parseInt(val, 16);
                    lastInstructionFlags = new String[] { "Z", "N", "V" };
                }
            }
        } else if (firstWord.equals("STA")) {

            // In STA section, add direct mode:
if (mode.equals(direct)) {
    opcode = "97";  // STA direct opcode
    cycle = 4;      // STA direct takes 4 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    // Store A to memory
    writeToRAM(address, reg.getA());
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = reg.getA();
    lastInstructionResult = Integer.parseInt(reg.getA(), 16);
    lastInstructionFlags = new String[] { "Z", "N", "V" };

// But ensure you're actually using it!
writeToRAM(address, reg.getA());  // Should write to 0080, not 0000
}
            // Handle Extended mode: >$XXXX or $XXXX (4 hex digits, no comma)
            if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
                    && secondWord.replace("$", "").length() == 4)) {
                opcode = "B7";
                cycle = 5;
                String addr = secondWord.replace(">", "").replace("$", "");
                int address = Integer.parseInt(addr, 16);
                writeToRAM(address, reg.getA());
                cleanedOperand = addr;

                lastInstructionHex = reg.getA();
                lastInstructionResult = Integer.parseInt(reg.getA(), 16);
                lastInstructionFlags = new String[] { "Z", "N", "V" };
            } else if (mode.equals(indexe)) {
                String parseResult = parseIndexedMode(secondWord);
                String[] parts = parseResult.split(":");
                String type = parts[0];

                // PHASE 6: Indirect Flag Extraction
                boolean isIndirect = type.contains("_INDIRECT");
                String cleanType = isIndirect ? type.replace("_INDIRECT", "") : type;

                int effectiveAddr = 0;

                if (cleanType.equals("ACC_OFFSET")) { // Use cleanType
                    String acc = parts[1];
                    String indexReg = parts[2];
                    // Pass isIndirect
                    effectiveAddr = calculateAccumulatorIndexed(acc, indexReg, isIndirect);
                    // Generate post-byte (Pass isIndirect)
                    cleanedOperand = generatePostByteAccOffset(acc, indexReg, isIndirect);
                } else if (!cleanType.equals("UNKNOWN")) {
                    String register = parts[1];
                    int value = Integer.parseInt(parts[2]);
                    // Pass dirty type (calculateIndexedAddress handles it)
                    effectiveAddr = calculateIndexedAddress(type, register, value);
                    // Pass dirty type (generatePostByte handles it)
                    cleanedOperand = generatePostByte(type, register, value);

                    if (cleanType.equals("OFFSET_8_BIT") || cleanType.equals("PC_REL_8_BIT")) {
                        cleanedOperand += String.format("%02X", value & 0xFF);
                    } else if (cleanType.equals("OFFSET_16_BIT") || cleanType.equals("PC_REL_16_BIT")) {
                        cleanedOperand += String.format("%04X", value & 0xFFFF);
                    }
                } else {
                    cleanedOperand = "";
                }

                if (!cleanType.equals("UNKNOWN")) {
                    opcode = "A7";
                    cycle = 5;
                    writeToRAM(effectiveAddr, reg.getA());

                    lastInstructionHex = reg.getA();
                    lastInstructionResult = Integer.parseInt(reg.getA(), 16);
                    lastInstructionFlags = new String[] { "Z", "N", "V" };
                }
            }
        } else if (firstWord.equals("STB")) {

            // In STB section, add direct mode:
if (mode.equals(direct)) {
    opcode = "D7";  // STB direct opcode
    cycle = 4;      // STB direct takes 4 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    writeToRAM(address, reg.getB());
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = reg.getB();
    lastInstructionResult = Integer.parseInt(reg.getB(), 16);
    lastInstructionFlags = new String[] { "Z", "N", "V" };

    // But ensure you're actually using it!
writeToRAM(address, reg.getB());  // Should write to 0080, not 0000
}
            // Handle Extended mode: >$XXXX or $XXXX (4 hex digits, no comma)
            if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
                    && secondWord.replace("$", "").length() == 4)) {
                opcode = "F7";
                cycle = 5;
                String addr = secondWord.replace(">", "").replace("$", "");
                int address = Integer.parseInt(addr, 16);
                writeToRAM(address, reg.getB());
                cleanedOperand = addr;

                lastInstructionHex = reg.getB();
                lastInstructionResult = Integer.parseInt(reg.getB(), 16);
                lastInstructionFlags = new String[] { "Z", "N", "V" };
            } else if (mode.equals(indexe)) {
                String parseResult = parseIndexedMode(secondWord);
                String[] parts = parseResult.split(":");
                String type = parts[0];

                // PHASE 6: Indirect Flag Extraction
                boolean isIndirect = type.contains("_INDIRECT");
                String cleanType = isIndirect ? type.replace("_INDIRECT", "") : type;

                int effectiveAddr = 0;

                if (cleanType.equals("ACC_OFFSET")) {
                    String acc = parts[1];
                    String indexReg = parts[2];
                    effectiveAddr = calculateAccumulatorIndexed(acc, indexReg, isIndirect);
                    cleanedOperand = generatePostByteAccOffset(acc, indexReg, isIndirect);
                } else if (!cleanType.equals("UNKNOWN")) {
                    String register = parts[1];
                    int value = Integer.parseInt(parts[2]);
                    effectiveAddr = calculateIndexedAddress(type, register, value);
                    cleanedOperand = generatePostByte(type, register, value);

                    if (cleanType.equals("OFFSET_8_BIT") || cleanType.equals("PC_REL_8_BIT")) {
                        cleanedOperand += String.format("%02X", value & 0xFF);
                    } else if (cleanType.equals("OFFSET_16_BIT") || cleanType.equals("PC_REL_16_BIT")) {
                        cleanedOperand += String.format("%04X", value & 0xFFFF);
                    }
                }

                if (!cleanType.equals("UNKNOWN")) {
                    opcode = "E7";
                    cycle = 5;
                    writeToRAM(effectiveAddr, reg.getB());

                    lastInstructionHex = reg.getB();
                    lastInstructionResult = Integer.parseInt(reg.getB(), 16);
                    lastInstructionFlags = new String[] { "Z", "N", "V" };
                }
            }
        } else if (
                firstWord.equals("INC") || firstWord.equals("DEC") || firstWord.equals("CLR") || firstWord.equals("COM") ||
                firstWord.equals("NEG") || firstWord.equals("TST") || firstWord.equals("LSR") || firstWord.equals("LSL") ||
                firstWord.equals("ASR") || firstWord.equals("ASL") || firstWord.equals("ROL") || firstWord.equals("ROR")) {

            java.util.function.BiFunction<Integer, registre, Integer> op;
            String extOpcode = "00";
            String idxOpcode = "00";
            int extCycle = 6;
            int idxCycle = 6;

            switch (firstWord) {
                case "INC":
                    extOpcode = "7C"; idxOpcode = "6C";
                    op = this::performINC_Mem; break;
                case "DEC":
                    extOpcode = "7A"; idxOpcode = "6A";
                    op = this::performDEC_Mem; break;
                case "CLR":
                    extOpcode = "7F"; idxOpcode = "6F";
                    op = this::performCLR_Mem; break;
                case "COM":
                    extOpcode = "73"; idxOpcode = "63";
                    op = this::performCOM_Mem; break;
                case "NEG":
                    extOpcode = "70"; idxOpcode = "60";
                    op = this::performNEG_Mem; break;
                case "TST":
                    extOpcode = "7D"; idxOpcode = "6D";
                    op = this::performTST_Mem; break;
                case "LSR":
                    extOpcode = "74"; idxOpcode = "64";
                    op = this::performLSR_Mem; break;
                case "ASR":
                    extOpcode = "77"; idxOpcode = "67";
                    op = this::performASR_Mem; break;
                case "LSL": // alias of ASL
                case "ASL":
                    extOpcode = "78"; idxOpcode = "68";
                    op = this::performASL_Mem; break;
                case "ROL":
                    extOpcode = "79"; idxOpcode = "69";
                    op = this::performROL_Mem; break;
                case "ROR":
                    extOpcode = "76"; idxOpcode = "66";
                    op = this::performROR_Mem; break;
                default:
                    op = this::performTST_Mem; // fallback
            }

            return handleRMW(firstWord, secondWord, extOpcode, idxOpcode, extCycle, idxCycle, op);
        }
        
        // Add this case after the STS case and before the LDD case
else if (firstWord.equals("STD")) {

    // In STD section, add direct mode:
if (mode.equals(direct)) {
    opcode = "DD";  // STD direct opcode
    cycle = 5;      // STD direct takes 5 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    // Store 16-bit D register (2 bytes)
    writeToRAM16(address, reg.getD());
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = reg.getD();
    lastInstructionResult = Integer.parseInt(reg.getD(), 16);
    lastInstructionFlags = new String[] { "Z", "N", "V" };

    // But ensure you're actually using it!
writeToRAM(address, reg.getD());  // Should write to 0080, not 0000
}
    // Handle Extended mode: >$XXXX or $XXXX (4 hex digits, no comma)
    if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "FD";
        cycle = 6; // STD extended takes 6 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        writeToRAM16(address, reg.getD());
        cleanedOperand = addr;

        // Store data for flag calculation
        lastInstructionHex = reg.getD();
        lastInstructionResult = Integer.parseInt(reg.getD(), 16);
        lastInstructionFlags = new String[] { "Z", "N", "V" };
    }
    // PHASE 2/3: Support du mode indexé pour STD (Optional - add if needed)
    else if (mode.equals(indexe)) {
        String parseResult = parseIndexedMode(secondWord);
        String[] parts = parseResult.split(":");
        String type = parts[0];

        // PHASE 6: Indirect Flag Extraction
        boolean isIndirect = type.contains("_INDIRECT");
        String cleanType = isIndirect ? type.replace("_INDIRECT", "") : type;

        int effectiveAddr = 0;

        if (cleanType.equals("ACC_OFFSET")) {
            String acc = parts[1];
            String indexReg = parts[2];
            effectiveAddr = calculateAccumulatorIndexed(acc, indexReg, isIndirect);
            cleanedOperand = generatePostByteAccOffset(acc, indexReg, isIndirect);
        } else if (!cleanType.equals("UNKNOWN")) {
            String register = parts[1];
            int value = Integer.parseInt(parts[2]);
            effectiveAddr = calculateIndexedAddress(type, register, value);
            cleanedOperand = generatePostByte(type, register, value);

            if (cleanType.equals("OFFSET_8_BIT") || cleanType.equals("PC_REL_8_BIT")) {
                cleanedOperand += String.format("%02X", value & 0xFF);
            } else if (cleanType.equals("OFFSET_16_BIT") || cleanType.equals("PC_REL_16_BIT")) {
                cleanedOperand += String.format("%04X", value & 0xFFFF);
            }
        }

        if (!cleanType.equals("UNKNOWN")) {
            opcode = "ED"; // STD indexed opcode
            cycle = 7; // STD indexed takes 7 cycles
            writeToRAM16(effectiveAddr, reg.getD());

            lastInstructionHex = reg.getD();
            lastInstructionResult = Integer.parseInt(reg.getD(), 16);
            lastInstructionFlags = new String[] { "Z", "N", "V" };
        }
    }
}else if (firstWord.equals("STX")) {
    // In STX section, add direct mode:
if (mode.equals(direct)) {
    opcode = "9F";  // STX direct opcode
    cycle = 5;      // STX direct takes 5 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    writeToRAM16(address, reg.getX());
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = reg.getX();
    lastInstructionResult = Integer.parseInt(reg.getX(), 16);
    lastInstructionFlags = new String[] { "Z", "N", "V" };

    // But ensure you're actually using it!
writeToRAM(address, reg.getX());  // Should write to 0080, not 0000
}
            // Handle Extended mode: >$XXXX or $XXXX (4 hex digits, no comma)
            if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
                    && secondWord.replace("$", "").length() == 4)) {
                opcode = "BF";
                cycle = 6;
                String addr = secondWord.replace(">", "").replace("$", "");
                int address = Integer.parseInt(addr, 16);
                writeToRAM16(address, reg.getX());
                cleanedOperand = addr;

                lastInstructionHex = reg.getX();
                lastInstructionResult = Integer.parseInt(reg.getX(), 16);
                lastInstructionFlags = new String[] { "Z", "N", "V" };
            } else if (mode.equals(indexe)) {
                // Indexed STX logic
                String parseResult = parseIndexedMode(secondWord);
                String[] parts = parseResult.split(":");
                String type = parts[0];

                // PHASE 6: Indirect Flag Extraction
                boolean isIndirect = type.contains("_INDIRECT");
                String cleanType = isIndirect ? type.replace("_INDIRECT", "") : type;

                int effectiveAddr = 0;

                if (cleanType.equals("ACC_OFFSET")) {
                    String acc = parts[1];
                    String indexReg = parts[2];
                    effectiveAddr = calculateAccumulatorIndexed(acc, indexReg, isIndirect);
                    cleanedOperand = generatePostByteAccOffset(acc, indexReg, isIndirect);
                } else if (!cleanType.equals("UNKNOWN")) {
                    String register = parts[1];
                    int value = Integer.parseInt(parts[2]);
                    effectiveAddr = calculateIndexedAddress(type, register, value);
                    cleanedOperand = generatePostByte(type, register, value);

                    if (cleanType.equals("OFFSET_8_BIT") || cleanType.equals("PC_REL_8_BIT")) {
                        cleanedOperand += String.format("%02X", value & 0xFF);
                    } else if (cleanType.equals("OFFSET_16_BIT") || cleanType.equals("PC_REL_16_BIT")) {
                        cleanedOperand += String.format("%04X", value & 0xFFFF);
                    }
                }

                if (!cleanType.equals("UNKNOWN")) {
                    opcode = "AF";
                    cycle = 6;
                    writeToRAM16(effectiveAddr, reg.getX());

                    lastInstructionHex = reg.getX();
                    lastInstructionResult = Integer.parseInt(reg.getX(), 16);
                    lastInstructionFlags = new String[] { "Z", "N", "V" };
                }
            }
        } else if (firstWord.equals("STY")) {
            // In STX section, add direct mode:
if (mode.equals(direct)) {
    opcode = "9F";  // STX direct opcode
    cycle = 5;      // STX direct takes 5 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    writeToRAM16(address, reg.getY());
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = reg.getY();
    lastInstructionResult = Integer.parseInt(reg.getX(), 16);
    lastInstructionFlags = new String[] { "Z", "N", "V" };

    // But ensure you're actually using it!
writeToRAM(address, reg.getY());  // Should write to 0080, not 0000
}
            // Handle Extended mode: >$XXXX or $XXXX (4 hex digits, no comma)
            if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
                    && secondWord.replace("$", "").length() == 4)) {
                opcode = "10BF";
                cycle = 7;
                String addr = secondWord.replace(">", "").replace("$", "");
                int address = Integer.parseInt(addr, 16);
                writeToRAM16(address, reg.getY());
                cleanedOperand = addr;

                lastInstructionHex = reg.getY();
                lastInstructionResult = Integer.parseInt(reg.getY(), 16);
                lastInstructionFlags = new String[] { "Z", "N", "V" };
            } else if (mode.equals(indexe)) {
                // Indexed STY logic
                String parseResult = parseIndexedMode(secondWord);
                String[] parts = parseResult.split(":");
                String type = parts[0];

                // PHASE 6: Indirect Flag Extraction
                boolean isIndirect = type.contains("_INDIRECT");
                String cleanType = isIndirect ? type.replace("_INDIRECT", "") : type;

                int effectiveAddr = 0;

                if (cleanType.equals("ACC_OFFSET")) {
                    String acc = parts[1];
                    String indexReg = parts[2];
                    effectiveAddr = calculateAccumulatorIndexed(acc, indexReg, isIndirect);
                    cleanedOperand = generatePostByteAccOffset(acc, indexReg, isIndirect);
                } else if (!cleanType.equals("UNKNOWN")) {
                    String register = parts[1];
                    int value = Integer.parseInt(parts[2]);
                    effectiveAddr = calculateIndexedAddress(type, register, value);
                    cleanedOperand = generatePostByte(type, register, value);
                    // Append offsets...
                    if (cleanType.contains("8_BIT"))
                        cleanedOperand += String.format("%02X", value & 0xFF);
                    else if (cleanType.contains("16_BIT"))
                        cleanedOperand += String.format("%04X", value & 0xFFFF);
                }

                if (!cleanType.equals("UNKNOWN")) {
                    opcode = "10AF";
                    cycle = 7;
                    writeToRAM16(effectiveAddr, reg.getY());
                    lastInstructionHex = reg.getY();
                    lastInstructionResult = Integer.parseInt(reg.getY(), 16);
                    lastInstructionFlags = new String[] { "Z", "N", "V" };
                }
            }
        } else if (firstWord.equals("STU")) {

            // In STU section, add direct mode:
if (mode.equals(direct)) {
    opcode = "DF";  // STU direct opcode
    cycle = 5;      // STU direct takes 5 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    writeToRAM16(address, reg.getU());
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = reg.getU();
    lastInstructionResult = Integer.parseInt(reg.getU(), 16);
    lastInstructionFlags = new String[] { "Z", "N", "V" };

    // But ensure you're actually using it!
writeToRAM(address, reg.getU());  // Should write to 0080, not 0000
}
            // Handle Extended mode: >$XXXX or $XXXX (4 hex digits, no comma)
            if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
                    && secondWord.replace("$", "").length() == 4)) {
                opcode = "DF";
                cycle = 6;
                String addr = secondWord.replace(">", "").replace("$", "");
                int address = Integer.parseInt(addr, 16);
                writeToRAM16(address, reg.getU());
                cleanedOperand = addr;

                lastInstructionHex = reg.getU();
                lastInstructionResult = Integer.parseInt(reg.getU(), 16);
                lastInstructionFlags = new String[] { "Z", "N", "V" };
            } else if (mode.equals(indexe)) {
                // Indexed STU logic
                String parseResult = parseIndexedMode(secondWord);
                String[] parts = parseResult.split(":");
                String type = parts[0];

                // PHASE 6: Indirect Flag Extraction
                boolean isIndirect = type.contains("_INDIRECT");
                String cleanType = isIndirect ? type.replace("_INDIRECT", "") : type;

                int effectiveAddr = 0;

                if (cleanType.equals("ACC_OFFSET")) {
                    String acc = parts[1];
                    String indexReg = parts[2];
                    effectiveAddr = calculateAccumulatorIndexed(acc, indexReg, isIndirect);
                    cleanedOperand = generatePostByteAccOffset(acc, indexReg, isIndirect);
                } else if (!cleanType.equals("UNKNOWN")) {
                    String register = parts[1];
                    int value = Integer.parseInt(parts[2]);
                    effectiveAddr = calculateIndexedAddress(type, register, value);
                    cleanedOperand = generatePostByte(type, register, value);
                    if (cleanType.contains("8_BIT"))
                        cleanedOperand += String.format("%02X", value & 0xFF);
                    else if (cleanType.contains("16_BIT"))
                        cleanedOperand += String.format("%04X", value & 0xFFFF);
                }

                if (!cleanType.equals("UNKNOWN")) {
                    opcode = "CF"; // STU Indexed
                    cycle = 6;
                    writeToRAM16(effectiveAddr, reg.getU());
                    lastInstructionHex = reg.getU();
                    lastInstructionResult = Integer.parseInt(reg.getU(), 16);
                    lastInstructionFlags = new String[] { "Z", "N", "V" };
                }
            }
        } else if (firstWord.equals("STS")) {

            // In STS section, add direct mode:
if (mode.equals(direct)) {
    opcode = "10DF";  // STS direct opcode (prefix 10 + DF)
    cycle = 6;        // STS direct takes 6 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    writeToRAM16(address, reg.getS());
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = reg.getS();
    lastInstructionResult = Integer.parseInt(reg.getS(), 16);
    lastInstructionFlags = new String[] { "Z", "N", "V" };

    // But ensure you're actually using it!
writeToRAM(address, reg.getS());  // Should write to 0080, not 0000
}
            // Handle Extended mode: >$XXXX or $XXXX (4 hex digits, no comma)
            if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
                    && secondWord.replace("$", "").length() == 4)) {
                opcode = "10DF";
                cycle = 7;
                String addr = secondWord.replace(">", "").replace("$", "");
                int address = Integer.parseInt(addr, 16);
                writeToRAM16(address, reg.getS());
                cleanedOperand = addr;

                lastInstructionHex = reg.getS();
                lastInstructionResult = Integer.parseInt(reg.getS(), 16);
                lastInstructionFlags = new String[] { "Z", "N", "V" };
            } else if (mode.equals(indexe)) {
                // Indexed STS logic
                String parseResult = parseIndexedMode(secondWord);
                String[] parts = parseResult.split(":");
                String type = parts[0];

                // PHASE 6: Indirect Flag Extraction
                boolean isIndirect = type.contains("_INDIRECT");
                String cleanType = isIndirect ? type.replace("_INDIRECT", "") : type;

                int effectiveAddr = 0;

                if (cleanType.equals("ACC_OFFSET")) {
                    String acc = parts[1];
                    String indexReg = parts[2];
                    effectiveAddr = calculateAccumulatorIndexed(acc, indexReg, isIndirect);
                    cleanedOperand = generatePostByteAccOffset(acc, indexReg, isIndirect);
                } else if (!cleanType.equals("UNKNOWN")) {
                    String register = parts[1];
                    int value = Integer.parseInt(parts[2]);
                    effectiveAddr = calculateIndexedAddress(type, register, value);
                    cleanedOperand = generatePostByte(type, register, value);
                    if (cleanType.contains("8_BIT"))
                        cleanedOperand += String.format("%02X", value & 0xFF);
                    else if (cleanType.contains("16_BIT"))
                        cleanedOperand += String.format("%04X", value & 0xFFFF);
                }

                if (!cleanType.equals("UNKNOWN")) {
                    opcode = "10CF"; // STS Indexed
                    cycle = 7;
                    writeToRAM16(effectiveAddr, reg.getS());
                    lastInstructionHex = reg.getS();
                    lastInstructionResult = Integer.parseInt(reg.getS(), 16);
                    lastInstructionFlags = new String[] { "Z", "N", "V" };
                }
            }
        } else if (firstWord.equals("LDD")) {
            // In the LDD section, add this after the immediate mode handling:
 if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
        && secondWord.replace("$", "").length() == 4)) {
    opcode = "FC";  // LDD extended opcode
    cycle = 6;      // LDD extended takes 6 cycles
    String addr = secondWord.replace(">", "").replace("$", "");
    int address = Integer.parseInt(addr, 16);
    
    // Read 2 bytes from RAM
    String highByte = readFromRAM(address);
    String lowByte = readFromRAM(address + 1);
    String val = highByte + lowByte;
    
    // Load into D (which also updates A and B)
    reg.setD(val);
    
    cleanedOperand = addr;

    // Store for flag calculation
    lastInstructionHex = val;
    lastInstructionResult = Integer.parseInt(val, 16);
    lastInstructionFlags = new String[] { "Z", "N", "V" };
//     ramMemory.getram().put("0000", highByte);
// ramMemory.getram().put("0001", lowByte);
}

// In LDD section, add direct mode:
if (mode.equals(direct)) {
    opcode = "DC";  // LDD direct opcode
    cycle = 4;      // LDD direct takes 4 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    // Read 2 bytes from RAM
    String highByte = readFromRAM(address);
    String lowByte = readFromRAM(address + 1);
    String val = highByte + lowByte;
    
    // Load into D (which also updates A and B)
    reg.setD(val);
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = val;
    lastInstructionResult = Integer.parseInt(val, 16);
    lastInstructionFlags = new String[] { "Z", "N", "V" };
}
            if (mode.equals(immediat)) {
                opcode = "CC";
                cycle = 3;
                cleanedOperand = secondWord.replace("#$", "");
                reg.setD(cleanedOperand);

                // Store data for later flag calculation (don't calculate now)
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = Integer.parseInt(cleanedOperand, 16);
                lastInstructionFlags = new String[] { "Z", "N", "V" };

            }
            // PHASE: Support du mode indexé pour LDD
            else if (mode.equals(indexe)) {
                String[] result = handle16BitIndexedLoad(secondWord, "EC", 6, reg::setD);
                opcode = result[0];
                cycle = Integer.parseInt(result[1]);
                cleanedOperand = result[2];
            }
        } else if (firstWord.equals("LDS")) {
            // In the LDS section, add this after the immediate mode handling:
 if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
        && secondWord.replace("$", "").length() == 4)) {
    opcode = "10EE";  // LDS extended opcode (prefix 10 + EE)
    cycle = 7;        // LDS extended takes 7 cycles
    String addr = secondWord.replace(">", "").replace("$", "");
    int address = Integer.parseInt(addr, 16);
    
    // Read 2 bytes from RAM
    String highByte = readFromRAM(address);
    String lowByte = readFromRAM(address + 1);
    String val = highByte + lowByte;
    
    // Load into S
    reg.setS(val);
    
    cleanedOperand = addr;

    // Store for flag calculation
    lastInstructionHex = val;
    lastInstructionResult = Integer.parseInt(val, 16);
    lastInstructionFlags = new String[] { "Z", "N", "V" };
//     ramMemory.getram().put("0000", highByte);
// ramMemory.getram().put("0001", lowByte);
}

// In LDS section, add direct mode:
if (mode.equals(direct)) {
    opcode = "10DE";  // LDS direct opcode (prefix 10 + DE)
    cycle = 5;        // LDS direct takes 5 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    String highByte = readFromRAM(address);
    String lowByte = readFromRAM(address + 1);
    String val = highByte + lowByte;
    
    reg.setS(val);
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = val;
    lastInstructionResult = Integer.parseInt(val, 16);
    lastInstructionFlags = new String[] { "Z", "N", "V" };
}
            if (mode.equals(immediat)) {
                opcode = "10CE"; // Correct opcode for LDS immediate
                cycle = 4;
                cleanedOperand = secondWord.replace("#$", "");

                // FIXED: Actually set the S register!
                reg.setS(cleanedOperand);

                // Also update D register (since D = A:B and S affects D)
                String aVal = reg.getA();
                String bVal = reg.getB();
                String dVal = aVal + bVal;
                reg.setD(dVal);

                // Store for flag calculation
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = Integer.parseInt(cleanedOperand, 16);
                lastInstructionFlags = new String[] { "Z", "N", "V" };
            }
            // PHASE: Support du mode indexé pour LDS
            else if (mode.equals(indexe)) {
                String[] result = handle16BitIndexedLoad(secondWord, "10EE", 7, reg::setS);
                opcode = result[0];
                cycle = Integer.parseInt(result[1]);
                cleanedOperand = result[2];
            }
        } else if (firstWord.equals("LDU")) {
            // In the LDU section, add this after the immediate mode handling:
 if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
        && secondWord.replace("$", "").length() == 4)) {
    opcode = "EE";  // LDU extended opcode
    cycle = 6;      // LDU extended takes 6 cycles
    String addr = secondWord.replace(">", "").replace("$", "");
    int address = Integer.parseInt(addr, 16);
    
    // Read 2 bytes from RAM
    String highByte = readFromRAM(address);
    String lowByte = readFromRAM(address + 1);
    String val = highByte + lowByte;
    
    // Load into U
    reg.setU(val);
    
    cleanedOperand = addr;

    // Store for flag calculation
    lastInstructionHex = val;
    lastInstructionResult = Integer.parseInt(val, 16);
    lastInstructionFlags = new String[] { "Z", "N", "V" };
//     ramMemory.getram().put("0000", highByte);
// ramMemory.getram().put("0001", lowByte);
}
// In LDU section, add direct mode:
if (mode.equals(direct)) {
    opcode = "DE";  // LDU direct opcode
    cycle = 4;      // LDU direct takes 4 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    String highByte = readFromRAM(address);
    String lowByte = readFromRAM(address + 1);
    String val = highByte + lowByte;
    
    reg.setU(val);
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = val;
    lastInstructionResult = Integer.parseInt(val, 16);
    lastInstructionFlags = new String[] { "Z", "N", "V" };
}
            if (mode.equals(immediat)) {
                opcode = "CE";
                cycle = 3;
                cleanedOperand = secondWord.replace("#$", "");
                reg.setU(cleanedOperand);

                // Store data for later flag calculation (don't calculate now)
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = Integer.parseInt(cleanedOperand, 16);
                lastInstructionFlags = new String[] { "Z", "N", "V" };

            }
            // PHASE: Support du mode indexé pour LDU
            else if (mode.equals(indexe)) {
                String[] result = handle16BitIndexedLoad(secondWord, "EE", 6, reg::setU);
                opcode = result[0];
                cycle = Integer.parseInt(result[1]);
                cleanedOperand = result[2];
            }
        } else if (firstWord.equals("LDX")) {
            // In the LDX section, add this after the immediate mode handling:
 if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
        && secondWord.replace("$", "").length() == 4)) {
    opcode = "FE";  // LDX extended opcode
    cycle = 6;      // LDX extended takes 6 cycles
    String addr = secondWord.replace(">", "").replace("$", "");
    int address = Integer.parseInt(addr, 16);
    
    // Read 2 bytes from RAM
    String highByte = readFromRAM(address);
    String lowByte = readFromRAM(address + 1);
    String val = highByte + lowByte;
    
    // Load into X
    reg.setX(val);
    
    cleanedOperand = addr;

    // Store for flag calculation
    lastInstructionHex = val;
    lastInstructionResult = Integer.parseInt(val, 16);
    lastInstructionFlags = new String[] { "Z", "N", "V" };
//     ramMemory.getram().put("0000", highByte);
// ramMemory.getram().put("0001", lowByte);
}

// In LDX section, add direct mode:
if (mode.equals(direct)) {
    opcode = "9E";  // LDX direct opcode
    cycle = 4;      // LDX direct takes 4 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    // Read 2 bytes from RAM
    String highByte = readFromRAM(address);
    String lowByte = readFromRAM(address + 1);
    String val = highByte + lowByte;
    
    reg.setX(val);
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = val;
    lastInstructionResult = Integer.parseInt(val, 16);
    lastInstructionFlags = new String[] { "Z", "N", "V" };
}
            if (mode.equals(immediat)) {
                opcode = "8E";
                cycle = 3;
                cleanedOperand = secondWord.replace("#$", "");
                reg.setX(cleanedOperand);

                // Store data for later flag calculation (don't calculate now)
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = Integer.parseInt(cleanedOperand, 16);
                lastInstructionFlags = new String[] { "Z", "N", "V" };

            }
            // PHASE: Support du mode indexé pour LDX
            else if (mode.equals(indexe)) {
                String[] result = handle16BitIndexedLoad(secondWord, "AE", 6, reg::setX);
                opcode = result[0];
                cycle = Integer.parseInt(result[1]);
                cleanedOperand = result[2];
            }
        } else if (firstWord.equals("LDY")) {
            // In the LDY section, add this after the immediate mode handling:
 if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
        && secondWord.replace("$", "").length() == 4)) {
    opcode = "10FE";  // LDY extended opcode (prefix 10 + FE)
    cycle = 7;        // LDY extended takes 7 cycles
    String addr = secondWord.replace(">", "").replace("$", "");
    int address = Integer.parseInt(addr, 16);
    
    // Read 2 bytes from RAM
    String highByte = readFromRAM(address);
    String lowByte = readFromRAM(address + 1);
    String val = highByte + lowByte;
    
    // Load into Y
    reg.setY(val);
    
    cleanedOperand = addr;

    // Store for flag calculation
    lastInstructionHex = val;
    lastInstructionResult = Integer.parseInt(val, 16);
    lastInstructionFlags = new String[] { "Z", "N", "V" };
//     ramMemory.getram().put("0000", highByte);
// ramMemory.getram().put("0001", lowByte);
}

// In LDY section, add direct mode:
if (mode.equals(direct)) {
    opcode = "109E";  // LDY direct opcode (prefix 10 + 9E)
    cycle = 5;        // LDY direct takes 5 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    String highByte = readFromRAM(address);
    String lowByte = readFromRAM(address + 1);
    String val = highByte + lowByte;
    
    reg.setY(val);
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = val;
    lastInstructionResult = Integer.parseInt(val, 16);
    lastInstructionFlags = new String[] { "Z", "N", "V" };
}
            if (mode.equals(immediat)) {
                opcode = "108E";
                cycle = 4;
                cleanedOperand = secondWord.replace("#$", "");
                reg.setY(cleanedOperand);

                // Store data for later flag calculation (don't calculate now)
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = Integer.parseInt(cleanedOperand, 16);
                lastInstructionFlags = new String[] { "Z", "N", "V" };

            }
            // PHASE: Support du mode indexé pour LDY
            else if (mode.equals(indexe)) {
                String[] result = handle16BitIndexedLoad(secondWord, "10AE", 7, reg::setY);
                opcode = result[0];
                cycle = Integer.parseInt(result[1]);
                cleanedOperand = result[2];
            }
        } else if (firstWord.equals("SUBA")) {
            // Add after SUBA immediate/extended sections:
if (mode.equals(direct)) {
    opcode = "90";  // SUBA direct opcode
    cycle = 3;      // SUBA direct takes 3 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    String memVal = readFromRAM(address);
    int result = performSubtractionA(memVal, reg);
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = memVal;
    lastInstructionResult = result;
    lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
}
            if (mode.equals(immediat)) {
                opcode = "80";
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performSubtractionA(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
            }
            // ADD EXTENDED MODE
    else if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "B0";  // SUBA extended opcode
        cycle = 4;      // SUBA extended takes 4 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        String val = readFromRAM(address);
        
        int result = performSubtractionA(val, reg);
        
        cleanedOperand = addr;

        lastInstructionHex = val;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
            // PHASE: Support du mode indexé pour SUBA
            else if (mode.equals(indexe)) {
                String[] result = handleIndexedALU(secondWord, "A0", 4, this::performSubtractionA);
                opcode = result[0];
                cycle = Integer.parseInt(result[1]);
                cleanedOperand = result[2];
            }
        } else if (firstWord.equals("SUBB")) {
            // Add after SUBB immediate/extended sections:
if (mode.equals(direct)) {
    opcode = "D0";  // SUBB direct opcode
    cycle = 3;      // SUBB direct takes 3 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    String memVal = readFromRAM(address);
    int result = performSubtractionB(memVal, reg);
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = memVal;
    lastInstructionResult = result;
    lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
}
            if (mode.equals(immediat)) {
                opcode = "C0";
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performSubtractionB(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
            }
            // ADD EXTENDED MODE
    else if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "F0";  // SUBB extended opcode
        cycle = 4;      // SUBB extended takes 4 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        String val = readFromRAM(address);
        
        int result = performSubtractionB(val, reg);
        
        cleanedOperand = addr;

        lastInstructionHex = val;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
            // PHASE: Support du mode indexé pour SUBB
            else if (mode.equals(indexe)) {
                String[] result = handleIndexedALU(secondWord, "E0", 4, this::performSubtractionB);
                opcode = result[0];
                cycle = Integer.parseInt(result[1]);
                cleanedOperand = result[2];
            }
        } else if (firstWord.equals("SUBD")) {
            // Add after SUBD immediate/extended sections:
if (mode.equals(direct)) {
    opcode = "93";  // SUBD direct opcode
    cycle = 6;      // SUBD direct takes 6 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    // Read 2 bytes from memory
    String highByte = readFromRAM(address);
    String lowByte = readFromRAM(address + 1);
    String memVal = highByte + lowByte;
    
    int result = performSubtractionD(memVal, reg);
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = memVal;
    lastInstructionResult = result;
    lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
}
            if (mode.equals(immediat)) {
                opcode = "83";
                cycle = 4;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performSubtractionD(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
            }
            // ADD EXTENDED MODE
    else if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "B3";  // SUBD extended opcode
        cycle = 7;      // SUBD extended takes 7 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        // Read 2 bytes from RAM
        String highByte = readFromRAM(address);
        String lowByte = readFromRAM(address + 1);
        String val = highByte + lowByte;
        
        int result = performSubtractionD(val, reg);
        
        cleanedOperand = addr;

        lastInstructionHex = val;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
            // PHASE: Support du mode indexé pour SUBD
            else if (mode.equals(indexe)) {
                String[] result = handle16BitIndexedALU(secondWord, "A3", 6, this::performSubtractionD);
                opcode = result[0];
                cycle = Integer.parseInt(result[1]);
                cleanedOperand = result[2];
            }
        } // NEW COMPARE INSTRUCTIONS
        else if (firstWord.equals("CMPA")) {
            // Add after CMPA immediate/extended sections:
if (mode.equals(direct)) {
    opcode = "91";  // CMPA direct opcode
    cycle = 3;      // CMPA direct takes 3 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    // Read from memory
    String memVal = readFromRAM(address);
    int memValue = Integer.parseInt(memVal, 16);
    
    // Get A value
    int aValue = Integer.parseInt(reg.getA(), 16);
    
    // Calculate comparison (A - memory)
    int result = aValue - memValue;
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = memVal;
    lastInstructionResult = result;
    lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
}
            if (mode.equals(immediat)) {
                opcode = "81";
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performCompareA(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
            }
            // ADD EXTENDED MODE
    else if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "B1";  // CMPA extended opcode
        cycle = 5;      // CMPA extended takes 5 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        // Read from RAM
        String val = readFromRAM(address);
        
        // Compare A with memory value (don't change registers)
        int aValue = Integer.parseInt(reg.getA(), 16);
        int memValue = Integer.parseInt(val, 16);
        int result = aValue - memValue;  // For flag calculation
        
        cleanedOperand = addr;

        // Store for flag calculation
        lastInstructionHex = val;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
            // PHASE: Support du mode indexé pour CMPA
            else if (mode.equals(indexe)) {
                String[] result = handleIndexedALU(secondWord, "A1", 4, this::performCompareA);
                opcode = result[0];
                cycle = Integer.parseInt(result[1]);
                cleanedOperand = result[2];
            }
        } else if (firstWord.equals("CMPB")) {
            // Add after CMPB immediate/extended sections:
if (mode.equals(direct)) {
    opcode = "D1";  // CMPB direct opcode
    cycle = 3;      // CMPB direct takes 3 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    String memVal = readFromRAM(address);
    int memValue = Integer.parseInt(memVal, 16);
    int bValue = Integer.parseInt(reg.getB(), 16);
    int result = bValue - memValue;
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = memVal;
    lastInstructionResult = result;
    lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
}
            if (mode.equals(immediat)) {
                opcode = "C1";
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performCompareB(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
            }
            // ADD EXTENDED MODE
    else if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "F1";  // CMPB extended opcode
        cycle = 5;      // CMPB extended takes 5 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        String val = readFromRAM(address);
        
        int bValue = Integer.parseInt(reg.getB(), 16);
        int memValue = Integer.parseInt(val, 16);
        int result = bValue - memValue;
        
        cleanedOperand = addr;

        lastInstructionHex = val;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
            // PHASE: Support du mode indexé pour CMPB
            else if (mode.equals(indexe)) {
                String[] result = handleIndexedALU(secondWord, "E1", 4, this::performCompareB);
                opcode = result[0];
                cycle = Integer.parseInt(result[1]);
                cleanedOperand = result[2];
            }
        } else if (firstWord.equals("CMPD")) {
            // Add after CMPD immediate/extended sections:
if (mode.equals(direct)) {
    opcode = "1093";  // CMPD direct opcode (prefix 10 + 93)
    cycle = 5;        // CMPD direct takes 5 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    // Read 2 bytes from memory
    String highByte = readFromRAM(address);
    String lowByte = readFromRAM(address + 1);
    String memVal = highByte + lowByte;
    int memValue = Integer.parseInt(memVal, 16);
    
    // Get D value
    int dValue = Integer.parseInt(reg.getD(), 16);
    int result = dValue - memValue;
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = memVal;
    lastInstructionResult = result;
    lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
}
            if (mode.equals(immediat)) {
                opcode = "1083";
                cycle = 5;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performCompareD(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
            }
            // ADD EXTENDED MODE
    else if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "10B3";  // CMPD extended opcode (prefix 10 + B3)
        cycle = 7;        // CMPD extended takes 7 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        // Read 2 bytes from RAM
        String highByte = readFromRAM(address);
        String lowByte = readFromRAM(address + 1);
        String val = highByte + lowByte;
        
        int dValue = Integer.parseInt(reg.getD(), 16);
        int memValue = Integer.parseInt(val, 16);
        int result = dValue - memValue;
        
        cleanedOperand = addr;

        lastInstructionHex = val;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
            // PHASE: Support du mode indexé pour CMPD
            else if (mode.equals(indexe)) {
                String[] result = handle16BitIndexedALU(secondWord, "10A3", 7, this::performCompareD);
                opcode = result[0];
                cycle = Integer.parseInt(result[1]);
                cleanedOperand = result[2];
            }
        } else if (firstWord.equals("CMPS")) {
            // Add after CMPS immediate/extended sections:
if (mode.equals(direct)) {
    opcode = "119C";  // CMPS direct opcode (prefix 11 + 9C)
    cycle = 5;        // CMPS direct takes 5 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    String highByte = readFromRAM(address);
    String lowByte = readFromRAM(address + 1);
    String memVal = highByte + lowByte;
    int memValue = Integer.parseInt(memVal, 16);
    int sValue = Integer.parseInt(reg.getS(), 16);
    int result = sValue - memValue;
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = memVal;
    lastInstructionResult = result;
    lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
}
            if (mode.equals(immediat)) {
                opcode = "118C";
                cycle = 5;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performCompareS(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
            }
            // ADD EXTENDED MODE
    else if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "11BC";  // CMPS extended opcode (prefix 11 + BC)
        cycle = 7;        // CMPS extended takes 7 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        String highByte = readFromRAM(address);
        String lowByte = readFromRAM(address + 1);
        String val = highByte + lowByte;
        
        int sValue = Integer.parseInt(reg.getS(), 16);
        int memValue = Integer.parseInt(val, 16);
        int result = sValue - memValue;
        
        cleanedOperand = addr;

        lastInstructionHex = val;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
            // PHASE: Support du mode indexé pour CMPS
            else if (mode.equals(indexe)) {
                String[] result = handle16BitIndexedALU(secondWord, "11AC", 7, this::performCompareS);
                opcode = result[0];
                cycle = Integer.parseInt(result[1]);
                cleanedOperand = result[2];
            }
        } else if (firstWord.equals("CMPU")) {
            // Add after CMPU immediate/extended sections:
if (mode.equals(direct)) {
    opcode = "1193";  // CMPU direct opcode (prefix 11 + 93)
    cycle = 5;        // CMPU direct takes 5 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    String highByte = readFromRAM(address);
    String lowByte = readFromRAM(address + 1);
    String memVal = highByte + lowByte;
    int memValue = Integer.parseInt(memVal, 16);
    int uValue = Integer.parseInt(reg.getU(), 16);
    int result = uValue - memValue;
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = memVal;
    lastInstructionResult = result;
    lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
}
            if (mode.equals(immediat)) {
                opcode = "1183";
                cycle = 5;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performCompareU(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
            }
            else if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "11B3";  // CMPU extended opcode (prefix 11 + B3)
        cycle = 7;        // CMPU extended takes 7 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        String highByte = readFromRAM(address);
        String lowByte = readFromRAM(address + 1);
        String val = highByte + lowByte;
        
        int uValue = Integer.parseInt(reg.getU(), 16);
        int memValue = Integer.parseInt(val, 16);
        int result = uValue - memValue;
        
        cleanedOperand = addr;

        lastInstructionHex = val;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
            // PHASE: Support du mode indexé pour CMPU
            else if (mode.equals(indexe)) {
                String[] result = handle16BitIndexedALU(secondWord, "11A3", 7, this::performCompareU);
                opcode = result[0];
                cycle = Integer.parseInt(result[1]);
                cleanedOperand = result[2];
            }
        } else if (firstWord.equals("CMPX")) {
            // Add after CMPX immediate/extended sections:
if (mode.equals(direct)) {
    opcode = "9C";  // CMPX direct opcode
    cycle = 5;      // CMPX direct takes 5 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    String highByte = readFromRAM(address);
    String lowByte = readFromRAM(address + 1);
    String memVal = highByte + lowByte;
    int memValue = Integer.parseInt(memVal, 16);
    int xValue = Integer.parseInt(reg.getX(), 16);
    int result = xValue - memValue;
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = memVal;
    lastInstructionResult = result;
    lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
}
            if (mode.equals(immediat)) {
                opcode = "8C";
                cycle = 4;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performCompareX(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
            }
            // ADD EXTENDED MODE
    else if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "BC";  // CMPX extended opcode
        cycle = 6;      // CMPX extended takes 6 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        String highByte = readFromRAM(address);
        String lowByte = readFromRAM(address + 1);
        String val = highByte + lowByte;
        
        int xValue = Integer.parseInt(reg.getX(), 16);
        int memValue = Integer.parseInt(val, 16);
        int result = xValue - memValue;
        
        cleanedOperand = addr;

        lastInstructionHex = val;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
            // PHASE: Support du mode indexé pour CMPX
            else if (mode.equals(indexe)) {
                String[] result = handle16BitIndexedALU(secondWord, "AC", 6, this::performCompareX);
                opcode = result[0];
                cycle = Integer.parseInt(result[1]);
                cleanedOperand = result[2];
            }
        } else if (firstWord.equals("CMPY")) {
            // Add after CMPY immediate/extended sections:
if (mode.equals(direct)) {
    opcode = "109C";  // CMPY direct opcode (prefix 10 + 9C)
    cycle = 5;        // CMPY direct takes 5 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    String highByte = readFromRAM(address);
    String lowByte = readFromRAM(address + 1);
    String memVal = highByte + lowByte;
    int memValue = Integer.parseInt(memVal, 16);
    int yValue = Integer.parseInt(reg.getY(), 16);
    int result = yValue - memValue;
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = memVal;
    lastInstructionResult = result;
    lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
}
            if (mode.equals(immediat)) {
                opcode = "108C";
                cycle = 5;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performCompareY(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
            }
            // ADD EXTENDED MODE
    else if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "10BC";  // CMPY extended opcode (prefix 10 + BC)
        cycle = 7;        // CMPY extended takes 7 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        String highByte = readFromRAM(address);
        String lowByte = readFromRAM(address + 1);
        String val = highByte + lowByte;
        
        int yValue = Integer.parseInt(reg.getY(), 16);
        int memValue = Integer.parseInt(val, 16);
        int result = yValue - memValue;
        
        cleanedOperand = addr;

        lastInstructionHex = val;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
            // PHASE: Support du mode indexé pour CMPY
            else if (mode.equals(indexe)) {
                String[] result = handle16BitIndexedALU(secondWord, "10AC", 7, this::performCompareY);
                opcode = result[0];
                cycle = Integer.parseInt(result[1]);
                cleanedOperand = result[2];
            }
        } // === ADD INSTRUCTIONS ===
        else if (firstWord.equals("ADDA")) {
            // Add after ADDA immediate/extended sections:
if (mode.equals(direct)) {
    opcode = "9B";  // ADDA direct opcode
    cycle = 3;      // ADDA direct takes 3 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    String memVal = readFromRAM(address);
    int result = performAdditionA(memVal, reg);
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = memVal;
    lastInstructionResult = result;
    lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
}
            if (mode.equals(immediat)) {
                opcode = "8B";
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performAdditionA(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
            }
            // ADD EXTENDED MODE
    else if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "BB";  // ADDA extended opcode
        cycle = 4;      // ADDA extended takes 4 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        String val = readFromRAM(address);
        
        int result = performAdditionA(val, reg);
        
        cleanedOperand = addr;

        lastInstructionHex = val;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
            // PHASE: Support du mode indexé pour ADDA
            else if (mode.equals(indexe)) {
                String[] result = handleIndexedALU(secondWord, "AB", 4, this::performAdditionA);
                opcode = result[0];
                cycle = Integer.parseInt(result[1]);
                cleanedOperand = result[2];
            }
        } else if (firstWord.equals("ADDB")) {
            // Add after ADDB immediate/extended sections:
if (mode.equals(direct)) {
    opcode = "DB";  // ADDB direct opcode
    cycle = 3;      // ADDB direct takes 3 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    String memVal = readFromRAM(address);
    int result = performAdditionB(memVal, reg);
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = memVal;
    lastInstructionResult = result;
    lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
}
            if (mode.equals(immediat)) {
                opcode = "CB";
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performAdditionB(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
            }
            // ADD EXTENDED MODE
    else if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "FB";  // ADDB extended opcode
        cycle = 4;      // ADDB extended takes 4 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        String val = readFromRAM(address);
        
        int result = performAdditionB(val, reg);
        
        cleanedOperand = addr;

        lastInstructionHex = val;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
            // PHASE: Support du mode indexé pour ADDB
            else if (mode.equals(indexe)) {
                String[] result = handleIndexedALU(secondWord, "EB", 4, this::performAdditionB);
                opcode = result[0];
                cycle = Integer.parseInt(result[1]);
                cleanedOperand = result[2];
            }
        } else if (firstWord.equals("ADDD")) {
            // Add after ADDD immediate/extended sections:
if (mode.equals(direct)) {
    opcode = "D3";  // ADDD direct opcode
    cycle = 6;      // ADDD direct takes 6 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    // Read 2 bytes from memory
    String highByte = readFromRAM(address);
    String lowByte = readFromRAM(address + 1);
    String memVal = highByte + lowByte;
    
    int result = performAdditionD(memVal, reg);
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = memVal;
    lastInstructionResult = result;
    lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
}
            if (mode.equals(immediat)) {
                opcode = "C3";
                cycle = 4;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performAdditionD(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
            }
            // ADD EXTENDED MODE
    else if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "F3";  // ADDD extended opcode
        cycle = 7;      // ADDD extended takes 7 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        // Read 2 bytes from RAM
        String highByte = readFromRAM(address);
        String lowByte = readFromRAM(address + 1);
        String val = highByte + lowByte;
        
        int result = performAdditionD(val, reg);
        
        cleanedOperand = addr;

        lastInstructionHex = val;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
            // PHASE: Support du mode indexé pour ADDD
            else if (mode.equals(indexe)) {
                String[] result = handle16BitIndexedALU(secondWord, "E3", 6, this::performAdditionD);
                opcode = result[0];
                cycle = Integer.parseInt(result[1]);
                cleanedOperand = result[2];
            }
        }

        // === AND INSTRUCTIONS ===
        else if (firstWord.equals("ANDA")) {
            if (mode.equals(direct)) {
        opcode = "94";  // ANDA direct opcode
        cycle = 3;
        
        String addrStr = secondWord.replace("<", "").replace("$", "");
        int address = Integer.parseInt(addrStr, 16) & 0x00FF;
        
        String memVal = readFromRAM(address);
        int result = performAndA(memVal, reg);
        
        cleanedOperand = String.format("%02X", address);
        
        lastInstructionHex = memVal;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V" };
    }
            if (mode.equals(immediat)) {
                opcode = "84";
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performAndA(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V" };
            }
            // ADD EXTENDED MODE
    else if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "B4";  // ANDA extended opcode
        cycle = 4;      // ANDA extended takes 4 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        // Read from RAM
        String val = readFromRAM(address);
        
        // Logical AND with A
        int result = performAndA(val, reg);
        
        cleanedOperand = addr;

        // Store for flag calculation
        lastInstructionHex = val;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V" };
    }
            // PHASE: Support du mode indexé pour ANDA
            else if (mode.equals(indexe)) {
                String[] result = handleIndexedALU(secondWord, "A4", 4, this::performAndA);
                opcode = result[0];
                cycle = Integer.parseInt(result[1]);
                cleanedOperand = result[2];
            }
        } else if (firstWord.equals("ANDB")) {
            if (mode.equals(direct)) {
        opcode = "D4";  // ANDB direct opcode
        cycle = 3;
        
        String addrStr = secondWord.replace("<", "").replace("$", "");
        int address = Integer.parseInt(addrStr, 16) & 0x00FF;
        
        String memVal = readFromRAM(address);
        int result = performAndB(memVal, reg);
        
        cleanedOperand = String.format("%02X", address);
        
        lastInstructionHex = memVal;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V" };
    }
            if (mode.equals(immediat)) {
                opcode = "C4";
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performAndB(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V" };
            }
            // ADD EXTENDED MODE
    else if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "F4";  // ANDB extended opcode
        cycle = 4;      // ANDB extended takes 4 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        String val = readFromRAM(address);
        
        int result = performAndB(val, reg);
        
        cleanedOperand = addr;

        lastInstructionHex = val;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V" };
    }
            // PHASE: Support du mode indexé pour ANDB
            else if (mode.equals(indexe)) {
                String[] result = handleIndexedALU(secondWord, "E4", 4, this::performAndB);
                opcode = result[0];
                cycle = Integer.parseInt(result[1]);
                cleanedOperand = result[2];
            }
        } else if (firstWord.equals("ANDCC")) {
            if (mode.equals(immediat)) {
                opcode = "1C";
                cycle = 3;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performAndCC(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V" };
            }
        }

        // === OR INSTRUCTIONS ===
        else if (firstWord.equals("ORA")) {
            if (mode.equals(direct)) {
        opcode = "9A";  // ORA direct opcode
        cycle = 3;
        
        String addrStr = secondWord.replace("<", "").replace("$", "");
        int address = Integer.parseInt(addrStr, 16) & 0x00FF;
        
        String memVal = readFromRAM(address);
        int result = performOrA(memVal, reg);
        
        cleanedOperand = String.format("%02X", address);
        
        lastInstructionHex = memVal;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V" };
    }
            if (mode.equals(immediat)) {
                opcode = "8A";
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performOrA(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V" };
            }
            // ADD EXTENDED MODE
    else if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "BA";  // ORA extended opcode
        cycle = 4;      // ORA extended takes 4 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        String val = readFromRAM(address);
        
        int result = performOrA(val, reg);
        
        cleanedOperand = addr;

        lastInstructionHex = val;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V" };
    }
            // PHASE: Support du mode indexé pour ORA
            else if (mode.equals(indexe)) {
                String[] result = handleIndexedALU(secondWord, "AA", 4, this::performOrA);
                opcode = result[0];
                cycle = Integer.parseInt(result[1]);
                cleanedOperand = result[2];
            }
        } else if (firstWord.equals("ORB")) {
            if (mode.equals(direct)) {
        opcode = "DA";  // ORB direct opcode
        cycle = 3;
        
        String addrStr = secondWord.replace("<", "").replace("$", "");
        int address = Integer.parseInt(addrStr, 16) & 0x00FF;
        
        String memVal = readFromRAM(address);
        int result = performOrB(memVal, reg);
        
        cleanedOperand = String.format("%02X", address);
        
        lastInstructionHex = memVal;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V" };
    }
            if (mode.equals(immediat)) {
                opcode = "CA";
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performOrB(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V" };
            }
            // ADD EXTENDED MODE
    else if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "FA";  // ORB extended opcode
        cycle = 4;      // ORB extended takes 4 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        String val = readFromRAM(address);
        
        int result = performOrB(val, reg);
        
        cleanedOperand = addr;

        lastInstructionHex = val;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V" };
    }
            // PHASE: Support du mode indexé pour ORB
            else if (mode.equals(indexe)) {
                String[] result = handleIndexedALU(secondWord, "EA", 4, this::performOrB);
                opcode = result[0];
                cycle = Integer.parseInt(result[1]);
                cleanedOperand = result[2];
            }
        } else if (firstWord.equals("ORCC")) {
            if (mode.equals(immediat)) {
                opcode = "1A";
                cycle = 3;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performOrCC(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V" };
            }
        }

        // === EOR INSTRUCTIONS ===
        else if (firstWord.equals("EORA")) {
            if (mode.equals(direct)) {
        opcode = "98";  // EORA direct opcode
        cycle = 3;
        
        String addrStr = secondWord.replace("<", "").replace("$", "");
        int address = Integer.parseInt(addrStr, 16) & 0x00FF;
        
        String memVal = readFromRAM(address);
        int result = performEorA(memVal, reg);
        
        cleanedOperand = String.format("%02X", address);
        
        lastInstructionHex = memVal;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V" };
    }
            if (mode.equals(immediat)) {
                opcode = "88";
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performEorA(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V" };
            }
            // ADD EXTENDED MODE
    else if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "B8";  // EORA extended opcode
        cycle = 4;      // EORA extended takes 4 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        String val = readFromRAM(address);
        
        int result = performEorA(val, reg);
        
        cleanedOperand = addr;

        lastInstructionHex = val;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V" };
    }
            // PHASE: Support du mode indexé pour EORA
            else if (mode.equals(indexe)) {
                String[] result = handleIndexedALU(secondWord, "A8", 4, this::performEorA);
                opcode = result[0];
                cycle = Integer.parseInt(result[1]);
                cleanedOperand = result[2];
            }
        } else if (firstWord.equals("EORB")) {
            if (mode.equals(direct)) {
        opcode = "D8";  // EORB direct opcode
        cycle = 3;
        
        String addrStr = secondWord.replace("<", "").replace("$", "");
        int address = Integer.parseInt(addrStr, 16) & 0x00FF;
        
        String memVal = readFromRAM(address);
        int result = performEorB(memVal, reg);
        
        cleanedOperand = String.format("%02X", address);
        
        lastInstructionHex = memVal;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V" };
    }
            if (mode.equals(immediat)) {
                opcode = "C8";
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performEorB(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V" };
            }
            // ADD EXTENDED MODE
    else if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "F8";  // EORB extended opcode
        cycle = 4;      // EORB extended takes 4 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        String val = readFromRAM(address);
        
        int result = performEorB(val, reg);
        
        cleanedOperand = addr;

        lastInstructionHex = val;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V" };
    }
            // PHASE: Support du mode indexé pour EORB
            else if (mode.equals(indexe)) {
                String[] result = handleIndexedALU(secondWord, "E8", 4, this::performEorB);
                opcode = result[0];
                cycle = Integer.parseInt(result[1]);
                cleanedOperand = result[2];
            }
        }

        // === ADD WITH CARRY INSTRUCTIONS ===
        else if (firstWord.equals("ADCA")) {
            // Add after ADCA immediate/extended sections:
if (mode.equals(direct)) {
    opcode = "99";  // ADCA direct opcode
    cycle = 3;      // ADCA direct takes 3 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    String memVal = readFromRAM(address);
    int result = performAddWithCarryA(memVal, reg);
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = memVal;
    lastInstructionResult = result;
    lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
}
            if (mode.equals(immediat)) {
                opcode = "89"; // ADCA immediate opcode
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performAddWithCarryA(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
            }
            // ADD EXTENDED MODE
    else if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "B9";  // ADCA extended opcode
        cycle = 5;      // ADCA extended takes 5 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        // Read from RAM
        String val = readFromRAM(address);
        
        // Add with carry to A
        int result = performAddWithCarryA(val, reg);
        
        cleanedOperand = addr;

        // Store for flag calculation
        lastInstructionHex = val;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
        } else if (firstWord.equals("ADCB")) {
            // Add after ADCB immediate/extended sections:
if (mode.equals(direct)) {
    opcode = "D9";  // ADCB direct opcode
    cycle = 3;      // ADCB direct takes 3 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    String memVal = readFromRAM(address);
    int result = performAddWithCarryB(memVal, reg);
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = memVal;
    lastInstructionResult = result;
    lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
}
            if (mode.equals(immediat)) {
                opcode = "C9"; // ADCB immediate opcode
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performAddWithCarryB(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
            }
            // ADD EXTENDED MODE
    else if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "F9";  // ADCB extended opcode
        cycle = 5;      // ADCB extended takes 5 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        String val = readFromRAM(address);
        
        int result = performAddWithCarryB(val, reg);
        
        cleanedOperand = addr;

        lastInstructionHex = val;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
        }
        // === SUBTRACT WITH CARRY INSTRUCTIONS ===
        else if (firstWord.equals("SBCA")) {
            // Add after SBCA immediate/extended sections:
if (mode.equals(direct)) {
    opcode = "92";  // SBCA direct opcode
    cycle = 3;      // SBCA direct takes 3 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    String memVal = readFromRAM(address);
    int result = performSubtractWithCarryA(memVal, reg);
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = memVal;
    lastInstructionResult = result;
    lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
}
            if (mode.equals(immediat)) {
                opcode = "82"; // SBCA immediate opcode
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performSubtractWithCarryA(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
            }
            // ADD EXTENDED MODE
    else if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "B2";  // SBCA extended opcode
        cycle = 5;      // SBCA extended takes 5 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        String val = readFromRAM(address);
        
        int result = performSubtractWithCarryA(val, reg);
        
        cleanedOperand = addr;

        lastInstructionHex = val;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
        } else if (firstWord.equals("SBCB")) {
            // Add after SBCB immediate/extended sections:
if (mode.equals(direct)) {
    opcode = "D2";  // SBCB direct opcode
    cycle = 3;      // SBCB direct takes 3 cycles
    
    String addrStr = secondWord.replace("<", "").replace("$", "");
    int address = Integer.parseInt(addrStr, 16) & 0x00FF;
    
    String memVal = readFromRAM(address);
    int result = performSubtractWithCarryB(memVal, reg);
    
    cleanedOperand = String.format("%02X", address);
    
    lastInstructionHex = memVal;
    lastInstructionResult = result;
    lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
}
            if (mode.equals(immediat)) {
                opcode = "C2"; // SBCB immediate opcode
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performSubtractWithCarryB(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
            }
            // ADD EXTENDED MODE
    else if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "F2";  // SBCB extended opcode
        cycle = 5;      // SBCB extended takes 5 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        String val = readFromRAM(address);
        
        int result = performSubtractWithCarryB(val, reg);
        
        cleanedOperand = addr;

        lastInstructionHex = val;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
        }
        // Add these cases in your processAndConvertInstruction method:

// === DEC IN EXTENDED MODE ===
else if (firstWord.equals("DEC")) {
    if (mode.equals(direct)) {
        opcode = "7A";  // DEC direct opcode (hypothetical)
        cycle = 5;
        
        String addrStr = secondWord.replace("<", "").replace("$", "");
        int address = Integer.parseInt(addrStr, 16) & 0x00FF;
        
        // Read from RAM
        String val = readFromRAM(address);
        int value = Integer.parseInt(val, 16);
        
        // Decrement value
        int result = (value - 1) & 0xFF;
        String resultHex = String.format("%02X", result);
        
        // Write back to RAM
        writeToRAM(address, resultHex);
        
        cleanedOperand = String.format("%02X", address);

        // Store for flag calculation
        lastInstructionHex = resultHex;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V" };
    }
    if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "7A";  // DEC extended opcode
        cycle = 6;      // DEC extended takes 6 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        // Read from RAM
        String val = readFromRAM(address);
        int value = Integer.parseInt(val, 16);
        
        // Decrement value
        int result = (value - 1) & 0xFF;
        String resultHex = String.format("%02X", result);
        
        // Write back to RAM
        writeToRAM(address, resultHex);
        
        cleanedOperand = addr;

        // Store for flag calculation
        lastInstructionHex = resultHex;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V" };
    }
}

// === INC IN EXTENDED MODE ===
else if (firstWord.equals("INC")) {
    if (mode.equals(direct)) {
        opcode = "7C";  // INC direct opcode (hypothetical)
        cycle = 5;
        
        String addrStr = secondWord.replace("<", "").replace("$", "");
        int address = Integer.parseInt(addrStr, 16) & 0x00FF;
        
        // Read from RAM
        String val = readFromRAM(address);
        int value = Integer.parseInt(val, 16);
        
        // Increment value
        int result = (value + 1) & 0xFF;
        String resultHex = String.format("%02X", result);
        
        // Write back to RAM
        writeToRAM(address, resultHex);
        
        cleanedOperand = String.format("%02X", address);

        // Store for flag calculation
        lastInstructionHex = resultHex;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V" };
    }
    if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "7C";  // INC extended opcode
        cycle = 6;      // INC extended takes 6 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        // Read from RAM
        String val = readFromRAM(address);
        int value = Integer.parseInt(val, 16);
        
        // Increment value
        int result = (value + 1) & 0xFF;
        String resultHex = String.format("%02X", result);
        
        // Write back to RAM
        writeToRAM(address, resultHex);
        
        cleanedOperand = addr;

        // Store for flag calculation
        lastInstructionHex = resultHex;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V" };
    }
}
// === ASL IN EXTENDED MODE ===
else if (firstWord.equals("ASL")) {
    if (mode.equals(direct)) {
        opcode = "78";  // ASL direct opcode (actually extended, direct doesn't exist)
        cycle = 5;      // ASL direct takes 5 cycles
        
        String addrStr = secondWord.replace("<", "").replace("$", "");
        int address = Integer.parseInt(addrStr, 16) & 0x00FF;
        
        // Read from RAM
        String val = readFromRAM(address);
        int value = Integer.parseInt(val, 16);
        
        // Perform ASL (Arithmetic Shift Left) - same as LSL
        int carryOut = (value >> 7) & 0x01;
        int result = (value << 1) & 0xFF;
        String resultHex = String.format("%02X", result);
        
        // Write back to RAM
        writeToRAM(address, resultHex);
        
        cleanedOperand = String.format("%02X", address);

        // Store for flag calculation
        lastInstructionHex = resultHex;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
    if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "78";  // ASL extended opcode
        cycle = 6;      // ASL extended takes 6 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        // Read from RAM
        String val = readFromRAM(address);
        int value = Integer.parseInt(val, 16);
        
        // Perform ASL (Arithmetic Shift Left) - same as LSL
        int carryOut = (value >> 7) & 0x01;
        int result = (value << 1) & 0xFF;
        String resultHex = String.format("%02X", result);
        
        // Write back to RAM
        writeToRAM(address, resultHex);
        
        cleanedOperand = addr;

        // Store for flag calculation
        lastInstructionHex = resultHex;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
}

// === ASR IN EXTENDED MODE ===
else if (firstWord.equals("ASR")) {
    if (mode.equals(direct)) {
        opcode = "77";  // ASR direct opcode (hypothetical)
        cycle = 5;
        
        String addrStr = secondWord.replace("<", "").replace("$", "");
        int address = Integer.parseInt(addrStr, 16) & 0x00FF;
        
        // Read from RAM
        String val = readFromRAM(address);
        int value = Integer.parseInt(val, 16);
        
        // Perform ASR (Arithmetic Shift Right) - sign extended
        int signBit = (value >> 7) & 0x01;
        int carryOut = value & 0x01;
        int result = (value >> 1) | (signBit << 7);
        String resultHex = String.format("%02X", result);
        
        // Write back to RAM
        writeToRAM(address, resultHex);
        
        cleanedOperand = String.format("%02X", address);

        // Store for flag calculation
        lastInstructionHex = resultHex;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
    if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "77";  // ASR extended opcode
        cycle = 6;      // ASR extended takes 6 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        // Read from RAM
        String val = readFromRAM(address);
        int value = Integer.parseInt(val, 16);
        
        // Perform ASR (Arithmetic Shift Right) - sign extended
        int signBit = (value >> 7) & 0x01;
        int carryOut = value & 0x01;
        int result = (value >> 1) | (signBit << 7);
        String resultHex = String.format("%02X", result);
        
        // Write back to RAM
        writeToRAM(address, resultHex);
        
        cleanedOperand = addr;

        // Store for flag calculation
        lastInstructionHex = resultHex;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
}

// === LSL IN EXTENDED MODE ===
else if (firstWord.equals("LSL")) {
    if (mode.equals(direct)) {
        opcode = "78";  // LSL direct opcode (same as ASL)
        cycle = 5;
        
        String addrStr = secondWord.replace("<", "").replace("$", "");
        int address = Integer.parseInt(addrStr, 16) & 0x00FF;
        
        // Read from RAM
        String val = readFromRAM(address);
        int value = Integer.parseInt(val, 16);
        
        // Perform LSL (Logical Shift Left)
        int carryOut = (value >> 7) & 0x01;
        int result = (value << 1) & 0xFF;
        String resultHex = String.format("%02X", result);
        
        // Write back to RAM
        writeToRAM(address, resultHex);
        
        cleanedOperand = String.format("%02X", address);

        // Store for flag calculation
        lastInstructionHex = resultHex;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
    if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "78";  // LSL extended opcode (same as ASL)
        cycle = 6;      // LSL extended takes 6 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        // Read from RAM
        String val = readFromRAM(address);
        int value = Integer.parseInt(val, 16);
        
        // Perform LSL (Logical Shift Left)
        int carryOut = (value >> 7) & 0x01;
        int result = (value << 1) & 0xFF;
        String resultHex = String.format("%02X", result);
        
        // Write back to RAM
        writeToRAM(address, resultHex);
        
        cleanedOperand = addr;

        // Store for flag calculation
        lastInstructionHex = resultHex;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
}

// === LSR IN EXTENDED MODE ===
else if (firstWord.equals("LSR")) {
    if (mode.equals(direct)) {
        opcode = "74";  // LSR direct opcode (hypothetical)
        cycle = 5;
        
        String addrStr = secondWord.replace("<", "").replace("$", "");
        int address = Integer.parseInt(addrStr, 16) & 0x00FF;
        
        // Read from RAM
        String val = readFromRAM(address);
        int value = Integer.parseInt(val, 16);
        
        // Perform LSR (Logical Shift Right)
        int carryOut = value & 0x01;
        int result = (value >> 1) & 0xFF;
        String resultHex = String.format("%02X", result);
        
        // Write back to RAM
        writeToRAM(address, resultHex);
        
        cleanedOperand = String.format("%02X", address);

        // Store for flag calculation
        lastInstructionHex = resultHex;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
    if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "74";  // LSR extended opcode
        cycle = 6;      // LSR extended takes 6 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        // Read from RAM
        String val = readFromRAM(address);
        int value = Integer.parseInt(val, 16);
        
        // Perform LSR (Logical Shift Right)
        int carryOut = value & 0x01;
        int result = (value >> 1) & 0xFF;
        String resultHex = String.format("%02X", result);
        
        // Write back to RAM
        writeToRAM(address, resultHex);
        
        cleanedOperand = addr;

        // Store for flag calculation
        lastInstructionHex = resultHex;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
}
// === CLR IN EXTENDED MODE ===
else if (firstWord.equals("CLR")) {
    if (mode.equals(direct)) {
        opcode = "7F";  // CLR direct opcode (actually extended, but we'll use it)
        cycle = 5;
        
        String addrStr = secondWord.replace("<", "").replace("$", "");
        int address = Integer.parseInt(addrStr, 16) & 0x00FF;
        
        // Clear memory location (write 00)
        writeToRAM(address, "00");
        
        cleanedOperand = String.format("%02X", address);

        // Store for flag calculation
        lastInstructionHex = "00";
        lastInstructionResult = 0;
        lastInstructionFlags = new String[] { "Z", "N", "V" };
    }
    if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "7F";  // CLR extended opcode
        cycle = 6;      // CLR extended takes 6 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        // Clear memory location (write 00)
        writeToRAM(address, "00");
        
        cleanedOperand = addr;

        // Store for flag calculation
        lastInstructionHex = "00";
        lastInstructionResult = 0;
        lastInstructionFlags = new String[] { "Z", "N", "V" };
    }
}

// === COM IN EXTENDED MODE ===
else if (firstWord.equals("COM")) {
    if (mode.equals(direct)) {
        opcode = "73";  // COM direct opcode (hypothetical)
        cycle = 5;
        
        String addrStr = secondWord.replace("<", "").replace("$", "");
        int address = Integer.parseInt(addrStr, 16) & 0x00FF;
        
        // Read from RAM
        String val = readFromRAM(address);
        int value = Integer.parseInt(val, 16);
        
        // Perform COM (One's Complement)
        int result = (~value) & 0xFF;
        String resultHex = String.format("%02X", result);
        
        // Write back to RAM
        writeToRAM(address, resultHex);
        
        cleanedOperand = String.format("%02X", address);

        // Store for flag calculation
        lastInstructionHex = resultHex;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
    if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "73";  // COM extended opcode
        cycle = 6;      // COM extended takes 6 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        // Read from RAM
        String val = readFromRAM(address);
        int value = Integer.parseInt(val, 16);
        
        // Perform COM (One's Complement)
        int result = (~value) & 0xFF;
        String resultHex = String.format("%02X", result);
        
        // Write back to RAM
        writeToRAM(address, resultHex);
        
        cleanedOperand = addr;

        // Store for flag calculation
        lastInstructionHex = resultHex;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
}

// === NEG IN EXTENDED MODE ===
else if (firstWord.equals("NEG")) {
    if (mode.equals(direct)) {
        opcode = "70";  // NEG direct opcode (hypothetical)
        cycle = 5;
        
        String addrStr = secondWord.replace("<", "").replace("$", "");
        int address = Integer.parseInt(addrStr, 16) & 0x00FF;
        
        // Read from RAM
        String val = readFromRAM(address);
        int value = Integer.parseInt(val, 16);
        
        // Perform NEG (Two's Complement Negation)
        // NEG = 0 - value (two's complement)
        int result = (0 - value) & 0xFF;
        String resultHex = String.format("%02X", result);
        
        // Write back to RAM
        writeToRAM(address, resultHex);
        
        cleanedOperand = String.format("%02X", address);

        // Store for flag calculation
        lastInstructionHex = resultHex;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
    if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "70";  // NEG extended opcode
        cycle = 6;      // NEG extended takes 6 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        // Read from RAM
        String val = readFromRAM(address);
        int value = Integer.parseInt(val, 16);
        
        // Perform NEG (Two's Complement Negation)
        // NEG = 0 - value (two's complement)
        int result = (0 - value) & 0xFF;
        String resultHex = String.format("%02X", result);
        
        // Write back to RAM
        writeToRAM(address, resultHex);
        
        cleanedOperand = addr;

        // Store for flag calculation
        lastInstructionHex = resultHex;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
}
// === TST IN EXTENDED MODE ===
else if (firstWord.equals("TST")) {
    if (mode.equals(direct)) {
        opcode = "7D";  // TST direct opcode (hypothetical)
        cycle = 5;
        
        String addrStr = secondWord.replace("<", "").replace("$", "");
        int address = Integer.parseInt(addrStr, 16) & 0x00FF;
        
        // Read from RAM (but don't change it!)
        String val = readFromRAM(address);
        int value = Integer.parseInt(val, 16);
        
        // TST only sets flags based on the value
        // Result = value (for flag calculation, memory unchanged)
        
        cleanedOperand = String.format("%02X", address);

        // Store for flag calculation
        lastInstructionHex = val;
        lastInstructionResult = value;
        lastInstructionFlags = new String[] { "Z", "N", "V" };
    }
    if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "7D";  // TST extended opcode
        cycle = 6;      // TST extended takes 6 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        // Read from RAM (but don't change it!)
        String val = readFromRAM(address);
        int value = Integer.parseInt(val, 16);
        
        // TST only sets flags based on the value
        // Result = value (for flag calculation, memory unchanged)
        
        cleanedOperand = addr;

        // Store for flag calculation
        lastInstructionHex = val;
        lastInstructionResult = value;
        lastInstructionFlags = new String[] { "Z", "N", "V" };
    }
}

// === ROL IN EXTENDED MODE ===
else if (firstWord.equals("ROL")) {
     if (mode.equals(direct)) {
        opcode = "79";  // ROL direct opcode (hypothetical)
        cycle = 5;
        
        String addrStr = secondWord.replace("<", "").replace("$", "");
        int address = Integer.parseInt(addrStr, 16) & 0x00FF;
        
        // Read from RAM
        String val = readFromRAM(address);
        int value = Integer.parseInt(val, 16);
        
        // Perform ROL (Rotate Left through Carry)
        int carry = getCarryFlag(reg);
        int newCarry = (value >> 7) & 0x01;
        int result = ((value << 1) & 0xFF) | carry;
        String resultHex = String.format("%02X", result);
        
        // Write back to RAM
        writeToRAM(address, resultHex);
        
        cleanedOperand = String.format("%02X", address);

        // Store for flag calculation
        lastInstructionHex = resultHex;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
    if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "79";  // ROL extended opcode
        cycle = 6;      // ROL extended takes 6 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        // Read from RAM
        String val = readFromRAM(address);
        int value = Integer.parseInt(val, 16);
        
        // Perform ROL (Rotate Left through Carry)
        int carry = getCarryFlag(reg);
        int newCarry = (value >> 7) & 0x01;
        int result = ((value << 1) & 0xFF) | carry;
        String resultHex = String.format("%02X", result);
        
        // Write back to RAM
        writeToRAM(address, resultHex);
        
        cleanedOperand = addr;

        // Store for flag calculation
        lastInstructionHex = resultHex;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
}

// === ROR IN EXTENDED MODE ===
else if (firstWord.equals("ROR")) {
    if (mode.equals(direct)) {
        opcode = "76";  // ROR direct opcode (hypothetical)
        cycle = 5;
        
        String addrStr = secondWord.replace("<", "").replace("$", "");
        int address = Integer.parseInt(addrStr, 16) & 0x00FF;
        
        // Read from RAM
        String val = readFromRAM(address);
        int value = Integer.parseInt(val, 16);
        
        // Perform ROR (Rotate Right through Carry)
        int carry = getCarryFlag(reg);
        int newCarry = value & 0x01;
        int result = ((value >> 1) & 0xFF) | (carry << 7);
        String resultHex = String.format("%02X", result);
        
        // Write back to RAM
        writeToRAM(address, resultHex);
        
        cleanedOperand = String.format("%02X", address);

        // Store for flag calculation
        lastInstructionHex = resultHex;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
    if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "76";  // ROR extended opcode
        cycle = 6;      // ROR extended takes 6 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        // Read from RAM
        String val = readFromRAM(address);
        int value = Integer.parseInt(val, 16);
        
        // Perform ROR (Rotate Right through Carry)
        int carry = getCarryFlag(reg);
        int newCarry = value & 0x01;
        int result = ((value >> 1) & 0xFF) | (carry << 7);
        String resultHex = String.format("%02X", result);
        
        // Write back to RAM
        writeToRAM(address, resultHex);
        
        cleanedOperand = addr;

        // Store for flag calculation
        lastInstructionHex = resultHex;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
    }
}
// === JMP IN EXTENDED MODE ===
else if (firstWord.equals("JMP")) {
    if (mode.equals(direct)) {
        opcode = "7E";  // JMP direct opcode (hypothetical - actually extended)
        cycle = 3;
        
        String addrStr = secondWord.replace("<", "").replace("$", "");
        int address = Integer.parseInt(addrStr, 16) & 0x00FF;
        
        // Jump to address (set PC)
        reg.setPC(address);
        pcChangedManually = true; // PC was manually changed, don't auto-increment
        
        cleanedOperand = String.format("%02X", address);

        // JMP doesn't affect flags
        lastInstructionHex = addrStr;
        lastInstructionResult = address;
        lastInstructionFlags = new String[] {};

        // Skip PC auto-increment
        this.op = opcode;
        return new String[] { opcode, cleanedOperand };
    }
    if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "7E";  // JMP extended opcode
        cycle = 3;      // JMP extended takes 3 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        // Jump to address (set PC)
        reg.setPC(address);
        pcChangedManually = true; // PC was manually changed, don't auto-increment
        
        cleanedOperand = addr;

        // JMP doesn't affect flags
        lastInstructionHex = addr;
        lastInstructionResult = address;
        lastInstructionFlags = new String[] {};

        skipPCIncrement = true;

        this.op = opcode;
        // CRITICAL: Return here to prevent auto PC increment!
        return new String[] { opcode, cleanedOperand };
    }
    // PHASE: Support du mode indexé pour JMP
    else if (mode.equals(indexe)) {
        opcode = "6E";  // JMP indexed opcode
        cycle = 3;      // JMP indexed takes 3 cycles
        
        // Parse indexed mode and calculate effective address
        String parseResult = parseIndexedMode(secondWord);
        String[] parts = parseResult.split(":");
        String type = parts[0];
        
        // PHASE 6: Indirect Flag Extraction
        boolean isIndirect = type.contains("_INDIRECT");
        String cleanType = isIndirect ? type.replace("_INDIRECT", "") : type;
        
        int effectiveAddr = 0;
        
        if (cleanType.equals("ACC_OFFSET")) {
            // PHASE 3: Offset accumulateur
            String acc = parts[1]; // A, B, ou D
            String indexReg = parts[2]; // X, Y, U, S
            effectiveAddr = calculateAccumulatorIndexed(acc, indexReg, isIndirect);
            cleanedOperand = generatePostByteAccOffset(acc, indexReg, isIndirect);
        } else if (!cleanType.equals("UNKNOWN")) {
            // PHASE 2: Autres types (offsets, auto-inc/dec)
            String register = parts[1];
            int value = Integer.parseInt(parts[2]);
            effectiveAddr = calculateIndexedAddress(type, register, value);
            cleanedOperand = generatePostByte(type, register, value);
            
            // PHASE 4 & 5: Ajouter les octets d'offset au post-byte
            if (cleanType.equals("OFFSET_8_BIT") || cleanType.equals("PC_REL_8_BIT")) {
                String offsetHex = String.format("%02X", value & 0xFF);
                cleanedOperand += offsetHex;
            } else if (cleanType.equals("OFFSET_16_BIT") || cleanType.equals("PC_REL_16_BIT")) {
                String offsetHex = String.format("%04X", value & 0xFFFF);
                cleanedOperand += offsetHex;
            }
        } else {
            System.out.println("Mode indexé non supporté ou invalide: " + secondWord);
            cleanedOperand = "";
        }
        
        if (!cleanType.equals("UNKNOWN")) {
            // Jump to effective address (set PC)
            reg.setPC(effectiveAddr);
            pcChangedManually = true; // PC was manually changed, don't auto-increment
            
            // JMP doesn't affect flags
            lastInstructionHex = String.format("%04X", effectiveAddr);
            lastInstructionResult = effectiveAddr;
            lastInstructionFlags = new String[] {};
            
            skipPCIncrement = true;
            
            this.op = opcode;
            // CRITICAL: Return here to prevent auto PC increment!
            return new String[] { opcode, cleanedOperand };
        }
    }
}

// === JSR IN EXTENDED MODE ===
else if (firstWord.equals("JSR")) {
    if (mode.equals(direct)) {
        opcode = "BD";  // JSR direct opcode (actually extended)
        cycle = 8;
        
        String addrStr = secondWord.replace("<", "").replace("$", "");
        int address = Integer.parseInt(addrStr, 16) & 0x00FF;
        
        // Get current PC (which already points to next instruction)
        int currentPC = Integer.parseInt(reg.getPC(), 16);
        
        // Push return address onto stack
        int sValue = Integer.parseInt(reg.getS(), 16);
        
        // Decrement stack and push high byte
        sValue -= 2;
        String pcVal = String.format("%04X", currentPC);
        ramMemory.getram().put(String.format("%04X", sValue), pcVal.substring(0, 2));
        ramMemory.getram().put(String.format("%04X", sValue + 1), pcVal.substring(2));
        
        // Update stack pointer
        reg.setS(String.format("%04X", sValue));
        
        // Jump to subroutine
        reg.setPC(address);
        pcChangedManually = true; // PC was manually changed, don't auto-increment
        
        cleanedOperand = String.format("%02X", address);

        // JSR doesn't affect flags
        lastInstructionHex = addrStr;
        lastInstructionResult = address;
        lastInstructionFlags = new String[] {};

        this.op = opcode;
        // Return immediately to prevent auto PC increment
        return new String[] { opcode, cleanedOperand };
    }
    if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "BD";  // JSR extended opcode
        cycle = 8;      // JSR extended takes 8 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        // Get current PC (which already points to next instruction)
        int currentPC = Integer.parseInt(reg.getPC(), 16);
        
        // Push return address onto stack
        int sValue = Integer.parseInt(reg.getS(), 16);
        
        // Decrement stack and push high byte
        sValue -= 2;
        String pcVal = String.format("%04X", currentPC);
        ramMemory.getram().put(String.format("%04X", sValue), pcVal.substring(0, 2));
        ramMemory.getram().put(String.format("%04X", sValue + 1), pcVal.substring(2));
        
        // Update stack pointer
        reg.setS(String.format("%04X", sValue));
        
        // Jump to subroutine
        reg.setPC(address);
        pcChangedManually = true; // PC was manually changed, don't auto-increment
        
        cleanedOperand = addr;

        // JSR doesn't affect flags
        lastInstructionHex = addr;
        lastInstructionResult = address;
        lastInstructionFlags = new String[] {};

        this.op = opcode;
        // Return immediately to prevent auto PC increment
        return new String[] { opcode, cleanedOperand };
    }
    // PHASE: Support du mode indexé pour JSR
    else if (mode.equals(indexe)) {
        opcode = "AD";  // JSR indexed opcode
        cycle = 7;      // JSR indexed takes 7 cycles
        
        // Parse indexed mode and calculate effective address
        String parseResult = parseIndexedMode(secondWord);
        String[] parts = parseResult.split(":");
        String type = parts[0];
        
        // PHASE 6: Indirect Flag Extraction
        boolean isIndirect = type.contains("_INDIRECT");
        String cleanType = isIndirect ? type.replace("_INDIRECT", "") : type;
        
        int effectiveAddr = 0;
        
        if (cleanType.equals("ACC_OFFSET")) {
            // PHASE 3: Offset accumulateur
            String acc = parts[1]; // A, B, ou D
            String indexReg = parts[2]; // X, Y, U, S
            effectiveAddr = calculateAccumulatorIndexed(acc, indexReg, isIndirect);
            cleanedOperand = generatePostByteAccOffset(acc, indexReg, isIndirect);
        } else if (!cleanType.equals("UNKNOWN")) {
            // PHASE 2: Autres types (offsets, auto-inc/dec)
            String register = parts[1];
            int value = Integer.parseInt(parts[2]);
            effectiveAddr = calculateIndexedAddress(type, register, value);
            cleanedOperand = generatePostByte(type, register, value);
            
            // PHASE 4 & 5: Ajouter les octets d'offset au post-byte
            if (cleanType.equals("OFFSET_8_BIT") || cleanType.equals("PC_REL_8_BIT")) {
                String offsetHex = String.format("%02X", value & 0xFF);
                cleanedOperand += offsetHex;
            } else if (cleanType.equals("OFFSET_16_BIT") || cleanType.equals("PC_REL_16_BIT")) {
                String offsetHex = String.format("%04X", value & 0xFFFF);
                cleanedOperand += offsetHex;
            }
        } else {
            System.out.println("Mode indexé non supporté ou invalide: " + secondWord);
            cleanedOperand = "";
        }
        
        if (!cleanType.equals("UNKNOWN")) {
            // Get current PC (which already points to next instruction)
            // Note: PC will be incremented after this instruction, so we push the current PC
            int currentPC = Integer.parseInt(reg.getPC(), 16);
            
            // Push return address onto stack
            int sValue = Integer.parseInt(reg.getS(), 16);
            
            // Decrement stack and push high byte first (big endian)
            sValue -= 2;
            String pcVal = String.format("%04X", currentPC);
            ramMemory.getram().put(String.format("%04X", sValue), pcVal.substring(0, 2));
            ramMemory.getram().put(String.format("%04X", sValue + 1), pcVal.substring(2));
            
            // Update stack pointer
            reg.setS(String.format("%04X", sValue));
            
            // Jump to subroutine (set PC to effective address)
            reg.setPC(effectiveAddr);
            pcChangedManually = true; // PC was manually changed, don't auto-increment
            
            // JSR doesn't affect flags
            lastInstructionHex = String.format("%04X", effectiveAddr);
            lastInstructionResult = effectiveAddr;
            lastInstructionFlags = new String[] {};
            
            skipPCIncrement = true;
            
            this.op = opcode;
            // CRITICAL: Return here to prevent auto PC increment!
            return new String[] { opcode, cleanedOperand };
        }
    }
}

else if (firstWord.equals("CWAI")) {
    // CWAI #$Operand
    opcode = "3C";
    cycle = 20; // CWAI takes 20 cycles
    String operand = secondWord.replace("#$", "");
    cleanedOperand = operand;
    
    // Call the execution method
    int result = performCWAI(operand, reg);
    
    // Store for any potential flag tracking (CWAI affects flags via pushed CCR)
    lastInstructionHex = operand;
    lastInstructionResult = result;
    lastInstructionFlags = new String[] { }; // Flags are not set in the standard way
}

        // === BIT TEST INSTRUCTIONS ===
        else if (firstWord.equals("BITA")) {
            if (mode.equals(direct)) {
        opcode = "95";  // BITA direct opcode
        cycle = 3;
        
        String addrStr = secondWord.replace("<", "").replace("$", "");
        int address = Integer.parseInt(addrStr, 16) & 0x00FF;
        
        String memVal = readFromRAM(address);
        int memValue = Integer.parseInt(memVal, 16);
        int aValue = Integer.parseInt(reg.getA(), 16);
        int result = aValue & memValue;  // For flags only
        
        cleanedOperand = String.format("%02X", address);
        
        lastInstructionHex = memVal;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V" };
    }
            if (mode.equals(immediat)) {
                opcode = "85"; // BITA immediate opcode
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performBitTestA(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V" };
            }
            // === BITA IN EXTENDED MODE ===

    if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "B5";  // BITA extended opcode
        cycle = 4;      // BITA extended takes 4 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        // Read from RAM
        String val = readFromRAM(address);
        int memValue = Integer.parseInt(val, 16);
        
        // Get A register value
        int aValue = Integer.parseInt(reg.getA(), 16);
        
        // Perform BIT test: A AND memory (sets flags, doesn't change registers)
        int result = aValue & memValue;
        
        cleanedOperand = addr;

        // Store for flag calculation
        lastInstructionHex = val;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V" };
    }
            // PHASE: Support du mode indexé pour BITA
            else if (mode.equals(indexe)) {
                String[] result = handleIndexedALU(secondWord, "A5", 4, this::performBitTestA);
                opcode = result[0];
                cycle = Integer.parseInt(result[1]);
                cleanedOperand = result[2];
            }

        } else if (firstWord.equals("BITB")) {
            if (mode.equals(direct)) {
        opcode = "D5";  // BITB direct opcode
        cycle = 3;
        
        String addrStr = secondWord.replace("<", "").replace("$", "");
        int address = Integer.parseInt(addrStr, 16) & 0x00FF;
        
        String memVal = readFromRAM(address);
        int memValue = Integer.parseInt(memVal, 16);
        int bValue = Integer.parseInt(reg.getB(), 16);
        int result = bValue & memValue;  // For flags only
        
        cleanedOperand = String.format("%02X", address);
        
        lastInstructionHex = memVal;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V" };
    }
            if (mode.equals(immediat)) {
                opcode = "C5"; // BITB immediate opcode
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performBitTestB(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V" };
            }
            if (mode.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
            && secondWord.replace("$", "").length() == 4)) {
        opcode = "F5";  // BITB extended opcode
        cycle = 4;      // BITB extended takes 4 cycles
        String addr = secondWord.replace(">", "").replace("$", "");
        int address = Integer.parseInt(addr, 16);
        
        // Read from RAM
        String val = readFromRAM(address);
        int memValue = Integer.parseInt(val, 16);
        
        // Get B register value
        int bValue = Integer.parseInt(reg.getB(), 16);
        
        // Perform BIT test: B AND memory (sets flags, doesn't change registers)
        int result = bValue & memValue;
        
        cleanedOperand = addr;

        // Store for flag calculation
        lastInstructionHex = val;
        lastInstructionResult = result;
        lastInstructionFlags = new String[] { "Z", "N", "V" };
    }
            // PHASE: Support du mode indexé pour BITB
            else if (mode.equals(indexe)) {
                String[] result = handleIndexedALU(secondWord, "E5", 4, this::performBitTestB);
                opcode = result[0];
                cycle = Integer.parseInt(result[1]);
                cleanedOperand = result[2];
            }
        }
        
        // === STACK OPERATIONS ===
        else if (firstWord.equals("PSHS")) {
            if (mode.equals(immediat)) {
                opcode = "34"; // PSHS opcode
                cycle = 5;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performPushS(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] {}; // PSHS doesn't affect flags
            }
        } else if (firstWord.equals("PSHU")) {
            if (mode.equals(immediat)) {
                opcode = "36"; // PSHU opcode
                cycle = 5;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performPushU(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] {}; // PSHU doesn't affect flags
            }
        } else if (firstWord.equals("PULS")) {
            if (mode.equals(immediat)) {
                opcode = "35"; // PULS opcode
                cycle = 5;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performPullS(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] {}; // PULS may affect flags if pulling CCR
            }
        } else if (firstWord.equals("PULU")) {
            if (mode.equals(immediat)) {
                opcode = "37"; // PULU opcode
                cycle = 5;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performPullU(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] {}; // PULU may affect flags if pulling CCR
            }
        }

        // === INHERENT MODE INSTRUCTIONS ===
        else if (firstWord.equals("ABX")) {
            opcode = "3A";
            cycle = 3;
            cleanedOperand = "";
            int result = performABX(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] {}; // ABX doesn't affect flags

        } else if (firstWord.equals("CLRA")) {
            opcode = "4F";
            cycle = 2;
            cleanedOperand = "";
            int result = performCLRA(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] { "Z", "N", "V" };

        } else if (firstWord.equals("CLRB")) {
            opcode = "5F";
            cycle = 2;
            cleanedOperand = "";
            int result = performCLRB(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] { "Z", "N", "V" };

        } else if (firstWord.equals("COMA")) {
            opcode = "43";
            cycle = 2;
            cleanedOperand = "";
            int result = performCOMA(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] { "Z", "N", "V" };

        } else if (firstWord.equals("COMB")) {
            opcode = "53";
            cycle = 2;
            cleanedOperand = "";
            int result = performCOMB(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] { "Z", "N", "V" };

        } else if (firstWord.equals("DAA")) {
            opcode = "19";
            cycle = 2;
            cleanedOperand = "";
            int result = performDAA(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] { "Z", "N", "V", "C" };

        } else if (firstWord.equals("DECA")) {
            opcode = "4A";
            cycle = 2;
            cleanedOperand = "";
            int result = performDECA(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] { "Z", "N", "V" };

        } else if (firstWord.equals("DECB")) {
            opcode = "5A";
            cycle = 2;
            cleanedOperand = "";
            int result = performDECB(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] { "Z", "N", "V" };

        } else if (firstWord.equals("INCA")) {
            opcode = "4C";
            cycle = 2;
            cleanedOperand = "";
            int result = performINCA(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] { "Z", "N", "V" };

        } else if (firstWord.equals("INCB")) {
            opcode = "5C";
            cycle = 2;
            cleanedOperand = "";
            int result = performINCB(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] { "Z", "N", "V" };

        } else if (firstWord.equals("MUL")) {
            opcode = "3D";
            cycle = 11;
            cleanedOperand = "";
            int result = performMUL(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] { "Z", "C" };

        } else if (firstWord.equals("NEGA")) {
            opcode = "40";
            cycle = 2;
            cleanedOperand = "";
            int result = performNEGA(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] { "Z", "N", "V", "C" };

        } else if (firstWord.equals("NEGB")) {
            opcode = "50";
            cycle = 2;
            cleanedOperand = "";
            int result = performNEGB(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] { "Z", "N", "V", "C" };

        } else if (firstWord.equals("NOP")) {
            opcode = "12";
            cycle = 2;
            cleanedOperand = "";
            lastInstructionHex = "";
            lastInstructionResult = 0;
            lastInstructionFlags = new String[] {}; // NOP doesn't affect flags
        }

        // === ROTATE INSTRUCTIONS ===
        else if (firstWord.equals("ROLA")) {
            opcode = "49";
            cycle = 2;
            cleanedOperand = "";
            int result = performROLA(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
        } else if (firstWord.equals("ROLB")) {
            opcode = "59";
            cycle = 2;
            cleanedOperand = "";
            int result = performROLB(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
        } else if (firstWord.equals("RORA")) {
            opcode = "46";
            cycle = 2;
            cleanedOperand = "";
            int result = performRORA(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
        } else if (firstWord.equals("RORB")) {
            opcode = "56";
            cycle = 2;
            cleanedOperand = "";
            int result = performRORB(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
        }

        // === SUBROUTINE & INTERRUPT INSTRUCTIONS ===
        else if (firstWord.equals("RTS")) {
            opcode = "39";
            cycle = 5;
            cleanedOperand = "";
            int result = performRTS(reg);
            pcChangedManually = true; // RTS modifies PC (pulls return address from stack)
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] {};
        } else if (firstWord.equals("RTI")) {
            opcode = "3B";
            cycle = 15; // RTI is complex, restores many registers
            cleanedOperand = "";
            int result = performRTI(reg);
            pcChangedManually = true; // RTI modifies PC (restores PC from stack)
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] { "Z", "N", "V", "C" }; // Restores all flags
        } else if (firstWord.equals("SWI")) {
            opcode = "3F";
            cycle = 19; // SWI saves all registers
            cleanedOperand = "";
            int result = performSWI(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] {};
        } else if (firstWord.equals("SWI2")) {
            opcode = "103F";
            cycle = 20;
            cleanedOperand = "";
            int result = performSWI2(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] {};
        } else if (firstWord.equals("SWI3")) {
            opcode = "113F";
            cycle = 20;
            cleanedOperand = "";
            int result = performSWI3(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] {};
        } else if (firstWord.equals("SYNC")) {
            opcode = "13";
            cycle = 2; // Plus wait time for interrupt
            cleanedOperand = "";
            int result = performSYNC(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] {};
        }

        // === TEST INSTRUCTIONS ===
        else if (firstWord.equals("TSTA")) {
            opcode = "4D";
            cycle = 2;
            cleanedOperand = "";
            int result = performTSTA(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] { "Z", "N", "V" };
        } else if (firstWord.equals("TSTB")) {
            opcode = "5D";
            cycle = 2;
            cleanedOperand = "";
            int result = performTSTB(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] { "Z", "N", "V" };
        }

        // === DATA TRANSFER & EXCHANGE ===
        else if (firstWord.equals("TFR")) {
            opcode = "1F";
            cycle = 6;
            // cleanedOperand will hold the Post-byte
            cleanedOperand = performTFR(secondWord, reg);
            lastInstructionHex = cleanedOperand;
            lastInstructionResult = 0;
            lastInstructionFlags = new String[] {}; // Flags usually unaffected (unless CC dest)
        } else if (firstWord.equals("EXG")) {
            opcode = "1E";
            cycle = 8;
            cleanedOperand = performEXG(secondWord, reg);
            lastInstructionHex = cleanedOperand;
            lastInstructionResult = 0;
            lastInstructionFlags = new String[] {};
        }

        // === DATA CONVERSION (6809 only) ===
        else if (firstWord.equals("SEX")) {
            opcode = "1D";
            cycle = 2;
            cleanedOperand = "";
            int result = performSEX(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] { "Z", "N" };
        }

        // === ARITHMETIC SHIFT INSTRUCTIONS ===
        else if (firstWord.equals("ASLA")) {
            opcode = "48";
            cycle = 2;
            cleanedOperand = "";
            int result = performASLA(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
        } else if (firstWord.equals("ASLB")) {
            opcode = "58";
            cycle = 2;
            cleanedOperand = "";
            int result = performASLB(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
        } else if (firstWord.equals("ASRA")) {
            opcode = "47";
            cycle = 2;
            cleanedOperand = "";
            int result = performASRA(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
        } else if (firstWord.equals("ASRB")) {
            opcode = "57";
            cycle = 2;
            cleanedOperand = "";
            int result = performASRB(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
        }

        // === LOGICAL SHIFT INSTRUCTIONS ===
        else if (firstWord.equals("LSLA")) {
            opcode = "48"; // Same as ASLA in 6809
            cycle = 2;
            cleanedOperand = "";
            int result = performLSLA(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
        } else if (firstWord.equals("LSLB")) {
            opcode = "58"; // Same as ASLB in 6809
            cycle = 2;
            cleanedOperand = "";
            int result = performLSLB(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
        } else if (firstWord.equals("LSRA")) {
            opcode = "44";
            cycle = 2;
            cleanedOperand = "";
            int result = performLSRA(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
        } else if (firstWord.equals("LSRB")) {
            opcode = "54";
            cycle = 2;
            cleanedOperand = "";
            int result = performLSRB(reg);
            lastInstructionHex = "";
            lastInstructionResult = result;
            lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
        }

        // === ADD WITH CARRY INSTRUCTIONS (IMMEDIATE) ===
        else if (firstWord.equals("ADCA")) {
            if (mode.equals(immediat)) {
                opcode = "89"; // ADCA immediate opcode
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performADCA(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
            }
        } else if (firstWord.equals("ADCB")) {
            if (mode.equals(immediat)) {
                opcode = "C9"; // ADCB immediate opcode
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performADCB(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
            }
        }

        // === SUBTRACT WITH CARRY INSTRUCTIONS (IMMEDIAT) ===
        else if (firstWord.equals("SBCA")) {
            if (mode.equals(immediat)) {
                opcode = "82"; // SBCA immediate opcode
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performSBCA(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
            }
        } else if (firstWord.equals("SBCB")) {
            if (mode.equals(immediat)) {
                opcode = "C2"; // SBCB immediate opcode
                cycle = 2;
                cleanedOperand = secondWord.replace("#$", "");
                int result = performSBCB(cleanedOperand, reg);
                lastInstructionHex = cleanedOperand;
                lastInstructionResult = result;
                lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
            }
        }

        else if (firstWord.equals("END")) {
            opcode = "3F"; // SWI instruction as END marker
            cleanedOperand = "";
        }
        // Add more instructions as needed


        
        // Update the instance variables
        this.op = opcode;

        // ====================================================================
        // MISE À JOUR DU PROGRAM COUNTER (PC)
        // ====================================================================
        // Le PC doit avancer selon la taille de l'instruction:
        // - 1 octet: opcode seul (ex: END)
        // - 2 octets: opcode + 1 byte (ex: LDA #$10, LDA ,X avec post-byte)
        // - 3 octets: opcode + 2 bytes (ex: LDX #$1000)
        // - 4 octets: opcode + 1 byte prefix + 2 bytes (ex: LDY #$2000)

        int instructionSize = 1; // Au minimum, l'opcode (1 octet)

        // Si l'opcode est 00, c'est une instruction invalide, ne pas incrémenter
        if (!opcode.equals("00")) {
            // Calculer la taille selon le cleanedOperand
            if (!cleanedOperand.isEmpty()) {
                // Cas spéciaux pour les opcodes préfixés (page 2)
                if (opcode.equals("10")) {
                    // Prefix pour Y, U, S (ex: 10 8E pour LDY #)
                    // On a: prefix (1 byte) + opcode réel (1 byte) + données
                    instructionSize = 2; // prefix + opcode
                    // Si cleanedOperand a 4 caractères hex = 2 bytes de données
                    if (cleanedOperand.length() == 4) {
                        instructionSize += 2; // Total: 4 bytes
                    } else if (cleanedOperand.length() == 2) {
                        instructionSize += 1; // Total: 3 bytes
                    }
                } else {
                    // Instructions normales
                    // cleanedOperand contient soit la valeur immédiate, soit le post-byte
                    int operandLength = cleanedOperand.length() / 2; // 2 chars hex = 1 byte
                    instructionSize += operandLength;
                }
            }

            // SPECIAL CASE: For END (SWI=3F), it's always 1 byte
            if (firstWord.equals("END")) {
                instructionSize = 1;
            }

            // Incrémenter le PC avec masque 16-bits pour éviter l'overflow
            // BUT: Skip if PC was manually changed by control flow instructions (JMP, JSR, etc.)
            if (!pcChangedManually && !skipPCIncrement) {
                int currentPC = Integer.parseInt(reg.getPC(), 16);
                int newPC = (currentPC + instructionSize) & 0xFFFF; // Masque pour rester dans 16-bits
                reg.setPC(newPC);
            }
        }
        
        // Note: skipPCIncrement is NOT reset here - it's reset at the start of the next instruction
        // This allows pas.java to check the flag after processAndConvertInstruction returns

        return new String[] { opcode, cleanedOperand };
    }

    // === STACK OPERATION METHODS ===

    // ==============================================
    // ARITHMETIC SHIFT METHODS
    // ==============================================

    // ASLA - Arithmetic Shift Left A (same as LSLA)
    private int performASLA(registre reg) {
        return performLSLA(reg); // ASLA and LSLA are identical in 6809
    }

    // ASLB - Arithmetic Shift Left B (same as LSLB)
    private int performASLB(registre reg) {
        return performLSLB(reg); // ASLB and LSLB are identical in 6809
    }

    

    // ASRA - Arithmetic Shift Right A (sign extended)
    private int performASRA(registre reg) {
        int aValue = Integer.parseInt(reg.getA(), 16);

        // Save bit 7 (sign bit) for sign extension
        int signBit = (aValue >> 7) & 0x01;
        // Get bit 0 for carry flag
        int carryOut = aValue & 0x01;

        // Shift right, extending sign bit
        int result = (aValue >> 1) | (signBit << 7);

        String resultHex = String.format("%02X", result);
        reg.setA(resultHex);

        // Calculate flags
        int zFlag = (result == 0) ? 1 : 0;
        int nFlag = signBit; // N = sign bit (bit 7)
        int vFlag = 0; // V is cleared

        updateCCRBasedOnFlags(zFlag, nFlag, vFlag, carryOut, reg);

        return result;
    }

    // ASRB - Arithmetic Shift Right B (sign extended)
    private int performASRB(registre reg) {
        int bValue = Integer.parseInt(reg.getB(), 16);

        int signBit = (bValue >> 7) & 0x01;
        int carryOut = bValue & 0x01;

        int result = (bValue >> 1) | (signBit << 7);

        String resultHex = String.format("%02X", result);
        reg.setB(resultHex);

        int zFlag = (result == 0) ? 1 : 0;
        int nFlag = signBit;
        int vFlag = 0;

        updateCCRBasedOnFlags(zFlag, nFlag, vFlag, carryOut, reg);

        return result;
    }

    // ==============================================
    // ADD/SUBTRACT WITH CARRY METHODS
    // ==============================================

    // ADCA - Add with Carry to A
    private int performADCA(String operand, registre reg) {
        int currentA = Integer.parseInt(reg.getA(), 16);
        int operandValue = Integer.parseInt(operand, 16);
        int carry = getCarryFlag(reg); // Get current carry flag

        // ADCA: A = A + operand + C
        int result = currentA + operandValue + carry;

        // Handle 8-bit overflow
        int carryOut = (result > 0xFF) ? 1 : 0;
        result = result & 0xFF; // Keep only lower 8 bits

        String resultHex = String.format("%02X", result);
        reg.setA(resultHex);

        // Calculate flags
        int zFlag = (result == 0) ? 1 : 0;
        int nFlag = ((result & 0x80) != 0) ? 1 : 0;

        // V flag: Overflow if both operands same sign but result different sign
        int signA = (currentA >> 7) & 0x01;
        int signOp = (operandValue >> 7) & 0x01;
        int signRes = (result >> 7) & 0x01;
        int vFlag = ((signA == signOp) && (signA != signRes)) ? 1 : 0;

        updateCCRBasedOnFlags(zFlag, nFlag, vFlag, carryOut, reg);

        return result;
    }

    // ADCB - Add with Carry to B
    private int performADCB(String operand, registre reg) {
        int currentB = Integer.parseInt(reg.getB(), 16);
        int operandValue = Integer.parseInt(operand, 16);
        int carry = getCarryFlag(reg);

        int result = currentB + operandValue + carry;

        int carryOut = (result > 0xFF) ? 1 : 0;
        result = result & 0xFF;

        String resultHex = String.format("%02X", result);
        reg.setB(resultHex);

        int zFlag = (result == 0) ? 1 : 0;
        int nFlag = ((result & 0x80) != 0) ? 1 : 0;

        int signB = (currentB >> 7) & 0x01;
        int signOp = (operandValue >> 7) & 0x01;
        int signRes = (result >> 7) & 0x01;
        int vFlag = ((signB == signOp) && (signB != signRes)) ? 1 : 0;

        updateCCRBasedOnFlags(zFlag, nFlag, vFlag, carryOut, reg);

        return result;
    }

    // SBCA - Subtract with Carry from A
    private int performSBCA(String operand, registre reg) {
        int currentA = Integer.parseInt(reg.getA(), 16);
        int operandValue = Integer.parseInt(operand, 16);
        int carry = getCarryFlag(reg); // Note: In subtraction, carry = borrow

        // SBCA: A = A - operand - C
        int result = currentA - operandValue - carry;

        // Handle underflow (borrow)
        int borrowOut = (result < 0) ? 1 : 0;
        if (result < 0) {
            result += 256; // Two's complement wrap-around
        }
        result = result & 0xFF;

        String resultHex = String.format("%02X", result);
        reg.setA(resultHex);

        // Calculate flags
        int zFlag = (result == 0) ? 1 : 0;
        int nFlag = ((result & 0x80) != 0) ? 1 : 0;

        // V flag for subtraction: Overflow if signs differ and result sign = operand
        // sign
        int signA = (currentA >> 7) & 0x01;
        int signOp = (operandValue >> 7) & 0x01;
        int signRes = (result >> 7) & 0x01;
        int vFlag = ((signA != signOp) && (signOp == signRes)) ? 1 : 0;

        updateCCRBasedOnFlags(zFlag, nFlag, vFlag, borrowOut, reg);

        return result;
    }

    // SBCB - Subtract with Carry from B
    private int performSBCB(String operand, registre reg) {
        int currentB = Integer.parseInt(reg.getB(), 16);
        int operandValue = Integer.parseInt(operand, 16);
        int carry = getCarryFlag(reg);

        int result = currentB - operandValue - carry;

        int borrowOut = (result < 0) ? 1 : 0;
        if (result < 0) {
            result += 256;
        }
        result = result & 0xFF;

        String resultHex = String.format("%02X", result);
        reg.setB(resultHex);

        int zFlag = (result == 0) ? 1 : 0;
        int nFlag = ((result & 0x80) != 0) ? 1 : 0;

        int signB = (currentB >> 7) & 0x01;
        int signOp = (operandValue >> 7) & 0x01;
        int signRes = (result >> 7) & 0x01;
        int vFlag = ((signB != signOp) && (signOp == signRes)) ? 1 : 0;

        updateCCRBasedOnFlags(zFlag, nFlag, vFlag, borrowOut, reg);

        return result;
    }

    // ==============================================
    // LOGICAL SHIFT METHODS
    // ==============================================

    // LSLA - Logical Shift Left A (same as ASLA)

    private int performExtendedASL(int value, registre reg) {
    // Implementation similar to performASLA but for memory
    int carryOut = (value >> 7) & 0x01;
    int result = (value << 1) & 0xFF;
    
    // Calculate flags
    int zFlag = (result == 0) ? 1 : 0;
    int nFlag = ((result & 0x80) != 0) ? 1 : 0;
    int previousBit6 = (value >> 6) & 0x01;
    int vFlag = ((carryOut ^ previousBit6) != 0) ? 1 : 0;
    
    updateCCRBasedOnFlags(zFlag, nFlag, vFlag, carryOut, reg);
    return result;
}
    private int performLSLA(registre reg) {
        int aValue = Integer.parseInt(reg.getA(), 16);

        // Get bit 7 for carry flag
        int carryOut = (aValue >> 7) & 0x01;
        // Shift left
        int result = (aValue << 1) & 0xFF;

        String resultHex = String.format("%02X", result);
        reg.setA(resultHex);

        // Calculate flags
        int zFlag = (result == 0) ? 1 : 0;
        int nFlag = ((result & 0x80) != 0) ? 1 : 0;
        // V flag: Set if sign changes (bit 7 XOR previous carryOut)
        int previousBit6 = (aValue >> 6) & 0x01;
        int vFlag = ((carryOut ^ previousBit6) != 0) ? 1 : 0;


        updateCCRBasedOnFlags(zFlag, nFlag, vFlag, carryOut, reg);

        return result;
    }
    private boolean skipPCIncrement = false;
    
    /**
     * Returns true if the PC was manually changed by a control flow instruction (JMP, JSR, RTS, RTI).
     * This is used by the execution engine to prevent overwriting the PC value.
     * @return true if PC should not be auto-updated, false otherwise
     */
    public boolean shouldSkipPCUpdate() {
        return skipPCIncrement;
    }
    
    /**
     * Resets the skipPCIncrement flag. Called by pas.java after checking the flag.
     */
    public void resetSkipPCIncrementFlag() {
        skipPCIncrement = false;
    }

    // LSLB - Logical Shift Left B (same as ASLB)
    private int performLSLB(registre reg) {
        int bValue = Integer.parseInt(reg.getB(), 16);

        int carryOut = (bValue >> 7) & 0x01;
        int result = (bValue << 1) & 0xFF;

        String resultHex = String.format("%02X", result);
        reg.setB(resultHex);

        int zFlag = (result == 0) ? 1 : 0;
        int nFlag = ((result & 0x80) != 0) ? 1 : 0;
        int previousBit6 = (bValue >> 6) & 0x01;
        int vFlag = ((carryOut ^ previousBit6) != 0) ? 1 : 0;

        updateCCRBasedOnFlags(zFlag, nFlag, vFlag, carryOut, reg);

        return result;
    }

    // LSRA - Logical Shift Right A
    private int performLSRA(registre reg) {
        int aValue = Integer.parseInt(reg.getA(), 16);

        // Get bit 0 for carry flag
        int carryOut = aValue & 0x01;
        // Shift right, 0 shifted into bit 7
        int result = (aValue >> 1) & 0xFF;

        String resultHex = String.format("%02X", result);
        reg.setA(resultHex);

        // Calculate flags
        int zFlag = (result == 0) ? 1 : 0;
        int nFlag = 0; // Always 0 since bit 7 becomes 0
        int vFlag = 0; // Always cleared

        updateCCRBasedOnFlags(zFlag, nFlag, vFlag, carryOut, reg);

        return result;
    }

    // LSRB - Logical Shift Right B
    private int performLSRB(registre reg) {
        int bValue = Integer.parseInt(reg.getB(), 16);

        int carryOut = bValue & 0x01;
        int result = (bValue >> 1) & 0xFF;

        String resultHex = String.format("%02X", result);
        reg.setB(resultHex);

        int zFlag = (result == 0) ? 1 : 0;
        int nFlag = 0;
        int vFlag = 0;

        updateCCRBasedOnFlags(zFlag, nFlag, vFlag, carryOut, reg);

        return result;
    }

    // === ROTATE INSTRUCTION METHODS ===

    private int performROLA(registre reg) {
        int aValue = Integer.parseInt(reg.getA(), 16);
        int carry = getCarryFlag(reg);

        // ROLA: C ← bit7 ← bit6 ← ... ← bit0 ← C
        int newCarry = (aValue >> 7) & 0x01; // Current bit7 becomes new carry
        int result = ((aValue << 1) & 0xFF) | carry; // Shift left, insert old carry at bit0

        String resultHex = String.format("%02X", result);
        reg.setA(resultHex);

        // Calculate flags
        int zFlag = (result == 0) ? 1 : 0;
        int nFlag = ((result & 0x80) != 0) ? 1 : 0;
        int vFlag = nFlag ^ newCarry; // V = N XOR C (for rotate)

        updateCCRBasedOnFlags(zFlag, nFlag, vFlag, newCarry, reg);

        return result;
    }

    private int performROLB(registre reg) {
        int bValue = Integer.parseInt(reg.getB(), 16);
        int carry = getCarryFlag(reg);

        int newCarry = (bValue >> 7) & 0x01;
        int result = ((bValue << 1) & 0xFF) | carry;

        String resultHex = String.format("%02X", result);
        reg.setB(resultHex);

        int zFlag = (result == 0) ? 1 : 0;
        int nFlag = ((result & 0x80) != 0) ? 1 : 0;
        int vFlag = nFlag ^ newCarry;

        updateCCRBasedOnFlags(zFlag, nFlag, vFlag, newCarry, reg);

        return result;
    }

    private int performRORA(registre reg) {
        int aValue = Integer.parseInt(reg.getA(), 16);
        int carry = getCarryFlag(reg);

        // RORA: C → bit7 → bit6 → ... → bit0 → C
        int newCarry = aValue & 0x01; // Current bit0 becomes new carry
        int result = ((aValue >> 1) & 0xFF) | (carry << 7); // Shift right, insert old carry at bit7

        String resultHex = String.format("%02X", result);
        reg.setA(resultHex);

        int zFlag = (result == 0) ? 1 : 0;
        int nFlag = ((result & 0x80) != 0) ? 1 : 0;
        int vFlag = nFlag ^ newCarry; // V = N XOR C

        updateCCRBasedOnFlags(zFlag, nFlag, vFlag, newCarry, reg);

        return result;
    }

    private int performRORB(registre reg) {
        int bValue = Integer.parseInt(reg.getB(), 16);
        int carry = getCarryFlag(reg);

        int newCarry = bValue & 0x01;
        int result = ((bValue >> 1) & 0xFF) | (carry << 7);

        String resultHex = String.format("%02X", result);
        reg.setB(resultHex);

        int zFlag = (result == 0) ? 1 : 0;
        int nFlag = ((result & 0x80) != 0) ? 1 : 0;
        int vFlag = nFlag ^ newCarry;

        updateCCRBasedOnFlags(zFlag, nFlag, vFlag, newCarry, reg);

        return result;
    }

    // === SUBROUTINE & INTERRUPT METHODS ===

    private int performRTS(registre reg) {
        // RTS: Pull return address from hardware stack (S)
        int sValue = Integer.parseInt(reg.getS(), 16);

        // Pull 2-byte return address
        String addrHigh = ramMemory.getram().get(String.format("%04X", sValue));
        String addrLow = ramMemory.getram().get(String.format("%04X", sValue + 1));

        if (addrHigh == null)
            addrHigh = "00";
        if (addrLow == null)
            addrLow = "00";

        int returnAddr = Integer.parseInt(addrHigh + addrLow, 16);

        // Update stack pointer (increment by 2)
        reg.setS(String.format("%04X", sValue + 2));

        // Set PC to return address
        reg.setPC(returnAddr);

        return returnAddr;
    }

    private int performRTI(registre reg) {
        // RTI: Restore all registers from interrupt
        // Stack frame AFTER interrupt (S points to CCR):
        // S → [CCR] (address 00F7)
        // [B] (00F8)
        // [A] (00F9)
        // [X high] (00FA)
        // [X low] (00FB)
        // [Y high] (00FC)
        // [Y low] (00FD)
        // [PC high] (00FE)
        // [PC low] (00FF)
        // [Original S before interrupt = 0100]

        int sValue = Integer.parseInt(reg.getS(), 16);
        System.out.println("[RTI] Reading from stack at: " + String.format("%04X", sValue));

        // 1. CCR (1 byte) - Read THEN increment
        String ccrVal = ramMemory.getram().get(String.format("%04X", sValue));
        if (ccrVal == null)
            ccrVal = "00";
        System.out.println("  CCR read: " + ccrVal);
        sValue++;

        // 2. B (1 byte)
        String bVal = ramMemory.getram().get(String.format("%04X", sValue));
        if (bVal == null)
            bVal = "00";
        System.out.println("  B read: " + bVal);
        sValue++;

        // 3. A (1 byte)
        String aVal = ramMemory.getram().get(String.format("%04X", sValue));
        if (aVal == null)
            aVal = "00";
        System.out.println("  A read: " + aVal);
        sValue++;

        // 4. X (2 bytes - high byte first)
        String xHigh = ramMemory.getram().get(String.format("%04X", sValue));
        if (xHigh == null)
            xHigh = "00";
        sValue++;

        String xLow = ramMemory.getram().get(String.format("%04X", sValue));
        if (xLow == null)
            xLow = "00";
        String xVal = xHigh + xLow;
        System.out.println("  X read: " + xVal);
        sValue++;

        // 5. Y (2 bytes - high byte first)
        String yHigh = ramMemory.getram().get(String.format("%04X", sValue));
        if (yHigh == null)
            yHigh = "00";
        sValue++;

        String yLow = ramMemory.getram().get(String.format("%04X", sValue));
        if (yLow == null)
            yLow = "00";
        String yVal = yHigh + yLow;
        System.out.println("  Y read: " + yVal);
        sValue++;

        // 6. PC (2 bytes - high byte first)
        String pcHigh = ramMemory.getram().get(String.format("%04X", sValue));
        if (pcHigh == null)
            pcHigh = "00";
        sValue++;

        String pcLow = ramMemory.getram().get(String.format("%04X", sValue));
        if (pcLow == null)
            pcLow = "00";
        String pcVal = pcHigh + pcLow;
        int returnPC = Integer.parseInt(pcVal, 16);
        System.out.println("  PC read: " + pcVal + " = " + String.format("%04X", returnPC));
        sValue++;

        // 7. RESTORE REGISTERS
        reg.setCCR(ccrVal);
        reg.setB(bVal);
        reg.setA(aVal);
        reg.setX(xVal);
        reg.setY(yVal);
        reg.setPC(returnPC);

        // 8. Update stack pointer (S now points to original position before interrupt)
        reg.setS(String.format("%04X", sValue & 0xFFFF));
        System.out.println("[RTI] New S: " + reg.getS() + ", PC: " + reg.getPC());

        return returnPC;
    }

    private int performSWI(registre reg) {
        // SWI: Save all registers to stack IN CORRECT ORDER
        // Motorola 6809 saves in order: PC, Y, X, A, B, CCR
        // Stack grows DOWNWARD (decrement first, then store)

        int sValue = Integer.parseInt(reg.getS(), 16);

        // ====== SAVE IN CORRECT ORDER ======

        // 1. Save PC (2 bytes) - HIGH byte first!
        sValue -= 2;
        String pcVal = reg.getPC();
        if (pcVal.length() == 4) {
            // Store high byte at S, low byte at S+1
            ramMemory.getram().put(String.format("%04X", sValue), pcVal.substring(0, 2));
            ramMemory.getram().put(String.format("%04X", sValue + 1), pcVal.substring(2));

        }

        // Show registers
        reg.displayRegisters();

        // Show registers
        reg.displayRegisters();

        // 2. Save Y (2 bytes)
        sValue -= 2;
        String yVal = reg.getY();
        if (yVal.length() == 4) {
            ramMemory.getram().put(String.format("%04X", sValue), yVal.substring(0, 2));
            ramMemory.getram().put(String.format("%04X", sValue + 1), yVal.substring(2));

        }

        // 3. Save X (2 bytes)
        sValue -= 2;
        String xVal = reg.getX();
        if (xVal.length() == 4) {
            ramMemory.getram().put(String.format("%04X", sValue), xVal.substring(0, 2));
            ramMemory.getram().put(String.format("%04X", sValue + 1), xVal.substring(2));

        }

        // 4. Save A (1 byte)
        sValue -= 1;
        String aVal = reg.getA();
        ramMemory.getram().put(String.format("%04X", sValue), aVal);

        // 5. Save B (1 byte)
        sValue -= 1;
        String bVal = reg.getB();
        ramMemory.getram().put(String.format("%04X", sValue), bVal);

        // 6. Save CCR (1 byte)
        sValue -= 1;
        String ccrVal = reg.getCCR();
        ramMemory.getram().put(String.format("%04X", sValue), ccrVal);

        // Update stack pointer
        reg.setS(String.format("%04X", sValue & 0xFFFF));

        // Set I flag (bit 4) in CCR to mask interrupts
        int ccrValue = Integer.parseInt(ccrVal, 16);
        ccrValue |= 0x10; // Set I flag (bit 4)
        reg.setCCR(String.format("%02X", ccrValue));

        // Jump to SWI vector address (Motorola 6809 uses FFF6-FFF7 for SWI)
        reg.setPC(0xFFFA); // This will make PC jump to SWI handler

        return 0xFFFA;
    }

    private int performSWI2(registre reg) {
        // Similar to SWI but doesn't set I flag
        return performSWI(reg); // Simplified - same as SWI without I flag
    }

    private int performSWI3(registre reg) {
        // Similar to SWI2 with different vector
        return performSWI(reg); // Simplified
    }

    private int performSYNC(registre reg) {
        // SYNC: Halt CPU until interrupt
        // In emulation, we just decrement cycle count and continue
        // Real implementation would wait for hardware interrupt
        System.out.println("[SYNC] CPU waiting for interrupt...");
        return 0;
    }

    // === TEST INSTRUCTION METHODS ===

    private int performTSTA(registre reg) {
        int aValue = Integer.parseInt(reg.getA(), 16);

        // TSTA doesn't change A, only sets flags
        int zFlag = (aValue == 0) ? 1 : 0;
        int nFlag = ((aValue & 0x80) != 0) ? 1 : 0;
        int vFlag = 0; // TST clears V flag

        updateCCRBasedOnFlags(zFlag, nFlag, vFlag, 0, reg);

        return aValue;
    }

    private int performTSTB(registre reg) {
        int bValue = Integer.parseInt(reg.getB(), 16);

        int zFlag = (bValue == 0) ? 1 : 0;
        int nFlag = ((bValue & 0x80) != 0) ? 1 : 0;
        int vFlag = 0;

        updateCCRBasedOnFlags(zFlag, nFlag, vFlag, 0, reg);

        return bValue;
    }

    // === DATA CONVERSION METHOD ===

    private int performSEX(registre reg) {
        // SEX: Sign-extend B into D (A:B)
        int bValue = Integer.parseInt(reg.getB(), 16);

        int signBit = (bValue >> 7) & 0x01; // Get bit 7 (sign)
        int aValue;

        if (signBit == 1) {
            aValue = 0xFF; // If negative, A = $FF
        } else {
            aValue = 0x00; // If positive, A = $00
        }

        String aHex = String.format("%02X", aValue);
        String bHex = String.format("%02X", bValue);
        String dHex = aHex + bHex;

        reg.setA(aHex);
        reg.setD(dHex);

        // Set flags
        int zFlag = (bValue == 0) ? 1 : 0; // Z based on B (not D)
        int nFlag = signBit; // N based on sign bit

        updateCCRBasedOnFlags(zFlag, nFlag, 0, 0, reg);

        return Integer.parseInt(dHex, 16);
    }

    // ==============================================
    // TFR / EXG INSTRUCTION METHODS (PHASE SPECIAL)
    // ==============================================

    /**
     * Get 4-bit code for register (Post-byte encoding)
     */
    private int getRegisterCode(String regName) {
        switch (regName.toUpperCase()) {
            case "D":
                return 0b0000; // 0
            case "X":
                return 0b0001; // 1
            case "Y":
                return 0b0010; // 2
            case "U":
                return 0b0011; // 3
            case "S":
                return 0b0100; // 4
            case "PC":
            return 0b0101; // 5  ← ADD THIS LINE
            case "A":
                return 0b1000; // 8
            case "B":
                return 0b1001; // 9
            case "CC":
                return 0b1010; // A
            case "DP":
                return 0b1011; // B
            default:
                return -1;
        }
    }

    /**
     * Check if register is 16-bit
     */
    private boolean is16BitRegister(String regName) {
        int code = getRegisterCode(regName);
        // Codes 0-5 are 16-bit, Codes 8-11 are 8-bit

        if (code == -1) return false;
        return code <= 5;
    }

    private String getRegisterValue(String regName, registre reg) {
        switch (regName.toUpperCase()) {
            case "D":
                return reg.getD();
            case "X":
                return reg.getX();
            case "Y":
                return reg.getY();
            case "U":
                return reg.getU();
            case "S":
                return reg.getS();
            case "PC":
                return reg.getPC();
            case "A":
                return reg.getA();
            case "B":
                return reg.getB();
            case "CC":
                return reg.getCCR();
            // DP not implemented fully, return 00
            case "DP":
                return "00";
            default:
                return "00";
        }
    }

    private void setRegisterValue(String regName, String val, registre reg) {
        switch (regName.toUpperCase()) {
            case "D":
                reg.setD(val);
                break;
            case "X":
                reg.setX(val);
                break;
            case "Y":
                reg.setY(val);
                break;
            case "U":
                reg.setU(val);
                break;
            case "S":
                reg.setS(val);
                break;
            case "PC":
                reg.setPC(Integer.parseInt(val, 16));
                break;
            case "A":
                reg.setA(val);
                break;
            case "B":
                reg.setB(val);
                break;
            case "CC":
                reg.setCCR(val);
                break;
            // DP ignored for now
        }
    }

    private String performTFR(String operands, registre reg) {
        String[] parts = operands.split(",");
        if (parts.length != 2)
            return "00";

        String r1 = parts[0].trim();
        String r2 = parts[1].trim();

        // Validation: Must be same size
        boolean r1Is16 = is16BitRegister(r1);
        boolean r2Is16 = is16BitRegister(r2);

        if (r1Is16 != r2Is16) {
            System.out.println("ERROR: TFR size mismatch (" + r1 + "->" + r2 + ")");
            return "00"; // Invalid TFR
        }

        // Transfer value
        String val = getRegisterValue(r1, reg);
        setRegisterValue(r2, val, reg);

        // Generate Post-byte
        int code1 = getRegisterCode(r1);
        int code2 = getRegisterCode(r2);
        int postByte = (code1 << 4) | code2;

        return String.format("%02X", postByte);
    }


    private int performCWAI(String operand, registre reg) {
    // 1. Get immediate value and current CCR
    int mask = Integer.parseInt(operand, 16);
    int ccrValue = Integer.parseInt(reg.getCCR(), 16);
    
    // 2. AND the CCR with the mask (sets condition codes)
    int result = ccrValue & mask;
    reg.setCCR(String.format("%02X", result));
    
    // 3. Push ALL registers onto the hardware stack (S)
    //    Order: PC, Y, X, A, B, CCR (same as SWI)
    int sValue = Integer.parseInt(reg.getS(), 16);
    
    // Save PC
    sValue -= 2;
    String pcVal = reg.getPC();
    ramMemory.getram().put(String.format("%04X", sValue), pcVal.substring(0, 2));
    ramMemory.getram().put(String.format("%04X", sValue + 1), pcVal.substring(2));
    
    // Save Y, X, A, B, CCR (refer to your performSWI method for the exact sequence)
    // ... (implementation detail: continue pushing registers) ...
    
    // Update stack pointer
    reg.setS(String.format("%04X", sValue));
    
    // 4. Enter WAIT state for interrupt.
    // In an emulator, you might set a CPU state flag or decrement cycle count.
    //System.out.println("[CWAI] CPU waiting for interrupt with mask: $" + operand);
    // For a simple emulation, you could just return.
    
    return result;
}

    private String performEXG(String operands, registre reg) {
        String[] parts = operands.split(",");
        if (parts.length != 2)
            return "00";

        String r1 = parts[0].trim();
        String r2 = parts[1].trim();

        // Validation: Must be same size
        boolean r1Is16 = is16BitRegister(r1);
        boolean r2Is16 = is16BitRegister(r2);

        if (r1Is16 != r2Is16) {
            System.out.println("ERROR: EXG size mismatch (" + r1 + "<->" + r2 + ")");
            return "00";
        }

        // Exchange values
        String val1 = getRegisterValue(r1, reg);
        String val2 = getRegisterValue(r2, reg);

        setRegisterValue(r1, val2, reg);
        setRegisterValue(r2, val1, reg);

        // Generate Post-byte
        int code1 = getRegisterCode(r1);
        int code2 = getRegisterCode(r2);
        int postByte = (code1 << 4) | code2;

        return String.format("%02X", postByte);
    }

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

    // === INHERENT MODE METHODS ===

    // === UPDATE EXISTING INHERENT METHODS ===
    // Modify performDECA to calculate and set flags
    private int performDECA(registre reg) {
        int aValue = Integer.parseInt(reg.getA(), 16);
        int result = (aValue - 1) & 0xFF;

        String resultHex = String.format("%02X", result);
        reg.setA(resultHex);

        // Calculate flags
        int zFlag = (result == 0) ? 1 : 0;
        int nFlag = ((result & 0x80) != 0) ? 1 : 0;
        int vFlag = (aValue == 0x80) ? 1 : 0; // Overflow if decrementing from 0x80

        updateCCRBasedOnFlags(zFlag, nFlag, vFlag, 0, reg);

        return result;
    }

    private int performDAA(registre reg) {
        int aValue = Integer.parseInt(reg.getA(), 16);
        int result = aValue;
        int originalCarry = getCarryFlag(reg);
        int adjust = 0;

        // Get half-carry flag (H flag is bit 5 in CCR)
        int halfCarry = getHalfCarryFlag(reg);

        // Step 1: Check lower nibble (bits 3-0)
        if (((aValue & 0x0F) > 9) || halfCarry == 1) {
            adjust += 0x06;
        }

        // Step 2: Check upper nibble (bits 7-4) - FIXED!
        // The bug was: checking (aValue > 0x9F) instead of ((aValue & 0xF0) > 0x90)
        if (((aValue & 0xF0) > 0x90) || originalCarry == 1) {
            adjust += 0x60;
        }

        // Apply adjustment
        if (adjust != 0) {
            result = (aValue + adjust) & 0xFF;
        }

        // Calculate flags CORRECTLY:
        int zFlag = (result == 0) ? 1 : 0;
        int nFlag = ((result & 0x80) != 0) ? 1 : 0;
        int vFlag = 0; // DAA doesn't affect V

        // C flag: Set if we added 0x60 OR original carry was set
        int cFlag = (originalCarry == 1 || adjust >= 0x60) ? 1 : 0;

        // Only update register if adjustment was made
        if (adjust != 0) {
            String resultHex = String.format("%02X", result);
            reg.setA(resultHex);
        }

        updateCCRBasedOnFlags(zFlag, nFlag, vFlag, cFlag, reg);

        return result;
    }

    private void setCarryFlag(registre reg, int carry) {
        // Get current CCR value
        String ccrStr = reg.getCCR();
        int ccrValue = 0;

        if (ccrStr != null && !ccrStr.isEmpty()) {
            try {
                ccrValue = Integer.parseInt(ccrStr, 16);
            } catch (Exception e) {
                ccrValue = 0;
            }
        }

        // Set or clear ONLY the carry bit (bit 0)
        if (carry == 1) {
            ccrValue |= 0x01; // Set bit 0 (carry flag)
        } else {
            ccrValue &= ~0x01; // Clear bit 0 (carry flag)
        }

        // Preserve all other flags (Z, N, V, H, etc.)
        reg.setCCR(String.format("%02X", ccrValue));
    }

    // Modify performINCB to calculate and set flags
    private int performINCB(registre reg) {
        int bValue = Integer.parseInt(reg.getB(), 16);
        int result = (bValue + 1) & 0xFF;

        String resultHex = String.format("%02X", result);
        reg.setB(resultHex);

        // Calculate flags
        int zFlag = (result == 0) ? 1 : 0;
        int nFlag = ((result & 0x80) != 0) ? 1 : 0;
        int vFlag = (bValue == 0x7F) ? 1 : 0; // Overflow if incrementing from 0x7F

        updateCCRBasedOnFlags(zFlag, nFlag, vFlag, 0, reg);

        return result;
    }

    // Modify performCLRA to calculate and set flags
    private int performCLRA(registre reg) {
        reg.setA("00");

        // Calculate flags: Z=1, N=0, V=0, C=0
        updateCCRBasedOnFlags(1, 0, 0, 0, reg);

        return 0;
    }

    // Modify performCOMB to calculate and set flags
    private int performCOMB(registre reg) {
        int bValue = Integer.parseInt(reg.getB(), 16);
        int result = (~bValue) & 0xFF;

        String resultHex = String.format("%02X", result);
        reg.setB(resultHex);

        // Calculate flags
        int zFlag = (result == 0) ? 1 : 0;
        int nFlag = ((result & 0x80) != 0) ? 1 : 0;
        int vFlag = 0; // COM never sets V
        int cFlag = 1; // COM always sets C

        updateCCRBasedOnFlags(zFlag, nFlag, vFlag, cFlag, reg);

        return result;
    }

    // === UPDATE OTHER INHERENT METHODS SIMILARLY ===
    // You should also update these methods similarly:
    // performDECB, performINCA, performCOMA, performNEGA, performNEGB, performDAA,
    // performMUL

    // Example for DECB:
    private int performDECB(registre reg) {
        int bValue = Integer.parseInt(reg.getB(), 16);
        int result = (bValue - 1) & 0xFF;

        String resultHex = String.format("%02X", result);
        reg.setB(resultHex);

        // Calculate flags
        int zFlag = (result == 0) ? 1 : 0;
        int nFlag = ((result & 0x80) != 0) ? 1 : 0;
        int vFlag = (bValue == 0x80) ? 1 : 0; // Overflow if decrementing from 0x80

        updateCCRBasedOnFlags(zFlag, nFlag, vFlag, 0, reg);

        return result;
    }

    // Example for INCA:
    private int performINCA(registre reg) {
        int aValue = Integer.parseInt(reg.getA(), 16);
        int result = (aValue + 1) & 0xFF;

        String resultHex = String.format("%02X", result);
        reg.setA(resultHex);

        // Calculate flags
        int zFlag = (result == 0) ? 1 : 0;
        int nFlag = ((result & 0x80) != 0) ? 1 : 0;
        int vFlag = (aValue == 0x7F) ? 1 : 0; // Overflow if incrementing from 0x7F

        updateCCRBasedOnFlags(zFlag, nFlag, vFlag, 0, reg);

        return result;
    }

    // ABX - Add B to X (unsigned)
    private int performABX(registre reg) {
        int bValue = Integer.parseInt(reg.getB(), 16);
        int xValue = Integer.parseInt(reg.getX(), 16);
        int result = (xValue + bValue) & 0xFFFF; // Keep 16-bit

        String resultHex = String.format("%04X", result);
        reg.setX(resultHex);
        return result;
    }

    // CLRB - Clear Accumulator B
    private int performCLRB(registre reg) {
        reg.setB("00");
        return 0;
    }

    // MUL - Multiply A × B (unsigned)
    private int performMUL(registre reg) {
        int aValue = Integer.parseInt(reg.getA(), 16);
        int bValue = Integer.parseInt(reg.getB(), 16);
        int result = aValue * bValue; // 8-bit × 8-bit = 16-bit result

        // Store in D (A:B) where A = high byte, B = low byte
        int highByte = (result >> 8) & 0xFF;
        int lowByte = result & 0xFF;

        String highHex = String.format("%02X", highByte);
        String lowHex = String.format("%02X", lowByte);
        String dHex = highHex + lowHex;

        reg.setA(highHex);
        reg.setB(lowHex);
        reg.setD(dHex);

        // Set carry flag if high byte is non-zero
        if (highByte != 0) {
            setCarryFlag(reg, 1);
        } else {
            setCarryFlag(reg, 0);
        }

        return result;
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
                lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
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
                lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
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
            ramMemory.getram().put(addrLow, lowByte); // ← USE RAM
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
            String lowByte = ramMemory.getram().get(addrLow); // ← USE RAM
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
            setCarryFlag(reg, 1); // Set carry flag
        } else {
            setCarryFlag(reg, 0); // Clear carry flag
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
            setCarryFlag(reg, 1); // Set carry flag (borrow occurred)
        } else {
            setCarryFlag(reg, 0); // Clear carry flag
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

    private void updateCCRBasedOnFlags(int z, int n, int v, int c, registre reg) {
        // Get current CCR value
        String ccrStr = reg.getCCR();
        int ccrValue = 0;

        if (ccrStr != null && !ccrStr.isEmpty()) {
            try {
                ccrValue = Integer.parseInt(ccrStr, 16);
            } catch (Exception e) {
                ccrValue = 0;
            }
        }

        // Update ONLY the specified flags, preserve others

        // Z flag (bit 2 = 0x04)
        if (z == 1) {
            ccrValue |= 0x04; // Set Z flag
        } else if (z == 0) {
            ccrValue &= ~0x04; // Clear Z flag
        }
        // (if z == -1, leave unchanged)

        // N flag (bit 3 = 0x08)
        if (n == 1) {
            ccrValue |= 0x08; // Set N flag
        } else if (n == 0) {
            ccrValue &= ~0x08; // Clear N flag
        }

        // V flag (bit 1 = 0x02)
        if (v == 1) {
            ccrValue |= 0x02; // Set V flag
        } else if (v == 0) {
            ccrValue &= ~0x02; // Clear V flag
        }

        // C flag (bit 0 = 0x01)
        if (c == 1) {
            ccrValue |= 0x01; // Set C flag
        } else if (c == 0) {
            ccrValue &= ~0x01; // Clear C flag
        }

        // Save updated CCR
        reg.setCCR(String.format("%02X", ccrValue));
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
            } 
            // In calculateSelectedFlags() method:
else if (this.op.equals("B1") || this.op.equals("F1") || 
         this.op.equals("BC") || this.op.equals("10BC") ||
         this.op.equals("10B3") || this.op.equals("11B3") || 
         this.op.equals("11BC")) {
    // // This is an extended mode CMP instruction
    // int registerValue = 0;
    int memoryValue = Integer.parseInt(hexOperand, 16);
    
    // Get register value based on opcode
    if (this.op.equals("B1")) { // CMPA
        registerValue = Integer.parseInt(reg.getA(), 16);
    } else if (this.op.equals("F1")) { // CMPB
        registerValue = Integer.parseInt(reg.getB(), 16);
    } else if (this.op.equals("BC")) { // CMPX
        registerValue = Integer.parseInt(reg.getX(), 16);
    } else if (this.op.equals("10BC")) { // CMPY
        registerValue = Integer.parseInt(reg.getY(), 16);
    } else if (this.op.equals("10B3")) { // CMPD
        registerValue = Integer.parseInt(reg.getD(), 16);
    } else if (this.op.equals("11B3")) { // CMPU
        registerValue = Integer.parseInt(reg.getU(), 16);
    } else if (this.op.equals("11BC")) { // CMPS
        registerValue = Integer.parseInt(reg.getS(), 16);
    }
    
    // // Calculate comparison result
    // int result = registerValue - memoryValue;
    
    // System.out.print("Flags calculated: ");
    // for (String flag : flagsToCalculate) {
    //     switch (flag) {
    //         case "Z":
    //             int zFlag = (registerValue == memoryValue) ? 1 : 0;
    //             System.out.print("Z:" + zFlag + " ");
    //             break;
    //         case "N":
    //             int nFlag = (registerValue < memoryValue) ? 1 : 0;
    //             System.out.print("N:" + nFlag + " ");
    //             break;
    //         case "C":
    //             int cFlag = (registerValue < memoryValue) ? 1 : 0;
    //             System.out.print("C:" + cFlag + " ");
    //             break;
    //         case "V":
    //             int vFlag = 0; // Simplified
    //             System.out.print("V:" + vFlag + " ");
    //             break;
    //     }
    // }
    System.out.println();
}else {
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
                        int zFlag = (actualValue == 0) ? 1 : 0; // Zero if result is 0
                        System.out.print("Z:" + zFlag + " ");
                        break;
                    case "N":
                        // For 8-bit operations, check if bit 7 is set (0x80)
                        if (actualValue <= 0xFF) {
                            int nFlag = ((actualValue & 0x80) != 0) ? 1 : 0; // Negative if bit 7 is 1
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

    private int performCOMA(registre reg) {
        int aValue = Integer.parseInt(reg.getA(), 16);
        int result = (~aValue) & 0xFF;

        String resultHex = String.format("%02X", result);
        reg.setA(resultHex);

        // Calculate flags
        int zFlag = (result == 0) ? 1 : 0;
        int nFlag = ((result & 0x80) != 0) ? 1 : 0;
        int vFlag = 0; // COM never sets V
        int cFlag = 1; // COM always sets C

        updateCCRBasedOnFlags(zFlag, nFlag, vFlag, cFlag, reg);

        return result;
    }

    private int performNEGA(registre reg) {
        int aValue = Integer.parseInt(reg.getA(), 16);
        int result = (0 - aValue) & 0xFF;

        String resultHex = String.format("%02X", result);
        reg.setA(resultHex);

        // Calculate flags
        int zFlag = (result == 0) ? 1 : 0;
        int nFlag = ((result & 0x80) != 0) ? 1 : 0;
        int vFlag = (aValue == 0x80) ? 1 : 0; // Overflow if original was 0x80
        int cFlag = (result != 0 || aValue == 0x80) ? 1 : 0; // Set if result non-zero or original was 0x80

        updateCCRBasedOnFlags(zFlag, nFlag, vFlag, cFlag, reg);

        return result;
    }

    private int performNEGB(registre reg) {
        int bValue = Integer.parseInt(reg.getB(), 16);
        int result = (0 - bValue) & 0xFF;

        String resultHex = String.format("%02X", result);
        reg.setB(resultHex);

        // Calculate flags
        int zFlag = (result == 0) ? 1 : 0;
        int nFlag = ((result & 0x80) != 0) ? 1 : 0;
        int vFlag = (bValue == 0x80) ? 1 : 0;
        int cFlag = (result != 0 || bValue == 0x80) ? 1 : 0;

        updateCCRBasedOnFlags(zFlag, nFlag, vFlag, cFlag, reg);

        return result;
    }

    private int getCarryFlag(registre reg) {
        String ccr = reg.getCCR();
        if (ccr == null || ccr.equals("00")) {
            return 0;
        }
        try {
            // CORRECT: Parse hex, check bit 0
            int ccrValue = Integer.parseInt(ccr, 16);
            return (ccrValue & 0x01); // Bit 0 is carry flag
        } catch (Exception e) {
            return 0;
        }
    }

    // Helper method for half-carry flag (needed for DAA)
    private int getHalfCarryFlag(registre reg) {
        String ccr = reg.getCCR();
        if (ccr.equals("00"))
            return 0;
        try {
            // H is bit 5 in CCR (value 0x20)
            int ccrValue = Integer.parseInt(ccr, 16);
            return (ccrValue & 0x20) != 0 ? 1 : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    // === REPLACE YOUR EXISTING calculateLastInstructionFlags METHOD ===
    public void calculateLastInstructionFlags() {
        if (!lastInstructionHex.isEmpty() && this.reg != null) {
            calculateSelectedFlags(lastInstructionHex, lastInstructionResult, lastInstructionFlags, reg);
        } else if (lastInstructionFlags.length > 0) {
            // For inherent instructions, we need a different approach
            System.out.println("\n=== INHERENT INSTRUCTION FLAGS ===");
            System.out.print("Flags affected: ");
            for (String flag : lastInstructionFlags) {
                System.out.print(flag + " ");
            }
            System.out.println();

            // Get current CCR to show flags
            String ccr = reg.getCCR();
            if (!ccr.equals("00")) {
                int ccrValue = Integer.parseInt(ccr, 16);
                System.out.println("Current CCR: " + ccr + " (binary: " +
                        String.format("%8s", Integer.toBinaryString(ccrValue)).replace(' ', '0') + ")");
                System.out.print("Active flags: ");
                if ((ccrValue & 0x08) != 0)
                    System.out.print("N ");
                if ((ccrValue & 0x04) != 0)
                    System.out.print("Z ");
                if ((ccrValue & 0x02) != 0)
                    System.out.print("V ");
                if ((ccrValue & 0x01) != 0)
                    System.out.print("C ");
                System.out.println();
            }
        } else {
            System.out.println("\n=== NO INSTRUCTIONS TO CALCULATE FLAGS ===");
        }
    }

    // ============================================================================
    // PHASE 1: MODE INDEXÉ - INFRASTRUCTURE DE BASE
    // ============================================================================

    /**
     * Parse le mode indexé et détermine le type exact
     * Phase 1: Supporte uniquement le déplacement nul (,X, ,Y, ,U, ,S)
     * 
     * @param operand L'opérande complet (ex: ",X", "5,Y", "A,X")
     * @return Type de mode indexé sous forme de String
     */
    /**
     * Parse le mode indexé et détermine le type exact
     * Phase 2: Supporte offsets 5-bits et Auto-Inc/Dec
     * 
     * @param operand L'opérande complet (ex: ",X", "5,Y", ",X+")
     * @return String format "TYPE:REGISTRE:VALEUR"
     */
    private String parseIndexedMode(String operand) {
        operand = operand.trim();

        // PHASE 6: Indirect Addressing Support ([...])
        if (operand.startsWith("[") && operand.endsWith("]")) {
            String inner = operand.substring(1, operand.length() - 1);
            String innerResult = parseIndexedMode(inner);

            // HARDWARE RULE ENFORCEMENT for Indirect Mode
            if (innerResult.startsWith("OFFSET_5_BIT")) {
                // Force 5-bit offset to 8-bit offset (Indirect doesn't support 5-bit)
                innerResult = innerResult.replace("OFFSET_5_BIT", "OFFSET_8_BIT");
            } else if (innerResult.startsWith("ZERO_OFFSET")) {
                // Force Zero Offset to 8-bit Offset with 0 value
                innerResult = innerResult.replace("ZERO_OFFSET", "OFFSET_8_BIT");
            } else if (innerResult.startsWith("AUTO_INC")) {
                // CHECK ILLEGAL AUTO-INCREMENT BY 1
                String[] parts = innerResult.split(":");
                // AUTO_INC:REG:VAL
                if (parts[2].equals("1")) {
                    System.out
                            .println("Error: Indirect Auto-Increment by 1 ([,R+]) is not allowed. Use [,R++] instead.");
                    return "UNKNOWN:INVALID:0";
                }
            }

            // Insert _INDIRECT into the TYPE part (first part of the string)
            // Format is TYPE:REG:VAL
            String[] parts = innerResult.split(":");
            parts[0] = parts[0] + "_INDIRECT"; // Use Underscore to avoid split issues
            return String.join(":", parts);
        }

        // 1. Déplacement nul (,X)
        if (operand.matches("^,[XYUS]$")) {
            String reg = operand.substring(1);
            return "ZERO_OFFSET:" + reg + ":0";
        }

        // 2. Auto-Incrémentation (,X+ ou ,X++)
        if (operand.matches("^,[XYUS]\\+{1,2}$")) {
            String reg = operand.substring(1, 2); // Extrait X,Y,U,S
            int amount = operand.endsWith("++") ? 2 : 1;
            return "AUTO_INC:" + reg + ":" + amount;
        }

        // 3. Auto-Décrémentation (,-X ou ,--X)
        if (operand.matches("^,-{1,2}[XYUS]$")) {
            String reg = operand.substring(operand.length() - 1);
            int amount = operand.contains("--") ? 2 : 1;
            return "AUTO_DEC:" + reg + ":" + amount;
        }

        // 4. Offset Constant 5-bits (5,X ou -3,Y)
        // Regex: nombre (positif ou négatif) suivi de virgule et registre
        if (operand.matches("^-?\\d+,[XYUS]$")) {
            String[] parts = operand.split(",");
            int offset = Integer.parseInt(parts[0]);
            String reg = parts[1];
            return "OFFSET_5_BIT:" + reg + ":" + offset;
        }

        // 5. PHASE 3: Offset Accumulateur (A,X ou B,Y ou D,U)
        if (operand.matches("^[ABD],[XYUS]$")) {
            String acc = operand.substring(0, 1); // A, B, ou D
            String reg = operand.substring(2); // X, Y, U, ou S
            return "ACC_OFFSET:" + acc + ":" + reg;
        }

        // 6. PHASE 4: Offset 8-bits ou 16-bits ($XX,R ou $XXXX,R)
        // Pattern: $XX,R ou $XXXX,R avec $ obligatoire
        if (operand.matches("^\\$[0-9A-Fa-f]+,[XYUS]$")) {
            String[] parts = operand.split(",");
            String offsetHex = parts[0].substring(1); // Enlever le $
            String register = parts[1];

            int offsetValue = Integer.parseInt(offsetHex, 16);

            // Déterminer si 8-bits ou 16-bits selon la valeur
            if (offsetValue <= 0xFF) {
                // 8-bits (0-255, sera signé lors du calcul)
                return "OFFSET_8_BIT:" + register + ":" + offsetValue;
            } else {
                // 16-bits (> 255)
                return "OFFSET_16_BIT:" + register + ":" + offsetValue;
            }
        }

        // 7. PHASE 5: PC-Relative (PC)
        // Pattern: $XX,PC ou $XXXX,PC
        if (operand.matches("^\\$[0-9A-Fa-f]+,PCR?$")) {
            String[] parts = operand.split(",");
            String offsetHex = parts[0].substring(1); // Enlever le $
            // Le registre est toujours PC

            int offsetValue = Integer.parseInt(offsetHex, 16);

            // Déterminer si 8-bits ou 16-bits selon la valeur
            if (offsetValue <= 0xFF) {
                return "PC_REL_8_BIT:PC:" + offsetValue;
            } else {
                return "PC_REL_16_BIT:PC:" + offsetValue;
            }
        }

        return "UNKNOWN:?:0";
    }

    /**
     * Simule l'extraction d'un offset 5-bits signé depuis un post-byte.
     * Bit 7 = 0 (Mode 5-bit offset)
     * Bits 4-0 = Offset signé (-16 à +15)
     */
    private int decode5BitOffset(int postByte) {
        // 1. Extraire les 5 bits de poids faible
        int offset = postByte & 0x1F; // Masque 00011111

        // 2. Vérifier le bit de signe (Bit 4, valeur 16)
        if ((offset & 0x10) != 0) {
            // 3. Extension de signe (Sign Extension)
            // On remplit les bits supérieurs avec des 1
            offset = offset | 0xFFFFFFE0;
        }
        return offset;
    }

    /**
     * Calcule l'adresse effective et GÈRE LES EFFETS DE BORD (Mise à jour
     * registres)
     * 
     * @param type     Le type d'indexation (ZERO_OFFSET, AUTO_INC, etc.)
     * @param register Le registre d'index (X, Y, U, S)
     * @param value    La valeur associée (offset ou montant inc/dec)
     * @return L'adresse effective calculée
     */
    private int calculateIndexedAddress(String type, String register, int value) {
        // PHASE 6: Handle Indirect Addressing Flag
        boolean isIndirect = type.contains("_INDIRECT");
        String effectiveType = isIndirect ? type.replace("_INDIRECT", "") : type;

        int baseAddr = 0;

        // 1. Récupérer la valeur actuelle du registre
        if (!effectiveType.startsWith("PC_REL")) { // PC-Relative modes don't use X,Y,U,S as base
            switch (register.toUpperCase()) {
                case "X":
                    baseAddr = Integer.parseInt(reg.getX(), 16);
                    break;
                case "Y":
                    baseAddr = Integer.parseInt(reg.getY(), 16);
                    break;
                case "U":
                    baseAddr = Integer.parseInt(reg.getU(), 16);
                    break;
                case "S":
                    baseAddr = Integer.parseInt(reg.getS(), 16);
                    break;
                default:
                    if (!effectiveType.equals("ACC_OFFSET")) {
                        return 0;
                    }
            }
        }

        int effectiveAddr = 0;
        int newRegValue = baseAddr;

        // 2. Calculer selon le type
        switch (effectiveType) {
            case "ZERO_OFFSET":
                effectiveAddr = baseAddr;
                break;

            case "OFFSET_5_BIT":
                effectiveAddr = baseAddr + value;
                break;

            case "AUTO_INC": // Post-Incrément (,X+)
                effectiveAddr = baseAddr; // Utilise l'adresse AVANT incrément
                newRegValue = baseAddr + value; // +1 ou +2
                break;

            case "AUTO_DEC": // Pré-Décrément (,-X)
                newRegValue = baseAddr - value; // -1 ou -2
                effectiveAddr = newRegValue; // Utilise l'adresse APRÈS décrément
                break;

            case "OFFSET_8_BIT":
                effectiveAddr = baseAddr + (byte) value;
                System.out.println("[INDEXED PHASE 4] Type=OFFSET_8_BIT Reg=" + register +
                        " Offset=$" + String.format("%02X", value) +
                        " (signé=" + (byte) value + ") => EffAddr=" +
                        String.format("%04X", effectiveAddr & 0xFFFF));
                break;

            case "OFFSET_16_BIT":
                effectiveAddr = baseAddr + (short) value;
                System.out.println("[INDEXED PHASE 4] Type=OFFSET_16_BIT Reg=" + register +
                        " Offset=$" + String.format("%04X", value) +
                        " (signé=" + (short) value + ") => EffAddr=" +
                        String.format("%04X", effectiveAddr & 0xFFFF));
                break;

            case "PC_REL_8_BIT":
                int currentPC8 = Integer.parseInt(reg.getPC(), 16);
                effectiveAddr = (currentPC8 + 3) + (byte) value;
                effectiveAddr = effectiveAddr & 0xFFFF;
                System.out.println("[INDEXED PHASE 5] Type=PC_REL_8_BIT PC=" + String.format("%04X", currentPC8) +
                        " Offset=$" + String.format("%02X", value) +
                        " (signé=" + (byte) value + ") => EffAddr=" +
                        String.format("%04X", effectiveAddr));
                break;

            case "PC_REL_16_BIT":
                int currentPC16 = Integer.parseInt(reg.getPC(), 16);
                effectiveAddr = (currentPC16 + 4) + (short) value;
                effectiveAddr = effectiveAddr & 0xFFFF;
                System.out.println("[INDEXED PHASE 5] Type=PC_REL_16_BIT PC=" + String.format("%04X", currentPC16) +
                        " Offset=$" + String.format("%04X", value) +
                        " (signé=" + (short) value + ") => EffAddr=" +
                        String.format("%04X", effectiveAddr));
                break;

            default:
                return baseAddr;
        }

        // 3. Mettre à jour le registre si nécessaire (Effet de bord)
        if (effectiveType.equals("AUTO_INC") || effectiveType.equals("AUTO_DEC")) {
            String newHex = String.format("%04X", newRegValue & 0xFFFF);
            switch (register.toUpperCase()) {
                case "X":
                    reg.setX(newHex);
                    break;
                case "Y":
                    reg.setY(newHex);
                    break;
                case "U":
                    reg.setU(newHex);
                    break;
                case "S":
                    reg.setS(newHex);
                    break;
            }
            System.out.println("   -> Registre " + register + " mis à jour: " + newHex);
        }

        // PHASE 6: Indirect Addressing Dereferencing
        if (isIndirect) {
            int original = effectiveAddr & 0xFFFF;
            // Read Pointer High Byte
            String ptrHighHex = ramMemory.getram().get(String.format("%04X", original));
            int ptrHigh = Integer.parseInt(ptrHighHex != null ? ptrHighHex : "00", 16);

            // Read Pointer Low Byte
            String ptrLowHex = ramMemory.getram().get(String.format("%04X", (original + 1) & 0xFFFF));
            int ptrLow = Integer.parseInt(ptrLowHex != null ? ptrLowHex : "00", 16);

            // Combine
            effectiveAddr = ((ptrHigh << 8) | ptrLow) & 0xFFFF;

            System.out.println("   [INDIRECT] [" + String.format("%04X", original) + "] -> "
                    + String.format("%04X", effectiveAddr));
        }

        // Debug
        if (!effectiveType.startsWith("PC_REL") && !effectiveType.equals("OFFSET_8_BIT")
                && !effectiveType.equals("OFFSET_16_BIT")) {
            System.out.println("[INDEXED PHASE 2/6] Type=" + type + " Reg=" + register +
                    " Val=" + value + " => EffAddr=" + String.format("%04X", effectiveAddr));
        }

        return effectiveAddr;
    }

    /**
     * PHASE 3: Calcule l'adresse effective pour le mode indexé avec accumulateur
     * 
     * @param accumulator   L'accumulateur utilisé ("A", "B", ou "D")
     * @param indexRegister Le registre d'index ("X", "Y", "U", "S")
     * @return L'adresse effective calculée
     */
    private int calculateAccumulatorIndexed(String accumulator, String indexRegister, boolean isIndirect) {
        // 1. Récupérer la valeur du registre d'index
        int baseAddr = 0;
        switch (indexRegister.toUpperCase()) {
            case "X":
                baseAddr = Integer.parseInt(reg.getX(), 16);
                break;
            case "Y":
                baseAddr = Integer.parseInt(reg.getY(), 16);
                break;
            case "U":
                baseAddr = Integer.parseInt(reg.getU(), 16);
                break;
            case "S":
                baseAddr = Integer.parseInt(reg.getS(), 16);
                break;
        }

        // 2. Récupérer la valeur de l'accumulateur
        int accValue = 0;
        switch (accumulator.toUpperCase()) {
            case "A":
                // A est 8-bits, mais SIGNÉ
                accValue = Integer.parseInt(reg.getA(), 16);
                // Gérer le signe (si bit 7 = 1, c'est négatif)
                if (accValue > 127) {
                    accValue = accValue - 256; // Convertir en négatif
                }
                break;

            case "B":
                // B est 8-bits, mais SIGNÉ
                accValue = Integer.parseInt(reg.getB(), 16);
                if (accValue > 127) {
                    accValue = accValue - 256;
                }
                break;

            case "D":
                // D est 16-bits (D = A:B), NON SIGNÉ dans ce contexte
                accValue = Integer.parseInt(reg.getD(), 16);
                break;
        }

        // 3. Calculer l'adresse effective
        int effectiveAddr = baseAddr + accValue;
        effectiveAddr = effectiveAddr & 0xFFFF; // Masque 16-bits initial

        // PHASE 6: Indirect Addressing Dereferencing
        if (isIndirect) {
            int original = effectiveAddr;
            // Read Pointer High Byte
            String ptrHighHex = ramMemory.getram().get(String.format("%04X", original));
            int ptrHigh = Integer.parseInt(ptrHighHex != null ? ptrHighHex : "00", 16);

            // Read Pointer Low Byte
            String ptrLowHex = ramMemory.getram().get(String.format("%04X", (original + 1) & 0xFFFF));
            int ptrLow = Integer.parseInt(ptrLowHex != null ? ptrLowHex : "00", 16);

            // Combine
            effectiveAddr = ((ptrHigh << 8) | ptrLow) & 0xFFFF;

            System.out.println("   [INDIRECT ACC] [" + String.format("%04X", original) + "] -> "
                    + String.format("%04X", effectiveAddr));
        }

        // Debug
        System.out.println("[INDEXED PHASE 3] Type=ACC_OFFSET Acc=" + accumulator +
                " Val=" + accValue + " Base=" + String.format("%04X", baseAddr) +
                " => EffAddr=" + String.format("%04X", effectiveAddr));

        return effectiveAddr;
    }

    /**
     * Génère le post-byte pour le mode indexé selon la spécification 6809
     * 
     * @param type     Type de mode indexé (ZERO_OFFSET, AUTO_INC, etc.)
     * @param register Registre d'index (X, Y, U, S) ou accumulateur (A, B, D)
     * @param value    Valeur (offset ou quantité inc/dec)
     * @return Post-byte en format hexadécimal (String de 2 caractères)
     */
    private String generatePostByte(String type, String register, int value) {
        // PHASE 6: Indirect Addressing Support (Set Bit 4)
        if (type.endsWith("_INDIRECT")) {
            String baseType = type.replace("_INDIRECT", "");
            // Recursive call to get standard post-byte
            String baseHex = generatePostByte(baseType, register, value);
            int pb = Integer.parseInt(baseHex, 16);

            // Set Bit 4 for Indirection (Standard & PCR)
            // 8C (PCR 8) -> 9C
            // 8D (PCR 16) -> 9D
            // 9F (Indirect 16-bit) -> 9F (unchanged? No, Offset 16 is 8F/9F/AF/BF ?)
            // Offset 16 bit is 1RR01001 (89/A9/C9/E9). Indirect is 1RR11001 (99/B9/D9/F9).
            // 89 | 10 = 99. Correct.
            pb = pb | 0x10;

            return String.format("%02X", pb);
        }

        int postByte = 0;

        // Bits 6-5: Registre (RR)
        // 00=X, 01=Y, 10=U, 11=S
        int regBits = 0;
        switch (register.toUpperCase()) {
            case "X":
                regBits = 0b00;
                break;
            case "Y":
                regBits = 0b01;
                break;
            case "U":
                regBits = 0b10;
                break;
            case "S":
                regBits = 0b11;
                break;
        }

        switch (type) {
            case "ZERO_OFFSET":
                // Format: 1RR00100
                postByte = 0b10000100 | (regBits << 5);
                break;

            case "AUTO_INC":
                // ,R+ = 1RR00000
                // ,R++ = 1RR00001
                if (value == 1) {
                    postByte = 0b10000000 | (regBits << 5);
                } else { // value == 2
                    postByte = 0b10000001 | (regBits << 5);
                }
                break;

            case "AUTO_DEC":
                // ,-R = 1RR00010
                // ,--R = 1RR00011
                if (value == 1) {
                    postByte = 0b10000010 | (regBits << 5);
                } else { // value == 2
                    postByte = 0b10000011 | (regBits << 5);
                }
                break;

            case "OFFSET_5_BIT":
                // Format: 0RRnnnnn (offset signé 5-bits)
                // Convertir l'offset en 5-bits signé
                int offset5 = value & 0x1F; // Garde 5 bits
                postByte = (regBits << 5) | offset5;
                break;

            case "ACC_OFFSET":
                // A,R = 1RR00110
                // B,R = 1RR00101
                // D,R = 1RR01011
                switch (register.toUpperCase()) {
                    case "A":
                        postByte = 0b10000110;
                        break;
                    case "B":
                        postByte = 0b10000101;
                        break;
                    case "D":
                        postByte = 0b10001011;
                        break;
                }
                // Pour ACC_OFFSET, 'value' contient le registre d'index
                // On doit réinterpréter register comme l'acc et récupérer le vrai reg
                break;

            case "OFFSET_8_BIT":
                // Format: 1RR01000 (8-bit offset)
                // L'offset lui-même sera stocké dans cleanedOperand après le post-byte
                postByte = 0b10001000 | (regBits << 5);
                break;

            case "OFFSET_16_BIT":
                // Format: 1RR01001 (16-bit offset)
                // L'offset lui-même sera stocké dans cleanedOperand après le post-byte
                postByte = 0b10001001 | (regBits << 5);
                break;

            case "PC_REL_8_BIT":
                // Table 2: 8C
                postByte = 0x8C;
                break;

            case "PC_REL_16_BIT":
                // Table 2: 8D
                postByte = 0x8D;
                break;
        }

        return String.format("%02X", postByte);
    }

    /**
     * Génère le post-byte spécifiquement pour les modes avec offset accumulateur
     * Nécessaire car pour ACC_OFFSET, le registre est l'index (X,Y,U,S) pas
     * l'accumulateur
     * 
     * @param accumulator   L'accumulateur (A, B, ou D)
     * @param indexRegister Le registre d'index (X, Y, U, S)
     * @return Post-byte en format hexadécimal
     */
    private String generatePostByteAccOffset(String accumulator, String indexRegister, boolean isIndirect) {
        int postByte = 0;

        // Bits 6-5: Registre d'index (RR)
        int regBits = 0;
        switch (indexRegister.toUpperCase()) {
            case "X":
                regBits = 0b00;
                break;
            case "Y":
                regBits = 0b01;
                break;
            case "U":
                regBits = 0b10;
                break;
            case "S":
                regBits = 0b11;
                break;
        }

        // A,R = 1RR00110
        // B,R = 1RR00101
        // D,R = 1RR01011
        switch (accumulator.toUpperCase()) {
            case "A":
                postByte = 0b10000110 | (regBits << 5);
                break;
            case "B":
                postByte = 0b10000101 | (regBits << 5);
                break;
            case "D":
                postByte = 0b10001011 | (regBits << 5);
                break;
        }

        // PHASE 6: Indirect Addressing
        if (isIndirect) {
            postByte = postByte | 0x10;
        }

        return String.format("%02X", postByte);
    }

    /**
     * Handles 8-bit Indexed ALU operations (ADD, SUB, AND, OR, EOR, CMP, BIT).
     * 1. Calculates effective address.
     * 2. Reads 1 byte from RAM.
     * 3. Executes the math operation using the read value.
     * 
     * @param operand The indexed operand (e.g., ",X", "5,Y", "A,X")
     * @param hexOpcode The opcode in hex format (e.g., "AB", "A0", "A4")
     * @param cycleCount The cycle count for this instruction (typically 4 for indexed)
     * @param mathOperation A BiFunction that performs the math operation (e.g., performAdditionA, performSubtractionA)
     * @return String array with [opcode, cycle, cleanedOperand]
     */
    private String[] handleIndexedALU(String operand, String hexOpcode, int cycleCount, java.util.function.BiFunction<String, registre, Integer> mathOperation) {
        String parseResult = parseIndexedMode(operand);
        String[] parts = parseResult.split(":");
        String type = parts[0];

        // PHASE 6: Indirect Flag Extraction
        boolean isIndirect = type.contains("_INDIRECT");
        String cleanType = isIndirect ? type.replace("_INDIRECT", "") : type;

        int effectiveAddr = 0;
        String cleanedOperand = "";

        if (cleanType.equals("ACC_OFFSET")) {
            // PHASE 3: Offset accumulateur
            String acc = parts[1]; // A, B, ou D
            String indexReg = parts[2]; // X, Y, U, S
            // Pass isIndirect
            effectiveAddr = calculateAccumulatorIndexed(acc, indexReg, isIndirect);
            // Generate post-byte (Pass isIndirect)
            cleanedOperand = generatePostByteAccOffset(acc, indexReg, isIndirect);
        } else if (!cleanType.equals("UNKNOWN")) {
            // PHASE 2: Autres types (offsets, auto-inc/dec)
            String register = parts[1];
            int value = Integer.parseInt(parts[2]);
            // Pass dirty type (calculateIndexedAddress handles it)
            effectiveAddr = calculateIndexedAddress(type, register, value);
            // Pass dirty type (generatePostByte handles it)
            cleanedOperand = generatePostByte(type, register, value);

            // PHASE 4 & 5: Ajouter les octets d'offset au post-byte
            if (cleanType.equals("OFFSET_8_BIT") || cleanType.equals("PC_REL_8_BIT")) {
                String offsetHex = String.format("%02X", value & 0xFF);
                cleanedOperand += offsetHex;
            } else if (cleanType.equals("OFFSET_16_BIT") || cleanType.equals("PC_REL_16_BIT")) {
                String offsetHex = String.format("%04X", value & 0xFFFF);
                cleanedOperand += offsetHex;
            }
        } else {
            System.out.println("Mode indexé non supporté ou invalide: " + operand);
            cleanedOperand = "";
        }

        if (!cleanType.equals("UNKNOWN")) {
            // Read 1 byte (2 hex chars) from RAM at effectiveAddr
            String ramValue = readFromRAM(effectiveAddr);

            // Execute the math operation using the read value
            int result = mathOperation.apply(ramValue, reg);

            // Store for flag calculation
            lastInstructionHex = ramValue;
            lastInstructionResult = result;
            // Flags depend on the operation type (most use Z, N, V, C; BIT uses Z, N, V)
            // We'll set the flags based on the operation, but for now use common flags
            // The actual flags are set by the performXXX methods
            lastInstructionFlags = new String[] { "Z", "N", "V", "C" };

            return new String[] { hexOpcode, String.valueOf(cycleCount), cleanedOperand };
        } else {
            return new String[] { "00", "0", "" };
        }
    }

    private String[] handleRMW(String firstWord, String secondWord, String extOpcode, String idxOpcode, int extCycle, int idxCycle,
            java.util.function.BiFunction<Integer, registre, Integer> operation) {
        String modeDetected = determineMode(secondWord);
        int effectiveAddr = -1;
        String cleanedOperand = "";
        String opcodeSel = "00";
        int cyclesSel = 0;

        if (modeDetected.equals(indexe)) {
            String parseResult = parseIndexedMode(secondWord);
            String[] parts = parseResult.split(":");
            String type = parts[0];
            boolean isIndirect = type.contains("_INDIRECT");
            String cleanType = isIndirect ? type.replace("_INDIRECT", "") : type;
            if (!cleanType.equals("UNKNOWN")) {
                if (cleanType.equals("ACC_OFFSET")) {
                    String acc = parts[1];
                    String indexReg = parts[2];
                    effectiveAddr = calculateAccumulatorIndexed(acc, indexReg, isIndirect);
                    cleanedOperand = generatePostByteAccOffset(acc, indexReg, isIndirect);
                } else {
                    String register = parts[1];
                    int value = Integer.parseInt(parts[2]);
                    effectiveAddr = calculateIndexedAddress(type, register, value);
                    cleanedOperand = generatePostByte(type, register, value);
                    if (cleanType.equals("OFFSET_8_BIT") || cleanType.equals("PC_REL_8_BIT")) {
                        cleanedOperand += String.format("%02X", value & 0xFF);
                    } else if (cleanType.equals("OFFSET_16_BIT") || cleanType.equals("PC_REL_16_BIT")) {
                        cleanedOperand += String.format("%04X", value & 0xFFFF);
                    }
                }
                opcodeSel = idxOpcode;
                cyclesSel = idxCycle;
            } else {
                return new String[] { "00", "0", "" };
            }
        } else if (modeDetected.equals(etendu) || (secondWord.startsWith("$") && !secondWord.contains(",")
                && secondWord.replace("$", "").length() == 4)) {
            String addr = secondWord.replace(">", "").replace("$", "");
            effectiveAddr = Integer.parseInt(addr, 16);
            cleanedOperand = addr;
            opcodeSel = extOpcode;
            cyclesSel = extCycle;
        } else {
            return new String[] { "00", "0", "" };
        }

        String oldHex = readFromRAM(effectiveAddr);
        int oldVal = Integer.parseInt(oldHex, 16) & 0xFF;
        int newVal = operation.apply(oldVal, reg) & 0xFF;
        if (!firstWord.equals("TST")) {
            writeToRAM(effectiveAddr, String.format("%02X", newVal));
        }
        lastInstructionHex = String.format("%02X", oldVal);
        lastInstructionResult = newVal;
        lastInstructionFlags = new String[] { "Z", "N", "V", "C" };
        this.cycle = cyclesSel;
        return new String[] { opcodeSel, cleanedOperand };
    }

    private int getCCRInt() { return Integer.parseInt(reg.getCCR(), 16) & 0xFF; }
    private void setCCRInt(int ccr) { reg.setCCR(String.format("%02X", ccr & 0xFF)); }
    private void setZ(boolean on) { int c=getCCRInt(); c = on ? (c|0x04):(c&~0x04); setCCRInt(c); }
    private void setN(boolean on) { int c=getCCRInt(); c = on ? (c|0x08):(c&~0x08); setCCRInt(c); }
    private void setV(boolean on) { int c=getCCRInt(); c = on ? (c|0x02):(c&~0x02); setCCRInt(c); }
    private void setC(boolean on) { reg.setCarryFlag(on?1:0); }
    private void updateNZ(int val) { setZ((val & 0xFF)==0); setN((val & 0x80)!=0); }

    private int performINC_Mem(int value, registre reg) { int r=(value+1)&0xFF; updateNZ(r); setV(r==0x80); return r; }
    private int performDEC_Mem(int value, registre reg) { int r=(value-1)&0xFF; updateNZ(r); setV(r==0x7F); return r; }
    private int performCLR_Mem(int value, registre reg) { int r=0; updateNZ(r); setV(false); setC(false); return r; }
    private int performCOM_Mem(int value, registre reg) { int r=(~value)&0xFF; updateNZ(r); setV(false); setC(true); return r; }
    private int performNEG_Mem(int value, registre reg) { int r=(-value)&0xFF; updateNZ(r); setV((value&0xFF)==0x80); setC((r&0xFF)!=0); return r; }
    private int performTST_Mem(int value, registre reg) { int r=value&0xFF; updateNZ(r); setV(false); return r; }
    private int performLSR_Mem(int value, registre reg) { int c=value&1; int r=(value>>>1)&0x7F; updateNZ(r); setN(false); setV(false); setC(c!=0); return r; }
    private int performASR_Mem(int value, registre reg) { int c=value&1; int msb=value&0x80; int r=(value>>>1)|msb; r&=0xFF; updateNZ(r); setV(false); setC(c!=0); return r; }
    private int performASL_Mem(int value, registre reg) { int c=(value>>>7)&1; int r=(value<<1)&0xFF; updateNZ(r); setC(c!=0); setV(((r&0x80)!=0) ^ (c!=0)); return r; }
    private int performROL_Mem(int value, registre reg) { int oc=reg.getCarryFlag(); int c=(value>>>7)&1; int r=((value<<1)&0xFF)|(oc&1); updateNZ(r); setC(c!=0); setV(((r&0x80)!=0) ^ (c!=0)); return r; }
    private int performROR_Mem(int value, registre reg) { int oc=reg.getCarryFlag(); int c=value&1; int r=((value>>>1)&0x7F)|((oc&1)<<7); updateNZ(r); setC(c!=0); setV(((r&0x80)!=0) ^ (c!=0)); return r; }

    /**
     * Handles 16-bit Indexed ALU operations (ADDD, SUBD, CMPD, CMPX, CMPY, CMPU, CMPS).
     * 1. Calculates effective address.
     * 2. Reads 16 bits (2 bytes) from RAM.
     * 3. Executes the math operation using the read value.
     * 
     * @param operand The indexed operand (e.g., ",X", "5,Y", "A,X")
     * @param hexOpcode The opcode in hex format (e.g., "E3", "A3", "10A3")
     * @param cycleCount The cycle count for this instruction
     * @param mathOperation A BiFunction that performs the math operation (e.g., performAdditionD, performCompareX)
     * @return String array with [opcode, cycle, cleanedOperand]
     */
    private String[] handle16BitIndexedALU(String operand, String hexOpcode, int cycleCount, java.util.function.BiFunction<String, registre, Integer> mathOperation) {
        String parseResult = parseIndexedMode(operand);
        String[] parts = parseResult.split(":");
        String type = parts[0];

        // PHASE 6: Indirect Flag Extraction
        boolean isIndirect = type.contains("_INDIRECT");
        String cleanType = isIndirect ? type.replace("_INDIRECT", "") : type;

        int effectiveAddr = 0;
        String cleanedOperand = "";

        if (cleanType.equals("ACC_OFFSET")) {
            // PHASE 3: Offset accumulateur
            String acc = parts[1]; // A, B, ou D
            String indexReg = parts[2]; // X, Y, U, S
            // Pass isIndirect
            effectiveAddr = calculateAccumulatorIndexed(acc, indexReg, isIndirect);
            // Generate post-byte (Pass isIndirect)
            cleanedOperand = generatePostByteAccOffset(acc, indexReg, isIndirect);
        } else if (!cleanType.equals("UNKNOWN")) {
            // PHASE 2: Autres types (offsets, auto-inc/dec)
            String register = parts[1];
            int value = Integer.parseInt(parts[2]);
            // Pass dirty type (calculateIndexedAddress handles it)
            effectiveAddr = calculateIndexedAddress(type, register, value);
            // Pass dirty type (generatePostByte handles it)
            cleanedOperand = generatePostByte(type, register, value);

            // PHASE 4 & 5: Ajouter les octets d'offset au post-byte
            if (cleanType.equals("OFFSET_8_BIT") || cleanType.equals("PC_REL_8_BIT")) {
                String offsetHex = String.format("%02X", value & 0xFF);
                cleanedOperand += offsetHex;
            } else if (cleanType.equals("OFFSET_16_BIT") || cleanType.equals("PC_REL_16_BIT")) {
                String offsetHex = String.format("%04X", value & 0xFFFF);
                cleanedOperand += offsetHex;
            }
        } else {
            System.out.println("Mode indexé non supporté ou invalide: " + operand);
            cleanedOperand = "";
        }

        if (!cleanType.equals("UNKNOWN")) {
            // Read 2 bytes from RAM (Big Endian: High @ addr, Low @ addr+1)
            String highByte = readFromRAM(effectiveAddr);
            String lowByte = readFromRAM(effectiveAddr + 1);
            String val16 = highByte + lowByte;

            // Execute the math operation using the read value
            int result = mathOperation.apply(val16, reg);

            // Store for flag calculation
            lastInstructionHex = val16;
            lastInstructionResult = result;
            // Flags depend on the operation type (most use Z, N, V, C)
            lastInstructionFlags = new String[] { "Z", "N", "V", "C" };

            return new String[] { hexOpcode, String.valueOf(cycleCount), cleanedOperand };
        } else {
            return new String[] { "00", "0", "" };
        }
    }

    /**
     * Handles the logic for 16-bit Indexed Load instructions.
     * 1. Parses the indexed operand.
     * 2. Calculates the effective address (using existing engine).
     * 3. Reads 16 bits (2 bytes) from RAM.
     * 4. Updates the target register.
     * 5. Returns the Opcode, Cycle, and CleanedOperand components.
     * 
     * @param operand The indexed operand (e.g., ",X", "5,Y", "A,X")
     * @param hexOpcode The opcode in hex format (e.g., "EC", "AE", "10AE")
     * @param cycleCount The cycle count for this instruction
     * @param registerSetter A Consumer that sets the target register (e.g., reg::setD, reg::setX)
     * @return String array with [opcode, cycle, cleanedOperand]
     */
    private String[] handle16BitIndexedLoad(String operand, String hexOpcode, int cycleCount, java.util.function.Consumer<String> registerSetter) {
        String parseResult = parseIndexedMode(operand);
        String[] parts = parseResult.split(":");
        String type = parts[0];

        // PHASE 6: Indirect Flag Extraction
        boolean isIndirect = type.contains("_INDIRECT");
        String cleanType = isIndirect ? type.replace("_INDIRECT", "") : type;

        int effectiveAddr = 0;
        String cleanedOperand = "";

        if (cleanType.equals("ACC_OFFSET")) {
            // PHASE 3: Offset accumulateur
            String acc = parts[1]; // A, B, ou D
            String indexReg = parts[2]; // X, Y, U, S
            // Pass isIndirect
            effectiveAddr = calculateAccumulatorIndexed(acc, indexReg, isIndirect);
            // Generate post-byte (Pass isIndirect)
            cleanedOperand = generatePostByteAccOffset(acc, indexReg, isIndirect);
        } else if (!cleanType.equals("UNKNOWN")) {
            // PHASE 2: Autres types (offsets, auto-inc/dec)
            String register = parts[1];
            int value = Integer.parseInt(parts[2]);
            // Pass dirty type (calculateIndexedAddress handles it)
            effectiveAddr = calculateIndexedAddress(type, register, value);
            // Pass dirty type (generatePostByte handles it)
            cleanedOperand = generatePostByte(type, register, value);

            // PHASE 4 & 5: Ajouter les octets d'offset au post-byte
            if (cleanType.equals("OFFSET_8_BIT") || cleanType.equals("PC_REL_8_BIT")) {
                String offsetHex = String.format("%02X", value & 0xFF);
                cleanedOperand += offsetHex;
            } else if (cleanType.equals("OFFSET_16_BIT") || cleanType.equals("PC_REL_16_BIT")) {
                String offsetHex = String.format("%04X", value & 0xFFFF);
                cleanedOperand += offsetHex;
            }
        } else {
            System.out.println("Mode indexé non supporté ou invalide: " + operand);
            cleanedOperand = "";
        }

        if (!cleanType.equals("UNKNOWN")) {
            // Read 2 bytes from RAM (Big Endian: High @ addr, Low @ addr+1)
            String highByte = readFromRAM(effectiveAddr);
            String lowByte = readFromRAM(effectiveAddr + 1);
            String val = highByte + lowByte;

            // Update target register
            registerSetter.accept(val);

            // Store for flag calculation
            lastInstructionHex = val;
            lastInstructionResult = Integer.parseInt(val, 16);
            lastInstructionFlags = new String[] { "Z", "N", "V" };

            return new String[] { hexOpcode, String.valueOf(cycleCount), cleanedOperand };
        } else {
            return new String[] { "00", "0", "" };
        }
    }

    /**
     * Helper method to write to RAM
     * Used by Store instructions (STA, STB, etc.)
     */
    private void writeToRAM(int address, String valueHex) {
        String addr = String.format("%04X", address);
        // Ensure valueHex is 2 chars (pad with 0 if needed)
        if (valueHex.length() < 2) {
            valueHex = "0" + valueHex;
        } else if (valueHex.length() > 2) {
            valueHex = valueHex.substring(valueHex.length() - 2);
        }

        ramMemory.getram().put(addr, valueHex.toUpperCase());
        System.out.println("[RAM WRITE] Addr=" + addr + " Val=" + valueHex.toUpperCase());
    }

    /**
     * Helper method to write 16-bit value to RAM
     * Writes High byte at address, Low byte at address+1
     */
    private void writeToRAM16(int address, String valueHex16) {
        // Ensure 4 chars
        while (valueHex16.length() < 4) {
            valueHex16 = "0" + valueHex16;
        }

        String highByte = valueHex16.substring(0, 2);
        String lowByte = valueHex16.substring(2, 4);

        writeToRAM(address, highByte);
        writeToRAM(address + 1, lowByte);
    }

    /**
     * Lit une valeur en RAM à une adresse donnée
     * 
     * @param address L'adresse en format int
     * @return La valeur en hexadécimal (String de 2 caractères)
     */
    private String readFromRAM(int address) {
        String addr = String.format("%04X", address);
        String value = ramMemory.getram().get(addr);

        if (value == null) {
            System.out.println("Attention: Adresse RAM non initialisée: " + addr);
            return "00";
        }

        return value;
    }
}
