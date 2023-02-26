package logic.dynamicdatageneration;

import gui.view.wrapper.ExecutionSettings;
import logic.dynamicdatageneration.testrun.FunctionWithInputData;
import logic.dynamicdatageneration.testrun.TestData;
import logic.executionplatforms.AWSInvoker;
import logic.executionplatforms.Executor;
import logic.model.Testcase;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static logic.testcasegenerator.coveragetargets.LogNameConfiguration.*;

public class TestcaseSimulator {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final Executor executor;
    private final int maximalNumberOfTries;
    private String resetFunctionName;

    public TestcaseSimulator(int retriesForNewData, String region) {
        this.maximalNumberOfTries = retriesForNewData;
        executor = new AWSInvoker(region);
    }


    public Optional<TestData> simulateTestcase(Testcase testcase, ExecutionSettings executionSettings) {


        testcase.setCovered(false);
        testcase.setSpecificTargetCovered(false);
        TestData testDataCreated = new TestData(testcase.getFunctions(), executionSettings);

        for (int actualRun = 1; actualRun <= maximalNumberOfTries; actualRun++) {
            try {
                var potentialAuth = resetApplication();
                executeFunctions(testcase, testDataCreated, actualRun, potentialAuth);
                var logList = executor.getAllNewLogs(0L);
                LOGGER.info("Logs for Call of run " + actualRun + ":\n" + String.join("\n", logList));
                List<String> specificTargetsToBeCovered = getAllTestTargets(testcase);
                boolean allGeneralTestTargetsCovered = areAllTestTargetsCovered(testDataCreated, logList, testcase.getLogsOfTarget());
                boolean allSpecificTargetsCovered = areAllTestTargetsCovered(testDataCreated, logList, specificTargetsToBeCovered);

                if (allSpecificTargetsCovered) {
                    testcase.addTextToWriteOutput("Successfully covered in run " + actualRun + ":\n" + testDataCreated + "\n");
                    testcase.setCovered(true);
                    testcase.setTestData(testDataCreated);
                    testcase.setSpecificTargetCovered(true);
                    testcase.setTestTargetData(testDataCreated);
                    testcase.setNumberOfRuns(actualRun);
                    return Optional.of(testDataCreated);
                } else if (allGeneralTestTargetsCovered) {
                    testcase.addTextToWriteOutput("Only target successfully covered in run " + actualRun + ":\n" + testDataCreated + "\n");
                    if (!testcase.isCovered()) {
                        testcase.setCovered(true);
                        testcase.setTestTargetData(testDataCreated);
                        testcase.setNumberOfRuns(actualRun);
                    }
                }
                /* quite generic and has to be more specific */
            } catch (Exception e) {
                String message = String.format("Error occurred in run %d: %s", actualRun, e.getMessage());
                System.err.println(message);
                LOGGER.warning(message);
            }

        }
        return Optional.empty();
    }

    public String resetApplication() {
        return executor.resetApplication(resetFunctionName);
    }

    private List<String> getAllTestTargets(Testcase testcase) {
        var testTargetsToBeCovered = testcase.getLogsToCover();
        if (testTargetsToBeCovered == null || testTargetsToBeCovered.isEmpty()) {
            testcase.writeToOutput("No test target to be covered was declared for this test case");
            throw new IllegalStateException("No test target to be covered was declared for this test case");
        }
        return testTargetsToBeCovered;
    }

    private boolean areAllTestTargetsCovered(TestData testDataCreated, List<String> logList, List<String> testTargetsToBeCovered) {
        boolean allTestTargetsCovered = true;
        for (var logTestTarget : testTargetsToBeCovered) {
            var logTargetStatements = logTestTarget.split(LOGDELIMITER + LOGDELIMITER);
            var isLogTestTargetCovered = logList.parallelStream().anyMatch(log -> checkLogForTarget(log, logTargetStatements, testDataCreated.getTestFunctions()));
            if (!isLogTestTargetCovered) {
                allTestTargetsCovered = false;
                break;
            }
        }
        return allTestTargetsCovered;
    }

