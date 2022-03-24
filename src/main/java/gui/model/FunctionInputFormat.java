package gui.model;

import com.google.gson.annotations.Expose;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import shared.model.input.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FunctionInputFormat {

    @Expose
    private List<GeneralInput> generalInputs;

    final StringProperty text = new SimpleStringProperty();

    public FunctionInputFormat(List<GeneralInput> generalInput) {
        Objects.requireNonNull(generalInput, "string input list must not null");
        this.generalInputs = generalInput;
        text.set(this.toString());
    }

    public FunctionInputFormat() {
        generalInputs = new ArrayList<>();

    }

    public List<GeneralInput> getGeneralInputs() {
        return generalInputs;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (var stringInputEntry : generalInputs) {
            result.append(stringInputEntry);
            result.append("\n");
        }
        return result.toString();
    }

    public String getJSON() {
        StringBuilder result = new StringBuilder();
        result.append('{');
        var rootEntry = generalInputs.stream().filter(item -> item.getParentId() == null)
                .map(item -> item.getJsonFormat(generalInputs)).collect(Collectors.joining(","));
        result.append(rootEntry);
        result.append('}');

        return result.toString();
    }

    public String getJSONWithContent() {
        generalInputs.forEach(GeneralInput::calculateNewValues);
        StringBuilder result = new StringBuilder();
        result.append('{');
        var rootEntry = generalInputs.stream().filter(item -> item.getParentId() == null)
                .map(item -> item.getJsonWithData(generalInputs)).collect(Collectors.joining(","));
        result.append(rootEntry);
        result.append('}');

        return result.toString();
    }

    public FunctionInputFormat getCopy() {
        List<GeneralInput> generalInputCopy = new ArrayList<>();
        for (var stringInputEntry : generalInputs) {
            var entry = stringInputEntry.getCopy();
            generalInputCopy.add(entry);
        }
        return new FunctionInputFormat(generalInputCopy);
    }

    public void setStringInput(List<GeneralInput> generalInput) {
        this.generalInputs = generalInput;
        text.set(this.toString());
    }

    public ObservableValue<String> textProperty() {
        return text;
    }

    public void addGeneralInputValue(GeneralInput generalInputValue) {
        if (generalInputs != null) {
            generalInputs.add(generalInputValue);
        }
    }

    public void delete(GeneralInput item) {
        if (generalInputs != null) {
            generalInputs.remove(item);
        }
    }

    public void updateTypes() {
        List<GeneralInput> typedInputs = new ArrayList<>();

        for (var generalInput : generalInputs) {
            if (generalInput.getParentNode() != null && generalInput.getParentNode() && generalInput.getJsonSavedAsString() != null) {
                typedInputs.add(new ParentKeyInput(generalInput));
                continue;
            }
            if (generalInput.getArrayNode() != null && generalInput.getArrayNode()) {
                typedInputs.add(new ArrayKeyInput(generalInput));
                continue;
            }
            if (generalInput.getMaxValue() != null) {
                typedInputs.add(new IntegerInput(generalInput));
                continue;
            }
            if (generalInput.getDynamicValue() != null) {
                typedInputs.add(new DynamicKeyValue(generalInput));
            }
            if (generalInput.getKey() == null) {
                typedInputs.add(new ConstantValue(generalInput));
                continue;
            }
            if (generalInput.getKey() != null && generalInput.getConstantValue() != null) {
                typedInputs.add(new ConstantKeyValue(generalInput));
            }
        }
        this.generalInputs = typedInputs;
    }
}