package shared.model.input;


import com.github.curiousoddman.rgxgen.RgxGen;

import java.util.List;
import java.util.Random;

public class DynamicKeyValue extends GeneralInput {
    public DynamicKeyValue(String key, String dynamicValue, boolean base64) {
        super(key);
        this.setDynamicValue(dynamicValue);
        this.setJsonSavedAsBase64(base64);
    }

    public DynamicKeyValue(GeneralInput generalInput) {
        super(generalInput.getKey());
        this.setDynamicValue(generalInput.getDynamicValue());
        this.setParentId(generalInput.getParentId());
        this.setEntryID(generalInput.getEntryID());
        this.setJsonSavedAsBase64(generalInput.getJsonSavedAsBase64());
    }

    @Override
    public DynamicKeyValue getCopy() {
        return new DynamicKeyValue(this);
    }

    @Override
    public String getJsonFormat(List<GeneralInput> generalInputs) {
        return String.format("\"%s\" : \"%s\"", this.getKey(), this.getDynamicValue());
    }

    @Override
    public String getJsonWithData(List<GeneralInput> generalInputs) {
        if (this.isUndefined()) {
            return "";
        } else {
            String value = this.getGeneratedValue();

            if (this.getJsonSavedAsBase64() != null && this.getJsonSavedAsBase64()) {
                value = "##BASE64__" + value + "__BASE64##";
            }
            return String.format("\"%s\" : \"%s\"", this.getKey(), value);
        }

    }


    @Override
    public void setGeneratedValue(String generatedValue) {
        generatedValue = generatedValue.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"","\\\\\"");
        super.setGeneratedValue(generatedValue);
    }

    @Override
    public void calculateNewValues () {
        RgxGen rgxGen = new RgxGen(this.getDynamicValue());
        Random rnd = new Random();
        String result = rgxGen.generate(rnd);
        this.setGeneratedValue(result);
    }
}
