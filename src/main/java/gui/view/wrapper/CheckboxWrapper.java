package gui.view.wrapper;

import javafx.scene.control.CheckBox;
import logic.model.Testcase;
import logic.testcasegenerator.coveragetargets.CoverageTarget;

public class CheckboxWrapper extends CheckBox {
    private final Testcase testcase;
    private final CoverageTarget aspect;

    public CheckboxWrapper(String text, Testcase testcase, CoverageTarget aspect) {
       super(text);
        this.testcase = testcase;
        this.aspect = aspect;
    }

    public CheckboxWrapper(Testcase testcase, CoverageTarget aspect) {
        this.testcase = testcase;
        this.aspect = aspect;
    }

    public Testcase getTestcase() {
        return testcase;
    }

    public CoverageTarget getAspect() {
        return aspect;
    }
}
