package com.riverdetect.ui;

import com.riverdetect.models.WasteCategory;
import com.riverdetect.segregation.SegregationResult;
import com.riverdetect.tracking.ObjectTracker;
import com.riverdetect.tracking.Track;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleConsumer;

public class MainWindow extends JFrame {
    private final VideoPanel videoPanel = new VideoPanel();
    private final StatsPanel statsPanel;

    public MainWindow(double initialThreshold, DoubleConsumer onThresholdChange, Runnable onReset) {
        super("River Waste Detection System");
        statsPanel = new StatsPanel(initialThreshold, onThresholdChange, onReset);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(videoPanel, BorderLayout.CENTER);
        add(statsPanel, BorderLayout.EAST);
        pack();
        setLocationRelativeTo(null);
    }

    public void update(BufferedImage frame, List<Track> tracks, SegregationResult result,
                       ObjectTracker tracker, double fps) {
        List<VideoPanel.Overlay> overlays = new ArrayList<>();
        for (Track track : tracks) {
            overlays.add(new VideoPanel.Overlay(track.getId(), track.getLatest()));
        }
        Map<WasteCategory, Integer> unique = new EnumMap<>(WasteCategory.class);
        for (WasteCategory category : WasteCategory.values()) {
            unique.put(category, tracker.getUniqueCount(category));
        }
        int totalUnique = tracker.getTotalUniqueCount();

        SwingUtilities.invokeLater(() -> {
            videoPanel.setContent(frame, overlays);
            statsPanel.update(result, unique, totalUnique, fps);
        });
    }
}
