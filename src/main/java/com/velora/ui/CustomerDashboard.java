
package com.velora.ui;

import com.velora.authentication.Customer;
import com.velora.repository.NotificationRepository;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CustomerDashboard extends JFrame {

    private static final Color GOLD = new Color(214, 168, 91);
    private static final Color GOLD_LIGHT = new Color(238, 199, 140);
    private static final Color TEXT = new Color(245, 245, 245);
    private static final Color MUTED = new Color(170, 176, 186);
    private static final Color LINE = new Color(214, 168, 91, 75);

    private final Customer customer;
    private final CustomerAccountState accountState;
    private final NotificationRepository notificationRepository = new NotificationRepository();
    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pageCards = new JPanel(pageLayout);
    private final Map<String, MenuButton> menuButtons = new LinkedHashMap<>();
    private VehicleCatalog vehicleCatalog;
    private SupportPanel supportPanel;
    private JLabel totalRentalsValue;
    private JLabel activeRentalsValue;
    private JLabel loyaltyPointsValue;
    private JLabel totalSpentValue;
    private TopBadgeIcon notificationBell;
    private TopBadgeIcon mailIcon;
    private MyRentalsPanel myRentalsPanel;
    private LoyaltyPointsPanel loyaltyPointsPanel;

    private final BufferedImage iconImage;
    private final BufferedImage heroImage;
    private final BufferedImage rentalCard1;
    private final BufferedImage rentalCard2;
    private final BufferedImage rentalCard3;

    public CustomerDashboard(Customer customer) {
        super("Velora Motors - Customer Dashboard");
        this.customer = customer;
        this.accountState = CustomerAccountState.forCustomer(customer);

        iconImage = loadImage("/images/icon.png");
        heroImage = loadImage("/images/customer-dashboard-hero-reference.png");
        rentalCard1 = loadImage("/images/customer-rental-x7-custom.png");
        rentalCard2 = loadImage("/images/customer-rental-m8-custom.png");
        rentalCard3 = loadImage("/images/customer-rental-i7-custom.png");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setMinimumSize(new Dimension(1240, 760));
        setSize(1480, 860);
        setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        if (iconImage != null) {
            setIconImage(iconImage);
        }

        setContentPane(createRoot());
        accountState.addChangeListener(this::refreshCustomerMetrics);
        refreshCustomerMetrics();
    }

    public void showVehicleCatalog() {
        showSection("Vehicles");
    }

    public void showAvailabilityToast() {
        if (customer == null) {
            return;
        }
        List<NotificationRepository.NotificationRecord> available = notificationRepository
                .findUnreadByCustomerEmail(customer.getEmail()).stream()
                .filter(notification -> "VEHICLE_AVAILABLE".equalsIgnoreCase(notification.type()))
                .sorted(Comparator.comparing(
                        NotificationRepository.NotificationRecord::createdAt,
                        Comparator.reverseOrder()
                ))
                .toList();
        if (!available.isEmpty()) {
            new AvailabilityToast(available.get(0), available.size()).showToast();
        }
    }

    private JPanel createRoot() {
        JPanel root = new GradientRoot();
        root.setLayout(new BorderLayout());
        root.add(createSidebar(), BorderLayout.WEST);
        root.add(createMain(), BorderLayout.CENTER);
        root.add(new CustomerFooter(), BorderLayout.SOUTH);
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
        menu.add(menuButton(MenuIconType.CAR, "Vehicles", false, () -> showSection("Vehicles")));
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

        bottom.add(logout, BorderLayout.NORTH);

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
        vehicleCatalog = new VehicleCatalog(customer);
        supportPanel = new SupportPanel(customer);
        pageCards.add(vehicleCatalog, "Vehicles");
        myRentalsPanel = new MyRentalsPanel(customer);
        loyaltyPointsPanel = new LoyaltyPointsPanel(customer);
        pageCards.add(wrapPage(myRentalsPanel), "My Rentals");
        pageCards.add(wrapPage(new CustomerBillingPanel(customer)), "Billing & Invoices");
        pageCards.add(wrapPage(supportPanel), "Support");
        pageCards.add(wrapPage(new ReviewsPanel(customer)), "Reviews");
        pageCards.add(wrapPage(loyaltyPointsPanel), "Loyalty Points");
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
        JPanel content = new WidthTrackingPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(0, 0, 12, 0));

        JPanel hero = createHero();
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel sectionHeader = createRentalsSectionHeader();
        sectionHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel cards = createCardsGrid();
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel actions = createActionStrip();
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(hero);
        content.add(Box.createVerticalStrut(18));
        content.add(sectionHeader);
        content.add(Box.createVerticalStrut(12));
        content.add(cards);
        content.add(Box.createVerticalStrut(16));
        content.add(actions);
        content.add(Box.createVerticalGlue());

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
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        return scroll;
    }

    private void showSection(String section) {
        if ("Vehicles".equals(section) && vehicleCatalog != null) {
            vehicleCatalog.refreshData();
        } else if ("Support".equals(section) && supportPanel != null) {
            supportPanel.refreshData();
        } else if ("My Rentals".equals(section) && myRentalsPanel != null) {
            myRentalsPanel.refreshData();
        } else if ("Loyalty Points".equals(section) && loyaltyPointsPanel != null) {
            loyaltyPointsPanel.refreshData();
        }
        refreshCustomerMetrics();
        refreshNotificationBadge();
        pageLayout.show(pageCards, section);
        setActiveMenu(section);
    }

    private void setActiveMenu(String section) {
        for (Map.Entry<String, MenuButton> entry : menuButtons.entrySet()) {
            entry.getValue().setActive(entry.getKey().equals(section));
        }
    }

    private JPanel createTopBar() {
        JPanel top = new JPanel(new BorderLayout(16, 0));
        top.setOpaque(false);

        SearchPanel search = new SearchPanel("Search for BMW cars, models, or features...");
        search.setPreferredSize(new Dimension(540, 46));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);

        notificationBell = new TopBadgeIcon(BadgeIconType.BELL, unreadNotificationCount());
        notificationBell.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showNotificationInbox();
            }
        });

        mailIcon = new TopBadgeIcon(BadgeIconType.MAIL, unreadMailCount());
        mailIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showInbox(true);
            }
        });

        right.add(notificationBell);
        right.add(mailIcon);
        right.add(createProfileBlock());
        right.add(createWindowControls());

        top.add(search, BorderLayout.CENTER);
        top.add(right, BorderLayout.EAST);

        return top;
    }

    private int unreadNotificationCount() {
        if (customer == null || customer.getEmail() == null || customer.getEmail().isBlank()) {
            return 0;
        }
        return (int) notificationRepository.findUnreadByCustomerEmail(customer.getEmail()).stream()
                .filter(notification -> !"ADMIN_EMAIL".equalsIgnoreCase(notification.type()))
                .count();
    }

    private int unreadMailCount() {
        if (customer == null || customer.getEmail() == null || customer.getEmail().isBlank()) {
            return 0;
        }
        return (int) notificationRepository.findUnreadByCustomerEmail(customer.getEmail()).stream()
                .filter(notification -> "ADMIN_EMAIL".equalsIgnoreCase(notification.type()))
                .count();
    }

    private void refreshNotificationBadge() {
        if (notificationBell != null) {
            notificationBell.setCount(unreadNotificationCount());
        }
        if (mailIcon != null) {
            mailIcon.setCount(unreadMailCount());
        }
    }

    private void showNotificationInbox() {
        showInbox(false);
    }

    private void showInbox(boolean mailOnly) {
        if (customer == null) {
            VeloraNotificationDialog.showInfo(this, mailOnly ? "Inbox" : "Notifications", "No customer account is active.");
            return;
        }

        List<NotificationRepository.NotificationRecord> notifications = notificationRepository
                .findByCustomerEmail(customer.getEmail()).stream()
                .filter(notification -> mailOnly
                        == "ADMIN_EMAIL".equalsIgnoreCase(notification.type()))
                .sorted(Comparator.comparing(
                        NotificationRepository.NotificationRecord::createdAt,
                        Comparator.reverseOrder()
                ))
                .limit(6)
                .toList();

        if (notifications.isEmpty()) {
            VeloraNotificationDialog.showInfo(
                    this,
                    mailOnly ? "Admin Inbox" : "Notifications",
                    mailOnly ? "You have no messages from administration yet." : "You have no notifications yet."
            );
            refreshNotificationBadge();
            return;
        }

        JPanel list = new JPanel();
        list.setBackground(new Color(5, 12, 18));
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBorder(new EmptyBorder(10, 12, 10, 12));

        for (NotificationRepository.NotificationRecord notification : notifications) {
            JLabel title = new JLabel((notification.read() ? "" : "●  ") + notification.title());
            title.setForeground(notification.read() ? MUTED : GOLD_LIGHT);
            title.setFont(new Font("Segoe UI", Font.BOLD, 13));
            title.setAlignmentX(Component.LEFT_ALIGNMENT);

            JTextArea message = new JTextArea(notification.message());
            message.setEditable(false);
            message.setFocusable(false);
            message.setOpaque(false);
            message.setForeground(TEXT);
            message.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            message.setLineWrap(true);
            message.setWrapStyleWord(true);
            message.setMaximumSize(new Dimension(500, 48));
            message.setAlignmentX(Component.LEFT_ALIGNMENT);

            list.add(title);
            list.add(Box.createVerticalStrut(3));
            list.add(message);
            list.add(Box.createVerticalStrut(10));

            if (!notification.read()) {
                notificationRepository.markRead(notification.notificationId());
            }
        }

        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(540, 330));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(214, 168, 91, 90)));
        scroll.getViewport().setBackground(new Color(5, 12, 18));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JOptionPane.showMessageDialog(
                this,
                scroll,
                mailOnly ? "Velora Administration Inbox" : "Velora Notifications",
                JOptionPane.PLAIN_MESSAGE
        );

        refreshNotificationBadge();
    }

    private final class AvailabilityToast extends JWindow {
        private final NotificationRepository.NotificationRecord notification;
        private float opacity = 1f;

        AvailabilityToast(NotificationRepository.NotificationRecord notification, int total) {
            super(CustomerDashboard.this);
            this.notification = notification;
            setAlwaysOnTop(true);
            setFocusableWindowState(false);
            setBackground(new Color(0, 0, 0, 0));
            setSize(430, total > 1 ? 154 : 136);
            setContentPane(createToastContent(total));
        }

        void showToast() {
            Rectangle owner = CustomerDashboard.this.getBounds();
            setLocation(owner.x + owner.width - getWidth() - 28, owner.y + 78);
            setVisible(true);

            Timer stay = new Timer(5_000, event -> startFade());
            stay.setRepeats(false);
            stay.start();
        }

        private JComponent createToastContent(int total) {
            JPanel card = new JPanel(new BorderLayout(14, 0)) {
                @Override
                protected void paintComponent(Graphics raw) {
                    Graphics2D g = (Graphics2D) raw.create();
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(new Color(0, 0, 0, 105));
                    g.fillRoundRect(7, 8, getWidth() - 14, getHeight() - 14, 22, 22);
                    g.setPaint(new GradientPaint(0, 0, new Color(18, 29, 38), getWidth(), getHeight(), new Color(4, 10, 16)));
                    g.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, 20, 20);
                    g.setColor(new Color(214, 168, 91, 145));
                    g.drawRoundRect(0, 0, getWidth() - 9, getHeight() - 9, 20, 20);
                    g.dispose();
                }
            };
            card.setOpaque(false);
            card.setBorder(new EmptyBorder(15, 18, 17, 20));

            JLabel bell = new JLabel("🔔", SwingConstants.CENTER);
            bell.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
            bell.setPreferredSize(new Dimension(44, 44));
            card.add(bell, BorderLayout.WEST);

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            JLabel title = new JLabel("Vehicle Available");
            title.setForeground(GOLD_LIGHT);
            title.setFont(new Font("Segoe UI", Font.BOLD, 15));
            JTextArea message = new JTextArea(notification.message());
            message.setOpaque(false);
            message.setEditable(false);
            message.setFocusable(false);
            message.setForeground(TEXT);
            message.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            message.setLineWrap(true);
            message.setWrapStyleWord(true);
            text.add(title);
            text.add(Box.createVerticalStrut(5));
            text.add(message);
            if (total > 1) {
                JLabel more = new JLabel("+ " + (total - 1) + " more availability notification(s)");
                more.setForeground(MUTED);
                more.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                text.add(more);
            }
            card.add(text, BorderLayout.CENTER);

            JButton view = new JButton("View");
            view.setForeground(GOLD_LIGHT);
            view.setFont(new Font("Segoe UI", Font.BOLD, 11));
            view.setOpaque(false);
            view.setContentAreaFilled(false);
            view.setBorderPainted(false);
            view.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            view.addActionListener(event -> {
                dispose();
                showVehicleCatalog();
            });
            card.add(view, BorderLayout.EAST);
            return card;
        }

        private void startFade() {
            Timer fade = new Timer(45, null);
            fade.addActionListener(event -> {
                opacity -= 0.07f;
                if (opacity <= 0f) {
                    fade.stop();
                    dispose();
                    return;
                }
                try {
                    setOpacity(opacity);
                } catch (UnsupportedOperationException | IllegalComponentStateException ex) {
                    fade.stop();
                    dispose();
                }
            });
            fade.start();
        }
    }

    private JComponent createWindowControls() {
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        controls.setOpaque(false);

        WindowControlButton minimize = new WindowControlButton(WindowControlType.MINIMIZE);
        minimize.setToolTipText("Minimize");
        minimize.addActionListener(e -> setExtendedState(JFrame.ICONIFIED));

        WindowControlButton maximize = new WindowControlButton(WindowControlType.MAXIMIZE);
        maximize.setToolTipText("Maximize / Restore");
        maximize.addActionListener(e -> {
            int state = getExtendedState();
            if ((state & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
                setExtendedState(JFrame.NORMAL);
            } else {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
        });

        WindowControlButton close = new WindowControlButton(WindowControlType.CLOSE);
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

    private JPanel createHero() {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        ButtonOverlayImageCard hero = new ButtonOverlayImageCard(
                heroImage,
                true,
                0.035,
                0.74,
                0.155,
                0.13,
                () -> searchVehicles("BMW")
        );
        hero.setPreferredSize(new Dimension(1120, 292));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 292));

        JPanel heroHolder = new JPanel(new BorderLayout());
        heroHolder.setOpaque(false);
        heroHolder.setPreferredSize(new Dimension(1120, 292));
        heroHolder.setMaximumSize(new Dimension(Integer.MAX_VALUE, 292));
        heroHolder.add(hero, BorderLayout.CENTER);

        JPanel stats = createStats();
        stats.setPreferredSize(new Dimension(1120, 94));
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 94));

        wrapper.add(heroHolder);
        wrapper.add(Box.createVerticalStrut(12));
        wrapper.add(stats);

        return wrapper;
    }

    private JPanel createStats() {
        JPanel strip = new JPanel(new GridLayout(1, 4, 14, 0));
        strip.setOpaque(false);

        totalRentalsValue = statValueLabel();
        activeRentalsValue = statValueLabel();
        loyaltyPointsValue = statValueLabel();
        totalSpentValue = statValueLabel();

        strip.add(statCard(MenuIconType.CALENDAR, "TOTAL RENTALS", totalRentalsValue, "All time bookings",
                () -> showSection("My Rentals")));
        strip.add(statCard(MenuIconType.CALENDAR, "ACTIVE RENTALS", activeRentalsValue, "Currently ongoing",
                () -> showSection("My Rentals")));
        strip.add(statCard(MenuIconType.DIAMOND, "LOYALTY POINTS", loyaltyPointsValue, "Gold Member",
                () -> showSection("Loyalty Points")));
        strip.add(statCard(MenuIconType.FILE, "TOTAL SPENT", totalSpentValue, "Across all rentals",
                () -> showSection("Billing & Invoices")));

        return strip;
    }

    private JPanel statCard(MenuIconType iconType, String label, JLabel valueText, String subText, Runnable action) {
        RoundedPanel card = new RoundedPanel(16);
        card.setBackground(new Color(3, 7, 12, 232));
        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(new EmptyBorder(12, 16, 12, 14));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        StatIcon icon = new StatIcon(iconType, 32);
        RoundedPanel iconShell = new RoundedPanel(12);
        iconShell.setBackground(new Color(255, 255, 255, 16));
        iconShell.setPreferredSize(new Dimension(58, 58));
        iconShell.setLayout(new GridBagLayout());
        iconShell.add(icon);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel labelText = new JLabel(label);
        labelText.setForeground(new Color(185, 190, 198));
        labelText.setFont(new Font("Segoe UI", Font.BOLD, 10));
        labelText.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel(subText);
        sub.setForeground(MUTED);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(labelText);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(valueText);
        textPanel.add(Box.createVerticalStrut(1));
        textPanel.add(sub);

        JLabel arrow = new JLabel(">");
        arrow.setForeground(GOLD);
        arrow.setFont(new Font("Segoe UI", Font.PLAIN, 24));

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.run();
            }
        });

        card.add(iconShell, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        card.add(arrow, BorderLayout.EAST);

        return card;
    }

    private JLabel statValueLabel() {
        JLabel label = new JLabel();
        label.setForeground(TEXT);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void refreshCustomerMetrics() {
        if (totalRentalsValue == null) {
            return;
        }
        totalRentalsValue.setText(formatNumber(accountState.getTotalRentals()));
        activeRentalsValue.setText(formatNumber(accountState.getActiveRentals()));
        loyaltyPointsValue.setText(formatNumber(accountState.getLoyaltyPoints()));
        totalSpentValue.setText(formatMoney(accountState.getTotalSpent()));
    }

    private JPanel createCardsGrid() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(1120, 285));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 285));

        JPanel grid = new JPanel(new GridLayout(1, 3, 14, 0));
        grid.setOpaque(false);

        grid.add(rentalCard(
                rentalCard1,
                "BMW X7 xDrive40i",
                "Luxury SUV",
                "10 - 17 Jun 2026",
                "7 Days Left",
                "New York, NY"
        ));
        grid.add(rentalCard(
                rentalCard2,
                "BMW M8 Competition",
                "Performance Coupe",
                "01 - 07 Jun 2026",
                "2 Days Left",
                "Los Angeles, CA"
        ));
        grid.add(rentalCard(
                rentalCard3,
                "BMW i7 M70",
                "100% Electric Luxury Sedan",
                "05 - 12 Jun 2026",
                "5 Days Left",
                "Miami, FL"
        ));

        wrapper.add(grid, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createActionStrip() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(1120, 98));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 98));

        JPanel grid = new JPanel(new GridLayout(1, 3, 14, 0));
        grid.setOpaque(false);
        grid.add(actionCard(MenuIconType.STAR, "Gold Member Benefits",
                "You're enjoying premium benefits and exclusive privileges.",
                "View Benefits  ->",
                () -> showSection("Loyalty Points")));
        grid.add(actionCard(MenuIconType.DIAMOND, "Refer & Earn",
                "Invite friends and earn loyalty points when they rent.",
                "Refer Now  ->",
                () -> showMessage("Referral code: VELORA-"
                        + getCustomerName().trim().split("\\s+")[0].toUpperCase(Locale.US))));
        grid.add(actionCard(MenuIconType.HEADSET, "Need Assistance?",
                "Our support team is here to help you 24/7.",
                "Contact Support  ->",
                () -> showSection("Support")));

        wrapper.add(grid, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel actionCard(MenuIconType iconType, String title, String description, String buttonText, Runnable action) {
        RoundedPanel card = new RoundedPanel(16);
        card.setBackground(new Color(3, 7, 12, 232));
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(10, 18, 10, 18));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1;
        gc.insets = new Insets(0, 0, 0, 12);

        gc.gridx = 0;
        gc.weightx = 0;
        ActionIcon icon = new ActionIcon(iconType, 58);
        card.add(icon, gc);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(title);
        t.setForeground(GOLD_LIGHT);
        t.setFont(new Font("Segoe UI", Font.BOLD, 14));
        t.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel d = new JLabel("<html><body style='width:185px'>" + description + "</body></html>");
        d.setForeground(MUTED);
        d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        d.setAlignmentX(Component.LEFT_ALIGNMENT);

        text.add(Box.createVerticalGlue());
        text.add(t);
        text.add(Box.createVerticalStrut(4));
        text.add(d);
        text.add(Box.createVerticalGlue());

        gc.gridx = 1;
        gc.weightx = 1;
        card.add(text, gc);

        RoundedButton button = new RoundedButton(buttonText, 12);
        button.setPreferredSize(new Dimension(132, 34));
        button.setBackground(new Color(214, 168, 91, 20));
        button.setForeground(GOLD_LIGHT);
        button.setFont(new Font("Segoe UI", Font.BOLD, 9));
        button.addActionListener(e -> action.run());

        gc.gridx = 2;
        gc.weightx = 0;
        gc.insets = new Insets(0, 0, 0, 0);
        card.add(button, gc);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.run();
            }
        });

        return card;
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

    private JComponent rentalCard(
            BufferedImage image,
            String vehicleName,
            String category,
            String dates,
            String daysLeft,
            String location
    ) {
        RentalVehicleCard card = new RentalVehicleCard(image, vehicleName, category, dates, daysLeft, location);
        card.addActionListener(e -> showMessage(vehicleName + "\nVehicle details will open here."));
        card.setMinimumSize(new Dimension(0, 0));
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
        menuButtons.put(text, b);
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
        LogoutConfirmDialog confirmDialog = new LogoutConfirmDialog(this);
        confirmDialog.setVisible(true);

        if (confirmDialog.isConfirmed()) {
            dispose();
            new FarewellScreen(getCustomerName()).setVisible(true);
        }
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

    private static String formatNumber(int value) {
        return String.format(Locale.US, "%,d", value);
    }

    private static String formatMoney(double value) {
        return String.format(Locale.US, "$%,.0f", value);
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

    private enum WindowControlType {
        MINIMIZE, MAXIMIZE, CLOSE
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
            int iy = cover ? y : y + (h - ih) / 2;

            g.drawImage(img, ix, iy, iw, ih, null);
        }
    }

    private static final class RentalVehicleCard extends JButton {
        private final BufferedImage image;
        private final String vehicleName;
        private final String category;
        private final String dates;
        private final String daysLeft;
        private final String location;

        RentalVehicleCard(
                BufferedImage image,
                String vehicleName,
                String category,
                String dates,
                String daysLeft,
                String location
        ) {
            super("");
            this.image = image;
            this.vehicleName = vehicleName;
            this.category = category;
            this.dates = dates;
            this.daysLeft = daysLeft;
            this.location = location;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText(vehicleName);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            Shape clip = new RoundRectangle2D.Double(0, 0, w - 1, h - 1, 16, 16);
            g.setClip(clip);

            if (image != null) {
                ImageCard.drawImage(g, image, 0, 0, w, h, true);
            } else {
                g.setPaint(new GradientPaint(0, 0, new Color(12, 18, 26), w, h, new Color(3, 7, 11)));
                g.fillRect(0, 0, w, h);
            }

            g.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 188), w * 0.52f, 0, new Color(0, 0, 0, 30)));
            g.fillRect(0, 0, w, h);

            g.setPaint(new GradientPaint(0, h * 0.60f, new Color(0, 0, 0, 0), 0, h, new Color(0, 0, 0, 172)));
            g.fillRect(0, 0, w, h);

            if (getModel().isRollover()) {
                g.setColor(new Color(214, 168, 91, 22));
                g.fillRect(0, 0, w, h);
            }

            int left = Math.max(18, w / 22);
            int top = Math.max(25, h / 9);

            g.setColor(TEXT);
            g.setFont(new Font("Segoe UI", Font.BOLD, Math.max(17, Math.min(22, w / 24))));
            g.drawString(vehicleName, left, top);

            g.setColor(GOLD_LIGHT);
            g.setFont(new Font("Segoe UI", Font.PLAIN, Math.max(11, Math.min(13, w / 39))));
            g.drawString(category, left, top + 23);

            int infoY = top + 58;
            drawInfoLine(g, MenuIconType.CALENDAR, dates, left, infoY);
            drawInfoLine(g, MenuIconType.CALENDAR, daysLeft, left, infoY + 25);
            drawInfoLine(g, MenuIconType.USER, location, left, infoY + 50);

            drawStatus(g, w - 78, top - 20);
            drawDetailsButton(g, left, h - 55);
            drawMoreButton(g, w - 60, h - 55);

            g.setClip(null);
            g.setColor(LINE);
            g.setStroke(new BasicStroke(1.0f));
            g.draw(clip);

            g.dispose();
            super.paintComponent(raw);
        }

        private void drawInfoLine(Graphics2D g, MenuIconType icon, String text, int x, int y) {
            drawMenuIcon(g, icon, x, y - 12, 14, GOLD_LIGHT);
            g.setColor(new Color(224, 228, 234));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g.drawString(text, x + 22, y);
        }

        private void drawStatus(Graphics2D g, int x, int y) {
            RoundRectangle2D pill = new RoundRectangle2D.Double(x, y, 58, 22, 11, 11);
            g.setColor(new Color(38, 114, 48, 192));
            g.fill(pill);
            g.setColor(new Color(130, 224, 135, 90));
            g.draw(pill);
            g.setColor(new Color(225, 255, 225));
            g.setFont(new Font("Segoe UI", Font.BOLD, 9));
            g.drawString("Ongoing", x + 11, y + 14);
        }

        private void drawDetailsButton(Graphics2D g, int x, int y) {
            int bw = 125;
            int bh = 36;
            RoundRectangle2D button = new RoundRectangle2D.Double(x, y, bw, bh, 8, 8);
            g.setPaint(new GradientPaint(x, y, new Color(139, 91, 35, 238), x + bw, y + bh, new Color(79, 49, 19, 238)));
            g.fill(button);
            g.setColor(new Color(244, 211, 157, 105));
            g.draw(button);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g.drawString("View Details", x + 17, y + 23);
            g.setColor(GOLD_LIGHT);
            g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(x + bw - 27, y + 18, x + bw - 15, y + 18);
            g.drawLine(x + bw - 20, y + 13, x + bw - 15, y + 18);
            g.drawLine(x + bw - 20, y + 23, x + bw - 15, y + 18);
        }

        private void drawMoreButton(Graphics2D g, int x, int y) {
            int s = 36;
            RoundRectangle2D more = new RoundRectangle2D.Double(x, y, s, s, 9, 9);
            g.setColor(new Color(34, 25, 18, 210));
            g.fill(more);
            g.setColor(new Color(214, 168, 91, 70));
            g.draw(more);
            g.setColor(GOLD_LIGHT);
            g.setFont(new Font("Segoe UI", Font.BOLD, 18));
            g.drawString("...", x + 10, y + 21);
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
        private boolean active;

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

        void setActive(boolean active) {
            this.active = active;
            repaint();
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

    private static final class ActionIcon extends JPanel {
        private final MenuIconType iconType;
        private final int size;

        ActionIcon(MenuIconType iconType, int size) {
            this.iconType = iconType;
            this.size = size;
            setOpaque(false);
            setPreferredSize(new Dimension(size + 8, size + 8));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int s = Math.min(size, Math.min(getWidth(), getHeight()) - 8);
            int x = (getWidth() - s) / 2;
            int y = (getHeight() - s) / 2;

            if (iconType == MenuIconType.STAR) {
                drawShield(g, x, y, s);
            } else if (iconType == MenuIconType.DIAMOND) {
                drawGift(g, x, y, s);
            } else if (iconType == MenuIconType.HEADSET) {
                drawLargeHeadset(g, x, y, s);
            } else {
                drawMenuIcon(g, iconType, x + 8, y + 8, s - 16, GOLD_LIGHT);
            }
            g.dispose();
        }

        private void drawShield(Graphics2D g, int x, int y, int s) {
            Path2D shield = new Path2D.Double();
            shield.moveTo(x + s * 0.50, y + s * 0.08);
            shield.lineTo(x + s * 0.83, y + s * 0.22);
            shield.lineTo(x + s * 0.77, y + s * 0.68);
            shield.quadTo(x + s * 0.50, y + s * 0.93, x + s * 0.23, y + s * 0.68);
            shield.lineTo(x + s * 0.17, y + s * 0.22);
            shield.closePath();

            g.setPaint(new GradientPaint(x, y, GOLD_LIGHT, x + s, y + s, GOLD));
            g.fill(shield);
            g.setColor(new Color(38, 22, 8, 150));
            g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(shield);

            Path2D star = new Path2D.Double();
            double cx = x + s * 0.50;
            double cy = y + s * 0.47;
            double outer = s * 0.17;
            double inner = s * 0.075;
            for (int i = 0; i < 10; i++) {
                double angle = -Math.PI / 2 + i * Math.PI / 5;
                double r = i % 2 == 0 ? outer : inner;
                double px = cx + Math.cos(angle) * r;
                double py = cy + Math.sin(angle) * r;
                if (i == 0) {
                    star.moveTo(px, py);
                } else {
                    star.lineTo(px, py);
                }
            }
            star.closePath();
            g.setColor(new Color(34, 20, 8));
            g.draw(star);

            g.setColor(new Color(238, 199, 140, 175));
            g.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < 4; i++) {
                int yy = y + (int) (s * (0.52 + i * 0.075));
                g.drawArc(x + 1 + i * 2, yy, 16, 13, 96, 58);
                g.drawArc(x + s - 17 - i * 2, yy, 16, 13, 26, 58);
            }
        }

        private void drawGift(Graphics2D g, int x, int y, int s) {
            int boxX = x + s / 5;
            int boxY = y + s / 3;
            int boxW = s * 3 / 5;
            int boxH = s / 2;

            g.setPaint(new GradientPaint(x, y, GOLD_LIGHT, x + s, y + s, GOLD));
            g.fillRoundRect(boxX, boxY, boxW, boxH, 7, 7);
            g.fillRoundRect(boxX - 3, boxY - 8, boxW + 6, 13, 6, 6);

            g.setColor(new Color(34, 20, 8, 155));
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawRoundRect(boxX, boxY, boxW, boxH, 7, 7);
            g.drawLine(x + s / 2, boxY - 8, x + s / 2, boxY + boxH);
            g.drawLine(boxX - 3, boxY + 4, boxX + boxW + 3, boxY + 4);

            g.setPaint(new GradientPaint(x, y, GOLD_LIGHT, x + s, y + s, GOLD));
            g.draw(new Ellipse2D.Double(x + s * 0.22, y + s * 0.12, s * 0.25, s * 0.20));
            g.draw(new Ellipse2D.Double(x + s * 0.53, y + s * 0.12, s * 0.25, s * 0.20));
            g.drawLine(x + s / 2, y + s / 3, x + (int) (s * 0.33), y + (int) (s * 0.16));
            g.drawLine(x + s / 2, y + s / 3, x + (int) (s * 0.67), y + (int) (s * 0.16));
        }

        private void drawLargeHeadset(Graphics2D g, int x, int y, int s) {
            g.setColor(GOLD_LIGHT);
            g.setStroke(new BasicStroke(4.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Arc2D.Double(x + s * 0.18, y + s * 0.16, s * 0.64, s * 0.58, 0, 180, Arc2D.OPEN));
            g.fillRoundRect(x + (int) (s * 0.10), y + (int) (s * 0.48), s / 5, s / 3, 9, 9);
            g.fillRoundRect(x + (int) (s * 0.70), y + (int) (s * 0.48), s / 5, s / 3, 9, 9);
            g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(x + (int) (s * 0.80), y + (int) (s * 0.79), x + (int) (s * 0.70), y + (int) (s * 0.88));
            g.drawLine(x + (int) (s * 0.70), y + (int) (s * 0.88), x + (int) (s * 0.56), y + (int) (s * 0.88));
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

    private static final class WidthTrackingPanel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(72, visibleRect.height - 72);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
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
        private int count;

        TopBadgeIcon(BadgeIconType type, int count) {
            this.type = type;
            this.count = count;
            setOpaque(false);
            setPreferredSize(new Dimension(42, 42));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        void setCount(int count) {
            this.count = Math.max(0, count);
            repaint();
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

    private static final class WindowControlButton extends JButton {
        private final WindowControlType type;

        WindowControlButton(WindowControlType type) {
            super("");
            this.type = type;
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
            boolean close = type == WindowControlType.CLOSE;
            Color fillStart = close && hover ? new Color(118, 28, 31, 238) : new Color(7, 14, 20, 228);
            Color fillEnd = close && hover ? new Color(180, 45, 50, 238) : new Color(2, 8, 13, 238);

            g.setPaint(new GradientPaint(0, 0, fillStart, getWidth(), getHeight(), fillEnd));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

            g.setColor(close && hover ? new Color(255, 120, 126, 160) : new Color(214, 168, 91, hover ? 125 : 64));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

            g.setColor(close ? new Color(255, 174, 170) : GOLD_LIGHT);
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            if (type == WindowControlType.CLOSE) {
                g.drawLine(cx - 5, cy - 5, cx + 5, cy + 5);
                g.drawLine(cx + 5, cy - 5, cx - 5, cy + 5);
            } else if (type == WindowControlType.MAXIMIZE) {
                g.drawRoundRect(cx - 6, cy - 6, 12, 12, 2, 2);
            } else {
                g.drawLine(cx - 7, cy, cx + 7, cy);
            }

            g.dispose();
            super.paintComponent(raw);
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
