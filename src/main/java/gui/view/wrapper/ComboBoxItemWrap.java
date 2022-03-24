package gui.view.wrapper;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public class ComboBoxItemWrap<T> {
    private final BooleanProperty check = new SimpleBooleanProperty(false);
    private final ObjectProperty<T> item = new SimpleObjectProperty<>();

    ComboBoxItemWrap(T item) {
        this.item.set(item);
    }

    public BooleanProperty checkProperty() {
        return check;
    }

    public Boolean getCheck() {
        return check.getValue();
    }

    public T getItem() {
        return item.getValue();
    }

    @Override
    public String toString() {
        return item.getValue().toString();
    }
}
