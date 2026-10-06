package com.riverdetect.ui;

import com.riverdetect.models.WasteCategory;

import java.awt.Color;

final class CategoryColors {
    private CategoryColors() {
    }

    static Color of(WasteCategory category) {
        return switch (category) {
            case PLASTIC -> new Color(33, 150, 243);
            case METAL -> new Color(158, 158, 158);
            case GLASS -> new Color(0, 188, 212);
            case PAPER -> new Color(255, 193, 7);
            case ORGANIC -> new Color(76, 175, 80);
            case OTHER -> new Color(171, 71, 188);
        };
    }
}
