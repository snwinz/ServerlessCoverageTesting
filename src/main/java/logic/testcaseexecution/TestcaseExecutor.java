package logic.testcaseexecution;

import gui.view.wrapper.FunctionWrapper;
import gui.view.wrapper.TestcaseWrapper;
import logic.executionplatforms.KeyValueJsonGenerator;
import logic.executionplatforms.AWSInvoker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class TestcaseExecutor {
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
            addResultToOutput(result, outputValues);

            checkCorrectnessOfOutput(result, function);

            String resultFormatted = String.format("result: %s", result);

            LOGGER.info(resultFormatted);
            function.addTextToOutput(resultFormatted);
        }

    }

    private void checkCorrectnessOfOutput(String result, FunctionWrapper function) {
        boolean passed = true;
        for (var part : function.getFunction().getResults()) {
            if (result.contains(part)) {
                var splitResult = result.split(Pattern.quote(part));
                if (splitResult.length > 1) {
                    result = splitResult[1];
                }
            } else {
                passed = false;
                break;
            }
        }
        if (passed) {
            function.passedProperty().set(true);
        } else {
            function.passedProperty().set(false);
        }
    }

    private void addResultToOutput(String result, Map<String, List<String>> outputValues) {
        KeyValueJsonGenerator keyValueJsonGenerator = new KeyValueJsonGenerator(result);
        var outputKeyValues = keyValueJsonGenerator.getKeyValues();
        outputValues.putAll(outputKeyValues);
    }
}
