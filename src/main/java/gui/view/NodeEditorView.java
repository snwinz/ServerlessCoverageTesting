package gui.view;

import gui.controller.FunctionInputFormatViewController;
import gui.controller.NodeEditorController;
import gui.controller.dto.NodeInputData;
import gui.model.FunctionInputFormat;
import gui.model.Graph;
import shared.model.NodeType;
import gui.model.SourceCodeLine;
import gui.view.graphcomponents.DraggableNode;
import gui.view.wrapper.ComboBoxItemWrap;
import gui.view.wrapper.SourceEntryWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class NodeEditorView extends Stage {

    private final NodeEditorController controller;

    private final TextArea nodeNameArea = new TextArea();
    private final TextArea xArea = new TextArea();
    private final TextArea yArea = new TextArea();
    private final DraggableNode node;
    private final TableView<SourceEntryWrapper> tableView = new TableView<>();
    private final TextArea inputVariablesField = new TextArea();
    private final Graph model;
    private final FunctionInputFormat functionInputFormat;
    private final CheckBox considerDeletesCheckbox = new CheckBox("consider deletes");

    public NodeEditorView(NodeEditorController controller, DraggableNode draggableNode, Graph model) {
        this.node = draggableNode;
        this.controller = controller;
        inputVariablesField.setEditable(false);
        considerDeletesCheckbox.setSelected(false);
        if (draggableNode.getInputFormats() != null) {
            functionInputFormat = draggableNode.getInputFormats().getCopy();
        } else {
            functionInputFormat = new FunctionInputFormat();
        }

        tableView.setMinWidth(1000);
        this.model = model;
        this.setTitle("Edit Node " + draggableNode.getIdentifier());
        GridPane grid = getGrid();
        var scene = new Scene(grid);
        this.setScene(scene);
    }

    private GridPane getGrid() {
        NodeType typeOfEnum = node.getType();
        GridPane grid;
        switch (typeOfEnum) {
            case FUNCTION: {
                grid = getFunctionGrid(node);
                break;
            }
            case DATA_STORAGE:
                grid = getDataStorageGrid(node);
                break;
            case STANDARD_NODE:
            default:
                grid = getStandardNodeGrid(node);
                break;

        }
        return grid;

    }

    private GridPane getStandardNodeGrid(DraggableNode node) {
        var grid = new GridPane();
        var typeOfNode = new Label("Type of node: ");
        var nodeTypeText = new Label(node.getType().toString());
        var nameOfNode = new Label("Name of Node");
        var xCoordinates = new Label("X coordinates:");
        var yCoordinates = new Label("Y coordinates:");
        inputVariablesField.textProperty().bind(functionInputFormat.textProperty());
        xArea.setText(String.valueOf(node.layoutXProperty().doubleValue()));
        xArea.setPrefRowCount(1);
        yArea.setText(String.valueOf(node.layoutYProperty().doubleValue()));
        yArea.setPrefRowCount(1);
        nodeNameArea.setPrefRowCount(1);
        nodeNameArea.setText(node.getName());
        grid.add(typeOfNode, 1, 1);
        grid.add(nodeTypeText, 2, 1);
        grid.add(nameOfNode, 1, 2);
        grid.add(nodeNameArea, 2, 2);
        grid.add(xCoordinates, 1, 3);
        grid.add(yCoordinates, 1, 4);
        grid.add(xArea, 2, 3);
        grid.add(yArea, 2, 4);
        var updateButton = new Button("Update node");
        updateButton.setOnAction(getUpdateButtonHandler());
        var cancelButton = new Button("Cancel");
        cancelButton.setOnAction(getCancelButtonHandler());
        grid.add(updateButton, 1, 6);
        grid.add(cancelButton, 2, 6);
        return grid;
    }

    private GridPane getDataStorageGrid(DraggableNode node) {
        return getStandardNodeGrid(node);
    }

    private GridPane getFunctionGrid(DraggableNode node) {
        inputVariablesField.textProperty().bind(functionInputFormat.textProperty());
        var typeOfNode = new Label("Type of node: ");
        var nodeTypeText = new Label(node.getType().toString());
        var nameOfNode = new Label("Name of Node");
        var xCoordinates = new Label("X coordinates:");
        var yCoordinates = new Label("Y coordinates:");
        var sourceLabel = new Label("Source code:");
        var inputLabel = new Label("Input values:");

        xArea.setText(String.valueOf(node.layoutXProperty().doubleValue()));
        xArea.setPrefRowCount(1);
        yArea.setText(String.valueOf(node.layoutYProperty().doubleValue()));
        yArea.setPrefRowCount(1);
        nodeNameArea.setPrefRowCount(1);
        nodeNameArea.setText(node.getName());
        var grid = new GridPane();
        grid.add(typeOfNode, 1, 1);
        grid.add(nodeTypeText, 2, 1);
        grid.add(nameOfNode, 1, 2);
        grid.add(nodeNameArea, 2, 2);
        grid.add(xCoordinates, 1, 3);
        grid.add(yCoordinates, 1, 4);
        grid.add(xArea, 2, 3);
        grid.add(yArea, 2, 4);
        var updateButton = new Button("Update node");
        updateButton.setOnAction(getUpdateButtonHandler());
        var cancelButton = new Button("Cancel");
        cancelButton.setOnAction(getCancelButtonHandler());
        var loadSourceButton = new Button("Use source file");
        loadSourceButton.setOnAction(e -> loadSourceCode());
        var editInputFormatButton = new Button("Edit input format");
        editInputFormatButton.setOnAction(e -> editFunctionInputFormat());
        var instrumentSourceButton = new Button("Instrument source code");
        var analyzeSourceButton = new Button("Analyze source code");

        var analyzeBox = new HBox(analyzeSourceButton, considerDeletesCheckbox);
        var containerForSourceButtons = new VBox(loadSourceButton, instrumentSourceButton, analyzeBox);
        instrumentSourceButton.setOnAction(getSourceInstrumentationHandler());
        analyzeSourceButton.setOnAction(getAnalyzeHandler()
        );
        grid.add(sourceLabel, 1,
                5);
        setDataForTable(node.getSourceList());

        grid.add(tableView, 2, 5);
        grid.add(containerForSourceButtons, 3, 5);


        grid.add(inputLabel, 1, 6);
        grid.add(inputVariablesField, 2, 6);
        grid.add(editInputFormatButton, 3, 6);

        grid.add(updateButton, 1, 7);
        grid.add(cancelButton, 2, 7);
        return grid;
    }

    private void loadSourceCode() {

        var fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        var file = fileChooser.showOpenDialog(this);
        controller.setSourceFile(file);

    }

    private void editFunctionInputFormat() {
        FunctionInputFormatViewController controller = new FunctionInputFormatViewController();
        controller.setup(functionInputFormat);
    }


    private EventHandler<ActionEvent> getCancelButtonHandler() {
        return event -> this.close();
    }

    private EventHandler<ActionEvent> getUpdateButtonHandler() {
        return event -> {
            var infos = new NodeInputData();
            infos.setNodeType(node.getType());
            infos.setName(nodeNameArea.getText());
            List<SourceEntryWrapper> sourceList = new ArrayList<>(tableView.getItems());
            var sourceListUnwrapped = sourceList.stream().map(SourceEntryWrapper::getSourceEntry).collect(Collectors.toList());
            infos.setSourceData(sourceListUnwrapped);
            infos.setInputFormats(functionInputFormat);

            infos.setId(node.getIdentifier());
            try {
                var x = Double.parseDouble(xArea.getText());
                var y = Double.parseDouble(yArea.getText());
                infos.setX(x);
                infos.setY(y);
            } catch
            (NumberFormatException e) {
                System.err.printf("Values x: %s and y: %s could not be parsed. Default value 100 for each is used.", xArea.getText(), yArea.getText());
                infos.setX(100);
                infos.setY(100);
            }
            controller.updateNodeToGraph(infos);
            this.close();
        };
    }

    private EventHandler<ActionEvent> getSourceInstrumentationHandler() {
        return event -> {
            List<SourceEntryWrapper> sourceList = new ArrayList<>(tableView.getItems());
            var sourceListUnwrapped = sourceList.stream().map(SourceEntryWrapper::getSourceEntry).collect(Collectors.toList());
            controller.instrumentSourceCode(sourceListUnwrapped, node.getIdentifier());
        };
    }

    private EventHandler<ActionEvent> getAnalyzeHandler() {
        return event -> {
            List<SourceEntryWrapper> sourceList = new ArrayList<>(tableView.getItems());
            var sourceListUnwrapped = sourceList.stream().map(SourceEntryWrapper::getSourceEntry).collect(Collectors.toList());
            controller.analyzeSource(sourceListUnwrapped, node.getIdentifier(), this, considerDeletesCheckbox.isSelected());
        };
    }

    public void setup() {
        this.showAndWait();
    }

    public void setDataForTable(List<SourceCodeLine> tableItems) {
        List<SourceEntryWrapper> tableItemsWrapped = new LinkedList<>();
        for (var item : tableItems) {
            List<Long> neighbours = model.getNeighboursArrowsOfNode(node.getIdentifier());
            List<Long> nodes = model.getNeighbourNodesOfNode(node.getIdentifier());
            nodes.add(0, node.getIdentifier());
            nodes.addAll(model.getNodeIDs());
            nodes = nodes.stream().distinct().collect(Collectors.toList());
            List<Long> arrows = model.getArrowIDs();
            tableItemsWrapped.add(new SourceEntryWrapper(item, neighbours, nodes, arrows));
        }
        ObservableList<SourceEntryWrapper> tableData = FXCollections.observableList(tableItemsWrapped);

        TableColumn<SourceEntryWrapper, String> sourceCodeColumn = new TableColumn<>("Source code");
        PropertyValueFactory<SourceEntryWrapper, String> sourceCodeFactory = new PropertyValueFactory<>("sourceLine");
        sourceCodeColumn.setCellValueFactory(sourceCodeFactory);

        TableColumn<SourceEntryWrapper, String> defContainerColumn = new TableColumn<>("def tracking statement");
        PropertyValueFactory<SourceEntryWrapper, String> defContainerFactory = new PropertyValueFactory<>("defContainer");
        defContainerColumn.setCellValueFactory(defContainerFactory);

        TableColumn<SourceEntryWrapper, ComboBox<ComboBoxItemWrap<String>>> defInfluenceColumn = new TableColumn<>("relation influenced by def");
        PropertyValueFactory<SourceEntryWrapper, ComboBox<ComboBoxItemWrap<String>>> defInfluenceFactory = new PropertyValueFactory<>("relationsDefs");
        defInfluenceColumn.setCellValueFactory(defInfluenceFactory);

        TableColumn<SourceEntryWrapper, String> useColumn = new TableColumn<>("use statement");
        PropertyValueFactory<SourceEntryWrapper, String> useFactory = new PropertyValueFactory<>("use");
        useColumn.setCellValueFactory(useFactory);

        TableColumn<SourceEntryWrapper, ComboBox<ComboBoxItemWrap<String>>> useInfluenceColumn = new TableColumn<>("relation influencing use");
        PropertyValueFactory<SourceEntryWrapper, ComboBox<ComboBoxItemWrap<String>>> useInfluenceFactory = new PropertyValueFactory<>("relationsUses");
        useInfluenceColumn.setCellValueFactory(useInfluenceFactory);

        TableColumn<SourceEntryWrapper, String> nodeCalledByExecutionColumn = new TableColumn<>("node called by statement");
        PropertyValueFactory<SourceEntryWrapper, String> nodeCallFactory = new PropertyValueFactory<>("nodesCallableForAllNodes");
        nodeCalledByExecutionColumn.setCellValueFactory(nodeCallFactory);

        TableColumn<SourceEntryWrapper, String> relationCalledByExecutionColumn = new TableColumn<>("relation called by statement");
        PropertyValueFactory<SourceEntryWrapper, String> relationCallFactory = new PropertyValueFactory<>("relationsCallableForAllRelations");
        relationCalledByExecutionColumn.setCellValueFactory(relationCallFactory);

        TableColumn<SourceEntryWrapper, String> replaceLineColumn = new TableColumn<>("replace line");
        PropertyValueFactory<SourceEntryWrapper, String> replaceLineFactory = new PropertyValueFactory<>("replaceLine");
        replaceLineColumn.setCellValueFactory(replaceLineFactory);

        tableView.getColumns().clear();
        tableView.getColumns().addAll(Arrays.asList(sourceCodeColumn, defContainerColumn, defInfluenceColumn, useColumn,
                useInfluenceColumn, nodeCalledByExecutionColumn, relationCalledByExecutionColumn, replaceLineColumn));
        tableView.getColumns().forEach(column -> column.setSortable(false));

        tableView.setItems(tableData);
        tableView.setEditable(true);

        defContainerColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        defContainerColumn.setOnEditCommit(event -> {
            event.getRowValue().setDefContainer(event.getNewValue());
            event.getRowValue().getDefWrapper().activateAllOnly();
            event.getRowValue().getDefWrapper().refreshText();
        });
        useColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        useColumn.setOnEditCommit(event -> {
            event.getRowValue().setUse(event.getNewValue());
            event.getRowValue().getUseWrapper().activateAllOnly();
            event.getRowValue().getUseWrapper().refreshText();
        });
        replaceLineColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        replaceLineColumn.setOnEditCommit(event -> event.getRowValue().setReplaceLine(event.getNewValue()));


    }


}
