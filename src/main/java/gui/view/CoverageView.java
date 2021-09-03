package gui.view;

import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class CoverageView  extends Stage {
    private final TextArea textArea;

    public CoverageView(String title) {
        this.setTitle(title);
        textArea = new TextArea();
        Scene scene = new Scene(textArea);
        this.setScene(scene);
    }

    public CoverageView(String title, String text) {
        this.setTitle(title);
        textArea = new TextArea();
        Scene scene = new Scene(textArea);
        this.setScene(scene);
        textArea.setText(text);
    }

    public void setText(String text) {
        textArea.setText(text);
    }

}
