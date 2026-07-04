package com.velora.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.*;

public class FarewellScreen extends JFrame {

    private int progress = 0;
    private Timer loadingTimer;
    private final FarewellCanvas canvas;

    public FarewellScreen(String userName) {
        super("Velora Motors - Logout");

        canvas = new FarewellCanvas(userName);

        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(canvas);
        setMinimumSize(new Dimension(1100, 680));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        startLoading();
    }

    private void startLoading() {
        loadingTimer = new Timer(25, (ActionEvent e) -> {
            progress++;
            canvas.setProgress(progress);

            if (progress >= 100) {
                loadingTimer.stop();

                Timer backToLogin = new Timer(900, ev -> {
                    dispose();
                    new LoginScreen().setVisible(true);
                });

                backToLogin.setRepeats(false);
                backToLogin.start();
            }
        });

        loadingTimer.start();
    }

    private static final class FarewellCanvas extends JPanel {

        private int progress = 0;
        private final String userName;
        private final BufferedImage background;

        private static final Color GOLD = new Color(214, 160, 66);
        private static final Color GOLD_LIGHT = new Color(255, 221, 145);
        private static final Color GOLD_STRONG = new Color(255, 191, 70);
        private static final Color WHITE = new Color(246, 246, 246);
        private static final Color MUTED = new Color(195, 198, 205);

        FarewellCanvas(String userName) {
            this.userName = formatName(userName);
            this.background = loadImage("/images/velora-farewell-background.png");
            setOpaque(true);
        }

        private static String formatName(String name) {
            if (name == null || name.trim().isEmpty()) {
                return "Velora Guest";
            }

            String n = name.trim();

            if (n.equalsIgnoreCase("system")) {
                return "Velora Guest";
            }

            if (n.contains("@")) {
                n = n.substring(0, n.indexOf("@"));
            }

            if (n.contains(" ")) {
                n = n.substring(0, n.indexOf(" "));
            }

            if (n.isEmpty()) {
                return "Velora Guest";
            }

            return n.substring(0, 1).toUpperCase() + n.substring(1);
        }

        public void setProgress(int value) {
            progress = Math.max(0, Math.min(100, value));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics raw) {
            super.paintComponent(raw);

            Graphics2D g = (Graphics2D) raw.create();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            int w = getWidth();
            int h = getHeight();

            drawBackground(g, w, h);
            drawGlobalShade(g, w, h);
            drawGlassFocus(g, w, h);
            drawUserMessage(g, w, h);
            drawProgressSection(g, w, h);

            g.dispose();
        }

        private void drawBackground(Graphics2D g, int w, int h) {
            if (background != null) {
                double scale = Math.max(
                        w / (double) background.getWidth(),
                        h / (double) background.getHeight()
                );

                int bw = (int) Math.ceil(background.getWidth() * scale);
                int bh = (int) Math.ceil(background.getHeight() * scale);
                int x = (w - bw) / 2;
                int y = (h - bh) / 2 + scaleH(35);

                g.drawImage(background, x, y, bw, bh, null);
            } else {
                GradientPaint bg = new GradientPaint(
                        0, 0, new Color(3, 7, 13),
                        w, h, new Color(0, 0, 0)
                );
                g.setPaint(bg);
                g.fillRect(0, 0, w, h);
            }
        }

        private void drawGlobalShade(Graphics2D g, int w, int h) {
            GradientPaint shade = new GradientPaint(
                    0, 0, new Color(0, 0, 0, 18),
                    0, h, new Color(0, 0, 0, 105)
            );
            g.setPaint(shade);
            g.fillRect(0, 0, w, h);

            RadialGradientPaint centerFocus = new RadialGradientPaint(
                    new Point(w / 2, (int) (h * 0.47)),
                    Math.max(w, h) / 1.45f,
                    new float[]{0f, 0.62f, 1f},
                    new Color[]{
                            new Color(0, 0, 0, 0),
                            new Color(0, 0, 0, 35),
                            new Color(0, 0, 0, 180)
                    }
            );

            g.setPaint(centerFocus);
            g.fillRect(0, 0, w, h);
        }

