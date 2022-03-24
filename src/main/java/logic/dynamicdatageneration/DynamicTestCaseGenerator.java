package logic.dynamicdatageneration;

import gui.view.wrapper.Commands;
import logic.model.Testcase;

import java.util.List;
import java.util.logging.Logger;

public class DynamicTestCaseGenerator {


    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);


    public void generateTestcases(List<Testcase> testcasesToBeCreated, Commands commands) {
        TestcaseSimulator testcaseSimulator = new TestcaseSimulator(commands.getNumberOfRuns(), commands.getRegion());
        testcaseSimulator.setResetFunction(commands.getResetFunctionName());
        for (var testcase : testcasesToBeCreated) {
            var res = testcaseSimulator.simulateTestcase(testcase, commands);
            if (res.isPresent()) {
                var testData = res.get();
                System.out.println("Valid test case: ");
                System.out.println(testData);
            }
        }


    }
}


