package logic.executionplatforms;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyValueJsonGenerator {
    private final String input;
    private final Map<String, List<String>> keyValues = new HashMap<>();
    private static final JsonParser jsonParser = new JsonParser();

    public KeyValueJsonGenerator(String value) {
        this.input = value;
        if (isValueInJsonFormat(value)) {
            findAndAddValues(input);
        }
    }

    public String getInput() {
        return input;
    }

    public Map<String, List<String>> getKeyValues() {
        return keyValues;
    }

    private void findAndAddValues(String input) {
        try {
            var object = jsonParser.parse(input);
            if (object.isJsonObject()) {
                JsonObject ob = object.getAsJsonObject();
                var entrySet = ob.entrySet();
                for (var entry : entrySet) {
                    var key = entry.getKey();
                    var value = entry.getValue().toString();
                    if (isValueInJsonFormat(value)) {
                        findAndAddValues(value);
                    } else if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                        value = removeSquareBrackets(value);
                        if (isValueInJsonFormat(value)) {
                            findAndAddValues(value);
                        }
                        value = makeStringToJson(value);
                        if (isValueInJsonFormat(value)) {
                            findAndAddValues(value);
                        } else {
                            addKeyValueToMap(key, value);
                        }
                    } else {
                        addKeyValueToMap(key, value);
                    }
                }
            }
        } catch (JsonParseException e) {
            System.err.println("Input: " + input + "  could not be parsed");
        }
    }

    private String makeStringToJson(String value) {
        return value.replaceAll("\\\\\"", "\"").replaceAll("\\\\n", " ").replaceAll("\\\\", " ");
    }

    private String removeSquareBrackets(String value) {
        if (value.startsWith("[") && value.endsWith("]")) {
            value = value.substring(1, value.length() - 1);
            value = value.replace("},{", ",");
        }
        return value;
    }

    private boolean isValueInJsonFormat(String value) {
        try {
            var object = jsonParser.parse(value);
            return object.isJsonObject();
        } catch (JsonParseException e) {
            return false;
        }
    }


    private void addKeyValueToMap(String key, String value) {
        List<String> values = keyValues.containsKey(key) ? keyValues.get(key) : new ArrayList<>();
        values.add(value);
        keyValues.put(key, values);
    }

}
