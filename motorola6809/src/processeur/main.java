package processeur;
import java.util.ArrayList;
import java.util.Scanner;

public class main{
public static void main(String[] args) {
    
    
    ROM ram = new ROM();
    ram rom = new ram();
    
    // // Print entire memory (be careful - it's 65,536 entries!)
    // System.out.println("=== FULL MEMORY DUMP ===");
    // System.out.println(ram.memory);
    // // Warning: This will print 65,536 lines!
    
    rom.displayRange("0000", "000F");
    ram.displayRange("0000", "000F");

    

    //0000 a 03ff
   
    for (int i = 0; i < 20; i++) {  // 0000 to 03FF = 1024 bytes
    String addr = String.format("%04X", i);
    System.out.println(addr + ": " + ram.getMemory().get(addr));
}


///////////////////////////////////////////////////////////////////////////////

        ArrayList<ArrayList<String>> myList = new ArrayList<>();
        lecture lec = new lecture(myList, rom, ram); // This will read input until "END"
        
        

        // Show final states
        
        
        
        
        rom.displayRange("0000", "0010");


// Show memory after loading the assembly code
        System.out.println("\n=== MEMORY AFTER LOADING ASSEMBLY CODE (0000 to 0010) ===");
        for (int i = 0; i < 20; i++) {  // Show first 20 addresses to see your code
            String addr = String.format("%04X", i);
            System.out.println(addr + ": " + ram.getMemory().get(addr));
        }

        

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        mode modeDetector = new mode();
    
        for (int i = 0; i < myList.size(); i++) {
            ArrayList<String> currentLine = myList.get(i);
            
            // Determine and display addressing mode
            if (currentLine.size() > 1) {
                String secondWord = currentLine.get(1);
                String addressingMode = modeDetector.determineMode(secondWord);
                System.out.println(" Mode: " + addressingMode);
            }
        }

        Scanner sc = new Scanner(System.in);
        // on lit les nbrs et on les rends en binaire et String
        System.out.println("Number Digit 1 Display");
        System.out.print("Enter a number: ");
        int n1 = sc.nextInt();
        String b1 = Integer.toBinaryString(n1);
        
        System.out.println("Number Digit 2 Display");
        System.out.print("Enter a number: ");
        int n2 = sc.nextInt();
        String b2 = Integer.toBinaryString(n2);
        
        System.out.println("\nYour number: " + n1);
        System.out.println("\nYour number en binaire: " + b1);
        System.out.println("\nYour number: " + n2);
        System.out.println("\nYour number en binaire: " + b2);
        
        //on fait la somme
        int n3 = n1+n2;
        String b3 = Integer.toBinaryString(n3);
        
       //affichage des numero 1 par 1
        System.out.println("Individual digits n1:");
        for (int i = 0; i < b1.length(); i++) {
            int digit = Character.getNumericValue(b1.charAt(i));
            System.out.println("Digit " + (i + 1) + ": " + digit);
        }
     
        
        System.out.println("Individual digits 2:"); 
        for (int i = 0; i < b2.length(); i++) {
            int digit = Character.getNumericValue(b2.charAt(i));
            System.out.println("Digit " + (i + 1) + ": " + digit);
        }
     
        
        System.out.println("Individual digits 3:");
        for (int i = 0; i < b3.length(); i++) {
            int digit = Character.getNumericValue(b3.charAt(i));
            System.out.println("Digit " + (i + 1) + ": " + digit);
        }
        
        //compteur de 1 dans notre resultat
        int count1 = 0;
        for (int i = 0; i < b3.length(); i++) {
            if (b3.charAt(i) == '1') {
                count1++;
            }
            
        }
        
        int r=count1;
        
        //l recoit le langueur de b3 le resultat, de meme pour c3
        //c3 va etre modifier si elle est <8, on ajoute des 0 et c3 augmente , et l pour la flag o
        //et on prend le 8eme bit, comptant du droite a gauche
        int l= b3.length();
        int c3= b3.length();
        if (c3 < 8) {
            int zerosManquants = 8 - c3;
            String zeros = "0".repeat(zerosManquants);
            b3 = zeros + b3;
            c3= b3.length();
        }
        char eighthDigit = b3.charAt(c3-8);  
        
        int c2= b2.length();
        if (c2 < 8) {
            int zerosManquants = 8 - c2;
            String zeros = "0".repeat(zerosManquants);
            b2 = zeros + b2;
            c2= b2.length();
        }
        char eighthDigit2 = b2.charAt(c2-8);  
        
        int c1= b1.length();
        if (c1 < 8) {
            int zerosManquants = 8 - c1;
            String zeros = "0".repeat(zerosManquants);
            b1 = zeros + b1;
            c1 = b1.length();
        }
        char eighthDigit1 = b1.charAt(c1-8);  
        
        
        System.out.println(eighthDigit);
        //lafichage des flags
        System.out.println("Flag_Z  " +flagv1.flag_Z(n3));
        System.out.println("Flag_S  " +flagv1.flag_S(eighthDigit));
        System.out.println("Flag_C  " +flagv1.flag_C(eighthDigit1,eighthDigit2,eighthDigit));
        System.out.println("Flag_P  " +flagv1.flag_P(r));
        System.out.println("Flag_O  " +flagv1.flag_O(l));
    


}
}

