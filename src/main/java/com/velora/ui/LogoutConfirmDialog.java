package com.velora.ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LogoutConfirmDialog extends JDialog {

    private boolean confirmed = false;

    private static final int W = 430;
    private static final int H = 270;
    private static final int ARC = 28;

    private static final Color GOLD = new Color(202, 148, 58);
    private static final Color GOLD_LIGHT = new Color(238, 188, 92);
    private static final Color GOLD_DARK = new Color(128, 83, 27);

    private static final Color BG_1 = new Color(18, 19, 21);
    private static final Color BG_2 = new Color(8, 9, 12);
    private static final Color BG_3 = new Color(4, 5, 7);

    private static final Color TEXT_MAIN = new Color(245, 245, 247);
    private static final Color TEXT_MUTED = new Color(169, 171, 178);
    private static final Color TEXT_SOFT = new Color(116, 118, 126);

    public LogoutConfirmDialog(JFrame parent) {
        super(parent, true);

        setUndecorated(true);
        setSize(W, H);
        setLocationRelativeTo(parent);
        setBackground(new Color(0, 0, 0, 0));

        JPanel mainPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                applyQuality(g2);

                int w = getWidth();
                int h = getHeight();

                // Shadow
                g2.setColor(new Color(0, 0, 0, 105));
                g2.fillRoundRect(7, 8, w - 14, h - 12, ARC + 2, ARC + 2);

                // Main card background
                LinearGradientPaint cardBg = new LinearGradientPaint(
                        0, 0, 0, h,
                        new float[]{0f, 0.45f, 1f},
                        new Color[]{BG_1, BG_2, BG_3}
                );
                g2.setPaint(cardBg);
                g2.fillRoundRect(0, 0, w - 1, h - 1, ARC, ARC);

                // Top premium strip
                LinearGradientPaint header = new LinearGradientPaint(
                        0, 0, 0, 72,
                        new float[]{0f, 1f},
                        new Color[]{
                                new Color(255, 205, 105, 28),
                                new Color(255, 205, 105, 0)
                        }
                );
                g2.setPaint(header);
                g2.fillRoundRect(1, 1, w - 2, 78, ARC, ARC);

                // Subtle center glow
                RadialGradientPaint glow = new RadialGradientPaint(
                        new Point(w / 2, 76),
                        170,
                        new float[]{0f, 0.55f, 1f},
                        new Color[]{
                                new Color(202, 148, 58, 36),
                                new Color(202, 148, 58, 10),
                                new Color(202, 148, 58, 0)
                        }
                );
                g2.setPaint(glow);
                g2.fillOval(w / 2 - 170, -95, 340, 210);

                // Outer border
                g2.setStroke(new BasicStroke(1.15f));
                g2.setColor(new Color(202, 148, 58, 120));
                g2.drawRoundRect(0, 0, w - 1, h - 1, ARC, ARC);

                // Inner border
                g2.setColor(new Color(255, 225, 145, 28));
                g2.drawRoundRect(4, 4, w - 9, h - 9, ARC - 6, ARC - 6);

                // Divider line
                g2.setColor(new Color(255, 255, 255, 13));
                g2.drawLine(42, 187, w - 42, 187);

                // Tiny luxury dots
                g2.setColor(new Color(202, 148, 58, 100));
                g2.fillOval(28, 24, 4, 4);
                g2.setColor(new Color(202, 148, 58, 45));
                g2.fillOval(37, 24, 4, 4);

                g2.dispose();
            }
        };

        mainPanel.setOpaque(false);
        setContentPane(mainPanel);

        CloseButton closeButton = new CloseButton();
        closeButton.setBounds(W - 48, 18, 30, 30);
        closeButton.addActionListener(e -> dispose());
        mainPanel.add(closeButton);

        IconBadge iconBadge = new IconBadge();
        iconBadge.setBounds((W - 58) / 2, 34, 58, 58);
        mainPanel.add(iconBadge);

        JLabel titleLabel = new JLabel("Confirm Logout", SwingConstants.CENTER);
        titleLabel.setBounds(0, 106, W, 30);
        titleLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 22));
        titleLabel.setForeground(TEXT_MAIN);
        mainPanel.add(titleLabel);

        JLabel subtitleLabel = new JLabel("You're about to end your current session", SwingConstants.CENTER);
        subtitleLabel.setBounds(0, 137, W, 20);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(TEXT_MUTED);
        mainPanel.add(subtitleLabel);

        JLabel hintLabel = new JLabel("You will need to sign in again to access your account.", SwingConstants.CENTER);
        hintLabel.setBounds(0, 160, W, 20);
        hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hintLabel.setForeground(TEXT_SOFT);
        mainPanel.add(hintLabel);

        PremiumButton cancelButton = new PremiumButton("Cancel", false);
        cancelButton.setBounds(42, 208, 160, 42);
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
        mainPanel.add(cancelButton);

        PremiumButton logoutButton = new PremiumButton("Logout", true);
        logoutButton.setBounds(W - 202, 208, 160, 42);
        logoutButton.addActionListener(e -> {
            confirmed = true;
            dispose();
        });
        mainPanel.add(logoutButton);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    private static void applyQuality(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    private static class CloseButton extends JButton {

        private boolean hover = false;

        CloseButton() {
            super("×");
            setFont(new Font("Segoe UI", Font.PLAIN, 22));
            setForeground(new Color(178, 180, 186));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(0, 0, 3, 0));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            applyQuality(g2);

            if (hover) {
                g2.setColor(new Color(255, 255, 255, 14));
                g2.fillOval(1, 1, getWidth() - 2, getHeight() - 2);

                g2.setColor(new Color(202, 148, 58, 85));
                g2.drawOval(1, 1, getWidth() - 3, getHeight() - 3);

                setForeground(GOLD_LIGHT);
            } else {
                setForeground(new Color(178, 180, 186));
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class PremiumButton extends JButton {

        private final boolean primary;
        private boolean hover = false;
        private boolean pressed = false;

        PremiumButton(String text, boolean primary) {
            super(text);
            this.primary = primary;

            setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
            setFocusPainted(false);
            setBorder(new EmptyBorder(0, 0, 0, 0));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setContentAreaFilled(false);
            setOpaque(false);

            setForeground(primary ? new Color(22, 17, 10) : new Color(222, 181, 92));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    pressed = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    pressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    pressed = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            applyQuality(g2);

            int w = getWidth();
            int h = getHeight();
            int y = pressed ? 1 : 0;

            if (primary) {
                LinearGradientPaint gp = new LinearGradientPaint(
                        0, y, 0, h,
                        new float[]{0f, 0.48f, 1f},
                        new Color[]{
                                hover ? new Color(248, 201, 104) : new Color(224, 169, 73),
                                hover ? new Color(207, 142, 47) : new Color(190, 124, 39),
                                hover ? new Color(142, 88, 28) : GOLD_DARK
                        }
                );

                g2.setPaint(gp);
                g2.fillRoundRect(0, y, w, h - 1, 13, 13);

                g2.setColor(new Color(255, 237, 159, hover ? 145 : 95));
                g2.drawRoundRect(0, y, w - 1, h - 2, 13, 13);

                g2.setColor(new Color(255, 255, 255, 38));
                g2.drawLine(13, y + 3, w - 13, y + 3);

            } else {
                g2.setColor(hover ? new Color(18, 19, 22) : new Color(7, 8, 10));
                g2.fillRoundRect(0, y, w, h - 1, 13, 13);

                g2.setColor(hover ? new Color(202, 148, 58, 160) : new Color(202, 148, 58, 82));
                g2.drawRoundRect(0, y, w - 1, h - 2, 13, 13);

                g2.setColor(new Color(255, 255, 255, hover ? 15 : 7));
                g2.drawLine(13, y + 3, w - 13, y + 3);
            }

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();

            int iconW = primary ? 17 : 0;
            int gap = primary ? 8 : 0;

            int textW = fm.stringWidth(getText());
            int totalW = iconW + gap + textW;

            int startX = (w - totalW) / 2;
            int textY = ((h - fm.getHeight()) / 2) + fm.getAscent() + y;

            if (primary) {
                int iconX = startX;
                int iconY = h / 2 + y;

                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(new Color(28, 22, 13));

                // door
                g2.drawLine(iconX + 1, iconY - 7, iconX + 1, iconY + 7);
                g2.drawLine(iconX + 1, iconY - 7, iconX + 8, iconY - 7);
                g2.drawLine(iconX + 1, iconY + 7, iconX + 8, iconY + 7);

                // arrow
                g2.drawLine(iconX + 6, iconY, iconX + 17, iconY);
                g2.drawLine(iconX + 17, iconY, iconX + 12, iconY - 5);
                g2.drawLine(iconX + 17, iconY, iconX + 12, iconY + 5);
            }

            g2.setColor(getForeground());
            g2.drawString(getText(), startX + iconW + gap, textY);

            g2.dispose();
        }
    }

    private static class IconBadge extends JPanel {

        IconBadge() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            applyQuality(g2);

            int w = getWidth();
            int h = getHeight();

            RadialGradientPaint outerGlow = new RadialGradientPaint(
                    new Point(w / 2, h / 2),
                    34,
                    new float[]{0f, 0.65f, 1f},
                    new Color[]{
                            new Color(202, 148, 58, 52),
                            new Color(202, 148, 58, 18),
                            new Color(202, 148, 58, 0)
                    }
            );
            g2.setPaint(outerGlow);
            g2.fillOval(0, 0, w, h);

            int size = 46;
            int x = (w - size) / 2;
            int y = (h - size) / 2;

            LinearGradientPaint badgeBg = new LinearGradientPaint(
                    0, y, 0, y + size,
                    new float[]{0f, 1f},
                    new Color[]{
                            new Color(38, 31, 20),
                            new Color(15, 14, 13)
                    }
            );

            g2.setPaint(badgeBg);
            g2.fillOval(x, y, size, size);

            g2.setStroke(new BasicStroke(1.4f));
            g2.setColor(new Color(202, 148, 58, 125));
            g2.drawOval(x, y, size, size);

            g2.setColor(new Color(255, 230, 150, 36));
            g2.drawOval(x + 5, y + 5, size - 10, size - 10);

            // logout icon
            g2.setStroke(new BasicStroke(2.35f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(GOLD_LIGHT);

            int cx = w / 2;
            int cy = h / 2;

            g2.drawLine(cx - 13, cy - 11, cx - 13, cy + 11);
            g2.drawLine(cx - 13, cy - 11, cx - 2, cy - 11);
            g2.drawLine(cx - 13, cy + 11, cx - 2, cy + 11);

            g2.drawLine(cx - 9, cy, cx + 14, cy);
            g2.drawLine(cx + 14, cy, cx + 6, cy - 8);
            g2.drawLine(cx + 14, cy, cx + 6, cy + 8);

            g2.dispose();
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), ARC, ARC));
        }
        super.setVisible(visible);
    }
}