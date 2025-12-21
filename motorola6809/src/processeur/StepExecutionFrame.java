package processeur;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class StepExecutionFrame extends JFrame {
    private pas stepExecutor;
    private registre reg;
    private ram ram;
    private ROM rom;
    
    private JTextArea programTextArea;
    private JLabel statusLabel;
    private JButton nextButton;
    private JButton backButton;
    private JButton runRestButton;
    private JButton closeButton;
    
    private RAMDisplayFrame ramViewer;
    private ROMDisplayFrame romViewer;
    private RegisterDisplayFrame registerViewer;
    
    public StepExecutionFrame(pas stepExecutor, ram ram, ROM rom, registre reg, ArrayList<ArrayList<String>> lines) {
        this.stepExecutor = stepExecutor;
        this.ram = ram;
        this.rom = rom;
        this.reg = reg;
        
        setTitle("Step-by-Step Execution Debugger");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Program display area
        programTextArea = new JTextArea();
        programTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        programTextArea.setEditable(false);
        programTextArea.setBackground(new Color(245, 245, 245));
        
        // Display program
        StringBuilder programText = new StringBuilder();
        programText.append("=== ASSEMBLY PROGRAM ===\n\n");
        for (int i = 0; i < lines.size(); i++) {
            programText.append(String.format("%-4d | ", i + 1));
            for (String word : lines.get(i)) {
                programText.append(word).append(" ");
            }
            programText.append("\n");
        }
        programTextArea.setText(programText.toString());
        
        JScrollPane programScroll = new JScrollPane(programTextArea);
        programScroll.setBorder(BorderFactory.createTitledBorder("Program Code"));
        
        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Ready - Click 'Next' to execute first line");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        
        // Control buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        
        backButton = createStyledButton("← Back", new Color(200, 100, 0));
        backButton.setEnabled(false);
        backButton.addActionListener(e -> goBackOneStep());
        
        nextButton = createStyledButton("Next →", new Color(0, 100, 200));
        nextButton.addActionListener(e -> goNextStep());
        
        runRestButton = createStyledButton("⏩ Run Rest", new Color(0, 150, 0));
        runRestButton.addActionListener(e -> runRemainingSteps());
        
        JButton viewRAMButton = createStyledButton("View RAM", new Color(100, 100, 100));
        viewRAMButton.addActionListener(e -> showRAM());
        
        JButton viewROMButton = createStyledButton("View ROM", new Color(100, 100, 100));
        viewROMButton.addActionListener(e -> showROM());
        
        JButton viewRegistersButton = createStyledButton("View Registers", new Color(100, 100, 100));
        viewRegistersButton.addActionListener(e -> showRegisters());
        
        closeButton = createStyledButton("Close", new Color(150, 150, 150));
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(backButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(runRestButton);
        buttonPanel.add(viewRAMButton);
        buttonPanel.add(viewROMButton);
        buttonPanel.add(viewRegistersButton);
        buttonPanel.add(closeButton);
        
        // State info panel
        JPanel statePanel = new JPanel(new GridLayout(3, 1));
        statePanel.setBorder(BorderFactory.createTitledBorder("Execution State"));
        
        JLabel stateInfo1 = new JLabel();
        JLabel stateInfo2 = new JLabel();
        JLabel stateInfo3 = new JLabel();
        
        statePanel.add(stateInfo1);
        statePanel.add(stateInfo2);
        statePanel.add(stateInfo3);
        
        // Main layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(statusPanel, BorderLayout.NORTH);
        topPanel.add(statePanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        add(programScroll, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Update state display
        updateStateDisplay();
        highlightCurrentLine();
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(140, 35));
        return button;
    }
    
    private void goNextStep() {
        boolean hasMore = stepExecutor.executeGUISingleStep();
        updateStateDisplay();
        highlightCurrentLine();
        updateViewers();
        
        if (!hasMore) {
            nextButton.setEnabled(false);
            statusLabel.setText("✓ Execution Complete!");
            statusLabel.setForeground(new Color(0, 150, 0));
            JOptionPane.showMessageDialog(this, 
                "Program execution completed!\nAll " + stepExecutor.getTotalSteps() + " instructions executed.",
                "Execution Complete", 
                JOptionPane.INFORMATION_MESSAGE);
        }
        
        backButton.setEnabled(stepExecutor.canGoBack());
    }
    
    private void goBackOneStep() {
        boolean success = stepExecutor.goBackOneStep();
        if (success) {
            updateStateDisplay();
            highlightCurrentLine();
            updateViewers();
            nextButton.setEnabled(true);
            backButton.setEnabled(stepExecutor.canGoBack());
        }
    }
    
    private void runRemainingSteps() {
        int remaining = stepExecutor.getTotalSteps() - stepExecutor.getCurrentStepNumber();
        if (remaining <= 0) {
            JOptionPane.showMessageDialog(this, 
                "Already at the end of execution!",
                "Info", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Execute remaining " + remaining + " instructions?",
            "Run Rest",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            stepExecutor.executeGUIAllSteps();
            updateStateDisplay();
            highlightCurrentLine();
            updateViewers();
            nextButton.setEnabled(false);
            statusLabel.setText("✓ All instructions executed!");
            statusLabel.setForeground(new Color(0, 150, 0));
        }
    }
    
    private void updateStateDisplay() {
        int current = stepExecutor.getCurrentStepNumber();
        int total = stepExecutor.getTotalSteps();
        
        if (current > 0 && current <= total) {
            ArrayList<String> line = stepExecutor.getCurrentLineForDisplay();
            StringBuilder lineText = new StringBuilder();
            for (String word : line) {
                lineText.append(word).append(" ");
            }
            statusLabel.setText("Line " + current + "/" + total + ": " + lineText.toString());
        } else if (current == 0) {
            statusLabel.setText("Ready - Click 'Next' to execute first line");
        } else {
            statusLabel.setText("Execution complete - " + total + " instructions executed");
        }
        
        statusLabel.setForeground(Color.BLACK);
    }
    
    private void highlightCurrentLine() {
        int current = stepExecutor.getCurrentStepNumber();
        
        // Rebuild text with highlight
        StringBuilder programText = new StringBuilder();
        programText.append("=== ASSEMBLY PROGRAM ===\n\n");
        
        ArrayList<ArrayList<String>> lines = stepExecutor.getLineAtStep(0) != null ? 
            new ArrayList<>() : new ArrayList<>();
        
        for (int i = 0; i < stepExecutor.getTotalSteps(); i++) {
            lines.add(stepExecutor.getLineAtStep(i));
        }
        
        for (int i = 0; i < lines.size(); i++) {
            String prefix;
            if (i == current - 1) {
                prefix = ">>> ";  // Current line
            } else if (i < current - 1) {
                prefix = "[✓] ";  // Executed
            } else {
                prefix = "    ";  // Not executed yet
            }
            
            programText.append(String.format("%-4s%-4d | ", prefix, i + 1));
            for (String word : lines.get(i)) {
                programText.append(word).append(" ");
            }
            programText.append("\n");
        }
        
        programTextArea.setText(programText.toString());
    }
    
    private void showRAM() {
        if (ramViewer == null || !ramViewer.isDisplayable()) {
            ramViewer = new RAMDisplayFrame(ram);
            ramViewer.setLocation(50, 100);
        }
        ramViewer.updateRAMDisplay();
        ramViewer.setVisible(true);
        ramViewer.toFront();
    }
    
    private void showROM() {
        if (romViewer == null || !romViewer.isDisplayable()) {
            romViewer = new ROMDisplayFrame(rom);
            romViewer.setLocation(500, 100);
        }
        romViewer.updateROMDisplay();
        romViewer.setVisible(true);
        romViewer.toFront();
    }
    
    private void showRegisters() {
        if (registerViewer == null || !registerViewer.isDisplayable()) {
            registerViewer = new RegisterDisplayFrame(reg);
            registerViewer.setLocation(950, 100);
        }
        registerViewer.updateRegisterDisplay();
        registerViewer.setVisible(true);
        registerViewer.toFront();
    }
    
    private void updateViewers() {
        if (ramViewer != null && ramViewer.isVisible()) {
            ramViewer.updateRAMDisplay();
        }
        if (romViewer != null && romViewer.isVisible()) {
            romViewer.updateROMDisplay();
        }
        if (registerViewer != null && registerViewer.isVisible()) {
            registerViewer.updateRegisterDisplay();
        }
    }
    
    @Override
    public void dispose() {
        if (ramViewer != null) ramViewer.dispose();
        if (romViewer != null) romViewer.dispose();
        if (registerViewer != null) registerViewer.dispose();
        super.dispose();
    }
}