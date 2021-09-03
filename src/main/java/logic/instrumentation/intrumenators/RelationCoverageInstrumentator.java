package logic.instrumentation.intrumenators;

import logic.model.LogLine;
import logic.model.SourceCode;
import logic.model.SourceCodeLine;

import java.util.ArrayList;
import java.util.List;

import static logic.testcasegenerator.coveragetargets.LogNameConfiguration.LOGDELIMITER;
import static logic.testcasegenerator.coveragetargets.LogNameConfiguration.RELATION_MARKER;

public class RelationCoverageInstrumentator implements LineInstrumentator {

    @Override
    public void addLogToLine(SourceCode sourceCode) {
        var allLines = sourceCode.getSourceCode();
        for (var sourceCodeLine : allLines) {
            List<Long> relationsCoveredByStatement = sourceCodeLine.getRelationsCoveredByStatement();
            if (relationsCoveredByStatement != null && !relationsCoveredByStatement.isEmpty()) {
                for (Long relationCovered : relationsCoveredByStatement) {
                    String logMessage = String.format("%s%s%s", RELATION_MARKER, relationCovered, LOGDELIMITER);
                    String commentForTarget = String.format("Coverage of Relation %s; The following line should be logged: %s", relationCovered, logMessage);
                    var logLine = new LogLine(commentForTarget);
                    logLine.setLogMessage(logMessage);
                    sourceCodeLine.addBeforeLine(logLine);
                }
            }
        }
    }
}
