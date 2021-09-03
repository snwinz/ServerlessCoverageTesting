package gui.controller;

import gui.controller.dto.ArrowInputData;
import gui.model.Graph;
import gui.view.ArrowCreatorView;

public class ArrowCreatorController {

    private final Graph model;
    private ArrowCreatorView view;

    public ArrowCreatorController(Graph model) {
        this.model = model;
    }

    public void setup() {
        this.view = new ArrowCreatorView(this, model);
        view.showAndWait();
    }


    public void addArrowToGraph(ArrowInputData arrowInfos) {
        model.addArrow(arrowInfos);
        if (view != null) {
            view.close();
        }
    }



    public void closeWindow() {
        view.close();
    }
}
