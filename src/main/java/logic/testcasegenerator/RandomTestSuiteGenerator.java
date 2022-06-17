package logic.testcasegenerator;

import logic.model.LogicGraph;
import logic.model.NodeModel;
import shared.model.Function;
import shared.model.Testcase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomTestSuiteGenerator {
    private final LogicGraph logicGraph;
    private final Random random = new Random();

    public RandomTestSuiteGenerator(LogicGraph logicGraph) {
        this.logicGraph = logicGraph;
        logicGraph.addRelationsToElements();
    }

    public List<Testcase> generateTestcases(List<Testcase> testSuite) {
        List<Testcase> randomTCs = new ArrayList<>();
        for (var tc : testSuite) {
            var randomTC = generateSimilarTestcase(tc);
            randomTCs.add(randomTC);
        }
        return randomTCs;
    }

    private Testcase generateSimilarTestcase(Testcase testcase) {
        List<Function> randomFunctions = new ArrayList<>();

        var allFunctionNodes = GraphHelper.getAllFunctions(logicGraph);

        for (int i = 0; i < testcase.functions().size(); i++) {
            NodeModel node = getRandomFunction(allFunctionNodes);
            var functionName = node.getNameOfNode();
            var inputFormat = node.getInputFormats();
            var inputString = inputFormat.getJSONWithNewContent();
            var randomFunction = new Function(functionName, inputString);
            randomFunctions.add(randomFunction);
        }
        var randomTestcase = new Testcase(randomFunctions, List.of(""), "random Testcase");
        randomTestcase.setManualCreated(testcase.isManualCreated());
        return randomTestcase;
    }

    private NodeModel getRandomFunction(List<NodeModel> allFunctionNodes) {
        int functionNumber = random.nextInt(0, allFunctionNodes.size());
        return allFunctionNodes.get(functionNumber);
    }

}
