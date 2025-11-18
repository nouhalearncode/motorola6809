package interfaces;

import java.awt.Color;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;

public class interfaceone {
    public static void main(String[] args) {
  // JFrame = a GUI window to add components to
  
  JFrame frame = new JFrame(); 
  frame.setTitle("motorola 1st page"); 
  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
  frame.setResizable(false); 
  frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
  frame.setUndecorated(false); 
  
  frame.setVisible(true);
   
  String logoPath = "C:\\Users\\pc\\OneDrive\\Desktop\\motorola\\motorola6809\\src\\interfaces\\logoprincipale.png";
        ImageIcon logo = new ImageIcon(logoPath);
        frame.setIconImage(logo.getImage());
 
  frame.getContentPane().setBackground(new Color(123,50,250)); 

  
  
 }
} 
