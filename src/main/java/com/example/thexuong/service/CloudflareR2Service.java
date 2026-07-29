// ponytail: R2 credentials must be set via CF_R2_ACCESS_KEY / CF_R2_SECRET_KEY env vars (see application.yml).
package com.example.thexuong.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudflareR2Service {

    @Value("${app.cloudflare.r2.endpoint}")
    private String r2Endpoint;

    @Value("${app.cloudflare.r2.access-key}")
    private String accessKey;

    @Value("${app.cloudflare.r2.secret-key}")
    private String secretKey;

    @Value("${app.cloudflare.r2.bucket-name}")
    private String bucketName;

    @Value("${app.cloudflare.r2.public-url}")
    private String publicUrl;

    private S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(r2Endpoint))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }

    public List<String> uploadMultiple(MultipartFile[] files) {
        List<String> urls = new ArrayList<>();
        S3Client client = s3Client();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            try {
                String key = "products/" + UUID.randomUUID() + "-" + sanitize(file.getOriginalFilename());
                client.putObject(PutObjectRequest.builder()
                                .bucket(bucketName)
                                .key(key)
                                .contentType(file.getContentType())
                                .build(),
                        RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
                urls.add(publicUrl + "/" + key);
            } catch (Exception e) {
                throw new RuntimeException("Upload to R2 failed: " + e.getMessage(), e);
            }
        }
        return urls;
    }

    public void deleteFile(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;
        try {
            String key = imageUrl.startsWith(publicUrl)
                    ? imageUrl.substring(publicUrl.length()).replaceFirst("^/+", "")
                    : imageUrl;
            s3Client().deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
        } catch (Exception e) {
            log.warn("Delete from R2 failed for url {}: {}", imageUrl, e.getMessage());
        }
    }

    private String sanitize(String name) {
        if (name == null) return "file";
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
