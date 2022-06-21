package gui.controller.dto;

import logic.model.FunctionInputFormat;
import shared.model.NodeType;
import gui.model.SourceCodeLine;

import java.util.List;

public class NodeInputData {
    private double x;
    private double y;
    private String name;
    private long id = -1;

    private NodeType type;

    private List<SourceCodeLine> sourceList;
    private FunctionInputFormat inputFormats;

    public NodeInputData() {
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public void setName(String generalText) {
        this.name = generalText;
    }

    public void setNodeType(NodeType type) {
        this.type = type;
    }

    public NodeType getNodeType() {
        return type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setSourceData(List<SourceCodeLine> sourceList) {
        this.sourceList = sourceList;
    }

    public List<SourceCodeLine> getSourceList() {
        return sourceList;
    }

    public void setInputFormats(FunctionInputFormat inputFormats) {
        inputFormats.updateTypesOfGeneralInputs();
        this.inputFormats = inputFormats;
    }

    public FunctionInputFormat getInputFormats() {
        return inputFormats;
    }
}

