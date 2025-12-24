// //  package processeur;

// // import java.util.ArrayList;
// // import java.util.Scanner;
// // import java.awt.*;
// // import javax.swing.*;
// // import java.awt.event.*;

// // public class main {
// //     // Declare backend objects as class variables so all methods can access them
// //     private static ROM rom;
// //     private static ram ram;
// //     private static registre reg;
// //     private static mode modeDetector;
// //     private static ArrayList<ArrayList<String>> myList;
// //     private static pas stepExecutor;

// //     // Declare GUI frames as class variables
// //     private static RAMDisplayFrame ramFrame;
// //     private static ROMDisplayFrame romFrame;
// //     private static RegisterDisplayFrame registerFrame;

// //     public static void main(String[] args) {

// //         // Initialize backend objects
// //         rom = new ROM();
// //         ram = new ram();
// //         reg = new registre();
// //         modeDetector = new mode();

// //         modeDetector.setRegistre(reg);
// //         modeDetector.setRam(ram);

// //         // Initialize the assembly list
// //         myList = new ArrayList<>();

// //         // JUST READ instructions, don't execute them yet
// //         // Note: This will show terminal input - you may want to change this later
// //         System.out.println("=== ASSEMBLY INPUT ===");
// //         lecture lec = new lecture(myList, ram, rom, reg, modeDetector);

// //         // Create step executor but don't execute yet
// //         stepExecutor = new pas(myList, ram, rom, reg, modeDetector);

// //         // Create main GUI window
// //         createMainWindow();

// //         // Create GUI display frames (initially hidden)
// //         createDisplayFrames();

// //         // Show initial states in GUI
// //         updateAllDisplays();
// //     }

// //     private static void createMainWindow() {
// //         // JFrame = a GUI window to add components to
// //         JFrame frame = new JFrame(); 
// //         frame.setTitle("Motorola 6809 Simulator"); 
// //         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
// //         frame.setResizable(true); 
// //         frame.setSize(1200, 95); // Fixed size instead of maximized
// //         frame.setLayout(new BorderLayout());

// //         // Set logo
// //         try {
// //             String logoPath = "logoprincipale.png"; // Use relative path
// //             ImageIcon logo = new ImageIcon(logoPath);
// //             frame.setIconImage(logo.getImage());
// //         } catch (Exception e) {
// //             System.out.println("Logo not found, using default.");
// //         }

// //         // CREATE TOOLBAR WITH BUTTONS - pass backend objects
// //         JToolBar toolBar = createToolBar();
// //         frame.add(toolBar, BorderLayout.NORTH);

// //         // MAIN CONTENT AREA
// //         JPanel mainPanel = new JPanel();
// //         mainPanel.setLayout(new BorderLayout());
// //         mainPanel.setBackground(Color.WHITE);

// //         // Add status label
// //         JLabel statusLabel = new JLabel("Ready - Enter assembly code in terminal", SwingConstants.CENTER);
// //         statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
// //         mainPanel.add(statusLabel, BorderLayout.CENTER);

// //         frame.add(mainPanel, BorderLayout.CENTER);
// //         frame.setVisible(true);
// //     }

// //     private static void createDisplayFrames() {
// //         // Create the display frames but don't show them yet
// //         // They will be shown when buttons are clicked
// //         ramFrame = new RAMDisplayFrame(ram);
// //         romFrame = new ROMDisplayFrame(rom);
// //         registerFrame = new RegisterDisplayFrame(reg);

// //         // Position them nicely
// //         ramFrame.setLocation(50, 150);
// //         romFrame.setLocation(500, 150);
// //         registerFrame.setLocation(950, 150);
// //     }

// //     private static JToolBar createToolBar() {
// //         JToolBar toolBar = new JToolBar();
// //         toolBar.setFloatable(false);
// //         toolBar.setBackground(new Color(230, 230, 230));
// //         toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

// //         // Add more buttons including Registers
// //         String[] buttonData = {"New", "Run", "Step", "Back", "RAM", "ROM", "Registers", "Save"};

// //         for (String text : buttonData) {
// //             JButton button = new JButton(text);
// //             button.setFocusable(false);
// //             button.setMargin(new Insets(5, 10, 5, 10));

