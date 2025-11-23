package processeur;



public class flagv1 {

    // Binary storage fields
    private static String binaryOperand = "";  // Store binary representation
    private static int binaryLength = 0;       // Store binary length
    private String[] lastInstructionFlags = {};

    // NEW METHOD: Convert hex to binary and store it
    public static void convertAndStoreBinary(String hexValue) {
        binaryOperand = hexToBinary(hexValue);
        binaryLength = binaryOperand.length();

    }

    // NEW METHOD: Convert hex string to binary string
    private static String hexToBinary(String hexValue) {
        try {
            // Convert hex to integer
            int decimalValue = Integer.parseInt(hexValue, 16);
            // Convert to binary string
            return Integer.toBinaryString(decimalValue);
        } catch (NumberFormatException e) {
            System.out.println("Error converting hex to binary: " + hexValue);
            return "00000000";
        }
    }

    // NEW GETTERS: Access binary data
    public static String getBinaryOperand() {
        return binaryOperand;
    }
    
    public static int getBinaryLength() {
        return binaryLength;
    }
    
    public static String getPaddedBinary() {
        if (binaryOperand.length() < 8) {
            int zerosNeeded = 8 - binaryOperand.length();
            String zeros = "0".repeat(zerosNeeded);
            return zeros + binaryOperand;
        }
        return binaryOperand;
    }

    // NEW METHOD: Get binary length as integer
    public static int getBinaryLength(String binaryString) {
        return binaryString.length();
    }

    
    //flag z pour dire si un resultat est null
	public static int flag_Z(int n3) {
        if (n3 == 0) {
            return 1;
        } else {
            return 0;
        }
    }
	// flag N pour dire si le nombre est signe , negatif
    public static int flag_S(char eighthDigit) {
    
              
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
    // //flag p pour dire si un flag le nombre des 1 dans notre resultat est pair ou impaire
    // public static int flag_P(int r) {
    //     if (r%2 == 0) {
    //         return 1;
    //     } else {
    //         return 0;
    //     }
    // }
    //flag V pour dire que on a une retenue donc plus que 8 bits
    public static int flag_O(int l) {
        if (l > 8) {
            return 1;
        } else {
            return 0;
        }
}
}
    
