package logic.testcasegenerator.coveragetargets;

import logic.model.NodeModel;
import logic.model.Testcase;

import java.util.ArrayList;
import java.util.List;

import static logic.testcasegenerator.coveragetargets.LogNameConfiguration.LOGDELIMITER;
import static logic.testcasegenerator.coveragetargets.LogNameConfiguration.RESOURCE_MARKER;

public class CoverageTargetAllResources implements CoverageTarget {

    private final NodeModel aspect;

    private final List<Testcase> testcases;

    public CoverageTargetAllResources(NodeModel aspect) {
        this.aspect = aspect;
        testcases = new ArrayList<>();
    }


    public NodeModel getAspect() {
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
        return String.format("%s%d%s",RESOURCE_MARKER, aspect.getIdentifier(),LOGDELIMITER);
    }

    @Override
    public String getAspectTarget() {
        return String.format("Coverage of %s %s (id %d)",
                aspect.getType(), aspect.getNameOfNode(), aspect.getIdentifier());
    }


    @Override
    public String toString() {
        return "CoverageAspectAllResources{" +
                "aspect=" + aspect +
                '}';
    }
}
