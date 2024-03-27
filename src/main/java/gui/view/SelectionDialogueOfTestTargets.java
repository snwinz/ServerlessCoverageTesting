package gui.view;

import gui.controller.criteriaSelection.CriteriaSelectionDynamicTestCaseController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import logic.model.TestSuiteOfTargets;

public class SelectionDialogueOfTestTargets extends Stage {


    private final TestSuiteOfTargets testSuiteOfTargets;
    private final CriteriaSelectionDynamicTestCaseController controller;

    private final Spinner<Integer> minCounter;
    private final Spinner<Integer> maxCounter;
    private final TextField testTargetName;

    public SelectionDialogueOfTestTargets(TestSuiteOfTargets testSuiteOfTargets, CriteriaSelectionDynamicTestCaseController controller) {
        this.testSuiteOfTargets = testSuiteOfTargets;
        this.minCounter = new Spinner<>(0, testSuiteOfTargets.getTestTargets().size(), 1);
        this.maxCounter = new Spinner<>(0, testSuiteOfTargets.getTestTargets().size(), 1);
        this.testTargetName = new TextField("");
        minCounter.setEditable(true);
        maxCounter.setEditable(true);
        this.controller = controller;
        this.setTitle("Filter Test Targets to be generated");
        var grid = getGridPane();
        var scene = new Scene(grid);
        this.setScene(scene);
    }


    private GridPane getGridPane() {
        GridPane gridPane = new GridPane();
        var firstTestSuite = new Label("First Test Target (included): ");
        var lastTestSuite = new Label("Last Test Target (excluded): ");
        var testTargetName = new Label("Name of logs for target: ");


        gridPane.add(firstTestSuite, 0, 0);
        gridPane.add(minCounter, 1, 0);
        gridPane.add(lastTestSuite, 0, 1);
        gridPane.add(maxCounter, 1, 1);
        gridPane.add(testTargetName, 3, 0);
        gridPane.add(this.testTargetName, 4, 0);


        Button numberFilterButton = new Button("Create");
        numberFilterButton.setDefaultButton(true);
        numberFilterButton.setOnAction(getNumberFilterButtonEvent());
        Button cancelButton = new Button("Cancel");
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(getCancelButtonEvent());
        gridPane.add(numberFilterButton, 0, 3);
        gridPane.add(cancelButton, 1, 3);
        Button logNameFilterButton = new Button("Create");
        logNameFilterButton.setOnAction(getLogNameFilterButtonEvent());
        gridPane.add(logNameFilterButton, 2, 3);
        return gridPane;
    }

    private EventHandler<ActionEvent> getLogNameFilterButtonEvent() {
        return event -> {

            var targets = testSuiteOfTargets.getTestTargets();

            TestSuiteOfTargets filteredTestSuiteOfTargets = new TestSuiteOfTargets();
            var filteredTargets = targets.stream().filter(entry -> entry.getTestcases().stream().
                    filter(tc -> testTargetName.getText().equals(String.join("", tc.getLogsOfTarget()))).findAny().isPresent()).toList();
            filteredTestSuiteOfTargets.add(filteredTargets);
            controller.showTestSuites(filteredTestSuiteOfTargets);
            this.close();

        };
    }

    private EventHandler<ActionEvent> getNumberFilterButtonEvent() {

        return event -> {
            if (minCounter.getValue() < maxCounter.getValue()) {
                var targets = testSuiteOfTargets.getTestTargets();
                TestSuiteOfTargets filteredTestSuiteOfTargets = new TestSuiteOfTargets();
                for (int i = minCounter.getValue(); i <= maxCounter.getValue(); i++) {
                    filteredTestSuiteOfTargets.add(targets.get(i - 1));
                }
                controller.showTestSuites(filteredTestSuiteOfTargets);
                this.close();
            }
        };
    }

    private EventHandler<ActionEvent> getCancelButtonEvent() {
        return event -> {
            this.close();
            controller.cancel();
        };
    }


}
