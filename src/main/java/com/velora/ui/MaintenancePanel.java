package com.velora.ui;

import com.velora.authentication.Customer;
import com.velora.service.VehicleService;
import com.velora.vehicle.Vehicle;
import com.velora.vehicle.VehicleStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

public final class MaintenancePanel extends JPanel {

    private static final Color GOLD = new Color(214, 160, 66);
    private static final Color PALE = new Color(238, 201, 139);
    private static final Color TEXT = new Color(243, 244, 247);
    private static final Color MUTED = new Color(157, 164, 175);
    private static final Color GREEN = new Color(86, 207, 114);
    private static final Color RED = new Color(235, 93, 98);
    private static final Color BLUE = new Color(89, 151, 255);
    private static final Color ORANGE = new Color(245, 164, 75);
    private static final int PAGE_SIZE = 8;
    private static final Path MAINTENANCE_FILE = Path.of(System.getProperty("user.dir"), "data", "maintenance-jobs.tsv");

    private final Customer manager;
    private final VehicleService vehicleService = new VehicleService();
    private final List<ServiceJob> jobs = new ArrayList<>();
    private final List<ServiceJob> filteredJobs = new ArrayList<>();

    private JTextField searchField;
    private DarkComboButton statusFilter;
    private DarkComboButton typeFilter;
    private DarkComboButton priorityFilter;

    private JLabel totalJobsValue;
    private JLabel inServiceValue;
    private JLabel dueSoonValue;
    private JLabel completedValue;

    private MaintenanceTableModel tableModel;
    private JTable table;
    private MaintenanceSummaryPanel summaryPanel;
    private JPanel paginationPanel;
    private JLabel showingLabel;

    private int currentPage = 1;
    private int totalPages = 1;

