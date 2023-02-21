package logic.dynamicdatageneration.testrun;

import gui.view.wrapper.ExecutionSettings;
import logic.executionplatforms.KeyValueJsonGenerator;
import logic.model.ServerlessFunction;
import shared.model.input.DynamicKeyValue;
import shared.model.input.IntegerInput;

import java.util.*;

public class TestData {

    private final double probChangeGoodData;
    private final double probEntryUndefined;
    private final double probSimilarInputAsValue;
    private final double probRandomInputAsValue;
    private final double probSimilarOutputAsValue;
    private final double probRandomOutputAsValue;

    private final double probSameValueEverywhere;

    private final Map<String, List<String>> inputValues = new HashMap<>();

    private final Map<String, List<String>> outputValues = new HashMap<>();
    private final Set<String> authKeys;
    private final List<String> authValues = new ArrayList<>();

    final List<FunctionWithInputData> testFunctions = new ArrayList<>();
    private static final Random rn = new Random();

    private boolean useSameValues = false;
    private String sameInputString = null;
    private String sameInputInteger = null;


    public TestData(List<ServerlessFunction> functions, ExecutionSettings executionSettings) {
        this.probChangeGoodData = executionSettings.getProbChangeGoodData();
        this.probEntryUndefined = executionSettings.getProbEntryUndefined();
        this.probSimilarInputAsValue = executionSettings.getProbSimilarInputAsValue();
        this.probRandomInputAsValue = executionSettings.getProbRandomInputAsValue();
        this.probSimilarOutputAsValue = executionSettings.getProbSimilarOutputAsValue();
        this.probRandomOutputAsValue = executionSettings.getProbRandomOutputAsValue();
        this.probSameValueEverywhere = executionSettings.getProbSameValueEverywhere();
        this.authKeys = executionSettings.getAuthKeys();
        for (var function : functions) {
            this.addFunction(function);
        }

    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (var testFunction : testFunctions) {
            result.append(testFunction.getName()).append("\n");
            result.append(testFunction.getJSON()).append("\n");
        }
        return result.toString();
    }

    private void addFunction(ServerlessFunction function) {
        FunctionWithInputData functionWithInputData = new FunctionWithInputData(function);
        testFunctions.add(functionWithInputData);
    }

    public List<FunctionWithInputData> getTestFunctions() {
        return testFunctions;
    }


    public void resetOutputData() {
        this.outputValues.clear();
    }

    public void resetInputData() {
        this.inputValues.clear();
    }

    public void resetAuthValues() {
        this.authValues.clear();
    }

    public Map<String, List<String>> getInputValues() {
        return new HashMap<>(inputValues);
    }

    public Map<String, List<String>> getOutputValues() {
        return new HashMap<>(outputValues);
    }

    public double getProbChangeGoodData() {
        return probChangeGoodData;
    }

    public boolean isUseSameValues() {
        return useSameValues;
    }

    public Optional<String> getSameInputString() {
        return Optional.ofNullable(sameInputString);
    }

    public Optional<String> getSameInputInteger() {
        return Optional.ofNullable(sameInputInteger);
    }

    public double getProbEntryUndefined() {
        return probEntryUndefined;
    }

    public double getProbSimilarInputAsValue() {
        return probSimilarInputAsValue;
    }

    public double getProbRandomInputAsValue() {
        return probRandomInputAsValue;
    }

    public double getProbSimilarOutputAsValue() {
        return probSimilarOutputAsValue;
    }

    public double getProbSameValueEverywhere() {
        return probSameValueEverywhere;
    }

    public double getProbRandomOutputAsValue() {
        return probRandomOutputAsValue;
    }

    public void addInputOfFunction(FunctionWithInputData function) {
        function.getActualGeneralInputData().stream().filter(data -> data instanceof DynamicKeyValue || data instanceof IntegerInput)
                .forEach(entry -> this.addInputValue(entry.getKey(), entry.getGeneratedValue()));

    }

    private void addInputValue(String key, String value) {
        List<String> valueList = inputValues.containsKey(key) ? inputValues.get(key) : new LinkedList<>();
        valueList.add(value);
        inputValues.put(key, valueList);
    }

    public void addResultToOutputAndAuth(String result) {
        KeyValueJsonGenerator keyValueJsonGenerator = new KeyValueJsonGenerator(result);
        var outputKeyValues = keyValueJsonGenerator.getKeyValues();
        for (var authKey : authKeys) {
            if (outputKeyValues.containsKey(authKey)) {
                authValues.addAll(outputKeyValues.get(authKey));
                outputKeyValues.remove(authKey);
            }
        }
        outputValues.putAll(outputKeyValues);
    }

    public String getDataAsText() {
        StringBuilder result = new StringBuilder();
        for (var testFunction : testFunctions) {
            result.append(testFunction.getName()).append(" ");
            result.append(testFunction.getJSON()).append(" ");
        }
        return result.toString();
    }

    public String getExecutableDataAsText() {
        StringBuilder result = new StringBuilder();
        for (var testFunction : testFunctions) {
            result.append(testFunction.getName()).append(" ");
            result.append(testFunction.getJSON()).append("\n");
        }
        return result.toString();

    }

    public void checkToUseSameValues() {
        double randomValue = rn.nextDouble();
        this.useSameValues = probSameValueEverywhere > randomValue;
        if (!this.useSameValues) {
            this.sameInputString = null;
            this.sameInputInteger = null;
            return;
        }
        var inputDataString = testFunctions.stream()
                .flatMap(function -> function.getActualGeneralInputData().stream())
                .filter(input -> input instanceof DynamicKeyValue).toList();
        if (inputDataString.size() > 0) {
            var randomInputString = inputDataString.get(rn.nextInt(inputDataString.size()));
            randomInputString.calculateNewValues();
            this.sameInputString = randomInputString.getGeneratedValue();
        }
        var inputDataInteger = testFunctions.stream()
                .flatMap(function -> function.getActualGeneralInputData().stream())
                .filter(input -> input instanceof IntegerInput).toList();
        if (inputDataInteger.size() > 0) {
            var randomInputInteger = inputDataInteger.get(rn.nextInt(inputDataInteger.size()));
            randomInputInteger.calculateNewValues();
            this.sameInputInteger = randomInputInteger.getGeneratedValue();
        }


    }

    public void setGeneralAuthenticationValue(String potentialAuthentication) {
        authValues.clear();
        KeyValueJsonGenerator keyValueJsonGenerator = new KeyValueJsonGenerator(potentialAuthentication);
        var outputKeyValues = keyValueJsonGenerator.getKeyValues();
        for (var authKey : authKeys) {
            if (outputKeyValues.containsKey(authKey)) {
                authValues.addAll(outputKeyValues.get(authKey));
            }
        }
    }

    public boolean containsAuthKey(String key) {
        return authKeys.contains(key);
    }

    public List<String> getAuthValues() {
        return List.copyOf(authValues);
    }

    public Set<String> getAuthKeys() {
        return Set.copyOf(authKeys);
    }
}
