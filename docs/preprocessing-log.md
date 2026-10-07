# Preprocessing Log (feeds the paper's methodology section)

Owner: P1 (Manasi). Update this after every experiment. Keep entries short and factual.

## Pipeline (in order)

| # | Step | Purpose | Parameters (defaults) | Config switch |
|---|------|---------|-----------------------|---------------|
| 1 | Resize | Fixed input size for the detector and consistent timing | 640x480; INTER_AREA when shrinking, INTER_LINEAR when enlarging | `setResizeEnabled` |
| 2 | Gaussian blur | Suppress sensor noise and small water ripples/glare speckle that could trigger false detections | 5x5 kernel, sigma 0 (auto) | `setBlurEnabled` |
| 3 | CLAHE (optional) | Local contrast enhancement for dull or hazy river scenes | clip limit 2.0, 8x8 tiles, applied to the L channel in LAB so colors are not shifted | `setClaheEnabled` |

Default state: resize ON, blur ON, CLAHE OFF.

## What each step does (draft methodology wording)

- **Resize:** Frames are rescaled to 640x480 so every image reaches the detector at the same resolution. Area interpolation is used when downscaling to limit aliasing.
- **Gaussian blur:** A 5x5 Gaussian filter smooths high-frequency noise. Trade-off: it also softens edges of small objects, which may reduce recall on small waste items. Needs testing.
- **CLAHE:** Contrast-limited adaptive histogram equalisation boosts local contrast. Applied only to lightness in LAB space to avoid colour distortion. Trade-off: can amplify noise in flat regions, which is why it is paired with blur and limited by a clip limit.

## Experiment configurations to compare

| ID | Resize | Blur | CLAHE | Notes |
|----|--------|------|-------|-------|
| C0 | off | off | off | Baseline (raw frame) |
| C1 | on | off | off | Resize only |
| C2 | on | on | off | Default pipeline |
| C3 | on | off | on | Resize + CLAHE |
| C4 | on | on | on | Full pipeline |

## Observation log

Fill in as you test. Include lighting, scene, and what you noticed.

| Date | Config | Scene / lighting | Observation | Per-step time (ms) | Detector result (mAP / count) |
|------|--------|------------------|-------------|--------------------|-------------------------------|
| 2026-10-06 | | | (pending first live test) | | |

## Open questions

- Does blur kernel size (3 vs 5 vs 7) change detection of small items?
- Does CLAHE clip limit 2.0 vs 3.0 help on overcast footage?
- Does preprocessing time matter relative to the HTTP round trip to the detector?
