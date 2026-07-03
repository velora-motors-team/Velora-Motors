package com.velora.ui;

import com.velora.service.VehicleService;
import com.velora.vehicle.Vehicle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class ManagerDashboard extends JFrame {

    private static final Color SIDEBAR = new Color(8, 14, 24);
    private static final Color CARD = new Color(13, 21, 34);
    private static final Color CARD_DARK = new Color(10, 17, 28);
    private static final Color GOLD = new Color(214, 166, 95);
    private static final Color TEXT = new Color(238, 240, 245);
    private static final Color MUTED = new Color(150, 158, 172);
    private static final Color GREEN = new Color(70, 220, 135);
    private static final Color BLUE = new Color(88, 160, 255);
    private static final Color PURPLE = new Color(180, 120, 255);

    private final VehicleService vehicleService;

    private DefaultTableModel tableModel;
    private JTable vehicleTable;

    private JLabel totalVehiclesValue;
    private JLabel availableVehiclesValue;
    private JLabel electricVehiclesValue;
    private JLabel batteryVehiclesValue;

    public ManagerDashboard() {
        this.vehicleService = new VehicleService();

        setTitle("Velora Motors - Manager Dashboard");
        setSize(1400, 820);
        setMinimumSize(new Dimension(1200, 720));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setContentPane(createMainPanel());

        loadDashboardData();
    }

    private JPanel createMainPanel() {
        GradientPanel root = new GradientPanel();
        root.setLayout(new BorderLayout());

        root.add(createSidebar(), BorderLayout.WEST);
        root.add(createContent(), BorderLayout.CENTER);

        return root;
    }

    private JPanel createSidebar() {
        RoundedPanel sidebar = new RoundedPanel(0);
        sidebar.setPreferredSize(new Dimension(250, 820));
        sidebar.setBackground(SIDEBAR);
        sidebar.setLayout(new BorderLayout());
        sidebar.setBorder(new EmptyBorder(28, 22, 22, 22));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel logo = new JLabel("V", SwingConstants.CENTER);
        logo.setForeground(GOLD);
        logo.setFont(new Font("Serif", Font.BOLD, 52));

        JLabel brand = new JLabel("VELORA", SwingConstants.CENTER);
        brand.setForeground(TEXT);
        brand.setFont(new Font("Serif", Font.BOLD, 26));

        JLabel subBrand = new JLabel("MOTORS", SwingConstants.CENTER);
        subBrand.setForeground(GOLD);
        subBrand.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel brandPanel = new JPanel(new GridLayout(3, 1, 0, 0));
        brandPanel.setOpaque(false);
        brandPanel.add(logo);
        brandPanel.add(brand);
        brandPanel.add(subBrand);

        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new GridLayout(7, 1, 0, 14));
        menu.setBorder(new EmptyBorder(45, 0, 0, 0));

        menu.add(createMenuButton("Dashboard", true));
        menu.add(createMenuButton("Available Vehicles", false));
        menu.add(createMenuButton("Rentals", false));
        menu.add(createMenuButton("Customers", false));
        menu.add(createMenuButton("Reports", false));
        menu.add(createMenuButton("Settings", false));
        menu.add(Box.createVerticalStrut(10));

        top.add(brandPanel, BorderLayout.NORTH);
        top.add(menu, BorderLayout.CENTER);

        RoundedPanel managerCard = new RoundedPanel(22);
        managerCard.setBackground(new Color(16, 27, 43));
        managerCard.setPreferredSize(new Dimension(206, 105));
        managerCard.setLayout(null);

        JLabel managerTitle = new JLabel("Manager");
        managerTitle.setBounds(20, 17, 170, 24);
        managerTitle.setForeground(TEXT);
        managerTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        managerCard.add(managerTitle);

        JLabel managerEmail = new JLabel("admin@velora.com");
        managerEmail.setBounds(20, 42, 170, 22);
        managerEmail.setForeground(MUTED);
        managerEmail.setFont(new Font("SansSerif", Font.PLAIN, 12));
        managerCard.add(managerEmail);

        JLabel status = new JLabel("ACTIVE SESSION");
        status.setBounds(20, 70, 170, 22);
        status.setForeground(GREEN);
        status.setFont(new Font("SansSerif", Font.BOLD, 11));
        managerCard.add(status);

        sidebar.add(top, BorderLayout.CENTER);
        sidebar.add(managerCard, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createMenuButton(String text, boolean active) {
        RoundedButton button = new RoundedButton(text, 18);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, 14));
        button.setForeground(active ? Color.BLACK : TEXT);
        button.setBackground(active ? GOLD : new Color(14, 23, 37));
        button.setBorder(new EmptyBorder(12, 18, 12, 18));
        return button;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(0, 22));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(28, 28, 24, 28));

        content.add(createHeader(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 22));
        center.setOpaque(false);
        center.add(createStatsPanel(), BorderLayout.NORTH);
        center.add(createVehiclesTablePanel(), BorderLayout.CENTER);

        content.add(center, BorderLayout.CENTER);

        return content;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        textPanel.setOpaque(false);

        JLabel title = new JLabel("Manager Dashboard");
        title.setForeground(TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 34));

        JLabel subtitle = new JLabel("Welcome back, Manager");
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));

        textPanel.add(title);
        textPanel.add(subtitle);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttons.setOpaque(false);

        JButton refreshButton = createPrimaryButton("Refresh");
        refreshButton.addActionListener(e -> loadDashboardData());

        JButton logoutButton = createDangerButton("Logout");
        logoutButton.addActionListener(e -> logout());

        buttons.add(refreshButton);
        buttons.add(logoutButton);

        header.add(textPanel, BorderLayout.WEST);
        header.add(buttons, BorderLayout.EAST);

        return header;
    }

    private JPanel createStatsPanel() {
        JPanel stats = new JPanel(new GridLayout(1, 4, 18, 0));
        stats.setOpaque(false);

        totalVehiclesValue = new JLabel("0");
        availableVehiclesValue = new JLabel("0");
        electricVehiclesValue = new JLabel("0");
        batteryVehiclesValue = new JLabel("0");

        stats.add(createStatCard("Total Fleet", totalVehiclesValue, "All vehicles in system", BLUE));
        stats.add(createStatCard("Available Now", availableVehiclesValue, "Visible to customers", GREEN));
        stats.add(createStatCard("Electric Units", electricVehiclesValue, "EVs and e-bikes", GOLD));
        stats.add(createStatCard("Battery Tracked", batteryVehiclesValue, "Shows battery level", PURPLE));

        return stats;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, String subtitle, Color accent) {
        RoundedPanel card = new RoundedPanel(24);
        card.setBackground(CARD);
        card.setLayout(null);
        card.setPreferredSize(new Dimension(240, 135));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setBounds(22, 18, 190, 24);
        titleLabel.setForeground(MUTED);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        card.add(titleLabel);

        valueLabel.setBounds(22, 45, 120, 48);
        valueLabel.setForeground(TEXT);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 34));
        card.add(valueLabel);

        JLabel sub = new JLabel(subtitle);
        sub.setBounds(22, 95, 190, 24);
        sub.setForeground(MUTED);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        card.add(sub);

        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };

        dot.setOpaque(false);
        dot.setBounds(205, 24, 14, 14);
        card.add(dot);

        return card;
    }

    private JPanel createVehiclesTablePanel() {
        RoundedPanel tableCard = new RoundedPanel(26);
        tableCard.setBackground(CARD_DARK);
        tableCard.setLayout(new BorderLayout(0, 18));
        tableCard.setBorder(new EmptyBorder(22, 22, 22, 22));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Available Vehicles");
        title.setForeground(TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));

        JLabel subtitle = new JLabel("Only AVAILABLE vehicles are displayed. RENTED and MAINTENANCE vehicles are hidden.");
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));

        titlePanel.add(title);
        titlePanel.add(subtitle);

        JPanel adminButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        adminButtons.setOpaque(false);

        JButton addVehicleButton = createAdminButton("Add Vehicle");
        JButton editVehicleButton = createAdminButton("Edit Vehicle");
        JButton changeStatusButton = createAdminButton("Change Status");
        JButton setBatteryButton = createAdminButton("Set Battery");
        JButton deleteVehicleButton = createAdminDeleteButton("Delete Vehicle");

        addVehicleButton.addActionListener(e -> showComingSoon("Add Vehicle"));
        editVehicleButton.addActionListener(e -> showComingSoon("Edit Vehicle"));
        changeStatusButton.addActionListener(e -> showComingSoon("Change Status"));
        setBatteryButton.addActionListener(e -> showComingSoon("Set Battery"));
        deleteVehicleButton.addActionListener(e -> showComingSoon("Delete Vehicle"));

        adminButtons.add(addVehicleButton);
        adminButtons.add(editVehicleButton);
        adminButtons.add(changeStatusButton);
        adminButtons.add(setBatteryButton);
        adminButtons.add(deleteVehicleButton);

        top.add(titlePanel, BorderLayout.WEST);
        top.add(adminButtons, BorderLayout.EAST);

        tableCard.add(top, BorderLayout.NORTH);

        tableModel = new DefaultTableModel();
        tableModel.addColumn("ID");
        tableModel.addColumn("Brand");
        tableModel.addColumn("Model");
        tableModel.addColumn("Type");
        tableModel.addColumn("Status");
        tableModel.addColumn("Daily Price");
        tableModel.addColumn("Battery");

        vehicleTable = new JTable(tableModel);
        vehicleTable.setRowHeight(46);
        vehicleTable.setShowVerticalLines(false);
        vehicleTable.setShowHorizontalLines(true);
        vehicleTable.setGridColor(new Color(32, 43, 60));
        vehicleTable.setBackground(new Color(11, 18, 29));
        vehicleTable.setForeground(TEXT);
        vehicleTable.setSelectionBackground(new Color(35, 55, 82));
        vehicleTable.setSelectionForeground(TEXT);
        vehicleTable.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JTableHeader tableHeader = vehicleTable.getTableHeader();
        tableHeader.setBackground(new Color(15, 25, 40));
        tableHeader.setForeground(GOLD);
        tableHeader.setFont(new Font("SansSerif", Font.BOLD, 13));
        tableHeader.setPreferredSize(new Dimension(tableHeader.getWidth(), 42));
        tableHeader.setBorder(BorderFactory.createEmptyBorder());

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
        cellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        cellRenderer.setBackground(new Color(11, 18, 29));
        cellRenderer.setForeground(TEXT);

        for (int i = 0; i < vehicleTable.getColumnCount(); i++) {
            vehicleTable.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(vehicleTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(38, 51, 70), 1));
        scrollPane.getViewport().setBackground(new Color(11, 18, 29));

        tableCard.add(scrollPane, BorderLayout.CENTER);

        return tableCard;
    }

    private void loadDashboardData() {
        tableModel.setRowCount(0);

        List<Vehicle> allVehicles = vehicleService.getAllVehicles();
        List<Vehicle> availableVehicles = vehicleService.getAvailableVehicles();

        int electricCount = 0;
        int batteryTrackedCount = 0;

        for (Vehicle vehicle : allVehicles) {
            if (vehicle.hasBattery()) {
                electricCount++;
            }

            if (vehicle.getBatteryLevel() != null) {
                batteryTrackedCount++;
            }
        }

        totalVehiclesValue.setText(String.valueOf(allVehicles.size()));
        availableVehiclesValue.setText(String.valueOf(availableVehicles.size()));
        electricVehiclesValue.setText(String.valueOf(electricCount));
        batteryVehiclesValue.setText(String.valueOf(batteryTrackedCount));

        for (Vehicle vehicle : availableVehicles) {
            String batteryText = "N/A";

            if (vehicle.hasBattery() && vehicle.getBatteryLevel() != null) {
                batteryText = vehicle.getBatteryLevel() + "%";
            }

            tableModel.addRow(new Object[]{
                    vehicle.getId(),
                    vehicle.getBrand(),
                    vehicle.getModel(),
                    vehicle.getType(),
                    vehicle.getStatus(),
                    "$" + vehicle.getDailyPrice(),
                    batteryText
            });
        }
    }

    private JButton createPrimaryButton(String text) {
        RoundedButton button = new RoundedButton(text, 18);
        button.setPreferredSize(new Dimension(120, 44));
        button.setBackground(GOLD);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createDangerButton(String text) {
        RoundedButton button = new RoundedButton(text, 18);
        button.setPreferredSize(new Dimension(120, 44));
        button.setBackground(new Color(80, 22, 28));
        button.setForeground(new Color(255, 175, 175));
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createAdminButton(String text) {
        RoundedButton button = new RoundedButton(text, 16);
        button.setPreferredSize(new Dimension(122, 38));
        button.setBackground(new Color(18, 31, 50));
        button.setForeground(new Color(220, 225, 235));
        button.setFont(new Font("SansSerif", Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createAdminDeleteButton(String text) {
        RoundedButton button = new RoundedButton(text, 16);
        button.setPreferredSize(new Dimension(122, 38));
        button.setBackground(new Color(70, 20, 25));
        button.setForeground(new Color(255, 175, 175));
        button.setFont(new Font("SansSerif", Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void showComingSoon(String featureName) {
        JOptionPane.showMessageDialog(
                this,
                featureName + " feature will be implemented in the next step.",
                "Velora Motors",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void logout() {
        new LoginScreen().setVisible(true);
        dispose();
    }

    private static class GradientPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(5, 9, 16),
                    getWidth(), getHeight(), new Color(10, 18, 30)
            );

            g2.setPaint(gradient);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(214, 166, 95, 25));
            g2.fillOval(getWidth() - 430, -160, 560, 420);

            g2.setColor(new Color(70, 120, 220, 20));
            g2.fillOval(250, getHeight() - 240, 520, 320);

            g2.dispose();
        }
    }

    private static class RoundedButton extends JButton {

        private final int radius;

        public RoundedButton(String text, int radius) {
            super(text);
            this.radius = radius;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            g2.setColor(new Color(255, 255, 255, 35));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            g2.dispose();

            super.paintComponent(g);
        }
    }

    private static class RoundedPanel extends JPanel {

        private final int radius;

        public RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());

            if (radius == 0) {
                g2.fillRect(0, 0, getWidth(), getHeight());
            } else {
                g2.fill(new RoundRectangle2D.Double(
                        0,
                        0,
                        getWidth() - 1,
                        getHeight() - 1,
                        radius,
                        radius
                ));

                g2.setColor(new Color(255, 255, 255, 20));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Double(
                        0.5,
                        0.5,
                        getWidth() - 2,
                        getHeight() - 2,
                        radius,
                        radius
                ));
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}