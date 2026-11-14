package com.mycompany.refaccionaria.automotriz.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class ImageBackgroundPanel extends JPanel {

    private final BufferedImage image;
    private final Color overlay;

    public ImageBackgroundPanel(String resourcePath, Color overlay) {
        image = ResourceImages.loadImage(resourcePath);
        this.overlay = overlay;
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        double scale = Math.max(
                (double) getWidth() / image.getWidth(),
                (double) getHeight() / image.getHeight()
        );
        int width = (int) Math.ceil(image.getWidth() * scale);
        int height = (int) Math.ceil(image.getHeight() * scale);
        int x = (getWidth() - width) / 2;
        int y = (getHeight() - height) / 2;
        g2.drawImage(image, x, y, width, height, this);

        g2.setColor(overlay);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }
}
