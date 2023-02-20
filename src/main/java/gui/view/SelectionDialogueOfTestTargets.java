package gui.view;

import gui.controller.criteriaSelection.CriteriaSelectionDynamicTestCaseController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import logic.model.TestSuiteOfTargets;

public class SelectionDialogueOfTestTargets extends Stage {


    private final TestSuiteOfTargets testSuiteOfTargets;
    private final CriteriaSelectionDynamicTestCaseController controller;

    private final Spinner<Integer> minCounter;
    private final Spinner<Integer> maxCounter;

    public SelectionDialogueOfTestTargets(TestSuiteOfTargets testSuiteOfTargets, CriteriaSelectionDynamicTestCaseController controller) {
        this.testSuiteOfTargets = testSuiteOfTargets;
        this.minCounter = new Spinner<>(1, testSuiteOfTargets.getTestTargets().size(), 1);
        this.maxCounter = new Spinner<>(1, testSuiteOfTargets.getTestTargets().size(), 1);
        minCounter.setEditable(true);
        maxCounter.setEditable(true);
        this.controller = controller;
        this.setTitle("Select Test Targets to be generated");
        var grid = getGridPane();
        var scene = new Scene(grid);
        this.setScene(scene);
    }


    private GridPane getGridPane() {
        GridPane gridPane = new GridPane();
        var firstTestSuite = new Label("First Test Suite (included): ");
        var lastTestSuite = new Label("Last Test Suite (included): ");


        gridPane.add(firstTestSuite, 0, 0);
        gridPane.add(minCounter, 1, 0);
        gridPane.add(lastTestSuite, 0, 1);
        gridPane.add(maxCounter, 1, 1);

        Button createButton = new Button("Create");
        createButton.setDefaultButton(true);
        createButton.setOnAction(getCreateButtonEvent());
        Button cancelButton = new Button("Cancel");
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(getCancelButtonEvent());
        gridPane.add(createButton, 0, 3);
        gridPane.add(cancelButton, 1, 3);
        return gridPane;
    }

    private EventHandler<ActionEvent> getCreateButtonEvent() {

        return event -> {
            if (minCounter.getValue() <= maxCounter.getValue()) {
                var targets = testSuiteOfTargets.getTestTargets();
                TestSuiteOfTargets testSuiteOfTargets = new TestSuiteOfTargets();
                for (int i = minCounter.getValue(); i <= maxCounter.getValue(); i++) {
                    testSuiteOfTargets.add(targets.get(i));
                }
                controller.showTestSuites(testSuiteOfTargets);
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
