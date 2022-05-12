package logic.model;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LogicGraph {

    @Expose
    private final List<ArrowModel> arrows;
    @Expose
    private final List<NodeModel> nodes;


    public LogicGraph(String graphJSON) {
        var gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        LogicGraph modelLoaded = gson.fromJson(graphJSON, LogicGraph.class);
        this.arrows = modelLoaded.getArrows();
        this.nodes = modelLoaded.getNodes();
        updateTypesOfInput();
    }

    private void updateTypesOfInput() {
        for (var node : nodes) {
            node.updateTypeForInputFormats();
        }
    }


    public List<ArrowModel> getArrows() {
        return new ArrayList<>(arrows);
    }

    public List<NodeModel> getNodes() {
        return nodes;
    }


    public void addRelationsToElements() {
        for (var arrow : arrows) {
            Optional<NodeModel> predecessorNode = findNodeByID(arrow.getPredecessor());
            if (predecessorNode.isEmpty()) {
                String errorMessage = String.format("Predecessor with id %d of arrow %d cannot be found", arrow.getPredecessor(), arrow.getIdentifier());
                throw new IllegalArgumentException(errorMessage);
            }
            Optional<NodeModel> successorNode = findNodeByID(arrow.getSuccessor());
            if (successorNode.isEmpty()) {
                String errorMessage = String.format("Successor with id %d of arrow %d cannot be found", arrow.getSuccessor(), arrow.getIdentifier());
                throw new IllegalArgumentException(errorMessage);
            }
            predecessorNode.ifPresent(node -> {
                arrow.setPredecessorNode(node);
                node.addOutgoingArrow(arrow);
            });
            successorNode.ifPresent(node -> {
                arrow.setSuccessorNode(node);
                node.addIncomingArrow(arrow);
            });
        }
    }

    public Optional<NodeModel> findNodeByID(long id) {
        return nodes.stream().filter(n -> n.getIdentifier() == id).findFirst();
    }

}