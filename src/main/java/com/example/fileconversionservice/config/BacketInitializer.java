package com.example.fileconversionservice.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BacketInitializer implements ApplicationRunner {

    private final MinioClient s3Client;

    @Value("${app.seaweedfs.bucket}")
    private String bucket;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!s3Client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            s3Client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            log.info("Bucket {} created", bucket);
        } else {
            log.info("Bucket {} already exists", bucket);
        }
    }
}
