package com.huffman.compressor.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class HuffmanServiceTest {

    @Test
    public void testCompressionDecompression() throws Exception {
        HuffmanService huffmanService = new HuffmanService();
        
        // Test with simple text
        String originalText = "This is a test string for Huffman compression. It contains repeated characters.";
        byte[] originalData = originalText.getBytes();
        
        // Compress
        byte[] compressed = huffmanService.compress(originalData);
        assertNotNull(compressed);
        assertTrue(compressed.length > 0);
        
        // Decompress
        byte[] decompressed = huffmanService.decompress(compressed);
        assertNotNull(decompressed);
        
        // Verify original data is restored
        String decompressedText = new String(decompressed);
        assertEquals(originalText, decompressedText);
    }
    
    @Test
    public void testEmptyData() throws Exception {
        HuffmanService huffmanService = new HuffmanService();
        
        byte[] emptyData = new byte[0];
        byte[] compressed = huffmanService.compress(emptyData);
        byte[] decompressed = huffmanService.decompress(compressed);
        
        assertArrayEquals(emptyData, decompressed);
    }
    
    @Test
    public void testNullData() throws Exception {
        HuffmanService huffmanService = new HuffmanService();
        
        byte[] compressed = huffmanService.compress(null);
        byte[] decompressed = huffmanService.decompress(compressed);
        
        assertEquals(0, decompressed.length);
    }
}
