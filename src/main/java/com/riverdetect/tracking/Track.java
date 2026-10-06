package com.riverdetect.tracking;

import com.riverdetect.models.WasteObject;

public class Track {
    private final int id;
    private WasteObject latest;
    private int hits;
    private int missed;
    private boolean confirmed;

    Track(int id, WasteObject first) {
        this.id = id;
        this.latest = first;
        this.hits = 1;
    }

    public int getId() { return id; }
    public WasteObject getLatest() { return latest; }
    public int getHits() { return hits; }
    public boolean isConfirmed() { return confirmed; }

    int getMissed() { return missed; }

    void markMatched(WasteObject detection) {
        this.latest = detection;
        this.hits++;
        this.missed = 0;
    }

    void markMissed() { this.missed++; }

    void confirm() { this.confirmed = true; }
}
