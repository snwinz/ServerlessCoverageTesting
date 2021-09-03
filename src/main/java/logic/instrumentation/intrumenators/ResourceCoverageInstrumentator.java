package logic.instrumentation.intrumenators;

import logic.model.LogLine;
import logic.model.SourceCode;
import logic.model.SourceCodeLine;

import java.util.ArrayList;
import java.util.List;

import static logic.testcasegenerator.coveragetargets.LogNameConfiguration.LOGDELIMITER;
import static logic.testcasegenerator.coveragetargets.LogNameConfiguration.RESOURCE_MARKER;

public class ResourceCoverageInstrumentator implements LineInstrumentator {

    @Override
    public void addLogToLine(SourceCode sourceCode) {
        var allLines = sourceCode.getSourceCode();
        for (var sourceCodeLine : allLines) {
            List<Long> nodesCalledByStatement = sourceCodeLine.getNodesCoveredByStatement();
            if (nodesCalledByStatement != null && !nodesCalledByStatement.isEmpty()) {
                for (Long nodeCovered : nodesCalledByStatement) {
                    String logMessage = RESOURCE_MARKER + nodeCovered + LOGDELIMITER;
                    String commentForTarget = String.format("Coverage of Node %s; The following line should be logged: %s", nodeCovered, logMessage);
                    var logLine = new LogLine(commentForTarget);
                    logLine.setLogMessage(logMessage);
                    sourceCodeLine.addBeforeLine(logLine);
                }
            }
        }
    }
}
