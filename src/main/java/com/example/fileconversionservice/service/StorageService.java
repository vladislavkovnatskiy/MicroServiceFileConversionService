package com.example.fileconversionservice.service;

import com.example.fileconversionservice.exception.StorageException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final MinioClient s3Client;

    @Value("${app.seaweedfs.bucket}")
    private String bucket;

    public byte[] read(String path) {
        try (InputStream in = s3Client.getObject(GetObjectArgs.builder().bucket(bucket).object(path).build())) {
            return in.readAllBytes();
        } catch (Exception ex) {
            throw new StorageException("Cannot read from storage" + path, ex);
        }
    }

    public void write(String path, byte[] data, String contentType) {
        try (InputStream in = new ByteArrayInputStream(data)) {
            s3Client.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .stream(in, data.length, -1)
                    .contentType(contentType)
                    .build()
            );
        } catch (Exception e) {
            throw new StorageException("Cannot write to storage" + path, e);
        }
    }
}
