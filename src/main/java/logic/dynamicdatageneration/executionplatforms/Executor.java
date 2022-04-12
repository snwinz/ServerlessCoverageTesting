package logic.dynamicdatageneration.executionplatforms;

import java.util.List;
import java.util.Map;

public interface Executor {
    String invokeFunction(String functionName, String jsonData, Map<String, List<String>> outputValues);

    List<String> getAllNewLogs(long startTime);

    void deleteOldLogs();

    void resetApplication(String resetFunctionName);
}
