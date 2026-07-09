package com.velora.ui;

import com.velora.authentication.Customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.time.format.DateTimeFormatter;
import java.awt.geom.Path2D;
public final class ReviewsPanel extends JPanel {

    private static final Color CARD = new Color(6, 13, 20);
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

    public ReviewsPanel(Customer customer) {
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

        JLabel title = label("Customer Reviews", 30, Font.BOLD, TEXT);
        JLabel sub = label(
                "Share your rental experience and help us improve Velora Motors service.",
                13,
                Font.PLAIN,
                MUTED
        );

        left.add(title);
        left.add(Box.createVerticalStrut(5));
        left.add(sub);

        JLabel status = label("● Review System Active", 13, Font.BOLD, GREEN);
        status.setHorizontalAlignment(SwingConstants.RIGHT);

        header.add(left, BorderLayout.WEST);
        header.add(status, BorderLayout.EAST);

        return header;
    }

    private JComponent createBody() {
        JPanel body = new JPanel(new BorderLayout(16, 16));
        body.setOpaque(false);

        body.add(createStatsRow(), BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 16, 0));
        center.setOpaque(false);

        center.add(createAddReviewCard());
        center.add(createReviewsCard());

        body.add(center, BorderLayout.CENTER);

        return body;
    }

    private JComponent createStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);

        row.add(statCard("4.9", "Average Rating", "Excellent service score"));
        row.add(statCard("128", "Total Reviews", "Customer feedback"));
        row.add(statCard("96%", "Satisfaction", "Positive experiences"));
        row.add(statCard("24h", "Response Time", "Support follow-up"));

        return row;
    }

    private JComponent statCard(String value, String title, String desc) {
        RoundedPanel card = new RoundedPanel(18, CARD);
        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        ReviewIcon icon = new ReviewIcon();
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

    private JComponent createAddReviewCard() {
        RoundedPanel card = new RoundedPanel(18, CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(22, 22, 22, 22));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = label("Write a Review", 22, Font.BOLD, TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = label("Tell us how your rental experience went.", 12, Font.PLAIN, MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(title);
        content.add(Box.createVerticalStrut(6));
        content.add(sub);
        content.add(Box.createVerticalStrut(20));

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
        reviewScroll.setBorder(BorderFactory.createLineBorder(new Color(214, 160, 66, 80)));
        reviewScroll.setOpaque(false);
        reviewScroll.getViewport().setOpaque(false);
        reviewScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        reviewScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(fieldTitle("Vehicle"));
        content.add(vehicleBox);
        content.add(Box.createVerticalStrut(12));

        content.add(fieldTitle("Rating"));
        content.add(ratingBox);
        content.add(Box.createVerticalStrut(12));

        content.add(fieldTitle("Review Message"));
        content.add(reviewScroll);
        content.add(Box.createVerticalStrut(20));

        JButton submit = new GoldButton("Submit Review");
        submit.setPreferredSize(new Dimension(240, 46));
        submit.addActionListener(e -> submitReview());

        JPanel holder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        holder.setOpaque(false);
        holder.setAlignmentX(Component.LEFT_ALIGNMENT);
        holder.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        holder.add(submit);

        content.add(holder);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JComponent createReviewsCard() {
        RoundedPanel card = new RoundedPanel(18, CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(22, 22, 22, 22));

        JLabel title = label("Recent Reviews", 22, Font.BOLD, TEXT);
        title.setBorder(new EmptyBorder(0, 0, 16, 0));

        reviewsList = new JPanel();
        reviewsList.setOpaque(false);
        reviewsList.setLayout(new BoxLayout(reviewsList, BoxLayout.Y_AXIS));

        reviewsList.add(reviewRow("Omar Al-Khatib", "BMW X7 xDrive40i", "★★★★★", "Amazing luxury experience. The car was clean and powerful."));
        reviewsList.add(Box.createVerticalStrut(12));
        reviewsList.add(reviewRow("Sara Johnson", "BMW M8 Competition", "★★★★★", "Fast pickup, premium service, and very professional team."));
        reviewsList.add(Box.createVerticalStrut(12));
        reviewsList.add(reviewRow("Ahmad Ali", "Xiaomi Electric Bike Pro", "★★★★☆", "Battery was good and the ride was smooth. Nice experience."));

        JScrollPane scroll = new JScrollPane(reviewsList);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        card.add(title, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JComponent reviewRow(String name, String vehicle, String stars, String message) {
    RoundedPanel row = new RoundedPanel(14, new Color(4, 10, 16));
    row.setLayout(new BorderLayout(12, 0));
    row.setBorder(new EmptyBorder(14, 14, 14, 14));
    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

    ReviewAvatar avatar = new ReviewAvatar();
    avatar.setPreferredSize(new Dimension(58, 58));

    JPanel text = new JPanel();
    text.setOpaque(false);
    text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

    JLabel top = label(name + "  •  " + vehicle, 13, Font.BOLD, TEXT);

    StarRating rating = new StarRating(stars);
    rating.setPreferredSize(new Dimension(115, 22));
    rating.setMaximumSize(new Dimension(115, 22));
    rating.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel msg = label(
            "<html><div style='width:360px'>" + message + "</div></html>",
            12,
            Font.PLAIN,
            MUTED
    );

    text.add(top);
    text.add(Box.createVerticalStrut(4));
    text.add(rating);
    text.add(Box.createVerticalStrut(5));
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
        String stars = rating.substring(0, 1).equals("5") ? "★★★★★"
                : rating.substring(0, 1).equals("4") ? "★★★★☆"
                : rating.substring(0, 1).equals("3") ? "★★★☆☆"
                : rating.substring(0, 1).equals("2") ? "★★☆☆☆"
                : "★☆☆☆☆";

        reviewsList.add(Box.createVerticalStrut(12), 0);
        reviewsList.add(reviewRow(customerName, vehicle, stars, message), 0);
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

    private JLabel fieldTitle(String text) {
        JLabel label = label(text, 12, Font.BOLD, PALE);
        label.setBorder(new EmptyBorder(0, 2, 6, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void styleCombo(JComboBox<String> combo) {
    combo.setOpaque(false);
    combo.setBackground(new Color(3, 9, 15));
    combo.setForeground(TEXT);
    combo.setFont(new Font("Segoe UI", Font.BOLD, 13));
    combo.setFocusable(false);
    combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
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
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus
            );

            label.setFont(new Font("Segoe UI", Font.BOLD, 13));
            label.setBorder(new EmptyBorder(9, 14, 9, 14));

            if (isSelected) {
                label.setBackground(new Color(93, 58, 23));
                label.setForeground(PALE);
            } else {
                label.setBackground(new Color(3, 9, 15));
                label.setForeground(TEXT);
            }

            return label;
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
            setBorder(new EmptyBorder(12, 14, 12, 14));

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
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setPaint(new GradientPaint(
                    0, 0, getModel().isRollover() ? new Color(250, 219, 158) : PALE,
                    getWidth(), getHeight(), new Color(164, 103, 33)
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
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            g.setColor(new Color(214, 160, 66, 22));
            g.fillOval(cx - 24, cy - 24, 48, 48);

            g.setColor(GOLD);
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            Polygon star = new Polygon();
            for (int i = 0; i < 10; i++) {
                double angle = -Math.PI / 2 + i * Math.PI / 5;
                double r = i % 2 == 0 ? 18 : 8;
                star.addPoint((int) (cx + Math.cos(angle) * r), (int) (cy + Math.sin(angle) * r));
            }
            g.drawPolygon(star);

            g.dispose();
        }
    }

    private static final class ReviewAvatar extends JComponent {

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = Math.min(getWidth(), getHeight()) - 4;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;
            int cx = getWidth() / 2;

            g.setColor(new Color(14, 25, 35));
            g.fillOval(x, y, size, size);

            g.setColor(PALE);
            g.setStroke(new BasicStroke(1.7f));
            g.drawOval(x, y, size, size);

            g.setColor(new Color(223, 179, 143));
            g.fillOval(cx - 9, y + 10, 18, 20);

            g.setColor(new Color(235, 238, 242));
            g.fillArc(cx - 20, y + 31, 40, 26, 0, 180);

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
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(3, 9, 15));
        g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

        g.setColor(new Color(214, 160, 66, 120));
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
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = 15;
        int gap = 6;
        int y = 3;

        for (int i = 0; i < 5; i++) {
            int x = i * (size + gap) + 2;
            Shape star = createStar(x + size / 2.0, y + size / 2.0, size / 2.0, size / 4.0);

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