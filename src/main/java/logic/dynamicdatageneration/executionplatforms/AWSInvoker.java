package logic.dynamicdatageneration.executionplatforms;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import com.amazonaws.services.logs.model.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AWSInvoker implements Executor {
    protected AWSLambda amazonLambda;
    protected AWSLogs amazonLogs;

    public AWSInvoker(String region) {
        this.amazonLambda = AWSLambdaClientBuilder.standard().withRegion(region).build();
        this.amazonLogs = AWSLogsClientBuilder.standard().withRegion(region).build();
    }


    @Override
    public String invokeFunction(String functionName, String json) {
        InvokeRequest invokeRequest = new InvokeRequest()
                .withFunctionName(functionName);
        json = json.replaceAll("'", "\"");
        invokeRequest.setPayload(json);

        InvokeResult invokeResult = amazonLambda.invoke(invokeRequest);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
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


}
