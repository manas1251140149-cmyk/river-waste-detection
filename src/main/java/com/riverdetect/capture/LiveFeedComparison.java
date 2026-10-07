package com.riverdetect.capture;

import com.riverdetect.preprocessing.ImageUtils;
import com.riverdetect.preprocessing.PreprocessingConfig;
import com.riverdetect.preprocessing.PreprocessingModule;
import org.opencv.core.Mat;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Standalone test: live webcam feed, raw (left) vs preprocessed (right).
 *
 * Run it from the project root:
 *   mvn compile exec:java -Dexec.mainClass=com.riverdetect.capture.LiveFeedComparison
 * (or just press Run on this class in your IDE)
 *
 * Checkboxes at the bottom switch each preprocessing step on/off live.
 * Press the window's close button to quit.
 */
public class LiveFeedComparison {

    private static final int TARGET_FPS = 30;

    public static void main(String[] args) {
        ImageCaptureModule camera = new ImageCaptureModule();
        if (!camera.open()) {
            System.err.println("Could not open the webcam. Close other apps using it "
                    + "(Zoom, Teams, browser tabs) and check camera permissions.");
            return;
        }

        PreprocessingConfig config = new PreprocessingConfig();
        PreprocessingModule preprocessor = new PreprocessingModule(config);

        SwingUtilities.invokeLater(() -> buildAndShowWindow(camera, preprocessor, config));
    }

    private static void buildAndShowWindow(ImageCaptureModule camera,
                                           PreprocessingModule preprocessor,
                                           PreprocessingConfig config) {
        JFrame frame = new JFrame("Raw vs Preprocessed");
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        ImagePanel rawPanel = new ImagePanel("Raw");
        ImagePanel procPanel = new ImagePanel("Preprocessed");
        JLabel status = new JLabel(" ");

        JPanel views = new JPanel(new GridLayout(1, 2, 8, 0));
        views.add(rawPanel);
        views.add(procPanel);

        // checkboxes: one per step, applied live
        JCheckBox resizeBox = new JCheckBox("Resize 640x480", config.isResizeEnabled());
        JCheckBox blurBox = new JCheckBox("Gaussian blur", config.isBlurEnabled());
        JCheckBox claheBox = new JCheckBox("CLAHE", config.isClaheEnabled());
        resizeBox.addActionListener(e -> config.setResizeEnabled(resizeBox.isSelected()));
        blurBox.addActionListener(e -> config.setBlurEnabled(blurBox.isSelected()));
        claheBox.addActionListener(e -> config.setClaheEnabled(claheBox.isSelected()));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(resizeBox);
        controls.add(blurBox);
        controls.add(claheBox);
        controls.add(status);

        frame.setLayout(new BorderLayout());
        frame.add(views, BorderLayout.CENTER);
        frame.add(controls, BorderLayout.SOUTH);
        frame.setSize(1340, 560);
        frame.setLocationRelativeTo(null);

        // Capture loop on a background thread so the UI never freezes.
        final boolean[] running = {true};
        Thread loop = new Thread(() -> {
            long frameTime = 1000L / TARGET_FPS;
            long lastFpsStamp = System.currentTimeMillis();
            int frames = 0;
            double fps = 0;

            while (running[0]) {
                long start = System.currentTimeMillis();

                Mat raw = camera.getFrame();
                if (raw != null) {
                    java.util.List<String> timings = new java.util.ArrayList<>();
                    Mat processed = preprocessor.processTimed(raw, timings);

                    BufferedImage rawImg = ImageUtils.matToBufferedImage(raw);
                    BufferedImage procImg = ImageUtils.matToBufferedImage(processed);
                    String info = String.format("raw %dx%d | out %dx%d | %.0f FPS | %s",
                            raw.cols(), raw.rows(),
                            processed.cols(), processed.rows(),
                            fps, String.join(" ", timings));

                    raw.release();
                    if (processed != null) {
                        processed.release();
                    }

                    SwingUtilities.invokeLater(() -> {
                        rawPanel.setImage(rawImg);
                        procPanel.setImage(procImg);
                        status.setText(info);
                    });

                    frames++;
                    long now = System.currentTimeMillis();
                    if (now - lastFpsStamp >= 1000) {
                        fps = frames * 1000.0 / (now - lastFpsStamp);
                        frames = 0;
                        lastFpsStamp = now;
                    }
                }

                long sleep = frameTime - (System.currentTimeMillis() - start);
                if (sleep > 0) {
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }, "capture-loop");
        loop.setDaemon(true);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                running[0] = false;
                try {
                    loop.join(1000);
                } catch (InterruptedException ignored) { }
                camera.close();
                frame.dispose();
                System.exit(0);
            }
        });

        frame.setVisible(true);
        loop.start();
    }

    /** Simple panel that draws one image scaled to fit, with a title. */
    private static class ImagePanel extends JPanel {
        private final String title;
        private volatile BufferedImage image;

        ImagePanel(String title) {
            this.title = title;
            setBackground(Color.BLACK);
            setPreferredSize(new Dimension(640, 480));
        }

        void setImage(BufferedImage img) {
            this.image = img;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            BufferedImage img = image;
            if (img != null) {
                double scale = Math.min((double) getWidth() / img.getWidth(),
                        (double) getHeight() / img.getHeight());
                int w = (int) (img.getWidth() * scale);
                int h = (int) (img.getHeight() * scale);
                g.drawImage(img, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
            }
            g.setColor(Color.YELLOW);
            g.drawString(title, 8, 16);
        }
    }
}
