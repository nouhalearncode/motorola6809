package processeur;

import java.util.LinkedHashMap;
import java.util.Map;

public class ROM {

    Map<String, String> memory;
    private int currentAddress = 0;

    public ROM() {
        memory = new LinkedHashMap<>();
        // Initialiser 65536 cases (0000 à FFFF)
        for (int i = 0; i < 65536; i++) {
            String addr = String.format("%04X", i); // 4-digit hex now
            memory.put(addr, "00");
        }
        
    }

    public void debugMemory() {
        System.out.println("DEBUG: ROM Memory Contents 0000-000F:");
        for (int i = 0; i <= 0x000F; i++) {
            String addr = String.format("%04X", i);
            String val = memory.get(addr);
            System.out.println("DEBUG: " + addr + " = " + val + " (exists: " + memory.containsKey(addr) + ")");
        }
    }

    // Add to ROM.java
public void setWritePointer(int address) {
    if (address >= 0 && address < 65536) {
        currentAddress = address;
        System.out.println("[ROM] Write pointer set to: " + String.format("%04X", address));
    }
}

    // ADD THIS METHOD - THIS IS WHAT'S MISSING
    public Map<String, String> getMemory() {
        return memory;
    }

    // Write data in 2-character chunks
    public void writeData(String data) {
        // Split data into 2-character chunks
        for (int i = 0; i < data.length(); i += 2) {
            if (currentAddress >= 65536) {
                System.out.println("ERROR: ROM full!");
                return;
            }

            String chunk;
            if (i + 2 <= data.length()) {
                chunk = data.substring(i, i + 2);
            } else {
                chunk = data.substring(i); // Last chunk might be 1 character
            }

            String address = String.format("%04X", currentAddress);
            memory.put(address, chunk);
            currentAddress++;
        }
    }

    // NEW METHOD: Reset write pointer to start
    public void resetWritePointer() {
        currentAddress = 0;
    }

    // NEW METHOD: Get current write address
    public String getCurrentAddress() {
        return String.format("%04X", currentAddress);
    }

    public int getCurrentAddressInt() {
        return currentAddress;
    }

    // ADD THIS METHOD
    // ADD THIS METHOD
    // ADD THIS METHOD
    public void displayRange(String startAddr, String endAddr) {
        int start = Integer.parseInt(startAddr, 16);
        int end = Integer.parseInt(endAddr, 16);

        System.out.println("\n=== ROM " + startAddr + "-" + endAddr + " ===");
        for (int i = start; i <= end; i++) {
            String addr = String.format("%04X", i);
            String val = memory.get(addr);

            // Always show the address, even if it's "00"
            System.out.println(addr + ": " + val);
        }
    }
}