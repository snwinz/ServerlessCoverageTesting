package logic.model;

import com.google.gson.annotations.Expose;
import shared.model.input.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FunctionInputFormat {
    @Expose
    private List<GeneralInput> generalInputs;

    public FunctionInputFormat(List<GeneralInput> generalInputs) {
        Objects.requireNonNull(generalInputs, "string input list must not null");
        this.generalInputs = generalInputs;
        this.generalInputs = getTypedGeneralInputs();
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

    public void updateTypesOfGeneralInputs() {
        this.generalInputs = getTypedGeneralInputs();
    }

    public List<GeneralInput> getTypedGeneralInputs() {
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
                continue;
            }
            if (generalInput.getKey() == null) {
                typedInputs.add(new ConstantValue(generalInput));
                continue;
            }
            if (generalInput.getKey() != null && generalInput.getConstantValue() != null) {
                typedInputs.add(new ConstantKeyValue(generalInput));
            }
        }
        return typedInputs;
    }

    public void recalculate() {
        for (var inputData : generalInputs) {
            inputData.calculateNewValues();
        }
    }

    public String getInputAsJson() {
        StringBuilder inputString = new StringBuilder();
        inputString.append('{');
        var rootEntry = generalInputs.stream().filter(item -> item.getParentId() == null && !item.isUndefined())
                .map(item -> item.getJsonWithData(generalInputs)).collect(Collectors.joining(","));
        inputString.append(rootEntry);
        inputString.append('}');
        return inputString.toString();
    }
}
