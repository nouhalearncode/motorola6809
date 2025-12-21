package processeur;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.TreeMap;

public class RAMDisplayFrame extends JFrame {
    private JTextArea ramTextArea;
    private ram ramMemory;
    private JComboBox<String> rangeSelector;
    
    public RAMDisplayFrame(ram ramMemory) {
        this.ramMemory = ramMemory;
        setTitle("RAM Viewer - 0000 to FFFF");
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Range selector
        String[] ranges = new String[16];
        for (int i = 0; i < 16; i++) {
            int start = i * 4096; // 4096 addresses per range
            int end = start + 4095;
            ranges[i] = String.format("%04X-%04X", start, end);
        }
        rangeSelector = new JComboBox<>(ranges);
        rangeSelector.addActionListener(e -> updateRAMDisplay());
        
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Select Range:"));
        topPanel.add(rangeSelector);
        
        // Create text area
        ramTextArea = new JTextArea();
        ramTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ramTextArea.setEditable(false);
        
        JScrollPane scrollPane = new JScrollPane(ramTextArea);
        
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        // Initial display
        updateRAMDisplay();
    }
    
    public void updateRAMDisplay() {
        int selectedRange = rangeSelector.getSelectedIndex();
        int startAddr = selectedRange * 4096;
        int endAddr = startAddr + 4096;
        
        StringBuilder sb = new StringBuilder();
        sb.append("RAM Addresses ").append(String.format("%04X-%04X", startAddr, endAddr-1)).append("\n");
        sb.append("==============\n");
        sb.append("Addr Value\n");
        sb.append("---- -----\n");
        
        Map<String, String> memory = ramMemory.getram();
        
        // Display each address on its own line
        for (int addr = startAddr; addr < endAddr; addr++) {
            String hexAddr = String.format("%04X", addr);
            String value = memory.get(hexAddr);
            if (value == null) value = "00";
            sb.append(String.format("%04X %s\n", addr, value));
        }
        
        ramTextArea.setText(sb.toString());
    }
}