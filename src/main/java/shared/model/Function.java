package shared.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.List;

public final class Function {
    private final String name;
    private final String parameter;
    private final List<String> results = new ArrayList<>();
    private final BooleanProperty passed = new SimpleBooleanProperty();
    private final StringProperty output = new SimpleStringProperty("");

    public Function(String name, String parameter) {
        this.name = name;
        this.parameter = parameter;
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

    public void addResults(List<String> results) {
        results.addAll(results);
    }

    public List<String> getResults() {
        return List.copyOf(results);
    }

    public void addTextToOutput(String text) {
        if (output != null) {
            if (!output.get().isEmpty()) {
                text = output.get() + "\n" + text;
            }
            output.set(text);
        }
    }


    public StringProperty outputProperty() {
        return output;
    }
}
