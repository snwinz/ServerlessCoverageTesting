package shared.model;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class Function {
    private String name;
    private String parameter;
    private final List<String> results = new ArrayList<>();

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

    public List<String> getResults() {
        return List.copyOf(results);
    }

    public void setResults(String newValue) {
        var parts = newValue.split("\\*");
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            if (part.endsWith("\\")) {
                parts[i + 1] = part.substring(0, part.length() - 1) + "*" + parts[i + 1];
                parts[i] = null;
            }
        }
        results.clear();
        results.addAll(Arrays.stream(parts).filter(o -> o != null).collect(Collectors.toList()));
    }


    public void setFunctionName(String functionName) {
        this.name = functionName;
    }

    public void setFunctionParameter(String value) {
        this.parameter = value;
    }
}
