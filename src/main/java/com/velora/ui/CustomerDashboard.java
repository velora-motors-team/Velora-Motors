package com.velora.ui;

import com.velora.authentication.Customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class CustomerDashboard extends JFrame {

    private static final Color BG = new Color(3, 8, 12);
    private static final Color PANEL = new Color(10, 15, 18);
    private static final Color PANEL_2 = new Color(18, 24, 28);
    private static final Color GOLD = new Color(210, 154, 65);
    private static final Color GOLD_LIGHT = new Color(235, 190, 112);
    private static final Color WHITE = new Color(242, 244, 247);
    private static final Color MUTED = new Color(155, 162, 172);
    private static final Color GREEN = new Color(70, 214, 103);

    private final Customer customer;

    public CustomerDashboard(Customer customer) {
        super("Velora Motors - Customer Dashboard");
        this.customer = customer;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 680));
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setContentPane(createRoot());
    }

    private JPanel createRoot() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);

        root.add(createSidebar(), BorderLayout.WEST);
        root.add(createMainArea(), BorderLayout.CENTER);

        return root;
    }

    private JPanel createSidebar() {
        RoundedPanel sidebar = new RoundedPanel(18, new Color(8, 12, 15), new Color(210, 154, 65, 75));
        sidebar.setPreferredSize(new Dimension(170, 0));
        sidebar.setLayout(new BorderLayout());
        sidebar.setBorder(new EmptyBorder(14, 0, 14, 0));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        LogoPanel logo = new LogoPanel();
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        top.add(logo);

        JLabel brand = new JLabel("VELORA MOTORS", SwingConstants.CENTER);
        brand.setFont(new Font("Segoe UI", Font.BOLD, 15));
        brand.setForeground(WHITE);
        brand.setAlignmentX(Component.CENTER_ALIGNMENT);
        top.add(brand);

        JLabel sub = new JLabel("PREMIUM BMW VEHICLE RENTAL", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.BOLD, 7));
        sub.setForeground(GOLD_LIGHT);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        top.add(sub);

        top.add(Box.createVerticalStrut(16));
        top.add(createProfileBox());
        top.add(Box.createVerticalStrut(12));

        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));

        menu.add(menuItem("Dashboard", "home", true));
        menu.add(menuItem("Vehicles", "car", false));
        menu.add(menuItem("Rentals", "calendar", false));
        menu.add(menuItem("Customers", "users", false));
        menu.add(menuItem("Billing", "billing", false));
        menu.add(menuItem("Maintenance", "wrench", false));
        menu.add(menuItem("Analytics", "chart", false));

        top.add(menu);
        sidebar.add(top, BorderLayout.NORTH);

        JButton logout = sideButton("Logout", "logout");
        logout.setPreferredSize(new Dimension(142, 40));
        logout.addActionListener(e -> {
            new LoginScreen().setVisible(true);
            dispose();
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        bottom.setOpaque(false);
        bottom.add(logout);

        sidebar.add(bottom, BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel createProfileBox() {
        RoundedPanel box = new RoundedPanel(15, new Color(12, 17, 20), new Color(210, 154, 65, 35));
        box.setLayout(null);
        box.setMaximumSize(new Dimension(150, 82));
        box.setPreferredSize(new Dimension(150, 82));
        box.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel avatarLabel = new JLabel(new AvatarIcon(42));
        avatarLabel.setBounds(10, 17, 42, 42);
        box.add(avatarLabel);

        JLabel name = new JLabel("Manager : " + safeFirstName());
        name.setForeground(WHITE);
        name.setFont(new Font("Segoe UI", Font.BOLD, 10));
        name.setBounds(58, 13, 90, 18);
        box.add(name);

        JLabel role = new JLabel("Role : Administrator");
        role.setForeground(MUTED);
        role.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        role.setBounds(58, 31, 90, 16);
        box.add(role);

        JLabel date = new JLabel(new SimpleDateFormat("dd MMM yyyy").format(new Date()));
        date.setForeground(MUTED);
        date.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        date.setBounds(58, 48, 90, 16);
        box.add(date);

        JLabel online = new JLabel("● Online");
        online.setForeground(GREEN);
        online.setFont(new Font("Segoe UI", Font.BOLD, 9));
        online.setBounds(58, 64, 80, 14);
        box.add(online);

        return box;
    }

    private JPanel menuItem(String text, String icon, boolean active) {
        RoundedPanel item = new RoundedPanel(
                10,
                active ? new Color(210, 154, 65, 210) : new Color(0, 0, 0, 0),
                active ? new Color(210, 154, 65, 0) : new Color(0, 0, 0, 0)
        );

        item.setLayout(null);
        item.setPreferredSize(new Dimension(146, 40));
        item.setMaximumSize(new Dimension(146, 40));
        item.setAlignmentX(Component.CENTER_ALIGNMENT);
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel ic = new JLabel(new LineIcon(icon, 20, active ? WHITE : GOLD));
        ic.setBounds(16, 10, 22, 22);
        item.add(ic);

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 12));
        label.setForeground(WHITE);
        label.setBounds(50, 9, 95, 22);
        item.add(label);

        item.addMouseListener(new HoverAdapter(
                item,
                active ? new Color(210, 154, 65, 210) : new Color(0, 0, 0, 0),
                active ? new Color(230, 180, 95, 230) : new Color(255, 255, 255, 12)
        ));

        return item;
    }

    private JButton sideButton(String text, String icon) {
        JButton btn = new JButton(text, new LineIcon(icon, 18, GOLD));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(12);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(GOLD_LIGHT);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 154, 65, 95)),
                new EmptyBorder(0, 14, 0, 0)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createMainArea() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(10, 16, 6, 12));

        main.add(createTopBar(), BorderLayout.NORTH);
        main.add(createDashboardContent(), BorderLayout.CENTER);

        return main;
    }

    private JPanel createTopBar() {
        JPanel top = new JPanel(new BorderLayout(16, 0));
        top.setOpaque(false);
        top.setPreferredSize(new Dimension(0, 46));

        JButton menu = iconButton("menu", 36, 36);
        top.add(menu, BorderLayout.WEST);

        SearchBox search = new SearchBox();
        search.setPreferredSize(new Dimension(315, 36));

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        center.setOpaque(false);
        center.add(search);
        top.add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        actions.add(iconButton("moon", 36, 36));
        actions.add(iconButton("bell", 36, 36));
        actions.add(iconButton("mail", 36, 36));
        actions.add(createUserChip());

        top.add(actions, BorderLayout.EAST);
        return top;
    }

    private JPanel createUserChip() {
        RoundedPanel chip = new RoundedPanel(10, new Color(8, 12, 16), new Color(210, 154, 65, 45));
        chip.setLayout(null);
        chip.setPreferredSize(new Dimension(165, 36));

        JLabel avatar = new JLabel(new AvatarIcon(28));
        avatar.setBounds(9, 4, 28, 28);
        chip.add(avatar);

        JLabel welcome = new JLabel("Welcome " + safeFirstName());
        welcome.setForeground(GOLD_LIGHT);
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 10));
        welcome.setBounds(44, 4, 105, 15);
        chip.add(welcome);

        JLabel role = new JLabel("Administrator");
        role.setForeground(MUTED);
        role.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        role.setBounds(44, 19, 100, 13);
        chip.add(role);

        JLabel arrow = new JLabel("⌄");
        arrow.setForeground(WHITE);
        arrow.setFont(new Font("Segoe UI", Font.BOLD, 16));
        arrow.setBounds(148, 8, 16, 18);
        chip.add(arrow);

        return chip;
    }

    private JButton iconButton(String icon, int w, int h) {
        JButton btn = new JButton(new LineIcon(icon, 18, GOLD_LIGHT));
        btn.setPreferredSize(new Dimension(w, h));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(210, 154, 65, 40)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createDashboardContent() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(createHero());
        content.add(Box.createVerticalStrut(6));
        content.add(sectionTitle("Quick Navigation"));
        content.add(Box.createVerticalStrut(6));
        content.add(createMiddleGrid());
        content.add(Box.createVerticalStrut(8));
        content.add(createBottomGrid());
        content.add(Box.createVerticalGlue());
        content.add(createFooter());

        wrap.add(content, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel createHero() {
        HeroPanel hero = new HeroPanel();
        hero.setPreferredSize(new Dimension(0, 255));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 255));
        hero.setLayout(null);

        JLabel welcome = new JLabel("Welcome back,");
        welcome.setForeground(WHITE);
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        welcome.setBounds(30, 55, 300, 32);
        hero.add(welcome);

        JLabel name = new JLabel(safeFirstName() + " 👋");
        name.setForeground(GOLD_LIGHT);
        name.setFont(new Font("Segoe UI", Font.BOLD, 31));
        name.setBounds(30, 88, 420, 40);
        hero.add(name);

        JLabel desc1 = new JLabel("Drive luxury. Drive Velora.");
        desc1.setForeground(WHITE);
        desc1.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        desc1.setBounds(30, 136, 280, 22);
        hero.add(desc1);

        JLabel desc2 = new JLabel("We provide premium experience");
        desc2.setForeground(WHITE);
        desc2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        desc2.setBounds(30, 158, 300, 22);
        hero.add(desc2);

        JLabel desc3 = new JLabel("and top quality service.");
        desc3.setForeground(WHITE);
        desc3.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        desc3.setBounds(30, 180, 300, 22);
        hero.add(desc3);

        JButton stats = goldOutlineButton("View Statistics", "chart");
        stats.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        stats.setBounds(30, 210, 155, 34);
        hero.add(stats);

        return hero;
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setForeground(WHITE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JPanel createMiddleGrid() {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));

        JPanel cards = new JPanel(new GridLayout(2, 4, 8, 8));
        cards.setOpaque(false);

        cards.add(navCard("Vehicles", "Manage your BMW fleet", "car"));
        cards.add(navCard("Rentals", "Manage rentals and reservations", "calendar"));
        cards.add(navCard("Customers", "View and manage customers", "users"));
        cards.add(navCard("Billing", "Invoices, payments and transactions", "billing"));
        cards.add(navCard("Maintenance", "Schedule and track maintenance", "wrench"));
        cards.add(navCard("Analytics", "View reports and performance", "chart"));

        JPanel empty1 = new JPanel();
        empty1.setOpaque(false);

        JPanel empty2 = new JPanel();
        empty2.setOpaque(false);

        cards.add(empty1);
        cards.add(empty2);

        row.add(cards, BorderLayout.CENTER);

        JPanel car = carOfWeekCard();
        car.setPreferredSize(new Dimension(300, 184));
        row.add(car, BorderLayout.EAST);

        return row;
    }

    private JPanel navCard(String title, String desc, String icon) {
        RoundedPanel card = new RoundedPanel(13, PANEL, new Color(210, 154, 65, 45));
        card.setLayout(null);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel ic = new JLabel(new CircleIcon(icon, 46));
        ic.setBounds(14, 20, 48, 48);
        card.add(ic);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(WHITE);
        titleLabel.setBounds(72, 22, 150, 20);
        card.add(titleLabel);

        JLabel descLabel = new JLabel("<html><div style='width:120px;'>" + desc + "</div></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        descLabel.setForeground(WHITE);
        descLabel.setBounds(72, 44, 125, 38);
        card.add(descLabel);

        JLabel arrow = new JLabel("➜", SwingConstants.CENTER);
        arrow.setOpaque(true);
        arrow.setBackground(GOLD);
        arrow.setForeground(new Color(17, 13, 8));
        arrow.setFont(new Font("Segoe UI", Font.BOLD, 13));
        arrow.setBounds(218, 64, 24, 24);
        arrow.setBorder(BorderFactory.createEmptyBorder());
        card.add(arrow);

        card.addMouseListener(new HoverAdapter(card, PANEL, PANEL_2));
        return card;
    }

    private JPanel carOfWeekCard() {
        CarWeekPanel card = new CarWeekPanel();
        card.setLayout(null);

        JLabel small = new JLabel("LUXURY CAR OF THE WEEK");
        small.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        small.setForeground(WHITE);
        small.setBounds(16, 15, 180, 16);
        card.add(small);

        JLabel title = new JLabel("BMW M8 Competition");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(GOLD_LIGHT);
        title.setBounds(16, 34, 210, 24);
        card.add(title);

        card.add(specRow("briefcase", "4.4L V8 Twin Turbo", 68));
        card.add(specRow("wrench", "617 HP", 92));
        card.add(specRow("speed", "305 km/h", 116));
        card.add(specRow("person", "3.2 sec (0-100 km/h)", 140));

        JButton details = goldOutlineButton("View Details", "arrow");
        details.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        details.setBounds(16, 165, 130, 30);
        card.add(details);

        return card;
    }

    private JLabel specRow(String icon, String text, int y) {
        JLabel label = new JLabel(text, new LineIcon(icon, 14, GOLD), SwingConstants.LEFT);
        label.setIconTextGap(10);
        label.setForeground(WHITE);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        label.setBounds(16, y, 155, 18);
        return label;
    }

    private JPanel createBottomGrid() {
        JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        row.add(todayOverview());
        row.add(recentActivity());
        row.add(announcements());

        return row;
    }

    private JPanel todayOverview() {
        RoundedPanel card = baseBottomCard();
        card.setLayout(null);

        JLabel title = cardTitle("Today’s Overview");
        title.setBounds(14, 9, 190, 18);
        card.add(title);

        addMiniStat(card, "Total Rentals", "15", "calendar", 12);
        addMiniStat(card, "Returns", "7", "return", 106);
        addMiniStat(card, "New Customers", "8", "users", 200);
        addMiniStat(card, "Maintenance Due", "4", "wrench", 294);

        JButton btn = goldOutlineButton("View All Statistics", "none");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btn.setBounds(14, 96, 125, 25);
        card.add(btn);

        return card;
    }

    private void addMiniStat(JPanel parent, String title, String value, String icon, int x) {
        JLabel ic = new JLabel(new LineIcon(icon, 18, GOLD));
        ic.setBounds(x + 34, 36, 20, 20);
        parent.add(ic);

        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setForeground(WHITE);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        t.setBounds(x, 58, 92, 15);
        parent.add(t);

        JLabel v = new JLabel(value, SwingConstants.CENTER);
        v.setForeground(WHITE);
        v.setFont(new Font("Segoe UI", Font.PLAIN, 19));
        v.setBounds(x, 76, 92, 21);
        parent.add(v);

        if (x > 20) {
            JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
            sep.setForeground(new Color(210, 154, 65, 25));
            sep.setBounds(x - 8, 40, 1, 50);
            parent.add(sep);
        }
    }

    private JPanel recentActivity() {
        RoundedPanel card = baseBottomCard();
        card.setLayout(null);

        JLabel title = cardTitle("Recent Activity");
        title.setBounds(16, 12, 180, 20);
        card.add(title);

        JLabel view = new JLabel("View All");
        view.setForeground(GOLD_LIGHT);
        view.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        view.setBounds(330, 10, 60, 16);
        card.add(view);

        addActivity(card, "New rental created for BMW X7", "by John Doe", "2 min ago", 35, "calendar");
        addActivity(card, "Vehicle returned BMW 430i", "by Sarah Johnson", "15 min ago", 56, "car");
        addActivity(card, "Maintenance scheduled for BMW M5", "Oil Change", "1 hour ago", 77, "wrench");
        addActivity(card, "New customer registered", "Michael Brown", "2 hours ago", 98, "users");

        return card;
    }

    private void addActivity(JPanel parent, String text, String sub, String time, int y, String icon) {
        JLabel ic = new JLabel(new CircleMiniIcon(icon));
        ic.setBounds(18, y, 22, 22);
        parent.add(ic);

        JLabel main = new JLabel(text);
        main.setForeground(WHITE);
        main.setFont(new Font("Segoe UI", Font.BOLD, 9));
        main.setBounds(48, y - 1, 230, 14);
        parent.add(main);

        JLabel small = new JLabel(sub);
        small.setForeground(MUTED);
        small.setFont(new Font("Segoe UI", Font.PLAIN, 8));
        small.setBounds(48, y + 12, 200, 12);
        parent.add(small);

        JLabel tm = new JLabel(time, SwingConstants.RIGHT);
        tm.setForeground(WHITE);
        tm.setFont(new Font("Segoe UI", Font.PLAIN, 8));
        tm.setBounds(300, y + 5, 82, 12);
        parent.add(tm);
    }

    private JPanel announcements() {
        AnnouncementPanel card = new AnnouncementPanel();
        card.setLayout(null);

        JLabel title = cardTitle("Announcements");
        title.setBounds(16, 12, 180, 20);
        card.add(title);

        JLabel megaphone = new JLabel(new MegaIcon(58, 58));
        megaphone.setBounds(20, 43, 62, 62);
        card.add(megaphone);

        JLabel offer = new JLabel("Summer Discount!");
        offer.setForeground(WHITE);
        offer.setFont(new Font("Segoe UI", Font.BOLD, 14));
        offer.setBounds(95, 45, 190, 20);
        card.add(offer);

        JLabel desc = new JLabel("<html><div style='width:190px;'>Enjoy up to 30% off on selected models.</div></html>");
        desc.setForeground(WHITE);
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        desc.setBounds(95, 68, 205, 35);
        card.add(desc);

        JButton btn = goldOutlineButton("View Offers", "none");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btn.setBounds(16, 96, 110, 25);
        card.add(btn);

        return card;
    }

    private RoundedPanel baseBottomCard() {
        RoundedPanel card = new RoundedPanel(13, PANEL, new Color(210, 154, 65, 45));
        card.setPreferredSize(new Dimension(0, 126));
        return card;
    }

    private JLabel cardTitle(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return label;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(6, 0, 0, 0));
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel copy = new JLabel("© 2026 Velora Motors. All rights reserved.");
        copy.setForeground(MUTED);
        copy.setFont(new Font("Segoe UI", Font.PLAIN, 10));

        JPanel midPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        midPanel.setOpaque(false);

        JLabel miniLogo = new JLabel(new MiniLogoIcon(58, 28));
        JLabel mid = new JLabel("<html><div style='text-align:center;'>VELORA MOTORS<br><span style='font-size:7px;'>PREMIUM BMW VEHICLE RENTAL</span></div></html>");
        mid.setForeground(WHITE);
        mid.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        midPanel.add(miniLogo);
        midPanel.add(mid);

        JLabel right = new JLabel("Drive Luxury. Drive BMW.");
        right.setForeground(GOLD_LIGHT);
        right.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        right.setHorizontalAlignment(SwingConstants.RIGHT);

        footer.add(copy, BorderLayout.WEST);
        footer.add(midPanel, BorderLayout.CENTER);
        footer.add(right, BorderLayout.EAST);

        return footer;
    }

    private JButton goldOutlineButton(String text, String icon) {
        JButton btn = icon.equals("none")
                ? new JButton(text)
                : new JButton(text, new LineIcon(icon, 15, GOLD_LIGHT));

        btn.setHorizontalTextPosition(SwingConstants.LEFT);
        btn.setIconTextGap(10);
        btn.setForeground(GOLD_LIGHT);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(210, 154, 65, 90)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private String safeFirstName() {
        if (customer == null || customer.getFullName() == null || customer.getFullName().trim().isEmpty()) {
            return "System Admin";
        }
        return customer.getFullName().trim().split("\\s+")[0];
    }

    private static final class RoundedPanel extends JPanel {
        private final int arc;
        private Color bg;
        private final Color border;

        RoundedPanel(int arc, Color bg, Color border) {
            this.arc = arc;
            this.bg = bg;
            this.border = border;
            setOpaque(false);
        }

        void setPanelBackground(Color bg) {
            this.bg = bg;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics raw) {
            super.paintComponent(raw);

            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(bg);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

            g.setColor(border);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

            g.dispose();
        }
    }

    private static final class HoverAdapter extends MouseAdapter {
        private final RoundedPanel panel;
        private final Color normal;
        private final Color hover;

        HoverAdapter(RoundedPanel panel, Color normal, Color hover) {
            this.panel = panel;
            this.normal = normal;
            this.hover = hover;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            panel.setPanelBackground(hover);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            panel.setPanelBackground(normal);
        }
    }

    private static final class LogoPanel extends JPanel {
        LogoPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(118, 52));
            setMaximumSize(new Dimension(118, 52));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            super.paintComponent(raw);
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            drawVeloraLogo(g, getWidth() / 2, 28, 0.86f);

            g.dispose();
        }
    }

    private static void drawVeloraLogo(Graphics2D g, int cx, int cy, float scale) {
        Graphics2D gg = (Graphics2D) g.create();
        gg.translate(cx, cy);
        gg.scale(scale, scale);

        GradientPaint gp = new GradientPaint(-60, -25, GOLD_LIGHT, 60, 25, GOLD);
        gg.setPaint(gp);

        Polygon left = new Polygon();
        left.addPoint(-8, 23);
        left.addPoint(-46, -16);
        left.addPoint(-25, -16);
        left.addPoint(0, 14);

        Polygon right = new Polygon();
        right.addPoint(8, 23);
        right.addPoint(46, -16);
        right.addPoint(25, -16);
        right.addPoint(0, 14);

        gg.fill(left);
        gg.fill(right);

        gg.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gg.drawLine(-58, -10, -22, -8);
        gg.drawLine(-52, 0, -16, 0);
        gg.drawLine(22, -8, 58, -10);
        gg.drawLine(16, 0, 52, 0);

        gg.dispose();
    }

    private static final class HeroPanel extends JPanel {
        HeroPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            super.paintComponent(raw);

            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            RoundRectangle2D rr = new RoundRectangle2D.Double(0, 0, w - 1, h - 1, 15, 15);
            g.setClip(rr);

            g.setPaint(new GradientPaint(0, 0, new Color(3, 9, 13), w, h, new Color(14, 10, 7)));
            g.fillRect(0, 0, w, h);

            drawShowroom(g, w, h);
            drawCar(g, w / 2 - 70, h - 86, 300, 62, true);
            drawCar(g, w - 320, h - 80, 260, 54, false);
            drawCar(g, w / 2 - 345, h - 70, 190, 42, false);

            g.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 210), w / 2, 0, new Color(0, 0, 0, 45)));
            g.fillRect(0, 0, w, h);

            g.setPaint(new GradientPaint(0, h - 70, new Color(0, 0, 0, 0), 0, h, new Color(0, 0, 0, 150)));
            g.fillRect(0, 0, w, h);

            g.setClip(null);
            g.setColor(new Color(210, 154, 65, 65));
            g.drawRoundRect(0, 0, w - 1, h - 1, 15, 15);

            g.dispose();
        }

        private void drawShowroom(Graphics2D g, int w, int h) {
            int buildingX = w / 2 - 80;
            int buildingY = 28;
            int buildingW = w / 2 + 40;
            int buildingH = h - 60;

            g.setColor(new Color(18, 22, 24, 220));
            g.fillRect(buildingX, buildingY, buildingW, buildingH);

            g.setColor(new Color(230, 170, 75, 45));
            for (int row = 0; row < 3; row++) {
                int y = buildingY + 42 + row * 58;
                for (int col = 0; col < 8; col++) {
                    int x = buildingX + 22 + col * 72;
                    g.fillRect(x, y, 55, 38);
                    g.setColor(new Color(255, 200, 110, 90));
                    g.drawRect(x, y, 55, 38);
                    g.setColor(new Color(230, 170, 75, 45));
                }
            }

            g.setColor(new Color(10, 12, 14, 235));
            g.fillRect(buildingX + 40, buildingY + 35, 390, 70);

            drawVeloraLogo(g, buildingX + 120, buildingY + 68, 0.62f);

            g.setColor(new Color(245, 190, 92, 190));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 28));
            g.drawString("VELORA MOTORS", buildingX + 155, buildingY + 75);

            g.setColor(new Color(255, 210, 120, 40));
            for (int i = 0; i < 12; i++) {
                g.drawLine(buildingX + 30 + i * 70, buildingY + 118, buildingX + 5 + i * 70, h - 40);
            }

            g.setColor(new Color(255, 190, 80, 38));
            for (int i = 0; i < 40; i++) {
                int x = buildingX + 80 + (i * 43) % Math.max(1, buildingW - 120);
                int y = buildingY + 48 + (i * 29) % 120;
                g.fillOval(x, y, 4, 4);
            }

            g.setColor(new Color(6, 11, 14, 170));
            g.fillRect(0, 0, buildingX + 50, h);
        }

        private void drawCar(Graphics2D g, int x, int y, int w, int h, boolean main) {
            g.setColor(new Color(1, 4, 6, 235));
            g.fillRoundRect(x + 24, y + h / 3, w - 48, h / 2, 34, 34);

            g.setColor(new Color(10, 16, 20, 245));
            Polygon roof = new Polygon();
            roof.addPoint(x + w / 4, y + h / 2);
            roof.addPoint(x + w / 3, y + 8);
            roof.addPoint(x + w * 2 / 3, y + 8);
            roof.addPoint(x + w * 3 / 4, y + h / 2);
            g.fillPolygon(roof);

            g.setColor(new Color(65, 88, 105, 135));
            g.drawLine(x + 18, y + h / 2, x + w - 18, y + h / 2);

            g.setColor(main ? new Color(220, 235, 245, 230) : new Color(190, 210, 220, 190));
            g.fillOval(x + 44, y + h / 2, 38, 8);
            g.fillOval(x + w - 82, y + h / 2, 38, 8);

            g.setColor(Color.BLACK);
            g.fillOval(x + 60, y + h - 14, 34, 34);
            g.fillOval(x + w - 96, y + h - 14, 34, 34);

            g.setColor(new Color(210, 154, 65, 42));
            g.drawLine(x + 5, y + h + 18, x + w - 5, y + h + 18);
        }
    }

    private static final class CarWeekPanel extends JPanel {
        CarWeekPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            super.paintComponent(raw);

            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g.setColor(new Color(9, 13, 16));
            g.fillRoundRect(0, 0, w - 1, h - 1, 13, 13);

            g.setPaint(new GradientPaint(w / 2, 0, new Color(210, 154, 65, 30), w, h, new Color(0, 0, 0, 120)));
            g.fillRoundRect(0, 0, w - 1, h - 1, 13, 13);

            g.setColor(new Color(210, 154, 65, 55));
            g.drawRoundRect(0, 0, w - 1, h - 1, 13, 13);

            drawCar(g, w - 160, 78, 145, 70);

            g.setColor(new Color(210, 154, 65, 30));
            g.drawLine(165, 18, 165, h - 20);

            g.dispose();
        }

        private void drawCar(Graphics2D g, int x, int y, int w, int h) {
            g.setColor(new Color(2, 5, 8, 225));
            g.fillRoundRect(x + 8, y + 25, w - 16, 30, 26, 26);

            g.setColor(new Color(18, 24, 28));
            Polygon roof = new Polygon();
            roof.addPoint(x + 36, y + 30);
            roof.addPoint(x + 55, y + 8);
            roof.addPoint(x + 98, y + 8);
            roof.addPoint(x + 116, y + 30);
            g.fillPolygon(roof);

            g.setColor(new Color(230, 238, 242));
            g.fillOval(x + 18, y + 35, 25, 6);
            g.fillOval(x + w - 45, y + 35, 25, 6);

            g.setColor(Color.BLACK);
            g.fillOval(x + 27, y + 52, 25, 25);
            g.fillOval(x + w - 54, y + 52, 25, 25);

            g.setColor(new Color(210, 154, 65, 50));
            g.drawLine(x + 5, y + 76, x + w - 5, y + 76);
        }
    }

    private static final class AnnouncementPanel extends JPanel {
        AnnouncementPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            super.paintComponent(raw);

            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g.setColor(PANEL);
            g.fillRoundRect(0, 0, w - 1, h - 1, 13, 13);

            g.setColor(new Color(210, 154, 65, 45));
            g.drawRoundRect(0, 0, w - 1, h - 1, 13, 13);

            g.setColor(new Color(210, 154, 65, 35));
            for (int i = 0; i < 6; i++) {
                g.fillOval(w - 75 + i * 9, 38 + i * 10, 28, 8);
            }

            g.setColor(new Color(145, 110, 60, 60));
            g.setStroke(new BasicStroke(2));
            g.drawLine(w - 58, 42, w - 22, 124);

            g.dispose();
        }
    }

    private static final class SearchBox extends JPanel {
        SearchBox() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            super.paintComponent(raw);

            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(new Color(6, 11, 16));
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
            g.setColor(new Color(210, 154, 65, 35));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);

            g.setColor(MUTED);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g.drawString("Search anything...", 18, 23);

            g.setColor(WHITE);
            g.setStroke(new BasicStroke(1.6f));
            g.drawOval(getWidth() - 34, 10, 13, 13);
            g.drawLine(getWidth() - 23, 21, getWidth() - 16, 28);

            g.dispose();
        }
    }

    private static final class AvatarIcon implements Icon {
        private final int size;

        AvatarIcon(int size) {
            this.size = size;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component c, Graphics raw, int x, int y) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(new Color(18, 25, 30));
            g.fillOval(x, y, size, size);
            g.setColor(GOLD_LIGHT);
            g.drawOval(x, y, size - 1, size - 1);

            g.setColor(new Color(230, 185, 140));
            g.fillOval(x + size / 3, y + size / 5, size / 3, size / 3);

            g.setColor(new Color(22, 28, 34));
            g.fillRoundRect(x + size / 4, y + size / 2, size / 2, size / 3, 8, 8);

            g.setColor(WHITE);
            g.drawLine(x + size / 2, y + size / 2 + 4, x + size / 2 - 6, y + size - 7);
            g.drawLine(x + size / 2, y + size / 2 + 4, x + size / 2 + 6, y + size - 7);

            g.dispose();
        }
    }

    private static final class LineIcon implements Icon {
        private final String type;
        private final int size;
        private final Color color;

        LineIcon(String type, int size, Color color) {
            this.type = type;
            this.size = size;
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component c, Graphics raw, int x, int y) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setStroke(new BasicStroke(Math.max(1.4f, size / 11f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(color);

            int s = size;
            int cx = x + s / 2;
            int cy = y + s / 2;

            switch (type) {
                case "home":
                    g.drawLine(x + 3, y + s / 2, cx, y + 3);
                    g.drawLine(cx, y + 3, x + s - 3, y + s / 2);
                    g.drawRect(x + 6, y + s / 2, s - 12, s / 2 - 4);
                    break;

                case "car":
                    g.drawRoundRect(x + 3, y + s / 2 - 2, s - 6, s / 3, 8, 8);
                    g.drawLine(x + 7, y + s / 2 - 2, x + 12, y + s / 3);
                    g.drawLine(x + 12, y + s / 3, x + s - 12, y + s / 3);
                    g.drawLine(x + s - 12, y + s / 3, x + s - 7, y + s / 2 - 2);
                    g.fillOval(x + 6, y + s - 7, 4, 4);
                    g.fillOval(x + s - 10, y + s - 7, 4, 4);
                    break;

                case "calendar":
                    g.drawRoundRect(x + 4, y + 5, s - 8, s - 7, 3, 3);
                    g.drawLine(x + 4, y + 10, x + s - 4, y + 10);
                    g.drawLine(x + 8, y + 3, x + 8, y + 7);
                    g.drawLine(x + s - 8, y + 3, x + s - 8, y + 7);
                    break;

                case "users":
                    g.drawOval(x + 4, y + 4, s / 3, s / 3);
                    g.drawOval(x + s / 2, y + 4, s / 3, s / 3);
                    g.drawArc(x + 2, y + s / 2, s / 2, s / 2, 0, 180);
                    g.drawArc(x + s / 2 - 2, y + s / 2, s / 2, s / 2, 0, 180);
                    break;

                case "billing":
                    g.drawRect(x + 5, y + 3, s - 10, s - 4);
                    g.setFont(new Font("Segoe UI", Font.BOLD, Math.max(9, s - 8)));
                    g.drawString("$", x + s / 2 - 4, y + s / 2 + 5);
                    break;

                case "wrench":
                    g.drawLine(x + 5, y + s - 5, x + s - 5, y + 5);
                    g.drawOval(x + s - 8, y + 3, 6, 6);
                    g.drawOval(x + 3, y + s - 8, 6, 6);
                    break;

                case "chart":
                    g.drawLine(x + 4, y + s - 4, x + s - 3, y + s - 4);
                    g.drawRect(x + 5, y + s - 10, 3, 6);
                    g.drawRect(x + 11, y + s - 14, 3, 10);
                    g.drawRect(x + 17, y + s - 18, 3, 14);
                    break;

                case "menu":
                    g.drawLine(x + 4, y + 6, x + s - 4, y + 6);
                    g.drawLine(x + 4, y + s / 2, x + s - 4, y + s / 2);
                    g.drawLine(x + 4, y + s - 6, x + s - 4, y + s - 6);
                    break;

                case "moon":
                    g.drawArc(x + 6, y + 3, s - 8, s - 6, 80, 220);
                    g.drawArc(x + 10, y + 3, s - 9, s - 6, 100, 220);
                    break;

                case "bell":
                    g.drawArc(x + 5, y + 5, s - 10, s - 8, 0, 180);
                    g.drawLine(x + 5, y + s / 2, x + 5, y + s - 7);
                    g.drawLine(x + s - 5, y + s / 2, x + s - 5, y + s - 7);
                    g.drawLine(x + 4, y + s - 7, x + s - 4, y + s - 7);
                    g.fillOval(cx - 2, y + s - 5, 4, 4);
                    break;

                case "mail":
                    g.drawRect(x + 3, y + 5, s - 6, s - 10);
                    g.drawLine(x + 3, y + 5, cx, cy);
                    g.drawLine(x + s - 3, y + 5, cx, cy);
                    break;

                case "logout":
                    g.drawRect(x + 3, y + 5, s / 2, s - 10);
                    g.drawLine(x + s / 2, cy, x + s - 3, cy);
                    g.drawLine(x + s - 7, cy - 4, x + s - 3, cy);
                    g.drawLine(x + s - 7, cy + 4, x + s - 3, cy);
                    break;

                case "return":
                    g.drawArc(x + 4, y + 5, s - 8, s - 8, 30, 270);
                    g.drawLine(x + 5, y + s / 2, x + 1, y + s / 2 - 5);
                    g.drawLine(x + 5, y + s / 2, x + 10, y + s / 2 - 5);
                    break;

                case "arrow":
                    g.drawLine(x + 3, cy, x + s - 4, cy);
                    g.drawLine(x + s - 8, cy - 4, x + s - 4, cy);
                    g.drawLine(x + s - 8, cy + 4, x + s - 4, cy);
                    break;

                case "briefcase":
                    g.drawRoundRect(x + 3, y + 7, s - 6, s - 8, 3, 3);
                    g.drawLine(x + 6, y + 7, x + 6, y + 4);
                    g.drawLine(x + s - 6, y + 7, x + s - 6, y + 4);
                    g.drawLine(x + 6, y + 4, x + s - 6, y + 4);
                    break;

                case "speed":
                    g.drawArc(x + 3, y + 5, s - 6, s - 6, 0, 180);
                    g.drawLine(cx, cy, x + s - 4, y + 8);
                    break;

                case "person":
                    g.drawOval(cx - 3, y + 3, 6, 6);
                    g.drawArc(x + 4, y + 10, s - 8, s - 6, 0, 180);
                    break;

                default:
                    g.drawOval(x + 3, y + 3, s - 6, s - 6);
                    break;
            }

            g.dispose();
        }
    }

    private static final class CircleIcon implements Icon {
        private final String type;
        private final int size;

        CircleIcon(String type, int size) {
            this.type = type;
            this.size = size;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component c, Graphics raw, int x, int y) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(new Color(210, 154, 65, 18));
            g.fillOval(x, y, size - 1, size - 1);

            g.setColor(new Color(210, 154, 65, 95));
            g.drawOval(x, y, size - 1, size - 1);

            new LineIcon(type, size / 2, GOLD).paintIcon(c, g, x + size / 4, y + size / 4);

            g.dispose();
        }
    }

    private static final class CircleMiniIcon implements Icon {
        private final String type;

        CircleMiniIcon(String type) {
            this.type = type;
        }

        @Override
        public int getIconWidth() {
            return 22;
        }

        @Override
        public int getIconHeight() {
            return 22;
        }

        @Override
        public void paintIcon(Component c, Graphics raw, int x, int y) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(new Color(66, 175, 77, 55));
            g.fillOval(x, y, 22, 22);

            g.setColor(new Color(80, 210, 91));
            g.drawOval(x, y, 21, 21);

            new LineIcon(type, 13, new Color(90, 220, 100)).paintIcon(c, g, x + 4, y + 4);

            g.dispose();
        }
    }

    private static final class MegaIcon implements Icon {
        private final int w;
        private final int h;

        MegaIcon(int w, int h) {
            this.w = w;
            this.h = h;
        }

        @Override
        public int getIconWidth() {
            return w;
        }

        @Override
        public int getIconHeight() {
            return h;
        }

        @Override
        public void paintIcon(Component c, Graphics raw, int x, int y) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(new Color(210, 154, 65, 30));
            g.fillOval(x, y, w, h);

            g.setColor(new Color(210, 154, 65, 75));
            g.drawOval(x + 4, y + 4, w - 8, h - 8);

            g.setColor(GOLD_LIGHT);
            Polygon horn = new Polygon();
            horn.addPoint(x + 18, y + 30);
            horn.addPoint(x + 48, y + 14);
            horn.addPoint(x + 48, y + 52);
            horn.addPoint(x + 18, y + 42);
            g.fillPolygon(horn);

            g.setColor(new Color(140, 97, 40));
            g.fillRect(x + 13, y + 30, 10, 18);
            g.fillRect(x + 39, y + 47, 10, 14);

            g.setColor(new Color(255, 220, 140, 100));
            g.drawArc(x + 43, y + 14, 22, 40, -50, 100);

            g.dispose();
        }
    }

    private static final class MiniLogoIcon implements Icon {
        private final int w;
        private final int h;

        MiniLogoIcon(int w, int h) {
            this.w = w;
            this.h = h;
        }

        @Override
        public int getIconWidth() {
            return w;
        }

        @Override
        public int getIconHeight() {
            return h;
        }

        @Override
        public void paintIcon(Component c, Graphics raw, int x, int y) {
            Graphics2D g = (Graphics2D) raw.create();
            g.translate(x + w / 2, y + h / 2);
            drawVeloraLogo(g, 0, 0, 0.42f);
            g.dispose();
        }
    }
}