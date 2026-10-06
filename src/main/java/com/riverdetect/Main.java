package com.riverdetect;

import com.riverdetect.models.WasteObject;
import com.riverdetect.segregation.SegregationEngine;
import com.riverdetect.segregation.SegregationResult;
import com.riverdetect.tracking.ObjectTracker;
import com.riverdetect.tracking.Track;
import com.riverdetect.ui.MainWindow;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::launch);
    }

    private static void launch() {
        SegregationEngine engine = new SegregationEngine(0.5);
        ObjectTracker tracker = new ObjectTracker();
        MockFeed feed = new MockFeed();

        MainWindow window = new MainWindow(
                engine.getConfidenceThreshold(),
                engine::setConfidenceThreshold,
                tracker::reset);
        window.setVisible(true);

        long[] lastTick = {System.nanoTime()};
        double[] fps = {0.0};

        Timer timer = new Timer(33, e -> {
            long now = System.nanoTime();
            double instant = 1e9 / Math.max(1, now - lastTick[0]);
            fps[0] = fps[0] == 0.0 ? instant : 0.9 * fps[0] + 0.1 * instant;
            lastTick[0] = now;

            feed.next();
            SegregationResult result = engine.segregate(feed.detections());
            List<WasteObject> accepted = result.getAllBins().values().stream()
                    .flatMap(List::stream).toList();
            List<Track> visible = tracker.update(accepted);
            window.update(feed.frame(), visible, result, tracker, fps[0]);
        });
        timer.start();
    }
}
