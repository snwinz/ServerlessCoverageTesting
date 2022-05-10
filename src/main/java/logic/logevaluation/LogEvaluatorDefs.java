package logic.logevaluation;

import logic.testcasegenerator.coveragetargets.LogNameConfiguration;

import java.util.*;
import java.util.stream.Collectors;


public class LogEvaluatorDefs extends LogEvaluator {

    final List<String> logs = new ArrayList<>();

    public LogEvaluatorDefs(List<String> logStatements) {
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
        List<String> coveredResources =
                logs.stream().filter(LogEvaluatorDefs::isStatement)
                        .map(LogEvaluatorDefs::cutDef)
                        .map(entry -> entry.split(LogNameConfiguration.USELOG_MARKER)[0])
                        .collect(Collectors.toList());
        return countNumberOfOccurrences(coveredResources);
    }

    private static String cutDef(String logStatement) {
        String shortenedLogStatement = logStatement.substring(
                logStatement.indexOf(LogNameConfiguration.LOGDELIMITER + LogNameConfiguration.LOGDELIMITER)
                        + LogNameConfiguration.LOGDELIMITER.length());
        return shortenedLogStatement.contains(LogNameConfiguration.DEFLOG_MARKER) ? shortenedLogStatement : logStatement;
    }

    @Override
    public String getCriteriaName() {
        return "All Defs";
    }

}
