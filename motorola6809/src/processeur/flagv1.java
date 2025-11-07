package processeur;
import java.util.Scanner;

public class flagv1 {
    //flag z pour dire si un resultat est null
	public static int flag_Z(int n3) {
        if (n3 == 0) {
            return 1;
        } else {
            return 0;
        }
    }
	// flag s pour dire si le nombre est signe , negatif
    public static int flag_S(int c3, char eighthDigit) {
    
              
            if (eighthDigit=='1') {
            		return 1;
            }
            else {
            		return 0;
            }
            } 
    
    //flag c pour dire si il ya un debordement
    public static int flag_C(int fd1,int fd2,int fd3) {
    		if ((fd1==1 && fd2==1 && fd3==0)||(fd1==0 && fd2==0 && fd3==1)) {
            return 1;
            }	
         else {
            return 0;
        }	
    	} 
    //flag p pour dire si un flag le nombre des 1 dans notre resultat est pair ou impaire
    public static int flag_P(int r) {
        if (r%2 == 0) {
            return 1;
        } else {
            return 0;
        }
    }
    //flag z pour dire que on a une retenue donc plus que 8 bits
    public static int flag_O(int l) {
        if (l > 8) {
            return 1;
        } else {
            return 0;
        }
}
    
    public static void main(String[] args) {
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
        
        
        //lafichage des flags
        System.out.println("Flag_Z  " +flag_Z(n3));
        System.out.println("Flag_S  " +flag_S(c3,eighthDigit));
        System.out.println("Flag_C  " +flag_C(eighthDigit1,eighthDigit2,eighthDigit));
        System.out.println("Flag_P  " +flag_P(r));
        System.out.println("Flag_O  " +flag_O(l));
    }
}