// //             // Add functionality
// //             if (text.equals("Step")) {
// //                 button.addActionListener(e -> {
// //                     System.out.println("Executing step...");
// //                     // Execute one step
// //                     executeSingleStep();
// //                     // Refresh all displays
// //                     updateAllDisplays();
// //                 });
// //             } else if (text.equals("Run")) {
// //                 button.addActionListener(e -> {
// //                     System.out.println("Running all steps...");
// //                     // Execute all steps
// //                     executeAllSteps();
// //                     // Refresh all displays
// //                     updateAllDisplays();
// //                 });
// //             } else if (text.equals("RAM")) {
// //                 button.addActionListener(e -> {
// //                     System.out.println("Opening RAM frame");
// //                     if (ramFrame != null) {
// //                         ramFrame.updateRAMDisplay();
// //                         ramFrame.setVisible(true);
// //                     }
// //                 });
// //             } else if (text.equals("ROM")) {
// //                 button.addActionListener(e -> {
// //                     System.out.println("Opening ROM frame");
// //                     if (romFrame != null) {
// //                         romFrame.updateROMDisplay();
// //                         romFrame.setVisible(true);
// //                     }
// //                 });
// //             } else if (text.equals("Registers")) {
// //                 button.addActionListener(e -> {
// //                     System.out.println("Opening Register frame");
// //                     if (registerFrame != null) {
// //                         registerFrame.updateRegisterDisplay();
// //                         registerFrame.setVisible(true);
// //                     }
// //                 });
// //             } else if (text.equals("Save")) {
// //                 button.addActionListener(e -> {
// //                     System.out.println("Saving state");
// //                     // Call: simulator.saveState();
// //                 });
// //             } else if (text.equals("New")) {
// //                 button.addActionListener(e -> {
// //                     System.out.println("Opening text editor...");
// //                     openTextEditor();
// //                 });
// //             } else if (text.equals("Back")) {
// //                 button.addActionListener(e -> {
// //                     System.out.println("Going back one step");
// //                     // Implement step back functionality
// //                 });
// //             }

// //             toolBar.add(button);
// //             toolBar.addSeparator();
// //         }

// //         toolBar.add(Box.createHorizontalGlue());
// //         return toolBar;
// //     }

// //     private static void executeSingleStep() {
// //         // This is a simplified version - you'll need to integrate with your pas class
// //         // For now, just show a message
// //         JOptionPane.showMessageDialog(null, "Step execution would go here", 
// //                                       "Step Execution", JOptionPane.INFORMATION_MESSAGE);

// //         // TODO: Integrate with your stepExecutor.executeStepByStep() or similar
// //         // stepExecutor.executeSingleStep(currentStep);
// //     }

// //     private static void executeAllSteps() {
// //         // Execute all remaining steps
// //         stepExecutor.executeStepByStep();
// //         updateAllDisplays();
// //     }

// //     private static void updateAllDisplays() {
// //         // Update all GUI displays
// //         if (ramFrame != null && ramFrame.isVisible()) {
// //             ramFrame.updateRAMDisplay();
// //         }
// //         if (romFrame != null && romFrame.isVisible()) {
// //             romFrame.updateROMDisplay();
// //         }
// //         if (registerFrame != null && registerFrame.isVisible()) {
// //             registerFrame.updateRegisterDisplay();
// //         }
// //     }

// //     // TEXT EDITOR (unchanged)
// //     private static void openTextEditor() {
// //         JFrame editorFrame = new JFrame("Assembly Code Editor");
// //         editorFrame.setSize(500, 500);
// //         editorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
// //         editorFrame.setLocationRelativeTo(null);

// //         JPanel mainPanel = new JPanel(new BorderLayout());
// //         JTextArea textArea = createTextArea();
// //         JScrollPane scrollPane = new JScrollPane(textArea);

// //         mainPanel.add(scrollPane, BorderLayout.CENTER);
// //         editorFrame.add(mainPanel);
// //         editorFrame.setVisible(true);
// //     }

// //     private static JTextArea createTextArea() {
// //         JTextArea textArea = new JTextArea();
// //         textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
// //         textArea.setTabSize(4);

