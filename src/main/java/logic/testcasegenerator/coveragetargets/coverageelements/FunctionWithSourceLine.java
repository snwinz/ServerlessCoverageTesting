package logic.testcasegenerator.coveragetargets.coverageelements;

import shared.model.NodeType;
import logic.model.NodeModel;
import logic.model.SourceCodeLine;

import java.util.Objects;

public class FunctionWithSourceLine {
    NodeModel function;
    SourceCodeLine sourceCodeLine;

    public FunctionWithSourceLine(NodeModel function,
                                  SourceCodeLine sourceCodeLine) {
        Objects.requireNonNull(function);
        Objects.requireNonNull(sourceCodeLine);

        if (!NodeType.FUNCTION.equals(function.getType())) {
            throw new IllegalArgumentException(String.format("node %s with id %d is not a function",
                    function.getNameOfNode(), function.getIdentifier()));
        }
        this.function = function;
        this.sourceCodeLine = sourceCodeLine;
    }

    public NodeModel getFunction() {
        return function;
    }

    public SourceCodeLine getSourceCodeLine() {
        return sourceCodeLine;
    }

    @Override
    public String toString() {
        return String.format("%s with name %s (id %d) with sourcecode %s",
                function.getType(), function.getNameOfNode(), function.getIdentifier(), sourceCodeLine.getSourceLine());
    }


    public String getLogMessage() {
        return "";
    }

    public String getCoverageTargetDescription() {
        return "";
    }
}
