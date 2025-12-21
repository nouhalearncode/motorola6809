// // package interfaces;

// // import java.awt.Color;
// // import javax.swing.ImageIcon;
// // import javax.swing.JButton;
// // import javax.swing.JFrame;

// // public class interfaceone {
// //     public static void main(String[] args) {
// //   // JFrame = a GUI window to add components to
  
// //   JFrame frame = new JFrame(); 
// //   frame.setTitle("motorola 1st page"); 
// //   frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
// //   frame.setResizable(false); 
// //   frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
// //   frame.setUndecorated(false); 
  
// //   frame.setVisible(true);
   
// //   String logoPath = "C:\\Users\\pc\\OneDrive\\Desktop\\motorola\\motorola6809\\src\\interfaces\\logoprincipale.png";
// //         ImageIcon logo = new ImageIcon(logoPath);
// //         frame.setIconImage(logo.getImage());
 
// //   frame.getContentPane().setBackground(new Color(123,50,250)); 

  
  
// //  }
// // } 
// // package interfaces;

// // import java.awt.*;
// // import javax.swing.*;
// // import java.awt.event.*;

// // public class interfaceone {
// //     public static void main(String[] args) {
// //         // JFrame = a GUI window to add components to
// //         JFrame frame = new JFrame(); 
// //         frame.setTitle("motorola 1st page"); 
// //         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
// //         frame.setResizable(true); 
// //         frame.setSize(1200, 95); // Fixed size instead of maximized
// //         frame.setLayout(new BorderLayout());
        
// //         // Set logo
// //         String logoPath = "logoprincipale.png"; // Use relative path
// //         ImageIcon logo = new ImageIcon(logoPath);
// //         frame.setIconImage(logo.getImage());
        
      
        
// //         // 2. CREATE TOOLBAR WITH BUTTONS (under menu bar)
// //         JToolBar toolBar = createToolBar();
// //         frame.add(toolBar, BorderLayout.NORTH);
        
// //         // 3. MAIN CONTENT AREA
// //         JPanel mainPanel = new JPanel();
       
// //         mainPanel.setLayout(new BorderLayout());
        
        
        
// //         frame.add(mainPanel, BorderLayout.CENTER);
// //         frame.setVisible(true);
// //     }
    
    
    
// //     private static JToolBar createToolBar() {
// //         JToolBar toolBar = new JToolBar();
// //         toolBar.setFloatable(false); // Can't drag it away
// //         toolBar.setBackground(new Color(230, 230, 230));
// //         toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        
// //         // Create buttons with icons (using text for now, you can add images later)
// //         String[] buttonData = {
// //             "New", "Run", "Step", "Back", "RAM", "ROM", "Save"
// //         };
        
// //         for (String text : buttonData) {
// //             JButton button = new JButton(text);
// //             button.setFocusable(false);
// //             button.setMargin(new Insets(5, 10, 5, 10));
            
// //             // Add functionality
// //             if (text.equals("Step")) {
// //                 button.addActionListener(e -> {
// //                     System.out.println("Step by step execution");
// //                     // Connect to: simulator.stepExecution();
// //                 });
// //             } else if (text.equals("RAM")) {
// //                 button.addActionListener(e -> {
// //                     System.out.println("Opening RAM viewer");
// //                     // Call: showRAMWindow();
// //                 });
// //             } else if (text.equals("ROM")) {
// //                 button.addActionListener(e -> {
// //                     System.out.println("Opening ROM viewer");
// //                     // Call: showROMWindow();
// //                 });
// //             } else if (text.equals("Save")) {
// //                 button.addActionListener(e -> {
// //                     System.out.println("Saving state");
// //                     // Call: simulator.saveState();
// //                 });
// //             }
            
// //             toolBar.add(button);
// //             toolBar.addSeparator(); // Add spacing between buttons
// //         }
        
// //         // Add a separator and some extra space
// //         toolBar.add(Box.createHorizontalGlue());
        
// //         // Status indicator
// //         JLabel statusLabel = new JLabel(" Ready");
// //         statusLabel.setForeground(Color.DARK_GRAY);
// //         toolBar.add(statusLabel);
        
