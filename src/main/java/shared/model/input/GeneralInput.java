package shared.model.input;

import com.google.gson.annotations.Expose;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Stream;

public class GeneralInput {
    @Expose
    private final String key;
    @Expose
    private String dynamicValue;
    @Expose
    private String constantValue;
    @Expose
    private Integer minValue;
    @Expose
    private Integer maxValue;
    @Expose
    private Integer entryID;
    @Expose
    private Integer parentId;
    @Expose
    private Boolean isArrayNode;
    @Expose
    private Boolean isParentNode;
    @Expose
    private Boolean isJsonSavedAsString;
    @Expose
    private Boolean isJsonSavedAsBase64;

    private String generatedValue;
    private boolean undefined;


    public GeneralInput(String key) {
        this.key = key;
    }


    public String getKey() {
        return key;
    }

    public String getDynamicValue() {
        return dynamicValue;
    }

    public Integer getEntryID() {
        return entryID;
    }

    public void setDynamicValue(String dynamicValue) {
        this.dynamicValue = dynamicValue;
    }

    public void setMinValue(Integer minValue) {
        this.minValue = minValue;
    }

    public void setMaxValue(Integer maxValue) {
        this.maxValue = maxValue;
    }

    public void setEntryID(Integer entryID) {
        this.entryID = entryID;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public void setArrayNode(Boolean arrayNode) {
        isArrayNode = arrayNode;
    }

    public void setConstantValue(String constantValue) {
        this.constantValue = constantValue;
    }

    public Boolean getParentNode() {
        return isParentNode;
    }

    public void setParentNode(Boolean parentNode) {
        isParentNode = parentNode;
    }

    public Boolean getJsonSavedAsString() {
        return isJsonSavedAsString;
    }

    public void setJsonSavedAsString(Boolean jsonSavedAsString) {
        isJsonSavedAsString = jsonSavedAsString;
    }

    @Override
    public String toString() {
        String minMax = null;
        if (minValue != null && maxValue != null) {
            minMax = "min :" + minValue + " max: " + maxValue;
        }
        var value = Stream.of(dynamicValue, constantValue, minMax).filter(Objects::nonNull).findFirst().orElse("not defined");
        return "key: " + key +
                "; value: " + value +
                "; entry ID: " + entryID;
    }


    public Integer getMinValue() {
        return minValue;
    }

    public Integer getMaxValue() {
        return maxValue;
    }

    public Integer getParentId() {
        return parentId;
    }

    public Boolean getArrayNode() {
        return isArrayNode;
    }

    public String getConstantValue() {
        return constantValue;
    }

    public String getJsonFormat(List<GeneralInput> generalInputs) {
        return "";
    }

    public String getGeneratedValue() {
        if (generatedValue == null) {
            return constantValue;
        } else {
            return generatedValue;
        }
    }

    public GeneralInput getCopy() {
        GeneralInput copy = new GeneralInput(this.key);
        copy.dynamicValue = this.dynamicValue;
        copy.minValue = this.minValue;
        copy.maxValue = this.maxValue;
        copy.entryID = this.entryID;
        copy.parentId = this.parentId;
        copy.isArrayNode = this.isArrayNode;
        copy.constantValue = this.constantValue;
        return copy;
    }

    public void setGeneratedValue(String generatedValue) {
        this.generatedValue = generatedValue;
    }

    public void calculateNewValues() {
    }

    public boolean isUndefined() {
        return undefined;
    }

    public void setUndefined(boolean undefined) {
        this.undefined = undefined;
    }

    public String getJsonWithData(List<GeneralInput> generalInputData) {
        return "";
    }

    public Boolean getJsonSavedAsBase64() {
        return isJsonSavedAsBase64;
    }

    public void setJsonSavedAsBase64(Boolean jsonSavedAsBase64) {
        isJsonSavedAsBase64 = jsonSavedAsBase64;
    }
}
