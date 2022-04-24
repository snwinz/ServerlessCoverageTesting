package gui.view;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class ViewHelper {
    static void addToGridInHBox(GridPane grid, HBox box, Node...buttons) {
        for(Node node : buttons){
            HBox.setMargin(node, new Insets(10, 10, 10, 10));
            box.getChildren().add(node);
        }
        grid.add(box, 1, grid.getRowCount(), 5, 1);
    }
}
