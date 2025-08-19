package com.aditya2254.ecommerceapp.productservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.utils.StringUtils;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${app.images.s3.region}")
    private String region;
    
    @Value("${app.images.s3.endpoint:}")
    private String endpoint;
    
    @Value("${aws.access-key-id:}")
    private String accessKeyId;
    
    @Value("${aws.secret-access-key:}")
    private String secretAccessKey;
    
    @Value("${app.images.s3.path-style-access:false}")
    private boolean pathStyleAccess;

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        if (!StringUtils.isBlank(accessKeyId) && !StringUtils.isBlank(secretAccessKey)) {
            // Use explicit credentials from application.properties
            return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKeyId, secretAccessKey)
            );
        } else {
            // Fall back to default credential provider chain
            return DefaultCredentialsProvider.create();
        }
    }

    @Bean
    S3Client s3Client(AwsCredentialsProvider credentialsProvider) {
        var builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .httpClient(UrlConnectionHttpClient.builder().build())
                .overrideConfiguration(ClientOverrideConfiguration.builder().build());
        
        // Configure for MinIO or custom endpoint
        if (!StringUtils.isBlank(endpoint)) {
            builder = builder.endpointOverride(URI.create(endpoint));
            
            // Enable path-style access for MinIO compatibility
            builder = builder.serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(pathStyleAccess)
                    .build()
            );
        }
        
        return builder.build();
    }

    @Bean
    S3Presigner s3Presigner(AwsCredentialsProvider credentialsProvider) {
        S3Presigner.Builder builder = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider);
                
        if (!StringUtils.isBlank(endpoint)) {
            builder = builder.endpointOverride(URI.create(endpoint));
        }
        
        return builder.build();
    }
}