package com.velora.ui;

import com.velora.authentication.Customer;
import com.velora.repository.RatingRepository;
import com.velora.service.AuthenticationService;
import com.velora.service.VehicleService;
import com.velora.vehicle.Vehicle;
import com.velora.vehicle.VehicleStatus;
import com.velora.vehicle.VehicleType;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class ManagerDashboard extends JFrame {

    private static final Color BACKGROUND = new Color(2, 7, 12);
    private static final Color SIDEBAR = new Color(3, 9, 14);
    private static final Color CARD = new Color(6, 13, 20);
    private static final Color CARD_LIGHT = new Color(9, 18, 27);
    private static final Color GOLD = new Color(214, 160, 66);
    private static final Color PALE = new Color(238, 201, 139);
    private static final Color TEXT = new Color(243, 244, 247);
    private static final Color MUTED = new Color(157, 164, 175);
    private static final Color GREEN = new Color(86, 207, 114);
    private static final Color RED = new Color(235, 93, 98);

    private final Customer manager;
    private final VehicleService vehicleService = new VehicleService();
    private final AuthenticationService authenticationService = new AuthenticationService();
    private final RatingRepository ratingRepository = new RatingRepository();
    private final Map<String, MenuButton> menuButtons = new LinkedHashMap<>();

    private final CardLayout contentLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(contentLayout);
    private final DashboardBackground root = new DashboardBackground();

    private JPanel sidebar;
    private JTextField searchField;
    private DefaultTableModel vehicleModel;
    private JTable vehicleTable;
    private TableRowSorter<DefaultTableModel> vehicleSorter;
    private JLabel fleetMetric;
    private JLabel availableMetric;
    private JLabel customerMetric;
    private JLabel maintenanceMetric;
    private DefaultTableModel customerModel;
    private JTable customerTable;
    private TableRowSorter<DefaultTableModel> customerSorter;
    private JPanel customerProfileHost;
    private JLabel totalCustomersMetric;
    private JLabel activeCustomersMetric;
    private JLabel newCustomersMetric;
    private JLabel loyalCustomersMetric;
    private JLabel customerPagerLabel;
    private String activeSection = "Dashboard";

    public ManagerDashboard() {
        this(new Customer(
                "System Admin",
                "admin@velora.com",
                "",
                Customer.Role.MANAGER
        ));
    }

    public ManagerDashboard(Customer manager) {
        super("Velora Motors - Manager Dashboard");
        this.manager = manager;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setMinimumSize(new Dimension(1280, 760));
        setSize(1540, 920);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setContentPane(buildInterface());
        refreshAllData();
    }

   private JComponent buildInterface() {
    root.setLayout(new BorderLayout());

    sidebar = createSidebar();
    root.add(sidebar, BorderLayout.WEST);

    JPanel workspace = new JPanel(new BorderLayout());
    workspace.setOpaque(false);
    workspace.add(createTopBar(), BorderLayout.NORTH);

    contentCards.setOpaque(false);

    contentCards.add(createDashboardPage(), "Dashboard");
    contentCards.add(createVehiclesPage(), "Vehicles");
    contentCards.add(new RentalReturnPanel(manager), "Rentals");
    contentCards.add(createCustomersPage(), "Customers");
    contentCards.add(new BillingPanel(manager), "Billing");
    contentCards.add(new MaintenancePanel(manager), "Maintenance");
    contentCards.add(createAnalyticsPage(), "Analytics");

    workspace.add(contentCards, BorderLayout.CENTER);
    root.add(workspace, BorderLayout.CENTER);

    return root;
}

    private JPanel createSidebar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(232, 860));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        RoundedPanel shell = new RoundedPanel(18, new Color(2, 8, 13, 244));
        shell.setLayout(new BorderLayout());
        shell.setBorder(new EmptyBorder(14, 12, 14, 12));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        BrandMark brandMark = new BrandMark();
        brandMark.setAlignmentX(Component.CENTER_ALIGNMENT);
        brandMark.setPreferredSize(new Dimension(185, 60));
        brandMark.setMaximumSize(new Dimension(185, 60));

        JLabel brand = label("VELORA MOTORS", 19, Font.BOLD, TEXT);
        brand.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel subBrand = label("PREMIUM BMW VEHICLE RENTAL", 9, Font.PLAIN, PALE);
        subBrand.setAlignmentX(Component.CENTER_ALIGNMENT);

        top.add(brandMark);
        top.add(Box.createVerticalStrut(6));
        top.add(brand);
        top.add(Box.createVerticalStrut(4));
        top.add(subBrand);
        top.add(Box.createVerticalStrut(16));
        top.add(createManagerCard());
        top.add(Box.createVerticalStrut(13));

        String[][] menu = {
                {"Dashboard", "HOME"},
                {"Vehicles", "CAR"},
                {"Rentals", "CAL"},
                {"Customers", "USERS"},
                {"Billing", "BILL"},
                {"Maintenance", "TOOLS"},
                {"Analytics", "CHART"}
        };

        for (String[] item : menu) {
            MenuButton button = new MenuButton(item[0], item[1]);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setMaximumSize(new Dimension(198, 45));
            button.setPreferredSize(new Dimension(198, 45));
            button.addActionListener(e -> showSection(item[0]));
            menuButtons.put(item[0], button);
            top.add(button);
            top.add(Box.createVerticalStrut(4));
        }

        

        GoldOutlineButton logout = new GoldOutlineButton("LOGOUT");
logout.setAlignmentX(Component.CENTER_ALIGNMENT);
logout.setMaximumSize(new Dimension(198, 45));
logout.setPreferredSize(new Dimension(198, 45));
logout.addActionListener(e -> handleLogout());

top.add(Box.createVerticalStrut(14));
top.add(logout);