    public MaintenancePanel(Customer manager) {
        this.manager = manager;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8, 12, 18, 22));
        loadSavedJobs();
        filteredJobs.addAll(jobs);
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
        summaryPanel = new MaintenanceSummaryPanel();
        summaryPanel.setPreferredSize(new Dimension(330, 100));
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
        left.add(label("Maintenance Management", 28, Font.BOLD, TEXT));
        left.add(Box.createVerticalStrut(3));
        left.add(label("Dashboard  ›  Maintenance", 12, Font.PLAIN, MUTED));

        JButton schedule = new GoldActionButton("Schedule Service");
        schedule.setPreferredSize(new Dimension(170, 42));
        schedule.addActionListener(e -> scheduleService());

        titleRow.add(left, BorderLayout.WEST);
        titleRow.add(schedule, BorderLayout.EAST);
        wrapper.add(titleRow);
        wrapper.add(Box.createVerticalStrut(14));

        JPanel cards = new JPanel(new GridLayout(1, 4, 13, 0));
        cards.setOpaque(false);

        totalJobsValue = label("0", 24, Font.BOLD, PALE);
        inServiceValue = label("0", 24, Font.BOLD, TEXT);
        dueSoonValue = label("0", 24, Font.BOLD, PALE);
        completedValue = label("0", 24, Font.BOLD, TEXT);

        cards.add(metricCard("TOTAL SERVICES", totalJobsValue, "All service records", "TOOLS", GOLD));
        cards.add(metricCard("IN SERVICE", inServiceValue, "Vehicles unavailable", "CAR", BLUE));
        cards.add(metricCard("DUE SOON", dueSoonValue, "Need attention", "CLOCK", ORANGE));
        cards.add(metricCard("COMPLETED", completedValue, "Finished services", "CHECK", GREEN));

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
        text.add(Box.createVerticalGlue());
        text.add(label(title, 11, Font.BOLD, new Color(210, 216, 224)));
        text.add(Box.createVerticalStrut(4));
        text.add(value);
        text.add(Box.createVerticalStrut(4));
        text.add(label("+ " + hint, 10, Font.PLAIN, iconColor));
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

        searchField = new SearchBox("Search by service ID, vehicle, or technician...");
        searchField.setPreferredSize(new Dimension(350, 40));
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilters();
            }
        });

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(searchField);

        statusFilter = new DarkComboButton("All Status", "Scheduled", "In Service", "Due Soon", "Completed", "Delayed");
        typeFilter = new DarkComboButton("All Types", "Oil Service", "Battery Check", "Brake Inspection", "Tire Service", "Full Inspection");
        priorityFilter = new DarkComboButton("All Priority", "High", "Medium", "Low");

        statusFilter.setOnChange(e -> applyFilters());
        typeFilter.setOnChange(e -> applyFilters());
        priorityFilter.setOnChange(e -> applyFilters());

        left.add(statusFilter);
        left.add(typeFilter);
        left.add(priorityFilter);

        JButton clear = new OutlineButton("Clear Filters");
        clear.setPreferredSize(new Dimension(125, 40));
        clear.addActionListener(e -> {
            searchField.setText("");
            statusFilter.setSelectedValue("All Status");
            typeFilter.setSelectedValue("All Types");
            priorityFilter.setSelectedValue("All Priority");
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
        tableModel = new MaintenanceTableModel();
        table = new JTable(tableModel);
        configureTable(table);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row >= 0 && row < tableModel.getRowCount()) {
                    summaryPanel.setJob(tableModel.getJobAt(row));
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
        showingLabel = label("Showing 0 to 0 of 0 services", 11, Font.PLAIN, MUTED);
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
        footer.add(new FooterItem("FLEET HEALTH", "Service tracking", "CAR"));
        footer.add(new FooterItem("BATTERY CARE", "Electric units", "BOLT"));
        footer.add(new FooterItem("SAFETY FIRST", "Inspections", "SHIELD"));
        footer.add(new FooterItem("24/7 SUPPORT", "Maintenance team", "HEADSET"));
        return footer;
    }

    private void configureTable(JTable table) {
        table.setRowHeight(50);
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
        header.setPreferredSize(new Dimension(100, 42));
        header.setDefaultRenderer(new MaintenanceHeaderRenderer());

        DefaultTableCellRenderer renderer = new MaintenanceCellRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    private void refreshAll() {
        refreshMetrics();
        applyFilters();
    }

    public void refreshData() {
        jobs.clear();
        loadSavedJobs();
        filteredJobs.clear();
        filteredJobs.addAll(jobs);
        refreshAll();
    }

    private void refreshMetrics() {
        totalJobsValue.setText(String.valueOf(jobs.size()));
        long inService = jobs.stream().filter(j -> j.status.equals("In Service")).count();
        long dueSoon = jobs.stream().filter(j -> j.status.equals("Due Soon") || j.status.equals("Delayed")).count();
        long completed = jobs.stream().filter(j -> j.status.equals("Completed")).count();
        inServiceValue.setText(String.valueOf(inService));
        dueSoonValue.setText(String.valueOf(dueSoon));
        completedValue.setText(String.valueOf(completed));
    }

    private void applyFilters() {
        String query = searchField == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        String status = statusFilter == null ? "All Status" : statusFilter.getSelectedValue();
        String type = typeFilter == null ? "All Types" : typeFilter.getSelectedValue();
        String priority = priorityFilter == null ? "All Priority" : priorityFilter.getSelectedValue();

        filteredJobs.clear();
        for (ServiceJob job : jobs) {
            boolean matchesSearch = query.isBlank()
                    || job.serviceId.toLowerCase(Locale.ROOT).contains(query)
                    || job.vehicle.toLowerCase(Locale.ROOT).contains(query)
                    || job.technician.toLowerCase(Locale.ROOT).contains(query);
            boolean matchesStatus = "All Status".equals(status) || job.status.equals(status);
            boolean matchesType = "All Types".equals(type) || job.serviceType.equals(type);
            boolean matchesPriority = "All Priority".equals(priority) || job.priority.equals(priority);
            if (matchesSearch && matchesStatus && matchesType && matchesPriority) {
                filteredJobs.add(job);
            }
        }
        currentPage = 1;
        refreshPage();
    }

    private void refreshPage() {
        totalPages = Math.max(1, (int) Math.ceil(filteredJobs.size() / (double) PAGE_SIZE));
        currentPage = Math.max(1, Math.min(currentPage, totalPages));
        int fromIndex = Math.min((currentPage - 1) * PAGE_SIZE, filteredJobs.size());
        int toIndex = Math.min(fromIndex + PAGE_SIZE, filteredJobs.size());
        List<ServiceJob> pageData = new ArrayList<>(filteredJobs.subList(fromIndex, toIndex));
        tableModel.setRows(pageData);
        if (!pageData.isEmpty()) {
            table.setRowSelectionInterval(0, 0);
            summaryPanel.setJob(pageData.get(0));
        } else {
            summaryPanel.setJob(null);
        }
        refreshPagination();
    }

    private void refreshPagination() {
        paginationPanel.removeAll();
        paginationPanel.add(new SmallPageButton("‹", false, () -> { currentPage--; refreshPage(); }));
        for (int i = 1; i <= totalPages; i++) {
            final int pageNumber = i;
            paginationPanel.add(new SmallPageButton(String.valueOf(pageNumber), pageNumber == currentPage, () -> {
                currentPage = pageNumber;
                refreshPage();
            }));
        }
        paginationPanel.add(new SmallPageButton("›", false, () -> { currentPage++; refreshPage(); }));
        int from = filteredJobs.isEmpty() ? 0 : (currentPage - 1) * PAGE_SIZE + 1;
        int to = Math.min(filteredJobs.size(), currentPage * PAGE_SIZE);
        showingLabel.setText("Showing " + from + " to " + to + " of " + filteredJobs.size() + " services");
        paginationPanel.revalidate();
        paginationPanel.repaint();
    }

    private void scheduleService() {
        List<Vehicle> fleet = vehicleService.getAllVehicles();

        if (fleet.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No vehicles are available in the fleet.",
                    "Velora Maintenance",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        JComboBox<String> vehicleBox = new JComboBox<>(
                fleet.stream()
                        .map(FleetUiData::displayName)
                        .toArray(String[]::new)
        );

        JTextField technicianField = new JTextField();
        JTextField costField = new JTextField("250");
        JTextField notesField = new JTextField("General service check");
        DarkComboButton typeBox = new DarkComboButton("Oil Service", "Battery Check", "Brake Inspection", "Tire Service", "Full Inspection");
        DarkComboButton priorityBox = new DarkComboButton("Medium", "High", "Low");
        DarkComboButton statusBox = new DarkComboButton("Scheduled", "In Service", "Due Soon", "Completed", "Delayed");

        JPanel form = new JPanel(new GridLayout(7, 2, 8, 8));
        form.setBorder(new EmptyBorder(12, 12, 12, 12));
        form.add(new JLabel("Vehicle")); form.add(vehicleBox);
        form.add(new JLabel("Service Type")); form.add(typeBox);
        form.add(new JLabel("Technician")); form.add(technicianField);
        form.add(new JLabel("Priority")); form.add(priorityBox);
        form.add(new JLabel("Status")); form.add(statusBox);
        form.add(new JLabel("Estimated Cost")); form.add(costField);
        form.add(new JLabel("Notes")); form.add(notesField);

        int choice = JOptionPane.showConfirmDialog(this, form, "Schedule Service", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return;

        try {
            String vehicle = String.valueOf(vehicleBox.getSelectedItem()).trim();
            String technician = technicianField.getText().trim();
            String notes = notesField.getText().trim();
            double cost = Double.parseDouble(costField.getText().trim());
            if (vehicle.isBlank() || technician.isBlank()) throw new IllegalArgumentException("Vehicle and technician are required.");
            if (cost < 0) throw new IllegalArgumentException("Cost cannot be negative.");

            int next = jobs.stream()
                    .map(job -> job.serviceId)
                    .filter(id -> id != null && id.startsWith("SRV-"))
                    .map(id -> id.substring(4))
                    .mapToInt(value -> {
                        try {
                            return Integer.parseInt(value);
                        } catch (NumberFormatException ex) {
                            return 1000;
                        }
                    })
                    .max()
                    .orElse(1000) + 1;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a", Locale.ENGLISH);
            ServiceJob job = new ServiceJob(
                    "SRV-" + String.format("%04d", next),
                    vehicle,
                    typeBox.getSelectedValue(),
                    technician,
                    LocalDateTime.now().plusDays(1).format(formatter),
                    priorityBox.getSelectedValue(),
                    statusBox.getSelectedValue(),
                    cost,
                    notes
            );
            jobs.add(job);
            applyVehicleStatusForJob(job);
            saveJobs();
            refreshAll();
            JOptionPane.showMessageDialog(this, "Service " + job.serviceId + " has been scheduled.", "Velora Maintenance", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cost must be a valid number.", "Velora Maintenance", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Velora Maintenance", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startSelectedJob(ServiceJob job) {
        if (job == null) return;
        if (job.status.equals("Completed")) {
            JOptionPane.showMessageDialog(this, "This service is already completed.", "Velora Maintenance", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        job.status = "In Service";
        updateVehicleStatus(job.vehicle, VehicleStatus.MAINTENANCE);
        saveJobs();
        refreshMetrics();
        refreshPage();
        JOptionPane.showMessageDialog(this, job.serviceId + " is now In Service.", "Velora Maintenance", JOptionPane.INFORMATION_MESSAGE);
    }

    private void completeSelectedJob(ServiceJob job) {
        if (job == null) return;
        job.status = "Completed";
        updateVehicleStatus(job.vehicle, VehicleStatus.AVAILABLE);
        saveJobs();
        refreshMetrics();
        refreshPage();
        JOptionPane.showMessageDialog(this, job.serviceId + " has been completed.", "Velora Maintenance", JOptionPane.INFORMATION_MESSAGE);
    }

    private void applyVehicleStatusForJob(ServiceJob job) {
        if (job == null) {
            return;
        }
        if ("Completed".equals(job.status)) {
            updateVehicleStatus(job.vehicle, VehicleStatus.AVAILABLE);
        } else if ("In Service".equals(job.status) || "Delayed".equals(job.status) || "Due Soon".equals(job.status)) {
            updateVehicleStatus(job.vehicle, VehicleStatus.MAINTENANCE);
        }
    }

    private void updateVehicleStatus(String vehicleName, VehicleStatus status) {
        if (vehicleName == null || vehicleName.isBlank() || status == null) {
            return;
        }
        for (Vehicle vehicle : vehicleService.getAllVehicles()) {
            if (FleetUiData.displayName(vehicle).equalsIgnoreCase(vehicleName.trim())) {
                vehicle.setStatus(status);
                vehicleService.saveVehicles();
                return;
            }
        }
    }

    private boolean loadSavedJobs() {
        if (!Files.exists(MAINTENANCE_FILE)) {
            return false;
        }
        try {
            List<String> lines = Files.readAllLines(MAINTENANCE_FILE, StandardCharsets.UTF_8);
            jobs.clear();
            for (String line : lines) {
                if (line == null || line.isBlank()) {
                    continue;
                }
                String[] parts = line.split("\t", -1);
                if (parts.length < 10 || !"JOB".equals(parts[0])) {
                    continue;
                }
                jobs.add(new ServiceJob(
                        decode(parts[1]),
                        decode(parts[2]),
                        decode(parts[3]),
                        decode(parts[4]),
                        decode(parts[5]),
                        decode(parts[6]),
                        decode(parts[7]),
                        parseDouble(parts[8], 0),
                        decode(parts[9])
                ));
            }
            return !jobs.isEmpty() || !lines.isEmpty();
        } catch (IOException | IllegalArgumentException ex) {
            jobs.clear();
            return false;
        }
    }

    private void saveJobs() {
        try {
            Files.createDirectories(MAINTENANCE_FILE.getParent());
            List<String> lines = new ArrayList<>();
            for (ServiceJob job : jobs) {
                lines.add(String.join(
                        "\t",
                        "JOB",
                        encode(job.serviceId),
                        encode(job.vehicle),
                        encode(job.serviceType),
                        encode(job.technician),
                        encode(job.scheduledDate),
                        encode(job.priority),
                        encode(job.status),
                        String.valueOf(job.cost),
                        encode(job.notes)
                ));
            }
            Files.write(MAINTENANCE_FILE, lines, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            // Keep the UI responsive even if local persistence is temporarily unavailable.
        }
    }

    private static String encode(String value) {
        return Base64.getEncoder().encodeToString((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        return new String(Base64.getDecoder().decode(value == null ? "" : value), StandardCharsets.UTF_8);
    }

    private static double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return fallback;
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

    private static final class ServiceJob {
        private final String serviceId, vehicle, serviceType, technician, scheduledDate, priority, notes;
        private String status;
        private final double cost;
        ServiceJob(String serviceId, String vehicle, String serviceType, String technician, String scheduledDate,
                   String priority, String status, double cost, String notes) {
            this.serviceId = serviceId; this.vehicle = vehicle; this.serviceType = serviceType; this.technician = technician;
            this.scheduledDate = scheduledDate; this.priority = priority; this.status = status; this.cost = cost; this.notes = notes;
        }
    }

    private final class MaintenanceTableModel extends AbstractTableModel {
        private final String[] columns = {"Service ID", "Vehicle", "Service Type", "Technician", "Scheduled Date", "Priority", "Status", "Cost", "Actions"};
        private final List<ServiceJob> rows = new ArrayList<>();
        void setRows(List<ServiceJob> jobs) { rows.clear(); rows.addAll(jobs); fireTableDataChanged(); }
        ServiceJob getJobAt(int row) { return rows.get(row); }
        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int column) { return columns[column]; }
        @Override public Object getValueAt(int rowIndex, int columnIndex) {
            ServiceJob job = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> job.serviceId;
                case 1 -> job.vehicle;
                case 2 -> job.serviceType;
                case 3 -> job.technician;
                case 4 -> job.scheduledDate;
                case 5 -> job.priority;
                case 6 -> job.status;
                case 7 -> formatMoney(job.cost);
                default -> "View   Update";
            };
        }
    }

    private static final class MaintenanceHeaderRenderer extends DefaultTableCellRenderer {
        MaintenanceHeaderRenderer() { setOpaque(false); setHorizontalAlignment(SwingConstants.CENTER); setBorder(new EmptyBorder(0, 6, 0, 6)); }
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            label.setOpaque(false); label.setForeground(PALE); label.setFont(new Font("Segoe UI", Font.BOLD, 12)); label.setHorizontalAlignment(SwingConstants.CENTER); return label;
        }
        @Override protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(0, 0, new Color(16, 25, 32), getWidth(), getHeight(), new Color(8, 14, 20)));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setPaint(new GradientPaint(0, 0, new Color(214, 160, 66, 38), getWidth(), 0, new Color(214, 160, 66, 5)));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(new Color(214, 160, 66, 105)); g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            g.dispose(); super.paintComponent(raw);
        }
    }

    private static final class MaintenanceCellRenderer extends DefaultTableCellRenderer {
        MaintenanceCellRenderer() { setOpaque(true); setHorizontalAlignment(SwingConstants.CENTER); setBorder(new EmptyBorder(0, 7, 0, 7)); }
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            label.setFont(new Font("Segoe UI", column == 0 ? Font.BOLD : Font.PLAIN, 12));
            label.setBackground(selected ? new Color(72, 45, 19) : new Color(3, 9, 14));
            label.setForeground(TEXT);
            if (column == 0 || column == 8) { label.setForeground(PALE); label.setFont(new Font("Segoe UI", Font.BOLD, 12)); }
            if (column == 5) {
                String priority = String.valueOf(value);
                label.setForeground("High".equals(priority) ? RED : "Medium".equals(priority) ? ORANGE : GREEN);
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            }
            if (column == 6) {
                String status = String.valueOf(value);
                Color c = switch (status) { case "Completed" -> GREEN; case "In Service" -> BLUE; case "Due Soon" -> ORANGE; case "Delayed" -> RED; default -> PALE; };
                label.setForeground(c); label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            }
            return label;
        }
    }

    private final class MaintenanceSummaryPanel extends RoundedPanel {
        private ServiceJob job;
        private final JButton startButton = new SummaryButton("Start Service", true);
        private final JButton completeButton = new SummaryButton("Mark as Completed", false);
        MaintenanceSummaryPanel() {
            super(16, new Color(5, 12, 18, 238)); setLayout(null); setCursor(Cursor.getDefaultCursor());
            startButton.addActionListener(e -> startSelectedJob(job)); completeButton.addActionListener(e -> completeSelectedJob(job)); add(startButton); add(completeButton);
        }
        void setJob(ServiceJob job) {
            this.job = job;
            boolean hasJob = job != null;
            startButton.setVisible(hasJob); completeButton.setVisible(hasJob);
            if (hasJob) {
                startButton.setText("In Service".equals(job.status) ? "Service Running" : "Start Service");
                startButton.setEnabled(!"Completed".equals(job.status)); completeButton.setEnabled(!"Completed".equals(job.status));
            }
            repaint();
        }
        @Override public void doLayout() {
            int buttonW = Math.max(120, getWidth() - 40); int bottom = getHeight() - 22;
            completeButton.setBounds(20, bottom - 38, buttonW, 38); startButton.setBounds(20, bottom - 92, buttonW, 42);
        }
        @Override protected void paintComponent(Graphics raw) {
            super.paintComponent(raw);
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(); int y = 26;
            g.setColor(TEXT); g.setFont(new Font("Segoe UI", Font.BOLD, 15)); g.drawString("Service Summary", 20, y);
            if (job == null) { g.setColor(MUTED); g.setFont(new Font("Segoe UI", Font.PLAIN, 12)); g.drawString("Select a service from the table.", 20, y + 35); g.dispose(); return; }
            drawBadge(g, w - 98, 18, job.status);
            y += 38; divider(g, y, w); y += 24;
            g.setColor(MUTED); g.setFont(new Font("Segoe UI", Font.PLAIN, 11)); g.drawString("Service ID", 20, y);
            g.setColor(TEXT); g.setFont(new Font("Segoe UI", Font.BOLD, 20)); g.drawString(job.serviceId, 20, y + 26);
            y += 65; drawInfoLine(g, "Vehicle", job.vehicle, 20, y);
            y += 52; drawInfoLine(g, "Service Type", job.serviceType, 20, y);
            y += 52; drawInfoLine(g, "Technician", job.technician, 20, y);
            y += 52; drawInfoLine(g, "Scheduled Date", job.scheduledDate, 20, y);
            y += 52; drawInfoLine(g, "Notes", job.notes, 20, y);
            y += 46; divider(g, y, w); y += 34;
            g.setColor(TEXT); g.setFont(new Font("Segoe UI", Font.BOLD, 13)); g.drawString("Estimated Cost", 20, y);
            g.setColor(PALE); g.setFont(new Font("Segoe UI", Font.BOLD, 24));
            String cost = formatMoney(job.cost); g.drawString(cost, w - g.getFontMetrics().stringWidth(cost) - 20, y);
            g.dispose();
        }
        private void drawInfoLine(Graphics2D g, String title, String value, int x, int y) {
            g.setColor(PALE); g.setFont(new Font("Segoe UI", Font.BOLD, 11)); g.drawString(title, x, y);
            g.setColor(TEXT); g.setFont(new Font("Segoe UI", Font.PLAIN, 12)); drawTrimmed(g, value, x, y + 20, getWidth() - x - 25);
        }
        private void drawTrimmed(Graphics2D g, String value, int x, int y, int maxWidth) {
            String text = value == null ? "" : value; FontMetrics fm = g.getFontMetrics();
            while (fm.stringWidth(text) > maxWidth && text.length() > 4) text = text.substring(0, text.length() - 2) + "…";
            g.drawString(text, x, y);
        }
        private void divider(Graphics2D g, int y, int w) { g.setColor(new Color(214, 160, 66, 60)); g.drawLine(20, y, w - 20, y); }
        private void drawBadge(Graphics2D g, int x, int y, String status) {
            Color c = switch (status) { case "Completed" -> GREEN; case "In Service" -> BLUE; case "Due Soon" -> ORANGE; case "Delayed" -> RED; default -> PALE; };
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 24)); g.fillRoundRect(x, y, 78, 25, 9, 9);
            g.setColor(c); g.drawRoundRect(x, y, 78, 25, 9, 9); g.setFont(new Font("Segoe UI", Font.BOLD, 9));
            int tw = g.getFontMetrics().stringWidth(status); g.drawString(status, x + (78 - tw) / 2, y + 17);
        }
    }

    private static final class SummaryButton extends JButton {
        private final boolean filled;
        SummaryButton(String text, boolean filled) {
            super(text); this.filled = filled; setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 12)); setForeground(filled ? new Color(30, 20, 8) : PALE); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            boolean hover = getModel().isRollover() && isEnabled();
            if (filled) {
                g.setPaint(new GradientPaint(0, 0, hover ? new Color(250, 214, 150) : PALE, getWidth(), getHeight(), hover ? new Color(194, 130, 45) : new Color(170, 110, 36)));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                if (!isEnabled()) { g.setColor(new Color(0, 0, 0, 85)); g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); }
                g.setColor(isEnabled() ? new Color(30, 20, 8) : new Color(210, 210, 210));
            } else {
                g.setColor(hover ? new Color(10, 18, 26) : new Color(3, 9, 15)); g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g.setColor(new Color(214, 160, 66, hover ? 155 : 110)); g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g.setColor(isEnabled() ? PALE : MUTED);
            }
            g.setFont(getFont()); FontMetrics fm = g.getFontMetrics(); g.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent()) / 2 - 3); g.dispose();
        }
    }

    private static final class DarkComboButton extends JButton {
        private final String[] items; private String selected; private java.awt.event.ActionListener listener;
        DarkComboButton(String... items) {
            super(items.length > 0 ? items[0] : ""); this.items = items; this.selected = getText(); setPreferredSize(new Dimension(145, 40));
            setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false); setForeground(TEXT); setFont(new Font("Segoe UI", Font.BOLD, 11)); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addActionListener(e -> showPopup());
        }
        String getSelectedValue() { return selected; }
        void setSelectedValue(String value) { for (String item : items) if (item.equals(value)) { selected = item; setText(item); repaint(); return; } }
        void setOnChange(java.awt.event.ActionListener listener) { this.listener = listener; }
        private void showPopup() {
            JPopupMenu popup = new JPopupMenu(); popup.setBackground(new Color(3, 9, 15)); popup.setBorder(BorderFactory.createLineBorder(new Color(214, 160, 66, 130)));
            for (String itemText : items) {
                JMenuItem item = new JMenuItem(itemText); item.setOpaque(true); item.setBackground(new Color(3, 9, 15)); item.setForeground(TEXT); item.setFont(new Font("Segoe UI", Font.BOLD, 11));
                item.addActionListener(e -> { selected = itemText; setText(itemText); repaint(); if (listener != null) listener.actionPerformed(e); }); popup.add(item);
            }
            popup.show(this, 0, getHeight() + 3);
        }
        @Override protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(getModel().isRollover() ? new Color(9, 18, 26) : new Color(3, 9, 15)); g.fillRoundRect(0, 0, getWidth(), getHeight(), 9, 9);
            g.setColor(new Color(214, 160, 66, getModel().isRollover() ? 150 : 100)); g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 9, 9);
            g.setColor(TEXT); g.setFont(getFont()); FontMetrics fm = g.getFontMetrics(); g.drawString(getText(), 13, (getHeight() + fm.getAscent()) / 2 - 3);
            int cx = getWidth() - 18, cy = getHeight() / 2; g.setColor(PALE); g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(cx - 4, cy - 2, cx, cy + 3); g.drawLine(cx, cy + 3, cx + 4, cy - 2); g.dispose();
        }
    }

    private static final class SearchBox extends JTextField {
        private final String placeholder;
        SearchBox(String placeholder) { this.placeholder = placeholder; setOpaque(false); setForeground(TEXT); setCaretColor(PALE); setFont(new Font("Segoe UI", Font.PLAIN, 12)); setBorder(new EmptyBorder(0, 38, 0, 12)); }
        @Override protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(3, 9, 15)); g.fillRoundRect(0, 0, getWidth(), getHeight(), 9, 9);
            g.setColor(new Color(214, 160, 66, isFocusOwner() ? 130 : 65)); g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 9, 9);
            g.setColor(PALE); g.setStroke(new BasicStroke(1.5f)); g.drawOval(15, getHeight() / 2 - 7, 12, 12); g.drawLine(25, getHeight() / 2 + 4, 31, getHeight() / 2 + 10);
            g.dispose(); super.paintComponent(raw);
            if (getText().isBlank() && !isFocusOwner()) { g = (Graphics2D) raw.create(); g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); g.setColor(MUTED); g.setFont(getFont()); g.drawString(placeholder, 42, (getHeight() + g.getFontMetrics().getAscent()) / 2 - 3); g.dispose(); }
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int radius; private final Color fill;
        RoundedPanel(int radius, Color fill) { this.radius = radius; this.fill = fill; setOpaque(false); }
        @Override protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D shape = new RoundRectangle2D.Double(.5, .5, getWidth() - 1, getHeight() - 1, radius, radius);
            g.setColor(new Color(0, 0, 0, 75)); g.fillRoundRect(5, 7, Math.max(0, getWidth() - 10), Math.max(0, getHeight() - 10), radius, radius);
            g.setPaint(new GradientPaint(0, 0, brighten(fill, 24), getWidth(), getHeight(), fill)); g.fill(shape);
            g.setPaint(new GradientPaint(0, 0, new Color(255, 235, 181, 28), getWidth(), 0, new Color(214, 160, 66, 2))); g.fill(shape);
            g.setColor(new Color(214, 160, 66, 84)); g.draw(shape); g.dispose(); super.paintComponent(raw);
        }
    }

    private static final class IconCircle extends JComponent {
        private final String icon; private final Color color;
        IconCircle(String icon, Color color) { this.icon = icon; this.color = color; setOpaque(false); }
        @Override protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int s = Math.min(getWidth(), getHeight()) - 6, x = (getWidth() - s) / 2, y = (getHeight() - s) / 2;
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30)); g.fillOval(x, y, s, s);
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 135)); g.drawOval(x, y, s, s);
            g.setColor(color); g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); drawIcon(g, icon, getWidth() / 2, getHeight() / 2, 22); g.dispose();
        }
    }

    private static final class FooterBrand extends JComponent {
        @Override protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); drawVeloraMark(g, 45, getHeight() / 2, 72);
            g.setColor(TEXT); g.setFont(new Font("Segoe UI", Font.BOLD, 18)); g.drawString("VELORA MOTORS", 88, 30);
            g.setColor(PALE); g.setFont(new Font("Segoe UI", Font.PLAIN, 9)); g.drawString("PREMIUM VEHICLE RENTAL", 90, 48); g.dispose();
        }
    }

    private static final class FooterItem extends JComponent {
        private final String title, subtitle, icon;
        FooterItem(String title, String subtitle, String icon) { this.title = title; this.subtitle = subtitle; this.icon = icon; }
        @Override protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cx = 34, cy = getHeight() / 2; g.setColor(new Color(214, 160, 66, 26)); g.fillOval(cx - 18, cy - 18, 36, 36);
            g.setColor(new Color(214, 160, 66, 110)); g.drawOval(cx - 18, cy - 18, 36, 36); g.setColor(PALE); g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); drawIcon(g, icon, cx, cy, 18);
            g.setColor(TEXT); g.setFont(new Font("Segoe UI", Font.BOLD, 12)); g.drawString(title, 62, 29);
            g.setColor(MUTED); g.setFont(new Font("Segoe UI", Font.PLAIN, 10)); g.drawString(subtitle, 62, 47); g.dispose();
        }
    }

    private static final class GoldActionButton extends JButton {
        GoldActionButton(String text) { super(text); setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false); setForeground(PALE); setFont(new Font("Segoe UI", Font.BOLD, 12)); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
        @Override protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(3, 9, 15)); g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            if (getModel().isRollover()) { g.setColor(new Color(214, 160, 66, 22)); g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); }
            g.setColor(new Color(214, 160, 66, 120)); g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g.setColor(PALE); g.setStroke(new BasicStroke(1.7f)); int plusX = 18, plusY = getHeight() / 2; g.drawLine(plusX - 5, plusY, plusX + 5, plusY); g.drawLine(plusX, plusY - 5, plusX, plusY + 5); g.dispose(); super.paintComponent(raw);
        }
    }

    private static final class OutlineButton extends JButton {
        OutlineButton(String text) { super(text); setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false); setForeground(PALE); setFont(new Font("Segoe UI", Font.BOLD, 11)); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
        @Override protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(getModel().isRollover() ? new Color(10, 18, 26) : new Color(3, 9, 15)); g.fillRoundRect(0, 0, getWidth(), getHeight(), 9, 9);
            g.setColor(new Color(214, 160, 66, 110)); g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 9, 9); g.dispose(); super.paintComponent(raw);
        }
    }

    private static final class SmallPageButton extends JButton {
        private final boolean active; private final Runnable action;
        SmallPageButton(String text, boolean active, Runnable action) { super(text); this.active = active; this.action = action; setPreferredSize(new Dimension(32, 28)); setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false); setFocusable(false); setMargin(new Insets(0,0,0,0)); setFont(new Font("Segoe UI", Font.BOLD, 12)); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); addActionListener(e -> { if (this.action != null) this.action.run(); }); }
        @Override protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (active) { g.setPaint(new GradientPaint(0, 0, PALE, getWidth(), getHeight(), new Color(174, 116, 43))); g.fillRoundRect(0, 0, getWidth(), getHeight(), 7, 7); }
            else { g.setColor(getModel().isRollover() ? new Color(214, 160, 66, 28) : new Color(4, 10, 16, 235)); g.fillRoundRect(0, 0, getWidth(), getHeight(), 7, 7); }
            g.setColor(new Color(214, 160, 66, 110)); g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 7, 7);
            g.setColor(active ? new Color(25, 16, 8) : PALE); g.setFont(getFont()); FontMetrics fm = g.getFontMetrics(); g.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent()) / 2 - 3); g.dispose();
        }
    }

    private static final class DarkScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() { thumbColor = new Color(214, 160, 66, 100); trackColor = new Color(2, 7, 12); }
        @Override protected JButton createDecreaseButton(int orientation) { return hidden(); }
        @Override protected JButton createIncreaseButton(int orientation) { return hidden(); }
        private JButton hidden() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b; }
        @Override protected void paintThumb(Graphics raw, JComponent c, Rectangle b) { if (b.isEmpty()) return; Graphics2D g = (Graphics2D) raw.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g.setColor(new Color(214, 160, 66, 105)); g.fillRoundRect(b.x + 1, b.y, Math.max(3, b.width - 2), b.height, b.width, b.width); g.dispose(); }
    }

    private static void drawIcon(Graphics2D g, String type, int cx, int cy, int size) {
        int h = size / 2;
        switch (type) {
            case "TOOLS" -> { g.drawLine(cx - h, cy + h, cx + h, cy - h); g.drawOval(cx + 3, cy - h - 1, 7, 7); g.drawOval(cx - h - 1, cy + 3, 7, 7); }
            case "CAR" -> { g.drawRoundRect(cx - h, cy - 3, size, 9, 4, 4); g.drawLine(cx - 7, cy - 3, cx - 3, cy - h); g.drawLine(cx - 3, cy - h, cx + 6, cy - h); g.drawLine(cx + 6, cy - h, cx + 9, cy - 3); g.fillOval(cx - 7, cy + 5, 4, 4); g.fillOval(cx + 5, cy + 5, 4, 4); }
            case "CLOCK" -> { g.drawOval(cx - h, cy - h, size, size); g.drawLine(cx, cy, cx, cy - 7); g.drawLine(cx, cy, cx + 6, cy + 4); }
            case "CHECK" -> { g.drawOval(cx - h, cy - h, size, size); g.drawLine(cx - 7, cy, cx - 1, cy + 6); g.drawLine(cx - 1, cy + 6, cx + 8, cy - 7); }
            case "BOLT" -> { Path2D p = new Path2D.Double(); p.moveTo(cx + 2, cy - h); p.lineTo(cx - 7, cy + 1); p.lineTo(cx + 1, cy + 1); p.lineTo(cx - 2, cy + h); p.lineTo(cx + 9, cy - 3); p.lineTo(cx + 1, cy - 3); p.closePath(); g.draw(p); }
            case "SHIELD" -> { Path2D p = new Path2D.Double(); p.moveTo(cx, cy - h); p.lineTo(cx + h, cy - 4); p.lineTo(cx + h - 4, cy + h); p.lineTo(cx, cy + h + 5); p.lineTo(cx - h + 4, cy + h); p.lineTo(cx - h, cy - 4); p.closePath(); g.draw(p); }
            case "HEADSET" -> { g.drawArc(cx - h, cy - h, size, size, 0, 180); g.drawLine(cx - h, cy, cx - h, cy + 7); g.drawLine(cx + h, cy, cx + h, cy + 7); g.drawArc(cx - 4, cy + 7, 12, 7, 180, 180); }
            default -> g.drawOval(cx - h, cy - h, size, size);
        }
    }

    private static void drawVeloraMark(Graphics2D g, int cx, int cy, int width) {
        Graphics2D c = (Graphics2D) g.create(); c.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); c.setColor(PALE); c.setStroke(new BasicStroke(Math.max(2f, width / 42f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int half = width / 2; Path2D p = new Path2D.Double();
        p.moveTo(cx, cy + width / 4.0); p.lineTo(cx - half / 3.0, cy - width / 5.0); p.lineTo(cx - half, cy - width / 5.0);
        p.moveTo(cx, cy + width / 4.0); p.lineTo(cx + half / 3.0, cy - width / 5.0); p.lineTo(cx + half, cy - width / 5.0);
        p.moveTo(cx - 8, cy - 4); p.lineTo(cx - half + 12, cy - 4); p.moveTo(cx + 8, cy - 4); p.lineTo(cx + half - 12, cy - 4); c.draw(p); c.dispose();
    }

    private static Color brighten(Color color, int amount) {
        return new Color(Math.min(255, color.getRed() + amount), Math.min(255, color.getGreen() + amount), Math.min(255, color.getBlue() + amount), color.getAlpha());
    }
}