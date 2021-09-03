package logic.model;

import logic.testcasegenerator.coveragetargets.CoverageTarget;

import java.util.ArrayList;
import java.util.List;

public class TestSuite {

    final List<CoverageTarget> testTargets = new ArrayList<>();

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();


        int numberOfTestAspects = 0;
        for (var testAspect : testTargets) {
            var testcases = testAspect.getTestcases();
            if (testcases == null ||testcases.isEmpty()) {
                result.append(String.format("No testcases were created for %s", testAspect));
                continue;
            }
            result.append(String.format("#Testcase #%d:%n", ++numberOfTestAspects));
            result.append(String.format("#The following aspect should be covered: %s%n",testAspect.getAspectTarget()));
            result.append(String.format("#target of tc: %s%n", testcases.get(0).getTarget()));
            result.append(String.format("%s%n", testcases.get(0).getCommandsForTestcase()));
            if (testcases.size() > 1) {
                for (int j = 1; j < testcases.size(); j++) {
                    result.append(String.format("#Alternative test case:%n"));
                    result.append(String.format("#target of tc: %s%n", testcases.get(j).getTarget()));
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
}
