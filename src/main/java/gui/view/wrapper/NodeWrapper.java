package gui.view.wrapper;

import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class NodeWrapper {
    private final ComboBox<ComboBoxItemWrap<String>> comboBox = new ComboBox<>();
    private final List<Long> nodes;

    public NodeWrapper(List<Long> nodes) {
        this.nodes = new ArrayList<>(nodes);
    }


    public ComboBox<ComboBoxItemWrap<String>> getComboBox() {
        return comboBox;
    }

    public void setupCombobox() {
        {
            fillCombobox();
            comboBox.setCellFactory(c -> {
                ListCell<ComboBoxItemWrap<String>> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(ComboBoxItemWrap<String> item, boolean empty) {
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
        var entries = nodes.stream().map(n ->
                new ComboBoxItemWrap<>(n.toString())).collect(Collectors.toCollection(LinkedList::new));
        comboBox.getItems().addAll(entries);
    }

    public void refreshText() {
        String combination = comboBox.getItems().stream().filter(f -> f != null && f.getCheck()).map(ComboBoxItemWrap::getItem).
                collect(Collectors.joining(";"));

        comboBox.setPromptText(combination);
    }

    public void activateNodes(List<Long> nodesInfluenced) {
        if (nodesInfluenced != null && nodesInfluenced.size() > 0) {
            activateNeighboursOfCombobox(nodesInfluenced);
            refreshText();
        }
    }

    private void activateNeighboursOfCombobox(List<Long> relationsInfluenced) {
        var items = comboBox.getItems();
        for (var item : items) {
            String entry = item.getItem();
            try {
                Long entrySaved = Long.valueOf(entry);
                if (relationsInfluenced.contains(entrySaved)) {
                    item.checkProperty().set(true);
                }
            } catch (NumberFormatException e) {
                System.err.printf("String %s could not be parsed to a Long%n", item.getItem());
            }
        }
    }


    public List<Long> getSelectedNodes() {
        ObservableList<ComboBoxItemWrap<String>> items = comboBox.getItems();
        List<Long> nodesInfluenced = new LinkedList<>();
        for (var item : items) {
            if (item.getCheck()) {
                try {
                    Long relation = Long.valueOf(item.getItem());
                    nodesInfluenced.add(relation);
                } catch (NumberFormatException e) {
                    System.err.printf("String %s could not be parsed to a Long%n", item.getItem());
                }
            }
        }
        return nodesInfluenced;
    }
}
