package processeur;

public class mode {
    
    private String immediat;
    private String direct;
    private String indexe;
    private String etendu;
    private String inherent;

    // Constructor to initialize modes
    public mode() {
        this.immediat = "Immediate";
        this.direct = "Direct";
        this.indexe = "Indexed";
        this.etendu = "Extended";
        this.inherent = "Inherent";
    }
    public String determineMode(String secondWord) {
    if (secondWord.charAt(0) == '#'){

        // immediat
        return immediat;

    }

    if (secondWord.charAt(0) == '>'){

        // etendu
        return etendu;
        
    }

    if (secondWord.charAt(0) == '<'){

        // direct
        return direct;
    }

    if (secondWord.charAt(0) == '$'){

        // inherent
        return inherent;

    }
    return "Unknown mode";
}   
    // Getters for the modes (optional)
    public String getImmediat() { return immediat; }
    public String getDirect() { return direct; }
    public String getIndexe() { return indexe; }
    public String getEtendu() { return etendu; }
    public String getInherent() { return inherent; }
}










        
        

