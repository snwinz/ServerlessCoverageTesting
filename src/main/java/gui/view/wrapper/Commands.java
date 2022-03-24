package gui.view.wrapper;

public class Commands {


    private String resetFunctionName;


    private int numberOfTries = 1;

    private Double probChangeGoodData;

    private Double probEntryUndefined;
    private Double probSimilarInputAsValue;
    private Double probRandomInputAsValue;
    private Double probSimilarOutputAsValue;
    private Double probRandomOutputAsValue;
    private Double probSameValueEverywhere;
    private String region;


    public Double getProbChangeGoodData() {
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

    public Double getProbEntryUndefined() {
        return probEntryUndefined;
    }

    public void setProbEntryUndefined(Double probEntryUndefined) {
        this.probEntryUndefined = probEntryUndefined;
    }

    public Double getProbSimilarInputAsValue() {
        return probSimilarInputAsValue;
    }

    public void setProbSimilarInputAsValue(Double probSimilarInputAsValue) {
        this.probSimilarInputAsValue = probSimilarInputAsValue;
    }

    public Double getProbRandomInputAsValue() {
        return probRandomInputAsValue;
    }

    public void setProbRandomInputAsValue(Double probRandomInputAsValue) {
        this.probRandomInputAsValue = probRandomInputAsValue;
    }

    public Double getProbSimilarOutputAsValue() {
        return probSimilarOutputAsValue;
    }

    public void setProbSimilarOutputAsValue(Double probSimilarOutputAsValue) {
        this.probSimilarOutputAsValue = probSimilarOutputAsValue;
    }

    public Double getProbRandomOutputAsValue() {
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

    public void setResetFunctionName(String resetFunctionName) {
        this.resetFunctionName = resetFunctionName;
    }

    public String getResetFunctionName() {
        return resetFunctionName;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getRegion() {
        return region;
    }
}
