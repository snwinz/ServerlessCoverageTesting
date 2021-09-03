package logic.testcasegenerator.coveragetargets;

import logic.model.Testcase;
import logic.testcasegenerator.coveragetargets.aspect.FunctionWithDefSourceLine;

import java.util.ArrayList;
import java.util.List;

public class CoverageTargetAllDefs implements CoverageTarget {

    private final FunctionWithDefSourceLine aspect;

    private final List<Testcase> testcases;

    public CoverageTargetAllDefs(FunctionWithDefSourceLine aspect) {
        this.aspect = aspect;
        testcases = new ArrayList<>();
    }


    public FunctionWithDefSourceLine getAspect() {
        return aspect;
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
