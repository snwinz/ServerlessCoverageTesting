package logic.dynamicdatageneration;

import gui.view.wrapper.ExecutionSettings;
import logic.dynamicdatageneration.testrun.TestData;
import logic.model.Testcase;
import logic.testcasegenerator.coveragetargets.CoverageTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class DynamicTestCaseGenerator {


    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);


    public void generateTestcases(List<Testcase> testcasesToBeCreated, ExecutionSettings executionSettings) {
        TestcaseSimulator testcaseSimulator = new TestcaseSimulator(executionSettings.getNumberOfRuns(), executionSettings.getRegion());
        testcaseSimulator.setResetFunction(executionSettings.getResetFunctionName());
        for (var testcase : testcasesToBeCreated) {
            var res = testcaseSimulator.simulateTestcase(testcase, executionSettings);
            if (res.isPresent()) {
                var testData = res.get();
                System.out.println("Valid test case: ");
                System.out.println(testData);
            }
        }


    }

    public Optional<TestData> generateTestcase(Testcase testcase, ExecutionSettings executionSettings) {
        TestcaseSimulator testcaseSimulator = new TestcaseSimulator(executionSettings.getNumberOfRuns(), executionSettings.getRegion());
        testcaseSimulator.setResetFunction(executionSettings.getResetFunctionName());

        var res = testcaseSimulator.simulateTestcase(testcase, executionSettings);
        if (res.isPresent()) {
            var testData = res.get();
            System.out.println("Valid test case: ");
            System.out.println(testData);
        }

        return res;
    }


    public void generateTestcasesForTarget(List<CoverageTarget> testTargets, ExecutionSettings executionSettings) {
        TestcaseSimulator testcaseSimulator = new TestcaseSimulator(1, executionSettings.getRegion());
        testcaseSimulator.setResetFunction(executionSettings.getResetFunctionName());
        int runs = executionSettings.getNumberOfRuns();
        List<ExecutionSettings> settings = getSettings(executionSettings);
        for (int i = 0; i < runs; i++) {
            for (var setting : settings) {
                var targetsNotCovered = testTargets.stream().filter(Predicate.not(CoverageTarget::isCovered)).toList();
                for (var target : targetsNotCovered) {
                    for (var testcase : target.getTestcases()) {
                        var res = testcaseSimulator.simulateTestcase(testcase, setting);
                        if (res.isPresent()) {
                            var testData = res.get();
                            System.out.println("Valid test case: ");
                            System.out.println(testData);
                            target.specificTargetCoveredProperty().set(true);
                            break;
                        }
                    }
                }
            }
        }
        System.out.println("Target generation finished");
    }

    private List<ExecutionSettings> getSettings(ExecutionSettings executionSettings) {
        List<ExecutionSettings> settings = new ArrayList<>();

        ExecutionSettings allValuesSame = executionSettings.getSimilarSettings();
        allValuesSame.setProbSameValueEverywhere(1.0);
        settings.add(allValuesSame);

        ExecutionSettings similarInputAsValue = executionSettings.getSimilarSettings();
        similarInputAsValue.setProbSimilarInputAsValue(1.0);
        settings.add(similarInputAsValue);

        ExecutionSettings similarOutputAsValue = executionSettings.getSimilarSettings();
        similarOutputAsValue.setProbSimilarOutputAsValue(1.0);
        settings.add(similarOutputAsValue);

        ExecutionSettings randomInputAsValue = executionSettings.getSimilarSettings();
        randomInputAsValue.setProbRandomInputAsValue(1.0);
        settings.add(randomInputAsValue);

        ExecutionSettings randomOutAsValue = executionSettings.getSimilarSettings();
        randomOutAsValue.setProbRandomOutputAsValue(1.0);
        settings.add(randomOutAsValue);

        return settings;
    }
}


