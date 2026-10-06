package com.riverdetect.ui;

import com.riverdetect.models.WasteObject;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

class VideoPanel extends JPanel {

    record Overlay(int trackId, WasteObject object) {
    }

    private BufferedImage frame;
    private List<Overlay> overlays = new ArrayList<>();

    VideoPanel() {
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(640, 480));
    }

    void setContent(BufferedImage frame, List<Overlay> overlays) {
        this.frame = frame;
        this.overlays = overlays;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (frame == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double scale = Math.min((double) getWidth() / frame.getWidth(),
                (double) getHeight() / frame.getHeight());
        int w = (int) (frame.getWidth() * scale);
        int h = (int) (frame.getHeight() * scale);
        int offsetX = (getWidth() - w) / 2;
        int offsetY = (getHeight() - h) / 2;
        g2.drawImage(frame, offsetX, offsetY, w, h, null);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
        FontMetrics fm = g2.getFontMetrics();
        g2.setStroke(new BasicStroke(2f));

        for (Overlay overlay : overlays) {
            WasteObject o = overlay.object();
            Color color = CategoryColors.of(o.getCategory());
            int bx = offsetX + (int) (o.getX() * scale);
            int by = offsetY + (int) (o.getY() * scale);
            int bw = (int) (o.getWidth() * scale);
            int bh = (int) (o.getHeight() * scale);

            g2.setColor(color);
            g2.drawRect(bx, by, bw, bh);

            String label = String.format("%s #%d %.0f%%",
                    o.getCategory().getDisplayName(), overlay.trackId(), o.getConfidence() * 100);
            int labelWidth = fm.stringWidth(label) + 6;
            int labelHeight = fm.getHeight();
            int labelY = Math.max(by - labelHeight, offsetY);
            g2.fillRect(bx, labelY, labelWidth, labelHeight);
            g2.setColor(Color.BLACK);
            g2.drawString(label, bx + 3, labelY + fm.getAscent());
        }
        g2.dispose();
    }
}
