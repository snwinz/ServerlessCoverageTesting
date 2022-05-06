package gui.view.wrapper;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import shared.model.Testcase;

import java.util.LinkedList;
import java.util.List;

public class TestcaseWrapper  {
    private final StringProperty expectedLogs = new SimpleStringProperty("");
    private final Testcase testcase;

    private final List<FunctionWrapper> functionsWrapped = new LinkedList<>();

    public TestcaseWrapper(Testcase testcase) {
        this.testcase = testcase;
        for(var function  :testcase.functions()){
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
}