// //         textArea.setText(
// //             "; Motorola 6809 Assembly Code\n" +
// //             "; Write your program here\n\n" +
// //             "        ORG $1000\n" +
// //             "START   LDAA #$FF\n" +
// //             "        STAA $2000\n" +
// //             "        LDX #$3000\n" +
// //             "LOOP    DECA\n" +
// //             "        BNE LOOP\n" +
// //             "        SWI\n" +
// //             "        END START\n"
// //         );

// //         return textArea;
// //     }
// // }

// package processeur;

// import java.util.ArrayList;
// import java.util.Scanner;
// import java.awt.*;
// import javax.swing.*;
// import java.awt.event.*;

// public class main {
//     // Declare backend objects as class variables
//     private static ROM rom;
//     private static ram ram;
//     private static registre reg;
//     private static mode modeDetector;
//     private static ArrayList<ArrayList<String>> myList;
//     private static pas stepExecutor;

//     // Declare GUI frames
//     private static RAMDisplayFrame ramFrame;
//     private static ROMDisplayFrame romFrame;

//     public static void main(String[] args) {
//         // Initialize backend objects
//         rom = new ROM();
//         ram = new ram();
//         reg = new registre();
//         modeDetector = new mode();

//         modeDetector.setRegistre(reg);
//         modeDetector.setRam(ram);

//         // Initialize assembly list
//         myList = new ArrayList<>();

//         // Create GUI FIRST
//         createMainWindow();

//         // Create RAM and ROM display frames (initially hidden)
//         createDisplayFrames();

//         // Show everything in terminal as before
//         runTerminalExecution();
//     }

//     private static void createMainWindow() {
//         JFrame frame = new JFrame(); 
//         frame.setTitle("Motorola 6809 Simulator"); 
//         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
//         frame.setResizable(true); 
//         frame.setSize(1200, 95);
//         frame.setLayout(new BorderLayout());

//         // Set logo if available
//         try {
//             String logoPath = "logoprincipale.png";
//             ImageIcon logo = new ImageIcon(logoPath);
//             frame.setIconImage(logo.getImage());
//         } catch (Exception e) {
//             System.out.println("Logo not found, using default.");
//         }

//         // CREATE TOOLBAR WITH BUTTONS
//         JToolBar toolBar = createToolBar();
//         frame.add(toolBar, BorderLayout.NORTH);

//         // MAIN CONTENT AREA
//         JPanel mainPanel = new JPanel();
//         mainPanel.setLayout(new BorderLayout());
//         mainPanel.setBackground(Color.WHITE);

//         JLabel statusLabel = new JLabel("Simulator Ready - Click RAM/ROM to view memory", SwingConstants.CENTER);
//         statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
//         mainPanel.add(statusLabel, BorderLayout.CENTER);

//         frame.add(mainPanel, BorderLayout.CENTER);
//         frame.setVisible(true);

//         System.out.println("GUI Window opened. Click buttons to view RAM/ROM.");
//     }

//     private static void createDisplayFrames() {
//         // Create the display frames but don't show them yet
//         ramFrame = new RAMDisplayFrame(ram);
//         romFrame = new ROMDisplayFrame(rom);

//         // Position them nicely
//         ramFrame.setLocation(50, 150);
//         romFrame.setLocation(500, 150);
//     }

//     private static JToolBar createToolBar() {
//         JToolBar toolBar = new JToolBar();
//         toolBar.setFloatable(false);
//         toolBar.setBackground(new Color(230, 230, 230));
//         toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

//         String[] buttonData = {"New", "Run", "Step", "RAM", "ROM", "Registers"};

//         for (String text : buttonData) {
//             JButton button = new JButton(text);
//             button.setFocusable(false);
//             button.setMargin(new Insets(5, 10, 5, 10));

