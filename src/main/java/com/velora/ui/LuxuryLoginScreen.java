package com.velora.ui;

import com.velora.theme.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.regex.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Production component-based login built from the supplied design references. */
public final class LuxuryLoginScreen extends JFrame {
    public LuxuryLoginScreen() {
        super("Velora Motors — Manager Login");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setMinimumSize(new Dimension(1120, 700));
        setContentPane(new View(this));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void toggleMaximize() {
        int state = getExtendedState();
        setExtendedState((state & Frame.MAXIMIZED_BOTH) != 0 ? Frame.NORMAL : Frame.MAXIMIZED_BOTH);
    }

    private static final class View extends JPanel {
        private final LuxuryLoginScreen window;
        private final BufferedImage background = image("/assets/backgrounds/velora-dealership.png");
        private final List<HoverCard> hoverCards = new ArrayList<>();
        private final List<PrimaryButton> primaryButtons = new ArrayList<>();
        private final StatsPanel stats = new StatsPanel();
        private final long started = System.nanoTime();

        View(LuxuryLoginScreen window) {
            this.window = window;
            setLayout(new PhysicalViewportLayout());
            JPanel viewport = clear(new JPanel(new BorderLayout()));
            viewport.add(main(), BorderLayout.CENTER);
            viewport.add(footer(), BorderLayout.SOUTH);
            add(viewport);
            javax.swing.Timer timer = new javax.swing.Timer(UIConstants.ANIMATION_DELAY, e -> {
                hoverCards.forEach(HoverCard::animate);
                primaryButtons.forEach(PrimaryButton::animate);
                stats.progress = ease(Math.min(1f, (System.nanoTime() - started) / 1_300_000_000f));
                repaint();
            });
            timer.setCoalesce(true);
            timer.start();
        }

        private JComponent main() {
            JPanel panel = clear(new JPanel(new GridBagLayout()));
            GridBagConstraints c = fill();
            JComponent hero = hero();
            JComponent right = right();
            hero.setMinimumSize(new Dimension(0, 0));
            right.setMinimumSize(new Dimension(0, 0));
            c.gridx = 0; c.weightx = .65; panel.add(hero, c);
            c.gridx = 1; c.weightx = .35; panel.add(right, c);
            return panel;
        }

        private JComponent hero() {
            JPanel hero = clear(new JPanel(new BorderLayout(16, 16)));
            hero.setBorder(new EmptyBorder(24, 42, 16, 20));
            hero.add(heroTop(), BorderLayout.NORTH);
            JPanel stage = clear(new JPanel(new GridBagLayout()));
            GridBagConstraints c = fill();
            c.anchor = GridBagConstraints.NORTH; c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(16, 210, 0, 160);
            GlassPanel sign = new GlassPanel(14, new Color(8, 13, 20, 175));
            sign.setLayout(new FlowLayout(FlowLayout.CENTER, 12, 8));
            sign.add(new BmwLogo(34));
            sign.add(label("VELORA MOTORS", 20, Font.PLAIN, AppColors.WHITE));
            stage.add(sign, c);
            hero.add(stage, BorderLayout.CENTER);
            hero.add(heroBottom(), BorderLayout.SOUTH);
            return hero;
        }

        private JComponent heroTop() {
            JPanel top = clear(new JPanel(new BorderLayout(12, 0)));
            Brand brand = new Brand();
            top.add(brand, BorderLayout.CENTER);
            GlassPanel info = new GlassPanel();
            info.setMinimumSize(new Dimension(0, 0));
            info.setPreferredSize(new Dimension(350, 138));
            info.setMaximumSize(new Dimension(Integer.MAX_VALUE, 145));
            info.setLayout(new GridLayout(1, 4));
            String[][] data = {
                {"shield.svg", "SECURE SYSTEM", "Your data is always", "protected with us."},
                {"calendar.svg", "24/7 SUPPORT", "We're here for you,", "anytime, anywhere."},
                {"star.svg", "PREMIUM EXPERIENCE", "Excellence in every", "drive, every time."},
                {"tag.svg", "LOYALTY REWARDS", "Earn points & unlock", "exclusive benefits."}
            };
            for (String[] item : data) info.add(infoItem(item));
            JPanel infoWrap = clear(new JPanel(new BorderLayout()));
            infoWrap.setBorder(new EmptyBorder(34, 0, 52, 0));
            infoWrap.setPreferredSize(new Dimension(350, 242));
            infoWrap.add(info, BorderLayout.CENTER);
            top.add(infoWrap, BorderLayout.EAST);
            return top;
        }

        private JComponent infoItem(String[] data) {
            JPanel item = clear(new JPanel());
            item.setMinimumSize(new Dimension(0, 0));
            item.setBorder(new EmptyBorder(12, 3, 8, 3));
            item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
            JLabel icon = new JLabel(new SvgAsset(data[0], 25, AppColors.GOLD));
            icon.setAlignmentX(.5f); item.add(icon); item.add(Box.createVerticalStrut(7));
            for (int i = 1; i < data.length; i++) {
                JLabel line = label(data[i], i == 1 ? 8 : 9, i == 1 ? Font.BOLD : Font.PLAIN,
                    i == 1 ? AppColors.GOLD_LIGHT : AppColors.MUTED);
                line.setAlignmentX(.5f); item.add(line); item.add(Box.createVerticalStrut(4));
            }
            return item;
        }

        private JComponent heroBottom() {
            JPanel bottom = clear(new JPanel());
            bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
            GlassPanel features = new GlassPanel();
            features.setPreferredSize(new Dimension(860, 88));
            features.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
            features.setLayout(new GridLayout(1, 4));
            String[][] content = {
                {"car.svg", "LATEST BMW MODELS", "Drive the newest BMW models", "with cutting-edge technology."},
                {"calendar.svg", "FLEXIBLE RENTALS", "Daily, weekly, or monthly", "rentals to fit your schedule."},
                {"shield.svg", "FULL INSURANCE", "All rentals include comprehensive", "insurance coverage."},
                {"tag.svg", "BEST RATES", "Competitive pricing with", "premium service."}
            };
            for (String[] card : content) {
                HoverCard feature = new FeatureCard(card);
                hoverCards.add(feature); features.add(feature);
            }
            bottom.add(features); bottom.add(Box.createVerticalStrut(10));
            JPanel row = clear(new JPanel(new GridBagLayout()));
            GridBagConstraints c = fill();
            HoverCard quote = new QuoteCard(); hoverCards.add(quote);
            quote.setMinimumSize(new Dimension(0, 0));
            stats.setMinimumSize(new Dimension(0, 0));
            c.gridx = 0; c.weightx = .42; row.add(quote, c);
            c.gridx = 1; c.weightx = .58; row.add(stats, c);
            row.setPreferredSize(new Dimension(860, 54));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
            bottom.add(row);
            return bottom;
        }

        private JComponent right() {
            JPanel right = clear(new JPanel());
            right.setBorder(new EmptyBorder(0, 10, 16, 24));
            right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
            right.add(windowButtons());
            LoginCard login = new LoginCard();
            login.setAlignmentX(.5f); right.add(login); right.add(Box.createVerticalStrut(10));
            FeaturedCard featured = new FeaturedCard();
            featured.setAlignmentX(.5f); right.add(featured);
            return right;
        }

        private JComponent windowButtons() {
            JPanel bar = clear(new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)));
            bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            String[] marks = {"—", "□", "×"};
            for (int i = 0; i < marks.length; i++) {
                int action = i;
                JButton button = link(marks[i]);
                button.setPreferredSize(new Dimension(44, 30));
                button.addActionListener(e -> {
                    if (action == 0) window.setState(Frame.ICONIFIED);
                    else if (action == 1) window.toggleMaximize();
                    else window.dispose();
                });
                bar.add(button);
            }
            return bar;
        }

