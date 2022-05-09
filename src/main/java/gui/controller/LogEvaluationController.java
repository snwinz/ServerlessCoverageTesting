package gui.controller;

import gui.model.Graph;
import gui.view.LogEvaluationView;
import javafx.stage.FileChooser;
import logic.logevaluation.EvaluationLogic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LogEvaluationController {
    private final LogEvaluationView view;
    private final EvaluationLogic model;

    public LogEvaluationController(List<String> allLogs, Graph graph) {
        this.view = new LogEvaluationView(allLogs, graph);
        this.model = new EvaluationLogic(graph);

    }

    public void setup() {
        view.setup(this, model);
        view.show();
    }


    public void exit() {
        view.close();
    }

    public void calculateCoverage(List<String> log) {
        model.calculateCoverage(log);
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
