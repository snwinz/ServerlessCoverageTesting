package logic.testcasegenerator.coveragetargets;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import logic.model.Testcase;
import logic.testcasegenerator.coveragetargets.coverageelements.FunctionWithSourceLine;

import java.util.ArrayList;
import java.util.List;

public class CoverageTargetAllDefUse implements CoverageTarget {

    private final FunctionWithSourceLine coverageElement;

    private final List<Testcase> testcases;
    private final SimpleBooleanProperty specificTargetCoveredProperty;

    public CoverageTargetAllDefUse(FunctionWithSourceLine coverageElement) {
        this.coverageElement = coverageElement;
        testcases = new ArrayList<>();
        specificTargetCoveredProperty = new SimpleBooleanProperty();
    }


    public FunctionWithSourceLine getCoverageElement() {
        return this.coverageElement;
    }

    public List<Testcase> getTestcases() {
        return testcases;
    }

    @Override
    public BooleanProperty specificTargetCoveredProperty() {
        return specificTargetCoveredProperty;
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
        return "CoverageTargetAllDefUse{" +
                "coverageElement=" + coverageElement +
                '}';
    }
}
