package gui.view;

import gui.controller.MutationController;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import logic.model.LogicGraph;
import logic.model.NodeModel;
import logic.mutation.MutationExecutor;
import shared.model.Mutant;
import shared.model.NodeType;
import shared.model.TestSuite;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

public class MutationView extends Stage implements PropertyChangeListener {
    private final MutationController controller;
    private final MutationExecutor mutationExecutor;
    private final TextField resetFunctionName = new TextField();
    private final TextField regionAWS = new TextField();
    private final TextField mutationFolder = new TextField();
    private final TextField testSuiteFolder = new TextField();
    private final TextField resultFolder = new TextField();
    private final TextArea applicationFunctions = new TextArea();
    private final LogicGraph logicGraph;
    private List<Mutant> mutants;


    private List<TestSuite> testSuites;
    private final TextArea output = new TextArea();


    public MutationView(MutationExecutor mutationExecutor, MutationController mutationController, LogicGraph graph) {
        this.mutationExecutor = mutationExecutor;
        this.controller = mutationController;
        this.logicGraph = graph;
        createView();
    }

    private void createView() {
        getConfigProperties();
        var grid = getGridPane();
        var scene = new Scene(grid);
        this.setScene(scene);
    }


    private Pane getGridPane() {
        var borderPane = new BorderPane();
        var menuBar = createMenuBar();
        borderPane.setTop(menuBar);
        var mutationOverview = getMutationOverview();
        borderPane.setCenter(mutationOverview);
        return borderPane;
    }

    private ScrollPane getMutationOverview() {
        ScrollPane scrollpane = new ScrollPane();
        var grid = new GridPane();
        scrollpane.setContent(grid);


        var regionLabel = new Label("AWS region:");
        var resetLabel = new Label("Reset function:");
        var mutationFolderLabel = new Label("Mutation folder:");
        var testSuiteFolderLabel = new Label("Test suites folder:");
        Button selectTestSuiteFolder = new Button("...");
        Button selectMutationFolder = new Button("...");
        selectTestSuiteFolder.setOnAction(event -> {
            var directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choose folder of test suites");
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            var inputFolder = directoryChooser.showDialog(this);
            if (inputFolder != null) {
                testSuiteFolder.setText(inputFolder.getAbsolutePath());
            }
        });

        selectMutationFolder.setOnAction(event -> {
            var directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choose folder of mutants");
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            var inputFolder = directoryChooser.showDialog(this);
            if (inputFolder != null) {
                mutationFolder.setText(inputFolder.getAbsolutePath());
            }
        });


        Button loadData = new Button("Read from folders");
        loadData.setOnAction(event -> {
            saveConfigProperties();
            controller.openMutants(Path.of(mutationFolder.getText()));
            controller.openTestSuites(Path.of(testSuiteFolder.getText()));
        });


        ViewHelper.addToGridInHBox(grid, regionLabel, regionAWS, resetLabel, resetFunctionName);
        ViewHelper.addToGridInHBox(grid, mutationFolderLabel, mutationFolder, selectMutationFolder, testSuiteFolderLabel, testSuiteFolder, selectTestSuiteFolder, loadData);

        setAllFunctions(applicationFunctions);

        Label allFunctionsLabel = new Label("All functions of app:");
        ViewHelper.addToGridInHBox(grid, allFunctionsLabel, applicationFunctions);

        if (areMutationsAndTestSuitsAvailable()) {
            var text = String.format("""
                    Number of Mutants: %d
                    Number of TestSuits: %d
                    """, mutants.size(), testSuites.size());
            output.setText(text);
            Label outputLabel = new Label("Info:");
            ViewHelper.addToGridInHBox(grid, outputLabel, output);

            final Label startLabelMutant = new Label("Start mutants at:");
            final Label endLabelMutant = new Label("End mutants at:");
            final Spinner<Integer> mutantsNumberMin = new Spinner<>(0, mutants.size()-1, 0, 1);
            final Spinner<Integer> mutantsNumberMax = new Spinner<>(1, mutants.size()-1, 0, 1);
            mutantsNumberMin.setEditable(true);
            mutantsNumberMax.setEditable(true);
            ViewHelper.addToGridInHBox(grid, startLabelMutant, mutantsNumberMin, endLabelMutant, mutantsNumberMax);


            Label targetLabel = new Label("Target folder:");
            Button selectFolder = new Button("...");
            selectFolder.setOnAction(event -> {
                var directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle("Choose folder for mutant results");
                directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
                var mutantTargetFile = directoryChooser.showDialog(this);
                if (mutantTargetFile != null) {
                    resultFolder.setText(mutantTargetFile.getAbsolutePath());
                }
            });
            ViewHelper.addToGridInHBox(grid, targetLabel, resultFolder, selectFolder);


            Button startMutations = new Button("Start killing of mutants");
            startMutations.setOnAction(event -> {
                saveConfigProperties();
                var allFunctions = Arrays.asList(applicationFunctions.getText().split(Pattern.quote("\n")));
                controller.startMutations(mutants, testSuites, allFunctions, mutantsNumberMin.getValue(),
                        mutantsNumberMax.getValue(), regionAWS.getText(), resetFunctionName.getText(), resultFolder.getText());
            });
            ViewHelper.addToGridInHBox(grid, startMutations);

        }


        return scrollpane;
    }

