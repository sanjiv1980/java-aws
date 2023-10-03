package com.aws.demo;


import com.amazonaws.arn.Arn;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AWSSecurityTokenServiceException;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@org.springframework.stereotype.Service
@Slf4j
public class Service {

    public List<Bucket> listBuckets(String arnString) {
        Arn arn = Arn.fromString(arnString);

        AssumedRoleCredentials assumeRole = assumeRole(arn);

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .withCredentials(new AWSStaticCredentialsProvider(assumeRole))
                .withForceGlobalBucketAccessEnabled(true)
                .build();

        return s3Client.listBuckets();

    }

    public AssumedRoleCredentials assumeRole(Arn arn) {
        log.trace("Assuming role: {}", arn.getResourceAsString());
        AssumedRoleCredentials assumedRoleCredentials = null;
        try {
            assumedRoleCredentials = new AssumedRoleCredentials(AWSSecurityTokenServiceClientBuilder.standard()
                    .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                    .withRegion(arn.getRegion())
                    .build()
                    .assumeRole(new AssumeRoleRequest()
                            .withRoleArn(arn.toBuilder().withRegion("").build().toString())
                            .withRoleSessionName("TEST")
                            .withDurationSeconds(900)
                    ).getCredentials()
            );
        } catch (AWSSecurityTokenServiceException e) {
            log.error("Assume role not having permission {}", e.getMessage());
        }
        return assumedRoleCredentials;
    }
}