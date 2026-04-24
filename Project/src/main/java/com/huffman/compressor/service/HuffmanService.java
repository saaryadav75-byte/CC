package com.huffman.compressor.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.springframework.stereotype.Service;

import com.huffman.compressor.model.HuffmanNode;

@Service
public class HuffmanService {

    public byte[] compress(byte[] inputData) throws IOException {
        if (inputData == null || inputData.length == 0) {
            return new byte[0];
        }

        Map<Byte, Integer> frequencyMap = buildFrequencyMap(inputData);
        HuffmanNode root = buildHuffmanTree(frequencyMap);
        Map<Byte, String> huffmanCodes = generateHuffmanCodes(root);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        
        objectOutputStream.writeObject(frequencyMap);
        objectOutputStream.flush();
        
        BitOutputStream bitOutputStream = new BitOutputStream(outputStream);
        
        for (byte b : inputData) {
            String code = huffmanCodes.get(b);
            for (char bit : code.toCharArray()) {
                bitOutputStream.writeBit(bit == '1');
            }
        }
        
        bitOutputStream.flush();
        objectOutputStream.close();
        
        return outputStream.toByteArray();
    }

    public byte[] decompress(byte[] compressedData) throws IOException, ClassNotFoundException {
        if (compressedData == null || compressedData.length == 0) {
            return new byte[0];
        }

        // Read the frequency map first
        ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedData);
        ObjectInputStream objectInputStream = null;
        Map<Byte, Integer> frequencyMap = null;
        int bytesUsedByObject = 0;
        
        try {
            objectInputStream = new ObjectInputStream(inputStream);
            
            @SuppressWarnings("unchecked")
            Map<Byte, Integer> map = (Map<Byte, Integer>) objectInputStream.readObject();
            frequencyMap = map;
            
            // Calculate how many bytes were used by ObjectInputStream
            bytesUsedByObject = compressedData.length - inputStream.available();
            objectInputStream.close();
            objectInputStream = null;
            
            if (frequencyMap == null || frequencyMap.isEmpty()) {
                return new byte[0];
            }
            
            // Extract remaining bytes (the actual compressed bit data)
            byte[] remainingBytes = new byte[inputStream.available()];
            inputStream.read(remainingBytes);
            
            HuffmanNode root = buildHuffmanTree(frequencyMap);
            if (root == null) {
                return new byte[0];
            }
            
            // Create a new stream for the remaining data
            ByteArrayInputStream bitStream = new ByteArrayInputStream(remainingBytes);
            BitInputStream bitInputStream = new BitInputStream(bitStream);
            List<Byte> decompressedBytes = new ArrayList<>();
            
            HuffmanNode current = root;
            try {
                while (true) {
                    boolean bit = bitInputStream.readBit();
                    if (bit) {
                        current = current.getRight();
                    } else {
                        current = current.getLeft();
                    }
                    
                    if (current == null) {
                        break; // Invalid tree traversal
                    }
                    
                    if (current.isLeaf()) {
                        decompressedBytes.add(current.getData());
                        current = root;
                    }
                }
            } catch (EOFException e) {
                // End of stream reached - this is normal
            }
            
            bitInputStream.close();
            
            byte[] result = new byte[decompressedBytes.size()];
            for (int i = 0; i < decompressedBytes.size(); i++) {
                result[i] = decompressedBytes.get(i);
            }
            
            return result;
        } finally {
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    private Map<Byte, Integer> buildFrequencyMap(byte[] data) {
        Map<Byte, Integer> frequencyMap = new HashMap<>();
        for (byte b : data) {
            frequencyMap.put(b, frequencyMap.getOrDefault(b, 0) + 1);
        }
        return frequencyMap;
    }

    private HuffmanNode buildHuffmanTree(Map<Byte, Integer> frequencyMap) {
        PriorityQueue<HuffmanNode> priorityQueue = new PriorityQueue<>();
        
        for (Map.Entry<Byte, Integer> entry : frequencyMap.entrySet()) {
            priorityQueue.offer(new HuffmanNode(entry.getKey(), entry.getValue()));
        }
        
        while (priorityQueue.size() > 1) {
            HuffmanNode left = priorityQueue.poll();
            HuffmanNode right = priorityQueue.poll();
            
            HuffmanNode parent = new HuffmanNode(
                left.getFrequency() + right.getFrequency(), left, right);
            
            priorityQueue.offer(parent);
        }
        
        return priorityQueue.poll();
    }

    private Map<Byte, String> generateHuffmanCodes(HuffmanNode root) {
        Map<Byte, String> huffmanCodes = new HashMap<>();
        generateCodesRecursive(root, "", huffmanCodes);
        return huffmanCodes;
    }

    private void generateCodesRecursive(HuffmanNode node, String code, Map<Byte, String> huffmanCodes) {
        if (node == null) {
            return;
        }
        
        if (node.isLeaf()) {
            if (code.isEmpty()) {
                huffmanCodes.put(node.getData(), "0");
            } else {
                huffmanCodes.put(node.getData(), code);
            }
            return;
        }
        
        generateCodesRecursive(node.getLeft(), code + "0", huffmanCodes);
        generateCodesRecursive(node.getRight(), code + "1", huffmanCodes);
    }

    private static class BitOutputStream {
        private final OutputStream outputStream;
        private int currentByte;
        private int bitCount;

        public BitOutputStream(OutputStream outputStream) {
            this.outputStream = outputStream;
            this.currentByte = 0;
            this.bitCount = 0;
        }

        public void writeBit(boolean bit) throws IOException {
            currentByte = (currentByte << 1) | (bit ? 1 : 0);
            bitCount++;

            if (bitCount == 8) {
                outputStream.write(currentByte);
                currentByte = 0;
                bitCount = 0;
            }
        }

        public void flush() throws IOException {
            if (bitCount > 0) {
                currentByte = currentByte << (8 - bitCount);
                outputStream.write(currentByte);
                currentByte = 0;
                bitCount = 0;
            }
            outputStream.flush();
        }
    }

    private static class BitInputStream {
        private final InputStream inputStream;
        private int currentByte;
        private int bitCount;

        public BitInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
            this.currentByte = 0;
            this.bitCount = 8;
        }

        public boolean readBit() throws IOException {
            if (bitCount == 8) {
                currentByte = inputStream.read();
                if (currentByte == -1) {
                    throw new EOFException();
                }
                bitCount = 0;
            }

            boolean bit = ((currentByte >> (7 - bitCount)) & 1) == 1;
            bitCount++;
            return bit;
        }

        public void close() throws IOException {
            inputStream.close();
        }
    }
}
