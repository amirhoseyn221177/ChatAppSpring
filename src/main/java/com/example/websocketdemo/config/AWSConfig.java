package com.example.websocketdemo.config;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;

@Configuration
public class AWSConfig {
    private final String accessKey="AKIA3HZEOI6AKVTKQF4O";
    private final String secretKey="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";
    private final  Regions regions=Regions.US_WEST_2;


    @Bean
    public URL preSignedURl(){
       AWSCredentialsProvider credentials= credentials();
        AmazonS3 client = AmazonS3ClientBuilder.standard()
                .withCredentials(credentials)
                .withRegion(regions)
                .build();

        java.util.Date expiration = new java.util.Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 60;
        expiration.setTime(expTimeMillis);

        String bucket = "advancednodejs";
        GeneratePresignedUrlRequest preSigned= new GeneratePresignedUrlRequest(bucket,"amir2211", HttpMethod.PUT)
                .withExpiration(expiration);
//                .withContentType("*/*");
        URL url = client.generatePresignedUrl(preSigned);
        System.out.println(url);

        return url;
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
