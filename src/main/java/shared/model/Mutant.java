package shared.model;

import java.util.Objects;

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

    public String getLocation() {
        return location;
    }

    public void setIsInputValue(boolean isInputValue) {
        this.isInputValue = isInputValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mutant mutant = (Mutant) o;
        return isInputValue == mutant.isInputValue && mutationType == mutant.mutationType && Objects.equals(location, mutant.location) && Objects.equals(variable, mutant.variable) && Objects.equals(value, mutant.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mutationType, location, variable, value, isInputValue);
    }
}
