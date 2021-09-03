package gui.model;

import com.google.gson.annotations.Expose;

import java.util.List;

public class SourceCodeLine {
    @Expose
    private String sourceLine;
    @Expose
    private String defContainer;
    @Expose
    private List<Long> relationsInfluencedByDef;
    @Expose
    private String use;
    @Expose
    private List<Long> relationsInfluencingUse;
    @Expose
    private List<Long> nodesCoveredByStatement;
    @Expose
    private List<Long> relationsCoveredByStatement;
    @Expose
    Long lineNumber;
    @Expose
    private String replaceLine;

    public String getSourceLine() {
        return sourceLine;
    }

    public void setSourceLine(String sourceLine) {
        this.sourceLine = sourceLine;
    }


    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }


    @Override
    public String toString() {
        return
                "''" + sourceLine + "''" +
                        ", def tracker='" + defContainer + '\'' +
                        ", relations Influenced by" + "Def=" + relationsInfluencedByDef +
                        ", use='" + use + '\'' +
                        ", relations Influencing Use=" + relationsInfluencingUse +
                        ", nodes Called by Statement=" + nodesCoveredByStatement +
                        ", relations Called by Statement=" + relationsCoveredByStatement +
                        ", lineNumber=" + lineNumber;
    }


    public void setRelationsInfluencedByDef(List<Long> relationsInfluencedByDef) {
        this.relationsInfluencedByDef = relationsInfluencedByDef;
    }


    public void setRelationsInfluencingUse(List<Long> relationsInfluencingUse) {
        this.relationsInfluencingUse = relationsInfluencingUse;
    }

    public void setNodesCoveredByStatement(List<Long> nodesCoveredByStatement) {
        this.nodesCoveredByStatement = nodesCoveredByStatement;
    }

    public void setRelationsCoveredByStatement(List<Long> relationsCoveredByStatement) {
        this.relationsCoveredByStatement = relationsCoveredByStatement;
    }

    public void removeRelationsInContextWithDefOrUseOrStatement(Long id) {
        if (relationsInfluencedByDef != null) {
            this.relationsInfluencedByDef.remove(id);
        }
        if (relationsInfluencingUse != null) {
            this.relationsInfluencingUse.remove(id);
        }
        if (relationsCoveredByStatement != null) {
            this.relationsCoveredByStatement.remove(id);
        }
    }

    public List<Long> getRelationsInfluencedByDef() {
        return relationsInfluencedByDef;
    }

    public List<Long> getRelationsInfluencingUse() {
        return relationsInfluencingUse;
    }

    public Long getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Long lineNumber) {
        this.lineNumber = lineNumber;
    }

    public List<Long> getNodesCoveredByStatement() {
        return nodesCoveredByStatement;
    }

    public List<Long> getRelationsCoveredByStatement() {
        return relationsCoveredByStatement;
    }


    public void setDefContainer(String jsonKey) {
        this.defContainer = jsonKey;
    }

    public String getDefContainer() {
        return defContainer;
    }

    public String getReplaceLine() {
        return replaceLine;
    }

    public void setReplaceLine(String replaceLine) {
        this.replaceLine = replaceLine;
    }
}
