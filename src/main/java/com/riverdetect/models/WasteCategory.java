package com.riverdetect.models;

public enum WasteCategory {
    PLASTIC("Plastic"),
    METAL("Metal"),
    GLASS("Glass"),
    PAPER("Paper"),
    ORGANIC("Organic"),
    OTHER("Other");

    private final String displayName;

    WasteCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static WasteCategory fromLabel(String label) {
        if (label == null) {
            return OTHER;
        }
        try {
            return valueOf(label.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return OTHER;
        }
    }
}
