package com.twitter.twitter_rest_api.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class S3Service {

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    private final AmazonS3 s3Client;

    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif","image/webp","image,jpg"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public S3Service(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(MultipartFile file) {
        log.debug("Dosya yükleme işlemi başlatılıyor: {}", file.getOriginalFilename());
        log.debug("Dosya boyutu: {} bytes", file.getSize());
        log.debug("Dosya türü: {}", file.getContentType());

        File fileObj = null;
        try {
            fileObj = convertMultiPartFileToFile(file);
            log.debug("Dosya dönüştürme başarılı, boyut: {} bytes", fileObj.length());

            String fileName = generateUniqueFileName(file);
            log.debug("Oluşturulan dosya adı: {}", fileName);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            log.debug("S3'e yükleme başlıyor...");
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, fileObj)
                    .withMetadata(metadata);

            s3Client.putObject(putObjectRequest);
            log.debug("S3'e yükleme tamamlandı");

            String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
                    bucketName, region, fileName);
            log.debug("Oluşturulan URL: {}", fileUrl);

            return fileUrl;

        } catch (Exception e) {
            log.error("Dosya yükleme hatası: ", e);
            throw new RuntimeException("Dosya yüklenemedi: " + e.getMessage());
        } finally {
            if (fileObj != null && fileObj.exists()) {
                fileObj.delete();
            }
        }
    }

    private String generateUniqueFileName(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName != null ?
                originalFileName.substring(originalFileName.lastIndexOf(".")) : "";
        return UUID.randomUUID().toString() + extension;
    }

    private File convertMultiPartFileToFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("Original filename is null");
        }

        File convertedFile = new File(originalFilename);
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
            return convertedFile;
        } catch (IOException e) {
            log.error("Error converting MultipartFile to File: {}", e.getMessage());
            throw new RuntimeException("Failed to convert file: " + e.getMessage());
        }
    }

    public void deleteFile(String fileName) {
        log.info("Attempting to delete file: {}", fileName);
        try {
            if (!s3Client.doesObjectExist(bucketName, fileName)) {
                log.warn("File does not exist: {}", fileName);
                return;
            }

            s3Client.deleteObject(bucketName, fileName);
            log.info("File deleted successfully: {}", fileName);
        } catch (AmazonServiceException e) {
            log.error("AWS S3 error during file deletion: {}", e.getMessage());
            throw new RuntimeException("Failed to delete file: " + e.getMessage());
        }
    }
}