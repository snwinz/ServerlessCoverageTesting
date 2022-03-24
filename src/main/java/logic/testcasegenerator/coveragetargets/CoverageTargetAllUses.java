package logic.testcasegenerator.coveragetargets;

import logic.testcasegenerator.coveragetargets.coverageelements.DefUsePair;
import logic.model.Testcase;

import java.util.ArrayList;
import java.util.List;

public class CoverageTargetAllUses implements CoverageTarget {

    private final DefUsePair coverageElement;

    private final List<Testcase> testcases;

    public CoverageTargetAllUses(DefUsePair coverageElement) {
        this.coverageElement = coverageElement;
        testcases = new ArrayList<>();
    }


    public DefUsePair getCoverageElement() {
        return coverageElement;
    }

    public List<Testcase> getTestcases() {
        return testcases;
    }

    public void addTestcases(List<Testcase> testcases) {
        this.testcases.addAll(testcases);
    }


    @Override
    public String getCoverageTargetDescription() {
        return coverageElement.getCoverageTargetDescription();
    }


    @Override
    public String toString() {
        return "CoverageTargetAllUses{" +
                "coverageElement=" + coverageElement +
                '}';
    }
}
