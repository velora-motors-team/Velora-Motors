package com.velora.ui;

import com.velora.authentication.Customer;
import com.velora.service.VehicleService;

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
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CustomerBillingPanel extends JPanel {

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

private final Customer customer;
private final VehicleService vehicleService = new VehicleService();
private final CustomerAccountState accountState;

private final List<CustomerAccountState.CustomerInvoice> filteredInvoices = new ArrayList<>();

private JTextField searchField;
private DarkComboButton statusFilter;
private DarkComboButton paymentFilter;
private DarkComboButton dateFilter;

private JLabel totalRevenueValue;
private JLabel walletBalanceValue;
private JLabel paidInvoicesValue;
private JLabel pendingPaymentsValue;
private JLabel lateFeesValue;
private JLabel totalSpentHint;
private JLabel walletBalanceHint;
private JLabel paidInvoicesHint;
private JLabel pendingPaymentsHint;
private JLabel lateFeesHint;

private BillingTableModel tableModel;
private JTable table;

private JPanel paginationPanel;
private JLabel showingLabel;

private InvoiceSummaryPanel summaryPanel;

private int currentPage = 1;
private int totalPages = 1;

public CustomerBillingPanel(Customer customer) {
    this.customer = customer;
    this.accountState = CustomerAccountState.forCustomer(customer);
    setOpaque(false);
    setLayout(new BorderLayout());
    setBorder(new EmptyBorder(8, 12, 18, 22));

    filteredInvoices.addAll(accountState.getInvoices());
    accountState.addChangeListener(this::refreshAll);

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
    JPanel wrapper = new JPanel(new BorderLayout(0, 16));
    wrapper.setOpaque(false);

    JPanel titleRow = new JPanel(new BorderLayout());
    titleRow.setOpaque(false);

    JPanel left = new JPanel();
    left.setOpaque(false);
    left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

    JLabel title = label("Billing & Invoices", 30, Font.BOLD, TEXT);
    JLabel subtitle = label(
            "Track payments, invoices, and billing history.",
            13,
            Font.PLAIN,
            new Color(205, 208, 214)
    );

    left.add(title);
    left.add(Box.createVerticalStrut(5));
    left.add(subtitle);

    titleRow.add(left, BorderLayout.WEST);

    JPanel cards = new JPanel(new GridLayout(1, 5, 12, 0));
    cards.setOpaque(false);
    cards.setBorder(new EmptyBorder(0, 0, 0, 0));

    totalRevenueValue = label("$0.00", 22, Font.PLAIN, TEXT);
    walletBalanceValue = label("$0.00", 29, Font.BOLD, TEXT);
    paidInvoicesValue = label("0", 22, Font.PLAIN, TEXT);
    pendingPaymentsValue = label("0", 22, Font.PLAIN, TEXT);
    lateFeesValue = label("$0.00", 22, Font.PLAIN, TEXT);

    cards.add(metricCard(
            "AVAILABLE BALANCE",
            walletBalanceValue,
            walletBalanceHint = label("", 8, Font.PLAIN, PALE),
            "MONEY",
            GOLD
    ));

    cards.add(metricCard(
            "TOTAL SPENT",
            totalRevenueValue,
            totalSpentHint = label("", 8, Font.PLAIN, GREEN),
            "MONEY",
            GOLD
    ));

    cards.add(metricCard(
            "PAID INVOICES",
            paidInvoicesValue,
            paidInvoicesHint = label("", 8, Font.PLAIN, GREEN),
            "DOC",
            GOLD
    ));

    cards.add(metricCard(
            "PENDING PAYMENTS",
            pendingPaymentsValue,
            pendingPaymentsHint = label("", 8, Font.PLAIN, GOLD),
            "CLOCK",
            GOLD
    ));

    cards.add(metricCard(
            "LATE FEES",
            lateFeesValue,
            lateFeesHint = label("", 8, Font.PLAIN, RED),
            "WARN",
            RED
    ));

    wrapper.add(titleRow, BorderLayout.NORTH);
    wrapper.add(cards, BorderLayout.CENTER);

    return wrapper;
}

private JComponent metricCard(String title, JLabel value, JLabel hintLabel, String icon, Color iconColor) {
    RoundedPanel card = new RoundedPanel(14, new Color(6, 13, 20, 238));
    card.setLayout(new BorderLayout(13, 0));
    boolean walletCard = "AVAILABLE BALANCE".equals(title);
    card.setBorder(new EmptyBorder(walletCard ? 7 : 8, walletCard ? 12 : 13, walletCard ? 7 : 8, walletCard ? 12 : 13));
    card.setPreferredSize(new Dimension(walletCard ? 285 : 235, 100));
    card.setMinimumSize(new Dimension(walletCard ? 230 : 170, 100));
    card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

    MetricIconBadge iconBadge = new MetricIconBadge(icon, iconColor);
    Dimension iconSize = walletCard ? new Dimension(62, 62) : new Dimension(54, 54);
    iconBadge.setPreferredSize(iconSize);
    iconBadge.setMinimumSize(iconSize);
    iconBadge.setMaximumSize(iconSize);
    card.add(iconBadge, BorderLayout.WEST);

    JPanel text = new JPanel();
    text.setOpaque(false);
    text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

    JLabel titleLabel = label(title, walletCard ? 10 : 9, Font.BOLD, walletCard ? PALE : new Color(210, 214, 220));

    titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    value.setAlignmentX(Component.LEFT_ALIGNMENT);
    hintLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    text.add(Box.createVerticalGlue());
    text.add(titleLabel);
    text.add(Box.createVerticalStrut(2));
    text.add(value);
    text.add(Box.createVerticalStrut(2));
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
    JPanel filters = new JPanel(new GridBagLayout());
    filters.setOpaque(false);

    searchField = new SearchBox("Search invoices...");
    searchField.addKeyListener(new KeyAdapter() {
        @Override
        public void keyReleased(KeyEvent e) {
            applyFilters();
        }
    });

    statusFilter = new DarkComboButton(
            null,
            new String[]{"Status: All", "Paid", "Pending", "Overdue"}
    );
    paymentFilter = new DarkComboButton(
            null,
            new String[]{"Payment Method: All", "Card", "Cash", "Bank Transfer"}
    );
    dateFilter = new DarkComboButton(
            "PERIOD",
            new String[]{
                    "Period: This Year",
                    "Today",
                    "This Week",
                    "Last 30 Days",
                    "This Month",
                    "This Year"
            }
    );

    statusFilter.setOnChange(e -> applyFilters());
    paymentFilter.setOnChange(e -> applyFilters());
    dateFilter.setOnChange(e -> applyFilters());

    JButton export = new OutlineButton("Export", "EXPORT");
    export.addActionListener(e -> exportInvoices());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weighty = 1.0;

    gbc.gridx = 0;
    gbc.weightx = 1.65;
    gbc.insets = new Insets(0, 0, 0, 10);
    filters.add(searchField, gbc);

    gbc.gridx = 1;
    gbc.weightx = 0.95;
    gbc.insets = new Insets(0, 0, 0, 10);
    filters.add(statusFilter, gbc);

    gbc.gridx = 2;
    gbc.weightx = 1.20;
    gbc.insets = new Insets(0, 0, 0, 10);
    filters.add(paymentFilter, gbc);

    gbc.gridx = 3;
    gbc.weightx = 1.10;
    gbc.insets = new Insets(0, 0, 0, 10);
    filters.add(dateFilter, gbc);

    gbc.gridx = 4;
    gbc.weightx = 0.80;
    gbc.insets = new Insets(0, 0, 0, 0);
    filters.add(export, gbc);

    searchField.setPreferredSize(new Dimension(270, 42));
    statusFilter.setPreferredSize(new Dimension(150, 42));
    paymentFilter.setPreferredSize(new Dimension(185, 42));
    dateFilter.setPreferredSize(new Dimension(175, 42));
    export.setPreferredSize(new Dimension(120, 42));

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
    RoundedPanel footer = new RoundedPanel(15, new Color(6, 12, 18, 242));
    footer.setLayout(new GridLayout(1, 5, 0, 0));
    footer.setPreferredSize(new Dimension(900, 92));
    footer.setMinimumSize(new Dimension(900, 92));
    footer.setBorder(new EmptyBorder(8, 10, 8, 10));

    footer.add(new FooterBrand());
    footer.add(new FooterItem("PREMIUM FLEET", "Latest BMW models", "CAR"));
    footer.add(new FooterItem("TRUSTED SERVICE", "Excellence in every step", "SHIELD"));
    footer.add(new FooterItem("BEST PRICES", "Luxury within reach", "STAR"));
    footer.add(new FooterItem("24/7 SUPPORT", "We are here for you", "HEADSET"));

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

    table.getColumnModel().getColumn(0).setPreferredWidth(105);
    table.getColumnModel().getColumn(1).setPreferredWidth(145);
    table.getColumnModel().getColumn(2).setPreferredWidth(170);
    table.getColumnModel().getColumn(3).setPreferredWidth(72);
    table.getColumnModel().getColumn(4).setPreferredWidth(110);
    table.getColumnModel().getColumn(5).setPreferredWidth(95);
    table.getColumnModel().getColumn(6).setPreferredWidth(85);
    table.getColumnModel().getColumn(7).setPreferredWidth(115);
    table.getColumnModel().getColumn(8).setPreferredWidth(105);
    table.getColumnModel().getColumn(9).setPreferredWidth(120);
}

private void refreshAll() {
    refreshMetrics();
    applyFilters();
}

private void refreshMetrics() {
    double totalRevenue = accountState.getTotalSpent();
    double walletBalance = accountState.getWalletBalance();
    long paid = accountState.getPaidInvoices();
    long pending = accountState.getPendingPayments();
    double lateFees = accountState.getLateFees();

    walletBalanceValue.setText(formatMoney(walletBalance));
    totalRevenueValue.setText(formatMoney(totalRevenue));
    paidInvoicesValue.setText(String.valueOf(paid));
    pendingPaymentsValue.setText(String.valueOf(pending));
    lateFeesValue.setText(formatMoney(lateFees));
    walletBalanceHint.setText("Linked wallet balance");
    totalSpentHint.setText(formatMoney(accountState.getOutstandingBalance()) + " outstanding");
    paidInvoicesHint.setText(formatMoney(totalRevenue) + " paid");
    pendingPaymentsHint.setText(formatMoney(accountState.getOutstandingBalance()) + " pending");
    lateFeesHint.setText(accountState.getLateReturns() + " invoices overdue");
}

private void applyFilters() {
    String query = searchField == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
    String status = statusFilter == null ? "Status: All" : statusFilter.getSelectedValue();
    String method = paymentFilter == null ? "Payment Method: All" : paymentFilter.getSelectedValue();

    filteredInvoices.clear();

    for (CustomerAccountState.CustomerInvoice invoice : accountState.getInvoices()) {
        boolean matchesSearch = query.isBlank()
                || invoice.invoiceId.toLowerCase(Locale.ROOT).contains(query)
                || invoice.customerName.toLowerCase(Locale.ROOT).contains(query)
                || invoice.vehicleName.toLowerCase(Locale.ROOT).contains(query);

        boolean matchesStatus = "Status: All".equals(status) || invoice.status.equals(status);
        boolean matchesMethod = "Payment Method: All".equals(method) || invoice.paymentMethod.equals(method);

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

    List<CustomerAccountState.CustomerInvoice> pageData = new ArrayList<>(filteredInvoices.subList(fromIndex, toIndex));
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

private void exportInvoices() {
    try {
        Path folder = Path.of(System.getProperty("user.dir"), "generated-invoices");
        Files.createDirectories(folder);
        Path file = folder.resolve("velora-customer-billing-export.txt");

        StringBuilder content = new StringBuilder();
        content.append("VELORA MOTORS - CUSTOMER BILLING EXPORT\n");
        content.append("================================\n\n");

        for (CustomerAccountState.CustomerInvoice invoice : filteredInvoices) {
            content.append(invoice.invoiceId).append(" | ")
                    .append(invoice.customerName).append(" | ")
                    .append(invoice.vehicleName).append(" | ")
                    .append(invoice.rentalDays).append(" days | ")
                    .append(formatMoney(invoice.baseAmount)).append(" | ")
                    .append(formatMoney(invoice.lateFee)).append(" | ")
                    .append(formatMoney(invoice.tax)).append(" | ")
                    .append(formatMoney(invoice.totalAmount())).append(" | ")
                    .append(invoice.status).append("\n");
        }

        Files.writeString(file, content.toString(), StandardCharsets.UTF_8);

        VeloraNotificationDialog.showSuccess(
                this,
                "Export Successful",
                "Billing export created successfully:\n" + file.toAbsolutePath()
        );
    } catch (Exception ex) {
        VeloraNotificationDialog.showError(
                this,
                "Export Failed",
                "Unable to export billing data: " + ex.getMessage()
        );
    }
}

private void addDemoInvoice() {
    JTextField customerField = new JTextField("New Customer");
    JTextField vehicleField = new JTextField(vehicleService.getAllVehicles().isEmpty()
            ? "Velora Vehicle"
            : FleetUiData.displayName(vehicleService.getAllVehicles().get(0)));
    JTextField daysField = new JTextField("4");
    JTextField baseField = new JTextField("1200");
    JTextField lateFeeField = new JTextField("0");

    DarkComboButton statusBox = new DarkComboButton(
            null,
            new String[]{"Pending", "Paid", "Overdue"}
    );
    DarkComboButton methodBox = new DarkComboButton(
            null,
            new String[]{"Card", "Cash", "Bank Transfer"}
    );

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
        String customerName = customerField.getText().trim();
        String vehicle = vehicleField.getText().trim();
        int days = Integer.parseInt(daysField.getText().trim());
        double base = Double.parseDouble(baseField.getText().trim());
        double late = Double.parseDouble(lateFeeField.getText().trim());

        if (customerName.isBlank() || vehicle.isBlank()) {
            throw new IllegalArgumentException("Customer and vehicle are required.");
        }
        if (days <= 0 || base < 0 || late < 0) {
            throw new IllegalArgumentException("Days must be positive, and amounts cannot be negative.");
        }

        CustomerAccountState.CustomerInvoice invoice = CustomerAccountState.createInvoice(
                this.customer,
                accountState.getInvoices().size(),
                vehicle,
                days,
                base,
                late,
                statusBox.getSelectedValue(),
                methodBox.getSelectedValue()
        );

        accountState.addInvoice(invoice);

        int lastPage = Math.max(1, (int) Math.ceil(filteredInvoices.size() / (double) PAGE_SIZE));
        currentPage = lastPage;
        refreshPage();

        VeloraNotificationDialog.showSuccess(
                this,
                "Invoice Generated",
                "Invoice " + invoice.invoiceId + " has been generated successfully."
        );
    } catch (NumberFormatException ex) {
        VeloraNotificationDialog.showError(
                this,
                "Invalid Values",
                "Days and amounts must be valid numbers."
        );
    } catch (IllegalArgumentException ex) {
        VeloraNotificationDialog.showError(
                this,
                "Invalid Invoice Data",
                ex.getMessage()
        );
    }
}

private void recordPayment(CustomerAccountState.CustomerInvoice invoice) {
    if (invoice == null) {
        return;
    }

    if ("Paid".equals(invoice.status)) {
        VeloraNotificationDialog.showInfo(
                this,
                "Invoice Already Paid",
                "This invoice has already been paid."
        );
        return;
    }

    if (!accountState.canAfford(invoice.totalAmount())) {
        VeloraNotificationDialog.showError(
                this,
                "Insufficient Balance",
                "Your available balance is " + formatMoney(accountState.getWalletBalance())
                        + ".\nRequired amount: " + formatMoney(invoice.totalAmount())
        );
        return;
    }

    boolean confirmed = VeloraNotificationDialog.showConfirm(
            this,
            "Confirm Payment",
            "Pay invoice " + invoice.invoiceId + "?\nTotal: " + formatMoney(invoice.totalAmount())
                    + "\nCurrent balance: " + formatMoney(accountState.getWalletBalance()),
            "Pay Now"
    );

    if (!confirmed) {
        return;
    }

    if (!accountState.recordPayment(invoice, "Card")) {
        VeloraNotificationDialog.showError(
                this,
                "Payment Failed",
                "The payment could not be completed because the wallet balance changed."
        );
        return;
    }

    int pageBeforeRefresh = currentPage;
    refreshMetrics();
    applyFilters();
    currentPage = Math.min(pageBeforeRefresh, totalPages);
    refreshPage();

    VeloraNotificationDialog.showSuccess(
            this,
            "Payment Successful",
            "Payment completed successfully for " + invoice.invoiceId
                    + ".\nRemaining balance: " + formatMoney(accountState.getWalletBalance())
    );
}

private void downloadInvoice(CustomerAccountState.CustomerInvoice invoice) {
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
                + "Tax (10%): " + formatMoney(invoice.tax) + "\n"
                + "Total Amount: " + formatMoney(invoice.totalAmount()) + "\n\n"
                + "Status: " + invoice.status + "\n"
                + "Payment Method: " + invoice.paymentMethod + "\n";

        Files.writeString(file, content, StandardCharsets.UTF_8);

        VeloraNotificationDialog.showSuccess(
                this,
                "Invoice Downloaded",
                "Invoice file created successfully:\n" + file.toAbsolutePath()
        );
    } catch (Exception ex) {
        VeloraNotificationDialog.showError(
                this,
                "Download Failed",
                "Unable to download invoice: " + ex.getMessage()
        );
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

private final class BillingTableModel extends AbstractTableModel {

    private final String[] columns = {
            "Invoice ID", "Customer", "Vehicle", "Days", "Base", "Late Fee", "Tax", "Total", "Status", "Actions"
    };

    private final List<CustomerAccountState.CustomerInvoice> rows = new ArrayList<>();

    void setRows(List<CustomerAccountState.CustomerInvoice> invoices) {
        rows.clear();
        rows.addAll(invoices);
        fireTableDataChanged();
    }

    CustomerAccountState.CustomerInvoice getInvoiceAt(int row) {
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
        CustomerAccountState.CustomerInvoice invoice = rows.get(rowIndex);
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
            default -> "◉    ⋮";
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
        setOpaque(false);
        setHorizontalAlignment(SwingConstants.CENTER);
        setBorder(new EmptyBorder(0, 9, 0, 9));
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
        if (column == 8) {
            return new StatusPill(String.valueOf(value), selected);
        }

        if (column == 9) {
            return new ActionIconsCell(selected);
        }

        JLabel label = (JLabel) super.getTableCellRendererComponent(
                table, value, selected, focus, row, column
        );

        label.setOpaque(true);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(new EmptyBorder(0, 9, 0, 9));
        label.setFont(new Font("Segoe UI", column == 0 || column == 7 ? Font.BOLD : Font.PLAIN, 12));
        label.setBackground(selected ? new Color(72, 45, 19) : new Color(3, 9, 14));
        label.setForeground(TEXT);

        if (column == 0) {
            label.setForeground(PALE);
        }

        if (column == 5 && String.valueOf(value).contains("$0.00")) {
            label.setForeground(new Color(211, 216, 223));
        } else if (column == 5) {
            label.setForeground(RED);
        }

        return label;
    }
}

private static final class StatusPill extends JComponent {

    private final String status;
    private final boolean selected;

    StatusPill(String status, boolean selected) {
        this.status = status;
        this.selected = selected;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics raw) {
        Graphics2D g = (Graphics2D) raw.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (selected) {
            g.setColor(new Color(72, 45, 19));
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        Color c;
        switch (status) {
            case "Paid" -> c = GREEN;
            case "Pending" -> c = GOLD;
            default -> c = RED;
        }

        g.setFont(new Font("Segoe UI", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        int pillW = Math.max(58, fm.stringWidth(status) + 24);
        int pillH = 25;
        int x = (getWidth() - pillW) / 2;
        int y = (getHeight() - pillH) / 2;

        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 34));
        g.fillRoundRect(x, y, pillW, pillH, 9, 9);

        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 115));
        g.drawRoundRect(x, y, pillW - 1, pillH - 1, 9, 9);

        g.setColor(c);
        g.drawString(
                status,
                x + (pillW - fm.stringWidth(status)) / 2,
                y + (pillH + fm.getAscent()) / 2 - 3
        );

        g.dispose();
    }
}

private static final class ActionIconsCell extends JComponent {

    private final boolean selected;

    ActionIconsCell(boolean selected) {
        this.selected = selected;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics raw) {
        Graphics2D g = (Graphics2D) raw.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (selected) {
            g.setColor(new Color(72, 45, 19));
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        int cy = getHeight() / 2;
        int eyeX = getWidth() / 2 - 18;
        int menuX = getWidth() / 2 + 24;

        g.setColor(PALE);
        g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Eye icon
        g.drawOval(eyeX - 9, cy - 6, 18, 12);
        g.fillOval(eyeX - 2, cy - 2, 4, 4);

        // Three-dot menu
        g.fillOval(menuX - 1, cy - 8, 3, 3);
        g.fillOval(menuX - 1, cy - 1, 3, 3);
        g.fillOval(menuX - 1, cy + 6, 3, 3);

        g.dispose();
    }
}

private final class InvoiceSummaryPanel extends RoundedPanel {

    private CustomerAccountState.CustomerInvoice invoice;
    private final JButton recordPaymentButton = new SummaryButton("Record Payment", true, "PAY");
    private final JButton downloadButton = new SummaryButton("Download Invoice", false, "DOWNLOAD");

    InvoiceSummaryPanel() {
        super(16, new Color(5, 11, 17, 244));
        setLayout(null);
        setCursor(Cursor.getDefaultCursor());

        recordPaymentButton.addActionListener(e -> recordPayment(invoice));
        downloadButton.addActionListener(e -> downloadInvoice(invoice));

        add(recordPaymentButton);
        add(downloadButton);
    }

    void setInvoice(CustomerAccountState.CustomerInvoice invoice) {
        this.invoice = invoice;

        boolean hasInvoice = invoice != null;
        recordPaymentButton.setVisible(hasInvoice);
        downloadButton.setVisible(hasInvoice);

        if (hasInvoice) {
            boolean isPaid = "Paid".equals(invoice.status);

            // Customer logic:
            // Paid invoice -> show a disabled "Paid" button.
            // Pending/Overdue invoice -> show an active "Pay Now" button.
            recordPaymentButton.setText(isPaid ? "Paid" : "Pay Now");
            recordPaymentButton.setEnabled(!isPaid);

            downloadButton.setEnabled(true);
        }

        repaint();
    }

    @Override
    public void doLayout() {
        int buttonW = Math.max(120, getWidth() - 36);
        int bottom = getHeight() - 18;

        downloadButton.setBounds(18, bottom - 40, buttonW, 40);
        recordPaymentButton.setBounds(18, bottom - 94, buttonW, 44);
    }

    @Override
    protected void paintComponent(Graphics raw) {
        super.paintComponent(raw);

        Graphics2D g = (Graphics2D) raw.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int y = 28;

        // Compact gold title, like the original design.
        g.setColor(PALE);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawRoundRect(18, 17, 15, 17, 3, 3);
        g.drawLine(22, 22, 29, 22);
        g.drawLine(22, 27, 29, 27);
        g.drawLine(22, 32, 27, 32);

        g.setPaint(new GradientPaint(
                42, 12, new Color(255, 226, 164),
                210, 36, GOLD
        ));
        g.setFont(new Font("Segoe UI", Font.BOLD, 17));
        g.drawString("Invoice Summary", 43, y);

        if (invoice == null) {
            g.setColor(MUTED);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g.drawString("Select an invoice from the table.", 18, y + 42);
            g.dispose();
            return;
        }

        drawBadge(g, w - 82, 15, invoice.status);

        y += 34;
        divider(g, y, w);
        y += 25;

        g.setColor(MUTED);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g.drawString("Invoice ID", 18, y);

        g.setColor(TEXT);
        g.setFont(new Font("Segoe UI", Font.BOLD, 21));
        g.drawString(invoice.invoiceId, 18, y + 27);

        y += 64;
        drawInfoLine(g, "Customer", invoice.customerName, 18, y);
        y += 48;
        drawInfoLine(g, "Vehicle", invoice.vehicleName, 18, y);
        y += 48;
        drawInfoLine(g, "Rental Period", invoice.startDate + "  →  " + invoice.endDate, 18, y);
        y += 48;
        drawInfoLine(g, "Rental Days", invoice.rentalDays + " days", 18, y);

        y += 39;
        divider(g, y, w);
        y += 28;

        drawMoneyRow(g, "Base Rental", invoice.baseAmount, y, false);
        y += 27;
        drawMoneyRow(g, "Late Fee", invoice.lateFee, y, invoice.lateFee > 0);
        y += 27;
        drawMoneyRow(g, "Tax (10%)", invoice.tax, y, false);

        int totalY = Math.min(y + 56, Math.max(y + 38, getHeight() - 150));
        divider(g, totalY - 32, w);

        g.setColor(TEXT);
        g.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g.drawString("Total Amount", 18, totalY);

        g.setPaint(new GradientPaint(
                w - 155, totalY - 20, new Color(255, 228, 171),
                w - 18, totalY, GOLD
        ));
        g.setFont(new Font("Segoe UI", Font.BOLD, 25));
        String total = formatMoney(invoice.totalAmount());
        g.drawString(total, w - g.getFontMetrics().stringWidth(total) - 18, totalY);

        g.dispose();
    }

    private void drawInfoLine(Graphics2D g, String title, String value, int x, int y) {
        g.setColor(PALE);
        g.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g.drawString(title, x, y);

        g.setColor(TEXT);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        drawTrimmed(g, value, x, y + 19, getWidth() - x - 22);
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
        g.drawString(title, 18, y);

        String money = formatMoney(value);
        g.setColor(red ? RED : TEXT);
        g.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g.drawString(money, getWidth() - g.getFontMetrics().stringWidth(money) - 18, y);
    }

    private void divider(Graphics2D g, int y, int w) {
        g.setPaint(new GradientPaint(
                18, y, new Color(214, 160, 66, 110),
                w - 18, y, new Color(214, 160, 66, 18)
        ));
        g.drawLine(18, y, w - 18, y);
    }

    private void drawBadge(Graphics2D g, int x, int y, String status) {
        Color c = switch (status) {
            case "Paid" -> GREEN;
            case "Pending" -> GOLD;
            default -> RED;
        };

        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 26));
        g.fillRoundRect(x, y, 64, 24, 9, 9);

        g.setColor(c);
        g.drawRoundRect(x, y, 64, 24, 9, 9);

        g.setFont(new Font("Segoe UI", Font.BOLD, 10));
        int tw = g.getFontMetrics().stringWidth(status);
        g.drawString(status, x + (64 - tw) / 2, y + 16);
    }
}

private static final class SummaryButton extends JButton {

    private final boolean filled;
    private final String iconType;

    SummaryButton(String text, boolean filled, String iconType) {
        super(text);
        this.filled = filled;
        this.iconType = iconType;

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

        boolean hover = getModel().isRollover();

        if (filled) {
            g.setPaint(new GradientPaint(
                    0, 0, hover ? new Color(255, 227, 174) : new Color(241, 200, 132),
                    getWidth(), getHeight(),
                    hover ? new Color(205, 141, 52) : new Color(181, 117, 34)
            ));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g.setColor(new Color(31, 20, 8));
        } else {
            g.setColor(hover ? new Color(11, 20, 29) : new Color(3, 9, 15));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

            g.setColor(new Color(214, 160, 66, hover ? 170 : 120));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

            g.setColor(PALE);
        }

        g.setFont(getFont());
        FontMetrics fm = g.getFontMetrics();

        int iconSpace = 22;
        int totalW = fm.stringWidth(getText()) + iconSpace;
        int startX = (getWidth() - totalW) / 2;

        if ("PAY".equals(iconType)) {
            drawPaymentIcon(g, startX + 8, getHeight() / 2);
        } else if ("DOWNLOAD".equals(iconType)) {
            drawDownloadIcon(g, startX + 8, getHeight() / 2);
        }

        g.drawString(
                getText(),
                startX + iconSpace,
                (getHeight() + fm.getAscent()) / 2 - 3
        );

        g.dispose();
    }

    private void drawPaymentIcon(Graphics2D g, int cx, int cy) {
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawRoundRect(cx - 7, cy - 5, 14, 10, 3, 3);
        g.drawLine(cx - 4, cy - 1, cx + 4, cy - 1);
        g.drawLine(cx - 4, cy + 2, cx, cy + 2);
    }

    private void drawDownloadIcon(Graphics2D g, int cx, int cy) {
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx, cy - 7, cx, cy + 2);
        g.drawLine(cx - 4, cy - 1, cx, cy + 3);
        g.drawLine(cx, cy + 3, cx + 4, cy - 1);
        g.drawLine(cx - 7, cy + 7, cx + 7, cy + 7);
    }
}

