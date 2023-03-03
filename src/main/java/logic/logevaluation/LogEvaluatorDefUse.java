package logic.logevaluation;

import logic.model.LogicGraph;
import logic.testcasegenerator.TargetGenerator;
import logic.testcasegenerator.coveragetargets.CoverageTargetAllDefUse;
import logic.testcasegenerator.coveragetargets.LogNameConfiguration;
import logic.testcasegenerator.coveragetargets.coverageelements.FunctionWithSourceLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        return statement.contains(LogNameConfiguration.DEFLOG_MARKER)
                && statement.contains(LogNameConfiguration.USELOG_MARKER);
    }

    public Map<String, Integer> getUnitsCovered() {
        List<String> coveredDefs =
                logs.stream()
                        .map(LogEvaluatorDefUse::cutDef)
                        .map(entry -> entry.split(LogNameConfiguration.USELOG_MARKER)[0])
                        .map(entry -> entry.replaceAll("#", ""))
                        .toList();
        List<String> coveredUses =
                logs.stream().filter(entry -> entry.contains(LogNameConfiguration.USELOG_MARKER)).
                        map(entry -> entry.substring(entry.indexOf(LogNameConfiguration.USELOG_MARKER)))
                        .map(entry -> entry.replaceAll("#", "")).toList();

        Map<String, Integer> unitsCovered = countNumberOfOccurrences(coveredDefs);
        Map<String, Integer> unitsCoveredUses = countNumberOfOccurrences(coveredUses);
        unitsCovered.putAll(unitsCoveredUses);
        return unitsCovered;
    }

    private static String cutDef(String logStatement) {
        if (logStatement.contains(LogNameConfiguration.DEFLOG_MARKER)) {
            String shortenedStatement = logStatement.substring(logStatement.indexOf(LogNameConfiguration.DEFLOG_MARKER) + LogNameConfiguration.DEFLOG_MARKER.length());
            return shortenedStatement.contains(LogNameConfiguration.DEFLOG_MARKER) ? cutDef(shortenedStatement) : LogNameConfiguration.DEFLOG_MARKER + shortenedStatement;
        }
        return logStatement;
    }

    @Override
    public String getCriteriaName() {
        return "All DefUse";
    }

    @Override
    public List<String> getTargets(LogicGraph logicGraph) {
        TargetGenerator testcaseGenerator = new TargetGenerator();
        var targets = testcaseGenerator.getAllTargetsToBeCoveredByAllDefUse(logicGraph);
        return targets.stream().map(CoverageTargetAllDefUse::getCoverageElement).map(FunctionWithSourceLine::getLogMessage)
                .map(entry -> entry.replaceAll("#", "")).toList();
    }

}
