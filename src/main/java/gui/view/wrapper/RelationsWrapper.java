package gui.view.wrapper;

import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseEvent;
import logic.model.SourceCodeLine;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class RelationsWrapper {
    private final ComboBox<ComboBoxItemWrap<String>> comboBox = new ComboBox<>();
    private final List<Long> neighbours;

    public RelationsWrapper(List<Long> neighbours) {
        this.neighbours = new ArrayList<>(neighbours);
    }


    public ComboBox<ComboBoxItemWrap<String>> getComboBox() {
        return comboBox;
    }

    public void setupComboBox() {
        {
            fillComboBox();
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

    private void fillComboBox() {
        if (neighbours.contains(SourceCodeLine.INFLUENCING_ALL_RELATIONS_CONSTANT)) {
            var allEntry = new ComboBoxItemWrap<>("all neighbours");
            allEntry.checkProperty().set(false);
            var items = comboBox.getItems();
            items.add(allEntry);
        }
        var entries = neighbours.stream().
                filter(r -> r != SourceCodeLine.INFLUENCING_ALL_RELATIONS_CONSTANT).
                map(n ->
                new ComboBoxItemWrap<>(n.toString())).collect(Collectors.toCollection(LinkedList::new));
        comboBox.getItems().addAll(entries);
    }

    public void refreshText() {
        String combination = comboBox.getItems().stream().filter(f -> f != null && f.getCheck()).map(ComboBoxItemWrap::getItem).collect(Collectors.joining(";"));
        comboBox.setPromptText(combination);
    }

    public void activateNeighbours(List<Long> relationsInfluenced) {
        if (relationsInfluenced != null && relationsInfluenced.size() > 0) {
            activateNeighboursOfComboBox(relationsInfluenced);
            refreshText();
        }
    }

    private void activateNeighboursOfComboBox(List<Long> relationsInfluenced) {
        var items = comboBox.getItems();
        for (var item : items) {
            String entry = item.getItem();
            if ("all neighbours".equals(entry)) {
                if (relationsInfluenced.contains(SourceCodeLine.INFLUENCING_ALL_RELATIONS_CONSTANT)) {
                    item.checkProperty().set(true);
                }
            } else {
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
    }


    public List<Long> getSelectedRelations() {
        ObservableList<ComboBoxItemWrap<String>> items = comboBox.getItems();
        List<Long> influencedRelations = new LinkedList<>();
        for (var item : items) {
            if (item.getCheck()) {
                if ("all neighbours".equals(item.getItem())) {
                    influencedRelations.clear();
                    influencedRelations.add(SourceCodeLine.INFLUENCING_ALL_RELATIONS_CONSTANT);
                    break;
                }

                try {
                    Long relation = Long.valueOf(item.getItem());
                    influencedRelations.add(relation);
                } catch (NumberFormatException e) {
                    System.err.printf("String %s could not be parsed to a Long%n", item.getItem());
                }
            }
        }
        return influencedRelations;
    }

    public void activateAllOnly() {
        comboBox.getItems().clear();
        fillComboBox();
        comboBox.getItems().stream().filter(n -> "all neighbours".equals(n.getItem())).forEach(n -> n.checkProperty().set(true));
    }

}
