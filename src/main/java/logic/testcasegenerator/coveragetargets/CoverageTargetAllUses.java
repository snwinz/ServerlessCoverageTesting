package logic.testcasegenerator.coveragetargets;

import logic.testcasegenerator.coveragetargets.aspect.DefUsePair;
import logic.model.Testcase;

import java.util.ArrayList;
import java.util.List;

public class CoverageTargetAllUses implements CoverageTarget {

    private final DefUsePair aspect;

    private final List<Testcase> testcases;

    public CoverageTargetAllUses(DefUsePair aspect) {
        this.aspect = aspect;
        testcases = new ArrayList<>();
    }


    public DefUsePair getAspect() {
        return aspect;
    }

    public List<Testcase> getTestcases() {
        return testcases;
    }

    public void addTestcases(List<Testcase> testcases) {
        this.testcases.addAll(testcases);
    }


    @Override
    public String getAspectLogMessage() {
        return aspect.getLogMessage();
    }

    @Override
    public String getAspectTarget() {
        return aspect.getAspectTarget();
    }


    @Override
    public String toString() {
        return "CoverageAspectAllDefs{" +
                "aspect=" + aspect +
                '}';
    }
}
