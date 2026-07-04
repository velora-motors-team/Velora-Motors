package com.velora.ui;

import com.velora.authentication.Customer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public final class CustomerDashboard extends JFrame {

    private static final Color GOLD = new Color(214, 168, 91);
    private static final Color PALE = new Color(236, 203, 157);
    private static final Color WHITE = new Color(244, 246, 249);
    private static final Color MUTED = new Color(155, 163, 176);

    public CustomerDashboard(Customer customer) {
        super("Velora Motors - Customer");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setContentPane(createContent(customer));
    }

    private JPanel createContent(Customer customer) {
        GradientPanel root = new GradientPanel();
        root.setLayout(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(38, 48, 38, 48));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel brand = new JLabel("V E L O R A   M O T O R S");
        brand.setForeground(PALE);
        brand.setFont(new Font("Serif", Font.PLAIN, 25));

        JButton logout = new JButton("LOG OUT");
        logout.setForeground(PALE);
        logout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logout.setOpaque(false);
        logout.setContentAreaFilled(false);
        logout.setFocusPainted(false);
        logout.setBorder(BorderFactory.createLineBorder(new Color(214, 168, 91, 120)));
        logout.setPreferredSize(new Dimension(110, 36));
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> {
            new LoginScreen().setVisible(true);
            dispose();
        });

        header.add(brand, BorderLayout.WEST);
        header.add(logout, BorderLayout.EAST);

        JPanel center = new JPanel(null);
        center.setOpaque(false);

        JLabel welcome = new JLabel(
                "Welcome, " + customer.getFullName(),
                SwingConstants.CENTER
        );
        welcome.setForeground(WHITE);
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 34));
        welcome.setBounds(90, 115, 820, 55);

        JLabel subtitle = new JLabel(
                "Your Velora account is connected and ready.",
                SwingConstants.CENTER
        );
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setBounds(90, 176, 820, 30);

        JLabel email = accountLabel("EMAIL", customer.getEmail());
        email.setBounds(230, 260, 540, 70);

        JLabel phone = accountLabel("PHONE", customer.getPhone());
        phone.setBounds(230, 345, 540, 70);

        center.add(welcome);
        center.add(subtitle);
        center.add(email);
        center.add(phone);

        root.add(header, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        return root;
    }

    private JLabel accountLabel(String title, String value) {
        JLabel label = new JLabel(
                "<html><div style='text-align:center'>"
                + "<span style='color:#D6A85B;font-size:10px'>" + title + "</span><br>"
                + "<span style='color:#F4F6F9;font-size:15px'>" + value + "</span>"
                + "</div></html>",
                SwingConstants.CENTER
        );
        label.setOpaque(true);
        label.setBackground(new Color(9, 16, 24, 220));
        label.setBorder(BorderFactory.createLineBorder(new Color(214, 168, 91, 70)));
        return label;
    }

    private static final class GradientPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics raw) {
            super.paintComponent(raw);
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setPaint(new GradientPaint(
                    0, 0, new Color(4, 10, 17),
                    getWidth(), getHeight(), new Color(12, 20, 31)
            ));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.dispose();
        }
    }
}
