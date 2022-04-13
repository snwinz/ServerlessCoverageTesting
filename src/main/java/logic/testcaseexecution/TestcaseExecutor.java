package logic.testcaseexecution;

import logic.executionplatforms.KeyValueJsonGenerator;
import logic.executionplatforms.AWSInvoker;
import shared.model.Testcase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class TestcaseExecutor {
    private final AWSInvoker executor;
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public TestcaseExecutor(String region) {
        this.executor = new AWSInvoker(region);
    }

    public void executeTC(Testcase testcase) {
        var functions = testcase.functions();
        final Map<String, List<String>> outputValues = new HashMap<>();
        for (var function : functions) {
            String functionName = function.getName();

            String jsonData = function.getParameter();
            String invocation = String.format("invoke function '%s' with parameter '%s'", functionName, jsonData);
            LOGGER.info(invocation);

            String result = executor.invokeFunction(functionName, jsonData, outputValues);
            addResultToOutput(result, outputValues);

            String resultFormatted = String.format("result: %s", result);

            LOGGER.info(resultFormatted);
            function.addTextToOutput(resultFormatted);
        }

    }

    private void addResultToOutput(String result, Map<String, List<String>> outputValues) {
        KeyValueJsonGenerator keyValueJsonGenerator = new KeyValueJsonGenerator(result);
        var outputKeyValues = keyValueJsonGenerator.getKeyValues();
        outputValues.putAll(outputKeyValues);
    }
}