package com.example.thexuong.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudflareR2ServiceTest {

    @InjectMocks
    private CloudflareR2Service cloudflareR2Service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cloudflareR2Service, "r2Endpoint", "http://localhost");
        ReflectionTestUtils.setField(cloudflareR2Service, "accessKey", "acc");
        ReflectionTestUtils.setField(cloudflareR2Service, "secretKey", "sec");
        ReflectionTestUtils.setField(cloudflareR2Service, "bucketName", "my-bucket");
        ReflectionTestUtils.setField(cloudflareR2Service, "publicUrl", "https://pub.com");
    }

    private void setupMockS3(MockedStatic<S3Client> mockedS3, S3Client clientMock) {
        S3ClientBuilder builderMock = mock(S3ClientBuilder.class);
        when(builderMock.endpointOverride(any())).thenReturn(builderMock);
        when(builderMock.region(any())).thenReturn(builderMock);
        when(builderMock.credentialsProvider(any())).thenReturn(builderMock);
        when(builderMock.serviceConfiguration(any(software.amazon.awssdk.services.s3.S3Configuration.class))).thenReturn(builderMock);
        when(builderMock.build()).thenReturn(clientMock);
        mockedS3.when(S3Client::builder).thenReturn(builderMock);
    }

    // ==================== uploadMultiple ====================

    @Test
    void uploadMultiple_EmptyOrNullFiles_Skips() {
        try (MockedStatic<S3Client> mockedS3 = mockStatic(S3Client.class)) {
            S3Client clientMock = mock(S3Client.class);
            setupMockS3(mockedS3, clientMock);

            MultipartFile emptyFile = mock(MultipartFile.class);
            when(emptyFile.isEmpty()).thenReturn(true);

            MultipartFile[] files = new MultipartFile[]{null, emptyFile};

            List<String> urls = cloudflareR2Service.uploadMultiple(files);

            assertTrue(urls.isEmpty());
            verify(clientMock, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }
    }

    @Test
    void uploadMultiple_Exception_ThrowsRuntimeException() throws IOException {
        try (MockedStatic<S3Client> mockedS3 = mockStatic(S3Client.class)) {
            S3Client clientMock = mock(S3Client.class);
            setupMockS3(mockedS3, clientMock);

            MultipartFile mockFile = mock(MultipartFile.class);
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
            when(mockFile.getContentType()).thenReturn("image/jpeg");
            when(mockFile.getInputStream()).thenThrow(new IOException("Stream error"));

            MultipartFile[] files = new MultipartFile[]{mockFile};

            RuntimeException ex = assertThrows(RuntimeException.class, () -> cloudflareR2Service.uploadMultiple(files));
            assertTrue(ex.getMessage().contains("Upload to R2 failed"));
        }
    }

    @Test
    void uploadMultiple_Success_WithSanitization() throws IOException {
        try (MockedStatic<S3Client> mockedS3 = mockStatic(S3Client.class)) {
            S3Client clientMock = mock(S3Client.class);
            setupMockS3(mockedS3, clientMock);

            MultipartFile mockFile1 = mock(MultipartFile.class);
            when(mockFile1.isEmpty()).thenReturn(false);
            when(mockFile1.getOriginalFilename()).thenReturn("my image @#.jpg");
            when(mockFile1.getContentType()).thenReturn("image/jpeg");
            when(mockFile1.getSize()).thenReturn(5L);
            when(mockFile1.getInputStream()).thenReturn(new ByteArrayInputStream("dummy".getBytes()));

            MultipartFile mockFile2 = mock(MultipartFile.class);
            when(mockFile2.isEmpty()).thenReturn(false);
            when(mockFile2.getOriginalFilename()).thenReturn(null);
            when(mockFile2.getContentType()).thenReturn("image/jpeg");
            when(mockFile2.getSize()).thenReturn(5L);
            when(mockFile2.getInputStream()).thenReturn(new ByteArrayInputStream("dummy".getBytes()));

            MultipartFile[] files = new MultipartFile[]{mockFile1, mockFile2};

            List<String> urls = cloudflareR2Service.uploadMultiple(files);

            System.out.println("DEBUG_URLS: " + urls);
            assertEquals(2, urls.size());

            verify(clientMock, times(2)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }
    }

    // ==================== deleteFile ====================

    @Test
    void deleteFile_NullOrBlank_ReturnsEarly() {
        try (MockedStatic<S3Client> mockedS3 = mockStatic(S3Client.class)) {
            cloudflareR2Service.deleteFile(null);
            cloudflareR2Service.deleteFile("   ");

            mockedS3.verifyNoInteractions();
        }
    }

    @Test
    void deleteFile_StartsWithPublicUrl_StripsUrl() {
        try (MockedStatic<S3Client> mockedS3 = mockStatic(S3Client.class)) {
            S3Client clientMock = mock(S3Client.class);
            setupMockS3(mockedS3, clientMock);

            cloudflareR2Service.deleteFile("https://pub.com/products/123-image.jpg");

            verify(clientMock, times(1)).deleteObject(any(DeleteObjectRequest.class));
        }
    }

    @Test
    void deleteFile_DoesNotStartWithPublicUrl_UsesRaw() {
        try (MockedStatic<S3Client> mockedS3 = mockStatic(S3Client.class)) {
            S3Client clientMock = mock(S3Client.class);
            setupMockS3(mockedS3, clientMock);

            cloudflareR2Service.deleteFile("raw-key-image.jpg");

            verify(clientMock, times(1)).deleteObject(any(DeleteObjectRequest.class));
        }
    }

    @Test
    void deleteFile_Exception_CatchesAndLogs() {
        try (MockedStatic<S3Client> mockedS3 = mockStatic(S3Client.class)) {
            S3Client clientMock = mock(S3Client.class);
            setupMockS3(mockedS3, clientMock);

            doThrow(new RuntimeException("S3 Delete Error")).when(clientMock).deleteObject(any(DeleteObjectRequest.class));

            assertDoesNotThrow(() -> cloudflareR2Service.deleteFile("raw-key-image.jpg"));
            
            verify(clientMock, times(1)).deleteObject(any(DeleteObjectRequest.class));
        }
    }
}
