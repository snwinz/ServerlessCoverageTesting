package gui.view;

import gui.controller.ArrowEditorController;
import gui.controller.dto.ArrowInputData;
import gui.model.Graph;
import gui.model.NodeModel;
import gui.view.graphcomponents.DraggableArrow;
import gui.view.wrapper.AccessModesCombobox;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import shared.model.AccessMode;
import shared.model.NodeType;

public class ArrowEditorView extends Stage {

    private final ArrowEditorController controller;
    private final DraggableArrow arrow;

    private final ComboBox<NodeWrapper> comboBoxSuccessor = new ComboBox<>();
    private final ComboBox<NodeWrapper> comboBoxPredecessor = new ComboBox<>();
    private final AccessModesCombobox accessModesCombobox = new AccessModesCombobox();


    public ArrowEditorView(ArrowEditorController arrowEditorController, DraggableArrow draggableArrow, Graph model) {
        this.controller = arrowEditorController;
        this.arrow = draggableArrow;

        this.setTitle("Edit Arrow");
        var grid = getGridPane(model);
        var scene = new Scene(grid);
        this.setScene(scene);
    }

    private GridPane getGridPane(Graph model) {
        var accessText = new Label("Access mode: ");
        setupComboboxes(model, accessText);


        var grid = new GridPane();

        var predecessorText = new Label("Name of predecessor node: ");
        var successorText = new Label("Name of successor node: ");


        grid.add(predecessorText, 1, 1);
        grid.add(comboBoxPredecessor, 2, 1);
        grid.add(successorText, 1, 2);
        grid.add(comboBoxSuccessor, 2, 2);
        grid.add(accessText, 1, 3);
        grid.add(accessModesCombobox.getCombobox(), 2, 3);

        var updateButton = new Button("Update arrow");
        updateButton.setOnAction(getUpdateButtonHandler());
        var cancelButton = new Button("Cancel");
        cancelButton.setOnAction(getCancelButtonHandler());
        grid.add(updateButton, 1, 5);
        grid.add(cancelButton, 2, 5);
        return grid;
    }

    private EventHandler<ActionEvent> getUpdateButtonHandler() {
        return event -> {
            var successorNode = comboBoxSuccessor.getValue().node;
            var predecessorNode = comboBoxPredecessor.getValue().node;
            var infos =
                    new ArrowInputData();
            infos.setSuccessor(successorNode.getIdentifier());
            infos.setPredecessor(predecessorNode.getIdentifier());
            infos.setId(arrow.getIdentifier());

            infos.setAccessMode(accessModesCombobox.getModes());
            controller.updateArrowToGraph(infos);
        };
    }

    private EventHandler<ActionEvent> getCancelButtonHandler() {
        return event -> controller.closeWindow();
    }


    private void setupComboboxes(Graph model, Label accessText) {
        for (var node : model.getNodes()) {
            var pre = new NodeWrapper(node, node.getNameOfNode());
            comboBoxSuccessor.getItems().add(pre);
            var suc = new NodeWrapper(node, node.getNameOfNode());
            comboBoxPredecessor.getItems().add(suc);
            if (arrow.getPredecessor().getIdentifier() == node.getIdentifier()) {
                comboBoxPredecessor.getSelectionModel().select(pre);
            }
            if (arrow.getSuccessor().getIdentifier() == node.getIdentifier()) {
                comboBoxSuccessor.getSelectionModel().select(suc);
            }
        }

        ChangeListener<NodeWrapper> showAndHideAccessMode = (observable, oldValue, newValue) -> {
            if (comboBoxPredecessor.getValue() == null || comboBoxSuccessor.getValue() == null) {
                accessText.setVisible(false);
                accessModesCombobox.clear();
                accessModesCombobox.setVisible(false);
                return;
            }
            NodeWrapper pre = comboBoxPredecessor.getValue();
            NodeWrapper suc = comboBoxSuccessor.getValue();

            if (isDBCall(pre.node, suc.node)) {
                accessText.setVisible(true);
                accessModesCombobox.activateModes(AccessMode.READ, AccessMode.CREATE, AccessMode.UPDATE, AccessMode.DELETE);
                accessModesCombobox.setVisible(true);

            } else if (isFunctionCall(pre.node, suc.node)) {
                accessText.setVisible(true);
                accessModesCombobox.activateModes(AccessMode.FUNCTIONCALL, AccessMode.RETURN);
                accessModesCombobox.setVisible(true);
            } else {
                accessText.setVisible(false);
                accessModesCombobox.clear();
                accessModesCombobox.setVisible(false);
            }
        };


        comboBoxSuccessor.valueProperty().addListener(showAndHideAccessMode);
        comboBoxPredecessor.valueProperty().addListener(showAndHideAccessMode);

        if (isDBCall(comboBoxPredecessor.getValue().node, comboBoxSuccessor.getValue().node)) {
            accessText.setVisible(true);
            accessModesCombobox.activateModes(AccessMode.READ, AccessMode.CREATE, AccessMode.UPDATE, AccessMode.DELETE);
            //comboboxAccess.setValue(arrow.getAccessMode().toArray();
            accessModesCombobox.setVisible(true);
        } else if (isFunctionCall(comboBoxPredecessor.getValue().node, comboBoxSuccessor.getValue().node)) {
            accessText.setVisible(true);
            accessModesCombobox.activateModes(AccessMode.FUNCTIONCALL, AccessMode.RETURN);
            // comboboxAccess.setValue(arrow.getAccessMode());
            accessModesCombobox.setVisible(true);
        } else {
            accessModesCombobox.clear();
            accessText.setVisible(false);
            accessModesCombobox.setVisible(false);
        }
    }

    private boolean isDBCall(NodeModel pre, NodeModel suc) {
        return pre.getType().equals(NodeType.FUNCTION) && suc.getType().equals(NodeType.DATA_STORAGE);
    }


    private boolean isFunctionCall(NodeModel pre, NodeModel suc) {
        return pre.getType().equals(NodeType.FUNCTION) && suc.getType().equals(NodeType.FUNCTION);
    }

    static class NodeWrapper {
        public NodeWrapper(NodeModel node, String name) {
            this.node = node;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        final NodeModel node;
        final String name;
    }
}
