package gui.view;

import gui.controller.FunctionInputFormatViewController;
import gui.model.FunctionInputFormat;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import shared.model.input.*;

import java.util.Arrays;
import java.util.stream.Collectors;

public class FunctionInputFormatView extends Stage {

    private final FunctionInputFormat functionInputFormat;
    private final FunctionInputFormatViewController controller;
    private TableView<GeneralInput> tableViewString = new TableView<>();
    private final ComboBox<TypOfJsonContent> comboBoxTypOfKey;
    private final ComboBox<GeneralInput> comboBoxOfParents = new ComboBox<>();

    public FunctionInputFormatView(FunctionInputFormat inputFormats, FunctionInputFormatViewController controller) {
        this.controller = controller;
        this.functionInputFormat = inputFormats;
        this.initModality(Modality.APPLICATION_MODAL);
        this.setTitle("Edit input format of node");
        ObservableList<TypOfJsonContent> options =
                FXCollections.observableArrayList(
                        TypOfJsonContent.KEY_DYNAMIC_VALUE, TypOfJsonContent.KEY_INTEGER_VALUE, TypOfJsonContent.ARRAY_KEY,
                        TypOfJsonContent.PARENT_KEY, TypOfJsonContent.KEY_CONSTANT_VALUE, TypOfJsonContent.CONSTANT_VALUE
                );
        comboBoxTypOfKey = new ComboBox<>(options);
        comboBoxTypOfKey.getSelectionModel().selectFirst();
        setupGUI();
        refreshData();
    }

    private Pane getGrid() {
        var pane = new Pane();
        pane.setPrefWidth(900);
        final VBox stringInputTable = getGeneralInputTable();
        pane.getChildren().addAll(stringInputTable);
        return pane;
    }

    private HBox getControlButtons() {
        Button updateButton = new Button("Update input format");
        updateButton.setOnAction(e -> {
            updateFunctionInputFormat();
            controller.closeView();
        });
        Button showPotentialJson = new Button("Show potential input");
        showPotentialJson.setOnAction(e -> controller.showPotentialInput(functionInputFormat));
        Button showPotentialJsonWithContent = new Button("Show potential input with content");
        showPotentialJsonWithContent.setOnAction(e -> controller.showPotentialInputWithContent(functionInputFormat));
        final HBox hbox = new HBox();
        hbox.getChildren().addAll(showPotentialJson, showPotentialJsonWithContent, updateButton);
        return hbox;
    }

    private void updateFunctionInputFormat() {
        this.functionInputFormat.setStringInput(tableViewString.getItems());
    }

    private void refreshData() {
        final ObservableList<GeneralInput> dataGeneralInput = FXCollections.observableArrayList(
                functionInputFormat.getGeneralInputs()
        );
        tableViewString.setItems(dataGeneralInput);

        final ObservableList<GeneralInput> dataPotentialParents = FXCollections.observableArrayList(
                functionInputFormat.getGeneralInputs().stream().filter(entry -> entry instanceof ParentKeyInput || entry instanceof ArrayKeyInput).collect(Collectors.toList())
        );
        comboBoxOfParents.setItems(dataPotentialParents);

    }

