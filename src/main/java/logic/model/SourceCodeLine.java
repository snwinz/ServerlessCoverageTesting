package logic.model;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

import static logic.testcasegenerator.coveragetargets.LogNameConfiguration.*;

public class SourceCodeLine {

    public static final long INFLUENCING_ALL_RELATIONS_CONSTANT = Long.MIN_VALUE;

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

    private final List<LogLine> afterLines = new ArrayList<>();
    private final List<LogLine> beforeLines = new ArrayList<>();

    public String getSourceLine() {
        return sourceLine;
    }

    public String getSourceLineWithLogLine() {
        StringBuilder result = new StringBuilder();
        for (var beforeLine : beforeLines) {
            if (beforeLine != null) {
                result.append(beforeLine);
                result.append("\n");
            }
        }
        result.append(sourceLine);
        for (var afterLine : afterLines) {
            if (afterLine != null) {
                result.append("\n");
                result.append(afterLine);
            }
        }
        return result.toString();
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


    public List<Long> getNodesCoveredByStatement() {
        return nodesCoveredByStatement;
    }

    public void setNodesCoveredByStatement(List<Long> nodesCoveredByStatement) {
        this.nodesCoveredByStatement = nodesCoveredByStatement;
    }

    public void addNodeCoveredByStatement(Long nodeID) {
        if (nodesCoveredByStatement == null) {
            nodesCoveredByStatement = new ArrayList<>();
        }
        nodesCoveredByStatement.add(nodeID);
    }


    public void setRelationsCoveredByStatement(List<Long> relationsCoveredByStatement) {
        this.relationsCoveredByStatement = relationsCoveredByStatement;
    }

    public List<Long> getRelationsInfluencedByDef() {
        return relationsInfluencedByDef;
    }

    public void setRelationsInfluencedByDef(List<Long> relationsInfluencedByDef) {
        this.relationsInfluencedByDef = relationsInfluencedByDef;
    }

    public List<Long> getRelationsInfluencingUse() {
        return relationsInfluencingUse;
    }

    public void setRelationsInfluencingUse(List<Long> relationsInfluencingUse) {
        this.relationsInfluencingUse = relationsInfluencingUse;
    }

    @Override
    public String toString() {
        return "SourceEntry{" +
                "sourceLine='" + sourceLine + '\'' +
                ", def tracker='" + defContainer + '\'' +
                ", use='" + use + '\'' +
                '}';
    }

    public List<Long> getRelationsCoveredByStatement() {
        return relationsCoveredByStatement;
    }

    public void addRelationCoveredByStatement(Long relationID) {
        if (relationsCoveredByStatement == null) {
            relationsCoveredByStatement = new ArrayList<>();
        }
        relationsCoveredByStatement.add(relationID);
    }

    public Long getLineNumber() {
        return this.lineNumber;
    }

    public void setLineNumber(Long lineNumber) {
        this.lineNumber = lineNumber;
    }


    public void addBeforeLine(LogLine beforeLine) {
        beforeLines.add(beforeLine);
    }

    public void addAfterLine(LogLine afterLine) {
        afterLines.add(afterLine);
    }

    public void setDefKey(String defContainerVariable) {
        this.defContainer = defContainerVariable;
    }

    public String getDefContainer() {
        return defContainer;
    }


    public String getUseTracker(String variableName, Long idOfNode) {
        return String.format("%s%d_line%d_%s%s", USELOG_MARKER, idOfNode, this.getLineNumber(), variableName, LOGDELIMITER);
    }

    public String getDefTracker(String variableName, Long idOfNode) {
        return String.format("%s%d_line%d_%s%s", DEFLOG_MARKER, idOfNode, this.getLineNumber(), variableName, LOGDELIMITER);
    }

    public void setReplaceLine(String replaceLine) {
        this.replaceLine = replaceLine;
    }

    public String getReplaceLine() {
        return replaceLine;
    }
}
