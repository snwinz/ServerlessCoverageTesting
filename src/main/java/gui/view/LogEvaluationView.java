package gui.view;

import gui.controller.LogEvaluationController;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import logic.logevaluation.EvaluationLogic;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class LogEvaluationView extends Stage implements PropertyChangeListener {
    private final TextArea log = new TextArea();
    private final TextArea output = new TextArea();
    private LogEvaluationController controller;
    private EvaluationLogic model;


    public LogEvaluationView() {
    }

    public void setup(LogEvaluationController controller, EvaluationLogic model) {
        this.controller = controller;
        this.model = model;
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
        var logOverview = getTestExecutionEnvironment();
        borderPane.setCenter(logOverview);
        return borderPane;
    }

    private ScrollPane getTestExecutionEnvironment() {
        {
            ScrollPane scrollpane = new ScrollPane();
            var grid = new GridPane();
            scrollpane.setContent(grid);

            Label descriptionLog = new Label("Log to be evaluated:");


            grid.addRow(grid.getRowCount(), descriptionLog);
            log.textProperty().bind(model.logTextProperty());
            log.setEditable(false);
            grid.addRow(grid.getRowCount(), log);

            Label coverage = new Label("Coverage:");

            output.textProperty().bind(model.coverageTextProperty());
            grid.addRow(grid.getRowCount(), coverage);
            grid.addRow(grid.getRowCount(), output);

            Button calculateCoverage = new Button("Calculate Coverage");
            calculateCoverage.setOnAction(e -> controller.calculateCoverage());
            ViewHelper.addToGridInHBox(grid, calculateCoverage);
            return scrollpane;
        }
    }

    private MenuBar createMenuBar() {
        var menuBar = new MenuBar();
        var file = new Menu("File");

        var close = new MenuItem("close");
        var openLogFile = new MenuItem("Open Log file");

        close.setOnAction(event -> {
            controller.exit();
        });
        openLogFile.setOnAction(event -> {
            controller.openLogFile();
        });
        file.getItems().addAll(close, openLogFile);
        menuBar.getMenus().addAll(file);
        return menuBar;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    }

}
