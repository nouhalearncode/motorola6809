package processeur;

// BackendManager.java


import java.util.ArrayList;
import javax.swing.*;

public class BackendManager {
    private static BackendManager instance;
    private ROM rom;
    private ram ram;
    private registre reg;
    private mode modeDetector;
    private ArrayList<ArrayList<String>> assemblyLines;
    
    private BackendManager() {}
    
    public static BackendManager getInstance() {
        if (instance == null) {
            instance = new BackendManager();
        }
        return instance;
    }
    
    public void initialize(ROM rom, ram ram, registre reg, mode modeDetector) {
        this.rom = rom;
        this.ram = ram;
        this.reg = reg;
        this.modeDetector = modeDetector;
    }
    
    public void executeStep() {
        // Call your pas.java to execute one step
        // Then notify GUI components to update
    }
    
    // Getters
    public ROM getROM() { return rom; }
    public ram getRAM() { return ram; }
    public registre getRegisters() { return reg; }
    
    // Method to update GUI after execution
    public void notifyGUIUpdate() {
        // This would notify all registered GUI components
    }
}