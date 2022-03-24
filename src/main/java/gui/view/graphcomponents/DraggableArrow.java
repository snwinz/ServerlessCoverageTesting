package gui.view.graphcomponents;

import gui.controller.GraphVisualisationController;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import shared.model.AccessMode;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

public class DraggableArrow extends Group {
    private static final double ARROW_LENGTH = 20;
    private final Line line;
    private final Text text;

    private GraphVisualisationController controller;

    private final DoubleProperty startXOffset = new SimpleDoubleProperty(0);
    private final DoubleProperty startYOffset = new SimpleDoubleProperty(0);
    private final DoubleProperty endXOffset = new SimpleDoubleProperty(0);
    private final DoubleProperty endYOffset = new SimpleDoubleProperty(0);

    private double originalStartOffsetPositionX = 0;
    private double originalStartOffsetPositionY = 0;
    private double originalEndOffsetPositionX = 0;
    private double originalEndOffsetPositionY = 0;

    private double originalClickPositionX = 0;

    private double originalClickPositionY = 0;
    private boolean wasClickNearerToStartNode = true;

    private final DraggableNode successor;
    private Set<AccessMode> accessMode;

    private long identifier;

    private final DraggableNode predecessor;

    public DraggableArrow(DraggableNode predecessor, DraggableNode successor,
                          Line line, Line arrow1, Line arrow2) {
        super(line, arrow1, arrow2);
        this.successor = successor;
        this.predecessor = predecessor;
        DoubleProperty startX = predecessor.layoutXProperty();
        DoubleProperty startY = predecessor.layoutYProperty();
        DoubleProperty endX = successor.layoutXProperty();
        DoubleProperty endY = successor.layoutYProperty();

        line.strokeWidthProperty().set(3);
        arrow1.strokeWidthProperty().set(3);
        arrow2.strokeWidthProperty().set(3);
        this.line = line;
        InvalidationListener updater = o -> {
            double ex = getEndX();
            double ey = getEndY();
            double sx = getStartX();
            double sy = getStartY();

            arrow1.setEndX(ex);
            arrow1.setEndY(ey);
            arrow2.setEndX(ex);
            arrow2.setEndY(ey);

            if (ex == sx && ey == sy) {
                // arrow parts of length 0
                arrow1.setStartX(ex);
                arrow1.setStartY(ey);
                arrow2.setStartX(ex);
                arrow2.setStartY(ey);
            } else {
                double factor = ARROW_LENGTH / Math.hypot(sx - ex, sy - ey);

                double dx = (sx - ex) * factor;
                double dy = (sy - ey) * factor;

                double ox = (sx - ex) * factor;
                double oy = (sy - ey) * factor;

                arrow1.setStartX(ex + dx - oy);
                arrow1.setStartY(ey + dy + ox);
                arrow2.setStartX(ex + dx + oy);
                arrow2.setStartY(ey + dy - ox);
            }
        };

        startXProperty().addListener(updater);
        startYProperty().addListener(updater);
        endXProperty().addListener(updater);
        endYProperty().addListener(updater);
        updater.invalidated(null);

        DoubleBinding startXBinding = getPositiveSummedBinding(startX, startXOffset);
        DoubleBinding startYBinding = getPositiveSummedBinding(startY, startYOffset);
        DoubleBinding endXBinding = getPositiveSummedBinding(endX, endXOffset);
        DoubleBinding endYBinding = getPositiveSummedBinding(endY, endYOffset);


        startXProperty().bind(startXBinding);
        startYProperty().bind(startYBinding);
        endXProperty().bind(endXBinding);
        endYProperty().bind(endYBinding);

        this.text = new Text();
        var defaultFont = Font.getDefault();
        this.text.setFont(Font.font(defaultFont.getName(), FontWeight.BOLD, defaultFont.getSize()));
        getChildren().add(text);

        DoubleBinding xPositionText = getMiddle(startXBinding, endXBinding);
        DoubleBinding yPositionText = getMiddle(startYBinding, endYBinding);


        text.xProperty().bind(xPositionText);
        text.yProperty().bind(yPositionText);

        this.setOnMousePressed(mouseClicked());
        this.setOnMouseDragged(dragMouse());
        this.setOnMouseDragReleased(releaseMouse());
    }

