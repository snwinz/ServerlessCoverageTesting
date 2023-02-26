package gui.view;

import gui.controller.GraphVisualisationController;
import gui.model.Arrow;
import gui.model.Graph;
import gui.model.NodeModel;
import gui.view.graphcomponents.DraggableArrow;
import gui.view.graphcomponents.DraggableNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GraphVisualisationView extends Stage implements PropertyChangeListener {

    private static final int HEIGHT = 900;
    private static final int WIDTH = 1400;
    private GraphVisualisationController controller;


    private Graph model;


    private List<DraggableNode> nodes = new ArrayList<>();
    private List<DraggableArrow> arrows = new ArrayList<>();


    public GraphVisualisationView(Graph model) {
        this.setTitle("Graph model");
        this.model = model;
        model.addPropertyChangeListener(this);
    }


    public void setup(GraphVisualisationController controller) {
        this.controller = controller;
    }


    public void updateAndShow() {
        ScrollPane visualizedGraph = createGraphDisplay();
        var borderPane = createBorderPane(visualizedGraph);

        var scene = new Scene(borderPane, WIDTH, HEIGHT);
        scene.setFill(Color.GHOSTWHITE);

        this.setScene(scene);
        this.show();
    }

    private EventHandler<ActionEvent> createEventHandlerCreateNode(double x, double y) {
        return event -> controller.createNode(x, y, nodes, arrows);
    }

    private EventHandler<ActionEvent> createEventHandlerCreateArrow() {
        return event -> controller.createArrow(nodes, arrows);
    }

    private EventHandler<ActionEvent> createEventHandlerCreateStaticTestCases() {
        return event -> controller.createStaticTestCases();
    }

    private EventHandler<ActionEvent> createEventHandlerCreateDynamicTestCases() {
        return event -> controller.createDynamicTestCases();
    }


    private BorderPane createBorderPane(ScrollPane visualizedGraph) {
        var borderPane = new BorderPane();
        var menuBar = createMenuBar();
        borderPane.setTop(menuBar);
        borderPane.setCenter(visualizedGraph);
        return borderPane;
    }


    private MenuBar createMenuBar() {
        var menuBar = new MenuBar();
        var file = new Menu("File");

        var saveGraphItemAs = new MenuItem("Save Graph As...");
        var saveGraphItem = new MenuItem("Save Graph");
        var closeItem = new MenuItem("Close");
        var executeTestCase = new MenuItem("Execute TestCases");
        var executeTestCasesFolder = new MenuItem("Execute TestCases of Folder");
        var analyzeLogFile = new MenuItem("Analyze Log File");
        var runMutants = new MenuItem("Run mutants");
        var openNewGraphItem = new MenuItem("Open Graph");
        closeItem.setOnAction(event -> controller.closeWindow());

        saveGraphItemAs.setOnAction(event -> controller.saveGraphAs(nodes, arrows));
        saveGraphItem.setOnAction(event -> controller.saveGraph(nodes, arrows));
        executeTestCase.setOnAction(event -> controller.executeTestcases());
        executeTestCasesFolder.setOnAction(event -> controller.executeTestcasesOfFolder());
        analyzeLogFile.setOnAction(event -> controller.evaluateLog(model));
        openNewGraphItem.setOnAction(event -> controller.openGraph());
        runMutants.setOnAction(event -> controller.runMutants());
        file.getItems().addAll(saveGraphItem, saveGraphItemAs, openNewGraphItem, analyzeLogFile, runMutants, executeTestCase, executeTestCasesFolder, closeItem);
        menuBar.getMenus().addAll(file);
        return menuBar;
    }

    private Optional<File> getLogFile() {
        var fileChooser = new FileChooser();
        var extFilter = new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        var file = fileChooser.showOpenDialog(this);
        return Optional.ofNullable(file);
    }

    private ScrollPane createGraphDisplay() {
        var group = new Group();
        createTitle(group);
        List<NodeModel> nodesModels = model.getNodes();
        List<Arrow> arrows = model.getArrows();

        List<DraggableNode> nodeViews = createNodeViews(nodesModels);
        List<DraggableArrow> arrowViews = createArrowViews(arrows);

        group.getChildren().addAll(nodeViews);
        group.getChildren().addAll(arrowViews);

        var visualizedGraph = new ScrollPane(group);
        visualizedGraph.setOnContextMenuRequested(contextMenuClicked());
        return visualizedGraph;
    }

    private List<DraggableArrow> createArrowViews(List<Arrow> arrowModels) {
        List<DraggableArrow> arrows = new ArrayList<>();
        for (var arrowModel : arrowModels) {
            Optional<DraggableNode> predecessor = getNodeByID(arrowModel.getPredecessor());
            Optional<DraggableNode> successor = getNodeByID(arrowModel.getSuccessor());
            if (successor.isPresent() && predecessor.isPresent()) {
                var line = new Line();
                var arrow1 = new Line();
                var arrow2 = new Line();
                var arrow = new DraggableArrow(predecessor.get(), successor.get(), line, arrow1, arrow2);
                arrow.setupArrow(controller);
                arrow.setOriginalEndOffsetPositionX(arrowModel.getOriginalEndOffsetPositionX());
                arrow.setOriginalEndOffsetPositionY(arrowModel.getOriginalEndOffsetPositionY());
                arrow.setOriginalStartOffsetPositionX(arrowModel.getOriginalStartOffsetPositionX());
                arrow.setOriginalStartOffsetPositionY(arrowModel.getOriginalStartOffsetPositionY());
                arrow.setAccessMode(arrowModel.getAccessMode());
                arrow.setIdentifier(arrowModel.getIdentifier());
                arrow.updatePosition();
                arrows.add(arrow);
            }
        }
        this.arrows = arrows;
        return arrows;
    }

    private Optional<DraggableNode> getNodeByID(long identifier) {
        DraggableNode resultNode = null;
        for (var node : nodes) {
            if (node.getIdentifier() == identifier) {
                resultNode = node;
                break;
            }
        }
        return Optional.ofNullable(resultNode);
    }

    private List<DraggableNode> createNodeViews(List<NodeModel> nodesModels) {
        List<DraggableNode> nodes = new ArrayList<>();
        for (var nodeModel : nodesModels) {
            String name = nodeModel.getNameOfNode();
            double x = nodeModel.getX();
            double y = nodeModel.getY();
            var node = new DraggableNode(name);
            node.setupDraggableNode(controller, x, y);
            node.setType(nodeModel.getType());
            node.setIdentifier(nodeModel.getIdentifier());
            node.setSourceList(nodeModel.getSourceList());
            node.setInputFormats(nodeModel.getInputFormats());
            nodes.add(node);
        }
        this.nodes = nodes;
        return nodes;
    }


    private EventHandler<ContextMenuEvent> contextMenuClicked() {
        return event -> {
            var contextMenu = new ContextMenu();
            var createNodeItem = new MenuItem("Create Node");
            createNodeItem.setOnAction(createEventHandlerCreateNode(event.getX(), event.getY()));

            var createArrowItem = new MenuItem("Create DraggableArrow");
            createArrowItem.setOnAction(createEventHandlerCreateArrow());


            var createStaticTestCasesItem = new MenuItem("Create static template for test cases");
            createStaticTestCasesItem.setOnAction(createEventHandlerCreateStaticTestCases());

            var createDynamicTestCasesItem = new MenuItem("Create data for test cases");
            createDynamicTestCasesItem.setOnAction(createEventHandlerCreateDynamicTestCases());


            contextMenu.getItems().addAll(createNodeItem, createArrowItem, createStaticTestCasesItem, createDynamicTestCasesItem);
            contextMenu.show(this, event.getScreenX(), event.getScreenY());
        };
    }

    private void createTitle(Group group) {
        var title = new Text("");
        var titleGroup = new Group();
        titleGroup.setLayoutX(0);
        titleGroup.setLayoutY(0);
        group.getChildren().add(title);
    }

    public void closeWindow() {
        this.close();
    }

    public void setModel(Graph model) {
        this.model = model;
        this.updateAndShow();
    }


    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        updateAndShow();
    }


}
