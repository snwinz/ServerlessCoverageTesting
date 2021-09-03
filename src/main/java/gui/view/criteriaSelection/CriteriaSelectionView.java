package gui.view.criteriaSelection;


import gui.controller.criteriaSelection.CriteriaSelectionStrategyController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class CriteriaSelectionView extends Stage {


    protected final CheckBox allResourceCB = new CheckBox();
    protected final CheckBox allRelationsCB = new CheckBox();
    protected final CheckBox allDefsCB = new CheckBox();
    protected final CheckBox allDefUseCB = new CheckBox();
    protected final CheckBox allUsesCB = new CheckBox();
    private final CriteriaSelectionStrategyController controller;


    public CriteriaSelectionView(String title, CriteriaSelectionStrategyController controller) {
        this.controller = controller;
        this.setTitle(title);
        var grid = getGridPane();
        var scene = new Scene(grid);
        this.setScene(scene);
    }

    private GridPane getGridPane() {
        GridPane gridPane = new GridPane();
        var allResourcesText = new Label("All resources: ");
        var allRelationsText = new Label("All relations: ");
        var allDefsText = new Label("All defs: ");
        var allDefUseText = new Label("All defuse: ");
        var allUsesText = new Label("All uses: ");

        gridPane.add(allResourcesText, 0, 0);
        gridPane.add(allResourceCB, 1, 0);
        gridPane.add(allRelationsText, 0, 1);
        gridPane.add(allRelationsCB, 1, 1);
        gridPane.add(allDefsText, 0, 2);
        gridPane.add(allDefsCB, 1, 2);
        gridPane.add(allDefUseText, 0, 3);
        gridPane.add(allDefUseCB, 1, 3);
        gridPane.add(allUsesText, 0, 4);
        gridPane.add(allUsesCB, 1, 4);

        Button createButton = new Button("Create");
        createButton.setDefaultButton(true);
        createButton.setOnAction(getCreateButtonEvent());
        Button cancelButton = new Button("Cancel");
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(getCancelButtonEvent());
        gridPane.add(createButton, 0, 6);
        gridPane.add(cancelButton, 1, 6);
        return gridPane;
    }

    private EventHandler<ActionEvent> getCreateButtonEvent() {
        return event ->
                controller.handleInput(allResourceCB.isSelected(), allRelationsCB.isSelected(),
                        allDefsCB.isSelected(), allDefUseCB.isSelected(), allUsesCB.isSelected());
    }

    private EventHandler<ActionEvent> getCancelButtonEvent() {
        return event -> controller.cancel();
    }

}
