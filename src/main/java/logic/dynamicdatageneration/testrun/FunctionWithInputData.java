package logic.dynamicdatageneration.testrun;

import logic.model.ServerlessFunction;
import shared.model.input.DynamicKeyValue;
import shared.model.input.GeneralInput;
import shared.model.input.IntegerInput;

import java.util.*;
import java.util.stream.Collectors;

import static logic.testcasegenerator.testcaseexecution.TestcaseExecutor.*;

public class FunctionWithInputData {

    private ServerlessFunction function;

    private final List<GeneralInput> generalInputData;
    private boolean promisingData = false;


    private final static Random rn = new Random();

    public FunctionWithInputData(ServerlessFunction function) {
        this.function = function;
        var inputFormats = function.getFunctionInputFormat();
        if (inputFormats == null) {
            this.generalInputData = new ArrayList<>();
        } else {
            this.generalInputData = inputFormats.getTypedGeneralInputs();
        }
    }

    public boolean isPromisingData() {
        return promisingData;
    }

    public void setPromisingData(boolean promisingData) {
        this.promisingData = promisingData;
    }

    public ServerlessFunction getFunction() {
        return function;
    }

    public void setFunction(ServerlessFunction function) {
        this.function = function;
    }

    public String getName() {
        return this.function.getName();
    }

    public String getJSON() {
        StringBuilder result = new StringBuilder();

        result.append('{');
        var rootEntry = generalInputData.stream().filter(item -> item.getParentId() == null && !item.isUndefined())
                .map(item -> item.getJsonWithData(generalInputData)).collect(Collectors.joining(","));
        result.append(rootEntry);
        result.append('}');

        return result.toString();
    }

    public List<GeneralInput> getActualGeneralInputData() {
        return generalInputData;
    }

    public void changeData(TestData testData) {
        if (testData.isUseSameValues()) {
            for (var inputData : generalInputData) {
                if (inputData instanceof DynamicKeyValue) {
                    var sameValue = testData.getSameInputString();
                    sameValue.ifPresent(inputData::setGeneratedValue);
                }
                if (inputData instanceof IntegerInput) {
                    var sameValue = testData.getSameInputInteger();
                    sameValue.ifPresent(inputData::setGeneratedValue);
                }
            }
            return;
        }

        if (promisingData) {
            double randomValue = rn.nextDouble();
            if (randomValue > testData.getProbChangeGoodData()) {
                return;
            }
        }
        for (var inputData : generalInputData) {
            inputData.setUndefined(false);
            inputData.calculateNewValues();
            double decisionEntryUndefined = rn.nextDouble();
            if (decisionEntryUndefined < testData.getProbEntryUndefined()) {
                inputData.setUndefined(true);
                continue;
            }
            if (inputData instanceof DynamicKeyValue || inputData instanceof IntegerInput) {
                double decisionRandomInputAsValue = decisionEntryUndefined - testData.getProbEntryUndefined();
                double decisionSimilarInputAsValue = decisionRandomInputAsValue - testData.getProbRandomInputAsValue();
                double decisionRandomOutputAsValue = decisionSimilarInputAsValue - testData.getProbSimilarInputAsValue();
                double decisionSimilarOutputAsValue = decisionRandomOutputAsValue - testData.getProbRandomOutputAsValue();

                if (decisionRandomInputAsValue < testData.getProbRandomInputAsValue()) {
                    var inputValues = testData.getInputValues().values().stream()
                            .flatMap(Collection::stream).collect(Collectors.toList());
                    setRandomEntryOfListAsValue(inputData, inputValues);
                } else if (decisionSimilarInputAsValue < testData.getProbSimilarInputAsValue()) {
                    var inputValues = testData.getInputValues().get(inputData.getKey());
                    setRandomEntryOfListAsValue(inputData, inputValues);
                } else if (decisionRandomOutputAsValue < testData.getProbRandomOutputAsValue()) {
                    var inputValues = testData.getOutputValues().entrySet().stream().map(entry -> {
                        var key = entry.getKey();
                        var values = entry.getValue();
                        List<Pair> result = new ArrayList<>();
                        for (int i = 0; i < values.size(); i++) {
                            var value = values.get(i);
                            result.add(new Pair(key, value, i));
                        }
                        return result;
                    }).flatMap(List::stream).collect(Collectors.toList());
                    setRandomEntryOfOutputAsValue(inputData, inputValues);
                } else if (decisionSimilarOutputAsValue < testData.getProbSimilarOutputAsValue()) {
                    var inputValues = testData.getOutputValues().entrySet().stream()
                            .filter(entry -> entry.getKey().equals(inputData.getKey()))
                            .map(entry -> {
                                var key = entry.getKey();
                                var values = entry.getValue();
                                List<Pair> result = new ArrayList<>();
                                for (int i = 0; i < values.size(); i++) {
                                    var value = values.get(i);
                                    result.add(new Pair(key, value, i));
                                }
                                return result;
                            }).flatMap(List::stream).collect(Collectors.toList());
                    setRandomEntryOfOutputAsValue(inputData, inputValues);
                }
            }
        }

    }

    private void setRandomEntryOfOutputAsValue(GeneralInput inputData, List<Pair> inputValues) {
        if (inputValues != null && inputValues.size() > 0) {
            var item = getRandomItem(inputValues);
            var inputText = String.format(PREVIOUSOUTPUT_PREFIX + "%s" + SEPARATOR + "%s" + PREVIOUSOUTPUT_SUFFIX, item.key(), item.occurrence());
            inputData.setGeneratedValue(inputText);
        }

    }

    private void setRandomEntryOfListAsValue(GeneralInput inputData, List<String> inputValues) {
        if (inputValues != null && inputValues.size() > 0) {
            var item = getRandomItem(inputValues);
            inputData.setGeneratedValue(item);
        }
    }


    private <T> T getRandomItem(List<T> list) {
        int entryNumber = rn.nextInt(list.size());
        return list.get(entryNumber);
    }



    private record Pair(String key, String value, int occurrence) {
    }
}