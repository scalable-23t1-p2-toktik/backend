package com.example.videoupload.service;

import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import java.time.Duration;

import org.springframework.stereotype.Service;

import java.net.URL;

@Service
public class PresignedService {
    /**
     * From S3 presigned User Guide
     * Create a presigned URL for uploading a String object.
     * @param bucketName - The name of the bucket.
     * @param keyName - The name of the object.
     * @return - The presigned URL for an HTTP PUT.
     */
    public URL createSignedUrlForStringPut(String bucketName, String keyName) {
        try (S3Presigner presigner = S3Presigner.create()) {

            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))  // The URL will expire in 10 minutes.
                    .putObjectRequest(objectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);

            return presignedRequest.url();
        }
    }
}
