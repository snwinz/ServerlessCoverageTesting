package logic.logevaluation;

import gui.model.Graph;
import logic.model.LogicGraph;
import logic.testcasegenerator.TargetGenerator;
import logic.testcasegenerator.coveragetargets.CoverageTargetAllRelations;
import logic.testcasegenerator.coveragetargets.LogNameConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LogEvaluatorAllRelations extends LogEvaluator {
    final List<String> logs = new ArrayList<>();

    public LogEvaluatorAllRelations(List<String> logStatements) {
        for (String statement : logStatements) {
            if (isStatement(statement)) {
                logs.add(statement);
            }
        }
    }

    private static boolean isStatement(String statement) {
        return statement.startsWith(LogNameConfiguration.RELATION_MARKER);
    }

    @Override
    public Map<String, Integer> getUnitsCovered() {
        List<String> coveredResources =
                logs.stream().filter(LogEvaluatorAllRelations::isStatement)
                        .map(a -> a.replaceAll(LogNameConfiguration.RELATION_MARKER, "").trim()
                        .replace(LogNameConfiguration.LOGDELIMITER, "").trim())
                        .collect(Collectors.toList());
        return countNumberOfOccurrences(coveredResources);
    }

    @Override
    public String getCriteriaName() {
        return "All Resources Relations";
    }

    @Override
    public List<String> getTargets(LogicGraph logicGraph) {
        TargetGenerator testcaseGenerator = new TargetGenerator();
        var targets = testcaseGenerator.getAllTargetsToBeCoveredByAllRelations(logicGraph);
        var result = targets.stream()
                .map(CoverageTargetAllRelations::getCoverageElement)
                .map(element -> String.valueOf(element.getIdentifier())).toList();
        return result;
    }
}
