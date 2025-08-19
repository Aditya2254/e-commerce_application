package com.aditya2254.ecommerceapp.productservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
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

    @Bean
    S3Client s3Client() {
        var b = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .httpClient(UrlConnectionHttpClient.builder().build())
                .overrideConfiguration(ClientOverrideConfiguration.builder().build());
        if (!StringUtils.isBlank(endpoint)) {
            b = b.endpointOverride(URI.create(endpoint));
        }
        return b.build();
    }

    @Bean
    S3Presigner s3Presigner() {
        S3Presigner.Builder b = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create());
        if (!StringUtils.isBlank(endpoint)) {
            b = b.endpointOverride(URI.create(endpoint));
        }
        return b.build();
    }
}
