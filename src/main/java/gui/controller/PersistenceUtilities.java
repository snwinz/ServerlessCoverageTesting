package gui.controller;

import com.google.gson.GsonBuilder;
import gui.controller.dto.ArrowInputData;
import gui.controller.dto.NodeInputData;
import gui.model.Graph;
import shared.model.NodeType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

public class PersistenceUtilities {
    public static void saveGraph(Graph model, String absolutePath) {
        var gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation().setPrettyPrinting()
                .create();

        String json = gson.toJson(model);
        var destination = Path.of(absolutePath);
        try {
            Files.writeString(destination, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.printf("Could not write the following to %s:%n%s ", absolutePath, json);
        }
    }

    public static Optional<Graph> loadGraph(String absolutePath, Graph model) {
        var gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        var source = Path.of(absolutePath);
        try {
            var json = Files.readString(source);
            Graph modelLoaded = gson.fromJson(json, Graph.class);
            var arrows = modelLoaded.getArrows();
            var nodes = modelLoaded.getNodes();
            for (var node : nodes) {
                var inputNode = new NodeInputData();
                inputNode.setNodeType(node.getType());
                inputNode.setName(node.getNameOfNode());
                inputNode.setSourceData(node.getSourceList());
                inputNode.setX(node.getX());
                inputNode.setY(node.getY());
                inputNode.setId(node.getIdentifier());
                if (inputNode.getNodeType().equals(NodeType.FUNCTION)) {
                    inputNode.setInputFormats(node.getInputFormats());
                }
                model.addNode(inputNode);
            }
            for (var arrow : arrows) {
                var inputArrow = new ArrowInputData();

                inputArrow.setOriginalEndOffsetPositionX(arrow.getOriginalEndOffsetPositionX());
                inputArrow.setOriginalEndOffsetPositionY(arrow.getOriginalEndOffsetPositionY());
                inputArrow.setOriginalStartOffsetPositionX(arrow.getOriginalStartOffsetPositionX());
                inputArrow.setOriginalStartOffsetPositionY(arrow.getOriginalStartOffsetPositionY());
                inputArrow.setAccessMode(arrow.getAccessMode());
                inputArrow.setId(arrow.getIdentifier());
                var pred = model.getNode(arrow.getPredecessor());
                var suc = model.getNode(arrow.getSuccessor());
                inputArrow.setPredecessor(pred.orElseThrow(() -> {
                    throw new IllegalStateException("pred not available");
                }).getIdentifier());
                inputArrow.setSuccessor(suc.orElseThrow(() -> {
                    throw new IllegalStateException("suc not available");
                }).getIdentifier());
                model.addArrow(inputArrow);
            }


        } catch (IOException e) {
            System.err.printf("Could not read file '%s'", absolutePath);
        }
        return Optional.ofNullable(model);
    }


}
