package com.velora.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;

public final class VeloraNotificationDialog extends JDialog {

    private static final Color BG = new Color(5, 10, 16);
    private static final Color PANEL = new Color(10, 18, 27);

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
        setSize(365, 165);
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

    public static String showReplyDialog(
            Component parent,
            String customerName
    ) {
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

    private JComponent createContent(
            String title,
            String message
    ) {
        NotificationCard card = new NotificationCard();

        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(
                new EmptyBorder(
                        18,
                        18,
                        16,
                        16
                )
        );

        NotificationIcon icon = new NotificationIcon(
                type,
                typeColor()
        );

        icon.setPreferredSize(new Dimension(52, 52));
        card.add(icon, BorderLayout.WEST);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(
                new BoxLayout(
                        center,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT);
        titleLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        16
                )
        );
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea messageArea = new JTextArea(message);
        messageArea.setEditable(false);
        messageArea.setOpaque(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setForeground(MUTED);
        messageArea.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        11
                )
        );
        messageArea.setRows(2);
        messageArea.setBorder(null);
        messageArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        center.add(titleLabel);
        center.add(Box.createVerticalStrut(6));
        center.add(messageArea);

        card.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT,
                        0,
                        0
                )
        );
        bottom.setOpaque(false);

        JButton ok = new GoldButton("OK");
        ok.setPreferredSize(new Dimension(82, 30));
        ok.addActionListener(e -> dispose());

        bottom.add(ok);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    private Color typeColor() {
        return switch (type) {
            case SUCCESS -> GREEN;
            case WARNING -> ORANGE;
            case ERROR -> RED;
            case INFO -> BLUE;
        };
    }

    private final class NotificationCard extends JPanel {

        NotificationCard() {
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

            g.setColor(new Color(0, 0, 0, 100));
            g.fillRoundRect(
                    5,
                    6,
                    Math.max(0, w - 10),
                    Math.max(0, h - 10),
                    20,
                    20
            );

            g.setPaint(
                    new GradientPaint(
                            0,
                            0,
                            new Color(14, 24, 35),
                            w,
                            h,
                            BG
                    )
            );

            g.fillRoundRect(
                    0,
                    0,
                    w,
                    h,
                    20,
                    20
            );

            g.setColor(
                    new Color(
                            GOLD.getRed(),
                            GOLD.getGreen(),
                            GOLD.getBlue(),
                            120
                    )
            );

            g.setStroke(new BasicStroke(1.1f));

            g.drawRoundRect(
                    0,
                    0,
                    w - 1,
                    h - 1,
                    20,
                    20
            );

            g.setPaint(
                    new GradientPaint(
                            0,
                            12,
                            typeColor(),
                            0,
                            h - 12,
                            new Color(
                                    typeColor().getRed(),
                                    typeColor().getGreen(),
                                    typeColor().getBlue(),
                                    40
                            )
                    )
            );

            g.fillRoundRect(
                    0,
                    12,
                    3,
                    Math.max(0, h - 24),
                    3,
                    3
            );

            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class NotificationIcon extends JComponent {

        private final NotificationType type;
        private final Color color;

        NotificationIcon(
                NotificationType type,
                Color color
        ) {
            this.type = type;
            this.color = color;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int s = Math.min(getWidth(), getHeight()) - 8;
            int x = (getWidth() - s) / 2;
            int y = (getHeight() - s) / 2;

            g.setColor(
                    new Color(
                            color.getRed(),
                            color.getGreen(),
                            color.getBlue(),
                            24
                    )
            );

            g.fillOval(x, y, s, s);

            g.setColor(color);
            g.setStroke(
                    new BasicStroke(
                            2.1f,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND
                    )
            );

            g.drawOval(x, y, s, s);

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            switch (type) {
                case SUCCESS -> {
                    g.drawLine(cx - 8, cy, cx - 2, cy + 7);
                    g.drawLine(cx - 2, cy + 7, cx + 10, cy - 8);
                }

                case WARNING -> {
                    Path2D triangle = new Path2D.Double();
                    triangle.moveTo(cx, cy - 12);
                    triangle.lineTo(cx + 13, cy + 11);
                    triangle.lineTo(cx - 13, cy + 11);
                    triangle.closePath();

                    g.draw(triangle);
                    g.drawLine(cx, cy - 4, cx, cy + 4);
                    g.fillOval(cx - 2, cy + 7, 4, 4);
                }

                case ERROR -> {
                    g.drawLine(cx - 8, cy - 8, cx + 8, cy + 8);
                    g.drawLine(cx + 8, cy - 8, cx - 8, cy + 8);
                }

                case INFO -> {
                    g.fillOval(cx - 2, cy - 9, 4, 4);
                    g.drawLine(cx, cy - 1, cx, cy + 9);
                }
            }

            g.dispose();
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

            setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            11
                    )
            );

            setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );

            addMouseListener(
                    new MouseAdapter() {
                        @Override
                        public void mouseEntered(MouseEvent e) {
                            repaint();
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                            repaint();
                        }
                    }
            );
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            Color start =
                    getModel().isRollover()
                            ? new Color(250, 220, 160)
                            : GOLD_LIGHT;

            g.setPaint(
                    new GradientPaint(
                            0,
                            0,
                            start,
                            getWidth(),
                            getHeight(),
                            GOLD_DARK
                    )
            );

            g.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    9,
                    9
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
            setFocusPainted(false);
            setForeground(GOLD_LIGHT);

            setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            11
                    )
            );

            setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );

            setBorder(
                    BorderFactory.createLineBorder(
                            new Color(
                                    GOLD.getRed(),
                                    GOLD.getGreen(),
                                    GOLD.getBlue(),
                                    120
                            )
                    )
            );
        }
    }

    private static final class ReplyDialog extends JDialog {

        private String reply;
        private final JTextArea replyArea = new JTextArea();

        ReplyDialog(
                Window parent,
                String customerName
        ) {
            super(parent);

            setModal(true);
            setUndecorated(true);
            setResizable(false);
            setSize(430, 285);
            setBackground(new Color(0, 0, 0, 0));
            setLocationRelativeTo(parent);

            setContentPane(
                    createReplyContent(customerName)
            );
        }

        String getReply() {
            return reply;
        }

        private JComponent createReplyContent(
                String customerName
        ) {
            ReplyCard card = new ReplyCard();

            card.setLayout(
                    new BorderLayout(0, 14)
            );

            card.setBorder(
                    new EmptyBorder(
                            18,
                            18,
                            16,
                            18
                    )
            );

            JPanel header = new JPanel();
            header.setOpaque(false);

            header.setLayout(
                    new BoxLayout(
                            header,
                            BoxLayout.Y_AXIS
                    )
            );

            JLabel title = new JLabel(
                    "Reply to "
                    + customerName
            );

            title.setForeground(TEXT);

            title.setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            17
                    )
            );

            JLabel subtitle = new JLabel(
                    "Write your response below."
            );

            subtitle.setForeground(MUTED);

            subtitle.setFont(
                    new Font(
                            "Segoe UI",
                            Font.PLAIN,
                            11
                    )
            );

            header.add(title);
            header.add(Box.createVerticalStrut(5));
            header.add(subtitle);

            card.add(header, BorderLayout.NORTH);

            replyArea.setLineWrap(true);
            replyArea.setWrapStyleWord(true);
            replyArea.setForeground(TEXT);
            replyArea.setCaretColor(GOLD_LIGHT);
            replyArea.setBackground(new Color(7, 15, 23));

            replyArea.setFont(
                    new Font(
                            "Segoe UI",
                            Font.PLAIN,
                            13
                    )
            );

            replyArea.setBorder(
                    new EmptyBorder(
                            12,
                            12,
                            12,
                            12
                    )
            );

            JScrollPane scroll = new JScrollPane(replyArea);

            scroll.setBorder(
                    BorderFactory.createLineBorder(
                            new Color(
                                    GOLD.getRed(),
                                    GOLD.getGreen(),
                                    GOLD.getBlue(),
                                    95
                            )
                    )
            );

            scroll.getViewport().setBackground(
                    new Color(7, 15, 23)
            );

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
            cancel.setPreferredSize(new Dimension(92, 32));

            cancel.addActionListener(e -> {
                reply = null;
                dispose();
            });

            JButton send = new GoldButton("Send Reply");
            send.setPreferredSize(new Dimension(112, 32));

            send.addActionListener(e -> {
                reply = replyArea.getText().trim();
                dispose();
            });

            buttons.add(cancel);
            buttons.add(send);

            card.add(buttons, BorderLayout.SOUTH);

            SwingUtilities.invokeLater(
                    replyArea::requestFocusInWindow
            );

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

                g.setColor(new Color(0, 0, 0, 95));

                g.fillRoundRect(
                        5,
                        6,
                        Math.max(0, w - 10),
                        Math.max(0, h - 10),
                        20,
                        20
                );

                g.setPaint(
                        new GradientPaint(
                                0,
                                0,
                                new Color(14, 24, 35),
                                w,
                                h,
                                BG
                        )
                );

                g.fillRoundRect(
                        0,
                        0,
                        w,
                        h,
                        20,
                        20
                );

                g.setColor(
                        new Color(
                                GOLD.getRed(),
                                GOLD.getGreen(),
                                GOLD.getBlue(),
                                115
                        )
                );

                g.setStroke(new BasicStroke(1.1f));

                g.drawRoundRect(
                        0,
                        0,
                        w - 1,
                        h - 1,
                        20,
                        20
                );

                g.setPaint(
                        new GradientPaint(
                                0,
                                0,
                                GOLD,
                                0,
                                h,
                                new Color(
                                        GOLD.getRed(),
                                        GOLD.getGreen(),
                                        GOLD.getBlue(),
                                        45
                                )
                        )
                );

                g.fillRoundRect(
                        0,
                        16,
                        3,
                        Math.max(0, h - 32),
                        3,
                        3
                );

                g.dispose();
                super.paintComponent(raw);
            }
        }
    }
}
