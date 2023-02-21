package gui.view.wrapper;

import java.util.HashSet;
import java.util.Set;

public class ExecutionSettings {


    private final String resetFunctionName;
    private final String region;
    private final Set<String> authKeys;


    private int numberOfTries = 1;

    private double probChangeGoodData;

    private double probEntryUndefined;
    private double probSimilarInputAsValue;
    private double probRandomInputAsValue;
    private double probSimilarOutputAsValue;
    private double probRandomOutputAsValue;
    private double probSameValueEverywhere;

    public ExecutionSettings(String region, String resetFunctionName, Set<String> authKeys) {
        this.region = region;
        this.resetFunctionName = resetFunctionName;
        this.authKeys = authKeys;
    }


    public double getProbChangeGoodData() {
        return probChangeGoodData;
    }

    public void setNumberOfTries(int numberOfTries) {
        this.numberOfTries = numberOfTries;
    }

    public int getNumberOfRuns() {
        return numberOfTries;
    }

    public void setProbChangeGoodData(Double probChangeGoodData) {
        this.probChangeGoodData = probChangeGoodData;
    }

    public double getProbEntryUndefined() {
        return probEntryUndefined;
    }

    public void setProbEntryUndefined(Double probEntryUndefined) {
        this.probEntryUndefined = probEntryUndefined;
    }

    public double getProbSimilarInputAsValue() {
        return probSimilarInputAsValue;
    }

    public void setProbSimilarInputAsValue(Double probSimilarInputAsValue) {
        this.probSimilarInputAsValue = probSimilarInputAsValue;
    }

    public double getProbRandomInputAsValue() {
        return probRandomInputAsValue;
    }

    public void setProbRandomInputAsValue(Double probRandomInputAsValue) {
        this.probRandomInputAsValue = probRandomInputAsValue;
    }

    public double getProbSimilarOutputAsValue() {
        return probSimilarOutputAsValue;
    }

    public void setProbSimilarOutputAsValue(Double probSimilarOutputAsValue) {
        this.probSimilarOutputAsValue = probSimilarOutputAsValue;
    }

    public double getProbRandomOutputAsValue() {
        return probRandomOutputAsValue;
    }

    public void setProbRandomOutputAsValue(Double probRandomOutputAsValue) {
        this.probRandomOutputAsValue = probRandomOutputAsValue;
    }

    public void setProbSameValueEverywhere(Double probSameValueEverywhere) {
        this.probSameValueEverywhere = probSameValueEverywhere;
    }

    public Double getProbSameValueEverywhere() {
        return probSameValueEverywhere;
    }

    public String getResetFunctionName() {
        return resetFunctionName;
    }

    public String getRegion() {
        return region;
    }

    public Set<String> getAuthKeys() {
        return authKeys;
    }

    public ExecutionSettings getSimilarSettings() {
        return new ExecutionSettings(this.region, this.resetFunctionName, new HashSet<>(authKeys));
    }
}
