package gui.controller;

import gui.controller.criteriaSelection.CriteriaSelectionTestCaseTemplateController;
import gui.model.Graph;
import gui.view.GraphVisualisationView;
import gui.view.StandardPresentationView;
import gui.view.graphcomponents.DraggableArrow;
import gui.view.graphcomponents.DraggableNode;
import javafx.stage.FileChooser;
import logic.logevaluation.CoverageCalculation;

import java.io.File;
import java.util.List;

public class GraphVisualisationController {
    private final GraphVisualisationView view;

    private final Graph model;

    public GraphVisualisationController(Graph model) {
        this.view = new GraphVisualisationView(model);
        this.model = model;
    }


    public void saveGraph(List<DraggableNode> nodes, List<DraggableArrow> arrows) {

        updatePositionsInModel(nodes, arrows);
        var fileChooser = new FileChooser();
        var extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        var file = fileChooser.showSaveDialog(view);
        if (file != null) {
            PersistenceUtilities.saveGraph(model, file.getAbsolutePath());
        }
    }

    private void updatePositionsInModel(List<DraggableNode> nodes, List<DraggableArrow> arrows) {
        for (DraggableNode node : nodes) {
            node.updatePosition();
            model.updateNodePosition(node.getIdentifier(), node.getX(), node.getY());
        }
        for (DraggableArrow arrow : arrows) {
            arrow.updateOffset();
            model.updateArrowPosition(arrow.getIdentifier(), arrow.getOriginalStartOffsetPositionX(),
                    arrow.getOriginalStartOffsetPositionY(), arrow.getOriginalEndOffsetPositionX(), arrow.getOriginalEndOffsetPositionY());
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
            result.ifPresent(view::setModel);
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


    public void createArrow() {
        var controller = new ArrowCreatorController(model);
        controller.setup();
    }

    public void createNode(double x, double y) {
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
                arrow.getOriginalStartOffsetPositionY(), arrow.getOriginalEndOffsetPositionX(), arrow.getOriginalEndOffsetPositionY());
    }


    public void setup() {
        view.setup(this);
    }

    public void show() {
        view.updateAndshow();
    }

    public void createStaticTestCases() {

        CriteriaSelectionTestCaseTemplateController controller = new CriteriaSelectionTestCaseTemplateController(model);
        controller.setup();
    }

    public void analyzeLogFile(File file) {
        CoverageCalculation coverageCalculation = new CoverageCalculation();
        String result = coverageCalculation.calculateCoverage(file);
        StandardPresentationView view = new StandardPresentationView("Coverage");
        view.setText(result);
        view.show();
    }
}
