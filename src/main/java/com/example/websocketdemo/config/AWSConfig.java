package com.example.websocketdemo.config;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.net.URL;

@Configuration
public class AWSConfig {
    private final String accessKey="AKIA3HZEOI6AEXVDOD35";
    private final String secretKey ="AKIA3HZEOI6AEXVDOD35";
    private final  Regions regions=Regions.US_WEST_2;

    @Bean
    public AmazonS3 creatClient(){
        AWSCredentialsProvider credentials= credentials();
        return AmazonS3ClientBuilder.standard()
                .withCredentials(credentials)
                .withRegion(regions)
                .build();
    }


    @Bean
    public TransferManager creatingTransferManager(){
        TransferManager tm= TransferManagerBuilder.standard()
                .withMultipartUploadThreshold((long)100 *1024*1025)
                .withS3Client(creatClient())
                .build();

        
    }


    @Bean
    public AWSCredentialsProvider credentials(){
        AWSCredentials credentials = new AWSCredentials() {
            @Override
            public String getAWSAccessKeyId() {
                return accessKey;
            }

            @Override
            public String getAWSSecretKey() {
                return secretKey;
            }
        };
        return new AWSCredentialsProvider() {

            @Override
            public AWSCredentials getCredentials() {
                return credentials;
            }

            @Override
            public void refresh() {

            }
        };
    }
}
