package gui.model.modelcreation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gui.controller.dto.ArrowInputData;
import gui.controller.dto.NodeInputData;
import gui.model.Graph;
import shared.model.NodeType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Creator {


    public static Graph createGraph(Path pathOfResourceFile) {

        return createBasicModel(pathOfResourceFile);
    }


    private static Graph createBasicModel(Path path) {
        Graph graph = new Graph();
        if (Files.exists(path)) {
            try {

                byte[] jsonData = Files.readAllBytes(path);

                // create ObjectMapper instance
                ObjectMapper objectMapper = new ObjectMapper();

                // read JSON like DOM Parser
                JsonNode rootNode = objectMapper.readTree(jsonData);
                JsonNode descriptionNode = rootNode.path("Description");
                System.out.println("Description = " + descriptionNode.toString());

                JsonNode resourcesNode = rootNode.path("Resources");
                Iterator<Map.Entry<String, JsonNode>> resources = resourcesNode.fields();

                // Create nodes first
                while (resources.hasNext()) {
                    Map.Entry<String, JsonNode> resource = resources.next();
                    String nameOfNode = resource.getKey();
                    JsonNode resourceNode = resource.getValue();
                    JsonNode type = resourceNode.path("Type");
                    if (isServerlessFunction(type)) {
                        NodeInputData lambda = new NodeInputData();
                        lambda.setName(nameOfNode);
                        lambda.setSourceData(new ArrayList<>());
                        lambda.setNodeType(NodeType.FUNCTION);
                        graph.addNode(lambda);
                    } else if (isS3Instance(type)) {
                        NodeInputData s3 = new NodeInputData();
                        s3.setName(nameOfNode);
                        s3.setSourceData(new ArrayList<>());
                        s3.setNodeType(NodeType.DATA_STORAGE);
                        graph.addNode(s3);
                    } else if (isDynamoDB(type)) {
                        NodeInputData dynamo = new NodeInputData();
                        dynamo.setName(nameOfNode);
                        dynamo.setSourceData(new ArrayList<>());
                        dynamo.setNodeType(NodeType.DATA_STORAGE);
                        graph.addNode(dynamo);
                    }
                }

                // Check for relations (e.g. triggers) saved in file)
                resources = resourcesNode.fields();
                while (resources.hasNext()) {
                    Map.Entry<String, JsonNode> resource = resources.next();
                    String nameOfNode = resource.getKey();
                    JsonNode resourceNode = resource.getValue();
                    JsonNode type = resourceNode.path("Type");
                    if (isServerlessFunction(type)) {
                        JsonNode properties = resourceNode.path("Properties");
                        if (properties.has("Events")) {
                            JsonNode eventNode = properties.path("Events");
                            Iterator<JsonNode> eventIterator = eventNode.elements();
                            while (eventIterator.hasNext()) {
                                JsonNode event = eventIterator.next();
                                JsonNode typeNode = event.path("Type");
                                if (typeNode.asText().contains("S3")) {
                                    JsonNode propertiesOfEvent = event.path("Properties");
                                    JsonNode bucketOfEvent = propertiesOfEvent.path("Bucket");
                                    JsonNode triggerDB = bucketOfEvent.path("Ref");
                                    String nameOfTriggerDB = triggerDB.asText();
                                    var dbNode = graph.getNodes().stream().filter(node -> nameOfTriggerDB.equals(node.getNameOfNode())).findAny();
                                    var triggeredNode = graph.getNodes().stream().filter(node -> nameOfNode.equals(node.getNameOfNode())).findAny();
                                    if (dbNode.isPresent() && triggeredNode.isPresent()) {

                                    }
                                    ArrowInputData arrow = new ArrowInputData();
                                    arrow.setPredecessor(dbNode.get().getIdentifier());
                                    arrow.setSuccessor(triggeredNode.get().getIdentifier());
                                    graph.addArrow(arrow);
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("File could not be read");
                e.printStackTrace();
            }
        }
        return graph;
    }

    private static boolean isServerlessFunction(JsonNode type) {
        return type.toString().contains("AWS::Serverless::Function");
    }

    private static boolean isS3Instance(JsonNode type) {
        return type.toString().contains("AWS::S3::Bucket");
    }

    private static boolean isDynamoDB(JsonNode type) {
        return type.toString().contains("AWS::DynamoDB::Table");
    }





    private static String getHandlerName(List<String> allLines) {
        for (String line : allLines) {
            if (line.contains("public class")) {
                return line.split(" ")[2];
            }
        }
        return "";
    }

}
