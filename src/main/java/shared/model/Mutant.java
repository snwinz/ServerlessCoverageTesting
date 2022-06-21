package shared.model;

public class Mutant {
    private final MutationType mutationType;
    private final String location;
    private String variable;
    private String value;
    private boolean isInputValue;

    public Mutant(MutationType mutationType, String location) {
        this.mutationType = mutationType;
        this.location = location;
    }

    public MutationType getMutationType() {
        return mutationType;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setIsInputValue(boolean isInputValue) {
        this.isInputValue = isInputValue;
    }
}
