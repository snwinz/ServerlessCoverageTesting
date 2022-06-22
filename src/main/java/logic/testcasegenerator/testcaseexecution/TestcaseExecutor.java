package logic.testcasegenerator.testcaseexecution;

import gui.view.wrapper.FunctionWrapper;
import gui.view.wrapper.TestcaseWrapper;
import logic.executionplatforms.AWSInvoker;
import logic.executionplatforms.KeyValueJsonGenerator;
import shared.model.Function;
import shared.model.Testcase;

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TestcaseExecutor {
    public static final String PREVIOUSOUTPUT_PREFIX = "##PREVIOUSOUTPUT__";
    public static final String PREVIOUSOUTPUT_SUFFIX = "__PREVIOUSOUTPUT##";
    public static final String SEPARATOR = "__";
    private final AWSInvoker executor;
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public TestcaseExecutor(String region) {
        this.executor = new AWSInvoker(region);
    }

    public void executeTC(TestcaseWrapper testcase) {
        var functions = testcase.getFunctionsWrapped();
        final Map<String, List<String>> outputValues = new HashMap<>();
        for (var function : functions) {
            var originalFunction = function.getFunction();
            String functionName = originalFunction.getName();

            String jsonData = originalFunction.getParameter();
            String invocation = String.format("invoke function '%s' with parameter '%s'", functionName, jsonData);
            LOGGER.info(invocation);

            String result = executor.invokeFunction(functionName, jsonData, outputValues);


            checkCorrectnessOfOutput(result, function, outputValues);


            addResultToOutputValues(result, outputValues);

            String resultFormatted = String.format("result: %s", result);

            LOGGER.info(resultFormatted);
            function.addTextToOutput(resultFormatted);
        }
        checkCorrectnessOfLogs(testcase);
    }


    public Optional<String> executeTC(Testcase testcase) {
        var functions = testcase.getFunctions();
        final Map<String, List<String>> outputValues = new HashMap<>();
        for (var function : functions) {
            String functionName = function.getName();
            String jsonData = function.getParameter();
            String invocation = String.format("invoke function '%s' with parameter '%s'", functionName, jsonData);
            LOGGER.info(invocation);

            String result = executor.invokeFunction(functionName, jsonData, outputValues);

            var partNotCovered = getExecutionResultNotCovered(result, function, outputValues);
            if(partNotCovered.isPresent()){
                return partNotCovered;
            }

            addResultToOutputValues(result, outputValues);

            String resultFormatted = String.format("result: %s", result);

            LOGGER.info(resultFormatted);
        }
        return getLogPartNotCovered(testcase);
    }

    private boolean notRemovedFromList(List<String> logsCompare, String part) {
        boolean removed = true;
        for (int i = 0; i < logsCompare.size(); i++) {
            var log = logsCompare.get(i);
            if (log.contains(part)) {
                int position = log.indexOf(part);
                log = log.substring(0, position) + log.substring(position + part.length());
                logsCompare.remove(i);
                logsCompare.add(i, log);
                removed = false;
                break;
            }
        }
        return removed;
    }

    private List<String> filterLogs(List<String> logs) {
        List<String> listFiltered = new LinkedList<>();
        for (var log : logs) {
            if (log.contains("INFO")) {
                String entry = log.substring(log.indexOf("\tINFO\t") + 6);
                entry = entry.substring(0, entry.length() - 1);
                listFiltered.add(entry);
            }
        }
        return listFiltered;
    }

    public void executeTCs(List<TestcaseWrapper> testcases, String resetFunction) {
        for (var testcase : testcases) {
            executor.deleteOldLogs();
            executor.callResetFunction(resetFunction);
            this.executeTC(testcase);
        }
    }


    private void checkCorrectnessOfOutput(String result, FunctionWrapper function, Map<String, List<String>> outputValues) {
        boolean passed = true;
        List<String> incorrectParts = new LinkedList<>();
        for (var part : function.getFunction().getExpectedOutputs()) {
            while (part.contains(PREVIOUSOUTPUT_PREFIX) && part.contains(PREVIOUSOUTPUT_SUFFIX)) {
                int startPositionMarker = part.indexOf(PREVIOUSOUTPUT_PREFIX);
                var splitPart = part.substring(startPositionMarker + PREVIOUSOUTPUT_PREFIX.length());
                int endPositionMarker = splitPart.indexOf(PREVIOUSOUTPUT_SUFFIX);
                if (endPositionMarker == -1) {
                    passed = false;
                    break;
                }
                splitPart = splitPart.substring(0, endPositionMarker);
                var valueArray = splitPart.split(Pattern.quote(SEPARATOR));
                var partsOfKey = Arrays.copyOfRange(valueArray, 0, valueArray.length - 1);
                String key = String.join(SEPARATOR, partsOfKey);
                int number;
                if (partsOfKey.length == 0 || !outputValues.containsKey(key)) {
                    passed = false;
                    break;
                }
                try {
                    number = Integer.parseInt(valueArray[valueArray.length - 1]);
                } catch (NumberFormatException e) {
                    passed = false;
                    break;
                }

                String partBeforeOutputValue = part.substring(0, startPositionMarker);
                var outputValue = outputValues.get(key).get(number);
                var potentialPartAfterOutputValue = part.substring(endPositionMarker + PREVIOUSOUTPUT_PREFIX.length());
                potentialPartAfterOutputValue = potentialPartAfterOutputValue.substring(potentialPartAfterOutputValue.indexOf(PREVIOUSOUTPUT_SUFFIX) + PREVIOUSOUTPUT_SUFFIX.length());
                part = partBeforeOutputValue + outputValue + potentialPartAfterOutputValue;
            }
            if (result.contains(part)) {
                result = result.substring(result.indexOf(part) + part.length());
            } else {
                incorrectParts.add(part);
                passed = false;
                break;
            }
        }
        function.passedProperty().set(true);
        function.executedProperty().set(true);
        if (!passed) {
            function.addTextToOutput("The following parts were not correct:\n\t\t" + String.join("\n\t\t", incorrectParts));
            function.passedProperty().set(false);
        }
    }


    private Optional<String> getExecutionResultNotCovered(String result, Function function, Map<String, List<String>> outputValues) {
        for (var part : function.getExpectedOutputs()) {
            while (part.contains(PREVIOUSOUTPUT_PREFIX) && part.contains(PREVIOUSOUTPUT_SUFFIX)) {
                int startPositionMarker = part.indexOf(PREVIOUSOUTPUT_PREFIX);
                var splitPart = part.substring(startPositionMarker + PREVIOUSOUTPUT_PREFIX.length());
                int endPositionMarker = splitPart.indexOf(PREVIOUSOUTPUT_SUFFIX);
                if (endPositionMarker == -1) {
                    return Optional.of("PREVIOUSOUTPUT_SUFFIX not found");
                }
                splitPart = splitPart.substring(0, endPositionMarker);
                var valueArray = splitPart.split(Pattern.quote(SEPARATOR));
                var partsOfKey = Arrays.copyOfRange(valueArray, 0, valueArray.length - 1);
                String key = String.join(SEPARATOR, partsOfKey);
                int number;
                if (partsOfKey.length == 0 || !outputValues.containsKey(key)) {
                    return Optional.of("key not found in output values");
                }
                try {
                    number = Integer.parseInt(valueArray[valueArray.length - 1]);
                } catch (NumberFormatException e) {
                    return Optional.of("number could not be parsed");
                }
                String partBeforeOutputValue = part.substring(0, startPositionMarker);
                var outputValue = outputValues.get(key).get(number);
                var potentialPartAfterOutputValue = part.substring(endPositionMarker + PREVIOUSOUTPUT_PREFIX.length());
                potentialPartAfterOutputValue = potentialPartAfterOutputValue.substring(potentialPartAfterOutputValue.indexOf(PREVIOUSOUTPUT_SUFFIX) + PREVIOUSOUTPUT_SUFFIX.length());
                part = partBeforeOutputValue + outputValue + potentialPartAfterOutputValue;
            }
            if (result.contains(part)) {
                result = result.substring(result.indexOf(part) + part.length());
            } else {
                return Optional.of(part);
            }
        }
        return Optional.empty();
    }


    private void checkCorrectnessOfLogs(TestcaseWrapper testcase) {
        testcase.executedProperty().set(true);
        if (testcase.getTestcase().getLogsToBeCovered().size() == 0 && !testcase.isSaveLogs()) {
            var functions = testcase.getFunctionsWrapped();
            boolean allFunctionsSuccessful = functions.stream().allMatch(FunctionWrapper::isPassed);
            testcase.passedProperty().set(false);
            testcase.passedProperty().set(true);
            testcase.passedProperty().set(allFunctionsSuccessful);
            return;
        }
        var logs = executor.getAllNewLogs(0L);
        boolean passed = true;
        List<String> incorrectParts = new LinkedList<>();
        List<String> logsCompare = filterLogs(logs);
        if (testcase.isSaveLogs()) {
            testcase.setLogsMeasured(List.copyOf(logsCompare));
        }
        for (var part : testcase.getTestcase().getLogsToBeCovered()) {
            if (notRemovedFromList(logsCompare, part)) {
                incorrectParts.add(part);
                passed = false;
                break;
            }
        }
        var functions = testcase.getFunctionsWrapped();
        if (functions.size() > 0) {
            var lastFunction = functions.get(functions.size() - 1);
            if (passed) {
                boolean allFunctionsSuccessful = functions.stream().allMatch(FunctionWrapper::isPassed);
                testcase.passedProperty().set(false);
                testcase.passedProperty().set(true);
                testcase.passedProperty().set(allFunctionsSuccessful);
            } else {
                lastFunction.addTextToOutput("The following parts were not correct in log:\n" + String.join("\n", incorrectParts));
                testcase.passedProperty().set(true);
                testcase.passedProperty().set(false);
            }
        }

    }


    private Optional<String> getLogPartNotCovered(Testcase testcase) {
        if (testcase.getLogsToBeCovered().size() == 0) {
            return Optional.empty();
        }
        var logs = executor.getAllNewLogs(0L);
        List<String> logsCompare = filterLogs(logs);
        for (var part : testcase.getLogsToBeCovered()) {
            if (notRemovedFromList(logsCompare, part)) {
                return Optional.of(part);
            }
        }
        return Optional.empty();
    }

    private void addResultToOutputValues(String result, Map<String, List<String>> outputValues) {
        KeyValueJsonGenerator keyValueJsonGenerator = new KeyValueJsonGenerator(result);
        var generatedKeyValues = keyValueJsonGenerator.getKeyValues();
        for (var entry : generatedKeyValues.entrySet()) {
            var generatedKey = entry.getKey();
            var generatedValues = entry.getValue();
            List<String> existingValues = outputValues.containsKey(generatedKey) ? outputValues.get(generatedKey) : new ArrayList<>();
            existingValues.addAll(generatedValues);
            outputValues.put(generatedKey, existingValues);
        }
    }

    public void calibrate(List<TestcaseWrapper> testcases, String resetFunction) {
        for (var testcase : testcases) {
            try {
                this.calibrate(testcase, resetFunction);
            } catch (Exception e) {
                //brutal retry
                this.calibrate(testcase, resetFunction);
            }
        }
    }

    public void calibrate(TestcaseWrapper testcase, String resetFunction) {
        executor.resetApplication(resetFunction);
        var functions = testcase.getFunctionsWrapped();
        List<String> resultsFirstExecution = getResultOfExecution(functions);
        List<String> resultsFirstExecutionLogs = executor.getAllNewLogs(0L);
        executor.resetApplication(resetFunction);
        List<String> resultsSecondExecution = getResultOfExecution(functions);
        List<String> resultsSecondExecutionLogs = executor.getAllNewLogs(0L);
        calibrateFunctions(functions, resultsFirstExecution, resultsSecondExecution);
        resultsFirstExecutionLogs = filterLogs(resultsFirstExecutionLogs);
        resultsSecondExecutionLogs = filterLogs(resultsSecondExecutionLogs);
        calibrateLogsOnTestcase(testcase, resultsFirstExecutionLogs, resultsSecondExecutionLogs);
    }


    public void recalibrate(TestcaseWrapper testcase, String resetFunction) {
        executor.resetApplication(resetFunction);
        var functions = testcase.getFunctionsWrapped();
        List<String> resultsFirstExecution = getResultOfExecution(functions);
        List<String> resultsFirstExecutionLogs = executor.getAllNewLogs(0L);
        List<String> oldResults = functions.stream().map(FunctionWrapper::getFunction).map(Function::getExpectedOutputs)
                .map(entry -> String.join("", entry)).toList();
        List<String> oldLogs = testcase.getTestcase().getExpectedLogs();
        calibrateFunctions(functions, resultsFirstExecution, oldResults);
        resultsFirstExecutionLogs = filterLogs(resultsFirstExecutionLogs);
        calibrateLogsOnTestcase(testcase, resultsFirstExecutionLogs, oldLogs);
    }

    private void calibrateFunctions(List<FunctionWrapper> functions, List<String> resultsFirstExecution, List<String> resultsSecondExecution) {
        var calibratedResults = calibrateResults(resultsFirstExecution, resultsSecondExecution);
        if (functions.size() == calibratedResults.size()) {
            for (int i = 0; i < functions.size(); i++) {
                var expectedOutputList = calibratedResults.get(i);
                var function = functions.get(i);
                var originalFunction = function.getFunction();
                originalFunction.setExpectedOutputs(expectedOutputList);

                var textExpectedOutput = expectedOutputList.stream().map(part -> part.replace("*", "\\*")).collect(Collectors.joining("*"));
                function.expectedResultProperty().set(textExpectedOutput);
            }
        }
    }

    private void calibrateLogsOnTestcase(TestcaseWrapper testcase, List<String> resultsFirstExecutionLogs, List<String> resultsSecondExecutionLogs) {
        var calibratedLogs = calibrateLogs(resultsFirstExecutionLogs, resultsSecondExecutionLogs);
        var originalTestcase = testcase.getTestcase();
        originalTestcase.setExpectedLogOutput(calibratedLogs);
        String expectedTextLog = String.join("*", calibratedLogs);
        testcase.expectedLogsProperty().set(expectedTextLog);
    }


    private List<String> calibrateLogs(List<String> resultsFirstExecutionLogs, List<String> resultsSecondExecutionLogs) {
        List<String> result = new LinkedList<>();
        for (var entry : resultsFirstExecutionLogs) {
            if (resultsSecondExecutionLogs.contains(entry)) {
                result.add(entry);
            }
        }
        return result;
    }

    private List<List<String>> calibrateResults(List<String> resultsFirstExecution, List<String> resultsSecondExecution) {
        List<List<String>> result = new LinkedList<>();
        if (resultsFirstExecution.size() != resultsSecondExecution.size()) {
            return result;
        }

        for (int i = 0; i < resultsFirstExecution.size(); i++) {
            var firstResult = resultsFirstExecution.get(i);
            var secondResult = resultsSecondExecution.get(i);
            var calibratedText = calibrateText(firstResult, secondResult);
            result.add(calibratedText);
        }

        return result;
    }

    private List<String> calibrateText(String firstResult, String secondResult) {
        List<String> result = new ArrayList<>();
        while (firstResult.length() >= 3) {
            int potentialMatchSize = 3;
            Optional<String> match = Optional.empty();
            var potentialMatch = firstResult.substring(0, potentialMatchSize);
            while (secondResult.contains(potentialMatch)) {
                match = Optional.of(firstResult.substring(0, potentialMatchSize));
                potentialMatchSize++;
                if (firstResult.length() < potentialMatchSize) {
                    break;
                } else {
                    potentialMatch = firstResult.substring(0, potentialMatchSize);
                }
            }
            if (match.isPresent()) {
                var matchIdentified = match.get();
                result.add(matchIdentified);
                firstResult = firstResult.substring(matchIdentified.length());
                if (matchIdentified.equals(secondResult)) {
                    secondResult = "";
                    break;
                } else {
                    int startIndex = secondResult.indexOf(matchIdentified) + matchIdentified.length();
                    secondResult = secondResult.substring(startIndex);
                }
            } else {
                firstResult = firstResult.substring(1);
            }
        }
        if (secondResult.contains(firstResult) && firstResult.length() > 0) {
            result.add(secondResult);
        }
        return result;
    }

    private List<String> getResultOfExecution(List<FunctionWrapper> functions) {
        var results = new ArrayList<String>();
        final Map<String, List<String>> outputValues = new HashMap<>();
        for (var function : functions) {
            var originalFunction = function.getFunction();
            String functionName = originalFunction.getName();
            String jsonData = originalFunction.getParameter();
            String invocation = String.format("invoke function '%s' with parameter '%s'", functionName, jsonData);
            LOGGER.info(invocation);
            function.addTextToOutput(invocation);
            String result = executor.invokeFunction(functionName, jsonData, outputValues);
            System.out.println(result);
            String resultWithParameters = replaceResultsOfPreviousOutput(result, outputValues);
            addResultToOutputValues(result, outputValues);
            String resultInfoMessage = String.format("result: %s", resultWithParameters);
            function.addTextToOutput(resultInfoMessage);
            LOGGER.info(String.format(resultInfoMessage));
            results.add(resultWithParameters);
        }
        return results;
    }

    private String replaceResultsOfPreviousOutput(String result, Map<String, List<String>> outputValues) {
        for (var entry : outputValues.entrySet()) {
            for (int i = 0; i < entry.getValue().size(); i++) {
                var outputValue = entry.getValue().get(i);
                if (outputValue.length() > 3 && result.contains(outputValue)) {
                    result = result.replaceAll(Pattern.quote(outputValue), String.format(PREVIOUSOUTPUT_PREFIX + "%s" + SEPARATOR + "%d" + PREVIOUSOUTPUT_SUFFIX, entry.getKey(), i));
                }
            }
        }
        return result;
    }


    public void recalibrateTCs(List<TestcaseWrapper> testcases, String resetFunction) {
        for (var testcase : testcases) {
            this.recalibrate(testcase, resetFunction);
        }
    }

    public AWSInvoker getExecutor() {
        return executor;
    }
}
