package gui.model;

import com.google.gson.annotations.Expose;

public class StringInput {
    @Expose
    private final String key;
    @Expose
    private final String jsonValue;

    public StringInput(String key, String jsonValue) {
        this.key = key;
        this.jsonValue = jsonValue;
    }

    public String getKey() {
        return key;
    }

    public String getJsonValue() {
        return jsonValue;
    }

    @Override
    public String toString() {
        return
                "key=" + key + ", regex=" + jsonValue;
    }

}