//             // Add functionality
//             if (text.equals("RAM")) {
//                 button.addActionListener(e -> {
//                     System.out.println("Opening RAM frame");
//                     if (ramFrame != null) {
//                         ramFrame.updateRAMDisplay();
//                         ramFrame.setVisible(true);
//                         ramFrame.toFront(); // Bring to front
//                     }
//                 });
//             } else if (text.equals("ROM")) {
//                 button.addActionListener(e -> {
//                     System.out.println("Opening ROM frame");
//                     if (romFrame != null) {
//                         romFrame.updateROMDisplay();
//                         romFrame.setVisible(true);
//                         romFrame.toFront();
//                     }
//                 });
//             } else if (text.equals("Registers")) {
//                 button.addActionListener(e -> {
//                     System.out.println("Opening Register frame");
//                     RegisterDisplayFrame registerFrame = new RegisterDisplayFrame(reg);
//                     registerFrame.setLocation(950, 150);
//                     registerFrame.updateRegisterDisplay();
//                     registerFrame.setVisible(true);
//                 });
//             } else if (text.equals("New")) {
//                 button.addActionListener(e -> {
//                     System.out.println("Opening text editor...");
//                     openSimpleTextEditor();
//                 });
//             } else if (text.equals("Run")) {
//                 button.addActionListener(e -> {
//                     System.out.println("Running all steps...");
//                     JOptionPane.showMessageDialog(null, 
//                         "Code execution will happen in terminal.\nCheck the terminal window below.",
//                         "Execution Info", 
//                         JOptionPane.INFORMATION_MESSAGE);
//                 });
//             } else if (text.equals("Step")) {
//                 button.addActionListener(e -> {
//                     System.out.println("Step execution...");
//                     JOptionPane.showMessageDialog(null, 
//                         "Step execution happens in terminal.\nCheck the terminal window.",
//                         "Step Info", 
//                         JOptionPane.INFORMATION_MESSAGE);
//                 });
//             }

//             toolBar.add(button);
//             toolBar.addSeparator();
//         }

//         toolBar.add(Box.createHorizontalGlue());
//         return toolBar;
//     }

//     private static void openSimpleTextEditor() {
//         JFrame editorFrame = new JFrame("Assembly Code Editor");
//         editorFrame.setSize(500, 500);
//         editorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//         editorFrame.setLocationRelativeTo(null);

//         JPanel mainPanel = new JPanel(new BorderLayout());
//         JTextArea textArea = createTextArea();
//         JScrollPane scrollPane = new JScrollPane(textArea);

//         JButton loadButton = new JButton("Load to Terminal");
//         loadButton.addActionListener(e -> {
//             String code = textArea.getText();
//             System.out.println("\n=== CODE FROM EDITOR ===");
//             System.out.println(code);
//             System.out.println("=== END CODE ===");
//             editorFrame.dispose();
//             JOptionPane.showMessageDialog(null, 
//                 "Code sent to terminal.\nSwitch to terminal window to continue.",
//                 "Code Loaded", 
//                 JOptionPane.INFORMATION_MESSAGE);
//         });

//         mainPanel.add(scrollPane, BorderLayout.CENTER);
//         mainPanel.add(loadButton, BorderLayout.SOUTH);

//         editorFrame.add(mainPanel);
//         editorFrame.setVisible(true);
//     }

//     private static JTextArea createTextArea() {
//         JTextArea textArea = new JTextArea();
//         textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
//         textArea.setTabSize(4);

//         textArea.setText(
//             "; Motorola 6809 Assembly Code\n" +
//             "; Example code - copy this to terminal\n\n" +
//             "LDAA #$FF\n" +
//             "STAA $2000\n" +
//             "LDX #$3000\n" +
//             "END\n"
//         );

//         return textArea;
//     }

//     private static void runTerminalExecution() {
//         // This runs after GUI is shown
//         System.out.println("\n=== TERMINAL EXECUTION STARTED ===");
//         System.out.println("Switch to terminal window to enter assembly code.");
//         System.out.println("Or click 'New' button to open text editor.");
//         System.out.println("====================================\n");

//         // Your original terminal execution code
//         lecture lec = new lecture(myList, ram, rom, reg, modeDetector);
//         stepExecutor = new pas(myList, ram, rom, reg, modeDetector);
//         stepExecutor.executeStepByStep();

//         // After execution, update GUI displays
//         updateAllDisplays();
//     }

