package com.velora.ui;

import com.velora.authentication.Customer;
import com.velora.service.AuthenticationService;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public final class LoginScreen extends JFrame {

    public LoginScreen() {
        super("Velora Motors - Manager Login");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setMinimumSize(new Dimension(1200, 720));
        setContentPane(new VeloraCanvas(this));
        setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale", "1.0");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            new LoginScreen().setVisible(true);
        });
    }

    private void toggleMaximize() {
        int state = getExtendedState();
        setExtendedState((state & Frame.MAXIMIZED_BOTH) != 0 ? Frame.NORMAL : Frame.MAXIMIZED_BOTH);
    }

    private static final class VeloraCanvas extends JPanel {

        private static final Color GOLD = new Color(214, 168, 91);
        private static final Color PALE = new Color(236, 203, 157);
        private static final Color WHITE = new Color(248, 248, 248);
        private static final Color MUTED = new Color(170, 176, 186);
        private static final Color BLUE_TEXT = new Color(106, 140, 192);
        private static final char PASSWORD_BULLET = '•';
        private static final int[] REJECTION_SHAKE = {0, -7, 7, -6, 6, -4, 4, -2, 2, 0};

        private final LoginScreen frame;
        private final AuthenticationService authenticationService = new AuthenticationService();

        private final BufferedImage bg;
        private final BufferedImage bmwLogo;
        private final BufferedImage veloraLogo;
        private final BufferedImage featuredCar;

        private final RoundTextField username = new RoundTextField("Enter your username", false);
        private final RoundTextField password = new RoundTextField("Enter your password", true);

        private final JButton createAccount = ghostButton("Create Account");
        private final JButton forgot = ghostButton("Forgot Password?");
        private final JButton signIn = new GoldButton("SIGN IN      →");
        private final JButton eye = new EyeButton();
        private final JButton explore = new ExploreButton("EXPLORE NOW     ›");

        private final JButton min = topButton("—");
        private final JButton max = topButton("□");
        private final JButton close = topButton("×");

        private final long start = System.nanoTime();

        private float intro;
        private float statProgress;
        private float pulse;
        private float shimmer;
        private float floatOffset;

        private int failed;
        private int lockSeconds;
        private boolean passwordVisible;
        private Timer rejectionShakeTimer;
        private int rejectionShakeStep;
        private int passwordBaseX;
        private int eyeBaseX;

        VeloraCanvas(LoginScreen frame) {
            this.frame = frame;

            bg = loadFirst(
                    "/assets/backgrounds/velora-dealership.png",
                    "/images/velora-dealership.png",
                    "/assets/backgrounds/background.png",
                    "/images/background.png"
            );

            bmwLogo = loadFirst(
                    "/images/bmw-logo-reference.png",
                    "/assets/icons/bmw-logo.png",
                    "/assets/icons/bmw.png",
                    "/images/bmw-logo.png",
                    "/icons/bmw-logo.png"
            );

            veloraLogo = loadFirst(
                    "/assets/icons/velora-logo.png",
                    "/assets/icons/velora-wing.png",
                    "/images/velora-logo.png",
                    "/icons/velora-logo.png"
            );

            featuredCar = loadFirst(
                    "/images/featured-roadster-clean.png",
                    "/assets/backgrounds/bmw-i8-roadster.png",
                    "/assets/backgrounds/featured-car.png",
                    "/images/bmw-i8-roadster.png",
                    "/images/featured-car.png"
            );

            setLayout(null);
            setOpaque(true);

            createAccount.setForeground(PALE);
            createAccount.setFont(new Font("Segoe UI", Font.BOLD, 11));
            createAccount.setHorizontalAlignment(SwingConstants.LEFT);
            createAccount.setToolTipText("Create a new Velora account");
            createAccount.addActionListener(e -> showCreateAccount());

            forgot.setForeground(PALE);
            forgot.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            forgot.addActionListener(e -> showResetPassword());

            explore.setForeground(PALE);
            explore.setFont(new Font("Segoe UI", Font.BOLD, 11));
            explore.setToolTipText("Explore the BMW i8 Roadster");
            explore.addActionListener(e -> showFeaturedModel());

            eye.putClientProperty("visible", Boolean.FALSE);
            eye.setToolTipText("Show password");

            signIn.addActionListener(e -> authenticate());
            password.addActionListener(e -> authenticate());

            eye.addActionListener(e -> {
                passwordVisible = !passwordVisible;
                password.setEchoChar(passwordVisible ? (char) 0 : PASSWORD_BULLET);
                eye.putClientProperty("visible", passwordVisible);
                eye.setToolTipText(passwordVisible ? "Hide password" : "Show password");
                password.requestFocusInWindow();
                password.repaint();
                eye.repaint();
            });

            min.addActionListener(e -> frame.setState(Frame.ICONIFIED));
            max.addActionListener(e -> frame.toggleMaximize());
            close.addActionListener(e -> frame.dispose());

            add(username);
            add(password);
            add(eye);
            add(createAccount);
            add(forgot);
            add(signIn);
            add(explore);
            add(min);
            add(max);
            add(close);

            // خلي زر العين فوق حقل كلمة السر حتى يستقبل الضغطات أكيد.
            setComponentZOrder(eye, 0);

            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    layoutControls();
                }
            });

            Timer timer = new Timer(16, e -> {
                float seconds = (System.nanoTime() - start) / 1_000_000_000f;
                intro = ease(Math.min(1f, seconds / .85f));
                statProgress = ease(Math.min(1f, seconds / 1.45f));
                pulse = (float) ((Math.sin(seconds * 2.4) + 1.0) * .5);
                shimmer = (seconds % 5.5f) / 5.5f;
                floatOffset = (float) Math.sin(seconds * 1.35f) * 2.2f;
                repaint();
            });

            timer.setCoalesce(true);
            timer.start();
        }

        private void authenticate() {
            if (lockSeconds > 0) {
                return;
            }

            String u = username.getRealText().trim();
            String p = password.getRealText();

            java.util.Optional<Customer> account = authenticationService.authenticateCustomer(
                    u,
                    p.toCharArray()
            );

            if (account.isPresent()) {
                Customer signedInAccount = account.get();
                new SplashScreen(signedInAccount).setVisible(true);
                frame.dispose();
                return;
            }

            failed++;
            shakePasswordField();

            if (failed >= 3) {
                lockSeconds = 30;
                signIn.setEnabled(false);
                username.setEnabled(false);
                password.setEnabled(false);
                eye.setEnabled(false);

                Timer t = new Timer(1000, null);

                t.addActionListener(e -> {
                    lockSeconds--;

                    if (lockSeconds <= 0) {
                        failed = 0;
                        signIn.setEnabled(true);
                        username.setEnabled(true);
                        password.setEnabled(true);
                        eye.setEnabled(true);
                        username.requestFocusInWindow();
                        t.stop();
                    }

                    repaint();
                });

                t.start();
            }
        }

        private void shakePasswordField() {
            if (rejectionShakeTimer != null && rejectionShakeTimer.isRunning()) {
                rejectionShakeTimer.stop();
                password.setLocation(passwordBaseX, password.getY());
                eye.setLocation(eyeBaseX, eye.getY());
            }

            passwordBaseX = password.getX();
            eyeBaseX = eye.getX();
            rejectionShakeStep = 0;

            rejectionShakeTimer = new Timer(28, e -> {
                if (rejectionShakeStep >= REJECTION_SHAKE.length) {
                    password.setLocation(passwordBaseX, password.getY());
                    eye.setLocation(eyeBaseX, eye.getY());
                    rejectionShakeTimer.stop();
                    return;
                }

                int offset = sw(REJECTION_SHAKE[rejectionShakeStep++]);
                password.setLocation(passwordBaseX + offset, password.getY());
                eye.setLocation(eyeBaseX + offset, eye.getY());
            });
            rejectionShakeTimer.setCoalesce(true);
            rejectionShakeTimer.start();
        }

        private void showFeaturedModel() {
            JPanel details = new JPanel();
            details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
            details.setBackground(new Color(7, 12, 19));
            details.setBorder(new EmptyBorder(14, 18, 10, 18));

            JLabel title = new JLabel("BMW i8 ROADSTER");
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            title.setForeground(PALE);
            title.setFont(new Font("Segoe UI", Font.BOLD, 22));

            JLabel description = new JLabel(
                    "<html><div style='width:340px;color:#E9EDF2'>"
                    + "Future meets performance.<br><br>"
                    + "<span style='color:#AAB0BA'>A premium open-top hybrid experience, "
                    + "featured exclusively by Velora Motors. Sign in to view availability "
                    + "and rental details.</span></div></html>"
            );
            description.setAlignmentX(Component.LEFT_ALIGNMENT);
            description.setBorder(new EmptyBorder(12, 0, 4, 0));

            details.add(title);
            details.add(description);

            JOptionPane.showMessageDialog(
                    frame,
                    details,
                    "Featured Model",
                    JOptionPane.PLAIN_MESSAGE
            );
        }

        private void showCreateAccount() {
            CreateAccountDialog dialog = new CreateAccountDialog(
                    frame,
                    authenticationService,
                    email -> {
                username.setText(email);
                password.setText("");
                password.requestFocusInWindow();
            });
            dialog.setVisible(true);
        }

        private void showResetPassword() {
            ResetPasswordDialog dialog = new ResetPasswordDialog(
                    frame,
                    authenticationService,
                    email -> {
                username.setText(email);
                password.setText("");
                password.requestFocusInWindow();
            });
            dialog.setVisible(true);
        }

        private void layoutControls() {
            int w = getWidth();
            int h = getHeight();

            if (w <= 0 || h <= 0) {
                return;
            }

            int margin = sideMargin();
            int rightW = rightW();
            int rightX = w - margin - rightW;
            int rightY = topY();
            int loginH = loginH();

            int ctrlY = Math.max(3, rightY - sh(30));
            int ctrlW = sw(36);

            close.setBounds(w - sw(50), ctrlY, ctrlW, sh(24));
            max.setBounds(w - sw(86), ctrlY, ctrlW, sh(24));
            min.setBounds(w - sw(122), ctrlY, ctrlW, sh(24));

            int fieldMargin = (int) (rightW * .105);
            int px = rightX + fieldMargin;
            int fieldW = rightW - fieldMargin * 2;

            int fieldH = clamp((int) (h * .050), sh(38), sh(50));
            int buttonH = sh(50);
            int securityH = sh(58);
            int errorH = sh(74);

            int cardBottom = rightY + loginH;

            int errorY = cardBottom - errorH - sh(22);
            int securityY = errorY - securityH - sh(14);
            int buttonY = securityY - buttonH - sh(16);
            int optionsY = buttonY - sh(34);
            int passY = optionsY - fieldH - sh(20);
            int userY = passY - fieldH - sh(42);

            int minUserY = rightY + sh(218);

            if (userY < minUserY) {
                int diff = minUserY - userY;

                userY += diff;
                passY += diff;
                optionsY += diff;
                buttonY += diff;
                securityY += diff;
                errorY += diff;

                int overflow = errorY + errorH - (cardBottom - sh(18));

                if (overflow > 0) {
                    userY -= overflow;
                    passY -= overflow;
                    optionsY -= overflow;
                    buttonY -= overflow;
                    securityY -= overflow;
                    errorY -= overflow;
                }
            }

            username.setBounds(px, userY, fieldW, fieldH);
            password.setBounds(px, passY, fieldW, fieldH);
            eye.setBounds(px + fieldW - sw(52), passY + sh(6), sw(40), fieldH - sh(12));
            setComponentZOrder(eye, 0);

            createAccount.setBounds(px, optionsY, fieldW / 2, sh(26));
            forgot.setBounds(px + fieldW / 2, optionsY, fieldW / 2, sh(26));

            signIn.setBounds(px, buttonY, fieldW, buttonH);

            int featuredY = rightY + loginH + sh(10);
            int featuredH = Math.max(sh(128), h - footerH() - featuredY - sh(24));

            int exploreW = Math.max(sw(118), Math.round(rightW * .225f));
            int exploreH = Math.max(sh(27), Math.round(featuredH * .19f));
            int exploreX = rightX + sw(32);
            int exploreY = featuredY + featuredH - exploreH - sh(10);
            explore.setBounds(exploreX, exploreY, exploreW, exploreH);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            super.paintComponent(raw);

            Graphics2D g = (Graphics2D) raw.create();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int w = getWidth();
            int h = getHeight();

            drawBackground(g, w, h);
            drawAnimatedLighting(g, w, h);
            drawHero(g, w, h);
            drawLoginCard(g, w, h);
            drawFeatured(g, w, h);
            drawFooter(g, w, h);

            g.dispose();
        }

        private void drawBackground(Graphics2D g, int w, int h) {
            if (bg != null) {
                drawCover(g, bg, 0, 0, w, h);
            } else {
                g.setPaint(new GradientPaint(0, 0, new Color(2, 6, 12), w, h, Color.BLACK));
                g.fillRect(0, 0, w, h);
            }

            g.setPaint(new GradientPaint(
                    0, 0, new Color(0, 0, 0, 35),
                    w, 0, new Color(0, 0, 0, 155)
            ));
            g.fillRect(0, 0, w, h);

            g.setPaint(new GradientPaint(
                    0, 0, new Color(0, 0, 0, 0),
                    0, h, new Color(0, 0, 0, 120)
            ));
            g.fillRect(0, 0, w, h);
        }

        private void drawAnimatedLighting(Graphics2D g, int w, int h) {
            Graphics2D gg = (Graphics2D) g.create();
            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            float a = Math.max(0f, Math.min(1f, intro));
            int sweepX = (int) (-w * .35 + shimmer * w * 1.75);
            int sweepY = (int) (h * .36);

            // لمعة متحركة واضحة فوق واجهة المحل والسيارات
            gg.setComposite(AlphaComposite.SrcOver.derive(.11f * a));
            gg.setStroke(new BasicStroke(Math.max(1f, ss(22f)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            gg.setColor(new Color(214, 168, 91, 128));
            gg.drawLine(sweepX, sweepY + sh(96), sweepX + sw(320), sweepY - sh(6));

            gg.setComposite(AlphaComposite.SrcOver.derive(.20f * a));
            gg.setStroke(new BasicStroke(Math.max(1f, ss(2.2f)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            gg.setColor(new Color(245, 206, 142, 120));
            gg.drawLine(sw(110), sh(333), sw(675), sh(311));
            gg.drawLine(sw(705), sh(316), sw(965), sh(322));

            // هالات نبض خفيفة حول اللوجو العلوي وشعار BMW على الواجهة
            gg.setComposite(AlphaComposite.SrcOver.derive((.10f + .07f * pulse) * a));
            gg.setColor(new Color(214, 168, 91, 58));
            gg.fillOval(sw(238), sh(18), sw(220), sh(72));

            // هالة شعار BMW على الواجهة صارت أصغر وأنظف حتى ما تبين كبقعة كبيرة
            gg.setComposite(AlphaComposite.SrcOver.derive((.055f + .045f * pulse) * a));
            gg.setColor(new Color(214, 168, 91, 46));
            gg.fillOval(sw(735), sh(218), sw(108), sh(64));

            // خطوط ذهبية سريعة تعطي إحساس حركة بدون فوضى
            gg.setComposite(AlphaComposite.SrcOver.derive(.16f * a));
            gg.setStroke(new BasicStroke(Math.max(1f, ss(1.1f)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int dashX = (int) (-sw(260) + shimmer * (w + sw(520)));
            gg.setColor(new Color(255, 224, 170, 120));
            gg.drawLine(dashX, sh(198), dashX + sw(135), sh(198));
            gg.drawLine(dashX - sw(180), sh(654), dashX - sw(35), sh(654));

            gg.dispose();
        }

        private void drawHero(Graphics2D g, int w, int h) {
            Composite old = g.getComposite();
            g.setComposite(AlphaComposite.SrcOver.derive(Math.max(.08f, intro)));

            int margin = sideMargin();
            int gap = sw(38);

            int rightW = rightW();
            int rightX = w - margin - rightW;

            int leftX = margin;
            int leftW = rightX - gap - leftX;

            if (leftW < sw(640)) {
                leftW = rightX - leftX - sw(22);
            }

            // نفس ترتيب الصورة: لوجو Velora كبير فوق اليسار
            int brandW = Math.min(sw(610), Math.max(sw(450), (int) (leftW * .50)));
            int brandX = leftX + sw(34);
            int brandY = sh(22) + Math.round(floatOffset);
            drawBrand(g, brandX, brandY, brandW);

            // صندوق المميزات فوق يمين، قريب من العنوان
            int trustW = Math.min(sw(420), Math.max(sw(325), (int) (leftW * .30)));
            int trustX = rightX - trustW - sw(20);
            drawTrustPanel(g, trustX, sh(48), trustW, sh(104));

            // اسم المحل فوق خط الإضاءة الذهبي الأصلي الموجود على واجهة المبنى
            int signW = Math.min(sw(300), Math.max(sw(260), (int) (leftW * .265)));
            int signH = sh(48);
            int signX = leftX + (int) (leftW * .335);
            int signY = sh(246);
            drawCenterSign(g, signX, signY, signW, signH);

            int footerTop = h - footerH();

            int featureX = leftX;
            int featureW = leftW;
            int featureH = sh(100);

            int quoteH = sh(72);
            int quoteY = footerTop - quoteH - sh(18);
            int featureY = quoteY - featureH - sh(14);

            if (featureY < (int) (h * .640)) {
                featureY = (int) (h * .640);
                quoteY = featureY + featureH + sh(14);
            }

            drawFeatures(g, featureX, featureY, featureW, featureH);

            int quoteW = (int) (featureW * .42);

            drawQuote(g, featureX, quoteY, quoteW, quoteH);
            drawStats(g, featureX + quoteW, quoteY, featureW - quoteW, quoteH);

            g.setComposite(old);
        }

        private void drawBrand(Graphics2D g, int x, int y, int bw) {
            int cx = x + bw / 2;

            // شعار Velora في الأعلى مثل الصورة
            int logoW = Math.min((int) (bw * .28), sw(200));
            int logoH = sh(68);

            if (veloraLogo != null) {
                drawContain(g, veloraLogo, cx - logoW / 2, y - sh(2), logoW, logoH);
            } else {
                drawWings(g, cx, y + sh(4), Math.max(.58, Math.min(.86, bw / 760.0)));
            }

            drawFitCentered(
                    g,
                    "VELORA MOTORS",
                    cx,
                    y + sh(112),
                    bw,
                    new Font("Serif", Font.PLAIN, sf(44)),
                    WHITE
            );

            drawFitCentered(
                    g,
                    "P R E M I U M   B M W   V E H I C L E   R E N T A L   S Y S T E M",
                    cx,
                    y + sh(144),
                    bw,
                    new Font("Segoe UI", Font.PLAIN, sf(12)),
                    PALE
            );

            // خطين ذهبيين مع ماسة بالنص
            int lineY = y + sh(170);
            int left1 = cx - (int) (bw * .36);
            int left2 = cx - sw(20);
            int right1 = cx + sw(20);
            int right2 = cx + (int) (bw * .36);

            g.setStroke(new BasicStroke(ss(1.7f)));
            g.setColor(new Color(214, 168, 91, 165));
            g.drawLine(left1, lineY, left2, lineY);
            g.drawLine(right1, lineY, right2, lineY);

            Path2D diamond = new Path2D.Double();
            diamond.moveTo(cx, lineY - sh(6));
            diamond.lineTo(cx + sw(6), lineY);
            diamond.lineTo(cx, lineY + sh(6));
            diamond.lineTo(cx - sw(6), lineY);
            diamond.closePath();
            g.draw(diamond);

            drawLuxuryLine(g, cx, y + sh(202), bw);
        }

        private void drawCenterSign(Graphics2D g, int x, int y, int w, int h) {
            Graphics2D gg = (Graphics2D) g.create();

            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            gg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            double angle = Math.toRadians(-3.2);
            Graphics2D sign = (Graphics2D) gg.create();
            sign.rotate(angle, x + w / 2.0, y + h / 2.0);

            String title = "VELORA MOTORS";
            Font titleFont = fitFont(
                    sign,
                    title,
                    new Font("Times New Roman", Font.PLAIN, sf(24)),
                    w - sw(16)
            );
            sign.setFont(titleFont);
            FontMetrics fm = sign.getFontMetrics();
            int textW = fm.stringWidth(title);
            int textX = x + (w - textW) / 2;
            int titleY = y + (h - fm.getHeight()) / 2 + fm.getAscent();

            // Very soft halo: the letters stay thin and printed on the facade.
            sign.setComposite(AlphaComposite.SrcOver.derive(.10f + .03f * pulse));
            sign.setColor(new Color(255, 245, 226));
            sign.drawString(title, textX - sw(1), titleY);
            sign.drawString(title, textX + sw(1), titleY);
            sign.drawString(title, textX, titleY - sh(1));
            sign.drawString(title, textX, titleY + sh(1));

            sign.setComposite(AlphaComposite.SrcOver);
            sign.setColor(new Color(0, 0, 0, 190));
            sign.drawString(title, textX + sw(1), titleY + sh(2));

            sign.setPaint(new GradientPaint(
                    0, titleY - fm.getAscent(), new Color(255, 255, 252),
                    0, titleY + fm.getDescent(), new Color(224, 214, 195)
            ));
            sign.drawString(title, textX, titleY);
            sign.dispose();

            // Keep the BMW emblem on its original architectural column.
            int logoSize = sh(40);
            int logoX = x + w + sw(158);
            int logoY = y - sh(1);

            drawBMW(gg, logoX, logoY, logoSize);

            gg.dispose();
        }

        private void drawLuxuryLine(Graphics2D g, int cx, int y, int maxW) {
            String left = "D R I V E   L U X U R Y .   D R I V E   ";
            String right = "B M W .";

            Font f = fitFont(g, left + right, new Font("Segoe UI", Font.PLAIN, sf(13)), maxW);
            g.setFont(f);

            FontMetrics fm = g.getFontMetrics();
            int total = fm.stringWidth(left + right);
            int x = cx - total / 2;

            g.setColor(WHITE);
            g.drawString(left, x, y);

            g.setColor(BLUE_TEXT);
            g.drawString(right, x + fm.stringWidth(left), y);
        }

        private void drawTrustPanel(Graphics2D g, int x, int y, int w, int h) {
            if (w <= 0) {
                return;
            }

            drawGlass(g, x, y, w, h, 12, 105);

            String[][] data = {
                    {"shield", "SECURE SYSTEM", "Your data is always", "protected with us."},
                    {"calendar", "24/7 SUPPORT", "We're here for you,", "anytime, anywhere."},
                    {"star", "PREMIUM EXPERI...", "Excellence in every", "drive, every time."},
                    {"tag", "LOYALTY REWARDS", "Earn points & unlock", "exclusive benefits."}
            };

            int cell = w / 4;

            for (int i = 0; i < 4; i++) {
                int cx = x + i * cell + cell / 2;

                drawSmallIcon(g, data[i][0], cx, y + sh(21), sw(20));

                drawFitCentered(
                        g,
                        data[i][1],
                        cx,
                        y + sh(46),
                        cell - sw(8),
                        new Font("Segoe UI", Font.BOLD, sf(6)),
                        PALE
                );

                drawFitCentered(
                        g,
                        data[i][2],
                        cx,
                        y + sh(58),
                        cell - sw(10),
                        new Font("Segoe UI", Font.PLAIN, sf(7)),
                        WHITE
                );

                drawFitCentered(
                        g,
                        data[i][3],
                        cx,
                        y + sh(91),
                        cell - sw(10),
                        new Font("Segoe UI", Font.PLAIN, sf(7)),
                        WHITE
                );
            }
        }

        private void drawFeatures(Graphics2D g, int x, int y, int w, int h) {
            String[][] data = {
                    {"car", "LATEST BMW MODELS", "Drive the newest BMW models", "with cutting-edge technology."},
                    {"calendar", "FLEXIBLE RENTALS", "Daily, weekly, or monthly", "rentals to fit your schedule."},
                    {"shield", "FULL INSURANCE", "All rentals include comprehensive", "insurance coverage."},
                    {"tag", "BEST RATES", "Competitive pricing with", "premium service."}
            };

            int cell = w / 4;

            for (int i = 0; i < 4; i++) {
                int px = x + i * cell;

                drawGlass(g, px, y, cell + 1, h, 10, 135);
                drawSmallIcon(g, data[i][0], px + sw(34), y + h / 2 + sh(2), sw(30));

                drawFit(
                        g,
                        data[i][1],
                        px + sw(70),
                        y + sh(36),
                        cell - sw(84),
                        new Font("Segoe UI", Font.BOLD, sf(12)),
                        PALE,
                        false
                );

                drawFit(
                        g,
                        data[i][2],
                        px + sw(70),
                        y + sh(62),
                        cell - sw(84),
                        new Font("Segoe UI", Font.PLAIN, sf(11)),
                        WHITE,
                        false
                );

                drawFit(
                        g,
                        data[i][3],
                        px + sw(70),
                        y + sh(84),
                        cell - sw(84),
                        new Font("Segoe UI", Font.PLAIN, sf(11)),
                        WHITE,
                        false
                );
            }
        }

        private void drawQuote(Graphics2D g, int x, int y, int w, int h) {
            drawGlass(g, x, y, w, h, 10, 128);

            g.setColor(GOLD);
            g.setFont(new Font("Serif", Font.BOLD, sf(42)));
            g.drawString("“", x + sw(26), y + sh(42));

            g.setColor(WHITE);
            g.setFont(new Font("Segoe UI", Font.ITALIC, sf(13)));
            g.drawString("The Ultimate Driving Experience.", x + sw(67), y + sh(30));
            g.drawString("Now Available For Every Journey.", x + sw(67), y + sh(52));
        }

        private void drawStats(Graphics2D g, int x, int y, int w, int h) {
            drawGlass(g, x, y, w, h, 10, 115);

            String[] icon = {
                    "car",
                    "users",
                    "location",
                    "star"
            };

            String[] value = {
                    (int) (250 * statProgress) + "+",
                    statProgress > .995f ? "15K+" : String.format("%.0fK+", 15 * statProgress),
                    (int) (25 * statProgress) + "+",
                    String.format("%.1f/5", 4.9 * statProgress)
            };

            String[] label = {
                    "Premium Vehicles",
                    "Satisfied Customers",
                    "Locations",
                    "Customer Rating"
            };

            int cell = w / 4;

            Graphics2D gg = (Graphics2D) g.create();
            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            gg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            for (int i = 0; i < 4; i++) {
                int cx = x + i * cell + cell / 2;

                // فواصل خفيفة مثل الصورة
                if (i > 0) {
                    gg.setComposite(AlphaComposite.SrcOver.derive(.34f));
                    gg.setColor(new Color(214, 168, 91, 55));
                    gg.drawLine(x + i * cell, y + sh(13), x + i * cell, y + h - sh(13));
                    gg.setComposite(AlphaComposite.SrcOver);
                }

                Font valueFont = new Font("Segoe UI", Font.BOLD, sf(22));
                gg.setFont(valueFont);
                FontMetrics fm = gg.getFontMetrics();

                // كبرنا الرموز وخليّنا كل مجموعة بالنص بشكل متناسق
                int iconSize = Math.max(sw(24), sh(24));
                int gap = sw(10);
                int textW = fm.stringWidth(value[i]);
                int totalW = iconSize + gap + textW;
                int groupX = cx - totalW / 2;
                int iconY = y + sh(13);
                int valueBaseY = y + sh(32);

                // الرموز المطلوبة مرسومة يدويًا حتى تظهر أكيد بدون مشاكل إيموجي أو مربعات
                drawStatIcon(gg, icon[i], groupX, iconY, iconSize);

                gg.setColor(WHITE);
                gg.setFont(valueFont);
                gg.drawString(value[i], groupX + iconSize + gap, valueBaseY);

                drawFitCentered(
                        gg,
                        label[i],
                        cx,
                        y + sh(58),
                        cell - sw(8),
                        new Font("Segoe UI", Font.PLAIN, sf(11)),
                        BLUE_TEXT
                );
            }

            gg.dispose();
        }

        private void drawLoginCard(Graphics2D g, int w, int h) {
            int margin = sideMargin();
            int cw = rightW();
            int x = w - margin - cw;
            int y = topY();
            int ch = loginH();
            int cx = x + cw / 2;

            drawGlass(g, x, y, cw, ch, 18, 205);
            drawLoginCardPattern(g, x, y, cw, ch);

            drawBMW(g, cx - sw(34), y + sh(24), sw(68));

            drawFitCentered(
                    g,
                    "M A N A G E R   L O G I N",
                    cx,
                    y + sh(136),
                    cw - sw(50),
                    new Font("Segoe UI", Font.PLAIN, sf(27)),
                    PALE
            );

            drawFitCentered(
                    g,
                    "Welcome back! Please sign in to continue.",
                    cx,
                    y + sh(171),
                    cw - sw(50),
                    new Font("Segoe UI", Font.PLAIN, sf(13)),
                    WHITE
            );

            g.setColor(new Color(214, 168, 91, 170));
            g.setStroke(new BasicStroke(ss(1.5f)));
            g.drawLine(cx - sw(32), y + sh(194), cx + sw(32), y + sh(194));

            int px = username.getX();

            int userLabelY = username.getY() - sh(10);
            int passLabelY = password.getY() - sh(10);

            g.setFont(new Font("Segoe UI", Font.PLAIN, sf(12)));
            g.setColor(WHITE);

            g.drawString("USERNAME", px, userLabelY);
            g.drawString("PASSWORD", px, passLabelY);

            drawFieldIcon(g, px + sw(13), username.getY() + sh(12), "user");
            drawFieldIcon(g, px + sw(13), password.getY() + sh(12), "lock");

            int noticeX = username.getX();
            int noticeW = username.getWidth();

            int securityY = signIn.getY() + signIn.getHeight() + sh(16);
            int errorY = securityY + sh(58) + sh(14);

            int cardBottom = y + ch;

            if (errorY + sh(74) > cardBottom - sh(18)) {
                errorY = cardBottom - sh(74) - sh(18);
                securityY = errorY - sh(58) - sh(14);
            }

            drawNotice(g, noticeX, securityY, noticeW, sh(58), false);

            if (lockSeconds > 0) {
                drawNotice(g, noticeX, errorY, noticeW, sh(74), true);
            }
        }

        private void drawLoginCardPattern(Graphics2D g, int x, int y, int w, int h) {
            Graphics2D gg = (Graphics2D) g.create();

            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gg.setComposite(AlphaComposite.SrcOver.derive(.10f));
            gg.setColor(new Color(214, 168, 91, 90));
            gg.setStroke(new BasicStroke(ss(.8f)));

            int size = sh(18);
            int startX = x + w - sw(150);
            int startY = y + sh(28);

            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 6; col++) {
                    int hx = startX + col * size + (row % 2) * size / 2;
                    int hy = startY + row * (int) (size * .85);

                    Path2D hex = new Path2D.Double();
                    for (int i = 0; i < 6; i++) {
                        double a = Math.PI / 6 + i * Math.PI / 3;
                        double px = hx + Math.cos(a) * size * .35;
                        double py = hy + Math.sin(a) * size * .35;

                        if (i == 0) {
                            hex.moveTo(px, py);
                        } else {
                            hex.lineTo(px, py);
                        }
                    }
                    hex.closePath();
                    gg.draw(hex);
                }
            }

            gg.dispose();
        }

        private void drawNotice(Graphics2D g, int x, int y, int w, int h, boolean error) {
            Graphics2D gg = (Graphics2D) g.create();

            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (error) {
                RoundRectangle2D box = new RoundRectangle2D.Double(x, y, w, h, sh(7), sh(7));

                gg.setColor(new Color(110, 8, 18, 228));
                gg.fill(box);

                gg.setColor(new Color(255, 60, 72, 95));
                gg.setStroke(new BasicStroke(ss(1.2f)));
                gg.draw(box);

                int iconCx = x + sw(28);
                int iconCy = y + h / 2;

                gg.setColor(new Color(255, 76, 76));
                gg.setStroke(new BasicStroke(ss(2f)));
                gg.drawOval(iconCx - sw(14), iconCy - sw(14), sw(28), sw(28));
                gg.drawLine(iconCx, iconCy - sh(8), iconCx, iconCy + sh(3));
                gg.fillOval(iconCx - 2, iconCy + sh(8), 4, 4);

                String sec = String.valueOf(lockSeconds);

                gg.setColor(new Color(255, 92, 92));
                gg.setFont(new Font("Segoe UI", Font.PLAIN, sf(11)));
                gg.drawString("Too many failed attempts.", x + sw(58), y + sh(28));
                gg.drawString("Wait " + sec + " seconds before trying again.", x + sw(58), y + sh(52));

                int badgeSize = sw(38);
                int badgeX = x + w - badgeSize - sw(10);
                int badgeY = y + (h - badgeSize) / 2;

                gg.setColor(new Color(135, 20, 20, 210));
                gg.fillOval(badgeX, badgeY, badgeSize, badgeSize);

                gg.setStroke(new BasicStroke(ss(2f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                gg.setColor(new Color(255, 76, 76, 80));
                gg.drawOval(badgeX, badgeY, badgeSize, badgeSize);

                int countdownArc = Math.round(360f * lockSeconds / 30f);
                gg.setColor(new Color(255, 92, 92));
                gg.drawArc(
                        badgeX,
                        badgeY,
                        badgeSize,
                        badgeSize,
                        90,
                        -countdownArc
                );

                gg.setColor(WHITE);
                gg.setFont(new Font("Segoe UI", Font.BOLD, sf(10)));

                FontMetrics fm = gg.getFontMetrics();
                gg.drawString(sec, badgeX + (badgeSize - fm.stringWidth(sec)) / 2, badgeY + sh(16));

                gg.setFont(new Font("Segoe UI", Font.PLAIN, sf(8)));
                fm = gg.getFontMetrics();
                gg.drawString("SEC", badgeX + (badgeSize - fm.stringWidth("SEC")) / 2, badgeY + sh(28));
            } else {
                RoundRectangle2D box = new RoundRectangle2D.Double(x, y, w, h, sh(7), sh(7));

                gg.setColor(new Color(7, 13, 19, 220));
                gg.fill(box);

                gg.setColor(new Color(255, 255, 255, 24));
                gg.setStroke(new BasicStroke(ss(1.1f)));
                gg.draw(box);

                drawShieldIcon(gg, x + sw(24), y + h / 2, sw(22));

                gg.setColor(WHITE);
                gg.setFont(new Font("Segoe UI", Font.PLAIN, sf(11)));
                gg.drawString("For your security, your session will expire", x + sw(52), y + sh(24));
                gg.drawString("after 10 minutes of inactivity.", x + sw(52), y + sh(45));
            }

            gg.dispose();
        }

        private void drawFeatured(Graphics2D g, int w, int h) {
            int margin = sideMargin();
            int fw = rightW();
            int x = w - margin - fw;
            int loginY = topY();
            int loginH = loginH();
            int y = loginY + loginH + sh(10);
            int fh = Math.max(sh(128), h - footerH() - y - sh(24));

            Graphics2D banner = (Graphics2D) g.create();
            banner.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            banner.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            banner.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            RoundRectangle2D frameShape = new RoundRectangle2D.Double(
                    x + .5,
                    y + .5,
                    fw - 1,
                    fh - 1,
                    sh(13),
                    sh(13)
            );

            banner.setColor(new Color(0, 0, 0, 105));
            banner.fill(new RoundRectangle2D.Double(
                    x + sw(2),
                    y + sh(3),
                    fw - sw(4),
                    fh,
                    sh(13),
                    sh(13)
            ));

            banner.clip(frameShape);

            if (featuredCar != null) {
                drawCover(banner, featuredCar, x, y, fw, fh);
            } else {
                banner.setPaint(new GradientPaint(
                        x, y, new Color(7, 13, 20),
                        x + fw, y + fh, new Color(18, 12, 9)
                ));
                banner.fillRect(x, y, fw, fh);
            }

            banner.setPaint(new GradientPaint(
                    x, y, new Color(2, 7, 12, 244),
                    x + (int) (fw * .61), y, new Color(2, 7, 12, 8)
            ));
            banner.fillRect(x, y, (int) (fw * .67), fh);

            banner.setPaint(new GradientPaint(
                    x, y, new Color(0, 0, 0, 42),
                    x, y + fh, new Color(0, 0, 0, 82)
            ));
            banner.fillRect(x, y, fw, fh);

            int leftX = x + sw(32);
            int accentX = x + sw(18);

            banner.setColor(new Color(224, 177, 101, 125));
            banner.fillRoundRect(accentX, y + sh(16), sw(2), fh - sh(32), sw(2), sw(2));

            banner.setColor(PALE);
            banner.setFont(new Font("Segoe UI", Font.BOLD, sf(8)));
            banner.drawString("FEATURED MODEL", leftX, y + sh(22));

            banner.setColor(WHITE);
            banner.setFont(new Font("Segoe UI", Font.BOLD, sf(17)));
            banner.drawString("BMW i8 ROADSTER", leftX, y + sh(48));

            banner.setColor(new Color(193, 198, 207));
            banner.setFont(new Font("Segoe UI", Font.PLAIN, sf(10)));
            banner.drawString("Future meets performance.", leftX, y + sh(70));
            banner.setColor(new Color(153, 160, 171));
            banner.drawString("Rent the extraordinary.", leftX, y + sh(88));

            banner.setClip(null);
            banner.setStroke(new BasicStroke(ss(1.2f)));
            banner.setColor(new Color(218, 171, 94, 150));
            banner.draw(frameShape);

            banner.setStroke(new BasicStroke(ss(.6f)));
            banner.setColor(new Color(255, 226, 174, 42));
            banner.draw(new RoundRectangle2D.Double(
                    x + sw(2),
                    y + sh(2),
                    fw - sw(4),
                    fh - sh(4),
                    sh(11),
                    sh(11)
            ));
            banner.dispose();
        }

        private void drawFooter(Graphics2D g, int w, int h) {
            int fh = footerH();
            int y = h - fh;

            g.setColor(new Color(3, 7, 12, 248));
            g.fillRect(0, y, w, fh);

            g.setColor(new Color(255, 215, 150, 35));
            g.drawLine(0, y, w, y);

            // Left footer brand - شعار Velora Motors تحت بدون رموز مربعة
            int brandX = sw(52);
            int brandY = y + sh(22);

            drawMiniVeloraMark(g, brandX, brandY, sw(18));

            g.setColor(WHITE);
            g.setFont(new Font("Segoe UI", Font.PLAIN, sf(16)));
            g.drawString("V E L O R A   M O T O R S", brandX + sw(30), y + sh(31));

            g.setColor(MUTED);
            g.setFont(new Font("Segoe UI", Font.PLAIN, sf(11)));
            g.drawString("Premium BMW Vehicle Rental System", brandX + sw(30), y + sh(55));

            drawFitCentered(
                    g,
                    "© 2026 Velora Motors. All rights reserved.",
                    w / 2,
                    y + sh(46),
                    (int) (w * .32),
                    new Font("Segoe UI", Font.PLAIN, sf(13)),
                    MUTED
            );

            // Social media area - مرسومة يدويًا حتى ما تطلع مربعات غريبة حسب الخط
            int sx = (int) (w * .725);
            int cy = y + fh / 2 + sh(1);

            g.setColor(MUTED);
            g.setFont(new Font("Segoe UI", Font.PLAIN, sf(13)));
            g.drawString("F O L L O W   U S", sx, cy + sh(5));

            int iconGap = sw(58);
            int firstIconX = sx + sw(140);

            drawSocialIcon(g, "facebook", firstIconX, cy, sw(18));
            drawSocialIcon(g, "instagram", firstIconX + iconGap, cy, sw(18));
            drawSocialIcon(g, "linkedin", firstIconX + iconGap * 2, cy, sw(18));
            drawSocialIcon(g, "youtube", firstIconX + iconGap * 3, cy, sw(18));
        }

        private void drawMiniVeloraMark(Graphics2D g, int x, int y, int size) {
            Graphics2D gg = (Graphics2D) g.create();

            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gg.setColor(PALE);
            gg.setStroke(new BasicStroke(ss(1.5f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int cx = x + size / 2;
            int cy = y + size / 2;
            int wing = size / 2;

            Path2D mark = new Path2D.Double();

            mark.moveTo(cx, cy + size * .35);
            mark.lineTo(cx - size * .28, cy - size * .25);
            mark.lineTo(cx - wing, cy - size * .25);

            mark.moveTo(cx, cy + size * .35);
            mark.lineTo(cx + size * .28, cy - size * .25);
            mark.lineTo(cx + wing, cy - size * .25);

            mark.moveTo(cx - size * .23, cy - size * .02);
            mark.lineTo(cx - size * .45, cy - size * .02);

            mark.moveTo(cx + size * .23, cy - size * .02);
            mark.lineTo(cx + size * .45, cy - size * .02);

            gg.draw(mark);
            gg.dispose();
        }

        private void drawSocialIcon(Graphics2D g, String type, int cx, int cy, int size) {
            Graphics2D gg = (Graphics2D) g.create();

            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            gg.setColor(PALE);
            gg.setStroke(new BasicStroke(ss(1.5f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            if ("facebook".equals(type)) {
                gg.setFont(new Font("Segoe UI", Font.BOLD, sf(15)));
                FontMetrics fm = gg.getFontMetrics();
                String t = "f";
                gg.drawString(t, cx - fm.stringWidth(t) / 2, cy + fm.getAscent() / 2 - sh(2));
            } else if ("instagram".equals(type)) {
                int s = size;
                int x = cx - s / 2;
                int y = cy - s / 2;
                gg.drawRoundRect(x, y, s, s, Math.max(4, s / 4), Math.max(4, s / 4));
                gg.drawOval(cx - s / 5, cy - s / 5, (s * 2) / 5, (s * 2) / 5);
                gg.fillOval(cx + s / 4, cy - s / 4, Math.max(3, s / 7), Math.max(3, s / 7));
            } else if ("linkedin".equals(type)) {
                gg.setFont(new Font("Segoe UI", Font.BOLD, sf(13)));
                FontMetrics fm = gg.getFontMetrics();
                String t = "in";
                gg.drawString(t, cx - fm.stringWidth(t) / 2, cy + fm.getAscent() / 2 - sh(2));
            } else if ("youtube".equals(type)) {
                int ww = Math.round(size * 1.35f);
                int hh = Math.round(size * .82f);
                int x = cx - ww / 2;
                int y = cy - hh / 2;
                gg.drawRoundRect(x, y, ww, hh, Math.max(5, hh / 2), Math.max(5, hh / 2));

                Path2D play = new Path2D.Double();
                play.moveTo(cx - size * .15, cy - size * .25);
                play.lineTo(cx - size * .15, cy + size * .25);
                play.lineTo(cx + size * .28, cy);
                play.closePath();
                gg.fill(play);
            }

            gg.dispose();
        }

        private void drawGlass(Graphics2D g, int x, int y, int w, int h, int r, int alpha) {
            Graphics2D gg = (Graphics2D) g.create();

            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            RoundRectangle2D box = new RoundRectangle2D.Double(
                    x + .5,
                    y + .5,
                    w - 1,
                    h - 1,
                    sh(r),
                    sh(r)
            );

            gg.setColor(new Color(5, 10, 16, alpha));
            gg.fill(box);

            gg.setStroke(new BasicStroke(ss(1.2f)));
            gg.setColor(new Color(214, 168, 91, 76));
            gg.draw(box);

            gg.setColor(new Color(255, 255, 255, 10));
            gg.draw(new RoundRectangle2D.Double(
                    x + sw(1),
                    y + sh(1),
                    w - sw(2),
                    h - sh(2),
                    sh(r),
                    sh(r)
            ));

            gg.dispose();
        }

        private void drawBMW(Graphics2D g, int x, int y, int size) {
            Graphics2D logo = (Graphics2D) g.create();
            logo.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            logo.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            logo.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            if (bmwLogo != null) {
                int shadow = Math.max(1, size / 24);
                logo.setColor(new Color(0, 0, 0, 105));
                logo.fillOval(
                        x - shadow,
                        y + shadow,
                        size + shadow * 2,
                        size + shadow * 2
                );

                Shape circle = new Ellipse2D.Double(
                        x,
                        y,
                        size,
                        size
                );
                logo.clip(circle);

                // Crop only the source image's black square margin. The BMW
                // artwork itself stays unchanged and fills the round badge.
                int sourceCrop = Math.max(1, Math.round(bmwLogo.getWidth() * .028f));
                logo.drawImage(
                        bmwLogo,
                        x,
                        y,
                        x + size,
                        y + size,
                        sourceCrop,
                        sourceCrop,
                        bmwLogo.getWidth() - sourceCrop,
                        bmwLogo.getHeight() - sourceCrop,
                        null
                );

                logo.setClip(null);
                logo.dispose();
                return;
            }

            int in4 = Math.max(2, size / 15);
            int in12 = Math.max(6, size / 5);
            int d = size - in12 * 2;
            int xx = x + in12;
            int yy = y + in12;

            logo.setColor(WHITE);
            logo.fillOval(x, y, size, size);

            logo.setColor(new Color(4, 8, 13));
            logo.fillOval(x + in4, y + in4, size - in4 * 2, size - in4 * 2);

            logo.setColor(WHITE);
            logo.fillArc(xx, yy, d, d, 0, 90);
            logo.fillArc(xx, yy, d, d, 180, 90);

            logo.setColor(new Color(20, 126, 199));
            logo.fillArc(xx, yy, d, d, 90, 90);
            logo.fillArc(xx, yy, d, d, 270, 90);

            logo.setColor(new Color(255, 255, 255, 200));
            logo.setStroke(new BasicStroke(ss(2f)));
            logo.drawOval(xx, yy, d, d);
            logo.dispose();
        }

        private void drawWings(Graphics2D g, int cx, int y, double s) {
            Graphics2D gg = (Graphics2D) g.create();

            gg.translate(cx, y);
            gg.scale(s, s);
            gg.setColor(GOLD);
            gg.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            Path2D p = new Path2D.Double();

            p.moveTo(0, 55);
            p.lineTo(-37, 0);
            p.lineTo(-105, 0);

            p.moveTo(0, 55);
            p.lineTo(37, 0);
            p.lineTo(105, 0);

            p.moveTo(-28, 13);
            p.lineTo(-86, 13);

            p.moveTo(28, 13);
            p.lineTo(86, 13);

            p.moveTo(-19, 26);
            p.lineTo(-63, 26);

            p.moveTo(19, 26);
            p.lineTo(63, 26);

            gg.draw(p);
            gg.dispose();
        }

        private void drawSmallIcon(Graphics2D g, String type, int cx, int cy, int size) {
            g.setColor(GOLD);
            g.setStroke(new BasicStroke(ss(2f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int s = size;

            if ("shield".equals(type)) {
                drawShieldIcon(g, cx, cy, s);
            } else if ("calendar".equals(type)) {
                g.drawRoundRect(cx - s / 2, cy - s / 2, s, s, 3, 3);
                g.drawLine(cx - s / 2, cy - s / 5, cx + s / 2, cy - s / 5);
                g.drawLine(cx - s / 4, cy - s / 2 - 3, cx - s / 4, cy - s / 3);
                g.drawLine(cx + s / 4, cy - s / 2 - 3, cx + s / 4, cy - s / 3);
            } else if ("star".equals(type)) {
                Path2D star = new Path2D.Double();

                for (int i = 0; i < 10; i++) {
                    double a = -Math.PI / 2 + i * Math.PI / 5;
                    double rr = (i % 2 == 0) ? s / 2.0 : s / 4.0;
                    double px = cx + Math.cos(a) * rr;
                    double py = cy + Math.sin(a) * rr;

                    if (i == 0) {
                        star.moveTo(px, py);
                    } else {
                        star.lineTo(px, py);
                    }
                }

                star.closePath();
                g.draw(star);
            } else if ("tag".equals(type)) {
                Path2D tag = new Path2D.Double();

                tag.moveTo(cx - s / 2, cy - s / 7);
                tag.lineTo(cx, cy - s / 2);
                tag.lineTo(cx + s / 2, cy);
                tag.lineTo(cx, cy + s / 2);
                tag.closePath();

                g.draw(tag);
                g.fillOval(cx - s / 8, cy - s / 4, 4, 4);
            } else if ("car".equals(type)) {
                g.drawRoundRect(cx - s / 2, cy - s / 6, s, s / 3, 4, 4);
                g.drawLine(cx - s / 3, cy - s / 6, cx - s / 5, cy - s / 2);
                g.drawLine(cx - s / 5, cy - s / 2, cx + s / 4, cy - s / 2);
                g.drawLine(cx + s / 4, cy - s / 2, cx + s / 2, cy - s / 6);
                g.fillOval(cx - s / 3, cy + s / 6, s / 6, s / 6);
                g.fillOval(cx + s / 5, cy + s / 6, s / 6, s / 6);
            }
        }

        private void drawStatIcon(Graphics2D g, String type, int x, int y, int size) {
            Graphics2D gg = (Graphics2D) g.create();

            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gg.setColor(new Color(235, 240, 248, 235));
            gg.setStroke(new BasicStroke(Math.max(1.8f, ss(2.05f)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int s = size;
            int cx = x + s / 2;
            int cy = y + s / 2;

            if ("car".equals(type)) {
                int bodyY = y + (int) (s * .46);
                int bodyH = Math.max(5, (int) (s * .31));
                int bodyX = x + (int) (s * .08);
                int bodyW = (int) (s * .84);

                gg.drawRoundRect(bodyX, bodyY, bodyW, bodyH, Math.max(3, s / 5), Math.max(3, s / 5));

                Path2D roof = new Path2D.Double();
                roof.moveTo(x + s * .24, bodyY);
                roof.lineTo(x + s * .36, y + s * .25);
                roof.lineTo(x + s * .64, y + s * .25);
                roof.lineTo(x + s * .78, bodyY);
                gg.draw(roof);

                gg.drawLine(x + (int) (s * .48), y + (int) (s * .27), x + (int) (s * .48), bodyY);
                gg.fillOval(x + (int) (s * .22), y + (int) (s * .73), Math.max(3, s / 5), Math.max(3, s / 5));
                gg.fillOval(x + (int) (s * .63), y + (int) (s * .73), Math.max(3, s / 5), Math.max(3, s / 5));
            } else if ("users".equals(type)) {
                int r = Math.max(3, s / 5);

                gg.drawOval(cx - r / 2, y + (int) (s * .13), r, r);
                gg.drawArc(cx - (int) (s * .34), y + (int) (s * .49), (int) (s * .68), (int) (s * .42), 0, 180);

                int sideR = Math.max(3, s / 6);
                gg.drawOval(x + (int) (s * .07), y + (int) (s * .24), sideR, sideR);
                gg.drawArc(x, y + (int) (s * .58), (int) (s * .35), (int) (s * .30), 12, 160);

                gg.drawOval(x + (int) (s * .75), y + (int) (s * .24), sideR, sideR);
                gg.drawArc(x + (int) (s * .65), y + (int) (s * .58), (int) (s * .35), (int) (s * .30), 8, 160);
            } else if ("location".equals(type)) {
                Path2D pin = new Path2D.Double();
                pin.moveTo(cx, y + s - 1);
                pin.curveTo(x + (int) (s * .13), y + (int) (s * .58), x + (int) (s * .16), y + (int) (s * .16), cx, y + (int) (s * .10));
                pin.curveTo(x + (int) (s * .84), y + (int) (s * .16), x + (int) (s * .87), y + (int) (s * .58), cx, y + s - 1);
                pin.closePath();
                gg.draw(pin);
                gg.drawOval(cx - s / 7, y + (int) (s * .34), Math.max(3, s / 4), Math.max(3, s / 4));
            } else if ("star".equals(type)) {
                Path2D star = new Path2D.Double();

                for (int i = 0; i < 10; i++) {
                    double a = -Math.PI / 2 + i * Math.PI / 5;
                    double rr = (i % 2 == 0) ? s * .47 : s * .21;
                    double px = cx + Math.cos(a) * rr;
                    double py = cy + Math.sin(a) * rr;

                    if (i == 0) {
                        star.moveTo(px, py);
                    } else {
                        star.lineTo(px, py);
                    }
                }

                star.closePath();
                gg.draw(star);
            }

            gg.dispose();
        }

        private void drawFieldIcon(Graphics2D g, int x, int y, String type) {
            g.setColor(new Color(215, 220, 228, 140));
            g.setStroke(new BasicStroke(ss(1.7f)));

            if ("user".equals(type)) {
                g.drawOval(x, y, sw(10), sw(10));
                g.drawArc(x - sw(5), y + sh(13), sw(20), sh(15), 0, 180);
            } else {
                g.drawRoundRect(x - sw(2), y + sh(9), sw(16), sh(16), 3, 3);
                g.drawArc(x + sw(1), y, sw(10), sh(17), 0, 180);
            }
        }

        private void drawShieldIcon(Graphics2D g, int cx, int cy, int s) {
            Path2D p = new Path2D.Double();

            p.moveTo(cx, cy - s / 2.0);
            p.lineTo(cx + s / 2.0, cy - s / 4.0);
            p.lineTo(cx + s / 3.0, cy + s / 3.0);
            p.lineTo(cx, cy + s / 2.0);
            p.lineTo(cx - s / 3.0, cy + s / 3.0);
            p.lineTo(cx - s / 2.0, cy - s / 4.0);
            p.closePath();

            g.draw(p);
            g.drawLine(cx, cy - s / 4, cx, cy + s / 4);
            g.drawLine(cx - s / 5, cy, cx + s / 5, cy);
        }

        private void drawFitCentered(Graphics2D g, String t, int cx, int y, int maxW, Font font, Color color) {
            Font f = fitFont(g, t, font, maxW);

            g.setFont(f);
            g.setColor(color);

            int tw = g.getFontMetrics().stringWidth(t);
            g.drawString(t, cx - tw / 2, y);
        }

        private void drawFit(Graphics2D g, String t, int x, int y, int maxW, Font font, Color color, boolean right) {
            Font f = fitFont(g, t, font, maxW);

            g.setFont(f);
            g.setColor(color);

            int tw = g.getFontMetrics().stringWidth(t);
            g.drawString(t, right ? x + maxW - tw : x, y);
        }

        private Font fitFont(Graphics2D g, String t, Font font, int maxW) {
            Font f = font;

            while (f.getSize() > 8) {
                g.setFont(f);

                if (g.getFontMetrics().stringWidth(t) <= maxW) {
                    return f;
                }

                f = f.deriveFont((float) f.getSize() - 1f);
            }

            return f;
        }

        private int sideMargin() {
            return sw(48);
        }

        private int topY() {
            return sh(30);
        }

        private int rightW() {
            int w = getWidth();
            return Math.min(Math.max(sw(430), (int) (w * .315)), w - sw(180));
        }

        private int footerH() {
            int h = getHeight();
            return clamp((int) (h * .085), sh(62), sh(86));
        }

        private int loginH() {
            int h = getHeight();

            int footer = footerH();
            int availableH = h - footer - topY() - sh(12);

            int featuredReserve = sh(150);
            int preferred = availableH - featuredReserve;

            int min = sh(620);
            int max = availableH - sh(125);

            if (max < min) {
                return Math.max(sh(600), max);
            }

            return clamp(preferred, min, max);
        }

        private int sw(int v) {
            return Math.max(1, Math.round(v * getWidth() / 1536f));
        }

        private int sh(int v) {
            return Math.max(1, Math.round(v * getHeight() / 864f));
        }

        private int sf(int v) {
            float s = Math.min(getWidth() / 1536f, getHeight() / 864f);
            return Math.max(8, Math.round(v * s));
        }

        private float ss(float v) {
            float s = Math.min(getWidth() / 1536f, getHeight() / 864f);
            return Math.max(1f, v * s);
        }

        private static int clamp(int v, int min, int max) {
            return Math.max(min, Math.min(max, v));
        }

        private static float ease(float p) {
            return 1f - (1f - p) * (1f - p) * (1f - p);
        }

        private static BufferedImage loadFirst(String... paths) {
            for (String path : paths) {
                try {
                    java.net.URL url = VeloraCanvas.class.getResource(path);

                    if (url != null) {
                        BufferedImage img = ImageIO.read(url);

                        if (img != null) {
                            return img;
                        }
                    }
                } catch (IOException | IllegalArgumentException ignored) {
                }
            }

            return null;
        }

        private static void drawContain(Graphics2D g, BufferedImage img, int x, int y, int w, int h) {
            if (img == null || w <= 0 || h <= 0) {
                return;
            }

            double s = Math.min(w / (double) img.getWidth(), h / (double) img.getHeight());

            int iw = (int) Math.round(img.getWidth() * s);
            int ih = (int) Math.round(img.getHeight() * s);

            g.drawImage(img, x + (w - iw) / 2, y + (h - ih) / 2, iw, ih, null);
        }

        private static void drawCover(Graphics2D g, BufferedImage img, int x, int y, int w, int h) {
            if (img == null || w <= 0 || h <= 0) {
                return;
            }

            double s = Math.max(w / (double) img.getWidth(), h / (double) img.getHeight());

            int iw = (int) Math.round(img.getWidth() * s);
            int ih = (int) Math.round(img.getHeight() * s);

            g.drawImage(img, x + (w - iw) / 2, y + (h - ih) / 2, iw, ih, null);
        }

        private static JButton ghostButton(String text) {
            JButton b = new JButton(text);

            b.setOpaque(false);
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            return b;
        }

        private static JButton topButton(String text) {
            JButton b = ghostButton(text);

            b.setForeground(WHITE);
            b.setFont(new Font("Segoe UI", Font.BOLD, 12));

            return b;
        }
    }

    private static final class CreateAccountDialog extends JDialog {

        CreateAccountDialog(
                Frame owner,
                AuthenticationService authenticationService,
                java.util.function.Consumer<String> onCreated
        ) {
            super(owner, true);
            setUndecorated(true);
            setResizable(false);
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            setSize(450, 620);
            setBackground(new Color(0, 0, 0, 0));
            setContentPane(new CreateAccountPanel(
                    this,
                    authenticationService,
                    onCreated
            ));
            setLocationRelativeTo(owner);

            try {
                setShape(new RoundRectangle2D.Double(0, 0, 450, 620, 24, 24));
            } catch (UnsupportedOperationException ignored) {
            }

            getRootPane().registerKeyboardAction(
                    e -> dispose(),
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                    JComponent.WHEN_IN_FOCUSED_WINDOW
            );
        }
    }

    private static final class ResetPasswordDialog extends JDialog {

        ResetPasswordDialog(
                Frame owner,
                AuthenticationService authenticationService,
                java.util.function.Consumer<String> onReset
        ) {
            super(owner, true);
            setUndecorated(true);
            setResizable(false);
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            setSize(410, 580);
            setBackground(new Color(0, 0, 0, 0));
            setContentPane(new ResetPasswordPanel(
                    this,
                    authenticationService,
                    onReset
            ));
            setLocationRelativeTo(owner);

            try {
                setShape(new RoundRectangle2D.Double(0, 0, 410, 580, 24, 24));
            } catch (UnsupportedOperationException ignored) {
            }

            getRootPane().registerKeyboardAction(
                    e -> dispose(),
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                    JComponent.WHEN_IN_FOCUSED_WINDOW
            );
        }
    }

    private static final class ResetPasswordPanel extends JPanel {

        private final JDialog dialog;
        private final AuthenticationService authenticationService;
        private final java.util.function.Consumer<String> onReset;

        private final SignupField email = new SignupField("Email Address", "mail", false);
        private final SignupField newPassword = new SignupField("New Password", "lock", true);
        private final SignupField confirmPassword = new SignupField("Confirm New Password", "lock", true);

        private final EyeButton newPasswordEye = new EyeButton();
        private final EyeButton confirmPasswordEye = new EyeButton();
        private final JButton reset = new GoldButton("RESET PASSWORD     →");
        private final JButton close = new DialogCloseButton();
        private final JButton signIn = new JButton("Sign in");
        private final JLabel status = new JLabel("", SwingConstants.CENTER);

        ResetPasswordPanel(
                JDialog dialog,
                AuthenticationService authenticationService,
                java.util.function.Consumer<String> onReset
        ) {
            this.dialog = dialog;
            this.authenticationService = authenticationService;
            this.onReset = onReset;

            setOpaque(false);
            setLayout(null);

            close.setToolTipText("Close");
            close.addActionListener(e -> dialog.dispose());

            signIn.setOpaque(false);
            signIn.setContentAreaFilled(false);
            signIn.setBorderPainted(false);
            signIn.setFocusPainted(false);
            signIn.setForeground(new Color(236, 203, 157));
            signIn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            signIn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            signIn.addActionListener(e -> dialog.dispose());

            status.setForeground(new Color(255, 104, 104));
            status.setFont(new Font("Segoe UI", Font.PLAIN, 11));

            configureEye(newPasswordEye, newPassword);
            configureEye(confirmPasswordEye, confirmPassword);
            reset.addActionListener(e -> resetPassword());

            email.setBounds(30, 160, 350, 43);
            newPassword.setBounds(30, 213, 350, 43);
            confirmPassword.setBounds(30, 266, 350, 43);
            newPasswordEye.setBounds(337, 219, 36, 31);
            confirmPasswordEye.setBounds(337, 272, 36, 31);
            status.setBounds(30, 401, 350, 19);
            reset.setBounds(30, 427, 350, 50);
            close.setBounds(354, 13, 38, 38);
            signIn.setBounds(236, 517, 72, 30);

            add(email);
            add(newPassword);
            add(confirmPassword);
            add(newPasswordEye);
            add(confirmPasswordEye);
            add(status);
            add(reset);
            add(close);
            add(signIn);

            setComponentZOrder(newPasswordEye, 0);
            setComponentZOrder(confirmPasswordEye, 0);
        }

        private void configureEye(EyeButton eye, SignupField field) {
            eye.putClientProperty("visible", Boolean.FALSE);
            eye.setToolTipText("Show password");
            eye.addActionListener(e -> {
                boolean visible = !Boolean.TRUE.equals(eye.getClientProperty("visible"));
                field.setEchoChar(visible ? (char) 0 : '•');
                eye.putClientProperty("visible", visible);
                eye.setToolTipText(visible ? "Hide password" : "Show password");
                field.requestFocusInWindow();
                eye.repaint();
            });
        }

        private void resetPassword() {
            String mail = email.getRealText().trim();
            String pass = newPassword.getRealText();
            String repeated = confirmPassword.getRealText();

            if (mail.isBlank() || pass.isBlank() || repeated.isBlank()) {
                showError("Please complete all fields.");
                return;
            }

            if (!mail.matches("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")) {
                showError("Please enter a valid email address.");
                email.requestFocusInWindow();
                return;
            }

            boolean validPassword = pass.length() >= 8
                    && pass.matches(".*[A-Z].*")
                    && pass.matches(".*[a-z].*")
                    && pass.matches(".*[0-9].*")
                    && pass.matches(".*[^A-Za-z0-9].*");

            if (!validPassword) {
                showError("Password does not meet the security requirements.");
                newPassword.requestFocusInWindow();
                return;
            }

            if (!pass.equals(repeated)) {
                showError("Passwords do not match.");
                confirmPassword.requestFocusInWindow();
                return;
            }

            try {
                boolean updated = authenticationService.resetPassword(
                        mail,
                        pass.toCharArray()
                );

                if (!updated) {
                    showError("No account was found for this email address.");
                    email.requestFocusInWindow();
                    return;
                }
            } catch (IllegalStateException ex) {
                showError(ex.getMessage());
                return;
            }

            onReset.accept(mail);
            dialog.dispose();

            JOptionPane.showMessageDialog(
                    dialog.getOwner(),
                    "Your password has been updated successfully.",
                    "Password Reset",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }

        private void showError(String message) {
            status.setText(message);
            Toolkit.getDefaultToolkit().beep();
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int cx = w / 2;

            RoundRectangle2D card = new RoundRectangle2D.Double(
                    .75, .75, w - 1.5, h - 1.5, 23, 23
            );
            g.setPaint(new GradientPaint(
                    0, 0, new Color(10, 17, 24, 252),
                    w, h, new Color(3, 8, 13, 252)
            ));
            g.fill(card);
            g.setStroke(new BasicStroke(1.35f));
            g.setColor(new Color(214, 168, 91, 185));
            g.draw(card);

            int iconY = 52;
            g.setColor(new Color(237, 239, 244, 145));
            g.setStroke(new BasicStroke(1.2f));
            g.drawOval(cx - 23, iconY - 23, 46, 46);

            g.setColor(new Color(236, 203, 157));
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawRoundRect(cx - 7, iconY - 2, 14, 13, 2, 2);
            g.drawArc(cx - 5, iconY - 11, 10, 12, 0, 180);
            g.fillOval(cx - 1, iconY + 3, 3, 3);
            g.drawLine(cx, iconY + 6, cx, iconY + 8);

            drawCentered(g, "R E S E T   P A S S W O R D", 100,
                    new Font("Segoe UI", Font.BOLD, 16), new Color(236, 203, 157));

            g.setColor(new Color(214, 168, 91, 125));
            g.drawLine(cx - 64, 116, cx - 12, 116);
            g.drawLine(cx + 12, 116, cx + 64, 116);
            Path2D diamond = new Path2D.Double();
            diamond.moveTo(cx, 111);
            diamond.lineTo(cx + 5, 116);
            diamond.lineTo(cx, 121);
            diamond.lineTo(cx - 5, 116);
            diamond.closePath();
            g.draw(diamond);

            drawCentered(g, "Enter your email and choose a new password.", 137,
                    new Font("Segoe UI", Font.PLAIN, 11), new Color(220, 223, 228));
            drawCentered(g, "We'll update your password securely.", 153,
                    new Font("Segoe UI", Font.PLAIN, 11), new Color(220, 223, 228));

            RoundRectangle2D requirements = new RoundRectangle2D.Double(
                    30.5, 320.5, 349, 76, 8, 8
            );
            g.setColor(new Color(7, 13, 19, 220));
            g.fill(requirements);
            g.setColor(new Color(255, 255, 255, 28));
            g.setStroke(new BasicStroke(1f));
            g.draw(requirements);

            g.setColor(new Color(226, 181, 105));
            g.setStroke(new BasicStroke(1.3f));
            g.drawOval(43, 332, 14, 14);
            g.drawLine(50, 336, 50, 340);
            g.fillOval(49, 342, 2, 2);

            g.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g.setColor(new Color(224, 227, 232));
            g.drawString("Password Requirements:", 66, 343);

            g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            String[] rules = {
                    "At least 8 characters long",
                    "Include uppercase and lowercase letters",
                    "Include a number and a special character"
            };
            for (int i = 0; i < rules.length; i++) {
                int ruleY = 359 + i * 16;
                g.setColor(new Color(226, 181, 105));
                g.drawLine(68, ruleY - 3, 71, ruleY);
                g.drawLine(71, ruleY, 77, ruleY - 7);
                g.setColor(new Color(187, 192, 201));
                g.drawString(rules[i], 84, ruleY);
            }

            int dividerY = 500;
            g.setColor(new Color(255, 255, 255, 34));
            g.drawLine(30, dividerY, cx - 25, dividerY);
            g.drawLine(cx + 25, dividerY, w - 30, dividerY);
            drawCentered(g, "OR", dividerY + 4,
                    new Font("Segoe UI", Font.PLAIN, 10), new Color(187, 192, 201));

            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g.setColor(new Color(224, 227, 232));
            g.drawString("Remember your password?", 91, 537);

            g.dispose();
        }

        private void drawCentered(Graphics2D g, String text, int baseline, Font font, Color color) {
            g.setFont(font);
            g.setColor(color);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, baseline);
        }
    }

    private static final class CreateAccountPanel extends JPanel {

        private final JDialog dialog;
        private final AuthenticationService authenticationService;
        private final java.util.function.Consumer<String> onCreated;

        private final SignupField fullName = new SignupField("Full Name", "user", false);
        private final SignupField email = new SignupField("Email Address", "mail", false);
        private final SignupField phone = new SignupField("Phone Number", "phone", false);
        private final SignupField password = new SignupField("Password", "lock", true);
        private final SignupField confirm = new SignupField("Confirm Password", "lock", true);

        private final EyeButton passwordEye = new EyeButton();
        private final EyeButton confirmEye = new EyeButton();
        private final JButton create = new GoldButton("CREATE ACCOUNT     →");
        private final JButton close = new DialogCloseButton();
        private final JButton signIn = dialogButton("Sign in");
        private final JLabel status = new JLabel("", SwingConstants.CENTER);

        CreateAccountPanel(
                JDialog dialog,
                AuthenticationService authenticationService,
                java.util.function.Consumer<String> onCreated
        ) {
            this.dialog = dialog;
            this.authenticationService = authenticationService;
            this.onCreated = onCreated;

            setOpaque(false);
            setLayout(null);

            close.setToolTipText("Close");
            close.addActionListener(e -> dialog.dispose());

            signIn.setForeground(new Color(236, 203, 157));
            signIn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            signIn.addActionListener(e -> dialog.dispose());

            status.setForeground(new Color(255, 104, 104));
            status.setFont(new Font("Segoe UI", Font.PLAIN, 11));

            configureEye(passwordEye, password);
            configureEye(confirmEye, confirm);
            create.addActionListener(e -> createAccount());

            fullName.setBounds(30, 175, 190, 43);
            email.setBounds(230, 175, 190, 43);
            phone.setBounds(30, 230, 390, 43);
            password.setBounds(30, 285, 390, 43);
            confirm.setBounds(30, 340, 390, 43);
            passwordEye.setBounds(376, 291, 37, 31);
            confirmEye.setBounds(376, 346, 37, 31);
            status.setBounds(30, 428, 390, 20);
            create.setBounds(30, 456, 390, 52);
            close.setBounds(394, 13, 38, 38);
            signIn.setBounds(271, 546, 72, 30);

            add(fullName);
            add(email);
            add(phone);
            add(password);
            add(confirm);
            add(passwordEye);
            add(confirmEye);
            add(status);
            add(create);
            add(close);
            add(signIn);

            setComponentZOrder(passwordEye, 0);
            setComponentZOrder(confirmEye, 0);
        }

        private void configureEye(EyeButton eye, SignupField field) {
            eye.putClientProperty("visible", Boolean.FALSE);
            eye.setToolTipText("Show password");
            eye.addActionListener(e -> {
                boolean visible = !Boolean.TRUE.equals(eye.getClientProperty("visible"));
                field.setEchoChar(visible ? (char) 0 : '•');
                eye.putClientProperty("visible", visible);
                eye.setToolTipText(visible ? "Hide password" : "Show password");
                field.requestFocusInWindow();
                eye.repaint();
            });
        }

        private static JButton dialogButton(String text) {
            JButton button = new JButton(text);
            button.setOpaque(false);
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return button;
        }

        private void createAccount() {
            String name = fullName.getRealText().trim();
            String mail = email.getRealText().trim();
            String number = phone.getRealText().trim();
            String pass = password.getRealText();
            String repeated = confirm.getRealText();

            if (name.isBlank() || mail.isBlank() || number.isBlank()
                    || pass.isBlank() || repeated.isBlank()) {
                showError("Please complete all fields.");
                return;
            }

            if (!mail.matches("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")) {
                showError("Please enter a valid email address.");
                email.requestFocusInWindow();
                return;
            }

            if (!number.matches("[0-9+()\\-\\s]{7,}")) {
                showError("Please enter a valid phone number.");
                phone.requestFocusInWindow();
                return;
            }

            boolean strongPassword = pass.length() >= 8
                    && pass.matches(".*[A-Z].*")
                    && pass.matches(".*[0-9].*")
                    && pass.matches(".*[^A-Za-z0-9].*");

            if (!strongPassword) {
                showError("Password does not meet the security requirements.");
                password.requestFocusInWindow();
                return;
            }

            if (!pass.equals(repeated)) {
                showError("Passwords do not match.");
                confirm.requestFocusInWindow();
                return;
            }

            try {
                authenticationService.registerCustomer(
                        name,
                        mail,
                        number,
                        pass.toCharArray()
                );
            } catch (IllegalArgumentException | IllegalStateException ex) {
                showError(ex.getMessage());
                return;
            }

            onCreated.accept(mail);
            dialog.dispose();

            JOptionPane.showMessageDialog(
                    dialog.getOwner(),
                    "Your Velora account has been created successfully.",
                    "Account Created",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }

        private void showError(String message) {
            status.setText(message);
            Toolkit.getDefaultToolkit().beep();
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            RoundRectangle2D card = new RoundRectangle2D.Double(
                    .75, .75, w - 1.5, h - 1.5, 23, 23
            );
            g.setPaint(new GradientPaint(
                    0, 0, new Color(10, 17, 24, 252),
                    w, h, new Color(3, 8, 13, 252)
            ));
            g.fill(card);

            g.setStroke(new BasicStroke(1.35f));
            g.setColor(new Color(214, 168, 91, 185));
            g.draw(card);

            int cx = w / 2;
            int iconY = 59;

            g.setColor(new Color(237, 239, 244, 150));
            g.setStroke(new BasicStroke(1.2f));
            g.drawOval(cx - 25, iconY - 25, 50, 50);

            g.setColor(new Color(236, 203, 157));
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawOval(cx - 7, iconY - 13, 14, 14);
            g.drawArc(cx - 12, iconY + 2, 24, 17, 15, 150);
            g.drawLine(cx + 13, iconY + 7, cx + 13, iconY + 18);
            g.drawLine(cx + 8, iconY + 12, cx + 18, iconY + 12);

            drawCentered(g, "C R E A T E   A C C O U N T", 112,
                    new Font("Segoe UI", Font.BOLD, 17), new Color(236, 203, 157));

            g.setColor(new Color(214, 168, 91, 125));
            g.drawLine(cx - 66, 130, cx - 13, 130);
            g.drawLine(cx + 13, 130, cx + 66, 130);
            Path2D diamond = new Path2D.Double();
            diamond.moveTo(cx, 125);
            diamond.lineTo(cx + 5, 130);
            diamond.lineTo(cx, 135);
            diamond.lineTo(cx - 5, 130);
            diamond.closePath();
            g.draw(diamond);

            drawCentered(g, "Join Velora Motors and start your journey.", 155,
                    new Font("Segoe UI", Font.PLAIN, 12), new Color(220, 223, 228));

            g.setColor(new Color(226, 181, 105));
            g.setStroke(new BasicStroke(1.3f));
            g.drawOval(34, 396, 12, 14);
            g.drawLine(40, 399, 40, 407);
            g.drawLine(37, 402, 43, 402);

            g.setColor(new Color(174, 180, 190));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g.drawString("Password must be at least 8 characters and include", 56, 404);
            g.drawString("a number, an uppercase letter, and a special character.", 56, 421);

            int dividerY = 533;
            g.setColor(new Color(255, 255, 255, 32));
            g.drawLine(30, dividerY, cx - 26, dividerY);
            g.drawLine(cx + 26, dividerY, w - 30, dividerY);
            drawCentered(g, "OR", dividerY + 4,
                    new Font("Segoe UI", Font.PLAIN, 10), new Color(187, 192, 201));

            g.setColor(new Color(224, 227, 232));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            String footer = "Already have an account?";
            int footerW = g.getFontMetrics().stringWidth(footer);
            g.drawString(footer, cx - 104, 566);

            g.dispose();
        }

        private void drawCentered(Graphics2D g, String text, int baseline, Font font, Color color) {
            g.setFont(font);
            g.setColor(color);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, baseline);
        }
    }

    private static final class DialogCloseButton extends JButton {

        DialogCloseButton() {
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            boolean hover = getModel().isRollover();
            boolean pressed = getModel().isPressed();

            if (hover) {
                g.setColor(new Color(214, 168, 91, pressed ? 55 : 28));
                g.fillOval(3, 3, getWidth() - 6, getHeight() - 6);
            }

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            int arm = 7;
            g.setColor(new Color(236, 203, 157, pressed ? 210 : 255));
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(cx - arm, cy - arm, cx + arm, cy + arm);
            g.drawLine(cx + arm, cy - arm, cx - arm, cy + arm);
            g.dispose();
        }
    }

    private static final class SignupField extends JPasswordField {

        private final String placeholder;
        private final String icon;

        SignupField(String placeholder, String icon, boolean passwordMode) {
            this.placeholder = placeholder;
            this.icon = icon;
            setEchoChar(passwordMode ? '•' : (char) 0);
            setOpaque(false);
            setForeground(new Color(248, 248, 248));
            setCaretColor(new Color(214, 168, 91));
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setBorder(new EmptyBorder(0, 42, 0, passwordMode ? 52 : 14));
        }

        String getRealText() {
            return new String(getPassword());
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            RoundRectangle2D box = new RoundRectangle2D.Double(
                    .5, .5, getWidth() - 1, getHeight() - 1, 9, 9
            );
            g.setColor(new Color(6, 12, 18, 230));
            g.fill(box);
            g.setColor(new Color(214, 168, 91, isFocusOwner() ? 150 : 62));
            g.setStroke(new BasicStroke(isFocusOwner() ? 1.35f : 1f));
            g.draw(box);
            drawFieldIcon(g);
            g.dispose();

            super.paintComponent(raw);

            if (getPassword().length == 0) {
                g = (Graphics2D) raw.create();
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setColor(new Color(142, 148, 158));
                g.setFont(getFont());
                FontMetrics fm = g.getFontMetrics();
                g.drawString(placeholder, 42, (getHeight() + fm.getAscent()) / 2 - 3);
                g.dispose();
            }
        }

        private void drawFieldIcon(Graphics2D g) {
            int cx = 20;
            int cy = getHeight() / 2;
            g.setColor(new Color(151, 169, 191));
            g.setStroke(new BasicStroke(1.25f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (icon) {
                case "user" -> {
                    g.drawOval(cx - 4, cy - 8, 8, 8);
                    g.drawArc(cx - 7, cy + 1, 14, 11, 15, 150);
                }
                case "mail" -> {
                    g.drawRoundRect(cx - 7, cy - 6, 14, 11, 2, 2);
                    g.drawLine(cx - 7, cy - 5, cx, cy + 1);
                    g.drawLine(cx + 7, cy - 5, cx, cy + 1);
                }
                case "phone" -> {
                    g.drawArc(cx - 7, cy - 8, 14, 16, 135, 105);
                    g.drawLine(cx - 7, cy - 4, cx - 3, cy);
                    g.drawLine(cx + 3, cy + 5, cx + 7, cy + 2);
                }
                default -> {
                    g.drawRoundRect(cx - 6, cy - 3, 12, 11, 2, 2);
                    g.drawArc(cx - 4, cy - 9, 8, 10, 0, 180);
                }
            }
        }
    }

    private static final class RoundTextField extends JPasswordField {

        private final String placeholder;

        RoundTextField(String placeholder, boolean passwordMode) {
            this.placeholder = placeholder;

            setEchoChar(passwordMode ? '•' : (char) 0);
            setOpaque(false);
            setForeground(new Color(248, 248, 248));
            setCaretColor(new Color(214, 168, 91));
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setBorder(new EmptyBorder(0, 48, 0, passwordMode ? 62 : 14));
        }

        String getRealText() {
            return new String(getPassword());
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            RoundRectangle2D box = new RoundRectangle2D.Double(
                    .5,
                    .5,
                    getWidth() - 1,
                    getHeight() - 1,
                    10,
                    10
            );

            g.setColor(new Color(7, 13, 20, 235));
            g.fill(box);

            g.setColor(new Color(214, 168, 91, isFocusOwner() ? 150 : 48));
            g.setStroke(new BasicStroke(1.2f));
            g.draw(box);

            g.dispose();

            super.paintComponent(raw);

            if (getPassword().length == 0) {
                g = (Graphics2D) raw.create();

                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setFont(getFont());
                g.setColor(new Color(135, 139, 146));

                FontMetrics fm = g.getFontMetrics();
                int tw = fm.stringWidth(placeholder);

                g.drawString(
                        placeholder,
                        Math.max(48, (getWidth() - tw) / 2),
                        (getHeight() + fm.getAscent()) / 2 - 3
                );

                g.dispose();
            }
        }
    }

    private static final class EyeButton extends JButton {

        EyeButton() {
            super("");
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFocusable(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int w = getWidth();
            int h = getHeight();
            int cx = w / 2;
            int cy = h / 2;

            boolean visible = Boolean.TRUE.equals(getClientProperty("visible"));
            boolean hover = getModel().isRollover();
            boolean pressed = getModel().isPressed();

            if (hover || visible) {
                g.setColor(new Color(214, 168, 91, pressed ? 58 : (visible ? 44 : 28)));
                g.fillRoundRect(2, 2, w - 4, h - 4, Math.max(8, h / 2), Math.max(8, h / 2));
            }

            Color eyeColor = visible
                    ? new Color(236, 203, 157, pressed ? 255 : 245)
                    : new Color(214, 168, 91, hover ? 215 : 125);

            g.setColor(eyeColor);
            g.setStroke(new BasicStroke(Math.max(1.4f, h / 18f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            Path2D eye = new Path2D.Double();
            eye.moveTo(swLocal(w, 5), cy);
            eye.curveTo(swLocal(w, 12), shLocal(h, 6), swLocal(w, 28), shLocal(h, 6), swLocal(w, 35), cy);
            eye.curveTo(swLocal(w, 28), shLocal(h, 28), swLocal(w, 12), shLocal(h, 28), swLocal(w, 5), cy);
            g.draw(eye);

            if (visible) {
                g.setColor(new Color(236, 203, 157, 55));
                g.fillOval(cx - swLocal(w, 8), cy - swLocal(w, 8), swLocal(w, 16), swLocal(w, 16));
                g.setColor(new Color(248, 248, 248, 238));
                g.fillOval(cx - swLocal(w, 4), cy - swLocal(w, 4), swLocal(w, 8), swLocal(w, 8));
            } else {
                g.fillOval(cx - swLocal(w, 3), cy - swLocal(w, 3), swLocal(w, 6), swLocal(w, 6));
                g.setColor(new Color(214, 168, 91, hover ? 230 : 155));
                g.setStroke(new BasicStroke(Math.max(1.6f, h / 16f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine(swLocal(w, 8), shLocal(h, 25), swLocal(w, 33), shLocal(h, 8));
            }

            g.dispose();
        }

        private int swLocal(int w, int v) {
            return Math.max(1, Math.round(v * w / 40f));
        }

        private int shLocal(int h, int v) {
            return Math.max(1, Math.round(v * h / 34f));
        }
    }

    private static final class ExploreButton extends JButton {

        ExploreButton(String text) {
            super(text);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 8));
            setForeground(new Color(236, 203, 157));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            boolean hover = getModel().isRollover();
            boolean press = getModel().isPressed();

            RoundRectangle2D body = new RoundRectangle2D.Double(
                    .5,
                    .5,
                    getWidth() - 1,
                    getHeight() - 1,
                    6,
                    6
            );

            Color top = hover
                    ? new Color(39, 31, 22, 238)
                    : new Color(8, 14, 20, 226);
            Color bottom = press
                    ? new Color(87, 59, 29, 242)
                    : new Color(3, 8, 13, 238);

            g.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
            g.fill(body);

            g.setPaint(new GradientPaint(
                    0, 0, new Color(255, 225, 169, hover ? 225 : 170),
                    getWidth(), getHeight(), new Color(176, 121, 55, hover ? 235 : 165)
            ));
            g.setStroke(new BasicStroke(press ? 1.65f : 1.1f));
            g.draw(body);

            g.setColor(new Color(255, 240, 209, hover ? 48 : 22));
            g.drawLine(10, 2, getWidth() - 10, 2);

            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class GoldButton extends JButton {

        GoldButton(String text) {
            super(text);

            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(new Color(28, 20, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color top = getModel().isRollover()
                    ? new Color(246, 214, 163)
                    : new Color(232, 202, 148);

            Color bottom = getModel().isPressed()
                    ? new Color(180, 134, 78)
                    : new Color(206, 165, 103);

            RoundRectangle2D shadow = new RoundRectangle2D.Double(
                    0,
                    3,
                    getWidth(),
                    getHeight() - 1,
                    12,
                    12
            );

            RoundRectangle2D body = new RoundRectangle2D.Double(
                    0,
                    0,
                    getWidth(),
                    getHeight() - 2,
                    12,
                    12
            );

            g.setColor(new Color(0, 0, 0, 55));
            g.fill(shadow);

            g.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
            g.fill(body);

            g.setColor(new Color(255, 255, 255, 55));
            g.drawLine(2, 2, getWidth() - 3, 2);

            g.dispose();

            super.paintComponent(raw);
        }
    }
    
}
