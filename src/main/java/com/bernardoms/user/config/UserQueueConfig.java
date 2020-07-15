package com.bernardoms.user.config;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class UserQueueConfig {
    @Value("${sns.endpoint}")
    private String endpoint;
    @Value("${sns.region}")
    private String region;
    @Value("${sns.accessKey}")
    private String accessKey;
    @Value("${sns.secretKey}")
    private String secretKey;
    @Value("${sns.topic}")
    private String topic;

    @Bean
    public BasicAWSCredentials awsCredentialsProvider() {
        return new BasicAWSCredentials(accessKey, secretKey);
    }

    @Bean
    public AmazonSNSAsync amazonSnsAsync() {
        return AmazonSNSAsyncClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentialsProvider()))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                .build();
    }
}
