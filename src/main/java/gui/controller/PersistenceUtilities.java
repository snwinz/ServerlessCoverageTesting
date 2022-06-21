package gui.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import gui.controller.dto.ArrowInputData;
import gui.controller.dto.NodeInputData;
import gui.model.Graph;
import shared.model.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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
                var pre = model.getNode(arrow.getPredecessor());
                var suc = model.getNode(arrow.getSuccessor());
                inputArrow.setPredecessor(pre.orElseThrow(() -> {
                    throw new IllegalStateException("pre not available");
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


    public static List<Testcase> loadTCs(String absolutePath) {
        List<Testcase> testcasesRead = new ArrayList<>();
        var source = Path.of(absolutePath);

        try {
            var testcaseOneLine = Files.readString(source);
            if (absolutePath.endsWith("json")) {
                Type tcListType = new TypeToken<LinkedList<Testcase>>() {
                }.getType();
                var gson = new GsonBuilder().setPrettyPrinting().create();
                testcasesRead = gson.fromJson(testcaseOneLine, tcListType);
            } else {

                var testcases = testcaseOneLine.split("##Target ");
                for (var testcaseLine : testcases) {
                    var lines = testcaseLine.split("\n");
                    if (lines.length > 1) {
                        var target = getTarget(lines[0]);
                        var logs = getLogs(lines[0]);
                        List<Function> functions = new LinkedList<>();
                        for (int i = 1; i < lines.length; i++) {
                            var function = getFunction(lines[i]);
                            functions.add(function);
                        }
                        Testcase testcase = new Testcase(functions, logs, target);
                        testcasesRead.add(testcase);
                    }
                }
            }

        } catch (IOException e) {
            System.err.printf("Could not read file '%s'", absolutePath);
        }
        return testcasesRead;
    }

    private static Function getFunction(String line) {
        var functionInformation = line.split(" ", 2);
        return new Function(functionInformation[0], functionInformation[1]);
    }

    private static List<String> getLogs(String line) {
        var logs = line.split(" with logs ")[1];
        return List.of(logs.split(";"));
    }

    private static String getTarget(String line) {
        return line.split(" with logs ")[0];
    }

    public static void saveTestSuite(List<Testcase> testSuite, String absolutePath) {
        var gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(testSuite);
        var destination = Path.of(absolutePath);
        try {
            Files.writeString(destination, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.printf("Could not write the following to %s:%n%s ", absolutePath, json);
        }
    }

    public static List<Mutant> loadMutants(Path path) throws IOException {
        List<String> allMutantsList = Files.readAllLines(path);
        String allMutants = String.join("\n", allMutantsList);
        Gson gson = new Gson();
        Type mutationListType = new TypeToken<ArrayList<Mutant>>() {
        }.getType();
        return gson.fromJson(allMutants, mutationListType);

    }

    public static void saveMutationResult(MutationResult mutationResult, Path target) {
        if (!Files.isDirectory(target)) {
            System.err.println(target.toAbsolutePath() + " is not a directory");
        }

        var gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(mutationResult);
        String status = mutationResult.isKilled() ? "killed" : "survived";
        Path file = Path.of(status + "_" + mutationResult.getTestSuiteName() + "_" + mutationResult.getMutantNumber() + ".txt");
        var destination = target.resolve(file);
        try {
            Files.writeString(destination, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.printf("Could not write the following to %s:%n%s ", destination, json);
        }
    }
}
