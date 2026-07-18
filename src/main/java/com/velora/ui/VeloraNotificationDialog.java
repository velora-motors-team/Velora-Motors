package com.velora.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

public final class VeloraNotificationDialog extends JDialog {

    private static final Color BG = new Color(4, 9, 15);
    private static final Color PANEL = new Color(7, 14, 21);
    private static final Color PANEL_2 = new Color(12, 20, 29);

    private static final Color GOLD = new Color(214, 160, 66);
    private static final Color GOLD_LIGHT = new Color(238, 201, 139);
    private static final Color GOLD_DARK = new Color(164, 103, 33);

    private static final Color TEXT = new Color(244, 245, 247);
    private static final Color MUTED = new Color(166, 173, 184);

    private static final Color GREEN = new Color(86, 207, 114);
    private static final Color RED = new Color(235, 93, 98);
    private static final Color BLUE = new Color(82, 156, 255);
    private static final Color ORANGE = new Color(240, 170, 70);

    private final NotificationType type;

    private enum NotificationType {
        SUCCESS,
        WARNING,
        ERROR,
        INFO
    }

    private VeloraNotificationDialog(
            Window parent,
            String title,
            String message,
            NotificationType type
    ) {
        super(parent);

        this.type = type;

        setModal(true);
        setUndecorated(true);
        setResizable(false);
        setSize(460, 215);
        setBackground(new Color(0, 0, 0, 0));
        setLocationRelativeTo(parent);

        setContentPane(createContent(title, message));
    }

    public static void showSuccess(Component parent, String title, String message) {
        new VeloraNotificationDialog(
                getParentWindow(parent),
                title,
                message,
                NotificationType.SUCCESS
        ).setVisible(true);
    }

    public static void showWarning(Component parent, String title, String message) {
        new VeloraNotificationDialog(
                getParentWindow(parent),
                title,
                message,
                NotificationType.WARNING
        ).setVisible(true);
    }

    public static void showError(Component parent, String title, String message) {
        new VeloraNotificationDialog(
                getParentWindow(parent),
                title,
                message,
                NotificationType.ERROR
        ).setVisible(true);
    }

    public static void showInfo(Component parent, String title, String message) {
        new VeloraNotificationDialog(
                getParentWindow(parent),
                title,
                message,
                NotificationType.INFO
        ).setVisible(true);
    }

    public static String showReplyDialog(Component parent, String customerName) {
        ReplyDialog dialog = new ReplyDialog(
                getParentWindow(parent),
                customerName
        );

        dialog.setVisible(true);
        return dialog.getReply();
    }

    private static Window getParentWindow(Component parent) {
        return parent == null
                ? null
                : SwingUtilities.getWindowAncestor(parent);
    }

