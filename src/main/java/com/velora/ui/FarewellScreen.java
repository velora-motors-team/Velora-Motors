package com.velora.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LinearGradientPaint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

/**
 * Premium logout transition shown after a user closes a Velora session.
 */
public final class FarewellScreen extends JFrame {

    private static final Color GOLD = new Color(216, 157, 53);
    private static final Color GOLD_LIGHT = new Color(255, 218, 132);

    private int progress;
    private Timer loadingTimer;
    private final FarewellCanvas canvas;
    private Rectangle restoreBounds;

    public static void main(String[] args) {
        String previewName = args.length > 0 ? args[0] : "Hamada";
        SwingUtilities.invokeLater(() -> new FarewellScreen(previewName).setVisible(true));
    }

    public FarewellScreen(String userName) {
        super("Velora Motors - Logout");

        canvas = new FarewellCanvas(userName);
        canvas.setLayout(null);

        setUndecorated(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 680));
        setSize(1440, 850);
        setLocationRelativeTo(null);
        setContentPane(canvas);
        installWindowControls();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        startLoading();
    }

    private void installWindowControls() {
        LogoutWindowButton minimize = new LogoutWindowButton("MINIMIZE");
        LogoutWindowButton maximize = new LogoutWindowButton("MAXIMIZE");
        LogoutWindowButton close = new LogoutWindowButton("CLOSE");

        minimize.setToolTipText("Minimize");
        maximize.setToolTipText("Maximize / Restore");
        close.setToolTipText("Close");

        minimize.addActionListener(e -> setExtendedState(JFrame.ICONIFIED));
        maximize.addActionListener(e -> toggleMaximized());
        close.addActionListener(e -> returnToLogin());

        canvas.add(minimize);
        canvas.add(maximize);
        canvas.add(close);

        Runnable positionControls = () -> {
            int top = Math.max(14, Math.round(canvas.getHeight() * .026f));
            int size = Math.max(42, Math.round(canvas.getHeight() * .048f));
            int right = Math.max(26, Math.round(canvas.getWidth() * .025f));
            close.setBounds(canvas.getWidth() - right - size, top, size, size);
            maximize.setBounds(canvas.getWidth() - right - size * 2 - 7, top, size, size);
            minimize.setBounds(canvas.getWidth() - right - size * 3 - 14, top, size, size);
        };

        canvas.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                positionControls.run();
            }
        });
        SwingUtilities.invokeLater(positionControls);
    }

    private void toggleMaximized() {
        if ((getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
            setExtendedState(JFrame.NORMAL);
            if (restoreBounds != null) {
                setBounds(restoreBounds);
            }
        } else {
            restoreBounds = getBounds();
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }

    private void startLoading() {
        loadingTimer = new Timer(24, (ActionEvent e) -> {
            progress = Math.min(100, progress + 1);
            canvas.setProgress(progress);
            if (progress >= 100) {
                loadingTimer.stop();
                Timer returnTimer = new Timer(1150, event -> returnToLogin());
                returnTimer.setRepeats(false);
                returnTimer.start();
            }
        });
        loadingTimer.start();
    }

    private void returnToLogin() {
        if (loadingTimer != null) {
            loadingTimer.stop();
        }
        dispose();
        new LoginScreen().setVisible(true);
    }

    private static final class FarewellCanvas extends JPanel {

        private static final int DESIGN_W = 1920;
        private static final int DESIGN_H = 1080;
        private static final Color WHITE = new Color(246, 247, 249);
        private static final Color MUTED = new Color(202, 205, 211);

        private final String userName;
        private final BufferedImage background;
        private final BufferedImage logo;
        private int progress;

        FarewellCanvas(String userName) {
            this.userName = formatName(userName);
            background = loadImage("/images/velora-logout-clean-v2.png");
            logo = loadImage("/assets/icons/velora-logo-gold.png");
            setOpaque(true);
            setBackground(new Color(1, 4, 8));
        }

        private static String formatName(String value) {
            if (value == null || value.isBlank()) {
                return "Guest";
            }
            String name = value.trim();
            if (name.contains("@")) {
                name = name.substring(0, name.indexOf('@'));
            }
            StringBuilder formatted = new StringBuilder();
            for (String word : name.split("\\s+")) {
                if (!formatted.isEmpty()) {
                    formatted.append(' ');
                }
                if (word.length() == 1) {
                    formatted.append(word.toUpperCase());
                } else {
                    formatted.append(Character.toUpperCase(word.charAt(0)))
                            .append(word.substring(1));
                }
            }
            return formatted.isEmpty() ? "Guest" : formatted.toString();
        }

        void setProgress(int value) {
            progress = Math.max(0, Math.min(100, value));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics raw) {
            super.paintComponent(raw);
            Graphics2D g = (Graphics2D) raw.create();
            quality(g);

            int w = getWidth();
            int h = getHeight();
            float sx = w / (float) DESIGN_W;
            float sy = h / (float) DESIGN_H;
            float scale = Math.min(sx, sy);

            drawBackground(g, w, h);
            drawAtmosphere(g, w, h);
            drawOuterFrame(g, w, h, scale);
            drawBrand(g, w, sx, sy, scale);
            drawMessage(g, w, sx, sy, scale);
            drawBenefits(g, w, sx, sy, scale);
            drawProgress(g, w, sx, sy, scale);
            drawTagline(g, w, sx, sy, scale);
            g.dispose();
        }

        private void drawBackground(Graphics2D g, int w, int h) {
            if (background == null) {
                g.setPaint(new GradientPaint(0, 0, new Color(5, 11, 17), w, h, Color.BLACK));
                g.fillRect(0, 0, w, h);
                return;
            }
            double scale = Math.max(
                    w / (double) background.getWidth(),
                    h / (double) background.getHeight()
            );
            int imageW = (int) Math.ceil(background.getWidth() * scale);
            int imageH = (int) Math.ceil(background.getHeight() * scale);
            int x = (w - imageW) / 2;
            int y = (h - imageH) / 2;
            g.drawImage(background, x, y, imageW, imageH, null);
        }

        private void drawAtmosphere(Graphics2D g, int w, int h) {
            g.setPaint(new LinearGradientPaint(
                    0, 0, 0, h,
                    new float[]{0f, .40f, 1f},
                    new Color[]{
                        new Color(0, 0, 0, 26),
                        new Color(0, 0, 0, 54),
                        new Color(0, 0, 0, 152)
                    }
            ));
            g.fillRect(0, 0, w, h);

            RadialGradientPaint focus = new RadialGradientPaint(
                    new Point(w / 2, (int) (h * .44)),
                    Math.max(w, h) * .47f,
                    new float[]{0f, .58f, 1f},
                    new Color[]{
                        new Color(0, 0, 0, 26),
                        new Color(0, 0, 0, 74),
                        new Color(0, 0, 0, 188)
                    }
            );
            g.setPaint(focus);
            g.fillRect(0, 0, w, h);
        }

        private void drawOuterFrame(Graphics2D g, int w, int h, float scale) {
            int inset = Math.max(8, Math.round(13 * scale));
            int arc = Math.max(20, Math.round(28 * scale));
            RoundRectangle2D frame = new RoundRectangle2D.Double(
                    inset, inset, w - inset * 2 - 1, h - inset * 2 - 1, arc, arc
            );
            g.setStroke(new BasicStroke(Math.max(1f, 1.2f * scale)));
            g.setColor(new Color(214, 160, 66, 150));
            g.draw(frame);
            g.setColor(new Color(255, 225, 150, 24));
            g.draw(new RoundRectangle2D.Double(
                    inset + 4, inset + 4, w - inset * 2 - 9, h - inset * 2 - 9,
                    Math.max(15, arc - 7), Math.max(15, arc - 7)
            ));
        }

        private void drawBrand(Graphics2D g, int w, float sx, float sy, float scale) {
            int logoW = Math.round(230 * scale);
            int logoH = Math.round(94 * scale);
            int logoX = w / 2 - logoW / 2;
            int logoY = Math.round(65 * sy);
            if (logo != null) {
                g.drawImage(logo.getScaledInstance(logoW, logoH, Image.SCALE_SMOOTH),
                        logoX, logoY, null);
            }

            g.setFont(font("Segoe UI", Font.PLAIN, 35, scale));
            g.setColor(WHITE);
            drawCentered(g, "V E L O R A   M O T O R S", w / 2, Math.round(218 * sy));

            g.setFont(font("Segoe UI", Font.PLAIN, 16, scale));
            g.setColor(GOLD);
            drawCentered(g, "P R E M I U M   B M W   V E H I C L E   R E N T A L",
                    w / 2, Math.round(255 * sy));

            int dividerY = Math.round(294 * sy);
            int dividerHalf = Math.round(170 * sx);
            g.setStroke(new BasicStroke(Math.max(1f, scale)));
            g.setColor(new Color(214, 160, 66, 135));
            g.drawLine(w / 2 - dividerHalf, dividerY, w / 2 - Math.round(18 * sx), dividerY);
            g.drawLine(w / 2 + Math.round(18 * sx), dividerY, w / 2 + dividerHalf, dividerY);
            Path2D diamond = new Path2D.Double();
            int d = Math.max(4, Math.round(6 * scale));
            diamond.moveTo(w / 2, dividerY - d);
            diamond.lineTo(w / 2 + d, dividerY);
            diamond.lineTo(w / 2, dividerY + d);
            diamond.lineTo(w / 2 - d, dividerY);
            diamond.closePath();
            g.draw(diamond);
        }

        private void drawMessage(Graphics2D g, int w, float sx, float sy, float scale) {
            int centerY = Math.round(394 * sy);
            String first = "Thank You, ";
            String second = userName;
            String wave = "  \uD83D\uDC4B";

            g.setFont(font("Segoe UI", Font.BOLD, 39, scale));
            FontMetrics metrics = g.getFontMetrics();
            int totalWidth = metrics.stringWidth(first + second + wave);
            int x = w / 2 - totalWidth / 2;

            g.setColor(new Color(0, 0, 0, 150));
            g.drawString(first + second + wave, x + 2, centerY + 3);
            g.setColor(WHITE);
            g.drawString(first, x, centerY);
            int nameX = x + metrics.stringWidth(first);
            g.setColor(GOLD);
            g.drawString(second, nameX, centerY);
            g.setColor(GOLD_LIGHT);
            g.drawString(wave, nameX + metrics.stringWidth(second), centerY);

            g.setFont(font("Segoe UI", Font.PLAIN, 17, scale));
            g.setColor(new Color(235, 237, 240));
            drawCentered(g, "You have been successfully logged out.",
                    w / 2, Math.round(462 * sy));

            g.setFont(font("Segoe UI", Font.PLAIN, 17, scale));
            g.setColor(GOLD_LIGHT);
            drawCentered(g, "We hope to see you again soon.",
                    w / 2, Math.round(500 * sy));
        }

        private void drawBenefits(Graphics2D g, int w, float sx, float sy, float scale) {
            String[] labels = {
                "Premium Fleet", "Safe & Secure", "24/7 Support", "Luxury Experience"
            };
            String[] icons = {"CAR", "SHIELD", "HEADSET", "CROWN"};
            int groupWidth = Math.round(650 * sx);
            int startX = w / 2 - groupWidth / 2;
            int cellWidth = groupWidth / labels.length;
            int iconY = Math.round(600 * sy);
            int labelY = Math.round(674 * sy);

            for (int i = 0; i < labels.length; i++) {
                int cx = startX + cellWidth * i + cellWidth / 2;
                drawBenefitIcon(g, icons[i], cx, iconY, Math.max(28, Math.round(38 * scale)));
                g.setFont(font("Segoe UI", Font.PLAIN, 13, scale));
                g.setColor(new Color(232, 233, 236));
                drawCentered(g, labels[i], cx, labelY);
                if (i < labels.length - 1) {
                    int lineX = startX + cellWidth * (i + 1);
                    g.setColor(new Color(214, 160, 66, 38));
                    g.drawLine(lineX, Math.round(574 * sy), lineX, Math.round(690 * sy));
                }
            }
        }

        private void drawProgress(Graphics2D g, int w, float sx, float sy, float scale) {
            int barW = Math.min(Math.round(900 * sx), w - Math.round(430 * sx));
            int barH = Math.max(13, Math.round(16 * sy));
            int barX = w / 2 - barW / 2 - Math.round(35 * sx);
            int barY = Math.round(790 * sy);

            g.setFont(font("Segoe UI", Font.PLAIN, 15, scale));
            g.setColor(WHITE);
            drawCentered(g, "Logging you out securely...", w / 2, barY - Math.round(28 * sy));

            g.setColor(new Color(0, 0, 0, 172));
            g.fillRoundRect(barX, barY, barW, barH, barH, barH);
            g.setColor(new Color(255, 219, 130, 130));
            g.setStroke(new BasicStroke(Math.max(1f, 1.2f * scale)));
            g.drawRoundRect(barX, barY, barW, barH, barH, barH);

            int fillW = Math.round(barW * progress / 100f);
            if (fillW > 0) {
                g.setPaint(new GradientPaint(
                        barX, barY, new Color(255, 228, 145),
                        barX + fillW, barY + barH, new Color(238, 153, 33)
                ));
                g.fillRoundRect(barX, barY, fillW, barH, barH, barH);
                g.setColor(new Color(255, 244, 193, 145));
                g.drawLine(barX + 6, barY + 3, barX + Math.max(6, fillW - 6), barY + 3);
                RadialGradientPaint glow = new RadialGradientPaint(
                        new Point(barX + fillW, barY + barH / 2),
                        Math.max(16, Math.round(28 * scale)),
                        new float[]{0f, 1f},
                        new Color[]{new Color(255, 191, 61, 120), new Color(255, 191, 61, 0)}
                );
                g.setPaint(glow);
                g.fillOval(barX + fillW - Math.round(30 * scale),
                        barY - Math.round(23 * scale),
                        Math.round(60 * scale), Math.round(60 * scale));
            }

            g.setFont(font("Segoe UI", Font.BOLD, 18, scale));
            g.setColor(GOLD_LIGHT);
            g.drawString(progress + "%", barX + barW + Math.round(25 * sx),
                    barY + barH + Math.round(1 * sy));

            String status = progress >= 100
                    ? "Session closed successfully"
                    : "Closing dashboard session...";
            int statusY = Math.round(858 * sy);
            g.setFont(font("Segoe UI", Font.PLAIN, 15, scale));
            FontMetrics fm = g.getFontMetrics();
            int statusX = w / 2 - fm.stringWidth(status) / 2;
            drawSmallShield(g, statusX - Math.round(42 * sx),
                    statusY - Math.round(22 * sy), Math.max(21, Math.round(26 * scale)));
            g.setColor(MUTED);
            g.drawString(status, statusX, statusY);
            if (progress >= 100) {
                drawCheck(g, statusX + fm.stringWidth(status) + Math.round(28 * sx),
                        statusY - Math.round(22 * sy), Math.max(23, Math.round(28 * scale)));
            }
        }

        private void drawTagline(Graphics2D g, int w, float sx, float sy, float scale) {
            int y = Math.round(970 * sy);
            int halfGap = Math.round(220 * sx);
            int line = Math.round(255 * sx);
            g.setColor(new Color(214, 160, 66, 130));
            g.drawLine(w / 2 - halfGap - line, y, w / 2 - halfGap, y);
            g.drawLine(w / 2 + halfGap, y, w / 2 + halfGap + line, y);
            g.setFont(font("Serif", Font.ITALIC, 27, scale));
            g.setColor(new Color(230, 173, 78));
            drawCentered(g, "Drive Luxury. Drive BMW.", w / 2, y + Math.round(10 * sy));
        }

        private void drawBenefitIcon(Graphics2D g, String type, int cx, int cy, int size) {
            g.setColor(GOLD);
            g.setStroke(new BasicStroke(Math.max(1.6f, size / 15f),
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int half = size / 2;
            switch (type) {
                case "CAR" -> {
                    g.drawRoundRect(cx - half, cy - 4, size, 14, 5, 5);
                    g.drawLine(cx - half + 5, cy - 4, cx - half + 10, cy - half + 2);
                    g.drawLine(cx - half + 10, cy - half + 2, cx + half - 9, cy - half + 2);
                    g.drawLine(cx + half - 9, cy - half + 2, cx + half - 4, cy - 4);
                    g.fillOval(cx - half + 6, cy + 7, 6, 6);
                    g.fillOval(cx + half - 12, cy + 7, 6, 6);
                }
                case "SHIELD" -> {
                    Path2D shield = new Path2D.Double();
                    shield.moveTo(cx, cy - half);
                    shield.lineTo(cx + half, cy - half + 7);
                    shield.lineTo(cx + half - 4, cy + half - 6);
                    shield.quadTo(cx, cy + half + 5, cx - half + 4, cy + half - 6);
                    shield.lineTo(cx - half, cy - half + 7);
                    shield.closePath();
                    g.draw(shield);
                    g.drawLine(cx - 7, cy, cx - 1, cy + 6);
                    g.drawLine(cx - 1, cy + 6, cx + 9, cy - 7);
                }
                case "HEADSET" -> {
                    g.drawArc(cx - half, cy - half, size, size, 0, 180);
                    g.drawLine(cx - half, cy, cx - half, cy + 10);
                    g.drawLine(cx + half, cy, cx + half, cy + 10);
                    g.drawRoundRect(cx - half - 3, cy + 2, 7, 12, 3, 3);
                    g.drawRoundRect(cx + half - 4, cy + 2, 7, 12, 3, 3);
                    g.drawArc(cx + 2, cy + 8, half - 2, 12, 180, 180);
                }
                default -> {
                    Path2D crown = new Path2D.Double();
                    crown.moveTo(cx - half, cy + half / 2);
                    crown.lineTo(cx - half + 3, cy - half + 2);
                    crown.lineTo(cx - 7, cy - 2);
                    crown.lineTo(cx, cy - half);
                    crown.lineTo(cx + 7, cy - 2);
                    crown.lineTo(cx + half - 3, cy - half + 2);
                    crown.lineTo(cx + half, cy + half / 2);
                    crown.closePath();
                    g.draw(crown);
                    g.drawLine(cx - half, cy + half / 2 + 6, cx + half, cy + half / 2 + 6);
                }
            }
        }

        private void drawSmallShield(Graphics2D g, int x, int y, int size) {
            Path2D shield = new Path2D.Double();
            shield.moveTo(x + size / 2.0, y);
            shield.lineTo(x + size, y + size * .25);
            shield.lineTo(x + size * .85, y + size * .75);
            shield.quadTo(x + size / 2.0, y + size * 1.12, x + size * .15, y + size * .75);
            shield.lineTo(x, y + size * .25);
            shield.closePath();
            g.setColor(GOLD);
            g.setStroke(new BasicStroke(Math.max(1.4f, size / 14f),
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(shield);
            g.drawLine(x + size / 3, y + size / 2,
                    x + Math.round(size * .47f), y + Math.round(size * .66f));
            g.drawLine(x + Math.round(size * .47f), y + Math.round(size * .66f),
                    x + Math.round(size * .73f), y + Math.round(size * .34f));
        }

        private void drawCheck(Graphics2D g, int x, int y, int size) {
            g.setColor(new Color(214, 160, 66, 36));
            g.fillOval(x - size / 3, y - size / 3, size + size * 2 / 3, size + size * 2 / 3);
            g.setColor(GOLD_LIGHT);
            g.drawOval(x, y, size, size);
            g.setStroke(new BasicStroke(Math.max(1.6f, size / 14f),
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(x + size / 4, y + size / 2, x + size * 2 / 5, y + size * 2 / 3);
            g.drawLine(x + size * 2 / 5, y + size * 2 / 3, x + size * 3 / 4, y + size / 3);
        }

        private static Font font(String family, int style, int size, float scale) {
            return new Font(family, style, Math.max(9, Math.round(size * scale)));
        }

        private static void drawCentered(Graphics2D g, String text, int cx, int baseline) {
            FontMetrics metrics = g.getFontMetrics();
            g.drawString(text, cx - metrics.stringWidth(text) / 2, baseline);
        }

        private static BufferedImage loadImage(String path) {
            try (InputStream input = FarewellCanvas.class.getResourceAsStream(path)) {
                return input == null ? null : ImageIO.read(input);
            } catch (IOException ex) {
                return null;
            }
        }

        private static void quality(Graphics2D g) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        }
    }

    private static final class LogoutWindowButton extends JButton {

        private final String type;

        LogoutWindowButton(String type) {
            super("");
            this.type = type;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            FarewellCanvas.quality(g);
            if (getModel().isRollover()) {
                g.setColor("CLOSE".equals(type)
                        ? new Color(145, 35, 35, 165)
                        : new Color(214, 160, 66, 28));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            }
            g.setColor(new Color(214, 160, 66, 40));
            g.drawLine(0, 5, 0, getHeight() - 6);
            g.setColor(GOLD_LIGHT);
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            switch (type) {
                case "MINIMIZE" -> g.drawLine(cx - 8, cy + 5, cx + 8, cy + 5);
                case "MAXIMIZE" -> g.drawRoundRect(cx - 7, cy - 7, 14, 14, 2, 2);
                default -> {
                    g.drawLine(cx - 7, cy - 7, cx + 7, cy + 7);
                    g.drawLine(cx + 7, cy - 7, cx - 7, cy + 7);
                }
            }
            g.dispose();
        }
    }
}
