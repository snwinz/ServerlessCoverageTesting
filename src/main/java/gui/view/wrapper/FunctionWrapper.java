package gui.view.wrapper;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import shared.model.Function;

public class FunctionWrapper {
    private final Function function;
    private final StringProperty output = new SimpleStringProperty("");

    public FunctionWrapper(Function function) {
        this.function = function;
    }

    public Function getFunction() {
        return function;
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
