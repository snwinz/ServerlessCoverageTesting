package logic.testcasegenerator.coveragetargets.aspect;

import logic.model.NodeModel;
import logic.model.SourceCodeLine;

import java.util.Objects;

import static logic.testcasegenerator.coveragetargets.LogNameConfiguration.*;

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
    public String getAspectMessage() {
        return String.format("#%s%s%s%s", DEFLOG_MARKER,USELOG_MARKER, this.getSourceCodeLine().getUseTracker("",this.getFunction().getIdentifier()),LOGDELIMITER);
    }
    @Override
    public String getAspectTarget(){
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
        if (!(o instanceof FunctionWithUseSourceLine)) {
            return false;
        }
        FunctionWithUseSourceLine functionWithUseSourceLine = (FunctionWithUseSourceLine) o;
        return functionWithUseSourceLine.function.equals(this.function) && functionWithUseSourceLine.sourceCodeLine.equals(this.sourceCodeLine);
    }
}
