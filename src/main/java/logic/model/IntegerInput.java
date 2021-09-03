package logic.model;

import com.google.gson.annotations.Expose;

public class IntegerInput {
    @Expose
    private String key;
    @Expose
    private String minValue;
    @Expose
    private String maxValue;

    public IntegerInput(String key, String minValue, String maxValue) {
        this.key = key;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public String getKey() {
        return key;
    }

    public String getMinValue() {
        return minValue;
    }

    public String getMaxValue() {
        return maxValue;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public String toString() {
        return
                "key=" + key +
                        ", minValue=" + minValue +
                        ", maxValue=" + maxValue;
    }
}
