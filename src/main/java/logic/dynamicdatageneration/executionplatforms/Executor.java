package logic.dynamicdatageneration.executionplatforms;

import java.util.List;

public interface Executor {
    String invokeFunction(String functionName, String jsonData);

    List<String> getAllNewLogs(long startTime);

    void deleteOldLogs();
}
