package com.example.fileconversionservice.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    // Создаём бин MinioClient, который Spring будет инжектить везде, где нужен доступ к хранилищу. Это единственная точка конфигурации S3.
    @Bean
    public MinioClient s3Client(
            @Value("${app.seaweedfs.url}") String url,
            @Value("${app.seaweedfs.access-key}") String accessKey,
            @Value("${app.seaweedfs.secret-key}") String secretKey
    ) {
        return MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .region("us-east-1")
                .build();
    }
}
