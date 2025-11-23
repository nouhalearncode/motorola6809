package processeur;

public class registre {

    
    private static int pc = 0x0000;  // Program Counter in hex
    private String A = "00";         // Accumulator A in hex
    private String B = "00";         // Accumulator B in hex
    private String X = "0000";       // Index Register X in hex
    private String Y = "0000";       // Index Register Y in hex
    private String SP = "0000";      // Stack Pointer in hex
    private String CCR = "00";       // Condition Code Register in hex

    // Getters
    public String getA() { return A; }
    public String getB() { return B; }
    public String getX() { return X; }
    public String getY() { return Y; }
    public String getSP() { return SP; }
    public String getCCR() { return CCR; }
    public String getPC() { return String.format("%04X", pc); }

    // Setters
    public void setA(String value) { this.A = value; }
    public void setB(String value) { this.B = value; }
    public void setX(String value) { this.X = value; }
    public void setY(String value) { this.Y = value; }
    public void setSP(String value) { this.SP = value; }
    public void setCCR(String value) { this.CCR = value; }
    public void setPC(int value) { this.pc = value; }

    // Display all registers
    public void displayRegisters() {
        System.out.println("\n=== REGISTERS ===");
        System.out.println("A: " + A + "  B: " + B + "  X: " + X + "  Y: " + Y);
        System.out.println("SP: " + SP + "  CCR: " + CCR + "  PC: " + getPC());
    }
}
