package logic.testcasegenerator.coveragetargets.coverageelements;

import logic.model.NodeModel;
import logic.model.SourceCodeLine;

import java.util.Objects;

public class FunctionWithUseSourceLine extends FunctionWithSourceLine {
    public FunctionWithUseSourceLine(NodeModel function, SourceCodeLine sourceCodeLine) {
        super(function, sourceCodeLine);
        Objects.requireNonNull(sourceCodeLine.getUse());
        Objects.requireNonNull(sourceCodeLine.getRelationsInfluencingUse());
    }

    @Override
     public String toString() {
        return String.format("%s with name %s (id %d) and use coverage of the corresponding variable: %s",
                function.getType(), function.getNameOfNode(), function.getIdentifier(), sourceCodeLine.getUseTracker("", function.getIdentifier()));
    }

    @Override
    public String getCoverageTargetDescription(){
        return String.format("Coverage of Usage %s with any definition", this.getSourceCodeLine().getUseTracker("",this.getFunction().getIdentifier()));
    }
    @Override
    public String getLogMessage() {
        return String.format("%s",  this.getSourceCodeLine().getUseTracker("",this.getFunction().getIdentifier()));
    }


    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof FunctionWithUseSourceLine functionWithUseSourceLine)) {
            return false;
        }
        return functionWithUseSourceLine.function.equals(this.function) && functionWithUseSourceLine.sourceCodeLine.equals(this.sourceCodeLine);
    }
}