    private void executeFunctions(Testcase testcase, TestData testData, int actualRun, String potentialAuth) {
        String runInfo = String.format("run #%d:%n", actualRun);
        testcase.writeToOutput(runInfo);
        testData.resetOutputData();
        testData.resetInputData();
        testData.resetAuthValues();
        testData.setGeneralAuthenticationValue(potentialAuth);
        testData.checkToUseSameValues();
        for (var function : testData.getTestFunctions()) {
            function.changeData(testData);
            String functionName = function.getName();
            String jsonData = function.getJSON();
            testData.addInputOfFunction(function);
            invokeFunction(testcase, testData, functionName, jsonData);
        }
    }

    private void invokeFunction(Testcase testcase, TestData testData, String functionName, String jsonData) {
        String invocation = String.format("invoke function '%s' with json '%s'", functionName, jsonData);
        LOGGER.info(invocation);
        testcase.addTextToWriteOutput(invocation);
        String result = executor.invokeFunction(functionName, jsonData, testData.getOutputValues(), testData.getAuthValues());
        testData.addResultToOutputAndAuth(result);
        String resultFormatted = String.format("result: %s", result);
        LOGGER.info(resultFormatted);
        testcase.addTextToWriteOutput(resultFormatted);
    }

    private void executeFunctionsWithOldData(Testcase testcase, TestData testData) {
        String runInfo = "Reexecution";
        testcase.writeToOutput(runInfo);
        for (var function : testData.getTestFunctions()) {
            String functionName = function.getName();
            String jsonData = function.getJSON();
            invokeFunction(testcase, testData, functionName, jsonData);
        }
    }


    private boolean checkLogForTarget(String log, String[] logTargetStatements, List<FunctionWithInputData> functions) {

        for (int i = 0; i < logTargetStatements.length; i++) {
            var logStatement = logTargetStatements[i];
            logStatement = logStatement.replace(FUNCTION_MARKER, "");

            if (logStatement.startsWith(USELOG_MARKER + DEFLOG_MARKER) && log.contains(USELOG_MARKER)) {
                logStatement = logStatement.replace(USELOG_MARKER, "");
            }

            var shortenedLogStatement = log.replace(logStatement, "");

            boolean partOfTargetWasCovered = shortenedLogStatement.length() < log.length();
            if (partOfTargetWasCovered) {
                log = shortenedLogStatement;
                if (functions.size() == logTargetStatements.length) {
                    functions.get(i).setPromisingData(true);
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public void executeSingleTestCase(Testcase testcase) {

        testcase.resetCoverageIndicator();
        var potentialAuthentication = resetApplication();

        TestData testDataCreated = testcase.getTestData() == null ? testcase.getTestTargetData() : testcase.getTestData();
        if (testDataCreated == null) {
            testcase.writeToOutput("no test data created yet");
            return;
        }
        testDataCreated.setGeneralAuthenticationValue(potentialAuthentication);
        executeFunctionsWithOldData(testcase, testDataCreated);
        var logList = executor.getAllNewLogs(0L);
        LOGGER.info("Logs for Call of run reexecution:\n" + String.join("\n", logList));
        executor.deleteOldLogs();

        List<String> specificTargetsToBeCovered = getAllTestTargets(testcase);
        boolean allGeneralTestTargetsCovered = areAllTestTargetsCovered(testDataCreated, logList, testcase.getLogsOfTarget());
        boolean allSpecificTargetsCovered = areAllTestTargetsCovered(testDataCreated, logList, specificTargetsToBeCovered);

        if (allSpecificTargetsCovered) {
            testcase.addTextToWriteOutput("Successfully covered in run  reexecution +\n" + testDataCreated + "\n");
            testcase.setCovered(true);
            testcase.setTestData(testDataCreated);
            testcase.setSpecificTargetCovered(true);
            testcase.setTestTargetData(testDataCreated);
        } else if (allGeneralTestTargetsCovered) {
            testcase.addTextToWriteOutput("Successfully covered in run reexecution:\n" + testDataCreated + "\n");
            testcase.setCovered(true);
            testcase.setTestTargetData(testDataCreated);
        }
    }

    public void setResetFunction(String resetFunctionName) {
        this.resetFunctionName = resetFunctionName;
    }


}
