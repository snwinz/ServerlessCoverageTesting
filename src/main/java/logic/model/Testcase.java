package logic.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.List;

public class Testcase {
    private final List<ServerlessFunction> functionNames;
    private final String target;
    private StringProperty testCaseOutput;
    private BooleanProperty testCovered;

    private static final String INVOCATION_COMMAND = "serverless invoke -f ";

    private final List<String> logAspects;


    public Testcase(List<ServerlessFunction> functionNames, String target, List<String> logAspects) {
        this.functionNames = new ArrayList<>(functionNames);
        this.target = target;
        this.logAspects = logAspects;
    }

    public String getCommandsForTestcase() {
        StringBuilder result = new StringBuilder();
        for (ServerlessFunction functionName : functionNames) {
            result.append(INVOCATION_COMMAND).append(functionName.getName()).append("\n");
        }
        return result.toString();
    }

    public void setCovered(){
        testCovered.set(true);
    }


    public void setFailed(){
        testCovered.set(false);
    }
    public List<ServerlessFunction> getFunctions() {
        return functionNames;
    }


    public String getTarget() {
        return target;
    }

    public List<String> getLogAspects() {

        return logAspects;
    }

    public void setTestCaseOutput(StringProperty testCaseOutput) {
        this.testCaseOutput = testCaseOutput;
    }

    public void writeToOutput(String text) {
        if (testCaseOutput != null) {
            testCaseOutput.set(text);
        }
    }

    public void setTeststatus(BooleanProperty testStatus) {
        this.testCovered =testStatus;
    }
}