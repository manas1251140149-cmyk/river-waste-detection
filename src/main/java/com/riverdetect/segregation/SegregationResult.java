package com.riverdetect.segregation;

import com.riverdetect.models.WasteCategory;
import com.riverdetect.models.WasteObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SegregationResult {
    private final Map<WasteCategory, List<WasteObject>> bins;
    private final int totalCount;

    SegregationResult(Map<WasteCategory, List<WasteObject>> source) {
        EnumMap<WasteCategory, List<WasteObject>> copy = new EnumMap<>(WasteCategory.class);
        int total = 0;
        for (WasteCategory category : WasteCategory.values()) {
            List<WasteObject> items = source.getOrDefault(category, Collections.emptyList());
            copy.put(category, Collections.unmodifiableList(new ArrayList<>(items)));
            total += items.size();
        }
        this.bins = Collections.unmodifiableMap(copy);
        this.totalCount = total;
    }

    public List<WasteObject> getBin(WasteCategory category) {
        return bins.get(category);
    }

    public Map<WasteCategory, List<WasteObject>> getAllBins() {
        return bins;
    }

    public int getCount(WasteCategory category) {
        return bins.get(category).size();
    }

    public int getTotalCount() {
        return totalCount;
    }

    public double getPercentage(WasteCategory category) {
        return totalCount == 0 ? 0.0 : 100.0 * getCount(category) / totalCount;
    }

    public Optional<WasteCategory> getDominantCategory() {
        WasteCategory best = null;
        int bestCount = 0;
        for (WasteCategory category : WasteCategory.values()) {
            if (getCount(category) > bestCount) {
                best = category;
                bestCount = getCount(category);
            }
        }
        return Optional.ofNullable(best);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SegregationResult[total=" + totalCount);
        for (WasteCategory category : WasteCategory.values()) {
            sb.append(", ").append(category).append("=").append(getCount(category));
        }
        return sb.append("]").toString();
    }
}
