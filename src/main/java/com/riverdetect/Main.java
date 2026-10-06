package com.riverdetect;

import com.riverdetect.models.WasteCategory;
import com.riverdetect.models.WasteObject;
import com.riverdetect.segregation.SegregationEngine;
import com.riverdetect.segregation.SegregationResult;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("River Waste Detection System starting...");

        List<WasteObject> mock = List.of(
            new WasteObject(WasteCategory.PLASTIC, 0.91, 120, 80, 60, 45),
            new WasteObject(WasteCategory.PLASTIC, 0.78, 300, 150, 50, 40),
            new WasteObject(WasteCategory.METAL, 0.66, 410, 200, 35, 30),
            new WasteObject(WasteCategory.ORGANIC, 0.55, 80, 300, 70, 60),
            new WasteObject(WasteCategory.GLASS, 0.30, 500, 90, 25, 25),
            new WasteObject(WasteCategory.PLASTIC, 0.45, 250, 350, 40, 40)
        );

        SegregationEngine engine = new SegregationEngine(0.5);
        SegregationResult result = engine.segregate(mock);

        System.out.println("Detections in: " + mock.size() + ", accepted: " + result.getTotalCount());
        for (WasteCategory c : WasteCategory.values()) {
            System.out.printf("%-8s %d (%.1f%%)%n", c.getDisplayName(), result.getCount(c), result.getPercentage(c));
        }
        System.out.println("Dominant: " + result.getDominantCategory().map(WasteCategory::getDisplayName).orElse("none"));
    }
}
