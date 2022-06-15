package gui.controller;

import gui.model.Graph;
import gui.view.LogEvaluationView;
import javafx.stage.FileChooser;
import logic.logevaluation.EvaluationLogic;
import logic.model.LogicGraph;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class LogEvaluationController {
    private final LogEvaluationView view;
    private final EvaluationLogic model;

    public LogEvaluationController(Graph graph) {
        this.view = new LogEvaluationView();
        this.model = new EvaluationLogic(new LogicGraph(graph.getJSON()));
    }

    public void setup(List<String> allLogs) {
        view.setup(this, model);
        view.show();
        model.addPropertyChangeListener(view);
        model.setLogs(allLogs);
    }


    public void exit() {
        view.close();
    }

    public void calculateCoverage() {
        model.calculateCoverage();
    }

    public void openLogFile() {
        var fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        var logFile = fileChooser.showOpenDialog(view);
        if (logFile != null) {
            try {
                var logs = Files.readAllLines(logFile.toPath());
                model.setLogs(logs);
            } catch (IOException e) {
                System.err.println("Could not read file " + logFile.getAbsolutePath());
                throw new RuntimeException(e);
            }
        }
    }
}
