package logic.instrumentation.intrumenators;

import logic.model.LogLine;
import logic.model.SourceCode;

public class DefUsePairCoverageInstrumentator implements LineInstrumentator {


    @Override
    public void addLogToLine(SourceCode sourceCode) {
        var allLines = sourceCode.getSourceCode();
        for (var sourceCodeLine : allLines) {
            if (sourceCodeLine.getDefContainer() != null) {

                String commentForTarget = String.format(
                        "The definition variable should contain the following identifier in order to " +
                                "track the source when used: %s", sourceCodeLine.getDefTracker("", sourceCode.getIdOfNode()));

                var logLine = new LogLine(commentForTarget);
                logLine.setBeforeLogStatement(sourceCodeLine.getDefContainer());
                sourceCodeLine.addAfterLine(logLine);
            }
            if (sourceCodeLine.getUse() != null) {
                String commentForTarget = String.format("Coverage of a definition with a use. " +
                                "The following line should log that the definition of the variable was used by " +
                                "logging the definition in combination with the following use: %s",
                        sourceCodeLine.getUseTracker("", sourceCode.getIdOfNode()));
                var logLine = new LogLine(commentForTarget);
                logLine.setBeforeLogStatement(sourceCodeLine.getUse());
                sourceCodeLine.addBeforeLine(logLine);
            }
            if (sourceCodeLine.getReplaceLine() != null) {
                String commentForTarget = String.format("Old line '%s' is replaced by %n '%s'",
                        sourceCodeLine.getSourceLine(), sourceCodeLine.getReplaceLine());
                var logLine = new LogLine(commentForTarget);
                sourceCodeLine.addBeforeLine(logLine);
                sourceCodeLine.setSourceLine(sourceCodeLine.getReplaceLine());
            }
        }
    }
}