// //         return toolBar;  // THIS LINE WAS MISSING!
// //     }  // THIS BRACE WAS MISSING!
// // }

// package interfaces;

// import java.awt.*;
// import javax.swing.*;
// import java.awt.event.*;

// public class interfaceone {
//     public static void main(String[] args) {
//         // JFrame = a GUI window to add components to
//         JFrame frame = new JFrame(); 
//         frame.setTitle("motorola 1st page"); 
//         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
//         frame.setResizable(true); 
//         frame.setSize(1200, 95); // Fixed size instead of maximized
//         frame.setLayout(new BorderLayout());
        
//         // Set logo
//         String logoPath = "logoprincipale.png"; // Use relative path
//         ImageIcon logo = new ImageIcon(logoPath);
//         frame.setIconImage(logo.getImage());
        
//         // 2. CREATE TOOLBAR WITH BUTTONS (under menu bar)
//         JToolBar toolBar = createToolBar();
//         frame.add(toolBar, BorderLayout.NORTH);
        
//         // 3. MAIN CONTENT AREA
//         JPanel mainPanel = new JPanel();
//         mainPanel.setLayout(new BorderLayout());
        
//         frame.add(mainPanel, BorderLayout.CENTER);
//         frame.setVisible(true);
//     }
    
//     private static JToolBar createToolBar() {
//         JToolBar toolBar = new JToolBar();
//         toolBar.setFloatable(false); // Can't drag it away
//         toolBar.setBackground(new Color(230, 230, 230));
//         toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        
//         // Create buttons with icons (using text for now, you can add images later)
//         String[] buttonData = {
//             "New", "Run", "Step", "Back", "RAM", "ROM", "Save"
//         };
        
//         for (String text : buttonData) {
//             JButton button = new JButton(text);
//             button.setFocusable(false);
//             button.setMargin(new Insets(5, 10, 5, 10));
            
//             // Add functionality
//             if (text.equals("Step")) {
//                 button.addActionListener(e -> {
//                     System.out.println("Step by step execution");
//                     // Connect to: simulator.stepExecution();
//                 });
//             } else if (text.equals("RAM")) {
//                 button.addActionListener(e -> {
//                     System.out.println("Opening RAM viewer");
//                     // Call: showRAMWindow();
//                 });
//             } else if (text.equals("ROM")) {
//                 button.addActionListener(e -> {
//                     System.out.println("Opening ROM viewer");
//                     // Call: showROMWindow();
//                 });
//             } else if (text.equals("Save")) {
//                 button.addActionListener(e -> {
//                     System.out.println("Saving state");
//                     // Call: simulator.saveState();
//                 });
//             } else if (text.equals("New")) {
//                 // NEW: When "New" button is clicked, open text editor
//                 button.addActionListener(e -> {
//                     System.out.println("Opening text editor...");
//                     openTextEditor();
//                 });
//             }
            
//             toolBar.add(button);
//             toolBar.addSeparator(); // Add spacing between buttons
//         }
        
//         // Add a separator and some extra space
//         toolBar.add(Box.createHorizontalGlue());
        
       
//         return toolBar;
//     }
    
//     // NEW METHOD: Creates and shows the text editor window
//     private static void openTextEditor() {
//         // Create a new JFrame for the text editor
//         JFrame editorFrame = new JFrame("Assembly Code Editor");
        
//         // Set up the editor window
//         editorFrame.setSize(500, 500);
//         editorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Just close this window, not the whole app
//         editorFrame.setLocationRelativeTo(null); // Center on screen
        
//         // Create main panel with BorderLayout
//         JPanel mainPanel = new JPanel(new BorderLayout());
        
//         // 1. TOP: Toolbar with file options
//         JPanel topPanel = createEditorToolbar(editorFrame);
//         mainPanel.add(topPanel, BorderLayout.NORTH);
        
//         // 2. CENTER: Text area for writing assembly code
//         JTextArea textArea = createTextArea();
//         JScrollPane scrollPane = new JScrollPane(textArea);
//         scrollPane.setBorder(BorderFactory.createTitledBorder("Write your Motorola 6809 Assembly Code:"));
//         mainPanel.add(scrollPane, BorderLayout.CENTER);
        
