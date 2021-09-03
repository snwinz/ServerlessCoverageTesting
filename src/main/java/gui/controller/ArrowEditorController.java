package gui.controller;

import gui.controller.dto.ArrowInputData;
import gui.model.Graph;
import gui.view.ArrowEditorView;
import gui.view.graphcomponents.DraggableArrow;

public class ArrowEditorController {
    private final Graph model;
    private ArrowEditorView view;

    public ArrowEditorController(Graph model) {
        this.model = model;
    }

    public void setup(DraggableArrow draggableArrow) {
        this.view = new ArrowEditorView(this, draggableArrow, model);
        view.showAndWait();
    }

    public void updateArrowToGraph(ArrowInputData infos) {
        model.updateArrow(infos);
        if (view != null) {
            view.close();
        }
    }

    public void closeWindow() {
        view.close();
    }
}
