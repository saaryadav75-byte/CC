package com.huffman.compressor.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.huffman.compressor.model.CompressionHistory;
import com.huffman.compressor.service.CompressionHistoryService;
import com.huffman.compressor.service.FileValidationService;
import com.huffman.compressor.service.HuffmanService;

@Controller
public class CompressionController {

    @Autowired
    private HuffmanService huffmanService;

    @Autowired
    private FileValidationService fileValidationService;

    @Autowired
    private CompressionHistoryService historyService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/compress")
    public ResponseEntity<Resource> compressFile(@RequestParam("file") MultipartFile file) {
        try {
            if (!fileValidationService.isValidFile(file)) {
                return ResponseEntity.badRequest().build();
            }

            byte[] inputData = file.getBytes();
            byte[] compressedData = huffmanService.compress(inputData);
            if (compressedData == null) {
                return ResponseEntity.internalServerError().build();
            }

            ByteArrayResource resource = new ByteArrayResource(compressedData);
            String originalFilename = file.getOriginalFilename();
            String compressedFilename = originalFilename != null ? 
                originalFilename.replaceAll("\\.[^.]+$", "") + ".huf" : "compressed.huf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + compressedFilename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(compressedData.length)
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/decompress")
    public ResponseEntity<Resource> decompressFile(@RequestParam("file") MultipartFile file) {
        try {
            if (!fileValidationService.isValidCompressedFile(file)) {
                String errorMsg = fileValidationService.getValidationErrorMessage(file, true);
                return ResponseEntity.badRequest().build();
            }

            byte[] compressedData = file.getBytes();
            byte[] decompressedData = huffmanService.decompress(compressedData);

            ByteArrayResource resource = new ByteArrayResource(decompressedData);
            String originalFilename = file.getOriginalFilename();
            String decompressedFilename = originalFilename != null ? 
                originalFilename.replaceAll("\\.[^.]+$", "") + "_decompressed.txt" : "decompressed.txt";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + decompressedFilename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(decompressedData.length)
                    .body(resource);

        } catch (ClassNotFoundException e) {
            return ResponseEntity.status(400).build(); // Bad compressed file format
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/stats")
    @ResponseBody
    public ResponseEntity<CompressionStats> getCompressionStats(@RequestParam("file") MultipartFile file) {
        try {
            if (!fileValidationService.isValidFile(file)) {
                return ResponseEntity.badRequest().build();
            }

            byte[] inputData = file.getBytes();
            byte[] compressedData = huffmanService.compress(inputData);
            
            double compressionRatio = inputData.length > 0 ? (double) compressedData.length / inputData.length : 0;
            double spaceSaved = inputData.length > 0 ? (1 - compressionRatio) * 100 : 0;

            CompressionStats stats = new CompressionStats(
                inputData.length,
                compressedData.length,
                compressionRatio,
                spaceSaved
            );

            return ResponseEntity.ok(stats);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/compress-text")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> compressText(@RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            if (text == null || text.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            byte[] inputData = text.getBytes("UTF-8");
            byte[] compressedData = huffmanService.compress(inputData);
            
            String compressedBase64 = Base64.getEncoder().encodeToString(compressedData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("compressedData", compressedBase64);
            response.put("originalSize", inputData.length);
            response.put("compressedSize", compressedData.length);
            response.put("compressionRatio", inputData.length > 0 ? (double) compressedData.length / inputData.length : 0);
            response.put("spaceSavedPercentage", inputData.length > 0 ? (1 - (double) compressedData.length / inputData.length) * 100 : 0);
            
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/decompress-text")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> decompressText(@RequestBody Map<String, String> request) {
        try {
            String compressedBase64 = request.get("compressedData");
            if (compressedBase64 == null || compressedBase64.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            byte[] compressedData = Base64.getDecoder().decode(compressedBase64);
            byte[] decompressedData = huffmanService.decompress(compressedData);
            String decompressedText = new String(decompressedData, "UTF-8");
            
            Map<String, Object> response = new HashMap<>();
            response.put("decompressedText", decompressedText);
            response.put("compressedSize", compressedData.length);
            response.put("decompressedSize", decompressedData.length);
            
            return ResponseEntity.ok(response);

        } catch (IOException | IllegalArgumentException | ClassNotFoundException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/compress-batch")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> compressBatch(@RequestParam("files") MultipartFile[] files) {
        try {
            List<Map<String, Object>> results = new ArrayList<>();
            String sessionId = historyService.createSession();
            
            for (MultipartFile file : files) {
                if (!fileValidationService.isValidFile(file)) {
                    results.add(Map.of(
                        "filename", file.getOriginalFilename(),
                        "status", "FAILED",
                        "error", "Invalid file"
                    ));
                    continue;
                }

                var history = historyService.startOperation(sessionId, "COMPRESS_FILE", 
                    file.getOriginalFilename(), file.getSize(), 0);
                
                try {
                    byte[] inputData = file.getBytes();
                    byte[] compressedData = huffmanService.compress(inputData);
                    
                    history.setCompressedSize(compressedData.length);
                    history.markSuccess();
                    
                    double compressionRatio = inputData.length > 0 ? (double) compressedData.length / inputData.length : 0;
                    double spaceSavedPercentage = inputData.length > 0 ? (1 - (double) compressedData.length / inputData.length) * 100 : 0;
                    
                    results.add(Map.of(
                        "filename", file.getOriginalFilename(),
                        "status", "SUCCESS",
                        "originalSize", inputData.length,
                        "compressedSize", compressedData.length,
                        "compressionRatio", compressionRatio,
                        "spaceSavedPercentage", spaceSavedPercentage,
                        "compressedData", Base64.getEncoder().encodeToString(compressedData)
                    ));
                } catch (Exception e) {
                    history.markFailed();
                    results.add(Map.of(
                        "filename", file.getOriginalFilename(),
                        "status", "FAILED",
                        "error", e.getMessage()
                    ));
                }
            }

            return ResponseEntity.ok(Map.of(
                "sessionId", sessionId,
                "results", results,
                "totalFiles", files.length,
                "successful", results.stream().mapToInt(r -> "SUCCESS".equals(r.get("status")) ? 1 : 0).sum(),
                "failed", results.stream().mapToInt(r -> "FAILED".equals(r.get("status")) ? 1 : 0).sum()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/history/{sessionId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSessionHistory(@PathVariable String sessionId) {
        try {
            List<CompressionHistory> history = historyService.getSessionHistory(sessionId);
            Map<String, Object> stats = historyService.getSessionStats(sessionId);
            
            return ResponseEntity.ok(Map.of(
                "history", history,
                "stats", stats
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/stats/system")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        try {
            Map<String, Object> stats = historyService.getSystemStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/session/create")
    @ResponseBody
    public ResponseEntity<Map<String, String>> createSession() {
        try {
            String sessionId = historyService.createSession();
            return ResponseEntity.ok(Map.of("sessionId", sessionId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/session/{sessionId}")
    @ResponseBody
    public ResponseEntity<Void> clearSession(@PathVariable String sessionId) {
        try {
            historyService.clearSession(sessionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    public static class CompressionStats {
        private final int originalSize;
        private final int compressedSize;
        private final double compressionRatio;
        private final double spaceSavedPercentage;

        public CompressionStats(int originalSize, int compressedSize, 
                               double compressionRatio, double spaceSavedPercentage) {
            this.originalSize = originalSize;
            this.compressedSize = compressedSize;
            this.compressionRatio = compressionRatio;
            this.spaceSavedPercentage = spaceSavedPercentage;
        }

        public int getOriginalSize() {
            return originalSize;
        }

        public int getCompressedSize() {
            return compressedSize;
        }

        public double getCompressionRatio() {
            return compressionRatio;
        }

        public double getSpaceSavedPercentage() {
            return spaceSavedPercentage;
        }
    }
}
