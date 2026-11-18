package com.riverdetect.preprocessing;

import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * Conversion helpers shared by the detector client (JPEG bytes) and the UI
 * (BufferedImage).
 */
public final class ImageUtils {

    static {
        OpenCV.loadLocally();
    }

    /** JPEG quality used when sending frames to the detection service. */
    public static final int DEFAULT_JPEG_QUALITY = 90;

    private ImageUtils() { }

    /** Encodes a Mat as JPEG bytes using DEFAULT_JPEG_QUALITY. */
    public static byte[] matToJpegBytes(Mat mat) {
        return matToJpegBytes(mat, DEFAULT_JPEG_QUALITY);
    }

    /**
     * Encodes a Mat as JPEG bytes.
     *
     * @param quality 0-100, higher = better quality and bigger payload
     * @throws IllegalArgumentException if mat is null/empty
     * @throws IllegalStateException    if OpenCV fails to encode it
     */
    public static byte[] matToJpegBytes(Mat mat, int quality) {
        if (mat == null || mat.empty()) {
            throw new IllegalArgumentException("Cannot encode a null or empty Mat");
        }
        MatOfByte buffer = new MatOfByte();
        MatOfInt params = new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, quality);
        try {
            if (!Imgcodecs.imencode(".jpg", mat, buffer, params)) {
                throw new IllegalStateException("JPEG encoding failed");
            }
            return buffer.toArray();
        } finally {
            buffer.release();
            params.release();
        }
    }

    /**
     * Converts a BGR (3-channel) or grayscale (1-channel) Mat into a
     * BufferedImage that Swing/JavaFX code can draw.
     *
     * The returned image has its own copy of the pixel data, so it is safe
     * to release the Mat afterwards.
     */
    public static BufferedImage matToBufferedImage(Mat mat) {
        if (mat == null || mat.empty()) {
            return null;
        }
        int channels = mat.channels();
        int type;
        if (channels == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else if (channels == 3) {
            type = BufferedImage.TYPE_3BYTE_BGR; // matches OpenCV's BGR order
        } else {
            throw new IllegalArgumentException("Unsupported channel count: " + channels);
        }

        int width = mat.cols();
        int height = mat.rows();
        BufferedImage image = new BufferedImage(width, height, type);
        byte[] target = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

        // Mat.get() needs a contiguous Mat; clone() guarantees that.
        Mat contiguous = mat.isContinuous() ? mat : mat.clone();
        contiguous.get(0, 0, target);
        if (contiguous != mat) {
            contiguous.release();
        }
        return image;
    }
}
