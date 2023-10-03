package com.aws.demo;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        //System.out.println("processing started");
        //String arn = "arn:aws:iam::702162754193:role/dp-role-fed-gen-dbk-activation-prod";
        //String arn = "arn:aws:iam::716915692812:role/dtci-admin";
        ConfigurableApplicationContext ctx = SpringApplication.run(DemoApplication.class, args);
        //ApplicationContext applicationContext = SpringApplication.run(DemoApplication.class, args);
        //Service service = ctx.getBean(Service.class);
        //List<Bucket> buckets = service.listBuckets(arn);
        //System.out.println(buckets);
       // System.out.println("processing closed");
       // ctx.close();
    }
    @Bean
    public AmazonS3 amazonS3() {
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .withRegion(Regions.US_EAST_1)
                .build();
    }
}