//         // 3. BOTTOM: Status bar and buttons
//         JPanel bottomPanel = createEditorBottomPanel(textArea);
//         mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
//         // Add everything to the frame
//         editorFrame.add(mainPanel);
        
//         // Make the editor window visible
//         editorFrame.setVisible(true);
//     }
    
//     private static JPanel createEditorToolbar(JFrame parentFrame) {
//         JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
//         toolbar.setBackground(new Color(240, 240, 240));
//         toolbar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
//         // File operations buttons
//         JButton newBtn = new JButton("New File");
//         JButton openBtn = new JButton("Open...");
//         JButton saveBtn = new JButton("Save");
//         JButton saveAsBtn = new JButton("Save As...");
//         JButton closeBtn = new JButton("Close");
        
//         // Add actions
//         closeBtn.addActionListener(e -> parentFrame.dispose()); // Close window
        
//         // Add buttons to toolbar
//         toolbar.add(newBtn);
//         toolbar.add(openBtn);
//         toolbar.add(saveBtn);
//         toolbar.add(saveAsBtn);
//         toolbar.add(Box.createHorizontalStrut(20)); // Spacer
//         toolbar.add(closeBtn);
        
//         return toolbar;
//     }
    
//     private static JTextArea createTextArea() {
//         JTextArea textArea = new JTextArea();
//         textArea.setFont(new Font("Monospaced", Font.PLAIN, 14)); // Monospaced for code
//         textArea.setTabSize(4); // Set tab to 4 spaces
        
//         // Add some sample assembly code
//         textArea.setText(
//             "; Motorola 6809 Assembly Code\n" +
//             "; Write your program here\n\n" +
//             "        ORG $1000           ; Start at memory address $1000\n\n" +
//             "START   LDAA #$FF           ; Load accumulator A with $FF\n" +
//             "        STAA $2000          ; Store A at address $2000\n" +
//             "        LDX #$3000          ; Load index register X with $3000\n" +
//             "LOOP    DECA                ; Decrement A\n" +
//             "        BNE LOOP            ; Branch if not zero\n" +
//             "        SWI                 ; Software interrupt\n" +
//             "        END START           ; End of program\n"
//         );
        
//         return textArea;
//     }
    
//     private static JPanel createEditorBottomPanel(JTextArea textArea) {
//         JPanel bottomPanel = new JPanel(new BorderLayout());
//         bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
//         bottomPanel.setBackground(new Color(245, 245, 245));
        
//         // Left: Line/Column counter
//         JLabel positionLabel = new JLabel("Line 1, Column 1");
//         positionLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        
//         // Update position when cursor moves
//         textArea.addCaretListener(e -> {
//             int pos = textArea.getCaretPosition();
//             try {
//                 int line = textArea.getLineOfOffset(pos);
//                 int column = pos - textArea.getLineStartOffset(line);
//                 positionLabel.setText("Line " + (line + 1) + ", Column " + (column + 1));
//             } catch (Exception ex) {
//                 positionLabel.setText("Position: Unknown");
//             }
//         });
        
//         // Right: Action buttons
//         JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
//         JButton assembleBtn = new JButton("Assemble");
//         JButton loadBtn = new JButton("Load to Simulator");
//         JButton clearBtn = new JButton("Clear");
        
//         // Add button actions
//         assembleBtn.addActionListener(e -> {
//             String code = textArea.getText();
//             System.out.println("Assembling code...");
//             // Here you would connect to your assembler
//             JOptionPane.showMessageDialog(null, "Code assembled (placeholder)", 
//                 "Assembly Complete", JOptionPane.INFORMATION_MESSAGE);
//         });
        
//         loadBtn.addActionListener(e -> {
//             System.out.println("Loading code to simulator...");
//             // Here you would connect to your Motorola 6809 simulator
//             JOptionPane.showMessageDialog(null, "Code loaded to simulator (placeholder)", 
//                 "Load Complete", JOptionPane.INFORMATION_MESSAGE);
//         });
        
