package gui.model;

import com.google.gson.annotations.Expose;
import logic.model.FunctionInputFormat;
import shared.model.NodeType;

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
    private static long idCounter = 0;

    public NodeModel() {
        identifier = idCounter;
        idCounter++;
    }


    public List<SourceCodeLine> getSourceList() {
        return sourceList;
    }

    public void setSourceList(List<SourceCodeLine> sourceList) {
        for (var entry : sourceList) {
            boolean defNotSet = (entry.getDefContainer() == null || entry.getDefContainer().isBlank());
            boolean defRelationsNotSet = entry.getRelationsInfluencedByDef() == null || entry.getRelationsInfluencedByDef().isEmpty();

            boolean useNotSet = entry.getUse() == null || entry.getUse().isBlank();
            boolean useRelationsNotSet = entry.getRelationsInfluencingUse() == null || entry.getRelationsInfluencingUse().isEmpty();

            if (defNotSet || defRelationsNotSet) {
                entry.setDefContainer(null);
                entry.setRelationsInfluencedByDef(null);
            }
            if (useNotSet || useRelationsNotSet) {
                entry.setUse(null);
                entry.setRelationsInfluencingUse(null);
            }
            boolean nodesNotSet = entry.getNodesCoveredByStatement() == null || entry.getNodesCoveredByStatement().isEmpty();
            if (nodesNotSet) {
                entry.setNodesCoveredByStatement(null);
            }
            boolean relationsNotSet = entry.getRelationsCoveredByStatement() == null || entry.getRelationsCoveredByStatement().isEmpty();
            if (relationsNotSet) {
                entry.setRelationsCoveredByStatement(null);
            }
        }
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

    public long getIdentifier() {
        return identifier;
    }


    public void setIdentifier(long identifier) {
        if (identifier + 1 >= idCounter) {
            idCounter = identifier + 1;
        } else {
            idCounter--;
        }
        this.identifier = identifier;
    }

    public static void resetCounter() {
        idCounter = 0;
    }

    public void setInputFormats(FunctionInputFormat inputFormats) {
        this.inputFormats = inputFormats;
    }

    public FunctionInputFormat getInputFormats() {
        return inputFormats;
    }

    public void removeRelations(long arrowID) {
        if (sourceList != null) {
            sourceList.forEach(n -> n.removeRelationsInContextWithDefOrUseOrStatement(arrowID));
        }
    }
}
