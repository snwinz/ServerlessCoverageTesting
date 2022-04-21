package gui.view.graphcomponents;

import gui.controller.GraphVisualisationController;
import gui.model.FunctionInputFormat;
import shared.model.NodeType;
import gui.model.SourceCodeLine;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class DraggableNode extends Group {

    private double mouseClickPositionX = 0;
    private double mouseClickPositionY = 0;
    private List<SourceCodeLine> sourceList;
    private final String nameOfNode;


    private double x;
    private double y;


    private GraphVisualisationController controller;
    private long identifier;
    private NodeType type;
    private FunctionInputFormat inputFormats;


    public DraggableNode(String name) {
        this.nameOfNode = name;
        var text = new Text(name);
        getChildren().add(text);
    }


    public void setupDraggableNode(GraphVisualisationController controller, double xPosition, double yPosition) {
        this.controller = controller;
        setOnMousePressed(clickMouse());
        setOnMouseDragged(dragMouse());
        setOnMouseDragReleased(releaseMouse());
        layoutXProperty().set(xPosition);
        layoutYProperty().set(yPosition);
        updatePosition();
        setOnContextMenuRequested(contextMenuClicked());
    }


    private void createSymbol(NodeType type) {

        Node symbol;
        switch (type) {
            case DATA_STORAGE:
                symbol = getSymbol("images/database.png");
                break;
            case FUNCTION:
                symbol = getSymbol("images/lambda.png");
                break;
            case STREAM:
                symbol = getSymbol("images/stream.png");
                break;
            case QUEUE:
                symbol = getSymbol("images/queue.png");
                break;
            case MAIL:
                symbol = getSymbol("images/mail.png");
                break;
            case STANDARD_NODE:
                symbol = new Sphere(50);
            default:
                symbol = new Sphere(50);
        }
        getChildren().add(symbol);
    }

    private Node getSymbol(String pathToImage) {
        var myImagePath = Paths.get(pathToImage);
        Node symbol = new Sphere(10);
        if (!Files.exists(myImagePath)) {
            System.err.println("Path to \"" + pathToImage + "\" is not valid. default symbol is used");
            return symbol;
        }
        try {
            String path = new File(myImagePath.toAbsolutePath().toString()).toURI().toURL().toExternalForm();
            var image = new Image(path);
            var imageView = new ImageView();
            imageView.setImage(image);
            symbol = imageView;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return symbol;
    }

    private EventHandler<MouseEvent> clickMouse() {

        return event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                mouseClickPositionX = event.getSceneX();
                mouseClickPositionY = event.getSceneY();
                if (event.getClickCount() == 2) {
                    showNodeInfo();
                }
            }

        };
    }

    private EventHandler<MouseEvent> releaseMouse() {
        return event -> {
            updatePosition();
            controller.updateNodePosition(this);
        };
    }


    private EventHandler<MouseEvent> dragMouse() {
        return event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                double deltaX = event.getSceneX() - mouseClickPositionX;
                double deltaY = event.getSceneY() - mouseClickPositionY;
                double updatedPositionX = getLayoutX() + deltaX;
                double updatedPositionY = getLayoutY() + deltaY;
                if (updatedPositionX >= 0 && updatedPositionY >= 0) {
                    layoutXProperty().set(getLayoutX() + deltaX);
                    layoutYProperty().set(getLayoutY() + deltaY);
                    mouseClickPositionX = event.getSceneX();
                    mouseClickPositionY = event.getSceneY();
                }
            }
        };
    }

    private EventHandler<ContextMenuEvent> contextMenuClicked() {

        var contextMenu = new ContextMenu();

        var removeNodeItem = new MenuItem("Remove Node");
        var infoOfNodeItem = new MenuItem("Show information of node");
        var editNodeItem = new MenuItem("Edit node");


        removeNodeItem.setOnAction(event -> controller.remove(this));
        infoOfNodeItem.setOnAction(event -> showNodeInfo());

        editNodeItem.setOnAction(event -> controller.editNode(this));

        contextMenu.getItems().addAll(removeNodeItem, infoOfNodeItem, editNodeItem);

        return event -> {
            event.consume();
            contextMenu.show(this, event.getScreenX(), event.getScreenY());

        };
    }

    private void showNodeInfo() {
        controller.showNodeInfoBox(this.toString());
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
        createSymbol(type);
    }

    public String getName() {
        return this.nameOfNode;
    }

    public long getIdentifier() {
        return identifier;
    }

    public void setIdentifier(long identifier) {
        this.identifier = identifier;
    }

    public void updatePosition() {
        x = layoutXProperty().get();
        y = layoutYProperty().get();
    }

    public List<SourceCodeLine> getSourceList() {
        return sourceList;
    }

    public void setSourceList(List<SourceCodeLine> sourceList) {
        this.sourceList = sourceList;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setInputFormats(FunctionInputFormat inputFormats) {
        this.inputFormats = inputFormats;
    }

    public FunctionInputFormat getInputFormats() {
        return inputFormats;
    }

    @Override
    public String toString() {
        String sourceListEntry =     sourceList != null ? ", source=\" +" +    sourceList.stream().map(SourceCodeLine::toString).reduce("", (a, b) ->
                a + "\n" + b) : "";
        return "DraggableNode{" +
                "x=" + layoutXProperty().get() +
                ", y=" + layoutYProperty().get() +
                ", nameOfNode='" + nameOfNode + "\n" +
                ", type=" + type +
                sourceListEntry +
                ", input formats=" + inputFormats + "\n" +
                ", identifier=" + identifier +
                '}';
    }


}