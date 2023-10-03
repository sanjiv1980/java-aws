package com.aws.demo;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class Controller {
    @Autowired
    private Service service;
    @Autowired
    private AmazonS3 amazonS3;
    @Value("${s3.bucket.name}")
    private String s3BucketName;

    @Value("${s3.output.filename}")
    private String filename;
    @Value("${app.assume.role}")
    private String appAssumeRole;

    @GetMapping("/list-buckets")
    public List<Bucket> listBuckets(@RequestParam("arn") String arn) {
        return service.listBuckets(arn);
    }
    private String generateUrl(String fileName, HttpMethod httpMethod) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, 1); // Generated URL will be valid for 24 hours
        return amazonS3.generatePresignedUrl(s3BucketName, fileName, calendar.getTime(), httpMethod).toString();
    }
    @GetMapping("/presigned-url")
    public String save() {
        String fileName = UUID.randomUUID().toString() + ".pdf";
        return generateUrl(fileName, HttpMethod.PUT);
    }
    @PostMapping("/upload-json")
    public String saveJson(@RequestBody User user){
        String payload = getRequestJson(user);
        if(payload==null){
            System.out.println("payload json has null generation.!");
        }

        DecimalFormat mFormat= new DecimalFormat("00");
        Calendar c  = Calendar.getInstance();
        String year = mFormat.format(Double.valueOf(c.get(Calendar.YEAR)));
        String month = mFormat.format(Double.valueOf(c.get(Calendar.MONTH)));
        String day = mFormat.format(Double.valueOf(c.get(Calendar.DAY_OF_MONTH)));

        InputStream PayloadStream = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
        String objectKey = "user-output" + "/year="+year + "/month="+month+"/day="+day+"/" + filename;
        AmazonS3 s3Client = getS3Client(appAssumeRole);
        //PutObjectResult result = s3Client.putObject(sparkOutputBucket, objectKey, payload);
        PutObjectRequest request = new PutObjectRequest(s3BucketName, objectKey, PayloadStream, new ObjectMetadata()).withCannedAcl(CannedAccessControlList.BucketOwnerFullControl);
        PutObjectResult result = s3Client.putObject(request);
        //log.info("File has been successfully sent to s3 location: "+ sparkOutputBucket +"/" +  objectKey);
        return  s3BucketName + "/" + objectKey;
    }

    private AmazonS3 getS3Client(String assumeRoleArn){

       /* Arn arn = Arn.fromString(arnString);
        AssumedRoleCredentials assumeRole = assumeRole(arn);
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .withCredentials(new AWSStaticCredentialsProvider(assumeRole))
                .withForceGlobalBucketAccessEnabled(true)
                .build();
        return s3Client;*/

        BasicSessionCredentials assumeRole = getAwsAssumeRoleCredentials(assumeRoleArn);
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .withCredentials(new AWSStaticCredentialsProvider(assumeRole))
                .withForceGlobalBucketAccessEnabled(true)
                .build();
        return s3Client;
    }

    private BasicSessionCredentials getAwsAssumeRoleCredentials(String assumeRoleArn){
        String roleSessionName="nielson-session";
        AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard()
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .withRegion(Regions.US_EAST_1)
                .build();
        // Obtain credentials for the IAM role. Note that you cannot assume the role of an AWS root account;
        // Amazon S3 will deny access. You must use credentials for an IAM user or an IAM role.
        AssumeRoleRequest roleRequest = new AssumeRoleRequest()
                .withRoleArn(assumeRoleArn)
                .withRoleSessionName(roleSessionName);
        AssumeRoleResult roleResponse = stsClient.assumeRole(roleRequest);
        Credentials sessionCredentials = roleResponse.getCredentials();
        // Create a BasicSessionCredentials object that contains the credentials you just retrieved.
        BasicSessionCredentials awsCredentials = new BasicSessionCredentials(
                sessionCredentials.getAccessKeyId(),
                sessionCredentials.getSecretAccessKey(),
                sessionCredentials.getSessionToken());

        return awsCredentials;
    }
    private String getRequestJson(User user) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.createObjectNode();
        ((ObjectNode) rootNode).put("userId", user.getUserId());
        ((ObjectNode) rootNode).put("userName", user.getUserName());
        ((ObjectNode) rootNode).put("email", user.getEmail());
        ((ObjectNode) rootNode).put("phone", user.getPhone());
        ((ObjectNode) rootNode).put("address", user.getAddress());

        String jsonString = null;
        try {
            jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonString;
    }

}
