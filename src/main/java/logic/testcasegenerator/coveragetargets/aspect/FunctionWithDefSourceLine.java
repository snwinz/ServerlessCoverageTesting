package logic.testcasegenerator.coveragetargets.aspect;

import logic.model.NodeModel;
import logic.model.SourceCodeLine;

import java.util.Objects;

public class FunctionWithDefSourceLine extends FunctionWithSourceLine {
    public FunctionWithDefSourceLine(NodeModel function, SourceCodeLine sourceCodeLine) {
        super(function, sourceCodeLine);
        Objects.requireNonNull(sourceCodeLine.getRelationsInfluencedByDef());
    }

    @Override
    public String toString() {
        return String.format("%s with name %s (id %d) and definition coverage of the corresponding variable '%s'",
                function.getType(), function.getNameOfNode(), function.getIdentifier(), sourceCodeLine.getDefTracker("", function.getIdentifier()));
    }

    @Override
    public String getAspectMessage() {
        return String.format("%s",
                this.getSourceCodeLine().getDefTracker("", this.getFunction().getIdentifier()));
    }

    @Override
    public String getLogMessage() {
        return String.format("%s",
                this.getSourceCodeLine().getDefTracker("", this.getFunction().getIdentifier()));
    }


    @Override
    public String getAspectTarget() {
        return String.format("Coverage of Definition %s with any use", this.getSourceCodeLine().getDefTracker("", this.getFunction().getIdentifier()));
    }


}
