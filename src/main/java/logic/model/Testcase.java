package logic.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import logic.dynamicdatageneration.testrun.TestData;
import shared.model.AccessMode;
import shared.model.Function;
import shared.model.NodeType;

import java.util.*;

public class Testcase {
    private final List<ServerlessFunction> functionNames;
    private final String target;
    private final StringProperty testCaseOutput;
    private final BooleanProperty testCovered;
    private final BooleanProperty specificTargetCovered;
    private Set<NodeModel> nodesForOracle;
    private Set<NodeModel> nodesHoldingState;


    private static final String INVOCATION_COMMAND = "serverless invoke -f ";

    private final List<String> logsToCover;
    private List<String> logsOfTarget;
    private TestData testData;
    private TestData testTargetData;
    private int numberOfRuns;


    public Testcase(List<ServerlessFunction> functionNames, String target, List<String> logsToCover) {
        this.functionNames = new ArrayList<>(functionNames);
        this.target = target;
        this.logsToCover = logsToCover;
        testCaseOutput = new SimpleStringProperty();
        testCovered = new SimpleBooleanProperty();
        specificTargetCovered = new SimpleBooleanProperty();
    }

    public String getCommandsForTestcase() {
        StringBuilder result = new StringBuilder();
        for (ServerlessFunction functionName : functionNames) {
            result.append(INVOCATION_COMMAND).append(functionName.getName()).append("\n");
        }
        return result.toString();
    }

    public void setCovered(boolean covered) {
        testCovered.set(covered);
    }

    public boolean isCovered() {
        return testCovered.get();
    }

    public boolean isSpecificTargetCovered() {
        return specificTargetCovered.get();
    }

    public void setSpecificTargetCovered(boolean covered) {
        specificTargetCovered.set(covered);
    }

    public List<ServerlessFunction> getFunctions() {
        return functionNames;
    }

    public Set<NodeModel> getNodesForOracle() {
        if (nodesForOracle == null) {
            nodesForOracle = new HashSet<>();
        }
        return nodesForOracle;
    }

    public Set<NodeModel> getNodesHoldingState() {
        if (nodesHoldingState == null) {
            nodesHoldingState = new HashSet<>();
        }
        return nodesHoldingState;
    }

    public String getTarget() {
        return target;
    }

    public List<String> getLogsToCover() {
        return new ArrayList<>(logsToCover);
    }


    public void writeToOutput(String text) {
        testCaseOutput.set(text);
    }

    public void addTextToWriteOutput(String text) {
        text = testCaseOutput.get() + "\n" + text;
        testCaseOutput.set(text);
    }

    public void calculateNodesForOracle(LogicGraph logicGraph) {
        Set<NodeModel> nodesWalked = new HashSet<>();
        Set<NodeModel> nodesToCover = new HashSet<>();

        for (var function : functionNames) {
            var rootNode = logicGraph.findNodeByID(function.getId());
            if (rootNode.isPresent()) {
                nodesWalked.add(rootNode.get());
                if (nodesToCover.add(rootNode.get())) {
                    findCoveredNodes(rootNode.get(), nodesWalked, nodesToCover);
                }
            }
        }
        nodesForOracle = nodesToCover;
    }

    public void calculateNodesHoldingState(LogicGraph logicGraph) {

        Set<NodeModel> nodesWithState = new HashSet<>();

        Set<NodeModel> nodesWalked = new HashSet<>();
        for (var function : functionNames) {
            var rootNode = logicGraph.findNodeByID(function.getId());
            rootNode.ifPresent(nodeModel -> findStateNodesAfter(nodeModel, nodesWalked, nodesWithState));
        }
        nodesHoldingState = nodesWithState;
    }

    private void findStateNodesAfter(NodeModel rootNode, Set<NodeModel> nodesWalked, Set<NodeModel> nodesWithState) {
        if (nodesWalked.add(rootNode)) {
            for (var arrow : rootNode.getOutgoingArrows()) {
                var successorNode = arrow.getSuccessorNode();

                boolean readStateFromSuccessor = successorNode.getType() == NodeType.DATA_STORAGE && arrow.hasAccessMode(AccessMode.READ);
                if (readStateFromSuccessor) {
                    nodesWithState.add(successorNode);
                } else {
                    findStateNodesAfter(successorNode, nodesWalked, nodesWithState);
                }
            }
        }
    }


    private void findCoveredNodes(NodeModel rootNode, Set<NodeModel> nodesWalked, Set<NodeModel> nodesToCover) {
        for (var arrow : rootNode.getOutgoingArrows()) {
            var successorNode = arrow.getSuccessorNode();
            if (!nodesWalked.contains(successorNode)) {
                boolean noStateChangeInSuccessor = successorNode.getType() == NodeType.DATA_STORAGE && arrow.hasAccessMode(AccessMode.READ);
                if (!noStateChangeInSuccessor) {
                    nodesWalked.add(successorNode);
                    if (nodesToCover.add(successorNode)) {
                        findCoveredNodes(successorNode, nodesWalked, nodesToCover);
                    }
                }
            }
        }
    }

    public void setTestData(TestData testdata) {
        this.testData = testdata;
    }

    public void setTestTargetData(TestData testdata) {
        this.testTargetData = testdata;
    }

    public TestData getTestData() {
        return testData;
    }

    public TestData getTestTargetData() {
        return testTargetData;
    }

    public List<String> getLogsOfTarget() {
        return logsOfTarget;
    }

    public void setLogsOfTarget(List<String> logsOfTarget) {
        this.logsOfTarget = logsOfTarget;
    }

    public void resetCoverageIndicator() {
        testCovered.set(false);
        specificTargetCovered.set(false);

    }

    public String getTestCaseOutput() {
        return testCaseOutput.get();
    }

    public StringProperty testCaseOutputProperty() {
        return testCaseOutput;
    }

    public boolean isTestCovered() {
        return testCovered.get();
    }

    public BooleanProperty testCoveredProperty() {
        return testCovered;
    }

    public BooleanProperty specificTargetCoveredProperty() {
        return specificTargetCovered;
    }

    public String getInfos() {
        if (this.testData == null) {
            return "no test data created yet";
        }
        return testData.toString();
    }

    public void setNumberOfRuns(int numberOfRuns) {
        this.numberOfRuns = numberOfRuns;
    }

    public int getNumberOfRuns() {
        return numberOfRuns;
    }

    public shared.model.Testcase getSharedTestcaseCopy(String target, Set<String> authKeys) {
        var functionsForExecution = new LinkedList<Function>();
        var testData = this.getTestData();
        for (var function : testData.getTestFunctions()) {
            var functionName = function.getFunction().getName();
            var argument = function.getJSON();
            functionsForExecution.add(new Function(functionName, argument));
        }
        var logsToCoverForExecution = this.getLogsToCover();
        String targetForExecution = target;
        var result =
                new shared.model.Testcase(functionsForExecution, logsToCoverForExecution, targetForExecution);
        result.addAuthKeys(authKeys);
        return result;
    }
}