        private JComponent footer() {
            JPanel footer = new JPanel(new GridBagLayout()) {
                @Override protected void paintComponent(Graphics raw) {
                    Graphics2D g = (Graphics2D) raw.create();
                    g.setColor(new Color(4, 8, 13, 242)); g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(AppColors.BORDER); g.drawLine(0, 0, getWidth(), 0); g.dispose();
                }
            };
            footer.setOpaque(false);
            footer.setBorder(new EmptyBorder(10, 40, 10, 40));
            footer.setPreferredSize(new Dimension(100, UIConstants.FOOTER_HEIGHT));
            GridBagConstraints c = fill();
            JPanel company = clear(new JPanel()); company.setLayout(new BoxLayout(company, BoxLayout.Y_AXIS));
            company.add(label("◆   V E L O R A   M O T O R S", 14, Font.PLAIN, AppColors.WHITE));
            company.add(label("      Premium BMW Vehicle Rental System", 11, Font.PLAIN, AppColors.MUTED));
            c.gridx = 0; c.weightx = .35; footer.add(company, c);
            JLabel copyright = label("© 2026 Velora Motors. All rights reserved.", 13, Font.PLAIN, AppColors.MUTED);
            copyright.setHorizontalAlignment(SwingConstants.CENTER);
            c.gridx = 1; c.weightx = .30; footer.add(copyright, c);
            JPanel social = clear(new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0)));
            social.add(label("F O L L O W   U S", 11, Font.PLAIN, AppColors.MUTED));
            for (String icon : new String[]{"f", "◎", "in", "▶"}) social.add(socialButton(icon));
            c.gridx = 2; c.weightx = .35; footer.add(social, c);
            return footer;
        }

        @Override protected void paintComponent(Graphics raw) {
            super.paintComponent(raw);
            Graphics2D g = (Graphics2D) raw.create();
            double scale = Math.max(getWidth() / (double) background.getWidth(), getHeight() / (double) background.getHeight());
            int width = (int) Math.ceil(background.getWidth() * scale);
            int height = (int) Math.ceil(background.getHeight() * scale);
            g.drawImage(background, (getWidth() - width) / 2, (getHeight() - height) / 2, width, height, null);
            g.setPaint(new GradientPaint(0, 0, new Color(2, 6, 12, 45), getWidth(), 0, new Color(1, 4, 8, 135)));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.dispose();
        }

        private final class LoginCard extends GlassPanel {
            private final VeloraTextField username = new VeloraTextField("Enter your username");
            private final VeloraPasswordField password = new VeloraPasswordField("Enter your password");
            private final PrimaryButton signIn = new PrimaryButton("SIGN IN      →");
            private int attempts;

            LoginCard() {
                super(UIConstants.PANEL_ARC, AppColors.PANEL_STRONG);
                setBorder(new EmptyBorder(17, 36, 17, 36));
                setLayout(new GridBagLayout());
                setPreferredSize(new Dimension(510, 500));
                setMinimumSize(new Dimension(0, 465));
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 515));
                GridBagConstraints c = fill();
                c.gridx = 0; c.weightx = 1; c.insets = new Insets(0, 0, 4, 0);
                c.gridy = 0; c.weighty = .12; add(new BmwLogo(58), c);
                c.gridy++; c.weighty = .055; add(center("M A N A G E R   L O G I N", 21, Font.PLAIN, AppColors.GOLD_LIGHT), c);
                c.gridy++; c.weighty = .04; add(center("Welcome back! Please sign in to continue.", 12, Font.PLAIN, AppColors.WHITE), c);
                c.gridy++; c.weighty = .035; add(new Divider(), c);
                c.gridy++; c.weighty = .035; add(label("USERNAME", 11, Font.PLAIN, AppColors.WHITE), c);
                c.gridy++; c.weighty = .08; add(fieldWrap(username, "user.svg"), c);
                c.gridy++; c.weighty = .035; add(label("PASSWORD", 11, Font.PLAIN, AppColors.WHITE), c);
                c.gridy++; c.weighty = .08; add(fieldWrap(password, "lock.svg"), c);
                JPanel options = clear(new JPanel(new BorderLayout()));
                JCheckBox remember = new JCheckBox("Remember Me", true);
                remember.setOpaque(false); remember.setForeground(AppColors.WHITE); remember.setFont(AppFonts.body(11));
                JButton forgot = link("Forgot Password?"); forgot.setForeground(AppColors.GOLD_LIGHT); forgot.setFont(AppFonts.body(10));
                options.add(remember, BorderLayout.WEST); options.add(forgot, BorderLayout.EAST);
                c.gridy++; c.weighty = .055; add(options, c);
                c.gridy++; c.weighty = .085; add(signIn, c);
                c.gridy++; c.weighty = .11; add(new Notice(false), c);
                c.gridy++; c.weighty = .12; add(new Notice(true), c);
                primaryButtons.add(signIn);
                signIn.addActionListener(e -> authenticate());
                password.addActionListener(e -> authenticate());
            }

            private JComponent fieldWrap(JComponent field, String icon) {
                JPanel wrap = clear(new JPanel(new BorderLayout()));
                JLabel leading = new JLabel(new SvgAsset(icon, 20, AppColors.MUTED));
                leading.setBorder(new EmptyBorder(0, 15, 0, 0));
                wrap.add(field); wrap.add(leading, BorderLayout.WEST);
                return wrap;
            }

            private void authenticate() {
                if ("manager".equalsIgnoreCase(username.getText().trim())
                    && "velora2026".equals(new String(password.getPassword()))) {
                    JOptionPane.showMessageDialog(this, "Welcome to Velora Motors Management System.");
                } else if (++attempts >= 3) signIn.setEnabled(false);
            }
        }

        private final class FeaturedCard extends GlassPanel {
            FeaturedCard() {
                setLayout(new GridBagLayout());
                setPreferredSize(new Dimension(510, 150));
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
                JPanel copy = clear(new JPanel());
                copy.setBorder(new EmptyBorder(16, 20, 12, 8));
                copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
                copy.add(label("FEATURED MODEL", 9, Font.PLAIN, AppColors.GOLD_LIGHT));
                copy.add(Box.createVerticalStrut(5));
                copy.add(label("BMW i8 ROADSTER", 16, Font.BOLD, AppColors.WHITE));
                copy.add(Box.createVerticalStrut(6));
                copy.add(label("Future meets performance.", 10, Font.PLAIN, AppColors.MUTED));
                copy.add(label("Rent the extraordinary.", 10, Font.PLAIN, AppColors.MUTED));
                copy.add(Box.createVerticalGlue());
                JButton explore = link("EXPLORE NOW   ›");
                explore.setBorder(BorderFactory.createLineBorder(new Color(203, 163, 106, 90)));
                copy.add(explore);
                GridBagConstraints c = fill();
                c.gridx = 0; c.weightx = .48; add(copy, c);
                c.gridx = 1; c.weightx = .52; add(new CarCrop(background), c);
            }
        }

        private final class Brand extends JComponent {
            Brand() { setPreferredSize(new Dimension(640, 242)); setMinimumSize(new Dimension(0, 0)); }
            @Override protected void paintComponent(Graphics raw) {
                Graphics2D g = (Graphics2D) raw.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int center = Math.min(getWidth() / 2, 390);
                double pulse = (Math.sin(System.nanoTime() / 1_200_000_000d) + 1) / 2;
                g.setColor(new Color(203, 163, 106, (int) (15 + pulse * 18)));
                g.fillOval(center - 110, 4, 220, 78);
                drawWings(g, center, 29);
                drawCentered(g, "VELORA MOTORS", center, 130, 49, Font.PLAIN, AppColors.WHITE, "Serif");
                drawCentered(g, "P R E M I U M   B M W   V E H I C L E   R E N T A L   S Y S T E M",
                    center, 172, 13, Font.PLAIN, AppColors.GOLD_LIGHT, "SansSerif");
                g.setColor(new Color(203, 163, 106, 155));
                g.drawLine(center - 260, 193, center - 10, 193); g.drawLine(center + 10, 193, center + 260, 193);
                drawCentered(g, "D R I V E   L U X U R Y .   D R I V E   B M W .",
                    center, 224, 15, Font.PLAIN, AppColors.WHITE, "SansSerif");
                g.dispose();
            }
        }

        private final class BmwLogo extends JComponent {
            private final int diameter;
            BmwLogo(int diameter) { this.diameter = diameter; setPreferredSize(new Dimension(diameter + 10, diameter + 10)); }
            @Override protected void paintComponent(Graphics raw) {
                Graphics2D g = (Graphics2D) raw.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                double angle = Math.toRadians(5 * Math.sin(System.nanoTime() / 4_500_000_000d * Math.PI));
                g.rotate(angle, getWidth() / 2d, getHeight() / 2d);
                int radius = Math.min(diameter, Math.min(getWidth(), getHeight())) / 2;
                int cx = getWidth() / 2, cy = getHeight() / 2;
                g.setColor(AppColors.WHITE); g.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
                g.setColor(AppColors.BACKGROUND); g.fillOval(cx - radius + 3, cy - radius + 3, radius * 2 - 6, radius * 2 - 6);
                int d = radius * 2 - 18, x = cx - radius + 9, y = cy - radius + 9;
                g.setColor(AppColors.WHITE); g.fillArc(x, y, d, d, 0, 90); g.fillArc(x, y, d, d, 180, 90);
                g.setColor(new Color(22, 127, 198)); g.fillArc(x, y, d, d, 90, 90); g.fillArc(x, y, d, d, 270, 90);
                g.dispose();
            }
        }

        private class HoverCard extends GlassPanel {
            private float hover;
            HoverCard() {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
                });
            }
            void animate() {
                float target = getMousePosition() == null ? 0f : 1f;
                hover += (target - hover) * .1f; repaint();
            }
            @Override protected void paintComponent(Graphics raw) {
                Graphics2D g = (Graphics2D) raw.create();
                g.translate(0, -Math.round(7 * hover));
                g.setColor(new Color(203, 163, 106, Math.round(25 + 60 * hover)));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), UIConstants.CARD_ARC, UIConstants.CARD_ARC);
                g.dispose();
                super.paintComponent(raw);
            }
        }

        private final class FeatureCard extends HoverCard {
            FeatureCard(String[] data) {
                setBorder(new EmptyBorder(18, 14, 13, 12));
                setLayout(new BorderLayout(11, 0));
                add(new JLabel(new SvgAsset(data[0], 27, AppColors.GOLD)), BorderLayout.WEST);
                JPanel copy = clear(new JPanel()); copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
                copy.add(label(data[1], 11, Font.BOLD, AppColors.GOLD_LIGHT)); copy.add(Box.createVerticalStrut(8));
                copy.add(label(data[2], 10, Font.PLAIN, AppColors.WHITE));
                copy.add(label(data[3], 10, Font.PLAIN, AppColors.WHITE));
                add(copy);
            }
        }

        private final class QuoteCard extends HoverCard {
            QuoteCard() {
                setBorder(new EmptyBorder(12, 18, 10, 18));
                setLayout(new BorderLayout(10, 0));
                add(label("“", 36, Font.BOLD, AppColors.GOLD), BorderLayout.WEST);
                JPanel copy = clear(new JPanel()); copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
                copy.add(label("The Ultimate Driving Experience.", 12, Font.ITALIC, AppColors.WHITE));
                copy.add(label("Now Available For Every Journey.", 12, Font.ITALIC, AppColors.WHITE));
                add(copy);
            }
        }

        private final class StatsPanel extends GlassPanel {
            private float progress;
            @Override protected void paintComponent(Graphics raw) {
                super.paintComponent(raw);
                Graphics2D g = (Graphics2D) raw.create();
                String[] values = {(int) (250 * progress) + "+", progress > .995 ? "15K+" : String.format("%.1fK+", 15 * progress),
                    (int) (25 * progress) + "+", String.format("%.1f/5", 4.9 * progress)};
                String[] captions = {"Premium Vehicles", "Satisfied Customers", "Locations", "Customer Rating"};
                int cell = getWidth() / 4;
                for (int i = 0; i < 4; i++) {
                    g.setFont(AppFonts.bodyBold(18)); g.setColor(AppColors.WHITE); drawCentered(g, values[i], i * cell + cell / 2, 28);
                    g.setFont(AppFonts.body(9)); g.setColor(new Color(92, 126, 180)); drawCentered(g, captions[i], i * cell + cell / 2, 48);
                }
                g.dispose();
            }
        }

        private final class Notice extends JComponent {
            private final boolean error;
            Notice(boolean error) {
                this.error = error;
                setPreferredSize(new Dimension(100, error ? 58 : 50));
                setMinimumSize(new Dimension(80, error ? 52 : 46));
            }
            @Override protected void paintComponent(Graphics raw) {
                Graphics2D g = (Graphics2D) raw.create();
                g.setColor(error ? new Color(90, 9, 14, 215) : new Color(8, 14, 20, 220));
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g.setColor(error ? new Color(235, 72, 78) : AppColors.WHITE);
                g.setFont(AppFonts.body(10));
                g.drawString(error ? "Too many failed attempts." : "For your security, session expires", 42, 22);
                g.drawString(error ? "Wait 30 seconds before trying again." : "after 10 minutes of inactivity.", 42, 40);
                g.dispose();
            }
        }

        private final class Divider extends JComponent {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(203, 163, 106, 170));
                g.drawLine(getWidth() / 2 - 28, getHeight() / 2, getWidth() / 2 + 28, getHeight() / 2);
            }
        }

        private static final class CarCrop extends JComponent {
            private final BufferedImage source;
            CarCrop(BufferedImage source) { this.source = source; }
            @Override protected void paintComponent(Graphics g) {
                Shape old = g.getClip();
                g.setClip(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
                g.drawImage(source, 0, 0, getWidth(), getHeight(), source.getWidth() / 5, source.getHeight() / 2,
                    source.getWidth() * 4 / 5, source.getHeight(), null);
                g.setClip(old);
            }
        }

        private static JButton socialButton(String text) {
            JButton button = link(text);
            button.setPreferredSize(new Dimension(34, 34));
            button.setForeground(AppColors.GOLD_LIGHT);
            button.setBorder(BorderFactory.createLineBorder(new Color(203, 163, 106, 75)));
            return button;
        }

        private static JPanel clear(JPanel panel) { panel.setOpaque(false); return panel; }
        private static JLabel label(String value, int size, int style, Color color) {
            JLabel label = new JLabel(value); label.setForeground(color); label.setFont(new Font("Inter", style, size)); return label;
        }
        private static JLabel center(String value, int size, int style, Color color) {
            JLabel label = label(value, size, style, color); label.setHorizontalAlignment(SwingConstants.CENTER); return label;
        }
        private static JButton link(String text) {
            JButton button = new JButton(text); button.setOpaque(false); button.setContentAreaFilled(false);
            button.setBorderPainted(false); button.setFocusPainted(false); button.setForeground(AppColors.WHITE);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return button;
        }
        private static GridBagConstraints fill() {
            GridBagConstraints c = new GridBagConstraints(); c.fill = GridBagConstraints.BOTH; c.weighty = 1; return c;
        }
        private static float ease(float value) { return 1 - (1 - value) * (1 - value) * (1 - value); }
        private static BufferedImage image(String path) {
            try { return ImageIO.read(LuxuryLoginScreen.class.getResource(path)); }
            catch (IOException | IllegalArgumentException e) { throw new IllegalStateException("Missing asset " + path, e); }
        }
        private static void drawCentered(Graphics2D g, String value, int x, int y) {
            g.drawString(value, x - g.getFontMetrics().stringWidth(value) / 2, y);
        }
        private static void drawCentered(Graphics2D g, String value, int x, int y, int size, int style, Color color, String family) {
            g.setFont(new Font(family, style, size)); g.setColor(color); drawCentered(g, value, x, y);
        }
        private static void drawWings(Graphics2D g, int center, int y) {
            g.setColor(AppColors.GOLD); g.setStroke(new BasicStroke(4));
            Path2D path = new Path2D.Double();
            path.moveTo(center, y + 55); path.lineTo(center - 37, y); path.lineTo(center - 105, y);
            path.moveTo(center, y + 55); path.lineTo(center + 37, y); path.lineTo(center + 105, y);
            path.moveTo(center - 28, y + 13); path.lineTo(center - 86, y + 13);
            path.moveTo(center + 28, y + 13); path.lineTo(center + 86, y + 13);
            path.moveTo(center - 19, y + 26); path.lineTo(center - 63, y + 26);
            path.moveTo(center + 19, y + 26); path.lineTo(center + 63, y + 26);
            g.draw(path);
        }
    }

    /** Normalizes Windows DPI virtualization once; the UI inside uses layouts. */
    private static final class PhysicalViewportLayout implements LayoutManager {
        public void addLayoutComponent(String name, Component component) {}
        public void removeLayoutComponent(Component component) {}
        public Dimension preferredLayoutSize(Container parent) { return parent.getSize(); }
        public Dimension minimumLayoutSize(Container parent) { return new Dimension(1120, 700); }
        public void layoutContainer(Container parent) {
            if (parent.getComponentCount() == 0) return;
            double dpi = Math.max(1d, Toolkit.getDefaultToolkit().getScreenResolution() / 96d);
            parent.getComponent(0).setBounds(0, 0,
                (int) Math.round(parent.getWidth() / dpi),
                (int) Math.round(parent.getHeight() / dpi));
        }
    }

    /** Renderer for the project's simple SVG line assets. */
    private static final class SvgAsset implements Icon {
        private final int size;
        private final Color color;
        private final List<Shape> shapes = new ArrayList<>();

        SvgAsset(String file, int size, Color color) {
            this.size = size; this.color = color;
            try (InputStream stream = LuxuryLoginScreen.class.getResourceAsStream("/assets/icons/" + file)) {
                String svg = new String(Objects.requireNonNull(stream).readAllBytes(), StandardCharsets.UTF_8);
                parse(svg);
            } catch (Exception ignored) {}
        }

        private void parse(String svg) {
            Matcher matcher = Pattern.compile("<(path|polyline)[^>]*points=\"([^\"]+)\"").matcher(svg);
            while (matcher.find()) {
                Path2D path = new Path2D.Double();
                String[] points = matcher.group(2).trim().split("\\s+");
                for (int i = 0; i < points.length; i++) {
                    String[] point = points[i].split(",");
                    double x = Double.parseDouble(point[0]), y = Double.parseDouble(point[1]);
                    if (i == 0) path.moveTo(x, y); else path.lineTo(x, y);
                }
                if ("path".equals(matcher.group(1))) path.closePath();
                shapes.add(path);
            }
            matcher = Pattern.compile("<line x1=\"([^\"]+)\" y1=\"([^\"]+)\" x2=\"([^\"]+)\" y2=\"([^\"]+)\"").matcher(svg);
            while (matcher.find()) shapes.add(new Line2D.Double(number(matcher,1),number(matcher,2),number(matcher,3),number(matcher,4)));
            matcher = Pattern.compile("<circle cx=\"([^\"]+)\" cy=\"([^\"]+)\" r=\"([^\"]+)\"").matcher(svg);
            while (matcher.find()) {
                double r = number(matcher,3);
                shapes.add(new Ellipse2D.Double(number(matcher,1)-r,number(matcher,2)-r,r*2,r*2));
            }
            matcher = Pattern.compile("<rect x=\"([^\"]+)\" y=\"([^\"]+)\" width=\"([^\"]+)\" height=\"([^\"]+)\"").matcher(svg);
            while (matcher.find()) shapes.add(new RoundRectangle2D.Double(number(matcher,1),number(matcher,2),number(matcher,3),number(matcher,4),2,2));
        }

        private static double number(Matcher matcher, int index) { return Double.parseDouble(matcher.group(index)); }
        public int getIconWidth() { return size; }
        public int getIconHeight() { return size; }
        public void paintIcon(Component component, Graphics raw, int x, int y) {
            Graphics2D g = (Graphics2D) raw.create();
            g.translate(x, y); g.scale(size / 24d, size / 24d);
            g.setColor(color); g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            shapes.forEach(g::draw); g.dispose();
        }
    }
}
