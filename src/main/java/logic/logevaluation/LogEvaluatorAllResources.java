package logic.logevaluation;


import logic.model.LogicGraph;
import logic.testcasegenerator.TargetGenerator;
import logic.testcasegenerator.coveragetargets.CoverageTargetAllResources;
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

    private static boolean isStatement(String statement) {
        return statement.startsWith(LogNameConfiguration.RESOURCE_MARKER);
    }

    @Override
    public Map<String, Integer> getUnitsCovered() {
        List<String> coveredResources =
                logs.stream().filter(LogEvaluatorAllResources::isStatement)
                        .map(a -> a.replaceAll(LogNameConfiguration.RESOURCE_MARKER, "")
                        .replace(LogNameConfiguration.LOGDELIMITER, "").trim())
                        .collect(Collectors.toList());
        return countNumberOfOccurrences(coveredResources);
    }

    @Override
    public String getCriteriaName() {
        return "All Resources";
    }

    @Override
    public List<String> getTargets(LogicGraph logicGraph) {
        TargetGenerator testcaseGenerator = new TargetGenerator();
        var targets = testcaseGenerator.getAllTargetsToBeCoveredByAllResources(logicGraph);
        return targets.stream()
                .map(CoverageTargetAllResources::getCoverageElement)
                .map(element -> String.valueOf(element.getIdentifier())).toList();
    }

}
