package gui.view.wrapper;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import shared.model.Testcase;

import java.util.LinkedList;
import java.util.List;

public class TestcaseWrapper {
    private final StringProperty expectedLogs = new SimpleStringProperty("");
    private final Testcase testcase;
    private final BooleanProperty passed = new SimpleBooleanProperty();
    private final List<FunctionWrapper> functionsWrapped = new LinkedList<>();
    private final BooleanProperty executed = new SimpleBooleanProperty();

    public TestcaseWrapper(Testcase testcase) {
        this.testcase = testcase;
        for (var function : testcase.functions()) {
            functionsWrapped.add(new FunctionWrapper(function));
        }
    }

    public Testcase getTestcase() {
        return testcase;
    }

    public List<FunctionWrapper> getFunctionsWrapped() {
        return functionsWrapped;
    }


    public StringProperty expectedLogsProperty() {
        return expectedLogs;
    }

    public BooleanProperty passedProperty() {
        return passed;
    }

    public void reset() {
        this.passed.set(false);
        this.executed.set(false);
    }

    public boolean isExecuted() {
        return executed.get();
    }

    public BooleanProperty executedProperty() {
        return executed;
    }
}
