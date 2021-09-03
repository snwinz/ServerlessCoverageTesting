package logic.logevaluation;

import logic.testcasegenerator.coveragetargets.LogNameConfiguration;

import java.util.*;
import java.util.stream.Collectors;


public class LogEvaluatorDefs extends LogEvaluator {

    List<String> logs = new ArrayList<>();

    public LogEvaluatorDefs(List<String> logStatements) {
        for (String statement : logStatements) {
            if (isStatement(statement)) {
                logs.add(statement);
            }
        }
    }

    private boolean isStatement(String statement) {
        return statement.startsWith(LogNameConfiguration.DEFLOG_MARKER);
    }

    public Map<String, Integer> getCoveredResources() {
        List<String> coveredResources =
                logs.stream().filter(s -> s.startsWith(LogNameConfiguration.DEFLOG_MARKER)).map(entry-> entry.split(LogNameConfiguration.USELOG_MARKER)[0]).collect(Collectors.toList());
        Map<String, Integer> unitsCovered = countNumberOfOccurrences(coveredResources);
        return unitsCovered;
    }

    @Override
    public String getCriteriaName() {
        return "All Defs";
    }

}
