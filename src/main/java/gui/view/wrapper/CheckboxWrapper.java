package gui.view.wrapper;

import javafx.scene.control.CheckBox;

public class CheckboxWrapper<T> extends CheckBox {
    private final T entry;

    public CheckboxWrapper(T testcase) {
        this.entry = testcase;
    }

    public T getEntry() {
        return entry;
    }

}
