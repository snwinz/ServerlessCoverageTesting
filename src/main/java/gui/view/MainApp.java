package gui.view;

import gui.controller.GraphVisualisationController;
import gui.model.Graph;
import javafx.application.Application;
import javafx.stage.Stage;
import logic.util.logger.MyLogger;

import java.io.IOException;

public class MainApp extends Application {


    /**
     * The main() method is ignored in correctly deployed JavaFX application. main()
     * serves only as fallback in case the application can not be launched through
     * deployment artifacts, e.g., in IDEs with limited FX support. NetBeans ignores
     * main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            MyLogger.setup();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Problems with creating the log files");
        }
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        createMainWindow();

    }

    private void createMainWindow() {

        var model = new Graph();
        var controller = new GraphVisualisationController(model);
        controller.setup();
        controller.show();

    }
}
