package logic.logevaluation;

import logic.model.LogicGraph;
import logic.testcasegenerator.TargetGenerator;
import logic.testcasegenerator.coveragetargets.CoverageTargetAllDefs;
import logic.testcasegenerator.coveragetargets.LogNameConfiguration;
import logic.testcasegenerator.coveragetargets.coverageelements.FunctionWithDefSourceLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        return statement.contains(LogNameConfiguration.DEFLOG_MARKER) && statement.contains(LogNameConfiguration.USELOG_MARKER);
    }

    public Map<String, Integer> getUnitsCovered() {
        List<String> coveredResources = logs.stream().map(LogEvaluatorDefs::cutDef).map(entry -> entry.split(LogNameConfiguration.USELOG_MARKER)[0]).map(entry -> entry.replaceAll("#", "")).collect(Collectors.toList());
        return countNumberOfOccurrences(coveredResources);
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
        return "All Defs";
    }

    @Override
    public List<String> getTargets(LogicGraph logicGraph) {
        TargetGenerator testcaseGenerator = new TargetGenerator();
        var targets = testcaseGenerator.getAllTargetsToBeCoveredByAllDefs(logicGraph);
        return targets.stream().map(CoverageTargetAllDefs::getCoverageElement).map(FunctionWithDefSourceLine::getLogMessage).map(entry -> entry.replaceAll("#", "")).toList();
    }

}
