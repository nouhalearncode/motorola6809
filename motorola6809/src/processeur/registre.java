package processeur;

public class registre {

    
    private static int pc = 0x0000;  // Program Counter in hex
    private String A = "00";         // Accumulator A in hex
    private String B = "00";         // Accumulator B in hex
    private String D = "0000";
    private String X = "0000";       // Index Register X in hex
    private String Y = "0000";       // Index Register Y in hex
    private String S = "0000";      // Stack Pointer in hex
    private String U = "0000";
    private String CCR = "00";       // Condition Code Register in hex

    // Getters
    public String getA() { return A; }
    public String getB() { return B; }
    public String getD() { return D; }
    public String getX() { return X; }
    public String getY() { return Y; }
    public String getS() { return S; }
    public String getU() { return U; }
    public String getCCR() { return CCR; }
    public String getPC() { return String.format("%04X", pc); }

    // Setters
    public void setA(String value) { this.A = value; }
    public void setB(String value) { this.B = value; }
    public void setD(String value) { this.D = value; }
    public void setX(String value) { this.X = value; }
    public void setY(String value) { this.Y = value; }
    public void setS(String value) { this.S = value; }
    public void setU(String value) { this.U = value; }
    public void setCCR(String value) { this.CCR = value; }
    public void setPC(int value) { this.pc = value; }

    // Display all registers
    public void displayRegisters() {
        System.out.println("\n=== REGISTERS ===");
        System.out.println("A: " + A + "  B: " + B + " D: " + D + "  X: " + X + "  Y: " + Y);
        System.out.println("S: " + S + " U: " + U +"  CCR: " + CCR + "  PC: " + getPC());
    }
}
