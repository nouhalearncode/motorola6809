package processeur;

import javax.swing.*;
import java.awt.*;

public class EnhancedRegisterDisplayFrame extends JFrame {
    private JLabel aLabel, bLabel, dLabel, xLabel, yLabel;
    private JLabel sLabel, uLabel, ccrLabel, pcLabel;
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
        
        setTitle("Register Viewer - Motorola 6809");
        setSize(450, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Registers panel
        JPanel registersPanel = new JPanel(new GridLayout(9, 2, 10, 8));
        registersPanel.setBorder(BorderFactory.createTitledBorder("Registers"));
        registersPanel.setBackground(Color.WHITE);
        
        // Add registers
        registersPanel.add(createBoldLabel("A (Accumulator):"));
        aLabel = createValueLabel("00");
        registersPanel.add(aLabel);
        
        registersPanel.add(createBoldLabel("B (Accumulator):"));
        bLabel = createValueLabel("00");
        registersPanel.add(bLabel);
        
        registersPanel.add(createBoldLabel("D (A:B):"));
        dLabel = createValueLabel("0000");
        registersPanel.add(dLabel);
        
        registersPanel.add(createBoldLabel("X (Index):"));
        xLabel = createValueLabel("0000");
        registersPanel.add(xLabel);
        
        registersPanel.add(createBoldLabel("Y (Index):"));
        yLabel = createValueLabel("0000");
        registersPanel.add(yLabel);
        
        registersPanel.add(createBoldLabel("S (Stack Pointer):"));
        sLabel = createValueLabel("0000");
        registersPanel.add(sLabel);
        
        registersPanel.add(createBoldLabel("U (User Stack):"));
        uLabel = createValueLabel("0000");
        registersPanel.add(uLabel);
        
        registersPanel.add(createBoldLabel("CCR (Hex):"));
        ccrLabel = createValueLabel("00");
        registersPanel.add(ccrLabel);
        
        registersPanel.add(createBoldLabel("PC (Program Counter):"));
        pcLabel = createValueLabel("0000");
        registersPanel.add(pcLabel);
        
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
        
        // Bit 7 - E (Entire flag)
        flagsPanel.add(createFlagHeaderLabel("Bit 7 - E"));
        eFlagLabel = createFlagValueLabel("0");
        flagsPanel.add(eFlagLabel);
        flagsPanel.add(createFlagDescLabel("Entire"));
        flagsPanel.add(new JLabel());
        
        // Bit 6 - F (FIRQ mask)
        flagsPanel.add(createFlagHeaderLabel("Bit 6 - F"));
        fFlagLabel = createFlagValueLabel("0");
        flagsPanel.add(fFlagLabel);
        flagsPanel.add(createFlagDescLabel("FIRQ Mask"));
        flagsPanel.add(new JLabel());
        
        // Bit 5 - H (Half carry)
        flagsPanel.add(createFlagHeaderLabel("Bit 5 - H"));
        hFlagLabel = createFlagValueLabel("0");
        flagsPanel.add(hFlagLabel);
        flagsPanel.add(createFlagDescLabel("Half Carry"));
        flagsPanel.add(new JLabel());
        
        // Bit 4 - I (IRQ mask)
        flagsPanel.add(createFlagHeaderLabel("Bit 4 - I"));
        iFlagLabel = createFlagValueLabel("0");
        flagsPanel.add(iFlagLabel);
        flagsPanel.add(createFlagDescLabel("IRQ Mask"));
        flagsPanel.add(new JLabel());
        
        // Bit 3 - N (Negative)
        flagsPanel.add(createFlagHeaderLabel("Bit 3 - N"));
        nFlagLabel = createFlagValueLabel("0");
        flagsPanel.add(nFlagLabel);
        flagsPanel.add(createFlagDescLabel("Negative"));
        flagsPanel.add(new JLabel());
        
        // Bit 2 - Z (Zero)
        flagsPanel.add(createFlagHeaderLabel("Bit 2 - Z"));
        zFlagLabel = createFlagValueLabel("0");
        flagsPanel.add(zFlagLabel);
        flagsPanel.add(createFlagDescLabel("Zero"));
        flagsPanel.add(new JLabel());
        
        // Bit 1 - V (Overflow)
        flagsPanel.add(createFlagHeaderLabel("Bit 1 - V"));
        vFlagLabel = createFlagValueLabel("0");
        flagsPanel.add(vFlagLabel);
        flagsPanel.add(createFlagDescLabel("Overflow"));
        flagsPanel.add(new JLabel());
        
        // Bit 0 - C (Carry)
        flagsPanel.add(createFlagHeaderLabel("Bit 0 - C"));
        cFlagLabel = createFlagValueLabel("0");
        flagsPanel.add(cFlagLabel);
        flagsPanel.add(createFlagDescLabel("Carry"));
        flagsPanel.add(new JLabel());
        
        ccrPanel.add(flagsPanel, BorderLayout.CENTER);
        
        // Assemble panels
        mainPanel.add(registersPanel, BorderLayout.NORTH);
        mainPanel.add(ccrPanel, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Update timer
        updateTimer = new Timer(500, e -> updateRegisterDisplay());
        updateTimer.start();
        
        // Initial display
        updateRegisterDisplay();
    }
    
    private JLabel createBoldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        return label;
    }
    
    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Monospaced", Font.BOLD, 14));
        label.setForeground(new Color(0, 100, 200));
        return label;
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
            // Update register values
            aLabel.setText(registers.getA());
            bLabel.setText(registers.getB());
            dLabel.setText(registers.getD());
            xLabel.setText(registers.getX());
            yLabel.setText(registers.getY());
            sLabel.setText(registers.getS());
            uLabel.setText(registers.getU());
            ccrLabel.setText(registers.getCCR());
            pcLabel.setText(registers.getPC());
            
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
            // Bit 7 - E
            int eBit = (ccrValue >> 7) & 1;
            updateFlagDisplay(eFlagLabel, eBit);
            
            // Bit 6 - F
            int fBit = (ccrValue >> 6) & 1;
            updateFlagDisplay(fFlagLabel, fBit);
            
            // Bit 5 - H
            int hBit = (ccrValue >> 5) & 1;
            updateFlagDisplay(hFlagLabel, hBit);
            
            // Bit 4 - I
            int iBit = (ccrValue >> 4) & 1;
            updateFlagDisplay(iFlagLabel, iBit);
            
            // Bit 3 - N (Negative)
            int nBit = (ccrValue >> 3) & 1;
            updateFlagDisplay(nFlagLabel, nBit);
            
            // Bit 2 - Z (Zero)
            int zBit = (ccrValue >> 2) & 1;
            updateFlagDisplay(zFlagLabel, zBit);
            
            // Bit 1 - V (Overflow)
            int vBit = (ccrValue >> 1) & 1;
            updateFlagDisplay(vFlagLabel, vBit);
            
            // Bit 0 - C (Carry)
            int cBit = ccrValue & 1;
            updateFlagDisplay(cFlagLabel, cBit);
        });
    }
    
    private void updateFlagDisplay(JLabel label, int value) {
        label.setText(String.valueOf(value));
        if (value == 1) {
            label.setBackground(new Color(255, 200, 200)); // Light red for set
            label.setForeground(new Color(200, 0, 0));
        } else {
            label.setBackground(new Color(240, 240, 240)); // Gray for clear
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