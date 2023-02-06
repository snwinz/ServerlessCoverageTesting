package shared.model.input;


import com.github.curiousoddman.rgxgen.RgxGen;

import java.util.List;
import java.util.Random;

import static shared.model.StringSeparators.BASE_64_PREFIX;
import static shared.model.StringSeparators.BASE_64_SUFFIX;

public class DynamicValue extends GeneralInput {
    public DynamicValue(String dynamicValue) {
        super(null);
        this.setDynamicValue(dynamicValue);
    }

    public DynamicValue(GeneralInput generalInput) {
        super(generalInput.getKey());
        this.setDynamicValue(generalInput.getConstantValue());
        this.setParentId(generalInput.getParentId());
        this.setEntryID(generalInput.getEntryID());
    }


    @Override
    public DynamicValue getCopy() {
        return new DynamicValue(this);
    }

    @Override
    public String getJsonFormat(List<GeneralInput> generalInputs) {
        return String.format("%s", this.getConstantValue());
    }


    @Override
    public String getJsonWithData(List<GeneralInput> generalInputs) {
        if (this.isUndefined()) {
            return "";
        } else {
            String value = this.getGeneratedValue();
            if (this.getJsonSavedAsBase64() != null && this.getJsonSavedAsBase64()) {
                value = BASE_64_PREFIX + value + BASE_64_SUFFIX;
            }
            return String.format("%s", value);
        }

    }


    @Override
    public void setGeneratedValue(String generatedValue) {
        generatedValue = generatedValue.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"");
        super.setGeneratedValue(generatedValue);
    }

    @Override
    public void calculateNewValues() {
        RgxGen rgxGen = new RgxGen(this.getDynamicValue());
        Random rnd = new Random();
        String result = rgxGen.generate(rnd);
        this.setGeneratedValue(result);
    }

}
