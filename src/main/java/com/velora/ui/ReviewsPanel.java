package com.velora.ui;

import com.velora.authentication.Customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class ReviewsPanel extends JPanel {

    private static final Color CARD = new Color(6, 13, 20);
    private static final Color CARD_SOFT = new Color(8, 17, 25);
    private static final Color GOLD = new Color(214, 160, 66);
    private static final Color PALE = new Color(238, 201, 139);
    private static final Color TEXT = new Color(243, 244, 247);
    private static final Color MUTED = new Color(157, 164, 175);
    private static final Color GREEN = new Color(86, 207, 114);

    private final Customer customer;

    private JComboBox<String> vehicleBox;
    private JComboBox<String> ratingBox;
    private JTextArea reviewArea;
    private JPanel reviewsList;
    private JLabel reviewCountLabel;

    public ReviewsPanel(Customer customer) {
        this.customer = customer;

        setOpaque(false);
        setLayout(new BorderLayout(0, 18));
        setBorder(new EmptyBorder(16, 22, 18, 22));

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
    }

    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = label("Customer Reviews", 27, Font.BOLD, TEXT);
        JLabel sub = label(
                "Share your rental experience and help us improve Velora Motors service.",
                12,
                Font.PLAIN,
                MUTED
        );

        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(sub);

        RoundedPanel statusBadge = new RoundedPanel(16, new Color(10, 28, 20));
        statusBadge.setLayout(new BorderLayout());
        statusBadge.setBorder(new EmptyBorder(8, 13, 8, 13));
        statusBadge.setPreferredSize(new Dimension(175, 36));

        JLabel status = label("●  Review System Active", 11, Font.BOLD, GREEN);
        status.setHorizontalAlignment(SwingConstants.CENTER);
        statusBadge.add(status, BorderLayout.CENTER);

        header.add(left, BorderLayout.WEST);
        header.add(statusBadge, BorderLayout.EAST);

        return header;
    }

    private JComponent createBody() {
        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setOpaque(false);

        body.add(createStatsRow(), BorderLayout.NORTH);

        JPanel contentStack = new JPanel();
        contentStack.setOpaque(false);
        contentStack.setLayout(new BoxLayout(contentStack, BoxLayout.Y_AXIS));

        JPanel mainRow = new JPanel(new GridLayout(1, 2, 20, 0));
        mainRow.setOpaque(false);
        mainRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 440));
        mainRow.setPreferredSize(new Dimension(1000, 440));

        mainRow.add(createAddReviewCard());
        mainRow.add(createReviewsCard());

        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 20, 0));
        bottomRow.setOpaque(false);
        bottomRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottomRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 175));
        bottomRow.setPreferredSize(new Dimension(1000, 175));

        bottomRow.add(createActivityCard());
        bottomRow.add(createTipsCard());

        contentStack.add(mainRow);
        contentStack.add(Box.createVerticalStrut(20));
        contentStack.add(bottomRow);
        contentStack.add(Box.createVerticalGlue());

        body.add(contentStack, BorderLayout.CENTER);

        return body;
    }

    private JComponent createStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(1000, 100));

        row.add(statCard("4.9", "Average Rating", "Excellent service score"));
        row.add(statCard("128", "Total Reviews", "Customer feedback"));
        row.add(statCard("96%", "Satisfaction", "Positive experiences"));
        row.add(statCard("24h", "Response Time", "Support follow-up"));

        return row;
    }

    private JComponent statCard(String value, String title, String desc) {
        RoundedPanel card = new RoundedPanel(16, CARD);
        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        ReviewIcon icon = new ReviewIcon();
        icon.setPreferredSize(new Dimension(48, 48));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        text.add(label(title, 13, Font.BOLD, TEXT));
        text.add(Box.createVerticalStrut(4));
        text.add(label(value, 24, Font.BOLD, PALE));
        text.add(Box.createVerticalStrut(3));
        text.add(label(desc, 11, Font.PLAIN, MUTED));

        card.add(icon, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);

        return card;
    }

    private JComponent createAddReviewCard() {
        RoundedPanel card = new RoundedPanel(18, CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 22, 20, 22));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = label("Write a Review", 22, Font.BOLD, TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = label("Tell us how your rental experience went.", 12, Font.PLAIN, MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(title);
        content.add(Box.createVerticalStrut(4));
        content.add(sub);
        content.add(Box.createVerticalStrut(18));

        vehicleBox = new DarkComboBox<>(new String[]{
                "BMW X7 xDrive40i",
                "BMW M8 Competition",
                "BMW i7 M70",
                "Toyota Prius Hybrid",
                "Xiaomi Electric Bike Pro",
                "Yamaha MT-07"
        });
        styleCombo(vehicleBox);

        ratingBox = new DarkComboBox<>(new String[]{
                "5 Stars - Excellent",
                "4 Stars - Very Good",
                "3 Stars - Good",
                "2 Stars - Fair",
                "1 Star - Poor"
        });
        styleCombo(ratingBox);

        reviewArea = new DarkTextArea("Write your review here...");

        JScrollPane reviewScroll = new JScrollPane(reviewArea);
        reviewScroll.setBorder(BorderFactory.createLineBorder(new Color(214, 160, 66, 90)));
        reviewScroll.setOpaque(false);
        reviewScroll.getViewport().setOpaque(false);
        reviewScroll.setPreferredSize(new Dimension(100, 128));
        reviewScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 128));
        reviewScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(fieldTitle("Vehicle"));
        content.add(vehicleBox);
        content.add(Box.createVerticalStrut(12));

        content.add(fieldTitle("Rating"));
        content.add(ratingBox);
        content.add(Box.createVerticalStrut(12));

        content.add(fieldTitle("Review Message"));
        content.add(reviewScroll);
        content.add(Box.createVerticalStrut(18));

        JButton submit = new GoldButton("Submit Review");
        submit.setPreferredSize(new Dimension(205, 40));
        submit.addActionListener(e -> submitReview());

        JPanel holder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        holder.setOpaque(false);
        holder.setAlignmentX(Component.LEFT_ALIGNMENT);
        holder.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        holder.add(submit);

        content.add(holder);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JComponent createReviewsCard() {
        RoundedPanel card = new RoundedPanel(18, CARD);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(20, 22, 20, 22));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = label("Recent Reviews", 22, Font.BOLD, TEXT);

        reviewCountLabel = label("3 reviews", 11, Font.BOLD, PALE);
        reviewCountLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        top.add(title, BorderLayout.WEST);
        top.add(reviewCountLabel, BorderLayout.EAST);

        reviewsList = new JPanel();
        reviewsList.setOpaque(false);
        reviewsList.setLayout(new BoxLayout(reviewsList, BoxLayout.Y_AXIS));

        reviewsList.add(reviewRow(
                "Omar Al-Khatib",
                "BMW X7 xDrive40i",
                "★★★★★",
                "Amazing luxury experience. The car was clean and powerful."
        ));
        reviewsList.add(Box.createVerticalStrut(12));
        reviewsList.add(reviewRow(
                "Sara Johnson",
                "BMW M8 Competition",
                "★★★★★",
                "Fast pickup, premium service, and very professional team."
        ));
        reviewsList.add(Box.createVerticalStrut(12));
        reviewsList.add(reviewRow(
                "Ahmad Ali",
                "Xiaomi Electric Bike Pro",
                "★★★★☆",
                "Battery was good and the ride was smooth. Nice experience."
        ));

        JScrollPane scroll = new JScrollPane(reviewsList);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        card.add(top, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JComponent createActivityCard() {
        RoundedPanel card = new RoundedPanel(18, CARD_SOFT);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        JLabel title = label("Your Review Activity", 20, Font.BOLD, TEXT);
        JLabel sub = label("Your contribution to the Velora community", 12, Font.PLAIN, MUTED);

        JPanel titleText = new JPanel();
        titleText.setOpaque(false);
        titleText.setLayout(new BoxLayout(titleText, BoxLayout.Y_AXIS));
        titleText.add(title);
        titleText.add(Box.createVerticalStrut(3));
        titleText.add(sub);

        titlePanel.add(titleText, BorderLayout.WEST);

        JPanel metrics = new JPanel(new GridLayout(1, 3, 18, 0));
        metrics.setOpaque(false);

        metrics.add(activityMetric("📝", "3", "Reviews Shared"));
        metrics.add(activityMetric("⭐", "4.7", "Average Given"));
        metrics.add(activityMetric("👍", "2", "Helpful Votes"));

        card.add(titlePanel, BorderLayout.NORTH);
        card.add(metrics, BorderLayout.CENTER);

        return card;
    }

    private JComponent activityMetric(String emoji, String value, String text) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel emojiLabel = new JLabel(emoji);
        emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        emojiLabel.setForeground(PALE);
        emojiLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valueLabel = label(value, 26, Font.BOLD, PALE);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel textLabel = label(text, 12, Font.PLAIN, MUTED);
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(emojiLabel);
        panel.add(Box.createVerticalStrut(3));
        panel.add(valueLabel);
        panel.add(Box.createVerticalStrut(2));
        panel.add(textLabel);

        return panel;
    }

    private JComponent createTipsCard() {
        RoundedPanel card = new RoundedPanel(18, CARD_SOFT);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));

        JLabel title = label("Review Tips", 20, Font.BOLD, TEXT);
        JLabel sub = label("Make your feedback useful and trustworthy", 12, Font.PLAIN, MUTED);

        head.add(title);
        head.add(Box.createVerticalStrut(3));
        head.add(sub);

        JPanel tips = new JPanel();
        tips.setOpaque(false);
        tips.setLayout(new BoxLayout(tips, BoxLayout.Y_AXIS));

        tips.add(tipLine("Mention vehicle condition, cleanliness, and pickup experience."));
        tips.add(Box.createVerticalStrut(6));
        tips.add(tipLine("Keep your feedback honest, clear, and specific."));
        tips.add(Box.createVerticalStrut(6));
        tips.add(tipLine("Avoid sharing phone numbers or private information."));

        card.add(head, BorderLayout.NORTH);
        card.add(tips, BorderLayout.CENTER);

        return card;
    }

    private JComponent tipLine(String text) {
        JPanel line = new JPanel(new BorderLayout(8, 0));
        line.setOpaque(false);

        JLabel dot = label("◆", 9, Font.BOLD, GOLD);
        JLabel txt = label(text, 12, Font.PLAIN, MUTED);

        line.add(dot, BorderLayout.WEST);
        line.add(txt, BorderLayout.CENTER);

        return line;
    }

    private JComponent reviewRow(String name, String vehicle, String stars, String message) {
        RoundedPanel row = new RoundedPanel(14, new Color(4, 10, 16));
        row.setLayout(new BorderLayout(10, 0));
        row.setBorder(new EmptyBorder(11, 12, 11, 12));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));
        row.setPreferredSize(new Dimension(100, 88));

        ReviewAvatar avatar = new ReviewAvatar();
        avatar.setPreferredSize(new Dimension(50, 50));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel top = label(name + "  •  " + vehicle, 13, Font.BOLD, TEXT);

        StarRating rating = new StarRating(stars);
        rating.setPreferredSize(new Dimension(102, 18));
        rating.setMaximumSize(new Dimension(102, 18));
        rating.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel msg = label(
                "<html><div style='width:330px'>" + message + "</div></html>",
                11,
                Font.PLAIN,
                MUTED
        );

        text.add(top);
        text.add(Box.createVerticalStrut(2));
        text.add(rating);
        text.add(Box.createVerticalStrut(2));
        text.add(msg);

        row.add(avatar, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);

        return row;
    }

    private void submitReview() {
        String message = reviewArea.getText().trim();

        if (message.isBlank() || message.equals("Write your review here...")) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please write your review message.",
                    "Velora Reviews",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String customerName = customer == null ? "Customer" : customer.getFullName();
        String vehicle = String.valueOf(vehicleBox.getSelectedItem());
        String rating = String.valueOf(ratingBox.getSelectedItem());

        String stars = rating.startsWith("5") ? "★★★★★"
                : rating.startsWith("4") ? "★★★★☆"
                : rating.startsWith("3") ? "★★★☆☆"
                : rating.startsWith("2") ? "★★☆☆☆"
                : "★☆☆☆☆";

        reviewsList.add(Box.createVerticalStrut(9), 0);
        reviewsList.add(reviewRow(customerName, vehicle, stars, message), 0);

        reviewCountLabel.setText((countReviewRows()) + " reviews");

        reviewsList.revalidate();
        reviewsList.repaint();

        JOptionPane.showMessageDialog(
                this,
                "Review submitted successfully.\n\nDate: "
                        + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                "Velora Reviews",
                JOptionPane.INFORMATION_MESSAGE
        );

        reviewArea.setText("");
    }

    private int countReviewRows() {
        int count = 0;
        for (Component component : reviewsList.getComponents()) {
            if (component instanceof RoundedPanel) {
                count++;
            }
        }
        return count;
    }

    private JLabel fieldTitle(String text) {
        JLabel label = label(text, 12, Font.BOLD, PALE);
        label.setBorder(new EmptyBorder(0, 2, 5, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setOpaque(false);
        combo.setBackground(new Color(3, 9, 15));
        combo.setForeground(TEXT);
        combo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        combo.setFocusable(false);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        combo.setPreferredSize(new Dimension(100, 42));
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
        combo.setBorder(BorderFactory.createEmptyBorder());

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                JLabel item = (JLabel) super.getListCellRendererComponent(
                        list,
                        value,
                        index,
                        isSelected,
                        cellHasFocus
                );

                item.setFont(new Font("Segoe UI", Font.BOLD, 13));
                item.setBorder(new EmptyBorder(8, 12, 8, 12));

                if (isSelected) {
                    item.setBackground(new Color(93, 58, 23));
                    item.setForeground(PALE);
                } else {
                    item.setBackground(new Color(3, 9, 15));
                    item.setForeground(TEXT);
                }

                return item;
            }
        });

        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton("⌄");
                button.setForeground(PALE);
                button.setBackground(new Color(3, 9, 15));
                button.setBorder(BorderFactory.createEmptyBorder());
                button.setFocusPainted(false);
                button.setOpaque(false);
                button.setContentAreaFilled(false);
                return button;
            }
        });
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

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            RoundRectangle2D shape = new RoundRectangle2D.Double(
                    0.5,
                    0.5,
                    Math.max(0, getWidth() - 1),
                    Math.max(0, getHeight() - 1),
                    radius,
                    radius
            );

            g.setColor(new Color(0, 0, 0, 55));
            g.fillRoundRect(
                    4,
                    5,
                    Math.max(0, getWidth() - 8),
                    Math.max(0, getHeight() - 8),
                    radius,
                    radius
            );

            g.setPaint(new GradientPaint(
                    0,
                    0,
                    new Color(15, 24, 31),
                    getWidth(),
                    getHeight(),
                    fill
            ));
            g.fill(shape);

            g.setPaint(new GradientPaint(
                    0,
                    0,
                    new Color(214, 160, 66, 18),
                    getWidth(),
                    0,
                    new Color(214, 160, 66, 2)
            ));
            g.fill(shape);

            g.setColor(new Color(214, 160, 66, 72));
            g.draw(shape);

            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class DarkTextArea extends JTextArea {

        private final String placeholder;

        DarkTextArea(String placeholder) {
            this.placeholder = placeholder;

            setText(placeholder);
            setOpaque(false);
            setForeground(MUTED);
            setCaretColor(PALE);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setLineWrap(true);
            setWrapStyleWord(true);
            setBorder(new EmptyBorder(10, 12, 10, 12));

            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    if (getText().equals(placeholder)) {
                        setText("");
                        setForeground(TEXT);
                    }
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    if (getText().isBlank()) {
                        setText(placeholder);
                        setForeground(MUTED);
                    }
                }
            });
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
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g.setPaint(new GradientPaint(
                    0,
                    0,
                    getModel().isRollover()
                            ? new Color(250, 219, 158)
                            : PALE,
                    getWidth(),
                    getHeight(),
                    new Color(164, 103, 33)
            ));

            g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class ReviewIcon extends JComponent {

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            g.setColor(new Color(214, 160, 66, 18));
            g.fillOval(cx - 19, cy - 19, 38, 38);

            g.setColor(GOLD);
            g.setStroke(new BasicStroke(
                    1.8f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));

            Polygon star = new Polygon();

            for (int i = 0; i < 10; i++) {
                double angle = -Math.PI / 2 + i * Math.PI / 5;
                double r = i % 2 == 0 ? 14 : 6;

                star.addPoint(
                        (int) (cx + Math.cos(angle) * r),
                        (int) (cy + Math.sin(angle) * r)
                );
            }

            g.drawPolygon(star);
            g.dispose();
        }
    }

    private static final class ReviewAvatar extends JComponent {

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int size = Math.min(getWidth(), getHeight()) - 4;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;
            int cx = getWidth() / 2;

            g.setColor(new Color(14, 25, 35));
            g.fillOval(x, y, size, size);

            g.setColor(PALE);
            g.setStroke(new BasicStroke(1.5f));
            g.drawOval(x, y, size, size);

            g.setColor(new Color(223, 179, 143));
            g.fillOval(cx - 7, y + 8, 14, 16);

            g.setColor(new Color(235, 238, 242));
            g.fillArc(cx - 15, y + 25, 30, 20, 0, 180);

            g.dispose();
        }
    }

    private static final class DarkComboBox<E> extends JComboBox<E> {

        DarkComboBox(E[] items) {
            super(items);

            setOpaque(false);
            setBackground(new Color(3, 9, 15));
            setForeground(TEXT);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g.setColor(new Color(3, 9, 15));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

            g.setColor(new Color(214, 160, 66, 105));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class StarRating extends JComponent {

        private final int rating;

        StarRating(String stars) {
            int count = 0;

            if (stars != null) {
                for (int i = 0; i < stars.length(); i++) {
                    if (stars.charAt(i) == '★') {
                        count++;
                    }
                }
            }

            this.rating = Math.max(1, Math.min(5, count));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int size = 13;
            int gap = 5;
            int y = 2;

            for (int i = 0; i < 5; i++) {
                int x = i * (size + gap) + 2;

                Shape star = createStar(
                        x + size / 2.0,
                        y + size / 2.0,
                        size / 2.0,
                        size / 4.0
                );

                if (i < rating) {
                    g.setColor(GOLD);
                    g.fill(star);
                } else {
                    g.setColor(new Color(214, 160, 66, 55));
                    g.draw(star);
                }
            }

            g.dispose();
        }

        private Shape createStar(double cx, double cy, double outer, double inner) {
            Path2D path = new Path2D.Double();

            for (int i = 0; i < 10; i++) {
                double angle = -Math.PI / 2 + i * Math.PI / 5;
                double radius = i % 2 == 0 ? outer : inner;

                double x = cx + Math.cos(angle) * radius;
                double y = cy + Math.sin(angle) * radius;

                if (i == 0) {
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                }
            }

            path.closePath();
            return path;
        }
    }
}
