package com.riverdetect;

import com.riverdetect.models.WasteCategory;
import com.riverdetect.models.WasteObject;
import com.riverdetect.segregation.SegregationEngine;
import com.riverdetect.segregation.SegregationResult;
import com.riverdetect.tracking.ObjectTracker;
import com.riverdetect.tracking.Track;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("River Waste Detection System starting...");

        SegregationEngine engine = new SegregationEngine(0.5);
        ObjectTracker tracker = new ObjectTracker();
        int perFrameTotal = 0;

        for (int f = 0; f < 10; f++) {
            List<WasteObject> frame = new ArrayList<>();
            frame.add(new WasteObject(WasteCategory.PLASTIC, 0.90, 100 + 5 * f, 100, 60, 40));
            if (f >= 3) {
                frame.add(new WasteObject(WasteCategory.METAL, 0.80, 300, 200, 40, 40));
            }
            if (f == 5) {
                frame.add(new WasteObject(WasteCategory.GLASS, 0.80, 450, 50, 30, 30));
            }

            SegregationResult result = engine.segregate(frame);
            List<WasteObject> accepted = result.getAllBins().values().stream()
                    .flatMap(List::stream).toList();
            List<Track> visible = tracker.update(accepted);

            perFrameTotal += result.getTotalCount();
            System.out.printf("Frame %d: detected=%d, confirmed tracks=%d%n",
                    f, result.getTotalCount(), visible.size());
        }

        System.out.println("Per-frame counting total: " + perFrameTotal);
        System.out.println("Unique objects (tracked): " + tracker.getTotalUniqueCount());
        for (WasteCategory c : WasteCategory.values()) {
            System.out.printf("%-8s %d%n", c.getDisplayName(), tracker.getUniqueCount(c));
        }
    }
}
