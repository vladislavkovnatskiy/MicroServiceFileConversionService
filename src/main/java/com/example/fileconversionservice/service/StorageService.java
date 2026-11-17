package com.example.fileconversionservice.service;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final MinioClient s3Client;

    @Value("${app.seaweedfs.bucket}")
    private String bucket;


}
