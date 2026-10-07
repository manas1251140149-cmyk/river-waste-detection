package com.riverdetect.preprocessing;

/**
 * Switches and parameters for PreprocessingModule.
 *
 * Every step can be turned on/off on its own, so we can run clean
 * before/after comparisons for the paper. Setters return "this" so you
 * can chain them:
 *
 *   PreprocessingConfig cfg = new PreprocessingConfig()
 *       .setResizeEnabled(true)
 *       .setBlurEnabled(false)
 *       .setClaheEnabled(true);
 */
public class PreprocessingConfig {

    // ---- step switches ----
    private boolean resizeEnabled = true;
    private boolean blurEnabled = true;
    private boolean claheEnabled = false; // optional step, off by default

    // ---- resize ----
    private int targetWidth = 640;
    private int targetHeight = 480;

    // ---- Gaussian blur ----
    private int blurKernelSize = 5;   // must be odd and positive
    private double blurSigma = 0;     // 0 = OpenCV derives it from kernel size

    // ---- CLAHE (applied to the L channel in LAB color space) ----
    private double claheClipLimit = 2.0;
    private int claheTileSize = 8;    // tiles are claheTileSize x claheTileSize

    public PreprocessingConfig() { }

    /** Copy constructor, handy for building "variant" configs from one base. */
    public PreprocessingConfig(PreprocessingConfig other) {
        this.resizeEnabled = other.resizeEnabled;
        this.blurEnabled = other.blurEnabled;
        this.claheEnabled = other.claheEnabled;
        this.targetWidth = other.targetWidth;
        this.targetHeight = other.targetHeight;
        this.blurKernelSize = other.blurKernelSize;
        this.blurSigma = other.blurSigma;
        this.claheClipLimit = other.claheClipLimit;
        this.claheTileSize = other.claheTileSize;
    }

    // ---- getters / setters ----

    public boolean isResizeEnabled() { return resizeEnabled; }
    public PreprocessingConfig setResizeEnabled(boolean v) { this.resizeEnabled = v; return this; }

    public boolean isBlurEnabled() { return blurEnabled; }
    public PreprocessingConfig setBlurEnabled(boolean v) { this.blurEnabled = v; return this; }

    public boolean isClaheEnabled() { return claheEnabled; }
    public PreprocessingConfig setClaheEnabled(boolean v) { this.claheEnabled = v; return this; }

    public int getTargetWidth() { return targetWidth; }
    public int getTargetHeight() { return targetHeight; }
    public PreprocessingConfig setTargetSize(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Target size must be positive");
        }
        this.targetWidth = width;
        this.targetHeight = height;
        return this;
    }

    public int getBlurKernelSize() { return blurKernelSize; }
    public double getBlurSigma() { return blurSigma; }
    public PreprocessingConfig setBlur(int kernelSize, double sigma) {
        if (kernelSize <= 0 || kernelSize % 2 == 0) {
            throw new IllegalArgumentException("Blur kernel size must be odd and positive");
        }
        this.blurKernelSize = kernelSize;
        this.blurSigma = sigma;
        return this;
    }

    public double getClaheClipLimit() { return claheClipLimit; }
    public int getClaheTileSize() { return claheTileSize; }
    public PreprocessingConfig setClahe(double clipLimit, int tileSize) {
        if (clipLimit <= 0 || tileSize <= 0) {
            throw new IllegalArgumentException("CLAHE clip limit and tile size must be positive");
        }
        this.claheClipLimit = clipLimit;
        this.claheTileSize = tileSize;
        return this;
    }

    @Override
    public String toString() {
        return "PreprocessingConfig{resize=" + resizeEnabled
                + " (" + targetWidth + "x" + targetHeight + ")"
                + ", blur=" + blurEnabled
                + " (k=" + blurKernelSize + ", sigma=" + blurSigma + ")"
                + ", clahe=" + claheEnabled
                + " (clip=" + claheClipLimit + ", tile=" + claheTileSize + ")}";
    }
}
