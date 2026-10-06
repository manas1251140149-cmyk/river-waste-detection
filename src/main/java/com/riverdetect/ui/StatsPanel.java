package com.riverdetect.ui;

import com.riverdetect.models.WasteCategory;
import com.riverdetect.segregation.SegregationResult;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.DoubleConsumer;

class StatsPanel extends JPanel {
    private final Map<WasteCategory, JLabel> countLabels = new EnumMap<>(WasteCategory.class);
    private final Map<WasteCategory, JProgressBar> bars = new EnumMap<>(WasteCategory.class);
    private final JLabel totalLabel = new JLabel("Unique objects: 0");
    private final JLabel fpsLabel = new JLabel("FPS: --");

    StatsPanel(double initialThreshold, DoubleConsumer onThresholdChange, Runnable onReset) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setPreferredSize(new Dimension(320, 480));

        JLabel title = new JLabel("Segregation results");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        addLeft(title);
        add(Box.createVerticalStrut(10));

        for (WasteCategory category : WasteCategory.values()) {
            addLeft(buildRow(category));
            add(Box.createVerticalStrut(8));
        }

        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 14f));
        addLeft(totalLabel);
        add(Box.createVerticalStrut(4));
        addLeft(fpsLabel);
        add(Box.createVerticalStrut(14));

        JLabel thresholdLabel = new JLabel(String.format("Confidence threshold: %.2f", initialThreshold));
        addLeft(thresholdLabel);
        JSlider slider = new JSlider(0, 100, (int) Math.round(initialThreshold * 100));
        slider.addChangeListener(e -> {
            double threshold = slider.getValue() / 100.0;
            thresholdLabel.setText(String.format("Confidence threshold: %.2f", threshold));
            onThresholdChange.accept(threshold);
        });
        addLeft(slider);
        add(Box.createVerticalStrut(10));

        JButton reset = new JButton("Reset unique count");
        reset.addActionListener(e -> onReset.run());
        addLeft(reset);
        add(Box.createVerticalGlue());
    }

    private void addLeft(JComponent component) {
        component.setAlignmentX(LEFT_ALIGNMENT);
        add(component);
    }

    private JPanel buildRow(WasteCategory category) {
        JPanel row = new JPanel(new BorderLayout(6, 2));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        JLabel name = new JLabel(category.getDisplayName());
        name.setOpaque(true);
        name.setBackground(CategoryColors.of(category));
        name.setForeground(Color.BLACK);
        name.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        name.setPreferredSize(new Dimension(80, 20));

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setForeground(CategoryColors.of(category));
        bars.put(category, bar);

        JLabel counts = new JLabel("now 0  |  unique 0");
        counts.setFont(counts.getFont().deriveFont(12f));
        countLabels.put(category, counts);

        row.add(name, BorderLayout.WEST);
        row.add(bar, BorderLayout.CENTER);
        row.add(counts, BorderLayout.SOUTH);
        return row;
    }

    void update(SegregationResult result, Map<WasteCategory, Integer> unique, int totalUnique, double fps) {
        for (WasteCategory category : WasteCategory.values()) {
            bars.get(category).setValue((int) Math.round(result.getPercentage(category)));
            countLabels.get(category).setText(String.format("now %d  |  unique %d",
                    result.getCount(category), unique.get(category)));
        }
        totalLabel.setText("Unique objects: " + totalUnique);
        fpsLabel.setText(String.format("FPS: %.0f", fps));
    }
}
