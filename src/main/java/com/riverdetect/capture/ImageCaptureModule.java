package com.riverdetect.capture;

import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

/**
 * Opens the default webcam and hands out frames.
 *
 * Usage:
 *   ImageCaptureModule cam = new ImageCaptureModule();
 *   if (cam.open()) {
 *       Mat frame = cam.getFrame();   // may be null
 *       if (frame != null) { ... frame.release(); }
 *   }
 *   cam.close();
 *
 * NOTE: the caller owns every Mat returned by getFrame() and should call
 * release() on it when done, otherwise native memory slowly leaks.
 */
public class ImageCaptureModule implements AutoCloseable {

    static {
        // Safe to call more than once; loads the native OpenCV library.
        OpenCV.loadLocally();
    }

    private final int cameraIndex;
    private VideoCapture capture;

    /** Uses the default webcam (index 0). */
    public ImageCaptureModule() {
        this(0);
    }

    public ImageCaptureModule(int cameraIndex) {
        this.cameraIndex = cameraIndex;
    }

    /**
     * Tries the default backend first. On Windows, if that fails,
     * retries with DirectShow (CAP_DSHOW).
     *
     * @return true if the camera is open and ready
     */
    public boolean open() {
        close(); // in case open() is called twice

        capture = new VideoCapture(cameraIndex);
        if (capture.isOpened()) {
            return true;
        }
        capture.release();

        if (isWindows()) {
            capture = new VideoCapture(cameraIndex, Videoio.CAP_DSHOW);
            if (capture.isOpened()) {
                return true;
            }
            capture.release();
        }

        capture = null;
        return false;
    }

    public boolean isOpen() {
        return capture != null && capture.isOpened();
    }

    /**
     * @return the next frame (BGR Mat), or null if the camera is closed
     *         or no frame could be read. Caller must release() it.
     */
    public Mat getFrame() {
        if (!isOpen()) {
            return null;
        }
        Mat frame = new Mat();
        if (!capture.read(frame) || frame.empty()) {
            frame.release();
            return null;
        }
        return frame;
    }

    public void close() {
        if (capture != null) {
            capture.release();
            capture = null;
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
