package logic.testcasegenerator.coveragetargets;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import logic.model.ArrowModel;
import logic.model.Testcase;

import java.util.ArrayList;
import java.util.List;

public class CoverageTargetAllRelations implements CoverageTarget {

    private final ArrowModel coverageElement;

    private final List<Testcase> testcases;
    private final SimpleBooleanProperty specificTargetCoveredProperty;

    public CoverageTargetAllRelations(ArrowModel coverageElement) {
        this.coverageElement = coverageElement;
        testcases = new ArrayList<>();
        specificTargetCoveredProperty = new SimpleBooleanProperty();
    }


    public ArrowModel getCoverageElement() {
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
        return String.format("Coverage of relation from %s to %s by calling relation %s",
                coverageElement.getPredecessorNode(), coverageElement.getSuccessorNode(), coverageElement.getIdentifier());
    }

    @Override
    public String toString() {
        return "CoverageTargetAllRelations{" +
                "coverageElement=" + coverageElement +
                '}';
    }
}
