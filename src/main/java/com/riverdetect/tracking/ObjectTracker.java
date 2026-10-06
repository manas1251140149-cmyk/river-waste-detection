package com.riverdetect.tracking;

import com.riverdetect.models.WasteCategory;
import com.riverdetect.models.WasteObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ObjectTracker {
    private final double iouThreshold;
    private final int maxMissedFrames;
    private final int minHitsToConfirm;

    private final List<Track> tracks = new ArrayList<>();
    private final Map<WasteCategory, Integer> uniqueCounts = new EnumMap<>(WasteCategory.class);
    private int nextId = 1;

    private record Match(Track track, int detectionIndex, double iou) {}

    public ObjectTracker() {
        this(0.3, 5, 3);
    }

    public ObjectTracker(double iouThreshold, int maxMissedFrames, int minHitsToConfirm) {
        if (iouThreshold <= 0.0 || iouThreshold > 1.0) {
            throw new IllegalArgumentException("iouThreshold must be in (0, 1]");
        }
        if (maxMissedFrames < 0 || minHitsToConfirm < 1) {
            throw new IllegalArgumentException("invalid maxMissedFrames or minHitsToConfirm");
        }
        this.iouThreshold = iouThreshold;
        this.maxMissedFrames = maxMissedFrames;
        this.minHitsToConfirm = minHitsToConfirm;
        resetCounts();
    }

    public List<Track> update(List<WasteObject> detections) {
        List<Match> candidates = new ArrayList<>();
        for (Track track : tracks) {
            for (int i = 0; i < detections.size(); i++) {
                WasteObject d = detections.get(i);
                if (d.getCategory() != track.getLatest().getCategory()) {
                    continue;
                }
                double overlap = computeIou(track.getLatest(), d);
                if (overlap >= iouThreshold) {
                    candidates.add(new Match(track, i, overlap));
                }
            }
        }
        candidates.sort(Comparator.comparingDouble(Match::iou).reversed());

        Set<Track> matchedTracks = new HashSet<>();
        Set<Integer> matchedDetections = new HashSet<>();
        for (Match m : candidates) {
            if (matchedTracks.contains(m.track()) || matchedDetections.contains(m.detectionIndex())) {
                continue;
            }
            matchedTracks.add(m.track());
            matchedDetections.add(m.detectionIndex());
            m.track().markMatched(detections.get(m.detectionIndex()));
            if (!m.track().isConfirmed() && m.track().getHits() >= minHitsToConfirm) {
                confirmTrack(m.track());
            }
        }

        Iterator<Track> it = tracks.iterator();
        while (it.hasNext()) {
            Track track = it.next();
            if (!matchedTracks.contains(track)) {
                track.markMissed();
                if (track.getMissed() > maxMissedFrames) {
                    it.remove();
                }
            }
        }

        for (int i = 0; i < detections.size(); i++) {
            if (matchedDetections.contains(i)) {
                continue;
            }
            Track track = new Track(nextId++, detections.get(i));
            tracks.add(track);
            if (track.getHits() >= minHitsToConfirm) {
                confirmTrack(track);
            }
        }

        List<Track> visible = new ArrayList<>();
        for (Track track : tracks) {
            if (track.isConfirmed() && track.getMissed() == 0) {
                visible.add(track);
            }
        }
        return visible;
    }

    public int getUniqueCount(WasteCategory category) {
        return uniqueCounts.get(category);
    }

    public int getTotalUniqueCount() {
        int total = 0;
        for (int count : uniqueCounts.values()) {
            total += count;
        }
        return total;
    }

    public void reset() {
        tracks.clear();
        nextId = 1;
        resetCounts();
    }

    private void resetCounts() {
        for (WasteCategory category : WasteCategory.values()) {
            uniqueCounts.put(category, 0);
        }
    }

    private void confirmTrack(Track track) {
        track.confirm();
        uniqueCounts.merge(track.getLatest().getCategory(), 1, Integer::sum);
    }

    static double computeIou(WasteObject a, WasteObject b) {
        int x1 = Math.max(a.getX(), b.getX());
        int y1 = Math.max(a.getY(), b.getY());
        int x2 = Math.min(a.getX() + a.getWidth(), b.getX() + b.getWidth());
        int y2 = Math.min(a.getY() + a.getHeight(), b.getY() + b.getHeight());
        double intersection = (double) Math.max(0, x2 - x1) * Math.max(0, y2 - y1);
        double union = (double) a.getWidth() * a.getHeight()
                + (double) b.getWidth() * b.getHeight() - intersection;
        return union <= 0 ? 0.0 : intersection / union;
    }
}
