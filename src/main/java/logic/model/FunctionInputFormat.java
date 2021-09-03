package logic.model;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FunctionInputFormat {
    @Expose
    private final List<StringInput> stringInput;
    @Expose
    private final List<IntegerInput> integerInput;
    @Expose
    private final String JSONInput;

    public FunctionInputFormat(List<StringInput> stringInput, List<IntegerInput> integerInput, String JSONInput) {
        Objects.requireNonNull(stringInput, "string input list must not null");
        Objects.requireNonNull(integerInput, "string input list must not null");
        Objects.requireNonNull(JSONInput, "string input list must not null");

        this.stringInput = stringInput;
        this.integerInput = integerInput;
        this.JSONInput = JSONInput;
    }

    public FunctionInputFormat() {
        stringInput = new ArrayList<>();
        integerInput = new ArrayList<>();
        JSONInput ="";
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
}
