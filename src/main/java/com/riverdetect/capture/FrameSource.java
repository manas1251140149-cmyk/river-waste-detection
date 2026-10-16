package com.riverdetect.capture;

import org.opencv.core.Mat;

/**
 * Anything that can hand out camera frames: the laptop webcam, or frames
 * arriving over the network from the Raspberry Pi.
 *
 * The caller owns every Mat returned by getFrame() and must release() it.
 */
public interface FrameSource extends AutoCloseable {

    /** Starts the source. @return true if it is ready. */
    boolean open();

    /** @return the next frame (BGR), or null if none is available right now. */
    Mat getFrame();

    @Override
    void close();
}
