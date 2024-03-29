package gui.controller;

import gui.controller.criteriaSelection.CriteriaSelectionDynamicTestCaseController;
import gui.controller.criteriaSelection.CriteriaSelectionTestCaseTemplateController;
import gui.model.Graph;
import gui.model.modelcreation.Creator;
import gui.view.GraphVisualisationView;
import gui.view.StandardPresentationView;
import gui.view.graphcomponents.DraggableArrow;
import gui.view.graphcomponents.DraggableNode;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import logic.model.LogicGraph;
import shared.model.Testcase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GraphVisualisationController {
    private final GraphVisualisationView view;
    private File fileAliasOfView;
    private final Graph model;

    public GraphVisualisationController(Graph model) {
        this.view = new GraphVisualisationView(model);
        this.model = model;
    }


    public void saveGraphAs(List<DraggableNode> nodes, List<DraggableArrow> arrows) {
        var fileChooser = new FileChooser();
        var extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileAliasOfView = fileChooser.showSaveDialog(view);
        saveGraph(nodes, arrows);
    }


    public void saveGraph(List<DraggableNode> nodes, List<DraggableArrow> arrows) {
        if (fileAliasOfView == null) {
            saveGraphAs(nodes, arrows);
            return;
        }
        updatePositionsInModel(nodes, arrows);
        PersistenceUtilities.saveGraph(model, fileAliasOfView.getAbsolutePath());
    }


    private void updatePositionsInModel(List<DraggableNode> nodes, List<DraggableArrow> arrows) {
        for (DraggableNode node : nodes) {
            node.updatePosition();
            model.updateNodePosition(node.getIdentifier(), node.getX(), node.getY());
        }
        for (DraggableArrow arrow : arrows) {
            arrow.updateOffset();
            model.updateArrowPosition(arrow.getIdentifier(), arrow.getOriginalStartOffsetPositionX(),
                    arrow.getOriginalStartOffsetPositionY(), arrow.getOriginalEndOffsetPositionX(),
                    arrow.getOriginalEndOffsetPositionY());
        }
        model.informObservers();
    }


    public void openGraph() {
        var fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        var file = fileChooser.showOpenDialog(view);
        if (file != null) {
            model.clearGraph();
            var result = PersistenceUtilities.loadGraph(file.getAbsolutePath(), model);
            view.setModel(result);
            fileAliasOfView = file;
        }
    }
    public void createGraphFromApplication() {
        var fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        var file = fileChooser.showOpenDialog(view);
        if (file != null) {
            model.clearGraph();
            var result = Creator.createGraph(file.toPath());
            view.setModel(result);
            fileAliasOfView = file;
        }
    }

    public void remove(DraggableArrow draggableArrow) {
        model.removeNodeArrow(draggableArrow.getIdentifier());
    }

    public void remove(DraggableNode draggableNode) {
        model.removeNode(draggableNode.getIdentifier());
    }


    public void showArrowInfoBox(String info) {
        StandardPresentationView view = new StandardPresentationView("Arrow", info);
        view.show();
    }

    public void showNodeInfoBox(String info) {
        StandardPresentationView view = new StandardPresentationView("Node", info);
        view.show();
    }


    public void createArrow(List<DraggableNode> nodes, List<DraggableArrow> arrows) {
        updatePositionsInModel(nodes, arrows);
        var controller = new ArrowCreatorController(model);
        controller.setup();
    }

    public void createNode(double x, double y, List<DraggableNode> nodes, List<DraggableArrow> arrows) {
        updatePositionsInModel(nodes, arrows);
        var controller = new NodeCreatorController(model);
        controller.setup(x, y);
    }

    public void closeWindow() {
        view.closeWindow();

    }

    public void editNode(DraggableNode draggableNode) {
        var controller = new NodeEditorController(model);
        controller.setup(draggableNode);
    }

    public void editArrow(DraggableArrow draggableArrow) {
        var controller = new ArrowEditorController(model);
        controller.setup(draggableArrow);
    }

    public void updateNodePosition(DraggableNode draggableNode) {
        model.updateNodePosition(draggableNode.getIdentifier(), draggableNode.getX(), draggableNode.getY());
    }

    public void updateArrowPosition(DraggableArrow arrow) {
        model.updateArrowPosition(arrow.getIdentifier(), arrow.getOriginalStartOffsetPositionX(),
                arrow.getOriginalStartOffsetPositionY(), arrow.getOriginalEndOffsetPositionX(),
                arrow.getOriginalEndOffsetPositionY());
    }


    public void setup() {
        view.setup(this);
    }

    public void show() {
        view.updateAndShow();
    }

    public void createStaticTestCases() {

        CriteriaSelectionTestCaseTemplateController controller = new CriteriaSelectionTestCaseTemplateController(model);
        controller.setup();
    }

    public void createDynamicTestCases() {
        CriteriaSelectionDynamicTestCaseController controller = new CriteriaSelectionDynamicTestCaseController(model);
        controller.setup();
    }

    public void executeTestcases() {
        var fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        var tcFiles = fileChooser.showOpenMultipleDialog(view);
        if (tcFiles != null) {
            for (var tcFile : tcFiles) {
                var testcases = PersistenceUtilities.loadTCs(tcFile.toPath());
                TestCaseExecutionController controller = new TestCaseExecutionController(testcases, model, tcFile.toPath());
                controller.setup();
            }
        }
    }

    public void executeTestcasesOfFolder() {
        var directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        var folder = directoryChooser.showDialog(view);

        if (folder != null) {
            var folderPath = folder.toPath();
            try (var walk = Files.walk(folderPath)) {
                var files = walk
                        .filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().endsWith(".json")).toList();
                List<Testcase> allTestcases = new ArrayList<>();
                for (Path file : files) {
                    var testcases = PersistenceUtilities.loadTCs(file);
                    allTestcases.addAll(testcases);
                }

                TestCaseExecutionController controller = new TestCaseExecutionController(allTestcases, model, folderPath.resolve("allTCs.json"));
                controller.setup();
            } catch (IOException e) {
                System.err.println("Could not read : " + folder);
            }
        }
    }

    public void evaluateLog(Graph graph) {

        var fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        var file = fileChooser.showOpenDialog(view);
        if (file != null) {
            try {
                var logs = Files.readAllLines(file.toPath());
                LogEvaluationController controller = new LogEvaluationController(graph);
                controller.setup(logs);
            } catch (IOException e) {
                System.err.println("Error while reading file " + file.getName());
            }
        }
    }

    public void runMutants() {
        MutationController controller = new MutationController();
        controller.setup(new LogicGraph(model.getJSON()));
    }
}
