package processeur;

// RegisterDisplayFrame.java
import javax.swing.*;
import java.awt.*;
import processeur.registre;

public class RegisterDisplayFrame extends JFrame {
    private JLabel aLabel, bLabel, dLabel, xLabel, yLabel;
    private JLabel sLabel, uLabel, ccrLabel, pcLabel;
    private registre registers;
    private Timer updateTimer;
    
    public RegisterDisplayFrame(registre registers) {
        this.registers = registers;
        setTitle("Register Viewer");
        setSize(300, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(10, 2, 5, 5));
        
        // Create labels
        add(new JLabel("A (Accumulator):"));
        aLabel = new JLabel("00");
        add(aLabel);
        
        add(new JLabel("B (Accumulator):"));
        bLabel = new JLabel("00");
        add(bLabel);
        
        add(new JLabel("D (A:B):"));
        dLabel = new JLabel("0000");
        add(dLabel);
        
        add(new JLabel("X (Index):"));
        xLabel = new JLabel("0000");
        add(xLabel);
        
        add(new JLabel("Y (Index):"));
        yLabel = new JLabel("0000");
        add(yLabel);
        
        add(new JLabel("S (Stack Pointer):"));
        sLabel = new JLabel("0000");
        add(sLabel);
        
        add(new JLabel("U (User Stack):"));
        uLabel = new JLabel("0000");
        add(uLabel);
        
        add(new JLabel("CCR (Flags):"));
        ccrLabel = new JLabel("00");
        add(ccrLabel);
        
        add(new JLabel("PC (Program Counter):"));
        pcLabel = new JLabel("0000");
        add(pcLabel);
        
        // Update timer (updates every 500ms)
        updateTimer = new Timer(500, e -> updateRegisterDisplay());
        updateTimer.start();
        
        // Initial display
        updateRegisterDisplay();
    }
    
    public void updateRegisterDisplay() {
        SwingUtilities.invokeLater(() -> {
            aLabel.setText(registers.getA());
            bLabel.setText(registers.getB());
            dLabel.setText(registers.getD());
            xLabel.setText(registers.getX());
            yLabel.setText(registers.getY());
            sLabel.setText(registers.getS());
            uLabel.setText(registers.getU());
            ccrLabel.setText(registers.getCCR());
            pcLabel.setText(registers.getPC());
        });
    }
    
    @Override
    public void dispose() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
        super.dispose();
    }
}
