package com.eprtracker.service;

import java.io.InputStream;

public interface S3Service {
    String uploadFile(String key, byte[] content, String contentType);
    String generatePresignedUrl(String key, int durationMinutes);
    InputStream downloadFile(String key);
}
