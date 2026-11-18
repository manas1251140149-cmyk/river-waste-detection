package com.riverdetect.capture;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Receives camera frames sent over the network, e.g. from the Raspberry Pi.
 *
 * Starts a small HTTP server (built into Java, no extra libraries). The Pi
 * sends each frame as:   POST http://<this-laptop>:<port>/frame
 * with the JPEG image as the request body. The server keeps only the NEWEST
 * frame, so if the app is slower than the Pi, old frames are dropped instead
 * of piling up.
 *
 * Same methods as ImageCaptureModule: open(), getFrame(), close().
 *
 * Note: open() returns true as soon as the server is listening, it does not
 * wait for the Pi. getFrame() returns null until a frame arrives.
 */
public class RemoteFrameSource implements FrameSource {

    static {
        OpenCV.loadLocally();
    }

    /** Reject anything bigger than this (a 640x480 JPEG is ~50-150 KB). */
    private static final int MAX_BODY_BYTES = 8 * 1024 * 1024;

    private final int port;
    private final long frameTimeoutMs;

    private HttpServer server;
    private ExecutorService executor;

    private final Object lock = new Object();
    private byte[] latestJpeg;
    private long latestSeq = 0;
    private long returnedSeq = 0;
    private long framesReceived = 0;
    private volatile boolean running;

    /** Listens on port 8081, waits up to 2 s for a frame in getFrame(). */
    public RemoteFrameSource() {
        this(8081, 2000);
    }

    public RemoteFrameSource(int port) {
        this(port, 2000);
    }

    public RemoteFrameSource(int port, long frameTimeoutMs) {
        this.port = port;
        this.frameTimeoutMs = frameTimeoutMs;
    }

    @Override
    public boolean open() {
        close();
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0); // all network interfaces
        } catch (IOException e) {
            System.err.println("Could not listen on port " + port + ": " + e.getMessage()
                    + " (is another program already using it?)");
            server = null;
            return false;
        }
        executor = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "remote-frame-http");
            t.setDaemon(true);
            return t;
        });
        server.setExecutor(executor);
        server.createContext("/frame", this::handleFrame);
        running = true;
        server.start();
        return true;
    }

    public boolean isOpen() {
        return running && server != null;
    }

    public int getPort() {
        return port;
    }

    /** How many frames have arrived since open(). */
    public long getFramesReceived() {
        synchronized (lock) {
            return framesReceived;
        }
    }

    /**
     * @return the newest frame not returned yet (decoded, BGR), or null if
     *         none arrived within the timeout. The caller must release() it.
     */
    @Override
    public Mat getFrame() {
        byte[] jpeg;
        synchronized (lock) {
            long deadline = System.currentTimeMillis() + frameTimeoutMs;
            while (running && latestSeq == returnedSeq) {
                long left = deadline - System.currentTimeMillis();
                if (left <= 0) {
                    return null;
                }
                try {
                    lock.wait(left);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
            if (latestSeq == returnedSeq) {
                return null; // closed while waiting
            }
            jpeg = latestJpeg;
            returnedSeq = latestSeq;
        }

        MatOfByte buf = new MatOfByte(jpeg);
        try {
            Mat frame = Imgcodecs.imdecode(buf, Imgcodecs.IMREAD_COLOR);
            if (frame == null || frame.empty()) {
                if (frame != null) {
                    frame.release();
                }
                return null;
            }
            return frame;
        } finally {
            buf.release();
        }
    }

    @Override
    public void close() {
        running = false;
        if (server != null) {
            server.stop(0);
            server = null;
        }
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        synchronized (lock) {
            latestJpeg = null;
            latestSeq = 0;
            returnedSeq = 0;
            framesReceived = 0;
            lock.notifyAll();
        }
    }

    // ------------------------------------------------------------------

    private void handleFrame(HttpExchange ex) throws IOException {
        try {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                reply(ex, 405);
                return;
            }
            byte[] body = ex.getRequestBody().readNBytes(MAX_BODY_BYTES + 1);
            if (body.length > MAX_BODY_BYTES) {
                reply(ex, 413);
                return;
            }
            // every JPEG starts with FF D8
            if (body.length < 4 || (body[0] & 0xFF) != 0xFF || (body[1] & 0xFF) != 0xD8) {
                reply(ex, 400);
                return;
            }
            synchronized (lock) {
                latestJpeg = body;
                latestSeq++;
                framesReceived++;
                lock.notifyAll();
            }
            reply(ex, 204);
        } finally {
            ex.close();
        }
    }

    private static void reply(HttpExchange ex, int status) throws IOException {
        ex.sendResponseHeaders(status, -1);
    }
}
