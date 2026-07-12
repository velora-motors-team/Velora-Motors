package com.velora.ui;

import com.velora.authentication.Customer;
import com.velora.repository.CustomerRepository;
import com.velora.repository.RentalRepository;
import com.velora.service.VehicleService;
import com.velora.vehicle.Vehicle;
import com.velora.vehicle.VehicleStatus;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

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
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public final class RentalReturnPanel extends JPanel {

    private static final Color BACKGROUND = new Color(2, 7, 12);
    private static final Color PANEL = new Color(5, 12, 18);
    private static final Color GOLD = new Color(214, 160, 66);
    private static final Color PALE = new Color(238, 201, 139);
    private static final Color TEXT = new Color(244, 246, 248);
    private static final Color MUTED = new Color(157, 164, 175);
    private static final Color GREEN = new Color(75, 200, 102);
    private static final Color RED = new Color(235, 93, 98);
    private static final Color BLUE = new Color(67, 132, 207);

    private static final int PAGE_SIZE = 8;
    private final Customer manager;
    private final VehicleService vehicleService = new VehicleService();
    private final RentalRepository rentalRepository = new RentalRepository();
    private final CustomerRepository customerRepository = new CustomerRepository();
    private final List<RentalRecord> allRentals = new ArrayList<>();
    private final List<RentalRecord> filteredRentals = new ArrayList<>();
    private final List<RentalRecord> pageRentals = new ArrayList<>();

    private final RentalTableModel tableModel = new RentalTableModel(pageRentals);

    private JTable table;
    private SearchField searchField;
    private BlackDropDown statusFilter;
    private BlackDropDown vehicleFilter;
    private BlackDropDown dateFilter;
    private ReturnSummaryPanel summaryPanel;
    private JPanel pageButtonsPanel;
    private JLabel showingLabel;

    private int currentPage = 1;
    private int totalPages = 1;

    public RentalReturnPanel(Customer manager) {
        this.manager = manager;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(12, 14, 14, 18));

        loadSavedRentals();
        filteredRentals.addAll(allRentals);
        updatePageData();

        add(buildContent(), BorderLayout.CENTER);

        javax.swing.SwingUtilities.invokeLater(() -> {
            if (table.getRowCount() > 0) {
                table.setRowSelectionInterval(0, 0);
                summaryPanel.setRecord(pageRentals.get(0));
            }
        });
    }

    private JComponent buildContent() {
        RoundedPanel root = new RoundedPanel(18, new Color(4, 10, 16, 235));
        root.setLayout(new BorderLayout(14, 0));
        root.setBorder(new EmptyBorder(22, 22, 18, 14));

        JPanel main = new JPanel(new BorderLayout(0, 14));
        main.setOpaque(false);

        main.add(createHeader(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(14, 0));
        center.setOpaque(false);

        JPanel left = new JPanel(new BorderLayout(0, 13));
        left.setOpaque(false);
        left.add(createMetricRow(), BorderLayout.NORTH);
        left.add(createRentalTableCard(), BorderLayout.CENTER);

        summaryPanel = new ReturnSummaryPanel();
        summaryPanel.setPreferredSize(new Dimension(340, 600));

        center.add(left, BorderLayout.CENTER);
        center.add(summaryPanel, BorderLayout.EAST);

        main.add(center, BorderLayout.CENTER);

        root.add(main, BorderLayout.CENTER);
        return root;
    }

    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel title = label("Rental Return Management", 24, Font.BOLD, GOLD);
        JLabel sub = label("Manage rental returns, late fees, and final charges.", 12, Font.PLAIN, new Color(205, 211, 219));

        titles.add(title);
        titles.add(Box.createVerticalStrut(5));
        titles.add(sub);

        header.add(titles, BorderLayout.WEST);
        return header;
    }

    private JComponent createMetricRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(900, 96));

        int active = (int) allRentals.stream().filter(r -> r.status.equals("Active")).count();
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        int dueToday = (int) allRentals.stream().filter(r -> r.expectedReturn.contains(today)).count();
        int late = (int) allRentals.stream().filter(r -> r.status.equals("Late")).count();
        int lateFees = allRentals.stream().mapToInt(r -> r.lateFee).sum();

        row.add(new MetricCard("Active Rentals", String.valueOf(active), "View active rentals", "CAR", GREEN));
        row.add(new MetricCard("Due Today", String.valueOf(dueToday), "Returns due today", "CAL", PALE));
        row.add(new MetricCard("Late Returns", String.valueOf(late), "Overdue rentals", "CLOCK", RED));
        row.add(new MetricCard("Collected Late Fees", "$" + formatNumber(lateFees), "This month", "DOLLAR", GOLD));

        return row;
    }

    private JComponent createRentalTableCard() {
        RoundedPanel card = new RoundedPanel(16, new Color(5, 12, 18, 238));
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel filters = new JPanel(new BorderLayout(10, 0));
        filters.setOpaque(false);

        searchField = new SearchField("Search rentals...");
        searchField.setPreferredSize(new Dimension(220, 36));
        searchField.addActionListener(e -> applyFilters());
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                applyFilters();
            }
        });

        statusFilter = new BlackDropDown(new String[]{"All Status", "Active", "Late", "Returned"});
        vehicleFilter = new BlackDropDown(vehicleFilterItems());
        dateFilter = new BlackDropDown(new String[]{"All Dates", "Today", "This Week", "This Month"});

        statusFilter.setChangeAction(this::applyFilters);
        vehicleFilter.setChangeAction(this::applyFilters);
        dateFilter.setChangeAction(this::applyFilters);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(searchField);
        left.add(statusFilter);
        left.add(vehicleFilter);
        left.add(dateFilter);

        GoldOutlineButton export = new GoldOutlineButton("Export");
        export.setPreferredSize(new Dimension(92, 35));
        export.addActionListener(e -> JOptionPane.showMessageDialog(
                this,
                "Export feature will be connected to reports later.",
                "Velora Motors",
                JOptionPane.INFORMATION_MESSAGE
        ));

        filters.add(left, BorderLayout.WEST);
        filters.add(export, BorderLayout.EAST);

        card.add(filters, BorderLayout.NORTH);

        table = new JTable(tableModel);
        configureTable(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSummaryFromSelection();
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scroll.getVerticalScrollBar().setUI(new DarkScrollBarUI());

        card.add(scroll, BorderLayout.CENTER);
        card.add(createPaginationBar(), BorderLayout.SOUTH);

        return card;
    }

    private JComponent createPaginationBar() {
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(4, 2, 0, 2));

        showingLabel = label("", 11, Font.PLAIN, MUTED);
        bottom.add(showingLabel, BorderLayout.WEST);

        pageButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pageButtonsPanel.setOpaque(false);
        bottom.add(pageButtonsPanel, BorderLayout.EAST);

        refreshPaginationButtons();
        return bottom;
    }

    private JComponent createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setPreferredSize(new Dimension(900, 45));

        JLabel left = label("© 2026 Velora Motors. All rights reserved.", 10, Font.PLAIN, MUTED);
        BrandFooter brand = new BrandFooter();
        brand.setPreferredSize(new Dimension(300, 44));
        JLabel right = label("<html>Drive Luxury. Drive <font color='#D6A042'><b>BMW.</b></font></html>",
                10, Font.PLAIN, MUTED);
        right.setHorizontalAlignment(SwingConstants.RIGHT);

        footer.add(left, BorderLayout.WEST);
        footer.add(brand, BorderLayout.CENTER);
        footer.add(right, BorderLayout.EAST);

        return footer;
    }

    private void configureTable(JTable table) {
        table.setRowHeight(54);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(38, 49, 58, 110));
        table.setBackground(new Color(5, 12, 18));
        table.setForeground(TEXT);
        table.setSelectionBackground(new Color(70, 47, 20, 130));
        table.setSelectionForeground(TEXT);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(6, 13, 20));
        header.setForeground(new Color(174, 181, 190));
        header.setFont(new Font("Segoe UI", Font.BOLD, 9));
        header.setPreferredSize(new Dimension(100, 34));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(214, 160, 66, 55)));

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        headerRenderer.setBackground(new Color(6, 13, 20));
        headerRenderer.setForeground(new Color(174, 181, 190));
        headerRenderer.setFont(new Font("Segoe UI", Font.BOLD, 9));
        headerRenderer.setBorder(new EmptyBorder(0, 10, 0, 0));
        header.setDefaultRenderer(headerRenderer);

        table.getColumnModel().getColumn(0).setPreferredWidth(75);
        table.getColumnModel().getColumn(1).setPreferredWidth(105);
        table.getColumnModel().getColumn(2).setPreferredWidth(105);
        table.getColumnModel().getColumn(3).setPreferredWidth(118);
        table.getColumnModel().getColumn(4).setPreferredWidth(118);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);
        table.getColumnModel().getColumn(6).setPreferredWidth(70);
        table.getColumnModel().getColumn(7).setPreferredWidth(75);
        table.getColumnModel().getColumn(8).setPreferredWidth(82);

        table.setDefaultRenderer(Object.class, new RentalCellRenderer());
    }

    private void applyFilters() {
        String query = searchField == null ? "" : searchField.getRealText().trim().toLowerCase();
        String status = statusFilter == null ? "All Status" : statusFilter.getSelectedText();
        String vehicle = vehicleFilter == null ? "All Vehicles" : vehicleFilter.getSelectedText();
        String date = dateFilter == null ? "All Dates" : dateFilter.getSelectedText();

        filteredRentals.clear();

        for (RentalRecord r : allRentals) {
            boolean matchQuery = query.isBlank()
                    || r.id.toLowerCase().contains(query)
                    || r.customer.toLowerCase().contains(query)
                    || r.vehicle.toLowerCase().contains(query)
                    || r.status.toLowerCase().contains(query);

            boolean matchStatus = "All Status".equals(status) || r.status.equalsIgnoreCase(status);
            boolean matchVehicle = "All Vehicles".equals(vehicle) || r.vehicle.toLowerCase().contains(vehicle.toLowerCase());

            boolean matchDate = switch (date) {
                case "Today" -> r.expectedReturn.contains("07 Jul");
                case "This Week" -> r.expectedReturn.contains("05 Jul")
                        || r.expectedReturn.contains("06 Jul")
                        || r.expectedReturn.contains("07 Jul")
                        || r.expectedReturn.contains("08 Jul")
                        || r.expectedReturn.contains("09 Jul");
                case "This Month" -> r.expectedReturn.contains("Jul 2026");
                default -> true;
            };

            if (matchQuery && matchStatus && matchVehicle && matchDate) {
                filteredRentals.add(r);
            }
        }

        currentPage = 1;
        updatePageData();
        tableModel.fireTableDataChanged();
        refreshPaginationButtons();
        selectFirstVisibleRow();
    }

    private void updatePageData() {
        totalPages = Math.max(1, (int) Math.ceil(filteredRentals.size() / (double) PAGE_SIZE));
        currentPage = Math.max(1, Math.min(currentPage, totalPages));

        int from = (currentPage - 1) * PAGE_SIZE;
        int to = Math.min(filteredRentals.size(), from + PAGE_SIZE);

        pageRentals.clear();

        if (from < to) {
            pageRentals.addAll(filteredRentals.subList(from, to));
        }

        if (showingLabel != null) {
            int start = filteredRentals.isEmpty() ? 0 : from + 1;
            showingLabel.setText("Showing " + start + " to " + to + " of " + filteredRentals.size() + " rentals");
        }
    }

    private void goToPage(int page) {
        currentPage = Math.max(1, Math.min(page, totalPages));
        updatePageData();
        tableModel.fireTableDataChanged();
        refreshPaginationButtons();
        selectFirstVisibleRow();
    }

    private void refreshPaginationButtons() {
    pageButtonsPanel.removeAll();

    SmallPageButton prev = new SmallPageButton("‹", false, () -> goToPage(currentPage - 1));
    pageButtonsPanel.add(prev);

    for (int i = 1; i <= totalPages; i++) {
        final int pageNumber = i;

        SmallPageButton pageButton = new SmallPageButton(
                String.valueOf(pageNumber),
                pageNumber == currentPage,
                () -> goToPage(pageNumber)
        );

        pageButtonsPanel.add(pageButton);
    }

    SmallPageButton next = new SmallPageButton("›", false, () -> goToPage(currentPage + 1));
    pageButtonsPanel.add(next);

    pageButtonsPanel.revalidate();
    pageButtonsPanel.repaint();

    int from = filteredRentals.isEmpty() ? 0 : (currentPage - 1) * PAGE_SIZE + 1;
    int to = Math.min(filteredRentals.size(), currentPage * PAGE_SIZE);

    showingLabel.setText("Showing " + from + " to " + to + " of " + filteredRentals.size() + " rentals");
}

    private void selectFirstVisibleRow() {
        if (table == null || summaryPanel == null) {
            return;
        }

        if (table.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
            summaryPanel.setRecord(pageRentals.get(0));
        } else {
            table.clearSelection();
            summaryPanel.setRecord(null);
        }
    }

    private void updateSummaryFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= pageRentals.size()) {
            return;
        }

        summaryPanel.setRecord(pageRentals.get(row));
    }

    private void confirmSelectedReturn() {
        int row = table.getSelectedRow();

        if (row < 0 || row >= pageRentals.size()) {
            JOptionPane.showMessageDialog(this, "Select a rental first.", "Velora Motors", JOptionPane.WARNING_MESSAGE);
            return;
        }

        RentalRecord record = pageRentals.get(row);

        if ("Returned".equals(record.status)) {
            JOptionPane.showMessageDialog(this, "This rental is already returned.", "Velora Motors", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                "Confirm return for " + record.id + "?\nVehicle: " + record.vehicleFullName,
                "Confirm Return",
                JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {
            LocalDateTime actualReturn = LocalDateTime.now();
            double lateFee = record.lateFee;

            boolean updated = rentalRepository.updateStatus(
                    record.id,
                    "COMPLETED",
                    actualReturn.toString(),
                    lateFee
            );

            if (!updated) {
                JOptionPane.showMessageDialog(
                        this,
                        "The rental could not be updated in rentals.tsv.",
                        "Velora Motors",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            markVehicleAvailable(record.vehicleFullName);
            loadSavedRentals();
            applyFilters();
        }
    }

    private void markVehicleAvailable(String vehicleName) {
        if (vehicleName == null || vehicleName.isBlank()) {
            return;
        }
        for (Vehicle vehicle : vehicleService.getAllVehicles()) {
            if (FleetUiData.displayName(vehicle).equalsIgnoreCase(vehicleName.trim())) {
                vehicle.setStatus(VehicleStatus.AVAILABLE);
                vehicleService.saveVehicles();
                return;
            }
        }
    }

    public void refreshData() {
        loadSavedRentals();
        applyFilters();
    }

    private boolean loadSavedRentals() {
        allRentals.clear();

        for (RentalRepository.RentalRecord stored : rentalRepository.findAll()) {
            Vehicle vehicle = findVehicleById(stored.vehicleId());
            Customer customer = customerRepository.findByEmail(stored.customerEmail())
                    .map(CustomerRepository.StoredCustomer::customer)
                    .orElse(null);

            String expected = formatDateTime(stored.expectedReturnDateTime());
            String actual = stored.actualReturnDateTime() == null || stored.actualReturnDateTime().isBlank()
                    ? "-"
                    : formatDateTime(stored.actualReturnDateTime());

            String lateDuration = calculateLateDuration(
                    stored.expectedReturnDateTime(),
                    stored.actualReturnDateTime(),
                    stored.status()
            );

            allRentals.add(new RentalRecord(
                    stored.rentalId(),
                    stored.customerName(),
                    stored.vehicleName(),
                    vehicle == null ? "-" : FleetUiData.color(vehicle),
                    expected,
                    actual,
                    lateDuration,
                    (int) Math.round(stored.lateFee()),
                    toUiStatus(stored.status()),
                    stored.customerEmail(),
                    customer == null ? "" : customer.getPhone(),
                    vehicle == null ? "" : FleetUiData.imagePath(vehicle),
                    stored.vehicleName(),
                    vehicle == null ? stored.vehicleId() : FleetUiData.vin(vehicle),
                    (int) Math.round(stored.baseAmount()),
                    0,
                    0
            ));
        }

        allRentals.sort((a, b) -> b.id.compareToIgnoreCase(a.id));
        return !allRentals.isEmpty();
    }

    private Vehicle findVehicleById(String vehicleId) {
        for (Vehicle vehicle : vehicleService.getAllVehicles()) {
            if (vehicle.getId().equalsIgnoreCase(vehicleId)) {
                return vehicle;
            }
        }
        return null;
    }

    private static String toUiStatus(String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase();
        return switch (normalized) {
            case "ACTIVE" -> "Active";
            case "OVERDUE", "LATE" -> "Late";
            case "COMPLETED", "RETURNED" -> "Returned";
            case "CANCELLED" -> "Returned";
            default -> normalized.isBlank() ? "Active" : normalized;
        };
    }

    private static String formatDateTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return "-";
        }
        try {
            return LocalDateTime.parse(raw)
                    .format(DateTimeFormatter.ofPattern("dd MMM yyyy\nhh:mm a"));
        } catch (DateTimeParseException ex) {
            return raw;
        }
    }

    private static String calculateLateDuration(String expectedRaw, String actualRaw, String status) {
        try {
            LocalDateTime expected = LocalDateTime.parse(expectedRaw);
            LocalDateTime end;

            if (actualRaw != null && !actualRaw.isBlank()) {
                end = LocalDateTime.parse(actualRaw);
            } else if ("OVERDUE".equalsIgnoreCase(status) || "LATE".equalsIgnoreCase(status)) {
                end = LocalDateTime.now();
            } else {
                return "-";
            }

            if (!end.isAfter(expected)) {
                return "-";
            }

            Duration delay = Duration.between(expected, end);
            long hours = delay.toHours();
            long minutes = delay.minusHours(hours).toMinutes();
            return hours + "h " + minutes + "m";
        } catch (DateTimeParseException ex) {
            return "-";
        }
    }

    private void loadDemoData() {
        allRentals.clear();

        String[] customers = {
                "John Doe", "Michael Smith", "Sarah Johnson", "David Brown", "Emily Davis",
                "James Wilson", "Olivia Martinez", "Daniel White", "Lina Khaled", "Adam Naser",
                "Maya Saleh", "Rami Hasan", "Noah Anderson", "Ava Thompson", "Liam Johnson"
        };

        List<Vehicle> vehicles = vehicleService.getAllVehicles();
        for (int i = 0; i < vehicles.size(); i++) {
            Vehicle vehicle = vehicles.get(i);
            String status = switch (i % 6) {
                case 0 -> "Returned";
                case 1 -> "Late";
                default -> "Active";
            };
            String actual = "Returned".equals(status) ? "05 Jul 2026\n9:45 AM" : "-";
            String lateDuration = "Late".equals(status) ? "2h 30m" : "-";
            int lateFee = "Late".equals(status) ? 70 + (i % 4) * 25 : 0;
            int day = 5 + (i % 10);
            int dailyRate = (int) Math.round(vehicle.getDailyPrice());
            int base = dailyRate * (2 + (i % 5));
            String customer = customers[i % customers.length];
            allRentals.add(new RentalRecord(
                    "RNT-" + String.format("%04d", 1001 + i),
                    customer,
                    FleetUiData.displayName(vehicle),
                    FleetUiData.color(vehicle),
                    String.format("%02d Jul 2026\n10:00 AM", day),
                    actual,
                    lateDuration,
                    lateFee,
                    status,
                    customer.toLowerCase().replace(" ", ".") + "@email.com",
                    "+970 59 " + String.format("%03d %04d", 100 + i, 1000 + i),
                    FleetUiData.imagePath(vehicle),
                    FleetUiData.displayName(vehicle),
                    FleetUiData.vin(vehicle),
                    base,
                    i % 3 == 0 ? 300 : 0,
                    dailyRate
            ));
        }

        allRentals.sort(Comparator.comparing(r -> r.id));
    }

    private String[] vehicleFilterItems() {
        List<Vehicle> vehicles = vehicleService.getAllVehicles();
        String[] items = new String[vehicles.size() + 1];
        items[0] = "All Vehicles";
        for (int i = 0; i < vehicles.size(); i++) {
            items[i + 1] = FleetUiData.displayName(vehicles.get(i));
        }
        return items;
    }

    private JLabel label(String text, int size, int style, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", style, size));
        label.setForeground(color);
        return label;
    }

    private static String formatNumber(int value) {
        return String.format("%,d", value);
    }

    private static BufferedImage loadImage(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }

        String normalized = path.startsWith("/") ? path : "/" + path;

        try {
            return ImageIO.read(RentalReturnPanel.class.getResource(normalized));
        } catch (IOException | IllegalArgumentException ex) {
            return null;
        }
    }

    private final class ConfirmButton extends JButton {

        ConfirmButton() {
            super("Confirm Return");
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(new Color(35, 24, 10));
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addActionListener(e -> confirmSelectedReturn());
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setPaint(new GradientPaint(
                    0, 0, getModel().isRollover() ? new Color(250, 222, 171) : PALE,
                    getWidth(), getHeight(), new Color(178, 119, 43)
            ));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 9, 9);

            g.setColor(new Color(35, 24, 10));
            g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int iconX = Math.max(16, getWidth() / 2 - 68);
            int cy = getHeight() / 2;
            g.drawLine(iconX, cy, iconX + 5, cy + 5);
            g.drawLine(iconX + 5, cy + 5, iconX + 15, cy - 7);

            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class InvoiceButton extends JButton {

        InvoiceButton() {
            super("Generate Updated Invoice");
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

            g.setColor(getModel().isRollover() ? new Color(214, 160, 66, 18) : new Color(4, 10, 16, 240));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g.setColor(new Color(214, 160, 66, 90));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

            int iconX = Math.max(16, getWidth() / 2 - 92);
            int cy = getHeight() / 2;
            g.setColor(PALE);
            g.setStroke(new BasicStroke(1.3f));
            g.drawRect(iconX, cy - 8, 12, 16);
            g.drawLine(iconX + 3, cy - 3, iconX + 9, cy - 3);
            g.drawLine(iconX + 3, cy + 2, iconX + 9, cy + 2);

            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class RentalRecord {
        final String id;
        final String customer;
        final String vehicle;
        final String color;
        String expectedReturn;
        String actualReturn;
        String lateDuration;
        final int lateFee;
        String status;
        final String email;
        final String phone;
        final String imagePath;
        final String vehicleFullName;
        final String vin;
        final int baseRental;
        final int insurance;
        final int taxes;

        RentalRecord(String id, String customer, String vehicle, String color,
                     String expectedReturn, String actualReturn, String lateDuration,
                     int lateFee, String status, String email, String phone, String imagePath,
                     String vehicleFullName, String vin, int baseRental, int insurance, int taxes) {
            this.id = id;
            this.customer = customer;
            this.vehicle = vehicle;
            this.color = color;
            this.expectedReturn = expectedReturn;
            this.actualReturn = actualReturn;
            this.lateDuration = lateDuration;
            this.lateFee = lateFee;
            this.status = status;
            this.email = email;
            this.phone = phone;
            this.imagePath = imagePath;
            this.vehicleFullName = vehicleFullName;
            this.vin = vin;
            this.baseRental = baseRental;
            this.insurance = insurance;
            this.taxes = taxes;
        }

        int finalAmount() {
            return baseRental + lateFee + insurance + taxes;
        }
    }

    private static final class RentalTableModel extends AbstractTableModel {

        private final String[] columns = {
                "RENTAL ID", "CUSTOMER", "VEHICLE", "EXPECTED RETURN", "ACTUAL RETURN",
                "LATE DURATION", "LATE FEE", "STATUS", "ACTIONS"
        };

        private final List<RentalRecord> data;

        RentalTableModel(List<RentalRecord> data) {
            this.data = data;
        }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int column) { return columns[column]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            RentalRecord r = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> r.id;
                case 1 -> r.customer;
                case 2 -> r.vehicle + "\n" + r.color;
                case 3 -> r.expectedReturn;
                case 4 -> r.actualReturn;
                case 5 -> r.lateDuration;
                case 6 -> r.lateFee == 0 ? "$0" : "$" + r.lateFee;
                case 7 -> r.status;
                default -> "actions";
            };
        }
    }

    private static final class RentalCellRenderer extends DefaultTableCellRenderer {

        RentalCellRenderer() {
            setOpaque(true);
            setBorder(new EmptyBorder(0, 10, 0, 8));
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean selected, boolean focus, int row, int column
        ) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, selected, focus, row, column);

            label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            label.setForeground(TEXT);
            label.setBackground(selected ? new Color(62, 43, 19) : new Color(5, 12, 18));
            label.setBorder(new EmptyBorder(0, 10, 0, 8));
            label.setHorizontalAlignment(SwingConstants.LEFT);

            String text = value == null ? "" : value.toString();

            if (column == 0) {
                label.setForeground(GOLD);
                label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            } else if (column == 2 || column == 3 || column == 4) {
                label.setText("<html>" + text.replace("\n", "<br><font color='#9DA4AF'>") + "</font></html>");
            } else if (column == 5) {
                label.setForeground(text.contains("h") ? RED : MUTED);
                label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            } else if (column == 6) {
                label.setForeground("$0".equals(text) ? TEXT : GOLD);
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            } else if (column == 7) {
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setForeground(switch (text) {
                    case "Returned" -> GREEN;
                    case "Late" -> RED;
                    default -> BLUE;
                });
                label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            } else if (column == 8) {
                label.setText("View   More");
                label.setForeground(GOLD);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 10));
            }

            return label;
        }
    }

    private final class ReturnSummaryPanel extends RoundedPanel {

        private RentalRecord record;
        private BufferedImage image;
        private final ConfirmButton confirmButton = new ConfirmButton();
        private final InvoiceButton invoiceButton = new InvoiceButton();

        ReturnSummaryPanel() {
            super(16, new Color(5, 12, 18, 242));
            setBorder(new EmptyBorder(14, 16, 14, 16));
            setLayout(null);
            add(confirmButton);
            add(invoiceButton);

            invoiceButton.addActionListener(e -> JOptionPane.showMessageDialog(
                    RentalReturnPanel.this,
                    "Invoice generation will be connected to billing.txt later.",
                    "Velora Motors",
                    JOptionPane.INFORMATION_MESSAGE
            ));
        }

        void setRecord(RentalRecord record) {
            this.record = record;
            this.image = record == null ? null : loadImage(record.imagePath);
            confirmButton.setVisible(record != null);
            invoiceButton.setVisible(record != null);
            repaint();
        }

        @Override
        public void doLayout() {
            int x = 18;
            int w = getWidth() - 36;
            int y = Math.max(getHeight() - 102, 500);

            confirmButton.setBounds(x, y, w, 40);
            invoiceButton.setBounds(x, y + 52, w, 38);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            super.paintComponent(raw);

            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            int x = 18;
            int y = 24;
            int w = getWidth() - 36;

            g.setColor(TEXT);
            g.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g.drawString("Return Summary", x, y);

            if (record == null) {
                g.setColor(MUTED);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g.drawString("No rental selected.", x, y + 42);
                g.dispose();
                return;
            }

            drawBadge(g, getWidth() - 88, 15, 70, 25, record.id);

            y += 42;
            g.setColor(PALE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g.drawString("Customer", x, y);

            drawCustomerIcon(g, x + 8, y + 32, 20);

            g.setColor(TEXT);
            g.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g.drawString(record.customer, x + 34, y + 27);

            g.setColor(MUTED);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            g.drawString(record.email, x + 34, y + 41);
            g.drawString(record.phone, x + 34, y + 55);

            y += 82;
            drawSeparator(g, x, y, w);

            y += 22;
            g.setColor(PALE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g.drawString("Vehicle", x, y);

            if (image != null) {
                Shape old = g.getClip();
                RoundRectangle2D clip = new RoundRectangle2D.Double(x, y + 18, 100, 60, 10, 10);
                g.clip(clip);
                drawCover(g, image, x, y + 18, 100, 60);
                g.setClip(old);
                g.setColor(new Color(214, 160, 66, 90));
                g.draw(clip);
            } else {
                g.setColor(new Color(14, 22, 31));
                g.fillRoundRect(x, y + 18, 100, 60, 10, 10);
                g.setColor(new Color(130, 91, 41));
                g.setFont(new Font("Serif", Font.PLAIN, 20));
                g.drawString("VELORA", x + 18, y + 53);
            }

            g.setColor(TEXT);
            g.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g.drawString(record.vehicleFullName, x + 116, y + 34);

            g.setColor(MUTED);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            g.drawString(record.color, x + 116, y + 49);

            drawSmallPlate(g, x + 116, y + 61, 72, 19, record.vehicle.replace(" ", "").toUpperCase());

            g.setColor(MUTED);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 8));
            g.drawString("VIN: " + record.vin, x + 116, y + 91);

            y += 118;
            drawTwoColumnText(g, "Expected Return", record.expectedReturn.replace("\n", " - "), y, false);
            y += 25;
            drawTwoColumnText(g, "Actual Return", record.actualReturn.replace("\n", " - "), y, false);
            y += 25;
            drawTwoColumnText(g, "Delay", record.lateDuration, y, record.lateFee > 0);

            y += 34;
            drawSeparator(g, x, y, w);

            y += 22;
            g.setColor(PALE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g.drawString("Fee Breakdown", x, y);

            g.setColor(MUTED);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            g.drawString("Fee Policy", x, y + 24);
            g.drawString("$30 per hour after grace period", getWidth() - 195, y + 24);
            g.drawString("Grace Period", x, y + 42);
            g.drawString("30 minutes", getWidth() - 92, y + 42);

            y += 70;
            drawSeparator(g, x, y, w);

            y += 22;
            drawPriceRow(g, "Base Rental (4 Days)", "$" + formatNumber(record.baseRental), y, TEXT);
            y += 24;
            drawPriceRow(g, "Late Return Fee (" + record.lateDuration + ")", "$" + record.lateFee, y, GOLD);
            y += 24;
            drawPriceRow(g, "Insurance", "$" + record.insurance, y, MUTED);
            y += 24;
            drawPriceRow(g, "Taxes (7%)", "$" + record.taxes, y, MUTED);

            y += 33;
            drawSeparator(g, x, y, w);

            y += 34;
            g.setColor(PALE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 15));
            g.drawString("Final Amount", x, y);

            g.setColor(GOLD);
            g.setFont(new Font("Segoe UI", Font.BOLD, 21));
            String amount = "$" + formatNumber(record.finalAmount());
            g.drawString(amount, getWidth() - g.getFontMetrics().stringWidth(amount) - 18, y + 2);

            g.dispose();
        }

        private void drawTwoColumnText(Graphics2D g, String left, String right, int y, boolean danger) {
            g.setColor(MUTED);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            g.drawString(left, 18, y);

            g.setColor(danger ? RED : new Color(210, 215, 222));
            g.setFont(new Font("Segoe UI", danger ? Font.BOLD : Font.PLAIN, 9));
            int tw = g.getFontMetrics().stringWidth(right);
            g.drawString(right, getWidth() - tw - 18, y);
        }

        private void drawPriceRow(Graphics2D g, String left, String right, int y, Color priceColor) {
            g.setColor(MUTED);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g.drawString(left, 18, y);

            g.setColor(priceColor);
            g.setFont(new Font("Segoe UI", Font.BOLD, 12));
            int tw = g.getFontMetrics().stringWidth(right);
            g.drawString(right, getWidth() - tw - 18, y);
        }

        private void drawSeparator(Graphics2D g, int x, int y, int w) {
            g.setColor(new Color(255, 255, 255, 22));
            g.drawLine(x, y, x + w, y);
        }

        private void drawBadge(Graphics2D g, int x, int y, int w, int h, String text) {
            g.setColor(new Color(38, 28, 14, 220));
            g.fillRoundRect(x, y, w, h, 8, 8);
            g.setColor(new Color(214, 160, 66, 110));
            g.drawRoundRect(x, y, w, h, 8, 8);
            g.setColor(GOLD);
            g.setFont(new Font("Segoe UI", Font.BOLD, 9));
            g.drawString(text, x + 10, y + 16);
        }

        private void drawSmallPlate(Graphics2D g, int x, int y, int w, int h, String text) {
            g.setColor(new Color(13, 21, 30));
            g.fillRoundRect(x, y, w, h, 5, 5);
            g.setColor(new Color(214, 160, 66, 70));
            g.drawRoundRect(x, y, w, h, 5, 5);
            g.setColor(MUTED);
            g.setFont(new Font("Segoe UI", Font.BOLD, 8));
            String label = text.length() > 8 ? text.substring(0, 8) : text;
            g.drawString(label, x + 7, y + 13);
        }

        private void drawCustomerIcon(Graphics2D g, int cx, int cy, int size) {
            g.setColor(GOLD);
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawOval(cx - 5, cy - 10, 10, 10);
            g.drawArc(cx - 11, cy + 2, 22, 16, 15, 150);
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

            RoundRectangle2D shape = new RoundRectangle2D.Double(.5, .5, getWidth() - 1, getHeight() - 1, radius, radius);
            g.setColor(new Color(0, 0, 0, 70));
            g.fillRoundRect(5, 7, Math.max(0, getWidth() - 10), Math.max(0, getHeight() - 10), radius, radius);

            g.setPaint(new GradientPaint(0, 0, brighten(fill, 18), getWidth(), getHeight(), fill));
            g.fill(shape);

            g.setPaint(new GradientPaint(0, 0, new Color(255, 235, 181, 25), getWidth(), 0, new Color(214, 160, 66, 3)));
            g.fill(shape);

            g.setColor(new Color(214, 160, 66, 78));
            g.setStroke(new BasicStroke(1f));
            g.draw(shape);

            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class MetricCard extends RoundedPanel {
        private final String title;
        private final String value;
        private final String subtitle;
        private final String icon;
        private final Color accent;

        MetricCard(String title, String value, String subtitle, String icon, Color accent) {
            super(14, new Color(7, 14, 21, 235));
            this.title = title;
            this.value = value;
            this.subtitle = subtitle;
            this.icon = icon;
            this.accent = accent;
        }

        @Override
        protected void paintComponent(Graphics raw) {
            super.paintComponent(raw);
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int cx = 46;
            int cy = getHeight() / 2;
            g.setColor(new Color(214, 160, 66, 18));
            g.fillOval(cx - 28, cy - 28, 56, 56);
            g.setColor(new Color(214, 160, 66, 48));
            g.drawOval(cx - 28, cy - 28, 56, 56);

            g.setColor(accent);
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawIcon(g, icon, cx, cy, 25);

            g.setColor(TEXT);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g.drawString(title, 86, 28);

            g.setColor("Late Returns".equals(title) ? RED : PALE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 23));
            g.drawString(value, 86, 58);

            g.setColor(MUTED);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            g.drawString(subtitle, 86, 77);
            g.dispose();
        }
    }

    private static final class SearchField extends JTextField {
        private final String placeholder;
        private boolean showingPlaceholder = true;

        SearchField(String placeholder) {
            super(placeholder);
            this.placeholder = placeholder;
            setOpaque(false);
            setForeground(MUTED);
            setCaretColor(PALE);
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setBorder(new EmptyBorder(0, 12, 0, 38));

            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override public void focusGained(java.awt.event.FocusEvent e) {
                    if (showingPlaceholder) {
                        setText("");
                        setForeground(TEXT);
                        showingPlaceholder = false;
                    }
                }
                @Override public void focusLost(java.awt.event.FocusEvent e) {
                    if (getText().isBlank()) {
                        setText(placeholder);
                        setForeground(MUTED);
                        showingPlaceholder = true;
                    }
                }
            });
        }

        String getRealText() {
            return showingPlaceholder ? "" : getText();
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(3, 9, 15, 240));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g.setColor(new Color(214, 160, 66, isFocusOwner() ? 125 : 58));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

            int cx = getWidth() - 23;
            int cy = getHeight() / 2;
            g.setColor(GOLD);
            g.setStroke(new BasicStroke(1.5f));
            g.drawOval(cx - 6, cy - 6, 12, 12);
            g.drawLine(cx + 5, cy + 5, cx + 10, cy + 10);
            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class BlackDropDown extends JButton {
        private final String[] items;
        private int selectedIndex;
        private final JPopupMenu menu = new JPopupMenu();
        private Runnable changeAction;

        BlackDropDown(String[] items) {
            super(items == null || items.length == 0 ? "Select" : items[0]);
            this.items = items == null || items.length == 0 ? new String[]{"Select"} : items.clone();

            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(TEXT);
            setFont(new Font("Segoe UI", Font.PLAIN, 11));
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(new EmptyBorder(0, 11, 0, 32));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(130, 35));

            menu.setOpaque(true);
            menu.setBackground(new Color(4, 10, 16));
            menu.setBorder(BorderFactory.createLineBorder(new Color(214, 160, 66, 120)));

            for (int i = 0; i < this.items.length; i++) {
                final int index = i;
                JMenuItem item = new JMenuItem(this.items[i]);
                item.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                item.setForeground(TEXT);
                item.setBackground(new Color(4, 10, 16));
                item.setOpaque(true);
                item.setPreferredSize(new Dimension(170, 31));
                item.addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) {
                        item.setBackground(PALE);
                        item.setForeground(new Color(30, 20, 8));
                    }
                    @Override public void mouseExited(MouseEvent e) {
                        item.setBackground(new Color(4, 10, 16));
                        item.setForeground(TEXT);
                    }
                });
                item.addActionListener(e -> {
                    selectedIndex = index;
                    setText(BlackDropDown.this.items[selectedIndex]);
                    repaint();
                    if (changeAction != null) {
                        changeAction.run();
                    }
                });
                menu.add(item);
            }

            addActionListener(e -> menu.show(this, 0, getHeight() + 3));
        }

        void setChangeAction(Runnable changeAction) {
            this.changeAction = changeAction;
        }

        String getSelectedText() {
            return items[selectedIndex];
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            boolean hover = getModel().isRollover();
            RoundRectangle2D box = new RoundRectangle2D.Double(.5, .5, getWidth() - 1, getHeight() - 1, 8, 8);
            g.setColor(hover ? new Color(10, 18, 26) : new Color(3, 9, 15));
            g.fill(box);
            g.setColor(new Color(214, 160, 66, hover ? 155 : 80));
            g.draw(box);

            g.setColor(TEXT);
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();

            String text = getText();
            int max = getWidth() - 35;
            while (fm.stringWidth(text) > max && text.length() > 4) {
                text = text.substring(0, text.length() - 2) + "…";
            }
            g.drawString(text, 11, (getHeight() + fm.getAscent()) / 2 - 3);

            int cx = getWidth() - 18;
            int cy = getHeight() / 2;
            g.setColor(PALE);
            g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(cx - 4, cy - 2, cx, cy + 3);
            g.drawLine(cx, cy + 3, cx + 4, cy - 2);
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
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 9, 9);
            }
            g.setColor(new Color(214, 160, 66, 125));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 9, 9);
            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class SmallPageButton extends JButton {

    private final boolean active;
    private final Runnable action;

    SmallPageButton(String text, boolean active, Runnable action) {
        super(text);
        this.active = active;
        this.action = action;

        setPreferredSize(new Dimension(32, 28));
        setMinimumSize(new Dimension(32, 28));
        setMaximumSize(new Dimension(32, 28));

        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setFocusable(false);
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setMargin(new java.awt.Insets(0, 0, 0, 0));

        setForeground(active ? new Color(25, 16, 8) : PALE);
        setFont(new Font("Segoe UI", Font.BOLD, 12));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addActionListener(e -> {
            if (this.action != null) {
                this.action.run();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics raw) {
        Graphics2D g = (Graphics2D) raw.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (active) {
            g.setPaint(new GradientPaint(
                    0, 0, PALE,
                    getWidth(), getHeight(), new Color(174, 116, 43)
            ));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 7, 7);
        } else if (getModel().isRollover()) {
            g.setColor(new Color(214, 160, 66, 28));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 7, 7);
        } else {
            g.setColor(new Color(4, 10, 16, 235));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 7, 7);
        }

        g.setColor(new Color(214, 160, 66, 110));
        g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 7, 7);

        String text = getText();

        g.setFont(getFont());
        g.setColor(active ? new Color(25, 16, 8) : PALE);

        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = (getHeight() + fm.getAscent()) / 2 - 3;

        g.drawString(text, x, y);

        g.dispose();
    }
}

    private static final class BrandFooter extends JComponent {
        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawVeloraWingLogo(g, getWidth() / 2, 14, 82, PALE, true);
            g.setColor(TEXT);
            g.setFont(new Font("Serif", Font.BOLD, 20));
            drawCentered(g, "VELORA MOTORS", getWidth() / 2, 34);
            g.setColor(PALE);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            drawCentered(g, "PREMIUM BMW VEHICLE RENTAL", getWidth() / 2, 48);
            g.dispose();
        }
    }

    private static final class DarkScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor = new Color(214, 160, 66, 115);
            trackColor = new Color(3, 9, 15);
        }
        @Override protected JButton createDecreaseButton(int orientation) { return hiddenButton(); }
        @Override protected JButton createIncreaseButton(int orientation) { return hiddenButton(); }
        private JButton hiddenButton() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }
        @Override protected void paintThumb(Graphics raw, JComponent c, java.awt.Rectangle bounds) {
            if (bounds.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(214, 160, 66, 105));
            g.fillRoundRect(bounds.x + 1, bounds.y, Math.max(3, bounds.width - 2), bounds.height,
                    bounds.width, bounds.width);
            g.dispose();
        }
    }

    private static void drawCover(Graphics2D g, BufferedImage img, int x, int y, int w, int h) {
        if (img == null || w <= 0 || h <= 0) return;
        double scale = Math.max(w / (double) img.getWidth(), h / (double) img.getHeight());
        int iw = (int) Math.round(img.getWidth() * scale);
        int ih = (int) Math.round(img.getHeight() * scale);
        g.drawImage(img, x + (w - iw) / 2, y + (h - ih) / 2, iw, ih, null);
    }

    private static void drawIcon(Graphics2D g, String type, int cx, int cy, int size) {
        int s = size;
        int half = s / 2;
        switch (type) {
            case "CAR" -> {
                g.drawRoundRect(cx - half, cy - 4, s, 10, 4, 4);
                g.drawLine(cx - 8, cy - 4, cx - 4, cy - half);
                g.drawLine(cx - 4, cy - half, cx + 6, cy - half);
                g.drawLine(cx + 6, cy - half, cx + 10, cy - 4);
                g.fillOval(cx - 7, cy + 6, 5, 5);
                g.fillOval(cx + 5, cy + 6, 5, 5);
            }
            case "CAL" -> {
                g.drawRoundRect(cx - half, cy - half, s, s, 4, 4);
                g.drawLine(cx - half, cy - 4, cx + half, cy - 4);
                g.drawLine(cx - 6, cy - half - 3, cx - 6, cy - half + 5);
                g.drawLine(cx + 6, cy - half - 3, cx + 6, cy - half + 5);
            }
            case "CLOCK" -> {
                g.drawOval(cx - half, cy - half, s, s);
                g.drawLine(cx, cy, cx, cy - 7);
                g.drawLine(cx, cy, cx + 7, cy + 4);
            }
            case "DOLLAR" -> {
                g.setFont(new Font("Segoe UI", Font.BOLD, size));
                FontMetrics fm = g.getFontMetrics();
                g.drawString("$", cx - fm.stringWidth("$") / 2, cy + fm.getAscent() / 2 - 4);
            }
            default -> g.drawOval(cx - half, cy - half, s, s);
        }
    }

    private static void drawVeloraWingLogo(Graphics2D g, int cx, int cy, int width, Color color, boolean glow) {
        Graphics2D copy = (Graphics2D) g.create();
        copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float stroke = Math.max(2f, width / 42f);
        int half = width / 2;
        int vTop = cy - width / 7;
        int vBottom = cy + width / 4;

        if (glow) {
            copy.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 40));
            copy.setStroke(new BasicStroke(stroke + 6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawVeloraWingPaths(copy, cx, cy, half, vTop, vBottom);
        }

        copy.setPaint(new GradientPaint(cx - half, cy - width / 4, brighten(color, 35),
                cx + half, cy + width / 4, new Color(142, 93, 34)));
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

        int[] offsets = {0, 10, 20};
        for (int i = 0; i < offsets.length; i++) {
            int y = vTop + offsets[i];
            int longWing = half - i * 10;
            int inner = half / 4 - i * 2;
            p.moveTo(cx - inner, y);
            p.lineTo(cx - longWing, y);
            p.moveTo(cx + inner, y);
            p.lineTo(cx + longWing, y);
        }
        g.draw(p);
    }

    private static void drawCentered(Graphics2D g, String text, int cx, int baseline) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, cx - fm.stringWidth(text) / 2, baseline);
    }

    private static Color brighten(Color color, int amount) {
        return new Color(
                Math.min(255, color.getRed() + amount),
                Math.min(255, color.getGreen() + amount),
                Math.min(255, color.getBlue() + amount),
                color.getAlpha()
        );
    }
}
