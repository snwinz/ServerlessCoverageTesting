package gui.view;

import gui.controller.TestCaseExecutionController;
import gui.model.Graph;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import shared.model.Testcase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

public class TestCaseExecutionView extends Stage {
    private final TestCaseExecutionController controller;
    private final Graph model;
    private final Spinner<Integer> numberOfRuns = new Spinner<>(2, 100, 2);
    private final TextField resetFunctionName = new TextField();
    private final TextField regionAWS = new TextField();
    private final List<Testcase> testcases;
    private final File tcFile;


    public TestCaseExecutionView(TestCaseExecutionController controller, List<Testcase> testcases, File tcFile, Graph model) {
        this.controller = controller;
        this.model = model;
        this.testcases = testcases;
        this.tcFile = tcFile;
        getConfigProperties();
        createView();
    }

    private void createView() {
        var grid = getGridPane();
        var scene = new Scene(grid);
        this.setScene(scene);
    }

    private Pane getGridPane() {
        var borderPane = new BorderPane();
        var menuBar = createMenuBar();
        borderPane.setTop(menuBar);
        var testCasesOverview = getTestExecutionEnvioronment();
        borderPane.setCenter(testCasesOverview);
        return borderPane;
    }

    private ScrollPane getTestExecutionEnvioronment() {
        {
            ScrollPane scrollpane = new ScrollPane();
            var grid = new GridPane();
            scrollpane.setContent(grid);


            var runsLabel = new Label("Runs for output generation:");
            HBox runsFunctionBox = new HBox();
            HBox.setMargin(runsLabel, new Insets(10, 0, 10, 10));
            HBox.setMargin(numberOfRuns, new Insets(10, 10, 10, 0));
            runsFunctionBox.getChildren().addAll(runsLabel, numberOfRuns);
            grid.add(runsFunctionBox, 1, 1);


            var regionLabel = new Label("AWS region:");
            HBox.setMargin(regionLabel, new Insets(10, 0, 10, 10));
            HBox.setMargin(regionAWS, new Insets(10, 10, 10, 0));
            HBox regionBox = new HBox();
            regionBox.getChildren().addAll(regionLabel, regionAWS);
            grid.add(regionBox, 2, 1);

            var resetLabel = new Label("Reset function:");
            var startResetButton = new Button("Start reset");
            HBox.setMargin(startResetButton, new Insets(10, 10, 10, 10));
            HBox.setMargin(resetLabel, new Insets(10, 0, 10, 10));
            HBox.setMargin(resetFunctionName, new Insets(10, 10, 10, 0));
            startResetButton.setOnAction(e -> controller.executeReset(resetFunctionName.getText(), regionAWS.getText()));
            HBox resetFunctionBox = new HBox();
            resetFunctionBox.getChildren().addAll(resetLabel, resetFunctionName, startResetButton);
            grid.add(resetFunctionBox, 3, 1);












            Label operationsLabel = new Label("Operations:");
            grid.add(operationsLabel, 2, 2);


            Label expectedOutput = new Label("Output expected");
            grid.add(expectedOutput, 3, 2);

            Label outputInfoLabel = new Label("Output info");
            grid.add(outputInfoLabel, 5, 2);
            Label passedLabel = new Label("Passed");
            grid.add(passedLabel, 6, 2);


            /*TC list
            for (var testTarget : testSuite.getTestTargets()) {
                int lastRow = grid.getRowCount();
                Label testTargetLabel = new Label(testTarget.getCoverageTargetDescription());
                grid.add(testTargetLabel, 1, lastRow);
                for (var testcase : testTarget.getTestcases()) {
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

                    CheckboxWrapper checkbox = new CheckboxWrapper(testcase);

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
*/

            HBox executionButtons = new HBox();

            Button showPassedTCs = new Button("Show passed TCs");

            Button executeTCs = new Button("Execute TCs");
            executeTCs.setOnAction(e-> saveConfigProperties());

            Button executeAllTCs = new Button("Execute all TCs");
            executeAllTCs.setOnAction(e-> saveConfigProperties());

            executionButtons.getChildren().addAll(showPassedTCs, executeTCs, executeAllTCs);
            grid.add(executionButtons, 1, grid.getRowCount());
            HBox.setMargin(showPassedTCs, new Insets(10, 10, 10, 10));
            HBox.setMargin(executeTCs, new Insets(10, 10, 10, 10));
            HBox.setMargin(executeAllTCs, new Insets(10, 10, 10, 10));



            Label logLabel = new Label("Logs:");
            grid.add(logLabel,1,grid.getRowCount());

            HBox logRow = new HBox();
            Button getAllDataButton = new Button("reset Logs");

            Button getAllTCsWithInput = new Button("get Logs");

            Button selectAllTestCases = new Button("evaluate Logs");

            logRow.getChildren().addAll(getAllDataButton, getAllTCsWithInput, selectAllTestCases);
            grid.add(logRow, 1, grid.getRowCount());
            HBox.setMargin(getAllDataButton, new Insets(10, 10, 10, 10));
            HBox.setMargin(getAllTCsWithInput, new Insets(10, 10, 10, 10));
            HBox.setMargin(selectAllTestCases, new Insets(10, 10, 10, 10));



            return scrollpane;


        }
    }



    private MenuBar createMenuBar() {
        var menuBar = new MenuBar();
        var file = new Menu("File");

        var saveTCs = new MenuItem("Save TCs");
        var save = new MenuItem("Save");
        var loadTC = new MenuItem("Load test cases");

        file.getItems().addAll(save, saveTCs,loadTC);
        menuBar.getMenus().addAll(file);
        return menuBar;
    }


    private void saveConfigProperties() {
        Properties properties = new Properties();
        String pathOfProperties = "settingTCExecution.xml";

        try {
            properties.setProperty("numberOfRuns", String.valueOf(numberOfRuns.getValue()));
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
        String pathOfProperties = "settingTCExecution.xml";
        Path path = Path.of(pathOfProperties);
        if (Files.notExists(path)) {
            return;
        }
        try {
            properties.loadFromXML(Files.newInputStream(path));

            String numberOfTriesText = properties.getProperty("numberOfRuns");
            numberOfRuns.getValueFactory().setValue(Integer.valueOf(numberOfTriesText));

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
