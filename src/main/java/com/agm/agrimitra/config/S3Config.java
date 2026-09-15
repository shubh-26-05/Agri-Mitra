package com.agm.agrimitra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.access-key-id}")
    private String accessKeyId;

    @Value("${aws.secret-access-key}")
    private String secretAccessKey;

    @Bean
    public S3Client s3Client() {
        String accessKey = (accessKeyId != null && !accessKeyId.isBlank()) ? accessKeyId : "placeholder-access-key";
        String secretKey = (secretAccessKey != null && !secretAccessKey.isBlank()) ? secretAccessKey : "placeholder-secret-key";
        Region awsRegion = (region != null && !region.isBlank()) ? Region.of(region) : Region.US_EAST_1;

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
                .region(awsRegion)
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}
