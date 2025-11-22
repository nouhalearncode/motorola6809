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
        System.out.println("[ROM] Initialisée - 65536 octets disponibles (0000-FFFF)\n");
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

    // ADD THIS METHOD
    public void displayRange(String startAddr, String endAddr) {
        int start = Integer.parseInt(startAddr, 16);
        int end = Integer.parseInt(endAddr, 16);
        
        System.out.println("\n=== ROM " + startAddr + "-" + endAddr + " ===");
        for (int i = start; i <= end; i++) {
            String addr = String.format("%04X", i);
            String val = memory.get(addr);
            
            System.out.println(addr + ": " + val);
            }
        }
    }

    
    


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    
//     // ÉCRIRE en RAM
//     public void write(String address, String value) {
//         String formattedAddr = formatAddress(address);
//         if (memory.containsKey(formattedAddr)) {
//             memory.put(formattedAddr, value.toUpperCase());
//             System.out.println("[RAM WRITE] @" + formattedAddr + " <- " + value);
//         } else {
//             System.out.println("[RAM ERROR] Adresse invalide: " + address);
//         }
//     }
    
//     // LIRE depuis RAM
//     public String read(String address) {
//         String formattedAddr = formatAddress(address);
//         String value = memory.get(formattedAddr);
//         if (value != null) {
//             System.out.println("[RAM READ] @" + formattedAddr + " -> " + value);
//             return value;
//         }
//         System.out.println("[RAM ERROR] Adresse invalide: " + address);
//         return "00";
//     }
    
//     // Formater l'adresse en 4 digits hexa
//     private String formatAddress(String address) {
//         if (address.length() < 4) {
//             return String.format("%04X", Integer.parseInt(address, 16));
//         }
//         return address.toUpperCase();
//     }
    
//     // AFFICHER la RAM (seulement les cases non-vides)
//     public void display() {
//         System.out.println("\n╔══════════════════════╗");
//         System.out.println("║     CONTENU RAM      ║");
//         System.out.println("╠══════════════════════╣");
        
//         boolean hasData = false;
//         for (String addr : memory.keySet()) {
//             String val = memory.get(addr);
//             if (!val.equals("00")) {
//                 System.out.println("║  " + addr + " : " + val + "             ║");
//                 hasData = true;
//             }
//         }
        
//         if (!hasData) {
//             System.out.println("║     (vide)            ║");
//         }
        
//         System.out.println("╚══════════════════════╝\n");
//     }
    
//     // Afficher une plage spécifique
//     public void displayRange(String startAddr, String endAddr) {
//         int start = Integer.parseInt(startAddr, 16);
//         int end = Integer.parseInt(endAddr, 16);
        
//         System.out.println("\n╔══════════════════════╗");
//         System.out.println("║   RAM " + startAddr + "-" + endAddr + "         ║");
//         System.out.println("╠══════════════════════╣");
        
//         for (int i = start; i <= end; i++) {
//             String addr = String.format("%04X", i);
//             String val = memory.get(addr);
//             System.out.println("║  " + addr + " : " + val + "             ║");
//         }
        
//         System.out.println("╚══════════════════════╝\n");
//     }
// }