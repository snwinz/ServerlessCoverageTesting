package gui.view;

import gui.controller.DynamicTCSelectionController;
import gui.model.Graph;
import gui.view.wrapper.CheckboxWrapper;
import gui.view.wrapper.Commands;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import logic.model.ServerlessFunction;
import logic.model.TestSuite;
import logic.model.Testcase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class DynamicTCSelectionView extends Stage {


    private final TestSuite testSuite;
    private final Graph model;
    private final List<CheckboxWrapper<Testcase>> availableTestcases = new ArrayList<>();
    private final DynamicTCSelectionController controller;
    private final Spinner<Integer> numberOfTries = new Spinner<>(1, 100, 2);
    private final Spinner<Double> probChangeGoodData = new Spinner<>(0, 1, 0.1, 0.01);
    private final Spinner<Double> probEntryUndefined = new Spinner<>(0, 1, 0.1, 0.01);
    private final Spinner<Double> probSimilarInputAsValue = new Spinner<>(0, 1, 0.1, 0.01);
    private final Spinner<Double> probRandomInputAsValue = new Spinner<>(0, 1, 0.1, 0.01);
    private final Spinner<Double> probSimilarOutputAsValue = new Spinner<>(0, 1, 0.1, 0.01);
    private final Spinner<Double> probRandomOutputAsValue = new Spinner<>(0, 1, 0.1, 0.01);
    private final Spinner<Double> probSameValueEverywhere = new Spinner<>(0, 1, 0.1, 0.01);
    private final TextField resetFunctionName = new TextField();
    private final TextField regionAWS = new TextField();

    public DynamicTCSelectionView(TestSuite testSuite, Graph model, DynamicTCSelectionController controller) {
        this.controller = controller;
        this.testSuite = testSuite;
        this.model = model;
        this.setTitle("Select test targets for dynamic test case generation");
        createView();
        getConfigProperties();
    }


    private ScrollPane getScrollPane() {
        ScrollPane scrollpane = new ScrollPane();
        var grid = new GridPane();
        scrollpane.setContent(grid);
        createRowForRunOptions(grid);


        var startButton = new Button("Start");
        var cancelButton = new Button("Cancel");
        startButton.setOnAction(getActionEventEventHandlerForStartButton());
        cancelButton.setOnAction(e -> controller.closeView());
        grid.add(startButton, 1, 1);
        grid.add(cancelButton, 2, 1);

        var resetLabel = new Label("Reset function:");
        HBox resetFunctionBox = new HBox();
        var startResetButton = new Button("Start reset");
        startResetButton.setOnAction(e -> controller.executeReset(resetFunctionName.getText(), regionAWS.getText()));
        resetFunctionBox.getChildren().addAll(resetLabel, resetFunctionName, startResetButton);
        grid.add(resetFunctionBox, 3, 1);

        var regionLabel = new Label("AWS region:");
        HBox regionBox = new HBox();
        regionBox.getChildren().addAll(regionLabel, regionAWS);
        grid.add(regionBox, 4, 1);


        Label infoOfTestCaseLabel = new Label("Summary of test case:");
        grid.add(infoOfTestCaseLabel, 2, 7);


        Label statusOfRunningTestCase = new Label("Output of data generation");
        grid.add(statusOfRunningTestCase, 4, 7);

        Label checkboxDescription = new Label("Use testcase for \ndata generation");
        grid.add(checkboxDescription, 3, 7);
        for (var testTarget : testSuite.getTestTargets()) {
            int lastRow = grid.getRowCount();
            Label testTargetLabel = new Label(testTarget.getCoverageTargetDescription());
            grid.add(testTargetLabel, 1, lastRow);
            for (Testcase testcase : testTarget.getTestcases()) {
                lastRow = grid.getRowCount();
                StringBuilder summaryOfTestCase = new StringBuilder();
                summaryOfTestCase.append("Log statements to be called:").append("\n");
                String logStatements = String.join("\n", testcase.getLogsToCover());
                summaryOfTestCase.append(logStatements).append("\n\n");
                summaryOfTestCase.append("Functions to be invoked for target:").append("\n");
                String functions = testcase.getFunctions().stream().map(ServerlessFunction::getName).collect(Collectors.joining("\n"));
                summaryOfTestCase.append(functions).append("\n\n");
                summaryOfTestCase.append("Input format of functions called:").append("\n");
                if (testcase.getNodesForOracle() != null && !testcase.getNodesForOracle().isEmpty()) {
                    var nodeForOracle = testcase.getNodesForOracle();
                    var entry = nodeForOracle.stream().map(node -> String.valueOf(node.getIdentifier())).collect(Collectors.joining(";"));
                    summaryOfTestCase.append(String.format("Nodes whose state has to be checked: %s%n", entry));
                }
                if (testcase.getNodesHoldingState() != null && !testcase.getNodesHoldingState().isEmpty()) {
                    var nodesWithStates = testcase.getNodesHoldingState();
                    var entry = nodesWithStates.stream().map(node -> String.valueOf(node.getIdentifier())).collect(Collectors.joining(";"));
                    summaryOfTestCase.append(String.format("Nodes whose state affect test case execution: %s%n", entry));
                }
                StringBuilder inputFormat = new StringBuilder();
                List<ServerlessFunction> serverlessFunctions = testcase.getFunctions();
                for (var serverlessFunction : serverlessFunctions) {
                    inputFormat.append(String.format("Input Format of %s:%n", serverlessFunction.getName()));
                    inputFormat.append(serverlessFunction.getInputFormatString());
                }
                summaryOfTestCase.append(inputFormat);

                TextArea summaryOfTestcaseTextArea = new TextArea(summaryOfTestCase.toString());
                summaryOfTestcaseTextArea.setEditable(false);
                summaryOfTestcaseTextArea.setPrefHeight(100);
                grid.add(summaryOfTestcaseTextArea, 2, lastRow);

                CheckboxWrapper<Testcase> checkbox = new CheckboxWrapper<>(testcase);

                TextArea outputOfRunningTestCase = new TextArea();


                outputOfRunningTestCase.setEditable(false);
                outputOfRunningTestCase.setPrefHeight(2);
                grid.add(outputOfRunningTestCase, 4, lastRow);
                StringProperty stringProperty = new SimpleStringProperty();
                outputOfRunningTestCase.textProperty().bind(stringProperty);
                BooleanProperty testState = new SimpleBooleanProperty();
                testState.addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        outputOfRunningTestCase.setStyle("-fx-control-inner-background:green;   -fx-text-fill: black; ");
                    } else {
                        outputOfRunningTestCase.setStyle("-fx-control-inner-background:white;   -fx-text-fill: black; ");

                    }
                });
                testcase.setSpecificTargetState(testState);

                BooleanProperty testTargetState = new SimpleBooleanProperty();
                testTargetState.addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        outputOfRunningTestCase.setStyle("-fx-control-inner-background:yellow;   -fx-text-fill: black; ");
                    } else {
                        outputOfRunningTestCase.setStyle("-fx-control-inner-background:white;   -fx-text-fill: black; ");

                    }
                });
                testcase.setTestState(testTargetState);
                testcase.setTestCaseOutput(stringProperty);

                Button replayButton = new Button("Reexecute");
                replayButton.setOnAction(e -> controller.reexecuteTestcase(testcase, regionAWS.getText()));

                Button getDataOfTestCaseButton = new Button("Get data");
                getDataOfTestCaseButton.setOnAction(e -> controller.getTestCaseData(testcase));

                HBox.setMargin(getDataOfTestCaseButton, new Insets(10, 10, 10, 10));
                HBox.setMargin(replayButton, new Insets(10, 10, 10, 10));

                HBox tcButtons = new HBox();
                tcButtons.getChildren().addAll(replayButton, getDataOfTestCaseButton);
                grid.add(tcButtons, 5, lastRow);

                availableTestcases.add(checkbox);
                HBox hbox = new HBox();
                hbox.getChildren().add(checkbox);
                HBox.setMargin(checkbox, new Insets(10, 10, 10, 10));
                grid.add(hbox, 3, lastRow);

            }

        }


        HBox lastRow = new HBox();

        Button getAllDataButton = new Button("All tc data");
        getAllDataButton.setOnAction(e -> controller.showTestSuitData(testSuite));

        Button getAllTCsWithInput = new Button("All test cases with input");
        getAllTCsWithInput.setOnAction(e -> controller.showTestSuiteForExecution(testSuite));

        Button selectAllTestCases = new Button("Select all test cases");
        selectAllTestCases.setOnAction(e -> availableTestcases.forEach(cb -> cb.setSelected(true)));

        Button unselectAllTestCases = new Button("Unselect all test cases");
        unselectAllTestCases.setOnAction(e -> availableTestcases.forEach(cb -> cb.setSelected(false)));

        lastRow.getChildren().addAll(getAllDataButton, getAllTCsWithInput, selectAllTestCases, unselectAllTestCases);
        grid.add(lastRow, 1, grid.getRowCount());
        HBox.setMargin(getAllDataButton, new Insets(10, 10, 10, 10));
        HBox.setMargin(getAllTCsWithInput, new Insets(10, 10, 10, 10));
        HBox.setMargin(selectAllTestCases, new Insets(10, 10, 10, 10));
        HBox.setMargin(unselectAllTestCases, new Insets(10, 10, 10, 10));
        return scrollpane;
    }

    private void createRowForRunOptions(GridPane grid) {
        Label numberOfTriesLabel = new Label("Number of tries:");
        Label probChangeGoodDataLabel = new Label("Probability change promising data:");
        Label probSameValueEverywhereLabel = new Label("Probability same value everywhere:");
        Label probEntryUndefinedLabel = new Label("Probability entry undefined:");
        Label probSimilarInputAsValueLabel = new Label("Probability use value of similar input key:");
        Label probRandomInputAsValueLabel = new Label("Probability use value of random input key:");
        Label probSimilarOutputAsValueLabel = new Label("Probability use value of similar output key:");
        Label probRandomOutputAsValueLabel = new Label("Probability use value of random output key:");

        grid.add(numberOfTriesLabel, 1, 2);
        numberOfTries.setEditable(true);
        grid.add(numberOfTries, 1, 3);


        grid.add(probChangeGoodDataLabel, 2, 2);
        probChangeGoodData.setEditable(true);
        grid.add(probChangeGoodData, 2, 3);

        grid.add(probSameValueEverywhereLabel, 3, 2);
        probSameValueEverywhere.setEditable(true);
        grid.add(probSameValueEverywhere, 3, 3);

        grid.add(probEntryUndefinedLabel, 1, 4);
        probEntryUndefined.setEditable(true);
        grid.add(probEntryUndefined, 1, 5);

        grid.add(probSimilarInputAsValueLabel, 2, 4);
        probSimilarInputAsValue.setEditable(true);
        grid.add(probSimilarInputAsValue, 2, 5);

        grid.add(probRandomInputAsValueLabel, 3, 4);
        probRandomInputAsValue.setEditable(true);
        grid.add(probRandomInputAsValue, 3, 5);

        grid.add(probSimilarOutputAsValueLabel, 4, 4);
        probSimilarOutputAsValue.setEditable(true);
        grid.add(probSimilarOutputAsValue, 4, 5);

        grid.add(probRandomOutputAsValueLabel, 5, 4);
        probRandomOutputAsValue.setEditable(true);
        grid.add(probRandomOutputAsValue, 5, 5);


    }

    private EventHandler<ActionEvent> getActionEventEventHandlerForStartButton() {
        return e -> {
            List<Testcase> testcasesToBeCreated = new ArrayList<>();
            for (var checkboxWrapper : availableTestcases) {
                if (checkboxWrapper.isSelected()) {
                    testcasesToBeCreated.add(checkboxWrapper.getEntry());
                }
            }
            Commands commands = new Commands();
            commands.setNumberOfTries(numberOfTries.getValue());
            commands.setProbChangeGoodData(probChangeGoodData.getValue());
            commands.setProbEntryUndefined(probEntryUndefined.getValue());
            commands.setProbSimilarInputAsValue(probSimilarInputAsValue.getValue());
            commands.setProbRandomInputAsValue(probRandomInputAsValue.getValue());
            commands.setProbSimilarOutputAsValue(probSimilarOutputAsValue.getValue());
            commands.setProbRandomOutputAsValue(probRandomOutputAsValue.getValue());
            commands.setProbSameValueEverywhere(probSameValueEverywhere.getValue());
            commands.setResetFunctionName(resetFunctionName.getText());
            commands.setRegion(regionAWS.getText());
            saveConfigProperties();
            controller.startDynamicTCCalculation(testcasesToBeCreated, model, commands);
        };
    }


    private void createView() {
        var grid = getScrollPane();
        var scene = new Scene(grid);
        this.setScene(scene);
    }

    private void saveConfigProperties() {
        Properties properties = new Properties();
        String pathOfProperties = "settingDynExecution.xml";

        try {
            properties.setProperty("numberOfTries", String.valueOf(numberOfTries.getValue()));
            properties.setProperty("probChangeGoodData", String.valueOf(probChangeGoodData.getValue()));
            properties.setProperty("probEntryUndefined", String.valueOf(probEntryUndefined.getValue()));
            properties.setProperty("probSimilarInputAsValue", String.valueOf(probSimilarInputAsValue.getValue()));
            properties.setProperty("probRandomInputAsValue", String.valueOf(probRandomInputAsValue.getValue()));
            properties.setProperty("probSimilarOutputAsValue", String.valueOf(probSimilarOutputAsValue.getValue()));
            properties.setProperty("probRandomOutputAsValue", String.valueOf(probRandomOutputAsValue.getValue()));
            properties.setProperty("probSameValueEverywhere", String.valueOf(probSameValueEverywhere.getValue()));
            properties.setProperty("resetFunctionName", resetFunctionName.getText());
            properties.setProperty("regionAWS", regionAWS.getText());

            Path path = Path.of(pathOfProperties);
            properties.storeToXML(Files.newOutputStream(path), null);
        } catch (IOException e) {
            System.err.println("Problem while writing " + pathOfProperties);
            e.printStackTrace();
        }
    }

    private void getConfigProperties() {

        Properties properties = new Properties();
        String pathOfProperties = "settingDynExecution.xml";
        Path path = Path.of(pathOfProperties);
        if (Files.notExists(path)) {
            return;
        }
        try {
            properties.loadFromXML(Files.newInputStream(path));

            String numberOfTriesText = properties.getProperty("numberOfTries");
            numberOfTries.getValueFactory().setValue(Integer.valueOf(numberOfTriesText));

            String probChangeGoodDataText = properties.getProperty("probChangeGoodData");
            probChangeGoodData.getValueFactory().setValue(Double.valueOf(probChangeGoodDataText));


            String probEntryUndefinedText = properties.getProperty("probEntryUndefined");
            probEntryUndefined.getValueFactory().setValue(Double.valueOf(probEntryUndefinedText));

            String probSimilarInputAsValueText = properties.getProperty("probSimilarInputAsValue");
            probSimilarInputAsValue.getValueFactory().setValue(Double.valueOf(probSimilarInputAsValueText));


            String probRandomInputAsValueText = properties.getProperty("probRandomInputAsValue");
            probRandomInputAsValue.getValueFactory().setValue(Double.valueOf(probRandomInputAsValueText));

            String probSimilarOutputAsValueText = properties.getProperty("probSimilarOutputAsValue");
            probSimilarOutputAsValue.getValueFactory().setValue(Double.valueOf(probSimilarOutputAsValueText));

            String probRandomOutputAsValueText = properties.getProperty("probRandomOutputAsValue");
            probRandomOutputAsValue.getValueFactory().setValue(Double.valueOf(probRandomOutputAsValueText));

            String probSameValueEverywhereText = properties.getProperty("probSameValueEverywhere");
            probSameValueEverywhere.getValueFactory().setValue(Double.valueOf(probSameValueEverywhereText));

            String resetFunctionNameText = properties.getProperty("resetFunctionName");
            resetFunctionName.setText(resetFunctionNameText);

            String regionAWSText = properties.getProperty("regionAWS");
            regionAWS.setText(regionAWSText);
        } catch (IOException | NumberFormatException e) {
            System.err.println("Problem while reading " + pathOfProperties);
            e.printStackTrace();
        }
    }
}
