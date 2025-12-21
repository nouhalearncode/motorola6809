package processeur;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

public class RAMDisplayFrame extends JFrame {
    private JTable ramTable;
    private DefaultTableModel tableModel;
    private ram ramMemory;
    private JComboBox<String> rangeSelector;
    
    public RAMDisplayFrame(ram ramMemory) {
        this.ramMemory = ramMemory;
        setTitle("RAM Viewer - 0000 to FFFF (Double-click to edit)");
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
        rangeSelector.addActionListener(e -> updateRAMDisplay());
        
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Select Range:"));
        topPanel.add(rangeSelector);
        
        JLabel instructionLabel = new JLabel("💡 Double-click any value to edit");
        instructionLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        instructionLabel.setForeground(new Color(100, 100, 100));
        topPanel.add(instructionLabel);
        
        // Create table with columns: Address, Value
        String[] columnNames = {"Address", "Value"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Only Value column is editable
            }
        };
        
        ramTable = new JTable(tableModel);
        ramTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ramTable.setRowHeight(20);
        ramTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Center align the table cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        ramTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        ramTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        
        // Set column widths
        ramTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        ramTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        
        // Add custom editor for Value column with validation
        ramTable.getColumnModel().getColumn(1).setCellEditor(new HexCellEditor());
        
        // Add listener to update backend when cell is edited
        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                
                if (column == 1) { // Value column
                    String address = (String) tableModel.getValueAt(row, 0);
                    String newValue = (String) tableModel.getValueAt(row, 1);
                    
                    // Validate and update backend
                    if (isValidHex(newValue)) {
                        String formattedValue = formatHexValue(newValue);
                        ramMemory.getram().put(address, formattedValue);
                        tableModel.setValueAt(formattedValue, row, 1); // Update display
                        System.out.println("[RAM EDIT] " + address + " <- " + formattedValue);
                    } else {
                        // Revert to old value if invalid
                        String oldValue = ramMemory.getram().get(address);
                        tableModel.setValueAt(oldValue, row, 1);
                        JOptionPane.showMessageDialog(this,
                            "Invalid hex value! Please enter 2 hex digits (00-FF)",
                            "Invalid Input",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(ramTable);
        
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        // Initial display
        updateRAMDisplay();
    }
    
    private void setLogo() {
        try {
            ImageIcon logo = new ImageIcon("logoprincipale.png");
            if (logo.getIconWidth() > 0) {
                setIconImage(logo.getImage());
            }
        } catch (Exception e) {
            System.out.println("[RAMFrame] Logo not found: " + e.getMessage());
        }
    }
    
    public void updateRAMDisplay() {
        int selectedRange = rangeSelector.getSelectedIndex();
        int startAddr = selectedRange * 4096;
        int endAddr = startAddr + 4096;
        
        // Clear existing data
        tableModel.setRowCount(0);
        
        // Add rows for the selected range
        for (int addr = startAddr; addr < endAddr; addr++) {
            String hexAddr = String.format("%04X", addr);
            String value = ramMemory.getram().get(hexAddr);
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
    
    // Custom cell editor with hex validation
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