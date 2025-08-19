package com.aditya2254.ecommerceapp.productservice.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutBucketPolicyRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.net.URI;

@Component
public class MinioBucketConfigurator implements ApplicationRunner {

    @Value("${app.images.s3.endpoint}")
    private String endpoint;

    @Value("${app.images.s3.region:us-east-1}")
    private String region;

    @Value("${app.images.s3.bucket}")
    private String bucket;

    @Value("${aws.accessKeyId}")
    private String accessKey;

    @Value("${aws.secretAccessKey}")
    private String secretKey;

    @Value("${app.images.s3.path-style-access:true}")
    private boolean pathStyle;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        S3Configuration s3Config = S3Configuration.builder()
                .pathStyleAccessEnabled(pathStyle)
                .build();

        S3Client s3 = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .serviceConfiguration(s3Config)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();

        ensureBucketExists(s3);
        applyPublicReadPolicy(s3);

        s3.close();
    }

    private void ensureBucketExists(S3Client s3) {
        try {
            HeadBucketRequest headReq = HeadBucketRequest.builder().bucket(bucket).build();
            s3.headBucket(headReq);
            System.out.println("Bucket already exists: " + bucket);
        } catch (S3Exception e) {
            // If HTTP status 404 => bucket not found; otherwise rethrow
            int status = e.statusCode();
            if (status == 404) {
                System.out.println("Bucket does not exist, creating: " + bucket);
                CreateBucketRequest createReq = CreateBucketRequest.builder()
                        .bucket(bucket)
                        .build();
                s3.createBucket(createReq);
                // wait until it's available (optional)
                s3.waiter().waitUntilBucketExists(HeadBucketRequest.builder().bucket(bucket).build());
                System.out.println("Bucket created: " + bucket);
            } else {
                // authentication/permission/network problems -> rethrow so startup fails
                throw e;
            }
        }
    }

    private void applyPublicReadPolicy(S3Client s3) {
        // Public read policy (only GetObject)
        String policyJson = "{\n" +
                "  \"Version\":\"2012-10-17\",\n" +
                "  \"Statement\":[\n" +
                "    {\n" +
                "      \"Effect\":\"Allow\",\n" +
                "      \"Principal\": {\"AWS\": [\"*\"]},\n" +
                "      \"Action\":[\"s3:GetObject\"],\n" +
                "      \"Resource\":[\"arn:aws:s3:::" + bucket + "/*\"]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        PutBucketPolicyRequest pbp = PutBucketPolicyRequest.builder()
                .bucket(bucket)
                .policy(policyJson)
                .build();
        s3.putBucketPolicy(pbp);
        System.out.println("Public-read policy applied to bucket: " + bucket);
    }
}
