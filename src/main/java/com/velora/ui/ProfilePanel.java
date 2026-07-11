package com.velora.ui;

import com.velora.authentication.Customer;
import com.velora.service.AuthenticationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Premium Customer Profile Panel for Velora Motors.
 */
public final class ProfilePanel extends JPanel {

    private static final Color CARD_TOP = new Color(13, 23, 31);
    private static final Color CARD_BOTTOM = new Color(6, 13, 20);
    private static final Color INPUT_BG = new Color(3, 10, 16);

    private static final Color GOLD = new Color(214, 160, 66);
    private static final Color GOLD_LIGHT = new Color(241, 204, 139);
    private static final Color GOLD_DARK = new Color(174, 109, 33);

    private static final Color TEXT = new Color(247, 248, 250);
    private static final Color SECONDARY_TEXT = new Color(194, 200, 208);
    private static final Color MUTED = new Color(145, 155, 168);

    private static final Color GREEN = new Color(73, 218, 112);
    private static final Color BLUE = new Color(94, 173, 255);

    private static final String FONT_NAME = "Segoe UI";

    private static final Font PAGE_TITLE_FONT = new Font(FONT_NAME, Font.BOLD, 31);
    private static final Font PAGE_SUBTITLE_FONT = new Font(FONT_NAME, Font.PLAIN, 13);
    private static final Font PROFILE_NAME_FONT = new Font(FONT_NAME, Font.BOLD, 22);
    private static final Font SECTION_TITLE_FONT = new Font(FONT_NAME, Font.BOLD, 20);
    private static final Font CARD_TITLE_FONT = new Font(FONT_NAME, Font.BOLD, 19);
    private static final Font SMALL_CARD_TITLE_FONT = new Font(FONT_NAME, Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font(FONT_NAME, Font.BOLD, 13);
    private static final Font BODY_FONT = new Font(FONT_NAME, Font.PLAIN, 13);
    private static final Font SMALL_FONT = new Font(FONT_NAME, Font.PLAIN, 11);
    private static final Font SMALL_BOLD_FONT = new Font(FONT_NAME, Font.BOLD, 11);
    private static final Font STAT_VALUE_FONT = new Font(FONT_NAME, Font.BOLD, 23);

    private final Customer customer;
    private final Runnable openRentalsAction;
    private final AuthenticationService authenticationService = new AuthenticationService();

    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JLabel totalRentalsValue;
    private JLabel activeRentalsValue;
    private JLabel loyaltyPointsValue;
    private JLabel totalSpentValue;

    public ProfilePanel(Customer customer) {
        this(customer, null);
    }

    public ProfilePanel(Customer customer, Runnable openRentalsAction) {
        this.customer = customer;
        this.openRentalsAction = openRentalsAction;

        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(16, 22, 20, 22));

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
    }

    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel left = createVerticalPanel();

        JLabel title = new JLabel("My Profile");
        title.setFont(PAGE_TITLE_FONT);
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel(
                "Manage your personal information, account details, and security settings."
        );
        subtitle.setFont(PAGE_SUBTITLE_FONT);
        subtitle.setForeground(MUTED);

        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(subtitle);

        JLabel status = new JLabel("●  Active Customer");
        status.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        status.setForeground(GREEN);

