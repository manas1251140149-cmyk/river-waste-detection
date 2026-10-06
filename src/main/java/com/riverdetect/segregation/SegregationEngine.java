package com.riverdetect.segregation;

import com.riverdetect.models.WasteCategory;
import com.riverdetect.models.WasteObject;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class SegregationEngine {
    public static final double DEFAULT_CONFIDENCE_THRESHOLD = 0.5;

    private volatile double confidenceThreshold;

    public SegregationEngine() {
        this(DEFAULT_CONFIDENCE_THRESHOLD);
    }

    public SegregationEngine(double confidenceThreshold) {
        setConfidenceThreshold(confidenceThreshold);
    }

    public double getConfidenceThreshold() {
        return confidenceThreshold;
    }

    public void setConfidenceThreshold(double threshold) {
        if (threshold < 0.0 || threshold > 1.0) {
            throw new IllegalArgumentException("threshold must be between 0 and 1");
        }
        this.confidenceThreshold = threshold;
    }

    public SegregationResult segregate(List<WasteObject> detections) {
        Map<WasteCategory, List<WasteObject>> bins = new EnumMap<>(WasteCategory.class);
        for (WasteCategory category : WasteCategory.values()) {
            bins.put(category, new ArrayList<>());
        }
        double threshold = this.confidenceThreshold;
        if (detections != null) {
            for (WasteObject object : detections) {
                if (object.getConfidence() >= threshold) {
                    bins.get(object.getCategory()).add(object);
                }
            }
        }
        return new SegregationResult(bins);
    }
}
