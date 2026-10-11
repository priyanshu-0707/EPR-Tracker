package com.eprtracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;
import software.amazon.awssdk.services.location.LocationClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sns.SnsClient;

import java.net.URI;

@Configuration
public class AwsConfig {

    private final String awsRegion;
    private final String dynamoDbEndpoint;

    public AwsConfig(
            @Value("${aws.region:ap-south-1}") String awsRegion,
            @Value("${aws.dynamodb.endpoint:}") String dynamoDbEndpoint) {
        this.awsRegion = awsRegion;
        this.dynamoDbEndpoint = dynamoDbEndpoint;
    }

    private Region getRegion() {
        return Region.of(awsRegion);
    }

    private software.amazon.awssdk.auth.credentials.AwsCredentialsProvider getCredentialsProvider() {
        try {
            DefaultCredentialsProvider defaultProvider = DefaultCredentialsProvider.create();
            defaultProvider.resolveCredentials();
            return defaultProvider;
        } catch (Exception e) {
            // Development/Sandbox fallback credentials
            return software.amazon.awssdk.auth.credentials.StaticCredentialsProvider.create(
                    software.amazon.awssdk.auth.credentials.AwsBasicCredentials.create("test", "test")
            );
        }
    }

    @Bean
    public DynamoDbClient dynamoDbClient() {
        DynamoDbClientBuilder builder = DynamoDbClient.builder()
                .region(getRegion())
                .credentialsProvider(getCredentialsProvider());

        if (dynamoDbEndpoint != null && !dynamoDbEndpoint.isBlank()) {
            builder.endpointOverride(URI.create(dynamoDbEndpoint));
        }

        return builder.build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(getRegion())
                .credentialsProvider(getCredentialsProvider())
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(getRegion())
                .credentialsProvider(getCredentialsProvider())
                .build();
    }

    @Bean
    public BedrockRuntimeClient bedrockRuntimeClient() {
        return BedrockRuntimeClient.builder()
                .region(getRegion())
                .credentialsProvider(getCredentialsProvider())
                .build();
    }

    @Bean
    public SnsClient snsClient() {
        return SnsClient.builder()
                .region(getRegion())
                .credentialsProvider(getCredentialsProvider())
                .build();
    }

    @Bean
    public LocationClient locationClient() {
        return LocationClient.builder()
                .region(getRegion())
                .credentialsProvider(getCredentialsProvider())
                .build();
    }
}
