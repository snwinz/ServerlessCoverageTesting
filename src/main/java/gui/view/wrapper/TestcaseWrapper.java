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
    private final BooleanProperty manualCreated = new SimpleBooleanProperty();
    private final List<FunctionWrapper> functionsWrapped = new LinkedList<>();
    private final BooleanProperty executed = new SimpleBooleanProperty();
    private List<String> logsMeasured;
    private boolean saveLogs = false;

    public TestcaseWrapper(Testcase testcase) {
        this.testcase = testcase;
        for (var function : testcase.getFunctions()) {
            functionsWrapped.add(new FunctionWrapper(function));
        }
        manualCreated.set(testcase.isManualCreated());
        manualCreated.addListener((observable, oldValue, newValue) -> testcase.setManualCreated(newValue));
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

    public boolean isPassed() {
        return passed.get();
    }

    public void reset() {
        this.passed.set(false);
        this.executed.set(false);
        functionsWrapped.forEach(FunctionWrapper::reset);
    }

    public boolean isExecuted() {
        return executed.get();
    }

    public BooleanProperty executedProperty() {
        return executed;
    }

    public void setLogsMeasured(List<String> logList) {
        this.logsMeasured = logList;
    }

    public List<String> getLogsMeasured() {
        return logsMeasured;
    }

    public boolean isSaveLogs() {
        return saveLogs;
    }


    public BooleanProperty manualCreatedProperty() {
        return manualCreated;
    }

    public void setSaveLogs(boolean saveLogs) {
        this.saveLogs = saveLogs;
    }
}
