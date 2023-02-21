package logic.executionplatforms;

import java.util.List;
import java.util.Map;

public interface Executor {
    String invokeFunction(String functionName, String jsonData, Map<String, List<String>> outputValues, List<String> authValues);

    List<String> getAllNewLogs(long startTime);

    void deleteOldLogs();

    String resetApplication(String resetFunctionName);

    String callResetFunction(String resetFunctionName);

    void setEnvironmentVariables(List<String> functions, Map<String, String> envVariables);
}
