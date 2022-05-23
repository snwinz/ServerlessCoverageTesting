package logic.testcasegenerator.coveragetargets;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import logic.model.Testcase;
import logic.testcasegenerator.coveragetargets.coverageelements.DefUsePair;

import java.util.ArrayList;
import java.util.List;

public class CoverageTargetAllUses implements CoverageTarget {

    private final DefUsePair coverageElement;

    private final List<Testcase> testcases;
    private final SimpleBooleanProperty specificTargetCoveredProperty;

    public CoverageTargetAllUses(DefUsePair coverageElement) {
        this.coverageElement = coverageElement;
        testcases = new ArrayList<>();
        specificTargetCoveredProperty = new SimpleBooleanProperty();
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
    public BooleanProperty specificTargetCoveredProperty() {
        return specificTargetCoveredProperty;
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
