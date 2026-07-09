package com.velora.ui;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

final class CustomerFooter extends JPanel {

    private static final Color BACKGROUND = new Color(1, 7, 11);
    private static final Color BORDER = new Color(214, 160, 66, 34);
    private static final Color GOLD = new Color(214, 160, 66);
    private static final Color TEXT = new Color(232, 236, 242);
    private static final Color MUTED = new Color(158, 166, 178);

    CustomerFooter() {
        setOpaque(true);
        setBackground(BACKGROUND);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1200, 58));
        setMinimumSize(new Dimension(900, 54));
        setBorder(new EmptyBorder(0, 14, 0, 14));

        JLabel left = label("© 2026 Velora Motors. All rights reserved.", 10, Font.PLAIN, MUTED);
        left.setVerticalAlignment(SwingConstants.CENTER);

        BrandSignature brand = new BrandSignature();
        brand.setPreferredSize(new Dimension(360, 56));

        JLabel right = label("<html>Drive Luxury. Drive <font color='#D6A042'><b>BMW.</b></font></html>",
                12, Font.PLAIN, new Color(198, 207, 224));
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        right.setVerticalAlignment(SwingConstants.CENTER);

        add(left, BorderLayout.WEST);
        add(brand, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);
    }

    @Override
    protected void paintComponent(Graphics raw) {
        super.paintComponent(raw);
        Graphics2D g = (Graphics2D) raw.create();
        g.setColor(BORDER);
        g.drawLine(0, 0, getWidth(), 0);
        g.dispose();
    }

    private static JLabel label(String text, int size, int style, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", style, size));
        label.setForeground(color);
        return label;
    }

    private static final class BrandSignature extends JComponent {

        private final BufferedImage logo = loadImage("/assets/icons/velora-logo-gold.png");

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int cx = getWidth() / 2 - 62;
            int cy = getHeight() / 2 + 1;

            if (logo != null) {
                g.drawImage(logo, cx - 52, cy - 18, 100, 36, null);
            } else {
                drawWingLogo(g, cx, cy, 86);
            }

            g.setColor(TEXT);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            g.drawString("VELORA MOTORS", cx + 64, cy - 3);

            g.setColor(MUTED);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 8));
            g.drawString("PREMIUM BMW VEHICLE RENTAL", cx + 66, cy + 13);
            g.dispose();
        }
    }

    private static BufferedImage loadImage(String resource) {
        try {
            if (CustomerFooter.class.getResource(resource) != null) {
                return ImageIO.read(CustomerFooter.class.getResource(resource));
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    private static void drawWingLogo(Graphics2D g, int cx, int cy, int width) {
        int half = width / 2;
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(GOLD);

        for (int i = 0; i < 4; i++) {
            int inset = i * 8;
            int y = cy - 10 + i * 5;
            g.drawLine(cx - 8 - inset, y, cx - half + inset / 2, y - 2);
            g.drawLine(cx + 8 + inset, y, cx + half - inset / 2, y - 2);
        }

        Path2D mark = new Path2D.Double();
        mark.moveTo(cx - 7, cy - 13);
        mark.lineTo(cx, cy + 18);
        mark.lineTo(cx + 7, cy - 13);
        mark.lineTo(cx + 2, cy - 13);
        mark.lineTo(cx, cy + 4);
        mark.lineTo(cx - 2, cy - 13);
        mark.closePath();
        g.fill(mark);

        g.setColor(new Color(246, 205, 127, 150));
        g.drawLine(cx - half, cy - 15, cx - 15, cy - 12);
        g.drawLine(cx + 15, cy - 12, cx + half, cy - 15);
    }
}