        JPanel statusHolder = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 7));
        statusHolder.setOpaque(false);
        statusHolder.add(status);

        header.add(left, BorderLayout.WEST);
        header.add(statusHolder, BorderLayout.EAST);

        return header;
    }

    private JComponent createBody() {
        JPanel body = createVerticalPanel();

        JComponent stats = createTopStats();
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(stats);

        body.add(Box.createVerticalStrut(16));

        JComponent main = createMainCards();
        main.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(main);

        body.add(Box.createVerticalStrut(16));

        JComponent bottom = createBottomCards();
        bottom.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(bottom);

        body.add(Box.createVerticalGlue());
        return body;
    }

    private JComponent createTopStats() {
        JPanel stats = new JPanel(new GridLayout(1, 4, 15, 0));
        stats.setOpaque(false);
        stats.setPreferredSize(new Dimension(0, 84));
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));

        stats.add(createStatCard("12", "Total Rentals", "Completed rental history"));
        stats.add(createStatCard("2", "Active Rentals", "Currently ongoing"));
        stats.add(createStatCard("2,450", "Loyalty Points", "Rewards balance"));
        stats.add(createStatCard("$24,560", "Total Spent", "Lifetime spending"));

        return stats;
    }

    private JComponent createStatCard(String value, String title, String description) {
        LuxuryCard card = new LuxuryCard(18);
        card.setLayout(new BorderLayout(13, 0));
        card.setBorder(new EmptyBorder(11, 15, 11, 15));

        CarIcon icon = new CarIcon();
        icon.setPreferredSize(new Dimension(48, 48));

        JPanel text = createVerticalPanel();

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        titleLabel.setForeground(TEXT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(STAT_VALUE_FONT);
        valueLabel.setForeground(GOLD_LIGHT);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(SMALL_FONT);
        descriptionLabel.setForeground(MUTED);

        text.add(titleLabel);
        text.add(Box.createVerticalStrut(1));
        text.add(valueLabel);
        text.add(Box.createVerticalStrut(1));
        text.add(descriptionLabel);

        card.add(icon, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);

        return card;
    }

    private JComponent createMainCards() {
        JPanel main = new JPanel(new GridBagLayout());
        main.setOpaque(false);
        main.setPreferredSize(new Dimension(0, 400));
        main.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.fill = GridBagConstraints.BOTH;
        gc.anchor = GridBagConstraints.NORTH;
        gc.weighty = 1.0;

        gc.gridx = 0;
        gc.weightx = 1.02;
        gc.insets = new Insets(0, 0, 0, 15);
        main.add(createProfileCard(), gc);

        gc.gridx = 1;
        gc.weightx = 1.0;
        gc.insets = new Insets(0, 0, 0, 0);
        main.add(createRightCards(), gc);

        return main;
    }

    private JComponent createProfileCard() {
        LuxuryCard card = new LuxuryCard(18);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(17, 19, 16, 19));

        JPanel content = createVerticalPanel();

        JPanel identity = new JPanel(new BorderLayout(15, 0));
        identity.setOpaque(false);
        identity.setAlignmentX(Component.LEFT_ALIGNMENT);
        identity.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        AvatarIcon avatar = new AvatarIcon();
        avatar.setPreferredSize(new Dimension(64, 64));

        JPanel identityText = createVerticalPanel();

        JLabel name = new JLabel(customerName());
        name.setFont(PROFILE_NAME_FONT);
        name.setForeground(TEXT);

        JLabel email = new JLabel(customerEmail());
        email.setFont(BODY_FONT);
        email.setForeground(SECONDARY_TEXT);

        JLabel verified = new JLabel("●  Verified Account");
        verified.setFont(SMALL_BOLD_FONT);
        verified.setForeground(GREEN);

        identityText.add(Box.createVerticalGlue());
        identityText.add(name);
        identityText.add(Box.createVerticalStrut(2));
        identityText.add(email);
        identityText.add(Box.createVerticalStrut(5));
        identityText.add(verified);
        identityText.add(Box.createVerticalGlue());

        identity.add(avatar, BorderLayout.WEST);
        identity.add(identityText, BorderLayout.CENTER);

        content.add(identity);
        content.add(Box.createVerticalStrut(10));
        content.add(createSectionDivider("Personal Information"));
        content.add(Box.createVerticalStrut(11));

        nameField = new DarkTextField(customerName());
        emailField = new DarkTextField(customerEmail());
        phoneField = new DarkTextField(customerPhone());
        emailField.setEditable(false);
        emailField.setToolTipText("Email is the account link for billing, reviews, and admin records.");

        content.add(createFieldGroup("Full Name", nameField));
        content.add(Box.createVerticalStrut(8));
        content.add(createFieldGroup("Email Address", emailField));
        content.add(Box.createVerticalStrut(8));
        content.add(createFieldGroup("Phone Number", phoneField));
        content.add(Box.createVerticalStrut(14));

        JButton save = new GoldButton("Save Profile Changes");
        save.setPreferredSize(new Dimension(205, 40));
        save.addActionListener(e -> saveProfile());
        content.add(createLeftButtonHolder(save));

        card.add(content, BorderLayout.NORTH);
        return card;
    }

    private JComponent createRightCards() {
        JPanel right = new JPanel(new GridLayout(2, 1, 0, 15));
        right.setOpaque(false);
        right.add(createSecurityCard());
        right.add(createPreferencesCard());
        return right;
    }

    private JComponent createSecurityCard() {
        LuxuryCard card = new LuxuryCard(18);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 15, 18));

        JPanel content = createVerticalPanel();
        content.add(createLeftCardTitle("Security Settings", "Protect your Velora account"));
        content.add(Box.createVerticalStrut(12));
        content.add(createInfoRow("Password", "Last changed recently"));
        content.add(Box.createVerticalStrut(4));
        content.add(createInfoRow("Login Protection", "Session expires after inactivity"));
        content.add(Box.createVerticalStrut(4));
        content.add(createInfoRow("Account Status", "Verified and active"));
        content.add(Box.createVerticalStrut(14));

        JButton change = new GoldOutlineButton("Change Password");
        change.setPreferredSize(new Dimension(172, 38));
        change.addActionListener(e -> changePassword());
        content.add(createLeftButtonHolder(change));

        card.add(content, BorderLayout.NORTH);
        return card;
    }

    private JComponent createPreferencesCard() {
        LuxuryCard card = new LuxuryCard(18);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 15, 18));

        JPanel content = createVerticalPanel();
        content.add(createLeftCardTitle("Rental Preferences", "Your personalized rental choices"));
        content.add(Box.createVerticalStrut(12));
        content.add(createInfoRow("Preferred Vehicle", "Luxury SUV / Electric Vehicle"));
        content.add(Box.createVerticalStrut(4));
        content.add(createInfoRow("Payment Method", "Credit Card"));
        content.add(Box.createVerticalStrut(4));
        content.add(createInfoRow("Notifications", "Email and system alerts enabled"));
        content.add(Box.createVerticalStrut(14));

        JButton view = new GoldOutlineButton("View Rentals");
        view.setPreferredSize(new Dimension(148, 38));
        view.addActionListener(e -> openRentals());
        content.add(createLeftButtonHolder(view));

        card.add(content, BorderLayout.NORTH);
        return card;
    }

    private JComponent createLeftCardTitle(String titleText, String subtitleText) {
        JPanel box = createVerticalPanel();
        box.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel(titleText);
        title.setFont(CARD_TITLE_FONT);
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel(subtitleText);
        subtitle.setFont(SMALL_FONT);
        subtitle.setForeground(MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        box.add(title);
        box.add(Box.createVerticalStrut(3));
        box.add(subtitle);
        return box;
    }

    private JComponent createInfoRow(String titleText, String valueText) {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JLabel left = new JLabel(titleText);
        left.setFont(LABEL_FONT);
        left.setForeground(GOLD_LIGHT);

        JLabel right = new JLabel(valueText);
        right.setFont(BODY_FONT);
        right.setForeground(SECONDARY_TEXT);
        right.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private JComponent createSectionDivider(String titleText) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel title = new JLabel(titleText);
        title.setFont(SECTION_TITLE_FONT);
        title.setForeground(TEXT);

        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(214, 160, 66, 50));
        separator.setBackground(new Color(214, 160, 66, 50));

        row.add(title, BorderLayout.WEST);
        row.add(separator, BorderLayout.CENTER);
        return row;
    }

    private JComponent createFieldGroup(String title, JTextField field) {
        JPanel group = createVerticalPanel();
        group.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        JLabel label = new JLabel(title);
        label.setFont(LABEL_FONT);
        label.setForeground(GOLD_LIGHT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setPreferredSize(new Dimension(0, 36));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        group.add(label);
        group.add(Box.createVerticalStrut(5));
        group.add(field);
        return group;
    }

    private JComponent createBottomCards() {
        JPanel cards = new JPanel(new GridLayout(1, 3, 15, 0));
        cards.setOpaque(false);
        cards.setPreferredSize(new Dimension(0, 180));
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        cards.add(createRecentRentalsCard());
        cards.add(createActivityCard());
        cards.add(createMembershipCard());

        return cards;
    }

    private JComponent createRecentRentalsCard() {
        LuxuryCard card = createBottomCard();
        JPanel content = createVerticalPanel();

        content.add(createBottomCardHeader(
                "Recent Rentals",
                "Your latest vehicle bookings",
                new RentalHistoryIcon()
        ));

        content.add(Box.createVerticalStrut(13));
        content.add(createRentalItem(
                "BMW X5 M Competition",
                "Premium BMW Rental",
                "Completed",
                GREEN
        ));
        content.add(Box.createVerticalStrut(9));
        content.add(createRentalItem(
                "BMW i7 xDrive60",
                "Premium BMW Rental",
                "Active Rental",
                GOLD_LIGHT
        ));

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JComponent createRentalItem(
            String vehicle,
            String subtitle,
            String status,
            Color statusColor
    ) {
        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setOpaque(false);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JPanel left = createVerticalPanel();

        JLabel vehicleLabel = new JLabel(vehicle);
        vehicleLabel.setFont(LABEL_FONT);
        vehicleLabel.setForeground(TEXT);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(SMALL_FONT);
        subtitleLabel.setForeground(MUTED);

        left.add(vehicleLabel);
        left.add(Box.createVerticalStrut(2));
        left.add(subtitleLabel);

        JLabel statusLabel = new JLabel("●  " + status);
        statusLabel.setFont(SMALL_BOLD_FONT);
        statusLabel.setForeground(statusColor);

        item.add(left, BorderLayout.WEST);
        item.add(statusLabel, BorderLayout.EAST);
        return item;
    }

    private JComponent createActivityCard() {
        LuxuryCard card = createBottomCard();
        JPanel content = createVerticalPanel();

        content.add(createBottomCardHeader(
                "Account Activity",
                "Latest security and profile activity",
                new ActivityIcon()
        ));

        content.add(Box.createVerticalStrut(13));
        content.add(createActivityItem("Profile information updated", "Recently", GREEN));
        content.add(Box.createVerticalStrut(8));
        content.add(createActivityItem("Password security checked", "Protected", GOLD_LIGHT));
        content.add(Box.createVerticalStrut(8));
        content.add(createActivityItem("Account verification active", "Verified", BLUE));

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JComponent createActivityItem(String text, String state, Color color) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 27));

        JLabel left = new JLabel("•  " + text);
        left.setFont(BODY_FONT);
        left.setForeground(SECONDARY_TEXT);

        JLabel right = new JLabel(state);
        right.setFont(SMALL_BOLD_FONT);
        right.setForeground(color);

        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private JComponent createMembershipCard() {
        LuxuryCard card = createBottomCard();
        JPanel content = createVerticalPanel();

        content.add(createBottomCardHeader(
                "Membership Status",
                "Your Velora rewards membership",
                new CrownIcon()
        ));

        content.add(Box.createVerticalStrut(11));

        JPanel membershipContent = createVerticalPanel();
        membershipContent.setBorder(new EmptyBorder(0, 55, 0, 0));
        membershipContent.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel member = new JLabel("GOLD MEMBER");
        member.setFont(new Font(FONT_NAME, Font.BOLD, 18));
        member.setForeground(GOLD_LIGHT);

        JLabel points = new JLabel("2,450 loyalty points");
        points.setFont(BODY_FONT);
        points.setForeground(TEXT);

        membershipContent.add(member);
        membershipContent.add(Box.createVerticalStrut(4));
        membershipContent.add(points);
        membershipContent.add(Box.createVerticalStrut(10));

        MembershipProgressBar progress = new MembershipProgressBar(2450, 5000);
        progress.setPreferredSize(new Dimension(0, 8));
        progress.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        progress.setAlignmentX(Component.LEFT_ALIGNMENT);
        membershipContent.add(progress);
        membershipContent.add(Box.createVerticalStrut(7));

        JLabel progressText = new JLabel("2,550 more points to Platinum");
        progressText.setFont(SMALL_FONT);
        progressText.setForeground(MUTED);
        membershipContent.add(progressText);

        content.add(membershipContent);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private LuxuryCard createBottomCard() {
        LuxuryCard card = new LuxuryCard(18);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(15, 17, 15, 17));
        return card;
    }

    private JComponent createBottomCardHeader(
            String titleText,
            String subtitleText,
            JComponent icon
    ) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        icon.setPreferredSize(new Dimension(42, 42));

        JPanel text = createVerticalPanel();

        JLabel title = new JLabel(titleText);
        title.setFont(SMALL_CARD_TITLE_FONT);
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel(subtitleText);
        subtitle.setFont(SMALL_FONT);
        subtitle.setForeground(MUTED);

        text.add(title);
        text.add(Box.createVerticalStrut(2));
        text.add(subtitle);

        row.add(icon, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        return row;
    }

    private JPanel createVerticalPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private JPanel createLeftButtonHolder(JButton button) {
        JPanel holder = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        holder.setOpaque(false);
        holder.setAlignmentX(Component.LEFT_ALIGNMENT);
        holder.add(button);
        return holder;
    }

    private void saveProfile() {
        String fullName = nameField == null ? "" : nameField.getText().trim();
        String phone = phoneField == null ? "" : phoneField.getText().trim();

        if (fullName.isBlank()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Full name is required.",
                    "Velora Profile",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            boolean updated = authenticationService.updateProfile(customerEmail(), fullName, phone);
            if (updated) {
                JOptionPane.showMessageDialog(
                        this,
                        "Profile changes saved successfully.\nAdmin customer records will show the updated name and phone.",
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
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Velora Profile",
                    JOptionPane.ERROR_MESSAGE
            );
        }
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

        String password = new String(newPassword.getPassword());
        String confirmation = new String(confirmPassword.getPassword());

        if (password.isBlank() || confirmation.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please fill both password fields.");
            return;
        }

        if (!password.equals(confirmation)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.");
            return;
        }

        if (password.length() < 8) {
            JOptionPane.showMessageDialog(this, "Password must be at least 8 characters.");
            return;
        }

        boolean updated = authenticationService.resetPassword(
                customerEmail(),
                password.toCharArray()
        );

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
        return customer == null
                || customer.getFullName() == null
                || customer.getFullName().isBlank()
                ? "Velora Customer"
                : customer.getFullName();
    }

    private String customerEmail() {
        return customer == null
                || customer.getEmail() == null
                || customer.getEmail().isBlank()
                ? "customer@velora.com"
                : customer.getEmail();
    }

    private String customerPhone() {
        return customer == null
                || customer.getPhone() == null
                || customer.getPhone().isBlank()
                ? "No phone number"
                : customer.getPhone();
    }

    private static final class LuxuryCard extends JPanel {
        private final int radius;

        LuxuryCard(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = createGraphics(raw);

            int width = getWidth();
            int height = getHeight();

            RoundRectangle2D shape = new RoundRectangle2D.Double(
                    0.5,
                    0.5,
                    width - 1,
                    height - 1,
                    radius,
                    radius
            );

            g.setPaint(new GradientPaint(
                    0,
                    0,
                    CARD_TOP,
                    width,
                    height,
                    CARD_BOTTOM
            ));
            g.fill(shape);

            g.setPaint(new GradientPaint(
                    0,
                    0,
                    new Color(214, 160, 66, 20),
                    width * 0.45f,
                    0,
                    new Color(214, 160, 66, 0)
            ));
            g.fill(shape);

            g.setColor(new Color(214, 160, 66, 74));
            g.draw(shape);

            g.setPaint(new GradientPaint(
                    18,
                    0,
                    new Color(241, 204, 139, 78),
                    width * 0.30f,
                    0,
                    new Color(241, 204, 139, 0)
            ));
            g.drawLine(
                    18,
                    1,
                    Math.max(18, (int) (width * 0.30)),
                    1
            );

            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class DarkTextField extends JTextField {
        DarkTextField(String value) {
            super(value);
            setOpaque(false);
            setForeground(TEXT);
            setCaretColor(GOLD_LIGHT);
            setFont(new Font(FONT_NAME, Font.PLAIN, 13));
            setBorder(new EmptyBorder(0, 12, 0, 12));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = createGraphics(raw);

            g.setColor(INPUT_BG);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 9, 9);

            g.setColor(new Color(
                    214,
                    160,
                    66,
                    isFocusOwner() ? 165 : 65
            ));

            g.drawRoundRect(
                    0,
                    0,
                    getWidth() - 1,
                    getHeight() - 1,
                    9,
                    9
            );

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
            setForeground(new Color(29, 18, 6));
            setFont(new Font(FONT_NAME, Font.BOLD, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = createGraphics(raw);

            Color start = getModel().isRollover()
                    ? new Color(252, 223, 169)
                    : GOLD_LIGHT;

            Color end = getModel().isPressed()
                    ? GOLD_DARK
                    : new Color(181, 113, 34);

            g.setPaint(new GradientPaint(
                    0,
                    0,
                    start,
                    getWidth(),
                    getHeight(),
                    end
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

    private static final class GoldOutlineButton extends JButton {
        GoldOutlineButton(String text) {
            super(text);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(GOLD_LIGHT);
            setFont(new Font(FONT_NAME, Font.BOLD, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = createGraphics(raw);

            if (getModel().isRollover()) {
                g.setColor(new Color(214, 160, 66, 30));
                g.fillRoundRect(
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        10,
                        10
                );
            }

            g.setColor(new Color(214, 160, 66, 145));
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

    private static final class AvatarIcon extends JComponent {
        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = createGraphics(raw);

            int size = Math.min(getWidth(), getHeight()) - 4;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            g.setColor(new Color(13, 25, 35));
            g.fillOval(x, y, size, size);

            g.setColor(GOLD_LIGHT);
            g.setStroke(new BasicStroke(2f));
            g.drawOval(x, y, size, size);

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            g.setColor(new Color(223, 179, 143));
            g.fillOval(cx - 10, cy - 20, 20, 23);

            g.setColor(new Color(36, 27, 23));
            g.fillArc(cx - 12, cy - 23, 24, 18, 0, 180);

            g.setColor(new Color(235, 238, 242));
            g.fillArc(cx - 21, cy + 3, 42, 28, 0, 180);

            g.setColor(GOLD);
            g.fillPolygon(
                    new int[]{cx, cx - 3, cx, cx + 3},
                    new int[]{cy + 6, cy + 14, cy + 24, cy + 14},
                    4
            );

            g.dispose();
        }
    }

    private static final class CarIcon extends JComponent {
        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = createGraphics(raw);

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            drawGlow(g, cx, cy, 20);

            g.setColor(GOLD);
            g.setStroke(new BasicStroke(
                    1.8f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));

            g.drawRoundRect(cx - 12, cy - 8, 24, 16, 5, 5);
            g.drawLine(cx - 8, cy - 8, cx - 3, cy - 14);
            g.drawLine(cx - 3, cy - 14, cx + 6, cy - 14);
            g.drawLine(cx + 6, cy - 14, cx + 11, cy - 8);
            g.fillOval(cx - 7, cy + 6, 4, 4);
            g.fillOval(cx + 4, cy + 6, 4, 4);

            g.dispose();
        }
    }

    private static final class RentalHistoryIcon extends JComponent {
        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = createGraphics(raw);
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            drawGlow(g, cx, cy, 19);
            g.setColor(GOLD);
            g.setStroke(new BasicStroke(
                    1.8f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));

            g.drawRoundRect(cx - 10, cy - 9, 20, 18, 4, 4);
            g.drawLine(cx - 6, cy - 13, cx - 6, cy - 5);
            g.drawLine(cx + 6, cy - 13, cx + 6, cy - 5);
            g.drawLine(cx - 10, cy - 3, cx + 10, cy - 3);

            g.dispose();
        }
    }

    private static final class ActivityIcon extends JComponent {
        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = createGraphics(raw);
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            drawGlow(g, cx, cy, 19);
            g.setColor(GOLD);
            g.setStroke(new BasicStroke(
                    1.8f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));

            g.drawOval(cx - 11, cy - 11, 22, 22);
            g.drawLine(cx, cy, cx, cy - 7);
            g.drawLine(cx, cy, cx + 6, cy + 3);

            g.dispose();
        }
    }

    private static final class CrownIcon extends JComponent {
        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = createGraphics(raw);
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            drawGlow(g, cx, cy, 19);
            g.setColor(GOLD);
            g.setStroke(new BasicStroke(
                    1.8f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));

            Polygon crown = new Polygon();
            crown.addPoint(cx - 12, cy - 5);
            crown.addPoint(cx - 6, cy + 2);
            crown.addPoint(cx, cy - 7);
            crown.addPoint(cx + 6, cy + 2);
            crown.addPoint(cx + 12, cy - 5);
            crown.addPoint(cx + 9, cy + 9);
            crown.addPoint(cx - 9, cy + 9);

            g.drawPolygon(crown);
            g.drawLine(cx - 9, cy + 5, cx + 9, cy + 5);

            g.dispose();
        }
    }

    private static final class MembershipProgressBar extends JComponent {
        private final int value;
        private final int maximum;

        MembershipProgressBar(int value, int maximum) {
            this.value = value;
            this.maximum = maximum;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = createGraphics(raw);

            int width = getWidth();
            int height = getHeight();

            g.setColor(new Color(255, 255, 255, 18));
            g.fillRoundRect(0, 0, width, height, height, height);

            double ratio = Math.min(1.0, (double) value / maximum);
            int progressWidth = (int) (width * ratio);

            g.setPaint(new GradientPaint(
                    0,
                    0,
                    GOLD_LIGHT,
                    progressWidth,
                    0,
                    GOLD_DARK
            ));

            g.fillRoundRect(
                    0,
                    0,
                    progressWidth,
                    height,
                    height,
                    height
            );

            g.dispose();
        }
    }

    private static Graphics2D createGraphics(Graphics raw) {
        Graphics2D g = (Graphics2D) raw.create();

        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        return g;
    }

    private static void drawGlow(
            Graphics2D g,
            int cx,
            int cy,
            int radius
    ) {
        g.setColor(new Color(214, 160, 66, 20));
        g.fillOval(
                cx - radius,
                cy - radius,
                radius * 2,
                radius * 2
        );
    }
}
