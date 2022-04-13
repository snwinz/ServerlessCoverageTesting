package logic.executionplatforms;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.logs.AWSLogsClientBuilder;

public class AWSLocalInvoker extends AWSInvoker {

    public AWSLocalInvoker(String region) {
        super(region);
        var endpointConfiguration = new AwsClientBuilder.EndpointConfiguration("http://localhost:4566", "eu-central-1");
        amazonLambda = AWSLambdaClientBuilder.standard().withEndpointConfiguration(endpointConfiguration).build();
        amazonLogs = AWSLogsClientBuilder.standard().withEndpointConfiguration(endpointConfiguration).build();
    }

}
