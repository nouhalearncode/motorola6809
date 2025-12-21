package processeur;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class EnhancedRegisterDisplayFrame extends JFrame {
    private JTextField aField, bField, dField, xField, yField;
    private JTextField sField, uField, ccrField, pcField;
    private JLabel ccrBinaryLabel;
    
    // Individual flag labels
    private JLabel eFlagLabel, fFlagLabel, hFlagLabel, iFlagLabel;
    private JLabel nFlagLabel, zFlagLabel, vFlagLabel, cFlagLabel;
    
    private registre registers;
    private mode modeDetector;
    private Timer updateTimer;
    
    public EnhancedRegisterDisplayFrame(registre registers, mode modeDetector) {
        this.registers = registers;
        this.modeDetector = modeDetector;
        
        setTitle("Register Viewer - Motorola 6809 (Double-click to edit)");
        setSize(450, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Set logo
        setLogo();
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Instruction label
        JLabel instructionLabel = new JLabel("💡 Double-click any register value to edit", SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        instructionLabel.setForeground(new Color(100, 100, 100));
        instructionLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.add(instructionLabel, BorderLayout.NORTH);
        
        // Registers panel
        JPanel registersPanel = new JPanel(new GridLayout(9, 2, 10, 8));
        registersPanel.setBorder(BorderFactory.createTitledBorder("Registers"));
        registersPanel.setBackground(Color.WHITE);
        
        // Create editable register fields
        registersPanel.add(createBoldLabel("A (Accumulator):"));
        aField = createEditableField("00", 2);
        registersPanel.add(aField);
        
        registersPanel.add(createBoldLabel("B (Accumulator):"));
        bField = createEditableField("00", 2);
        registersPanel.add(bField);
        
        registersPanel.add(createBoldLabel("D (A:B):"));
        dField = createEditableField("0000", 4);
        registersPanel.add(dField);
        
        registersPanel.add(createBoldLabel("X (Index):"));
        xField = createEditableField("0000", 4);
        registersPanel.add(xField);
        
        registersPanel.add(createBoldLabel("Y (Index):"));
        yField = createEditableField("0000", 4);
        registersPanel.add(yField);
        
        registersPanel.add(createBoldLabel("S (Stack Pointer):"));
        sField = createEditableField("0000", 4);
        registersPanel.add(sField);
        
        registersPanel.add(createBoldLabel("U (User Stack):"));
        uField = createEditableField("0000", 4);
        registersPanel.add(uField);
        
        registersPanel.add(createBoldLabel("CCR (Hex):"));
        ccrField = createEditableField("00", 2);
        registersPanel.add(ccrField);
        
        registersPanel.add(createBoldLabel("PC (Program Counter):"));
        pcField = createEditableField("0000", 4);
        registersPanel.add(pcField);
        
        // CCR Binary panel
        JPanel ccrPanel = new JPanel(new BorderLayout());
        ccrPanel.setBorder(BorderFactory.createTitledBorder("Condition Code Register (CCR) - 8 Bits"));
        ccrPanel.setBackground(Color.WHITE);
        
        ccrBinaryLabel = new JLabel("00000000", SwingConstants.CENTER);
        ccrBinaryLabel.setFont(new Font("Monospaced", Font.BOLD, 20));
        ccrBinaryLabel.setForeground(new Color(0, 100, 200));
        ccrPanel.add(ccrBinaryLabel, BorderLayout.NORTH);
        
        // Flags panel
        JPanel flagsPanel = new JPanel(new GridLayout(4, 4, 5, 5));
        flagsPanel.setBackground(Color.WHITE);
        flagsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create flag labels (same as before)
        flagsPanel.add(createFlagHeaderLabel("Bit 7 - E"));
        eFlagLabel = createFlagValueLabel("0");
        flagsPanel.add(eFlagLabel);
        flagsPanel.add(createFlagDescLabel("Entire"));
        flagsPanel.add(new JLabel());
        
        flagsPanel.add(createFlagHeaderLabel("Bit 6 - F"));
        fFlagLabel = createFlagValueLabel("0");
        flagsPanel.add(fFlagLabel);
        flagsPanel.add(createFlagDescLabel("FIRQ Mask"));
        flagsPanel.add(new JLabel());
        
        flagsPanel.add(createFlagHeaderLabel("Bit 5 - H"));
        hFlagLabel = createFlagValueLabel("0");
        flagsPanel.add(hFlagLabel);
        flagsPanel.add(createFlagDescLabel("Half Carry"));
        flagsPanel.add(new JLabel());
        
        flagsPanel.add(createFlagHeaderLabel("Bit 4 - I"));
        iFlagLabel = createFlagValueLabel("0");
        flagsPanel.add(iFlagLabel);
        flagsPanel.add(createFlagDescLabel("IRQ Mask"));
        flagsPanel.add(new JLabel());
        
        flagsPanel.add(createFlagHeaderLabel("Bit 3 - N"));
        nFlagLabel = createFlagValueLabel("0");
        flagsPanel.add(nFlagLabel);
        flagsPanel.add(createFlagDescLabel("Negative"));
        flagsPanel.add(new JLabel());
        
        flagsPanel.add(createFlagHeaderLabel("Bit 2 - Z"));
        zFlagLabel = createFlagValueLabel("0");
        flagsPanel.add(zFlagLabel);
        flagsPanel.add(createFlagDescLabel("Zero"));
        flagsPanel.add(new JLabel());
        
        flagsPanel.add(createFlagHeaderLabel("Bit 1 - V"));
        vFlagLabel = createFlagValueLabel("0");
        flagsPanel.add(vFlagLabel);
        flagsPanel.add(createFlagDescLabel("Overflow"));
        flagsPanel.add(new JLabel());
        
        flagsPanel.add(createFlagHeaderLabel("Bit 0 - C"));
        cFlagLabel = createFlagValueLabel("0");
        flagsPanel.add(cFlagLabel);
        flagsPanel.add(createFlagDescLabel("Carry"));
        flagsPanel.add(new JLabel());
        
        ccrPanel.add(flagsPanel, BorderLayout.CENTER);
        
        // Assemble panels
        mainPanel.add(registersPanel, BorderLayout.CENTER);
        mainPanel.add(ccrPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Update timer
        updateTimer = new Timer(500, e -> updateRegisterDisplay());
        updateTimer.start();
        
        // Initial display
        updateRegisterDisplay();
    }
    
    private void setLogo() {
        try {
            ImageIcon logo = new ImageIcon("logoprincipale.png");
            if (logo.getIconWidth() > 0) {
                setIconImage(logo.getImage());
            }
        } catch (Exception e) {
            System.out.println("[RegisterFrame] Logo not found: " + e.getMessage());
        }
    }
    
    private JLabel createBoldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        return label;
    }
    
    private JTextField createEditableField(String text, int maxLength) {
        JTextField field = new JTextField(text);
        field.setFont(new Font("Monospaced", Font.BOLD, 14));
        field.setForeground(new Color(0, 100, 200));
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setEditable(true);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        
        // Add focus listener for visual feedback
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.BLUE, 2),
                    BorderFactory.createEmptyBorder(2, 5, 2, 5)
                ));
                field.selectAll();
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(2, 5, 2, 5)
                ));
                validateAndUpdateBackend(field, maxLength);
            }
        });
        
        // Add Enter key listener
        field.addActionListener(e -> {
            validateAndUpdateBackend(field, maxLength);
            field.transferFocus();
        });
        
        return field;
    }
    
    private void validateAndUpdateBackend(JTextField field, int maxLength) {
        String value = field.getText().trim().toUpperCase();
        
        // Validate hex
        if (!value.matches("[0-9A-F]+") || value.length() > maxLength) {
            JOptionPane.showMessageDialog(this,
                "Invalid hex value! Please enter " + maxLength + " hex digits.",
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE);
            updateRegisterDisplay(); // Revert to backend value
            return;
        }
        
        // Pad with zeros if needed
        while (value.length() < maxLength) {
            value = "0" + value;
        }
        
        // Update backend based on which field
        try {
            if (field == aField) {
                registers.setA(value);
                System.out.println("[REG EDIT] A <- " + value);
            } else if (field == bField) {
                registers.setB(value);
                System.out.println("[REG EDIT] B <- " + value);
            } else if (field == dField) {
                registers.setD(value);
                System.out.println("[REG EDIT] D <- " + value);
            } else if (field == xField) {
                registers.setX(value);
                System.out.println("[REG EDIT] X <- " + value);
            } else if (field == yField) {
                registers.setY(value);
                System.out.println("[REG EDIT] Y <- " + value);
            } else if (field == sField) {
                registers.setS(value);
                System.out.println("[REG EDIT] S <- " + value);
            } else if (field == uField) {
                registers.setU(value);
                System.out.println("[REG EDIT] U <- " + value);
            } else if (field == ccrField) {
                registers.setCCR(value);
                System.out.println("[REG EDIT] CCR <- " + value);
            } else if (field == pcField) {
                int pcValue = Integer.parseInt(value, 16);
                registers.setPC(pcValue);
                System.out.println("[REG EDIT] PC <- " + value);
            }
            
            // Force immediate update
            updateRegisterDisplay();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error updating register: " + e.getMessage(),
                "Update Error",
                JOptionPane.ERROR_MESSAGE);
            updateRegisterDisplay(); // Revert
        }
    }
    
    private JLabel createFlagHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 11));
        return label;
    }
    
    private JLabel createFlagValueLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Monospaced", Font.BOLD, 18));
        label.setOpaque(true);
        label.setBackground(new Color(240, 240, 240));
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        return label;
    }
    
    private JLabel createFlagDescLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 10));
        label.setForeground(Color.DARK_GRAY);
        return label;
    }
    
    public void updateRegisterDisplay() {
        SwingUtilities.invokeLater(() -> {
            // Only update if field doesn't have focus (to avoid overwriting during edit)
            if (!aField.hasFocus()) aField.setText(registers.getA());
            if (!bField.hasFocus()) bField.setText(registers.getB());
            if (!dField.hasFocus()) dField.setText(registers.getD());
            if (!xField.hasFocus()) xField.setText(registers.getX());
            if (!yField.hasFocus()) yField.setText(registers.getY());
            if (!sField.hasFocus()) sField.setText(registers.getS());
            if (!uField.hasFocus()) uField.setText(registers.getU());
            if (!ccrField.hasFocus()) ccrField.setText(registers.getCCR());
            if (!pcField.hasFocus()) pcField.setText(registers.getPC());
            
            // Update CCR binary representation
            String ccrHex = registers.getCCR();
            int ccrValue = 0;
            try {
                ccrValue = Integer.parseInt(ccrHex, 16);
            } catch (Exception e) {
                ccrValue = 0;
            }
            
            String ccrBinary = String.format("%8s", Integer.toBinaryString(ccrValue)).replace(' ', '0');
            ccrBinaryLabel.setText(ccrBinary);
            
            // Update individual flags
            updateFlagDisplay(eFlagLabel, (ccrValue >> 7) & 1);
            updateFlagDisplay(fFlagLabel, (ccrValue >> 6) & 1);
            updateFlagDisplay(hFlagLabel, (ccrValue >> 5) & 1);
            updateFlagDisplay(iFlagLabel, (ccrValue >> 4) & 1);
            updateFlagDisplay(nFlagLabel, (ccrValue >> 3) & 1);
            updateFlagDisplay(zFlagLabel, (ccrValue >> 2) & 1);
            updateFlagDisplay(vFlagLabel, (ccrValue >> 1) & 1);
            updateFlagDisplay(cFlagLabel, ccrValue & 1);
        });
    }
    
    private void updateFlagDisplay(JLabel label, int value) {
        label.setText(String.valueOf(value));
        if (value == 1) {
            label.setBackground(new Color(255, 200, 200));
            label.setForeground(new Color(200, 0, 0));
        } else {
            label.setBackground(new Color(240, 240, 240));
            label.setForeground(new Color(100, 100, 100));
        }
    }
    
    @Override
    public void dispose() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
        super.dispose();
    }
}