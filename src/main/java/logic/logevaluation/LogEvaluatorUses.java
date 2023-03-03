package logic.logevaluation;

import logic.model.LogicGraph;
import logic.testcasegenerator.TargetGenerator;
import logic.testcasegenerator.coveragetargets.CoverageTargetAllUses;
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
        return statement.contains(LogNameConfiguration.DEFLOG_MARKER)
                && statement.contains(LogNameConfiguration.USELOG_MARKER);
    }

    public Map<String, Integer> getUnitsCovered() {
        List<String> coveredUnits =
                logs.stream().map(LogEvaluatorUses::cutDef).map(entry -> entry.replaceAll("#", ""))
                        .collect(Collectors.toList());
        return countNumberOfOccurrences(coveredUnits);
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
        return "All Uses";
    }

    @Override
    public List<String> getTargets(LogicGraph logicGraph) {
        TargetGenerator testcaseGenerator = new TargetGenerator();
        var targets = testcaseGenerator.getAllTargetsToBeCoveredByAllUses(logicGraph);
        return targets.stream().map(CoverageTargetAllUses::getCoverageElement).map(defusePair -> {
            String targetText = "";
            var def = defusePair.getDef();
            var use = defusePair.getUse();
            if (def != null && use != null) {
                targetText = def.getSourceCodeLine().getDefTracker("", def.getFunction().getIdentifier()) +
                        use.getSourceCodeLine().getUseTracker("", use.getFunction().getIdentifier());
            }
            return targetText;
        }).map(entry -> entry.replaceAll("#", "")).toList();
    }

}
