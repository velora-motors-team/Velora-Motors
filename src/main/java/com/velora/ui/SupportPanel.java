package com.velora.ui;

import com.velora.authentication.Customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.plaf.basic.BasicComboBoxUI;

public final class SupportPanel extends JPanel {

    private static final Color CARD = new Color(6, 13, 20);
    private static final Color GOLD = new Color(214, 160, 66);
    private static final Color PALE = new Color(238, 201, 139);
    private static final Color TEXT = new Color(243, 244, 247);
    private static final Color MUTED = new Color(157, 164, 175);
    private static final Color GREEN = new Color(86, 207, 114);

    private final Customer customer;

    private JTextField subjectField;
    private JComboBox<String> categoryBox;
    private JTextArea messageArea;
    private JTextArea chatArea;
    private JTextField chatInput;

    public SupportPanel(Customer customer) {
        this.customer = customer;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(18, 22, 18, 22));

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
    }

    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = label("Support Center", 30, Font.BOLD, TEXT);
        JLabel sub = label(
                "We are here to help you with rentals, payments, returns, and vehicle issues.",
                13,
                Font.PLAIN,
                MUTED
        );

        left.add(title);
        left.add(Box.createVerticalStrut(5));
        left.add(sub);

        JLabel status = label("● Online Support", 13, Font.BOLD, GREEN);
        status.setHorizontalAlignment(SwingConstants.RIGHT);

        header.add(left, BorderLayout.WEST);
        header.add(status, BorderLayout.EAST);

        return header;
    }

    private JComponent createBody() {
        JPanel body = new JPanel(new BorderLayout(14, 14));
        body.setOpaque(false);

        JPanel topCards = new JPanel(new GridLayout(1, 3, 14, 0));
        topCards.setOpaque(false);

        topCards.add(infoCard("Call Support", "+970 59 123 4567", "Available daily from 9 AM to 10 PM", "CALL"));
        topCards.add(infoCard("Email Support", "support@velora.com", "We usually reply within 24 hours", "MAIL"));
        topCards.add(infoCard("Emergency Help", "Roadside Assistance", "For urgent vehicle problems", "WARN"));

        body.add(topCards, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 14, 0));
        center.setOpaque(false);

        center.add(createTicketCard());
        center.add(createRightSide());

        body.add(center, BorderLayout.CENTER);

        return body;
    }

    private JComponent infoCard(String title, String value, String desc, String icon) {
        RoundedPanel card = new RoundedPanel(18, CARD);
        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        SupportIcon iconLabel = new SupportIcon(icon);
        iconLabel.setPreferredSize(new Dimension(58, 58));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        text.add(label(title, 13, Font.BOLD, TEXT));
        text.add(Box.createVerticalStrut(6));
        text.add(label(value, 19, Font.BOLD, PALE));
        text.add(Box.createVerticalStrut(6));
        text.add(label(desc, 11, Font.PLAIN, MUTED));

        card.add(iconLabel, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);

        return card;
    }

    private JComponent createTicketCard() {
        RoundedPanel card = new RoundedPanel(18, CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = label("Open Support Ticket", 20, Font.BOLD, TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = label("Send us the issue details and our support team will contact you.", 12, Font.PLAIN, MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(title);
        content.add(Box.createVerticalStrut(6));
        content.add(sub);
        content.add(Box.createVerticalStrut(18));

        subjectField = new DarkTextField("Ticket subject");
        subjectField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        subjectField.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(fieldTitle("Subject"));
        content.add(subjectField);
        content.add(Box.createVerticalStrut(12));

        categoryBox = new DarkComboBox<>(new String[]{
                "Problem with rental",
                "Payment issue",
                "Car pickup problem",
                "Late return question",
                "Account problem",
                "Other"
        });
        styleCombo(categoryBox);
        categoryBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        categoryBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(fieldTitle("Category"));
        content.add(categoryBox);
        content.add(Box.createVerticalStrut(12));

        messageArea = new DarkTextArea("Describe your issue here...");
        JScrollPane messageScroll = new JScrollPane(messageArea);
        messageScroll.setBorder(BorderFactory.createLineBorder(new Color(214, 160, 66, 80)));
        messageScroll.setOpaque(false);
        messageScroll.getViewport().setOpaque(false);
        messageScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        messageScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(fieldTitle("Message"));
        content.add(messageScroll);
        content.add(Box.createVerticalStrut(18));

        JButton send = new GoldButton("Submit Ticket");
        send.setPreferredSize(new Dimension(240, 46));
        send.addActionListener(e -> submitTicket());

        JPanel sendHolder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        sendHolder.setOpaque(false);
        sendHolder.setAlignmentX(Component.LEFT_ALIGNMENT);
        sendHolder.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        sendHolder.add(send);

        content.add(sendHolder);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JComponent createRightSide() {
        JPanel right = new JPanel(new GridLayout(2, 1, 0, 14));
        right.setOpaque(false);

        right.add(createFaqCard());
        right.add(createChatCard());

        return right;
    }

    private JComponent createFaqCard() {
        RoundedPanel card = new RoundedPanel(18, CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        JLabel title = label("Frequently Asked Questions", 20, Font.BOLD, TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        list.add(title);
        list.add(Box.createVerticalStrut(14));

        list.add(faqRow("How can I extend my rental?", "Go to My Rentals and request an extension before return time."));
        list.add(faqRow("What happens if I return late?", "A late fee may be added based on delay duration."));
        list.add(faqRow("Can I cancel a reservation?", "Yes, before the pickup time based on the rental policy."));
        list.add(faqRow("How do I pay my invoice?", "Open Billing & Invoices and choose Pay or Download Invoice."));

        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JComponent faqRow(String q, String a) {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setBorder(new EmptyBorder(0, 0, 12, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel question = label(q, 13, Font.BOLD, PALE);
        question.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel answer = label(a, 11, Font.PLAIN, MUTED);
        answer.setAlignmentX(Component.LEFT_ALIGNMENT);

        row.add(question);
        row.add(Box.createVerticalStrut(3));
        row.add(answer);

        return row;
    }

    private JComponent createChatCard() {
        RoundedPanel card = new RoundedPanel(18, CARD);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel title = label("Live Chat", 20, Font.BOLD, TEXT);
        card.add(title, BorderLayout.NORTH);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setOpaque(false);
        chatArea.setForeground(TEXT);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chatArea.setText("Velora Support: Hello " + firstName() + ", how can we help you today?\n");

        JScrollPane scroll = new JScrollPane(chatArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(214, 160, 66, 70)));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        card.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(10, 0));
        bottom.setOpaque(false);

        chatInput = new DarkTextField("Write a quick message...");
        JButton send = new GoldButton("Send");
        send.setPreferredSize(new Dimension(90, 40));
        send.addActionListener(e -> sendChatMessage());

        bottom.add(chatInput, BorderLayout.CENTER);
        bottom.add(send, BorderLayout.EAST);

        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    private JLabel fieldTitle(String text) {
        JLabel label = label(text, 12, Font.BOLD, PALE);
        label.setBorder(new EmptyBorder(0, 2, 6, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void submitTicket() {
        String subject = subjectField.getText().trim();
        String message = messageArea.getText().trim();

        if (subject.isBlank() || subject.equals("Ticket subject")) {
            showMessage("Please enter a ticket subject.");
            return;
        }

        if (message.isBlank() || message.equals("Describe your issue here...")) {
            showMessage("Please describe your issue.");
            return;
        }

        String ticketId = "SUP-" + DateTimeFormatter.ofPattern("HHmmss").format(LocalDateTime.now());

        JOptionPane.showMessageDialog(
                this,
                "Ticket created successfully.\n\nTicket ID: " + ticketId
                + "\nCategory: " + categoryBox.getSelectedItem()
                + "\nCustomer: " + customer.getFullName(),
                "Velora Support",
                JOptionPane.INFORMATION_MESSAGE
        );

        subjectField.setText("");
        messageArea.setText("");
    }

    private void sendChatMessage() {
        String msg = chatInput.getText().trim();

        if (msg.isBlank() || msg.equals("Write a quick message...")) {
            return;
        }

        chatArea.append(firstName() + ": " + msg + "\n");
        chatArea.append("Velora Support: Thanks, your message has been received.\n");
        chatInput.setText("");
    }

    private void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Velora Support", JOptionPane.WARNING_MESSAGE);
    }

    private String firstName() {
        String name = customer == null ? "Customer" : customer.getFullName();
        if (name == null || name.isBlank()) {
            return "Customer";
        }
        return name.split(" ")[0];
    }

    private JLabel label(String text, int size, int style, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", style, size));
        label.setForeground(color);
        return label;
    }

    private void styleCombo(JComboBox<String> combo) {
    combo.setForeground(TEXT);
    combo.setFont(new Font("Segoe UI", Font.BOLD, 13));
    combo.setFocusable(false);
    combo.setOpaque(false);
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

    private static final class SupportIcon extends JComponent {

        private final String type;

        SupportIcon(String type) {
            this.type = type;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            g.setColor(new Color(214, 160, 66, 24));
            g.fillOval(cx - 25, cy - 25, 50, 50);

            g.setColor(GOLD);

            switch (type) {
                case "CALL" -> {
                    g.drawArc(cx - 17, cy - 17, 34, 34, 135, 110);
                    g.drawLine(cx - 14, cy - 4, cx - 7, cy + 5);
                    g.drawLine(cx + 7, cy + 5, cx + 14, cy - 4);
                    g.drawRoundRect(cx - 18, cy + 4, 8, 13, 4, 4);
                    g.drawRoundRect(cx + 10, cy + 4, 8, 13, 4, 4);
                }
                case "MAIL" -> {
                    g.drawRoundRect(cx - 18, cy - 12, 36, 24, 5, 5);
                    g.drawLine(cx - 18, cy - 10, cx, cy + 3);
                    g.drawLine(cx + 18, cy - 10, cx, cy + 3);
                }
                default -> {
                    Path2D triangle = new Path2D.Double();
                    triangle.moveTo(cx, cy - 20);
                    triangle.lineTo(cx + 20, cy + 16);
                    triangle.lineTo(cx - 20, cy + 16);
                    triangle.closePath();
                    g.draw(triangle);
                    g.drawLine(cx, cy - 8, cx, cy + 5);
                    g.fillOval(cx - 2, cy + 10, 4, 4);
                }
            }

            g.dispose();
        }
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

    private static final class DarkTextField extends JTextField {

        private final String placeholder;

        DarkTextField(String placeholder) {
            this.placeholder = placeholder;
            setText(placeholder);
            setOpaque(false);
            setForeground(MUTED);
            setCaretColor(PALE);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setBorder(new EmptyBorder(0, 14, 0, 14));

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

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(new Color(3, 9, 15));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

            g.setColor(new Color(214, 160, 66, isFocusOwner() ? 135 : 70));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

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
}