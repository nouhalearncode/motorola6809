package processeur;

import java.util.*;

public class StateManager {
    private ArrayList<ExecutionState> stateHistory = new ArrayList<>();
    private int currentStateIndex = -1;
    
    // Public inner class so pas.java can access it
    public static class ExecutionState {
        public Map<String, String> ramState;
        public Map<String, String> romState;
        public Map<String, String> registerState;
        public int romWritePointer;
        public int currentStep;
        public String description;
        
        public ExecutionState(Map<String, String> ram, Map<String, String> rom, 
                             Map<String, String> reg, int romPtr, int step, String desc) {
            // Deep copy of RAM
            this.ramState = new LinkedHashMap<>();
            for (Map.Entry<String, String> entry : ram.entrySet()) {
                this.ramState.put(entry.getKey(), entry.getValue());
            }
            
            // Deep copy of ROM
            this.romState = new LinkedHashMap<>();
            for (Map.Entry<String, String> entry : rom.entrySet()) {
                this.romState.put(entry.getKey(), entry.getValue());
            }
            
            // Deep copy of registers
            this.registerState = new LinkedHashMap<>(reg);
            this.romWritePointer = romPtr;
            this.currentStep = step;
            this.description = desc;
        }
    }
    
    // Save current state
    public void saveState(Map<String, String> ram, Map<String, String> rom, 
                         Map<String, String> registers, int romWritePointer, 
                         int currentStep, String description) {
        
        // Remove future states if we're going back and then executing new steps
        if (currentStateIndex < stateHistory.size() - 1) {
            stateHistory = new ArrayList<>(stateHistory.subList(0, currentStateIndex + 1));
        }
        
        ExecutionState state = new ExecutionState(ram, rom, registers, romWritePointer, currentStep, description);
        stateHistory.add(state);
        currentStateIndex++;
        
        System.out.println("[StateManager] State saved: " + description + " (Index: " + currentStateIndex + ")");
    }
    
    // Check if we can go back
    public boolean canGoBack() {
        return currentStateIndex > 0;
    }
    
    // Check if we can go forward
    public boolean canGoForward() {
        return currentStateIndex < stateHistory.size() - 1;
    }
    
    // Go back one state
    public ExecutionState goBack() {
        if (!canGoBack()) {
            return null;
        }
        currentStateIndex--;
        return stateHistory.get(currentStateIndex);
    }
    
    // Go forward one state
    public ExecutionState goForward() {
        if (!canGoForward()) {
            return null;
        }
        currentStateIndex++;
        return stateHistory.get(currentStateIndex);
    }
    
    // Get current state index
    public int getCurrentStateIndex() {
        return currentStateIndex;
    }
    
    // Get total states
    public int getTotalStates() {
        return stateHistory.size();
    }
    
    // Get state description
    public String getCurrentStateDescription() {
        if (currentStateIndex >= 0 && currentStateIndex < stateHistory.size()) {
            return stateHistory.get(currentStateIndex).description;
        }
        return "Initial State";
    }
    
    // Clear all states
    public void clear() {
        stateHistory.clear();
        currentStateIndex = -1;
    }
}