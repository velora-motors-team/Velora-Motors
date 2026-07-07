
package com.velora.ui;

import com.velora.authentication.Customer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class CustomerDashboard extends JFrame {

    private static final Color GOLD = new Color(214, 168, 91);
    private static final Color GOLD_LIGHT = new Color(238, 199, 140);
    private static final Color TEXT = new Color(245, 245, 245);
    private static final Color MUTED = new Color(170, 176, 186);
    private static final Color LINE = new Color(214, 168, 91, 75);

    private final Customer customer;

    private final BufferedImage iconImage;
    private final BufferedImage heroImage;
    private final BufferedImage rentalCard1;
    private final BufferedImage rentalCard2;
    private final BufferedImage rentalCard3;
    private final BufferedImage sidebarCar;
    private final BufferedImage footerDriveImage;

    private JLabel totalRentalsValueLabel;
    private JLabel activeRentalsValueLabel;
    private JLabel loyaltyPointsValueLabel;
    private JLabel totalSpentValueLabel;
    private Timer statsAnimationTimer;

    public CustomerDashboard(Customer customer) {
        super("Velora Motors - Customer Dashboard");
        this.customer = customer;

        iconImage = loadImage("/images/icon.png");
        heroImage = loadImage("/images/customer-hero-reference.png");
        rentalCard1 = loadImage("/images/customer-rental-x7-v2.png");
        rentalCard2 = loadImage("/images/customer-rental-m8-v2.png");
        rentalCard3 = loadImage("/images/customer-featured-i7-v2.png");
        sidebarCar = loadImage("/images/SIDEBAR_CAR.png");
        footerDriveImage = loadImage("/images/customer-footer-drive.png");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setMinimumSize(new Dimension(1240, 760));
        setSize(1540, 920);
        setLocationRelativeTo(null);

        if (iconImage != null) {
            setIconImage(iconImage);
        }

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setContentPane(createRoot());
        SwingUtilities.invokeLater(() -> {
            refreshDashboardStats();

            Timer welcomeTimer = new Timer(320, e -> {
                ((Timer) e.getSource()).stop();
                showWelcomeToast();
            });
            welcomeTimer.setRepeats(false);
            welcomeTimer.start();
        });
    }

    private JPanel createRoot() {
        JPanel root = new GradientRoot();
        root.setLayout(new BorderLayout());

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(createSidebar(), BorderLayout.WEST);
        body.add(createMain(), BorderLayout.CENTER);

        JPanel footerHolder = new JPanel(new BorderLayout());
        footerHolder.setOpaque(true);
        footerHolder.setBackground(new Color(2, 5, 9));
        footerHolder.setBorder(new EmptyBorder(0, 14, 8, 14));
        footerHolder.add(createFooter(), BorderLayout.CENTER);

        root.add(body, BorderLayout.CENTER);
        root.add(footerHolder, BorderLayout.SOUTH);
        return root;
    }

    private JPanel createSidebar() {
        RoundedPanel sidebar = new RoundedPanel(24);
        sidebar.setPreferredSize(new Dimension(230, 760));
        sidebar.setBackground(new Color(4, 8, 13, 245));
        sidebar.setLayout(new BorderLayout());
        sidebar.setBorder(new EmptyBorder(14, 14, 12, 14));

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));

        LogoImagePanel wingsLogo = new LogoImagePanel(iconImage, 112, 62);
        wingsLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel name = new JLabel("VELORA MOTORS");
        name.setForeground(TEXT);
        name.setFont(new Font("Serif", Font.PLAIN, 21));
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("PREMIUM VEHICLE RENTAL");
        sub.setForeground(GOLD_LIGHT);
        sub.setFont(new Font("SansSerif", Font.BOLD, 11));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        brand.add(wingsLogo);
        brand.add(Box.createVerticalStrut(10));
        brand.add(name);
        brand.add(Box.createVerticalStrut(4));
        brand.add(sub);

        JPanel menu = new JPanel(new GridLayout(8, 1, 0, 6));
        menu.setOpaque(false);
        menu.setPreferredSize(new Dimension(202, 292));

        menu.add(menuButton(MenuIconType.HOME, "Dashboard", true, () -> showMessage("Dashboard")));
        menu.add(menuButton(MenuIconType.CAR, "Vehicles", false, () -> showMessage("Vehicles catalog will open here.")));
        menu.add(menuButton(MenuIconType.CALENDAR, "My Rentals", false, () -> showMessage("My Rentals will be implemented later.")));
        menu.add(menuButton(MenuIconType.FILE, "Billing & Invoices", false, () -> showMessage("Billing & Invoices will be implemented later.")));
        menu.add(menuButton(MenuIconType.DIAMOND, "Loyalty Points", false, () -> showMessage("Loyalty Points will be implemented later.")));
        menu.add(menuButton(MenuIconType.STAR, "Reviews", false, () -> showMessage("Reviews will be implemented later.")));
        menu.add(menuButton(MenuIconType.USER, "Profile", false, () -> showMessage("Profile will be implemented later.")));
        menu.add(menuButton(MenuIconType.HEADSET, "Support", false, () -> showMessage("Support will be implemented later.")));

        JPanel menuHolder = new JPanel(new BorderLayout());
        menuHolder.setOpaque(false);
        menuHolder.add(menu, BorderLayout.NORTH);

        top.add(brand, BorderLayout.NORTH);
        top.add(menuHolder, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(0, 10));
        bottom.setOpaque(false);

        JButton logout = outlineButton("Logout");
        logout.addActionListener(e -> logout());

        ImageCard sidebarPromo = new ImageCard(sidebarCar, true);
        sidebarPromo.setPreferredSize(new Dimension(202, 230));
        sidebarPromo.setLayout(null);
        RoundedButton sidebarExplore = new RoundedButton("Explore BMW Collection    →", 8);
        sidebarExplore.setForeground(GOLD_LIGHT);
        sidebarExplore.setBackground(new Color(8, 12, 17, 228));
        sidebarExplore.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        sidebarExplore.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sidebarExplore.addActionListener(e -> searchVehicles("BMW"));
        sidebarPromo.add(sidebarExplore);
        sidebarPromo.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int buttonY = Math.max(82, Math.round(sidebarPromo.getHeight() * .39f));
                sidebarExplore.setBounds(11, buttonY, Math.max(0, sidebarPromo.getWidth() - 22), 26);
            }
        });

        bottom.add(logout, BorderLayout.NORTH);
        bottom.add(sidebarPromo, BorderLayout.CENTER);

        sidebar.add(top, BorderLayout.CENTER);
        sidebar.add(bottom, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel createMain() {
        JPanel main = new JPanel(new BorderLayout(0, 6));
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(4, 12, 7, 18));

        main.add(createTopBar(), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JComponent hero = createHero();
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel cards = createCardsGrid();
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(hero);
        content.add(Box.createVerticalStrut(12));
        content.add(cards);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        scroll.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        main.add(scroll, BorderLayout.CENTER);

        return main;
    }

    private JPanel createTopBar() {
        JPanel top = new JPanel(new BorderLayout(16, 0));
        top.setOpaque(false);
        top.setPreferredSize(new Dimension(1000, 58));

        SearchPanel search = new SearchPanel("Search for BMW cars...");
        search.setPreferredSize(new Dimension(520, 38));

        JPanel searchHolder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 9));
        searchHolder.setOpaque(false);
        searchHolder.add(search);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 6));
        right.setOpaque(false);

        TopBadgeIcon bell = new TopBadgeIcon(BadgeIconType.BELL, 3);
        bell.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showMessage("Notifications:\n• Rental reminder will appear here.\n• Booking updates will appear here.");
            }
        });

        TopBadgeIcon mail = new TopBadgeIcon(BadgeIconType.MAIL, 2);
        mail.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showMessage("Messages:\n• Support replies and invoices will appear here.");
            }
        });

        right.add(bell);
        right.add(mail);
        right.add(createProfileBlock());
        right.add(createWindowControls());

        top.add(Box.createHorizontalStrut(240), BorderLayout.WEST);
        top.add(searchHolder, BorderLayout.CENTER);
        top.add(right, BorderLayout.EAST);

        return top;
    }

    private JComponent createWindowControls() {
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 5));
        controls.setOpaque(false);

        WindowControlButton minimize = new WindowControlButton("—", false);
        minimize.setToolTipText("Minimize");
        minimize.addActionListener(e -> setExtendedState(JFrame.ICONIFIED));

        WindowControlButton maximize = new WindowControlButton("▢", false);
        maximize.setToolTipText("Maximize / Restore");
        maximize.addActionListener(e -> {
            int state = getExtendedState();
            setExtendedState((state & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH
                    ? JFrame.NORMAL
                    : JFrame.MAXIMIZED_BOTH);
        });

        WindowControlButton close = new WindowControlButton("×", true);
        close.setToolTipText("Close");
        close.addActionListener(e -> dispatchEvent(new java.awt.event.WindowEvent(
                this,
                java.awt.event.WindowEvent.WINDOW_CLOSING
        )));

        controls.add(minimize);
        controls.add(maximize);
        controls.add(close);
        return controls;
    }

    private JPanel createProfileBlock() {
        JPanel profile = new JPanel(new BorderLayout(10, 0));
        profile.setOpaque(false);
        profile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel text = new JPanel(new GridLayout(2, 1));
        text.setOpaque(false);

        JLabel small = new JLabel("Welcome Back,");
        small.setForeground(MUTED);
        small.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel n = new JLabel(getCustomerName());
        n.setForeground(GOLD_LIGHT);
        n.setFont(new Font("Segoe UI", Font.BOLD, 14));

        text.add(small);
        text.add(n);

        JLabel arrow = new JLabel("v");
        arrow.setForeground(GOLD_LIGHT);
        arrow.setFont(new Font("Segoe UI", Font.BOLD, 17));

        profile.add(new AvatarIcon(48), BorderLayout.WEST);
        profile.add(text, BorderLayout.CENTER);
        profile.add(arrow, BorderLayout.EAST);

        profile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showMessage("Customer Profile:\n" + getCustomerName());
            }
        });

        return profile;
    }

    private JComponent createHero() {
        LuxuryHeroPanel hero = new LuxuryHeroPanel(
                heroImage,
                iconImage,
                () -> searchVehicles("BMW")
        );

        JPanel stats = createStats();

        HeroShowcasePanel wrapper = new HeroShowcasePanel(hero, stats);
        wrapper.setPreferredSize(new Dimension(1120, 370));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 370));
        return wrapper;
    }

    private JPanel createStats() {
        StatsStripPanel strip = new StatsStripPanel();
        strip.setBackground(new Color(5, 8, 13, 232));
        strip.setLayout(new GridLayout(1, 4, 0, 0));
        strip.setBorder(new EmptyBorder(8, 34, 8, 34));

        strip.add(statCard(MenuIconType.CAR, "TOTAL RENTALS", StatSlot.TOTAL_RENTALS));
        strip.add(statCard(MenuIconType.CALENDAR, "ACTIVE RENTALS", StatSlot.ACTIVE_RENTALS));
        strip.add(statCard(MenuIconType.DIAMOND, "LOYALTY POINTS", StatSlot.LOYALTY_POINTS));
        strip.add(statCard(MenuIconType.FILE, "TOTAL SPENT", StatSlot.TOTAL_SPENT));

        return strip;
    }

    private JPanel statCard(MenuIconType iconType, String label, StatSlot slot) {
        JPanel p = new JPanel(new BorderLayout(14, 0));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 18, 0, 18));

        StatIcon icon = new StatIcon(iconType, 34);
        p.add(icon, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel l = new JLabel(label);
        l.setForeground(new Color(229, 231, 236));
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel v = new JLabel(slot == StatSlot.TOTAL_SPENT ? "$0" : "0");
        v.setForeground(TEXT);
        v.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        v.setAlignmentX(Component.LEFT_ALIGNMENT);
        assignStatValueLabel(slot, v);

        textPanel.add(Box.createVerticalGlue());
        textPanel.add(l);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(v);
        textPanel.add(Box.createVerticalGlue());

        p.add(textPanel, BorderLayout.CENTER);

        return p;
    }

    private JPanel createCardsGrid() {
        JPanel wrapper = new DashboardCardsPanel(
                createActiveRentalsPanel(),
                createFeaturedVehiclePanel(),
                createQuickAccessPanel()
        );
        wrapper.setPreferredSize(new Dimension(1120, 282));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 282));
        return wrapper;
    }

    private JPanel createActiveRentalsPanel() {
        RoundedPanel panel = new RoundedPanel(14);
        panel.setBackground(new Color(3, 7, 12, 242));
        panel.setLayout(new BorderLayout(0, 8));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JLabel title = new JLabel("ACTIVE RENTALS");
        title.setForeground(TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel viewAll = new JLabel("View All");
        viewAll.setForeground(GOLD_LIGHT);
        viewAll.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        viewAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewAll.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showMessage("All active rentals will open here.");
            }
        });
        heading.add(title, BorderLayout.WEST);
        heading.add(viewAll, BorderLayout.EAST);

        JPanel rentals = new JPanel(new GridLayout(1, 2, 9, 0));
        rentals.setOpaque(false);
        rentals.add(new CompactRentalCard(
                rentalCard1,
                "BMW X7 xDrive40i",
                "#RT-2026-0123",
                "10 - 17 Jun 2026",
                "7 Days Left",
                () -> showMessage("BMW X7 xDrive40i\nRental details will open here.")
        ));
        rentals.add(new CompactRentalCard(
                rentalCard2,
                "BMW M8 Competition",
                "#RT-2026-0156",
                "01 - 07 Jun 2026",
                "2 Days Left",
                () -> showMessage("BMW M8 Competition\nRental details will open here.")
        ));

        panel.add(heading, BorderLayout.NORTH);
        panel.add(rentals, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFeaturedVehiclePanel() {
        return new FeaturedVehicleCard(
                rentalCard3,
                () -> showMessage("BMW i7 M70\nBooking details will open here.")
        );
    }

    private JPanel createQuickAccessPanel() {
        RoundedPanel panel = new RoundedPanel(14);
        panel.setBackground(new Color(3, 7, 12, 242));
        panel.setLayout(new BorderLayout(0, 8));
        panel.setBorder(new EmptyBorder(12, 10, 10, 10));

        JLabel title = new JLabel("QUICK ACCESS");
        title.setForeground(TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        title.setBorder(new EmptyBorder(0, 2, 0, 0));
        panel.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(3, 2, 6, 6));
        grid.setOpaque(false);
        grid.add(quickAccessButton(MenuIconType.CAR, "Browse Cars",
                () -> showMessage("Browse the BMW collection.")));
        grid.add(quickAccessButton(MenuIconType.CALENDAR, "Reservations",
                () -> showMessage("Your reservations will open here.")));
        grid.add(quickAccessButton(MenuIconType.FILE, "Billing & Invoices",
                () -> showMessage("Billing and invoices will open here.")));
        grid.add(quickAccessButton(MenuIconType.DIAMOND, "Loyalty Points",
                () -> showMessage("You have 2,450 loyalty points.")));
        grid.add(quickAccessButton(MenuIconType.CALENDAR, "My Rentals",
                () -> showMessage("Your rentals will open here.")));
        grid.add(quickAccessButton(MenuIconType.USER, "Profile",
                () -> showMessage("Customer Profile:\n" + getCustomerName())));
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JButton quickAccessButton(MenuIconType icon, String text, Runnable action) {
        QuickAccessButton button = new QuickAccessButton(icon, text);
        button.addActionListener(e -> action.run());
        return button;
    }

    private JPanel createFooter() {
        RoundedPanel footer = new RoundedPanel(10);
        footer.setPreferredSize(new Dimension(1120, 84));
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
        footer.setBackground(new Color(3, 6, 10, 245));
        footer.setLayout(new GridBagLayout());
        footer.setBorder(new EmptyBorder(6, 18, 6, 14));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1;
        gc.insets = new Insets(0, 8, 0, 8);

        gc.gridx = 0;
        gc.weightx = 2.1;
        footer.add(footerBrand(), gc);

        gc.gridx = 1;
        gc.weightx = 1.25;
        footer.add(footerItem(MenuIconType.CAR, "PREMIUM FLEET", "Latest BMW Models"), gc);

        gc.gridx = 2;
        gc.weightx = 1.25;
        footer.add(footerItem(MenuIconType.DIAMOND, "TRUSTED SERVICE", "Excellence in every step"), gc);

        gc.gridx = 3;
        gc.weightx = 1.15;
        footer.add(footerItem(MenuIconType.STAR, "BEST PRICES", "Luxury within reach"), gc);

        gc.gridx = 4;
        gc.weightx = 1.15;
        footer.add(footerItem(MenuIconType.HEADSET, "24/7 SUPPORT", "We are here for you"), gc);

        gc.gridx = 5;
        gc.weightx = 1.25;
        gc.insets = new Insets(0, 14, 0, 0);
        footer.add(footerDrivePanel(), gc);

        return footer;
    }

    private JPanel footerDrivePanel() {
        JPanel panel = new FooterDriveImagePanel(footerDriveImage);
        panel.setPreferredSize(new Dimension(214, 62));
        return panel;
    }

    private JPanel footerBrand() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);

        JPanel text = new JPanel(new GridLayout(2, 1));
        text.setOpaque(false);

        JLabel title = new JLabel("VELORA MOTORS");
        title.setForeground(TEXT);
        title.setFont(new Font("Serif", Font.PLAIN, 18));

        JLabel sub = new JLabel("PREMIUM BMW VEHICLE RENTAL");
        sub.setForeground(MUTED);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 9));

        text.add(title);
        text.add(sub);

        panel.add(new LogoImagePanel(iconImage, 58, 32), BorderLayout.WEST);
        panel.add(text, BorderLayout.CENTER);

        return panel;
    }

    private JPanel footerItem(MenuIconType iconType, String title, String sub) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);

        FooterSmallIcon icon = new FooterSmallIcon(iconType, 24);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel a = new JLabel(title);
        a.setForeground(TEXT);
        a.setFont(new Font("Segoe UI", Font.BOLD, 10));
        a.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel b = new JLabel(sub);
        b.setForeground(MUTED);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);

        text.add(Box.createVerticalGlue());
        text.add(a);
        text.add(Box.createVerticalStrut(3));
        text.add(b);
        text.add(Box.createVerticalGlue());

        p.add(icon, BorderLayout.WEST);
        p.add(text, BorderLayout.CENTER);

        return p;
    }

    private JPanel clickableCard(BufferedImage image, String vehicleName) {
        ButtonOverlayImageCard card = new ButtonOverlayImageCard(
                image,
                false,
                0.04,
                0.72,
                0.25,
                0.18,
                () -> showMessage(vehicleName + "\nVehicle details will open here.")
        );
        card.setToolTipText(null);
        return card;
    }

    private void searchVehicles(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();

        if (q.isEmpty()) {
            showMessage("Type a vehicle name to search.");
            return;
        }

        if (q.contains("x7")) {
            showMessage("Search result:\nBMW X7 xDrive40i is available.");
        } else if (q.contains("m8")) {
            showMessage("Search result:\nBMW M8 Competition is available.");
        } else if (q.contains("i7") || q.contains("electric")) {
            showMessage("Search result:\nBMW i7 M70 is available.");
        } else if (q.contains("bmw") || q.contains("car") || q.contains("vehicle")) {
            showMessage("Search results:\n- BMW X7 xDrive40i\n- BMW M8 Competition\n- BMW i7 M70");
        } else {
            showMessage("No vehicles found for: " + query);
        }
    }

    private JButton menuButton(MenuIconType iconType, String text, boolean active, Runnable action) {
        MenuButton b = new MenuButton(iconType, text, active);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> action.run());
        return b;
    }

    private JButton outlineButton(String text) {
        RoundedButton b = new RoundedButton(text, 14);
        b.setPreferredSize(new Dimension(220, 40));
        b.setBackground(new Color(0, 0, 0, 0));
        b.setForeground(GOLD_LIGHT);
        b.setFont(new Font("Segoe UI", Font.BOLD, 15));
        b.setBorder(new EmptyBorder(0, 0, 0, 0));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void assignStatValueLabel(StatSlot slot, JLabel valueLabel) {
        switch (slot) {
            case TOTAL_RENTALS -> totalRentalsValueLabel = valueLabel;
            case ACTIVE_RENTALS -> activeRentalsValueLabel = valueLabel;
            case LOYALTY_POINTS -> loyaltyPointsValueLabel = valueLabel;
            case TOTAL_SPENT -> totalSpentValueLabel = valueLabel;
        }
    }

    private void refreshDashboardStats() {
        updateDashboardStats(
                getTotalRentalsCount(),
                getActiveRentalsCount(),
                getLoyaltyPointsCount(),
                getTotalSpentAmount()
        );
    }

    public void updateDashboardStats(int totalRentals, int activeRentals, int loyaltyPoints, int totalSpent) {
        if (totalRentalsValueLabel == null
                || activeRentalsValueLabel == null
                || loyaltyPointsValueLabel == null
                || totalSpentValueLabel == null) {
            return;
        }

        if (statsAnimationTimer != null && statsAnimationTimer.isRunning()) {
            statsAnimationTimer.stop();
        }

        final int frames = 28;
        final int[] frame = {0};

        statsAnimationTimer = new Timer(24, e -> {
            frame[0]++;
            double progress = Math.min(1.0, frame[0] / (double) frames);
            double eased = 1 - Math.pow(1 - progress, 3);

            totalRentalsValueLabel.setText(formatNumber((int) Math.round(totalRentals * eased)));
            activeRentalsValueLabel.setText(formatNumber((int) Math.round(activeRentals * eased)));
            loyaltyPointsValueLabel.setText(formatNumber((int) Math.round(loyaltyPoints * eased)));
            totalSpentValueLabel.setText("$" + formatNumber((int) Math.round(totalSpent * eased)));

            if (progress >= 1.0) {
                statsAnimationTimer.stop();
                totalRentalsValueLabel.setText(formatNumber(totalRentals));
                activeRentalsValueLabel.setText(formatNumber(activeRentals));
                loyaltyPointsValueLabel.setText(formatNumber(loyaltyPoints));
                totalSpentValueLabel.setText("$" + formatNumber(totalSpent));
            }
        });
        statsAnimationTimer.start();
    }

    private int getTotalRentalsCount() {
        return 12;
    }

    private int getActiveRentalsCount() {
        return 2;
    }

    private int getLoyaltyPointsCount() {
        return 2450;
    }

    private int getTotalSpentAmount() {
        return 24560;
    }

    private static String formatNumber(int value) {
        return String.format("%,d", value);
    }

    private void logout() {
        LogoutConfirmDialog confirmDialog = new LogoutConfirmDialog(this);
        confirmDialog.setVisible(true);

        if (!confirmDialog.isConfirmed()) {
            return;
        }

        if (statsAnimationTimer != null && statsAnimationTimer.isRunning()) {
            statsAnimationTimer.stop();
        }

        dispose();
        new FarewellScreen(getCustomerName()).setVisible(true);
    }

    private void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Velora Motors", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWelcomeToast() {
        JLayeredPane layered = getLayeredPane();
        if (layered == null || layered.getWidth() <= 0 || layered.getHeight() <= 0) {
            return;
        }

        WelcomeToast toast = new WelcomeToast("Welcome back, " + getCustomerName());
        int width = 300;
        int height = 64;
        int targetX = Math.max(18, layered.getWidth() - width - 30);
        int startY = -height - 12;
        int targetY = 82;

        toast.setBounds(targetX, startY, width, height);
        layered.add(toast, JLayeredPane.POPUP_LAYER);
        layered.revalidate();
        layered.repaint();

        final int[] frame = {0};
        Timer timer = new Timer(16, null);
        timer.addActionListener(e -> {
            frame[0]++;
            int current = frame[0];

            if (current <= 16) {
                double p = current / 16.0;
                double eased = 1 - Math.pow(1 - p, 3);
                int y = startY + (int) Math.round((targetY - startY) * eased);
                toast.setBounds(targetX, y, width, height);
                toast.setAlpha((float) Math.min(1.0, .25 + eased * .75));
            } else if (current <= 140) {
                toast.setBounds(targetX, targetY, width, height);
                toast.setAlpha(1f);
            } else if (current <= 156) {
                double p = (current - 140) / 16.0;
                double eased = p * p;
                int y = targetY - (int) Math.round((height + 18) * eased);
                toast.setBounds(targetX, y, width, height);
                toast.setAlpha((float) Math.max(0, 1 - p));
            } else {
                timer.stop();
                layered.remove(toast);
                layered.repaint();
            }
        });
        timer.start();
    }

    private String getCustomerName() {
        if (customer == null || customer.getFullName() == null || customer.getFullName().trim().isEmpty()) {
            return "Omar Al-Khatib";
        }
        return customer.getFullName();
    }

    private static BufferedImage loadImage(String path) {
        try {
            java.net.URL url = CustomerDashboard.class.getResource(path);
            if (url != null) {
                return ImageIO.read(url);
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    private static void drawCentered(Graphics2D g, String text, int centerX, int baseline) {
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, centerX - metrics.stringWidth(text) / 2, baseline);
    }

    private enum MenuIconType {
        HOME, CAR, CALENDAR, FILE, DIAMOND, STAR, USER, HEADSET
    }

    private enum BadgeIconType {
        BELL, MAIL
    }

    private enum StatSlot {
        TOTAL_RENTALS, ACTIVE_RENTALS, LOYALTY_POINTS, TOTAL_SPENT
    }

    private static final class GradientRoot extends JPanel {
        @Override
        protected void paintComponent(Graphics raw) {
            super.paintComponent(raw);

            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setPaint(new GradientPaint(0, 0, new Color(2, 5, 9), getWidth(), getHeight(), new Color(7, 11, 16)));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setComposite(AlphaComposite.SrcOver.derive(0.07f));
            g.setColor(GOLD);
            g.fillOval(getWidth() - 250, -170, 360, 285);

            g.dispose();
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;

        RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            RoundRectangle2D body = new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g.setColor(getBackground());
            g.fill(body);

            g.setColor(LINE);
            g.setStroke(new BasicStroke(1.0f));
            g.draw(body);

            g.dispose();
        }
    }

    private static final class StatsStripPanel extends RoundedPanel {

        StatsStripPanel() {
            super(18);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            super.paintComponent(raw);

            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setPaint(new GradientPaint(
                    0, 0, new Color(255, 255, 255, 22),
                    0, getHeight(), new Color(255, 255, 255, 0)
            ));
            g.fillRoundRect(1, 1, getWidth() - 2, Math.max(18, getHeight() / 2), 18, 18);

            int top = 22;
            int bottom = getHeight() - 22;
            g.setStroke(new BasicStroke(1f));
            for (int i = 1; i < 4; i++) {
                int x = getWidth() * i / 4;
                g.setPaint(new GradientPaint(
                        x, top, new Color(214, 168, 91, 0),
                        x, (top + bottom) / 2, new Color(214, 168, 91, 58),
                        true
                ));
                g.drawLine(x, top, x, bottom);
            }

            g.dispose();
        }
    }

    private static final class DashboardCardsPanel extends JPanel {

        private final Component rentals;
        private final Component featured;
        private final Component quickAccess;

        DashboardCardsPanel(Component rentals, Component featured, Component quickAccess) {
            this.rentals = rentals;
            this.featured = featured;
            this.quickAccess = quickAccess;
            setOpaque(false);
            setLayout(null);
            add(rentals);
            add(featured);
            add(quickAccess);
        }

        @Override
        public void doLayout() {
            int gap = 12;
            int available = Math.max(0, getWidth() - gap * 2);
            int rentalsWidth = Math.round(available * .45f);
            int featuredWidth = Math.round(available * .32f);
            int quickWidth = Math.max(0, available - rentalsWidth - featuredWidth);

            rentals.setBounds(0, 0, rentalsWidth, getHeight());
            featured.setBounds(rentalsWidth + gap, 0, featuredWidth, getHeight());
            quickAccess.setBounds(rentalsWidth + featuredWidth + gap * 2, 0, quickWidth, getHeight());
        }
    }

    private static final class HeroShowcasePanel extends JPanel {

        private final Component hero;
        private final Component stats;

        HeroShowcasePanel(Component hero, Component stats) {
            this.hero = hero;
            this.stats = stats;
            setOpaque(false);
            setLayout(null);
            add(hero);
            add(stats);
            setComponentZOrder(stats, 0);
            setComponentZOrder(hero, 1);
        }

        @Override
        public void doLayout() {
            int w = getWidth();
            int h = getHeight();
            hero.setBounds(0, 0, w, h);

            int insetX = 14;
            int statsH = Math.min(74, Math.max(64, h / 5));
            int statsY = h - statsH - 10;
            stats.setBounds(insetX, statsY, Math.max(0, w - insetX * 2), statsH);
        }
    }

    private static final class DarkScrollBarUI extends BasicScrollBarUI {

        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(214, 168, 91, 72);
            trackColor = new Color(2, 6, 10);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return hiddenButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return hiddenButton();
        }

        private JButton hiddenButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintThumb(Graphics raw, JComponent component, Rectangle bounds) {
            if (bounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(214, 168, 91, 88));
            g.fillRoundRect(bounds.x + 1, bounds.y, Math.max(3, bounds.width - 2),
                    bounds.height, bounds.width, bounds.width);
            g.dispose();
        }
    }

    private static class ImageCard extends RoundedPanel {
        private final BufferedImage image;
        private final boolean cover;

        ImageCard(BufferedImage image, boolean cover) {
            super(16);
            this.image = image;
            this.cover = cover;
            setBackground(new Color(5, 10, 16, 220));
            setLayout(new BorderLayout());
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            Shape clip = new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g.setClip(clip);

            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());

            if (image != null) {
                drawImage(g, image, 0, 0, getWidth(), getHeight(), cover);
            } else {
                g.setPaint(new GradientPaint(0, 0, new Color(15, 20, 28), getWidth(), getHeight(), new Color(4, 8, 12)));
                g.fillRect(0, 0, getWidth(), getHeight());

                g.setColor(GOLD);
                g.setFont(new Font("Serif", Font.PLAIN, 26));
                g.drawString("VELORA MOTORS", 30, 60);
            }

            g.setClip(null);
            g.setColor(LINE);
            g.draw(clip);
            g.dispose();
        }

        private static void drawImage(Graphics2D g, BufferedImage img, int x, int y, int w, int h, boolean cover) {
            double s = cover
                    ? Math.max(w / (double) img.getWidth(), h / (double) img.getHeight())
                    : Math.min(w / (double) img.getWidth(), h / (double) img.getHeight());

            int iw = (int) Math.round(img.getWidth() * s);
            int ih = (int) Math.round(img.getHeight() * s);

            int ix = x + (w - iw) / 2;
            int iy = y + (h - ih) / 2;

            g.drawImage(img, ix, iy, iw, ih, null);
        }
    }

    private static final class RoundedButton extends JButton {
        private final int radius;

        RoundedButton(String text, int radius) {
            super(text);
            this.radius = radius;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            RoundRectangle2D body = new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g.setColor(getBackground());
            g.fill(body);

            g.setColor(getModel().isRollover() ? new Color(214, 168, 91, 135) : LINE);
            g.draw(body);

            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class WindowControlButton extends JButton {

        private final boolean close;
        private final String symbol;

        WindowControlButton(String symbol, boolean close) {
            super("");
            this.symbol = symbol;
            this.close = close;
            setPreferredSize(new Dimension(31, 31));
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean hover = getModel().isRollover();

            Color start = close && hover
                    ? new Color(118, 28, 31, 238)
                    : new Color(7, 14, 20, 228);
            Color end = close && hover
                    ? new Color(180, 45, 50, 238)
                    : new Color(2, 8, 13, 238);
            g.setPaint(new GradientPaint(0, 0, start, getWidth(), getHeight(), end));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g.setColor(close && hover
                    ? new Color(255, 120, 126, 160)
                    : new Color(214, 168, 91, hover ? 125 : 64));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            g.setColor(close ? new Color(255, 174, 170) : GOLD_LIGHT);
            g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if ("×".equals(symbol)) {
                g.drawLine(cx - 5, cy - 5, cx + 5, cy + 5);
                g.drawLine(cx + 5, cy - 5, cx - 5, cy + 5);
            } else if ("▢".equals(symbol)) {
                g.drawRoundRect(cx - 6, cy - 6, 12, 12, 2, 2);
            } else {
                g.drawLine(cx - 6, cy, cx + 6, cy);
            }
            g.dispose();
        }
    }

    private static final class LuxuryHeroPanel extends RoundedPanel {

        private final BufferedImage image;
        private final JButton action = new JButton();

        LuxuryHeroPanel(BufferedImage image, BufferedImage brandLogo, Runnable onAction) {
            super(16);
            this.image = image;
            setBackground(new Color(2, 6, 10));
            setLayout(null);

            action.setOpaque(false);
            action.setContentAreaFilled(false);
            action.setBorderPainted(false);
            action.setFocusPainted(false);
            action.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            action.addActionListener(e -> onAction.run());
            add(action);
        }

        @Override
        public void doLayout() {
            if (image == null || getWidth() <= 0 || getHeight() <= 0) {
                action.setBounds(0, 0, 0, 0);
                return;
            }

            double scale = Math.max(
                    getWidth() / (double) image.getWidth(),
                    getHeight() / (double) image.getHeight()
            );
            int renderedW = (int) Math.round(image.getWidth() * scale);
            int renderedH = (int) Math.round(image.getHeight() * scale);
            int imageX = (getWidth() - renderedW) / 2;
            int imageY = (getHeight() - renderedH) / 2;

            // Exact Explore button bounds in customer-hero-reference.png.
            int x = imageX + (int) Math.round(image.getWidth() * .4143 * scale);
            int y = imageY + (int) Math.round(image.getHeight() * .4250 * scale);
            int buttonW = (int) Math.round(image.getWidth() * .1900 * scale);
            int buttonH = (int) Math.round(image.getHeight() * .0650 * scale);
            action.setBounds(x, y, Math.max(1, buttonW), Math.max(1, buttonH));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Shape clip = new RoundRectangle2D.Double(
                    .5, .5, getWidth() - 1, getHeight() - 1, 16, 16
            );
            g.clip(clip);
            g.setColor(new Color(2, 6, 10));
            g.fillRect(0, 0, getWidth(), getHeight());
            if (image != null) {
                ImageCard.drawImage(g, image, 0, 0, getWidth(), getHeight(), true);
            }

            g.setClip(null);
            g.setColor(new Color(214, 168, 91, 84));
            g.draw(clip);
            g.dispose();
        }
    }

    private static final class CompactRentalCard extends RoundedPanel {

        private final BufferedImage image;
        private final String vehicle;
        private final String rentalId;
        private final String date;
        private final String remaining;
        private final RoundedButton details = new RoundedButton("View Details     →", 8);

        CompactRentalCard(
                BufferedImage image,
                String vehicle,
                String rentalId,
                String date,
                String remaining,
                Runnable action
        ) {
            super(12);
            this.image = image;
            this.vehicle = vehicle;
            this.rentalId = rentalId;
            this.date = date;
            this.remaining = remaining;
            setBackground(new Color(3, 7, 12));
            setLayout(null);

            details.setForeground(TEXT);
            details.setBackground(new Color(78, 44, 15, 225));
            details.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            details.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            details.addActionListener(e -> action.run());
            add(details);
        }

        @Override
        public void doLayout() {
            details.setBounds(12, getHeight() - 39, 112, 28);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Shape clip = new RoundRectangle2D.Double(.5, .5, getWidth() - 1, getHeight() - 1, 12, 12);
            g.clip(clip);
            if (image != null) {
                ImageCard.drawImage(g, image, 0, 0, getWidth(), getHeight(), true);
            } else {
                g.setColor(new Color(4, 9, 14));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            g.setPaint(new GradientPaint(
                    0, 0, new Color(1, 5, 9, 238),
                    getWidth() * .75f, getHeight(), new Color(1, 5, 9, 35)
            ));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(TEXT);
            g.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g.drawString(vehicle, 12, 24);
            g.setColor(MUTED);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 8));
            g.drawString(rentalId, 12, 42);

            g.setColor(new Color(50, 132, 70, 225));
            g.fillRoundRect(getWidth() - 59, 12, 48, 18, 6, 6);
            g.setColor(new Color(232, 255, 236));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 7));
            g.drawString("Ongoing", getWidth() - 52, 24);

            g.setColor(GOLD_LIGHT);
            g.setStroke(new BasicStroke(1.2f));
            g.drawRect(13, 54, 9, 8);
            g.drawOval(13, 69, 9, 9);
            g.setColor(new Color(222, 224, 229));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 8));
            g.drawString(date, 28, 62);
            g.drawString(remaining, 28, 77);

            g.setClip(null);
            g.setColor(new Color(214, 168, 91, 58));
            g.draw(clip);
            g.dispose();
        }
    }

    private static final class FeaturedVehicleCard extends RoundedPanel {

        private final BufferedImage image;
        private final RoundedButton book = new RoundedButton("Book Now     →", 8);

        FeaturedVehicleCard(BufferedImage image, Runnable action) {
            super(14);
            this.image = image;
            setBackground(new Color(3, 7, 12));
            setLayout(null);
            book.setForeground(TEXT);
            book.setBackground(new Color(78, 44, 15, 225));
            book.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            book.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            book.addActionListener(e -> action.run());
            add(book);
        }

        @Override
        public void doLayout() {
            book.setBounds(16, getHeight() - 48, 112, 29);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Shape clip = new RoundRectangle2D.Double(.5, .5, getWidth() - 1, getHeight() - 1, 14, 14);
            g.clip(clip);
            if (image != null) {
                ImageCard.drawImage(g, image, 0, 0, getWidth(), getHeight(), true);
            }
            g.setPaint(new GradientPaint(
                    0, 0, new Color(2, 6, 10, 235),
                    getWidth() * .72f, getHeight(), new Color(2, 6, 10, 18)
            ));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(new Color(210, 212, 217));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g.drawString("THE ALL-NEW", 16, 35);
            g.setColor(TEXT);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 25));
            g.drawString("BMW i7 M70", 16, 67);
            g.setColor(new Color(219, 220, 224));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g.drawString("100% ELECTRIC. 100% LUXURY.", 16, 88);

            g.setClip(null);
            g.setColor(new Color(214, 168, 91, 64));
            g.draw(clip);
            g.dispose();
        }
    }

    private static final class QuickAccessButton extends JButton {

        private final MenuIconType icon;
        private final String label;

        QuickAccessButton(MenuIconType icon, String label) {
            super("");
            this.icon = icon;
            this.label = label;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            boolean hover = getModel().isRollover();

            g.setColor(new Color(7, 13, 19, hover ? 248 : 215));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 9, 9);
            g.setColor(new Color(214, 168, 91, hover ? 92 : 25));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 9, 9);

            int iconSize = 21;
            drawMenuIcon(g, icon, (getWidth() - iconSize) / 2, 8, iconSize, GOLD_LIGHT);

            g.setColor(TEXT);
            g.setFont(new Font("Segoe UI", Font.PLAIN, getWidth() < 105 ? 8 : 9));
            FontMetrics fm = g.getFontMetrics();
            String shown = label;
            while (fm.stringWidth(shown) > getWidth() - 8 && shown.length() > 5) {
                shown = shown.substring(0, shown.length() - 2) + "…";
            }
            g.drawString(shown, (getWidth() - fm.stringWidth(shown)) / 2, getHeight() - 9);
            g.dispose();
        }
    }

    private static final class MenuButton extends JButton {
        private final MenuIconType iconType;
        private final String label;
        private final boolean active;

        MenuButton(MenuIconType iconType, String label, boolean active) {
            this.iconType = iconType;
            this.label = label;
            this.active = active;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setPreferredSize(new Dimension(210, 42));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            RoundRectangle2D body = new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

            if (active) {
                g.setColor(new Color(93, 58, 23, 210));
                g.fill(body);
            } else if (getModel().isRollover()) {
                g.setColor(new Color(214, 168, 91, 22));
                g.fill(body);
            }

            g.setColor(LINE);
            g.draw(body);

            Color iconColor = active ? new Color(255, 240, 220) : GOLD_LIGHT;
            CustomerDashboard.drawMenuIcon(g, iconType, 18, getHeight() / 2 - 9, 18, iconColor);

            g.setColor(TEXT);
            g.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 15));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(label, 50, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());

            g.dispose();
        }

        private void drawMenuIcon(Graphics2D g, MenuIconType type, int x, int y, int s, Color color) {
            g.setColor(color);

            switch (type) {
                case HOME -> {
                    Path2D roof = new Path2D.Double();
                    roof.moveTo(x + s / 2.0, y);
                    roof.lineTo(x, y + s / 2.0);
                    roof.lineTo(x + 3, y + s / 2.0);
                    roof.lineTo(x + 3, y + s);
                    roof.lineTo(x + s - 3, y + s);
                    roof.lineTo(x + s - 3, y + s / 2.0);
                    roof.lineTo(x + s, y + s / 2.0);
                    roof.closePath();
                    g.draw(roof);
                }
                case CAR -> {
                    RoundRectangle2D body = new RoundRectangle2D.Double(x + 2, y + 6, s - 4, 7, 4, 4);
                    g.draw(body);
                    g.drawLine(x + 5, y + 6, x + 8, y + 2);
                    g.drawLine(x + s - 5, y + 6, x + s - 8, y + 2);
                    g.draw(new Ellipse2D.Double(x + 3, y + 11, 4, 4));
                    g.draw(new Ellipse2D.Double(x + s - 7, y + 11, 4, 4));
                }
                case CALENDAR -> {
                    g.draw(new RoundRectangle2D.Double(x + 1, y + 2, s - 2, s - 3, 4, 4));
                    g.drawLine(x + 1, y + 7, x + s - 1, y + 7);
                    g.drawLine(x + 5, y, x + 5, y + 5);
                    g.drawLine(x + s - 5, y, x + s - 5, y + 5);
                }
                case FILE -> {
                    Path2D file = new Path2D.Double();
                    file.moveTo(x + 3, y + 1);
                    file.lineTo(x + s - 6, y + 1);
                    file.lineTo(x + s - 2, y + 5);
                    file.lineTo(x + s - 2, y + s - 1);
                    file.lineTo(x + 3, y + s - 1);
                    file.closePath();
                    g.draw(file);
                    g.drawLine(x + s - 6, y + 1, x + s - 6, y + 5);
                    g.drawLine(x + s - 6, y + 5, x + s - 2, y + 5);
                    g.drawLine(x + 6, y + 8, x + s - 6, y + 8);
                    g.drawLine(x + 6, y + 11, x + s - 6, y + 11);
                }
                case DIAMOND -> {
                    Path2D d = new Path2D.Double();
                    d.moveTo(x + s / 2.0, y);
                    d.lineTo(x + s - 1, y + s / 2.0);
                    d.lineTo(x + s / 2.0, y + s);
                    d.lineTo(x, y + s / 2.0);
                    d.closePath();
                    g.draw(d);
                }
                case STAR -> {
                    double cx = x + s / 2.0;
                    double cy = y + s / 2.0;
                    int r1 = s / 2;
                    int r2 = s / 4;
                    Path2D star = new Path2D.Double();
                    for (int i = 0; i < 10; i++) {
                        double ang = -Math.PI / 2 + i * Math.PI / 5;
                        double r = (i % 2 == 0) ? r1 : r2;
                        double px = cx + Math.cos(ang) * r;
                        double py = cy + Math.sin(ang) * r;
                        if (i == 0) star.moveTo(px, py);
                        else star.lineTo(px, py);
                    }
                    star.closePath();
                    g.draw(star);
                }
                case USER -> {
                    g.draw(new Ellipse2D.Double(x + 5, y + 1, 8, 8));
                    Arc2D arc = new Arc2D.Double(x + 2, y + 7, 14, 11, 0, 180, Arc2D.OPEN);
                    g.draw(arc);
                }
                case HEADSET -> {
                    Arc2D top = new Arc2D.Double(x + 2, y + 1, 14, 12, 0, 180, Arc2D.OPEN);
                    g.draw(top);
                    g.draw(new RoundRectangle2D.Double(x + 1, y + 8, 4, 6, 3, 3));
                    g.draw(new RoundRectangle2D.Double(x + s - 5, y + 8, 4, 6, 3, 3));
                    g.drawLine(x + s - 3, y + 14, x + s - 1, y + 16);
                    g.drawLine(x + s - 1, y + 16, x + s - 6, y + 16);
                }
            }
        }
    }

    private static final class StatIcon extends JPanel {
        private final MenuIconType iconType;
        private final int size;

        StatIcon(MenuIconType iconType, int size) {
            this.iconType = iconType;
            this.size = size;
            setOpaque(false);
            setPreferredSize(new Dimension(size + 8, size + 8));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(GOLD_LIGHT);

            int s = size;
            int x = (getWidth() - s) / 2;
            int y = (getHeight() - s) / 2;

            switch (iconType) {
                case CAR -> {
                    RoundRectangle2D body = new RoundRectangle2D.Double(x + 2, y + 12, s - 4, 10, 5, 5);
                    g.draw(body);
                    g.drawLine(x + 7, y + 12, x + 11, y + 6);
                    g.drawLine(x + s - 7, y + 12, x + s - 11, y + 6);
                    g.draw(new Ellipse2D.Double(x + 5, y + 20, 6, 6));
                    g.draw(new Ellipse2D.Double(x + s - 11, y + 20, 6, 6));
                }
                case CALENDAR -> {
                    g.draw(new RoundRectangle2D.Double(x + 3, y + 5, s - 6, s - 8, 5, 5));
                    g.drawLine(x + 3, y + 13, x + s - 3, y + 13);
                    g.drawLine(x + 10, y + 2, x + 10, y + 9);
                    g.drawLine(x + s - 10, y + 2, x + s - 10, y + 9);
                    g.drawLine(x + 10, y + 19, x + 13, y + 19);
                    g.drawLine(x + 17, y + 19, x + 20, y + 19);
                    g.drawLine(x + 10, y + 24, x + 13, y + 24);
                }
                case DIAMOND -> {
                    Path2D d = new Path2D.Double();
                    d.moveTo(x + s / 2.0, y + 2);
                    d.lineTo(x + s - 3, y + s / 2.0);
                    d.lineTo(x + s / 2.0, y + s - 2);
                    d.lineTo(x + 3, y + s / 2.0);
                    d.closePath();
                    g.draw(d);
                    g.drawLine(x + 10, y + s / 2, x + s - 10, y + s / 2);
                    g.drawLine(x + s / 2, y + 2, x + s / 2, y + s - 2);
                }
                case FILE -> {
                    RoundRectangle2D wallet = new RoundRectangle2D.Double(x + 3, y + 8, s - 6, s - 12, 5, 5);
                    g.draw(wallet);
                    g.draw(new RoundRectangle2D.Double(x + s - 15, y + 14, 11, 8, 4, 4));
                    g.draw(new Ellipse2D.Double(x + s - 10, y + 17, 2, 2));
                    g.drawLine(x + 7, y + 8, x + 12, y + 3);
                    g.drawLine(x + 12, y + 3, x + s - 7, y + 8);
                }
                default -> {
                    g.draw(new Ellipse2D.Double(x + 5, y + 5, s - 10, s - 10));
                }
            }

            g.dispose();
        }
    }

    private static final class FooterSmallIcon extends JPanel {
        private final MenuIconType iconType;
        private final int size;

        FooterSmallIcon(MenuIconType iconType, int size) {
            this.iconType = iconType;
            this.size = size;
            setOpaque(false);
            setPreferredSize(new Dimension(size + 6, size + 6));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawMenuIcon(g, iconType, 3, 3, size, GOLD_LIGHT);
            g.dispose();
        }
    }

    private static final class FooterDriveImagePanel extends JPanel {

        private final BufferedImage image;

        FooterDriveImagePanel(BufferedImage image) {
            this.image = image;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            int w = getWidth();
            int h = getHeight();

            RoundRectangle2D card = new RoundRectangle2D.Double(0, 2, w - 1, h - 5, 15, 15);
            g.setClip(card);
            g.setColor(new Color(1, 4, 8));
            g.fill(card);
            if (image != null) {
                g.drawImage(image, 0, 2, w, h - 5, null);
            }

            g.setClip(null);
            g.setColor(new Color(214, 168, 91, 28));
            g.draw(card);
            g.dispose();
        }
    }

    private static final class WelcomeToast extends JPanel {
        private final String message;
        private float alpha = 1f;

        WelcomeToast(String message) {
            this.message = message;
            setOpaque(false);
        }

        void setAlpha(float alpha) {
            this.alpha = Math.max(0f, Math.min(1f, alpha));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setComposite(AlphaComposite.SrcOver.derive(alpha));
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            RoundRectangle2D body = new RoundRectangle2D.Double(0, 0, w - 1, h - 1, 18, 18);

            g.setPaint(new LinearGradientPaint(
                    0, 0, w, h,
                    new float[]{0f, .55f, 1f},
                    new Color[]{
                            new Color(13, 18, 25, 242),
                            new Color(8, 12, 18, 238),
                            new Color(93, 58, 23, 226)
                    }
            ));
            g.fill(body);

            g.setColor(new Color(214, 168, 91, 115));
            g.draw(body);

            g.setColor(new Color(214, 168, 91, 24));
            g.fillOval(w - 72, -36, 112, 112);

            g.setStroke(new BasicStroke(1.35f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawMenuIcon(g, MenuIconType.USER, 18, 20, 23, GOLD_LIGHT);

            g.setColor(TEXT);
            g.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g.drawString("Velora Motors", 54, 25);

            g.setColor(new Color(224, 229, 236));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            FontMetrics fm = g.getFontMetrics();
            String shown = message;
            while (fm.stringWidth(shown) > w - 70 && shown.length() > 10) {
                shown = shown.substring(0, shown.length() - 2) + "…";
            }
            g.drawString(shown, 54, 45);
            g.dispose();
        }
    }

    private static final class CarHeadLightIcon extends JPanel {
        CarHeadLightIcon(int w, int h) {
            setOpaque(false);
            setPreferredSize(new Dimension(w, h));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g.setColor(new Color(8, 12, 17, 160));
            g.fillRoundRect(2, 8, w - 4, h - 14, 20, 20);

            g.setColor(new Color(25, 30, 36, 200));
            g.setStroke(new BasicStroke(1.2f));
            Path2D car = new Path2D.Double();
            car.moveTo(8, h - 12);
            car.curveTo(w * 0.25, 4, w * 0.62, 4, w - 8, h - 12);
            g.draw(car);

            g.setColor(new Color(240, 245, 255, 220));
            g.fillRoundRect(w - 38, h - 20, 18, 4, 4, 4);
            g.fillRoundRect(w - 17, h - 19, 10, 3, 3, 3);

            g.setComposite(AlphaComposite.SrcOver.derive(0.35f));
            g.setColor(new Color(200, 225, 255));
            g.fillPolygon(new int[]{w - 38, w - 3, w - 3, w - 38}, new int[]{h - 20, h - 30, h - 15, h - 16}, 4);

            g.dispose();
        }
    }

    private static void drawMenuIcon(Graphics2D g, MenuIconType type, int x, int y, int s, Color color) {
        g.setColor(color);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        switch (type) {
            case HOME -> {
                Path2D roof = new Path2D.Double();
                roof.moveTo(x + s / 2.0, y);
                roof.lineTo(x, y + s / 2.0);
                roof.lineTo(x + 3, y + s / 2.0);
                roof.lineTo(x + 3, y + s);
                roof.lineTo(x + s - 3, y + s);
                roof.lineTo(x + s - 3, y + s / 2.0);
                roof.lineTo(x + s, y + s / 2.0);
                roof.closePath();
                g.draw(roof);
            }
            case CAR -> {
                RoundRectangle2D body = new RoundRectangle2D.Double(x + 2, y + 6, s - 4, 7, 4, 4);
                g.draw(body);
                g.drawLine(x + 5, y + 6, x + 8, y + 2);
                g.drawLine(x + s - 5, y + 6, x + s - 8, y + 2);
                g.draw(new Ellipse2D.Double(x + 3, y + 11, 4, 4));
                g.draw(new Ellipse2D.Double(x + s - 7, y + 11, 4, 4));
            }
            case CALENDAR -> {
                g.draw(new RoundRectangle2D.Double(x + 1, y + 2, s - 2, s - 3, 4, 4));
                g.drawLine(x + 1, y + 7, x + s - 1, y + 7);
                g.drawLine(x + 5, y, x + 5, y + 5);
                g.drawLine(x + s - 5, y, x + s - 5, y + 5);
            }
            case FILE -> {
                Path2D file = new Path2D.Double();
                file.moveTo(x + 3, y + 1);
                file.lineTo(x + s - 6, y + 1);
                file.lineTo(x + s - 2, y + 5);
                file.lineTo(x + s - 2, y + s - 1);
                file.lineTo(x + 3, y + s - 1);
                file.closePath();
                g.draw(file);
                g.drawLine(x + 6, y + 8, x + s - 6, y + 8);
                g.drawLine(x + 6, y + 11, x + s - 6, y + 11);
            }
            case DIAMOND -> {
                Path2D d = new Path2D.Double();
                d.moveTo(x + s / 2.0, y);
                d.lineTo(x + s - 1, y + s / 2.0);
                d.lineTo(x + s / 2.0, y + s);
                d.lineTo(x, y + s / 2.0);
                d.closePath();
                g.draw(d);
            }
            case STAR -> {
                double cx = x + s / 2.0;
                double cy = y + s / 2.0;
                int r1 = s / 2;
                int r2 = s / 4;
                Path2D star = new Path2D.Double();
                for (int i = 0; i < 10; i++) {
                    double ang = -Math.PI / 2 + i * Math.PI / 5;
                    double r = (i % 2 == 0) ? r1 : r2;
                    double px = cx + Math.cos(ang) * r;
                    double py = cy + Math.sin(ang) * r;
                    if (i == 0) star.moveTo(px, py);
                    else star.lineTo(px, py);
                }
                star.closePath();
                g.draw(star);
            }
            case USER -> {
                g.draw(new Ellipse2D.Double(x + 5, y + 1, 8, 8));
                Arc2D arc = new Arc2D.Double(x + 2, y + 7, 14, 11, 0, 180, Arc2D.OPEN);
                g.draw(arc);
            }
            case HEADSET -> {
                Arc2D top = new Arc2D.Double(x + 2, y + 1, 14, 12, 0, 180, Arc2D.OPEN);
                g.draw(top);
                g.draw(new RoundRectangle2D.Double(x + 1, y + 8, 4, 6, 3, 3));
                g.draw(new RoundRectangle2D.Double(x + s - 5, y + 8, 4, 6, 3, 3));
                g.drawLine(x + s - 3, y + 14, x + s - 1, y + 16);
                g.drawLine(x + s - 1, y + 16, x + s - 6, y + 16);
            }
        }
    }

    private final class SearchPanel extends JPanel {
        private final JTextField field;

        SearchPanel(String placeholder) {
            setOpaque(false);
            setLayout(null);

            field = new JTextField();
            field.setText("");
            field.setForeground(TEXT);
            field.setBackground(new Color(0, 0, 0, 0));
            field.setCaretColor(GOLD_LIGHT);
            field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            field.setBorder(new EmptyBorder(0, 0, 0, 0));
            field.setOpaque(false);
            field.setVisible(false);
            field.setToolTipText("Type and press Enter to search");

            field.putClientProperty("placeholder", placeholder);

            field.addActionListener(e -> searchVehicles(field.getText()));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    String query = JOptionPane.showInputDialog(
                            CustomerDashboard.this,
                            "Search for BMW cars...",
                            "Velora Motors Search",
                            JOptionPane.PLAIN_MESSAGE
                    );
                    if (query != null) {
                        field.setText(query.trim());
                        repaint();
                        searchVehicles(query);
                    }
                }
            });

            add(field, BorderLayout.CENTER);
        }

        @Override
        public void doLayout() {
            int inset = 58;
            field.setBounds(inset, 0, Math.max(0, getWidth() - inset * 2), getHeight());
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = Math.min(34, Math.max(24, getHeight() - 8));
            RoundRectangle2D box = new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g.setColor(new Color(1, 4, 8, 232));
            g.fill(box);

            g.setColor(new Color(214, 168, 91, 105));
            g.setStroke(new BasicStroke(1.45f));
            g.draw(box);

            int midY = getHeight() / 2;
            g.setColor(new Color(249, 199, 124));
            g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Ellipse2D.Double(22, midY - 10, 18, 18));
            g.drawLine(38, midY + 6, 47, midY + 15);
            g.setColor(new Color(242, 246, 252, 230));
            g.draw(new Ellipse2D.Double(getWidth() - 47, midY - 10, 18, 18));
            g.drawLine(getWidth() - 31, midY + 6, getWidth() - 22, midY + 15);

            if (field.getText().isEmpty()) {
                g.setColor(new Color(206, 211, 220));
                g.setFont(new Font("Segoe UI", Font.PLAIN, Math.max(14, Math.min(18, getHeight() / 3))));
                Object ph = field.getClientProperty("placeholder");
                FontMetrics fm = g.getFontMetrics();
                g.drawString(String.valueOf(ph), 58, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            } else {
                g.setColor(TEXT);
                g.setFont(new Font("Segoe UI", Font.PLAIN, Math.max(14, Math.min(18, getHeight() / 3))));
                FontMetrics fm = g.getFontMetrics();
                g.drawString(field.getText(), 58, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            }

            g.dispose();
            super.paintComponent(raw);
        }
    }

    private final class ButtonOverlayImageCard extends ImageCard {
        ButtonOverlayImageCard(
                BufferedImage image,
                boolean cover,
                double rx,
                double ry,
                double rw,
                double rh,
                Runnable action
        ) {
            super(image, cover);
            setLayout(null);

            JButton hotButton = new JButton();
            hotButton.setOpaque(false);
            hotButton.setContentAreaFilled(false);
            hotButton.setBorderPainted(false);
            hotButton.setFocusPainted(false);
            hotButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            hotButton.addActionListener(e -> action.run());
            hotButton.setToolTipText("Click");

            add(hotButton);

            addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentResized(java.awt.event.ComponentEvent e) {
                    int x = (int) Math.round(getWidth() * rx);
                    int y = (int) Math.round(getHeight() * ry);
                    int w = (int) Math.round(getWidth() * rw);
                    int h = (int) Math.round(getHeight() * rh);
                    hotButton.setBounds(x, y, w, h);
                }
            });
        }
    }

    private static final class TopBadgeIcon extends JPanel {
        private final BadgeIconType type;
        private final int count;

        TopBadgeIcon(BadgeIconType type, int count) {
            this.type = type;
            this.count = count;
            setOpaque(false);
            setPreferredSize(new Dimension(42, 42));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(GOLD_LIGHT);

            if (type == BadgeIconType.BELL) {
                drawBell(g, 10, 8, 20);
            } else {
                drawMail(g, 8, 10, 22, 16);
            }

            if (count > 0) {
                g.setColor(new Color(238, 145, 45));
                g.fillOval(24, 0, 18, 18);

                g.setColor(Color.BLACK);
                g.setFont(new Font("Segoe UI", Font.BOLD, 10));

                String s = String.valueOf(count);
                FontMetrics fm = g.getFontMetrics();
                g.drawString(s, 24 + (18 - fm.stringWidth(s)) / 2, 13);
            }

            g.dispose();
        }

        private void drawBell(Graphics2D g, int x, int y, int s) {
            Path2D bell = new Path2D.Double();
            bell.moveTo(x + 4, y + s - 2);
            bell.lineTo(x + 4, y + 10);
            bell.curveTo(x + 4, y + 4, x + 8, y + 1, x + s / 2.0, y + 1);
            bell.curveTo(x + s - 8, y + 1, x + s - 4, y + 4, x + s - 4, y + 10);
            bell.lineTo(x + s - 4, y + s - 2);
            bell.closePath();
            g.draw(bell);
            g.drawLine(x + 2, y + s - 2, x + s - 2, y + s - 2);
            g.draw(new Ellipse2D.Double(x + s / 2.0 - 2, y + s - 2, 4, 4));
        }

        private void drawMail(Graphics2D g, int x, int y, int w, int h) {
            RoundRectangle2D body = new RoundRectangle2D.Double(x, y, w, h, 4, 4);
            g.draw(body);
            g.drawLine(x, y, x + w / 2, y + h / 2);
            g.drawLine(x + w, y, x + w / 2, y + h / 2);
        }
    }

    private static final class AvatarIcon extends JPanel {
        AvatarIcon(int size) {
            setOpaque(false);
            setPreferredSize(new Dimension(size, size));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int s = Math.min(getWidth(), getHeight()) - 2;
            int x = (getWidth() - s) / 2;
            int y = (getHeight() - s) / 2;

            Ellipse2D circle = new Ellipse2D.Double(x, y, s, s);

            g.setColor(new Color(6, 12, 19));
            g.fill(circle);

            g.setStroke(new BasicStroke(1.4f));
            g.setColor(GOLD);
            g.draw(circle);

            Shape old = g.getClip();
            g.setClip(circle);

            g.setColor(new Color(228, 175, 130));
            g.fillOval(x + s / 2 - s / 8, y + s / 6, s / 4, s / 4);

            g.setColor(new Color(18, 20, 24));
            g.fillArc(x + s / 2 - s / 7, y + s / 7, s / 3, s / 4, 0, 180);

            Path2D suit = new Path2D.Double();
            suit.moveTo(x + s * 0.23, y + s);
            suit.lineTo(x + s * 0.34, y + s * 0.57);
            suit.lineTo(x + s * 0.50, y + s * 0.72);
            suit.lineTo(x + s * 0.66, y + s * 0.57);
            suit.lineTo(x + s * 0.77, y + s);
            suit.closePath();

            g.setColor(new Color(18, 20, 25));
            g.fill(suit);

            Path2D shirt = new Path2D.Double();
            shirt.moveTo(x + s * 0.41, y + s * 0.59);
            shirt.lineTo(x + s * 0.50, y + s * 0.78);
            shirt.lineTo(x + s * 0.59, y + s * 0.59);
            shirt.closePath();

            g.setColor(new Color(240, 240, 235));
            g.fill(shirt);

            Path2D tie = new Path2D.Double();
            tie.moveTo(x + s * 0.48, y + s * 0.62);
            tie.lineTo(x + s * 0.52, y + s * 0.62);
            tie.lineTo(x + s * 0.55, y + s * 0.86);
            tie.lineTo(x + s * 0.50, y + s * 0.94);
            tie.lineTo(x + s * 0.45, y + s * 0.86);
            tie.closePath();

            g.setColor(GOLD);
            g.fill(tie);

            g.setClip(old);
            g.dispose();
        }
    }


    private static final class LogoImagePanel extends JPanel {
        private final BufferedImage image;

        LogoImagePanel(BufferedImage image, int w, int h) {
            this.image = image;
            setOpaque(false);
            setPreferredSize(new Dimension(w, h));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            if (image != null) {
                int availW = getWidth();
                int availH = getHeight();

                double scale = Math.min(
                        availW / (double) image.getWidth(),
                        availH / (double) image.getHeight()
                );

                int drawW = Math.max(1, (int) Math.round(image.getWidth() * scale));
                int drawH = Math.max(1, (int) Math.round(image.getHeight() * scale));

                int x = (availW - drawW) / 2;
                int y = (availH - drawH) / 2;

                g.drawImage(image, x, y, drawW, drawH, null);
            } else {
                drawFallback(g);
            }

            g.dispose();
        }

        private void drawFallback(Graphics2D g) {
            g.setColor(GOLD);
            g.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int cx = getWidth() / 2;
            int cy = getHeight() / 2 + 8;
            int wing = Math.min(getWidth() / 2 - 8, 42);

            Path2D p = new Path2D.Double();

            p.moveTo(cx, cy);
            p.lineTo(cx - wing / 2.0, cy - 28);
            p.lineTo(cx - wing, cy - 28);

            p.moveTo(cx, cy);
            p.lineTo(cx + wing / 2.0, cy - 28);
            p.lineTo(cx + wing, cy - 28);

            p.moveTo(cx - 17, cy - 18);
            p.lineTo(cx - wing + 12, cy - 18);

            p.moveTo(cx + 17, cy - 18);
            p.lineTo(cx + wing - 12, cy - 18);

            g.draw(p);
        }
    }

    private static final class WingsLogo extends JPanel {
        WingsLogo(int w, int h) {
            setOpaque(false);
            setPreferredSize(new Dimension(w, h));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(GOLD);

            int cx = getWidth() / 2;
            int cy = getHeight() / 2 + 8;
            int wing = Math.min(getWidth() / 2 - 8, 42);

            Path2D p = new Path2D.Double();
            p.moveTo(cx, cy);
            p.lineTo(cx - wing / 2.0, cy - 28);
            p.lineTo(cx - wing, cy - 28);

            p.moveTo(cx, cy);
            p.lineTo(cx + wing / 2.0, cy - 28);
            p.lineTo(cx + wing, cy - 28);

            p.moveTo(cx - 17, cy - 18);
            p.lineTo(cx - wing + 12, cy - 18);

            p.moveTo(cx + 17, cy - 18);
            p.lineTo(cx + wing - 12, cy - 18);

            g.draw(p);
            g.dispose();
        }
    }
}
