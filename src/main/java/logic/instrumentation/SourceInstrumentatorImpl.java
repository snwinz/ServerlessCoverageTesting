package logic.instrumentation;

import logic.instrumentation.intrumenators.DefUsePairCoverageInstrumentator;
import logic.instrumentation.intrumenators.LineInstrumentator;
import logic.instrumentation.intrumenators.RelationCoverageInstrumentator;
import logic.instrumentation.intrumenators.ResourceCoverageInstrumentator;
import logic.model.SourceCode;
import logic.model.SourceCodeLine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SourceInstrumentatorImpl implements SourceInstrumentator {


    @Override
    public String instrumentSourceCode(String sourceAsJSON, boolean resources, boolean relations, boolean defusepairs) {
        List<LineInstrumentator> instrumentators = new ArrayList<>();
        if (resources) {
            instrumentators.add(new ResourceCoverageInstrumentator());
        }
        if (relations) {
            instrumentators.add(new RelationCoverageInstrumentator());
        }
        if (defusepairs) {
            instrumentators.add(new DefUsePairCoverageInstrumentator());
        }
        SourceCode sourceCode = SourceCode.getSourceCodeObject(sourceAsJSON);

        for (var instrumentator : instrumentators) {
            instrumentator.addLogToLine(sourceCode);
        }

        return sourceCode.getSourceCode().stream().map(SourceCodeLine::getSourceLineWithLogLine).collect(Collectors.joining("\n"));
    }
}
