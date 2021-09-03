package logic.model;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class NodeModel {

    @Expose
    private long identifier;
    @Expose
    private List<SourceCodeLine> sourceList;
    @Expose
    private String nameOfNode = "";
    @Expose
    private NodeType type = NodeType.STANDARD_NODE;
    @Expose
    private double x;
    @Expose
    private double y;
    @Expose
    private FunctionInputFormat inputFormats;


    private final List<ArrowModel> incomingArrows = new ArrayList<>();
    private final List<ArrowModel> outgoingArrows = new ArrayList<>();

    public long getIdentifier() {
        return identifier;
    }

    public void setIdentifier(long identifier) {
        this.identifier = identifier;
    }

    public List<SourceCodeLine> getSourceList() {
        return sourceList;
    }

    public void setSourceList(List<SourceCodeLine> sourceList) {
        this.sourceList = sourceList;
    }

    public String getNameOfNode() {
        return nameOfNode;
    }

    public void setNameOfNode(String nameOfNode) {
        this.nameOfNode = nameOfNode;
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
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

    public FunctionInputFormat getInputFormats() {
        return inputFormats;
    }

    public void setInputFormats(FunctionInputFormat inputFormats) {
        this.inputFormats = inputFormats;
    }

    public List<ArrowModel> getIncomingArrows() {
        return incomingArrows;
    }

    public void addIncomingArrow(ArrowModel incomingArrow) {
        incomingArrows.add(incomingArrow);
    }

    public List<ArrowModel> getOutgoingArrows() {
        return outgoingArrows;
    }

    public void addOutgoingArrow(ArrowModel outgoingArrow) {
        outgoingArrows.add(outgoingArrow);
    }

    @Override
    public String toString() {
        return String.format(" %s %s (id %d)",
                this.getType(), this.getNameOfNode(), this.getIdentifier());
    }
}
