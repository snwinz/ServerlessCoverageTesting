package gui.view;

import gui.controller.NodeCreatorController;
import gui.controller.dto.NodeInputData;
import gui.model.FunctionInputFormat;
import gui.model.NodeType;
import gui.model.SourceCodeLine;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class NodeCreatorView extends Stage {

    private final NodeCreatorController controller;

    private final TextArea nodeNameArea = new TextArea();
    private final TextArea xArea = new TextArea();
    private final TextArea yArea = new TextArea();
    private final ComboBox<NodeType> nodeTypeCombobox;
    private final TableView<SourceEntryWrapper> tableView = new TableView<>();
    private final TextArea inputVariablesField = new TextArea();
    private FunctionInputFormat functionInputFormat;

    public NodeCreatorView(NodeCreatorController controller, double x, double y) {
        this.controller = controller;
        inputVariablesField.setEditable(false);

        tableView.setMinWidth(1000);
        this.setTitle("Create Node");

        ObservableList<NodeType> options =
                FXCollections.observableArrayList(
                        NodeType.values()
                );
        nodeTypeCombobox = new ComboBox<>(options);
        nodeTypeCombobox.getSelectionModel().selectFirst();
        linkViewsForCombobox(x, y);


        var grid = getStandardNode(x, y);
        var scene = new Scene(grid);
        this.setScene(scene);
    }

    private void linkViewsForCombobox(double x, double y) {
        nodeTypeCombobox.valueProperty().addListener((ov, t, typeOfEnum) -> {
            switch (typeOfEnum) {
                case FUNCTION: {
                    var grid = getFunctionGrid(x, y);
                    var scene = new Scene(grid);
                    this.setScene(scene);
                    break;
                }
                case DATA_STORAGE: {
                    var grid = getDataStorageGrid(x, y);
                    var scene = new Scene(grid);
                    this.setScene(scene);
                    break;
                }
                case STANDARD_NODE: {
                    var grid = getStandardNode(x, y);
                    var scene = new Scene(grid);
                    this.setScene(scene);
                    break;
                }
                default:


            }
        });
    }

    private GridPane getStandardNode(double x, double y) {
        var typeOfNode = new Label("Type of node: ");
        var nameOfNode = new Label("Name of Node");
        var xCoordinates = new Label("X coordinates:");
        var yCoordinates = new Label("Y coordinates:");
        xArea.setText(String.valueOf(x));
        xArea.setPrefRowCount(1);
        yArea.setText(String.valueOf(y));
        yArea.setPrefRowCount(1);
        nodeNameArea.setPrefRowCount(1);
        var grid = new GridPane();
        grid.add(typeOfNode, 1, 1);
        grid.add(nodeTypeCombobox, 2, 1);
        grid.add(nameOfNode, 1, 2);
        grid.add(nodeNameArea, 2, 2);
        grid.add(xCoordinates, 1, 3);
        grid.add(yCoordinates, 1, 4);
        grid.add(xArea, 2, 3);
        grid.add(yArea, 2, 4);
        var createButton = new Button("Add node");
        createButton.setOnAction(getCreateButtonHandler());
        var cancelButton = new Button("Cancel");
        cancelButton.setOnAction(getCancelButtonHandler());

        grid.add(createButton, 1, 6);
        grid.add(cancelButton, 2, 6);

        return grid;
    }

    private GridPane getDataStorageGrid(double x, double y) {
        return getStandardNode(x, y);
    }


    private GridPane getFunctionGrid(double x, double y) {
        functionInputFormat = new FunctionInputFormat();
        inputVariablesField.textProperty().bind(functionInputFormat.textProperty());
        var typeOfNode = new Label("Type of node: ");
        var nameOfNode = new Label("Name of Node");
        var xCoordinates = new Label("X coordinates:");
        var yCoordinates = new Label("Y coordinates:");
        var sourceLabel = new Label("Source code:");
        var inputLabel = new Label("Input values:");
        var inputTooltip = new Tooltip();
        inputTooltip.setText("format of the values being processed by the function:\n" +
                "event.body.myValue: string\n" +
                "event.myValue: [1-9]\n" +
                "event.myValue: [A-Z][0-9]*\n"
        );
        inputLabel.setTooltip(inputTooltip);
        xArea.setText(String.valueOf(x));
        xArea.setPrefRowCount(1);
        yArea.setText(String.valueOf(y));
        yArea.setPrefRowCount(1);
        nodeNameArea.setPrefRowCount(1);
        var grid = new GridPane();
        grid.setMinSize(1000, 200);
        grid.add(typeOfNode, 1, 1);
        grid.add(nodeTypeCombobox, 2, 1);
        grid.add(nameOfNode, 1, 2);
        grid.add(nodeNameArea, 2, 2);
        grid.add(xCoordinates, 1, 3);
        grid.add(yCoordinates, 1, 4);
        grid.add(xArea, 2, 3);
        grid.add(yArea, 2, 4);
        var createButton = new Button("Add node");
        createButton.setOnAction(getCreateButtonHandler());
        var cancelButton = new Button("Cancel");
        cancelButton.setOnAction(getCancelButtonHandler());
        var loadSourceButton = new Button("Use source file");
        loadSourceButton.setOnAction(e -> loadSourceCode());
        var editInputFormatButton = new Button("Edit input format");
        editInputFormatButton.setOnAction(e -> editFunctionInputFormat());

        grid.add(sourceLabel, 1, 5);
        grid.add(tableView, 2, 5);

        grid.add(loadSourceButton, 3, 5);
        grid.add(inputLabel, 1, 6);
        grid.add(inputVariablesField, 2, 6);
        grid.add(editInputFormatButton, 3, 6);
        grid.add(createButton, 1, 7);
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
        FunctionInputFormatView view = new FunctionInputFormatView(functionInputFormat);
        view.show();
    }


    private EventHandler<ActionEvent> getCancelButtonHandler() {
        return event -> controller.close();
    }

    private EventHandler<ActionEvent> getCreateButtonHandler() {
        return event -> {
            var infos = new NodeInputData();
            infos.setNodeType(nodeTypeCombobox.getValue());
            infos.setName(nodeNameArea.getText());
            List<SourceEntryWrapper> sourceList = new ArrayList<>(tableView.getItems());
            var sourceListUnwrapped = sourceList.stream().map(SourceEntryWrapper::getSourceEntry).collect(Collectors.toList());

            infos.setSourceData(sourceListUnwrapped);
            infos.setInputFormats(functionInputFormat);
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
            controller.addNodeToGraph(infos);
        };
    }


    public void setup() {
        this.showAndWait();
    }

    public void setDataForTable(List<SourceCodeLine> tableItems) {
        List<SourceEntryWrapper> tableItemsWrapped = new LinkedList<>();
        for (var item : tableItems) {
            tableItemsWrapped.add(new SourceEntryWrapper(item));
        }

        TableColumn<SourceEntryWrapper, String> sourceCodeColumn = new TableColumn<>("Source code");
        PropertyValueFactory<SourceEntryWrapper, String> sourceCodeFactory = new PropertyValueFactory<>("sourceLine");
        sourceCodeColumn.setCellValueFactory(sourceCodeFactory);

        TableColumn<SourceEntryWrapper, String> defContainerColumn = new TableColumn<>("def tracking statement");
        PropertyValueFactory<SourceEntryWrapper, String> defContainerFactory = new PropertyValueFactory<>("defContainer");
        defContainerColumn.setCellValueFactory(defContainerFactory);

        TableColumn<SourceEntryWrapper, ComboBox> defInfluenceColumn = new TableColumn<>("relation influenced by def");
        PropertyValueFactory<SourceEntryWrapper, ComboBox> defInfluenceFactory = new PropertyValueFactory<>("relationsDefs");
        defInfluenceColumn.setCellValueFactory(defInfluenceFactory);

        TableColumn<SourceEntryWrapper, String> useColumn = new TableColumn<>("use");
        PropertyValueFactory<SourceEntryWrapper, String> useFactory = new PropertyValueFactory<>("use statement");
        useColumn.setCellValueFactory(useFactory);

        TableColumn<SourceEntryWrapper, ComboBox> useInfluenceColumn = new TableColumn<>("relation influencing use");
        PropertyValueFactory<SourceEntryWrapper, ComboBox> useInfluenceFactory = new PropertyValueFactory<>("relationsUses");
        useInfluenceColumn.setCellValueFactory(useInfluenceFactory);

        tableView.getColumns().clear();
        tableView.getColumns().addAll(Arrays.asList(sourceCodeColumn, defContainerColumn, defInfluenceColumn, useColumn, useInfluenceColumn));
        tableView.getColumns().forEach(column -> column.setSortable(false));

        ObservableList<SourceEntryWrapper> tableData = FXCollections.observableList(tableItemsWrapped);

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

    }


}
