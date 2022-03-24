package gui.model;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import gui.controller.dto.ArrowInputData;
import gui.controller.dto.NodeInputData;
import shared.model.NodeType;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.stream.Collectors;

public class Graph {

    @Expose
    private final List<Arrow> arrows;
    @Expose
    private final List<NodeModel> nodes;


    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);


    public Graph() {
        this.arrows = new ArrayList<>();
        this.nodes = new ArrayList<>();

    }

    public Graph(List<Arrow> arrows, List<NodeModel> nodes) {
        this.arrows = arrows;
        this.nodes = nodes;
    }

    public void addArrow(ArrowInputData arrowInfos) {
        var predecessor = findNodeByID(arrowInfos.getPredecessor());
        var successor = findNodeByID(arrowInfos.getSuccessor());

        if (predecessor.isPresent() && successor.isPresent()) {
            var arrow = new Arrow();
            arrow.setOriginalEndOffsetPositionX(arrowInfos.getOriginalEndOffsetPositionX());
            arrow.setOriginalEndOffsetPositionY(arrowInfos.getOriginalEndOffsetPositionY());
            arrow.setOriginalStartOffsetPositionX(arrowInfos.getOriginalStartOffsetPositionX());
            arrow.setOriginalStartOffsetPositionY(arrowInfos.getOriginalStartOffsetPositionY());
            arrow.setPredecessor(predecessor.get().getIdentifier());
            arrow.setSuccessor(successor.get().getIdentifier());
            arrow.setAccessMode(arrowInfos.getAccessMode());
            if (arrowInfos.getId() != -1) {
                arrow.setIdentifier(arrowInfos.getId());
            }
            arrows.add(arrow);
            this.pcs.firePropertyChange("graphUpdated", null, this);
        } else {
            System.err.println("Nodes for arrow are not available");
        }
    }

    public void updateArrow(ArrowInputData infos) {
        Optional<Arrow> arrow = findArrowByID(infos.getId());
        arrow.ifPresent(a -> {
            arrows.remove(a);
            addArrow(infos);
        });
        this.pcs.firePropertyChange("graphUpdated", null, this);
    }

    public void addNode(NodeInputData nodeInfo) {
        NodeModel nodeCreated = copyNode(nodeInfo);
        boolean isNodeAlreadyAdded = nodes.stream().anyMatch(n -> n.getIdentifier() == nodeInfo.getId());
        if (!isNodeAlreadyAdded) {
            nodes.add(nodeCreated);
        }
        this.pcs.firePropertyChange("graphUpdated", null, this);
    }

    public void updateNode(NodeInputData nodeInfos) {
        var node = nodes.stream().filter(n -> n.getIdentifier() == nodeInfos.getId()).findFirst();
        node.ifPresent(
                n -> {
                    n.setX(nodeInfos.getX());
                    n.setY(nodeInfos.getY());
                    n.setType(nodeInfos.getNodeType());
                    n.setSourceList(nodeInfos.getSourceList());
                    n.setNameOfNode(nodeInfos.getName());
                    n.setInputFormats(nodeInfos.getInputFormats());
                    pcs.firePropertyChange("nodeUpdated", null, this);
                }
        );

    }

    private NodeModel copyNode(NodeInputData nodeInfo) {
        NodeModel node = new NodeModel();
        if (nodeInfo.getId() != -1) {
            node.setIdentifier(nodeInfo.getId());
        }
        node.setNameOfNode(nodeInfo.getName());
        node.setType(nodeInfo.getNodeType());
        if (node.getType().equals(NodeType.FUNCTION)) {
            node.setSourceList(nodeInfo.getSourceList());
        }
        node.setX(nodeInfo.getX());
        node.setY(nodeInfo.getY());
        node.setInputFormats(nodeInfo.getInputFormats());
        return node;
    }

    public Optional<NodeModel> getNode(long id) {
        return nodes.stream().filter(node -> node.getIdentifier() == id).findAny();
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removeNode(long id) {
        var node = findNodeByID(id);
        if (node.isPresent()) {
            var toBeRemoved = arrows.stream().
                    filter(arrow -> Objects.equals(node.get().getIdentifier(), arrow.getPredecessor()) || Objects.equals(node.get().getIdentifier(), arrow.getSuccessor())).collect(Collectors.toList());
            arrows.removeAll(toBeRemoved);
            nodes.remove(node.get());
        }
        this.pcs.firePropertyChange("nodeRemoved", null, this);
    }

    private Optional<NodeModel> findNodeByID(long id) {
        return nodes.stream().filter(n -> n.getIdentifier() == id).findAny();
    }

    private Optional<Arrow> findArrowByID(long id) {
        return arrows.stream().filter(a -> a.getIdentifier() == id).findAny();
    }


    public void removeNodeArrow(long id) {
        var arrow = arrows.stream().filter(a -> a.getIdentifier() == id).findAny();
        if (arrow.isPresent()) {
            arrows.remove(arrow.get());

            for (var node : nodes) {
                node.removeRelations(id);
            }

            this.pcs.firePropertyChange("arrowRemoved", null, this);
        } else {
            System.err.printf("Arrow with id %s could not be removed%n", id);
        }
    }

    public void updateNodePosition(long identifier, double x, double y) {
        var node = findNodeByID(identifier);
        node.ifPresent(n -> {
            n.setX(x);
            n.setY(y);
        });
        this.pcs.firePropertyChange("nodeUpdated", null, this);
    }

    public void updateArrowPosition(long identifier, double originalStartOffsetPositionX, double originalStartOffsetPositionY, double originalEndOffsetPositionX, double originalEndOffsetPositionY) {
        var arrow = findArrowByID(identifier);
        arrow.ifPresent(a -> {
            a.setOriginalStartOffsetPositionX(originalStartOffsetPositionX);
            a.setOriginalStartOffsetPositionY(originalStartOffsetPositionY);
            a.setOriginalEndOffsetPositionX(originalEndOffsetPositionX);
            a.setOriginalEndOffsetPositionY(originalEndOffsetPositionY);
        });
        this.pcs.firePropertyChange("arrowUpdated", null, this);

    }

    public void informObservers() {
        this.pcs.firePropertyChange("modelUpdated", null, this);
    }

    public List<Arrow> getArrows() {
        return arrows;
    }

    public List<NodeModel> getNodes() {
        return nodes;
    }

    public void clearGraph() {
        arrows.clear();
        nodes.clear();
        NodeModel.resetCounter();
        Arrow.resetCounter();
    }

    public String getJSON() {
        var gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation().setPrettyPrinting()
                .create();

        return gson.toJson(this);
    }

    public List<Long> getNeighboursArrowsOfNode(long identifier) {
        return arrows.parallelStream().filter(n -> n.getPredecessor() == identifier || n.getSuccessor() == identifier).
                map(Arrow::getIdentifier).collect(Collectors.toCollection(LinkedList::new));
    }

    public List<Long> getNeighbourNodesOfNode(long identifier) {
        return arrows.parallelStream().
                filter(n -> n.getPredecessor() == identifier).map(Arrow::getSuccessor).collect(Collectors.toCollection(ArrayList::new));
    }

    public List<Long> getNodeIDs() {
        return nodes.parallelStream().map(NodeModel::getIdentifier).collect(Collectors.toCollection(ArrayList::new));
    }


    public List<Long> getArrowIDs() {
        return arrows.stream().map(Arrow::getIdentifier).collect(Collectors.toCollection(ArrayList::new));
    }
}
