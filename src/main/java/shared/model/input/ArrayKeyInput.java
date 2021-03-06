package shared.model.input;

import java.util.List;
import java.util.stream.Collectors;

public class ArrayKeyInput extends GeneralInput {
    public ArrayKeyInput(String key) {
        super(key);
        this.setArrayNode(true);
    }

    public ArrayKeyInput(GeneralInput generalInput) {
        super(generalInput.getKey());
        this.setArrayNode(true);
        this.setParentId(generalInput.getParentId());
        this.setEntryID(generalInput.getEntryID());
    }

    public String getJsonFormat2(List<GeneralInput> generalInputs) {
        var children = generalInputs.stream().filter(entry -> entry.getParentId().equals(this.getEntryID())).collect(Collectors.toList());
        var allChildren = children.stream().map(child -> child.getJsonFormat(generalInputs)).collect(Collectors.joining(","));
        return String.format("{\"%s\" : [ %s]}", this.getKey(), allChildren);
    }

    @Override
    public ArrayKeyInput getCopy() {
        return new ArrayKeyInput(this);
    }

    @Override
    public String getJsonFormat(List<GeneralInput> generalInputs) {

        var children = generalInputs.stream().filter(entry -> this.getEntryID().equals(entry.getParentId())).collect(Collectors.toList());

        var allKeyValueChildren = children.stream().filter(a -> !(a instanceof ConstantValue))
                .map(child -> child.getJsonFormat(generalInputs)).collect(Collectors.joining(",", "{", "}"));
        var allOnlyValueChildren = children.stream().filter(a -> (a instanceof ConstantValue))
                .map(child -> child.getJsonFormat(generalInputs)).collect(Collectors.joining(",", "\"", "\""));
        final String entries = createEntries(allKeyValueChildren, allOnlyValueChildren);
        return entries;
    }


    @Override
    public String getJsonWithData(List<GeneralInput> generalInputs) {
        if (this.isUndefined()) {
            return "";
        } else {
            var children = generalInputs.stream().filter(entry -> this.getEntryID().equals(entry.getParentId())).collect(Collectors.toList());
            var allKeyValueChildren = children.stream().filter(a -> !(a instanceof ConstantValue))
                    .map(child -> child.getJsonWithData(generalInputs)).collect(Collectors.joining(",", "{", "}"));
            var allOnlyValueChildren = children.stream().filter(a -> (a instanceof ConstantValue))
                    .map(child -> child.getJsonWithData(generalInputs)).filter(entry -> !"".equals(entry)).collect(Collectors.joining(",", "\"", "\""));
            final String entries = createEntries(allKeyValueChildren, allOnlyValueChildren);
            return entries;
        }
    }

    private String createEntries(String allKeyValueChildren, String allOnlyValueChildren) {
        var entries = String.join(",", List.of(allOnlyValueChildren, allKeyValueChildren));
        if ("{}".equals(allKeyValueChildren) && "\"\"".equals(allOnlyValueChildren)){
            entries = "";
        }else{
            if("{}".equals(allKeyValueChildren)){
                entries = allOnlyValueChildren;
            }
            if("\"\"".equals(allOnlyValueChildren)){
                entries = allKeyValueChildren;
            }
        }
        return String.format("\"%s\" : [ %s ]", this.getKey(), entries);
    }
}
