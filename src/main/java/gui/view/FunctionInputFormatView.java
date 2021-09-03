package gui.view;

import gui.model.FunctionInputFormat;
import gui.model.IntegerInput;
import gui.model.StringInput;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Arrays;

public class FunctionInputFormatView extends Stage {

    private final FunctionInputFormat functionInputFormat;
    private TableView<IntegerInput> tableViewInteger = new TableView<>();
    private TableView<StringInput> tableViewString = new TableView<>();
    private final TextArea textAreaJson = new TextArea();

    public FunctionInputFormatView(FunctionInputFormat inputFormats) {
        this.functionInputFormat = inputFormats;
        this.initModality(Modality.APPLICATION_MODAL);
        this.setTitle("Edit input format of node");
        refreshView();
    }

    private GridPane getGrid() {

        var grid = new GridPane();
        final VBox stringInputTable = getStringInputTable();
        grid.add(stringInputTable, 1, 1);
        final VBox integerInputTable = getIntegerInputTable();
        grid.add(integerInputTable, 2, 1);
        final VBox jsonInputTable = getTextArea();

        grid.add(jsonInputTable, 3, 1);

        final Node controlButton = getControlButton();
        grid.add(controlButton, 2, 2);

        return grid;
    }

    private HBox getControlButton() {
        Button updateButton = new Button("Update input format");
        updateButton.setOnAction(e -> {
            updateFunctionInputFormat();
            this.close();
        });

        final HBox hbox = new HBox();
        hbox.getChildren().addAll(updateButton);
        return hbox;
    }

    private void updateFunctionInputFormat() {
        this.functionInputFormat.setJSONInput(textAreaJson.getText());
        this.functionInputFormat.setStringInput(tableViewString.getItems());
        this.functionInputFormat.setIntegerInput(tableViewInteger.getItems());
    }

    private VBox getTextArea() {
        textAreaJson.setText(functionInputFormat.getJSONInput());
        Label heading = new Label("Constant Json used");
        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new
                Insets(10, 0, 0, 10));
        vbox.getChildren().
                addAll(heading, textAreaJson);
        return vbox;
    }

    private VBox getStringInputTable() {
        tableViewString = new TableView<>();
        tableViewString.setEditable(true);
        tableViewString.getSelectionModel().
                setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<StringInput,String> keyCol = new TableColumn<>("Key");
        TableColumn<StringInput,String> regexCol = new TableColumn<>("Regex of value");
        tableViewString.getColumns().addAll(Arrays.asList(keyCol, regexCol));
        final ObservableList<StringInput> dataStringInput = FXCollections.observableArrayList(
                functionInputFormat.getStringInput()
        );

        keyCol.setCellValueFactory(
                new PropertyValueFactory<>("key")
        );
        regexCol.setCellValueFactory(
                new PropertyValueFactory<>("jsonValue")
        );
        tableViewString.setItems(dataStringInput);


        final TextField addStringInputKey = new TextField();
        addStringInputKey.setPromptText("Key");
        addStringInputKey.setMaxWidth(keyCol.getPrefWidth());
        final TextField addStringInputJSON = new TextField();
        addStringInputJSON.setMaxWidth(regexCol.getPrefWidth());
        addStringInputJSON.setPromptText("regex, e.g. \".*\" for any String");
        final Button addButton = new Button("Add");

        addButton.setOnAction(e -> {
            var newStringInputValue = new StringInput(
                    addStringInputKey.getText(),
                    addStringInputJSON.getText());
            functionInputFormat.addStringInputValue(newStringInputValue);
            refreshView();
        });
        final Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e ->
                deleteStringInputEntries());
        final HBox hb = new HBox();
        hb.getChildren().
                addAll(addStringInputKey, addStringInputJSON, addButton, deleteButton);
        hb.setSpacing(3);
        Label heading = new Label("String input values as regex");
        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new
                Insets(10, 0, 0, 10));
        vbox.getChildren().
                addAll(heading, tableViewString, hb);
        return vbox;
    }

    private VBox getIntegerInputTable() {
        tableViewInteger = new TableView<>();
        tableViewInteger.setEditable(true);
        tableViewInteger.getSelectionModel().
                setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<IntegerInput,String> keyCol = new TableColumn<>("Key");
        TableColumn<IntegerInput,String> minValueCol = new TableColumn<>("Min Value of Integer");
        TableColumn<IntegerInput,String> maxValueCol = new TableColumn<>("Max Value of Integer");
        tableViewInteger.getColumns().addAll(Arrays.asList(keyCol, minValueCol, maxValueCol));

        final ObservableList<IntegerInput> dataIntegerInput = FXCollections.observableArrayList(
                functionInputFormat.getIntegerInput()
        );

        keyCol.setCellValueFactory(
                new PropertyValueFactory<>("key")
        );
        minValueCol.setCellValueFactory(
                new PropertyValueFactory<>("minValue")
        );
        maxValueCol.setCellValueFactory(
                new PropertyValueFactory<>("maxValue")
        );
        tableViewInteger.setItems(dataIntegerInput);


        final TextField inputKeyField = new TextField();
        inputKeyField.setPromptText("Key");
        inputKeyField.setMaxWidth(keyCol.getPrefWidth());

        final TextField addMinValueField = new TextField();
        addMinValueField.setMaxWidth(minValueCol.getPrefWidth());
        addMinValueField.setPromptText("min value");

        final TextField addMaxValueField = new TextField();
        addMaxValueField.setMaxWidth(maxValueCol.getPrefWidth());
        addMaxValueField.setPromptText("max value");

        final Button addButton = new Button("Add");
        addButton.setOnAction(e -> {
            IntegerInput integerInputValue = new IntegerInput(inputKeyField.getText(), addMinValueField.getText(), addMaxValueField.getText());
            functionInputFormat.addIntegerInputValue(integerInputValue);
            refreshView();
        });


        final Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e ->
                deleteInputInputEntries());
        final HBox hb = new HBox();
        hb.getChildren().
                addAll(inputKeyField, addMinValueField, addMaxValueField, addButton, deleteButton);
        hb.setSpacing(3);
        Label heading = new Label("Integer input");
        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new
                Insets(10, 0, 0, 10));
        vbox.getChildren().
                addAll(heading, tableViewInteger, hb);
        return vbox;
    }


    private void deleteStringInputEntries() {
        var items = tableViewString.getSelectionModel().getSelectedItems();
        for (var item : items) {
            functionInputFormat.delete(item);
        }
        refreshView();
    }

    private void deleteInputInputEntries() {
        var items = tableViewInteger.getSelectionModel().getSelectedItems();
        for (var item : items) {
            functionInputFormat.delete(item);
        }
        refreshView();
    }


    private void refreshView() {
        var grid = getGrid();
        var scene = new Scene(grid);
        this.setScene(scene);
    }



}






