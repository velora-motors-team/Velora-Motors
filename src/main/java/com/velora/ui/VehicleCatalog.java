package com.velora.ui;

import com.velora.authentication.Customer;
import com.velora.service.VehicleService;
import com.velora.vehicle.Vehicle;
import com.velora.vehicle.VehicleStatus;
import com.velora.vehicle.VehicleType;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
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
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VehicleCatalog extends JPanel {

    private static final Color BG = new Color(2, 6, 10);
    private static final Color CARD = new Color(5, 11, 18, 238);
    private static final Color CARD_DARK = new Color(3, 8, 13, 238);
    private static final Color GOLD = new Color(214, 160, 66);
    private static final Color PALE = new Color(238, 201, 139);
    private static final Color TEXT = new Color(245, 245, 247);
    private static final Color MUTED = new Color(166, 172, 183);
    private static final Color GREEN = new Color(73, 190, 93);
    private static final Color RED = new Color(226, 87, 91);
    private static final Color LINE = new Color(214, 160, 66, 86);
    private static final int PAGE_SIZE = 6;

    private final Customer customer;
    private final VehicleService vehicleService = new VehicleService();
    private final CustomerAccountState accountState;
    private final JPanel cardsGrid = new JPanel(new GridLayout(0, 3, 14, 14));
    private final JTextField searchField = new JTextField();
    private final JLabel showingLabel = label("", 11, Font.PLAIN, MUTED);
    private final JLabel walletBalanceLabel = label("$0", 22, Font.BOLD, TEXT);
    private final JPanel pager = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));

    private FilterCombo categoryFilter;
    private FilterCombo priceFilter;
    private FilterCombo fuelFilter;
    private FilterCombo availabilityFilter;
    private int currentPage = 1;

    public VehicleCatalog() {
        this(null);
    }

    public VehicleCatalog(Customer customer) {
        this.customer = customer;
        this.accountState = CustomerAccountState.forCustomer(customer);

        setOpaque(false);
        setLayout(new BorderLayout());
        add(createRoot(), BorderLayout.CENTER);
        accountState.addChangeListener(this::refreshWalletBalance);
        refreshWalletBalance();
        refreshCards();
    }

    public void refreshData() {
        refreshWalletBalance();
        refreshCards();
    }

    private JComponent createRoot() {
        JPanel root = new GradientRoot();
        root.setLayout(new BorderLayout(0, 10));
        root.setBorder(new EmptyBorder(2, 20, 14, 20));

        root.add(createCenter(), BorderLayout.CENTER);
        root.add(featureStrip(), BorderLayout.SOUTH);
        return root;
    }

    private JComponent createTop() {
        JPanel top = new JPanel(new BorderLayout(22, 0));
        top.setOpaque(false);

        SearchBox search = new SearchBox();
        search.setPreferredSize(new Dimension(590, 44));
        top.add(search, BorderLayout.WEST);

        JPanel stats = new JPanel(new GridLayout(1, 4, 0, 0));
        stats.setOpaque(false);
        stats.setPreferredSize(new Dimension(620, 72));
        stats.add(topMetric("CAR", availableVehicles(), "Available Vehicles"));
        stats.add(topMetric("BOLT", electricVehicles(), "Electric Models"));
        stats.add(topMetric("SUV", countType(VehicleType.SUV), "SUVs"));
        stats.add(topMetric("STAR", "4.8", "Best Rated"));
        top.add(new MetricFrame(stats), BorderLayout.EAST);

        return top;
    }

    private JComponent createCenter() {
        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(new EmptyBorder(0, 0, 0, 0));

        page.add(createWalletHeader());
        page.add(Box.createVerticalStrut(10));
        page.add(createFilters());
        page.add(Box.createVerticalStrut(12));

        cardsGrid.setOpaque(false);
        cardsGrid.setBorder(new EmptyBorder(0, 0, 0, 0));
        JScrollPane scroll = new JScrollPane(cardsGrid);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(7, 0));
        scroll.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(scroll);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottom.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        pager.setOpaque(false);
        bottom.add(showingLabel, BorderLayout.WEST);
        bottom.add(pager, BorderLayout.EAST);
        page.add(Box.createVerticalStrut(10));
        page.add(bottom);
        return page;
    }

    private JComponent createWalletHeader() {
        RoundedPanel header = new RoundedPanel(14, new Color(5, 11, 18, 238));
        header.setLayout(new BorderLayout(18, 0));
        header.setBorder(new EmptyBorder(9, 16, 9, 16));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        header.setPreferredSize(new Dimension(1200, 80));

        JPanel title = new JPanel();
        title.setOpaque(false);
        title.setLayout(new BoxLayout(title, BoxLayout.Y_AXIS));
        title.add(label("BMW Vehicle Collection", 25, Font.BOLD, TEXT));
        title.add(Box.createVerticalStrut(3));
        title.add(label("Your wallet is linked with billing, rentals, loyalty, and saved activity.", 12, Font.PLAIN, MUTED));
        header.add(title, BorderLayout.CENTER);

        RoundedPanel wallet = new RoundedPanel(12, new Color(14, 18, 22, 238));
        wallet.setLayout(new BorderLayout(12, 0));
        wallet.setBorder(new EmptyBorder(8, 13, 7, 15));
        wallet.setPreferredSize(new Dimension(275, 62));
        wallet.add(new MiniIcon("DIAMOND"), BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel titleLabel = label("AVAILABLE BALANCE", 10, Font.BOLD, PALE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        walletBalanceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        walletBalanceLabel.setPreferredSize(new Dimension(180, 28));
        walletBalanceLabel.setMinimumSize(new Dimension(180, 28));
        walletBalanceLabel.setMaximumSize(new Dimension(220, 28));
        text.add(titleLabel);
        text.add(Box.createVerticalStrut(0));
        text.add(walletBalanceLabel);
        wallet.add(text, BorderLayout.CENTER);

        header.add(wallet, BorderLayout.EAST);
        return header;
    }

    private void refreshWalletBalance() {
        walletBalanceLabel.setText(formatMoney(accountState.getWalletBalance()));
        walletBalanceLabel.repaint();
    }

    private JComponent createFilters() {
        JPanel filters = new JPanel(new GridLayout(1, 5, 12, 0));
        filters.setOpaque(false);
        filters.setAlignmentX(Component.LEFT_ALIGNMENT);
        filters.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        filters.setPreferredSize(new Dimension(1200, 38));

        categoryFilter = filterCombo("All Categories", "Cars", "SUVs", "Electric", "Hybrid");
        priceFilter = filterCombo("All Prices", "Under $250", "$250 - $399", "$400+");
        fuelFilter = filterCombo("All Powertrains", "Fuel", "Electric", "Hybrid");
        availabilityFilter = filterCombo("All Availability", "Available", "Rented", "Maintenance");
        JButton reset = new GhostButton("Reset Filters");
        reset.addActionListener(e -> {
            searchField.setText("");
            categoryFilter.setSelectedIndex(0);
            priceFilter.setSelectedIndex(0);
            fuelFilter.setSelectedIndex(0);
            availabilityFilter.setSelectedIndex(0);
            currentPage = 1;
            refreshCards();
        });

        filters.add(categoryFilter);
        filters.add(priceFilter);
        filters.add(fuelFilter);
        filters.add(availabilityFilter);
        filters.add(reset);
        return filters;
    }

    private FilterCombo filterCombo(String... items) {
        FilterCombo combo = new FilterCombo(items);
        combo.addActionListener(e -> {
            currentPage = 1;
            refreshCards();
        });
        return combo;
    }

    private void refreshCards() {
        cardsGrid.removeAll();
        List<Vehicle> vehicles = filteredVehicles();

        int totalPages = Math.max(1, (int) Math.ceil(vehicles.size() / (double) PAGE_SIZE));
        currentPage = Math.max(1, Math.min(currentPage, totalPages));
        int start = Math.min((currentPage - 1) * PAGE_SIZE, vehicles.size());
        int end = Math.min(start + PAGE_SIZE, vehicles.size());

        if (vehicles.isEmpty()) {
            cardsGrid.setLayout(new BorderLayout());
            cardsGrid.add(emptyState(), BorderLayout.CENTER);
        } else {
            cardsGrid.setLayout(new GridLayout(0, 3, 14, 14));
            for (int i = start; i < end; i++) {
                cardsGrid.add(new VehicleCard(vehicles.get(i)));
            }
            for (int i = end; i < start + PAGE_SIZE; i++) {
                JPanel spacer = new JPanel();
                spacer.setOpaque(false);
                cardsGrid.add(spacer);
            }
        }

        showingLabel.setText("Showing " + (vehicles.isEmpty() ? 0 : start + 1) + " - " + end + " of " + vehicles.size() + " vehicles");
        rebuildPager(totalPages);
        cardsGrid.revalidate();
        cardsGrid.repaint();
    }

    private List<Vehicle> filteredVehicles() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        String category = selected(categoryFilter);
        String price = selected(priceFilter);
        String fuel = selected(fuelFilter);
        String availability = selected(availabilityFilter);

        List<Vehicle> matches = new ArrayList<>();
        for (Vehicle vehicle : vehicleService.getAllVehicles()) {
            if (!query.isBlank()
                    && !FleetUiData.displayName(vehicle).toLowerCase(Locale.ROOT).contains(query)
                    && !vehicle.getId().toLowerCase(Locale.ROOT).contains(query)
                    && !prettyType(vehicle.getType()).toLowerCase(Locale.ROOT).contains(query)) {
                continue;
            }
            if (!matchesCategory(vehicle, category) || !matchesPrice(vehicle, price)
                    || !matchesFuel(vehicle, fuel) || !matchesAvailability(vehicle, availability)) {
                continue;
            }
            matches.add(vehicle);
        }
        return matches;
    }

    private boolean matchesCategory(Vehicle vehicle, String value) {
        if (value.startsWith("All")) {
            return true;
        }
        return switch (value) {
            case "Cars" -> vehicle.getType() == VehicleType.CAR;
            case "SUVs" -> vehicle.getType() == VehicleType.SUV;
            case "Electric" -> vehicle.getType() == VehicleType.ELECTRIC_VEHICLE || vehicle.getType() == VehicleType.ELECTRIC_BIKE;
            case "Hybrid" -> vehicle.getType() == VehicleType.HYBRID_CAR;
            default -> true;
        };
    }

    private boolean matchesPrice(Vehicle vehicle, String value) {
        if (value.startsWith("All")) {
            return true;
        }
        double price = vehicle.getDailyPrice();
        return switch (value) {
            case "Under $250" -> price < 250;
            case "$250 - $399" -> price >= 250 && price <= 399;
            case "$400+" -> price >= 400;
            default -> true;
        };
    }

    private boolean matchesFuel(Vehicle vehicle, String value) {
        if (value.startsWith("All")) {
            return true;
        }
        return switch (value) {
            case "Electric" -> vehicle.getType() == VehicleType.ELECTRIC_VEHICLE || vehicle.getType() == VehicleType.ELECTRIC_BIKE;
            case "Hybrid" -> vehicle.getType() == VehicleType.HYBRID_CAR;
            case "Fuel" -> vehicle.getType() == VehicleType.CAR || vehicle.getType() == VehicleType.SUV || vehicle.getType() == VehicleType.MOTORCYCLE || vehicle.getType() == VehicleType.TRUCK;
            default -> true;
        };
    }

    private boolean matchesAvailability(Vehicle vehicle, String value) {
        if (value.startsWith("All")) {
            return true;
        }
        return switch (value) {
            case "Available" -> vehicle.getStatus() == VehicleStatus.AVAILABLE;
            case "Rented" -> vehicle.getStatus() == VehicleStatus.RENTED;
            case "Maintenance" -> vehicle.getStatus() == VehicleStatus.MAINTENANCE;
            default -> true;
        };
    }

    private void rebuildPager(int totalPages) {
        pager.removeAll();
        pager.add(pageButton("<", currentPage > 1, () -> {
            currentPage--;
            refreshCards();
        }));
        for (int i = 1; i <= totalPages; i++) {
            int page = i;
            pager.add(pageButton(String.valueOf(i), true, () -> {
                currentPage = page;
                refreshCards();
            }, page == currentPage));
        }
        pager.add(pageButton(">", currentPage < totalPages, () -> {
            currentPage++;
            refreshCards();
        }));
        pager.revalidate();
        pager.repaint();
    }

    private JButton pageButton(String text, boolean enabled, Runnable action) {
        return pageButton(text, enabled, action, false);
    }

    private JButton pageButton(String text, boolean enabled, Runnable action, boolean active) {
        JButton button = new PageButton(text, active);
        button.setPreferredSize(new Dimension(38, 31));
        button.setEnabled(enabled);
        button.addActionListener(e -> action.run());
        return button;
    }

    private JComponent emptyState() {
        JPanel empty = new JPanel(new GridLayout(1, 1));
        empty.setOpaque(false);
        JLabel label = label("No vehicles match these filters.", 22, Font.BOLD, MUTED);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        empty.add(label);
        return empty;
    }

    private void showDetails(Vehicle vehicle) {
        String battery = vehicle.getBatteryLevel() == null ? "" : "\nBattery: " + vehicle.getBatteryLevel() + "%";
        JOptionPane.showMessageDialog(
                this,
                FleetUiData.displayName(vehicle)
                        + "\nID: " + vehicle.getId()
                        + "\nCategory: " + prettyType(vehicle.getType())
                        + "\nStatus: " + prettyStatus(vehicle.getStatus())
                        + "\nDaily price: " + formatMoney(vehicle.getDailyPrice())
                        + battery
                        + "\nPlate: " + FleetUiData.plate(vehicle)
                        + "\nColor: " + FleetUiData.color(vehicle),
                "Vehicle Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void rentVehicle(Vehicle vehicle) {
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            VeloraNotificationDialog.showInfo(
                    this,
                    "Vehicle Unavailable",
                    "This vehicle is not available right now."
            );
            return;
        }
        double subtotal = vehicle.getDailyPrice() * 3;
        double total = subtotal * 1.10;
        if (!accountState.canAfford(total)) {
            VeloraNotificationDialog.showError(
                    this,
                    "Insufficient Balance",
                    "Your available balance is " + formatMoney(accountState.getWalletBalance())
                            + ".\nRequired amount: " + formatMoney(total)
            );
            return;
        }

        boolean confirmed = VeloraNotificationDialog.showConfirm(
                this,
                "Confirm Rental",
                "Rent " + FleetUiData.displayName(vehicle) + " for 3 days?\nEstimated total: "
                        + formatMoney(total) + "\nCurrent balance: " + formatMoney(accountState.getWalletBalance()),
                "Rent Now"
        );
        if (!confirmed) {
            return;
        }

        CustomerAccountState.CustomerInvoice invoice = CustomerAccountState.createInvoice(
                customer,
                accountState.getInvoices().size(),
                FleetUiData.displayName(vehicle),
                3,
                subtotal,
                0,
                "Paid",
                "Card"
        );
        if (!accountState.addPaidInvoice(invoice, "Card")) {
            VeloraNotificationDialog.showError(
                    this,
                    "Payment Failed",
                    "The rental could not be completed because the wallet balance changed."
            );
            return;
        }
        vehicle.setStatus(VehicleStatus.RENTED);
        vehicleService.saveVehicles();
        refreshWalletBalance();
        refreshCards();

        VeloraNotificationDialog.showSuccess(
                this,
                "Rental Completed",
                "You rented " + FleetUiData.displayName(vehicle)
                        + ".\nPaid: " + formatMoney(invoice.totalAmount())
                        + "\nRemaining balance: " + formatMoney(accountState.getWalletBalance())
                        + "\nInvoice: " + invoice.invoiceId
        );
    }

    private JComponent topMetric(String icon, int value, String caption) {
        return topMetric(icon, String.valueOf(value), caption);
    }

    private JComponent topMetric(String icon, String value, String caption) {
        JPanel metric = new JPanel(new BorderLayout(10, 0));
        metric.setOpaque(false);
        metric.setBorder(new EmptyBorder(10, 15, 10, 15));
        metric.add(new MiniIcon(icon), BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(label(value, 20, Font.PLAIN, TEXT));
        text.add(label(caption, 10, Font.PLAIN, MUTED));
        metric.add(text, BorderLayout.CENTER);
        return metric;
    }

    private JComponent featureStrip() {
        RoundedPanel strip = new RoundedPanel(10, CARD_DARK);
        strip.setLayout(new GridLayout(1, 5, 0, 0));
        strip.setBorder(new EmptyBorder(12, 16, 12, 16));
        strip.setPreferredSize(new Dimension(100, 74));
        strip.add(feature("SHIELD", "PREMIUM FLEET", "Latest BMW models"));
        strip.add(feature("DIAMOND", "TRUSTED SERVICE", "Excellence in every step"));
        strip.add(feature("STAR", "BEST PRICES", "Luxury within reach"));
        strip.add(feature("HEADSET", "24/7 SUPPORT", "We are here for you"));
        strip.add(feature("CHECK", "DRIVE LUXURY.", "LIVE EXCELLENCE."));
        return strip;
    }

    private JComponent feature(String icon, String title, String sub) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(214, 160, 66, 45)));
        panel.add(new MiniIcon(icon), BorderLayout.WEST);
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(Box.createVerticalGlue());
        text.add(label(title, 11, Font.BOLD, TEXT));
        text.add(Box.createVerticalStrut(4));
        text.add(label(sub, 10, Font.PLAIN, MUTED));
        text.add(Box.createVerticalGlue());
        panel.add(text, BorderLayout.CENTER);
        return panel;
    }

    private int availableVehicles() {
        return (int) vehicleService.getAllVehicles().stream().filter(v -> v.getStatus() == VehicleStatus.AVAILABLE).count();
    }

    private int electricVehicles() {
        return (int) vehicleService.getAllVehicles().stream()
                .filter(v -> v.getType() == VehicleType.ELECTRIC_VEHICLE || v.getType() == VehicleType.ELECTRIC_BIKE)
                .count();
    }

    private int countType(VehicleType type) {
        return (int) vehicleService.getAllVehicles().stream().filter(v -> v.getType() == type).count();
    }

    private static String selected(JComboBox<String> combo) {
        return combo == null || combo.getSelectedItem() == null ? "" : String.valueOf(combo.getSelectedItem());
    }

    private static String prettyType(VehicleType type) {
        if (type == null) {
            return "";
        }
        return type.name().replace('_', ' ');
    }

    private static String prettyStatus(VehicleStatus status) {
        if (status == null) {
            return "";
        }
        return status.name().charAt(0) + status.name().substring(1).toLowerCase(Locale.ROOT);
    }

    private static String formatMoney(double value) {
        return "$" + String.format(Locale.US, "%,.0f", value);
    }

    private static JLabel label(String text, int size, int style, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", style, size));
        label.setForeground(color);
        return label;
    }

    private static BufferedImage loadImage(String path) {
        try {
            if (path == null || path.isBlank()) {
                return null;
            }
            if (path.startsWith("/")) {
                var url = VehicleCatalog.class.getResource(path);
                return url == null ? null : ImageIO.read(url);
            }
            Path file = Paths.get(path);
            return Files.exists(file) ? ImageIO.read(file.toFile()) : null;
        } catch (IOException ex) {
            return null;
        }
    }

    private static void drawCover(Graphics2D g, BufferedImage image, int x, int y, int w, int h) {
        if (image == null) {
            g.setPaint(new GradientPaint(x, y, new Color(18, 24, 32), x + w, y + h, new Color(4, 8, 12)));
            g.fillRect(x, y, w, h);
            return;
        }
        double scale = Math.max(w / (double) image.getWidth(), h / (double) image.getHeight());
        int iw = (int) Math.round(image.getWidth() * scale);
        int ih = (int) Math.round(image.getHeight() * scale);
        int ix = x + (w - iw) / 2;
        int iy = y + (h - ih) / 2;
        g.drawImage(image, ix, iy, iw, ih, null);
    }

    private final class SearchBox extends RoundedPanel {
        SearchBox() {
            super(14, new Color(5, 9, 15, 225));
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(0, 48, 0, 14));
            searchField.setOpaque(false);
            searchField.setBorder(BorderFactory.createEmptyBorder());
            searchField.setForeground(TEXT);
            searchField.setCaretColor(PALE);
            searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            searchField.putClientProperty("placeholder", "Search for BMW cars...");
            searchField.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { currentPage = 1; refreshCards(); }
                public void removeUpdate(DocumentEvent e) { currentPage = 1; refreshCards(); }
                public void changedUpdate(DocumentEvent e) { currentPage = 1; refreshCards(); }
            });
            add(searchField, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            super.paintComponent(raw);
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(PALE);
            g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawOval(20, 15, 12, 12);
            g.drawLine(30, 25, 37, 32);
            if (searchField.getText().isBlank() && !searchField.hasFocus()) {
                g.setColor(MUTED);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                g.drawString("Search for BMW cars...", 50, 28);
            }
            g.dispose();
        }
    }

    private final class VehicleCard extends RoundedPanel {
        private final Vehicle vehicle;
        private final BufferedImage image;
        private final JButton details = new GhostButton("View Details");
        private final JButton rent = new GoldButton("Rent Now");

        VehicleCard(Vehicle vehicle) {
            super(10, CARD);
            this.vehicle = vehicle;
            this.image = loadImage(FleetUiData.imagePath(vehicle));
            setLayout(null);
            setPreferredSize(new Dimension(395, 205));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            details.addActionListener(e -> showDetails(vehicle));
            rent.addActionListener(e -> rentVehicle(vehicle));
            add(details);
            add(rent);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!(e.getSource() instanceof JButton)) {
                        showDetails(vehicle);
                    }
                }
            });
        }

        @Override
        public void doLayout() {
            int y = getHeight() - 42;
            details.setBounds(getWidth() - 218, y, 98, 30);
            rent.setBounds(getWidth() - 108, y, 92, 30);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            super.paintComponent(raw);
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            drawCover(g, image, 0, 0, getWidth(), getHeight());
            g.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 155), getWidth(), 0, new Color(0, 0, 0, 30)));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g.setPaint(new GradientPaint(0, getHeight() - 75, new Color(0, 0, 0, 15), 0, getHeight(), new Color(0, 0, 0, 165)));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

            int x = 18;
            g.setColor(TEXT);
            g.setFont(new Font("Serif", Font.BOLD, 18));
            g.drawString(FleetUiData.displayName(vehicle), x, 30);
            g.setColor(PALE);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g.drawString(prettyType(vehicle.getType()), x, 50);

            g.setColor(new Color(220, 185, 124));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g.drawString(seatsText(vehicle), x, 82);
            g.drawString(vehicle.getBatteryLevel() == null ? "Automatic" : vehicle.getBatteryLevel() + "% Battery", x, 104);
            g.drawString(rangeText(vehicle), x, 126);

            drawStatusPill(g, getWidth() - 82, 13, vehicle.getStatus());
            g.setColor(PALE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g.drawString("* " + rating(vehicle), x, getHeight() - 19);

            String price = formatMoney(vehicle.getDailyPrice());
            g.setColor(TEXT);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 20));
            int priceWidth = g.getFontMetrics().stringWidth(price);
            g.drawString(price, getWidth() - priceWidth - 18, 104);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g.setColor(MUTED);
            g.drawString("Per Day", getWidth() - 62, 121);
            g.dispose();
        }

        private void drawStatusPill(Graphics2D g, int x, int y, VehicleStatus status) {
            Color c = status == VehicleStatus.AVAILABLE ? GREEN : status == VehicleStatus.RENTED ? GOLD : RED;
            String text = prettyStatus(status);
            g.setFont(new Font("Segoe UI", Font.BOLD, 10));
            int w = Math.max(64, g.getFontMetrics().stringWidth(text) + 18);
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 115));
            g.fillRoundRect(x - (w - 64), y, w, 23, 7, 7);
            g.setColor(c);
            g.drawRoundRect(x - (w - 64), y, w, 23, 7, 7);
            g.setColor(TEXT);
            g.drawString(text, x - (w - 64) + 9, y + 15);
        }
    }

    private static String seatsText(Vehicle vehicle) {
        return (vehicle.getType() == VehicleType.CAR ? "4" : "5") + " Seats";
    }

    private static String rangeText(Vehicle vehicle) {
        if (vehicle.getType() == VehicleType.ELECTRIC_VEHICLE || vehicle.getType() == VehicleType.ELECTRIC_BIKE) {
            return (560 + Math.floorMod(vehicle.getId().hashCode(), 90)) + " km Range";
        }
        return "0-100 km/h Luxury Tune";
    }

    private static String rating(Vehicle vehicle) {
        double rating = 4.7 + Math.floorMod(vehicle.getId().hashCode(), 3) / 10.0;
        int reviews = 120 + Math.floorMod(FleetUiData.displayName(vehicle).hashCode(), 160);
        return String.format(Locale.US, "%.1f (%d)", rating, reviews);
    }

    private static final class FilterCombo extends JComboBox<String> {
        FilterCombo(String... items) {
            super(items);
            setOpaque(false);
            setBackground(new Color(5, 10, 16));
            setForeground(TEXT);
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setBorder(BorderFactory.createEmptyBorder());
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setUI(new BasicComboBoxUI());
            setRenderer(new DarkComboRenderer());
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(5, 10, 16, 230));
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 9, 9);
            g.setColor(LINE);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 9, 9);
            g.setColor(TEXT);
            g.setFont(getFont());
            String text = getSelectedItem() == null ? "" : getSelectedItem().toString();
            g.drawString(text, 15, (getHeight() + g.getFontMetrics().getAscent()) / 2 - 3);
            g.setColor(PALE);
            g.drawString("v", getWidth() - 24, (getHeight() + g.getFontMetrics().getAscent()) / 2 - 4);
            g.dispose();
        }
    }

    private static final class DarkComboRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean selected,
                boolean focus
        ) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
            label.setOpaque(true);
            label.setBorder(new EmptyBorder(7, 10, 7, 10));
            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            label.setForeground(selected ? new Color(22, 13, 6) : TEXT);
            label.setBackground(selected ? GOLD : new Color(5, 10, 16));
            list.setBackground(new Color(5, 10, 16));
            list.setForeground(TEXT);
            list.setSelectionBackground(GOLD);
            list.setSelectionForeground(new Color(22, 13, 6));
            return label;
        }
    }

    private static final class PageButton extends JButton {
        private final boolean active;

        PageButton(String text, boolean active) {
            super(text);
            this.active = active;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = active ? GOLD : new Color(0, 0, 0, 95);
            Color stroke = getModel().isRollover() ? PALE : LINE;
            Color text = active ? new Color(22, 13, 6) : PALE;
            if (!isEnabled()) {
                text = new Color(PALE.getRed(), PALE.getGreen(), PALE.getBlue(), 90);
                stroke = new Color(LINE.getRed(), LINE.getGreen(), LINE.getBlue(), 55);
            }

            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g.setColor(stroke);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

            g.setColor(text);
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            String value = getText();
            g.drawString(value, (getWidth() - fm.stringWidth(value)) / 2, (getHeight() + fm.getAscent()) / 2 - 3);
            g.dispose();
        }
    }

    private static class RoundedPanel extends JPanel {
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
            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g.setColor(LINE);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static class GhostButton extends JButton {
        GhostButton(String text) {
            super(text);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(PALE);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBackground(new Color(0, 0, 0, 80));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(getBackground());
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g.setColor(getModel().isRollover() ? PALE : LINE);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class GoldButton extends GhostButton {
        GoldButton(String text) {
            super(text);
            setForeground(new Color(23, 13, 5));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(0, 0, PALE, 0, getHeight(), new Color(132, 82, 29)));
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g.setColor(new Color(255, 225, 165, 130));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g.setColor(new Color(23, 13, 5));
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            String text = getText();
            g.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, (getHeight() + fm.getAscent()) / 2 - 3);
            g.dispose();
        }
    }

    private static final class MetricFrame extends RoundedPanel {
        MetricFrame(JComponent content) {
            super(12, new Color(5, 10, 16, 230));
            setLayout(new BorderLayout());
            add(content, BorderLayout.CENTER);
        }
    }

    private static final class MiniIcon extends JComponent {
        private final String type;

        MiniIcon(String type) {
            this.type = type;
            setPreferredSize(new Dimension(34, 34));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(PALE);
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            if ("BOLT".equals(type)) {
                Path2D p = new Path2D.Double();
                p.moveTo(cx + 3, 5);
                p.lineTo(cx - 8, cy + 2);
                p.lineTo(cx, cy + 2);
                p.lineTo(cx - 4, getHeight() - 4);
                p.lineTo(cx + 10, cy - 3);
                p.lineTo(cx + 1, cy - 3);
                p.closePath();
                g.draw(p);
            } else if ("STAR".equals(type)) {
                drawStar(g, cx, cy, 12, 5);
            } else if ("DIAMOND".equals(type)) {
                g.drawPolygon(new int[]{cx, cx + 12, cx, cx - 12}, new int[]{4, cy, getHeight() - 4, cy}, 4);
            } else if ("HEADSET".equals(type)) {
                g.drawArc(cx - 11, cy - 9, 22, 21, 0, 180);
                g.drawLine(cx - 11, cy + 1, cx - 11, cy + 10);
                g.drawLine(cx + 11, cy + 1, cx + 11, cy + 10);
                g.drawRoundRect(cx - 15, cy + 5, 7, 10, 4, 4);
                g.drawRoundRect(cx + 8, cy + 5, 7, 10, 4, 4);
            } else if ("CHECK".equals(type)) {
                g.drawRoundRect(cx - 12, cy - 12, 24, 24, 8, 8);
                g.drawLine(cx - 6, cy, cx - 1, cy + 6);
                g.drawLine(cx - 1, cy + 6, cx + 8, cy - 6);
            } else {
                g.drawRoundRect(cx - 13, cy - 4, 26, 11, 5, 5);
                g.drawLine(cx - 8, cy - 4, cx - 4, cy - 11);
                g.drawLine(cx + 8, cy - 4, cx + 4, cy - 11);
                g.drawOval(cx - 10, cy + 6, 6, 6);
                g.drawOval(cx + 4, cy + 6, 6, 6);
            }
            g.dispose();
        }

        private void drawStar(Graphics2D g, int cx, int cy, int outer, int inner) {
            Path2D p = new Path2D.Double();
            for (int i = 0; i < 10; i++) {
                double angle = -Math.PI / 2 + i * Math.PI / 5;
                int r = i % 2 == 0 ? outer : inner;
                double x = cx + Math.cos(angle) * r;
                double y = cy + Math.sin(angle) * r;
                if (i == 0) {
                    p.moveTo(x, y);
                } else {
                    p.lineTo(x, y);
                }
            }
            p.closePath();
            g.draw(p);
        }
    }

    private static final class DarkScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(214, 160, 66, 135);
            trackColor = new Color(0, 0, 0, 0);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return invisible();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return invisible();
        }

        private JButton invisible() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }
    }

    private static final class GradientRoot extends JPanel {
        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(0, 0, BG, getWidth(), getHeight(), new Color(5, 10, 17)));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setComposite(AlphaComposite.SrcOver.derive(0.12f));
            g.setColor(GOLD);
            g.fillOval(getWidth() - 230, -190, 390, 310);
            g.dispose();
        }
    }
}
