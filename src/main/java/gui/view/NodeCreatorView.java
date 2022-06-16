package gui.view;

import gui.controller.FunctionInputFormatViewController;
import gui.controller.NodeCreatorController;
import gui.controller.dto.NodeInputData;
import gui.model.SourceCodeLine;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import logic.model.FunctionInputFormat;
import shared.model.NodeType;

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
    private final ComboBox<NodeType> nodeTypeComboBox;
    private final TableView<SourceEntryWrapper> tableView = new TableView<>();
    private FunctionInputFormat functionInputFormat;

    public NodeCreatorView(NodeCreatorController controller, double x, double y) {
        this.controller = controller;
        tableView.setMinWidth(1000);
        this.setTitle("Create Node");

        ObservableList<NodeType> options =
                FXCollections.observableArrayList(
                        NodeType.values()
                );
        nodeTypeComboBox = new ComboBox<>(options);
        nodeTypeComboBox.getSelectionModel().selectFirst();
        linkViewsForComboBox(x, y);


        var grid = getStandardNode(x, y);
        var scene = new Scene(grid);
        this.setScene(scene);
    }

    private void linkViewsForComboBox(double x, double y) {
        nodeTypeComboBox.valueProperty().addListener((ov, t, typeOfEnum) -> {
            switch (typeOfEnum) {
                case FUNCTION -> {
                    var grid = getFunctionGrid(x, y);
                    var scene = new Scene(grid);
                    this.setScene(scene);
                }
                case DATA_STORAGE -> {
                    var grid = getDataStorageGrid(x, y);
                    var scene = new Scene(grid);
                    this.setScene(scene);
                }
                case STANDARD_NODE -> {
                    var grid = getStandardNode(x, y);
                    var scene = new Scene(grid);
                    this.setScene(scene);
                }
                default -> {
                }
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
        grid.add(nodeTypeComboBox, 2, 1);
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
        var typeOfNode = new Label("Type of node: ");
        var nameOfNode = new Label("Name of Node");
        var xCoordinates = new Label("X coordinates:");
        var yCoordinates = new Label("Y coordinates:");
        var sourceLabel = new Label("Source code:");
        var inputTooltip = new Tooltip();
        inputTooltip.setText("""
                format of the values being processed by the function:
                event.body.myValue: string
                event.myValue: [1-9]
                event.myValue: [A-Z][0-9]*
                """
        );
        xArea.setText(String.valueOf(x));
        xArea.setPrefRowCount(1);
        yArea.setText(String.valueOf(y));
        yArea.setPrefRowCount(1);
        nodeNameArea.setPrefRowCount(1);
        var grid = new GridPane();
        grid.setMinSize(1000, 200);
        grid.add(typeOfNode, 1, 1);
        grid.add(nodeTypeComboBox, 2, 1);
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
        FunctionInputFormatViewController controller = new FunctionInputFormatViewController();
        controller.setup(functionInputFormat);
    }


    private EventHandler<ActionEvent> getCancelButtonHandler() {
        return event -> controller.close();
    }

    private EventHandler<ActionEvent> getCreateButtonHandler() {
        return event -> {
            var info = new NodeInputData();
            info.setNodeType(nodeTypeComboBox.getValue());
            info.setName(nodeNameArea.getText());
            if(nodeTypeComboBox.getValue().equals(NodeType.FUNCTION)){
                List<SourceEntryWrapper> sourceList = new ArrayList<>(tableView.getItems());
                var sourceListUnwrapped = sourceList.stream().map(SourceEntryWrapper::getSourceEntry).collect(Collectors.toList());
                info.setSourceData(sourceListUnwrapped);
                info.setInputFormats(functionInputFormat);
            }
            try {
                var x = Double.parseDouble(xArea.getText());
                var y = Double.parseDouble(yArea.getText());
                info.setX(x);
                info.setY(y);
            } catch
            (NumberFormatException e) {
                System.err.printf("Values x: %s and y: %s could not be parsed. Default value 100 for each is used.", xArea.getText(), yArea.getText());
                info.setX(100);
                info.setY(100);
            }
            controller.addNodeToGraph(info);
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

        TableColumn<SourceEntryWrapper, ComboBox<ComboBoxItemWrap<String>> > defInfluenceColumn = new TableColumn<>("relation influenced by def");
        PropertyValueFactory<SourceEntryWrapper, ComboBox<ComboBoxItemWrap<String>> > defInfluenceFactory = new PropertyValueFactory<>("relationsDefs");
        defInfluenceColumn.setCellValueFactory(defInfluenceFactory);

        TableColumn<SourceEntryWrapper, String> useColumn = new TableColumn<>("use");
        PropertyValueFactory<SourceEntryWrapper, String> useFactory = new PropertyValueFactory<>("use statement");
        useColumn.setCellValueFactory(useFactory);

        TableColumn<SourceEntryWrapper, ComboBox<ComboBoxItemWrap<String>> > useInfluenceColumn = new TableColumn<>("relation influencing use");
        PropertyValueFactory<SourceEntryWrapper, ComboBox<ComboBoxItemWrap<String>> > useInfluenceFactory = new PropertyValueFactory<>("relationsUses");
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
