package gui.view;

import gui.controller.TestCaseExecutionController;
import gui.model.Graph;
import gui.view.wrapper.CheckboxWrapper;
import gui.view.wrapper.FunctionWrapper;
import gui.view.wrapper.TestcaseWrapper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import shared.model.Function;
import shared.model.Testcase;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class TestCaseExecutionView extends Stage implements PropertyChangeListener {
    private final TestCaseExecutionController controller;
    private final TextField resetFunctionName = new TextField();
    private final TextField regionAWS = new TextField();
    private final Graph graph;
    private final List<TestcaseWrapper> testcases;
    private final List<CheckboxWrapper<TestcaseWrapper>> selectedTestcases = new ArrayList<>();
    private final CheckBox keepLogsCheckbox = new CheckBox("save logs");

    public TestCaseExecutionView(TestCaseExecutionController controller, List<Testcase> testcases, Graph graph) {
        this.controller = controller;
        this.graph = graph;
        addFunctionToEmptyTCs(testcases);
        this.testcases = testcases.stream().map(TestcaseWrapper::new).collect(Collectors.toCollection(LinkedList::new));
        getConfigProperties();
        createView();
    }

    private void addFunctionToEmptyTCs(List<Testcase> testcases) {
        for (var tc : testcases) {
            if (tc.functions().size() == 0) {
                tc.addFunction(new Function("functionName", "parameters"));
            }
        }
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
        var testCasesOverview = getTestExecutionEnvironment();
        borderPane.setCenter(testCasesOverview);
        return borderPane;
    }

    private ScrollPane getTestExecutionEnvironment() {
        {
            ScrollPane scrollpane = new ScrollPane();
            var grid = new GridPane();
            scrollpane.setContent(grid);


            var regionLabel = new Label("AWS region:");
            HBox.setMargin(regionLabel, new Insets(10, 0, 10, 10));
            HBox.setMargin(regionAWS, new Insets(10, 10, 10, 0));

            var resetLabel = new Label("Reset function:");
            var startResetButton = new Button("execute reset function");
            var deleteLog = new Button("delete logs");
            var resetApplication = new Button("reset application");
            startResetButton.setOnAction(e -> controller.executeReset(resetFunctionName.getText(), regionAWS.getText()));
            deleteLog.setOnAction(e -> controller.deleteLogs(regionAWS.getText()));
            resetApplication.setOnAction(e -> controller.resetApplication(resetFunctionName.getText(), regionAWS.getText()));
            HBox.setMargin(startResetButton, new Insets(10, 10, 10, 10));
            HBox.setMargin(deleteLog, new Insets(10, 10, 10, 10));
            HBox.setMargin(resetApplication, new Insets(10, 10, 10, 10));
            HBox.setMargin(resetLabel, new Insets(10, 0, 10, 10));
            HBox.setMargin(resetFunctionName, new Insets(10, 10, 10, 0));


            ViewHelper.addToGridInHBox(grid, regionLabel, regionAWS, resetLabel, resetFunctionName, keepLogsCheckbox, startResetButton, deleteLog, resetApplication);

            Label operationsLabel = new Label("Operations:");
            grid.add(operationsLabel, 3, 2);


            Label expectedOutputLabel = new Label("Output expected");
            grid.add(expectedOutputLabel, 4, 2);

            Label outputInfoLabel = new Label("Output info");
            grid.add(outputInfoLabel, 5, 2);


            for (var testcase : testcases) {
                final int rowOfTestcase = grid.getRowCount();
                HBox testcaseDashboard = new HBox();
                Label testTargetLabel = new Label(testcase.getTestcase().target());
                testTargetLabel.setMaxWidth(300);
                testTargetLabel.setTooltip(new Tooltip(testcase.getTestcase().target()));

                Circle statusLightTestcase = new Circle(100 / 3.0 / 2, 100 / 4.0, 10);
                HBox.setMargin(statusLightTestcase, new Insets(0, 5, 0, 5));


                var checkboxForTestcase = new CheckboxWrapper<>(testcase);
                selectedTestcases.add(checkboxForTestcase);
                HBox.setMargin(checkboxForTestcase, new Insets(0, 5, 0, 5));

                testcaseDashboard.getChildren().addAll(testTargetLabel, statusLightTestcase, checkboxForTestcase);
                grid.add(testcaseDashboard, 1, rowOfTestcase);

                Button executeTC = new Button("execute TC");
                Button calibrateTC = new Button("calibrate TC");
                Button addFunction = new Button("add Function");
                HBox.setMargin(executeTC, new Insets(10, 10, 10, 10));
                HBox.setMargin(calibrateTC, new Insets(10, 10, 10, 10));
                HBox.setMargin(addFunction, new Insets(10, 10, 10, 10));

                executeTC.setOnAction(e -> {
                    testcase.setSaveLogs(keepLogsCheckbox.isSelected());
                    testcases.forEach(tc -> tc.setSaveLogs(keepLogsCheckbox.isSelected()));
                    testcase.getFunctionsWrapped().forEach(FunctionWrapper::reset);
                    testcase.reset();
                    controller.executeTC(testcase, regionAWS.getText());
                });
                calibrateTC.setOnAction(e -> {
                    testcase.getFunctionsWrapped().forEach(FunctionWrapper::reset);
                    testcase.reset();
                    controller.calibrateOutput(testcase, regionAWS.getText(), resetFunctionName.getText());
                });
                addFunction.setOnAction(e -> controller.addFunctionToTestcase(testcase));

                HBox buttons = new HBox();
                buttons.getChildren().addAll(executeTC, calibrateTC, addFunction);
                grid.add(buttons, 3, rowOfTestcase);

                var originalTestcase = testcase.getTestcase();
                var logsToBeCovered = originalTestcase.getLogsToBeCovered();
                logsToBeCovered = logsToBeCovered.stream().map(part -> part.replace("*", "\\*")).collect(Collectors.toList());
                String expectedLogOutput = String.join("*", logsToBeCovered);
                TextArea expectedLogOutputTextArea = new TextArea(expectedLogOutput);
                expectedLogOutputTextArea.setEditable(true);
                expectedLogOutputTextArea.setPrefHeight(25);
                expectedLogOutputTextArea.textProperty().addListener((observable, oldValue, newValue) -> originalTestcase.setExpectedLogOutput(newValue));
                testcase.expectedLogsProperty().bindBidirectional(expectedLogOutputTextArea.textProperty());
                testcase.passedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        statusLightTestcase.setFill(Color.GREEN);
                    } else {
                        statusLightTestcase.setFill(Color.RED);
                    }
                });
                testcase.executedProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue) {
                        statusLightTestcase.setFill(Color.BLACK);
                    }
                });

                grid.add(expectedLogOutputTextArea, 4, rowOfTestcase);

                var functions = testcase.getFunctionsWrapped();

                for (var function : functions) {
                    final int lastRow = grid.getRowCount();
                    Circle statusLightFunction = new Circle(100 / 3.0 / 2, 100 / 4.0, 10);
                    function.passedProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue) {
                            statusLightFunction.setFill(Color.GREEN);

                        } else {
                            statusLightFunction.setFill(Color.RED);
                        }
                    });

                    function.executedProperty().addListener((observable, oldValue, newValue) -> {
                        if (!newValue) {
                            statusLightFunction.setFill(Color.BLACK);
                        }
                    });

                    grid.add(statusLightFunction, 2, lastRow);
                    var originalFunction = function.getFunction();
                    String functionInvocation = String.format("%s %s", originalFunction.getName(), originalFunction.getParameter());
                    TextArea functionDescription = new TextArea(functionInvocation);
                    functionDescription.setEditable(true);
                    functionDescription.setPrefHeight(25);
                    functionDescription.textProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue.contains(" ")) {
                            int separatorFunctionParameter = newValue.indexOf(" ");
                            String functionName = newValue.substring(0, separatorFunctionParameter);
                            originalFunction.setFunctionName(functionName);
                            separatorFunctionParameter++;
                            if (separatorFunctionParameter <= newValue.length()) {
                                String arguments = newValue.substring(separatorFunctionParameter);
                                originalFunction.setFunctionParameter(arguments);
                            }
                        }
                    });
                    grid.add(functionDescription, 3, lastRow);

                    var partsToBeCovered = originalFunction.getExpectedOutputs();
                    partsToBeCovered = partsToBeCovered.stream().map(part -> part.replace("*", "\\*")).collect(Collectors.toList());

                    String expectedOutput = String.join("*", partsToBeCovered);
                    TextArea expectedOutputTextArea = new TextArea(expectedOutput);
                    expectedOutputTextArea.setEditable(true);
                    expectedOutputTextArea.setPrefHeight(25);
                    expectedOutputTextArea.textProperty().addListener((observable, oldValue, newValue) -> originalFunction.setExpectedOutputs(newValue));
                    function.expectedResultProperty().bindBidirectional(expectedOutputTextArea.textProperty());
                    grid.add(expectedOutputTextArea, 4, lastRow);

                    TextArea infoBox = new TextArea();
                    infoBox.textProperty().bindBidirectional(function.outputProperty());
                    infoBox.setEditable(true);
                    infoBox.setPrefHeight(25);
                    grid.add(infoBox, 5, lastRow);

                }
            }
            Label adminLabel = new Label("Administration:");
            grid.add(adminLabel, 1, grid.getRowCount());


            Button selectAllTestCases = new Button("Select all test cases");

            Button unselectAllTestCases = new Button("Unselect all test cases");
            Button showPassedTCs = new Button("Show passed TCs");
            HBox adminButtons = ViewHelper.addToGridInHBox(grid, selectAllTestCases, unselectAllTestCases, showPassedTCs);
            selectAllTestCases.setOnAction(e -> {
                selectedTestcases.forEach(cb -> cb.setSelected(true));
                adminButtons.getChildren().remove(selectAllTestCases);
                adminButtons.getChildren().add(0, unselectAllTestCases);
            });
            unselectAllTestCases.setOnAction(e -> {
                selectedTestcases.forEach(cb -> cb.setSelected(false));
                adminButtons.getChildren().remove(unselectAllTestCases);
                adminButtons.getChildren().add(0, selectAllTestCases);
            });


            showPassedTCs.setOnAction(e -> controller.showPassedTCs(testcases));

            adminButtons.getChildren().remove(unselectAllTestCases);

            Label executionLabel = new Label("Execution:");
            grid.add(executionLabel, 1, grid.getRowCount());

            Button executeTCs = new Button("Execute selected TCs");
            executeTCs.setOnAction(e -> {
                saveConfigProperties();
                var testcasesSelected = selectedTestcases.stream().filter(CheckboxWrapper::isSelected).map(CheckboxWrapper::getEntry).toList();
                testcasesSelected.forEach(tc -> tc.setSaveLogs(keepLogsCheckbox.isSelected()));
                testcasesSelected.stream().map(TestcaseWrapper::getFunctionsWrapped).flatMap(Collection::stream).forEach(FunctionWrapper::reset);
                testcasesSelected.forEach(TestcaseWrapper::reset);
                controller.executeTestcases(testcasesSelected, regionAWS.getText(), resetFunctionName.getText());
            });


            Button executeAllTCs = new Button("Execute all TCs");
            executeAllTCs.setOnAction(e -> {
                saveConfigProperties();
                testcases.forEach(tc -> tc.setSaveLogs(keepLogsCheckbox.isSelected()));
                testcases.stream().map(TestcaseWrapper::getFunctionsWrapped).flatMap(Collection::stream).forEach(FunctionWrapper::reset);
                testcases.forEach(TestcaseWrapper::reset);
                controller.executeTestcases(testcases, regionAWS.getText(), resetFunctionName.getText());
            });

            ViewHelper.addToGridInHBox(grid, executeTCs, executeAllTCs);

            Label calibrationLabel = new Label("Calibration:");
            grid.add(calibrationLabel, 1, grid.getRowCount());

            Button calibrateSelectedTestcases = new Button("Calibrate selected TCs");
            calibrateSelectedTestcases.setOnAction(e -> {
                saveConfigProperties();
                var testcasesSelected = selectedTestcases.stream().filter(CheckboxWrapper::isSelected).map(CheckboxWrapper::getEntry).toList();
                testcasesSelected.stream().map(TestcaseWrapper::getFunctionsWrapped).flatMap(Collection::stream).forEach(FunctionWrapper::reset);
                testcasesSelected.forEach(TestcaseWrapper::reset);
                controller.calibrateTestcases(testcasesSelected, regionAWS.getText(), resetFunctionName.getText());
            });


            Button calibrateAllTestcases = new Button("Calibrate all TCs");
            calibrateAllTestcases.setOnAction(e -> {
                saveConfigProperties();
                testcases.stream().map(TestcaseWrapper::getFunctionsWrapped).flatMap(Collection::stream).forEach(FunctionWrapper::reset);
                testcases.forEach(TestcaseWrapper::reset);
                controller.calibrateTestcases(testcases, regionAWS.getText(), resetFunctionName.getText());
            });

            ViewHelper.addToGridInHBox(grid, calibrateSelectedTestcases, calibrateAllTestcases);

            Label logLabel = new Label("Logs:");
            grid.add(logLabel, 1, grid.getRowCount());

            Button deleteLogs = new Button("delete Logs");
            deleteLogs.setOnAction(e -> controller.deleteLogs(regionAWS.getText()));

            Button getLogsOfTestcases = new Button("get all logs of testcases");
            getLogsOfTestcases.setOnAction(e -> controller.getLogsOfTestcases(testcases));

            Button getAllLogsOnPlatform = new Button("get all logs on platform");
            getAllLogsOnPlatform.setOnAction(e -> controller.getLogsOnPlatform(regionAWS.getText()));

            Button evaluateLogs = new Button("Evaluate logs");
            evaluateLogs.setOnAction(e -> controller.evaluateLogs(testcases, graph));

            HBox.setMargin(deleteLogs, new Insets(10, 10, 10, 10));
            HBox.setMargin(getAllLogsOnPlatform, new Insets(10, 10, 10, 10));
            HBox.setMargin(getLogsOfTestcases, new Insets(10, 10, 10, 10));
            ViewHelper.addToGridInHBox(grid, deleteLogs, getLogsOfTestcases, getAllLogsOnPlatform, evaluateLogs);

            return scrollpane;
        }
    }


    private MenuBar createMenuBar() {
        var menuBar = new MenuBar();
        var file = new Menu("File");

        var saveTCs = new MenuItem("Save TCs");

        saveTCs.setOnAction(event -> {
            var testcasesOriginal = testcases.stream().map(TestcaseWrapper::getTestcase).toList();
            controller.saveTestcases(testcasesOriginal);
        });

        file.getItems().addAll(saveTCs);
        menuBar.getMenus().addAll(file);
        return menuBar;
    }


    private void saveConfigProperties() {
        Properties properties = new Properties();
        String pathOfProperties = "settingTCExecution.xml";

        try {
            properties.setProperty("resetFunctionName", resetFunctionName.getText());
            properties.setProperty("regionAWS", regionAWS.getText());
            properties.setProperty("keepLogs", Boolean.toString(keepLogsCheckbox.isSelected()));

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

            String resetFunctionNameText = properties.getProperty("resetFunctionName");
            resetFunctionName.setText(resetFunctionNameText);

            String regionAWSText = properties.getProperty("regionAWS");
            regionAWS.setText(regionAWSText);

            String keepLogsText = properties.getProperty("keepLogs");
            boolean keepLogs = Boolean.parseBoolean(keepLogsText);
            keepLogsCheckbox.setSelected(keepLogs);
        } catch (IOException e) {
            System.err.println("Problem while reading " + pathOfProperties);
            e.printStackTrace();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        var value = evt.getNewValue();
        if ("functionAdded".equals(evt.getPropertyName()) && value instanceof Testcase testcase) {
            for (int i = 0; i < testcases.size(); i++) {
                var tcWrapped = testcases.get(i);
                if (testcase == tcWrapped.getTestcase()) {
                    TestcaseWrapper wrapperUpdated = new TestcaseWrapper(testcase);
                    testcases.remove(i);
                    testcases.add(i, wrapperUpdated);
                    break;
                }
            }
            createView();
        }
    }
}
