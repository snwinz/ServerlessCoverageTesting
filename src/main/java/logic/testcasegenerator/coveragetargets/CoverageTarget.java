package logic.testcasegenerator.coveragetargets;

import javafx.beans.property.BooleanProperty;
import logic.model.Testcase;

import java.util.List;

public interface CoverageTarget {
    List<Testcase> getTestcases();

    BooleanProperty specificTargetCoveredProperty();

    String getCoverageTargetDescription();
}
