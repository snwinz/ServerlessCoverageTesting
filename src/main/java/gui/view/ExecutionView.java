package gui.view;

import gui.controller.TestCaseExecutionController;
import gui.model.Graph;
import gui.view.wrapper.CheckboxWrapper;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ExecutionView extends Stage implements PropertyChangeListener {
    private final TestCaseExecutionController controller;
    private final TextField resetFunctionName = new TextField();
    private final TextField regionAWS = new TextField();
    private final Graph graph;
    private final List<TestcaseWrapper> testcases;
    private final List<CheckboxWrapper<TestcaseWrapper>> selectedTestcases = new ArrayList<>();
    private final CheckBox keepLogsCheckbox = new CheckBox("save logs");
    private Path path;

    public ExecutionView(TestCaseExecutionController controller, List<Testcase> testcases, Graph graph, Path path) {
        this.controller = controller;
        this.graph = graph;
        this.path = path;
        this.setTitle(path.getFileName().toString());
        addFunctionToEmptyTCs(testcases);
        this.testcases = testcases.stream().map(TestcaseWrapper::new).collect(Collectors.toCollection(LinkedList::new));
        getConfigProperties();
        createView();
    }

    private void addFunctionToEmptyTCs(List<Testcase> testcases) {
        for (var tc : testcases) {
            if (tc.getFunctions().size() == 0) {
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
        ViewHelper.addToGridInHBox(grid, regionLabel, regionAWS, resetLabel, resetFunctionName, keepLogsCheckbox, startResetButton, deleteLog, resetApplication);

        Label operationsLabel = new Label("Operations:");
        grid.add(operationsLabel, 3, 2);


        Label expectedOutputLabel = new Label("Output expected");
        grid.add(expectedOutputLabel, 4, 2);

        Label outputInfoLabel = new Label("Output info");
        grid.add(outputInfoLabel, 5, 2);

        Label manualCreatedLabel = new Label("Manually created");
        grid.add(manualCreatedLabel, 6, 2);

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
            Button recalibrateTC = new Button("recalibrate TC");
            Button addFunction = new Button("add Function");
            HBox.setMargin(executeTC, new Insets(10, 10, 10, 10));
            HBox.setMargin(calibrateTC, new Insets(10, 10, 10, 10));
            HBox.setMargin(recalibrateTC, new Insets(10, 10, 10, 10));
            HBox.setMargin(addFunction, new Insets(10, 10, 10, 10));

            executeTC.setOnAction(e -> {
                testcase.setSaveLogs(keepLogsCheckbox.isSelected());
                testcase.reset();
                controller.executeTC(testcase, regionAWS.getText(), resetFunctionName.getText());
            });
            calibrateTC.setOnAction(e -> {
                testcase.reset();
                controller.calibrateOutput(testcase, regionAWS.getText(), resetFunctionName.getText());
            });

            recalibrateTC.setOnAction(e -> {
                testcase.reset();
                controller.recalibrateOutput(testcase, regionAWS.getText(), resetFunctionName.getText());
            });
            addFunction.setOnAction(e -> controller.addFunctionToTestcase(testcase));

            HBox buttons = new HBox();
            buttons.getChildren().addAll(executeTC, calibrateTC, recalibrateTC, addFunction);
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

            CheckBox manualCreatedCheckbox = new CheckBox("Manual Created");
            manualCreatedCheckbox.selectedProperty().bindBidirectional(testcase.manualCreatedProperty());
            grid.add(manualCreatedCheckbox, 6, rowOfTestcase);


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
        Button selectFailedTestCases = new Button("Select failed test cases");
        Button selectTestCasesWithData = new Button("Select TCs with data");


        Button unselectAllTestCases = new Button("Unselect all test cases");
        Button showPassedTCs = new Button("Show passed TCs");
        HBox adminButtons = ViewHelper.addToGridInHBox(grid, selectAllTestCases, unselectAllTestCases, selectFailedTestCases, showPassedTCs, selectTestCasesWithData);
        selectAllTestCases.setOnAction(e -> {
            selectedTestcases.forEach(cb -> cb.setSelected(true));
            adminButtons.getChildren().remove(selectAllTestCases);
            adminButtons.getChildren().add(0, unselectAllTestCases);
        });
        selectFailedTestCases.setOnAction(e -> {
            selectedTestcases.forEach(cb -> cb.setSelected(false));
            var testcasesFailed = selectedTestcases.stream().filter(cb -> !cb.getEntry().isPassed()).toList();
            testcasesFailed.forEach(cb -> cb.setSelected(true));
        });
        unselectAllTestCases.setOnAction(e -> {
            selectedTestcases.forEach(cb -> cb.setSelected(false));
            adminButtons.getChildren().remove(unselectAllTestCases);
            adminButtons.getChildren().add(0, selectAllTestCases);
        });
        selectTestCasesWithData.setOnAction(e -> {
            selectedTestcases.forEach(cb -> cb.setSelected(true));
            var testcasesWithoutData = selectedTestcases.stream()
                    .filter(cb -> "parameters".equals(cb.getEntry().getTestcase().getFunctions().get(0).getParameter())).toList();
            testcasesWithoutData.forEach(cb -> cb.setSelected(false));
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
            testcasesSelected.forEach(TestcaseWrapper::reset);
            controller.executeTestcases(testcasesSelected, regionAWS.getText(), resetFunctionName.getText());
        });


        Button executeAllTCs = new Button("Execute all TCs");
        executeAllTCs.setOnAction(e -> {
            saveConfigProperties();
            testcases.forEach(tc -> tc.setSaveLogs(keepLogsCheckbox.isSelected()));
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
            testcasesSelected.forEach(TestcaseWrapper::reset);
            controller.calibrateTestcases(testcasesSelected, regionAWS.getText(), resetFunctionName.getText());
        });


        Button calibrateAllTestcases = new Button("Calibrate all TCs");
        calibrateAllTestcases.setOnAction(e -> {
            saveConfigProperties();
            testcases.forEach(TestcaseWrapper::reset);
            controller.calibrateTestcases(testcases, regionAWS.getText(), resetFunctionName.getText());
        });

        Button recalibrateAllTestcases = new Button("Recalibrate all failed TCs");
        recalibrateAllTestcases.setOnAction(e -> {
            saveConfigProperties();
            var testcasesFailed = testcases.stream().filter(Predicate.not(TestcaseWrapper::isPassed)).toList();
            testcasesFailed.forEach(TestcaseWrapper::reset);
            controller.recalibrateTestcases(testcasesFailed, regionAWS.getText(), resetFunctionName.getText());
        });

        ViewHelper.addToGridInHBox(grid, calibrateSelectedTestcases, calibrateAllTestcases, recalibrateAllTestcases);

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

        ViewHelper.addToGridInHBox(grid, deleteLogs, getLogsOfTestcases, getAllLogsOnPlatform, evaluateLogs);


        Label tcGenerationLabel = new Label("Testcase generation:");
        grid.add(tcGenerationLabel, 1, grid.getRowCount());


        Button generateTCs = new Button("Generate similar random test suite");
        generateTCs.setOnAction(e -> controller.createTestSuite(this.graph, testcases.stream().map(TestcaseWrapper::getTestcase).toList()));

        ViewHelper.addToGridInHBox(grid, generateTCs);

        return scrollpane;
    }


    private MenuBar createMenuBar() {
        var menuBar = new MenuBar();
        var file = new Menu("File");

        var saveTCs = new MenuItem("Save TCs");
        var saveTCsAs = new MenuItem("Save TCs as");
        var saveSelectedTCsAs = new MenuItem("Save selected TCs as");

        saveTCs.setOnAction(event -> {
            var testcasesOriginal = testcases.stream().map(TestcaseWrapper::getTestcase).toList();
            controller.saveTestcases(testcasesOriginal, this.path);
        });
        saveTCsAs.setOnAction(event -> {
            var testcasesOriginal = testcases.stream().map(TestcaseWrapper::getTestcase).toList();
            controller.saveTestcasesAs(testcasesOriginal);
        });
        saveSelectedTCsAs.setOnAction(event -> {
            var selectedTestcasesToSave = selectedTestcases.stream().filter(CheckBox::isSelected)
                    .map(CheckboxWrapper::getEntry).map(TestcaseWrapper::getTestcase).toList();
            controller.saveTestcasesAs(selectedTestcasesToSave);
        });

        file.getItems().addAll(saveTCs, saveTCsAs, saveSelectedTCsAs);
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

    public void setPath(Path path) {
        this.path = path;
        this.setTitle(path.getFileName().toString());
    }
}
