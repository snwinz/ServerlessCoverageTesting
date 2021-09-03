package gui.model;

import com.google.gson.annotations.Expose;

public class IntegerInput {
    @Expose
    private final String key;
    @Expose
    private final String minValue;
    @Expose
    private final String maxValue;

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

    @Override
    public String toString() {
        return
                "key=" + key +
                        ", minValue=" + minValue +
                        ", maxValue=" + maxValue;
    }
}
