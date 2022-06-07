package logic.executionplatforms;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.*;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import com.amazonaws.services.logs.model.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AWSInvoker implements Executor {
    protected AWSLambda amazonLambda;
    protected AWSLogs amazonLogs;

    public AWSInvoker(String region) {
        this.amazonLambda = AWSLambdaClientBuilder.standard().withRegion(region).build();
        this.amazonLogs = AWSLogsClientBuilder.standard().withRegion(region).build();
    }


    @Override
    public String invokeFunction(String functionName, String json, Map<String, List<String>> outputValues) {
        InvokeRequest invokeRequest = new InvokeRequest()
                .withFunctionName(functionName);
        json = json.replaceAll("'", "\"");

        json = setOutputsOfPreviousFunctions(json, outputValues);


        invokeRequest.setPayload(json);

        InvokeResult invokeResult = amazonLambda.invoke(invokeRequest);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
    }

    private String setOutputsOfPreviousFunctions(String json, Map<String, List<String>> outputValues) {
        while (json.contains("##PREVIOUSOUTPUT__")) {
            String key = json.split("##PREVIOUSOUTPUT")[1].split("__")[1];
            var occurrence = Integer.parseInt(json.split("##PREVIOUSOUTPUT")[1].split("__")[2]);
            if (outputValues.containsKey(key) && outputValues.get(key).size() > occurrence) {
                String value = outputValues.get(key).get(occurrence);
                var jsonFirstPart = json.split("##PREVIOUSOUTPUT")[0];
                var jsonSecondPart = json.split("PREVIOUSOUTPUT##", 2)[1];
                json = jsonFirstPart + value + jsonSecondPart;
            } else {
                break;
            }
        }
        return json;
    }


    @Override
    public List<String> getAllNewLogs(long startTime) {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        List<String> result = new ArrayList<>();
        DescribeLogGroupsRequest describeLogGroupsRequest = new DescribeLogGroupsRequest();
        var logGroups = amazonLogs.describeLogGroups(describeLogGroupsRequest);
        for (var logGroup : logGroups.getLogGroups()) {
            DescribeLogStreamsRequest logRequest = new DescribeLogStreamsRequest(logGroup.getLogGroupName());
            DescribeLogStreamsResult logResponse = amazonLogs.describeLogStreams(logRequest);
            for (var logStream : logResponse.getLogStreams()) {
                if (logStream.getLastEventTimestamp() < startTime) {
                    continue;
                }
                final GetLogEventsRequest logsRequest = new GetLogEventsRequest(logGroup.getLogGroupName(), logStream.getLogStreamName());
                logsRequest.setStartTime(startTime);
                final GetLogEventsResult logEvents = this.amazonLogs.getLogEvents(logsRequest);
                for (var event : logEvents.getEvents()) {
                    result.add(event.getMessage());
                }
            }
        }
        return result;
    }

    @Override
    public void deleteOldLogs() {
        DescribeLogGroupsRequest describeLogGroupsRequest = new DescribeLogGroupsRequest();
        var logGroups = amazonLogs.describeLogGroups(describeLogGroupsRequest);
        for (var logGroup : logGroups.getLogGroups()) {
            DeleteLogGroupRequest deleteLogGroupRequest = new DeleteLogGroupRequest();
            deleteLogGroupRequest.setLogGroupName(logGroup.getLogGroupName());
            amazonLogs.deleteLogGroup(deleteLogGroupRequest);
        }
    }

    @Override
    public void resetApplication(String resetFunctionName) {
        callResetFunction(resetFunctionName);
        deleteOldLogs();
    }

    @Override
    public void callResetFunction(String resetFunctionName) {
        if (resetFunctionName != null) {
            invokeFunction(resetFunctionName, "{}", new HashMap<>());
        }
    }

    @Override
    public void setEnvironmentVariables(List<String> functions, Map<String, String> envVariables) {
        UpdateFunctionConfigurationRequest configuration = new UpdateFunctionConfigurationRequest();

        for (var functionName : functions) {
            configuration.setFunctionName(functionName);
            GetFunctionConfigurationRequest request = new GetFunctionConfigurationRequest();
            request.setFunctionName(functionName);
            var res = amazonLambda.getFunctionConfiguration(request);
            var environmentResponse = res.getEnvironment();
            var currentVariables = environmentResponse.getVariables();
            currentVariables.putAll(envVariables);
            Environment environment = new Environment();
            environment.setVariables(currentVariables);
            configuration.setEnvironment(environment);
            amazonLambda.updateFunctionConfiguration(configuration);
        }
    }

}
