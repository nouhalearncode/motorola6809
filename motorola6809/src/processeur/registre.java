package processeur;

public class registre {

    private static int pc = 0x0000; // Program Counter in hex
    private String A = "00"; // Accumulator A in hex
    private String B = "00"; // Accumulator B in hex
    private String D = "0000";
    private String X = "0000"; // Index Register X in hex
    private String Y = "0000"; // Index Register Y in hex
    private String S = "0000"; // Stack Pointer in hex
    private String U = "0000";
    private String CCR = "00"; // Condition Code Register in hex

    // Getters
    public String getA() {
        return A;
    }

    public String getB() {
        return B;
    }

    public String getD() {
        return D;
    }

    public String getX() {
        return X;
    }

    public String getY() {
        return Y;
    }

    public String getS() {
        return S;
    }

    public String getU() {
        return U;
    }

    public String getCCR() {
        return CCR;
    }

    public String getPC() {
        return String.format("%04X", pc);
    }

    // Setters
    public void setA(String value) {
        this.A = value;
        // D est la concaténation de A et B (D = A:B)
        this.D = this.A + this.B;
    }

    public void setB(String value) {
        this.B = value;
        // D est la concaténation de A et B (D = A:B)
        this.D = this.A + this.B;
    }

    public void setD(String value) {
        this.D = value;
        // Quand D change, il faut aussi mettre à jour A et B
        if (value.length() >= 4) {
            this.A = value.substring(0, 2); // 2 premiers caractères = A
            this.B = value.substring(2, 4); // 2 derniers caractères = B
        }
    }

    public void setX(String value) {
        this.X = value;
    }

    public void setY(String value) {
        this.Y = value;
    }

    public void setS(String value) {
        this.S = value;
    }

    public void setU(String value) {
        this.U = value;
    }

    public void setCCR(String value) {
        this.CCR = value;
    }

    public void setPC(int value) {
        this.pc = value;
    }

    public int getCarryFlag() {
        // CCR format: bits 0=C, 1=V, 2=Z, 3=N, etc.
        int ccrValue = Integer.parseInt(CCR, 16);
        return (ccrValue & 0x01); // Bit 0 is carry
    }

    public void setCarryFlag(int carry) {
        int ccrValue = Integer.parseInt(CCR, 16);
        if (carry == 1) {
            ccrValue = ccrValue | 0x01; // Set bit 0
        } else {
            ccrValue = ccrValue & 0xFE; // Clear bit 0
        }
        CCR = String.format("%02X", ccrValue);
    }

    // Display all registers
    public void displayRegisters() {
        System.out.println("\n=== REGISTERS ===");
        System.out.println("A: " + A + "  B: " + B + " D: " + D + "  X: " + X + "  Y: " + Y);
        System.out.println("S: " + S + " U: " + U + "  CCR: " + CCR + "  PC: " + getPC());
    }
}
