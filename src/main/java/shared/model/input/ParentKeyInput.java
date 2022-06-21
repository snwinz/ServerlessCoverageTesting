package shared.model.input;


import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class ParentKeyInput extends GeneralInput {
    public ParentKeyInput(String key, boolean selected, boolean selected64) {
        super(key);
        this.setParentNode(true);
        this.setJsonSavedAsString(selected);
        this.setJsonSavedAsBase64(selected64);
    }

    public ParentKeyInput(GeneralInput generalInput) {
        super(generalInput.getKey());
        this.setParentNode(true);
        this.setParentId(generalInput.getParentId());
        this.setEntryID(generalInput.getEntryID());
        this.setJsonSavedAsString(generalInput.getJsonSavedAsString());
        this.setJsonSavedAsBase64(generalInput.getJsonSavedAsBase64());
    }


    @Override
    public ParentKeyInput getCopy() {
        return new ParentKeyInput(this);
    }

    @Override
    public String getJsonFormat(List<GeneralInput> generalInputs) {

        var children = generalInputs.stream().filter(entry -> this.getEntryID().equals(entry.getParentId())).toList();

        var allKeyValueChildren = children.stream().filter(a -> !(a instanceof ConstantValue))
                .map(child -> child.getJsonFormat(generalInputs)).collect(Collectors.joining(","));
        if (this.getJsonSavedAsString()) {
            allKeyValueChildren = allKeyValueChildren.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"");
            return String.format("\"%s\" : \"{ %s }\"", this.getKey(), allKeyValueChildren);
        } else {
            return String.format("\"%s\" : { %s }", this.getKey(), allKeyValueChildren);
        }


    }

    @Override
    public String getJsonWithData(List<GeneralInput> generalInputs) {
        if (this.isUndefined()) {
            return "";
        } else {
            var children = generalInputs.stream().filter(entry -> this.getEntryID().equals(entry.getParentId())).toList();
            var allKeyValueChildren = children.stream().filter(a -> !(a instanceof ConstantValue))
                    .map(child -> child.getJsonWithData(generalInputs)).filter(entry -> !"".equals(entry)).collect(Collectors.joining(","));
            if (this.getJsonSavedAsString()) {
                allKeyValueChildren = allKeyValueChildren.replaceAll("\\\\", "\\\\\\\\");
                allKeyValueChildren = allKeyValueChildren.replaceAll("\"", "\\\\\"");
                return String.format("\"%s\" : \"{ %s }\"", this.getKey(), allKeyValueChildren);
            } else {
                if (this.getJsonSavedAsBase64() != null && this.getJsonSavedAsBase64()) {
                    allKeyValueChildren = "{" + allKeyValueChildren + "}";
                    return String.format("\"%s\" : \" %s \"", this.getKey(), Base64.getEncoder().encodeToString(allKeyValueChildren.getBytes()));
                }

                return String.format("\"%s\" : { %s }", this.getKey(), allKeyValueChildren);
            }
        }
    }

}
