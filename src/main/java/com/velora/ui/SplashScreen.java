package com.velora.ui;

import com.velora.authentication.Customer;
import com.velora.service.VehicleService;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

public final class SplashScreen extends JFrame {

    public SplashScreen(Customer account) {
        super("Velora Motors - Loading");

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setUndecorated(true);
        setMinimumSize(new Dimension(1000, 650));
        setContentPane(new LoadingCanvas(account, this));
        setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private static final class LoadingCanvas extends JPanel {

        private static final Color GOLD = new Color(214, 168, 91);
        private static final Color PALE = new Color(236, 203, 157);
        private static final Color WHITE = new Color(246, 247, 249);
        private static final Color MUTED = new Color(166, 172, 183);

        private final Customer account;
        private final SplashScreen frame;
        private final BufferedImage background;
        private final long loadingStarted = System.nanoTime();
        private Timer loadingTimer;

        private int progress;
        private float glow;

        LoadingCanvas(Customer account, SplashScreen frame) {
            this.account = account;
            this.frame = frame;
            this.background = loadBackground();

            setOpaque(true);

            loadingTimer = new Timer(34, e -> {
                long elapsedMillis = (System.nanoTime() - loadingStarted) / 1_000_000L;
                progress = Math.min(100, Math.round(elapsedMillis * 100f / 5_000f));
                glow = (float) ((Math.sin(progress * .16) + 1) * .5);
                repaint();

                if (progress >= 100) {
                    loadingTimer.stop();

                    Timer finishTimer = new Timer(450, event -> openDashboard());
                    finishTimer.setRepeats(false);
                    finishTimer.start();
                }
            });
            loadingTimer.setInitialDelay(180);
            loadingTimer.start();
        }

        private void openDashboard() {
            if (account.getRole() == Customer.Role.MANAGER) {
                new ManagerDashboard(account).setVisible(true);
            } else {
                // Reconcile first so availability mail and toast are ready before the dashboard opens.
                new VehicleService().notifyWaitlistsForAvailableVehicles();
                CustomerDashboard dashboard = new CustomerDashboard(account);
                dashboard.setVisible(true);
                dashboard.showAvailabilityToast();
            }

            frame.dispose();
        }

        @Override
        protected void paintComponent(Graphics raw) {
            super.paintComponent(raw);

            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int w = getWidth();
            int h = getHeight();

            if (background != null) {
                drawCover(g, background, 0, 0, w, h);
            } else {
                g.setPaint(new GradientPaint(
                        0, 0, new Color(4, 8, 13),
                        w, h, new Color(13, 9, 6)
                ));
                g.fillRect(0, 0, w, h);
            }

            drawVignette(g, w, h);
            drawCenterExperience(g, w, h);
            drawFeatureStrip(g, w, h);

            g.dispose();
        }

        private void drawVignette(Graphics2D g, int w, int h) {
            g.setColor(new Color(0, 0, 0, 30));
            g.fillRect(0, 0, w, h);

            g.setPaint(new GradientPaint(
                    0, 0, new Color(0, 0, 0, 18),
                    0, h, new Color(0, 0, 0, 170)
            ));
            g.fillRect(0, 0, w, h);

            g.setPaint(new GradientPaint(
                    0, 0, new Color(0, 0, 0, 125),
                    w * .24f, 0, new Color(0, 0, 0, 0)
            ));
            g.fillRect(0, 0, w, h);

            g.setPaint(new GradientPaint(
                    w, 0, new Color(0, 0, 0, 125),
                    w * .76f, 0, new Color(0, 0, 0, 0)
            ));
            g.fillRect(0, 0, w, h);

            float focusRadius = Math.max(w, h) * .39f;
            java.awt.RadialGradientPaint focusShade = new java.awt.RadialGradientPaint(
                    new java.awt.geom.Point2D.Float(w / 2f, h * .55f),
                    focusRadius,
                    new float[]{0f, .42f, .78f, 1f},
                    new Color[]{
                            new Color(1, 5, 9, 158),
                            new Color(1, 5, 9, 112),
                            new Color(1, 5, 9, 38),
                            new Color(1, 5, 9, 0)
                    }
            );
            g.setPaint(focusShade);
            g.fillRect(0, 0, w, h);

            float goldRadius = Math.max(scaleX(w, 360), scaleY(h, 300));
            java.awt.RadialGradientPaint goldGlow = new java.awt.RadialGradientPaint(
                    new java.awt.geom.Point2D.Float(w / 2f, h * .47f),
                    goldRadius,
                    new float[]{0f, .5f, 1f},
                    new Color[]{
                            new Color(214, 168, 91, 18),
                            new Color(214, 168, 91, 7),
                            new Color(214, 168, 91, 0)
                    }
            );
            g.setPaint(goldGlow);
            g.fillRect(0, 0, w, h);
        }

        private void drawCenterExperience(Graphics2D g, int w, int h) {
            int cx = w / 2;
            int baseY = (int) (h * .43);

            drawVeloraMark(g, cx, baseY - scaleY(h, 76), scaleX(w, 150));

            drawCentered(
                    g,
                    "V E L O R A   M O T O R S",
                    cx,
                    baseY + scaleY(h, 18),
                    new Font("Serif", Font.PLAIN, scaleFont(w, h, 34)),
                    PALE
            );

            drawCentered(
                    g,
                    "P R E M I U M   B M W   V E H I C L E   R E N T A L   S Y S T E M",
                    cx,
                    baseY + scaleY(h, 50),
                    new Font("Segoe UI", Font.PLAIN, scaleFont(w, h, 11)),
                    PALE
            );

            int lineY = baseY + scaleY(h, 70);
            g.setStroke(new BasicStroke(Math.max(1f, scaleFont(w, h, 1))));
            g.setColor(new Color(214, 168, 91, 145));
            g.drawLine(cx - scaleX(w, 82), lineY, cx - scaleX(w, 10), lineY);
            g.drawLine(cx + scaleX(w, 10), lineY, cx + scaleX(w, 82), lineY);

            Path2D diamond = new Path2D.Double();
            diamond.moveTo(cx, lineY - scaleY(h, 5));
            diamond.lineTo(cx + scaleX(w, 5), lineY);
            diamond.lineTo(cx, lineY + scaleY(h, 5));
            diamond.lineTo(cx - scaleX(w, 5), lineY);
            diamond.closePath();
            g.draw(diamond);

            String left = "D R I V E   L U X U R Y .   D R I V E   ";
            String right = "B M W .";
            Font taglineFont = new Font("Segoe UI", Font.PLAIN, scaleFont(w, h, 12));
            g.setFont(taglineFont);
            FontMetrics taglineMetrics = g.getFontMetrics();
            int taglineW = taglineMetrics.stringWidth(left + right);
            int taglineX = cx - taglineW / 2;
            int taglineY = baseY + scaleY(h, 101);
            g.setColor(WHITE);
            g.drawString(left, taglineX, taglineY);
            g.setColor(new Color(94, 137, 195));
            g.drawString(right, taglineX + taglineMetrics.stringWidth(left), taglineY);

            int ringSize = scaleY(h, 88);
            int ringX = cx - ringSize / 2;
            int ringY = baseY + scaleY(h, 127);

            g.setStroke(new BasicStroke(
                    Math.max(3f, scaleY(h, 5)),
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));
            g.setColor(new Color(236, 203, 157, 48));
            g.drawOval(ringX, ringY, ringSize, ringSize);

            g.setColor(new Color(236, 203, 157, 150 + (int) (glow * 90)));
            g.draw(new Arc2D.Double(
                    ringX,
                    ringY,
                    ringSize,
                    ringSize,
                    90,
                    -360d * progress / 100d,
                    Arc2D.OPEN
            ));

            drawCentered(
                    g,
                    progress + "%",
                    cx,
                    ringY + ringSize / 2 + scaleY(h, 8),
                    new Font("Segoe UI", Font.PLAIN, scaleFont(w, h, 17)),
                    WHITE
            );

            int loadingY = ringY + ringSize + scaleY(h, 38);
            drawCentered(
                    g,
                    progress >= 100 ? "R E A D Y" : "L O A D I N G . . .",
                    cx,
                    loadingY,
                    new Font("Segoe UI", Font.PLAIN, scaleFont(w, h, 17)),
                    PALE
            );

            drawCentered(
                    g,
                    loadingMessage(),
                    cx,
                    loadingY + scaleY(h, 28),
                    new Font("Segoe UI", Font.PLAIN, scaleFont(w, h, 11)),
                    MUTED
            );

            int barW = scaleX(w, 260);
            int barH = Math.max(2, scaleY(h, 3));
            int barX = cx - barW / 2;
            int barY = loadingY + scaleY(h, 48);
            g.setColor(new Color(255, 255, 255, 28));
            g.fillRoundRect(barX, barY, barW, barH, barH, barH);
            g.setPaint(new GradientPaint(
                    barX, barY, new Color(176, 119, 54),
                    barX + barW, barY, new Color(246, 211, 158)
            ));
            g.fillRoundRect(
                    barX,
                    barY,
                    Math.max(barH, Math.round(barW * progress / 100f)),
                    barH,
                    barH,
                    barH
            );
        }

        private String loadingMessage() {
            if (progress < 25) {
                return "Securing your account...";
            }
            if (progress < 55) {
                return "Loading the premium fleet...";
            }
            if (progress < 82) {
                return "Preparing your personal dashboard...";
            }
            if (progress < 100) {
                return "Finalizing your premium experience...";
            }
            return "Welcome, " + account.getFullName();
        }

        private void drawFeatureStrip(Graphics2D g, int w, int h) {
            int stripW = Math.min(scaleX(w, 1040), (int) (w * .82));
            int stripH = scaleY(h, 118);
            int x = (w - stripW) / 2;
            int y = h - stripH - scaleY(h, 34);

            RoundRectangle2D strip = new RoundRectangle2D.Double(
                    x, y, stripW, stripH, scaleY(h, 18), scaleY(h, 18)
            );
            g.setColor(new Color(3, 8, 13, 205));
            g.fill(strip);
            g.setColor(new Color(214, 168, 91, 48));
            g.setStroke(new BasicStroke(1f));
            g.draw(strip);

            String[][] features = {
                    {"car", "PREMIUM FLEET", "The latest BMW models", "ready for you."},
                    {"shield", "SECURE & TRUSTED", "Your security is our", "top priority."},
                    {"support", "24/7 SUPPORT", "We're here for you,", "anytime, anywhere."},
                    {"crown", "VIP EXPERIENCE", "Excellence in every", "journey."}
            };

            int cellW = stripW / 4;
            for (int i = 0; i < features.length; i++) {
                int cellX = x + i * cellW;
                if (i > 0) {
                    g.setColor(new Color(255, 255, 255, 24));
                    g.drawLine(cellX, y + scaleY(h, 20), cellX, y + stripH - scaleY(h, 20));
                }

                drawFeatureIcon(
                        g,
                        features[i][0],
                        cellX + scaleX(w, 38),
                        y + stripH / 2,
                        scaleX(w, 27)
                );

                g.setColor(PALE);
                g.setFont(new Font("Segoe UI", Font.BOLD, scaleFont(w, h, 10)));
                g.drawString(features[i][1], cellX + scaleX(w, 78), y + scaleY(h, 43));

                g.setColor(MUTED);
                g.setFont(new Font("Segoe UI", Font.PLAIN, scaleFont(w, h, 9)));
                g.drawString(features[i][2], cellX + scaleX(w, 78), y + scaleY(h, 68));
                g.drawString(features[i][3], cellX + scaleX(w, 78), y + scaleY(h, 88));
            }
        }

        private void drawFeatureIcon(Graphics2D g, String icon, int cx, int cy, int size) {
            Graphics2D iconG = (Graphics2D) g.create();
            iconG.setColor(new Color(226, 181, 105));
            iconG.setStroke(new BasicStroke(
                    Math.max(1.4f, size / 15f),
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));

            int half = size / 2;
            switch (icon) {
                case "car" -> {
                    iconG.drawRoundRect(cx - half, cy - size / 4, size, size / 2, 5, 5);
                    iconG.drawLine(cx - size / 3, cy - size / 4, cx - size / 5, cy - half);
                    iconG.drawLine(cx - size / 5, cy - half, cx + size / 4, cy - half);
                    iconG.drawLine(cx + size / 4, cy - half, cx + size / 3, cy - size / 4);
                    iconG.fillOval(cx - size / 3, cy + size / 5, size / 6, size / 6);
                    iconG.fillOval(cx + size / 6, cy + size / 5, size / 6, size / 6);
                }
                case "shield" -> {
                    Path2D shield = new Path2D.Double();
                    shield.moveTo(cx, cy - half);
                    shield.lineTo(cx + half, cy - size / 3);
                    shield.lineTo(cx + size / 3, cy + size / 3);
                    shield.lineTo(cx, cy + half);
                    shield.lineTo(cx - size / 3, cy + size / 3);
                    shield.lineTo(cx - half, cy - size / 3);
                    shield.closePath();
                    iconG.draw(shield);
                    iconG.drawLine(cx, cy - size / 5, cx, cy + size / 5);
                    iconG.drawLine(cx - size / 5, cy, cx + size / 5, cy);
                }
                case "support" -> {
                    iconG.drawArc(cx - half, cy - half, size, size, 0, 180);
                    iconG.drawLine(cx - half, cy, cx - half, cy + size / 4);
                    iconG.drawLine(cx + half, cy, cx + half, cy + size / 4);
                    iconG.drawArc(cx + size / 5, cy + size / 5, size / 3, size / 3, 270, 90);
                }
                default -> {
                    Path2D crown = new Path2D.Double();
                    crown.moveTo(cx - half, cy + size / 3);
                    crown.lineTo(cx - size / 3, cy - size / 3);
                    crown.lineTo(cx, cy);
                    crown.lineTo(cx + size / 3, cy - size / 3);
                    crown.lineTo(cx + half, cy + size / 3);
                    crown.closePath();
                    iconG.draw(crown);
                    iconG.drawLine(cx - half, cy + half, cx + half, cy + half);
                }
            }
            iconG.dispose();
        }

        private void drawVeloraMark(Graphics2D g, int cx, int y, int width) {
            Graphics2D mark = (Graphics2D) g.create();
            mark.setColor(new Color(226, 181, 105));
            mark.setStroke(new BasicStroke(
                    Math.max(2f, width / 55f),
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));

            int wing = width / 2;
            int centerDrop = width / 4;

            Path2D path = new Path2D.Double();
            path.moveTo(cx, y + centerDrop);
            path.lineTo(cx - width / 6, y);
            path.lineTo(cx - wing, y);
            path.moveTo(cx, y + centerDrop);
            path.lineTo(cx + width / 6, y);
            path.lineTo(cx + wing, y);

            path.moveTo(cx - width / 8, y + width / 14);
            path.lineTo(cx - width * .41, y + width / 14);
            path.moveTo(cx + width / 8, y + width / 14);
            path.lineTo(cx + width * .41, y + width / 14);

            path.moveTo(cx - width / 11, y + width / 7);
            path.lineTo(cx - width / 3, y + width / 7);
            path.moveTo(cx + width / 11, y + width / 7);
            path.lineTo(cx + width / 3, y + width / 7);
            mark.draw(path);
            mark.dispose();
        }

        private void drawCentered(
                Graphics2D g,
                String text,
                int centerX,
                int baseline,
                Font font,
                Color color
        ) {
            g.setFont(font);
            g.setColor(color);
            FontMetrics metrics = g.getFontMetrics();
            g.drawString(text, centerX - metrics.stringWidth(text) / 2, baseline);
        }

        private static BufferedImage loadBackground() {
            try {
                return ImageIO.read(SplashScreen.class.getResource("/images/loading-showroom.png"));
            } catch (IOException | IllegalArgumentException ex) {
                return null;
            }
        }

        private static void drawCover(
                Graphics2D g,
                BufferedImage image,
                int x,
                int y,
                int w,
                int h
        ) {
            double scale = Math.max(
                    w / (double) image.getWidth(),
                    h / (double) image.getHeight()
            );
            int imageW = (int) Math.round(image.getWidth() * scale);
            int imageH = (int) Math.round(image.getHeight() * scale);
            g.drawImage(
                    image,
                    x + (w - imageW) / 2,
                    y + (h - imageH) / 2,
                    imageW,
                    imageH,
                    null
            );
        }

        private static int scaleX(int width, int value) {
            return Math.max(1, Math.round(value * width / 1920f));
        }

        private static int scaleY(int height, int value) {
            return Math.max(1, Math.round(value * height / 1080f));
        }

        private static int scaleFont(int width, int height, int value) {
            float scale = Math.min(width / 1920f, height / 1080f);
            return Math.max(7, Math.round(value * scale));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Customer preview = new Customer(
                    "Velora Guest",
                    "guest@velora.com",
                    "",
                    Customer.Role.CUSTOMER
            );
            new SplashScreen(preview).setVisible(true);
        });
    }
}
