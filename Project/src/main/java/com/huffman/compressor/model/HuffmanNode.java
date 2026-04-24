package com.huffman.compressor.model;

public class HuffmanNode implements Comparable<HuffmanNode> {
    private byte data;
    private int frequency;
    private HuffmanNode left;
    private HuffmanNode right;

    public HuffmanNode(byte data, int frequency) {
        this.data = data;
        this.frequency = frequency;
        this.left = null;
        this.right = null;
    }

    public HuffmanNode(int frequency, HuffmanNode left, HuffmanNode right) {
        this.data = 0;
        this.frequency = frequency;
        this.left = left;
        this.right = right;
    }

    public byte getData() {
        return data;
    }

    public int getFrequency() {
        return frequency;
    }

    public HuffmanNode getLeft() {
        return left;
    }

    public HuffmanNode getRight() {
        return right;
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }

    @Override
    public int compareTo(HuffmanNode other) {
        return Integer.compare(this.frequency, other.frequency);
    }
}
