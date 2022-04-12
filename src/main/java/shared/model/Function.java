package shared.model;

import java.util.ArrayList;
import java.util.List;

public final class Function {
    private final String name;
    private final String parameter;
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

    public void addResults(List<String> results){
        results.addAll(results);
    }

    public List<String> getResults() {
        return List.copyOf(results);
    }
}