shell.add(top, BorderLayout.NORTH);
panel.add(shell, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> setActiveMenu("Dashboard"));
        return panel;
    }

    private JComponent createManagerCard() {
        RoundedPanel card = new RoundedPanel(15, new Color(7, 14, 21, 235));
        card.setLayout(null);
        card.setPreferredSize(new Dimension(198, 105));
        card.setMaximumSize(new Dimension(198, 105));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        Avatar avatar = new Avatar();
        avatar.setBounds(11, 15, 48, 48);
        card.add(avatar);

        JLabel name = label(manager.getFullName(), 13, Font.BOLD, TEXT);
        name.setBounds(69, 12, 120, 22);
        card.add(name);

        JLabel role = label("Role • Administrator", 10, Font.PLAIN, MUTED);
        role.setBounds(69, 33, 125, 18);
        card.add(role);

        JLabel date = label(
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                10,
                Font.PLAIN,
                new Color(202, 206, 213)
        );
        date.setBounds(69, 54, 125, 18);
        card.add(date);

        JLabel online = label("●  Online", 10, Font.BOLD, GREEN);
        online.setBounds(69, 76, 110, 18);
        card.add(online);

        return card;
    }

    private JComponent createTopBar() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(10, 12, 8, 18));

        HeaderButton menu = new HeaderButton("MENU");
        menu.setToolTipText("Show or hide navigation");
        menu.addActionListener(e -> {
            sidebar.setVisible(!sidebar.isVisible());
            root.revalidate();
        });
        wrapper.add(menu, BorderLayout.WEST);

        searchField = new SearchField();
        searchField.setPreferredSize(new Dimension(355, 40));
        searchField.addActionListener(e -> searchCurrentSection());
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterVisibleTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterVisibleTable();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterVisibleTable();
            }
        });

        JPanel searchHolder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        searchHolder.setOpaque(false);
        searchHolder.add(searchField);
        wrapper.add(searchHolder, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        HeaderButton theme = new HeaderButton("MOON");
        theme.setToolTipText("Toggle ambient dashboard tone");
        theme.addActionListener(e -> root.toggleWarmMode());

        HeaderButton notifications = new HeaderButton("BELL");
        notifications.setToolTipText("Notifications");
        notifications.addActionListener(e -> showInfo(
                "Notifications",
                "4 new notifications:\n• New rental created\n• BMW M5 maintenance due\n"
                + "• New customer registered\n• Monthly report is ready"
        ));

        HeaderButton messages = new HeaderButton("MAIL");
        messages.setToolTipText("Messages");
        messages.addActionListener(e -> showInfo(
                "Messages",
                "7 unread messages from customers and rental support."
        ));

        JButton profile = new ProfileButton(manager.getFullName());
        profile.setPreferredSize(new Dimension(205, 40));
        profile.addActionListener(e -> showInfo(
                "Administrator Profile",
                manager.getFullName() + "\n" + manager.getEmail()
                + "\nRole: Manager\nStatus: Online"
        ));

        right.add(theme);
        right.add(notifications);
        right.add(messages);
        right.add(profile);
        right.add(createWindowControls());

        wrapper.add(right, BorderLayout.EAST);
        return wrapper;
    }

    private JComponent createWindowControls() {
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        controls.setOpaque(false);

        WindowControlButton minimize = new WindowControlButton("—", false);
        minimize.setToolTipText("Minimize");
        minimize.addActionListener(e -> setExtendedState(JFrame.ICONIFIED));

        WindowControlButton maximize = new WindowControlButton("▢", false);
        maximize.setToolTipText("Maximize / Restore");
        maximize.addActionListener(e -> {
            int state = getExtendedState();
            if ((state & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
                setExtendedState(JFrame.NORMAL);
            } else {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
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

    private JComponent createDashboardPage() {
        DashboardScrollPanel page = new DashboardScrollPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(new EmptyBorder(0, 12, 10, 18));

        HeroPanel hero = new HeroPanel(manager.getFullName());
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);
        hero.setPreferredSize(new Dimension(1200, 320));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        hero.getStatisticsButton().addActionListener(e -> showSection("Analytics"));
        page.add(hero);
        page.add(Box.createVerticalStrut(9));

        JLabel quickTitle = label("Quick Navigation", 15, Font.BOLD, TEXT);
        quickTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(quickTitle);
        JPanel underline = new JPanel();
        underline.setBackground(GOLD);
        underline.setMaximumSize(new Dimension(42, 2));
        underline.setPreferredSize(new Dimension(42, 2));
        underline.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(Box.createVerticalStrut(5));
        page.add(underline);
        page.add(Box.createVerticalStrut(8));

        JPanel navigationBand = new JPanel(new BorderLayout(13, 0));
        navigationBand.setOpaque(false);
        navigationBand.setAlignmentX(Component.LEFT_ALIGNMENT);
        navigationBand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        navigationBand.setPreferredSize(new Dimension(1100, 190));

        JPanel quickGrid = new JPanel(new GridLayout(2, 4, 11, 11));
        quickGrid.setOpaque(false);
        quickGrid.add(quickCard("Vehicles", "Manage your BMW fleet", "Vehicles"));
        quickGrid.add(quickCard("Rentals", "Manage rentals and reservations", "Rentals"));
        quickGrid.add(quickCard("Customers", "View and manage customers", "Customers"));
        quickGrid.add(quickCard("Billing", "Invoices, payments and transactions", "Billing"));
        quickGrid.add(quickCard("Maintenance", "Schedule and track maintenance", "Maintenance"));
        quickGrid.add(quickCard("Analytics", "View reports and performance", "Analytics"));
        quickGrid.add(transparentPanel());
        quickGrid.add(transparentPanel());
        navigationBand.add(quickGrid, BorderLayout.CENTER);

        LuxuryCarCard luxuryCard = new LuxuryCarCard();
        luxuryCard.setPreferredSize(new Dimension(350, 190));
        luxuryCard.addActionListener(e -> showSection("Vehicles"));
        navigationBand.add(luxuryCard, BorderLayout.EAST);
        page.add(navigationBand);
        page.add(Box.createVerticalStrut(10));

        JPanel overview = new JPanel(new GridLayout(1, 3, 12, 0));
        overview.setOpaque(false);
        overview.setAlignmentX(Component.LEFT_ALIGNMENT);
        overview.setMaximumSize(new Dimension(Integer.MAX_VALUE, 165));
        overview.setPreferredSize(new Dimension(1100, 165));
        overview.add(createOverviewCard());
        overview.add(createActivityCard());
        overview.add(createOfferCard());
        page.add(overview);
        page.add(Box.createVerticalStrut(9));
        page.add(createDashboardFooter());

        JScrollPane scroll = new JScrollPane(page);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(7, 0));
        scroll.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        return scroll;
    }

    private JPanel transparentPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        return panel;
    }

    private JComponent createDashboardFooter() {
        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setOpaque(false);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        footer.setPreferredSize(new Dimension(1000, 52));
        footer.setBorder(BorderFactory.createMatteBorder(
                1, 0, 0, 0, new Color(214, 160, 66, 28)
        ));

        JLabel copyright = label("© 2026 Velora Motors. All rights reserved.", 10, Font.PLAIN, MUTED);
        copyright.setBorder(new EmptyBorder(14, 0, 0, 0));
        BrandSignature signature = new BrandSignature();
        signature.setPreferredSize(new Dimension(330, 50));
        JLabel slogan = label("<html>Drive Luxury. Drive <font color='#D6A042'><b>BMW.</b></font></html>",
                11, Font.PLAIN, MUTED);
        slogan.setHorizontalAlignment(SwingConstants.RIGHT);
        slogan.setBorder(new EmptyBorder(14, 0, 0, 0));
        footer.add(copyright, BorderLayout.WEST);
        footer.add(signature, BorderLayout.CENTER);
        footer.add(slogan, BorderLayout.EAST);
        return footer;
    }

    private JComponent quickCard(String title, String description, String section) {
        QuickCard card = new QuickCard(title, description);
        card.addActionListener(e -> showSection(section));
        return card;
    }

    private JComponent createOverviewCard() {
        RoundedPanel card = cardPanel();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(14, 15, 14, 15));

        card.add(label("Today's Overview", 13, Font.BOLD, TEXT), BorderLayout.NORTH);

        JPanel metrics = new JPanel(new GridLayout(1, 4, 7, 0));
        metrics.setOpaque(false);
        fleetMetric = metricLabel("0", "Fleet");
        availableMetric = metricLabel("0", "Available");
        customerMetric = metricLabel("0", "Customers");
        maintenanceMetric = metricLabel("0", "Service Due");
        metrics.add(fleetMetric);
        metrics.add(availableMetric);
        metrics.add(customerMetric);
        metrics.add(maintenanceMetric);
        card.add(metrics, BorderLayout.CENTER);

        GoldOutlineButton view = new GoldOutlineButton("VIEW ALL STATISTICS");
        view.setPreferredSize(new Dimension(170, 35));
        view.addActionListener(e -> showSection("Analytics"));
        JPanel bottom = transparentFlow(FlowLayout.LEFT);
        bottom.add(view);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private JComponent createActivityCard() {
        RoundedPanel card = cardPanel();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(14, 15, 14, 15));
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        heading.add(label("Recent Activity", 13, Font.BOLD, TEXT), BorderLayout.WEST);
        JButton viewAll = new JButton("View All");
        viewAll.setForeground(GOLD);
        viewAll.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        viewAll.setOpaque(false);
        viewAll.setContentAreaFilled(false);
        viewAll.setBorderPainted(false);
        viewAll.setFocusPainted(false);
        viewAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewAll.addActionListener(e -> showSection("Analytics"));
        heading.add(viewAll, BorderLayout.EAST);
        card.add(heading, BorderLayout.NORTH);

        JPanel rows = new JPanel(new GridLayout(4, 1, 0, 4));
        rows.setOpaque(false);
        rows.add(new ActivityRow("New rental created for BMW X7", "by John Doe", "2 min ago", "CAL"));
        rows.add(new ActivityRow("Vehicle returned: BMW 430i", "by Sarah Johnson", "15 min ago", "CAR"));
        rows.add(new ActivityRow("Maintenance scheduled for BMW M5", "Oil Change", "1 hour ago", "TOOLS"));
        rows.add(new ActivityRow("New customer registered", "Michael Brown", "2 hours ago", "USERS"));
        card.add(rows, BorderLayout.CENTER);
        return card;
    }

    private JComponent createOfferCard() {
        AnnouncementCard card = new AnnouncementCard();
        card.getOfferButton().addActionListener(e -> showInfo(
                "Velora Summer Offers",
                "30% off selected BMW models.\nOffer valid until 31 August 2026."
        ));
        return card;
    }

    private JComponent createVehiclesPage() {
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setOpaque(false);
        page.setBorder(new EmptyBorder(8, 12, 24, 22));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        heading.add(sectionHeading(
                "Vehicles",
                "Manage the complete Velora fleet and vehicle availability."
        ), BorderLayout.WEST);

        JPanel actions = transparentFlow(FlowLayout.RIGHT);
        actions.add(actionButton("ADD VEHICLE", e -> addVehicle()));
        actions.add(actionButton("EDIT", e -> editSelectedVehicle()));
        actions.add(actionButton("STATUS", e -> changeSelectedStatus()));
        actions.add(actionButton("BATTERY", e -> setSelectedBattery()));
        GoldOutlineButton delete = new GoldOutlineButton("DELETE");
        delete.setForeground(RED);
        delete.addActionListener(e -> deleteSelectedVehicle());
        actions.add(delete);
        heading.add(actions, BorderLayout.EAST);
        page.add(heading, BorderLayout.NORTH);

        RoundedPanel tableCard = cardPanel();
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(14, 14, 14, 14));

        vehicleModel = new DefaultTableModel(
                new String[]{"ID", "Brand", "Model", "Type", "Status", "Daily Price", "Battery"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        vehicleTable = new JTable(vehicleModel);
        configureTable(vehicleTable);
        vehicleSorter = new TableRowSorter<>(vehicleModel);
        vehicleTable.setRowSorter(vehicleSorter);

        JScrollPane tableScroll = new JScrollPane(vehicleTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(214, 160, 66, 48)));
        tableScroll.getViewport().setBackground(new Color(4, 10, 16));
        tableCard.add(tableScroll, BorderLayout.CENTER);
        page.add(tableCard, BorderLayout.CENTER);
        return page;
    }

    private JComponent createCustomersPage() {
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setOpaque(false);
        page.setBorder(new EmptyBorder(8, 12, 24, 18));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        heading.add(sectionHeading(
                "Customers",
                "Manage and view all your customers"
        ), BorderLayout.WEST);
        JButton addCustomer = actionButton("+  Add Customer", e -> addCustomer());
        addCustomer.setPreferredSize(new Dimension(150, 40));
        heading.add(addCustomer, BorderLayout.EAST);
        page.add(heading, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(14, 0));
        center.setOpaque(false);

        JPanel left = new JPanel(new BorderLayout(0, 14));
        left.setOpaque(false);

        JPanel metricGrid = new JPanel(new GridLayout(1, 4, 12, 0));
        metricGrid.setOpaque(false);
        totalCustomersMetric = label("0", 22, Font.PLAIN, TEXT);
        activeCustomersMetric = label("0", 22, Font.PLAIN, TEXT);
        newCustomersMetric = label("0", 22, Font.PLAIN, TEXT);
        loyalCustomersMetric = label("0", 22, Font.PLAIN, TEXT);
        metricGrid.add(createCustomerStatCard("Users", totalCustomersMetric, "Total Customers"));
        metricGrid.add(createCustomerStatCard("Active", activeCustomersMetric, "Active Customers"));
        metricGrid.add(createCustomerStatCard("New", newCustomersMetric, "New This Month"));
        metricGrid.add(createCustomerStatCard("Loyal", loyalCustomersMetric, "Loyal Customers"));
        left.add(metricGrid, BorderLayout.NORTH);

        RoundedPanel tableCard = cardPanel();
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(14, 14, 14, 14));

        customerModel = new DefaultTableModel(
                new String[]{"Photo", "Name", "Phone", "Email", "Loyalty", "Rentals", "Rating", "Actions"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        customerTable = new JTable(customerModel);
        configureCustomerTable(customerTable);
        customerSorter = new TableRowSorter<>(customerModel);
        customerTable.setRowSorter(customerSorter);
        customerTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                showSelectedCustomerProfile();
            }
        });

        JScrollPane tableScroll = new JScrollPane(customerTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.getViewport().setBackground(new Color(4, 10, 16));
        tableScroll.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        tableCard.add(tableScroll, BorderLayout.CENTER);
        tableCard.add(createCustomerPager(), BorderLayout.SOUTH);
        left.add(tableCard, BorderLayout.CENTER);

        customerProfileHost = new JPanel(new BorderLayout());
        customerProfileHost.setOpaque(false);
        customerProfileHost.setPreferredSize(new Dimension(445, 680));

        center.add(left, BorderLayout.CENTER);
        center.add(customerProfileHost, BorderLayout.EAST);
        page.add(center, BorderLayout.CENTER);
        page.add(createDashboardFooter(), BorderLayout.SOUTH);
        loadCustomerTable();
        return page;
    }

    private JComponent createCustomerStatCard(String icon, JLabel valueLabel, String title) {
        RoundedPanel card = new RoundedPanel(10, new Color(5, 11, 18, 238));
        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(new EmptyBorder(18, 18, 16, 18));
        card.setPreferredSize(new Dimension(188, 86));

        CustomerStatIcon iconView = new CustomerStatIcon(icon);
        iconView.setPreferredSize(new Dimension(52, 52));
        card.add(iconView, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        JLabel titleLabel = label(title, 11, Font.PLAIN, new Color(183, 190, 203));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        valueLabel.setForeground(TEXT);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(7));
        textPanel.add(valueLabel);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    private JComponent createCustomerPager() {
        JPanel pager = new JPanel(new BorderLayout());
        pager.setOpaque(false);
        pager.setBorder(new EmptyBorder(12, 0, 0, 0));

        customerPagerLabel = label("", 11, Font.PLAIN, MUTED);
        pager.add(customerPagerLabel, BorderLayout.WEST);

        JPanel pages = transparentFlow(FlowLayout.RIGHT);
        pages.add(label("10 per page", 11, Font.PLAIN, TEXT));
        pages.add(new GoldOutlineButton("1"));
        pages.add(new GoldOutlineButton("2"));
        pages.add(new GoldOutlineButton("3"));
        pager.add(pages, BorderLayout.EAST);
        return pager;
    }

    private void configureCustomerTable(JTable table) {
        configureTable(table);
        table.setRowHeight(58);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setPreferredSize(new Dimension(100, 46));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(255, 255, 255, 12));
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(92, 62, 25, 210));
        table.setDefaultRenderer(Object.class, new CustomerTextRenderer());
        table.getColumnModel().getColumn(0).setCellRenderer(new CustomerAvatarRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new LoyaltyRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new StarRatingRenderer());

        int[] widths = {66, 150, 140, 205, 95, 80, 120, 80};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private void loadCustomerTable() {
        if (customerModel == null) {
            return;
        }

        customerModel.setRowCount(0);
        for (Customer customer : customerAccounts()) {
            CustomerInsights insights = customerInsights(customer);
            customerModel.addRow(new Object[]{
                    customer,
                    customer.getFullName(),
                    blankFallback(customer.getPhone(), "+970 59 000 0000"),
                    customer.getEmail(),
                    insights.tier(),
                    insights.rentals(),
                    insights.rating(),
                    "..."
            });
        }

        refreshCustomerStats();
        if (customerTable.getRowCount() > 0 && customerTable.getSelectedRow() < 0) {
            customerTable.setRowSelectionInterval(0, 0);
        } else {
            showSelectedCustomerProfile();
        }
    }

    private void refreshCustomerStats() {
        if (totalCustomersMetric == null) {
            return;
        }

        List<Customer> customers = customerAccounts();
        long loyal = customers.stream()
                .filter(customer -> "Gold".equals(customerInsights(customer).tier()))
                .count();
        int newThisMonth = customers.isEmpty() ? 0 : Math.max(1, Math.min(customers.size(), customers.size() / 3 + 1));

        totalCustomersMetric.setText(formatNumber(customers.size()));
        activeCustomersMetric.setText(formatNumber(customers.size()));
        newCustomersMetric.setText(formatNumber(newThisMonth));
        loyalCustomersMetric.setText(formatNumber((int) loyal));
        if (customerPagerLabel != null) {
            int visible = customerTable == null ? customers.size() : customerTable.getRowCount();
            customerPagerLabel.setText("Showing 1 to " + visible + " of "
                    + formatNumber(customers.size()) + " customers");
        }
    }

    private void showSelectedCustomerProfile() {
        if (customerProfileHost == null) {
            return;
        }

        Customer selected = selectedCustomer();
        customerProfileHost.removeAll();
        customerProfileHost.add(createCustomerProfile(selected), BorderLayout.CENTER);
        customerProfileHost.revalidate();
        customerProfileHost.repaint();
    }

    private Customer selectedCustomer() {
        if (customerTable == null || customerTable.getSelectedRow() < 0) {
            return customerAccounts().stream().findFirst().orElse(null);
        }

        int modelRow = customerTable.convertRowIndexToModel(customerTable.getSelectedRow());
        String email = String.valueOf(customerModel.getValueAt(modelRow, 3));
        return customerAccounts().stream()
                .filter(customer -> customer.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }

    private JComponent createCustomerProfile(Customer customer) {
        RoundedPanel profile = cardPanel();
        profile.setLayout(new BoxLayout(profile, BoxLayout.Y_AXIS));
        profile.setBorder(new EmptyBorder(22, 18, 18, 18));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleRow.add(label("Customer Profile", 18, Font.PLAIN, TEXT), BorderLayout.WEST);
        JLabel close = label("X", 20, Font.PLAIN, TEXT);
        close.setHorizontalAlignment(SwingConstants.RIGHT);
        titleRow.add(close, BorderLayout.EAST);
        profile.add(titleRow);
        profile.add(Box.createVerticalStrut(20));

        if (customer == null) {
            JLabel empty = label("No customer accounts registered yet.", 14, Font.PLAIN, MUTED);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            profile.add(empty);
            return profile;
        }

        CustomerInsights insights = customerInsights(customer);
        JPanel identity = new JPanel(new BorderLayout(16, 0));
        identity.setOpaque(false);
        identity.setAlignmentX(Component.LEFT_ALIGNMENT);
        CustomerPortrait avatar = new CustomerPortrait(customer, 118);
        avatar.setPreferredSize(new Dimension(118, 118));
        identity.add(avatar, BorderLayout.WEST);

        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        details.add(label(customer.getFullName(), 19, Font.PLAIN, TEXT));
        details.add(Box.createVerticalStrut(8));
        details.add(label(insights.tier() + " Member", 11, Font.BOLD, GOLD));
        details.add(Box.createVerticalStrut(13));
        details.add(label("Phone   " + blankFallback(customer.getPhone(), "+970 59 000 0000"), 12, Font.PLAIN, TEXT));
        details.add(label("Email   " + customer.getEmail(), 12, Font.PLAIN, TEXT));
        details.add(label("Location   Ramallah, Palestine", 12, Font.PLAIN, TEXT));
        details.add(label("Customer since   " + insights.customerSince(), 12, Font.PLAIN, TEXT));
        identity.add(details, BorderLayout.CENTER);
        profile.add(identity);
        profile.add(Box.createVerticalStrut(22));

        profile.add(createProfileTabs());
        profile.add(Box.createVerticalStrut(14));

        JPanel cards = new JPanel(new GridLayout(2, 2, 12, 12));
        cards.setOpaque(false);
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 355));
        cards.add(createPersonalInfoCard(customer, insights));
        cards.add(createLoyaltyCard(insights));
        cards.add(createRentalCard(insights));
        cards.add(createRatingCard(insights));
        profile.add(cards);
        profile.add(Box.createVerticalStrut(12));
        profile.add(createQuickStats(insights));
        return profile;
    }

    private JComponent createProfileTabs() {
        JPanel tabs = new JPanel(new GridLayout(1, 5, 0, 0));
        tabs.setOpaque(false);
        tabs.setAlignmentX(Component.LEFT_ALIGNMENT);
        tabs.setPreferredSize(new Dimension(410, 24));
        tabs.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        String[] labels = {"Overview", "Rented Cars", "Invoices", "Loyalty", "Reviews"};
        for (int i = 0; i < labels.length; i++) {
            JLabel tab = label(labels[i], 10, i == 0 ? Font.BOLD : Font.PLAIN, i == 0 ? GOLD : MUTED);
            tab.setHorizontalAlignment(SwingConstants.CENTER);
            tab.setOpaque(true);
            tab.setBackground(i == 0 ? new Color(214, 160, 66, 20) : new Color(6, 13, 20, 145));
            tab.setBorder(BorderFactory.createMatteBorder(0, 0, i == 0 ? 2 : 0, 1, new Color(214, 160, 66, 65)));
            tabs.add(tab);
        }
        return tabs;
    }

    private JComponent createPersonalInfoCard(Customer customer, CustomerInsights insights) {
        RoundedPanel card = profileCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel title = label("Personal Infor...", 14, Font.PLAIN, TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(16));
        card.add(detailLine("Full Name", customer.getFullName()));
        card.add(detailLine("Phone", blankFallback(customer.getPhone(), "Not provided")));
        card.add(detailLine("Email", customer.getEmail()));
        card.add(detailLine("Address", "Ramallah, Palestine"));
        card.add(detailLine("Driver License", insights.licenseStatus()));
        return card;
    }

    private JComponent createLoyaltyCard(CustomerInsights insights) {
        RoundedPanel card = profileCard();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        card.add(label("Loyalty Summary", 14, Font.PLAIN, TEXT), BorderLayout.NORTH);
        JLabel points = label(
                "<html><div style='text-align:center'><font size='5'>" + formatNumber(insights.points())
                        + "</font><br><font size='2'>Points</font><br><br>Next Tier: Platinum<br>"
                        + formatNumber(Math.max(0, 6000 - insights.points())) + " points to go</div></html>",
                12,
                Font.PLAIN,
                TEXT
        );
        points.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(points, BorderLayout.CENTER);
        return card;
    }

    private JComponent createRentalCard(CustomerInsights insights) {
        RoundedPanel card = profileCard();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(13, 14, 13, 14));
        card.add(label("Total Rentals", 15, Font.PLAIN, TEXT), BorderLayout.NORTH);
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(label(insights.rentals() + " Rentals", 13, Font.PLAIN, TEXT));
        content.add(label("Total Spent", 11, Font.PLAIN, MUTED));
        content.add(label(formatCurrency(insights.totalSpent()), 15, Font.PLAIN, TEXT));
        content.add(Box.createVerticalStrut(8));
        content.add(new MiniSparkline());
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JComponent createRatingCard(CustomerInsights insights) {
        RoundedPanel card = profileCard();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(13, 14, 13, 14));
        card.add(label("Average Rating", 15, Font.PLAIN, TEXT), BorderLayout.NORTH);
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(label(String.format(Locale.US, "%.1f", insights.rating()) + " / 5", 18, Font.PLAIN, TEXT));
        content.add(Box.createVerticalStrut(12));
        StarRatingView stars = new StarRatingView(insights.rating());
        stars.setAlignmentX(Component.LEFT_ALIGNMENT);
        stars.setPreferredSize(new Dimension(130, 24));
        stars.setMaximumSize(new Dimension(130, 24));
        content.add(stars);
        content.add(Box.createVerticalStrut(13));
        content.add(label("(" + insights.reviewCount() + " Reviews)", 12, Font.PLAIN, MUTED));
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JComponent createQuickStats(CustomerInsights insights) {
        RoundedPanel card = profileCard();
        card.setLayout(new GridLayout(1, 4, 10, 0));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        card.add(quickStat(String.valueOf(insights.rentals()), "Rentals"));
        card.add(quickStat(formatCurrency(insights.totalSpent()), "Total Spent"));
        card.add(quickStat(formatNumber(insights.points()), "Loyalty Points"));
        card.add(quickStat(String.format(Locale.US, "%.1f", insights.rating()), "Rating"));
        return card;
    }

    private RoundedPanel profileCard() {
        return new RoundedPanel(8, new Color(5, 12, 18, 236));
    }

    private JComponent detailLine(String caption, String value) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 23));
        row.add(label(caption, 10, Font.PLAIN, MUTED), BorderLayout.WEST);
        JLabel valueLabel = label(value, 10, Font.PLAIN, TEXT);
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    private JComponent quickStat(String value, String caption) {
        JPanel stat = new JPanel();
        stat.setOpaque(false);
        stat.setLayout(new BoxLayout(stat, BoxLayout.Y_AXIS));
        JLabel valueLabel = label(value, 17, Font.PLAIN, TEXT);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel captionLabel = label(caption, 11, Font.PLAIN, MUTED);
        captionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        stat.add(valueLabel);
        stat.add(Box.createVerticalStrut(5));
        stat.add(captionLabel);
        return stat;
    }

    private JComponent createOperationalPage(
            String title,
            String description,
            String[][] metrics,
            String[] rows,
            String actionText
    ) {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setOpaque(false);
        page.setBorder(new EmptyBorder(8, 12, 24, 22));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        heading.add(sectionHeading(title, description), BorderLayout.WEST);
        heading.add(actionButton(actionText, e -> operationalAction(title)), BorderLayout.EAST);
        page.add(heading, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel metricGrid = new JPanel(new GridLayout(1, metrics.length, 13, 0));
        metricGrid.setOpaque(false);
        metricGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        metricGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 135));
        for (String[] metric : metrics) {
            metricGrid.add(createLargeMetricCard(metric[0], metric[1]));
        }
        content.add(metricGrid);
        content.add(Box.createVerticalStrut(16));

        RoundedPanel activity = cardPanel();
        activity.setLayout(new BorderLayout(0, 12));
        activity.setBorder(new EmptyBorder(18, 18, 18, 18));
        activity.setAlignmentX(Component.LEFT_ALIGNMENT);
        activity.setMaximumSize(new Dimension(Integer.MAX_VALUE, 360));
        activity.add(label("Recent " + title + " Activity", 17, Font.BOLD, TEXT), BorderLayout.NORTH);

        JPanel rowPanel = new JPanel(new GridLayout(rows.length, 1, 0, 8));
        rowPanel.setOpaque(false);
        for (String row : rows) {
            JLabel item = label("  •  " + row, 13, Font.PLAIN, new Color(215, 219, 225));
            item.setOpaque(true);
            item.setBackground(new Color(10, 19, 28, 185));
            item.setBorder(new EmptyBorder(10, 10, 10, 10));
            rowPanel.add(item);
        }
        activity.add(rowPanel, BorderLayout.CENTER);
        content.add(activity);

        page.add(content, BorderLayout.CENTER);
        return page;
    }

   private JComponent createAnalyticsPage() {
    return new AnalyticsPanel(vehicleService, authenticationService);
}

    private JComponent sectionHeading(String title, String description) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(label(title, 27, Font.BOLD, TEXT));
        panel.add(Box.createVerticalStrut(4));
        panel.add(label(description, 12, Font.PLAIN, MUTED));
        return panel;
    }

    private JComponent createLargeMetricCard(String value, String title) {
        RoundedPanel card = cardPanel();
        card.setLayout(null);
        JLabel valueLabel = label(value, 30, Font.BOLD, PALE);
        valueLabel.setBounds(20, 25, 220, 42);
        card.add(valueLabel);
        JLabel titleLabel = label(title, 13, Font.PLAIN, MUTED);
        titleLabel.setBounds(20, 75, 240, 25);
        card.add(titleLabel);
        return card;
    }

    private JLabel metricLabel(String value, String caption) {
        return new MetricIconLabel(value, caption);
    }

    private JComponent activityRow(String text, String time) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(label("●  " + text, 10, Font.PLAIN, new Color(212, 217, 224)), BorderLayout.WEST);
        row.add(label(time, 9, Font.PLAIN, MUTED), BorderLayout.EAST);
        return row;
    }

    private void configureTable(JTable table) {
        table.setRowHeight(45);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(30, 42, 52));
        table.setBackground(new Color(5, 12, 18));
        table.setForeground(TEXT);
        table.setSelectionBackground(new Color(91, 65, 30));
        table.setSelectionForeground(TEXT);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(8, 17, 25));
        header.setForeground(PALE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setPreferredSize(new Dimension(100, 40));
        header.setBorder(BorderFactory.createEmptyBorder());

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setOpaque(true);
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        headerRenderer.setBackground(new Color(8, 17, 25));
        headerRenderer.setForeground(PALE);
        headerRenderer.setFont(new Font("Segoe UI", Font.BOLD, 11));
        headerRenderer.setBorder(BorderFactory.createMatteBorder(
                0, 0, 1, 1, new Color(214, 160, 66, 60)
        ));
        header.setDefaultRenderer(headerRenderer);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        renderer.setBackground(new Color(5, 12, 18));
        renderer.setForeground(TEXT);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    private JButton actionButton(String text, java.awt.event.ActionListener listener) {
        GoldButton button = new GoldButton(text);
        button.addActionListener(listener);
        return button;
    }

    private void showSection(String section) {
        activeSection = section;
        contentLayout.show(contentCards, section);
        setActiveMenu(section);
        if ("Vehicles".equals(section)) {
            loadVehicleTable();
            filterVisibleTable();
        } else if ("Customers".equals(section)) {
            loadCustomerTable();
            filterVisibleTable();
        }
    }

    private void setActiveMenu(String section) {
        menuButtons.forEach((name, button) -> button.setActive(name.equals(section)));
    }

    private void searchVehicles() {
        String query = currentSearchQuery();
        showSection("Vehicles");

        if (query.isBlank()) {
            vehicleSorter.setRowFilter(null);
        } else {
            vehicleSorter.setRowFilter(RowFilter.regexFilter(
                    "(?i)" + Pattern.quote(query)
            ));
        }
    }

    private void searchCurrentSection() {
        if ("Customers".equals(activeSection)) {
            searchCustomers();
        } else {
            searchVehicles();
        }
    }

    private void filterVisibleTable() {
        if ("Customers".equals(activeSection)) {
            searchCustomers();
        } else if ("Vehicles".equals(activeSection) && vehicleSorter != null) {
            String query = currentSearchQuery();
            vehicleSorter.setRowFilter(query.isBlank()
                    ? null
                    : RowFilter.regexFilter("(?i)" + Pattern.quote(query)));
        }
    }

    private void searchCustomers() {
        if (customerSorter == null) {
            return;
        }

        String query = currentSearchQuery();
        customerSorter.setRowFilter(query.isBlank()
                ? null
                : RowFilter.regexFilter("(?i)" + Pattern.quote(query)));
        if (customerPagerLabel != null) {
            customerPagerLabel.setText("Showing 1 to " + customerTable.getRowCount() + " of "
                    + formatNumber(customerAccounts().size()) + " customers");
        }

        if (customerTable.getRowCount() > 0) {
            customerTable.setRowSelectionInterval(0, 0);
        } else {
            showSelectedCustomerProfile();
        }
    }

    private String currentSearchQuery() {
        if (searchField == null) {
            return "";
        }

        String query = searchField.getText().trim();
        return "Search anything...".equals(query) || "Search customers...".equals(query) ? "" : query;
    }

    private void refreshAllData() {
        if (vehicleModel != null) {
            loadVehicleTable();
        }
        if (customerModel != null) {
            loadCustomerTable();
        }

        List<Vehicle> vehicles = vehicleService.getAllVehicles();
        if (fleetMetric != null) {
            updateMetric(fleetMetric, vehicles.size(), "Fleet");
            updateMetric(availableMetric, vehicleService.countAvailableVehicles(), "Available");
            updateMetric(customerMetric, customerCount(), "Customers");
            updateMetric(
                    maintenanceMetric,
                    (int) vehicles.stream().filter(v -> v.getStatus() == VehicleStatus.MAINTENANCE).count(),
                    "Service Due"
            );
        }
    }

    private void updateMetric(JLabel label, int value, String caption) {
        if (label instanceof MetricIconLabel metric) {
            metric.setMetric(value, caption);
            return;
        }
        label.setText(
                "<html><div style='text-align:center'><b><font color='#EEC98B' size='5'>"
                + value + "</font></b><br><font color='#9DA4AF' size='2'>"
                + caption + "</font></div></html>"
        );
    }

    private void loadVehicleTable() {
        vehicleModel.setRowCount(0);
        for (Vehicle vehicle : vehicleService.getAllVehicles()) {
            vehicleModel.addRow(new Object[]{
                    vehicle.getId(),
                    vehicle.getBrand(),
                    vehicle.getModel(),
                    vehicle.getType(),
                    vehicle.getStatus(),
                    String.format("$%.2f", vehicle.getDailyPrice()),
                    vehicle.getBatteryLevel() == null ? "N/A" : vehicle.getBatteryLevel() + "%"
            });
        }
        refreshAllDataMetricsOnly();
    }

    private void refreshAllDataMetricsOnly() {
        if (fleetMetric == null) {
            return;
        }
        List<Vehicle> vehicles = vehicleService.getAllVehicles();
        updateMetric(fleetMetric, vehicles.size(), "Fleet");
        updateMetric(availableMetric, vehicleService.countAvailableVehicles(), "Available");
        updateMetric(customerMetric, customerCount(), "Customers");
        updateMetric(
                maintenanceMetric,
                (int) vehicles.stream().filter(v -> v.getStatus() == VehicleStatus.MAINTENANCE).count(),
                "Service Due"
        );
    }

    private void addVehicle() {
        JTextField id = new JTextField("V" + String.format("%03d", vehicleService.getAllVehicles().size() + 1));
        JTextField brand = new JTextField("BMW");
        JTextField model = new JTextField();
        JComboBox<VehicleType> type = new JComboBox<>(VehicleType.values());
        JComboBox<VehicleStatus> status = new JComboBox<>(VehicleStatus.values());
        JTextField price = new JTextField();
        JTextField battery = new JTextField();

        JPanel form = formPanel(
                "ID", id,
                "Brand", brand,
                "Model", model,
                "Type", type,
                "Status", status,
                "Daily Price", price,
                "Battery % (optional)", battery
        );

        if (JOptionPane.showConfirmDialog(
                this,
                form,
                "Add Vehicle",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        ) != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            if (model.getText().isBlank()) {
                throw new IllegalArgumentException("Model is required.");
            }
            boolean duplicate = vehicleService.getAllVehicles().stream()
                    .anyMatch(v -> v.getId().equalsIgnoreCase(id.getText().trim()));
            if (duplicate) {
                throw new IllegalArgumentException("Vehicle ID already exists.");
            }

            Integer batteryLevel = battery.getText().isBlank()
                    ? null
                    : clampBattery(Integer.parseInt(battery.getText().trim()));

            Vehicle vehicle = new Vehicle(
                    id.getText().trim(),
                    brand.getText().trim(),
                    model.getText().trim(),
                    (VehicleType) type.getSelectedItem(),
                    (VehicleStatus) status.getSelectedItem(),
                    Double.parseDouble(price.getText().trim()),
                    batteryLevel
            );
            vehicleService.getAllVehicles().add(vehicle);
            loadVehicleTable();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void editSelectedVehicle() {
        Vehicle vehicle = selectedVehicle();
        if (vehicle == null) {
            return;
        }

        JTextField brand = new JTextField(vehicle.getBrand());
        JTextField model = new JTextField(vehicle.getModel());
        JTextField price = new JTextField(String.valueOf(vehicle.getDailyPrice()));
        JPanel form = formPanel("Brand", brand, "Model", model, "Daily Price", price);

        if (JOptionPane.showConfirmDialog(
                this,
                form,
                "Edit " + vehicle.getDisplayName(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        ) == JOptionPane.OK_OPTION) {
            try {
                vehicle.setBrand(brand.getText().trim());
                vehicle.setModel(model.getText().trim());
                vehicle.setDailyPrice(Double.parseDouble(price.getText().trim()));
                loadVehicleTable();
            } catch (NumberFormatException ex) {
                showError("Daily price must be a valid number.");
            }
        }
    }

    private void changeSelectedStatus() {
        Vehicle vehicle = selectedVehicle();
        if (vehicle == null) {
            return;
        }

        VehicleStatus status = (VehicleStatus) JOptionPane.showInputDialog(
                this,
                "Choose the new status:",
                "Vehicle Status",
                JOptionPane.PLAIN_MESSAGE,
                null,
                VehicleStatus.values(),
                vehicle.getStatus()
        );
        if (status != null) {
            vehicle.setStatus(status);
            loadVehicleTable();
        }
    }

    private void setSelectedBattery() {
        Vehicle vehicle = selectedVehicle();
        if (vehicle == null) {
            return;
        }

        String input = JOptionPane.showInputDialog(
                this,
                "Battery percentage (0-100):",
                vehicle.getBatteryLevel() == null ? "" : vehicle.getBatteryLevel()
        );
        if (input == null) {
            return;
        }

        try {
            vehicle.setBatteryLevel(clampBattery(Integer.parseInt(input.trim())));
            loadVehicleTable();
        } catch (NumberFormatException ex) {
            showError("Battery must be a number between 0 and 100.");
        }
    }

    private void deleteSelectedVehicle() {
        Vehicle vehicle = selectedVehicle();
        if (vehicle == null) {
            return;
        }

        if (JOptionPane.showConfirmDialog(
                this,
                "Delete " + vehicle.getDisplayName() + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        ) == JOptionPane.YES_OPTION) {
            vehicleService.getAllVehicles().remove(vehicle);
            loadVehicleTable();
        }
    }

    private Vehicle selectedVehicle() {
        int viewRow = vehicleTable.getSelectedRow();
        if (viewRow < 0) {
            showError("Select a vehicle from the table first.");
            return null;
        }
        int modelRow = vehicleTable.convertRowIndexToModel(viewRow);
        String id = String.valueOf(vehicleModel.getValueAt(modelRow, 0));
        return vehicleService.getAllVehicles().stream()
                .filter(v -> v.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private int clampBattery(int value) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("Battery must be between 0 and 100.");
        }
        return value;
    }

    private JPanel formPanel(Object... labelAndComponents) {
        JPanel form = new JPanel(new GridLayout(labelAndComponents.length / 2, 2, 9, 9));
        for (int i = 0; i < labelAndComponents.length; i += 2) {
            form.add(new JLabel(String.valueOf(labelAndComponents[i])));
            form.add((Component) labelAndComponents[i + 1]);
        }
        return form;
    }

    private void addCustomer() {
        JTextField fullName = new JTextField();
        JTextField email = new JTextField();
        JTextField phone = new JTextField();
        JPasswordField password = new JPasswordField();

        JPanel form = formPanel(
                "Full name", fullName,
                "Email", email,
                "Phone", phone,
                "Password", password
        );

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Add Customer",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            Customer customer = authenticationService.registerCustomer(
                    fullName.getText(),
                    email.getText(),
                    phone.getText(),
                    password.getPassword()
            );
            loadCustomerTable();
            selectCustomerByEmail(customer.getEmail());
            refreshAllDataMetricsOnly();
            showInfo(
                    "Customer Added",
                    "Customer account saved to:\n" + authenticationService.getAccountsFile()
            );
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void selectCustomerByEmail(String email) {
        if (customerTable == null || customerModel == null) {
            return;
        }

        for (int row = 0; row < customerModel.getRowCount(); row++) {
            if (String.valueOf(customerModel.getValueAt(row, 3)).equals(email)) {
                int viewRow = customerTable.convertRowIndexToView(row);
                if (viewRow >= 0) {
                    customerTable.setRowSelectionInterval(viewRow, viewRow);
                    customerTable.scrollRectToVisible(customerTable.getCellRect(viewRow, 0, true));
                }
                return;
            }
        }
    }

    private void operationalAction(String section) {
        String value = JOptionPane.showInputDialog(
                this,
                "Enter a short description for the new " + section.toLowerCase() + " record:",
                "New " + section + " Record",
                JOptionPane.PLAIN_MESSAGE
        );
        if (value != null && !value.isBlank()) {
            showInfo(section, "Record saved for this session:\n" + value.trim());
        }
    }

    private int customerCount() {
        return customerAccounts().size();
    }

    private String[] customerActivity() {
        List<Customer> customers = customerAccounts();
        if (customers.isEmpty()) {
            return new String[]{"No customer accounts registered yet."};
        }
        return customers.stream()
                .limit(5)
                .map(customer -> customer.getFullName() + " • " + customer.getEmail())
                .toArray(String[]::new);
    }

    private List<Customer> customerAccounts() {
        return authenticationService.getAllAccounts().stream()
                .filter(account -> account.getRole() == Customer.Role.CUSTOMER)
                .toList();
    }

    private CustomerInsights customerInsights(Customer customer) {
        int hash = Math.floorMod((customer.getEmail() + customer.getFullName()).hashCode(), Integer.MAX_VALUE);
        int rentals = 3 + hash % 13;
        int points = rentals * 165 + hash % 720;
        double fallbackRating = Math.min(5.0, 3.8 + (hash % 13) / 10.0);
        double rating = ratingRepository.averageForEmail(customer.getEmail()).orElse(fallbackRating);
        int totalSpent = rentals * (430 + hash % 570);
        String tier = points >= 2450 ? "Gold" : points >= 1550 ? "Silver" : "Bronze";
        String since = LocalDate.now()
                .minusDays(30L + hash % 560)
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US));
        String license = hash % 6 == 0 ? "Pending Review" : "Yes (Valid)";
        int savedReviews = ratingRepository.countForEmail(customer.getEmail());
        int reviews = savedReviews > 0 ? savedReviews : Math.max(1, rentals + hash % 14);
        return new CustomerInsights(rentals, points, rating, totalSpent, tier, since, license, reviews);
    }

    private String initials(String fullName) {
        String[] parts = fullName == null ? new String[0] : fullName.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isBlank()) {
            return "VM";
        }
        String first = parts[0].substring(0, 1);
        String second = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (first + second).toUpperCase(Locale.ROOT);
    }

    private String starText(double rating) {
        int filled = Math.max(1, Math.min(5, (int) Math.round(rating)));
        return "★★★★★".substring(0, filled) + "☆☆☆☆☆".substring(0, 5 - filled);
    }

    private String blankFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String formatNumber(int value) {
        return String.format(Locale.US, "%,d", value);
    }

    private String formatCurrency(int value) {
        return "$" + formatNumber(value);
    }

    private void logout() {
        new LoginScreen().setVisible(true);
        dispose();
    }

    private void showInfo(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Velora Motors", JOptionPane.ERROR_MESSAGE);
    }

    private RoundedPanel cardPanel() {
        return new RoundedPanel(16, new Color(5, 12, 18, 232));
    }

    private JPanel transparentFlow(int alignment) {
        JPanel panel = new JPanel(new FlowLayout(alignment, 8, 0));
        panel.setOpaque(false);
        return panel;
    }

    private JLabel label(String text, int size, int style, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", style, size));
        label.setForeground(color);
        return label;
    }

    private static Path2D starShape(double centerX, double centerY, double outerRadius, double innerRadius) {
        Path2D star = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double angle = -Math.PI / 2 + i * Math.PI / 5;
            double radius = i % 2 == 0 ? outerRadius : innerRadius;
            double x = centerX + Math.cos(angle) * radius;
            double y = centerY + Math.sin(angle) * radius;
            if (i == 0) {
                star.moveTo(x, y);
            } else {
                star.lineTo(x, y);
            }
        }
        star.closePath();
        return star;
    }

    private static Color customerAccent(Customer customer) {
        int hash = Math.floorMod((customer.getEmail() + customer.getFullName()).hashCode(), 3);
        return switch (hash) {
            case 0 -> new Color(35, 53, 64);
            case 1 -> new Color(57, 38, 49);
            default -> new Color(48, 48, 35);
        };
    }

    private static final class SidebarCarPanel extends JComponent {

        private final BufferedImage car = loadResourceImage("/images/SIDEBAR_CAR.png");

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setColor(new Color(3, 9, 14, 230));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

            if (car != null) {
                drawCover(g, car, 0, 0, getWidth(), getHeight());
                g.setPaint(new GradientPaint(
                        0, 0, new Color(0, 0, 0, 18),
                        0, getHeight(), new Color(0, 0, 0, 130)
                ));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            } else {
                g.setColor(new Color(214, 160, 66, 30));
                g.fillRoundRect(18, 92, getWidth() - 36, 36, 36, 36);
                g.setColor(GOLD);
                g.setStroke(new BasicStroke(2f));
                g.drawRoundRect(18, 94, getWidth() - 36, 34, 34, 34);
                g.drawOval(42, 122, 19, 19);
                g.drawOval(getWidth() - 62, 122, 19, 19);
            }

            g.setColor(new Color(214, 160, 66, 80));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g.dispose();
        }
    }

    private static final class CustomerTextRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column
            );
            label.setHorizontalAlignment(column == 1 || column == 2 || column == 3 ? SwingConstants.LEFT : SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            label.setForeground(isSelected ? TEXT : new Color(224, 228, 234));
            label.setBackground(isSelected ? new Color(92, 62, 25, 210) : new Color(4, 10, 16));
            label.setBorder(new EmptyBorder(0, column == 1 || column == 2 || column == 3 ? 10 : 4, 0, 4));
            return label;
        }
    }

    private static final class LoyaltyRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column
            );
            String tier = String.valueOf(value);
            label.setText(tier);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setForeground(switch (tier) {
                case "Gold" -> GOLD;
                case "Silver" -> new Color(211, 215, 221);
                default -> new Color(199, 119, 70);
            });
            label.setBackground(isSelected ? new Color(92, 62, 25, 210) : new Color(4, 10, 16));
            label.setBorder(new EmptyBorder(0, 0, 0, 0));
            return label;
        }
    }

    private static final class CustomerAvatarRenderer extends JComponent implements TableCellRenderer {

        private Customer customer;
        private boolean selected;

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            this.customer = value instanceof Customer c ? c : null;
            this.selected = isSelected;
            return this;
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(selected ? new Color(92, 62, 25, 210) : new Color(4, 10, 16));
            g.fillRect(0, 0, getWidth(), getHeight());

            if (customer != null) {
                paintPortrait(g, customer, (getWidth() - 34) / 2, (getHeight() - 34) / 2, 34);
            }
            g.dispose();
        }
    }

    private static final class CustomerPortrait extends JComponent {

        private final Customer customer;
        private final int size;

        CustomerPortrait(Customer customer, int size) {
            this.customer = customer;
            this.size = size;
            setOpaque(false);
            setPreferredSize(new Dimension(size, size));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            paintPortrait(g, customer, (getWidth() - size) / 2, (getHeight() - size) / 2, size);
            g.dispose();
        }
    }

    private static void paintPortrait(Graphics2D g, Customer customer, int x, int y, int size) {
        Color accent = customerAccent(customer);
        g.setPaint(new GradientPaint(x, y, brighten(accent, 18), x + size, y + size, new Color(9, 17, 26)));
        g.fillOval(x, y, size, size);
        g.setColor(PALE);
        g.setStroke(new BasicStroke(Math.max(1.2f, size / 54f)));
        g.drawOval(x, y, size - 1, size - 1);

        int cx = x + size / 2;
        int faceW = Math.max(10, size / 4);
        int faceH = Math.max(12, size / 3);
        boolean longHair = Math.floorMod(customer.getFullName().hashCode(), 4) == 1;

        g.setColor(longHair ? new Color(35, 25, 22) : new Color(40, 28, 22));
        g.fillArc(cx - faceW / 2 - 3, y + size / 5 - 4, faceW + 6, faceH / 2 + 10, 0, 180);
        if (longHair) {
            g.fillRoundRect(cx - faceW / 2 - 7, y + size / 4, faceW + 14, faceH + 8, 12, 12);
        }

        g.setColor(new Color(224, 172, 128));
        g.fillOval(cx - faceW / 2, y + size / 4, faceW, faceH);

        Path2D shirt = new Path2D.Double();
        shirt.moveTo(x + size * 0.25, y + size * 0.88);
        shirt.lineTo(x + size * 0.36, y + size * 0.58);
        shirt.lineTo(cx, y + size * 0.72);
        shirt.lineTo(x + size * 0.64, y + size * 0.58);
        shirt.lineTo(x + size * 0.75, y + size * 0.88);
        shirt.closePath();
        g.setColor(new Color(18, 23, 30));
        g.fill(shirt);

        Path2D collar = new Path2D.Double();
        collar.moveTo(x + size * 0.41, y + size * 0.60);
        collar.lineTo(cx, y + size * 0.78);
        collar.lineTo(x + size * 0.59, y + size * 0.60);
        collar.closePath();
        g.setColor(new Color(236, 238, 239));
        g.fill(collar);

        g.setColor(GOLD);
        g.fillPolygon(
                new int[]{cx, cx - Math.max(1, size / 38), cx, cx + Math.max(1, size / 38)},
                new int[]{y + size * 62 / 100, y + size * 70 / 100, y + size * 78 / 100, y + size * 70 / 100},
                4
        );
    }

    private static final class StarRatingRenderer extends JComponent implements TableCellRenderer {

        private double rating;
        private boolean selected;

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            this.rating = value instanceof Number n ? n.doubleValue() : 0;
            this.selected = isSelected;
            return this;
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(selected ? new Color(92, 62, 25, 210) : new Color(4, 10, 16));
            g.fillRect(0, 0, getWidth(), getHeight());
            int radius = 8;
            int gap = 5;
            int totalWidth = 5 * radius * 2 + 4 * gap;
            paintStars(g, Math.max(6, (getWidth() - totalWidth) / 2), getHeight() / 2, rating, radius);
            g.dispose();
        }
    }

    private static final class StarRatingView extends JComponent {

        private final double rating;

        StarRatingView(double rating) {
            this.rating = rating;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            paintStars(g, 4, getHeight() / 2, rating, 9);
            g.dispose();
        }
    }

    private static void paintStars(Graphics2D g, int x, int centerY, double rating, int radius) {
        int filled = Math.max(1, Math.min(5, (int) Math.round(rating)));
        int gap = 5;
        for (int i = 0; i < 5; i++) {
            int cx = x + i * (radius * 2 + gap) + radius;
            Path2D star = starShape(cx, centerY, radius, radius * 0.46);
            g.setColor(i < filled ? GOLD : new Color(214, 160, 66, 36));
            g.fill(star);
            g.setColor(new Color(214, 160, 66, i < filled ? 140 : 95));
            g.draw(star);
        }
    }

    private static final class MiniSparkline extends JComponent {

        MiniSparkline() {
            setOpaque(false);
            setPreferredSize(new Dimension(150, 35));
            setMaximumSize(new Dimension(150, 35));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int[] values = {11, 15, 13, 22, 16, 19, 25, 21, 24};
            int left = 2;
            int bottom = getHeight() - 7;
            int step = Math.max(10, (getWidth() - 18) / (values.length - 1));
            Path2D line = new Path2D.Double();
            for (int i = 0; i < values.length; i++) {
                double x = left + i * step;
                double y = bottom - values[i] * 0.75;
                if (i == 0) {
                    line.moveTo(x, y);
                } else {
                    line.lineTo(x, y);
                }
            }
            g.setColor(GOLD);
            g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(line);
            for (int i = 0; i < values.length; i++) {
                int x = left + i * step;
                int y = (int) Math.round(bottom - values[i] * 0.75);
                g.fillOval(x - 2, y - 2, 4, 4);
            }
            g.dispose();
        }
    }

    private static final class CustomerStatIcon extends JComponent {

        private final String kind;

        CustomerStatIcon(String kind) {
            this.kind = kind;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(new Color(214, 160, 66, 13));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g.setColor(new Color(214, 160, 66, 22));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

            g.setColor(GOLD);
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            switch (kind) {
                case "New" -> drawShield(g, cx, cy);
                case "Loyal" -> drawCrown(g, cx, cy);
                default -> drawUsers(g, cx, cy, "Active".equals(kind));
            }

            g.dispose();
        }

        private void drawUsers(Graphics2D g, int cx, int cy, boolean doubleUser) {
            if (doubleUser) {
                g.drawOval(cx - 14, cy - 11, 10, 10);
                g.drawArc(cx - 18, cy + 1, 18, 15, 20, 140);
                g.drawOval(cx + 4, cy - 11, 10, 10);
                g.drawArc(cx, cy + 1, 18, 15, 20, 140);
            } else {
                g.drawOval(cx - 6, cy - 13, 12, 12);
                g.drawArc(cx - 13, cy + 2, 26, 18, 20, 140);
                g.drawOval(cx + 9, cy - 8, 8, 8);
                g.drawArc(cx + 5, cy + 3, 16, 13, 20, 120);
            }
        }

        private void drawShield(Graphics2D g, int cx, int cy) {
            Path2D shield = new Path2D.Double();
            shield.moveTo(cx, cy - 17);
            shield.lineTo(cx + 14, cy - 11);
            shield.lineTo(cx + 11, cy + 7);
            shield.lineTo(cx, cy + 18);
            shield.lineTo(cx - 11, cy + 7);
            shield.lineTo(cx - 14, cy - 11);
            shield.closePath();
            g.draw(shield);
            g.drawLine(cx, cy - 8, cx, cy + 7);
            g.drawLine(cx - 5, cy - 2, cx, cy + 3);
            g.drawLine(cx + 6, cy - 5, cx, cy + 3);
        }

        private void drawCrown(Graphics2D g, int cx, int cy) {
            Path2D crown = new Path2D.Double();
            crown.moveTo(cx - 17, cy + 9);
            crown.lineTo(cx - 14, cy - 10);
            crown.lineTo(cx - 5, cy + 1);
            crown.lineTo(cx, cy - 14);
            crown.lineTo(cx + 5, cy + 1);
            crown.lineTo(cx + 14, cy - 10);
            crown.lineTo(cx + 17, cy + 9);
            crown.closePath();
            g.draw(crown);
            g.drawLine(cx - 14, cy + 12, cx + 14, cy + 12);
            g.fillOval(cx - 16, cy - 12, 4, 4);
            g.fillOval(cx - 2, cy - 17, 4, 4);
            g.fillOval(cx + 12, cy - 12, 4, 4);
        }
    }

    private static final class ProfileTab extends JComponent {

        private final String text;
        private final String icon;
        private final boolean active;

        ProfileTab(String text, String icon, boolean active) {
            this.text = text;
            this.icon = icon;
            this.active = active;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g.setColor(active ? new Color(214, 160, 66, 18) : new Color(6, 13, 20, 150));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(new Color(214, 160, 66, active ? 120 : 45));
            g.drawLine(getWidth() - 1, 8, getWidth() - 1, getHeight() - 8);
            if (active) {
                g.fillRect(0, getHeight() - 3, getWidth(), 3);
            }

            Color color = active ? GOLD : new Color(178, 184, 195);
            drawProfileIcon(g, icon, getWidth() / 2 - 8, 8, 16, color);

            g.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 9));
            g.setColor(color);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, Math.min(getHeight() - 9, 40));
            g.dispose();
        }
    }

    private static final class ProfileCardPanel extends JPanel {

        ProfileCardPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D shape = new RoundRectangle2D.Double(
                    .5, .5, getWidth() - 1, getHeight() - 1, 8, 8
            );
            g.setPaint(new GradientPaint(
                    0, 0, new Color(9, 16, 23, 238),
                    getWidth(), getHeight(), new Color(4, 10, 16, 238)
            ));
            g.fill(shape);
            g.setColor(new Color(214, 160, 66, 28));
            g.draw(shape);
            g.setColor(new Color(255, 255, 255, 9));
            g.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 7, 7);
            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class ProfileMiniIcon extends JComponent {

        private final String icon;
        private final int size;

        ProfileMiniIcon(String icon, int size) {
            this.icon = icon;
            this.size = size;
            setOpaque(false);
            setPreferredSize(new Dimension(size, size));
            setMaximumSize(new Dimension(size, size));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawProfileIcon(g, icon, (getWidth() - 22) / 2, (getHeight() - 22) / 2, 22, GOLD);
            g.dispose();
        }
    }

    private static final class LoyaltyRingPanel extends JComponent {

        private final int points;

        LoyaltyRingPanel(int points) {
            this.points = points;
            setOpaque(false);
            setPreferredSize(new Dimension(160, 130));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int size = Math.min(88, Math.min(getWidth() - 34, getHeight() - 38));
            int x = (getWidth() - size) / 2;
            int y = 8;
            int arc = Math.min(315, Math.max(28, (int) Math.round((points / 6000.0) * 315)));

            g.setStroke(new BasicStroke(6.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(214, 160, 66, 34));
            g.drawArc(x, y, size, size, 218, -315);
            g.setPaint(new GradientPaint(x, y, new Color(238, 201, 139), x + size, y + size, new Color(154, 98, 31)));
            g.drawArc(x, y, size, size, 218, -arc);

            g.setColor(TEXT);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 19));
            String pointsText = String.format(Locale.US, "%,d", points);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(pointsText, x + (size - fm.stringWidth(pointsText)) / 2, y + size / 2 + 1);

            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g.setColor(MUTED);
            String label = "Points";
            fm = g.getFontMetrics();
            g.drawString(label, x + (size - fm.stringWidth(label)) / 2, y + size / 2 + 17);

            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g.setColor(GOLD);
            String next = "Next Tier: Platinum";
            fm = g.getFontMetrics();
            g.drawString(next, (getWidth() - fm.stringWidth(next)) / 2, getHeight() - 25);

            g.setColor(new Color(216, 218, 223));
            String remaining = String.format(Locale.US, "%,d points to go", Math.max(0, 6000 - points));
            fm = g.getFontMetrics();
            g.drawString(remaining, (getWidth() - fm.stringWidth(remaining)) / 2, getHeight() - 10);
            g.dispose();
        }
    }

    private static void drawProfileIcon(Graphics2D g, String icon, int x, int y, int size, Color color) {
        g.setColor(color);
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int cx = x + size / 2;
        int cy = y + size / 2;

        switch (icon) {
            case "USERS" -> {
                g.drawOval(cx - 8, cy - 8, 7, 7);
                g.drawArc(cx - 12, cy - 1, 15, 12, 18, 144);
                g.drawOval(cx + 2, cy - 8, 7, 7);
                g.drawArc(cx - 2, cy - 1, 15, 12, 18, 144);
            }
            case "CAR" -> {
                int bodyY = y + size / 2;
                g.drawRoundRect(x + 2, bodyY - 3, size - 4, 8, 7, 7);
                g.drawLine(x + 6, bodyY - 3, x + 9, y + 5);
                g.drawLine(x + size - 6, bodyY - 3, x + size - 9, y + 5);
                g.drawLine(x + 9, y + 5, x + size - 9, y + 5);
                g.fillOval(x + 5, bodyY + 4, 4, 4);
                g.fillOval(x + size - 9, bodyY + 4, 4, 4);
            }
            case "FILE" -> {
                g.drawRoundRect(x + 4, y + 3, size - 8, size - 6, 3, 3);
                g.drawLine(x + 8, y + 8, x + size - 8, y + 8);
                g.drawLine(x + 8, y + 12, x + size - 8, y + 12);
                g.drawLine(x + 8, y + 16, x + size - 11, y + 16);
                g.drawOval(cx - 3, y + size - 8, 6, 4);
            }
            case "CROWN" -> {
                Path2D crown = new Path2D.Double();
                crown.moveTo(x + 3, y + size - 6);
                crown.lineTo(x + 5, y + 7);
                crown.lineTo(cx - 4, y + 13);
                crown.lineTo(cx, y + 4);
                crown.lineTo(cx + 4, y + 13);
                crown.lineTo(x + size - 5, y + 7);
                crown.lineTo(x + size - 3, y + size - 6);
                crown.closePath();
                g.draw(crown);
                g.drawLine(x + 5, y + size - 3, x + size - 5, y + size - 3);
            }
            case "SHIELD" -> {
                Path2D shield = new Path2D.Double();
                shield.moveTo(cx, y + 2);
                shield.lineTo(x + size - 3, y + 7);
                shield.lineTo(x + size - 5, y + size - 6);
                shield.lineTo(cx, y + size - 2);
                shield.lineTo(x + 5, y + size - 6);
                shield.lineTo(x + 3, y + 7);
                shield.closePath();
                g.draw(shield);
                g.drawLine(cx, y + 7, cx, y + size - 8);
            }
            default -> {
                Path2D star = starShape(cx, cy, size / 2.2, size / 4.8);
                g.draw(star);
            }
        }
    }

    private record CustomerInsights(
            int rentals,
            int points,
            double rating,
            int totalSpent,
            String tier,
            String customerSince,
            String licenseStatus,
            int reviewCount
    ) {
    }

    private static final class DashboardBackground extends JPanel {

        private boolean warmMode;

        DashboardBackground() {
            setOpaque(true);
        }

        void toggleWarmMode() {
            warmMode = !warmMode;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics raw) {
            super.paintComponent(raw);
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setPaint(new GradientPaint(
                    0, 0, warmMode ? new Color(12, 9, 5) : BACKGROUND,
                    getWidth(), getHeight(), warmMode ? new Color(17, 12, 6) : new Color(3, 10, 16)
            ));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(new Color(214, 160, 66, warmMode ? 18 : 8));
            g.fillOval(getWidth() - 500, -260, 660, 560);
            g.dispose();
        }
    }

    private static final class DashboardScrollPanel extends JPanel implements Scrollable {

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
            return 120;
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

    private static final class DarkScrollBarUI extends BasicScrollBarUI {

        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(214, 160, 66, 100);
            trackColor = new Color(2, 7, 12);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return hiddenScrollButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return hiddenScrollButton();
        }

        private JButton hiddenScrollButton() {
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
            g.setColor(new Color(214, 160, 66, 105));
            g.fillRoundRect(bounds.x + 1, bounds.y, Math.max(3, bounds.width - 2),
                    bounds.height, bounds.width, bounds.width);
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
            g.setColor(new Color(0, 0, 0, 72));
            g.fillRoundRect(5, 7, Math.max(0, getWidth() - 10), Math.max(0, getHeight() - 10), radius, radius);
            g.setPaint(new GradientPaint(
                    0, 0, brighten(fill, 16),
                    getWidth(), getHeight(), fill
            ));
            g.fill(shape);
            g.setPaint(new GradientPaint(
                    0, 0, new Color(255, 235, 181, 38),
                    getWidth(), 0, new Color(214, 160, 66, 4)
            ));
            g.fill(shape);
            g.setColor(new Color(214, 160, 66, 82));
            g.setStroke(new BasicStroke(1f));
            g.draw(shape);
            g.setColor(new Color(255, 255, 255, 13));
            g.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, Math.max(6, radius - 3), Math.max(6, radius - 3));
            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class MetricIconLabel extends JLabel {

        private int value;
        private String caption;

        MetricIconLabel(String value, String caption) {
            this.value = Integer.parseInt(value);
            this.caption = caption;
            setOpaque(false);
        }

        void setMetric(int value, String caption) {
            this.value = value;
            this.caption = caption;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cx = getWidth() / 2;
            int height = Math.max(32, getHeight());
            int iconY = Math.max(9, Math.min(18, height / 5));
            int captionY = Math.max(23, height / 2 + 2);
            int valueY = Math.max(35, height - 3);
            g.setColor(GOLD);
            g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            String icon = switch (caption) {
                case "Fleet", "Available" -> "CAR";
                case "Customers" -> "USERS";
                default -> "TOOLS";
            };
            drawCompactIcon(g, icon, cx, iconY, 14);
            g.setColor(MUTED);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            drawCentered(g, caption, cx, captionY);
            g.setColor(PALE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 17));
            drawCentered(g, String.valueOf(value), cx, valueY);
            g.setColor(new Color(214, 160, 66, 24));
            g.drawLine(getWidth() - 1, 10, getWidth() - 1, getHeight() - 10);
            g.dispose();
        }
    }

    private static final class ActivityRow extends JPanel {

        private final String title;
        private final String subtitle;
        private final String time;
        private final String icon;

        ActivityRow(String title, String subtitle, String time, String icon) {
            this.title = title;
            this.subtitle = subtitle;
            this.time = time;
            this.icon = icon;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cy = getHeight() / 2;
            g.setColor(new Color(76, 143, 75, 42));
            g.fillOval(0, cy - 11, 22, 22);
            g.setColor(new Color(104, 190, 102));
            g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawCompactIcon(g, icon, 11, cy, 11);
            g.setColor(new Color(226, 229, 233));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            g.drawString(title, 31, cy - 1);
            g.setColor(MUTED);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 8));
            g.drawString(subtitle, 31, cy + 10);
            int timeWidth = g.getFontMetrics().stringWidth(time);
            g.drawString(time, getWidth() - timeWidth - 2, cy + 3);
            g.dispose();
        }
    }

    private static final class MenuButton extends JButton {

        private final String iconKey;
        private boolean active;

        MenuButton(String text, String iconKey) {
            super(text);
            this.iconKey = iconKey;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(new EmptyBorder(0, 55, 0, 10));
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setForeground(TEXT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        void setActive(boolean active) {
            this.active = active;
            setForeground(active ? new Color(30, 21, 10) : TEXT);
            setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 14));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean hover = getModel().isRollover();
            if (active) {
                g.setPaint(new GradientPaint(
                        0, 0, new Color(164, 110, 39),
                        getWidth(), getHeight(), new Color(241, 207, 144)
                ));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            } else if (hover) {
                g.setColor(new Color(214, 160, 66, 22));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }

            g.setColor(active ? new Color(45, 30, 13) : GOLD);
            g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawCompactIcon(g, iconKey, 27, getHeight() / 2, 17);
            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class QuickCard extends JButton {

        private final String title;
        private final String description;
        private final BufferedImage thumbnail;

        QuickCard(String title, String description) {
            this.title = title;
            this.description = description;
            this.thumbnail = "Vehicles".equals(title)
                    ? loadResourceImage("/images/featured-roadster-clean.png")
                    : null;
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
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int arc = 15;
            RoundRectangle2D card = new RoundRectangle2D.Double(
                    .5, .5, getWidth() - 1, getHeight() - 1, arc, arc
            );
            g.setColor(new Color(0, 0, 0, 78));
            g.fillRoundRect(4, 5, Math.max(0, getWidth() - 8), Math.max(0, getHeight() - 7), arc, arc);
            g.setPaint(new GradientPaint(
                    0, 0, new Color(15, 24, 30, getModel().isRollover() ? 252 : 238),
                    getWidth(), getHeight(), new Color(2, 8, 13, 246)
            ));
            g.fill(card);
            g.setPaint(new GradientPaint(
                    0, 0, new Color(255, 235, 185, getModel().isRollover() ? 34 : 18),
                    getWidth(), 0, new Color(214, 160, 66, 3)
            ));
            g.fill(card);
            g.setColor(new Color(214, 160, 66, getModel().isRollover() ? 156 : 72));
            g.setStroke(new BasicStroke(getModel().isRollover() ? 1.4f : 1f));
            g.draw(card);

            int circle = Math.min(57, Math.max(42, getHeight() - 18));
            int circleY = (getHeight() - circle) / 2;
            int textX = 14 + circle + 12;
            int titleY = Math.max(27, getHeight() / 2 - 5);
            int descriptionY = titleY + 18;
            if (thumbnail != null) {
                java.awt.Shape previousClip = g.getClip();
                g.clip(new java.awt.geom.Ellipse2D.Double(14, circleY, circle, circle));
                int sourceX1 = (int) (thumbnail.getWidth() * .46);
                int sourceX2 = (int) (thumbnail.getWidth() * .94);
                g.drawImage(
                        thumbnail,
                        14, circleY, 14 + circle, circleY + circle,
                        sourceX1, 0, sourceX2, thumbnail.getHeight(),
                        null
                );
                g.setClip(previousClip);
            } else {
                g.setColor(new Color(214, 160, 66, 18));
                g.fillOval(14, circleY, circle, circle);
            }
            g.setColor(new Color(214, 160, 66, 110));
            g.drawOval(14, circleY, circle, circle);
            if (thumbnail == null) {
                g.setColor(GOLD);
                g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                drawCompactIcon(
                        g,
                        switch (title) {
                            case "Rentals" -> "CAL";
                            case "Customers" -> "USERS";
                            case "Billing" -> "BILL";
                            case "Maintenance" -> "TOOLS";
                            default -> "CHART";
                        },
                        14 + circle / 2,
                        circleY + circle / 2,
                        Math.min(20, circle / 2)
                );
            }

            g.setColor(new Color(249, 249, 250));
            g.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g.drawString(title, textX, titleY);
            g.setColor(new Color(184, 190, 199));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 8));
            drawWrapped(g, description, textX, descriptionY,
                    Math.max(54, getWidth() - textX - 42), 13);

            int arrowSize = 25;
            int arrowX = getWidth() - arrowSize - 13;
            int arrowY = (getHeight() - arrowSize) / 2;
            g.setPaint(new GradientPaint(
                    arrowX, arrowY, PALE,
                    arrowX + arrowSize, arrowY + arrowSize, new Color(151, 101, 35)
            ));
            g.fillOval(arrowX, arrowY, arrowSize, arrowSize);
            g.setColor(new Color(33, 23, 10));
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int ax = arrowX + arrowSize / 2;
            int ay = arrowY + arrowSize / 2;
            g.drawLine(ax - 4, ay, ax + 4, ay);
            g.drawLine(ax + 1, ay - 4, ax + 5, ay);
            g.drawLine(ax + 1, ay + 4, ax + 5, ay);
            g.setColor(new Color(33, 23, 10, 0));
            g.setFont(new Font("Segoe UI", Font.BOLD, 1));
            g.drawString("›", getWidth() - 31, getHeight() - 20);
            g.dispose();
        }
    }

    private static final class LuxuryCarCard extends JButton {

        private final BufferedImage image = loadResourceImage(
                "/assets/backgrounds/luxury-car-week-v2.png"
        );

        LuxuryCarCard() {
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Open vehicle details");
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            RoundRectangle2D card = new RoundRectangle2D.Double(
                    .5, .5, getWidth() - 1, getHeight() - 1, 16, 16
            );
            g.clip(card);
            g.setPaint(new GradientPaint(
                    0, 0, new Color(10, 17, 22),
                    getWidth(), getHeight(), new Color(1, 5, 9)
            ));
            g.fillRect(0, 0, getWidth(), getHeight());
            if (image != null) {
                // The dashboard hero is intentionally fitted to the full frame.
                // Its source aspect ratio is already banner-shaped; using "cover"
                // here cropped the wheels and the wet-floor reflection.
                g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
                g.setPaint(new GradientPaint(
                        0, 0, new Color(2, 8, 13, 245),
                        getWidth() * .82f, 0, new Color(2, 8, 13, 20)
                ));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setPaint(new GradientPaint(
                        0, 0, new Color(2, 8, 13, 35),
                        0, getHeight(), new Color(2, 8, 13, 120)
                ));
                g.fillRect(0, 0, getWidth(), getHeight());
            }

            g.setColor(new Color(214, 160, 66, getModel().isRollover() ? 155 : 90));
            g.setStroke(new BasicStroke(1f));
            g.draw(card);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            g.setColor(new Color(205, 208, 214));
            g.drawString("LUXURY CAR OF THE WEEK", 15, 23);
            g.setFont(new Font("Segoe UI", Font.BOLD, 16));
            g.setColor(GOLD);
            g.drawString("BMW M8 Competition", 15, 48);

            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g.setColor(new Color(221, 223, 228));
            g.drawString("4.4L V8 Twin Turbo", 33, 72);
            g.drawString("617 HP   •   305 km/h", 33, 91);
            g.setColor(GOLD);
            g.fillOval(16, 64, 7, 7);
            g.fillOval(16, 84, 7, 7);

            int buttonY = getHeight() - 38;
            g.setColor(new Color(3, 8, 12, 225));
            g.fillRoundRect(15, buttonY, 142, 28, 9, 9);
            g.setColor(new Color(214, 160, 66, 145));
            g.drawRoundRect(15, buttonY, 142, 28, 9, 9);
            g.setColor(PALE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g.drawString("VIEW DETAILS", 29, buttonY + 18);
            g.drawLine(126, buttonY + 14, 142, buttonY + 14);
            g.drawLine(137, buttonY + 9, 142, buttonY + 14);
            g.drawLine(142, buttonY + 14, 137, buttonY + 19);
            g.dispose();
        }
    }

    private static class GoldOutlineButton extends JButton {

        GoldOutlineButton(String text) {
            super(text);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(PALE);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isRollover()) {
                g.setColor(new Color(214, 160, 66, 22));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 11, 11);
            }
            g.setColor(new Color(214, 160, 66, 125));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 11, 11);
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
            setForeground(new Color(35, 24, 10));
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(125, 38));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(
                    0, 0, getModel().isRollover() ? new Color(248, 216, 158) : new Color(221, 176, 100),
                    getWidth(), getHeight(), new Color(173, 116, 44)
            ));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 11, 11);
            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class HeaderButton extends JButton {

        private final String icon;

        HeaderButton(String icon) {
            super("");
            this.icon = icon;
            setPreferredSize(new Dimension(44, 43));
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(TEXT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(6, 13, 20, getModel().isRollover() ? 248 : 220));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g.setColor(new Color(214, 160, 66, 75));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            g.setColor(TEXT);
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (icon) {
                case "MENU" -> {
                    g.drawLine(cx - 8, cy - 7, cx + 8, cy - 7);
                    g.drawLine(cx - 8, cy, cx + 8, cy);
                    g.drawLine(cx - 8, cy + 7, cx + 8, cy + 7);
                }
                case "MOON" -> {
                    g.drawArc(cx - 9, cy - 10, 18, 20, 65, 235);
                    g.drawArc(cx - 3, cy - 10, 13, 16, 90, 180);
                }
                case "BELL" -> {
                    g.drawArc(cx - 7, cy - 10, 14, 13, 0, 180);
                    g.drawLine(cx - 7, cy - 4, cx - 7, cy + 5);
                    g.drawLine(cx + 7, cy - 4, cx + 7, cy + 5);
                    g.drawLine(cx - 10, cy + 5, cx + 10, cy + 5);
                    g.fillOval(cx - 2, cy + 8, 4, 3);
                    g.setColor(GOLD);
                    g.fillOval(cx + 7, cy - 11, 7, 7);
                }
                default -> {
                    g.drawRoundRect(cx - 10, cy - 7, 20, 14, 3, 3);
                    g.drawLine(cx - 10, cy - 6, cx, cy + 1);
                    g.drawLine(cx + 10, cy - 6, cx, cy + 1);
                    g.setColor(GOLD);
                    g.fillOval(cx + 7, cy - 11, 7, 7);
                }
            }
            g.dispose();
        }
    }

    private static final class ProfileButton extends JButton {

        private final String managerName;

        ProfileButton(String managerName) {
            super("");
            this.managerName = managerName;
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

            g.setColor(new Color(5, 12, 18, getModel().isRollover() ? 248 : 226));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g.setColor(new Color(214, 160, 66, getModel().isRollover() ? 118 : 62));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

            int avatarSize = 31;
            int avatarX = 8;
            int avatarY = (getHeight() - avatarSize) / 2;
            g.setColor(new Color(17, 29, 39));
            g.fillOval(avatarX, avatarY, avatarSize, avatarSize);
            g.setColor(PALE);
            g.drawOval(avatarX, avatarY, avatarSize, avatarSize);
            g.setColor(new Color(223, 179, 143));
            g.fillOval(avatarX + 10, avatarY + 6, 11, 12);
            g.setColor(new Color(38, 29, 24));
            g.fillArc(avatarX + 9, avatarY + 4, 13, 11, 0, 180);
            g.setColor(new Color(232, 235, 239));
            g.fillArc(avatarX + 6, avatarY + 17, 19, 13, 0, 180);

            String displayName = managerName;
            Font nameFont = new Font("Segoe UI", Font.BOLD, 10);
            g.setFont(nameFont);
            FontMetrics metrics = g.getFontMetrics();
            int maxNameWidth = getWidth() - 78;
            while (metrics.stringWidth(displayName) > maxNameWidth && displayName.length() > 4) {
                displayName = displayName.substring(0, displayName.length() - 2) + "…";
            }
            g.setColor(PALE);
            g.drawString("Welcome " + displayName, 48, 16);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 8));
            g.setColor(MUTED);
            g.drawString("Administrator", 48, 29);

            int arrowX = getWidth() - 17;
            int arrowY = getHeight() / 2;
            g.setColor(TEXT);
            g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(arrowX - 4, arrowY - 2, arrowX, arrowY + 2);
            g.drawLine(arrowX, arrowY + 2, arrowX + 4, arrowY - 2);
            g.dispose();
        }
    }

    private static final class WindowControlButton extends JButton {

        private final boolean close;
        private final String symbol;

        WindowControlButton(String text, boolean close) {
            super("");
            this.close = close;
            this.symbol = text;
            setPreferredSize(new Dimension(31, 31));
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(close ? new Color(255, 174, 170) : PALE);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean hover = getModel().isRollover();
            Color fillStart = close && hover ? new Color(118, 28, 31, 238) : new Color(7, 14, 20, 228);
            Color fillEnd = close && hover ? new Color(180, 45, 50, 238) : new Color(2, 8, 13, 238);
            g.setPaint(new GradientPaint(0, 0, fillStart, getWidth(), getHeight(), fillEnd));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g.setColor(close && hover ? new Color(255, 120, 126, 160) : new Color(214, 160, 66, hover ? 125 : 64));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g.setColor(close ? new Color(255, 174, 170) : PALE);
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            if ("×".equals(symbol)) {
                g.drawLine(cx - 6, cy - 6, cx + 6, cy + 6);
                g.drawLine(cx + 6, cy - 6, cx - 6, cy + 6);
            } else if ("▢".equals(symbol)) {
                g.drawRoundRect(cx - 7, cy - 7, 14, 14, 3, 3);
            } else {
                g.drawLine(cx - 7, cy, cx + 7, cy);
            }
            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class SearchField extends JTextField {

        private boolean showingPlaceholder = true;

        SearchField() {
            super("Search customers...");
            setOpaque(false);
            setForeground(MUTED);
            setCaretColor(PALE);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setBorder(new EmptyBorder(0, 18, 0, 45));

            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    if (showingPlaceholder) {
                        setText("");
                        setForeground(TEXT);
                        showingPlaceholder = false;
                    }
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    if (getText().isBlank()) {
                        setText("Search customers...");
                        setForeground(MUTED);
                        showingPlaceholder = true;
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(5, 12, 18, 235));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g.setColor(new Color(255, 255, 255, isFocusOwner() ? 70 : 28));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
            g.setColor(PALE);
            g.setStroke(new BasicStroke(1.6f));
            g.drawOval(getWidth() - 31, 12, 13, 13);
            g.drawLine(getWidth() - 19, 24, getWidth() - 13, 30);
            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class BrandSignature extends JComponent {

        private final BufferedImage logo = loadResourceImage("/assets/icons/velora-logo-gold.png");

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            int cx = getWidth() / 2 - 58;
            int cy = 26;
            if (logo != null) {
                g.drawImage(logo, cx - 48, cy - 20, 96, 42, null);
            } else {
                drawVeloraWingLogo(g, cx, cy, 72, new Color(222, 166, 72), true);
            }
            g.setColor(new Color(226, 229, 235));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            g.drawString("VELORA MOTORS", cx + 64, 25);
            g.setColor(new Color(170, 177, 188));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 8));
            g.drawString("PREMIUM BMW VEHICLE RENTAL", cx + 66, 40);
            g.dispose();
        }
    }

    private static final class BrandMark extends JComponent {

        private final BufferedImage logo = loadResourceImage("/assets/icons/velora-logo-gold.png");

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            if (logo != null) {
                int w = Math.min(getWidth() - 16, 170);
                int h = w * logo.getHeight() / logo.getWidth();
                int availableHeight = Math.max(30, getHeight() - 6);
                if (h > availableHeight) {
                    h = availableHeight;
                    w = h * logo.getWidth() / logo.getHeight();
                }
                int x = (getWidth() - w) / 2;
                int y = (getHeight() - h) / 2;
                g.drawImage(logo, x, y, w, h, null);
            } else {
                drawVeloraWingLogo(g, getWidth() / 2, getHeight() / 2 + 3, 150, PALE, true);
            }
            g.dispose();
        }
    }

    private static final class Avatar extends JComponent {

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(getWidth(), getHeight()) - 2;
            g.setColor(new Color(13, 24, 33));
            g.fillOval(1, 1, size, size);
            g.setColor(PALE);
            g.setStroke(new BasicStroke(1.4f));
            g.drawOval(1, 1, size, size);

            int cx = size / 2 + 1;
            g.setColor(new Color(226, 181, 143));
            g.fillOval(cx - 8, 10, 16, 20);
            g.setColor(new Color(43, 30, 25));
            g.fillArc(cx - 9, 7, 18, 18, 0, 180);
            g.fillArc(cx - 8, 19, 16, 12, 180, 180);
            g.setColor(new Color(240, 242, 245));
            Path2D shirt = new Path2D.Double();
            shirt.moveTo(cx - 15, 46);
            shirt.lineTo(cx - 11, 34);
            shirt.lineTo(cx - 4, 30);
            shirt.lineTo(cx, 41);
            shirt.lineTo(cx + 4, 30);
            shirt.lineTo(cx + 11, 34);
            shirt.lineTo(cx + 15, 46);
            shirt.closePath();
            g.fill(shirt);
            g.setColor(new Color(19, 31, 42));
            g.fillPolygon(new int[]{cx - 17, cx - 4, cx - 1, cx - 7, cx - 17},
                    new int[]{46, 30, 39, 46, 46}, 5);
            g.fillPolygon(new int[]{cx + 17, cx + 4, cx + 1, cx + 7, cx + 17},
                    new int[]{46, 30, 39, 46, 46}, 5);
            g.setColor(GOLD);
            g.fillPolygon(new int[]{cx, cx - 2, cx, cx + 2},
                    new int[]{34, 39, 45, 39}, 4);
            g.dispose();
        }
    }

    private static final class AnnouncementCard extends JPanel {

        private final GoldOutlineButton offerButton = new GoldOutlineButton("View Offers");

        AnnouncementCard() {
            setOpaque(false);
            setLayout(null);
            offerButton.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            add(offerButton);
        }

        JButton getOfferButton() {
            return offerButton;
        }

        @Override
        public void doLayout() {
            int buttonWidth = Math.max(96, Math.min(112, getWidth() / 3));
            offerButton.setBounds(15, Math.max(116, getHeight() - 43), buttonWidth, 29);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            super.paintComponent(raw);

            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int w = getWidth();
            int h = getHeight();
            RoundRectangle2D card = new RoundRectangle2D.Double(
                    .5, .5, Math.max(0, w - 1), Math.max(0, h - 1), 15, 15
            );

            g.setColor(new Color(0, 0, 0, 82));
            g.fillRoundRect(4, 6, Math.max(0, w - 8), Math.max(0, h - 8), 15, 15);
            g.setPaint(new GradientPaint(
                    0, 0, new Color(7, 13, 18, 250),
                    w, h, new Color(2, 7, 11, 252)
            ));
            g.fill(card);

            g.setPaint(new GradientPaint(
                    0, 0, new Color(214, 160, 66, 17),
                    w * .58f, h, new Color(214, 160, 66, 0)
            ));
            g.fill(card);

            drawLeaves(g, w, h);
            drawMegaphone(g, 58, Math.min(82, h / 2 + 2));

            g.setColor(TEXT);
            g.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g.drawString("Announcements", 15, 25);

            int textX = 106;
            g.setColor(PALE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g.drawString("Summer Discount!", textX, 62);

            g.setColor(new Color(190, 196, 205));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g.drawString("Enjoy up to 30% off on", textX, 82);
            g.drawString("selected models.", textX, 98);

            g.setColor(new Color(214, 160, 66, 70));
            g.setStroke(new BasicStroke(1f));
            g.draw(card);
            g.setColor(new Color(255, 255, 255, 10));
            g.drawRoundRect(2, 2, Math.max(0, w - 5), Math.max(0, h - 5), 12, 12);
            g.dispose();
        }

        private void drawMegaphone(Graphics2D g, int cx, int cy) {
            g.setColor(new Color(214, 160, 66, 12));
            g.fillOval(cx - 35, cy - 35, 70, 70);
            g.setColor(new Color(214, 160, 66, 44));
            g.setStroke(new BasicStroke(1f));
            g.drawOval(cx - 31, cy - 31, 62, 62);

            Path2D horn = new Path2D.Double();
            horn.moveTo(cx - 14, cy - 10);
            horn.curveTo(cx + 1, cy - 13, cx + 11, cy - 20, cx + 20, cy - 25);
            horn.lineTo(cx + 20, cy + 17);
            horn.curveTo(cx + 9, cy + 11, cx, cy + 7, cx - 14, cy + 6);
            horn.closePath();
            g.setPaint(new GradientPaint(
                    cx - 18, cy - 17, new Color(249, 210, 137),
                    cx + 22, cy + 20, new Color(151, 91, 24)
            ));
            g.fill(horn);

            g.setColor(new Color(246, 202, 124));
            g.fillRoundRect(cx - 24, cy - 11, 12, 19, 6, 6);
            g.setColor(new Color(119, 71, 20));
            g.setStroke(new BasicStroke(1.1f));
            g.drawLine(cx + 18, cy - 24, cx + 18, cy + 16);

            Path2D handle = new Path2D.Double();
            handle.moveTo(cx - 5, cy + 7);
            handle.lineTo(cx + 5, cy + 9);
            handle.lineTo(cx + 1, cy + 27);
            handle.quadTo(cx - 4, cy + 30, cx - 9, cy + 26);
            handle.closePath();
            g.setPaint(new GradientPaint(
                    cx - 8, cy + 8, new Color(195, 126, 40),
                    cx + 4, cy + 27, new Color(111, 66, 20)
            ));
            g.fill(handle);

            g.setColor(new Color(232, 181, 94, 165));
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawArc(cx + 20, cy - 17, 10, 24, -58, 116);
            g.drawArc(cx + 22, cy - 23, 17, 36, -58, 116);
        }

        private void drawLeaves(Graphics2D g, int w, int h) {
            int baseX = w - 91;
            int baseY = h + 6;

            g.setColor(new Color(189, 137, 66, 38));
            g.setStroke(new BasicStroke(1.15f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            Path2D stem = new Path2D.Double();
            stem.moveTo(baseX, baseY);
            stem.curveTo(w - 88, h - 42, w - 59, 55, w - 31, 24);
            g.draw(stem);
            g.drawLine(baseX + 5, h - 31, w - 122, h - 57);
            g.drawLine(baseX + 15, h - 61, w - 116, h - 90);
            g.drawLine(baseX + 25, h - 88, w - 89, h - 119);
            g.drawLine(baseX + 28, h - 44, w - 40, h - 72);
            g.drawLine(baseX + 39, h - 74, w - 24, h - 101);

            drawLeaf(g, w - 119, h - 59, 32, 13, -.20, 42);
            drawLeaf(g, w - 113, h - 92, 34, 14, -.39, 47);
            drawLeaf(g, w - 87, h - 120, 30, 13, -.74, 38);
            drawLeaf(g, w - 43, h - 74, 34, 14, -2.72, 48);
            drawLeaf(g, w - 27, h - 102, 34, 14, -2.58, 42);
            drawLeaf(g, w - 57, h - 46, 31, 13, -2.78, 36);
            drawLeaf(g, w - 34, h - 125, 31, 13, -2.34, 32);
        }

        private void drawLeaf(
                Graphics2D g,
                double x,
                double y,
                double length,
                double width,
                double angle,
                int alpha
        ) {
            Graphics2D leaf = (Graphics2D) g.create();
            leaf.translate(x, y);
            leaf.rotate(angle);

            Path2D shape = new Path2D.Double();
            shape.moveTo(0, 0);
            shape.curveTo(length * .26, -width, length * .76, -width * .68, length, 0);
            shape.curveTo(length * .72, width * .72, length * .24, width, 0, 0);
            shape.closePath();

            leaf.setPaint(new GradientPaint(
                    0, 0, new Color(222, 169, 88, Math.min(70, alpha + 14)),
                    (float) length, 0, new Color(111, 72, 30, alpha / 2)
            ));
            leaf.fill(shape);
            leaf.setColor(new Color(230, 185, 110, Math.min(76, alpha + 10)));
            leaf.setStroke(new BasicStroke(.65f));
            leaf.drawLine(2, 0, (int) (length - 3), 0);
            leaf.dispose();
        }
    }

    private static final class OfferArtwork extends JComponent {

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            g.setColor(new Color(214, 160, 66, 14));
            g.fillOval(cx - 36, cy - 36, 72, 72);
            g.setColor(new Color(214, 160, 66, 60));
            g.drawOval(cx - 31, cy - 31, 62, 62);

            g.setPaint(new GradientPaint(
                    cx - 24, cy - 15, PALE,
                    cx + 21, cy + 18, new Color(143, 91, 28)
            ));
            Path2D horn = new Path2D.Double();
            horn.moveTo(cx - 22, cy - 8);
            horn.lineTo(cx + 15, cy - 23);
            horn.lineTo(cx + 15, cy + 17);
            horn.lineTo(cx - 22, cy + 6);
            horn.closePath();
            g.fill(horn);
            g.fillRoundRect(cx - 29, cy - 10, 11, 19, 5, 5);
            g.setColor(new Color(187, 129, 49));
            g.fillRoundRect(cx - 7, cy + 8, 9, 22, 4, 4);
            g.setColor(PALE);
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawArc(cx + 17, cy - 20, 20, 30, -55, 110);
            g.drawArc(cx + 20, cy - 27, 29, 44, -55, 110);
            g.dispose();
        }
    }

    private static final class LeafArtwork extends JComponent {

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(214, 160, 66, 26));
            g.setStroke(new BasicStroke(1.2f));
            g.drawLine(getWidth() - 8, getHeight(), 8, 12);
            for (int i = 0; i < 5; i++) {
                int x = 15 + i * 8;
                int y = getHeight() - 15 - i * 12;
                g.fillOval(x - 12, y - 8, 20, 9);
                g.fillOval(x + 2, y - 15, 18, 9);
            }
            g.dispose();
        }
    }

    private static final class HeroPanel extends JPanel {

        private final String managerName;
        private final BufferedImage image;
        private final GoldOutlineButton statistics = new GoldOutlineButton("VIEW STATISTICS");

        HeroPanel(String managerName) {
            this.managerName = managerName;
            this.image = loadHeroImage();
            setOpaque(false);
            setLayout(null);
            statistics.setPreferredSize(new Dimension(165, 39));
            add(statistics);
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    statistics.setBounds(24, getHeight() - 54, 170, 38);
                }
            });
        }

        JButton getStatisticsButton() {
            return statistics;
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            RoundRectangle2D clip = new RoundRectangle2D.Double(
                    0, 0, getWidth(), getHeight(), 18, 18
            );
            g.clip(clip);

            if (image != null) {
                drawCover(g, image, 0, 0, getWidth(), getHeight());
            } else {
                g.setPaint(new GradientPaint(0, 0, new Color(9, 17, 25), getWidth(), getHeight(), Color.BLACK));
                g.fillRect(0, 0, getWidth(), getHeight());
            }

            g.setPaint(new GradientPaint(
                    0, 0, new Color(1, 7, 12, 235),
                    getWidth() * .43f, 0, new Color(1, 7, 12, 25)
            ));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(new Color(214, 160, 66, 110));
            g.setStroke(new BasicStroke(1f));
            g.draw(clip);

            int nameSize = Math.max(23, Math.min(31, getWidth() / 44));
            int welcomeY = Math.max(62, getHeight() / 4);
            int nameY = welcomeY + 36;
            int descriptionY = nameY + 33;
            g.setColor(TEXT);
            g.setFont(new Font("Segoe UI", Font.BOLD, nameSize - 8));
            g.drawString("Welcome back,", 24, welcomeY);
            g.setColor(PALE);
            g.setFont(new Font("Segoe UI", Font.BOLD, nameSize));
            g.drawString(managerName, 24, nameY);
            g.setColor(TEXT);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            g.drawString("Drive luxury. Drive Velora.", 24, descriptionY);
            g.drawString("We provide premium experience", 24, descriptionY + 22);
            g.drawString("and top quality service.", 24, descriptionY + 44);

            g.dispose();
        }
    }

    private static void drawVeloraWingLogo(
            Graphics2D g,
            int cx,
            int cy,
            int width,
            Color color,
            boolean glow
    ) {
        Graphics2D copy = (Graphics2D) g.create();
        copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        float stroke = Math.max(2f, width / 44f);
        int half = width / 2;
        int vTop = cy - width / 7;
        int vBottom = cy + width / 4;

        if (glow) {
            copy.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 38));
            copy.setStroke(new BasicStroke(stroke + 6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawVeloraWingPaths(copy, cx, cy, half, vTop, vBottom);
        }

        copy.setPaint(new GradientPaint(
                cx - half, cy - width / 4, brighten(color, 44),
                cx + half, cy + width / 4, new Color(142, 93, 34, color.getAlpha())
        ));
        copy.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        drawVeloraWingPaths(copy, cx, cy, half, vTop, vBottom);
        copy.dispose();
    }

    private static void drawVeloraWingPaths(Graphics2D g, int cx, int cy, int half, int vTop, int vBottom) {
        Path2D p = new Path2D.Double();
        p.moveTo(cx, vBottom);
        p.lineTo(cx - half / 3.0, vTop);
        p.lineTo(cx - half, vTop);
        p.moveTo(cx, vBottom);
        p.lineTo(cx + half / 3.0, vTop);
        p.lineTo(cx + half, vTop);

        int[] offsets = {0, 12, 24};
        for (int i = 0; i < offsets.length; i++) {
            int y = vTop + offsets[i];
            int longWing = half - i * 13;
            int inner = half / 4 - i * 3;
            p.moveTo(cx - inner, y);
            p.lineTo(cx - longWing, y);
            p.moveTo(cx + inner, y);
            p.lineTo(cx + longWing, y);
        }
        g.draw(p);
    }

    private static Color brighten(Color color, int amount) {
        return new Color(
                Math.min(255, color.getRed() + amount),
                Math.min(255, color.getGreen() + amount),
                Math.min(255, color.getBlue() + amount),
                color.getAlpha()
        );
    }

    private static void drawCompactIcon(Graphics2D g, String type, int cx, int cy, int size) {
        int half = size / 2;
        switch (type) {
            case "HOME" -> {
                Path2D house = new Path2D.Double();
                house.moveTo(cx - half, cy);
                house.lineTo(cx, cy - half);
                house.lineTo(cx + half, cy);
                house.lineTo(cx + half - 2, cy);
                house.lineTo(cx + half - 2, cy + half);
                house.lineTo(cx - half + 2, cy + half);
                house.lineTo(cx - half + 2, cy);
                house.closePath();
                g.draw(house);
            }
            case "CAR" -> {
                g.drawRoundRect(cx - half, cy - 4, size, 9, 4, 4);
                g.drawLine(cx - 6, cy - 4, cx - 3, cy - half);
                g.drawLine(cx - 3, cy - half, cx + 5, cy - half);
                g.drawLine(cx + 5, cy - half, cx + 8, cy - 4);
                g.fillOval(cx - 6, cy + 4, 4, 4);
                g.fillOval(cx + 4, cy + 4, 4, 4);
            }
            case "CAL" -> {
                g.drawRoundRect(cx - half, cy - half + 2, size, size - 2, 3, 3);
                g.drawLine(cx - half, cy - 2, cx + half, cy - 2);
                g.drawLine(cx - 5, cy - half, cx - 5, cy - half + 5);
                g.drawLine(cx + 5, cy - half, cx + 5, cy - half + 5);
            }
            case "USERS" -> {
                g.drawOval(cx - 7, cy - half, 7, 7);
                g.drawOval(cx + 2, cy - half, 7, 7);
                g.drawArc(cx - 10, cy, 12, 10, 0, 180);
                g.drawArc(cx, cy, 12, 10, 0, 180);
            }
            case "BILL" -> {
                g.drawRect(cx - 7, cy - half, 14, size);
                g.drawString("$", cx - 4, cy + 5);
            }
            case "TOOLS" -> {
                g.drawLine(cx - half, cy + half, cx + half, cy - half);
                g.drawOval(cx + 3, cy - half - 1, 7, 7);
                g.drawOval(cx - half - 1, cy + 3, 7, 7);
            }
            default -> {
                for (int i = 0; i < 4; i++) {
                    int barH = 4 + i * 4;
                    g.drawRect(cx - half + i * 5, cy + half - barH, 3, barH);
                }
            }
        }
    }

    private static void drawWrapped(
            Graphics2D g,
            String text,
            int x,
            int y,
            int maxWidth,
            int lineHeight
    ) {
        StringBuilder line = new StringBuilder();
        int baseline = y;
        for (String word : text.split(" ")) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (g.getFontMetrics().stringWidth(candidate) > maxWidth && !line.isEmpty()) {
                g.drawString(line.toString(), x, baseline);
                baseline += lineHeight;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (!line.isEmpty()) {
            g.drawString(line.toString(), x, baseline);
        }
    }

    private static void drawCentered(Graphics2D g, String text, int cx, int baseline) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, cx - fm.stringWidth(text) / 2, baseline);
    }

    private static BufferedImage loadHeroImage() {
        return loadResourceImage("/assets/backgrounds/manager-hero-reference-v4.png");
    }

    private static BufferedImage loadResourceImage(String path) {
        try {
            return ImageIO.read(ManagerDashboard.class.getResource(path));
        } catch (IOException | IllegalArgumentException ex) {
            return null;
        }
    }

    private static void drawCover(
            Graphics2D g,
            BufferedImage image,
            int x,
            int y,
            int width,
            int height
    ) {
        double scale = Math.max(
                width / (double) image.getWidth(),
                height / (double) image.getHeight()
        );
        int imageW = (int) Math.round(image.getWidth() * scale);
        int imageH = (int) Math.round(image.getHeight() * scale);
        g.drawImage(
                image,
                x + (width - imageW) / 2,
                y + (height - imageH) / 2,
                imageW,
                imageH,
                null
        );
    }
    
    private void handleLogout() {
        LogoutConfirmDialog confirmDialog = new LogoutConfirmDialog(this);
        confirmDialog.setVisible(true);

        if (confirmDialog.isConfirmed()) {
            this.dispose();

            FarewellScreen farewellScreen = new FarewellScreen(manager.getFullName());
            farewellScreen.setVisible(true);
        }
    }
}
