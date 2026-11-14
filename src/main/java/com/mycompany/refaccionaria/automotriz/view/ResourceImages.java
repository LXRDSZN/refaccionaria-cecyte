package com.mycompany.refaccionaria.automotriz.view;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public final class ResourceImages {

    private ResourceImages() {
    }

    public static BufferedImage loadImage(String resourcePath) {
        try (InputStream stream = ResourceImages.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalStateException("No se encontró el recurso: " + resourcePath);
            }
            BufferedImage image = ImageIO.read(stream);
            if (image == null) {
                throw new IllegalStateException("El recurso no es una imagen válida: " + resourcePath);
            }
            return image;
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo cargar el recurso: " + resourcePath, exception);
        }
    }

    public static ImageIcon scaledIcon(String resourcePath, int width, int height) {
        Image scaledImage = loadImage(resourcePath).getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }
}
