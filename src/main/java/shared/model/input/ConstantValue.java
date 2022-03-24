package shared.model.input;


import java.util.List;

public class ConstantValue extends GeneralInput {
    public ConstantValue(String constantValue) {
        super(null);
        this.setConstantValue(constantValue);
    }

    public ConstantValue(GeneralInput generalInput) {
        super(generalInput.getKey());
        this.setConstantValue(generalInput.getConstantValue());
        this.setParentId(generalInput.getParentId());
        this.setEntryID(generalInput.getEntryID());
    }


    @Override
    public ConstantValue getCopy() {
        return new ConstantValue(this);
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
            return String.format("%s", this.getConstantValue());
        }
    }
}