    private JComponent createContent(String title, String message) {
        NotificationCard card = new NotificationCard();
        card.setLayout(new BorderLayout(0, 0));
        card.setBorder(new EmptyBorder(18, 18, 16, 18));

        JPanel topRow = new JPanel(new BorderLayout(16, 0));
        topRow.setOpaque(false);

        NotificationIconBadge icon = new NotificationIconBadge(type, typeColor());
        icon.setPreferredSize(new Dimension(66, 66));
        icon.setMinimumSize(new Dimension(66, 66));
        icon.setMaximumSize(new Dimension(66, 66));

        JPanel textBlock = new JPanel();
        textBlock.setOpaque(false);
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));

        JLabel category = new JLabel(typeLabel());
        category.setForeground(typeColor());
        category.setFont(new Font("Segoe UI", Font.BOLD, 10));
        category.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title == null ? "" : title);
        titleLabel.setForeground(TEXT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea messageArea = new JTextArea(message == null ? "" : message);
        messageArea.setEditable(false);
        messageArea.setFocusable(false);
        messageArea.setOpaque(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setForeground(MUTED);
        messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageArea.setRows(2);
        messageArea.setBorder(null);
        messageArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        textBlock.add(Box.createVerticalStrut(2));
        textBlock.add(category);
        textBlock.add(Box.createVerticalStrut(4));
        textBlock.add(titleLabel);
        textBlock.add(Box.createVerticalStrut(8));
        textBlock.add(messageArea);

        JButton close = new CloseButton();
        close.setPreferredSize(new Dimension(30, 30));
        close.addActionListener(e -> dispose());

        topRow.add(icon, BorderLayout.WEST);
        topRow.add(textBlock, BorderLayout.CENTER);
        topRow.add(close, BorderLayout.EAST);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(14, 82, 0, 0));

        JLabel hint = new JLabel("VELORA MOTORS  •  PREMIUM CUSTOMER EXPERIENCE");
        hint.setForeground(new Color(145, 151, 161));
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 9));

        JButton ok = new GoldButton("Got it");
        ok.setPreferredSize(new Dimension(96, 34));
        ok.addActionListener(e -> dispose());

        bottom.add(hint, BorderLayout.WEST);
        bottom.add(ok, BorderLayout.EAST);

        card.add(topRow, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    public static boolean showConfirm(
            Component parent,
            String title,
            String message,
            String confirmText
    ) {
        ConfirmDialog dialog = new ConfirmDialog(
                getParentWindow(parent),
                title,
                message,
                confirmText
        );
        dialog.setVisible(true);
        return dialog.isConfirmed();
    }

    private Color typeColor() {
        return switch (type) {
            case SUCCESS -> GREEN;
            case WARNING -> ORANGE;
            case ERROR -> RED;
            case INFO -> BLUE;
        };
    }

    private String typeLabel() {
        return switch (type) {
            case SUCCESS -> "SUCCESS";
            case WARNING -> "WARNING";
            case ERROR -> "ERROR";
            case INFO -> "INFORMATION";
        };
    }

    private final class NotificationCard extends JPanel {

        NotificationCard() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g.setColor(new Color(0, 0, 0, 115));
            g.fillRoundRect(
                    7,
                    8,
                    Math.max(0, w - 14),
                    Math.max(0, h - 14),
                    24,
                    24
            );

            RoundRectangle2D shape = new RoundRectangle2D.Double(
                    0.5,
                    0.5,
                    w - 1,
                    h - 1,
                    22,
                    22
            );

            g.setPaint(new GradientPaint(
                    0,
                    0,
                    new Color(18, 27, 36),
                    w,
                    h,
                    BG
            ));
            g.fill(shape);

            g.setPaint(new GradientPaint(
                    0,
                    0,
                    new Color(255, 235, 181, 24),
                    w,
                    0,
                    new Color(214, 160, 66, 2)
            ));
            g.fill(shape);

            g.setColor(new Color(
                    GOLD.getRed(),
                    GOLD.getGreen(),
                    GOLD.getBlue(),
                    105
            ));
            g.setStroke(new BasicStroke(1.15f));
            g.draw(shape);

            g.setPaint(new GradientPaint(
                    0,
                    25,
                    typeColor(),
                    0,
                    h - 25,
                    new Color(
                            typeColor().getRed(),
                            typeColor().getGreen(),
                            typeColor().getBlue(),
                            30
                    )
            ));
            g.fillRoundRect(0, 22, 4, Math.max(0, h - 44), 4, 4);

            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class NotificationIconBadge extends JComponent {

        private final NotificationType type;
        private final Color accent;

        NotificationIconBadge(NotificationType type, Color accent) {
            this.type = type;
            this.accent = accent;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );
            g.setRenderingHint(
                    RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_PURE
            );

            int s = Math.min(getWidth(), getHeight()) - 4;
            int x = (getWidth() - s) / 2;
            int y = (getHeight() - s) / 2;

            g.setPaint(new GradientPaint(
                    x,
                    y,
                    new Color(126, 86, 26),
                    x + s,
                    y + s,
                    new Color(31, 22, 13)
            ));
            g.fillRoundRect(x, y, s, s, 14, 14);

            g.setPaint(new GradientPaint(
                    x,
                    y,
                    new Color(255, 239, 185, 165),
                    x + s,
                    y + s / 2,
                    new Color(255, 204, 96, 0)
            ));
            g.fillRoundRect(x + 1, y + 1, s - 2, Math.max(16, s / 2), 13, 13);

            g.setColor(new Color(
                    accent.getRed(),
                    accent.getGreen(),
                    accent.getBlue(),
                    235
            ));
            g.setStroke(new BasicStroke(1.4f));
            g.drawRoundRect(x, y, s - 1, s - 1, 14, 14);

            g.setColor(new Color(255, 228, 160, 65));
            g.drawRoundRect(x + 3, y + 3, s - 7, s - 7, 11, 11);

            g.setColor(accent);
            g.setStroke(new BasicStroke(
                    2.4f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            switch (type) {
                case SUCCESS -> drawSuccess(g, cx, cy);
                case WARNING -> drawWarning(g, cx, cy);
                case ERROR -> drawError(g, cx, cy);
                case INFO -> drawInfo(g, cx, cy);
            }

            g.dispose();
        }

        private void drawSuccess(Graphics2D g, int cx, int cy) {
            g.drawOval(cx - 12, cy - 12, 24, 24);
            g.drawLine(cx - 7, cy, cx - 2, cy + 6);
            g.drawLine(cx - 2, cy + 6, cx + 9, cy - 7);
        }

        private void drawWarning(Graphics2D g, int cx, int cy) {
            Path2D triangle = new Path2D.Double();
            triangle.moveTo(cx, cy - 14);
            triangle.lineTo(cx + 14, cy + 11);
            triangle.lineTo(cx - 14, cy + 11);
            triangle.closePath();

            g.draw(triangle);
            g.drawLine(cx, cy - 5, cx, cy + 3);
            g.fillOval(cx - 2, cy + 6, 4, 4);
        }

        private void drawError(Graphics2D g, int cx, int cy) {
            g.drawOval(cx - 12, cy - 12, 24, 24);
            g.drawLine(cx - 7, cy - 7, cx + 7, cy + 7);
            g.drawLine(cx + 7, cy - 7, cx - 7, cy + 7);
        }

        private void drawInfo(Graphics2D g, int cx, int cy) {
            g.drawOval(cx - 12, cy - 12, 24, 24);
            g.fillOval(cx - 2, cy - 8, 4, 4);
            g.drawLine(cx, cy - 1, cx, cy + 9);
        }
    }

    private static final class CloseButton extends JButton {

        CloseButton() {
            super("×");

            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(MUTED);
            setFont(new Font("Segoe UI", Font.PLAIN, 20));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (getModel().isRollover()) {
                g.setColor(new Color(214, 160, 66, 24));
                g.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
                setForeground(GOLD_LIGHT);
            } else {
                setForeground(MUTED);
            }

            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class GoldButton extends JButton {

        GoldButton(String text) {
            super(text);

            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);

            setForeground(new Color(30, 20, 8));
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            Color start = getModel().isRollover()
                    ? new Color(250, 220, 160)
                    : GOLD_LIGHT;

            g.setPaint(new GradientPaint(
                    0,
                    0,
                    start,
                    getWidth(),
                    getHeight(),
                    GOLD_DARK
            ));

            g.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    10,
                    10
            );

            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class OutlineButton extends JButton {

        OutlineButton(String text) {
            super(text);

            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(GOLD_LIGHT);

            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(getModel().isRollover()
                    ? new Color(12, 21, 30)
                    : new Color(5, 11, 17));

            g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

            g.setColor(new Color(
                    GOLD.getRed(),
                    GOLD.getGreen(),
                    GOLD.getBlue(),
                    getModel().isRollover() ? 165 : 110
            ));

            g.drawRoundRect(
                    0,
                    0,
                    getWidth() - 1,
                    getHeight() - 1,
                    10,
                    10
            );

            g.dispose();
            super.paintComponent(raw);
        }
    }


    private static final class ConfirmDialog extends JDialog {

        private boolean confirmed = false;

        ConfirmDialog(Window parent, String title, String message, String confirmText) {
            super(parent);

            setModal(true);
            setUndecorated(true);
            setResizable(false);
            setSize(455, 225);
            setBackground(new Color(0, 0, 0, 0));
            setLocationRelativeTo(parent);

            setContentPane(createConfirmContent(title, message, confirmText));
        }

        boolean isConfirmed() {
            return confirmed;
        }

        private JComponent createConfirmContent(String title, String message, String confirmText) {
            ConfirmCard card = new ConfirmCard();
            card.setLayout(new BorderLayout(0, 16));
            card.setBorder(new EmptyBorder(20, 20, 18, 20));

            JPanel header = new JPanel(new BorderLayout(14, 0));
            header.setOpaque(false);

            ConfirmIconBadge icon = new ConfirmIconBadge();
            icon.setPreferredSize(new Dimension(60, 60));

            JPanel textBlock = new JPanel();
            textBlock.setOpaque(false);
            textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));

            JLabel category = new JLabel("PAYMENT CONFIRMATION");
            category.setForeground(GOLD);
            category.setFont(new Font("Segoe UI", Font.BOLD, 10));

            JLabel titleLabel = new JLabel(title == null ? "Confirm" : title);
            titleLabel.setForeground(TEXT);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));

            JTextArea messageArea = new JTextArea(message == null ? "" : message);
            messageArea.setEditable(false);
            messageArea.setFocusable(false);
            messageArea.setOpaque(false);
            messageArea.setLineWrap(true);
            messageArea.setWrapStyleWord(true);
            messageArea.setForeground(MUTED);
            messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            messageArea.setRows(2);
            messageArea.setBorder(null);

            textBlock.add(category);
            textBlock.add(Box.createVerticalStrut(4));
            textBlock.add(titleLabel);
            textBlock.add(Box.createVerticalStrut(8));
            textBlock.add(messageArea);

            header.add(icon, BorderLayout.WEST);
            header.add(textBlock, BorderLayout.CENTER);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            buttons.setOpaque(false);

            JButton cancel = new OutlineButton("Cancel");
            cancel.setPreferredSize(new Dimension(98, 36));
            cancel.addActionListener(e -> {
                confirmed = false;
                dispose();
            });

            JButton confirm = new GoldButton(
                    confirmText == null || confirmText.isBlank() ? "Confirm" : confirmText
            );
            confirm.setPreferredSize(new Dimension(112, 36));
            confirm.addActionListener(e -> {
                confirmed = true;
                dispose();
            });

            buttons.add(cancel);
            buttons.add(confirm);

            card.add(header, BorderLayout.CENTER);
            card.add(buttons, BorderLayout.SOUTH);

            return card;
        }

        private static final class ConfirmCard extends JPanel {

            ConfirmCard() {
                setOpaque(false);
            }

            @Override
            protected void paintComponent(Graphics raw) {
                Graphics2D g = (Graphics2D) raw.create();

                g.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                int w = getWidth();
                int h = getHeight();

                g.setColor(new Color(0, 0, 0, 105));
                g.fillRoundRect(
                        7,
                        8,
                        Math.max(0, w - 14),
                        Math.max(0, h - 14),
                        24,
                        24
                );

                RoundRectangle2D shape = new RoundRectangle2D.Double(
                        0.5,
                        0.5,
                        w - 1,
                        h - 1,
                        22,
                        22
                );

                g.setPaint(new GradientPaint(
                        0,
                        0,
                        new Color(18, 27, 36),
                        w,
                        h,
                        BG
                ));
                g.fill(shape);

                g.setPaint(new GradientPaint(
                        0,
                        0,
                        new Color(255, 235, 181, 22),
                        w,
                        0,
                        new Color(214, 160, 66, 2)
                ));
                g.fill(shape);

                g.setColor(new Color(
                        GOLD.getRed(),
                        GOLD.getGreen(),
                        GOLD.getBlue(),
                        110
                ));
                g.setStroke(new BasicStroke(1.15f));
                g.draw(shape);

                g.dispose();
                super.paintComponent(raw);
            }
        }
    }

    private static final class ConfirmIconBadge extends JComponent {

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int s = Math.min(getWidth(), getHeight()) - 4;
            int x = (getWidth() - s) / 2;
            int y = (getHeight() - s) / 2;

            g.setPaint(new GradientPaint(
                    x, y, new Color(126, 86, 26),
                    x + s, y + s, new Color(31, 22, 13)
            ));
            g.fillRoundRect(x, y, s, s, 14, 14);

            g.setPaint(new GradientPaint(
                    x, y, new Color(255, 239, 185, 165),
                    x + s, y + s / 2, new Color(255, 204, 96, 0)
            ));
            g.fillRoundRect(x + 1, y + 1, s - 2, Math.max(16, s / 2), 13, 13);

            g.setColor(new Color(214, 160, 66, 230));
            g.drawRoundRect(x, y, s - 1, s - 1, 14, 14);

            g.setColor(GOLD_LIGHT);
            g.setStroke(new BasicStroke(
                    2.0f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            g.drawRoundRect(cx - 13, cy - 9, 26, 18, 6, 6);
            g.drawLine(cx - 8, cy - 3, cx + 8, cy - 3);
            g.drawLine(cx - 7, cy + 3, cx + 1, cy + 3);

            g.dispose();
        }
    }

    private static final class ReplyDialog extends JDialog {

        private String reply;
        private final JTextArea replyArea = new JTextArea();

        ReplyDialog(Window parent, String customerName) {
            super(parent);

            setModal(true);
            setUndecorated(true);
            setResizable(false);
            setSize(470, 330);
            setBackground(new Color(0, 0, 0, 0));
            setLocationRelativeTo(parent);

            setContentPane(createReplyContent(customerName));
        }

        String getReply() {
            return reply;
        }

        private JComponent createReplyContent(String customerName) {
            ReplyCard card = new ReplyCard();

            card.setLayout(new BorderLayout(0, 16));
            card.setBorder(new EmptyBorder(20, 20, 18, 20));

            JPanel header = new JPanel(new BorderLayout(14, 0));
            header.setOpaque(false);

            ReplyIconBadge icon = new ReplyIconBadge();
            icon.setPreferredSize(new Dimension(58, 58));

            JPanel headerText = new JPanel();
            headerText.setOpaque(false);
            headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));

            JLabel category = new JLabel("CUSTOMER SUPPORT");
            category.setForeground(GOLD);
            category.setFont(new Font("Segoe UI", Font.BOLD, 10));

            JLabel title = new JLabel("Reply to " + customerName);
            title.setForeground(TEXT);
            title.setFont(new Font("Segoe UI", Font.BOLD, 18));

            JLabel subtitle = new JLabel("Write a clear and professional response below.");
            subtitle.setForeground(MUTED);
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));

            headerText.add(category);
            headerText.add(Box.createVerticalStrut(3));
            headerText.add(title);
            headerText.add(Box.createVerticalStrut(5));
            headerText.add(subtitle);

            header.add(icon, BorderLayout.WEST);
            header.add(headerText, BorderLayout.CENTER);

            card.add(header, BorderLayout.NORTH);

            replyArea.setLineWrap(true);
            replyArea.setWrapStyleWord(true);
            replyArea.setForeground(TEXT);
            replyArea.setCaretColor(GOLD_LIGHT);
            replyArea.setBackground(new Color(5, 12, 19));
            replyArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            replyArea.setBorder(new EmptyBorder(13, 13, 13, 13));

            JScrollPane scroll = new JScrollPane(replyArea);
            scroll.setBorder(BorderFactory.createLineBorder(
                    new Color(
                            GOLD.getRed(),
                            GOLD.getGreen(),
                            GOLD.getBlue(),
                            95
                    )
            ));
            scroll.getViewport().setBackground(new Color(5, 12, 19));

            card.add(scroll, BorderLayout.CENTER);

            JPanel buttons = new JPanel(
                    new FlowLayout(
                            FlowLayout.RIGHT,
                            10,
                            0
                    )
            );
            buttons.setOpaque(false);

            JButton cancel = new OutlineButton("Cancel");
            cancel.setPreferredSize(new Dimension(96, 34));
            cancel.addActionListener(e -> {
                reply = null;
                dispose();
            });

            JButton send = new GoldButton("Send Reply");
            send.setPreferredSize(new Dimension(118, 34));
            send.addActionListener(e -> {
                String value = replyArea.getText().trim();

                if (value.isEmpty()) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }

                reply = value;
                dispose();
            });

            buttons.add(cancel);
            buttons.add(send);

            card.add(buttons, BorderLayout.SOUTH);

            SwingUtilities.invokeLater(replyArea::requestFocusInWindow);

            return card;
        }

        private static final class ReplyCard extends JPanel {

            ReplyCard() {
                setOpaque(false);
            }

            @Override
            protected void paintComponent(Graphics raw) {
                Graphics2D g = (Graphics2D) raw.create();

                g.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                int w = getWidth();
                int h = getHeight();

                g.setColor(new Color(0, 0, 0, 105));
                g.fillRoundRect(
                        7,
                        8,
                        Math.max(0, w - 14),
                        Math.max(0, h - 14),
                        24,
                        24
                );

                RoundRectangle2D shape = new RoundRectangle2D.Double(
                        0.5,
                        0.5,
                        w - 1,
                        h - 1,
                        22,
                        22
                );

                g.setPaint(new GradientPaint(
                        0,
                        0,
                        new Color(18, 27, 36),
                        w,
                        h,
                        BG
                ));
                g.fill(shape);

                g.setPaint(new GradientPaint(
                        0,
                        0,
                        new Color(255, 235, 181, 22),
                        w,
                        0,
                        new Color(214, 160, 66, 2)
                ));
                g.fill(shape);

                g.setColor(new Color(
                        GOLD.getRed(),
                        GOLD.getGreen(),
                        GOLD.getBlue(),
                        110
                ));
                g.setStroke(new BasicStroke(1.15f));
                g.draw(shape);

                g.dispose();
                super.paintComponent(raw);
            }
        }

        private static final class ReplyIconBadge extends JComponent {

            @Override
            protected void paintComponent(Graphics raw) {
                Graphics2D g = (Graphics2D) raw.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int s = Math.min(getWidth(), getHeight()) - 4;
                int x = (getWidth() - s) / 2;
                int y = (getHeight() - s) / 2;

                g.setPaint(new GradientPaint(
                        x,
                        y,
                        new Color(126, 86, 26),
                        x + s,
                        y + s,
                        new Color(31, 22, 13)
                ));
                g.fillRoundRect(x, y, s, s, 14, 14);

                g.setPaint(new GradientPaint(
                        x,
                        y,
                        new Color(255, 239, 185, 165),
                        x + s,
                        y + s / 2,
                        new Color(255, 204, 96, 0)
                ));
                g.fillRoundRect(x + 1, y + 1, s - 2, Math.max(16, s / 2), 13, 13);

                g.setColor(new Color(214, 160, 66, 230));
                g.drawRoundRect(x, y, s - 1, s - 1, 14, 14);

                g.setColor(GOLD_LIGHT);
                g.setStroke(new BasicStroke(
                        2.0f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                ));

                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                g.drawRoundRect(cx - 13, cy - 9, 26, 18, 6, 6);
                g.drawLine(cx - 6, cy - 1, cx + 6, cy - 1);
                g.drawLine(cx - 6, cy + 4, cx + 2, cy + 4);

                g.dispose();
            }
        }
    }
}
