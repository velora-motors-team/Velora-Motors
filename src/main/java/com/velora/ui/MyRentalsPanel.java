package com.velora.ui;

import com.velora.authentication.Customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public final class MyRentalsPanel extends JPanel {

    private static final Color CARD = new Color(6, 13, 20);
    private static final Color GOLD = new Color(214, 160, 66);
    private static final Color PALE = new Color(238, 201, 139);
    private static final Color TEXT = new Color(243, 244, 247);
    private static final Color MUTED = new Color(157, 164, 175);
    private static final Color GREEN = new Color(86, 207, 114);
    private static final Color RED = new Color(235, 93, 98);

    private final Customer customer;

    public MyRentalsPanel(Customer customer) {
        this.customer = customer;
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(18, 22, 18, 22));

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
    }

    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = label("My Rentals", 30, Font.BOLD, TEXT);
        JLabel sub = label(
                "View your active rentals, return dates, rental history, and payment status.",
                13,
                Font.PLAIN,
                MUTED
        );

        left.add(title);
        left.add(Box.createVerticalStrut(5));
        left.add(sub);

        JLabel status = label("● Active Customer", 13, Font.BOLD, GREEN);
        status.setHorizontalAlignment(SwingConstants.RIGHT);

        header.add(left, BorderLayout.WEST);
        header.add(status, BorderLayout.EAST);

        return header;
    }

    private JComponent createBody() {
        JPanel body = new JPanel(new BorderLayout(16, 16));
        body.setOpaque(false);

        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);
        stats.add(statCard("2", "Active Rentals", "Currently rented"));
        stats.add(statCard("5", "Completed", "Rental history"));
        stats.add(statCard("$840", "Current Cost", "Active rental fees"));
        stats.add(statCard("0", "Late Returns", "No overdue rentals"));

        body.add(stats, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 16, 0));
        center.setOpaque(false);

        center.add(createActiveRentalCard());
        center.add(createHistoryCard());

        body.add(center, BorderLayout.CENTER);

        return body;
    }

    private JComponent statCard(String value, String title, String desc) {
        RoundedPanel card = new RoundedPanel(18, CARD);
        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        CircleIcon icon = new CircleIcon();
        icon.setPreferredSize(new Dimension(54, 54));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        text.add(label(title, 12, Font.BOLD, TEXT));
        text.add(Box.createVerticalStrut(5));
        text.add(label(value, 24, Font.BOLD, PALE));
        text.add(Box.createVerticalStrut(4));
        text.add(label(desc, 11, Font.PLAIN, MUTED));

        card.add(icon, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);

        return card;
    }

    private JComponent createActiveRentalCard() {
        RoundedPanel card = new RoundedPanel(18, CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(22, 22, 22, 22));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = label("Active Rentals", 22, Font.BOLD, TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(16));

        content.add(rentalRow("BMW X7 xDrive40i", "Return: 09 Jul 2026", "$475", "Active", GREEN));
        content.add(Box.createVerticalStrut(12));
        content.add(rentalRow("Xiaomi Electric Bike Pro", "Return: Today 08:00 PM", "$36", "Due Today", PALE));

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JComponent createHistoryCard() {
        RoundedPanel card = new RoundedPanel(18, CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(22, 22, 22, 22));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = label("Rental History", 22, Font.BOLD, TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(16));

        content.add(rentalRow("Toyota Prius Hybrid", "Completed: 01 Jul 2026", "$180", "Returned", GREEN));
        content.add(Box.createVerticalStrut(12));
        content.add(rentalRow("Yamaha MT-07", "Completed: 28 Jun 2026", "$126", "Returned", GREEN));
        content.add(Box.createVerticalStrut(12));
        content.add(rentalRow("Tesla Model 3", "Completed: 20 Jun 2026", "$320", "Returned Late", RED));

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JComponent rentalRow(String vehicle, String date, String price, String status, Color statusColor) {
        RoundedPanel row = new RoundedPanel(14, new Color(4, 10, 16));
        row.setLayout(new BorderLayout(12, 0));
        row.setBorder(new EmptyBorder(14, 14, 14, 14));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));

        CarIcon carIcon = new CarIcon();
        carIcon.setPreferredSize(new Dimension(58, 58));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel vehicleLabel = label(vehicle, 14, Font.BOLD, TEXT);
        JLabel dateLabel = label(date, 12, Font.PLAIN, MUTED);
        JLabel statusLabel = label(status, 12, Font.BOLD, statusColor);

        info.add(vehicleLabel);
        info.add(Box.createVerticalStrut(5));
        info.add(dateLabel);
        info.add(Box.createVerticalStrut(5));
        info.add(statusLabel);

        JLabel priceLabel = label(price, 18, Font.BOLD, PALE);
        priceLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(carIcon, BorderLayout.WEST);
        row.add(info, BorderLayout.CENTER);
        row.add(priceLabel, BorderLayout.EAST);

        return row;
    }

    private JLabel label(String text, int size, int style, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", style, size));
        label.setForeground(color);
        return label;
    }

    private static final class RoundedPanel extends JPanel {

        private final int radius;
        private final Color fill;

        RoundedPanel(int radius, Color fill) {
            this.radius = radius;
            this.fill = fill;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            RoundRectangle2D shape = new RoundRectangle2D.Double(
                    .5, .5, getWidth() - 1, getHeight() - 1, radius, radius
            );

            g.setColor(new Color(0, 0, 0, 70));
            g.fillRoundRect(5, 7, Math.max(0, getWidth() - 10), Math.max(0, getHeight() - 10), radius, radius);

            g.setPaint(new GradientPaint(
                    0, 0, new Color(12, 22, 31),
                    getWidth(), getHeight(), fill
            ));
            g.fill(shape);

            g.setPaint(new GradientPaint(
                    0, 0, new Color(214, 160, 66, 25),
                    getWidth(), 0, new Color(214, 160, 66, 3)
            ));
            g.fill(shape);

            g.setColor(new Color(214, 160, 66, 85));
            g.draw(shape);

            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class CircleIcon extends JComponent {
        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            g.setColor(new Color(214, 160, 66, 22));
            g.fillOval(cx - 24, cy - 24, 48, 48);
            g.setColor(GOLD);
            g.setStroke(new BasicStroke(2f));
            g.drawOval(cx - 17, cy - 17, 34, 34);
            g.drawLine(cx - 8, cy, cx + 8, cy);
            g.drawLine(cx, cy - 8, cx, cy + 8);

            g.dispose();
        }
    }

    private static final class CarIcon extends JComponent {
        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            g.setColor(new Color(214, 160, 66, 22));
            g.fillOval(cx - 25, cy - 25, 50, 50);

            g.setColor(GOLD);
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            g.drawRoundRect(cx - 20, cy - 5, 40, 15, 6, 6);
            g.drawLine(cx - 12, cy - 5, cx - 5, cy - 17);
            g.drawLine(cx - 5, cy - 17, cx + 9, cy - 17);
            g.drawLine(cx + 9, cy - 17, cx + 17, cy - 5);
            g.fillOval(cx - 14, cy + 8, 6, 6);
            g.fillOval(cx + 8, cy + 8, 6, 6);

            g.dispose();
        }
    }
}