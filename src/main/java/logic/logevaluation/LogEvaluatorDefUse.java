package logic.logevaluation;

import logic.testcasegenerator.coveragetargets.LogNameConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LogEvaluatorDefUse extends LogEvaluator {

    final List<String> logs = new ArrayList<>();

    public LogEvaluatorDefUse(List<String> logStatements) {
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
        List<String> coveredDefs =
                logs.stream().filter(LogEvaluatorDefUse::isStatement)
                        .map(LogEvaluatorDefUse::cutDef)
                        .map(entry -> entry.split(LogNameConfiguration.USELOG_MARKER)[0])
                        .collect(Collectors.toList());
        List<String> coveredUses =
                logs.stream().filter(LogEvaluatorDefUse::isStatement).filter(entry -> entry.contains(LogNameConfiguration.USELOG_MARKER)).
                        map(entry -> entry.split(LogNameConfiguration.USELOG_MARKER)[1]).map(entry -> LogNameConfiguration.USELOG_MARKER + entry).collect(Collectors.toList());

        Map<String, Integer> unitsCovered = countNumberOfOccurrences(coveredDefs);
        Map<String, Integer> unitsCoveredUses = countNumberOfOccurrences(coveredUses);
        unitsCovered.putAll(unitsCoveredUses);
        return unitsCovered;
    }
    private static String cutDef(String logStatement) {
        String shortenedLogStatement = logStatement.substring(
                logStatement.indexOf(LogNameConfiguration.LOGDELIMITER + LogNameConfiguration.LOGDELIMITER)
                        + LogNameConfiguration.LOGDELIMITER.length());
        return shortenedLogStatement.contains(LogNameConfiguration.DEFLOG_MARKER) ? shortenedLogStatement : logStatement;
    }
    @Override
    public String getCriteriaName() {
        return "All DefUse";
    }

}
