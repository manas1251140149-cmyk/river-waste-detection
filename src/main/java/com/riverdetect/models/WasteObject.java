package com.riverdetect.models;

public class WasteObject {
    private final WasteCategory category;
    private final double confidence;
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public WasteObject(WasteCategory category, double confidence,
                       int x, int y, int width, int height) {
        if (category == null) {
            throw new IllegalArgumentException("category must not be null");
        }
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("confidence must be between 0 and 1");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be positive");
        }
        this.category = category;
        this.confidence = confidence;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public WasteCategory getCategory() { return category; }
    public double getConfidence() { return confidence; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    @Override
    public String toString() {
        return String.format("WasteObject[%s, conf=%.2f, box=(%d,%d,%d,%d)]",
                category, confidence, x, y, width, height);
    }
}
