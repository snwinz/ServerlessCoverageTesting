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

    private static boolean isStatement(String statement) {
        return statement.startsWith(LogNameConfiguration.DEFLOG_MARKER) && !statement.contains("undefined");
    }

    public Map<String, Integer> getUnitsCovered() {
        List<String> coveredUnits =
                logs.stream().filter(LogEvaluatorUses::isStatement)
                        .map(LogEvaluatorUses::cutDef)
                        .collect(Collectors.toList());
        return countNumberOfOccurrences(coveredUnits);
    }

    private static String cutDef(String logStatement) {
        String shortenedLogStatement = logStatement.substring(
                logStatement.indexOf(LogNameConfiguration.LOGDELIMITER + LogNameConfiguration.LOGDELIMITER)
                        + LogNameConfiguration.LOGDELIMITER.length());
        return shortenedLogStatement.contains(LogNameConfiguration.DEFLOG_MARKER) ? shortenedLogStatement : logStatement;
    }

    @Override
    public String getCriteriaName() {
        return "All Uses";
    }

}
