package logic.logevaluation;

import logic.model.LogicGraph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class LogEvaluator {

    abstract public Map<String, Integer> getUnitsCovered();


    public Map<String, Integer> countNumberOfOccurrences(List<String> inputList) {
        Map<String, Integer> resultMap = new HashMap<>();
        inputList.forEach(e -> resultMap.put(e, resultMap.getOrDefault(e, 0) + 1));
        return resultMap;
    }

    abstract public String getCriteriaName();

    public abstract List<String> getTargets(LogicGraph graph);
}
