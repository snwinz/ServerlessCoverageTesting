package gui.view;

import gui.controller.ArrowCreatorController;
import gui.controller.dto.ArrowInputData;
import gui.model.AccessMode;
import gui.model.Graph;
import gui.model.NodeModel;
import gui.model.NodeType;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ArrowCreatorView extends Stage{

    private final ArrowCreatorController controller;
    private final ComboBox<NodeWrapper> comboBoxSuccessor = new ComboBox<>();
    private final ComboBox<NodeWrapper> comboBoxPredecessor = new ComboBox<>();
    private final ComboBox<AccessMode> comboboxAccess = new ComboBox<>();

    public ArrowCreatorView(ArrowCreatorController controller, Graph model) {
        this.controller = controller;
        this.setTitle("Edit Arrow");
        var grid = getGridPane(model);

        var scene = new Scene(grid);
        this.setScene(scene);
    }

    private GridPane getGridPane(Graph model) {
        var accessText = new Label("Access mode: ");
        accessText.setVisible(false);
        setupComboboxes(model, accessText);
        var grid = new GridPane();
        var createButton = new Button("Add arrow");
        createButton.setOnAction(getCreateButtonHandler());
        var cancelButton = new Button("Cancel");
        cancelButton.setOnAction(getCancelButtonHandler());
        var predecessorText = new Label("Name of predecessor node: ");
        var successorText = new Label("Name of successor node: ");
        grid.add(predecessorText, 1, 1);
        grid.add(comboBoxPredecessor, 2, 1);
        grid.add(successorText, 1, 2);
        grid.add(comboBoxSuccessor, 2, 2);
        grid.add(accessText, 1, 3);
        grid.add(comboboxAccess, 2, 3);
        grid.add(createButton, 1, 5);
        grid.add(cancelButton, 2, 5);
        return grid;
    }

    private void setupComboboxes(Graph model, Label accessText) {
        for (var node : model.getNodes()) {
            comboBoxSuccessor.getItems().add(new NodeWrapper(node, node.getNameOfNode()));
            comboBoxPredecessor.getItems().add(new NodeWrapper(node, node.getNameOfNode()));
        }

        ChangeListener<NodeWrapper> showAndHideAccessMode = (observable, oldValue, newValue) -> {

            if (comboBoxPredecessor.getValue() == null || comboBoxSuccessor.getValue() == null) {
                return;
            }
            NodeWrapper pre = comboBoxPredecessor.getValue();
            NodeWrapper suc = comboBoxSuccessor.getValue();

            if (isDBCall(pre.node, suc.node)) {
                accessText.setVisible(true);
                comboboxAccess.getItems().setAll(AccessMode.READ, AccessMode.WRITE, AccessMode.DELETE);
                comboboxAccess.setVisible(true);
            } else if (isFunctionCall(pre.node, suc.node)) {
                accessText.setVisible(true);
                comboboxAccess.getItems().setAll(AccessMode.FUNCTIONCALL, AccessMode.RETURN);
                comboboxAccess.setVisible(true);
            } else {
                accessText.setVisible(false);
                comboboxAccess.getItems().clear();
                comboboxAccess.setVisible(false);
            }
        };


        comboBoxSuccessor.valueProperty().addListener(showAndHideAccessMode);
        comboBoxPredecessor.valueProperty().addListener(showAndHideAccessMode);

        comboboxAccess.setVisible(false);
    }

    private boolean isDBCall(NodeModel pre, NodeModel suc) {
        return pre.getType().equals(NodeType.FUNCTION) && suc.getType().equals(NodeType.DATA_STORAGE);
    }

    private boolean isFunctionCall(NodeModel pre, NodeModel suc) {
        return pre.getType().equals(NodeType.FUNCTION) && suc.getType().equals(NodeType.FUNCTION);
    }

    private EventHandler<ActionEvent> getCancelButtonHandler() {
        return event -> controller.closeWindow();
    }

    private EventHandler<ActionEvent> getCreateButtonHandler() {
        return event -> {
            if (comboBoxPredecessor.getSelectionModel().isEmpty() || comboBoxSuccessor.getSelectionModel().isEmpty()) {
                System.err.println("No value in combobox selected");
            } else {
                var successorNode = comboBoxSuccessor.getValue().node;
                var predecessorNode = comboBoxPredecessor.getValue().node;
                var infos = new ArrowInputData();
                infos.setSuccessor(successorNode.getIdentifier());
                infos.setPredecessor(predecessorNode.getIdentifier());
                if (isDBCall(predecessorNode, successorNode) || isFunctionCall(predecessorNode, successorNode)) {
                    infos.setAccessMode(comboboxAccess.getValue());
                }
                controller.addArrowToGraph(infos);
                this.close();
            }

        };
    }

    static class NodeWrapper {
        public NodeWrapper(NodeModel node, String name) {
            this.node = node;
            this.name = name;
        }

        @Override
        public String toString() {
            return String.format("%s (id: %d)", name, node.getIdentifier());
        }

        final NodeModel node;
        final String name;
    }


}

