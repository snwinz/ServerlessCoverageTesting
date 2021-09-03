package gui.model;

import com.google.gson.annotations.Expose;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FunctionInputFormat {
    @Expose
    private List<StringInput> stringInput;
    @Expose
    private List<IntegerInput> integerInput;
    @Expose
    private String JSONInput;

    final StringProperty text = new SimpleStringProperty();

    public FunctionInputFormat(List<StringInput> stringInput, List<IntegerInput> integerInput, String JSONInput) {
        Objects.requireNonNull(stringInput, "string input list must not null");
        Objects.requireNonNull(integerInput, "string input list must not null");
        Objects.requireNonNull(JSONInput, "string input list must not null");

        this.stringInput = stringInput;
        this.integerInput = integerInput;
        this.JSONInput = JSONInput;
        text.set(this.toString());
    }

    public FunctionInputFormat() {
        stringInput = new ArrayList<>();
        integerInput = new ArrayList<>();
        JSONInput = "";
    }

    public List<StringInput> getStringInput() {
        return stringInput;
    }

    public List<IntegerInput> getIntegerInput() {
        return integerInput;
    }

    public String getJSONInput() {
        return JSONInput;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (var stringInputEntry : stringInput) {
            result.append(stringInputEntry);
            result.append("\n");
        }

        for (var integerInputEntry : integerInput) {
            result.append(integerInputEntry);
            result.append("\n");
        }

        result.append(this.JSONInput);

        return result.toString();
    }

    public FunctionInputFormat getCopy() {


        List<IntegerInput> integerInputCopy = new ArrayList<>();
        for (var integerInputEntry : integerInput) {
            var entry = new IntegerInput(integerInputEntry.getKey(), integerInputEntry.getMinValue(), integerInputEntry.getMaxValue());
            integerInputCopy.add(entry);
        }
        List<StringInput> stringInputCopy = new ArrayList<>();
        for (var stringInputEntry : stringInput) {
            var entry = new StringInput(stringInputEntry.getKey(), stringInputEntry.getJsonValue());
            stringInputCopy.add(entry);
        }
        return new FunctionInputFormat(stringInputCopy, integerInputCopy, this.JSONInput);
    }

    public void setStringInput(List<StringInput> stringInput) {
        this.stringInput = stringInput;
        text.set(this.toString());
    }

    public void setIntegerInput(List<IntegerInput> integerInput) {
        this.integerInput = integerInput;
        text.set(this.toString());
    }

    public void setJSONInput(String JSONInput) {
        this.JSONInput = JSONInput;
        text.set(this.toString());
    }

    public ObservableValue<String> textProperty() {
        return text;
    }

    public void addStringInputValue(StringInput stringInputValue) {
        if (stringInput != null) {
            stringInput.add(stringInputValue);
        }
    }

    public void delete(StringInput item) {
        if (stringInput != null) {
            stringInput.remove(item);
        }
    }
    public void delete(IntegerInput item) {
        if (integerInput != null) {
            integerInput.remove(item);
        }
    }

    public void addIntegerInputValue(IntegerInput item) {
        if (integerInput != null) {
            integerInput.add(item);
        }
    }
}

