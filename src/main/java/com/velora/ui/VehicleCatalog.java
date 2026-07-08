
package com.velora.ui;

import com.velora.authentication.Customer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Locale;

public class VehicleCatalog extends JFrame {

    private static final Color BG = new Color(3, 7, 11);
    private static final Color PANEL = new Color(5, 10, 16, 235);
    private static final Color PANEL_2 = new Color(7, 13, 20, 230);
    private static final Color GOLD = new Color(214, 168, 91);
    private static final Color GOLD_LIGHT = new Color(238, 199, 140);
    private static final Color TEXT = new Color(245, 245, 245);
    private static final Color MUTED = new Color(170, 176, 186);
    private static final Color GREEN = new Color(67, 210, 103);
    private static final Color LINE = new Color(214, 168, 91, 75);

    private static final Path DATA_FILE = Paths.get("data", "vehicles.txt");
    private static final Path IMAGE_DIR = Paths.get("data", "vehicle-images");

    private static final int PAGE_SIZE = 8;

    private final Customer customer;
    private final ArrayList<VehicleItem> allVehicles = new ArrayList<>();
    private final ArrayList<VehicleItem> filteredVehicles = new ArrayList<>();
    private final JPanel cardsGrid = new JPanel();
    private final JTextField searchField = new JTextField();
    private JLabel pageInfoLabel;
    private JPanel paginationPanel;

    private String currentFilter = "ALL";
    private int currentPage = 1;

    public VehicleCatalog() {
        this(null);
    }

