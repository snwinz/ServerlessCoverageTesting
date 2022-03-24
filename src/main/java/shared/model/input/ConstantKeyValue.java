package shared.model.input;


import java.util.List;

public class ConstantKeyValue extends GeneralInput {
    public ConstantKeyValue(String key, String constantValue) {
        super(key);
        this.setConstantValue(constantValue);
    }

    public ConstantKeyValue(GeneralInput generalInput) {
        super(generalInput.getKey());
        this.setConstantValue(generalInput.getConstantValue());
        this.setParentId(generalInput.getParentId());
        this.setEntryID(generalInput.getEntryID());

    }

    @Override
    public ConstantKeyValue getCopy() {
        return new ConstantKeyValue(this);
    }

    @Override
    public String getJsonFormat(List<GeneralInput> generalInputs) {
        return String.format("\"%s\" : \"%s\"", this.getKey(), this.getConstantValue());
    }

    @Override
    public String getJsonWithData(List<GeneralInput> generalInputs) {
        if (this.isUndefined()) {
            return "";
        } else {
            return String.format("\"%s\" : \"%s\"", this.getKey(), this.getConstantValue());
        }
    }

}
