package com.cloud.ccwebapp.recipe.service;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AWSService {

    @Value("${aws.region}")
    String region;

    @Value("${aws.profile}")
    String profile;

    @Bean
    public AmazonS3 amazonS3() {
        AWSCredentialsProviderChain awsCredentialsProviderChain = new AWSCredentialsProviderChain(
                new InstanceProfileCredentialsProvider(true),
                new ProfileCredentialsProvider(profile));
//        InstanceProfileCredentialsProvider provider
//                = new InstanceProfileCredentialsProvider(true);
        return AmazonS3ClientBuilder.standard()
                .withCredentials(awsCredentialsProviderChain)
                .withRegion(region)
                .build();

    }

}
