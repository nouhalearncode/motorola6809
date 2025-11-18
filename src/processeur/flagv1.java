package processeur;



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
}
    
