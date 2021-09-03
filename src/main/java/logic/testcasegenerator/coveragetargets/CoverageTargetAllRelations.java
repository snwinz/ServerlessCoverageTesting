package logic.testcasegenerator.coveragetargets;

import logic.model.ArrowModel;
import logic.model.Testcase;

import java.util.ArrayList;
import java.util.List;

import static logic.testcasegenerator.coveragetargets.LogNameConfiguration.LOGDELIMITER;
import static logic.testcasegenerator.coveragetargets.LogNameConfiguration.RELATION_MARKER;

public class CoverageTargetAllRelations implements CoverageTarget {

    private final ArrowModel aspect;

    private final List<Testcase> testcases;

    public CoverageTargetAllRelations(ArrowModel aspect) {
        this.aspect = aspect;
        testcases = new ArrayList<>();
    }


    public ArrowModel getAspect() {
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
        return String.format("#%s%s%s", RELATION_MARKER,aspect.getIdentifier(),LOGDELIMITER);
    }

    @Override
    public String getAspectTarget() {
        return String.format("Coverage of relation from %s to %s by calling relation %s",
                aspect.getPredecessorNode(), aspect.getSuccessorNode(), aspect.getIdentifier());
    }

    @Override
    public String toString() {
        return "CoverageAspectAllRelations{" +
                "aspect=" + aspect +
                '}';
    }
}
