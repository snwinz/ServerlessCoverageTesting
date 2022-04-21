package gui.view.wrapper;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseEvent;
import shared.model.AccessMode;

import java.util.*;
import java.util.stream.Collectors;

public class AccessModesCombobox {
    private final ComboBox<ComboBoxItemWrap<AccessMode>> comboBox = new ComboBox<>();
    private final Set<AccessMode> modes = new HashSet<>();

    public AccessModesCombobox() {
        setupCombobox();
    }

    public ComboBox<ComboBoxItemWrap<AccessMode>> getComboBox() {
        return this.comboBox;
    }

    public void setupCombobox() {
        {
            fillCombobox();
            comboBox.setCellFactory(c -> {
                ListCell<ComboBoxItemWrap<AccessMode>> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(ComboBoxItemWrap<AccessMode> item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {
                            final CheckBox cb = new CheckBox(item.toString());
                            cb.selectedProperty().bind(item.checkProperty());
                            setGraphic(cb);
                        }
                    }
                };
                cell.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
                    cell.getItem().checkProperty().set(!cell.getItem().checkProperty().get());
                    refreshText();
                });
                return cell;
            });
        }
    }

    private void fillCombobox() {
        var entries = modes.stream().
                map(ComboBoxItemWrap::new).collect(Collectors.toCollection(LinkedList::new));
        comboBox.getItems().clear();
        comboBox.getItems().addAll(entries);
    }

    public void refreshText() {
        String combination = comboBox.getItems().stream().filter(f -> f != null && f.getCheck()).map(ComboBoxItemWrap::getItem).map(AccessMode::toString).collect(Collectors.joining(";"));
        comboBox.setPromptText(combination);
    }


    public Set<AccessMode> getModes() {
        return comboBox.getItems().stream().filter(f->f != null && f.getCheck()).map(ComboBoxItemWrap::getItem).collect(Collectors.toSet());
    }

    public void clear() {
        this.modes.clear();
    }

    public void setVisible(boolean visible) {
        this.comboBox.setVisible(visible);
    }

    public void activateModes(AccessMode... modes) {
        this.modes.clear();
        this.modes.addAll(List.of(modes));
        this.fillCombobox();
    }
}
