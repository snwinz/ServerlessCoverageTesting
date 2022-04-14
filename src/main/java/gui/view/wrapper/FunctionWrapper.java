package gui.view.wrapper;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import shared.model.Function;

import java.util.Arrays;
import java.util.stream.Collectors;

public class FunctionWrapper {
    private  Function function;
    private final StringProperty output = new SimpleStringProperty("");
    private final BooleanProperty passed = new SimpleBooleanProperty();

    public FunctionWrapper(Function function) {
        this.function = function;
    }
    public BooleanProperty passedProperty() {
        return passed;
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
