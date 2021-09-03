package gui.model;

public enum NodeType {
    STANDARD_NODE("Standard Node"), FUNCTION("Function"), DATA_STORAGE("Data Storage");
    private final String label;

    NodeType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
