package logic.executionplatforms;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.*;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import com.amazonaws.services.logs.model.*;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static shared.model.StringSeparators.*;

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
        json = applyBase64(json);

        invokeRequest.setPayload(json);

        InvokeResult invokeResult = amazonLambda.invoke(invokeRequest);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
    }

    private String applyBase64(String json) {
        int indexStart = json.indexOf(BASE_64_PREFIX);
        int indexEnd = json.indexOf(BASE_64_SUFFIX);
        while (indexStart > 0 && indexEnd > 0 && indexEnd > indexStart) {
            String value = json.substring(BASE_64_PREFIX.length() + indexStart, indexEnd);

            value = Base64.getEncoder().encodeToString(value.getBytes());

            json = json.substring(0, indexStart) + value + json.substring(indexEnd + BASE_64_SUFFIX.length());
            indexStart = json.indexOf(BASE_64_PREFIX);
            indexEnd = json.indexOf(BASE_64_SUFFIX);
        }
        return json;
    }

    private String setOutputsOfPreviousFunctions(String json, Map<String, List<String>> outputValues) {
        while (isValidReferenceToPreviousContent(json)) {
            String key = getKeyOfPreviousContent(json);
            int occurrence = getOccurrencesOfPreviousContent(json);
            if (outputValues.containsKey(key) && outputValues.get(key).size() > occurrence) {
                String value = outputValues.get(key).get(occurrence);
                var jsonFirstPart = getPartBeforeSeparators(json);
                var jsonSecondPart = getPartAfterSeparators(json);
                json = jsonFirstPart + value + jsonSecondPart;
            } else {
                break;
            }
        }
        return json;
    }

    private int getOccurrencesOfPreviousContent(String json) {
        int indexStart = json.indexOf(PREVIOUSOUTPUT_PREFIX);
        String content = json.substring(indexStart + PREVIOUSOUTPUT_PREFIX.length());
        int indexSeparator = content.indexOf(SEPARATOR)+indexStart+ PREVIOUSOUTPUT_PREFIX.length();
        int indexEnd = json.indexOf(PREVIOUSOUTPUT_SUFFIX);
        return Integer.parseInt(json.substring(indexSeparator + SEPARATOR.length(), indexEnd));
    }

    private String getKeyOfPreviousContent(String json) {
        int indexStart = json.indexOf(PREVIOUSOUTPUT_PREFIX);
        String content = json.substring(indexStart + PREVIOUSOUTPUT_PREFIX.length());
        int indexSeparator = content.indexOf(SEPARATOR)+indexStart+ PREVIOUSOUTPUT_PREFIX.length();
        return json.substring(indexStart + PREVIOUSOUTPUT_PREFIX.length(), indexSeparator);
    }

    private boolean isValidReferenceToPreviousContent(String json) {
        int indexStart = json.indexOf(PREVIOUSOUTPUT_PREFIX);
        if (indexStart == -1) {
            return false;
        }
        String content = json.substring(indexStart + PREVIOUSOUTPUT_PREFIX.length());
        int indexSeparator = content.indexOf(SEPARATOR)+indexStart+ PREVIOUSOUTPUT_PREFIX.length();
        int indexEnd = json.indexOf(PREVIOUSOUTPUT_SUFFIX);
        return indexStart < indexSeparator && indexSeparator < indexEnd;
    }

    private String getPartBeforeSeparators(String json) {
        int indexStart = json.indexOf(PREVIOUSOUTPUT_PREFIX);
        return json.substring(0, indexStart);
    }

    private String getPartAfterSeparators(String json) {
        int indexEnd = json.indexOf(PREVIOUSOUTPUT_SUFFIX) + PREVIOUSOUTPUT_SUFFIX.length();
        return json.substring(indexEnd);
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
            Map<String, String> currentVariables = (environmentResponse == null) ? new HashMap<>() : environmentResponse.getVariables();
            currentVariables.putAll(envVariables);
            Environment environment = new Environment();
            environment.setVariables(currentVariables);
            configuration.setEnvironment(environment);
            amazonLambda.updateFunctionConfiguration(configuration);
        }
    }

}
