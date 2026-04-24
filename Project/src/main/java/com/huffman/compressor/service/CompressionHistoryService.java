package com.huffman.compressor.service;

import com.huffman.compressor.model.CompressionHistory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class CompressionHistoryService {
    
    private final Map<String, List<CompressionHistory>> sessionHistory = new ConcurrentHashMap<>();
    private final Map<String, CompressionHistory> activeOperations = new ConcurrentHashMap<>();
    
    public String createSession() {
        return UUID.randomUUID().toString();
    }
    
    public CompressionHistory startOperation(String sessionId, String operationType, String originalName, 
                                          long originalSize, long compressedSize) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        
        CompressionHistory history = new CompressionHistory(sessionId, operationType, originalName, 
                                                          originalSize, compressedSize);
        history.setId(UUID.randomUUID().toString());
        
        sessionHistory.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(history);
        activeOperations.put(history.getId(), history);
        
        return history;
    }
    
    public void completeOperation(String historyId, boolean success) {
        CompressionHistory history = activeOperations.get(historyId);
        if (history != null) {
            if (success) {
                history.markSuccess();
            } else {
                history.markFailed();
            }
            activeOperations.remove(historyId);
        }
    }
    
    public List<CompressionHistory> getSessionHistory(String sessionId) {
        return sessionHistory.getOrDefault(sessionId, new ArrayList<>());
    }
    
    public List<CompressionHistory> getAllHistory() {
        List<CompressionHistory> allHistory = new ArrayList<>();
        sessionHistory.values().forEach(allHistory::addAll);
        return allHistory.stream()
                .sorted((h1, h2) -> h2.getTimestamp().compareTo(h1.getTimestamp()))
                .limit(100) // Limit to last 100 operations
                .toList();
    }
    
    public Map<String, Object> getSessionStats(String sessionId) {
        List<CompressionHistory> history = getSessionHistory(sessionId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOperations", history.size());
        stats.put("successfulOperations", history.stream().mapToInt(h -> "SUCCESS".equals(h.getStatus()) ? 1 : 0).sum());
        stats.put("failedOperations", history.stream().mapToInt(h -> "FAILED".equals(h.getStatus()) ? 1 : 0).sum());
        
        long totalOriginalSize = history.stream().mapToLong(CompressionHistory::getOriginalSize).sum();
        long totalCompressedSize = history.stream().mapToLong(CompressionHistory::getCompressedSize).sum();
        
        stats.put("totalOriginalSize", totalOriginalSize);
        stats.put("totalCompressedSize", totalCompressedSize);
        stats.put("overallCompressionRatio", totalOriginalSize > 0 ? (double) totalCompressedSize / totalOriginalSize : 0);
        stats.put("totalSpaceSaved", totalOriginalSize > 0 ? (1 - (double) totalCompressedSize / totalOriginalSize) * 100 : 0);
        
        // Operation type breakdown
        Map<String, Long> operationCounts = history.stream()
                .collect(Collectors.groupingBy(CompressionHistory::getOperationType, Collectors.counting()));
        stats.put("operationCounts", operationCounts);
        
        return stats;
    }
    
    public void clearSession(String sessionId) {
        sessionHistory.remove(sessionId);
    }
    
    public Map<String, Object> getSystemStats() {
        List<CompressionHistory> allHistory = getAllHistory();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSessions", sessionHistory.size());
        stats.put("totalOperations", allHistory.size());
        stats.put("activeOperations", activeOperations.size());
        
        long totalOriginalSize = allHistory.stream().mapToLong(CompressionHistory::getOriginalSize).sum();
        long totalCompressedSize = allHistory.stream().mapToLong(CompressionHistory::getCompressedSize).sum();
        
        stats.put("totalDataProcessed", totalOriginalSize);
        stats.put("totalDataCompressed", totalCompressedSize);
        stats.put("systemCompressionRatio", totalOriginalSize > 0 ? (double) totalCompressedSize / totalOriginalSize : 0);
        stats.put("systemSpaceSaved", totalOriginalSize > 0 ? (1 - (double) totalCompressedSize / totalOriginalSize) * 100 : 0);
        
        // Recent activity (last hour)
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentOperations = allHistory.stream()
                .mapToLong(h -> h.getTimestamp().isAfter(oneHourAgo) ? 1 : 0)
                .sum();
        stats.put("recentOperations", recentOperations);
        
        return stats;
    }
}
