package logic.instrumentation;

public interface SourceInstrumentator {


    String instrumentSourceCode(String sourceAsJSON, boolean resources, boolean relations, boolean defusepairs);

}