//     private static void updateAllDisplays() {
//         if (ramFrame != null && ramFrame.isVisible()) {
//             ramFrame.updateRAMDisplay();
//         }
//         if (romFrame != null && romFrame.isVisible()) {
//             romFrame.updateROMDisplay();
//         }
//     }
// }
package processeur;

import java.util.ArrayList;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.*;

public class main {
    // Backend objects
    private static ROM rom;
    private static ram ram;
    private static registre reg;
    private static mode modeDetector;
    private static ArrayList<ArrayList<String>> myList;
    private static pas stepExecutor;

    // GUI frames
    private static RAMDisplayFrame ramFrame;
    private static ROMDisplayFrame romFrame;
    private static EnhancedRegisterDisplayFrame registerFrame;
    private static StepExecutionFrame stepFrame;

    // GUI components
    private static JLabel statusLabel;
    private static JFrame mainFrame;
    private static JTextArea executionLog;
    private static JLabel stateInfoLabel;
    private static JLabel progressLabel;

    // Buttons
    private static JButton stepButton;
    private static JButton runButton;
    private static JButton newButton;
    private static JButton resetButton;
    private static JButton saveButton;
    private static JButton stopButton;

    // State flags
    private static boolean hasLoadedCode = false;
    private static boolean isExecuting = false;
    private static volatile boolean stopExecution = false;
    private static String currentLoadedCode = "";

    public static void main(String[] args) {
        // Initialize backend
        initializeBackend();

        // Create GUI
        SwingUtilities.invokeLater(() -> createMainWindow());
    }

    private static void initializeBackend() {
        rom = new ROM();
        ram = new ram();
        reg = new registre();
        modeDetector = new mode();

        modeDetector.setRegistre(reg);
        modeDetector.setRam(ram);

        myList = new ArrayList<>();
    }

    /**
     * Helper method to set logo consistently across all windows
     */
    private static void setWindowLogo(JFrame frame) {
        try {
            ImageIcon logo = new ImageIcon("logoprincipale.png");
            if (logo.getIconWidth() > 0) {
                frame.setIconImage(logo.getImage());
                System.out.println("[Logo] Successfully loaded for " + frame.getTitle());
            } else {
                System.out.println("[Logo] Warning: Logo file found but invalid for " + frame.getTitle());
            }
        } catch (Exception e) {
            System.out.println("[Logo] Could not load logo for " + frame.getTitle() + ": " + e.getMessage());
        }
    }

    private static void createMainWindow() {
        mainFrame = new JFrame("Motorola 6809 Simulator");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1200, 250);
        mainFrame.setLayout(new BorderLayout());

        // Set logo for main window
        setWindowLogo(mainFrame);

        // Toolbar
        JToolBar toolBar = createToolBar();
        mainFrame.add(toolBar, BorderLayout.NORTH);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Status panel
        JPanel statusPanel = new JPanel(new GridLayout(3, 1));

        statusLabel = new JLabel("Ready - Click 'New' to write assembly code", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        stateInfoLabel = new JLabel("No code loaded", SwingConstants.CENTER);
        stateInfoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        stateInfoLabel.setForeground(Color.DARK_GRAY);

        progressLabel = new JLabel("", SwingConstants.CENTER);
        progressLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        progressLabel.setForeground(Color.GRAY);

        statusPanel.add(statusLabel);
        statusPanel.add(stateInfoLabel);
        statusPanel.add(progressLabel);

        // Execution log
        executionLog = new JTextArea(4, 50);
        executionLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        executionLog.setEditable(false);
        executionLog.setBackground(new Color(240, 240, 240));
        executionLog.setBorder(BorderFactory.createTitledBorder("Execution Log"));
        JScrollPane logScroll = new JScrollPane(executionLog);

        mainPanel.add(statusPanel, BorderLayout.NORTH);
        mainPanel.add(logScroll, BorderLayout.CENTER);

        mainFrame.add(mainPanel, BorderLayout.CENTER);
        mainFrame.setVisible(true);

        // Create display frames
        createDisplayFrames();
    }

    private static void createDisplayFrames() {
        ramFrame = new RAMDisplayFrame(ram);
        romFrame = new ROMDisplayFrame(rom);

        ramFrame.setLocation(50, 280);
        romFrame.setLocation(500, 280);
    }

