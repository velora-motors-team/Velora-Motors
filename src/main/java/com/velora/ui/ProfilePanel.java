package com.velora.ui;

import com.velora.authentication.Customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import com.velora.service.AuthenticationService;
public final class ProfilePanel extends JPanel {

    private final Runnable openRentalsAction;
private final AuthenticationService authenticationService = new AuthenticationService();
    private static final Color CARD = new Color(6, 13, 20);
    private static final Color GOLD = new Color(214, 160, 66);
    private static final Color PALE = new Color(238, 201, 139);
    private static final Color TEXT = new Color(243, 244, 247);
    private static final Color MUTED = new Color(157, 164, 175);
    private static final Color GREEN = new Color(86, 207, 114);

    private final Customer customer;

    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;

    public ProfilePanel(Customer customer) {
    this(customer, null);
}

public ProfilePanel(Customer customer, Runnable openRentalsAction) {
    this.customer = customer;
    this.openRentalsAction = openRentalsAction;

    setOpaque(false);
    setLayout(new BorderLayout(0, 18));
    setBorder(new EmptyBorder(18, 22, 18, 22));

    add(createHeader(), BorderLayout.NORTH);
    add(createBody(), BorderLayout.CENTER);
}
    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 8, 0));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = label("My Profile", 30, Font.BOLD, TEXT);
        JLabel sub = label("Manage your personal information, account details, and security settings.",
                13, Font.PLAIN, MUTED);

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

        body.add(createTopStats(), BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 16, 0));
        center.setOpaque(false);

        center.add(createProfileCard());
        center.add(createRightCards());

        body.add(center, BorderLayout.CENTER);

        return body;
    }

    private JComponent createTopStats() {
        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);

        stats.add(statCard("12", "Total Rentals", "Completed rental history"));
        stats.add(statCard("2", "Active Rentals", "Currently ongoing"));
        stats.add(statCard("2,450", "Loyalty Points", "Rewards balance"));
        stats.add(statCard("$24,560", "Total Spent", "Lifetime spending"));

        return stats;
    }

    private JComponent statCard(String value, String title, String desc) {
        RoundedPanel card = new RoundedPanel(18, CARD);
        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        ProfileIcon icon = new ProfileIcon("STAT");
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

    private JComponent createProfileCard() {
        RoundedPanel card = new RoundedPanel(18, CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(22, 22, 22, 22));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel top = new JPanel(new BorderLayout(16, 0));
        top.setOpaque(false);
        top.setAlignmentX(Component.LEFT_ALIGNMENT);

        AvatarIcon avatar = new AvatarIcon();
        avatar.setPreferredSize(new Dimension(88, 88));

        JPanel nameBox = new JPanel();
        nameBox.setOpaque(false);
        nameBox.setLayout(new BoxLayout(nameBox, BoxLayout.Y_AXIS));

        nameBox.add(label(customerName(), 24, Font.BOLD, TEXT));
        nameBox.add(Box.createVerticalStrut(5));
        nameBox.add(label(customerEmail(), 13, Font.PLAIN, MUTED));
        nameBox.add(Box.createVerticalStrut(8));
        nameBox.add(label("● Verified Account", 12, Font.BOLD, GREEN));

        top.add(avatar, BorderLayout.WEST);
        top.add(nameBox, BorderLayout.CENTER);

        content.add(top);
        content.add(Box.createVerticalStrut(26));

        JLabel section = label("Personal Information", 19, Font.BOLD, TEXT);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(section);
        content.add(Box.createVerticalStrut(15));

        nameField = new DarkTextField(customerName());
        emailField = new DarkTextField(customerEmail());
        phoneField = new DarkTextField(customerPhone());

        content.add(fieldTitle("Full Name"));
        content.add(nameField);
        content.add(Box.createVerticalStrut(12));

        content.add(fieldTitle("Email Address"));
        content.add(emailField);
        content.add(Box.createVerticalStrut(12));

        content.add(fieldTitle("Phone Number"));
        content.add(phoneField);
        content.add(Box.createVerticalStrut(22));

        JButton save = new GoldButton("Save Profile Changes");
        save.setPreferredSize(new Dimension(250, 46));
        save.addActionListener(e -> saveProfile());

        JPanel holder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        holder.setOpaque(false);
        holder.setAlignmentX(Component.LEFT_ALIGNMENT);
        holder.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        holder.add(save);

        content.add(holder);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JComponent createRightCards() {
        JPanel right = new JPanel(new GridLayout(2, 1, 0, 16));
        right.setOpaque(false);

        right.add(createSecurityCard());
        right.add(createPreferencesCard());

        return right;
    }

    private JComponent createSecurityCard() {
        RoundedPanel card = new RoundedPanel(18, CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(22, 22, 22, 22));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = label("Security Settings", 20, Font.BOLD, TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(title);
        content.add(Box.createVerticalStrut(14));
        content.add(infoRow("Password", "Last changed recently"));
        content.add(infoRow("Login Protection", "Session expires after inactivity"));
        content.add(infoRow("Account Status", "Verified and active"));

        content.add(Box.createVerticalStrut(18));

        JButton change = new GoldOutlineButton("Change Password");
        change.setPreferredSize(new Dimension(190, 42));
        change.addActionListener(e -> changePassword());

        JPanel holder = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        holder.setOpaque(false);
        holder.add(change);
        content.add(holder);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JComponent createPreferencesCard() {
        RoundedPanel card = new RoundedPanel(18, CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(22, 22, 22, 22));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = label("Rental Preferences", 20, Font.BOLD, TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(title);
        content.add(Box.createVerticalStrut(14));
        content.add(infoRow("Preferred Vehicle", "Luxury SUV / Electric Vehicle"));
        content.add(infoRow("Payment Method", "Credit Card"));
        content.add(infoRow("Notifications", "Email and system alerts enabled"));

        content.add(Box.createVerticalStrut(18));

        JButton view = new GoldOutlineButton("View Rentals");
        view.setPreferredSize(new Dimension(160, 42));
       view.addActionListener(e -> openRentals());

        JPanel holder = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        holder.setOpaque(false);
        holder.add(view);
        content.add(holder);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JComponent infoRow(String title, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 0, 13, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JLabel left = label(title, 13, Font.BOLD, PALE);
        JLabel right = label(value, 12, Font.PLAIN, MUTED);
        right.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);

        return row;
    }

    private JLabel fieldTitle(String text) {
        JLabel label = label(text, 12, Font.BOLD, PALE);
        label.setBorder(new EmptyBorder(0, 2, 6, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void saveProfile() {
        JOptionPane.showMessageDialog(
                this,
                "Profile changes saved for this session.\n\nLater we can connect it to accounts.txt.",
                "Velora Profile",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    
    private void changePassword() {
    JPasswordField newPassword = new JPasswordField();
    JPasswordField confirmPassword = new JPasswordField();

    newPassword.setEchoChar('•');
    confirmPassword.setEchoChar('•');

    JPanel form = new JPanel(new GridLayout(2, 2, 10, 10));
    form.add(new JLabel("New Password"));
    form.add(newPassword);
    form.add(new JLabel("Confirm Password"));
    form.add(confirmPassword);

    int result = JOptionPane.showConfirmDialog(
            this,
            form,
            "Change Password",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
    );

    if (result != JOptionPane.OK_OPTION) {
        return;
    }

    String pass = new String(newPassword.getPassword());
    String confirm = new String(confirmPassword.getPassword());

    if (pass.isBlank() || confirm.isBlank()) {
        JOptionPane.showMessageDialog(this, "Please fill both password fields.");
        return;
    }

    if (!pass.equals(confirm)) {
        JOptionPane.showMessageDialog(this, "Passwords do not match.");
        return;
    }

    if (pass.length() < 8) {
        JOptionPane.showMessageDialog(this, "Password must be at least 8 characters.");
        return;
    }

    boolean updated = authenticationService.resetPassword(customerEmail(), pass.toCharArray());

    if (updated) {
        JOptionPane.showMessageDialog(
                this,
                "Password changed successfully.",
                "Velora Profile",
                JOptionPane.INFORMATION_MESSAGE
        );
    } else {
        JOptionPane.showMessageDialog(
                this,
                "Could not find this account in accounts.txt.",
                "Velora Profile",
                JOptionPane.ERROR_MESSAGE
        );
    }
}

private void openRentals() {
    if (openRentalsAction != null) {
        openRentalsAction.run();
        return;
    }

    JOptionPane.showMessageDialog(
            this,
            "My Rentals page is not connected yet.",
            "Velora Profile",
            JOptionPane.INFORMATION_MESSAGE
    );
}
    private String customerName() {
        return customer == null || customer.getFullName() == null || customer.getFullName().isBlank()
                ? "Velora Customer"
                : customer.getFullName();
    }

    private String customerEmail() {
        return customer == null || customer.getEmail() == null || customer.getEmail().isBlank()
                ? "customer@velora.com"
                : customer.getEmail();
    }

    private String customerPhone() {
        return customer == null || customer.getPhone() == null || customer.getPhone().isBlank()
                ? "No phone number"
                : customer.getPhone();
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

    private static final class DarkTextField extends JTextField {

        DarkTextField(String value) {
            super(value);
            setOpaque(false);
            setForeground(TEXT);
            setCaretColor(PALE);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setBorder(new EmptyBorder(0, 14, 0, 14));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setAlignmentX(Component.LEFT_ALIGNMENT);
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

    private static final class GoldOutlineButton extends JButton {

        GoldOutlineButton(String text) {
            super(text);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(PALE);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (getModel().isRollover()) {
                g.setColor(new Color(214, 160, 66, 28));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 11, 11);
            }

            g.setColor(new Color(214, 160, 66, 130));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 11, 11);

            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class AvatarIcon extends JComponent {

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = Math.min(getWidth(), getHeight()) - 4;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            g.setColor(new Color(14, 25, 35));
            g.fillOval(x, y, size, size);

            g.setColor(PALE);
            g.setStroke(new BasicStroke(2f));
            g.drawOval(x, y, size, size);

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            g.setColor(new Color(223, 179, 143));
            g.fillOval(cx - 13, cy - 25, 26, 30);

            g.setColor(new Color(36, 27, 23));
            g.fillArc(cx - 15, cy - 29, 30, 22, 0, 180);

            g.setColor(new Color(235, 238, 242));
            g.fillArc(cx - 27, cy + 4, 54, 36, 0, 180);

            g.setColor(GOLD);
            g.fillPolygon(
                    new int[]{cx, cx - 4, cx, cx + 4},
                    new int[]{cy + 8, cy + 18, cy + 30, cy + 18},
                    4
            );

            g.dispose();
        }
    }

    private static final class ProfileIcon extends JComponent {

        private final String type;

        ProfileIcon(String type) {
            this.type = type;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            g.setColor(new Color(214, 160, 66, 20));
            g.fillOval(cx - 24, cy - 24, 48, 48);

            g.setColor(GOLD);
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            g.drawRoundRect(cx - 15, cy - 10, 30, 20, 6, 6);
            g.drawLine(cx - 10, cy - 10, cx - 4, cy - 18);
            g.drawLine(cx - 4, cy - 18, cx + 8, cy - 18);
            g.drawLine(cx + 8, cy - 18, cx + 14, cy - 10);
            g.fillOval(cx - 9, cy + 8, 5, 5);
            g.fillOval(cx + 5, cy + 8, 5, 5);

            g.dispose();
        }
    }
}