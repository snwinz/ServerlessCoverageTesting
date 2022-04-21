package gui.view.wrapper;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import shared.model.Function;

public class FunctionWrapper {
    private final Function function;
    private final StringProperty output = new SimpleStringProperty("");
    private final StringProperty expectedResult = new SimpleStringProperty("");
    private final BooleanProperty passed = new SimpleBooleanProperty();
    private final BooleanProperty executed = new SimpleBooleanProperty();

    public BooleanProperty executedProperty() {
        return executed;
    }

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
        if (!output.get().isEmpty()) {
            text = output.get() + "\n" + text;
        }
        output.set(text);
    }

    public StringProperty outputProperty() {
        return output;
    }

    public StringProperty expectedResultProperty() {
        return expectedResult;
    }

     public void reset() {
        this.output.set("");
        this.passed.set(false);
        this.executed.set(false);
    }
}