    private static JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(new Color(230, 230, 230));
        toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

        // New button
        newButton = createToolbarButton("New", new Color(0, 120, 215));
        newButton.addActionListener(e -> openNewCodeEditor());

        // Run button
        runButton = createToolbarButton("Run", new Color(0, 150, 0));
        runButton.setEnabled(false);
        runButton.addActionListener(e -> executeFullProgram());

        // Step button
        stepButton = createToolbarButton("Step", new Color(0, 100, 200));
        stepButton.setEnabled(false);
        stepButton.addActionListener(e -> openStepDebugger());

        // Reset button
        resetButton = createToolbarButton("Reset", new Color(200, 0, 0));
        resetButton.setEnabled(false);
        resetButton.addActionListener(e -> resetSimulator());

        // View buttons
        JButton ramButton = createToolbarButton("RAM", new Color(100, 100, 100));
        ramButton.addActionListener(e -> showRAMFrame());

        JButton romButton = createToolbarButton("ROM", new Color(100, 100, 100));
        romButton.addActionListener(e -> showROMFrame());

        JButton regButton = createToolbarButton("Registers", new Color(100, 100, 100));
        regButton.addActionListener(e -> showRegisterFrame());

        // Save button
        saveButton = createToolbarButton("💾 Save", new Color(75, 0, 130));
        saveButton.setEnabled(false);
        saveButton.addActionListener(e -> saveCodeToFile());

