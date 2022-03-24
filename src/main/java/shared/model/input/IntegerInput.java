package shared.model.input;


import java.util.List;
import java.util.Random;

public class IntegerInput extends GeneralInput {
    public IntegerInput(String key, Integer minValue, Integer maxValue) {
        super(key);
        this.setMaxValue(maxValue);
        this.setMinValue(minValue);
    }

    public IntegerInput(GeneralInput generalInput) {
        super(generalInput.getKey());
        this.setMaxValue(generalInput.getMaxValue());
        this.setMinValue(generalInput.getMinValue());
        this.setParentId(generalInput.getParentId());
        this.setEntryID(generalInput.getEntryID());
    }

    @Override
    public IntegerInput getCopy() {
        return new IntegerInput(this);
    }

    @Override
    public String getJsonFormat(List<GeneralInput> generalInputs) {
        return String.format("\"%s\" : min \"%s\" max \"%s\"", this.getKey(), this.getMinValue(), this.getMaxValue());
    }

    @Override
    public String getJsonWithData(List<GeneralInput> generalInputs) {
        if (this.isUndefined()) {
            return "";
        } else {
            return String.format("\"%s\" : %s", this.getKey(), this.getGeneratedValue());
        }
    }

    @Override
    public void calculateNewValues() {
        int min = this.getMinValue();
        int max = this.getMaxValue();
        int result = 0;
        if (min <= max) {
            Random random = new Random();
            result = random.nextInt(max + 1 - min) + min;
        }
        this.setGeneratedValue(String.valueOf(result));
    }
}
