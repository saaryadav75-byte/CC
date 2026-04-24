package com.huffman.compressor.model;

import java.time.LocalDateTime;

public class CompressionHistory {
    private String id;
    private String sessionId;
    private String operationType; // COMPRESS_TEXT, DECOMPRESS_TEXT, COMPRESS_FILE, DECOMPRESS_FILE
    private String originalName;
    private long originalSize;
    private long compressedSize;
    private double compressionRatio;
    private double spaceSavedPercentage;
    private LocalDateTime timestamp;
    private String status; // SUCCESS, FAILED, IN_PROGRESS

    public CompressionHistory() {
        this.timestamp = LocalDateTime.now();
        this.status = "IN_PROGRESS";
    }

    public CompressionHistory(String sessionId, String operationType, String originalName, 
                            long originalSize, long compressedSize) {
        this();
        this.sessionId = sessionId;
        this.operationType = operationType;
        this.originalName = originalName;
        this.originalSize = originalSize;
        this.compressedSize = compressedSize;
        this.compressionRatio = originalSize > 0 ? (double) compressedSize / originalSize : 0;
        this.spaceSavedPercentage = originalSize > 0 ? (1 - (double) compressedSize / originalSize) * 100 : 0;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public long getOriginalSize() {
        return originalSize;
    }

    public void setOriginalSize(long originalSize) {
        this.originalSize = originalSize;
    }

    public long getCompressedSize() {
        return compressedSize;
    }

    public void setCompressedSize(long compressedSize) {
        this.compressedSize = compressedSize;
    }

    public double getCompressionRatio() {
        return compressionRatio;
    }

    public void setCompressionRatio(double compressionRatio) {
        this.compressionRatio = compressionRatio;
    }

    public double getSpaceSavedPercentage() {
        return spaceSavedPercentage;
    }

    public void setSpaceSavedPercentage(double spaceSavedPercentage) {
        this.spaceSavedPercentage = spaceSavedPercentage;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void markSuccess() {
        this.status = "SUCCESS";
    }

    public void markFailed() {
        this.status = "FAILED";
    }
}
