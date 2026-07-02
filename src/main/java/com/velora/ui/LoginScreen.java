package com.velora.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ImageIcon;
import javax.swing.border.EmptyBorder;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;

/**
 * Premium manager login screen for Velora Motors.
 */
public class LoginScreen extends JFrame {

    private static final Color BG_DARK = new Color(4, 9, 15);
    private static final Color PANEL_DARK = new Color(8, 14, 22, 238);
    private static final Color PANEL_DARK_2 = new Color(10, 18, 28, 235);
    private static final Color INPUT_BG = new Color(11, 19, 29);
    private static final Color GOLD = new Color(214, 166, 95);
    private static final Color GOLD_LIGHT = new Color(235, 197, 139);
    private static final Color TEXT_WHITE = new Color(239, 239, 239);
    private static final Color TEXT_MUTED = new Color(160, 165, 175);
    private static final Color RED = new Color(225, 60, 60);

    private HintTextField usernameField;
    private HintPasswordField passwordField;
    private JLabel warningTextLabel;
    private RoundedPanel warningPanel;

    /**
     * Creates the login frame.
     */
    public LoginScreen() {
        setTitle("Velora Motors - Manager Login");
        setSize(1536, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setContentPane(createMainPanel());
    }

    /**
     * Creates the full login screen.
     *
     * @return main panel
     */
    private JPanel createMainPanel() {
        GradientBackgroundPanel root = new GradientBackgroundPanel();
        root.setLayout(null);

        JLabel centerImage = createImageLabel("/images/CENTER.png", 1000, 830);
        centerImage.setBounds(15, 15, 1000, 830);
        root.add(centerImage);

        JPanel loginCard = createLoginCard();
        loginCard.setBounds(1045, 15, 460, 820);
        root.add(loginCard);

        JPanel footer = createFooter();
        footer.setBounds(15, 842, 1490, 35);
        root.add(footer);

        return root;
    }

    /**
     * Creates the right-side login panel.
     *
     * @return login card
     */
    private JPanel createLoginCard() {
        RoundedPanel card = new RoundedPanel(30);
        card.setBackground(PANEL_DARK);
        card.setLayout(null);

        JLabel bmwLogo = createImageLabel("/images/BMW_LOGO_GLOW.png", 92, 92);
        bmwLogo.setBounds(184, 18, 92, 92);
        card.add(bmwLogo);

        JLabel title = new JLabel("MANAGER LOGIN", SwingConstants.CENTER);
        title.setBounds(35, 126, 390, 42);
        title.setForeground(GOLD);
        title.setFont(new Font("SansSerif", Font.PLAIN, 26));
        card.add(title);

        JLabel subTitle = new JLabel("Welcome back! Please sign in to continue.", SwingConstants.CENTER);
        subTitle.setBounds(35, 168, 390, 24);
        subTitle.setForeground(TEXT_WHITE);
        subTitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        card.add(subTitle);

        JLabel divider = new JLabel("────", SwingConstants.CENTER);
        divider.setBounds(35, 196, 390, 22);
        divider.setForeground(GOLD);
        divider.setFont(new Font("SansSerif", Font.PLAIN, 18));
        card.add(divider);

        JLabel usernameLabel = new JLabel("USERNAME");
        usernameLabel.setBounds(40, 235, 200, 22);
        usernameLabel.setForeground(TEXT_WHITE);
        usernameLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        card.add(usernameLabel);

        JPanel usernameBox = createInputBox("user");
        usernameBox.setBounds(40, 262, 388, 48);

        usernameField = new HintTextField("Enter your username");
        usernameField.setBounds(52, 0, 310, 48);
        styleField(usernameField);
        usernameBox.add(usernameField);

        card.add(usernameBox);

        JLabel passwordLabel = new JLabel("PASSWORD");
        passwordLabel.setBounds(40, 335, 200, 22);
        passwordLabel.setForeground(TEXT_WHITE);
        passwordLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        card.add(passwordLabel);

        JPanel passwordBox = createInputBox("password");
        passwordBox.setBounds(40, 362, 388, 48);

        passwordField = new HintPasswordField("Enter your password");
        passwordField.setEchoChar('•');
        passwordField.setBounds(52, 0, 270, 48);
        styleField(passwordField);
        passwordBox.add(passwordField);

        card.add(passwordBox);

        JCheckBox rememberMe = new JCheckBox("Remember Me");
        rememberMe.setBounds(40, 432, 145, 24);
        rememberMe.setOpaque(false);
        rememberMe.setForeground(TEXT_WHITE);
        rememberMe.setSelected(true);
        rememberMe.setFocusPainted(false);
        rememberMe.setFont(new Font("SansSerif", Font.PLAIN, 12));
        card.add(rememberMe);

        JLabel forgotPassword = new JLabel("Forgot Password?");
        forgotPassword.setBounds(300, 432, 130, 24);
        forgotPassword.setForeground(GOLD);
        forgotPassword.setFont(new Font("SansSerif", Font.PLAIN, 12));
        card.add(forgotPassword);

        GoldButton signInButton = new GoldButton("SIGN IN   →");
        signInButton.setBounds(40, 470, 388, 48);
        signInButton.addActionListener(e -> handleLogin());
        card.add(signInButton);

        RoundedPanel securityCard = new RoundedPanel(16);
        securityCard.setBackground(PANEL_DARK_2);
        securityCard.setBounds(40, 532, 388, 58);
        securityCard.setLayout(null);

        ShieldIconPanel shieldIcon = new ShieldIconPanel();
        shieldIcon.setBounds(18, 17, 24, 24);
        securityCard.add(shieldIcon);

        JLabel securityText = new JLabel(
                "<html>For your security, your session will expire<br>after 10 minutes of inactivity.</html>"
        );
        securityText.setBounds(58, 8, 310, 42);
        securityText.setForeground(TEXT_WHITE);
        securityText.setFont(new Font("SansSerif", Font.PLAIN, 12));
        securityCard.add(securityText);

        card.add(securityCard);

        warningPanel = new RoundedPanel(16);
        warningPanel.setBackground(new Color(40, 8, 8, 230));
        warningPanel.setBounds(40, 600, 388, 50);
        warningPanel.setLayout(null);

        JLabel warnIcon = new JLabel("!");
        warnIcon.setHorizontalAlignment(SwingConstants.CENTER);
        warnIcon.setBounds(16, 12, 28, 28);
        warnIcon.setForeground(RED);
        warnIcon.setFont(new Font("SansSerif", Font.BOLD, 20));
        warningPanel.add(warnIcon);

        warningTextLabel = new JLabel(
                "<html>Too many failed attempts.<br>Please wait 30 seconds before trying again.</html>"
        );
        warningTextLabel.setBounds(58, 7, 250, 35);
        warningTextLabel.setForeground(RED);
        warningTextLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        warningPanel.add(warningTextLabel);

        JPanel secCircle = createSecondCircle();
        secCircle.setBounds(330, 5, 40, 40);
        warningPanel.add(secCircle);

        card.add(warningPanel);

        JLabel carPhoto = createImageLabel("/images/CAR_PHOTO.png", 388, 130);
        carPhoto.setBounds(40, 663, 388, 130);
        card.add(carPhoto);

        return card;
    }

    /**
     * Creates an input box with icon.
     *
     * @param type icon type
     * @return input panel
     */
    private JPanel createInputBox(String type) {
        RoundedPanel box = new RoundedPanel(12);
        box.setBackground(INPUT_BG);
        box.setLayout(null);

        IconPanel iconPanel = new IconPanel(type);
        iconPanel.setBounds(14, 12, 24, 24);
        box.add(iconPanel);

        if ("password".equals(type)) {
            EyeButton eyeButton = new EyeButton();
            eyeButton.setBounds(350, 10, 28, 28);

            eyeButton.addActionListener(e -> {
                if (passwordField.getEchoChar() == 0) {
                    passwordField.setEchoChar('•');
                    eyeButton.setOpen(false);
                } else {
                    passwordField.setEchoChar((char) 0);
                    eyeButton.setOpen(true);
                }
            });

            box.add(eyeButton);
        }

        return box;
    }

    /**
     * Creates the red seconds circle.
     *
     * @return circle panel
     */
    private JPanel createSecondCircle() {
        JPanel secCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(45, 12, 12));
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);