    private EventHandler<MouseEvent> releaseMouse() {
        return event -> {
            updateOffset();
            controller.updateArrowPosition(this);
        };
    }

    private DoubleBinding getMiddle(DoubleBinding start, DoubleBinding end) {
        return new DoubleBinding() {
            {
                super.bind(start, end);
            }

            @Override
            protected double computeValue() {
                var startValue = start.doubleValue();
                var endValue = end.doubleValue();
                var diff = Math.abs(startValue - endValue);
                return startValue > endValue ? endValue + diff / 2 : startValue + diff / 2;

            }
        };
    }

    private DoubleBinding getPositiveSummedBinding(DoubleProperty summand1, DoubleProperty summand2) {
        return new DoubleBinding() {
            {
                super.bind(summand1, summand2);
            }

            @Override
            protected double computeValue() {
                double result = summand1.getValue() + summand2.getValue();
                return (result > 0) ? result : 0;
            }
        };
    }

    private static double getShift(Double currentOffset, String textOfAffectedCoordinate) {

        var dialog = new TextInputDialog(currentOffset.toString());
        dialog.setTitle("Dialog to change " + textOfAffectedCoordinate);
        dialog.setHeaderText("Change" + textOfAffectedCoordinate + "!");
        dialog.setContentText("Enter a number:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                return Double.parseDouble(result.get());
            } catch (NumberFormatException e) {
                System.err.println("Input could not be parsed to an integer");
                System.err.println(Arrays.toString(e.getStackTrace()));
                var alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Dialog");
                alert.setHeaderText("Input could not be parsed to an integer");
                alert.setContentText("Please try it again!");
                alert.showAndWait();
                return currentOffset;
            }
        }
        return currentOffset;
    }


    public void setupArrow(GraphVisualisationController controller) {
        this.controller = controller;
        this.setOnContextMenuRequested(contextMenuClicked());
    }

    private double getStartX() {
        return line.getStartX();
    }


    private DoubleProperty startXProperty() {
        return line.startXProperty();
    }

    private double getStartY() {
        return line.getStartY();
    }

    private DoubleProperty startYProperty() {
        return line.startYProperty();
    }

    private double getEndX() {
        return line.getEndX();
    }

    private DoubleProperty endXProperty() {
        return line.endXProperty();
    }

    private double getEndY() {
        return line.getEndY();
    }

    private DoubleProperty endYProperty() {
        return line.endYProperty();
    }

    private EventHandler<MouseEvent> dragMouse() {
        return event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                double deltaX = event.getX() - originalClickPositionX;
                double deltaY = event.getY() - originalClickPositionY;

                if (wasClickNearerToStartNode) {
                    startXOffset.set(deltaX + originalStartOffsetPositionX);
                    startYOffset.set(deltaY + originalStartOffsetPositionY);
                } else {
                    endXOffset.set(deltaX + originalEndOffsetPositionX);
                    endYOffset.set(deltaY + originalEndOffsetPositionY);
                }
            }
        };
    }

    public DraggableNode getSuccessor() {
        return successor;
    }

    public DraggableNode getPredecessor() {
        return predecessor;
    }

    private EventHandler<MouseEvent> mouseClicked() {
        return event -> {
            if (event.getButton() == MouseButton.PRIMARY) {


                originalClickPositionX = event.getX();
                originalClickPositionY = event.getY();
                updateOffset();

                double deltaXTolerance = Math.abs((endXProperty().getValue() - startXProperty().getValue()) / 2);
                wasClickNearerToStartNode = Math.abs(event.getX() - startXProperty().getValue()) < Math.abs(deltaXTolerance);


            }
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                var contentOfArrow = this.toString();
                controller.showArrowInfoBox(contentOfArrow);
            }
        };
    }

    private EventHandler<ContextMenuEvent> contextMenuClicked() {

        var contextMenu = new ContextMenu();

        var itemRemoveArrow = new MenuItem("Remove Arrow");
        var itemStartX = new MenuItem("Move starting point of X coordinates");
        var itemStartY = new MenuItem("Move starting point of Y coordinates");
        var itemEndX = new MenuItem("Move end point of X coordinates");
        var itemEndY = new MenuItem("Move end point of Y coordinates");
        var itemShowInfo = new MenuItem("Show information of arrow");
        var editItem = new MenuItem("Edit arrow");

        itemRemoveArrow.setOnAction(event -> controller.remove(DraggableArrow.this));

        itemStartX.setOnAction(event -> {
            double shift =
                    getShift(startXOffset.getValue(), "starting point x");
            startXOffset.set(shift);
        });

        itemStartY.setOnAction(event -> {
            double shift =
                    getShift(startYOffset.getValue(), "starting point y");
            startYOffset.set(shift);
        });
        itemEndX.setOnAction(event -> {
            double shift =
                    getShift(endXOffset.getValue(), "ending point x");
            endXOffset.set(shift);
        });

        itemEndY.setOnAction(event -> {
            double shift =
                    getShift(endYOffset.getValue(), "ending point y");
            endYOffset.set(shift);
        });


        itemShowInfo.setOnAction(event -> {
            var contentOfArrow = this.toString();
            controller.showArrowInfoBox(contentOfArrow);
        });

        editItem.setOnAction(event -> controller.editArrow(this));

        contextMenu.getItems().addAll(itemRemoveArrow, itemStartX, itemStartY, itemEndX, itemEndY, itemShowInfo, editItem);

        return event -> {
            event.consume();
            contextMenu.show(this, event.getScreenX(), event.getScreenY());

        };
    }

    public void updatePosition() {
        startXOffset.set(originalStartOffsetPositionX);
        startYOffset.set(originalStartOffsetPositionY);
        endXOffset.set(originalEndOffsetPositionX);
        endYOffset.set(originalEndOffsetPositionY);
    }

    public void updateOffset() {
        originalStartOffsetPositionX = startXOffset.getValue();
        originalStartOffsetPositionY = startYOffset.getValue();

        originalEndOffsetPositionX = endXOffset.getValue();
        originalEndOffsetPositionY = endYOffset.getValue();
    }

    public void setOriginalStartOffsetPositionX(double originalStartOffsetPositionX) {
        this.originalStartOffsetPositionX = originalStartOffsetPositionX;
    }

    public void setOriginalStartOffsetPositionY(double originalStartOffsetPositionY) {
        this.originalStartOffsetPositionY = originalStartOffsetPositionY;
    }

    public void setOriginalEndOffsetPositionX(double originalEndOffsetPositionX) {
        this.originalEndOffsetPositionX = originalEndOffsetPositionX;
    }

    public void setOriginalEndOffsetPositionY(double originalEndOffsetPositionY) {
        this.originalEndOffsetPositionY = originalEndOffsetPositionY;
    }

    @Override
    public String toString() {
        return "DraggableArrow{" +
                "line=" + line +
                ", controller=" + controller +
                ", startXOffset=" + startXOffset +
                ", startYOffset=" + startYOffset +
                ", endXOffset=" + endXOffset +
                ", endYOffset=" + endYOffset +
                ", originalClickPositionX=" + originalClickPositionX +
                ", originalClickPositionY=" + originalClickPositionY +
                ", originalStartOffsetPositionX=" + originalStartOffsetPositionX +
                ", originalStartOffsetPositionY=" + originalStartOffsetPositionY +
                ", originalEndOffsetPositionX=" + originalEndOffsetPositionX +
                ", originalEndOffsetPositionY=" + originalEndOffsetPositionY +
                ", successor=" + successor +
                ", predecessor=" + predecessor +
                ", accessMode =" + accessMode +
                ", identifier=" + identifier +
                '}';
    }

    public void setAccessMode(Set<AccessMode> accessMode) {
        this.accessMode = accessMode;
        updateText();
    }

    private void updateText() {
        if (accessMode != null) {
            StringBuilder text = new StringBuilder();
            for (var mode : accessMode) {
                text.append(" ").append(mode.toShortString());
            }
            this.text.setText(text.toString());
        }
    }

    public Set<AccessMode> getAccessMode() {
        return accessMode;
    }

    public double getOriginalStartOffsetPositionX() {
        return originalStartOffsetPositionX;
    }

    public double getOriginalStartOffsetPositionY() {
        return originalStartOffsetPositionY;
    }

    public double getOriginalEndOffsetPositionX() {
        return originalEndOffsetPositionX;
    }

    public double getOriginalEndOffsetPositionY() {
        return originalEndOffsetPositionY;
    }

    public long getIdentifier() {
        return identifier;
    }

    public void setIdentifier(long identifier) {
        this.identifier = identifier;
    }
}