private static final class DarkComboButton extends JButton {

    private final String[] items;
    private final String iconType;
    private String selected;
    private java.awt.event.ActionListener listener;

    DarkComboButton(String iconType, String[] items) {
        super(items != null && items.length > 0 ? items[0] : "");

        this.iconType = iconType;
        this.items = items == null ? new String[0] : items.clone();
        this.selected = getText();

        setPreferredSize(new Dimension(140, 42));
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

        g.setColor(new Color(214, 160, 66, getModel().isRollover() ? 155 : 105));
        g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 9, 9);

        int textX = 15;
        if ("PERIOD".equals(iconType)) {
            drawPeriodIcon(g, 18, getHeight() / 2);
            textX = 34;
        }

        g.setColor(TEXT);
        g.setFont(getFont());
        FontMetrics fm = g.getFontMetrics();
        g.drawString(getText(), textX, (getHeight() + fm.getAscent()) / 2 - 3);

        int cx = getWidth() - 18;
        int cy = getHeight() / 2;
        g.setColor(PALE);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx - 4, cy - 2, cx, cy + 3);
        g.drawLine(cx, cy + 3, cx + 4, cy - 2);

        g.dispose();
    }

    private void drawPeriodIcon(Graphics2D g, int cx, int cy) {
        g.setColor(PALE);
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        g.drawRoundRect(cx - 8, cy - 7, 16, 14, 4, 4);
        g.drawLine(cx - 8, cy - 2, cx + 8, cy - 2);
        g.drawLine(cx - 4, cy - 9, cx - 4, cy - 5);
        g.drawLine(cx + 4, cy - 9, cx + 4, cy - 5);
        g.fillOval(cx - 4, cy + 1, 3, 3);
        g.fillOval(cx + 2, cy + 1, 3, 3);
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

private static final class MetricIconBadge extends JComponent {

    private final String icon;
    private final Color color;

    MetricIconBadge(String icon, Color color) {
        this.icon = icon;
        this.color = color;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics raw) {
        Graphics2D g = (Graphics2D) raw.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int s = Math.min(getWidth(), getHeight()) - 2;
        int x = (getWidth() - s) / 2;
        int y = (getHeight() - s) / 2;

        Color accent = "WARN".equals(icon) ? RED : GOLD;

        g.setPaint(new GradientPaint(
                x, y,
                new Color(126, 86, 26),
                x + s, y + s,
                new Color(33, 24, 14)
        ));
        g.fillRoundRect(x, y, s, s, 10, 10);

        g.setPaint(new GradientPaint(
                x, y,
                new Color(255, 240, 188, 175),
                x + s, y + s / 2,
                new Color(255, 204, 96, 0)
        ));
        g.fillRoundRect(x + 1, y + 1, s - 2, Math.max(12, s / 2), 9, 9);

        g.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 230));
        g.drawRoundRect(x, y, s - 1, s - 1, 10, 10);

        // Inner glow
        g.setColor(new Color(255, 228, 160, 75));
        g.drawRoundRect(x + 2, y + 2, s - 5, s - 5, 8, 8);

        g.setColor("WARN".equals(icon) ? new Color(255, 92, 95) : new Color(255, 211, 112));
        g.setStroke(new BasicStroke(2.25f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int cx = getWidth() / 2;
        int cy = getHeight() / 2;

        switch (icon) {
            case "MONEY" -> drawMoneyStack(g, cx, cy);
            case "DOC" -> drawInvoiceCheck(g, cx, cy);
            case "CLOCK" -> drawDocumentClock(g, cx, cy);
            case "WARN" -> drawWarning(g, cx, cy);
            default -> drawIcon(g, icon, cx, cy, 22);
        }

        g.dispose();
    }

    private void drawMoneyStack(Graphics2D g, int cx, int cy) {
        g.drawOval(cx - 10, cy - 10, 20, 6);
        g.drawArc(cx - 10, cy - 7, 20, 6, 180, 180);
        g.drawArc(cx - 10, cy - 3, 20, 6, 180, 180);
        g.drawLine(cx - 10, cy - 7, cx - 10, cy + 5);
        g.drawLine(cx + 10, cy - 7, cx + 10, cy + 5);

        g.drawOval(cx + 1, cy + 1, 13, 13);
        g.setFont(new Font("Segoe UI", Font.BOLD, 9));
        g.drawString("$", cx + 5, cy + 11);
    }

    private void drawInvoiceCheck(Graphics2D g, int cx, int cy) {
        g.drawRoundRect(cx - 10, cy - 11, 18, 22, 3, 3);
        g.drawLine(cx - 6, cy - 5, cx + 3, cy - 5);
        g.drawLine(cx - 6, cy, cx + 2, cy);

        g.drawOval(cx + 1, cy + 2, 13, 13);
        g.drawLine(cx + 4, cy + 8, cx + 7, cy + 11);
        g.drawLine(cx + 7, cy + 11, cx + 11, cy + 6);
    }

    private void drawDocumentClock(Graphics2D g, int cx, int cy) {
        Path2D doc = new Path2D.Double();
        doc.moveTo(cx - 10, cy - 11);
        doc.lineTo(cx + 3, cy - 11);
        doc.lineTo(cx + 9, cy - 5);
        doc.lineTo(cx + 9, cy + 9);
        doc.lineTo(cx - 10, cy + 9);
        doc.closePath();
        g.draw(doc);

        g.drawLine(cx + 3, cy - 11, cx + 3, cy - 5);
        g.drawLine(cx + 3, cy - 5, cx + 9, cy - 5);
        g.drawLine(cx - 6, cy - 3, cx + 1, cy - 3);

        g.drawOval(cx + 1, cy + 1, 13, 13);
        g.drawLine(cx + 7, cy + 7, cx + 7, cy + 3);
        g.drawLine(cx + 7, cy + 7, cx + 11, cy + 9);
    }

    private void drawWarning(Graphics2D g, int cx, int cy) {
        Path2D p = new Path2D.Double();
        p.moveTo(cx, cy - 12);
        p.lineTo(cx + 12, cy + 10);
        p.lineTo(cx - 12, cy + 10);
        p.closePath();
        g.draw(p);

        g.drawLine(cx, cy - 4, cx, cy + 3);
        g.fillOval(cx - 1, cy + 6, 3, 3);
    }
}

private static final class FooterBrand extends JComponent {

    @Override
    protected void paintComponent(Graphics raw) {
        Graphics2D g = (Graphics2D) raw.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int cy = getHeight() / 2;

        drawFooterWingMark(g, 42, cy, 58);

        g.setColor(TEXT);
        g.setFont(new Font("Segoe UI", Font.BOLD, 16));
        g.drawString("VELORA MOTORS", 82, cy - 2);

        g.setColor(PALE);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        g.drawString("PREMIUM BMW VEHICLE RENTAL", 84, cy + 15);

        g.dispose();
    }

    private void drawFooterWingMark(Graphics2D g, int cx, int cy, int width) {
        g.setColor(PALE);
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        Path2D p = new Path2D.Double();

        p.moveTo(cx, cy + 10);
        p.lineTo(cx - 8, cy - 10);
        p.lineTo(cx - 26, cy - 10);
        p.moveTo(cx, cy + 10);
        p.lineTo(cx + 8, cy - 10);
        p.lineTo(cx + 26, cy - 10);

        p.moveTo(cx - 8, cy - 4);
        p.lineTo(cx - 23, cy - 4);
        p.moveTo(cx + 8, cy - 4);
        p.lineTo(cx + 23, cy - 4);

        p.moveTo(cx - 5, cy + 2);
        p.lineTo(cx - 18, cy + 2);
        p.moveTo(cx + 5, cy + 2);
        p.lineTo(cx + 18, cy + 2);

        g.draw(p);
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
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int cy = getHeight() / 2;

        // Left separator like the original design.
        g.setColor(new Color(214, 160, 66, 72));
        g.drawLine(0, 12, 0, getHeight() - 12);

        int cx = 30;
        g.setColor(new Color(214, 160, 66, 26));
        g.fillOval(cx - 18, cy - 18, 36, 36);

        g.setColor(new Color(214, 160, 66, 130));
        g.drawOval(cx - 18, cy - 18, 36, 36);

        g.setColor(PALE);
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        drawIcon(g, icon, cx, cy, 18);

        g.setColor(TEXT);
        g.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g.drawString(title, 56, cy - 2);

        g.setColor(MUTED);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g.drawString(subtitle, 56, cy + 15);

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

    private final String iconType;

    OutlineButton(String text) {
        this(text, null);
    }

    OutlineButton(String text, String iconType) {
        super(text);
        this.iconType = iconType;

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
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(getModel().isRollover() ? new Color(10, 18, 26) : new Color(3, 9, 15));
        g.fillRoundRect(0, 0, getWidth(), getHeight(), 9, 9);

        g.setColor(new Color(214, 160, 66, getModel().isRollover() ? 155 : 110));
        g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 9, 9);

        int iconSpace = 0;
        if ("EXPORT".equals(iconType)) {
            iconSpace = 20;
        }

        g.setFont(getFont());
        FontMetrics fm = g.getFontMetrics();
        int totalW = fm.stringWidth(getText()) + iconSpace;
        int startX = (getWidth() - totalW) / 2;

        if ("EXPORT".equals(iconType)) {
            drawExportIcon(g, startX + 7, getHeight() / 2);
        }

        g.setColor(PALE);
        g.drawString(
                getText(),
                startX + iconSpace,
                (getHeight() + fm.getAscent()) / 2 - 3
        );

        g.dispose();
    }

    private void drawExportIcon(Graphics2D g, int cx, int cy) {
        g.setColor(PALE);
        g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        g.drawLine(cx, cy - 8, cx, cy + 3);
        g.drawLine(cx - 4, cy - 1, cx, cy + 3);
        g.drawLine(cx, cy + 3, cx + 4, cy - 1);

        g.drawLine(cx - 7, cy + 7, cx + 7, cy + 7);
        g.drawLine(cx - 7, cy + 7, cx - 7, cy + 3);
        g.drawLine(cx + 7, cy + 7, cx + 7, cy + 3);
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
        case "STAR" -> {
            Path2D p = new Path2D.Double();
            for (int i = 0; i < 10; i++) {
                double angle = -Math.PI / 2 + i * Math.PI / 5;
                double r = (i % 2 == 0) ? h : h * 0.45;
                double px = cx + Math.cos(angle) * r;
                double py = cy + Math.sin(angle) * r;
                if (i == 0) p.moveTo(px, py);
                else p.lineTo(px, py);
            }
            p.closePath();
            g.draw(p);
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
