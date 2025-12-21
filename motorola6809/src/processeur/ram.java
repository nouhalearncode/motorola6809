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
    // WRITE AND CLEAR: Only keep current word at 0000, clear everything else
    // In your ram class - FIX THIS METHOD
// public void writeAndClear(String data) {
//     // DON'T clear all memory - just write sequentially
//     int address = 0;
    
//     // Find the next available address (instead of always starting at 0000)
//     for (int i = 0; i < 65536; i++) {
//         String addr = String.format("%04X", i);
//         if (ra.get(addr).equals("00")) {
//             address = i;
//             break;
//         }
//     }
    
//     // Split data into 2-character chunks and store sequentially
//     for (int i = 0; i < data.length(); i += 2) {
//         String chunk;
//         if (i + 2 <= data.length()) {
//             chunk = data.substring(i, i + 2);
//         } else {
//             chunk = data.substring(i);
//         }
        
//         String addr = String.format("%04X", address);
//         ra.put(addr, chunk);
//         address++;
        
//         if (address >= 65536) break;
//     }
// }

    

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
    
//     private Map<String, String> memory; // Stockage du programme
//     private int compteurLigne = 0;
    
//     public ROM() {
//         memory = new LinkedHashMap<>();
//         // Initialiser 65536 cases (0000 à FFFF)
//         for (int i = 0; i < 65536; i++) {
//             String addr = String.format("%04X", i); // 4-digit hex now
//             memory.put(addr, "00");
//         }
//         System.out.println("[ROM] Initialisée - 65536 octets disponibles (0000-FFFF)\n");
//     }
    
//     // CHARGER depuis des lignes
//     public void loadFromLines(ArrayList<ArrayList<String>> lines) {
//         System.out.println("[ROM] Chargement depuis lignes assembleur...\n");
        
//         int address = 0;
//         for (ArrayList<String> line : lines) {
//             if (line.size() > 0) {
//                 String instruction = line.get(0);
//                 String operand = line.size() > 1 ? line.get(1) : "";
                
//                 System.out.println("  Ligne: " + instruction + " " + operand);
                
//                 // Charger dans la ROM
//                 String addr = String.format("%04X", address); // 4-digit hex
//                 memory.put(addr, instruction);
//                 address++;
                
//                 // Si y a un opérande, le mettre à l'adresse suivante
//                 if (!operand.isEmpty()) {
//                     addr = String.format("%04X", address);
//                     memory.put(addr, operand.replaceAll("[#<>$]", ""));
//                     address++;
//                 }
//             }
//         }
//         System.out.println("\n[ROM] Chargement terminé! Dernière adresse utilisée: " + String.format("%04X", address-1) + "\n");
//     }
    
//     // CHARGER manuellement une instruction
//     public void load(String address, String opcode) {
//         String formattedAddr = formatAddress(address);
//         if (memory.containsKey(formattedAddr)) {
//             memory.put(formattedAddr, opcode.toUpperCase());
//             System.out.println("[ROM LOAD] @" + formattedAddr + " <- " + opcode);
//         } else {
//             System.out.println("[ROM ERROR] Adresse invalide: " + address);
//         }
//     }
    
//     // LIRE (FETCH) une instruction
//     public String fetch(String address) {
//         String formattedAddr = formatAddress(address);
//         String opcode = memory.get(formattedAddr);
//         if (opcode != null) {
//             System.out.println("[ROM FETCH] @" + formattedAddr + " -> " + opcode);
//             return opcode;
//         }
//         return "00";
//     }
    
//     // Formater l'adresse en 4 digits hexa
//     private String formatAddress(String address) {
//         // Si l'adresse est trop courte, ajouter des zéros devant
//         if (address.length() < 4) {
//             return String.format("%04X", Integer.parseInt(address, 16));
//         }
//         return address.toUpperCase();
//     }
    
//     // AFFICHER le programme (seulement les cases utilisées)
//     public void display() {
//         System.out.println("\n╔══════════════════════╗");
//         System.out.println("║    PROGRAMME ROM     ║");
//         System.out.println("╠══════════════════════╣");
        
//         boolean hasCode = false;
//         for (String addr : memory.keySet()) {
//             String val = memory.get(addr);
//             if (!val.equals("00")) {
//                 System.out.println("║  " + addr + " : " + val + "             ║");
//                 hasCode = true;
//             }
//         }
        
//         if (!hasCode) {
//             System.out.println("║     (vide)            ║");
//         }
        
//         System.out.println("╚══════════════════════╝\n");
//     }
    
//     // Afficher une plage spécifique d'adresses
//     public void displayRange(String startAddr, String endAddr) {
//         int start = Integer.parseInt(startAddr, 16);
//         int end = Integer.parseInt(endAddr, 16);
        
//         System.out.println("\n╔══════════════════════╗");
//         System.out.println("║  ROM " + startAddr + "-" + endAddr + "         ║");
//         System.out.println("╠══════════════════════╣");
        
//         for (int i = start; i <= end; i++) {
//             String addr = String.format("%04X", i);
//             String val = memory.get(addr);
//             System.out.println("║  " + addr + " : " + val + "             ║");
//         }
        
//         System.out.println("╚══════════════════════╝\n");
//     }
// }