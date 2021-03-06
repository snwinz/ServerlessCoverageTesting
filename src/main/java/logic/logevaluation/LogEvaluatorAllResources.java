package logic.logevaluation;


import logic.testcasegenerator.coveragetargets.LogNameConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LogEvaluatorAllResources extends LogEvaluator {

    final List<String> logs = new ArrayList<>();

    public LogEvaluatorAllResources(List<String> logStatements) {
        for (String statement : logStatements) {
            if (isStatement(statement)) {
                logs.add(statement);
            }
        }
    }

    private boolean isStatement(String statement) {
        return statement.startsWith(LogNameConfiguration.RESOURCE_MARKER);
    }

    @Override
    public Map<String, Integer> getCoveredResources() {
        List<String> coveredResources =
                logs.stream().filter(s -> s.startsWith(LogNameConfiguration.RESOURCE_MARKER)).map(a -> a.replaceAll(LogNameConfiguration.RELATION_MARKER, "")).collect(Collectors.toList());

        return countNumberOfOccurrences(coveredResources);
    }

    @Override
    public String getCriteriaName() {
        return "All Resources";
    }

}