        private void drawGlassFocus(Graphics2D g, int w, int h) {
            int cx = w / 2;

            int textW = Math.min(scaleW(760), w - scaleW(180));
            int textH = scaleH(245);
            int textX = cx - textW / 2;
            int textY = (int) (h * 0.29);

            RadialGradientPaint messageGlow = new RadialGradientPaint(
                    new Point(cx, textY + textH / 2),
                    textW / 1.7f,
                    new float[]{0f, 0.74f, 1f},
                    new Color[]{
                            new Color(0, 0, 0, 128),
                            new Color(0, 0, 0, 72),
                            new Color(0, 0, 0, 0)
                    }
            );

            g.setPaint(messageGlow);
            g.fillOval(
                    textX - scaleW(90),
                    textY - scaleH(55),
                    textW + scaleW(180),
                    textH + scaleH(95)
            );

            /*
             * Removed the glass rectangle behind the loading bar.
             * The progress bar will appear naturally without a box.
             */
        }

        private void drawUserMessage(Graphics2D g, int w, int h) {
            int cx = w / 2;
            int baseY = (int) (h * 0.365);

            g.setFont(new Font("Segoe UI", Font.BOLD, scaleFont(38)));
            FontMetrics fm = g.getFontMetrics();

            String first = "Thank You, ";
            String second = userName;

            int total = fm.stringWidth(first + second);
            int x = cx - total / 2;

            g.setColor(new Color(0, 0, 0, 145));
            g.drawString(first + second, x + scaleW(2), baseY + scaleH(3));

            g.setColor(WHITE);
            g.drawString(first, x, baseY);

            g.setColor(GOLD);
            g.drawString(second, x + fm.stringWidth(first), baseY);

            g.setFont(new Font("Segoe UI", Font.PLAIN, scaleFont(16)));
            fm = g.getFontMetrics();

            String line1 = "You have been successfully logged out.";
            g.setColor(new Color(238, 238, 238));
            g.drawString(line1, cx - fm.stringWidth(line1) / 2, baseY + scaleH(50));

            String line2 = "We hope to see you again soon.";
            g.setColor(GOLD_LIGHT);
            g.drawString(line2, cx - fm.stringWidth(line2) / 2, baseY + scaleH(80));

            g.setFont(new Font("Serif", Font.ITALIC, scaleFont(27)));
            fm = g.getFontMetrics();

            String line3 = "Drive Luxury. Drive BMW.";
            g.setColor(new Color(232, 174, 83));
            g.drawString(line3, cx - fm.stringWidth(line3) / 2, baseY + scaleH(125));

            int lineY = baseY + scaleH(154);

            g.setStroke(new BasicStroke(scaleStroke(1.1f)));
            g.setColor(new Color(214, 160, 66, 125));
            g.drawLine(cx - scaleW(120), lineY, cx + scaleW(120), lineY);
            g.fillOval(cx - scaleW(4), lineY - scaleH(4), scaleW(8), scaleW(8));
        }

        private void drawProgressSection(Graphics2D g, int w, int h) {
            int cx = w / 2;

            /*
             * Natural loading bar:
             * - No outer glass rectangle.
             * - Positioned above the icons enough so it does not cover them.
             */
            int barW = Math.min(scaleW(650), w - scaleW(310));
            int barH = scaleH(18);
            int barX = cx - barW / 2;
            int barY = (int) (h * 0.735);

            g.setFont(new Font("Segoe UI", Font.PLAIN, scaleFont(15)));
            FontMetrics fm = g.getFontMetrics();

            String msg = "Logging you out securely...";
            g.setColor(WHITE);
            g.drawString(msg, cx - fm.stringWidth(msg) / 2, barY - scaleH(24));

            drawProgressGlow(g, barX, barY, barW, barH);

            // Background track only, no big rectangle/panel around it
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRoundRect(barX, barY, barW, barH, scaleW(30), scaleW(30));

            int fillW = (int) (barW * (progress / 100.0));

            if (fillW > 0) {
                GradientPaint fill = new GradientPaint(
                        barX, barY, new Color(255, 232, 152),
                        barX + fillW, barY + barH, new Color(185, 105, 30)
                );

                g.setPaint(fill);
                g.fillRoundRect(barX, barY, fillW, barH, scaleW(30), scaleW(30));

                g.setColor(new Color(255, 250, 196, 150));
                g.fillRoundRect(
                        barX + scaleW(2),
                        barY + scaleH(2),
                        Math.max(0, fillW - scaleW(4)),
                        Math.max(3, barH / 3),
                        scaleW(30),
                        scaleW(30)
                );

                g.setColor(new Color(255, 190, 64, 60));
                g.fillOval(
                        barX + fillW - scaleW(23),
                        barY - scaleH(9),
                        scaleW(46),
                        scaleH(36)
                );
            }

            g.setColor(new Color(255, 222, 145, 118));
            g.setStroke(new BasicStroke(scaleStroke(1.25f)));
            g.drawRoundRect(barX, barY, barW, barH, scaleW(30), scaleW(30));

            g.setFont(new Font("Segoe UI", Font.BOLD, scaleFont(20)));
            g.setColor(GOLD_STRONG);
            g.drawString(progress + "%", barX + barW + scaleW(25), barY + barH);

            String status = progress >= 100
                    ? "Session closed successfully"
                    : "Closing dashboard session...";

            g.setFont(new Font("Segoe UI", Font.PLAIN, scaleFont(13)));
            fm = g.getFontMetrics();

            int statusY = barY + scaleH(45);
            int statusX = cx - fm.stringWidth(status) / 2;

            drawShield(g, statusX - scaleW(38), statusY - scaleH(18));

            g.setColor(MUTED);
            g.drawString(status, statusX, statusY);

            if (progress >= 100) {
                drawCheck(g, statusX + fm.stringWidth(status) + scaleW(25), statusY - scaleH(22));
            }
        }

