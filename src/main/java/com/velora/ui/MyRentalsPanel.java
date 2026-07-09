package com.velora.ui;

import com.velora.authentication.Customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public final class MyRentalsPanel extends JPanel {

    private static final Color CARD = new Color(7, 14, 21);
    private static final Color CARD_DARK = new Color(4, 10, 16);
    private static final Color CARD_HOVER = new Color(12, 23, 33);

    private static final Color GOLD = new Color(214, 160, 66);
    private static final Color GOLD_LIGHT = new Color(239, 196, 116);
    private static final Color PALE = new Color(238, 201, 139);

    private static final Color TEXT = new Color(243, 244, 247);
    private static final Color MUTED = new Color(170, 179, 192);

    private static final Color GREEN = new Color(86, 207, 114);
    private static final Color RED = new Color(235, 93, 98);
    private static final Color ORANGE = new Color(242, 174, 73);

    private final Customer customer;

    public MyRentalsPanel(Customer customer) {

        this.customer = customer;

        setOpaque(false);
        setLayout(new BorderLayout(0, 14));

        setBorder(
                new EmptyBorder(
                        16,
                        22,
                        16,
                        22
                )
        );

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
    }

    /* =========================================================
       HEADER
       ========================================================= */

    private JComponent createHeader() {

        JPanel header = new JPanel(
                new BorderLayout(24, 0)
        );

        header.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);

        left.setLayout(
                new BoxLayout(
                        left,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel eyebrow = label(
                "RENTAL CENTER",
                11,
                Font.BOLD,
                GOLD
        );

        eyebrow.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        JLabel title = label(
                "My Rentals",
                29,
                Font.BOLD,
                TEXT
        );

        title.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        JLabel sub = label(
                "Manage active rentals, return schedules, payment status, and your complete rental history.",
                13,
                Font.PLAIN,
                MUTED
        );

        sub.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        left.add(eyebrow);
        left.add(Box.createVerticalStrut(5));
        left.add(title);
        left.add(Box.createVerticalStrut(6));
        left.add(sub);

        JPanel right = new JPanel();
        right.setOpaque(false);

        right.setLayout(
                new BoxLayout(
                        right,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel status = label(
                "●  Active Customer",
                13,
                Font.BOLD,
                GREEN
        );

        status.setAlignmentX(
                Component.RIGHT_ALIGNMENT
        );

        status.setHorizontalAlignment(
                SwingConstants.RIGHT
        );

        JLabel customerName = label(
                getCustomerDisplayName(),
                11,
                Font.PLAIN,
                MUTED
        );

        customerName.setAlignmentX(
                Component.RIGHT_ALIGNMENT
        );

        customerName.setHorizontalAlignment(
                SwingConstants.RIGHT
        );

        right.add(Box.createVerticalGlue());
        right.add(status);
        right.add(Box.createVerticalStrut(6));
        right.add(customerName);
        right.add(Box.createVerticalGlue());

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private String getCustomerDisplayName() {

        if (customer == null) {
            return "Velora Member";
        }

        try {

            String value = customer.toString();

            if (value != null
                    && !value.trim().isEmpty()) {

                return value;
            }

        } catch (Exception ignored) {
        }

        return "Velora Member";
    }

    /* =========================================================
       BODY
       ========================================================= */

    private JComponent createBody() {

        JPanel body = new JPanel();
        body.setOpaque(false);

        body.setLayout(
                new BoxLayout(
                        body,
                        BoxLayout.Y_AXIS
                )
        );

        /*
         * نزول بسيط للبطاقات
         */
        body.add(
                Box.createVerticalStrut(34)
        );

        /* =====================================================
           STAT CARDS
           ===================================================== */

        JComponent stats = createStatsSection();

        stats.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        stats.setPreferredSize(
                new Dimension(
                        10,
                        102
                )
        );

        stats.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        102
                )
        );

        body.add(stats);

        body.add(
                Box.createVerticalStrut(22)
        );

        /* =====================================================
           RENTALS AREA
           ===================================================== */

        JPanel rentalsArea = new JPanel(
                new GridLayout(
                        1,
                        2,
                        20,
                        0
                )
        );

        rentalsArea.setOpaque(false);

        rentalsArea.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        /*
         * زدت الارتفاع حتى الخطوط
         * والحالات ما تنقص
         */
        rentalsArea.setPreferredSize(
                new Dimension(
                        10,
                        390
                )
        );

        rentalsArea.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        390
                )
        );

        rentalsArea.add(
                createActiveRentalCard()
        );

        rentalsArea.add(
                createHistoryCard()
        );

        body.add(rentalsArea);

        body.add(
                Box.createVerticalStrut(20)
        );

        /* =====================================================
           RENTAL INSIGHTS
           ===================================================== */

        JComponent insights =
                createInsightsSection();

        insights.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        insights.setPreferredSize(
                new Dimension(
                        10,
                        118
                )
        );

        insights.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        118
                )
        );

        body.add(insights);

        body.add(
                Box.createVerticalGlue()
        );

        return body;
    }

    /* =========================================================
       STATS
       ========================================================= */

    private JComponent createStatsSection() {

        JPanel stats = new JPanel(
                new GridLayout(
                        1,
                        4,
                        16,
                        0
                )
        );

        stats.setOpaque(false);

        stats.add(
                statCard(
                        "2",
                        "Active Rentals",
                        "Currently rented",
                        StatIconType.CAR
                )
        );

        stats.add(
                statCard(
                        "5",
                        "Completed",
                        "Rental history",
                        StatIconType.CHECK
                )
        );

        stats.add(
                statCard(
                        "$840",
                        "Current Cost",
                        "Active rental fees",
                        StatIconType.MONEY
                )
        );

        stats.add(
                statCard(
                        "0",
                        "Late Returns",
                        "No overdue rentals",
                        StatIconType.CLOCK
                )
        );

        return stats;
    }

    private JComponent statCard(
            String value,
            String title,
            String desc,
            StatIconType iconType
    ) {

        HoverRoundedPanel card =
                new HoverRoundedPanel(
                        16,
                        CARD,
                        CARD_HOVER
                );

        card.setLayout(
                new BorderLayout(
                        14,
                        0
                )
        );

        card.setBorder(
                new EmptyBorder(
                        14,
                        16,
                        14,
                        16
                )
        );

        StatIcon icon =
                new StatIcon(iconType);

        icon.setPreferredSize(
                new Dimension(
                        48,
                        48
                )
        );

        JPanel text = new JPanel();
        text.setOpaque(false);

        text.setLayout(
                new BoxLayout(
                        text,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel titleLabel = label(
                title,
                13,
                Font.BOLD,
                TEXT
        );

        JLabel valueLabel = label(
                value,
                23,
                Font.BOLD,
                GOLD_LIGHT
        );

        JLabel descLabel = label(
                desc,
                11,
                Font.PLAIN,
                MUTED
        );

        text.add(titleLabel);
        text.add(Box.createVerticalStrut(5));
        text.add(valueLabel);
        text.add(Box.createVerticalStrut(5));
        text.add(descLabel);

        card.add(icon, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);

        return card;
    }

    /* =========================================================
       ACTIVE RENTALS
       ========================================================= */

    private JComponent createActiveRentalCard() {

        RoundedPanel card =
                new RoundedPanel(
                        16,
                        CARD
                );

        card.setLayout(
                new BorderLayout()
        );

        card.setBorder(
                new EmptyBorder(
                        18,
                        18,
                        18,
                        18
                )
        );

        JPanel content = new JPanel();
        content.setOpaque(false);

        content.setLayout(
                new BoxLayout(
                        content,
                        BoxLayout.Y_AXIS
                )
        );

        content.add(
                sectionHeader(
                        "Active Rentals",
                        "2 ongoing",
                        GREEN
                )
        );

        content.add(
                Box.createVerticalStrut(12)
        );

        content.add(
                rentalRow(
                        "BMW X7 xDrive40i",
                        "Return: 09 Jul 2026",
                        "$475",
                        "Active",
                        GREEN,
                        "BMW"
                )
        );

        content.add(
                Box.createVerticalStrut(12)
        );

        content.add(
                rentalRow(
                        "Xiaomi Electric Bike Pro",
                        "Return: Today 08:00 PM",
                        "$36",
                        "Due Today",
                        ORANGE,
                        "E-BIKE"
                )
        );

        content.add(
                Box.createVerticalGlue()
        );

        card.add(
                content,
                BorderLayout.CENTER
        );

        return card;
    }

    /* =========================================================
       RENTAL HISTORY
       ========================================================= */

    private JComponent createHistoryCard() {

        RoundedPanel card =
                new RoundedPanel(
                        16,
                        CARD
                );

        card.setLayout(
                new BorderLayout()
        );

        card.setBorder(
                new EmptyBorder(
                        18,
                        18,
                        18,
                        18
                )
        );

        JPanel content = new JPanel();
        content.setOpaque(false);

        content.setLayout(
                new BoxLayout(
                        content,
                        BoxLayout.Y_AXIS
                )
        );

        content.add(
                sectionHeader(
                        "Rental History",
                        "5 completed",
                        GOLD
                )
        );

        content.add(
                Box.createVerticalStrut(12)
        );

        content.add(
                rentalRow(
                        "Toyota Prius Hybrid",
                        "Completed: 01 Jul 2026",
                        "$180",
                        "Returned",
                        GREEN,
                        "HYBRID"
                )
        );

        content.add(
                Box.createVerticalStrut(12)
        );

        content.add(
                rentalRow(
                        "Yamaha MT-07",
                        "Completed: 28 Jun 2026",
                        "$126",
                        "Returned",
                        GREEN,
                        "MOTO"
                )
        );

        content.add(
                Box.createVerticalStrut(12)
        );

        content.add(
                rentalRow(
                        "Tesla Model 3",
                        "Completed: 20 Jun 2026",
                        "$320",
                        "Returned Late",
                        RED,
                        "EV"
                )
        );

        content.add(
                Box.createVerticalGlue()
        );

        card.add(
                content,
                BorderLayout.CENTER
        );

        return card;
    }

    /* =========================================================
       SECTION HEADER
       ========================================================= */

    private JComponent sectionHeader(
            String title,
            String badgeText,
            Color badgeColor
    ) {

        JPanel header = new JPanel(
                new BorderLayout()
        );

        header.setOpaque(false);

        header.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        30
                )
        );

        JLabel titleLabel = label(
                title,
                18,
                Font.BOLD,
                TEXT
        );

        RoundedLabel badge =
                new RoundedLabel(
                        badgeText,
                        badgeColor,
                        new Color(
                                badgeColor.getRed(),
                                badgeColor.getGreen(),
                                badgeColor.getBlue(),
                                28
                        )
                );

        header.add(
                titleLabel,
                BorderLayout.WEST
        );

        header.add(
                badge,
                BorderLayout.EAST
        );

        return header;
    }

    /* =========================================================
       RENTAL ROW
       ========================================================= */

    private JComponent rentalRow(
            String vehicle,
            String date,
            String price,
            String status,
            Color statusColor,
            String category
    ) {

        HoverRoundedPanel row =
                new HoverRoundedPanel(
                        12,
                        CARD_DARK,
                        new Color(
                                9,
                                18,
                                27
                        )
                );

        row.setLayout(
                new BorderLayout(
                        14,
                        0
                )
        );

        /*
         * أقل Padding عمودي
         * حتى النصوص تظل واضحة
         */
        row.setBorder(
                new EmptyBorder(
                        10,
                        13,
                        10,
                        13
                )
        );

        /*
         * أهم تعديل:
         * ارتفاع أكبر للصف
         */
        row.setPreferredSize(
                new Dimension(
                        10,
                        94
                )
        );

        row.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        94
                )
        );

        VehicleIcon vehicleIcon =
                new VehicleIcon(category);

        vehicleIcon.setPreferredSize(
                new Dimension(
                        50,
                        50
                )
        );

        /* =========================
           INFO
           ========================= */

        JPanel info = new JPanel();
        info.setOpaque(false);

        info.setLayout(
                new BoxLayout(
                        info,
                        BoxLayout.Y_AXIS
                )
        );

        JPanel topLine = new JPanel();
        topLine.setOpaque(false);

        topLine.setLayout(
                new BoxLayout(
                        topLine,
                        BoxLayout.X_AXIS
                )
        );

        topLine.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        JLabel vehicleLabel = label(
                vehicle,
                14,
                Font.BOLD,
                TEXT
        );

        RoundedLabel categoryBadge =
                new RoundedLabel(
                        category,
                        GOLD,
                        new Color(
                                214,
                                160,
                                66,
                                18
                        )
                );

        categoryBadge.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        9
                )
        );

        topLine.add(vehicleLabel);

        topLine.add(
                Box.createHorizontalStrut(8)
        );

        topLine.add(categoryBadge);

        topLine.add(
                Box.createHorizontalGlue()
        );

        JLabel dateLabel = label(
                date,
                11,
                Font.PLAIN,
                MUTED
        );

        JLabel statusLabel = label(
                "●  " + status,
                11,
                Font.BOLD,
                statusColor
        );

        info.add(topLine);

        info.add(
                Box.createVerticalStrut(4)
        );

        info.add(dateLabel);

        info.add(
                Box.createVerticalStrut(4)
        );

        info.add(statusLabel);

        /* =========================
           PRICE
           ========================= */

        JPanel priceBox = new JPanel();
        priceBox.setOpaque(false);

        priceBox.setLayout(
                new BoxLayout(
                        priceBox,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel priceLabel = label(
                price,
                18,
                Font.BOLD,
                PALE
        );

        JLabel feeLabel = label(
                "total",
                10,
                Font.PLAIN,
                MUTED
        );

        priceLabel.setAlignmentX(
                Component.RIGHT_ALIGNMENT
        );

        feeLabel.setAlignmentX(
                Component.RIGHT_ALIGNMENT
        );

        priceBox.add(
                Box.createVerticalGlue()
        );

        priceBox.add(priceLabel);

        priceBox.add(
                Box.createVerticalStrut(2)
        );

        priceBox.add(feeLabel);

        priceBox.add(
                Box.createVerticalGlue()
        );

        row.add(
                vehicleIcon,
                BorderLayout.WEST
        );

        row.add(
                info,
                BorderLayout.CENTER
        );

        row.add(
                priceBox,
                BorderLayout.EAST
        );

        return row;
    }

    /* =========================================================
       RENTAL INSIGHTS
       ========================================================= */

    private JComponent createInsightsSection() {

        RoundedPanel shell =
                new RoundedPanel(
                        16,
                        new Color(
                                6,
                                13,
                                20
                        )
                );

        shell.setLayout(
                new BorderLayout(
                        24,
                        0
                )
        );

        shell.setBorder(
                new EmptyBorder(
                        14,
                        18,
                        14,
                        18
                )
        );

        shell.setPreferredSize(
                new Dimension(
                        10,
                        118
                )
        );

        shell.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        118
                )
        );

        JPanel titleArea = new JPanel();
        titleArea.setOpaque(false);

        titleArea.setLayout(
                new BoxLayout(
                        titleArea,
                        BoxLayout.Y_AXIS
                )
        );

        titleArea.setPreferredSize(
                new Dimension(
                        220,
                        82
                )
        );

        JLabel eyebrow = label(
                "RENTAL INSIGHTS",
                11,
                Font.BOLD,
                GOLD
        );

        JLabel title = label(
                "Your rental snapshot",
                18,
                Font.BOLD,
                TEXT
        );

        JLabel sub = label(
                "Quick status at a glance",
                11,
                Font.PLAIN,
                MUTED
        );

        eyebrow.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        title.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        sub.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        titleArea.add(
                Box.createVerticalGlue()
        );

        titleArea.add(eyebrow);

        titleArea.add(
                Box.createVerticalStrut(4)
        );

        titleArea.add(title);

        titleArea.add(
                Box.createVerticalStrut(4)
        );

        titleArea.add(sub);

        titleArea.add(
                Box.createVerticalGlue()
        );

        JPanel metrics = new JPanel(
                new GridLayout(
                        1,
                        3,
                        16,
                        0
                )
        );

        metrics.setOpaque(false);

        metrics.add(
                insightCard(
                        "Today, 08:00 PM",
                        "Next Return",
                        "Xiaomi Electric Bike Pro",
                        ORANGE
                )
        );

        metrics.add(
                insightCard(
                        "Paid",
                        "Payment Status",
                        "All current fees covered",
                        GREEN
                )
        );

        metrics.add(
                insightCard(
                        "92%",
                        "On-time Return Rate",
                        "Excellent rental record",
                        GOLD
                )
        );

        shell.add(
                titleArea,
                BorderLayout.WEST
        );

        shell.add(
                metrics,
                BorderLayout.CENTER
        );

        return shell;
    }

    private JComponent insightCard(
            String value,
            String title,
            String desc,
            Color accent
    ) {

        RoundedPanel card =
                new RoundedPanel(
                        14,
                        CARD_DARK
                );

        card.setLayout(
                new BorderLayout(
                        12,
                        0
                )
        );

        card.setBorder(
                new EmptyBorder(
                        12,
                        14,
                        12,
                        14
                )
        );

        AccentDot dot =
                new AccentDot(accent);

        dot.setPreferredSize(
                new Dimension(
                        14,
                        14
                )
        );

        JPanel text = new JPanel();
        text.setOpaque(false);

        text.setLayout(
                new BoxLayout(
                        text,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel titleLabel = label(
                title,
                11,
                Font.BOLD,
                MUTED
        );

        JLabel valueLabel = label(
                value,
                17,
                Font.BOLD,
                TEXT
        );

        JLabel descLabel = label(
                desc,
                10,
                Font.PLAIN,
                MUTED
        );

        titleLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        valueLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        descLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        text.add(
                Box.createVerticalGlue()
        );

        text.add(titleLabel);

        text.add(
                Box.createVerticalStrut(4)
        );

        text.add(valueLabel);

        text.add(
                Box.createVerticalStrut(4)
        );

        text.add(descLabel);

        text.add(
                Box.createVerticalGlue()
        );

        card.add(
                dot,
                BorderLayout.WEST
        );

        card.add(
                text,
                BorderLayout.CENTER
        );

        return card;
    }

    /* =========================================================
       LABEL HELPER
       ========================================================= */

    private JLabel label(
            String text,
            int size,
            int style,
            Color color
    ) {

        JLabel label = new JLabel(text);

        label.setFont(
                new Font(
                        "Segoe UI",
                        style,
                        size
                )
        );

        label.setForeground(color);

        return label;
    }

    private enum StatIconType {
        CAR,
        CHECK,
        MONEY,
        CLOCK
    }

    /* =========================================================
       ROUNDED PANEL
       ========================================================= */

    private static class RoundedPanel
            extends JPanel {

        protected final int radius;
        protected Color fill;

        RoundedPanel(
                int radius,
                Color fill
        ) {

            this.radius = radius;
            this.fill = fill;

            setOpaque(false);
        }

        @Override
        protected void paintComponent(
                Graphics raw
        ) {

            Graphics2D g =
                    (Graphics2D) raw.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int w = Math.max(
                    1,
                    getWidth()
            );

            int h = Math.max(
                    1,
                    getHeight()
            );

            RoundRectangle2D shape =
                    new RoundRectangle2D.Double(
                            0.5,
                            0.5,
                            w - 1.0,
                            h - 1.0,
                            radius,
                            radius
                    );

            g.setColor(
                    new Color(
                            0,
                            0,
                            0,
                            65
                    )
            );

            g.fillRoundRect(
                    4,
                    5,
                    Math.max(
                            0,
                            w - 8
                    ),
                    Math.max(
                            0,
                            h - 8
                    ),
                    radius,
                    radius
            );

            g.setPaint(
                    new GradientPaint(
                            0,
                            0,
                            new Color(
                                    13,
                                    23,
                                    31
                            ),
                            w,
                            h,
                            fill
                    )
            );

            g.fill(shape);

            g.setPaint(
                    new GradientPaint(
                            0,
                            0,
                            new Color(
                                    214,
                                    160,
                                    66,
                                    22
                            ),
                            w,
                            0,
                            new Color(
                                    214,
                                    160,
                                    66,
                                    2
                            )
                    )
            );

            g.fill(shape);

            g.setColor(
                    new Color(
                            214,
                            160,
                            66,
                            75
                    )
            );

            g.draw(shape);

            g.dispose();

            super.paintComponent(raw);
        }
    }

    /* =========================================================
       HOVER PANEL
       ========================================================= */

    private static final class HoverRoundedPanel
            extends RoundedPanel {

        private final Color normalColor;
        private final Color hoverColor;

        HoverRoundedPanel(
                int radius,
                Color normalColor,
                Color hoverColor
        ) {

            super(
                    radius,
                    normalColor
            );

            this.normalColor =
                    normalColor;

            this.hoverColor =
                    hoverColor;

            setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );

            addMouseListener(
                    new MouseAdapter() {

                        @Override
                        public void mouseEntered(
                                MouseEvent e
                        ) {

                            fill =
                                    HoverRoundedPanel
                                            .this
                                            .hoverColor;

                            repaint();
                        }

                        @Override
                        public void mouseExited(
                                MouseEvent e
                        ) {

                            fill =
                                    HoverRoundedPanel
                                            .this
                                            .normalColor;

                            repaint();
                        }
                    }
            );
        }
    }

    /* =========================================================
       ROUNDED LABEL
       ========================================================= */

    private static final class RoundedLabel
            extends JLabel {

        private final Color borderColor;
        private final Color backgroundColor;

        RoundedLabel(
                String text,
                Color borderColor,
                Color backgroundColor
        ) {

            super(text);

            this.borderColor =
                    borderColor;

            this.backgroundColor =
                    backgroundColor;

            setOpaque(false);
            setForeground(borderColor);

            setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            10
                    )
            );

            setBorder(
                    new EmptyBorder(
                            3,
                            8,
                            3,
                            8
                    )
            );
        }

        @Override
        protected void paintComponent(
                Graphics raw
        ) {

            Graphics2D g =
                    (Graphics2D) raw.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g.setColor(
                    backgroundColor
            );

            g.fillRoundRect(
                    0,
                    0,
                    Math.max(
                            0,
                            getWidth() - 1
                    ),
                    Math.max(
                            0,
                            getHeight() - 1
                    ),
                    16,
                    16
            );

            g.setColor(
                    new Color(
                            borderColor.getRed(),
                            borderColor.getGreen(),
                            borderColor.getBlue(),
                            100
                    )
            );

            g.drawRoundRect(
                    0,
                    0,
                    Math.max(
                            0,
                            getWidth() - 1
                    ),
                    Math.max(
                            0,
                            getHeight() - 1
                    ),
                    16,
                    16
            );

            g.dispose();

            super.paintComponent(raw);
        }
    }

    /* =========================================================
       ACCENT DOT
       ========================================================= */

    private static final class AccentDot
            extends JComponent {

        private final Color color;

        AccentDot(Color color) {
            this.color = color;
        }

        @Override
        protected void paintComponent(
                Graphics raw
        ) {

            Graphics2D g =
                    (Graphics2D) raw.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int size = 8;

            int x =
                    (getWidth() - size)
                            / 2;

            int y =
                    (getHeight() - size)
                            / 2;

            g.setColor(
                    new Color(
                            color.getRed(),
                            color.getGreen(),
                            color.getBlue(),
                            45
                    )
            );

            g.fillOval(
                    x - 4,
                    y - 4,
                    size + 8,
                    size + 8
            );

            g.setColor(color);

            g.fillOval(
                    x,
                    y,
                    size,
                    size
            );

            g.dispose();
        }
    }

    /* =========================================================
       STAT ICON
       ========================================================= */

    private static final class StatIcon
            extends JComponent {

        private final StatIconType type;

        StatIcon(
                StatIconType type
        ) {

            this.type = type;
        }

        @Override
        protected void paintComponent(
                Graphics raw
        ) {

            Graphics2D g =
                    (Graphics2D) raw.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            g.setColor(
                    new Color(
                            214,
                            160,
                            66,
                            20
                    )
            );

            g.fillOval(
                    cx - 22,
                    cy - 22,
                    44,
                    44
            );

            g.setColor(
                    new Color(
                            214,
                            160,
                            66,
                            55
                    )
            );

            g.setStroke(
                    new BasicStroke(
                            1.3f
                    )
            );

            g.drawOval(
                    cx - 18,
                    cy - 18,
                    36,
                    36
            );

            g.setColor(GOLD);

            g.setStroke(
                    new BasicStroke(
                            2f,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND
                    )
            );

            switch (type) {

                case CAR:

                    drawSmallCar(
                            g,
                            cx,
                            cy
                    );

                    break;

                case CHECK:

                    g.drawLine(
                            cx - 8,
                            cy,
                            cx - 2,
                            cy + 7
                    );

                    g.drawLine(
                            cx - 2,
                            cy + 7,
                            cx + 10,
                            cy - 7
                    );

                    break;

                case MONEY:

                    g.setFont(
                            new Font(
                                    "Segoe UI",
                                    Font.BOLD,
                                    22
                            )
                    );

                    FontMetrics fm =
                            g.getFontMetrics();

                    String money = "$";

                    g.drawString(
                            money,
                            cx
                                    - fm.stringWidth(money)
                                    / 2,
                            cy
                                    + fm.getAscent()
                                    / 2
                                    - 2
                    );

                    break;

                case CLOCK:

                    g.drawOval(
                            cx - 10,
                            cy - 10,
                            20,
                            20
                    );

                    g.drawLine(
                            cx,
                            cy,
                            cx,
                            cy - 6
                    );

                    g.drawLine(
                            cx,
                            cy,
                            cx + 6,
                            cy + 3
                    );

                    break;
                    
                    
                    
                    

                default:
                    break;
            }

            g.dispose();
        }

        private void drawSmallCar(
                Graphics2D g,
                int cx,
                int cy
        ) {

            g.drawRoundRect(
                    cx - 12,
                    cy - 3,
                    24,
                    9,
                    4,
                    4
            );

            g.drawLine(
                    cx - 8,
                    cy - 3,
                    cx - 4,
                    cy - 9
            );

            g.drawLine(
                    cx - 4,
                    cy - 9,
                    cx + 5,
                    cy - 9
            );

            g.drawLine(
                    cx + 5,
                    cy - 9,
                    cx + 9,
                    cy - 3
            );

            g.fillOval(
                    cx - 8,
                    cy + 4,
                    4,
                    4
            );

            g.fillOval(
                    cx + 4,
                    cy + 4,
                    4,
                    4
            );
        }
    }

    /* =========================================================
       VEHICLE ICON
       ========================================================= */

    private static final class VehicleIcon
            extends JComponent {

        private final String category;

        VehicleIcon(
                String category
        ) {

            this.category =
                    category == null
                            ? ""
                            : category.toUpperCase();
        }

        @Override
        protected void paintComponent(
                Graphics raw
        ) {

            Graphics2D g =
                    (Graphics2D) raw.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            g.setColor(
                    new Color(
                            214,
                            160,
                            66,
                            20
                    )
            );

            g.fillOval(
                    cx - 22,
                    cy - 22,
                    44,
                    44
            );

            g.setColor(
                    new Color(
                            214,
                            160,
                            66,
                            45
                    )
            );

            g.setStroke(
                    new BasicStroke(
                            1.2f
                    )
            );

            g.drawOval(
                    cx - 19,
                    cy - 19,
                    38,
                    38
            );

            g.setColor(GOLD);

            g.setStroke(
                    new BasicStroke(
                            2f,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND
                    )
            );

            if (category.contains("BIKE")) {

                drawBike(
                        g,
                        cx,
                        cy
                );

            } else if (
                    category.contains("MOTO")
            ) {

                drawMotorcycle(
                        g,
                        cx,
                        cy
                );

            } else {

                drawCar(
                        g,
                        cx,
                        cy
                );
            }

            g.dispose();
        }

        private void drawCar(
                Graphics2D g,
                int cx,
                int cy
        ) {

            g.drawRoundRect(
                    cx - 16,
                    cy - 3,
                    32,
                    11,
                    5,
                    5
            );

            g.drawLine(
                    cx - 10,
                    cy - 3,
                    cx - 5,
                    cy - 13
            );

            g.drawLine(
                    cx - 5,
                    cy - 13,
                    cx + 7,
                    cy - 13
            );

            g.drawLine(
                    cx + 7,
                    cy - 13,
                    cx + 13,
                    cy - 3
            );

            g.fillOval(
                    cx - 12,
                    cy + 6,
                    5,
                    5
            );

            g.fillOval(
                    cx + 7,
                    cy + 6,
                    5,
                    5
            );
        }

        private void drawBike(
                Graphics2D g,
                int cx,
                int cy
        ) {

            g.drawOval(
                    cx - 16,
                    cy + 1,
                    11,
                    11
            );

            g.drawOval(
                    cx + 6,
                    cy + 1,
                    11,
                    11
            );

            g.drawLine(
                    cx - 10,
                    cy + 6,
                    cx - 2,
                    cy - 5
            );

            g.drawLine(
                    cx - 2,
                    cy - 5,
                    cx + 7,
                    cy + 6
            );

            g.drawLine(
                    cx - 10,
                    cy + 6,
                    cx + 7,
                    cy + 6
            );

            g.drawLine(
                    cx - 2,
                    cy - 5,
                    cx + 3,
                    cy - 11
            );

            g.drawLine(
                    cx + 1,
                    cy - 11,
                    cx + 7,
                    cy - 11
            );
        }

        private void drawMotorcycle(
                Graphics2D g,
                int cx,
                int cy
        ) {

            g.drawOval(
                    cx - 16,
                    cy + 2,
                    10,
                    10
            );

            g.drawOval(
                    cx + 7,
                    cy + 2,
                    10,
                    10
            );

            g.drawLine(
                    cx - 11,
                    cy + 7,
                    cx - 1,
                    cy - 2
            );

            g.drawLine(
                    cx - 1,
                    cy - 2,
                    cx + 10,
                    cy + 7
            );

            g.drawLine(
                    cx - 1,
                    cy - 2,
                    cx + 7,
                    cy - 2
            );

            g.drawLine(
                    cx + 7,
                    cy - 2,
                    cx + 11,
                    cy - 8
            );

            g.drawLine(
                    cx + 8,
                    cy - 8,
                    cx + 14,
                    cy - 8
            );
        }
    }
}