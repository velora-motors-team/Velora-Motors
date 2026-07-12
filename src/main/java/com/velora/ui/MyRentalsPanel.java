package com.velora.ui;

import com.velora.authentication.Customer;
import com.velora.repository.RentalRepository;
import com.velora.service.RentalDatabaseService;
import com.velora.service.VehicleService;
import com.velora.strategy.LateFeeContext;
import com.velora.strategy.LateFeeStrategy;
import com.velora.strategy.LateFeeStrategyFactory;
import com.velora.vehicle.Vehicle;
import com.velora.vehicle.VehicleStatus;
import java.time.Duration;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class MyRentalsPanel extends JPanel {

    private static final Color CARD = new Color(7, 14, 21);
    private static final Color CARD_DARK = new Color(4, 10, 16);
    private static final Color CARD_HOVER = new Color(12, 23, 33);

    private static final Color GOLD = new Color(214, 160, 66);
    private static final Color GOLD_LIGHT = new Color(239, 196, 116);
    private static final Color PALE = new Color(238, 201, 139);

    private static final Color TEXT = new Color(243, 244, 247);
    private static final Color MUTED = new Color(170, 179, 192);

    private static final Color GREEN = new Color(86, 207, 114);
    private static final Color RED = new Color(235, 93, 98);
    private static final Color ORANGE = new Color(242, 174, 73);
    private static final Duration GRACE_PERIOD = Duration.ofMinutes(30);
    
    private JLabel nextReturnValue;
private JLabel nextReturnVehicle;

private JLabel paymentStatusValue;
private JLabel paymentStatusDescription;

private JLabel onTimeRateValue;
private JLabel onTimeRateDescription;

    private final Customer customer;
    private final CustomerAccountState accountState;
    private final RentalRepository rentalRepository = new RentalRepository();
    private final RentalDatabaseService rentalDatabaseService = new RentalDatabaseService();
    private final VehicleService vehicleService = new VehicleService();
    private final LateFeeContext lateFeeContext = new LateFeeContext();
    private final Timer countdownTimer;
    private boolean refreshInProgress;
    private JLabel activeRentalsValue;
    private JLabel completedRentalsValue;
    private JLabel currentCostValue;
    private JLabel lateReturnsValue;
    private JPanel activeRentalsList;
    private JPanel historyList;

    public MyRentalsPanel(Customer customer) {

        this.customer = customer;
        this.accountState = CustomerAccountState.forCustomer(customer);

        setOpaque(false);
        setLayout(new BorderLayout(0, 14));

        setBorder(
                new EmptyBorder(
                        16,
                        22,
                        16,
                        22
                )
        );

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);

        accountState.addChangeListener(this::refreshRentals);

        countdownTimer = new Timer(1000, e -> refreshRentals());
        countdownTimer.setInitialDelay(1000);
        countdownTimer.start();

        refreshRentals();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (countdownTimer != null && !countdownTimer.isRunning()) {
            countdownTimer.start();
        }
    }

    @Override
    public void removeNotify() {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
        super.removeNotify();
    }

    /* =========================================================
       HEADER
       ========================================================= */

    private JComponent createHeader() {

        JPanel header = new JPanel(
                new BorderLayout(24, 0)
        );

        header.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);

        left.setLayout(
                new BoxLayout(
                        left,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel eyebrow = label(
                "RENTAL CENTER",
                11,
                Font.BOLD,
                GOLD
        );

        eyebrow.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        JLabel title = label(
                "My Rentals",
                29,
                Font.BOLD,
                TEXT
        );

        title.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        JLabel sub = label(
                "Manage active rentals, return schedules, payment status, and your complete rental history.",
                13,
                Font.PLAIN,
                MUTED
        );

        sub.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        left.add(eyebrow);
        left.add(Box.createVerticalStrut(5));
        left.add(title);
        left.add(Box.createVerticalStrut(6));
        left.add(sub);

        JPanel right = new JPanel();
        right.setOpaque(false);

        right.setLayout(
                new BoxLayout(
                        right,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel status = label(
                "●  Active Customer",
                13,
                Font.BOLD,
                GREEN
        );

        status.setAlignmentX(
                Component.RIGHT_ALIGNMENT
        );

        status.setHorizontalAlignment(
                SwingConstants.RIGHT
        );

        JLabel customerName = label(
                getCustomerDisplayName(),
                11,
                Font.PLAIN,
                MUTED
        );

        customerName.setAlignmentX(
                Component.RIGHT_ALIGNMENT
        );

        customerName.setHorizontalAlignment(
                SwingConstants.RIGHT
        );

        right.add(Box.createVerticalGlue());
        right.add(status);
        right.add(Box.createVerticalStrut(6));
        right.add(customerName);
        right.add(Box.createVerticalGlue());

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private String getCustomerDisplayName() {

        if (customer == null) {
            return "Velora Member";
        }

        try {

            String value = customer.toString();

            if (value != null
                    && !value.trim().isEmpty()) {

                return value;
            }

        } catch (Exception ignored) {
        }

        return "Velora Member";
    }

    /* =========================================================
       BODY
       ========================================================= */

    private JComponent createBody() {

        JPanel body = new JPanel();
        body.setOpaque(false);

        body.setLayout(
                new BoxLayout(
                        body,
                        BoxLayout.Y_AXIS
                )
        );

        /*
         * نزول بسيط للبطاقات
         */
        body.add(
                Box.createVerticalStrut(34)
        );

        /* =====================================================
           STAT CARDS
           ===================================================== */

        JComponent stats = createStatsSection();

        stats.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        stats.setPreferredSize(
                new Dimension(
                        10,
                        102
                )
        );

        stats.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        102
                )
        );

        body.add(stats);

        body.add(
                Box.createVerticalStrut(22)
        );

        /* =====================================================
           RENTALS AREA
           ===================================================== */

        JPanel rentalsArea = new JPanel(
                new GridLayout(
                        1,
                        2,
                        20,
                        0
                )
        );

        rentalsArea.setOpaque(false);

        rentalsArea.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        /*
         * زدت الارتفاع حتى الخطوط
         * والحالات ما تنقص
         */
        rentalsArea.setPreferredSize(
                new Dimension(
                        10,
                        430
                )
        );

        rentalsArea.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        430
                )
        );

        rentalsArea.add(
                createActiveRentalCard()
        );

        rentalsArea.add(
                createHistoryCard()
        );

        body.add(rentalsArea);

        body.add(
                Box.createVerticalStrut(20)
        );

        /* =====================================================
           RENTAL INSIGHTS
           ===================================================== */

        JComponent insights =
                createInsightsSection();

        insights.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        insights.setPreferredSize(
                new Dimension(
                        10,
                        118
                )
        );

        insights.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        118
                )
        );

        body.add(insights);

        body.add(
                Box.createVerticalGlue()
        );

        return body;
    }

    /* =========================================================
       STATS
       ========================================================= */

    private JComponent createStatsSection() {

        JPanel stats = new JPanel(
                new GridLayout(
                        1,
                        4,
                        16,
                        0
                )
        );

        stats.setOpaque(false);

        activeRentalsValue = valueLabel();
        completedRentalsValue = valueLabel();
        currentCostValue = valueLabel();
        lateReturnsValue = valueLabel();

        stats.add(
                statCard(
                        activeRentalsValue,
                        "Active Rentals",
                        "Currently rented",
                        StatIconType.CAR
                )
        );

        stats.add(
                statCard(
                        completedRentalsValue,
                        "Completed",
                        "Rental history",
                        StatIconType.CHECK
                )
        );

        stats.add(
                statCard(
                        currentCostValue,
                        "Current Cost",
                        "Active rental fees",
                        StatIconType.MONEY
                )
        );

        stats.add(
                statCard(
                        lateReturnsValue,
                        "Late Returns",
                        "Overdue rentals",
                        StatIconType.CLOCK
                )
        );

        return stats;
    }

    private JComponent statCard(
            JLabel valueLabel,
            String title,
            String desc,
            StatIconType iconType
    ) {

        HoverRoundedPanel card =
                new HoverRoundedPanel(
                        16,
                        CARD,
                        CARD_HOVER
                );

        card.setLayout(
                new BorderLayout(
                        14,
                        0
                )
        );

        card.setBorder(
                new EmptyBorder(
                        14,
                        16,
                        14,
                        16
                )
        );

        StatIcon icon =
                new StatIcon(iconType);

        icon.setPreferredSize(
                new Dimension(
                        48,
                        48
                )
        );

        JPanel text = new JPanel();
        text.setOpaque(false);

        text.setLayout(
                new BoxLayout(
                        text,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel titleLabel = label(
                title,
                13,
                Font.BOLD,
                TEXT
        );

        JLabel descLabel = label(
                desc,
                11,
                Font.PLAIN,
                MUTED
        );

        text.add(titleLabel);
        text.add(Box.createVerticalStrut(5));
        text.add(valueLabel);
        text.add(Box.createVerticalStrut(5));
        text.add(descLabel);

        card.add(icon, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);

        return card;
    }

    /* =========================================================
       ACTIVE RENTALS
       ========================================================= */

    private JComponent createActiveRentalCard() {

    RoundedPanel card = new RoundedPanel(16, CARD);
    card.setLayout(new BorderLayout());
    card.setBorder(new EmptyBorder(18, 18, 18, 10));

    activeRentalsList = new JPanel();
    activeRentalsList.setOpaque(false);

    activeRentalsList.setLayout(
            new BoxLayout(
                    activeRentalsList,
                    BoxLayout.Y_AXIS
            )
    );

    JScrollPane scrollPane = new JScrollPane(activeRentalsList);

    scrollPane.setOpaque(false);
    scrollPane.getViewport().setOpaque(false);

    scrollPane.setBorder(
            BorderFactory.createEmptyBorder()
    );

    scrollPane.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
    );

    scrollPane.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
    );

    scrollPane.getVerticalScrollBar().setUnitIncrement(18);

    scrollPane.getVerticalScrollBar().setPreferredSize(
            new Dimension(7, 0)
    );

    scrollPane.getVerticalScrollBar().setUI(
            new LuxuryScrollBarUI()
    );

    card.add(scrollPane, BorderLayout.CENTER);

    return card;
}

    /* =========================================================
       RENTAL HISTORY
       ========================================================= */

    private JComponent createHistoryCard() {

        RoundedPanel card = new RoundedPanel(16, CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        historyList = new JPanel();
        historyList.setOpaque(false);
        historyList.setLayout(new BoxLayout(historyList, BoxLayout.Y_AXIS));

        card.add(historyList, BorderLayout.CENTER);
        return card;
    }

    /* =========================================================
       SECTION HEADER
       ========================================================= */

    private JComponent sectionHeader(
            String title,
            String badgeText,
            Color badgeColor
    ) {

        JPanel header = new JPanel(
                new BorderLayout()
        );

        header.setOpaque(false);

        header.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        30
                )
        );

        JLabel titleLabel = label(
                title,
                18,
                Font.BOLD,
                TEXT
        );

        RoundedLabel badge =
                new RoundedLabel(
                        badgeText,
                        badgeColor,
                        new Color(
                                badgeColor.getRed(),
                                badgeColor.getGreen(),
                                badgeColor.getBlue(),
                                28
                        )
                );

        header.add(
                titleLabel,
                BorderLayout.WEST
        );

        header.add(
                badge,
                BorderLayout.EAST
        );

        return header;
    }

    /* =========================================================
       RENTAL ROW
       ========================================================= */

    private JComponent rentalRow(
            String vehicle,
            String date,
            String price,
            String status,
            Color statusColor,
            String category
    ) {

        HoverRoundedPanel row =
                new HoverRoundedPanel(
                        12,
                        CARD_DARK,
                        new Color(
                                9,
                                18,
                                27
                        )
                );

        row.setLayout(
                new BorderLayout(
                        14,
                        0
                )
        );

        /*
         * أقل Padding عمودي
         * حتى النصوص تظل واضحة
         */
        row.setBorder(
                new EmptyBorder(
                        10,
                        13,
                        10,
                        13
                )
        );

        /*
         * أهم تعديل:
         * ارتفاع أكبر للصف
         */
        row.setPreferredSize(
                new Dimension(
                        10,
                        94
                )
        );

        row.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        94
                )
        );

        VehicleIcon vehicleIcon =
                new VehicleIcon(category);

        vehicleIcon.setPreferredSize(
                new Dimension(
                        50,
                        50
                )
        );

        /* =========================
           INFO
           ========================= */

        JPanel info = new JPanel();
        info.setOpaque(false);

        info.setLayout(
                new BoxLayout(
                        info,
                        BoxLayout.Y_AXIS
                )
        );

        JPanel topLine = new JPanel();
        topLine.setOpaque(false);

        topLine.setLayout(
                new BoxLayout(
                        topLine,
                        BoxLayout.X_AXIS
                )
        );

        topLine.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        JLabel vehicleLabel = label(
                vehicle,
                14,
                Font.BOLD,
                TEXT
        );

        RoundedLabel categoryBadge =
                new RoundedLabel(
                        category,
                        GOLD,
                        new Color(
                                214,
                                160,
                                66,
                                18
                        )
                );

        categoryBadge.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        9
                )
        );

        topLine.add(vehicleLabel);

        topLine.add(
                Box.createHorizontalStrut(8)
        );

        topLine.add(categoryBadge);

        topLine.add(
                Box.createHorizontalGlue()
        );

        JLabel dateLabel = label(
                date,
                11,
                Font.PLAIN,
                MUTED
        );

        JLabel statusLabel = label(
                "●  " + status,
                11,
                Font.BOLD,
                statusColor
        );

        info.add(topLine);

        info.add(
                Box.createVerticalStrut(4)
        );

        info.add(dateLabel);

        info.add(
                Box.createVerticalStrut(4)
        );

        info.add(statusLabel);

        /* =========================
           PRICE
           ========================= */

        JPanel priceBox = new JPanel();
        priceBox.setOpaque(false);

        priceBox.setLayout(
                new BoxLayout(
                        priceBox,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel priceLabel = label(
                price,
                18,
                Font.BOLD,
                PALE
        );

        JLabel feeLabel = label(
                "total",
                10,
                Font.PLAIN,
                MUTED
        );

        priceLabel.setAlignmentX(
                Component.RIGHT_ALIGNMENT
        );

        feeLabel.setAlignmentX(
                Component.RIGHT_ALIGNMENT
        );

        priceBox.add(
                Box.createVerticalGlue()
        );

        priceBox.add(priceLabel);

        priceBox.add(
                Box.createVerticalStrut(2)
        );

        priceBox.add(feeLabel);

        priceBox.add(
                Box.createVerticalGlue()
        );

        row.add(
                vehicleIcon,
                BorderLayout.WEST
        );

        row.add(
                info,
                BorderLayout.CENTER
        );

        row.add(
                priceBox,
                BorderLayout.EAST
        );

        return row;
    }

    /* =========================================================
       RENTAL INSIGHTS
       ========================================================= */

    private JComponent createInsightsSection() {

    RoundedPanel shell =
            new RoundedPanel(
                    16,
                    new Color(6, 13, 20)
            );

    shell.setLayout(
            new BorderLayout(24, 0)
    );

    shell.setBorder(
            new EmptyBorder(14, 18, 14, 18)
    );

    shell.setPreferredSize(
            new Dimension(10, 118)
    );

    shell.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 118)
    );

    JPanel titleArea = new JPanel();
    titleArea.setOpaque(false);

    titleArea.setLayout(
            new BoxLayout(
                    titleArea,
                    BoxLayout.Y_AXIS
            )
    );

    titleArea.setPreferredSize(
            new Dimension(220, 82)
    );

    JLabel eyebrow = label(
            "RENTAL INSIGHTS",
            11,
            Font.BOLD,
            GOLD
    );

    JLabel title = label(
            "Your rental snapshot",
            18,
            Font.BOLD,
            TEXT
    );

    JLabel sub = label(
            "Quick status at a glance",
            11,
            Font.PLAIN,
            MUTED
    );

    eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);
    title.setAlignmentX(Component.LEFT_ALIGNMENT);
    sub.setAlignmentX(Component.LEFT_ALIGNMENT);

    titleArea.add(Box.createVerticalGlue());
    titleArea.add(eyebrow);
    titleArea.add(Box.createVerticalStrut(4));
    titleArea.add(title);
    titleArea.add(Box.createVerticalStrut(4));
    titleArea.add(sub);
    titleArea.add(Box.createVerticalGlue());


    /*
     * Dynamic labels
     */

    nextReturnValue = label(
            "No active rentals",
            17,
            Font.BOLD,
            TEXT
    );

    nextReturnVehicle = label(
            "Nothing scheduled",
            10,
            Font.PLAIN,
            MUTED
    );


    paymentStatusValue = label(
            "-",
            17,
            Font.BOLD,
            TEXT
    );

    paymentStatusDescription = label(
            "No payment data",
            10,
            Font.PLAIN,
            MUTED
    );


    onTimeRateValue = label(
            "-",
            17,
            Font.BOLD,
            TEXT
    );

    onTimeRateDescription = label(
            "No completed rentals yet",
            10,
            Font.PLAIN,
            MUTED
    );


    JPanel metrics = new JPanel(
            new GridLayout(1, 3, 16, 0)
    );

    metrics.setOpaque(false);


    metrics.add(
            dynamicInsightCard(
                    nextReturnValue,
                    "Next Return",
                    nextReturnVehicle,
                    ORANGE
            )
    );


    metrics.add(
            dynamicInsightCard(
                    paymentStatusValue,
                    "Payment Status",
                    paymentStatusDescription,
                    GREEN
            )
    );


    metrics.add(
            dynamicInsightCard(
                    onTimeRateValue,
                    "On-time Return Rate",
                    onTimeRateDescription,
                    GOLD
            )
    );


    shell.add(
            titleArea,
            BorderLayout.WEST
    );

    shell.add(
            metrics,
            BorderLayout.CENTER
    );

    return shell;
}

    private JComponent dynamicInsightCard(
        JLabel valueLabel,
        String title,
        JLabel descriptionLabel,
        Color accent
) {

    RoundedPanel card =
            new RoundedPanel(
                    14,
                    CARD_DARK
            );

    card.setLayout(
            new BorderLayout(12, 0)
    );

    card.setBorder(
            new EmptyBorder(
                    12,
                    14,
                    12,
                    14
            )
    );

    AccentDot dot =
            new AccentDot(accent);

    dot.setPreferredSize(
            new Dimension(14, 14)
    );

    JPanel text = new JPanel();
    text.setOpaque(false);

    text.setLayout(
            new BoxLayout(
                    text,
                    BoxLayout.Y_AXIS
            )
    );

    JLabel titleLabel = label(
            title,
            11,
            Font.BOLD,
            MUTED
    );

    titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    text.add(Box.createVerticalGlue());
    text.add(titleLabel);
    text.add(Box.createVerticalStrut(4));
    text.add(valueLabel);
    text.add(Box.createVerticalStrut(4));
    text.add(descriptionLabel);
    text.add(Box.createVerticalGlue());

    card.add(
            dot,
            BorderLayout.WEST
    );

    card.add(
            text,
            BorderLayout.CENTER
    );

    return card;
}
        
    private JComponent insightCard(
            String value,
            String title,
            String desc,
            Color accent
    ) {

        RoundedPanel card =
                new RoundedPanel(
                        14,
                        CARD_DARK
                );

        card.setLayout(
                new BorderLayout(
                        12,
                        0
                )
        );

        card.setBorder(
                new EmptyBorder(
                        12,
                        14,
                        12,
                        14
                )
        );

        AccentDot dot =
                new AccentDot(accent);

        dot.setPreferredSize(
                new Dimension(
                        14,
                        14
                )
        );

        JPanel text = new JPanel();
        text.setOpaque(false);

        text.setLayout(
                new BoxLayout(
                        text,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel titleLabel = label(
                title,
                11,
                Font.BOLD,
                MUTED
        );

        JLabel valueLabel = label(
                value,
                17,
                Font.BOLD,
                TEXT
        );

        JLabel descLabel = label(
                desc,
                10,
                Font.PLAIN,
                MUTED
        );

        titleLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        valueLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        descLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        text.add(
                Box.createVerticalGlue()
        );

        text.add(titleLabel);

        text.add(
                Box.createVerticalStrut(4)
        );

        text.add(valueLabel);

        text.add(
                Box.createVerticalStrut(4)
        );

        text.add(descLabel);

        text.add(
                Box.createVerticalGlue()
        );

        card.add(
                dot,
                BorderLayout.WEST
        );

        card.add(
                text,
                BorderLayout.CENTER
        );

        return card;
    }


    private JLabel valueLabel() {
        return label(
                "0",
                23,
                Font.BOLD,
                GOLD_LIGHT
        );
    }

    public void refreshData() {
        refreshRentals();
    }

    private void refreshRentals() {

        if (refreshInProgress) {
            return;
        }

        if (activeRentalsValue == null
                || completedRentalsValue == null
                || currentCostValue == null
                || lateReturnsValue == null
                || activeRentalsList == null
                || historyList == null) {
            return;
        }

        refreshInProgress = true;

        try {
            processRentalTimingUpdates();

            List<RentalRepository.RentalRecord> rentals = customer == null
                    ? List.of()
                    : rentalRepository.findByCustomerEmail(customer.getEmail()).stream()
                            .sorted(Comparator.comparing(
                                    RentalRepository.RentalRecord::startDateTime,
                                    Comparator.reverseOrder()
                            ))
                            .toList();

            List<RentalRepository.RentalRecord> active = rentals.stream()
                    .filter(this::isActiveRental)
                    .toList();

            List<RentalRepository.RentalRecord> history = rentals.stream()
                    .filter(r -> !isActiveRental(r))
                    .toList();

            long lateCount = rentals.stream()
                    .filter(r -> r.lateFee() > 0
                            || "OVERDUE".equalsIgnoreCase(r.status())
                            || "LATE".equalsIgnoreCase(r.status()))
                    .count();

            double currentCost = active.stream()
                    .mapToDouble(r -> r.baseAmount() + r.lateFee())
                    .sum();

            activeRentalsValue.setText(String.format(Locale.US, "%,d", active.size()));
            completedRentalsValue.setText(String.format(Locale.US, "%,d", history.size()));
            currentCostValue.setText(String.format(Locale.US, "$%,.0f", currentCost));
            lateReturnsValue.setText(String.format(Locale.US, "%,d", lateCount));

            rebuildRentalList(activeRentalsList, "Active Rentals", active, true, GREEN);
            rebuildRentalList(historyList, "Rental History", history, false, GOLD);
            refreshInsights(rentals, active);

            revalidate();
            repaint();
        } finally {
            refreshInProgress = false;
        }
    }
    
    private void refreshInsights(
        List<RentalRepository.RentalRecord> allRentals,
        List<RentalRepository.RentalRecord> activeRentals
) {

    if (nextReturnValue == null
            || nextReturnVehicle == null
            || paymentStatusValue == null
            || paymentStatusDescription == null
            || onTimeRateValue == null
            || onTimeRateDescription == null) {
        return;
    }


    /*
     * =========================================
     * NEXT RETURN
     * =========================================
     */

    RentalRepository.RentalRecord nearestRental =
            activeRentals.stream()
                    .filter(r ->
                            parseRentalDateTime(
                                    r.expectedReturnDateTime()
                            ) != null
                    )
                    .min(
                            Comparator.comparing(
                                    r -> parseRentalDateTime(
                                            r.expectedReturnDateTime()
                                    )
                            )
                    )
                    .orElse(null);


    if (nearestRental == null) {

        nextReturnValue.setText(
                "No active rentals"
        );

        nextReturnValue.setForeground(MUTED);

        nextReturnVehicle.setText(
                "Nothing scheduled"
        );

    } else {

        LocalDateTime expectedReturn =
                parseRentalDateTime(
                        nearestRental.expectedReturnDateTime()
                );

        LocalDateTime now =
                LocalDateTime.now();


        if (expectedReturn != null
                && expectedReturn.isBefore(now)) {

            Duration overdue =
                    Duration.between(
                            expectedReturn,
                            now
                    );

            nextReturnValue.setText(
                    "Overdue by "
                            + formatShortDuration(overdue)
            );

            nextReturnValue.setForeground(RED);

        } else if (expectedReturn != null) {

            nextReturnValue.setText(
                    expectedReturn.format(
                            DateTimeFormatter.ofPattern(
                                    "dd MMM, hh:mm a",
                                    Locale.ENGLISH
                            )
                    )
            );

            nextReturnValue.setForeground(TEXT);
        }


        nextReturnVehicle.setText(
                nearestRental.vehicleName()
        );
    }


    /*
     * =========================================
     * PAYMENT STATUS
     * =========================================
     */

    if (activeRentals.isEmpty()) {

        paymentStatusValue.setText(
                "No Active Rentals"
        );

        paymentStatusValue.setForeground(MUTED);

        paymentStatusDescription.setText(
                "No current payment required"
        );

    } else {

        boolean allPaid =
                activeRentals.stream()
                        .allMatch(
                                r -> "PAID".equalsIgnoreCase(
                                        r.paymentStatus()
                                )
                        );


        if (allPaid) {

            paymentStatusValue.setText(
                    "Paid"
            );

            paymentStatusValue.setForeground(GREEN);

            paymentStatusDescription.setText(
                    "All current rental fees covered"
            );

        } else {

            paymentStatusValue.setText(
                    "Payment Due"
            );

            paymentStatusValue.setForeground(ORANGE);

            paymentStatusDescription.setText(
                    "One or more payments require attention"
            );
        }
    }


    /*
     * =========================================
     * ON-TIME RETURN RATE
     * =========================================
     */

    List<RentalRepository.RentalRecord> completedRentals =
            allRentals.stream()
                    .filter(this::isCompletedRental)
                    .toList();


    if (completedRentals.isEmpty()) {

        onTimeRateValue.setText("-");

        onTimeRateValue.setForeground(MUTED);

        onTimeRateDescription.setText(
                "No completed rentals yet"
        );

    } else {

        long onTimeReturns =
                completedRentals.stream()
                        .filter(r ->
                                r.lateFee() <= 0.001
                        )
                        .count();


        int rate =
                (int) Math.round(
                        onTimeReturns
                                * 100.0
                                / completedRentals.size()
                );


        onTimeRateValue.setText(
                rate + "%"
        );


        if (rate >= 90) {

            onTimeRateValue.setForeground(GREEN);

            onTimeRateDescription.setText(
                    "Excellent rental record"
            );

        } else if (rate >= 70) {

            onTimeRateValue.setForeground(GOLD);

            onTimeRateDescription.setText(
                    "Good rental record"
            );

        } else {

            onTimeRateValue.setForeground(RED);

            onTimeRateDescription.setText(
                    "Return timing needs improvement"
            );
        }
    }
}
    
    private LocalDateTime parseRentalDateTime(String value) {

    if (value == null || value.isBlank()) {
        return null;
    }

    try {
        return LocalDateTime.parse(value);
    } catch (DateTimeParseException ex) {
        return null;
    }
}
    
    private boolean isCompletedRental(
        RentalRepository.RentalRecord rental
) {

    if (rental == null
            || rental.status() == null) {
        return false;
    }

    String status =
            rental.status()
                    .trim()
                    .toUpperCase(Locale.ROOT);

    return "RETURNED".equals(status)
            || "COMPLETED".equals(status);
}
    
    private String formatShortDuration(Duration duration) {

    if (duration == null
            || duration.isNegative()
            || duration.isZero()) {
        return "0m";
    }

    long totalMinutes =
            duration.toMinutes();

    long days =
            totalMinutes / (24 * 60);

    long hours =
            (totalMinutes % (24 * 60)) / 60;

    long minutes =
            totalMinutes % 60;


    if (days > 0) {
        return days + "d " + hours + "h";
    }

    if (hours > 0) {
        return hours + "h " + minutes + "m";
    }

    return minutes + "m";
}

    private void processRentalTimingUpdates() {
        if (customer == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        for (RentalRepository.RentalRecord rental
                : rentalRepository.findByCustomerEmail(customer.getEmail())) {

            if (!isActiveRental(rental)) {
                continue;
            }

            LocalDateTime expectedReturn = parseDateTime(rental.expectedReturnDateTime());

            if (expectedReturn == null || now.isBefore(expectedReturn)) {
                continue;
            }

            LocalDateTime graceEndsAt = expectedReturn.plus(GRACE_PERIOD);

            if (now.isBefore(graceEndsAt)) {
                if (!"RETURN_DUE".equalsIgnoreCase(rental.status())) {
                    rentalDatabaseService.markReturnDue(rental.rentalId());
                }
                continue;
            }

            Vehicle vehicle = findVehicle(rental.vehicleId());

            if (vehicle == null) {
                continue;
            }

            Duration overdueAfterGrace = Duration.between(graceEndsAt, now);

            // Client chooses the concrete strategy for this vehicle.
            LateFeeStrategy selectedStrategy = LateFeeStrategyFactory.forVehicle(vehicle);

            // Context stores the selected strategy.
            lateFeeContext.setStrategy(selectedStrategy);

            // Context delegates the calculation to the active strategy.
            double calculatedLateFee = lateFeeContext.calculateLateFee(
                    vehicle,
                    overdueAfterGrace
            );

            CustomerAccountState.LateFeeChargeResult charge =
                    accountState.applyLateFee(rental.invoiceId(), calculatedLateFee);

            rentalDatabaseService.recordLateFeeCharge(
                    customer,
                    rental,
                    charge.totalLateFee(),
                    charge.chargedNow(),
                    accountState.getWalletBalance()
            );
        }
    }

    private void rebuildRentalList(
            JPanel target,
            String title,
            List<RentalRepository.RentalRecord> rentals,
            boolean activeSection,
            Color badgeColor
    ) {
        target.removeAll();

        String badgeText = rentals.size() + (activeSection ? " ongoing" : " completed");
        target.add(sectionHeader(title, badgeText, badgeColor));
        target.add(Box.createVerticalStrut(12));

        if (rentals.isEmpty()) {
            JLabel empty = label(
                    activeSection ? "No active rentals yet." : "No completed rentals yet.",
                    13,
                    Font.PLAIN,
                    MUTED
            );
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            target.add(Box.createVerticalStrut(24));
            target.add(empty);
            target.add(Box.createVerticalGlue());
            return;
        }

        int visible = rentals.size();
        for (int i = 0; i < visible; i++) {
            RentalRepository.RentalRecord rental = rentals.get(i);
            target.add(activeSection
                    ? activeRentalRow(rental)
                    : rentalRow(
                            rental.vehicleName(),
                            formatRentalDate(rental, false),
                            String.format(Locale.US, "$%,.0f", rental.baseAmount() + rental.lateFee()),
                            prettyRentalStatus(rental),
                            rentalStatusColor(rental),
                            vehicleCategory(rental)
                    ));
            if (i < visible - 1) {
                target.add(Box.createVerticalStrut(12));
            }
        }

        target.add(Box.createVerticalGlue());
        target.revalidate();
        target.repaint();
    }

    private JComponent activeRentalRow(RentalRepository.RentalRecord rental) {

        HoverRoundedPanel row = new HoverRoundedPanel(
                12,
                CARD_DARK,
                new Color(9, 18, 27)
        );

        row.setLayout(new BorderLayout(14, 0));
        row.setBorder(new EmptyBorder(10, 13, 10, 13));
        row.setPreferredSize(new Dimension(10, 110));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        String category = vehicleCategory(rental);

        VehicleIcon vehicleIcon = new VehicleIcon(category);
        vehicleIcon.setPreferredSize(new Dimension(50, 50));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JPanel topLine = new JPanel();
        topLine.setOpaque(false);
        topLine.setLayout(new BoxLayout(topLine, BoxLayout.X_AXIS));
        topLine.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel vehicleLabel = label(
                rental.vehicleName(),
                14,
                Font.BOLD,
                TEXT
        );

        RoundedLabel categoryBadge = new RoundedLabel(
                category,
                GOLD,
                new Color(214, 160, 66, 18)
        );
        categoryBadge.setFont(new Font("Segoe UI", Font.BOLD, 9));

        topLine.add(vehicleLabel);
        topLine.add(Box.createHorizontalStrut(8));
        topLine.add(categoryBadge);
        topLine.add(Box.createHorizontalGlue());

        JLabel returnDateLabel = label(
                formatRentalDate(rental, true),
                11,
                Font.PLAIN,
                MUTED
        );

        JLabel statusLabel = label(
                "●  " + prettyRentalStatus(rental),
                11,
                Font.BOLD,
                rentalStatusColor(rental)
        );

        JLabel countdownLabel = label(
                countdownText(rental),
                11,
                Font.BOLD,
                countdownColor(rental)
        );

        info.add(topLine);
        info.add(Box.createVerticalStrut(4));
        info.add(returnDateLabel);
        info.add(Box.createVerticalStrut(3));
        info.add(statusLabel);
        info.add(Box.createVerticalStrut(3));
        info.add(countdownLabel);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));

        JLabel priceLabel = label(
                String.format(Locale.US, "$%,.0f", rental.baseAmount() + rental.lateFee()),
                17,
                Font.BOLD,
                PALE
        );
        priceLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        ReturnVehicleButton returnButton = new ReturnVehicleButton("RETURN VEHICLE");
        returnButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
        returnButton.setPreferredSize(new Dimension(132, 32));
        returnButton.setMaximumSize(new Dimension(132, 32));

        boolean canReturn = isReturnButtonEnabled(rental);
        returnButton.setEnabled(canReturn);
        returnButton.setToolTipText(
                canReturn
                        ? "Return this vehicle now."
                        : "The return button activates when the rental time reaches zero."
        );
        returnButton.addActionListener(e -> returnVehicle(rental));

        actions.add(priceLabel);
        actions.add(Box.createVerticalStrut(8));
        actions.add(returnButton);

        row.add(vehicleIcon, BorderLayout.WEST);
        row.add(info, BorderLayout.CENTER);
        row.add(actions, BorderLayout.EAST);

        return row;
    }

    private boolean isReturnButtonEnabled(RentalRepository.RentalRecord rental) {
        LocalDateTime expectedReturn = parseDateTime(rental.expectedReturnDateTime());
        return expectedReturn != null && !LocalDateTime.now().isBefore(expectedReturn);
    }

    private String countdownText(RentalRepository.RentalRecord rental) {
        LocalDateTime expectedReturn = parseDateTime(rental.expectedReturnDateTime());

        if (expectedReturn == null) {
            return "Time remaining unavailable";
        }

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(expectedReturn)) {
            return "Time left: " + formatDuration(Duration.between(now, expectedReturn));
        }

        LocalDateTime graceEndsAt = expectedReturn.plus(GRACE_PERIOD);

        if (now.isBefore(graceEndsAt)) {
            return "Grace period: " + formatDuration(Duration.between(now, graceEndsAt));
        }

        return "Late by: "
                + formatDuration(Duration.between(graceEndsAt, now))
                + "  •  Fee "
                + String.format(Locale.US, "$%,.2f", rental.lateFee());
    }

    private Color countdownColor(RentalRepository.RentalRecord rental) {
        LocalDateTime expectedReturn = parseDateTime(rental.expectedReturnDateTime());

        if (expectedReturn == null) {
            return MUTED;
        }

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(expectedReturn)) {
            Duration remaining = Duration.between(now, expectedReturn);
            return remaining.compareTo(Duration.ofHours(1)) <= 0 ? ORANGE : GREEN;
        }

        if (now.isBefore(expectedReturn.plus(GRACE_PERIOD))) {
            return ORANGE;
        }

        return RED;
    }

    private String formatDuration(Duration duration) {
        long totalSeconds = Math.max(0L, duration.getSeconds());

        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (days > 0) {
            return String.format(Locale.US, "%dd %02dh %02dm %02ds", days, hours, minutes, seconds);
        }

        if (hours > 0) {
            return String.format(Locale.US, "%02dh %02dm %02ds", hours, minutes, seconds);
        }

        return String.format(Locale.US, "%02dm %02ds", minutes, seconds);
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(value.trim());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private Vehicle findVehicle(String vehicleId) {
        if (vehicleId == null || vehicleId.isBlank()) {
            return null;
        }

        return vehicleService.getAllVehicles().stream()
                .filter(vehicle -> vehicle.getId().equalsIgnoreCase(vehicleId))
                .findFirst()
                .orElse(null);
    }

    private void returnVehicle(RentalRepository.RentalRecord rental) {
        RentalRepository.RentalRecord latest = rentalRepository.findById(rental.rentalId())
                .orElse(rental);

        if (!isReturnButtonEnabled(latest)) {
            VeloraNotificationDialog.showInfo(
                    this,
                    "Return Not Available Yet",
                    "The return button becomes available when the rental countdown reaches zero."
            );
            return;
        }

        processRentalTimingUpdates();

        latest = rentalRepository.findById(rental.rentalId()).orElse(latest);

        boolean confirmed = VeloraNotificationDialog.showConfirm(
                this,
                "Return Vehicle",
                "Return " + latest.vehicleName() + " now?\n"
                        + "Late fee: " + String.format(Locale.US, "$%,.2f", latest.lateFee())
                        + "\nThe vehicle will become available again.",
                "Return Vehicle"
        );

        if (!confirmed) {
            return;
        }

        boolean returned = rentalDatabaseService.markRentalReturned(
                customer,
                latest.rentalId(),
                latest.lateFee()
        );

        if (!returned) {
            VeloraNotificationDialog.showError(
                    this,
                    "Return Failed",
                    "The rental could not be updated. Please try again."
            );
            return;
        }

        Vehicle vehicle = findVehicle(latest.vehicleId());

        if (vehicle != null) {
            vehicle.setStatus(VehicleStatus.AVAILABLE);
            vehicleService.saveVehicles();
        }

        refreshRentals();

        VeloraNotificationDialog.showSuccess(
                this,
                "Vehicle Returned",
                latest.vehicleName()
                        + " has been returned successfully.\nFinal late fee: "
                        + String.format(Locale.US, "$%,.2f", latest.lateFee())
        );
    }

    private boolean isActiveRental(RentalRepository.RentalRecord rental) {
        String status = rental.status() == null ? "" : rental.status().trim().toUpperCase(Locale.ROOT);
        return "ACTIVE".equals(status)
                || "RETURN_DUE".equals(status)
                || "OVERDUE".equals(status)
                || "LATE".equals(status);
    }

    private String prettyRentalStatus(RentalRepository.RentalRecord rental) {
        String status = rental.status() == null ? "" : rental.status().trim().toUpperCase(Locale.ROOT);
        return switch (status) {
            case "ACTIVE" -> "Active";
            case "RETURN_DUE" -> "Return Due";
            case "OVERDUE", "LATE" -> "Late";
            case "COMPLETED", "RETURNED" -> rental.lateFee() > 0 ? "Returned Late" : "Returned";
            case "CANCELLED" -> "Cancelled";
            default -> status.isBlank() ? "Unknown" : status;
        };
    }

    private Color rentalStatusColor(RentalRepository.RentalRecord rental) {
        String status = prettyRentalStatus(rental);
        if (status.contains("Late")) {
            return RED;
        }
        if ("Active".equals(status)) {
            return GREEN;
        }
        if ("Return Due".equals(status)) {
            return ORANGE;
        }
        if ("Cancelled".equals(status)) {
            return MUTED;
        }
        return GREEN;
    }

    private String formatRentalDate(RentalRepository.RentalRecord rental, boolean activeSection) {
        String raw = activeSection ? rental.expectedReturnDateTime() : rental.actualReturnDateTime();
        if (raw == null || raw.isBlank()) {
            raw = rental.expectedReturnDateTime();
        }

        String prefix = activeSection ? "Return: " : "Completed: ";
        try {
            LocalDateTime dateTime = LocalDateTime.parse(raw);
            return prefix + dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        } catch (DateTimeParseException ex) {
            return prefix + (raw == null || raw.isBlank() ? "-" : raw);
        }
    }

    private String vehicleCategory(RentalRepository.RentalRecord rental) {
        for (Vehicle vehicle : vehicleService.getAllVehicles()) {
            if (vehicle.getId().equalsIgnoreCase(rental.vehicleId())) {
                return switch (vehicle.getType()) {
                    case ELECTRIC_BIKE -> "E-BIKE";
                    case MOTORCYCLE -> "MOTO";
                    case HYBRID_CAR -> "HYBRID";
                    case ELECTRIC_VEHICLE -> "EV";
                    case SUV -> "SUV";
                    case TRUCK -> "TRUCK";
                    default -> "CAR";
                };
            }
        }

        String name = rental.vehicleName() == null ? "" : rental.vehicleName().toLowerCase(Locale.ROOT);
        if (name.contains("bike")) return "E-BIKE";
        if (name.contains("motor") || name.contains("yamaha")) return "MOTO";
        if (name.contains("hybrid") || name.contains("prius")) return "HYBRID";
        if (name.contains("electric") || name.contains("tesla") || name.contains(" i")) return "EV";
        return "CAR";
    }

    /* =========================================================
       LABEL HELPER
       ========================================================= */

    private JLabel label(
            String text,
            int size,
            int style,
            Color color
    ) {

        JLabel label = new JLabel(text);

        label.setFont(
                new Font(
                        "Segoe UI",
                        style,
                        size
                )
        );

        label.setForeground(color);

        return label;
    }

    private enum StatIconType {
        CAR,
        CHECK,
        MONEY,
        CLOCK
    }

    private static final class ReturnVehicleButton extends JButton {

        ReturnVehicleButton(String text) {
            super(text);

            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 10));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            Color fillColor;
            Color borderColor;
            Color textColor;

            if (isEnabled()) {
                fillColor = getModel().isRollover()
                        ? new Color(72, 224, 116)
                        : GREEN;
                borderColor = new Color(122, 244, 151);
                textColor = new Color(8, 28, 15);
            } else {
                fillColor = new Color(42, 49, 57);
                borderColor = new Color(95, 103, 114);
                textColor = new Color(150, 157, 168);
            }

            g.setColor(fillColor);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

            g.setColor(borderColor);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

            g.setColor(textColor);
            g.setFont(getFont());

            FontMetrics metrics = g.getFontMetrics();
            int x = (getWidth() - metrics.stringWidth(getText())) / 2;
            int y = (getHeight() + metrics.getAscent() - metrics.getDescent()) / 2;

            g.drawString(getText(), x, y);
            g.dispose();
        }
    }

    /* =========================================================
       ROUNDED PANEL
       ========================================================= */

    private static class RoundedPanel
            extends JPanel {

        protected final int radius;
        protected Color fill;

        RoundedPanel(
                int radius,
                Color fill
        ) {

            this.radius = radius;
            this.fill = fill;

            setOpaque(false);
        }

        @Override
        protected void paintComponent(
                Graphics raw
        ) {

            Graphics2D g =
                    (Graphics2D) raw.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int w = Math.max(
                    1,
                    getWidth()
            );

            int h = Math.max(
                    1,
                    getHeight()
            );

            RoundRectangle2D shape =
                    new RoundRectangle2D.Double(
                            0.5,
                            0.5,
                            w - 1.0,
                            h - 1.0,
                            radius,
                            radius
                    );

            g.setColor(
                    new Color(
                            0,
                            0,
                            0,
                            65
                    )
            );

            g.fillRoundRect(
                    4,
                    5,
                    Math.max(
                            0,
                            w - 8
                    ),
                    Math.max(
                            0,
                            h - 8
                    ),
                    radius,
                    radius
            );

            g.setPaint(
                    new GradientPaint(
                            0,
                            0,
                            new Color(
                                    13,
                                    23,
                                    31
                            ),
                            w,
                            h,
                            fill
                    )
            );

            g.fill(shape);

            g.setPaint(
                    new GradientPaint(
                            0,
                            0,
                            new Color(
                                    214,
                                    160,
                                    66,
                                    22
                            ),
                            w,
                            0,
                            new Color(
                                    214,
                                    160,
                                    66,
                                    2
                            )
                    )
            );

            g.fill(shape);

            g.setColor(
                    new Color(
                            214,
                            160,
                            66,
                            75
                    )
            );

            g.draw(shape);

            g.dispose();

            super.paintComponent(raw);
        }
    }

    /* =========================================================
       HOVER PANEL
       ========================================================= */

    private static final class HoverRoundedPanel
            extends RoundedPanel {

        private final Color normalColor;
        private final Color hoverColor;

        HoverRoundedPanel(
                int radius,
                Color normalColor,
                Color hoverColor
        ) {

            super(
                    radius,
                    normalColor
            );

            this.normalColor =
                    normalColor;

            this.hoverColor =
                    hoverColor;

            setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );

            addMouseListener(
                    new MouseAdapter() {

                        @Override
                        public void mouseEntered(
                                MouseEvent e
                        ) {

                            fill =
                                    HoverRoundedPanel
                                            .this
                                            .hoverColor;

                            repaint();
                        }

                        @Override
                        public void mouseExited(
                                MouseEvent e
                        ) {

                            fill =
                                    HoverRoundedPanel
                                            .this
                                            .normalColor;

                            repaint();
                        }
                    }
            );
        }
    }

    /* =========================================================
       ROUNDED LABEL
       ========================================================= */

    private static final class RoundedLabel
            extends JLabel {

        private final Color borderColor;
        private final Color backgroundColor;

        RoundedLabel(
                String text,
                Color borderColor,
                Color backgroundColor
        ) {

            super(text);

            this.borderColor =
                    borderColor;

            this.backgroundColor =
                    backgroundColor;

            setOpaque(false);
            setForeground(borderColor);

            setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            10
                    )
            );

            setBorder(
                    new EmptyBorder(
                            3,
                            8,
                            3,
                            8
                    )
            );
        }

        @Override
        protected void paintComponent(
                Graphics raw
        ) {

            Graphics2D g =
                    (Graphics2D) raw.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g.setColor(
                    backgroundColor
            );

            g.fillRoundRect(
                    0,
                    0,
                    Math.max(
                            0,
                            getWidth() - 1
                    ),
                    Math.max(
                            0,
                            getHeight() - 1
                    ),
                    16,
                    16
            );

            g.setColor(
                    new Color(
                            borderColor.getRed(),
                            borderColor.getGreen(),
                            borderColor.getBlue(),
                            100
                    )
            );

            g.drawRoundRect(
                    0,
                    0,
                    Math.max(
                            0,
                            getWidth() - 1
                    ),
                    Math.max(
                            0,
                            getHeight() - 1
                    ),
                    16,
                    16
            );

            g.dispose();

            super.paintComponent(raw);
        }
    }

    /* =========================================================
       ACCENT DOT
       ========================================================= */

    private static final class AccentDot
            extends JComponent {

        private final Color color;

        AccentDot(Color color) {
            this.color = color;
        }

        @Override
        protected void paintComponent(
                Graphics raw
        ) {

            Graphics2D g =
                    (Graphics2D) raw.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int size = 8;

            int x =
                    (getWidth() - size)
                            / 2;

            int y =
                    (getHeight() - size)
                            / 2;

            g.setColor(
                    new Color(
                            color.getRed(),
                            color.getGreen(),
                            color.getBlue(),
                            45
                    )
            );

            g.fillOval(
                    x - 4,
                    y - 4,
                    size + 8,
                    size + 8
            );

            g.setColor(color);

            g.fillOval(
                    x,
                    y,
                    size,
                    size
            );

            g.dispose();
        }
    }

    /* =========================================================
       STAT ICON
       ========================================================= */

    private static final class StatIcon
            extends JComponent {

        private final StatIconType type;

        StatIcon(
                StatIconType type
        ) {

            this.type = type;
        }

        @Override
        protected void paintComponent(
                Graphics raw
        ) {

            Graphics2D g =
                    (Graphics2D) raw.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            g.setColor(
                    new Color(
                            214,
                            160,
                            66,
                            20
                    )
            );

            g.fillOval(
                    cx - 22,
                    cy - 22,
                    44,
                    44
            );

            g.setColor(
                    new Color(
                            214,
                            160,
                            66,
                            55
                    )
            );

            g.setStroke(
                    new BasicStroke(
                            1.3f
                    )
            );

            g.drawOval(
                    cx - 18,
                    cy - 18,
                    36,
                    36
            );

            g.setColor(GOLD);

            g.setStroke(
                    new BasicStroke(
                            2f,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND
                    )
            );

            switch (type) {

                case CAR:

                    drawSmallCar(
                            g,
                            cx,
                            cy
                    );

                    break;

                case CHECK:

                    g.drawLine(
                            cx - 8,
                            cy,
                            cx - 2,
                            cy + 7
                    );

                    g.drawLine(
                            cx - 2,
                            cy + 7,
                            cx + 10,
                            cy - 7
                    );

                    break;

                case MONEY:

                    g.setFont(
                            new Font(
                                    "Segoe UI",
                                    Font.BOLD,
                                    22
                            )
                    );

                    FontMetrics fm =
                            g.getFontMetrics();

                    String money = "$";

                    g.drawString(
                            money,
                            cx
                                    - fm.stringWidth(money)
                                    / 2,
                            cy
                                    + fm.getAscent()
                                    / 2
                                    - 2
                    );

                    break;

                case CLOCK:

                    g.drawOval(
                            cx - 10,
                            cy - 10,
                            20,
                            20
                    );

                    g.drawLine(
                            cx,
                            cy,
                            cx,
                            cy - 6
                    );

                    g.drawLine(
                            cx,
                            cy,
                            cx + 6,
                            cy + 3
                    );

                    break;
                    
                    
                    
                    

                default:
                    break;
            }

            g.dispose();
        }

        private void drawSmallCar(
                Graphics2D g,
                int cx,
                int cy
        ) {

            g.drawRoundRect(
                    cx - 12,
                    cy - 3,
                    24,
                    9,
                    4,
                    4
            );

            g.drawLine(
                    cx - 8,
                    cy - 3,
                    cx - 4,
                    cy - 9
            );

            g.drawLine(
                    cx - 4,
                    cy - 9,
                    cx + 5,
                    cy - 9
            );

            g.drawLine(
                    cx + 5,
                    cy - 9,
                    cx + 9,
                    cy - 3
            );

            g.fillOval(
                    cx - 8,
                    cy + 4,
                    4,
                    4
            );

            g.fillOval(
                    cx + 4,
                    cy + 4,
                    4,
                    4
            );
        }
    }

    /* =========================================================
       VEHICLE ICON
       ========================================================= */

    private static final class VehicleIcon
            extends JComponent {

        private final String category;

        VehicleIcon(
                String category
        ) {

            this.category =
                    category == null
                            ? ""
                            : category.toUpperCase();
        }

        @Override
        protected void paintComponent(
                Graphics raw
        ) {

            Graphics2D g =
                    (Graphics2D) raw.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            g.setColor(
                    new Color(
                            214,
                            160,
                            66,
                            20
                    )
            );

            g.fillOval(
                    cx - 22,
                    cy - 22,
                    44,
                    44
            );

            g.setColor(
                    new Color(
                            214,
                            160,
                            66,
                            45
                    )
            );

            g.setStroke(
                    new BasicStroke(
                            1.2f
                    )
            );

            g.drawOval(
                    cx - 19,
                    cy - 19,
                    38,
                    38
            );

            g.setColor(GOLD);

            g.setStroke(
                    new BasicStroke(
                            2f,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND
                    )
            );

            if (category.contains("BIKE")) {

                drawBike(
                        g,
                        cx,
                        cy
                );

            } else if (
                    category.contains("MOTO")
            ) {

                drawMotorcycle(
                        g,
                        cx,
                        cy
                );

            } else {

                drawCar(
                        g,
                        cx,
                        cy
                );
            }

            g.dispose();
        }

        private void drawCar(
                Graphics2D g,
                int cx,
                int cy
        ) {

            g.drawRoundRect(
                    cx - 16,
                    cy - 3,
                    32,
                    11,
                    5,
                    5
            );

            g.drawLine(
                    cx - 10,
                    cy - 3,
                    cx - 5,
                    cy - 13
            );

            g.drawLine(
                    cx - 5,
                    cy - 13,
                    cx + 7,
                    cy - 13
            );

            g.drawLine(
                    cx + 7,
                    cy - 13,
                    cx + 13,
                    cy - 3
            );

            g.fillOval(
                    cx - 12,
                    cy + 6,
                    5,
                    5
            );

            g.fillOval(
                    cx + 7,
                    cy + 6,
                    5,
                    5
            );
        }

        private void drawBike(
                Graphics2D g,
                int cx,
                int cy
        ) {

            g.drawOval(
                    cx - 16,
                    cy + 1,
                    11,
                    11
            );

            g.drawOval(
                    cx + 6,
                    cy + 1,
                    11,
                    11
            );

            g.drawLine(
                    cx - 10,
                    cy + 6,
                    cx - 2,
                    cy - 5
            );

            g.drawLine(
                    cx - 2,
                    cy - 5,
                    cx + 7,
                    cy + 6
            );

            g.drawLine(
                    cx - 10,
                    cy + 6,
                    cx + 7,
                    cy + 6
            );

            g.drawLine(
                    cx - 2,
                    cy - 5,
                    cx + 3,
                    cy - 11
            );

            g.drawLine(
                    cx + 1,
                    cy - 11,
                    cx + 7,
                    cy - 11
            );
        }

        private void drawMotorcycle(
                Graphics2D g,
                int cx,
                int cy
        ) {

            g.drawOval(
                    cx - 16,
                    cy + 2,
                    10,
                    10
            );

            g.drawOval(
                    cx + 7,
                    cy + 2,
                    10,
                    10
            );

            g.drawLine(
                    cx - 11,
                    cy + 7,
                    cx - 1,
                    cy - 2
            );

            g.drawLine(
                    cx - 1,
                    cy - 2,
                    cx + 10,
                    cy + 7
            );

            g.drawLine(
                    cx - 1,
                    cy - 2,
                    cx + 7,
                    cy - 2
            );

            g.drawLine(
                    cx + 7,
                    cy - 2,
                    cx + 11,
                    cy - 8
            );

            g.drawLine(
                    cx + 8,
                    cy - 8,
                    cx + 14,
                    cy - 8
            );
        }
    }
    
    private static final class LuxuryScrollBarUI
        extends BasicScrollBarUI {

    @Override
    protected void configureScrollBarColors() {

        thumbColor = new Color(
                214,
                160,
                66,
                150
        );

        trackColor = new Color(
                7,
                14,
                21
        );
    }

    @Override
    protected JButton createDecreaseButton(
            int orientation
    ) {

        JButton button = new JButton();

        button.setPreferredSize(
                new Dimension(0, 0)
        );

        button.setMinimumSize(
                new Dimension(0, 0)
        );

        button.setMaximumSize(
                new Dimension(0, 0)
        );

        return button;
    }

    @Override
    protected JButton createIncreaseButton(
            int orientation
    ) {

        JButton button = new JButton();

        button.setPreferredSize(
                new Dimension(0, 0)
        );

        button.setMinimumSize(
                new Dimension(0, 0)
        );

        button.setMaximumSize(
                new Dimension(0, 0)
        );

        return button;
    }

    @Override
    protected void paintThumb(
            Graphics g,
            JComponent c,
            Rectangle thumbBounds
    ) {

        if (thumbBounds.isEmpty()
                || !scrollbar.isEnabled()) {
            return;
        }

        Graphics2D g2 =
                (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setColor(
                new Color(
                        214,
                        160,
                        66,
                        170
                )
        );

        g2.fillRoundRect(
                thumbBounds.x + 1,
                thumbBounds.y,
                Math.max(
                        4,
                        thumbBounds.width - 2
                ),
                thumbBounds.height,
                7,
                7
        );

        g2.dispose();
    }

    @Override
    protected void paintTrack(
            Graphics g,
            JComponent c,
            Rectangle trackBounds
    ) {

        Graphics2D g2 =
                (Graphics2D) g.create();

        g2.setColor(
                new Color(
                        10,
                        18,
                        25
                )
        );

        g2.fillRoundRect(
                trackBounds.x + 2,
                trackBounds.y,
                Math.max(
                        2,
                        trackBounds.width - 4
                ),
                trackBounds.height,
                6,
                6
        );

        g2.dispose();
    }
}
}
