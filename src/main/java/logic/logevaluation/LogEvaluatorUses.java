package logic.logevaluation;

import logic.testcasegenerator.coveragetargets.LogNameConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LogEvaluatorUses extends LogEvaluator {

    final List<String> logs = new ArrayList<>();

    public LogEvaluatorUses(List<String> logStatements) {
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
        List<String> coveredUnits =
                logs.stream().filter(s -> s.startsWith(LogNameConfiguration.DEFLOG_MARKER)).collect(Collectors.toList());

        return countNumberOfOccurrences(coveredUnits);
    }

    @Override
    public String getCriteriaName() {
        return "All Uses";
    }

}
