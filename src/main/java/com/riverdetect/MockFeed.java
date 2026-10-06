package com.riverdetect;

import com.riverdetect.models.WasteCategory;
import com.riverdetect.models.WasteObject;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class MockFeed {
    static final int WIDTH = 640;
    static final int HEIGHT = 480;

    private static class Item {
        final WasteCategory category;
        final int w;
        final int h;
        final double baseConfidence;
        double x;
        double y;
        double vx;
        double vy;

        Item(WasteCategory category, double x, double y, double vx, double vy,
             int w, int h, double baseConfidence) {
            this.category = category;
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.w = w;
            this.h = h;
            this.baseConfidence = baseConfidence;
        }
    }

    private final List<Item> items = new ArrayList<>();
    private final Random random = new Random(7);
    private BufferedImage frame;
    private List<WasteObject> detections = new ArrayList<>();

    MockFeed() {
        items.add(new Item(WasteCategory.PLASTIC, 60, 90, 1.6, 0.5, 60, 40, 0.90));
        items.add(new Item(WasteCategory.PLASTIC, 400, 300, -1.2, 0.7, 50, 35, 0.80));
        items.add(new Item(WasteCategory.METAL, 250, 200, 0.9, -0.8, 40, 40, 0.75));
        items.add(new Item(WasteCategory.ORGANIC, 120, 340, 1.0, -0.6, 70, 50, 0.65));
        items.add(new Item(WasteCategory.PAPER, 500, 80, -0.8, 0.9, 45, 55, 0.70));
        items.add(new Item(WasteCategory.GLASS, 300, 400, 0.6, 0.5, 30, 45, 0.55));
    }

    void next() {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(new Color(25, 70, 110));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(new Color(40, 95, 140));
        for (int y = 20; y < HEIGHT; y += 40) {
            g.drawLine(0, y, WIDTH, y);
        }

        List<WasteObject> found = new ArrayList<>();
        for (Item item : items) {
            item.x += item.vx;
            item.y += item.vy;
            if (item.x < 0 || item.x + item.w > WIDTH) {
                item.vx = -item.vx;
            }
            if (item.y < 0 || item.y + item.h > HEIGHT) {
                item.vy = -item.vy;
            }
            item.x = Math.max(0, Math.min(item.x, WIDTH - item.w));
            item.y = Math.max(0, Math.min(item.y, HEIGHT - item.h));

            g.setColor(new Color(215, 210, 195));
            g.fillOval((int) item.x, (int) item.y, item.w, item.h);

            if (random.nextDouble() < 0.05) {
                continue;
            }
            double confidence = item.baseConfidence + (random.nextDouble() - 0.5) * 0.2;
            confidence = Math.max(0.2, Math.min(0.99, confidence));
            found.add(new WasteObject(item.category, confidence,
                    (int) item.x, (int) item.y, item.w, item.h));
        }

        if (random.nextDouble() < 0.04) {
            found.add(new WasteObject(WasteCategory.OTHER, 0.55,
                    random.nextInt(WIDTH - 40), random.nextInt(HEIGHT - 40), 30, 30));
        }

        g.dispose();
        this.frame = image;
        this.detections = found;
    }

    BufferedImage frame() {
        return frame;
    }

    List<WasteObject> detections() {
        return detections;
    }
}
