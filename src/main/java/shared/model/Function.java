package shared.model;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class Function {
    private String name;
    private String parameter;
    private final List<String> expectedOutputs = new ArrayList<>();

    public Function(String name, String parameter) {
        this.name = name;
        this.parameter = parameter;
    }


    public String getName() {
        return name;
    }

    public String getParameter() {
        return parameter;
    }

    public List<String> getExpectedOutputs() {
        return List.copyOf(expectedOutputs);
    }

    public void setExpectedOutputs(String newValue) {
        var parts = newValue.split("\\*");
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            if (part.endsWith("\\")) {
                parts[i + 1] = part.substring(0, part.length() - 1) + "*" + parts[i + 1];
                parts[i] = null;
            }
        }
        expectedOutputs.clear();
        expectedOutputs.addAll(Arrays.stream(parts).filter(Objects::nonNull).toList());
    }


    public void setExpectedOutputs(List<String> outputs){
        expectedOutputs.clear();
        expectedOutputs.addAll(outputs);
    }

    public void setFunctionName(String functionName) {
        this.name = functionName;
    }

    public void setFunctionParameter(String value) {
        this.parameter = value;
    }
}
