package processeur;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class ram {
    private Map<String, String> ra;
    
    public ram() {
        ra = new LinkedHashMap<>();
        // Initialiser 65536 cases (0000 à FFFF)
        for (int i = 0; i < 65536; i++) {
            String addr = String.format("%04X", i);
            ra.put(addr, "00");
        }
       
    }
    
    public Map<String, String> getram() {
        return ra;
    }



    public void writeAndClear(String data) {
    // Clear ALL memory first
    for (int i = 0; i < 65536; i++) {
        String addr = String.format("%04X", i);
        ra.put(addr, "00");
    }
    
    // If no data, just return (all zeros)
    if (data.isEmpty()) {
        return;
    }
    
    // Split data into 2-character chunks starting from address 0000
    int address = 0;
    
    for (int i = 0; i < data.length(); i += 2) {
        String chunk;
        if (i + 2 <= data.length()) {
            chunk = data.substring(i, i + 2);  // Get 2 characters
        } else {
            // If odd number of characters, pad with zero
            chunk = data.substring(i) + "0";
        }
        
        String addr = String.format("%04X", address);
        ra.put(addr, chunk);
        address++;
        
        // Stop if we reach memory limit
        if (address >= 65536) break;
    }
}
    

    // DISPLAY range for RAM
    public void displayRange(String startAddr, String endAddr) {
        int start = Integer.parseInt(startAddr, 16);
        int end = Integer.parseInt(endAddr, 16);
        
        System.out.println("\n=== RAM " + startAddr + "-" + endAddr + " ===");
        for (int i = start; i <= end; i++) {
            String addr = String.format("%04X", i);
            String val = ra.get(addr);
            System.out.println(addr + ": " + val);
        }
    }

}