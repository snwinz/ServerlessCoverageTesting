package gui.view;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class ViewHelper {
    static HBox addToGridInHBox(GridPane grid, Node...buttons) {
        HBox box = new HBox();
        for(Node node : buttons){
            HBox.setMargin(node, new Insets(10, 10, 10, 10));
            box.getChildren().add(node);
        }
        grid.add(box, 1, grid.getRowCount(), 8, 1);
        return box;
    }
}
