package gui.view;

import gui.controller.TestCaseExecutionController;
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
import shared.model.Testcase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class TestCaseExecutionView extends Stage {
    private final TestCaseExecutionController controller;
    private final TextField resetFunctionName = new TextField();
    private final TextField regionAWS = new TextField();
    private final List<TestcaseWrapper> testcases;
    private final List<CheckboxWrapper<TestcaseWrapper>> selectedTestcases = new ArrayList<>();


    public TestCaseExecutionView(TestCaseExecutionController controller, List<Testcase> testcases) {
        this.controller = controller;
        this.testcases = testcases.stream().map(TestcaseWrapper::new).toList();
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
            HBox regionBox = new HBox();
            regionBox.getChildren().addAll(regionLabel, regionAWS);

            var resetLabel = new Label("Reset function:");
            var startResetButton = new Button("Start reset");
            startResetButton.setOnAction(e -> controller.executeReset(resetFunctionName.getText(), regionAWS.getText()));
            HBox.setMargin(startResetButton, new Insets(10, 10, 10, 10));
            HBox.setMargin(resetLabel, new Insets(10, 0, 10, 10));
            HBox.setMargin(resetFunctionName, new Insets(10, 10, 10, 0));
            startResetButton.setOnAction(e -> controller.executeReset(resetFunctionName.getText(), regionAWS.getText()));
            HBox resetFunctionBox = new HBox();
            resetFunctionBox.getChildren().addAll(resetLabel, resetFunctionName, startResetButton);
            HBox essentialFunctionality = new HBox();
            essentialFunctionality.getChildren().addAll(regionBox, resetFunctionBox);

            grid.add(essentialFunctionality, 3, 1, 4, 1);


            Label operationsLabel = new Label("Operations:");
            grid.add(operationsLabel, 3, 2);


            Label expectedOutputLabel = new Label("Output expected");
            grid.add(expectedOutputLabel, 4, 2);

            Label outputInfoLabel = new Label("Output info");
            grid.add(outputInfoLabel, 5, 2);


            for (var testcase : testcases) {
                int lastRow = grid.getRowCount();
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
                grid.add(testcaseDashboard, 1, lastRow);

                Button executeTC = new Button("execute TC");
                Button calibrateTC = new Button("calibrate TC");
                HBox.setMargin(executeTC, new Insets(10, 10, 10, 10));
                HBox.setMargin(calibrateTC, new Insets(10, 10, 10, 10));

                executeTC.setOnAction(e -> {
                    testcase.getFunctionsWrapped().forEach(FunctionWrapper::reset);
                    controller.executeTC(testcase, regionAWS.getText());
                });
                calibrateTC.setOnAction(e -> {
                    testcase.getFunctionsWrapped().forEach(FunctionWrapper::reset);
                    controller.calibrateOutput(testcase, regionAWS.getText(), resetFunctionName.getText());
                });

                HBox buttons = new HBox();
                buttons.getChildren().addAll(executeTC, calibrateTC);
                grid.add(buttons, 3, lastRow);

                var functions = testcase.getFunctionsWrapped();
                for (var function : functions) {
                    lastRow = grid.getRowCount();
                    Circle statusLightFunction = new Circle(100 / 3.0 / 2, 100 / 4.0, 10);
                    function.passedProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue) {
                            statusLightFunction.setFill(Color.GREEN);
                            if (testcase.getFunctionsWrapped().stream().allMatch(f -> f.passedProperty().get())) {
                                statusLightTestcase.setFill(Color.GREEN);
                            }
                        } else {
                            statusLightFunction.setFill(Color.RED);
                            statusLightTestcase.setFill(Color.RED);
                        }
                    });
                    function.executedProperty().addListener((observable, oldValue, newValue) -> {
                        if (!newValue) {
                            statusLightFunction.setFill(Color.BLACK);
                            statusLightTestcase.setFill(Color.BLACK);
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

                    var partsToBeCovered = originalFunction.getResults();
                    partsToBeCovered = partsToBeCovered.stream().map(part -> part.replace("*", "\\*")).collect(Collectors.toList());

                    String expectedOutput = String.join("*", partsToBeCovered);
                    TextArea expectedOutputTextArea = new TextArea(expectedOutput);
                    expectedOutputTextArea.setEditable(true);
                    expectedOutputTextArea.setPrefHeight(25);
                    expectedOutputTextArea.textProperty().addListener((observable, oldValue, newValue) -> originalFunction.setResults(newValue));
                    function.expectedResultProperty().bindBidirectional(expectedOutputTextArea.textProperty());
                    grid.add(expectedOutputTextArea, 4, lastRow);

                    TextArea infoBox = new TextArea();
                    infoBox.textProperty().bindBidirectional(function.outputProperty());
                    infoBox.setEditable(true);
                    infoBox.setPrefHeight(25);
                    grid.add(infoBox, 5, lastRow);

                }
            }


            HBox executionButtons = new HBox();

            Button selectAllTestCases = new Button("Select all test cases");

            Button unselectAllTestCases = new Button("Unselect all test cases");
            selectAllTestCases.setOnAction(e -> {
                selectedTestcases.forEach(cb -> cb.setSelected(true));
                executionButtons.getChildren().remove(selectAllTestCases);
                executionButtons.getChildren().add(0, unselectAllTestCases);
            });
            unselectAllTestCases.setOnAction(e -> {
                selectedTestcases.forEach(cb -> cb.setSelected(false));
                executionButtons.getChildren().remove(unselectAllTestCases);
                executionButtons.getChildren().add(0, selectAllTestCases);
            });


            Button showPassedTCs = new Button("Show passed TCs");
            showPassedTCs.setOnAction(e -> controller.showPassedTCs(testcases));


            Button executeTCs = new Button("Execute selected TCs");
            executeTCs.setOnAction(e -> {
                saveConfigProperties();
                var testcasesSelected = selectedTestcases.stream().filter(CheckboxWrapper::isSelected).map(CheckboxWrapper::getEntry).toList();
                testcasesSelected.stream().map(TestcaseWrapper::getFunctionsWrapped).flatMap(Collection::stream).forEach(FunctionWrapper::reset);
                controller.executeTestcases(testcasesSelected, regionAWS.getText(), resetFunctionName.getText());
            });


            Button executeAllTCs = new Button("Execute all TCs");
            executeAllTCs.setOnAction(e -> {
                saveConfigProperties();
                testcases.stream().map(TestcaseWrapper::getFunctionsWrapped).flatMap(Collection::stream).forEach(FunctionWrapper::reset);
                controller.executeTestcases(testcases, regionAWS.getText(), resetFunctionName.getText());
            });


            executionButtons.getChildren().addAll(selectAllTestCases, executeTCs, executeAllTCs, showPassedTCs);
            grid.add(executionButtons, 1, grid.getRowCount(), 5, 1);
            HBox.setMargin(selectAllTestCases, new Insets(10, 10, 10, 10));
            HBox.setMargin(unselectAllTestCases, new Insets(10, 10, 10, 10));
            HBox.setMargin(showPassedTCs, new Insets(10, 10, 10, 10));
            HBox.setMargin(executeTCs, new Insets(10, 10, 10, 10));
            HBox.setMargin(executeAllTCs, new Insets(10, 10, 10, 10));


            Label logLabel = new Label("Logs:");
            grid.add(logLabel, 1, grid.getRowCount());

            HBox logRow = new HBox();
            Button deleteLogs = new Button("delete Logs");
            deleteLogs.setOnAction(e -> controller.deleteLogs(regionAWS.getText()));

            Button getLogs = new Button("get Logs");
            getLogs.setOnAction(e -> controller.getLogs(regionAWS.getText()));

            logRow.getChildren().addAll(deleteLogs, getLogs);
            grid.add(logRow, 1, grid.getRowCount());
            HBox.setMargin(deleteLogs, new Insets(10, 10, 10, 10));
            HBox.setMargin(getLogs, new Insets(10, 10, 10, 10));


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
        } catch (IOException e) {
            System.err.println("Problem while reading " + pathOfProperties);
            e.printStackTrace();
        }
    }

}
