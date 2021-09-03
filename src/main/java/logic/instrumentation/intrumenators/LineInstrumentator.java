package logic.instrumentation.intrumenators;

import logic.model.SourceCode;

public interface LineInstrumentator {

    void addLogToLine(SourceCode sourceCode);
}