    private VBox getGeneralInputTable() {
        tableViewString = new TableView<>();
        tableViewString.prefWidthProperty().bind(this.widthProperty());
        tableViewString.setEditable(true);
        tableViewString.getSelectionModel().
                setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<GeneralInput, String> keyCol = new TableColumn<>("Key");
        TableColumn<GeneralInput, String> regexCol = new TableColumn<>("Regex of value");
        TableColumn<GeneralInput, String> minValueCol = new TableColumn<>("Min Value of Integer");
        TableColumn<GeneralInput, String> maxValueCol = new TableColumn<>("Max Value of Integer");
        TableColumn<GeneralInput, String> constantValueCol = new TableColumn<>("Constant value");

        TableColumn<GeneralInput, String> parentCol = new TableColumn<>("parent");
        TableColumn<GeneralInput, String> entryIdCol = new TableColumn<>("ID");
        tableViewString.getColumns().addAll(Arrays.asList(keyCol, regexCol, minValueCol, maxValueCol, constantValueCol, parentCol, entryIdCol));

        setMappingForColumns(keyCol, regexCol, minValueCol, maxValueCol, constantValueCol, parentCol, entryIdCol);


        final TextField addStringInputKey = new TextField();
        addStringInputKey.setPromptText("Key");
        addStringInputKey.setPrefWidth(300);

        final TextField addStringInputJSON = new TextField();
        addStringInputJSON.setPrefWidth(300);
        addStringInputJSON.setPromptText("regex, e.g. \"[a-zA-Z0-9]*\" for any String with common characters");

        final TextField addMinValueField = new TextField();
        addMinValueField.setMaxWidth(minValueCol.getPrefWidth());
        addMinValueField.setPromptText("min value");

        final TextField addMaxValueField = new TextField();
        addMaxValueField.setMaxWidth(maxValueCol.getPrefWidth());
        addMaxValueField.setPromptText("max value");

        final Button addButton = new Button("Add");

        final CheckBox valueInJson = new CheckBox("save json value as string");
        valueInJson.setSelected(false);

        final CheckBox asBase64 = new CheckBox("save json value as base64");
        asBase64.setSelected(false);

        addButton.setOnAction(e -> addNewJSONEntry(addStringInputKey, addStringInputJSON, addMinValueField, addMaxValueField, valueInJson,asBase64));

        final Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e ->
                deleteStringInputEntries());



        final HBox nodeOfKeyControl = new HBox();
        nodeOfKeyControl.getChildren().
                addAll(addStringInputKey, addButton, deleteButton, asBase64);
        nodeOfKeyControl.setSpacing(3);


        final HBox boxForInput = new HBox();
        boxForInput.getChildren().
                addAll(addStringInputJSON);
        boxForInput.setSpacing(3);


        final VBox inputOptions = new VBox();
        Label heading = new Label("String input values as regex");
        comboBoxTypOfKey.valueProperty().addListener((observable, oldValue, newValue) -> {
            switch (oldValue) {
                case KEY_CONSTANT_VALUE:
                    addStringInputJSON.setPromptText("regex, e.g. \"[a-zA-Z0-9]*\" for any String with common characters");
                    boxForInput.getChildren().remove(addStringInputJSON);
                    break;
                case CONSTANT_VALUE:
                    nodeOfKeyControl.getChildren().add(0, addStringInputKey);
                    addStringInputJSON.setPromptText("regex, e.g. \"[a-zA-Z0-9]*\" for any String with common characters");
                    boxForInput.getChildren().remove(addStringInputJSON);
                    break;
                case KEY_INTEGER_VALUE:
                    boxForInput.getChildren().removeAll(addMinValueField, addMaxValueField);
                    break;
                case KEY_DYNAMIC_VALUE:
                    addStringInputJSON.setText("");
                    boxForInput.getChildren().removeAll(addStringInputJSON);
                    nodeOfKeyControl.getChildren().removeAll(asBase64);
                    break;
                case PARENT_KEY:
                    nodeOfKeyControl.getChildren().removeAll(valueInJson,asBase64);
                case ARRAY_KEY:
                default:
                    break;
            }
            switch (newValue) {
                case KEY_CONSTANT_VALUE:
                    addStringInputJSON.setPromptText("JSON text");
                    boxForInput.getChildren().addAll(addStringInputJSON);
                    nodeOfKeyControl.getChildren().removeAll(valueInJson, asBase64);
                    break;
                case CONSTANT_VALUE:
                    nodeOfKeyControl.getChildren().remove(addStringInputKey);
                    addStringInputJSON.setPromptText("constant value");
                    boxForInput.getChildren().addAll(addStringInputJSON);
                    nodeOfKeyControl.getChildren().removeAll(valueInJson, asBase64);
                    break;
                case KEY_INTEGER_VALUE:
                    boxForInput.getChildren().addAll(addMinValueField, addMaxValueField);
                    nodeOfKeyControl.getChildren().removeAll(valueInJson, asBase64);
                    break;
                case KEY_DYNAMIC_VALUE:
                    addStringInputJSON.setText("");
                    addStringInputJSON.setPromptText("regex, e.g. \"[a-zA-Z0-9]*\" for any String with common characters");
                    boxForInput.getChildren().addAll(addStringInputJSON);
                    nodeOfKeyControl.getChildren().remove(valueInJson);
                    nodeOfKeyControl.getChildren().addAll(asBase64);
                    break;
                case ARRAY_KEY:
                    nodeOfKeyControl.getChildren().removeAll(valueInJson, asBase64);
                    comboBoxOfParents.getSelectionModel().select(null);
                    break;
                case PARENT_KEY:
                    nodeOfKeyControl.getChildren().addAll(valueInJson,asBase64);
                    comboBoxOfParents.getSelectionModel().select(null);
                default:
                    break;
            }
        });


