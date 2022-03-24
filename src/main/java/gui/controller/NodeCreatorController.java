package gui.controller;

import gui.controller.dto.NodeInputData;
import gui.model.Graph;
import gui.view.NodeCreatorView;
import gui.view.UtilityConverter;

import java.io.File;

public class NodeCreatorController {

    private final Graph model;
    private NodeCreatorView view = null;

    public NodeCreatorController(Graph model) {
        this.model = model;
    }


    public void setup(double x, double y) {
        view = new NodeCreatorView(this, x, y);
        view.setup();
    }

    public void addNodeToGraph(NodeInputData nodeInfo) {
        model.addNode(nodeInfo);
        if (view != null) {
            view.close();
        }
    }


    public void close() {
        view.close();
    }

    public void setSourceFile(File file) {
        var tableItems = UtilityConverter.getTableItems(file);
        view.setDataForTable(tableItems);
    }
}
