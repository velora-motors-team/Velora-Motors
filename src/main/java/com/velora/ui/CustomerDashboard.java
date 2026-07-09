
package com.velora.ui;

import com.velora.authentication.Customer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pageCards = new JPanel(pageLayout);

    private final BufferedImage iconImage;
    private final BufferedImage heroImage;
    private final BufferedImage rentalCard1;
    private final BufferedImage rentalCard2;
    private final BufferedImage rentalCard3;
    private final BufferedImage sidebarCar;

    public CustomerDashboard(Customer customer) {
        super("Velora Motors - Customer Dashboard");
        this.customer = customer;

        iconImage = loadImage("/images/icon.png");
        heroImage = loadImage("/images/HERO_CENTER.png");
        rentalCard1 = loadImage("/images/RENTAL_CARD_1.png");
        rentalCard2 = loadImage("/images/RENTAL_CARD_2.png");
        rentalCard3 = loadImage("/images/RENTAL_CARD_3.png");
        sidebarCar = loadImage("/images/SIDEBAR_CAR.png");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1240, 760));
        setSize(1480, 860);
        setLocationRelativeTo(null);

        if (iconImage != null) {
            setIconImage(iconImage);
        }

        setContentPane(createRoot());
    }

    private JPanel createRoot() {
        JPanel root = new GradientRoot();
        root.setLayout(new BorderLayout());
        root.add(createSidebar(), BorderLayout.WEST);
        root.add(createMain(), BorderLayout.CENTER);
        return root;
    }

    private JPanel createSidebar() {
        RoundedPanel sidebar = new RoundedPanel(24);
        sidebar.setPreferredSize(new Dimension(255, 760));
        sidebar.setBackground(new Color(4, 8, 13, 245));
        sidebar.setLayout(new BorderLayout());
        sidebar.setBorder(new EmptyBorder(20, 15, 15, 15));

        JPanel top = new JPanel(new BorderLayout(0, 20));
        top.setOpaque(false);

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));

        LogoImagePanel wingsLogo = new LogoImagePanel(iconImage, 124, 72);
        wingsLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel name = new JLabel("VELORA MOTORS");
        name.setForeground(TEXT);
        name.setFont(new Font("Serif", Font.PLAIN, 24));
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

        JPanel menu = new JPanel(new GridLayout(8, 1, 0, 10));
        menu.setOpaque(false);

        menu.add(menuButton(MenuIconType.HOME, "Dashboard", true, () -> showSection("Dashboard")));
        menu.add(menuButton(MenuIconType.CAR, "Vehicles", false, () -> showMessage("Vehicles catalog will open here.")));
        menu.add(menuButton(MenuIconType.CALENDAR, "My Rentals", false, () -> showSection("My Rentals")));
        menu.add(menuButton(MenuIconType.FILE, "Billing & Invoices", false, () -> showSection("Billing & Invoices")));
        menu.add(menuButton(MenuIconType.DIAMOND, "Loyalty Points", false, () -> showSection("Loyalty Points")));
        menu.add(menuButton(MenuIconType.STAR, "Reviews", false, () -> showSection("Reviews")));
        menu.add(menuButton(MenuIconType.USER, "Profile", false, () -> showSection("Profile")));
        menu.add(menuButton(MenuIconType.HEADSET, "Support", false, () -> showSection("Support")));

        top.add(brand, BorderLayout.NORTH);
        top.add(menu, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(0, 14));
        bottom.setOpaque(false);

        JButton logout = outlineButton("Logout");
        logout.addActionListener(e -> logout());

        ImageCard sidebarPromo = new ImageCard(sidebarCar, false);
        sidebarPromo.setPreferredSize(new Dimension(220, 240));
        sidebarPromo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sidebarPromo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showMessage("Explore vehicles collection.");
            }
        });

        bottom.add(logout, BorderLayout.NORTH);
        bottom.add(sidebarPromo, BorderLayout.CENTER);

        sidebar.add(top, BorderLayout.CENTER);
        sidebar.add(bottom, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel createMain() {
        JPanel main = new JPanel(new BorderLayout(0, 14));
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(16, 18, 12, 18));

        main.add(createTopBar(), BorderLayout.NORTH);

        pageCards.setOpaque(false);
        pageCards.add(wrapPage(createDashboardContent()), "Dashboard");
        pageCards.add(wrapPage(new MyRentalsPanel(customer)), "My Rentals");
        pageCards.add(wrapPage(new CustomerBillingPanel(customer)), "Billing & Invoices");
        pageCards.add(wrapPage(new SupportPanel(customer)), "Support");
        pageCards.add(wrapPage(new ReviewsPanel(customer)), "Reviews");
        pageCards.add(wrapPage(new LoyaltyPointsPanel(customer)), "Loyalty Points");
        pageCards.add(
                wrapPage(new ProfilePanel(
                        customer,
                        () -> showSection("My Rentals")
                )),
                "Profile"
        );

        main.add(pageCards, BorderLayout.CENTER);
        return main;
    }

    private JComponent createDashboardContent() {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel hero = createHero();
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel sectionHeader = createRentalsSectionHeader();
        sectionHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel cards = createCardsGrid();
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel footer = createFooter();
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(hero);
        content.add(Box.createVerticalStrut(18));
        content.add(sectionHeader);
        content.add(Box.createVerticalStrut(10));
        content.add(cards);
        content.add(Box.createVerticalStrut(18));
        content.add(footer);
        content.add(Box.createVerticalStrut(10));

        return content;
    }

    private JPanel createRentalsSectionHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(1120, 42));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("YOUR ACTIVE RENTALS");
        title.setForeground(TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));

        JLabel subtitle = new JLabel("Manage your current BMW rental experience");
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        left.add(title);
        left.add(Box.createVerticalStrut(2));
        left.add(subtitle);

        RoundedButton viewAll = new RoundedButton("View All Rentals  →", 14);
        viewAll.setPreferredSize(new Dimension(145, 34));
        viewAll.setBackground(new Color(214, 168, 91, 22));
        viewAll.setForeground(GOLD_LIGHT);
        viewAll.setFont(new Font("Segoe UI", Font.BOLD, 11));
        viewAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewAll.addActionListener(e -> showSection("My Rentals"));

        header.add(left, BorderLayout.WEST);
        header.add(viewAll, BorderLayout.EAST);

        return header;
    }

    private JScrollPane wrapPage(JComponent page) {
        JScrollPane scroll = new JScrollPane(page);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getHorizontalScrollBar().setUnitIncrement(18);
        return scroll;
    }

    private void showSection(String section) {
        pageLayout.show(pageCards, section);
    }

    private JPanel createTopBar() {
        JPanel top = new JPanel(new BorderLayout(16, 0));
        top.setOpaque(false);

        SearchPanel search = new SearchPanel("Search for BMW cars...");
        search.setPreferredSize(new Dimension(540, 46));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
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

        top.add(Box.createHorizontalStrut(240), BorderLayout.WEST);
        top.add(search, BorderLayout.CENTER);
        top.add(right, BorderLayout.EAST);

        return top;
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

    private JPanel createHero() {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        ButtonOverlayImageCard hero = new ButtonOverlayImageCard(
                heroImage,
                false,
                0.43,
                0.34,
                0.23,
                0.16,
                () -> searchVehicles("BMW")
        );
        hero.setPreferredSize(new Dimension(1120, 315));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 315));

        JPanel heroHolder = new JPanel(new BorderLayout());
        heroHolder.setOpaque(false);
        heroHolder.setPreferredSize(new Dimension(1120, 315));
        heroHolder.setMaximumSize(new Dimension(Integer.MAX_VALUE, 315));
        heroHolder.add(hero, BorderLayout.CENTER);

        JPanel stats = createStats();
        stats.setPreferredSize(new Dimension(1120, 82));
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));

        wrapper.add(heroHolder);
        wrapper.add(Box.createVerticalStrut(12));
        wrapper.add(stats);

        return wrapper;
    }

    private JPanel createStats() {
        RoundedPanel strip = new RoundedPanel(18);
        strip.setBackground(new Color(3, 7, 12, 238));
        strip.setLayout(new GridLayout(1, 4, 0, 0));
        strip.setBorder(new EmptyBorder(10, 18, 10, 18));

        strip.add(statCard(MenuIconType.CAR, "TOTAL RENTALS", "12"));
        strip.add(statCard(MenuIconType.CALENDAR, "ACTIVE RENTALS", "2"));
        strip.add(statCard(MenuIconType.DIAMOND, "LOYALTY POINTS", "2,450"));
        strip.add(statCard(MenuIconType.FILE, "TOTAL SPENT", "$24,560"));

        return strip;
    }

    private JPanel statCard(MenuIconType iconType, String label, String value) {
        JPanel card = new JPanel(new BorderLayout(14, 0));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(4, 18, 4, 18));

        StatIcon icon = new StatIcon(iconType, 32);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel labelText = new JLabel(label);
        labelText.setForeground(new Color(185, 190, 198));
        labelText.setFont(new Font("Segoe UI", Font.BOLD, 10));
        labelText.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueText = new JLabel(value);
        valueText.setForeground(TEXT);
        valueText.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 21));
        valueText.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(Box.createVerticalGlue());
        textPanel.add(labelText);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(valueText);
        textPanel.add(Box.createVerticalGlue());

        card.add(icon, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createCardsGrid() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(1120, 205));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 205));

        JPanel grid = new JPanel(new GridLayout(1, 3, 14, 0));
        grid.setOpaque(false);

        grid.add(clickableCard(rentalCard1, "BMW X7 xDrive40i"));
        grid.add(clickableCard(rentalCard2, "BMW M8 Competition"));
        grid.add(clickableCard(rentalCard3, "BMW i7 M70"));

        wrapper.add(grid, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createFooter() {
        RoundedPanel footer = new RoundedPanel(10);
        footer.setPreferredSize(new Dimension(1120, 76));
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        footer.setBackground(new Color(3, 6, 10, 245));
        footer.setLayout(new GridBagLayout());
        footer.setBorder(new EmptyBorder(9, 20, 9, 16));

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
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel text = new JLabel("<html>DRIVE LUXURY.<br>LIVE EXCELLENCE.</html>");
        text.setForeground(TEXT);
        text.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(text, BorderLayout.WEST);

        CarHeadLightIcon car = new CarHeadLightIcon(90, 42);
        panel.add(car, BorderLayout.EAST);

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
        b.setPreferredSize(new Dimension(220, 44));
        b.setBackground(new Color(0, 0, 0, 0));
        b.setForeground(GOLD_LIGHT);
        b.setFont(new Font("Segoe UI", Font.BOLD, 15));
        b.setBorder(new EmptyBorder(0, 0, 0, 0));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void logout() {
        new LoginScreen().setVisible(true);
        dispose();
    }

    private void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Velora Motors", JOptionPane.INFORMATION_MESSAGE);
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

    private enum MenuIconType {
        HOME, CAR, CALENDAR, FILE, DIAMOND, STAR, USER, HEADSET
    }

    private enum BadgeIconType {
        BELL, MAIL
    }

    private static final class GradientRoot extends JPanel {
        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setPaint(new GradientPaint(0, 0, new Color(2, 5, 9), getWidth(), getHeight(), new Color(7, 11, 16)));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setComposite(AlphaComposite.SrcOver.derive(0.18f));
            g.setColor(GOLD);
            g.fillOval(getWidth() - 300, -190, 450, 350);

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
            super.paintComponent(raw);
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
            setLayout(new BorderLayout());

            field = new JTextField();
            field.setText("");
            field.setForeground(TEXT);
            field.setCaretColor(GOLD_LIGHT);
            field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            field.setBorder(new EmptyBorder(0, 48, 0, 42));
            field.setOpaque(false);
            field.setToolTipText("Type and press Enter to search");

            field.putClientProperty("placeholder", placeholder);

            field.addActionListener(e -> searchVehicles(field.getText()));

            add(field, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            RoundRectangle2D box = new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
            g.setColor(new Color(2, 5, 9, 210));
            g.fill(box);

            g.setColor(new Color(255, 255, 255, 28));
            g.draw(box);

            g.setColor(GOLD_LIGHT);
            g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Ellipse2D.Double(18, 14, 12, 12));
            g.drawLine(28, 24, 34, 30);

            if (field.getText().isEmpty() && !field.hasFocus()) {
                g.setColor(MUTED);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                Object ph = field.getClientProperty("placeholder");
                g.drawString(String.valueOf(ph), 48, 29);
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
