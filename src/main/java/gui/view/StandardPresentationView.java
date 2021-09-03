package gui.view;

import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class StandardPresentationView extends Stage{
    private final TextArea textArea;

    public StandardPresentationView(String title) {
        this.setTitle(title);
        textArea = new TextArea();
        Scene scene = new Scene(textArea);
        this.setScene(scene);
    }

    public StandardPresentationView(String title, String text) {
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
