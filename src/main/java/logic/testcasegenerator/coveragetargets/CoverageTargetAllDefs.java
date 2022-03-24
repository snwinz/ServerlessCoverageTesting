package logic.testcasegenerator.coveragetargets;

import logic.model.Testcase;
import logic.testcasegenerator.coveragetargets.coverageelements.FunctionWithDefSourceLine;

import java.util.ArrayList;
import java.util.List;

public class CoverageTargetAllDefs implements CoverageTarget {

    private final FunctionWithDefSourceLine coverageElement;

    private final List<Testcase> testcases;

    public CoverageTargetAllDefs(FunctionWithDefSourceLine coverageElement) {
        this.coverageElement = coverageElement;
        testcases = new ArrayList<>();
    }


    public FunctionWithDefSourceLine getCoverageElement() {
        return coverageElement;
    }

    public List<Testcase> getTestcases() {
        return testcases;
    }

    @Override
    public String getCoverageTargetDescription() {
        return coverageElement.getCoverageTargetDescription();
    }

    public void addTestcases(List<Testcase> testcases) {
        this.testcases.addAll(testcases);
    }

    @Override
    public String toString() {
        return "CoverageTargetAllDefs{" +
                "coverageElement=" + coverageElement +
                '}';
    }
}
