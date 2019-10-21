package com.cloud.ccwebapp.recipe.service;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

@Configuration
public class AWSService {

    @Bean
    public AmazonS3 amazonS3() {
        InstanceProfileCredentialsProvider provider
                = new InstanceProfileCredentialsProvider(true);
        return AmazonS3ClientBuilder.standard()
                .withCredentials(provider)
                .build();
    }


}
