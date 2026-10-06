package com.riverdetect;

import com.riverdetect.models.WasteCategory;
import com.riverdetect.models.WasteObject;

public class Main {
    public static void main(String[] args) {
        System.out.println("River Waste Detection System starting...");
        WasteObject sample = new WasteObject(WasteCategory.fromLabel("PLASTIC"), 0.87, 120, 80, 60, 45);
        System.out.println("Sample detection: " + sample);
    }
}