    private void setAllFunctions(TextArea applicationFunctions) {
        var nodes = logicGraph.getNodes();
        if (nodes == null || nodes.size() == 0) {
            return;
        }

        var allFunctions = nodes.stream().filter(node -> NodeType.FUNCTION.equals(node.getType())).map(NodeModel::getNameOfNode).toList();
        applicationFunctions.setText(String.join(System.lineSeparator(), allFunctions));
    }

    private boolean areMutationsAndTestSuitsAvailable() {
        return this.mutants != null && this.testSuites != null;
    }


    private MenuBar createMenuBar() {
        var menuBar = new MenuBar();
        var file = new Menu("File");
        var saveResult = new MenuItem("Save result");

        file.getItems().addAll(saveResult);
        menuBar.getMenus().addAll(file);
        return menuBar;
    }

    private void getConfigProperties() {
        Properties properties = new Properties();
        String pathOfProperties = "settingMutation.xml";
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


            String folderOfMutants = properties.getProperty("folderOfMutants");
            mutationFolder.setText(folderOfMutants);

            String folderOfTestSuites = properties.getProperty("folderOfTestSuites");
            testSuiteFolder.setText(folderOfTestSuites);

            String folderOfResult = properties.getProperty("folderOfResult");
            resultFolder.setText(folderOfResult);

            String functionsOfApplication = properties.getProperty("functionsOfApplication");
            applicationFunctions.setText(functionsOfApplication);

        } catch (IOException e) {
            System.err.println("Problem while reading " + pathOfProperties);
            e.printStackTrace();
        }
    }


    private void saveConfigProperties() {
        Properties properties = new Properties();
        String pathOfProperties = "settingMutation.xml";

        try {
            if (resetFunctionName.getText() != null && regionAWS.getText() != null && mutationFolder.getText() != null
                    && testSuiteFolder.getText() != null) {
                properties.setProperty("resetFunctionName", resetFunctionName.getText());
                properties.setProperty("regionAWS", regionAWS.getText());
                properties.setProperty("folderOfMutants", mutationFolder.getText());
                properties.setProperty("folderOfTestSuites", testSuiteFolder.getText());
                properties.setProperty("functionsOfApplication", applicationFunctions.getText());
            }
            if (resultFolder.getText() != null) {
                properties.setProperty("folderOfResult", resultFolder.getText());
            }
            Path path = Path.of(pathOfProperties);
            properties.storeToXML(Files.newOutputStream(path), null);
        } catch (
                IOException e) {
            System.err.println("Problem while writing " + pathOfProperties);
            e.printStackTrace();
        }

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        var value = evt.getNewValue();
        if ("mutationsUpdated".equals(evt.getPropertyName())) {
            this.mutants = mutationExecutor.getMutants();
            createView();
        }
        if ("testSuitesUpdated".equals(evt.getPropertyName())) {
            this.testSuites = mutationExecutor.getTestSuites();
            createView();
        }
    }
}
