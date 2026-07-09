package com.velora.ui;

import com.velora.authentication.Customer;
import com.velora.service.VehicleService;
import com.velora.vehicle.Vehicle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BillingPanel extends JPanel {

    private static final Color BACKGROUND = new Color(2, 7, 12);
    private static final Color CARD = new Color(6, 13, 20);
    private static final Color CARD_2 = new Color(9, 18, 27);
    private static final Color GOLD = new Color(214, 160, 66);
    private static final Color PALE = new Color(238, 201, 139);
    private static final Color TEXT = new Color(243, 244, 247);
    private static final Color MUTED = new Color(157, 164, 175);
    private static final Color GREEN = new Color(86, 207, 114);
    private static final Color RED = new Color(235, 93, 98);
    private static final Color BLUE = new Color(89, 151, 255);

    private static final int PAGE_SIZE = 8;

    private final Customer manager;
    private final VehicleService vehicleService = new VehicleService();

    private final List<Invoice> invoices = new ArrayList<>();
    private final List<Invoice> filteredInvoices = new ArrayList<>();

    private JTextField searchField;
    private DarkComboButton statusFilter;
    private DarkComboButton paymentFilter;
    private DarkComboButton dateFilter;

    private JLabel totalRevenueValue;
    private JLabel paidInvoicesValue;
    private JLabel pendingPaymentsValue;
    private JLabel lateFeesValue;

    private BillingTableModel tableModel;
    private JTable table;

    private JPanel paginationPanel;
    private JLabel showingLabel;

    private InvoiceSummaryPanel summaryPanel;

    private int currentPage = 1;
    private int totalPages = 1;

    public BillingPanel(Customer manager) {
        this.manager = manager;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8, 12, 18, 22));

        loadDemoInvoices();
        filteredInvoices.addAll(invoices);

        add(createContent(), BorderLayout.CENTER);
        refreshAll();
    }

    private JComponent createContent() {
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setOpaque(false);

        page.add(createHeader(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(14, 0));
        center.setOpaque(false);
        center.add(createMainArea(), BorderLayout.CENTER);

        summaryPanel = new InvoiceSummaryPanel();
        summaryPanel.setPreferredSize(new Dimension(315, 100));
        center.add(summaryPanel, BorderLayout.EAST);

        page.add(center, BorderLayout.CENTER);

        return page;
    }

    private JComponent createHeader() {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = label("Billing & Invoices", 28, Font.BOLD, TEXT);
        JLabel subtitle = label("Dashboard  ›  Billing", 12, Font.PLAIN, MUTED);

        left.add(title);
        left.add(Box.createVerticalStrut(3));
        left.add(subtitle);

        JButton generate = new GoldActionButton("Generate Invoice");
        generate.setPreferredSize(new Dimension(165, 42));
        generate.addActionListener(e -> addDemoInvoice());

        titleRow.add(left, BorderLayout.WEST);
        titleRow.add(generate, BorderLayout.EAST);

        wrapper.add(titleRow);
        wrapper.add(Box.createVerticalStrut(14));

        JPanel cards = new JPanel(new GridLayout(1, 4, 13, 0));
        cards.setOpaque(false);

        totalRevenueValue = label("$0.00", 24, Font.BOLD, PALE);
        paidInvoicesValue = label("0", 24, Font.BOLD, TEXT);
        pendingPaymentsValue = label("0", 24, Font.BOLD, PALE);
        lateFeesValue = label("$0.00", 24, Font.BOLD, TEXT);

        cards.add(metricCard("TOTAL REVENUE", totalRevenueValue, "Live from invoices", "MONEY", GOLD));
        cards.add(metricCard("PAID INVOICES", paidInvoicesValue, "Collected payments", "DOC", GREEN));
        cards.add(metricCard("PENDING PAYMENTS", pendingPaymentsValue, "Need confirmation", "CLOCK", GOLD));
        cards.add(metricCard("LATE FEES", lateFeesValue, "From overdue invoices", "WARN", RED));

        wrapper.add(cards);

        return wrapper;
    }

    private JComponent metricCard(String title, JLabel value, String hint, String icon, Color iconColor) {
        RoundedPanel card = new RoundedPanel(15, new Color(6, 13, 20, 232));
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(new EmptyBorder(15, 18, 15, 18));

        IconCircle iconCircle = new IconCircle(icon, iconColor);
        iconCircle.setPreferredSize(new Dimension(60, 60));
        card.add(iconCircle, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel titleLabel = label(title, 11, Font.BOLD, new Color(210, 216, 224));
        JLabel hintLabel = label("+ " + hint, 10, Font.PLAIN, iconColor == RED ? RED : GREEN);

        text.add(Box.createVerticalGlue());
        text.add(titleLabel);
        text.add(Box.createVerticalStrut(4));
        text.add(value);
        text.add(Box.createVerticalStrut(4));
        text.add(hintLabel);
        text.add(Box.createVerticalGlue());

        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private JComponent createMainArea() {
        RoundedPanel shell = new RoundedPanel(16, new Color(5, 12, 18, 232));
        shell.setLayout(new BorderLayout(0, 10));
        shell.setBorder(new EmptyBorder(14, 14, 12, 14));

        shell.add(createFilters(), BorderLayout.NORTH);
        shell.add(createTableCard(), BorderLayout.CENTER);
        shell.add(createPaginationBar(), BorderLayout.SOUTH);

        return shell;
    }

    private JComponent createFilters() {
        JPanel filters = new JPanel(new BorderLayout(12, 0));
        filters.setOpaque(false);

        searchField = new SearchBox("Search by invoice ID or customer...");
        searchField.setPreferredSize(new Dimension(330, 40));
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilters();
            }
        });

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(searchField);

        statusFilter = new DarkComboButton("All Status", "Paid", "Pending", "Overdue");
        paymentFilter = new DarkComboButton("All Methods", "Card", "Cash", "Bank Transfer");
        dateFilter = new DarkComboButton("This Month", "Today", "This Week", "Last 30 Days", "This Year");

        statusFilter.setOnChange(e -> applyFilters());
        paymentFilter.setOnChange(e -> applyFilters());
        dateFilter.setOnChange(e -> applyFilters());

        left.add(statusFilter);
        left.add(paymentFilter);
        left.add(dateFilter);

        JButton clear = new OutlineButton("Clear Filters");
        clear.setPreferredSize(new Dimension(125, 40));
        clear.addActionListener(e -> {
            searchField.setText("");
            statusFilter.setSelectedValue("All Status");
            paymentFilter.setSelectedValue("All Methods");
            dateFilter.setSelectedValue("This Month");
            applyFilters();
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(clear);

        filters.add(left, BorderLayout.CENTER);
        filters.add(right, BorderLayout.EAST);

        return filters;
    }

    private JComponent createTableCard() {
        tableModel = new BillingTableModel();
        table = new JTable(tableModel);
        configureTable(table);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row >= 0 && row < tableModel.getRowCount()) {
                    summaryPanel.setInvoice(tableModel.getInvoiceAt(row));
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(214, 160, 66, 55)));
        scroll.getViewport().setBackground(new Color(3, 9, 14));
        scroll.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setOpaque(false);

        return scroll;
    }

    private JComponent createPaginationBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(4, 0, 0, 0));

        showingLabel = label("Showing 0 to 0 of 0 invoices", 11, Font.PLAIN, MUTED);
        paginationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        paginationPanel.setOpaque(false);

        bar.add(showingLabel, BorderLayout.WEST);
        bar.add(paginationPanel, BorderLayout.EAST);

        return bar;
    }

    private JComponent createFooter() {
        RoundedPanel footer = new RoundedPanel(15, new Color(8, 14, 20, 226));
        footer.setLayout(new GridLayout(1, 5, 10, 0));
        footer.setPreferredSize(new Dimension(900, 75));
        footer.setBorder(new EmptyBorder(10, 18, 10, 18));

        footer.add(new FooterBrand());
        footer.add(new FooterItem("FLEET", "Premium vehicles", "CAR"));
        footer.add(new FooterItem("BILLING", "Invoices & payments", "DOC"));
        footer.add(new FooterItem("SECURE", "Trusted payments", "SHIELD"));
        footer.add(new FooterItem("SUPPORT", "24/7 assistance", "HEADSET"));

        return footer;
    }

    private void configureTable(JTable table) {
        table.setRowHeight(48);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(25, 36, 46));
        table.setBackground(new Color(3, 9, 14));
        table.setForeground(TEXT);
        table.setSelectionBackground(new Color(72, 45, 19));
        table.setSelectionForeground(TEXT);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setOpaque(false);
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);
        header.setPreferredSize(new Dimension(100, 42));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(214, 160, 66, 90)));
        header.setDefaultRenderer(new BillingHeaderRenderer());

        DefaultTableCellRenderer renderer = new BillingCellRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        table.getColumnModel().getColumn(0).setPreferredWidth(95);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(78);
        table.getColumnModel().getColumn(4).setPreferredWidth(105);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(105);
        table.getColumnModel().getColumn(8).setPreferredWidth(90);
        table.getColumnModel().getColumn(9).setPreferredWidth(85);
    }

    private void refreshAll() {
        refreshMetrics();
        applyFilters();
    }

    private void refreshMetrics() {
        double totalRevenue = invoices.stream().mapToDouble(Invoice::totalAmount).sum();
        long paid = invoices.stream().filter(i -> i.status.equals("Paid")).count();
        long pending = invoices.stream().filter(i -> i.status.equals("Pending")).count();
        double lateFees = invoices.stream().mapToDouble(i -> i.lateFee).sum();

        totalRevenueValue.setText(formatMoney(totalRevenue));
        paidInvoicesValue.setText(String.valueOf(paid));
        pendingPaymentsValue.setText(String.valueOf(pending));
        lateFeesValue.setText(formatMoney(lateFees));
    }

    private void applyFilters() {
        String query = searchField == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        String status = statusFilter == null ? "All Status" : statusFilter.getSelectedValue();
        String method = paymentFilter == null ? "All Methods" : paymentFilter.getSelectedValue();

        filteredInvoices.clear();

        for (Invoice invoice : invoices) {
            boolean matchesSearch = query.isBlank()
                    || invoice.invoiceId.toLowerCase(Locale.ROOT).contains(query)
                    || invoice.customerName.toLowerCase(Locale.ROOT).contains(query)
                    || invoice.vehicleName.toLowerCase(Locale.ROOT).contains(query);

            boolean matchesStatus = "All Status".equals(status) || invoice.status.equals(status);
            boolean matchesMethod = "All Methods".equals(method) || invoice.paymentMethod.equals(method);

            if (matchesSearch && matchesStatus && matchesMethod) {
                filteredInvoices.add(invoice);
            }
        }

        currentPage = 1;
        refreshPage();
    }

    private void refreshPage() {
        totalPages = Math.max(1, (int) Math.ceil(filteredInvoices.size() / (double) PAGE_SIZE));
        currentPage = Math.max(1, Math.min(currentPage, totalPages));

        int fromIndex = Math.min((currentPage - 1) * PAGE_SIZE, filteredInvoices.size());
        int toIndex = Math.min(fromIndex + PAGE_SIZE, filteredInvoices.size());

        List<Invoice> pageData = new ArrayList<>(filteredInvoices.subList(fromIndex, toIndex));
        tableModel.setRows(pageData);

        if (!pageData.isEmpty()) {
            table.setRowSelectionInterval(0, 0);
            summaryPanel.setInvoice(pageData.get(0));
        } else {
            summaryPanel.setInvoice(null);
        }

        refreshPagination();
    }

    private void refreshPagination() {
        paginationPanel.removeAll();

        paginationPanel.add(new SmallPageButton("‹", false, () -> {
            currentPage--;
            refreshPage();
        }));

        for (int i = 1; i <= totalPages; i++) {
            final int pageNumber = i;
            paginationPanel.add(new SmallPageButton(
                    String.valueOf(pageNumber),
                    pageNumber == currentPage,
                    () -> {
                        currentPage = pageNumber;
                        refreshPage();
                    }
            ));
        }

        paginationPanel.add(new SmallPageButton("›", false, () -> {
            currentPage++;
            refreshPage();
        }));

        int from = filteredInvoices.isEmpty() ? 0 : (currentPage - 1) * PAGE_SIZE + 1;
        int to = Math.min(filteredInvoices.size(), currentPage * PAGE_SIZE);
        showingLabel.setText("Showing " + from + " to " + to + " of " + filteredInvoices.size() + " invoices");

        paginationPanel.revalidate();
        paginationPanel.repaint();
    }

    private void addDemoInvoice() {
        JTextField customerField = new JTextField("New Customer");
        JTextField vehicleField = new JTextField(vehicleService.getAllVehicles().isEmpty()
                ? "Velora Vehicle"
                : FleetUiData.displayName(vehicleService.getAllVehicles().get(0)));
        JTextField daysField = new JTextField("4");
        JTextField baseField = new JTextField("1200");
        JTextField lateFeeField = new JTextField("0");

        DarkComboButton statusBox = new DarkComboButton("Pending", "Paid", "Overdue");
        DarkComboButton methodBox = new DarkComboButton("Card", "Cash", "Bank Transfer");

        JPanel form = new JPanel(new GridLayout(7, 2, 8, 8));
        form.setBorder(new EmptyBorder(12, 12, 12, 12));
        form.add(new JLabel("Customer Name"));
        form.add(customerField);
        form.add(new JLabel("Vehicle"));
        form.add(vehicleField);
        form.add(new JLabel("Rental Days"));
        form.add(daysField);
        form.add(new JLabel("Base Amount"));
        form.add(baseField);
        form.add(new JLabel("Late Fee"));
        form.add(lateFeeField);
        form.add(new JLabel("Status"));
        form.add(statusBox);
        form.add(new JLabel("Payment Method"));
        form.add(methodBox);

        int choice = JOptionPane.showConfirmDialog(
                this,
                form,
                "Generate Invoice",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String customer = customerField.getText().trim();
            String vehicle = vehicleField.getText().trim();
            int days = Integer.parseInt(daysField.getText().trim());
            double base = Double.parseDouble(baseField.getText().trim());
            double late = Double.parseDouble(lateFeeField.getText().trim());

            if (customer.isBlank() || vehicle.isBlank()) {
                throw new IllegalArgumentException("Customer and vehicle are required.");
            }
            if (days <= 0 || base < 0 || late < 0) {
                throw new IllegalArgumentException("Days must be positive, and amounts cannot be negative.");
            }

            int next = invoices.size() + 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a", Locale.ENGLISH);
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = start.plusDays(days);

            Invoice invoice = new Invoice(
                    "INV-" + String.format("%04d", 1000 + next),
                    customer,
                    vehicle,
                    days,
                    base,
                    late,
                    statusBox.getSelectedValue(),
                    methodBox.getSelectedValue(),
                    start.format(formatter),
                    end.format(formatter)
            );

            invoices.add(invoice);
            refreshAll();

            int lastPage = Math.max(1, (int) Math.ceil(filteredInvoices.size() / (double) PAGE_SIZE));
            currentPage = lastPage;
            refreshPage();

            JOptionPane.showMessageDialog(
                    this,
                    "Invoice " + invoice.invoiceId + " has been generated successfully.",
                    "Velora Billing",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Days and amounts must be valid numbers.", "Velora Billing", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Velora Billing", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void recordPayment(Invoice invoice) {
        if (invoice == null) {
            return;
        }

        if ("Paid".equals(invoice.status)) {
            JOptionPane.showMessageDialog(
                    this,
                    "This invoice is already paid.",
                    "Velora Billing",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Record payment for " + invoice.invoiceId + "?\nTotal: " + formatMoney(invoice.totalAmount()),
                "Confirm Payment",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        invoice.status = "Paid";
        invoice.paymentMethod = "Card";

        int pageBeforeRefresh = currentPage;
        refreshMetrics();
        applyFilters();
        currentPage = Math.min(pageBeforeRefresh, totalPages);
        refreshPage();

        JOptionPane.showMessageDialog(
                this,
                "Payment recorded successfully for " + invoice.invoiceId + ".",
                "Velora Billing",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void downloadInvoice(Invoice invoice) {
        if (invoice == null) {
            return;
        }

        try {
            Path folder = Path.of(System.getProperty("user.dir"), "generated-invoices");
            Files.createDirectories(folder);

            Path file = folder.resolve(invoice.invoiceId + ".txt");

            String content = ""
                    + "VELORA MOTORS - INVOICE\n"
                    + "========================\n\n"
                    + "Invoice ID: " + invoice.invoiceId + "\n"
                    + "Customer: " + invoice.customerName + "\n"
                    + "Vehicle: " + invoice.vehicleName + "\n"
                    + "Rental Period: " + invoice.startDate + " -> " + invoice.endDate + "\n"
                    + "Rental Days: " + invoice.rentalDays + "\n\n"
                    + "Base Rental: " + formatMoney(invoice.baseAmount) + "\n"
                    + "Late Fee: " + formatMoney(invoice.lateFee) + "\n"
                    + "Tax (5%): " + formatMoney(invoice.tax) + "\n"
                    + "Total Amount: " + formatMoney(invoice.totalAmount()) + "\n\n"
                    + "Status: " + invoice.status + "\n"
                    + "Payment Method: " + invoice.paymentMethod + "\n";

            Files.writeString(file, content, StandardCharsets.UTF_8);

            JOptionPane.showMessageDialog(
                    this,
                    "Invoice file created successfully:\n" + file.toAbsolutePath(),
                    "Velora Billing",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to download invoice: " + ex.getMessage(),
                    "Velora Billing",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void loadDemoInvoices() {
        invoices.clear();

        String[] customers = {
                "Noah Anderson", "Ava Thompson", "Liam Johnson", "Sophia Brown", "Daniel White",
                "Olivia Martinez", "James Wilson", "Emily Davis", "Michael Smith", "Sarah Johnson",
                "David Brown", "John Doe", "Lina Khaled", "Adam Naser", "Maya Saleh", "Rami Hasan"
        };
        String[] statuses = {"Paid", "Pending", "Paid", "Overdue", "Paid", "Pending"};
        String[] methods = {"Card", "Cash", "Bank Transfer"};

        List<Vehicle> vehicles = vehicleService.getAllVehicles();
        for (int i = 0; i < vehicles.size(); i++) {
            Vehicle vehicle = vehicles.get(i);
            int days = 2 + (i % 7);
            double base = vehicle.getDailyPrice() * days;
            double late = "Overdue".equals(statuses[i % statuses.length]) ? 50 + (i % 4) * 35 : 0;
            int startDay = 1 + (i % 9);
            int endDay = startDay + days;
            invoices.add(new Invoice(
                    "INV-" + String.format("%04d", 1001 + i),
                    customers[i % customers.length],
                    FleetUiData.displayName(vehicle),
                    days,
                    base,
                    late,
                    statuses[i % statuses.length],
                    methods[i % methods.length],
                    String.format("%02d Jul 2026 10:00 AM", startDay),
                    String.format("%02d Jul 2026 10:00 AM", endDay)
            ));
        }
    }

    private JLabel label(String text, int size, int style, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", style, size));
        label.setForeground(color);
        return label;
    }

    private static String formatMoney(double value) {
        return "$" + String.format("%,.2f", value);
    }

    private static final class Invoice {
        private final String invoiceId;
        private final String customerName;
        private final String vehicleName;
        private final int rentalDays;
        private final double baseAmount;
        private final double lateFee;
        private final double tax;
        private String status;
        private String paymentMethod;
        private final String startDate;
        private final String endDate;

        Invoice(String invoiceId, String customerName, String vehicleName, int rentalDays,
                double baseAmount, double lateFee, String status, String paymentMethod,
                String startDate, String endDate) {
            this.invoiceId = invoiceId;
            this.customerName = customerName;
            this.vehicleName = vehicleName;
            this.rentalDays = rentalDays;
            this.baseAmount = baseAmount;
            this.lateFee = lateFee;
            this.tax = (baseAmount + lateFee) * 0.05;
            this.status = status;
            this.paymentMethod = paymentMethod;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        double totalAmount() {
            return baseAmount + lateFee + tax;
        }
    }

    private final class BillingTableModel extends AbstractTableModel {

        private final String[] columns = {
                "Invoice ID", "Customer", "Vehicle", "Days", "Base", "Late Fee", "Tax", "Total", "Status", "Actions"
        };

        private final List<Invoice> rows = new ArrayList<>();

        void setRows(List<Invoice> invoices) {
            rows.clear();
            rows.addAll(invoices);
            fireTableDataChanged();
        }

        Invoice getInvoiceAt(int row) {
            return rows.get(row);
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Invoice invoice = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> invoice.invoiceId;
                case 1 -> invoice.customerName;
                case 2 -> invoice.vehicleName;
                case 3 -> invoice.rentalDays;
                case 4 -> formatMoney(invoice.baseAmount);
                case 5 -> formatMoney(invoice.lateFee);
                case 6 -> formatMoney(invoice.tax);
                case 7 -> formatMoney(invoice.totalAmount());
                case 8 -> invoice.status;
                default -> invoice.status.equals("Paid") ? "View   Download" : "View   Pay";
            };
        }
    }

    private static final class BillingHeaderRenderer extends DefaultTableCellRenderer {

        BillingHeaderRenderer() {
            setOpaque(false);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(new EmptyBorder(0, 6, 0, 6));
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean selected,
                boolean focus,
                int row,
                int column
        ) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            label.setOpaque(false);
            label.setForeground(PALE);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setBorder(new EmptyBorder(0, 8, 0, 8));
            return label;
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setPaint(new GradientPaint(
                    0, 0, new Color(16, 25, 32),
                    getWidth(), getHeight(), new Color(8, 14, 20)
            ));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setPaint(new GradientPaint(
                    0, 0, new Color(214, 160, 66, 38),
                    getWidth(), 0, new Color(214, 160, 66, 5)
            ));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(new Color(214, 160, 66, 105));
            g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);

            g.setColor(new Color(255, 255, 255, 14));
            g.drawLine(0, 1, getWidth(), 1);

            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class BillingCellRenderer extends DefaultTableCellRenderer {

        BillingCellRenderer() {
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(new EmptyBorder(0, 7, 0, 7));
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean selected,
                boolean focus,
                int row,
                int column
        ) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, selected, focus, row, column);

            label.setFont(new Font("Segoe UI", column == 0 || column == 7 ? Font.BOLD : Font.PLAIN, 12));
            label.setBackground(selected ? new Color(72, 45, 19) : new Color(3, 9, 14));
            label.setForeground(TEXT);

            if (column == 0 || column == 9) {
                label.setForeground(PALE);
            }

            if (column == 8) {
                String status = String.valueOf(value);
                if ("Paid".equals(status)) {
                    label.setForeground(GREEN);
                } else if ("Pending".equals(status)) {
                    label.setForeground(GOLD);
                } else {
                    label.setForeground(RED);
                }
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            }

            if (column == 5 && String.valueOf(value).contains("$0.00")) {
                label.setForeground(new Color(211, 216, 223));
            } else if (column == 5) {
                label.setForeground(RED);
            }

            return label;
        }
    }

    private final class InvoiceSummaryPanel extends RoundedPanel {

        private Invoice invoice;
        private final JButton recordPaymentButton = new SummaryButton("Record Payment", true);
        private final JButton downloadButton = new SummaryButton("Download Invoice", false);

        InvoiceSummaryPanel() {
            super(16, new Color(5, 12, 18, 238));
            setLayout(null);
            setCursor(Cursor.getDefaultCursor());

            recordPaymentButton.addActionListener(e -> recordPayment(invoice));
            downloadButton.addActionListener(e -> downloadInvoice(invoice));

            add(recordPaymentButton);
            add(downloadButton);
        }

        void setInvoice(Invoice invoice) {
            this.invoice = invoice;

            boolean hasInvoice = invoice != null;
            recordPaymentButton.setVisible(hasInvoice);
            downloadButton.setVisible(hasInvoice);

            if (hasInvoice) {
                recordPaymentButton.setText("Paid".equals(invoice.status) ? "Payment Recorded" : "Record Payment");
                recordPaymentButton.setEnabled(true);
                recordPaymentButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                downloadButton.setEnabled(true);
                downloadButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            repaint();
        }

        @Override
        public void doLayout() {
            int buttonW = Math.max(120, getWidth() - 40);
            int bottom = getHeight() - 22;

            downloadButton.setBounds(20, bottom - 38, buttonW, 38);
            recordPaymentButton.setBounds(20, bottom - 92, buttonW, 42);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            super.paintComponent(raw);

            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int y = 26;

            g.setColor(TEXT);
            g.setFont(new Font("Segoe UI", Font.BOLD, 15));
            g.drawString("Invoice Summary", 20, y);

            if (invoice == null) {
                g.setColor(MUTED);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g.drawString("Select an invoice from the table.", 20, y + 35);
                g.dispose();
                return;
            }

            drawBadge(g, w - 88, 18, invoice.status);

            y += 38;
            divider(g, y, w);
            y += 24;

            g.setColor(MUTED);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g.drawString("Invoice ID", 20, y);

            g.setColor(TEXT);
            g.setFont(new Font("Segoe UI", Font.BOLD, 20));
            g.drawString(invoice.invoiceId, 20, y + 26);

            y += 65;
            drawInfoLine(g, "Customer", invoice.customerName, 20, y);
            y += 52;
            drawInfoLine(g, "Vehicle", invoice.vehicleName, 20, y);
            y += 52;
            drawInfoLine(g, "Rental Period", invoice.startDate + "  →  " + invoice.endDate, 20, y);
            y += 52;
            drawInfoLine(g, "Rental Days", invoice.rentalDays + " days", 20, y);

            y += 46;
            divider(g, y, w);
            y += 28;

            drawMoneyRow(g, "Base Rental", invoice.baseAmount, y, false);
            y += 27;
            drawMoneyRow(g, "Late Fee", invoice.lateFee, y, invoice.lateFee > 0);
            y += 27;
            drawMoneyRow(g, "Tax (5%)", invoice.tax, y, false);

            int totalY = Math.min(y + 58, Math.max(y + 38, getHeight() - 148));

            divider(g, totalY - 34, w);

            g.setColor(TEXT);
            g.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g.drawString("Total Amount", 20, totalY);

            g.setColor(PALE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 24));
            String total = formatMoney(invoice.totalAmount());
            g.drawString(total, w - g.getFontMetrics().stringWidth(total) - 20, totalY);

            g.dispose();
        }

        private void drawInfoLine(Graphics2D g, String title, String value, int x, int y) {
            g.setColor(PALE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g.drawString(title, x, y);
            g.setColor(TEXT);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            drawTrimmed(g, value, x, y + 20, getWidth() - x - 25);
        }

        private void drawTrimmed(Graphics2D g, String value, int x, int y, int maxWidth) {
            String text = value == null ? "" : value;
            FontMetrics fm = g.getFontMetrics();

            while (fm.stringWidth(text) > maxWidth && text.length() > 4) {
                text = text.substring(0, text.length() - 2) + "…";
            }

            g.drawString(text, x, y);
        }

        private void drawMoneyRow(Graphics2D g, String title, double value, int y, boolean red) {
            g.setColor(MUTED);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g.drawString(title, 20, y);

            String money = formatMoney(value);
            g.setColor(red ? RED : TEXT);
            g.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g.drawString(money, getWidth() - g.getFontMetrics().stringWidth(money) - 20, y);
        }

        private void divider(Graphics2D g, int y, int w) {
            g.setColor(new Color(214, 160, 66, 60));
            g.drawLine(20, y, w - 20, y);
        }

        private void drawBadge(Graphics2D g, int x, int y, String status) {
            Color c = switch (status) {
                case "Paid" -> GREEN;
                case "Pending" -> GOLD;
                default -> RED;
            };

            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 24));
            g.fillRoundRect(x, y, 68, 25, 9, 9);
            g.setColor(c);
            g.drawRoundRect(x, y, 68, 25, 9, 9);
            g.setFont(new Font("Segoe UI", Font.BOLD, 10));
            int tw = g.getFontMetrics().stringWidth(status);
            g.drawString(status, x + (68 - tw) / 2, y + 17);
        }
    }

    private static final class SummaryButton extends JButton {

        private final boolean filled;

        SummaryButton(String text, boolean filled) {
            super(text);
            this.filled = filled;

            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setForeground(filled ? new Color(30, 20, 8) : PALE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            boolean hover = getModel().isRollover() && isEnabled();

            if (filled) {
                g.setPaint(new GradientPaint(
                        0, 0, hover ? new Color(250, 214, 150) : PALE,
                        getWidth(), getHeight(), hover ? new Color(194, 130, 45) : new Color(170, 110, 36)
                ));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                if (!isEnabled()) {
                    g.setColor(new Color(0, 0, 0, 85));
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }

                g.setColor(isEnabled() ? new Color(30, 20, 8) : new Color(210, 210, 210));
            } else {
                g.setColor(hover ? new Color(10, 18, 26) : new Color(3, 9, 15));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g.setColor(new Color(214, 160, 66, hover ? 155 : 110));
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g.setColor(PALE);
            }

            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            g.drawString(
                    getText(),
                    (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent()) / 2 - 3
            );

            g.dispose();
        }
    }

    private static final class DarkComboButton extends JButton {

        private final String[] items;
        private String selected;
        private java.awt.event.ActionListener listener;

        DarkComboButton(String... items) {
            super(items.length > 0 ? items[0] : "");
            this.items = items;
            this.selected = getText();

            setPreferredSize(new Dimension(140, 40));
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(TEXT);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addActionListener(e -> showPopup());
        }

        String getSelectedValue() {
            return selected;
        }

        void setSelectedValue(String value) {
            for (String item : items) {
                if (item.equals(value)) {
                    selected = item;
                    setText(item);
                    repaint();
                    return;
                }
            }
        }

        void setOnChange(java.awt.event.ActionListener listener) {
            this.listener = listener;
        }

        private void showPopup() {
            javax.swing.JPopupMenu popup = new javax.swing.JPopupMenu();
            popup.setBackground(new Color(3, 9, 15));
            popup.setBorder(BorderFactory.createLineBorder(new Color(214, 160, 66, 130)));

            for (String itemText : items) {
                javax.swing.JMenuItem item = new javax.swing.JMenuItem(itemText);
                item.setOpaque(true);
                item.setBackground(new Color(3, 9, 15));
                item.setForeground(TEXT);
                item.setFont(new Font("Segoe UI", Font.BOLD, 11));
                item.addActionListener(e -> {
                    selected = itemText;
                    setText(itemText);
                    repaint();
                    if (listener != null) {
                        listener.actionPerformed(e);
                    }
                });
                popup.add(item);
            }

            popup.show(this, 0, getHeight() + 3);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g.setColor(getModel().isRollover() ? new Color(9, 18, 26) : new Color(3, 9, 15));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 9, 9);
            g.setColor(new Color(214, 160, 66, getModel().isRollover() ? 150 : 100));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 9, 9);

            g.setColor(TEXT);
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            g.drawString(getText(), 13, (getHeight() + fm.getAscent()) / 2 - 3);

            int cx = getWidth() - 18;
            int cy = getHeight() / 2;
            g.setColor(PALE);
            g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(cx - 4, cy - 2, cx, cy + 3);
            g.drawLine(cx, cy + 3, cx + 4, cy - 2);
            g.dispose();
        }
    }

    private static final class SearchBox extends JTextField {

        private final String placeholder;

        SearchBox(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setForeground(TEXT);
            setCaretColor(PALE);
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setBorder(new EmptyBorder(0, 38, 0, 12));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(new Color(3, 9, 15));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 9, 9);
            g.setColor(new Color(214, 160, 66, isFocusOwner() ? 130 : 65));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 9, 9);

            g.setColor(PALE);
            g.setStroke(new BasicStroke(1.5f));
            g.drawOval(15, getHeight() / 2 - 7, 12, 12);
            g.drawLine(25, getHeight() / 2 + 4, 31, getHeight() / 2 + 10);

            g.dispose();
            super.paintComponent(raw);

            if (getText().isBlank() && !isFocusOwner()) {
                g = (Graphics2D) raw.create();
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setColor(MUTED);
                g.setFont(getFont());
                g.drawString(placeholder, 42, (getHeight() + g.getFontMetrics().getAscent()) / 2 - 3);
                g.dispose();
            }
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

            RoundRectangle2D shape = new RoundRectangle2D.Double(
                    .5, .5, getWidth() - 1, getHeight() - 1, radius, radius
            );

            g.setColor(new Color(0, 0, 0, 75));
            g.fillRoundRect(5, 7, Math.max(0, getWidth() - 10), Math.max(0, getHeight() - 10), radius, radius);

            g.setPaint(new GradientPaint(
                    0, 0, brighten(fill, 24),
                    getWidth(), getHeight(), fill
            ));
            g.fill(shape);

            g.setPaint(new GradientPaint(
                    0, 0, new Color(255, 235, 181, 28),
                    getWidth(), 0, new Color(214, 160, 66, 2)
            ));
            g.fill(shape);

            g.setColor(new Color(214, 160, 66, 84));
            g.draw(shape);

            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class IconCircle extends JComponent {

        private final String icon;
        private final Color color;

        IconCircle(String icon, Color color) {
            this.icon = icon;
            this.color = color;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int s = Math.min(getWidth(), getHeight()) - 6;
            int x = (getWidth() - s) / 2;
            int y = (getHeight() - s) / 2;

            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
            g.fillOval(x, y, s, s);
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 135));
            g.drawOval(x, y, s, s);
            g.setColor(color);
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            drawIcon(g, icon, getWidth() / 2, getHeight() / 2, 22);

            g.dispose();
        }
    }

    private static final class FooterBrand extends JComponent {

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawVeloraMark(g, 45, getHeight() / 2, 72);
            g.setColor(TEXT);
            g.setFont(new Font("Segoe UI", Font.BOLD, 18));
            g.drawString("VELORA MOTORS", 88, 30);
            g.setColor(PALE);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            g.drawString("PREMIUM VEHICLE RENTAL", 90, 48);
            g.dispose();
        }
    }

    private static final class FooterItem extends JComponent {

        private final String title;
        private final String subtitle;
        private final String icon;

        FooterItem(String title, String subtitle, String icon) {
            this.title = title;
            this.subtitle = subtitle;
            this.icon = icon;
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cx = 34;
            int cy = getHeight() / 2;
            g.setColor(new Color(214, 160, 66, 26));
            g.fillOval(cx - 18, cy - 18, 36, 36);
            g.setColor(new Color(214, 160, 66, 110));
            g.drawOval(cx - 18, cy - 18, 36, 36);
            g.setColor(PALE);
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawIcon(g, icon, cx, cy, 18);

            g.setColor(TEXT);
            g.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g.drawString(title, 62, 29);
            g.setColor(MUTED);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g.drawString(subtitle, 62, 47);

            g.dispose();
        }
    }

    private static final class GoldActionButton extends JButton {

        GoldActionButton(String text) {
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

            g.setColor(new Color(3, 9, 15));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            if (getModel().isRollover()) {
                g.setColor(new Color(214, 160, 66, 22));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
            g.setColor(new Color(214, 160, 66, 120));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

            g.setColor(PALE);
            g.setStroke(new BasicStroke(1.7f));
            int plusX = 18;
            int plusY = getHeight() / 2;
            g.drawLine(plusX - 5, plusY, plusX + 5, plusY);
            g.drawLine(plusX, plusY - 5, plusX, plusY + 5);

            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class OutlineButton extends JButton {

        OutlineButton(String text) {
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

            g.setColor(getModel().isRollover() ? new Color(10, 18, 26) : new Color(3, 9, 15));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 9, 9);
            g.setColor(new Color(214, 160, 66, 110));
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
                g.setPaint(new GradientPaint(0, 0, PALE, getWidth(), getHeight(), new Color(174, 116, 43)));
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
            g.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, (getHeight() + fm.getAscent()) / 2 - 3);
            g.dispose();
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
            return hidden();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return hidden();
        }

        private JButton hidden() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintThumb(Graphics raw, JComponent c, Rectangle b) {
            if (b.isEmpty()) {
                return;
            }
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(214, 160, 66, 105));
            g.fillRoundRect(b.x + 1, b.y, Math.max(3, b.width - 2), b.height, b.width, b.width);
            g.dispose();
        }
    }

    private static void drawIcon(Graphics2D g, String type, int cx, int cy, int size) {
        int h = size / 2;
        switch (type) {
            case "MONEY" -> {
                g.drawOval(cx - h, cy - h, size, size);
                g.drawString("$", cx - 4, cy + 5);
            }
            case "DOC" -> {
                g.drawRoundRect(cx - h + 3, cy - h, size - 6, size, 3, 3);
                g.drawLine(cx - 5, cy - 3, cx + 5, cy - 3);
                g.drawLine(cx - 5, cy + 3, cx + 5, cy + 3);
            }
            case "CLOCK" -> {
                g.drawOval(cx - h, cy - h, size, size);
                g.drawLine(cx, cy, cx, cy - 7);
                g.drawLine(cx, cy, cx + 6, cy + 4);
            }
            case "WARN" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(cx, cy - h);
                p.lineTo(cx + h, cy + h);
                p.lineTo(cx - h, cy + h);
                p.closePath();
                g.draw(p);
                g.drawLine(cx, cy - 2, cx, cy + 6);
                g.fillOval(cx - 1, cy + 10, 3, 3);
            }
            case "CAR" -> {
                g.drawRoundRect(cx - h, cy - 3, size, 9, 4, 4);
                g.drawLine(cx - 7, cy - 3, cx - 3, cy - h);
                g.drawLine(cx - 3, cy - h, cx + 6, cy - h);
                g.drawLine(cx + 6, cy - h, cx + 9, cy - 3);
                g.fillOval(cx - 7, cy + 5, 4, 4);
                g.fillOval(cx + 5, cy + 5, 4, 4);
            }
            case "SHIELD" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(cx, cy - h);
                p.lineTo(cx + h, cy - 4);
                p.lineTo(cx + h - 4, cy + h);
                p.lineTo(cx, cy + h + 5);
                p.lineTo(cx - h + 4, cy + h);
                p.lineTo(cx - h, cy - 4);
                p.closePath();
                g.draw(p);
            }
            case "HEADSET" -> {
                g.drawArc(cx - h, cy - h, size, size, 0, 180);
                g.drawLine(cx - h, cy, cx - h, cy + 7);
                g.drawLine(cx + h, cy, cx + h, cy + 7);
                g.drawArc(cx - 4, cy + 7, 12, 7, 180, 180);
            }
            default -> {
                g.drawOval(cx - h, cy - h, size, size);
            }
        }
    }

    private static void drawVeloraMark(Graphics2D g, int cx, int cy, int width) {
        Graphics2D c = (Graphics2D) g.create();
        c.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        c.setColor(PALE);
        c.setStroke(new BasicStroke(Math.max(2f, width / 42f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int half = width / 2;
        Path2D p = new Path2D.Double();
        p.moveTo(cx, cy + width / 4.0);
        p.lineTo(cx - half / 3.0, cy - width / 5.0);
        p.lineTo(cx - half, cy - width / 5.0);
        p.moveTo(cx, cy + width / 4.0);
        p.lineTo(cx + half / 3.0, cy - width / 5.0);
        p.lineTo(cx + half, cy - width / 5.0);
        p.moveTo(cx - 8, cy - 4);
        p.lineTo(cx - half + 12, cy - 4);
        p.moveTo(cx + 8, cy - 4);
        p.lineTo(cx + half - 12, cy - 4);
        c.draw(p);
        c.dispose();
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
