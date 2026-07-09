package com.velora.ui;

import com.velora.authentication.Customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class LoyaltyPointsPanel extends JPanel {

    private static final Color CARD = new Color(6, 13, 20);
    private static final Color CARD_DARK = new Color(4, 10, 16);
    private static final Color GOLD = new Color(214, 160, 66);
    private static final Color PALE = new Color(238, 201, 139);
    private static final Color TEXT = new Color(243, 244, 247);
    private static final Color MUTED = new Color(157, 164, 175);
    private static final Color GREEN = new Color(86, 207, 114);
    private static final Color RED = new Color(235, 93, 98);
    private static final Color BLUE = new Color(91, 137, 210);

    private final Customer customer;
    private final CustomerAccountState accountState;

    private int points;

    private JLabel pointsLabel;
    private JLabel tierLabel;
    private JLabel nextTierLabel;
    private JLabel availablePointsValue;
    private JLabel currentLevelValue;
    private JLabel toNextTierValue;
    private JLabel rewardsUsedValue;
    private TierProgress progress;
    private JPanel historyList;

    public LoyaltyPointsPanel(Customer customer) {
        this.customer = customer;
        this.accountState = CustomerAccountState.forCustomer(customer);
        this.points = accountState.getLoyaltyPoints();

        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(18, 22, 18, 22));

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
        accountState.addChangeListener(this::refreshTier);

        refreshTier();
    }

    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = label("Loyalty Points", 30, Font.BOLD, TEXT);
        JLabel sub = label(
                "Earn points from every rental and redeem rewards for discounts and premium offers.",
                13,
                Font.PLAIN,
                MUTED
        );

        left.add(title);
        left.add(Box.createVerticalStrut(5));
        left.add(sub);

        JLabel status = label("● Rewards Active", 13, Font.BOLD, GREEN);
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

        center.add(createMainTierCard());
        center.add(createRewardsCard());

        body.add(center, BorderLayout.CENTER);
        body.add(createHistoryCard(), BorderLayout.SOUTH);

        return body;
    }

    private JComponent createStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);

        availablePointsValue = statValueLabel();
        currentLevelValue = statValueLabel();
        toNextTierValue = statValueLabel();
        rewardsUsedValue = statValueLabel();

        row.add(statCard(availablePointsValue, "Available Points", "Ready to redeem", "POINTS"));
        row.add(statCard(currentLevelValue, "Current Level", "Premium customer", "TIER"));
        row.add(statCard(toNextTierValue, "To Next Level", "Next upgrade", "NEXT"));
        row.add(statCard(rewardsUsedValue, "Rewards Used", "Total redeemed", "USED"));

        return row;
    }

    private JComponent statCard(JLabel value, String title, String desc, String iconType) {
        RoundedPanel card = new RoundedPanel(18, CARD);
        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        LoyaltyIcon icon = new LoyaltyIcon(iconType);
        icon.setPreferredSize(new Dimension(54, 54));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        text.add(label(title, 12, Font.BOLD, TEXT));
        text.add(Box.createVerticalStrut(5));
        text.add(value);
        text.add(Box.createVerticalStrut(4));
        text.add(label(desc, 11, Font.PLAIN, MUTED));

        card.add(icon, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);

        return card;
    }

    private JLabel statValueLabel() {
        return label("", 24, Font.BOLD, PALE);
    }

    private JComponent createMainTierCard() {
        RoundedPanel card = new RoundedPanel(18, CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel top = new JPanel(new BorderLayout(18, 0));
        top.setOpaque(false);
        top.setAlignmentX(Component.LEFT_ALIGNMENT);

        BigBadge badge = new BigBadge();
        badge.setPreferredSize(new Dimension(120, 120));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel welcome = label("Welcome, " + firstName(), 17, Font.BOLD, TEXT);
        pointsLabel = label(points + " Points", 40, Font.BOLD, PALE);
        tierLabel = label("Silver Member", 17, Font.BOLD, GREEN);
        nextTierLabel = label("650 points left to reach Gold level", 13, Font.PLAIN, MUTED);

        info.add(welcome);
        info.add(Box.createVerticalStrut(8));
        info.add(pointsLabel);
        info.add(Box.createVerticalStrut(5));
        info.add(tierLabel);
        info.add(Box.createVerticalStrut(8));
        info.add(nextTierLabel);

        top.add(badge, BorderLayout.WEST);
        top.add(info, BorderLayout.CENTER);

        content.add(top);
        content.add(Box.createVerticalStrut(28));

        JLabel progressTitle = label("Level Progress", 18, Font.BOLD, TEXT);
        progressTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(progressTitle);
        content.add(Box.createVerticalStrut(10));

        progress = new TierProgress();
        progress.setPreferredSize(new Dimension(300, 34));
        progress.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        progress.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(progress);

        content.add(Box.createVerticalStrut(12));

        JPanel levels = new JPanel(new GridLayout(1, 3));
        levels.setOpaque(false);
        levels.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        levels.setAlignmentX(Component.LEFT_ALIGNMENT);

        levels.add(levelLabel("Bronze", "0 pts", GOLD));
        levels.add(levelLabel("Silver", "500 pts", GREEN));
        levels.add(levelLabel("Gold", "1500 pts", PALE));

        content.add(levels);
        content.add(Box.createVerticalStrut(28));

        JLabel benefitsTitle = label("Your Benefits", 18, Font.BOLD, TEXT);
        benefitsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(benefitsTitle);
        content.add(Box.createVerticalStrut(12));

        content.add(benefitRow("Priority support", "Get faster help from Velora support team."));
        content.add(Box.createVerticalStrut(10));
        content.add(benefitRow("Rental discounts", "Redeem your points for exclusive rental discounts."));
        content.add(Box.createVerticalStrut(10));
        content.add(benefitRow("Premium offers", "Unlock seasonal rewards and luxury upgrades."));

        card.add(content, BorderLayout.CENTER);

        return card;
    }

    private JComponent levelLabel(String level, String min, Color color) {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        JLabel l1 = label(level, 12, Font.BOLD, color);
        JLabel l2 = label(min, 10, Font.PLAIN, MUTED);

        l1.setAlignmentX(Component.CENTER_ALIGNMENT);
        l2.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(l1);
        box.add(Box.createVerticalStrut(3));
        box.add(l2);

        return box;
    }

    private JComponent benefitRow(String title, String desc) {
        RoundedPanel row = new RoundedPanel(14, CARD_DARK);
        row.setLayout(new BorderLayout(12, 0));
        row.setBorder(new EmptyBorder(13, 14, 13, 14));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        SmallCheckIcon icon = new SmallCheckIcon();
        icon.setPreferredSize(new Dimension(36, 36));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        text.add(label(title, 13, Font.BOLD, TEXT));
        text.add(Box.createVerticalStrut(4));
        text.add(label(desc, 11, Font.PLAIN, MUTED));

        row.add(icon, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);

        return row;
    }

    private JComponent createRewardsCard() {
        RoundedPanel card = new RoundedPanel(18, CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = label("Available Rewards", 22, Font.BOLD, TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = label("Choose a reward and redeem it using your points.", 12, Font.PLAIN, MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(title);
        content.add(Box.createVerticalStrut(6));
        content.add(sub);
        content.add(Box.createVerticalStrut(20));

        content.add(rewardCard("10% Rental Discount", "Use on your next vehicle rental.", 300));
        content.add(Box.createVerticalStrut(12));
        content.add(rewardCard("Free Extra Hour", "Extend your rental by one hour for free.", 450));
        content.add(Box.createVerticalStrut(12));
        content.add(rewardCard("Premium Upgrade", "Upgrade to a higher vehicle class.", 700));
        content.add(Box.createVerticalStrut(12));
        content.add(rewardCard("VIP Cleaning Package", "Free premium cleaning service.", 250));

        card.add(content, BorderLayout.CENTER);

        return card;
    }

    private JComponent rewardCard(String title, String desc, int cost) {
        RoundedPanel row = new RoundedPanel(15, CARD_DARK);
        row.setLayout(new BorderLayout(12, 0));
        row.setBorder(new EmptyBorder(14, 14, 14, 14));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        RewardIcon icon = new RewardIcon();
        icon.setPreferredSize(new Dimension(54, 54));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        text.add(label(title, 14, Font.BOLD, TEXT));
        text.add(Box.createVerticalStrut(5));
        text.add(label(desc, 11, Font.PLAIN, MUTED));
        text.add(Box.createVerticalStrut(5));
        text.add(label(cost + " points", 12, Font.BOLD, PALE));

        JButton redeem = new GoldButton("Redeem");
        redeem.setPreferredSize(new Dimension(100, 38));
        redeem.addActionListener(e -> redeemReward(title, cost));

        row.add(icon, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        row.add(redeem, BorderLayout.EAST);

        return row;
    }

    private JComponent createHistoryCard() {
        RoundedPanel card = new RoundedPanel(18, CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 22, 18, 22));
        card.setPreferredSize(new Dimension(100, 185));

        JLabel title = label("Points History", 20, Font.BOLD, TEXT);
        title.setBorder(new EmptyBorder(0, 0, 12, 0));

        historyList = new JPanel();
        historyList.setOpaque(false);
        historyList.setLayout(new GridLayout(3, 1, 0, 8));

        historyList.add(historyRow("+120", "BMW X7 rental completed", "08 Jul 2026", GREEN));
        historyList.add(historyRow("-300", "Redeemed 10% rental discount", "04 Jul 2026", RED));
        historyList.add(historyRow("+80", "Toyota Prius rental completed", "01 Jul 2026", GREEN));

        card.add(title, BorderLayout.NORTH);
        card.add(historyList, BorderLayout.CENTER);

        return card;
    }

    private JComponent historyRow(String value, String title, String date, Color color) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel left = label(value, 15, Font.BOLD, color);
        left.setPreferredSize(new Dimension(80, 28));

        JLabel center = label(title, 12, Font.PLAIN, TEXT);
        JLabel right = label(date, 11, Font.PLAIN, MUTED);
        right.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(left, BorderLayout.WEST);
        row.add(center, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);

        return row;
    }

    private void redeemReward(String rewardName, int cost) {
        if (accountState.getLoyaltyPoints() < cost) {
            JOptionPane.showMessageDialog(
                    this,
                    "You do not have enough points for this reward.",
                    "Velora Loyalty",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Redeem \"" + rewardName + "\" for " + cost + " points?",
                "Confirm Reward",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        accountState.redeemReward(cost);

        addHistory("-" + cost, "Redeemed " + rewardName, RED);

        JOptionPane.showMessageDialog(
                this,
                "Reward redeemed successfully.\nRemaining points: " + accountState.getLoyaltyPoints(),
                "Velora Loyalty",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void addHistory(String value, String title, Color color) {
        historyList.remove(historyList.getComponentCount() - 1);

        JPanel newRow = (JPanel) historyRow(
                value,
                title,
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                color
        );

        historyList.add(newRow, 0);
        historyList.revalidate();
        historyList.repaint();
    }

    private void refreshTier() {
        points = accountState.getLoyaltyPoints();
        String tier;
        int tierStart;
        int tierEnd;
        String nextText;

        if (points < 500) {
            tier = "Bronze Member";
            tierStart = 0;
            tierEnd = 500;
            nextText = (500 - points) + " points left to reach Silver level";
        } else if (points < 1500) {
            tier = "Silver Member";
            tierStart = 500;
            tierEnd = 1500;
            nextText = (1500 - points) + " points left to reach Gold level";
        } else if (points < 3000) {
            tier = "Gold Member";
            tierStart = 1500;
            tierEnd = 3000;
            nextText = (3000 - points) + " points left to reach Platinum level";
        } else {
            tier = "Platinum Member";
            tierStart = 3000;
            tierEnd = 6000;
            nextText = "You are currently in the Platinum level";
        }

        if (pointsLabel != null) {
            pointsLabel.setText(formatNumber(points) + " Points");
        }

        if (tierLabel != null) {
            tierLabel.setText(tier);
            tierLabel.setForeground(tier.startsWith("Gold") || tier.startsWith("Platinum")
                    ? PALE
                    : tier.startsWith("Silver") ? GREEN : GOLD);
        }

        if (nextTierLabel != null) {
            nextTierLabel.setText(nextText);
        }

        if (progress != null) {
            int percentage = (int) (((points - tierStart) / (double) (tierEnd - tierStart)) * 100);
            progress.setProgress(Math.max(0, Math.min(100, percentage)));
        }
        if (availablePointsValue != null) {
            availablePointsValue.setText(formatNumber(points));
            currentLevelValue.setText(accountState.getTierName());
            toNextTierValue.setText(formatNumber(accountState.getPointsToNextTier()));
            rewardsUsedValue.setText(formatNumber(accountState.getRewardsUsed()));
        }
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

    private static String formatNumber(int value) {
        return String.format(Locale.US, "%,d", value);
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
                    .5,
                    .5,
                    getWidth() - 1,
                    getHeight() - 1,
                    radius,
                    radius
            );

            g.setColor(new Color(0, 0, 0, 70));
            g.fillRoundRect(5, 7, Math.max(0, getWidth() - 10), Math.max(0, getHeight() - 10), radius, radius);

            g.setPaint(new GradientPaint(
                    0,
                    0,
                    new Color(12, 22, 31),
                    getWidth(),
                    getHeight(),
                    fill
            ));
            g.fill(shape);

            g.setPaint(new GradientPaint(
                    0,
                    0,
                    new Color(214, 160, 66, 25),
                    getWidth(),
                    0,
                    new Color(214, 160, 66, 3)
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
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setPaint(new GradientPaint(
                    0,
                    0,
                    getModel().isRollover() ? new Color(250, 219, 158) : PALE,
                    getWidth(),
                    getHeight(),
                    new Color(164, 103, 33)
            ));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class TierProgress extends JComponent {

        private int progress = 35;

        void setProgress(int progress) {
            this.progress = Math.max(0, Math.min(100, progress));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int h = 16;
            int y = (getHeight() - h) / 2;

            g.setColor(new Color(3, 9, 15));
            g.fillRoundRect(0, y, getWidth(), h, h, h);

            int fillW = (int) (getWidth() * (progress / 100.0));

            g.setPaint(new GradientPaint(
                    0,
                    y,
                    new Color(214, 160, 66),
                    Math.max(1, fillW),
                    y + h,
                    new Color(86, 207, 114)
            ));
            g.fillRoundRect(0, y, fillW, h, h, h);

            g.setColor(new Color(214, 160, 66, 120));
            g.drawRoundRect(0, y, getWidth() - 1, h, h, h);

            g.dispose();
        }
    }

    private static final class BigBadge extends JComponent {

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            g.setColor(new Color(214, 160, 66, 25));
            g.fillOval(cx - 52, cy - 52, 104, 104);

            g.setPaint(new GradientPaint(
                    cx - 40,
                    cy - 40,
                    new Color(250, 218, 150),
                    cx + 40,
                    cy + 40,
                    new Color(154, 96, 28)
            ));
            g.fillOval(cx - 42, cy - 42, 84, 84);

            g.setColor(new Color(30, 20, 8));
            g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            Shape star = createStar(cx, cy, 30, 13);
            g.draw(star);

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

    private static final class LoyaltyIcon extends JComponent {

        private final String type;

        LoyaltyIcon(String type) {
            this.type = type;
            setOpaque(false);
        }

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

            if ("POINTS".equals(type)) {
                g.drawOval(cx - 14, cy - 14, 28, 28);
                g.drawLine(cx, cy - 9, cx, cy + 9);
                g.drawLine(cx - 7, cy - 3, cx + 5, cy - 3);
                g.drawLine(cx - 5, cy + 4, cx + 7, cy + 4);
            } else if ("TIER".equals(type)) {
                Shape star = createStar(cx, cy, 18, 8);
                g.draw(star);
            } else if ("NEXT".equals(type)) {
                g.drawLine(cx - 13, cy + 10, cx, cy - 12);
                g.drawLine(cx, cy - 12, cx + 13, cy + 10);
                g.drawLine(cx - 8, cy + 2, cx + 8, cy + 2);
            } else {
                g.drawRoundRect(cx - 15, cy - 13, 30, 26, 6, 6);
                g.drawLine(cx - 8, cy, cx - 2, cy + 6);
                g.drawLine(cx - 2, cy + 6, cx + 10, cy - 7);
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

    private static final class RewardIcon extends JComponent {

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

            g.drawRoundRect(cx - 16, cy - 10, 32, 24, 5, 5);
            g.drawLine(cx - 16, cy - 2, cx + 16, cy - 2);
            g.drawLine(cx, cy - 10, cx, cy + 14);
            g.drawArc(cx - 11, cy - 22, 11, 12, 180, -210);
            g.drawArc(cx, cy - 22, 11, 12, 0, 210);

            g.dispose();
        }
    }

    private static final class SmallCheckIcon extends JComponent {

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            g.setColor(new Color(86, 207, 114, 25));
            g.fillOval(cx - 16, cy - 16, 32, 32);

            g.setColor(GREEN);
            g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(cx - 8, cy, cx - 2, cy + 7);
            g.drawLine(cx - 2, cy + 7, cx + 10, cy - 8);

            g.dispose();
        }
    }
}
