package logic.testcasegenerator.coveragetargets;

import logic.model.Testcase;
import logic.testcasegenerator.coveragetargets.aspect.FunctionWithSourceLine;

import java.util.ArrayList;
import java.util.List;

public class CoverageTargetAllDefUse implements CoverageTarget {

    private final FunctionWithSourceLine aspect;

    private final List<Testcase> testcases;

    public CoverageTargetAllDefUse(FunctionWithSourceLine aspect) {
        this.aspect = aspect;
        testcases = new ArrayList<>();
    }


    public FunctionWithSourceLine getAspect() {
        return  this.aspect;
    }

    public List<Testcase> getTestcases() {
        return testcases;
    }


    @Override
    public String getAspectLogMessage() {
        return aspect.getLogMessage();
    }

    @Override
    public String getAspectTarget() {
        return aspect.getAspectTarget();
    }
    public void addTestcases(List<Testcase> testcases) {
        this.testcases.addAll(testcases);
    }

    @Override
    public String toString() {
        return "CoverageAspectAllDefs{" +
                "aspect=" + aspect +
                '}';
    }
}
