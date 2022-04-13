package shared.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.ArrayList;
import java.util.List;

public final class Function {
    private final String name;
    private final String parameter;
    private final List<String> results = new ArrayList<>();
    private BooleanProperty passed = new SimpleBooleanProperty();
    public Function(String name, String parameter) {
        this.name = name;
        this.parameter = parameter;
    }

    public BooleanProperty isPassed() {
        return passed;
    }

    public BooleanProperty passedProperty() {
        return passed;
    }

    public String getName() {
        return name;
    }

    public String getParameter() {
        return parameter;
    }

    public void addResults(List<String> results){
       results.addAll(results);
    }

    public List<String> getResults() {
        return List.copyOf(results);
    }
}
