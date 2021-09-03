package logic.model;

import com.google.gson.annotations.Expose;

public class StringInput {
    @Expose
    private String key;
    @Expose
    private String jsonValue;

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

    public void setKey(String key) {
        this.key = key;
    }

    public void setJsonValue(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @Override
    public String toString() {
        return
                "key='" + key + '\'' +
                        "jsonRegex='" + jsonValue + '\'';
    }
}
