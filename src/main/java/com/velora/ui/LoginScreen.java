package com.velora.ui;

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

        private final LoginScreen frame;

        private final BufferedImage bg;
        private final BufferedImage bmwLogo;
        private final BufferedImage veloraLogo;
        private final BufferedImage featuredCar;

        private final RoundTextField username = new RoundTextField("Enter your username", false);
        private final RoundTextField password = new RoundTextField("Enter your password", true);

        private final JCheckBox remember = new JCheckBox("Remember Me", true);
        private final JButton forgot = ghostButton("Forgot Password?");
        private final JButton signIn = new GoldButton("SIGN IN      →");
        private final JButton explore = new ExploreButton("EXPLORE  →");

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

        VeloraCanvas(LoginScreen frame) {
            this.frame = frame;

            bg = loadFirst(
                    "/assets/backgrounds/velora-dealership.png",
                    "/images/velora-dealership.png",
                    "/assets/backgrounds/background.png",
                    "/images/background.png"
            );

            bmwLogo = loadFirst(
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
                    "/assets/backgrounds/bmw-i8-roadster.png",
                    "/assets/backgrounds/featured-car.png",
                    "/images/bmw-i8-roadster.png",
                    "/images/featured-car.png"
            );

            setLayout(null);
            setOpaque(true);

            remember.setOpaque(false);
            remember.setForeground(WHITE);
            remember.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            remember.setFocusPainted(false);
            remember.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            forgot.setForeground(PALE);
            forgot.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            forgot.addActionListener(e -> JOptionPane.showMessageDialog(this, "Please contact your administrator."));

            explore.setForeground(PALE);
            explore.setFont(new Font("Segoe UI", Font.BOLD, 11));

            signIn.addActionListener(e -> authenticate());
            password.addActionListener(e -> authenticate());

            min.addActionListener(e -> frame.setState(Frame.ICONIFIED));
            max.addActionListener(e -> frame.toggleMaximize());
            close.addActionListener(e -> frame.dispose());

            add(username);
            add(password);
            add(remember);
            add(forgot);
            add(signIn);
            add(explore);
            add(min);
            add(max);
            add(close);

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

            if (u.equalsIgnoreCase("manager") && p.equals("velora2026")) {
                JOptionPane.showMessageDialog(this, "Welcome to Velora Motors Management System.");
                return;
            }

            failed++;

            if (failed >= 3) {
                lockSeconds = 30;
                signIn.setEnabled(false);

                Timer t = new Timer(1000, null);

                t.addActionListener(e -> {
                    lockSeconds--;

                    if (lockSeconds <= 0) {
                        failed = 0;
                        signIn.setEnabled(true);
                        t.stop();
                    }

                    repaint();
                });

                t.start();
            }
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

            remember.setBounds(px, optionsY, fieldW / 2, sh(26));
            forgot.setBounds(px + fieldW / 2, optionsY, fieldW / 2, sh(26));

            signIn.setBounds(px, buttonY, fieldW, buttonH);

            int featuredY = rightY + loginH + sh(16);
            int featuredH = Math.max(sh(145), h - footerH() - featuredY - sh(18));

            // زر Explore أصغر ومرفوع داخل بطاقة Featured Model، بعيد عن خط الفوتر
            int exploreW = sw(104);
            int exploreH = sh(23);
            int exploreX = rightX + sw(28);
            int exploreY = Math.min(featuredY + sh(96), featuredY + featuredH - exploreH - sh(26));
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
            int signW = Math.min(sw(400), Math.max(sw(315), (int) (leftW * .325)));
            int signH = sh(42);
            int signX = leftX + (int) (leftW * .352);
            int signY = sh(230);
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

            String title = "VELORA MOTORS";
            Font signFont = new Font("Serif", Font.PLAIN, sf(24));
            Font fitted = fitFont(gg, title, signFont, w - sw(44));

            /*
             * شعار المحل صار راكب على واجهة المبنى:
             * - بدون مستطيل ظاهر.
             * - ميل أوضح مع منظور خط الواجهة.
             * - ظل خفيف جدًا فقط خلف الحروف عشان يبين كأنه مثبت على الجدار.
             * - شعار BMW صغرناه وخففنا الهالة الكبيرة حوله.
             */
            double angle = Math.toRadians(-3.9);
            int textBaseY = y + sh(25);

            Graphics2D textG = (Graphics2D) gg.create();
            textG.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            textG.rotate(angle, x + w / 2.0, y + h / 2.0);

            textG.setFont(fitted);
            FontMetrics fm = textG.getFontMetrics();
            int textW = fm.stringWidth(title);
            int tx = x + (w - textW) / 2;

            // ظل جداري ناعم بدل البوكس، يعطي دمج مع الواجهة
            textG.setComposite(AlphaComposite.SrcOver.derive(.32f));
            textG.setColor(new Color(0, 0, 0, 135));
            textG.fillRoundRect(tx - sw(12), textBaseY - sh(25), textW + sw(24), sh(35), sh(5), sh(5));

            // ظل الحروف
            textG.setComposite(AlphaComposite.SrcOver.derive(1f));
            textG.setColor(new Color(0, 0, 0, 210));
            textG.drawString(title, tx + sw(3), textBaseY + sh(3));

            // النص الأساسي
            textG.setColor(new Color(255, 255, 255, 248));
            textG.drawString(title, tx, textBaseY);

            // لمعة متحركة على الحروف نفسها
            int shine = tx - sw(45) + (int) (shimmer * (textW + sw(90)));
            Shape oldClip = textG.getClip();
            textG.setClip(new Rectangle(tx, textBaseY - sh(28), textW, sh(36)));
            textG.setComposite(AlphaComposite.SrcOver.derive(.24f));
            textG.setStroke(new BasicStroke(ss(2.4f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            textG.setColor(new Color(255, 232, 188, 145));
            textG.drawLine(shine, textBaseY - sh(26), shine + sw(32), textBaseY + sh(6));
            textG.setClip(oldClip);

            // خط ذهبي رفيع جدًا تحت الاسم، نفس ميل واجهة المحل
            textG.setComposite(AlphaComposite.SrcOver.derive(.18f + .12f * pulse));
            textG.setStroke(new BasicStroke(ss(1.1f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            textG.setColor(new Color(214, 168, 91, 110));
            textG.drawLine(tx + sw(6), textBaseY + sh(7), tx + textW - sw(8), textBaseY + sh(4));
            textG.dispose();

            // شعار BMW متوازن مع النص وبدون هالة كبيرة
            int logoSize = sh(40);
            int logoX = x + w + sw(28);
            int logoY = y - sh(1);

            gg.setComposite(AlphaComposite.SrcOver.derive(.12f + .12f * pulse));
            gg.setColor(new Color(255, 255, 255, 82));
            gg.fillOval(logoX - sw(5), logoY - sh(5), logoSize + sw(10), logoSize + sh(10));

            gg.setComposite(AlphaComposite.SrcOver.derive(.08f + .10f * pulse));
            gg.setColor(new Color(214, 168, 91, 70));
            gg.fillOval(logoX - sw(10), logoY - sh(10), logoSize + sw(20), logoSize + sh(20));

            gg.setComposite(AlphaComposite.SrcOver);
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
                        y + sh(70),
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

            for (int i = 0; i < 4; i++) {
                int cx = x + i * cell + cell / 2;

                drawFitCentered(
                        g,
                        value[i],
                        cx,
                        y + sh(31),
                        cell,
                        new Font("Segoe UI", Font.BOLD, sf(20)),
                        WHITE
                );

                drawFitCentered(
                        g,
                        label[i],
                        cx,
                        y + sh(56),
                        cell,
                        new Font("Segoe UI", Font.PLAIN, sf(10)),
                        BLUE_TEXT
                );
            }
        }

        private void drawLoginCard(Graphics2D g, int w, int h) {
            int margin = sideMargin();
            int cw = rightW();
            int x = w - margin - cw;
            int y = topY();
            int ch = loginH();
            int cx = x + cw / 2;

            drawGlass(g, x, y, cw, ch, 18, 188);

            drawBMW(g, cx - sw(30), y + sh(26), sw(60));

            drawFitCentered(
                    g,
                    "M A N A G E R   L O G I N",
                    cx,
                    y + sh(132),
                    cw - sw(50),
                    new Font("Segoe UI", Font.PLAIN, sf(27)),
                    PALE
            );

            drawFitCentered(
                    g,
                    "Welcome back! Please sign in to continue.",
                    cx,
                    y + sh(168),
                    cw - sw(50),
                    new Font("Segoe UI", Font.PLAIN, sf(13)),
                    WHITE
            );

            g.setColor(new Color(214, 168, 91, 170));
            g.setStroke(new BasicStroke(ss(1.5f)));
            g.drawLine(cx - sw(30), y + sh(190), cx + sw(30), y + sh(190));

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
            drawNotice(g, noticeX, errorY, noticeW, sh(74), true);
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

                String sec = lockSeconds > 0 ? String.valueOf(lockSeconds) : "30";

                gg.setColor(new Color(255, 92, 92));
                gg.setFont(new Font("Segoe UI", Font.PLAIN, sf(11)));
                gg.drawString("Too many failed attempts.", x + sw(58), y + sh(28));
                gg.drawString("Wait " + sec + " seconds before trying again.", x + sw(58), y + sh(52));

                int badgeSize = sw(38);
                int badgeX = x + w - badgeSize - sw(10);
                int badgeY = y + (h - badgeSize) / 2;

                gg.setColor(new Color(135, 20, 20, 210));
                gg.fillOval(badgeX, badgeY, badgeSize, badgeSize);

                gg.setColor(new Color(255, 76, 76));
                gg.drawOval(badgeX, badgeY, badgeSize, badgeSize);

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

            int y = loginY + loginH + sh(16);
            int fh = Math.max(sh(145), h - footerH() - y - sh(18));

            drawGlass(g, x, y, fw, fh, 14, 150);

            Shape oldClip = g.getClip();

            RoundRectangle2D rightClip = new RoundRectangle2D.Double(
                    x + fw * .53,
                    y,
                    fw * .47,
                    fh,
                    sh(14),
                    sh(14)
            );

            g.setClip(rightClip);

            if (featuredCar != null) {
                drawCover(g, featuredCar, x + (int) (fw * .53), y, (int) (fw * .47), fh);
            } else if (bg != null) {
                g.drawImage(
                        bg,
                        x + (int) (fw * .53),
                        y,
                        x + fw,
                        y + fh,
                        bg.getWidth() / 5,
                        bg.getHeight() / 2,
                        bg.getWidth() * 4 / 5,
                        bg.getHeight(),
                        null
                );
            }

            g.setClip(oldClip);

            int leftX = x + sw(28);

            g.setColor(PALE);
            g.setFont(new Font("Segoe UI", Font.BOLD, sf(9)));
            g.drawString("FEATURED MODEL", leftX, y + sh(28));

            g.setColor(WHITE);
            g.setFont(new Font("Segoe UI", Font.BOLD, sf(18)));
            g.drawString("BMW i8 ROADSTER", leftX, y + sh(56));

            g.setColor(MUTED);
            g.setFont(new Font("Segoe UI", Font.PLAIN, sf(11)));
            g.drawString("Future meets performance.", leftX, y + sh(83));

            // خط فاصل صغير فوق الزر، حتى تبقى البطاقة مرتبة وما يطلع الزر على الفوتر
            int lineY = y + sh(91);
            g.setComposite(AlphaComposite.SrcOver.derive(.45f));
            g.setColor(new Color(214, 168, 91, 72));
            g.drawLine(leftX, lineY, leftX + sw(104), lineY);
            g.setComposite(AlphaComposite.SrcOver);

            // لمعة قصيرة فوق منطقة الزر بدل أسفل البطاقة
            int shineX = leftX + (int) (shimmer * sw(82));
            g.setComposite(AlphaComposite.SrcOver.derive(.24f));
            g.setColor(new Color(255, 226, 172, 115));
            g.drawLine(shineX, lineY + sh(12), shineX + sw(24), lineY + sh(12));
            g.setComposite(AlphaComposite.SrcOver);
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
            if (bmwLogo != null) {
                drawContain(g, bmwLogo, x, y, size, size);
                return;
            }

            int in4 = Math.max(2, size / 15);
            int in12 = Math.max(6, size / 5);
            int d = size - in12 * 2;
            int xx = x + in12;
            int yy = y + in12;

            g.setColor(WHITE);
            g.fillOval(x, y, size, size);

            g.setColor(new Color(4, 8, 13));
            g.fillOval(x + in4, y + in4, size - in4 * 2, size - in4 * 2);

            g.setColor(WHITE);
            g.fillArc(xx, yy, d, d, 0, 90);
            g.fillArc(xx, yy, d, d, 180, 90);

            g.setColor(new Color(20, 126, 199));
            g.fillArc(xx, yy, d, d, 90, 90);
            g.fillArc(xx, yy, d, d, 270, 90);

            g.setColor(new Color(255, 255, 255, 200));
            g.setStroke(new BasicStroke(ss(2f)));
            g.drawOval(xx, yy, d, d);
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

    private static final class RoundTextField extends JPasswordField {

        private final String placeholder;

        RoundTextField(String placeholder, boolean passwordMode) {
            this.placeholder = placeholder;

            setEchoChar(passwordMode ? '•' : (char) 0);
            setOpaque(false);
            setForeground(new Color(248, 248, 248));
            setCaretColor(new Color(214, 168, 91));
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setBorder(new EmptyBorder(0, 48, 0, 14));
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
                    7,
                    7
            );

            g.setColor(new Color(8, 14, 20, 230));
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

            g.setColor(new Color(4, 9, 15, hover ? 235 : 210));
            g.fill(body);

            g.setPaint(new GradientPaint(
                    0,
                    0,
                    new Color(255, 231, 180, hover ? 145 : 95),
                    getWidth(),
                    getHeight(),
                    new Color(214, 168, 91, hover ? 170 : 115)
            ));
            g.setStroke(new BasicStroke(press ? 1.55f : 1.05f));
            g.draw(body);

            g.setColor(new Color(255, 255, 255, hover ? 34 : 18));
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
                    7,
                    7
            );

            RoundRectangle2D body = new RoundRectangle2D.Double(
                    0,
                    0,
                    getWidth(),
                    getHeight() - 2,
                    7,
                    7
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