        private void drawProgressGlow(Graphics2D g, int x, int y, int w, int h) {
            RadialGradientPaint glow = new RadialGradientPaint(
                    new Point(x + w / 2, y + h / 2),
                    w / 1.55f,
                    new float[]{0f, 0.58f, 1f},
                    new Color[]{
                            new Color(214, 160, 66, 45),
                            new Color(214, 160, 66, 10),
                            new Color(214, 160, 66, 0)
                    }
            );

            g.setPaint(glow);
            g.fillOval(x - scaleW(90), y - scaleH(30), w + scaleW(180), h + scaleH(65));
        }

        private void drawShield(Graphics2D g, int x, int y) {
            Path2D shield = new Path2D.Double();

            shield.moveTo(x + scaleW(13), y);
            shield.lineTo(x + scaleW(27), y + scaleH(6));
            shield.lineTo(x + scaleW(24), y + scaleH(22));
            shield.quadTo(x + scaleW(13), y + scaleH(33), x + scaleW(2), y + scaleH(22));
            shield.lineTo(x, y + scaleH(6));
            shield.closePath();

            g.setStroke(new BasicStroke(scaleStroke(1.2f)));
            g.setColor(GOLD);
            g.draw(shield);

            g.setFont(new Font("Segoe UI", Font.BOLD, scaleFont(10)));
            g.drawString("✓", x + scaleW(8), y + scaleH(20));
        }

        private void drawCheck(Graphics2D g, int x, int y) {
            g.setColor(new Color(214, 160, 66, 45));
            g.fillOval(x - scaleW(8), y - scaleH(8), scaleW(40), scaleW(40));

            g.setStroke(new BasicStroke(scaleStroke(1.35f)));
            g.setColor(GOLD);
            g.drawOval(x, y, scaleW(25), scaleW(25));

            g.setFont(new Font("Segoe UI", Font.BOLD, scaleFont(16)));
            g.drawString("✓", x + scaleW(6), y + scaleH(19));
        }

        private int scaleW(int value) {
            return Math.max(1, Math.round(value * getWidth() / 1402f));
        }

        private int scaleH(int value) {
            return Math.max(1, Math.round(value * getHeight() / 1122f));
        }

        private int scaleFont(int value) {
            float scale = Math.min(getWidth() / 1402f, getHeight() / 1122f);
            return Math.max(8, Math.round(value * scale));
        }

        private float scaleStroke(float value) {
            float scale = Math.min(getWidth() / 1402f, getHeight() / 1122f);
            return Math.max(1f, value * scale);
        }

        private static BufferedImage loadImage(String path) {
            try (InputStream in = FarewellCanvas.class.getResourceAsStream(path)) {
                if (in == null) {
                    return null;
                }

                return ImageIO.read(in);
            } catch (IOException ex) {
                return null;
            }
        }
    }
}