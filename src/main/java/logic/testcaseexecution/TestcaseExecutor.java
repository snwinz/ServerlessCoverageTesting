package logic.testcaseexecution;

import gui.view.wrapper.FunctionWrapper;
import gui.view.wrapper.TestcaseWrapper;
import logic.executionplatforms.KeyValueJsonGenerator;
import logic.executionplatforms.AWSInvoker;

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

            addResultToOutputvalues(result, outputValues);

            String resultFormatted = String.format("result: %s", result);

            LOGGER.info(resultFormatted);
            function.addTextToOutput(resultFormatted);
        }

    }

    public void executeTCs(List<TestcaseWrapper> testcases, String resetFunction) {
        for (var testcase : testcases) {
            executor.callResetFunction(resetFunction);
            this.executeTC(testcase);
        }
    }


    private void checkCorrectnessOfOutput(String result, FunctionWrapper function, Map<String, List<String>> outputValues) {
        boolean passed = true;
        List<String> incorrectParts = new LinkedList<>();
        for (var part : function.getFunction().getResults()) {
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
            function.addTextToOutput("The following parts were not correct:\n" + String.join("\n", incorrectParts));
            function.passedProperty().set(false);
        }
    }

    private void addResultToOutputvalues(String result, Map<String, List<String>> outputValues) {
        KeyValueJsonGenerator keyValueJsonGenerator = new KeyValueJsonGenerator(result);
        var outputKeyValues = keyValueJsonGenerator.getKeyValues();
        outputValues.putAll(outputKeyValues);
    }


    public void calibrate(List<TestcaseWrapper> testcases, String resetFunction) {
        for (var testcase : testcases) {
            this.calibrate(testcase, resetFunction);
        }
    }

    public void calibrate(TestcaseWrapper testcase, String resetFunction) {
        var functions = testcase.getFunctionsWrapped();
        executor.callResetFunction(resetFunction);
        List<String> resultsFirstExecution = getResultOfExecution(functions);
        executor.callResetFunction(resetFunction);
        List<String> resultsSecondExecution = getResultOfExecution(functions);

        var calibratedResults = calibrateResults(resultsFirstExecution, resultsSecondExecution);

        if (functions.size() == calibratedResults.size()) {

            for (int i = 0; i < functions.size(); i++) {
                var function = functions.get(i);
                var expectedOutput = String.join("*", calibratedResults.get(i));
                function.expectedResultProperty().set(expectedOutput);
            }
        }

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
            calibratedText = calibratedText.stream().map(part -> part.replace("*", "\\*")).collect(Collectors.toList());
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
            result = replaceResultsOfPreviousOutput(result, outputValues);
            addResultToOutputvalues(result, outputValues);
            String resultInfoMessage = String.format("result: %s", result);
            function.addTextToOutput(resultInfoMessage);
            LOGGER.info(String.format(resultInfoMessage));
            results.add(result);
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


}
