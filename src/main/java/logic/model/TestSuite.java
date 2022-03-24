package logic.model;

import logic.testcasegenerator.coveragetargets.CoverageTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TestSuite {

    final List<CoverageTarget> testTargets = new ArrayList<>();

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();


        int numberOfCoverageTargets = 0;
        for (var testTarget : testTargets) {
            var testcases = testTarget.getTestcases();
            if (testcases == null || testcases.isEmpty()) {
                result.append(String.format("No testcases were created for %s%n%n", testTarget));
                continue;
            }
            result.append(String.format("#Testcase #%d:%n", ++numberOfCoverageTargets));
            result.append(String.format("#The following test target should be covered: %s%n", testTarget.getCoverageTargetDescription()));
            result.append(String.format("#target of tc: %s%n", testcases.get(0).getTarget()));
            result.append(String.format("#Nodes to be checked afterwards: %s%n", testcases.get(0).getNodesForOracle().stream().map(node -> node.getIdentifier() + ":" + node.getNameOfNode()).collect(Collectors.joining(";"))));
            result.append(String.format("#Nodes holding state which could influence test case: %s%n", testcases.get(0).getNodesHoldingState().stream().map(node -> node.getIdentifier() + ":" + node.getNameOfNode()).collect(Collectors.joining(";"))));
            result.append(String.format("%s%n", testcases.get(0).getCommandsForTestcase()));

            if (testcases.size() > 1) {
                for (int j = 1; j < testcases.size(); j++) {
                    result.append(String.format("#Alternative test case:%n"));
                    result.append(String.format("#target of tc: %s%n", testcases.get(j).getTarget()));
                    result.append(String.format("#Nodes to be checked afterwards: %s%n", testcases.get(j).getNodesForOracle().stream().map(node -> node.getIdentifier() + ":" + node.getNameOfNode()).collect(Collectors.joining(";"))));
                    result.append(String.format("#Nodes holding state which could influence test case: %s%n", testcases.get(j).getNodesHoldingState().stream().map(node -> node.getIdentifier() + ":" + node.getNameOfNode()).collect(Collectors.joining(";"))));
                    result.append(String.format("%s%n", testcases.get(j).getCommandsForTestcase()));
                }
            }
            result.append("\n\n");
        }
        return result.toString();
    }

    public List<CoverageTarget> getTestTargets() {
        return testTargets;
    }

    public void add(List<? extends CoverageTarget> coverageTarget) {
        testTargets.addAll(coverageTarget);
    }

    public void calculateOracleNodes(Graph graph) {
        for (var target : testTargets) {
            for (var testcase : target.getTestcases()) {
                testcase.calculateNodesForOracle(graph);
            }
        }
    }

    public void calculateStateNodes(Graph graph) {
        for (var target : testTargets) {
            for (var testcase : target.getTestcases()) {
                testcase.calculateNodesHoldingState(graph);
            }
        }
    }


    public String getCoverageData() {
        StringBuilder result = new StringBuilder();
        result.append(String.format("%s\t%s\t%s\t%s\t%s%n", "Coverage Target", "Specific Target", "Test data for specific target", "Test data for general target", "number of runs for coverage"));
        for (var target : this.testTargets) {

            var testcases = target.getTestcases();
            result.append(String.format("Target: %s%n", target));
            for (var testcase : testcases) {
                var testData = testcase.getTestData();
                String specificTestDataText = "";
                String generalTestDataText = "";
                if (testData != null) {
                    specificTestDataText = testData.getDataAsText();
                } else if (testcase.getTestTargetData() != null) {
                    var generalTCData = testcase.getTestTargetData();
                    generalTestDataText = generalTCData.getDataAsText();
                }
                result.append(String.format("\t%s\t%s\t%s\t%d%n", testcase.getTarget(), specificTestDataText, generalTestDataText, testcase.getNumberOfRuns()));
            }
        }
        return result.toString();
    }

    public String getTCsWithInput() {
        StringBuilder result = new StringBuilder();
        for (var target : this.testTargets) {

            var testcases = target.getTestcases();

            boolean isTargetCovered = false;
            for (var testcase : testcases) {
                if (testcase.isCovered()) {
                    isTargetCovered = true;
                    var testData = testcase.getTestData();
                    String specificTestDataText = "";
                    if (testData != null) {
                        specificTestDataText = testData.getExecutableDataAsText();
                    }
                    result.append(String.format("##Target %s%n", target.getCoverageTargetDescription())) ;
                    result.append(specificTestDataText);
                }
            }
            if (!isTargetCovered) {
                result.append(String.format("##Target %s not covered%n", target.getCoverageTargetDescription()));
            }
        }


        return result.toString();


    }
}