        inputOptions.setSpacing(5);
        inputOptions.setPadding(new
                Insets(10, 0, 0, 10));
        final Node controlButton = getControlButtons();
        inputOptions.getChildren().
                addAll(heading, tableViewString, nodeOfKeyControl, comboBoxTypOfKey, boxForInput, comboBoxOfParents, controlButton);
        return inputOptions;
    }

    private void addNewJSONEntry(TextField addStringInputKey, TextField addStringInputJSON, TextField addMinValueField, TextField addMaxValueField, CheckBox valueInJson, CheckBox asBase64) {
        GeneralInput generatedInputValue;
        var key = addStringInputKey.getText();

        var selection = comboBoxTypOfKey.getValue();
        generatedInputValue = switch (selection) {
            case KEY_CONSTANT_VALUE -> new ConstantKeyValue(key, addStringInputJSON.getText());
            case CONSTANT_VALUE -> new ConstantValue(addStringInputJSON.getText());
            case KEY_INTEGER_VALUE ->
                    new IntegerInput(key, Integer.valueOf(addMinValueField.getText()), Integer.valueOf(addMaxValueField.getText()));
            case KEY_DYNAMIC_VALUE -> new DynamicKeyValue(key, addStringInputJSON.getText(), asBase64.isSelected());
            case ARRAY_KEY -> new ArrayKeyInput(key);
            case PARENT_KEY -> new ParentKeyInput(key, valueInJson.isSelected(), asBase64.isSelected());
            default -> new GeneralInput(key);
        };

        Integer freeEntryID = functionInputFormat.getGeneralInputs().stream().mapToInt(GeneralInput::getEntryID).max().orElse(0) + 1;
        generatedInputValue.setEntryID(freeEntryID);
        var parent = comboBoxOfParents.getValue();
        if (parent != null) {
            generatedInputValue.setParentId(parent.getEntryID());
        }
        functionInputFormat.addGeneralInputValue(generatedInputValue);
        refreshData();
    }


    private void setMappingForColumns(TableColumn<GeneralInput, String> keyCol, TableColumn<GeneralInput,
            String> regexCol, TableColumn<GeneralInput, String> minValueCol, TableColumn<GeneralInput,
            String> maxValueCol, TableColumn<GeneralInput, String> constantValueCol, TableColumn<GeneralInput,
            String> parentCol, TableColumn<GeneralInput, String> entryIdCol) {
        keyCol.setCellValueFactory(
                new PropertyValueFactory<>("key")
        );
        regexCol.setCellValueFactory(
                new PropertyValueFactory<>("dynamicValue")
        );
        minValueCol.setCellValueFactory(
                new PropertyValueFactory<>("minValue")
        );
        maxValueCol.setCellValueFactory(
                new PropertyValueFactory<>("maxValue")
        );
        constantValueCol.setCellValueFactory(
                new PropertyValueFactory<>("constantValue")
        );
        parentCol.setCellValueFactory(
                new PropertyValueFactory<>("parentId")
        );
        entryIdCol.setCellValueFactory(
                new PropertyValueFactory<>("entryID")
        );
    }


    private void deleteStringInputEntries() {
        var items = tableViewString.getSelectionModel().getSelectedItems();
        for (var item : items) {
            functionInputFormat.delete(item);
        }
        refreshData();
    }

    private void setupGUI() {
        var grid = getGrid();
        var scene = new Scene(grid);
        this.setScene(scene);
    }

    enum TypOfJsonContent {
        PARENT_KEY("Parent key of entries"), ARRAY_KEY("Array key of entries"), KEY_DYNAMIC_VALUE("Key with dynamic value"),
        KEY_INTEGER_VALUE("Key with integer content"), KEY_CONSTANT_VALUE("Constant key and value"),
        CONSTANT_VALUE("Constant value");
        private final String description;

        TypOfJsonContent(String s) {
            this.description = s;
        }

        @Override
        public String toString() {
            return this.description;
        }
    }

}