//         clearBtn.addActionListener(e -> {
//             int choice = JOptionPane.showConfirmDialog(null, 
//                 "Clear all text?", "Confirm Clear", 
//                 JOptionPane.YES_NO_OPTION);
//             if (choice == JOptionPane.YES_OPTION) {
//                 textArea.setText("");
//             }
//         });
        
//         buttonPanel.add(assembleBtn);
//         buttonPanel.add(loadBtn);
//         buttonPanel.add(clearBtn);
        
//         bottomPanel.add(positionLabel, BorderLayout.WEST);
//         bottomPanel.add(buttonPanel, BorderLayout.EAST);
        
//         return bottomPanel;
//     }
// }

package interfaces;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class interfaceone {
    public static void main(String[] args) {
        // JFrame = a GUI window to add components to
        JFrame frame = new JFrame(); 
        frame.setTitle("motorola 1st page"); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        frame.setResizable(true); 
        frame.setSize(1200, 95); // Fixed size instead of maximized
        frame.setLayout(new BorderLayout());
        
        // Set logo
        String logoPath = "logoprincipale.png"; // Use relative path
        ImageIcon logo = new ImageIcon(logoPath);
        frame.setIconImage(logo.getImage());
        
        // CREATE TOOLBAR WITH BUTTONS
        JToolBar toolBar = createToolBar();
        frame.add(toolBar, BorderLayout.NORTH);
        
        // MAIN CONTENT AREA
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }
    
    private static JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(new Color(230, 230, 230));
        toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        
        String[] buttonData = {"New", "Run", "Step", "Back", "RAM", "ROM", "Save"};
        
        for (String text : buttonData) {
            JButton button = new JButton(text);
            button.setFocusable(false);
            button.setMargin(new Insets(5, 10, 5, 10));
            
            // Add functionality
            if (text.equals("Step")) {
                button.addActionListener(e -> {
                    System.out.println("Step by step execution");
                    // Connect to: simulator.stepExecution();
                });
            } else if (text.equals("RAM")) {
                button.addActionListener(e -> {
                    System.out.println("Opening empty RAM frame");
                    openEmptyFrame("RAM", 400, 300);
                });
            } else if (text.equals("ROM")) {
                button.addActionListener(e -> {
                    System.out.println("Opening empty ROM frame");
                    openEmptyFrame("ROM", 400, 300);
                });
            } else if (text.equals("Save")) {
                button.addActionListener(e -> {
                    System.out.println("Saving state");
                    // Call: simulator.saveState();
                });
            } else if (text.equals("New")) {
                button.addActionListener(e -> {
                    System.out.println("Opening text editor...");
                    openTextEditor();
                });
            }
            
            toolBar.add(button);
            toolBar.addSeparator();
        }
        
        toolBar.add(Box.createHorizontalGlue());
        return toolBar;
    }
    
    // SIMPLE EMPTY FRAME METHOD
    private static void openEmptyFrame(String title, int width, int height) {
        JFrame emptyFrame = new JFrame(title);
        emptyFrame.setSize(width, height);
        emptyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        emptyFrame.setLocationRelativeTo(null); // Center on screen
        
        // Just an empty panel
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        
        // Optional: Add a label saying what this is
        JLabel label = new JLabel(title + " Frame - Add content here", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        panel.add(label);
        
        emptyFrame.add(panel);
        emptyFrame.setVisible(true);
    }
    
    // TEXT EDITOR (unchanged)
    private static void openTextEditor() {
        JFrame editorFrame = new JFrame("Assembly Code Editor");
        editorFrame.setSize(500, 500);
        editorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        editorFrame.setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        JTextArea textArea = createTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        editorFrame.add(mainPanel);
        editorFrame.setVisible(true);
    }
    
    private static JTextArea createTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setTabSize(4);
        
        textArea.setText(
            "; Motorola 6809 Assembly Code\n" +
            "; Write your program here\n\n" +
            "        ORG $1000\n" +
            "START   LDAA #$FF\n" +
            "        STAA $2000\n" +
            "        LDX #$3000\n" +
            "LOOP    DECA\n" +
            "        BNE LOOP\n" +
            "        SWI\n" +
            "        END START\n"
        );
        
        return textArea;
    }
}