    public VehicleCatalog(Customer customer) {
        super("Velora Motors - Vehicle Collection");
        this.customer = customer;

        ensureDemoFile();
        allVehicles.addAll(readVehiclesFromFile());

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 720));
        setSize(1380, 820);
        setLocationRelativeTo(null);
        setContentPane(createRoot());

        refreshCards();
    }

    private JPanel createRoot() {
        JPanel root = new GradientRoot();
        root.setLayout(new BorderLayout());
        root.setBorder(new EmptyBorder(18, 18, 18, 18));
        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createContent(), BorderLayout.CENTER);
        return root;
    }

    private JPanel createHeader() {
        RoundedPanel header = new RoundedPanel(24);
        header.setBackground(new Color(4, 8, 13, 235));
        header.setLayout(new BorderLayout(16, 0));
        header.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("VEHICLE COLLECTION");
        title.setForeground(TEXT);
        title.setFont(new Font("Serif", Font.PLAIN, 32));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Browse premium cars, hybrid cars, electric vehicles, e-bikes, and motorcycles.");
        sub.setForeground(GOLD_LIGHT);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(sub);

        SearchBox search = new SearchBox();
        search.setPreferredSize(new Dimension(470, 46));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        right.add(createCustomerBadge());

        RoundedButton back = new RoundedButton("Back", 18);
        back.setPreferredSize(new Dimension(105, 46));
        back.setBackground(new Color(0, 0, 0, 0));
        back.setForeground(GOLD_LIGHT);
        back.setFont(new Font("Segoe UI", Font.BOLD, 14));
        back.addActionListener(e -> dispose());
        right.add(back);

        header.add(titleBlock, BorderLayout.WEST);
        header.add(search, BorderLayout.CENTER);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private JPanel createCustomerBadge() {
        JPanel badge = new JPanel(new BorderLayout(10, 0));
        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(210, 46));

        AvatarIcon avatar = new AvatarIcon(42);

        JPanel text = new JPanel(new GridLayout(2, 1));
        text.setOpaque(false);

        JLabel welcome = new JLabel("Welcome Back,");
        welcome.setForeground(MUTED);
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JLabel name = new JLabel(getCustomerName());
        name.setForeground(GOLD_LIGHT);
        name.setFont(new Font("Segoe UI", Font.BOLD, 14));

        text.add(welcome);
        text.add(name);

        badge.add(avatar, BorderLayout.WEST);
        badge.add(text, BorderLayout.CENTER);

        return badge;
    }

    private String getCustomerName() {
        if (customer == null || customer.getFullName() == null || customer.getFullName().trim().isEmpty()) {
            return "Omar Al-Khatib";
        }
        return customer.getFullName();
    }

    private JPanel createContent() {
        RoundedPanel content = new RoundedPanel(24);
        content.setBackground(PANEL);
        content.setLayout(new BorderLayout(0, 16));
        content.setBorder(new EmptyBorder(18, 18, 18, 18));

        content.add(createFilterBar(), BorderLayout.NORTH);
        content.add(createVehicleScroll(), BorderLayout.CENTER);
        content.add(createFooter(), BorderLayout.SOUTH);

        return content;
    }

    private JPanel createFilterBar() {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        JPanel filters = new JPanel(new BorderLayout());
        filters.setOpaque(false);

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftFilters.setOpaque(false);

        leftFilters.add(filterButton("All", "ALL"));
        leftFilters.add(filterButton("Cars", "CAR"));
        leftFilters.add(filterButton("Hybrid Cars", "HYBRID_CAR"));
        leftFilters.add(filterButton("Electric Vehicles", "ELECTRIC_VEHICLE"));
        leftFilters.add(filterButton("E-Bikes", "ELECTRIC_BIKE"));
        leftFilters.add(filterButton("Motorcycles", "MOTORCYCLE"));
        leftFilters.add(filterButton("Available Now", "AVAILABLE"));

        JLabel note = new JLabel("8 vehicles per page");
        note.setForeground(MUTED);
        note.setFont(new Font("Segoe UI", Font.BOLD, 12));

        filters.add(leftFilters, BorderLayout.WEST);
        filters.add(note, BorderLayout.EAST);

        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);
        stats.setBorder(new EmptyBorder(16, 0, 0, 0));

        stats.add(statCard("Available Vehicles", countAvailable(), "units", StatIconType.CAR));
        stats.add(statCard("Electric Units", countElectric(), "units", StatIconType.BOLT));
        stats.add(statCard("Hybrid Models", countByType("HYBRID_CAR"), "units", StatIconType.LEAF));
        stats.add(statCard("Bikes & Motorcycles", countBikes(), "units", StatIconType.BIKE));

        wrapper.add(filters);
        wrapper.add(stats);

        return wrapper;
    }

    private JButton filterButton(String text, String filter) {
        RoundedButton b = new RoundedButton(text, 16);
        b.setPreferredSize(new Dimension(Math.max(78, text.length() * 10 + 28), 38));
        b.setBackground(filter.equals(currentFilter) ? GOLD : new Color(0, 0, 0, 0));
        b.setForeground(filter.equals(currentFilter) ? new Color(25, 15, 7) : TEXT);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.addActionListener(e -> {
            currentFilter = filter;
            currentPage = 1;
            refreshCards();
        });
        return b;
    }

    private JPanel statCard(String title, int value, String suffix, StatIconType iconType) {
        RoundedPanel card = new RoundedPanel(18);
        card.setBackground(PANEL_2);
        card.setLayout(new BorderLayout(16, 0));
        card.setBorder(new EmptyBorder(13, 18, 13, 18));

        StatIcon icon = new StatIcon(iconType, 42);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(title);
        t.setForeground(TEXT);
        t.setFont(new Font("Segoe UI", Font.BOLD, 14));
        t.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel v = new JLabel(value + " " + suffix);
        v.setForeground(GOLD_LIGHT);
        v.setFont(new Font("Segoe UI", Font.PLAIN, 25));
        v.setAlignmentX(Component.LEFT_ALIGNMENT);

        text.add(Box.createVerticalGlue());
        text.add(t);
        text.add(Box.createVerticalStrut(3));
        text.add(v);
        text.add(Box.createVerticalGlue());

        card.add(icon, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);

        return card;
    }

    private JScrollPane createVehicleScroll() {
        cardsGrid.setOpaque(false);
        cardsGrid.setLayout(new GridLayout(0, 4, 14, 14));

        JScrollPane scroll = new JScrollPane(cardsGrid);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        return scroll;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        JLabel hint = new JLabel("Customer view shows AVAILABLE vehicles only. Admin adds cars/photos into data/vehicles.txt.");
        hint.setForeground(MUTED);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        paginationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        paginationPanel.setOpaque(false);

        pageInfoLabel = new JLabel();
        pageInfoLabel.setForeground(GOLD_LIGHT);
        pageInfoLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        footer.add(hint, BorderLayout.WEST);
        footer.add(paginationPanel, BorderLayout.EAST);

        return footer;
    }

    private void refreshCards() {
        cardsGrid.removeAll();
        filteredVehicles.clear();

        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);

        for (VehicleItem v : allVehicles) {
            if (!"AVAILABLE".equalsIgnoreCase(v.status)) {
                continue;
            }
            if (!matchesFilter(v)) {
                continue;
            }
            if (!query.isEmpty() && !v.matches(query)) {
                continue;
            }
            filteredVehicles.add(v);
        }

        int totalPages = Math.max(1, (int) Math.ceil(filteredVehicles.size() / (double) PAGE_SIZE));
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }
        if (currentPage < 1) {
            currentPage = 1;
        }

        if (filteredVehicles.isEmpty()) {
            cardsGrid.setLayout(new BorderLayout());
            cardsGrid.add(emptyState(), BorderLayout.CENTER);
        } else {
            cardsGrid.setLayout(new GridLayout(2, 4, 14, 14));

            int start = (currentPage - 1) * PAGE_SIZE;
            int end = Math.min(start + PAGE_SIZE, filteredVehicles.size());

            for (int i = start; i < end; i++) {
                cardsGrid.add(new VehicleCard(filteredVehicles.get(i)));
            }

            for (int i = end; i < start + PAGE_SIZE; i++) {
                JPanel empty = new JPanel();
                empty.setOpaque(false);
                cardsGrid.add(empty);
            }
        }

        updatePagination();

        cardsGrid.revalidate();
        cardsGrid.repaint();
    }

    private void updatePagination() {
        if (paginationPanel == null) {
            return;
        }

        paginationPanel.removeAll();

        int totalItems = filteredVehicles.size();
        int totalPages = Math.max(1, (int) Math.ceil(totalItems / (double) PAGE_SIZE));

        if (pageInfoLabel != null) {
            int first = totalItems == 0 ? 0 : ((currentPage - 1) * PAGE_SIZE + 1);
            int last = Math.min(currentPage * PAGE_SIZE, totalItems);
            pageInfoLabel.setText("Showing " + first + " - " + last + " of " + totalItems + " vehicles");
            paginationPanel.add(pageInfoLabel);
        }

        RoundedButton prev = paginationButton("<");
        prev.setEnabled(currentPage > 1);
        prev.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                refreshCards();
            }
        });
        paginationPanel.add(prev);

        for (int i = 1; i <= totalPages; i++) {
            int page = i;
            RoundedButton b = paginationButton(String.valueOf(i));
            b.setBackground(page == currentPage ? GOLD : new Color(0, 0, 0, 0));
            b.setForeground(page == currentPage ? new Color(25, 15, 7) : TEXT);
            b.addActionListener(e -> {
                currentPage = page;
                refreshCards();
            });
            paginationPanel.add(b);
        }

        RoundedButton next = paginationButton(">");
        next.setEnabled(currentPage < totalPages);
        next.addActionListener(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                refreshCards();
            }
        });
        paginationPanel.add(next);

        paginationPanel.revalidate();
        paginationPanel.repaint();
    }

    private RoundedButton paginationButton(String text) {
        RoundedButton b = new RoundedButton(text, 12);
        b.setPreferredSize(new Dimension(38, 30));
        b.setBackground(new Color(0, 0, 0, 0));
        b.setForeground(TEXT);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return b;
    }

    private boolean matchesFilter(VehicleItem v) {
        if ("ALL".equals(currentFilter) || "AVAILABLE".equals(currentFilter)) {
            return true;
        }
        if ("CAR".equals(currentFilter)) {
            return "CAR".equalsIgnoreCase(v.type) || "SUV".equalsIgnoreCase(v.type);
        }
        return currentFilter.equalsIgnoreCase(v.type);
    }

    private JPanel emptyState() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);

        JLabel label = new JLabel("No available vehicles found.");
        label.setForeground(MUTED);
        label.setFont(new Font("Segoe UI", Font.BOLD, 22));

        p.add(label);
        return p;
    }

    private int countAvailable() {
        int c = 0;
        for (VehicleItem v : allVehicles) {
            if ("AVAILABLE".equalsIgnoreCase(v.status)) {
                c++;
            }
        }
        return c;
    }

    private int countByType(String type) {
        int c = 0;
        for (VehicleItem v : allVehicles) {
            if ("AVAILABLE".equalsIgnoreCase(v.status) && type.equalsIgnoreCase(v.type)) {
                c++;
            }
        }
        return c;
    }

    private int countElectric() {
        int c = 0;
        for (VehicleItem v : allVehicles) {
            String type = v.type.toUpperCase(Locale.ROOT);
            if ("AVAILABLE".equalsIgnoreCase(v.status) && type.contains("ELECTRIC")) {
                c++;
            }
        }
        return c;
    }

    private int countBikes() {
        int c = 0;
        for (VehicleItem v : allVehicles) {
            if ("AVAILABLE".equalsIgnoreCase(v.status)
                    && ("MOTORCYCLE".equalsIgnoreCase(v.type) || "ELECTRIC_BIKE".equalsIgnoreCase(v.type))) {
                c++;
            }
        }
        return c;
    }

    private void showDetails(VehicleItem v) {
        String battery = v.hasBattery() ? "\\nBattery: " + v.battery + "%" : "";

        JOptionPane.showMessageDialog(
                this,
                v.brand + " " + v.model
                        + "\\nType: " + prettyType(v.type)
                        + "\\nPrice: $" + v.dailyPrice + " /day"
                        + "\\nStatus: " + v.status
                        + battery
                        + "\\n" + v.feature1
                        + "\\n" + v.feature2,
                "Vehicle Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void rentVehicle(VehicleItem v) {
        JOptionPane.showMessageDialog(
                this,
                "Rental flow will be implemented later for:\\n" + v.brand + " " + v.model,
                "Rent Vehicle",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private ArrayList<VehicleItem> readVehiclesFromFile() {
        ArrayList<VehicleItem> list = new ArrayList<>();

        if (!Files.exists(DATA_FILE)) {
            return list;
        }

        try {
            java.util.List<String> lines = Files.readAllLines(DATA_FILE, StandardCharsets.UTF_8);

            for (String line : lines) {
                if (line == null || line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }

                String[] p = line.split("\\|", -1);

                if (p.length < 10) {
                    continue;
                }

                list.add(new VehicleItem(
                        p[0].trim(),
                        p[1].trim(),
                        p[2].trim(),
                        p[3].trim(),
                        p[4].trim(),
                        parseDouble(p[5]),
                        p[6].trim(),
                        p[7].trim(),
                        p[8].trim(),
                        p[9].trim()
                ));
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not read vehicles.txt:\\n" + ex.getMessage());
        }

        return list;
    }

    private void ensureDemoFile() {
        try {
            Files.createDirectories(IMAGE_DIR);

            if (Files.exists(DATA_FILE)) {
                return;
            }

            Files.createDirectories(DATA_FILE.getParent());

            ArrayList<String> demo = new ArrayList<>();
            demo.add("# ID|Brand|Model|Type|Status|DailyPrice|Battery|Feature1|Feature2|ImagePath");
            demo.add("V001|BMW|X7 xDrive40i|CAR|AVAILABLE|95|N/A|7 Seats|3.0L Turbo|data/vehicle-images/bmw_x7.png");
            demo.add("V002|Toyota|Prius|HYBRID_CAR|AVAILABLE|45|N/A|5 Seats|4.4 L/100km|data/vehicle-images/toyota_prius.png");
            demo.add("V003|BMW|i7 M70|ELECTRIC_VEHICLE|AVAILABLE|120|100|5 Seats|Electric AWD|data/vehicle-images/bmw_i7.png");
            demo.add("V004|Xiaomi|Electric Bike Pro|ELECTRIC_BIKE|AVAILABLE|18|70|45 km Range|25 km/h|data/vehicle-images/electric_bike.png");
            demo.add("V005|Yamaha|MT-07|MOTORCYCLE|AVAILABLE|42|N/A|689cc|73 HP|data/vehicle-images/yamaha_mt07.png");
            demo.add("V006|Tesla|Model 3|ELECTRIC_VEHICLE|MAINTENANCE|85|50|5 Seats|491 km Range|data/vehicle-images/tesla_model3.png");

            Files.write(DATA_FILE, demo, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);

        } catch (IOException ignored) {
        }
    }

    private static double parseDouble(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception ex) {
            return 0.0;
        }
    }

    private static String prettyType(String type) {
        if (type == null) {
            return "";
        }
        return type.replace("_", " ");
    }

    private static BufferedImage loadImageFromPath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }

        try {
            String p = path.trim();

            if (p.startsWith("/")) {
                java.net.URL url = VehicleCatalog.class.getResource(p);
                return url == null ? null : ImageIO.read(url);
            }

            Path file = Paths.get(p);
            if (Files.exists(file)) {
                return ImageIO.read(file.toFile());
            }

        } catch (IOException ignored) {
        }

        return null;
    }


    private static final class AvatarIcon extends JPanel {
        private final int size;

        AvatarIcon(int size) {
            this.size = size;
            setOpaque(false);
            setPreferredSize(new Dimension(size, size));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int s = Math.min(getWidth(), getHeight()) - 2;
            int x = (getWidth() - s) / 2;
            int y = (getHeight() - s) / 2;

            Ellipse2D circle = new Ellipse2D.Double(x, y, s, s);

            g.setColor(new Color(6, 12, 19));
            g.fill(circle);

            g.setColor(GOLD);
            g.setStroke(new BasicStroke(1.5f));
            g.draw(circle);

            Shape oldClip = g.getClip();
            g.setClip(circle);

            g.setColor(new Color(228, 175, 130));
            g.fillOval(x + s / 2 - s / 8, y + s / 6, s / 4, s / 4);

            g.setColor(new Color(20, 22, 27));
            g.fillArc(x + s / 2 - s / 7, y + s / 8, s / 3, s / 4, 0, 180);

            Path2D suit = new Path2D.Double();
            suit.moveTo(x + s * 0.22, y + s);
            suit.lineTo(x + s * 0.34, y + s * 0.58);
            suit.lineTo(x + s * 0.50, y + s * 0.73);
            suit.lineTo(x + s * 0.66, y + s * 0.58);
            suit.lineTo(x + s * 0.78, y + s);
            suit.closePath();

            g.setColor(new Color(18, 20, 25));
            g.fill(suit);

            Path2D shirt = new Path2D.Double();
            shirt.moveTo(x + s * 0.41, y + s * 0.60);
            shirt.lineTo(x + s * 0.50, y + s * 0.78);
            shirt.lineTo(x + s * 0.59, y + s * 0.60);
            shirt.closePath();

            g.setColor(new Color(238, 238, 232));
            g.fill(shirt);

            Path2D tie = new Path2D.Double();
            tie.moveTo(x + s * 0.48, y + s * 0.63);
            tie.lineTo(x + s * 0.52, y + s * 0.63);
            tie.lineTo(x + s * 0.55, y + s * 0.87);
            tie.lineTo(x + s * 0.50, y + s * 0.95);
            tie.lineTo(x + s * 0.45, y + s * 0.87);
            tie.closePath();

            g.setColor(GOLD);
            g.fill(tie);

            g.setClip(oldClip);
            g.dispose();
        }
    }

    private final class SearchBox extends JPanel {
        SearchBox() {
            setOpaque(false);
            setLayout(new BorderLayout());

            searchField.setOpaque(false);
            searchField.setForeground(TEXT);
            searchField.setCaretColor(GOLD_LIGHT);
            searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            searchField.setBorder(new EmptyBorder(0, 48, 0, 42));

            searchField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    currentPage = 1;
                    refreshCards();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    currentPage = 1;
                    refreshCards();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    refreshCards();
                }
            });

            add(searchField, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            RoundRectangle2D box = new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);

            g.setColor(new Color(2, 5, 9, 210));
            g.fill(box);

            g.setColor(new Color(255, 255, 255, 30));
            g.draw(box);

            g.setColor(GOLD_LIGHT);
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Ellipse2D.Double(18, 14, 12, 12));
            g.drawLine(28, 24, 34, 30);

            if (searchField.getText().trim().isEmpty() && !searchField.hasFocus()) {
                g.setColor(MUTED);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                g.drawString("Search for vehicles, brands, or types...", 48, 30);
            }

            g.dispose();
            super.paintComponent(raw);
        }
    }

    private final class VehicleCard extends RoundedPanel {
        private final VehicleItem vehicle;
        private final BufferedImage image;

        VehicleCard(VehicleItem vehicle) {
            super(18);
            this.vehicle = vehicle;
            this.image = loadImageFromPath(vehicle.imagePath);

            setBackground(new Color(5, 10, 16, 230));
            setLayout(new BorderLayout());
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showDetails(vehicle);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics raw) {
            super.paintComponent(raw);

            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            int w = getWidth();
            int h = getHeight();
            int imageW = (int) (w * 0.50);

            Shape oldClip = g.getClip();
            RoundRectangle2D leftClip = new RoundRectangle2D.Double(0, 0, imageW, h, 18, 18);
            g.setClip(leftClip);

            if (image != null) {
                drawCover(g, image, 0, 0, imageW, h);
            } else {
                drawPlaceholder(g, 0, 0, imageW, h);
            }

            g.setClip(oldClip);

            int x = imageW + 16;
            int y = 24;

            g.setColor(TEXT);
            g.setFont(new Font("Segoe UI", Font.BOLD, 18));
            g.drawString(vehicle.brand + " " + vehicle.model, x, y);

            y += 24;

            g.setColor(GOLD_LIGHT);
            g.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g.drawString(prettyType(vehicle.type), x, y);

            y += 36;

            g.setColor(GOLD);
            g.setFont(new Font("Segoe UI", Font.BOLD, 28));
            g.drawString("$" + formatPrice(vehicle.dailyPrice), x, y);

            g.setColor(MUTED);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            g.drawString("/day", x + 82, y);

            y += 24;
            drawStatus(g, x, y);
            y += 34;

            if (vehicle.hasBattery()) {
                drawBattery(g, x, y, vehicle.batteryAsInt());
                y += 28;
            }

            g.setColor(MUTED);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            g.drawString(vehicle.feature1, x, Math.min(y, h - 52));
            g.drawString(vehicle.feature2, x + 105, Math.min(y, h - 52));

            drawButton(g, x, h - 40, 95, 28, "View Details", false);
            drawButton(g, x + 105, h - 40, 92, 28, "Rent Now", true);

            g.dispose();
        }

        private void drawStatus(Graphics2D g, int x, int y) {
            RoundRectangle2D pill = new RoundRectangle2D.Double(x, y - 17, 95, 24, 8, 8);
            g.setColor(new Color(23, 80, 34, 130));
            g.fill(pill);
            g.setColor(new Color(63, 170, 80, 180));
            g.draw(pill);

            g.setColor(GREEN);
            g.fillOval(x + 10, y - 9, 8, 8);

            g.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g.drawString("Available", x + 24, y);
        }

        private void drawBattery(Graphics2D g, int x, int y, int level) {
            g.setColor(MUTED);
            g.setFont(new Font("Segoe UI", Font.BOLD, 12));

            g.drawRoundRect(x, y - 14, 38, 16, 4, 4);
            g.drawRect(x + 38, y - 9, 3, 6);

            int fill = Math.max(0, Math.min(34, (int) (34 * (level / 100.0))));
            g.setColor(level >= 60 ? new Color(60, 160, 220) : GOLD_LIGHT);
            g.fillRoundRect(x + 2, y - 12, fill, 12, 4, 4);

            g.setColor(TEXT);
            g.drawString(level + "% Battery", x + 52, y);
        }

        private void drawButton(Graphics2D g, int x, int y, int w, int h, String text, boolean filled) {
            RoundRectangle2D b = new RoundRectangle2D.Double(x, y, w, h, 8, 8);

            if (filled) {
                g.setPaint(new GradientPaint(x, y, GOLD_LIGHT, x, y + h, new Color(130, 82, 28)));
                g.fill(b);
            } else {
                g.setColor(new Color(0, 0, 0, 90));
                g.fill(b);
            }

            g.setColor(GOLD);
            g.draw(b);

            g.setColor(filled ? new Color(20, 12, 5) : TEXT);
            g.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g.drawString(text, x + 12, y + 18);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(360, 230);
        }
    }

    private static void drawCover(Graphics2D g, BufferedImage img, int x, int y, int w, int h) {
        double s = Math.max(w / (double) img.getWidth(), h / (double) img.getHeight());

        int iw = (int) Math.round(img.getWidth() * s);
        int ih = (int) Math.round(img.getHeight() * s);

        int ix = x + (w - iw) / 2;
        int iy = y + (h - ih) / 2;

        g.drawImage(img, ix, iy, iw, ih, null);
    }

    private static void drawPlaceholder(Graphics2D g, int x, int y, int w, int h) {
        g.setPaint(new GradientPaint(x, y, new Color(17, 24, 33), x + w, y + h, new Color(4, 8, 12)));
        g.fillRect(x, y, w, h);

        g.setColor(new Color(214, 168, 91, 130));
        g.setFont(new Font("Serif", Font.PLAIN, 22));
        g.drawString("VELORA", x + 26, y + h / 2);
    }

    private static String formatPrice(double price) {
        if (Math.abs(price - Math.round(price)) < 0.001) {
            return String.valueOf((int) Math.round(price));
        }
        return String.format(Locale.US, "%.2f", price);
    }

    private static final class VehicleItem {
        private final String id;
        private final String brand;
        private final String model;
        private final String type;
        private final String status;
        private final double dailyPrice;
        private final String battery;
        private final String feature1;
        private final String feature2;
        private final String imagePath;

        VehicleItem(
                String id,
                String brand,
                String model,
                String type,
                String status,
                double dailyPrice,
                String battery,
                String feature1,
                String feature2,
                String imagePath
        ) {
            this.id = id;
            this.brand = brand;
            this.model = model;
            this.type = type;
            this.status = status;
            this.dailyPrice = dailyPrice;
            this.battery = battery;
            this.feature1 = feature1;
            this.feature2 = feature2;
            this.imagePath = imagePath;
        }

        boolean hasBattery() {
            return battery != null && !battery.equalsIgnoreCase("N/A") && !battery.trim().isEmpty();
        }

        int batteryAsInt() {
            try {
                return Integer.parseInt(battery.trim());
            } catch (Exception ex) {
                return 0;
            }
        }

        boolean matches(String q) {
            String all = (id + " " + brand + " " + model + " " + type + " " + status + " " + feature1 + " " + feature2)
                    .toLowerCase(Locale.ROOT);
            return all.contains(q);
        }
    }

    private enum StatIconType {
        CAR, BOLT, LEAF, BIKE
    }

    private static final class StatIcon extends JPanel {
        private final StatIconType type;
        private final int size;

        StatIcon(StatIconType type, int size) {
            this.type = type;
            this.size = size;
            setOpaque(false);
            setPreferredSize(new Dimension(size + 6, size + 6));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int s = size;
            int x = (getWidth() - s) / 2;
            int y = (getHeight() - s) / 2;

            g.setColor(GOLD_LIGHT);
            g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            if (type == StatIconType.CAR) {
                g.drawRoundRect(x + 4, y + 18, s - 8, 13, 6, 6);
                g.drawLine(x + 11, y + 18, x + 16, y + 9);
                g.drawLine(x + s - 11, y + 18, x + s - 16, y + 9);
                g.drawOval(x + 8, y + 29, 6, 6);
                g.drawOval(x + s - 14, y + 29, 6, 6);
            } else if (type == StatIconType.BOLT) {
                Path2D bolt = new Path2D.Double();
                bolt.moveTo(x + s * 0.58, y + 2);
                bolt.lineTo(x + s * 0.28, y + s * 0.55);
                bolt.lineTo(x + s * 0.50, y + s * 0.55);
                bolt.lineTo(x + s * 0.38, y + s - 2);
                bolt.lineTo(x + s * 0.74, y + s * 0.42);
                bolt.lineTo(x + s * 0.52, y + s * 0.42);
                bolt.closePath();
                g.draw(bolt);
            } else if (type == StatIconType.LEAF) {
                g.setColor(new Color(78, 210, 98));
                Path2D leaf = new Path2D.Double();
                leaf.moveTo(x + 5, y + s - 8);
                leaf.curveTo(x + 8, y + 6, x + s - 8, y + 4, x + s - 5, y + 5);
                leaf.curveTo(x + s - 4, y + s - 15, x + 20, y + s - 7, x + 5, y + s - 8);
                g.draw(leaf);
                g.drawLine(x + 10, y + s - 10, x + s - 12, y + 12);
            } else {
                g.drawOval(x + 7, y + 23, 8, 8);
                g.drawOval(x + s - 15, y + 23, 8, 8);
                g.drawLine(x + 11, y + 23, x + 18, y + 13);
                g.drawLine(x + 18, y + 13, x + 25, y + 23);
                g.drawLine(x + 18, y + 13, x + s - 11, y + 23);
                g.drawLine(x + 25, y + 23, x + 31, y + 13);
            }

            g.dispose();
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;

        RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            RoundRectangle2D body = new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            g.setColor(getBackground());
            g.fill(body);

            g.setColor(LINE);
            g.setStroke(new BasicStroke(1.0f));
            g.draw(body);

            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class RoundedButton extends JButton {
        private final int radius;

        RoundedButton(String text, int radius) {
            super(text);
            this.radius = radius;

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

            RoundRectangle2D body = new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            if (getModel().isRollover()) {
                g.setColor(new Color(214, 168, 91, 40));
            } else {
                g.setColor(getBackground());
            }

            g.fill(body);

            g.setColor(getModel().isRollover() ? GOLD_LIGHT : LINE);
            g.draw(body);

            g.dispose();
            super.paintComponent(raw);
        }
    }

    private static final class GradientRoot extends JPanel {
        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setPaint(new GradientPaint(0, 0, BG, getWidth(), getHeight(), new Color(7, 12, 19)));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setComposite(AlphaComposite.SrcOver.derive(0.15f));
            g.setColor(GOLD);
            g.fillOval(getWidth() - 280, -190, 430, 340);

            g.dispose();
        }
    }
}
