package logic.model;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.util.List;

public class SourceCode {
    @Expose
    private final List<SourceCodeLine> sourceCode;

    @Expose
    private Long idOfNode;

    public SourceCode(List<SourceCodeLine> sourceCode) {
        this.sourceCode = sourceCode;
    }

    public List<SourceCodeLine> getSourceCode() {
        return sourceCode;
    }

    public Long getIdOfNode() {
        return idOfNode;
    }

    public void setIdOfNode(Long idOfNode) {
        this.idOfNode = idOfNode;
    }

    public static SourceCode getSourceCodeObject(String sourceJSON) {
        var gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.fromJson(sourceJSON, SourceCode.class);
    }
    public String getJSON() {
        var gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation().setPrettyPrinting()
                .create();
        return gson.toJson(this, SourceCode.class);
    }

}
