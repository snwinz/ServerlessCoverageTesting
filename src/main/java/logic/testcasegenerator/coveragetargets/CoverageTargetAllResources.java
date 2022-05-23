package logic.testcasegenerator.coveragetargets;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import logic.model.NodeModel;
import logic.model.Testcase;

import java.util.ArrayList;
import java.util.List;

public class CoverageTargetAllResources implements CoverageTarget {

    private final NodeModel coverageElement;

    private final List<Testcase> testcases;
    private final SimpleBooleanProperty specificTargetCoveredProperty;

    public CoverageTargetAllResources(NodeModel coverageElement) {
        this.coverageElement = coverageElement;
        testcases = new ArrayList<>();
        specificTargetCoveredProperty = new SimpleBooleanProperty();
    }


    public NodeModel getCoverageElement() {
        return coverageElement;
    }

    public List<Testcase> getTestcases() {
        return testcases;
    }

    public void addTestcases(List<Testcase> testcases) {
        this.testcases.addAll(testcases);
    }

    @Override
    public BooleanProperty specificTargetCoveredProperty() {
        return specificTargetCoveredProperty;
    }

    @Override
    public String getCoverageTargetDescription() {
        return String.format("Coverage of %s %s (id %d)",
                coverageElement.getType(), coverageElement.getNameOfNode(), coverageElement.getIdentifier());
    }


    @Override
    public String toString() {
        return "CoverageTargetAllResources{" +
                "coverageElement=" + coverageElement +
                '}';
    }
}
