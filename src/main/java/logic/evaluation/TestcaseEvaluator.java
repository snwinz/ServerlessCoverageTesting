package logic.evaluation;

import gui.view.wrapper.TestcaseWrapper;

import java.util.List;
import java.util.function.Predicate;

public class TestcaseEvaluator {
    private final List<TestcaseWrapper> testcases;

    public TestcaseEvaluator(List<TestcaseWrapper> testcases) {
        this.testcases = testcases;

    }

    public String getPassedData() {
        StringBuilder result = new StringBuilder();

        var tcPassed = testcases.stream().filter(isPassed()).toList();
        var tcFailed = testcases.stream().filter(isPassed().negate()).toList();
        result.append(String.format("TC passed: %d %nTc failed: %d", tcPassed.size(), tcFailed.size()));
        return result.toString();
    }

    private Predicate<TestcaseWrapper> isPassed() {
        return testcase ->
                testcase.getFunctionsWrapped().stream().allMatch(f -> f.passedProperty().get());
    }
}
