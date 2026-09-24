package com.voting.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;

/**
 * Renders a candidate's photo scaled into a circle, or - if no photo is
 * available - a colored circle with their initials. Means the app looks
 * complete even when nobody has uploaded a picture yet.
 */
public class AvatarIcon implements Icon {

    private final int size;
    private final String initials;
    private final Color bg;
    private Image image;

    public AvatarIcon(String photoPath, String fullName, int size) {
        this.size = size;
        this.initials = computeInitials(fullName);
        this.bg = colorFor(fullName);
        if (photoPath != null && !photoPath.isBlank()) {
            try {
                File f = new File(photoPath);
                if (f.exists()) {
                    image = ImageIO.read(f);
                }
            } catch (IOException e) {
                image = null;
            }
        }
    }

    private static String computeInitials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(2, parts.length); i++) {
            sb.append(Character.toUpperCase(parts[i].charAt(0)));
        }
        return sb.toString();
    }

    private static Color colorFor(String name) {
        Color[] palette = {
                new Color(0x1F4E8C), new Color(0x2E9E5B), new Color(0xC0392B),
                new Color(0xE08E0B), new Color(0x6C3EA6), new Color(0x0F8FA9)
        };
        int hash = (name == null) ? 0 : Math.abs(name.hashCode());
        return palette[hash % palette.length];
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Ellipse2D clip = new Ellipse2D.Float(x, y, size, size);
        g2.setClip(clip);

        if (image != null) {
            g2.drawImage(image, x, y, size, size, null);
        } else {
            g2.setColor(bg);
            g2.fill(clip);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, size / 3));
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (size - fm.stringWidth(initials)) / 2;
            int ty = y + (size - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(initials, tx, ty);
        }

        g2.setClip(null);
        g2.setColor(UITheme.BORDER);
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(clip);
        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }
}
