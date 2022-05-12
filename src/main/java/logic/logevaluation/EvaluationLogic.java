package logic.logevaluation;

import gui.model.Graph;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import logic.model.LogicGraph;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Map.Entry.comparingByValue;

public class EvaluationLogic {

    private final StringProperty coverageText = new SimpleStringProperty("");
    private final StringProperty logText = new SimpleStringProperty("");
    private final LogicGraph logicGraph;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private List<String> logs;

    public EvaluationLogic(Graph graph) {
        this.logicGraph = new LogicGraph(graph.getJSON());
        logicGraph.addRelationsToElements();
    }

    public String getCoverageText() {
        return coverageText.get();
    }

    public StringProperty coverageTextProperty() {
        return coverageText;
    }

    public void calculateCoverage() {
        StringBuilder result = new StringBuilder();
        LinkedList<LogEvaluator> evaluators = new LinkedList<>();
        evaluators.add(new LogEvaluatorAllResources(logs));
        evaluators.add(new LogEvaluatorAllRelations(logs));
        evaluators.add(new LogEvaluatorDefs(logs));
        evaluators.add(new LogEvaluatorDefUse(logs));
        evaluators.add(new LogEvaluatorUses(logs));


        for (LogEvaluator evaluator : evaluators) {

            result.append(evaluator.getCriteriaName()).append(System.lineSeparator());
            Map<String, Integer> unitsCovered = evaluator.getUnitsCovered();

            var unitsCoveredSortedByOccurrence = unitsCovered.entrySet().stream()
                    .sorted(Collections.reverseOrder(comparingByValue()))
                    .toList();
            for (var entry : unitsCoveredSortedByOccurrence) {
                result.append(String.format("%s\t%sx%n", entry.getKey(), entry.getValue()));
            }
            result.append(String.format("%n%n"));
            evaluateCoverageOfModel(result, evaluator, unitsCovered);


        }
        coverageText.setValue(result.toString());


    }

    private void evaluateCoverageOfModel(StringBuilder result, LogEvaluator evaluator, Map<String, Integer> unitsCovered) {
        if (logicGraph != null && logicGraph.getNodes().size() != 0) {
            List<String> targetsToCover = evaluator.getTargets(logicGraph);
            result.append(String.format("Targets to cover: %d%n", targetsToCover.size()));
            result.append(String.format("Targets covered in log: %d%n", unitsCovered.size()));
            var targetsOfModelCovered = targetsToCover.stream().filter(target -> unitsCovered.containsKey(target)).toList();
            result.append(String.format("Targets of graph covered: %d%n%n", targetsOfModelCovered.size()));

            result.append(String.format("Missing targets:%n"));
            var missingTargets = targetsToCover.stream().filter(target -> !unitsCovered.containsKey(target)).toList();
            result.append(String.join("\n", missingTargets));
            result.append("\n\n");
        }
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
        if (logs != null) {
            var logText = String.join("\n", logs);
            logTextProperty().set(logText);
        }
    }


    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }


    public StringProperty logTextProperty() {
        return logText;
    }
}