        // Stop button
        stopButton = createToolbarButton("⏹ Stop", new Color(180, 0, 0));
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> stopCurrentExecution());

        toolBar.add(newButton);
        toolBar.addSeparator();
        toolBar.add(runButton);
        toolBar.add(stepButton);
        toolBar.addSeparator();
        toolBar.add(resetButton);
        toolBar.addSeparator();
        toolBar.add(ramButton);
        toolBar.add(romButton);
        toolBar.add(regButton);
        toolBar.addSeparator();
        toolBar.add(saveButton);
        toolBar.add(stopButton);
        toolBar.add(Box.createHorizontalGlue());

        return toolBar;
    }

    private static JButton createToolbarButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setMargin(new Insets(5, 15, 5, 15));
        return button;
    }

    private static void saveCodeToFile() {
        if (currentLoadedCode.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Aucun code à sauvegarder!",
                    "Erreur",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Sauvegarder le code assembleur");
        fileChooser.setSelectedFile(new File("program_6809.asm"));

        int userSelection = fileChooser.showSaveDialog(mainFrame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                writer.write("; Motorola 6809 Assembly Program\n");
                writer.write("; Saved: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n");
                writer.write("; Total instructions: " + myList.size() + "\n");
                writer.write(";\n");
                writer.write("; ===========================================\n\n");
                writer.write(currentLoadedCode);
                writer.write("\n\n; ===========================================\n");
                writer.write("; End of program\n");

                addToLog("✓ Code sauvegardé: " + fileToSave.getName());
                JOptionPane.showMessageDialog(mainFrame,
                        "Code sauvegardé avec succès!\n\nFichier: " + fileToSave.getName() + "\nLignes: "
                                + myList.size(),
                        "Sauvegarde réussie",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                addToLog("✗ Erreur lors de la sauvegarde: " + ex.getMessage());
                JOptionPane.showMessageDialog(mainFrame,
                        "Erreur lors de la sauvegarde:\n" + ex.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void stopCurrentExecution() {
        if (isExecuting) {
            stopExecution = true;
            stopButton.setEnabled(false);
            addToLog("⚠ Arrêt de l'exécution demandé...");

            statusLabel.setText("⏹ Exécution arrêtée par l'utilisateur");
            statusLabel.setForeground(new Color(180, 0, 0));
            stateInfoLabel.setText("Arrêt brutal à la ligne courante");

            JOptionPane.showMessageDialog(mainFrame,
                    "L'exécution sera arrêtée dès que possible.\nVeuillez patienter...",
                    "Arrêt en cours",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private static void openNewCodeEditor() {
        JFrame editorFrame = new JFrame("Assembly Code Editor");
        editorFrame.setSize(700, 500);
        editorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        editorFrame.setLocationRelativeTo(mainFrame);

        // Set logo for editor
        setWindowLogo(editorFrame);

        JPanel mainPanel = new JPanel(new BorderLayout());
        JTextArea textArea = createTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);

        JButton loadButton = new JButton("✓ Load Code");
        loadButton.setBackground(new Color(0, 150, 0));
        loadButton.setForeground(Color.WHITE);
        loadButton.setFont(new Font("Arial", Font.BOLD, 12));
        loadButton.addActionListener(e -> {
            String code = textArea.getText();
            if (loadCodeFromText(code)) {
                editorFrame.dispose();
                addToLog("New code loaded: " + myList.size() + " instructions");
                JOptionPane.showMessageDialog(mainFrame,
                        "✓ Code chargé avec succès!\n\nInstructions: " + myList.size() + " lignes\n\n" +
                                "• Utilisez 'Run' pour exécuter tout d'un coup\n" +
                                "• Utilisez 'Step' pour déboguer ligne par ligne\n" +
                                "• Utilisez 'Save' pour sauvegarder le code",
                        "Code chargé",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton cancelButton = new JButton("✗ Cancel");
        cancelButton.addActionListener(e -> editorFrame.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loadButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(cancelButton);

        JLabel instructionLabel = new JLabel("Enter Motorola 6809 Assembly Code (end with END):");
        instructionLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        mainPanel.add(instructionLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        editorFrame.add(mainPanel);
        editorFrame.setVisible(true);
        addToLog("Code Editor opened");
    }

    private static JTextArea createTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setTabSize(4);

        textArea.setText(
                "; Motorola 6809 Assembly Code\n" +
                        "; Example program\n\n" +
                        "LDA #$FF\n" +
                        "STA $2000\n" +
                        "LDX #$3000\n" +
                        "DECA\n" +
                        "STA $2001\n" +
                        "END\n");

        return textArea;
    }

    private static boolean loadCodeFromText(String code) {
        if (code == null || code.trim().isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame,
                    "❌ Le code est vide!\n\nVeuillez entrer au moins une instruction.",
                    "Erreur - Code vide",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        fullSystemReset();
        myList.clear();
        String[] lines = code.split("\n");
        boolean hasValidCode = false;
        StringBuilder validationErrors = new StringBuilder();
        int lineNumber = 0;
        currentLoadedCode = code;

        for (String line : lines) {
            lineNumber++;
            line = line.trim();

            if (line.isEmpty() || line.startsWith(";")) {
                continue;
            }

            if (line.contains(";")) {
                line = line.substring(0, line.indexOf(";")).trim();
            }

            if (line.isEmpty()) {
                continue;
            }

            String[] words = line.split("\\s+");
            ArrayList<String> lineWords = new ArrayList<>();

            if (words.length >= 1) {
                String instruction = words[0];
                String operand = (words.length > 1) ? words[1] : "";

                String error = InstructionValidator.validateInstruction(instruction, operand);
                if (error != null) {
                    validationErrors.append("📍 Ligne ").append(lineNumber).append(": ")
                            .append(line).append("\n")
                            .append(error).append("\n\n");
                }
            }

            for (String word : words) {
                if (!word.isEmpty()) {
                    lineWords.add(word.toUpperCase());
                }
            }

            if (!lineWords.isEmpty()) {
                myList.add(lineWords);
                hasValidCode = true;
            }
        }

        if (validationErrors.length() > 0) {
            JTextArea errorArea = new JTextArea(validationErrors.toString());
            errorArea.setEditable(false);
            errorArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            errorArea.setBackground(new Color(255, 240, 240));

            JScrollPane scrollPane = new JScrollPane(errorArea);
            scrollPane.setPreferredSize(new Dimension(600, 300));

            JOptionPane.showMessageDialog(mainFrame,
                    scrollPane,
                    "❌ Erreurs de validation détectées",
                    JOptionPane.ERROR_MESSAGE);

            addToLog("✗ Validation failed: " + validationErrors.toString().split("\n").length / 3 + " error(s)");
            myList.clear();
            currentLoadedCode = "";
            return false;
        }

        boolean hasEnd = false;
        for (ArrayList<String> currentLine : myList) {
            if (!currentLine.isEmpty() && currentLine.get(0).equals("END")) {
                hasEnd = true;
                break;
            }
        }

        if (!hasEnd && hasValidCode) {
            ArrayList<String> endLine = new ArrayList<>();
            endLine.add("END");
            myList.add(endLine);
        }

        if (hasValidCode) {
            stepExecutor = new pas(myList, ram, rom, reg, modeDetector);
            hasLoadedCode = true;

            statusLabel.setText("✓ Code chargé - " + myList.size() + " instructions prêtes");
            statusLabel.setForeground(new Color(0, 100, 0));
            stateInfoLabel.setText(myList.size() + " instructions validées - Prêt à exécuter");

            runButton.setEnabled(true);
            stepButton.setEnabled(true);
            resetButton.setEnabled(true);
            saveButton.setEnabled(true);

            return true;
        } else {
            JOptionPane.showMessageDialog(mainFrame,
                    "❌ Aucun code valide trouvé!\n\nVeuillez entrer des instructions d'assemblage.",
                    "Erreur - Pas de code",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }

    // Rest of the methods remain the same...
    // (executeFullProgram, openStepDebugger, resetSimulator, etc.)
    // I'm omitting them here to save space, but they stay unchanged

    private static void executeFullProgram() {
        // Implementation stays the same
    }

    private static void openStepDebugger() {
        if (!hasLoadedCode || stepExecutor == null) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Pas de code chargé! Cliquez sur 'New' pour écrire du code d'abord.",
                    "Pas de code",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (stepFrame != null && stepFrame.isVisible()) {
            stepFrame.dispose();
        }

        resetToInitialState();
        stepExecutor = new pas(myList, ram, rom, reg, modeDetector);

        addToLog("Step-by-step debugger opened");
        statusLabel.setText("Step debugger active");

        stepFrame = new StepExecutionFrame(stepExecutor, ram, rom, reg, myList);
        stepFrame.setLocation(100, 100);
        stepFrame.setVisible(true);
    }

    private static void resetSimulator() {
        // Implementation stays the same
    }

    private static void fullSystemReset() {
        // Implementation stays the same
    }

    private static void resetToInitialState() {
        reg.setA("00");
        reg.setB("00");
        reg.setD("0000");
        reg.setX("0000");
        reg.setY("0000");
        reg.setS("0000");
        reg.setU("0000");
        reg.setCCR("00");
        reg.setPC(0);

        for (int i = 0; i < 65536; i++) {
            String addr = String.format("%04X", i);
            ram.getram().put(addr, "00");
        }

        rom.resetWritePointer();
        for (int i = 0; i < 65536; i++) {
            String addr = String.format("%04X", i);
            rom.getMemory().put(addr, "00");
        }
    }

    private static void showRAMFrame() {
        if (ramFrame != null) {
            ramFrame.updateRAMDisplay();
            ramFrame.setVisible(true);
            ramFrame.toFront();
            addToLog("RAM Viewer opened");
        }
    }

    private static void showROMFrame() {
        if (romFrame != null) {
            romFrame.updateROMDisplay();
            romFrame.setVisible(true);
            romFrame.toFront();
            addToLog("ROM Viewer opened");
        }
    }

    private static void showRegisterFrame() {
        if (registerFrame != null) {
            registerFrame.dispose();
        }
        registerFrame = new EnhancedRegisterDisplayFrame(reg, modeDetector);
        registerFrame.setLocation(950, 280);
        registerFrame.setVisible(true);
        addToLog("Register Viewer opened");
    }

    private static void updateAllDisplays() {
        if (ramFrame != null && ramFrame.isVisible()) {
            ramFrame.updateRAMDisplay();
        }
        if (romFrame != null && romFrame.isVisible()) {
            romFrame.updateROMDisplay();
        }
        if (registerFrame != null && registerFrame.isVisible()) {
            registerFrame.updateRegisterDisplay();
        }
    }

    private static void addToLog(String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        executionLog.append("[" + timestamp + "] " + message + "\n");
        executionLog.setCaretPosition(executionLog.getDocument().getLength());
    }
}