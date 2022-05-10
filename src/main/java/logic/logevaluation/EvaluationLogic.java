package logic.logevaluation;

import gui.model.Graph;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByValue;

public class EvaluationLogic {

    private final StringProperty coverageText = new SimpleStringProperty("");
    private final StringProperty logText = new SimpleStringProperty("");
    private final Graph graph;
    private List<String> logs;

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public EvaluationLogic(Graph graph) {
        this.graph = graph;
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
            Map<String, Integer> unitsCovered = evaluator.getCoveredResources();

            var unitsCoveredSortedByOccurrence = unitsCovered.entrySet().stream().sorted(Collections.reverseOrder(comparingByValue()))
                    .collect(Collectors.toList());
            for (var entry : unitsCoveredSortedByOccurrence) {
                result.append(String.format("%s\t%sx%n", entry.getKey(), entry.getValue()));
            }
            result.append(String.format("%n%n"));
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
