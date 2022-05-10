package logic.logevaluation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByValue;

public class CoverageCalculation {


    public String calculateCoverage(File file) {
        StringBuilder result
                = new StringBuilder();
        try {
            var path = file.toPath();
            List<String> lines = Files.readAllLines(path);
            List<String> logStatements = getLogStatements(lines);

            LinkedList<LogEvaluator> evaluators = new LinkedList<>();

            evaluators.add(new LogEvaluatorAllResources(logStatements));
            evaluators.add(new LogEvaluatorAllRelations(logStatements));
            evaluators.add(new LogEvaluatorDefs(logStatements));
            evaluators.add(new LogEvaluatorDefUse(logStatements));
            evaluators.add(new LogEvaluatorUses(logStatements));


            for (LogEvaluator evaluator : evaluators) {

                result.append(evaluator.getCriteriaName()).append(System.lineSeparator());
                Map<String, Integer> unitsCovered = evaluator.getUnitsCovered();

                var unitsCoveredSortedByOccurrence = unitsCovered.entrySet().stream().sorted(Collections.reverseOrder(comparingByValue()))
                        .collect(Collectors.toList());
                for (var entry : unitsCoveredSortedByOccurrence) {
                    result.append(String.format("%s\t%sx%n", entry.getKey(), entry.getValue()));
                }
                result.append(String.format("%n%n"));
            }


        } catch (IOException e) {
            System.err.println("File could not be read.");
        }


        return result.toString();

    }

    private List<String> getLogStatements(List<String> lines) {

        List<String> statements = new ArrayList<>();
        for (String line : lines) {
            if (line.contains("INFO")) {
                line = line.substring(line.indexOf("INFO"));
                String[] infoArray = line.split("INFO");
                for (String info : infoArray) {
                    String statement = info.split(" ")[0].trim();
                    if (statement.startsWith("#")) {
                        statements.add(statement);
                    }
                }
            }
        }
        return statements;
    }
}


