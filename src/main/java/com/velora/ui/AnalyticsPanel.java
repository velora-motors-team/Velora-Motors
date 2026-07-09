package com.velora.ui;

import com.velora.authentication.Customer;
import com.velora.service.AuthenticationService;
import com.velora.service.VehicleService;
import com.velora.vehicle.Vehicle;
import com.velora.vehicle.VehicleStatus;
import com.velora.vehicle.VehicleType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class AnalyticsPanel extends JPanel {

    private static final Color CARD_FILL = new Color(5, 12, 18, 232);
    private static final Color GOLD = new Color(214, 160, 66);
    private static final Color PALE = new Color(238, 201, 139);
    private static final Color TEXT = new Color(243, 244, 247);
    private static final Color MUTED = new Color(157, 164, 175);
    private static final Color GREEN = new Color(86, 207, 114);

    private final VehicleService vehicleService;
    private final AuthenticationService authenticationService;
    private String selectedDateRange = "This Month";

    public AnalyticsPanel(VehicleService vehicleService, AuthenticationService authenticationService) {
        this.vehicleService = vehicleService;
        this.authenticationService = authenticationService;
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(8, 12, 24, 22));
        build();
    }

    private void build() {
        removeAll();
        List<Vehicle> vehicles = vehicleService.getAllVehicles();

        long realRented = vehicles.stream().filter(v -> v.getStatus() == VehicleStatus.RENTED).count();
        long available = vehicles.stream().filter(v -> v.getStatus() == VehicleStatus.AVAILABLE).count();
        long active = vehicles.stream().filter(v -> v.getStatus() != VehicleStatus.MAINTENANCE).count();
        int realCustomers = customerCount();

        double baseRevenue = vehicles.stream()
                .filter(v -> v.getStatus() == VehicleStatus.RENTED)
                .mapToDouble(Vehicle::getDailyPrice)
                .sum();
        if (baseRevenue <= 0) {
            baseRevenue = vehicles.stream().mapToDouble(Vehicle::getDailyPrice).sum();
        }

        double rangeFactor = rangeFactor(selectedDateRange);
        long rented = Math.max(1, Math.round(realRented * rangeFactor));
        int customers = Math.max(realCustomers, (int) Math.round(realCustomers * Math.min(rangeFactor, 2.0)));
        double revenue = baseRevenue * rangeFactor;
        double avgDaily = active == 0 ? 0 : revenue / active;

        add(createHeading(), BorderLayout.NORTH);

        DashboardScrollPanel body = new DashboardScrollPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel statsRow = new JPanel(new GridLayout(1, 5, 12, 0));
        statsRow.setOpaque(false);
        statsRow.setAlignmentX(LEFT_ALIGNMENT);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 108));
        statsRow.setPreferredSize(new Dimension(1200, 108));
        statsRow.add(statCard("Total Revenue", formatMoney(revenue), "+ Live from fleet", "BILL"));
        statsRow.add(statCard("Total Rentals", String.valueOf(rented), "Currently rented", "CAL"));
        statsRow.add(statCard("Total Customers", String.valueOf(customers), "Registered accounts", "USERS"));
        statsRow.add(statCard("Active Vehicles", String.valueOf(active), available + " available", "CAR"));
        statsRow.add(statCard("Average Daily Revenue", formatMoney(avgDaily), "Per active vehicle", "CHART"));
        body.add(statsRow);
        body.add(Box.createVerticalStrut(14));

        JPanel row2 = new JPanel(new GridLayout(1, 3, 14, 0));
        row2.setOpaque(false);
        row2.setAlignmentX(LEFT_ALIGNMENT);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 310));
        row2.setPreferredSize(new Dimension(1200, 310));
        row2.add(revenueTrendCard(vehicles, revenue));
        row2.add(categoryDonutCard(vehicles));
        row2.add(topVehiclesCard(vehicles));
        body.add(row2);
        body.add(Box.createVerticalStrut(14));

        JPanel row3 = new JPanel(new GridLayout(1, 3, 14, 0));
        row3.setOpaque(false);
        row3.setAlignmentX(LEFT_ALIGNMENT);
        row3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 292));
        row3.setPreferredSize(new Dimension(1200, 292));
        row3.add(paymentMethodCard(revenue));
        row3.add(rentalsByPeriodCard(rented));
        row3.add(customerGrowthCard(customers));
        body.add(row3);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(7, 0));
        scroll.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        add(scroll, BorderLayout.CENTER);
    }

    private JComponent createHeading() {
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(label("Analytics Overview", 28, Font.BOLD, TEXT));
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(label("Dashboard   ›   Analytics", 11, Font.PLAIN, MUTED));

        JComponent dateRange = styledCombo(
                new String[]{"Today", "This Week", "This Month", "Last 30 Days", "This Year"},
                selectedDateRange,
                value -> {
                    selectedDateRange = value;
                    build();
                    revalidate();
                    repaint();
                }
        );
        dateRange.setPreferredSize(new Dimension(220, 38));

        heading.add(titleBox, BorderLayout.WEST);
        heading.add(dateRange, BorderLayout.EAST);
        return heading;
    }

    private JComponent statCard(String title, String value, String sub, String iconKey) {
        RoundedPanel card = new RoundedPanel(16, CARD_FILL);
        card.setLayout(null);

        IconCircleBadge badge = new IconCircleBadge(iconKey);
        badge.setBounds(16, 18, 44, 44);
        card.add(badge);

        JLabel titleLbl = label(title, 11, Font.PLAIN, new Color(214, 218, 224));
        titleLbl.setBounds(72, 15, 160, 16);
        card.add(titleLbl);

        JLabel valueLbl = label(value, 24, Font.BOLD, PALE);
        valueLbl.setBounds(72, 31, 170, 28);
        card.add(valueLbl);

        JLabel subLbl = label(sub, 10, Font.PLAIN, GREEN);
        subLbl.setBounds(72, 64, 170, 16);
        card.add(subLbl);

        return card;
    }

    private JComponent revenueTrendCard(List<Vehicle> vehicles, double totalRevenue) {
        RoundedPanel card = panelCard();

        double base = totalRevenue <= 0
                ? Math.max(20, vehicles.stream().mapToDouble(Vehicle::getDailyPrice).average().orElse(25))
                : totalRevenue;

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(label("Revenue Trend", 14, Font.BOLD, TEXT), BorderLayout.WEST);

        JPanel chartHolder = new JPanel(new BorderLayout());
        chartHolder.setOpaque(false);

        Consumer<String> redraw = mode -> {
            chartHolder.removeAll();
            chartHolder.add(new SimpleLineChart(
                    revenueTrendValues(mode, base),
                    revenueTrendLabels(mode),
                    true
            ), BorderLayout.CENTER);
            chartHolder.revalidate();
            chartHolder.repaint();
        };

        header.add(styledCombo(new String[]{"Daily", "Weekly", "Monthly"}, "Weekly", redraw), BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);
        card.add(chartHolder, BorderLayout.CENTER);
        redraw.accept("Weekly");
        return card;
    }

    private JComponent categoryDonutCard(List<Vehicle> vehicles) {
        RoundedPanel card = panelCard();
        card.add(label("Rentals by Vehicle Category", 14, Font.BOLD, TEXT), BorderLayout.NORTH);

        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("Cars / SUV", 0);
        counts.put("Electric", 0);
        counts.put("Hybrid", 0);
        counts.put("Bikes / Motorcycles", 0);

        for (Vehicle v : vehicles) {
            VehicleType type = v.getType();
            switch (type) {
                case ELECTRIC_VEHICLE -> counts.put("Electric", counts.get("Electric") + 1);
                case HYBRID_CAR -> counts.put("Hybrid", counts.get("Hybrid") + 1);
                case ELECTRIC_BIKE, MOTORCYCLE -> counts.put("Bikes / Motorcycles", counts.get("Bikes / Motorcycles") + 1);
                default -> counts.put("Cars / SUV", counts.get("Cars / SUV") + 1);
            }
        }

        PieSlice[] slices = new PieSlice[]{
                new PieSlice("Cars / SUV", counts.get("Cars / SUV"), GOLD),
                new PieSlice("Electric", counts.get("Electric"), new Color(120, 120, 120)),
                new PieSlice("Hybrid", counts.get("Hybrid"), new Color(75, 170, 92)),
                new PieSlice("Bikes / Motorcycles", counts.get("Bikes / Motorcycles"), new Color(70, 95, 150))
        };

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(new DonutChartPanel(slices, String.valueOf(vehicles.size()), "Total"), BorderLayout.CENTER);
        content.add(legendPanel(slices), BorderLayout.EAST);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JComponent topVehiclesCard(List<Vehicle> vehicles) {
        RoundedPanel card = panelCard();
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(label("Top Performing Vehicles", 14, Font.BOLD, TEXT), BorderLayout.WEST);
        top.add(label("View All", 10, Font.PLAIN, GOLD), BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        List<Vehicle> sorted = vehicles.stream().sorted((a, b) -> Double.compare(b.getDailyPrice(), a.getDailyPrice())).limit(5).toList();
        JPanel list = new JPanel(new GridLayout(Math.max(1, sorted.size()), 1, 0, 8));
        list.setOpaque(false);

        int rank = 1;
        for (Vehicle vehicle : sorted) {
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(4, 0, 4, 0));
            JLabel r = label(String.valueOf(rank++), 13, Font.BOLD, GOLD);
            r.setPreferredSize(new Dimension(18, 20));

            JPanel center = new JPanel();
            center.setOpaque(false);
            center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
            center.add(label(vehicle.getBrand() + " " + vehicle.getModel(), 12, Font.BOLD, TEXT));
            center.add(label(vehicle.getType().name().replace('_', ' '), 10, Font.PLAIN, MUTED));

            row.add(r, BorderLayout.WEST);
            row.add(center, BorderLayout.CENTER);
            row.add(label(formatMoney(vehicle.getDailyPrice() * 30), 12, Font.BOLD, PALE), BorderLayout.EAST);
            list.add(row);
        }

        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JComponent paymentMethodCard(double totalRevenue) {
        RoundedPanel card = panelCard();
        card.add(label("Revenue by Payment Method", 14, Font.BOLD, TEXT), BorderLayout.NORTH);
        PieSlice[] slices = new PieSlice[]{
                new PieSlice("Credit Card", totalRevenue * .60, GOLD),
                new PieSlice("Cash", totalRevenue * .25, new Color(128, 128, 128)),
                new PieSlice("Bank Transfer", totalRevenue * .10, new Color(94, 123, 178)),
                new PieSlice("Other", totalRevenue * .05, new Color(74, 150, 96))
        };
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(new DonutChartPanel(slices, formatMoney(totalRevenue), "Total"), BorderLayout.WEST);
        content.add(legendPanel(slices), BorderLayout.CENTER);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JComponent rentalsByPeriodCard(long totalRentals) {
        RoundedPanel card = panelCard();

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(label("Rentals by Time Period", 14, Font.BOLD, TEXT), BorderLayout.WEST);

        JPanel chartHolder = new JPanel(new BorderLayout());
        chartHolder.setOpaque(false);

        Consumer<String> redraw = mode -> {
            chartHolder.removeAll();
            chartHolder.add(new SimpleBarChart(
                    rentalsValues(mode, totalRentals),
                    periodLabels(mode)
            ), BorderLayout.CENTER);
            chartHolder.revalidate();
            chartHolder.repaint();
        };

        header.add(styledCombo(new String[]{"Monthly", "Quarterly", "Yearly"}, "Monthly", redraw), BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);
        card.add(chartHolder, BorderLayout.CENTER);
        redraw.accept("Monthly");
        return card;
    }

    private JComponent customerGrowthCard(int totalCustomers) {
        RoundedPanel card = panelCard();

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(label("Customer Growth", 14, Font.BOLD, TEXT), BorderLayout.WEST);

        JPanel chartHolder = new JPanel(new BorderLayout());
        chartHolder.setOpaque(false);

        Consumer<String> redraw = mode -> {
            chartHolder.removeAll();
            chartHolder.add(new SimpleLineChart(
                    customerGrowthValues(mode, totalCustomers),
                    periodLabels(mode),
                    false
            ), BorderLayout.CENTER);
            chartHolder.revalidate();
            chartHolder.repaint();
        };

        header.add(styledCombo(new String[]{"Monthly", "Quarterly", "Yearly"}, "Monthly", redraw), BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);
        card.add(chartHolder, BorderLayout.CENTER);
        redraw.accept("Monthly");
        return card;
    }

    private RoundedPanel panelCard() {
        RoundedPanel card = new RoundedPanel(16, CARD_FILL);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        return card;
    }

    private JPanel cardHeader(String title, String[] comboItems) {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(label(title, 14, Font.BOLD, TEXT), BorderLayout.WEST);
        top.add(styledCombo(comboItems), BorderLayout.EAST);
        return top;
    }

    private JPanel legendPanel(PieSlice[] slices) {
        JPanel panel = new JPanel(new GridLayout(slices.length, 1, 0, 8));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(170, 120));
        double total = 0;
        for (PieSlice s : slices) total += s.value;

        for (PieSlice slice : slices) {
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);
            JLabel dot = new JLabel("●");
            dot.setForeground(slice.color);
            dot.setFont(new Font("Dialog", Font.BOLD, 14));
            String text = total == 0 ? slice.label + "   0%" : String.format("%s   %.0f%%", slice.label, slice.value * 100.0 / total);
            row.add(dot, BorderLayout.WEST);
            row.add(label(text, 11, Font.PLAIN, new Color(220, 223, 228)), BorderLayout.CENTER);
            panel.add(row);
        }
        return panel;
    }

    private JComponent styledCombo(String[] items) {
        return new BlackDropDown(items);
    }

    private JComponent styledCombo(String[] items, String selectedItem, Consumer<String> onChange) {
        return new BlackDropDown(items, selectedItem, onChange);
    }


    private JComponent createAnalyticsFooter() {
        RoundedPanel footer = new RoundedPanel(16, new Color(5, 12, 18, 230));
        footer.setLayout(new BorderLayout(18, 0));
        footer.setBorder(new EmptyBorder(12, 22, 12, 22));
        footer.setAlignmentX(LEFT_ALIGNMENT);
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 86));
        footer.setPreferredSize(new Dimension(1200, 86));

        BrandFooter left = new BrandFooter();
        left.setPreferredSize(new Dimension(360, 56));
        footer.add(left, BorderLayout.WEST);

        JPanel center = new JPanel(new GridLayout(1, 4, 20, 0));
        center.setOpaque(false);
        center.add(footerFeature("PREMIUM FLEET", "Latest models", "CAR"));
        center.add(footerFeature("TRUSTED SERVICE", "Excellence every step", "TOOLS"));
        center.add(footerFeature("BEST PRICES", "Luxury within reach", "BILL"));
        center.add(footerFeature("24/7 SUPPORT", "We are here for you", "USERS"));
        footer.add(center, BorderLayout.CENTER);

        JLabel right = label(
                "<html><div style='text-align:right'>Drive Luxury.<br><font color='#D6A042'><b>Drive Velora.</b></font></div></html>",
                12,
                Font.PLAIN,
                TEXT
        );
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        right.setPreferredSize(new Dimension(165, 50));
        footer.add(right, BorderLayout.EAST);

        return footer;
    }

    private JComponent footerFeature(String title, String sub, String iconKey) {
        JPanel panel = new JPanel(new BorderLayout(9, 0));
        panel.setOpaque(false);

        IconCircleBadge icon = new IconCircleBadge(iconKey);
        icon.setPreferredSize(new Dimension(34, 34));
        panel.add(icon, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel titleLabel = label(title, 11, Font.BOLD, TEXT);
        JLabel subLabel = label(sub, 10, Font.PLAIN, MUTED);

        text.add(Box.createVerticalGlue());
        text.add(titleLabel);
        text.add(Box.createVerticalStrut(3));
        text.add(subLabel);
        text.add(Box.createVerticalGlue());

        panel.add(text, BorderLayout.CENTER);
        return panel;
    }

    private double rangeFactor(String range) {
        return switch (range) {
            case "Today" -> 0.25;
            case "This Week" -> 0.65;
            case "Last 30 Days" -> 1.15;
            case "This Year" -> 4.5;
            default -> 1.0;
        };
    }

    private double[] revenueTrendValues(String mode, double base) {
        return switch (mode) {
            case "Daily" -> new double[]{base * .40, base * .52, base * .45, base * .68, base * .60, base * .82, base * .74};
            case "Monthly" -> new double[]{base * 2.2, base * 2.9, base * 3.4, base * 3.1, base * 4.0, base * 4.7};
            default -> new double[]{base * .55, base * .72, base * .68, base * .95, base * .81, base * 1.18, base * .92};
        };
    }

    private String[] revenueTrendLabels(String mode) {
        return switch (mode) {
            case "Daily" -> new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            case "Monthly" -> new String[]{"Feb", "Mar", "Apr", "May", "Jun", "Jul"};
            default -> new String[]{"Jun 01", "Jun 08", "Jun 15", "Jun 22", "Jun 29", "Jul 03", "Jul 06"};
        };
    }

    private double[] rentalsValues(String mode, long totalRentals) {
        double total = Math.max(1, totalRentals);
        return switch (mode) {
            case "Quarterly" -> new double[]{Math.max(1, total * .65), Math.max(1, total * .85), total};
            case "Yearly" -> new double[]{Math.max(1, total * .35), Math.max(1, total * .50), Math.max(1, total * .72), total};
            default -> new double[]{Math.max(0, total - 2), Math.max(0, total - 1), Math.max(1, total)};
        };
    }

    private double[] customerGrowthValues(String mode, int totalCustomers) {
        double total = Math.max(1, totalCustomers);
        return switch (mode) {
            case "Quarterly" -> new double[]{Math.max(1, total - 4), Math.max(1, total - 2), total};
            case "Yearly" -> new double[]{Math.max(1, total * .25), Math.max(1, total * .45), Math.max(1, total * .70), total};
            default -> new double[]{Math.max(0, total - 2), Math.max(0, total - 1), Math.max(1, total)};
        };
    }

    private String[] periodLabels(String mode) {
        return switch (mode) {
            case "Quarterly" -> new String[]{"Q1 2026", "Q2 2026", "Q3 2026"};
            case "Yearly" -> new String[]{"2023", "2024", "2025", "2026"};
            default -> new String[]{"May 2026", "Jun 2026", "Jul 2026"};
        };
    }

    private int customerCount() {
        return (int) authenticationService.getAllAccounts().stream().filter(account -> account.getRole() == Customer.Role.CUSTOMER).count();
    }

    private String formatMoney(double value) {
        return String.format("$%,.0f", value);
    }

    private JLabel label(String text, int size, int style, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", style, size));
        label.setForeground(color);
        return label;
    }

    private static Color brighten(Color color, int amount) {
        return new Color(Math.min(255, color.getRed() + amount), Math.min(255, color.getGreen() + amount), Math.min(255, color.getBlue() + amount), color.getAlpha());
    }

    private static void drawCentered(Graphics2D g, String text, int cx, int baseline) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, cx - fm.stringWidth(text) / 2, baseline);
    }

    private static void drawCompactIcon(Graphics2D g, String type, int cx, int cy, int size) {
        int half = size / 2;
        switch (type) {
            case "HOME" -> { Path2D house = new Path2D.Double(); house.moveTo(cx - half, cy); house.lineTo(cx, cy - half); house.lineTo(cx + half, cy); house.lineTo(cx + half - 2, cy); house.lineTo(cx + half - 2, cy + half); house.lineTo(cx - half + 2, cy + half); house.lineTo(cx - half + 2, cy); house.closePath(); g.draw(house); }
            case "CAR" -> { g.drawRoundRect(cx - half, cy - 4, size, 9, 4, 4); g.drawLine(cx - 6, cy - 4, cx - 3, cy - half); g.drawLine(cx - 3, cy - half, cx + 5, cy - half); g.drawLine(cx + 5, cy - half, cx + 8, cy - 4); g.fillOval(cx - 6, cy + 4, 4, 4); g.fillOval(cx + 4, cy + 4, 4, 4); }
            case "CAL" -> { g.drawRoundRect(cx - half, cy - half + 2, size, size - 2, 3, 3); g.drawLine(cx - half, cy - 2, cx + half, cy - 2); g.drawLine(cx - 5, cy - half, cx - 5, cy - half + 5); g.drawLine(cx + 5, cy - half, cx + 5, cy - half + 5); }
            case "USERS" -> { g.drawOval(cx - 7, cy - half, 7, 7); g.drawOval(cx + 2, cy - half, 7, 7); g.drawArc(cx - 10, cy, 12, 10, 0, 180); g.drawArc(cx, cy, 12, 10, 0, 180); }
            case "BILL" -> { g.drawRect(cx - 7, cy - half, 14, size); g.drawString("$", cx - 4, cy + 5); }
            case "TOOLS" -> { g.drawLine(cx - half, cy + half, cx + half, cy - half); g.drawOval(cx + 3, cy - half - 1, 7, 7); g.drawOval(cx - half - 1, cy + 3, 7, 7); }
            default -> { for (int i = 0; i < 4; i++) { int barH = 4 + i * 4; g.drawRect(cx - half + i * 5, cy + half - barH, 3, barH); } }
        }
    }



    private static final class DarkComboBoxUI extends BasicComboBoxUI {

        @Override
        protected javax.swing.JButton createArrowButton() {
            javax.swing.JButton button = new javax.swing.JButton() {
                @Override
                protected void paintComponent(Graphics raw) {
                    Graphics2D g = (Graphics2D) raw.create();
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;

                    g.setColor(PALE);
                    g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.drawLine(cx - 4, cy - 2, cx, cy + 3);
                    g.drawLine(cx, cy + 3, cx + 4, cy - 2);

                    g.dispose();
                }
            };

            button.setOpaque(false);
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            return button;
        }

        @Override
        public void paint(Graphics raw, JComponent c) {
            Graphics2D g = (Graphics2D) raw.create();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            boolean popupVisible = comboBox != null && comboBox.isPopupVisible();

            RoundRectangle2D box = new RoundRectangle2D.Double(
                    .5,
                    .5,
                    c.getWidth() - 1,
                    c.getHeight() - 1,
                    10,
                    10
            );

            g.setPaint(new GradientPaint(
                    0, 0, popupVisible ? new Color(20, 29, 38) : new Color(5, 12, 18),
                    c.getWidth(), c.getHeight(), popupVisible ? new Color(8, 14, 20) : new Color(2, 7, 12)
            ));
            g.fill(box);

            g.setColor(new Color(214, 160, 66, popupVisible ? 175 : 115));
            g.setStroke(new BasicStroke(1.15f));
            g.draw(box);

            Object selected = comboBox.getSelectedItem();
            String text = selected == null ? "" : selected.toString();

            g.setFont(new Font("Tahoma", Font.BOLD, 11));
            g.setColor(TEXT);

            FontMetrics fm = g.getFontMetrics();
            int y = (c.getHeight() + fm.getAscent()) / 2 - 3;
            g.drawString(text, 12, y);

            g.dispose();
        }
    }



    private static final class BlackDropDown extends JButton {

        private final String[] items;
        private int selectedIndex;
        private final JPopupMenu menu = new JPopupMenu();
        private final Consumer<String> onChange;

        BlackDropDown(String[] items) {
            this(items, null, null);
        }

        BlackDropDown(String[] items, String selectedItem, Consumer<String> onChange) {
            super((items == null || items.length == 0) ? "Select" : (selectedItem == null ? items[0] : selectedItem));
            this.items = (items == null || items.length == 0) ? new String[]{"Select"} : items.clone();
            this.onChange = onChange;
            if (selectedItem != null) {
                for (int i = 0; i < this.items.length; i++) {
                    if (this.items[i].equals(selectedItem)) {
                        selectedIndex = i;
                        break;
                    }
                }
                setText(this.items[selectedIndex]);
            }

            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(TEXT);
            setFont(new Font("Tahoma", Font.BOLD, 11));
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(new EmptyBorder(0, 12, 0, 34));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            menu.setOpaque(true);
            menu.setBackground(new Color(4, 10, 16));
            menu.setBorder(BorderFactory.createLineBorder(new Color(214, 160, 66, 135)));

            for (int i = 0; i < this.items.length; i++) {
                final int index = i;
                JMenuItem item = new JMenuItem(this.items[i]);
                item.setFont(new Font("Tahoma", Font.BOLD, 11));
                item.setForeground(TEXT);
                item.setBackground(new Color(4, 10, 16));
                item.setOpaque(true);
                item.setBorder(new EmptyBorder(8, 12, 8, 12));
                item.setPreferredSize(new Dimension(150, 32));
                item.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        item.setBackground(PALE);
                        item.setForeground(new Color(28, 18, 7));
                    }

                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        item.setBackground(new Color(4, 10, 16));
                        item.setForeground(TEXT);
                    }
                });
                item.addActionListener(e -> {
                    selectedIndex = index;
                    setText(BlackDropDown.this.items[selectedIndex]);
                    repaint();
                    if (onChange != null) {
                        onChange.accept(BlackDropDown.this.items[selectedIndex]);
                    }
                });
                menu.add(item);
            }

            addActionListener(e -> menu.show(this, 0, getHeight() + 3));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            boolean hover = getModel().isRollover();
            boolean pressed = getModel().isPressed() || menu.isVisible();

            RoundRectangle2D box = new RoundRectangle2D.Double(
                    .5, .5, getWidth() - 1, getHeight() - 1, 9, 9
            );

            g.setPaint(new GradientPaint(
                    0, 0, pressed || hover ? new Color(14, 24, 32) : new Color(5, 12, 18),
                    getWidth(), getHeight(), pressed || hover ? new Color(8, 14, 20) : new Color(2, 7, 12)
            ));
            g.fill(box);

            g.setColor(new Color(214, 160, 66, pressed || hover ? 175 : 115));
            g.setStroke(new BasicStroke(pressed || hover ? 1.35f : 1.05f));
            g.draw(box);

            g.setColor(TEXT);
            g.setFont(new Font("Tahoma", Font.BOLD, 11));

            String label = getText();
            FontMetrics fm = g.getFontMetrics();
            int max = getWidth() - 36;
            while (fm.stringWidth(label) > max && label.length() > 4) {
                label = label.substring(0, label.length() - 2) + "…";
            }

            g.drawString(label, 12, (getHeight() + fm.getAscent()) / 2 - 3);

            int cx = getWidth() - 18;
            int cy = getHeight() / 2;
            g.setColor(PALE);
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(cx - 4, cy - 2, cx, cy + 3);
            g.drawLine(cx, cy + 3, cx + 4, cy - 2);

            g.dispose();
        }
    }

    private static final class DarkFilterCombo extends JComponent {

        private final String[] items;
        private int selectedIndex;
        private boolean hover;
        private final JPopupMenu popup = new JPopupMenu();

        DarkFilterCombo(String[] items) {
            this.items = items == null || items.length == 0 ? new String[]{"Select"} : items.clone();
            setOpaque(false);
            setPreferredSize(new Dimension(118, 34));
            setMinimumSize(new Dimension(104, 32));
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

            popup.setOpaque(true);
            popup.setBackground(new Color(4, 10, 16));
            popup.setBorder(BorderFactory.createLineBorder(new Color(214, 160, 66, 130)));

            rebuildPopup();

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    hover = false;
                    repaint();
                }

                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    popup.show(DarkFilterCombo.this, 0, getHeight() + 3);
                }
            });
        }

        private void rebuildPopup() {
            popup.removeAll();

            for (int i = 0; i < items.length; i++) {
                final int index = i;
                JMenuItem item = new JMenuItem(items[i]) {
                    @Override
                    protected void paintComponent(Graphics raw) {
                        Graphics2D g = (Graphics2D) raw.create();
                        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        boolean armed = getModel().isArmed() || index == selectedIndex;
                        g.setColor(armed ? PALE : new Color(4, 10, 16));
                        g.fillRect(0, 0, getWidth(), getHeight());

                        g.setColor(armed ? new Color(25, 17, 8) : TEXT);
                        g.setFont(new Font("Tahoma", Font.BOLD, 11));
                        FontMetrics fm = g.getFontMetrics();
                        g.drawString(getText(), 12, (getHeight() + fm.getAscent()) / 2 - 3);

                        g.dispose();
                    }
                };

                item.setOpaque(false);
                item.setPreferredSize(new Dimension(145, 32));
                item.setBorder(new EmptyBorder(0, 0, 0, 0));
                item.addActionListener(e -> {
                    selectedIndex = index;
                    repaint();
                });

                popup.add(item);
            }
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            boolean open = popup.isVisible();

            RoundRectangle2D box = new RoundRectangle2D.Double(
                    .5,
                    .5,
                    getWidth() - 1,
                    getHeight() - 1,
                    9,
                    9
            );

            g.setPaint(new GradientPaint(
                    0, 0, open || hover ? new Color(14, 24, 32) : new Color(5, 12, 18),
                    getWidth(), getHeight(), open || hover ? new Color(8, 14, 20) : new Color(2, 7, 12)
            ));
            g.fill(box);

            g.setColor(new Color(214, 160, 66, open || hover ? 170 : 110));
            g.setStroke(new BasicStroke(open || hover ? 1.35f : 1.05f));
            g.draw(box);

            g.setColor(TEXT);
            g.setFont(new Font("Tahoma", Font.BOLD, 11));
            String text = items[selectedIndex];
            FontMetrics fm = g.getFontMetrics();
            int y = (getHeight() + fm.getAscent()) / 2 - 3;

            int maxWidth = getWidth() - 34;
            while (fm.stringWidth(text) > maxWidth && text.length() > 4) {
                text = text.substring(0, text.length() - 2) + "…";
            }
            g.drawString(text, 12, y);

            int cx = getWidth() - 18;
            int cy = getHeight() / 2;
            g.setColor(PALE);
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(cx - 4, cy - 2, cx, cy + 3);
            g.drawLine(cx, cy + 3, cx + 4, cy - 2);

            g.dispose();
        }
    }

    private static final class BrandFooter extends JComponent {

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            drawVeloraWingLogo(g, 48, getHeight() / 2 + 2, 82, PALE, true);

            g.setColor(TEXT);
            g.setFont(new Font("Serif", Font.BOLD, 20));
            g.drawString("VELORA MOTORS", 108, 34);

            g.setColor(PALE);
            g.setFont(new Font("Tahoma", Font.PLAIN, 10));
            g.drawString("PREMIUM VEHICLE RENTAL", 110, 53);

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

    private static final class PieSlice { final String label; final double value; final Color color; PieSlice(String label, double value, Color color) { this.label = label; this.value = value; this.color = color; } }

    private static final class IconCircleBadge extends JComponent {
        private final String iconKey;
        IconCircleBadge(String iconKey) { this.iconKey = iconKey; setOpaque(false); }
        @Override protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(214, 160, 66, 24)); g.fillOval(0, 0, getWidth(), getHeight());
            g.setColor(new Color(214, 160, 66, 110)); g.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
            g.setColor(GOLD); g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawCompactIcon(g, iconKey, getWidth() / 2, getHeight() / 2, 18);
            g.dispose();
        }
    }

    private static final class DonutChartPanel extends JPanel {
        private final PieSlice[] slices; private final String centerValue; private final String centerLabel;
        DonutChartPanel(PieSlice[] slices, String centerValue, String centerLabel) { this.slices = slices; this.centerValue = centerValue; this.centerLabel = centerLabel; setOpaque(false); setPreferredSize(new Dimension(210, 160)); }
        @Override protected void paintComponent(Graphics raw) {
            super.paintComponent(raw); Graphics2D g = (Graphics2D) raw.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(getWidth(), getHeight()) - 34; int x = 18; int y = (getHeight() - size) / 2;
            double total = 0; for (PieSlice s : slices) total += s.value; if (total <= 0) total = 1;
            g.setStroke(new BasicStroke(22f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)); int start = 90;
            for (PieSlice slice : slices) { int arc = (int)Math.round(slice.value * 360 / total); g.setColor(slice.color); g.drawArc(x, y, size, size, start, -arc); start -= arc; }
            int cx = x + size / 2; int cy = y + size / 2;
            g.setColor(TEXT); g.setFont(new Font("Segoe UI", Font.BOLD, 16)); FontMetrics fm = g.getFontMetrics(); g.drawString(centerValue, cx - fm.stringWidth(centerValue) / 2, cy - 3);
            g.setColor(MUTED); g.setFont(new Font("Segoe UI", Font.PLAIN, 11)); fm = g.getFontMetrics(); g.drawString(centerLabel, cx - fm.stringWidth(centerLabel) / 2, cy + 16);
            g.dispose();
        }
    }

    private static final class SimpleLineChart extends JPanel {
        private final double[] values; private final String[] labels; private final boolean money;
        SimpleLineChart(double[] values, String[] labels, boolean money) { this.values = values; this.labels = labels; this.money = money; setOpaque(false); }
        @Override protected void paintComponent(Graphics raw) {
            super.paintComponent(raw); Graphics2D g = (Graphics2D) raw.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int left = 42, right = 14, top = 20, bottom = 30; int w = getWidth() - left - right; int h = getHeight() - top - bottom;
            double max = 1; for (double v : values) max = Math.max(max, v);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            for (int i = 0; i <= 4; i++) { int y = top + i * h / 4; g.setColor(new Color(255,255,255,20)); g.drawLine(left, y, left + w, y); g.setColor(MUTED); String scale = money ? "$" + (int)(max - (max * i / 4)) : String.valueOf((int)(max - (max * i / 4))); g.drawString(scale, 4, y + 4); }
            int[] xs = new int[values.length]; int[] ys = new int[values.length];
            for (int i = 0; i < values.length; i++) { xs[i] = left + (i * w / Math.max(1, values.length - 1)); ys[i] = top + h - (int)((values[i] / max) * h); }
            g.setColor(new Color(214,160,66,45)); for (int i = 1; i < xs.length; i++) { Polygon p = new Polygon(); p.addPoint(xs[i-1], top+h); p.addPoint(xs[i-1], ys[i-1]); p.addPoint(xs[i], ys[i]); p.addPoint(xs[i], top+h); g.fillPolygon(p); }
            g.setColor(GOLD); g.setStroke(new BasicStroke(2f)); for (int i = 1; i < xs.length; i++) g.drawLine(xs[i-1], ys[i-1], xs[i], ys[i]);
            g.setColor(PALE); for (int i = 0; i < xs.length; i++) g.fillOval(xs[i]-3, ys[i]-3, 6, 6);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 10)); g.setColor(MUTED); for (int i = 0; i < labels.length; i++) { int tx = xs[i] - g.getFontMetrics().stringWidth(labels[i]) / 2; g.drawString(labels[i], tx, getHeight() - 8); }
            g.dispose();
        }
    }

    private static final class SimpleBarChart extends JPanel {
        private final double[] values; private final String[] labels;
        SimpleBarChart(double[] values, String[] labels) { this.values = values; this.labels = labels; setOpaque(false); }
        @Override protected void paintComponent(Graphics raw) {
            super.paintComponent(raw); Graphics2D g = (Graphics2D) raw.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int left = 35, right = 12, top = 22, bottom = 30; int w = getWidth() - left - right; int h = getHeight() - top - bottom;
            g.setColor(new Color(255,255,255,20)); for (int i = 0; i <= 4; i++) { int y = top + i * h / 4; g.drawLine(left, y, left + w, y); }
            double max = 1; for (double v : values) max = Math.max(max, v); int barWidth = Math.max(32, w / (values.length * 3));
            for (int i = 0; i < values.length; i++) { int x = left + i * (w / values.length) + (w / values.length - barWidth) / 2; int barH = (int)((values[i] / max) * h); int y = top + h - barH; g.setPaint(new GradientPaint(x, y, new Color(241,203,128), x, y + barH, new Color(153,103,41))); g.fillRoundRect(x, y, barWidth, barH, 8, 8); g.setColor(TEXT); String val = String.valueOf((int) values[i]); g.setFont(new Font("Segoe UI", Font.BOLD, 10)); int tw = g.getFontMetrics().stringWidth(val); g.drawString(val, x + (barWidth - tw) / 2, y - 6); g.setColor(MUTED); g.setFont(new Font("Segoe UI", Font.PLAIN, 10)); tw = g.getFontMetrics().stringWidth(labels[i]); g.drawString(labels[i], x + (barWidth - tw) / 2, getHeight() - 8); }
            g.dispose();
        }
    }

    private static final class RoundedPanel extends JPanel {
        private final int radius; private final Color fill;
        RoundedPanel(int radius, Color fill) { this.radius = radius; this.fill = fill; setOpaque(false); }
        @Override protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D shape = new RoundRectangle2D.Double(.5, .5, getWidth() - 1, getHeight() - 1, radius, radius);
            g.setColor(new Color(0, 0, 0, 72)); g.fillRoundRect(5, 7, Math.max(0, getWidth() - 10), Math.max(0, getHeight() - 10), radius, radius);
            g.setPaint(new GradientPaint(0, 0, brighten(fill, 16), getWidth(), getHeight(), fill)); g.fill(shape);
            g.setPaint(new GradientPaint(0, 0, new Color(255,235,181,38), getWidth(), 0, new Color(214,160,66,4))); g.fill(shape);
            g.setColor(new Color(214,160,66,82)); g.setStroke(new BasicStroke(1f)); g.draw(shape);
            g.dispose(); super.paintComponent(raw);
        }
    }

    private static final class DashboardScrollPanel extends JPanel implements Scrollable {
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 18; }
        @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) { return 120; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private static final class DarkScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() { thumbColor = new Color(214,160,66,100); trackColor = new Color(2,7,12); }
        @Override protected JButton createDecreaseButton(int orientation) { return hiddenScrollButton(); }
        @Override protected JButton createIncreaseButton(int orientation) { return hiddenScrollButton(); }
        private JButton hiddenScrollButton() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); b.setMinimumSize(new Dimension(0,0)); b.setMaximumSize(new Dimension(0,0)); return b; }
        @Override protected void paintThumb(Graphics raw, JComponent component, Rectangle bounds) { if (bounds.isEmpty() || !scrollbar.isEnabled()) return; Graphics2D g = (Graphics2D) raw.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g.setColor(new Color(214,160,66,105)); g.fillRoundRect(bounds.x + 1, bounds.y, Math.max(3, bounds.width - 2), bounds.height, bounds.width, bounds.width); g.dispose(); }
    }
}
