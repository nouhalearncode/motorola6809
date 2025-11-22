// package interfaces;
// import javax.swing.*;    // Importation des bibliothèques Swing pour l'interface graphique
// import java.awt.*;    // Importation des outils graphiques (couleurs, polices, layouts)
// // Importation des structures de données
// import java.util.LinkedHashMap;
// import java.util.Map;

// public class test {

//     // Map qui stocke la mémoire (adresse → valeur)
//     private Map<String, String> ramMap;

//     // Panel principal qui affichera la RAM dans l'interface
//     private JPanel ramPanel;

//     // Map qui relie chaque adresse aux deux labels (adresse + valeur)
//     private Map<String, JLabel[]> elements;

//     // Variable pour garder en mémoire l'adresse actuellement surlignée
//     private String currentKey = null;

//     // CONSTRUCTEUR : création de la RAM
    
//     public test() {

//         // LinkedHashMap pour garder l'ordre d'insertion (00 → FF)
//         ramMap = new LinkedHashMap<>();

//         // Stockera les labels graphiques
//         elements = new LinkedHashMap<>();

//         //Pour Créer 256 cases mémoire (00 à FF)
//         for (int i = 0; i < 256; i++) {

//             // Convertir l'indice en hexadécimal sur 2 caractères
//             String addr = String.format("%02X", i);

//             // Initialiser chaque cellule mémoire à "00"
//             ramMap.put(addr, "00");
//         }
//     }
//     // MÉTHODE spawn() : pour construire le panneau RAM
    
//     public JPanel spawn() {

//         // Création du panneau principal
//         ramPanel = new JPanel();
//         ramPanel.setLayout(new BorderLayout());
//         ramPanel.setBackground(Color.BLACK);

//         // Création du titre "RAM"
//         JLabel title = new JLabel("RAM");
//         title.setForeground(Color.WHITE);                // Couleur blanche
//         title.setHorizontalAlignment(SwingConstants.CENTER); // Centré
//         title.setFont(new Font("Consolas", Font.BOLD, 16)); // Police
//         ramPanel.add(title, BorderLayout.NORTH);         // Placé en haut

//         // Panel contenant les lignes mémoire
//         JPanel listPanel = new JPanel();
//         listPanel.setLayout(new GridLayout(ramMap.size(), 1)); // 256 lignes
//         listPanel.setBackground(Color.BLACK);

//         // Boucle : une ligne par adresse
//         for (String key : ramMap.keySet()) {

//             // Chaque ligne = adresse + valeur
//             JPanel line = new JPanel(new GridLayout(1, 2));
//             line.setBackground(Color.BLACK);

//             // Label pour l'adresse
//             JLabel k = new JLabel(key);
//             k.setForeground(Color.GREEN);

//             // Label pour la valeur
//             JLabel v = new JLabel(ramMap.get(key));
//             v.setForeground(Color.CYAN);

//             // Ajouter les labels à la ligne
//             line.add(k);
//             line.add(v);

//             // Ajouter la ligne au panel
//             listPanel.add(line);

//             // Sauvegarder les labels pour pouvoir les modifier plus tard
//             elements.put(key, new JLabel[]{k, v});
//         }

//         // Rendre la mémoire défilante avec un scroll
//         JScrollPane scroll = new JScrollPane(listPanel);
//         ramPanel.add(scroll, BorderLayout.CENTER);

//         // Retourner le panneau à afficher dans la fenêtre principale
//         return ramPanel;
//     }

//     // MODIFIER UNE VALEUR EN RAM (écriture mémoire)
    
//     public void setRAM(String address, String value) {

//         // Sécurité : vérifier que l'adresse existe
//         if (!ramMap.containsKey(address)) return;

//         // Modifier la valeur dans la Map interne (mémoire logique)
//         ramMap.put(address, value);

//         // Modifier la valeur affichée (interface graphique)
//         elements.get(address)[1].setText(value);
//     }

//     //SURBRILLANCE
    
//     public void setCurrent(String key) {

//         // Réinitialiser les couleurs de TOUTES les lignes
//         for (String addr : elements.keySet()) {
//             elements.get(addr)[0].setForeground(Color.GREEN);
//             elements.get(addr)[1].setForeground(Color.CYAN);
//         }

//         // Mettre en blanc l'adresse ciblée
//         if (elements.containsKey(key)) {
//             elements.get(key)[0].setForeground(Color.WHITE);
//             elements.get(key)[1].setForeground(Color.WHITE);
//         }

//         // Retenir l'adresse active
//         currentKey = key;
//     }
// }