                g2.setColor(RED);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(1, 1, getWidth() - 3, getHeight() - 3);

                g2.dispose();
            }
        };

        secCircle.setOpaque(false);
        secCircle.setLayout(new GridLayout(2, 1));

        JLabel secLabel1 = new JLabel("30", SwingConstants.CENTER);
        secLabel1.setForeground(TEXT_WHITE);
        secLabel1.setFont(new Font("SansSerif", Font.BOLD, 12));

        JLabel secLabel2 = new JLabel("SEC", SwingConstants.CENTER);
        secLabel2.setForeground(TEXT_WHITE);
        secLabel2.setFont(new Font("SansSerif", Font.PLAIN, 8));

        secCircle.add(secLabel1);
        secCircle.add(secLabel2);

        return secCircle;
    }

    /**
     * Creates the footer.
     *
     * @return footer panel
     */
    private JPanel createFooter() {
        JPanel footer = new JPanel(null);
        footer.setOpaque(false);

        JLabel brand = new JLabel("V  VELORA MOTORS");
        brand.setForeground(TEXT_WHITE);
        brand.setFont(new Font("SansSerif", Font.PLAIN, 13));
        brand.setBounds(0, 2, 220, 20);
        footer.add(brand);

        JLabel sub = new JLabel("Premium BMW Vehicle Rental System");
        sub.setForeground(TEXT_MUTED);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        sub.setBounds(20, 18, 240, 16);
        footer.add(sub);

        JLabel copy = new JLabel("© 2026 Velora Motors. All rights reserved.", SwingConstants.CENTER);
        copy.setForeground(TEXT_MUTED);
        copy.setFont(new Font("SansSerif", Font.PLAIN, 12));
        copy.setBounds(510, 8, 460, 20);
        footer.add(copy);

        JLabel follow = new JLabel("FOLLOW US");
        follow.setForeground(TEXT_MUTED);
        follow.setFont(new Font("SansSerif", Font.PLAIN, 12));
        follow.setBounds(1120, 8, 90, 20);
        footer.add(follow);

        JButton fb = createSocialButton("f");
        fb.setBounds(1225, 0, 34, 34);
        footer.add(fb);

        JButton ig = createSocialButton("ig");
        ig.setBounds(1273, 0, 34, 34);
        footer.add(ig);

        JButton in = createSocialButton("in");
        in.setBounds(1321, 0, 34, 34);
        footer.add(in);

        JButton yt = createSocialButton("▶");
        yt.setBounds(1369, 0, 34, 34);
        footer.add(yt);

        return footer;
    }

    /**
     * Creates a social button.
     *
     * @param text button text
     * @return social button
     */
    private JButton createSocialButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(GOLD);
        button.setFont(new Font("SansSerif", Font.BOLD, text.length() > 1 ? 11 : 14));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(145, 110, 60), 1));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    /**
     * Handles login action.
     */
    private void handleLogin() {
        String username = usernameField.getActualText().trim();
        String password = passwordField.getActualPassword();

        if (username.equals("admin") && password.equals("admin123")) {
            warningTextLabel.setText("<html>Login successful.<br>Ready to open dashboard.</html>");
            warningTextLabel.setForeground(new Color(70, 220, 120));
            warningPanel.setBackground(new Color(10, 35, 12, 230));

            JOptionPane.showMessageDialog(
                    this,
                    "Login successful.\nNext step: open Manager Dashboard.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } else {
            warningTextLabel.setText("<html>Invalid username or password.<br>Please try again.</html>");
            warningTextLabel.setForeground(RED);
            warningPanel.setBackground(new Color(40, 8, 8, 230));
        }
    }

    /**
     * Styles input fields.
     *
     * @param field input field
     */
    private void styleField(JTextField field) {
        field.setOpaque(false);
        field.setBorder(new EmptyBorder(0, 0, 0, 0));
        field.setForeground(TEXT_WHITE);
        field.setCaretColor(GOLD_LIGHT);
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
    }

    /**
     * Loads an image from resources and scales it.
     *
     * @param resourcePath image path
     * @param width target width
     * @param height target height
     * @return image label
     */
    private JLabel createImageLabel(String resourcePath, int width, int height) {
        URL url = getClass().getResource(resourcePath);

        if (url == null) {
            JLabel fallback = new JLabel("Image not found: " + resourcePath, SwingConstants.CENTER);
            fallback.setForeground(Color.RED);
            fallback.setBounds(0, 0, width, height);
            return fallback;
        }

        ImageIcon icon = new ImageIcon(url);
        Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);

        return new JLabel(new ImageIcon(scaled));
    }

    /**
     * Runs this screen directly.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
    }

    /**
     * Premium dark background.
     */
    private static class GradientBackgroundPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gradient = new GradientPaint(
                    0, 0, BG_DARK,
                    getWidth(), getHeight(), new Color(5, 10, 18)
            );

            g2.setPaint(gradient);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(19, 34, 55, 110));
            g2.fillOval(-200, -80, 700, 500);

            g2.setColor(new Color(92, 58, 25, 70));
            g2.fillOval(getWidth() - 520, 10, 520, 650);

            g2.setColor(new Color(255, 190, 100, 18));
            for (int i = 0; i < getWidth(); i += 58) {
                g2.drawLine(i, getHeight() - 150, i + 220, getHeight());
            }

            g2.dispose();
        }
    }

    /**
     * Rounded panel.
     */
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
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));

            g2.setColor(new Color(145, 110, 60, 95));
            g2.setStroke(new BasicStroke(1.1f));
            g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 2, getHeight() - 2, radius, radius));

            g2.dispose();

            super.paintComponent(g);
        }
    }

    /**
     * Gold gradient button.
     */
    private static class GoldButton extends JButton {

        public GoldButton(String text) {
            super(text);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setForeground(Color.BLACK);
            setFont(new Font("SansSerif", Font.BOLD, 14));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(
                    0, 0, GOLD_LIGHT,
                    0, getHeight(), GOLD
            );

            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

            g2.setColor(new Color(255, 235, 210, 90));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(getText());
            int x = (getWidth() - textWidth) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

            g2.setColor(Color.BLACK);
            g2.drawString(getText(), x, y);

            g2.dispose();
        }
    }

    /**
     * Input icon.
     */
    private static class IconPanel extends JPanel {

        private final String type;

        public IconPanel(String type) {
            this.type = type;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(145, 150, 160));
            g2.setStroke(new BasicStroke(1.7f));

            if ("user".equals(type)) {
                g2.drawOval(8, 3, 8, 8);
                g2.drawArc(4, 12, 16, 12, 0, 180);
                g2.drawLine(4, 18, 4, 22);
                g2.drawLine(20, 18, 20, 22);
            } else if ("password".equals(type)) {
                g2.drawRoundRect(5, 11, 14, 10, 3, 3);
                g2.drawArc(8, 4, 8, 12, 0, 180);
                g2.fillOval(11, 15, 3, 3);
            }

            g2.dispose();
        }
    }

    /**
     * Eye button for showing and hiding password.
     */
    private static class EyeButton extends JButton {

        private boolean open;

        public EyeButton() {
            this.open = false;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        public void setOpen(boolean open) {
            this.open = open;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(145, 150, 160));
            g2.setStroke(new BasicStroke(1.7f));

            g2.drawArc(4, 8, 20, 12, 0, 180);
            g2.drawArc(4, 8, 20, 12, 180, 180);

            if (open) {
                g2.fillOval(11, 12, 6, 6);
            } else {
                g2.drawOval(11, 12, 6, 6);
                g2.drawLine(5, 23, 24, 5);
            }

            g2.dispose();
        }
    }

    /**
     * Shield icon.
     */
    private static class ShieldIconPanel extends JPanel {

        public ShieldIconPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(170, 175, 185));
            g2.setStroke(new BasicStroke(1.6f));

            int[] x = {12, 20, 18, 12, 6, 4};
            int[] y = {2, 6, 16, 22, 16, 6};

            g2.drawPolygon(x, y, x.length);
            g2.dispose();
        }
    }

    /**
     * Text field with placeholder.
     */
    private static class HintTextField extends JTextField {

        private final String hint;

        public HintTextField(String hint) {
            this.hint = hint;
            setOpaque(false);

            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    repaint();
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    repaint();
                }
            });
        }

        public String getActualText() {
            return getText();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(110, 115, 125));
                g2.setFont(getFont());
                g2.drawString(hint, 0, 30);
                g2.dispose();
            }
        }
    }

    /**
     * Password field with placeholder.
     */
    private static class HintPasswordField extends JPasswordField {

        private final String hint;

        public HintPasswordField(String hint) {
            this.hint = hint;
            setOpaque(false);

            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    repaint();
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    repaint();
                }
            });
        }

        public String getActualPassword() {
            return new String(getPassword());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (getPassword().length == 0 && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(110, 115, 125));
                g2.setFont(getFont());
                g2.drawString(hint, 0, 30);
                g2.dispose();
            }
        }
    }
}