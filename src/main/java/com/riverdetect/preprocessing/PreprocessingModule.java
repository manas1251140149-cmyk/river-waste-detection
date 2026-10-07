package com.riverdetect.preprocessing;

import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Applies the preprocessing pipeline to one frame:
 *
 *   resize  ->  Gaussian blur  ->  CLAHE contrast enhancement
 *
 * Each step is switched on/off through PreprocessingConfig.
 *
 * Ownership rule: process() NEVER modifies or releases the Mat you pass in.
 * It always returns a NEW Mat (even if every step is disabled), so the caller
 * should release() the result when finished.
 */
public class PreprocessingModule {

    static {
        OpenCV.loadLocally();
    }

    private PreprocessingConfig config;
    private CLAHE clahe; // cached, rebuilt when CLAHE parameters change
    private double claheBuiltClip = Double.NaN;
    private int claheBuiltTile = -1;

    public PreprocessingModule() {
        this(new PreprocessingConfig());
    }

    public PreprocessingModule(PreprocessingConfig config) {
        this.config = config;
    }

    public PreprocessingConfig getConfig() {
        return config;
    }

    public void setConfig(PreprocessingConfig config) {
        this.config = config;
    }

    /**
     * @param frame BGR frame from the camera (not modified)
     * @return a new, processed BGR Mat, or null if frame is null/empty
     */
    public Mat process(Mat frame) {
        if (frame == null || frame.empty()) {
            return null;
        }

        Mat current = frame.clone();

        if (config.isResizeEnabled()) {
            current = replace(current, resize(current));
        }
        if (config.isBlurEnabled()) {
            current = replace(current, blur(current));
        }
        if (config.isClaheEnabled()) {
            current = replace(current, applyClahe(current));
        }
        return current;
    }

    /**
     * Same as process() but also reports how long each enabled step took
     * (milliseconds). Useful for the "processing overhead" part of the paper.
     * Timings are appended to the supplied list as "stepName=ms" strings.
     */
    public Mat processTimed(Mat frame, List<String> timingsOut) {
        if (frame == null || frame.empty()) {
            return null;
        }
        Mat current = frame.clone();

        if (config.isResizeEnabled()) {
            long t = System.nanoTime();
            current = replace(current, resize(current));
            timingsOut.add("resize=" + ms(t));
        }
        if (config.isBlurEnabled()) {
            long t = System.nanoTime();
            current = replace(current, blur(current));
            timingsOut.add("blur=" + ms(t));
        }
        if (config.isClaheEnabled()) {
            long t = System.nanoTime();
            current = replace(current, applyClahe(current));
            timingsOut.add("clahe=" + ms(t));
        }
        return current;
    }

    // ------------------------------------------------------------------
    // individual steps (each returns a NEW Mat)
    // ------------------------------------------------------------------

    private Mat resize(Mat src) {
        Mat dst = new Mat();
        Size target = new Size(config.getTargetWidth(), config.getTargetHeight());
        // INTER_AREA is the recommended interpolation when shrinking an image
        // INTER_LINEAR is better when enlarging
        boolean shrinking = src.width() > target.width || src.height() > target.height;
        Imgproc.resize(src, dst, target, 0, 0,
                shrinking ? Imgproc.INTER_AREA : Imgproc.INTER_LINEAR);
        return dst;
    }

    private Mat blur(Mat src) {
        Mat dst = new Mat();
        int k = config.getBlurKernelSize();
        Imgproc.GaussianBlur(src, dst, new Size(k, k), config.getBlurSigma());
        return dst;
    }

    /**
     * CLAHE only works on single-channel images, so we convert BGR to LAB,
     * enhance just the lightness (L) channel, and convert back. This boosts
     * contrast without shifting the colors, which matters because the
     * detector may rely on color cues (e.g. plastic vs. vegetation).
     */
    private Mat applyClahe(Mat src) {
        ensureClahe();

        Mat lab = new Mat();
        Imgproc.cvtColor(src, lab, Imgproc.COLOR_BGR2Lab);

        List<Mat> channels = new ArrayList<>();
        org.opencv.core.Core.split(lab, channels);

        Mat enhancedL = new Mat();
        clahe.apply(channels.get(0), enhancedL);
        channels.get(0).release();
        channels.set(0, enhancedL);

        org.opencv.core.Core.merge(channels, lab);

        Mat dst = new Mat();
        Imgproc.cvtColor(lab, dst, Imgproc.COLOR_Lab2BGR);

        for (Mat m : channels) {
            m.release();
        }
        lab.release();
        return dst;
    }

    private void ensureClahe() {
        double clip = config.getClaheClipLimit();
        int tile = config.getClaheTileSize();
        if (clahe == null || clip != claheBuiltClip || tile != claheBuiltTile) {
            clahe = Imgproc.createCLAHE(clip, new Size(tile, tile));
            claheBuiltClip = clip;
            claheBuiltTile = tile;
        }
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    /** Releases the old Mat and hands back the new one. */
    private static Mat replace(Mat old, Mat fresh) {
        old.release();
        return fresh;
    }

    private static String ms(long startNanos) {
        return String.format("%.2f", (System.nanoTime() - startNanos) / 1_000_000.0);
    }
}
