package processeur;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

public class ROMDisplayFrame extends JFrame {
    private JTable romTable;
    private DefaultTableModel tableModel;
    private ROM romMemory;
    private JComboBox<String> rangeSelector;
    
    public ROMDisplayFrame(ROM romMemory) {
        this.romMemory = romMemory;
        setTitle("ROM Viewer - 0000 to FFFF (Double-click to edit)");
        setSize(500, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Set logo
        setLogo();
        
        // Range selector
        String[] ranges = new String[16];
        for (int i = 0; i < 16; i++) {
            int start = i * 4096;
            int end = start + 4095;
            ranges[i] = String.format("%04X-%04X", start, end);
        }
        rangeSelector = new JComboBox<>(ranges);
        rangeSelector.addActionListener(e -> updateROMDisplay());
        
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Select Range:"));
        topPanel.add(rangeSelector);
        
        JLabel instructionLabel = new JLabel("💡 Double-click any value to edit");
        instructionLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        instructionLabel.setForeground(new Color(100, 100, 100));
        topPanel.add(instructionLabel);
        
        // Create table
        String[] columnNames = {"Address", "Value"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Only Value column is editable
            }
        };
        
        romTable = new JTable(tableModel);
        romTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        romTable.setRowHeight(20);
        romTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Center align
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        romTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        romTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        
        // Set column widths
        romTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        romTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        
        // Add custom editor
        romTable.getColumnModel().getColumn(1).setCellEditor(new HexCellEditor());
        
        // Add listener to update backend
        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                
                if (column == 1) {
                    String address = (String) tableModel.getValueAt(row, 0);
                    String newValue = (String) tableModel.getValueAt(row, 1);
                    
                    if (isValidHex(newValue)) {
                        String formattedValue = formatHexValue(newValue);
                        romMemory.getMemory().put(address, formattedValue);
                        tableModel.setValueAt(formattedValue, row, 1);
                        System.out.println("[ROM EDIT] " + address + " <- " + formattedValue);
                    } else {
                        String oldValue = romMemory.getMemory().get(address);
                        tableModel.setValueAt(oldValue, row, 1);
                        JOptionPane.showMessageDialog(this,
                            "Invalid hex value! Please enter 2 hex digits (00-FF)",
                            "Invalid Input",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(romTable);
        
        JButton refreshButton = new JButton("🔄 Refresh");
        refreshButton.addActionListener(e -> updateROMDisplay());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);
        
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        updateROMDisplay();
    }
    
    private void setLogo() {
        try {
            ImageIcon logo = new ImageIcon("logoprincipale.png");
            if (logo.getIconWidth() > 0) {
                setIconImage(logo.getImage());
            }
        } catch (Exception e) {
            System.out.println("[ROMFrame] Logo not found: " + e.getMessage());
        }
    }
    
    public void updateROMDisplay() {
        int selectedRange = rangeSelector.getSelectedIndex();
        int startAddr = selectedRange * 4096;
        int endAddr = startAddr + 4096;
        
        tableModel.setRowCount(0);
        
        for (int addr = startAddr; addr < endAddr; addr++) {
            String hexAddr = String.format("%04X", addr);
            String value = romMemory.getMemory().get(hexAddr);
            if (value == null) value = "00";
            
            tableModel.addRow(new Object[]{hexAddr, value});
        }
    }
    
    private boolean isValidHex(String value) {
        if (value == null || value.isEmpty()) return false;
        if (value.length() > 2) return false;
        return value.matches("[0-9A-Fa-f]+");
    }
    
    private String formatHexValue(String value) {
        value = value.toUpperCase();
        if (value.length() == 1) {
            value = "0" + value;
        }
        return value;
    }
    
    class HexCellEditor extends DefaultCellEditor {
        private JTextField textField;
        
        public HexCellEditor() {
            super(new JTextField());
            textField = (JTextField) getComponent();
            textField.setHorizontalAlignment(JTextField.CENTER);
        }
        
        @Override
        public boolean stopCellEditing() {
            String value = textField.getText();
            if (isValidHex(value)) {
                return super.stopCellEditing();
            } else {
                textField.setBorder(BorderFactory.createLineBorder(Color.RED));
                return false;
            }
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            textField.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }
    }
}