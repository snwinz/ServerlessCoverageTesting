package gui.view.wrapper;

import javafx.scene.control.CheckBox;
import logic.model.Testcase;

public class CheckboxWrapper extends CheckBox {
    private final Testcase testcase;

    public CheckboxWrapper(Testcase testcase) {
        this.testcase = testcase;
    }

    public Testcase getTestcase() {
        return testcase;
    }


}
