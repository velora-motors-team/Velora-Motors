package com.velora.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public final class AdminSupportInboxPanel extends JPanel {

    // =========================================================
    // COLORS
    // =========================================================

    private static final Color BG = new Color(3, 8, 13);
    private static final Color CARD = new Color(7, 14, 21);
    private static final Color CARD_2 = new Color(9, 18, 27);
    private static final Color CARD_3 = new Color(12, 22, 31);

    private static final Color GOLD = new Color(214, 160, 66);
    private static final Color GOLD_LIGHT = new Color(238, 201, 139);
    private static final Color GOLD_DARK = new Color(164, 103, 33);

    private static final Color TEXT = new Color(244, 245, 247);
    private static final Color MUTED = new Color(157, 164, 175);

    private static final Color GREEN = new Color(86, 207, 114);
    private static final Color BLUE = new Color(82, 156, 255);
    private static final Color RED = new Color(235, 93, 98);
    private static final Color PURPLE = new Color(186, 118, 255);

    // Extra luxury tones used only for visuals.
    private static final Color MIDNIGHT = new Color(4, 10, 16);
    private static final Color PANEL_GLOW = new Color(214, 160, 66, 36);
    private static final Color PANEL_BORDER = new Color(214, 160, 66, 72);
    private static final Color SOFT_TEXT = new Color(196, 202, 211);

    // =========================================================
    // DATA
    // =========================================================

    private final SupportMessageRepository repository =
            new SupportMessageRepository();

    private final JPanel conversationsPanel = new JPanel();

    private final java.util.Map<String, FilterTile> filterTiles =
            new java.util.LinkedHashMap<>();

    private JLabel allCountLabel;
    private JLabel liveCountLabel;
    private JLabel ticketCountLabel;
    private JLabel unreadCountLabel;
    private JLabel resolvedCountLabel;

    private JLabel selectedCustomerName;
    private JLabel selectedType;
    private JLabel selectedSubject;

    private JLabel receivedValue;
    private JLabel statusValue;
    private JLabel readValue;

    private JPanel messageContentHolder;

    private SupportMessage selectedMessage;
    private String currentFilter = "ALL";

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public AdminSupportInboxPanel() {

        setOpaque(false);
        setLayout(new BorderLayout(16, 16));
        setBorder(new EmptyBorder(16, 18, 18, 18));

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        refreshMessages();
    }

    // =========================================================
    // HEADER
    // =========================================================

    private JComponent createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = label(
                "Support Inbox",
                30,
                Font.BOLD,
                TEXT
        );

        JLabel subtitle = label(
                "Manage customer conversations, live chats, and support tickets.",
                12,
                Font.PLAIN,
                MUTED
        );

        left.add(title);
        left.add(Box.createVerticalStrut(5));
        left.add(subtitle);

        JButton refresh = new GoldButton("Refresh", "REFRESH");
        refresh.setPreferredSize(new Dimension(126, 42));
        refresh.addActionListener(e -> refreshMessages());

        header.add(left, BorderLayout.WEST);
        header.add(refresh, BorderLayout.EAST);

        return header;
    }

    // =========================================================
    // MAIN LAYOUT
    // =========================================================

    private JComponent createMainContent() {

        JPanel main = new JPanel(new BorderLayout(16, 0));
        main.setOpaque(false);

        main.add(createFiltersPanel(), BorderLayout.WEST);
        main.add(createConversationListPanel(), BorderLayout.CENTER);
        main.add(createDetailsPanel(), BorderLayout.EAST);

        return main;
    }

    // =========================================================
    // FILTERS
    // =========================================================

    private JComponent createFiltersPanel() {

        LuxuryPanel panel = new LuxuryPanel(18, CARD);
        panel.setPreferredSize(new Dimension(258, 650));

        panel.setLayout(
                new BoxLayout(panel, BoxLayout.Y_AXIS)
        );

        panel.setBorder(
                new EmptyBorder(18, 14, 18, 14)
        );

        JLabel title = label(
                "INBOX",
                12,
                Font.BOLD,
                GOLD_LIGHT
        );

        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(12));

        JSeparator line = new JSeparator();
        line.setForeground(new Color(214, 160, 66, 90));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        panel.add(line);

        panel.add(Box.createVerticalStrut(14));

        allCountLabel = createCountLabel();
        liveCountLabel = createCountLabel();
        ticketCountLabel = createCountLabel();
        unreadCountLabel = createCountLabel();
        resolvedCountLabel = createCountLabel();

        panel.add(
                createFilterButton(
                        "ALL",
                        "ALL",
                        "All Messages",
                        allCountLabel
                )
        );

        panel.add(Box.createVerticalStrut(9));

        panel.add(
                createFilterButton(
                        "LIVE",
                        "LIVE",
                        "Live Chat",
                        liveCountLabel
                )
        );

        panel.add(Box.createVerticalStrut(9));

        panel.add(
                createFilterButton(
                        "TICKETS",
                        "TICKET",
                        "Support Tickets",
                        ticketCountLabel
                )
        );

        panel.add(Box.createVerticalStrut(9));

        panel.add(
                createFilterButton(
                        "UNREAD",
                        "UNREAD",
                        "Unread",
                        unreadCountLabel
                )
        );

        panel.add(Box.createVerticalStrut(9));

        panel.add(
                createFilterButton(
                        "RESOLVED",
                        "RESOLVED",
                        "Resolved",
                        resolvedCountLabel
                )
        );

        panel.add(Box.createVerticalGlue());

        LuxuryPanel statusCard = new LuxuryPanel(
                14,
                new Color(8, 18, 24)
        );

        statusCard.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 144)
        );

        statusCard.setPreferredSize(
                new Dimension(222, 144)
        );

        statusCard.setLayout(
                new BoxLayout(statusCard, BoxLayout.Y_AXIS)
        );

        statusCard.setBorder(
                new EmptyBorder(14, 14, 14, 14)
        );

        JPanel onlineRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        onlineRow.setOpaque(false);
        onlineRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        UiIcon shieldIcon = new UiIcon("SHIELD", GREEN, 18);
        shieldIcon.setPreferredSize(new Dimension(20, 20));

        JLabel online = label(
                "Support System Online",
                11,
                Font.BOLD,
                GREEN
        );

        onlineRow.add(shieldIcon);
        onlineRow.add(Box.createHorizontalStrut(7));
        onlineRow.add(online);

        JLabel statusText = label(
                "<html>Messages update when<br>refreshed.</html>",
                10,
                Font.PLAIN,
                MUTED
        );
        statusText.setAlignmentX(Component.LEFT_ALIGNMENT);

        statusCard.add(onlineRow);
        statusCard.add(Box.createVerticalStrut(8));
        statusCard.add(statusText);
        statusCard.add(Box.createVerticalGlue());

        JPanel glowLine = new JPanel() {
            @Override
            protected void paintComponent(Graphics raw) {
                Graphics2D g = (Graphics2D) raw.create();

                g.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g.setPaint(
                        new GradientPaint(
                                0,
                                0,
                                new Color(86, 207, 114, 0),
                                getWidth() / 2f,
                                0,
                                new Color(86, 207, 114, 210),
                                true
                        )
                );

                g.fillRoundRect(
                        0,
                        Math.max(0, getHeight() / 2 - 1),
                        getWidth(),
                        3,
                        3,
                        3
                );

                g.dispose();
            }
        };

        glowLine.setOpaque(false);
        glowLine.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 8)
        );
        glowLine.setPreferredSize(
                new Dimension(190, 8)
        );

        statusCard.add(glowLine);

        statusCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(statusCard);

        return panel;
    }

    private JLabel createCountLabel() {

        JLabel label = new JLabel("0");

        label.setForeground(GOLD_LIGHT);

        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        return label;
    }

    private JComponent createFilterButton(
            String filter,
            String icon,
            String title,
            JLabel countLabel
    ) {

        FilterTile tile = new FilterTile(filter);

        tile.setLayout(
                new BorderLayout(10, 0)
        );

        tile.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        66
                )
        );

        tile.setPreferredSize(
                new Dimension(
                        190,
                        66
                )
        );

        tile.setBorder(
                new EmptyBorder(
                        12,
                        14,
                        12,
                        14
                )
        );

        tile.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        Color iconColor =
                filter.equals("LIVE") || filter.equals("RESOLVED")
                        ? GREEN
                        : GOLD_LIGHT;

        UiIcon iconLabel = new UiIcon(icon, iconColor, 18);
        iconLabel.setPreferredSize(new Dimension(24, 24));

        JLabel titleLabel = label(
                title,
                12,
                Font.BOLD,
                TEXT
        );

        JPanel left = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        0,
                        0
                )
        );

        left.setOpaque(false);
        left.add(iconLabel);
        left.add(Box.createHorizontalStrut(9));
        left.add(titleLabel);

        tile.add(left, BorderLayout.WEST);
        tile.add(countLabel, BorderLayout.EAST);

        tile.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent e) {

                        currentFilter = filter;
                        updateFilterSelection();
                        refreshMessages();
                    }
                }
        );

        addMouseListenerRecursively(
                tile,
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent e) {

                        currentFilter = filter;
                        updateFilterSelection();
                        refreshMessages();
                    }
                }
        );

        filterTiles.put(filter, tile);

        SwingUtilities.invokeLater(
                this::updateFilterSelection
        );

        return tile;
    }

    private void updateFilterSelection() {

        for (java.util.Map.Entry<String, FilterTile> entry
                : filterTiles.entrySet()) {

            entry.getValue().setSelected(
                    entry.getKey().equals(currentFilter)
            );
        }
    }

    // =========================================================
    // CONVERSATIONS
    // =========================================================

    private JComponent createConversationListPanel() {

        LuxuryPanel card = new LuxuryPanel(18, CARD);

        card.setLayout(
                new BorderLayout(0, 14)
        );

        card.setBorder(
                new EmptyBorder(18, 18, 18, 18)
        );

        card.setPreferredSize(
                new Dimension(520, 650)
        );

        JPanel heading = new JPanel(
                new BorderLayout()
        );

        heading.setOpaque(false);

        JLabel title = label(
                "Customer Conversations",
                18,
                Font.BOLD,
                TEXT
        );

        JPanel sort = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        sort.setOpaque(false);

        JLabel sortText = label(
                "Sort by:  Newest",
                11,
                Font.PLAIN,
                MUTED
        );

        UiIcon chevron = new UiIcon("CHEVRON", GOLD_LIGHT, 14);
        chevron.setPreferredSize(new Dimension(16, 16));

        sort.add(sortText);
        sort.add(chevron);

        heading.add(title, BorderLayout.WEST);
        heading.add(sort, BorderLayout.EAST);

        card.add(heading, BorderLayout.NORTH);

        conversationsPanel.setOpaque(false);

        conversationsPanel.setLayout(
                new BoxLayout(
                        conversationsPanel,
                        BoxLayout.Y_AXIS
                )
        );

        JScrollPane scroll =
                new JScrollPane(conversationsPanel);

        scroll.setBorder(
                BorderFactory.createEmptyBorder()
        );

        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        scroll.getVerticalScrollBar()
                .setUnitIncrement(18);

        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JComponent createConversationCard(
            SupportMessage message
    ) {

        ConversationCard card =
                new ConversationCard(message);

        card.setLayout(
                new BorderLayout(12, 0)
        );

        card.setBorder(
                new EmptyBorder(
                        14,
                        14,
                        14,
                        14
                )
        );

        card.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        124
                )
        );

        card.setPreferredSize(
                new Dimension(
                        450,
                        124
                )
        );

        card.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        JPanel left = new JPanel(
                new BorderLayout(8, 0)
        );

        left.setOpaque(false);

        JLabel stateDot = label(
                "●",
                18,
                Font.BOLD,
                message.getMessageType()
                        == SupportMessage.MessageType.LIVE_CHAT
                        ? GREEN
                        : GOLD_LIGHT
        );

        AvatarIcon avatar = new AvatarIcon();
        avatar.setPreferredSize(
                new Dimension(54, 54)
        );

        left.add(stateDot, BorderLayout.WEST);
        left.add(avatar, BorderLayout.CENTER);

        JPanel content = new JPanel();
        content.setOpaque(false);

        content.setLayout(
                new BoxLayout(
                        content,
                        BoxLayout.Y_AXIS
                )
        );

        JPanel top = new JPanel(
                new BorderLayout()
        );

        top.setOpaque(false);

        JLabel customer = label(
                message.getCustomerName(),
                14,
                Font.BOLD,
                TEXT
        );

        JLabel time = label(
                message.getFormattedDateTime(),
                9,
                Font.PLAIN,
                MUTED
        );

        top.add(customer, BorderLayout.WEST);
        top.add(time, BorderLayout.EAST);

        JLabel type = label(
                typeText(message),
                10,
                Font.BOLD,
                typeColor(message)
        );

        JLabel preview = label(
                shorten(
                        message.getMessage(),
                        58
                ),
                11,
                Font.PLAIN,
                MUTED
        );

        content.add(top);
        content.add(Box.createVerticalStrut(6));
        content.add(type);
        content.add(Box.createVerticalStrut(6));
        content.add(preview);

        JPanel right = new JPanel(
                new BorderLayout()
        );

        right.setOpaque(false);

        right.add(
                statusBadge(message),
                BorderLayout.SOUTH
        );

        card.add(left, BorderLayout.WEST);
        card.add(content, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);

        MouseAdapter openListener =
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(
                            MouseEvent e
                    ) {

                        openMessage(message);
                    }
                };

        addMouseListenerRecursively(
                card,
                openListener
        );

        return card;
    }

    private JComponent statusBadge(
            SupportMessage message
    ) {

        String text;
        Color color;

        switch (message.getStatus()) {

            case RESOLVED -> {
                text = "RESOLVED";
                color = GREEN;
            }

            case IN_PROGRESS -> {
                text = "IN_PROGRESS";
                color = BLUE;
            }

            default -> {
                text = "OPEN";
                color = GOLD_LIGHT;
            }
        }

        return new StatusBadge(text, color);
    }

    // =========================================================
    // DETAILS
    // =========================================================

    private JComponent createDetailsPanel() {

        LuxuryPanel panel = new LuxuryPanel(
                18,
                CARD
        );

        panel.setLayout(
                new BorderLayout(0, 14)
        );

        panel.setPreferredSize(
                new Dimension(540, 650)
        );

        panel.setBorder(
                new EmptyBorder(
                        18,
                        18,
                        18,
                        18
                )
        );

        panel.add(
                createDetailsHeader(),
                BorderLayout.NORTH
        );

        messageContentHolder =
                new JPanel(new BorderLayout());

        messageContentHolder.setOpaque(false);

        JScrollPane scroll =
                new JScrollPane(messageContentHolder);

        scroll.setBorder(
                BorderFactory.createEmptyBorder()
        );

        scroll.setOpaque(false);

        scroll.getViewport()
                .setOpaque(false);

        scroll.getVerticalScrollBar()
                .setUnitIncrement(18);

        panel.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(
                new GridLayout(1, 2, 12, 0)
        );

        bottom.setOpaque(false);

        JButton reply =
                new GoldButton("Reply", "SEND");

        reply.addActionListener(
                e -> replyToSelectedMessage()
        );

        JButton resolved =
                new OutlineButton("Mark Resolved", "CHECK");

        resolved.addActionListener(
                e -> markSelectedResolved()
        );

        bottom.add(reply);
        bottom.add(resolved);

        panel.add(bottom, BorderLayout.SOUTH);

        showEmptyDetails();

        return panel;
    }

    private JComponent createDetailsHeader() {

        JPanel wrapper =
                new JPanel();

        wrapper.setOpaque(false);

        wrapper.setLayout(
                new BoxLayout(
                        wrapper,
                        BoxLayout.Y_AXIS
                )
        );

        JPanel top =
                new JPanel(
                        new BorderLayout(12, 0)
                );

        top.setOpaque(false);

        AvatarIcon avatar =
                new AvatarIcon();

        avatar.setPreferredSize(
                new Dimension(54, 54)
        );

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);

        titleBox.setLayout(
                new BoxLayout(
                        titleBox,
                        BoxLayout.Y_AXIS
                )
        );

        selectedCustomerName = label(
                "Select a conversation",
                21,
                Font.BOLD,
                TEXT
        );

        selectedType = label(
                "Choose a message from the inbox.",
                11,
                Font.BOLD,
                MUTED
        );

        titleBox.add(selectedCustomerName);
        titleBox.add(Box.createVerticalStrut(5));
        titleBox.add(selectedType);

        top.add(avatar, BorderLayout.WEST);
        top.add(titleBox, BorderLayout.CENTER);

        selectedSubject = label(
                "",
                13,
                Font.BOLD,
                GOLD_LIGHT
        );

        wrapper.add(top);
        wrapper.add(Box.createVerticalStrut(12));
        wrapper.add(selectedSubject);
        wrapper.add(Box.createVerticalStrut(12));

        LuxuryPanel meta = new LuxuryPanel(
                13,
                CARD_2
        );

        meta.setLayout(
                new GridLayout(1, 3, 8, 0)
        );

        meta.setBorder(
                new EmptyBorder(
                        12,
                        12,
                        12,
                        12
                )
        );

        receivedValue = label(
                "-",
                10,
                Font.BOLD,
                GOLD_LIGHT
        );

        statusValue = label(
                "-",
                10,
                Font.BOLD,
                GREEN
        );

        readValue = label(
                "-",
                10,
                Font.BOLD,
                TEXT
        );

        meta.add(
                createMetaCell(
                        "CALENDAR",
                        "Received",
                        receivedValue
                )
        );

        meta.add(
                createMetaCell(
                        "CHECK",
                        "Status",
                        statusValue
                )
        );

        meta.add(
                createMetaCell(
                        "EYE",
                        "Read",
                        readValue
                )
        );

        wrapper.add(meta);

        return wrapper;
    }

    private JComponent createMetaCell(
            String iconType,
            String title,
            JLabel value
    ) {

        JPanel cell = new JPanel();
        cell.setOpaque(false);

        cell.setLayout(
                new BoxLayout(
                        cell,
                        BoxLayout.Y_AXIS
                )
        );

        JPanel headingRow = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 6, 0)
        );
        headingRow.setOpaque(false);
        headingRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        UiIcon icon = new UiIcon(iconType, GOLD_LIGHT, 14);
        icon.setPreferredSize(new Dimension(16, 16));

        JLabel heading = label(
                title,
                10,
                Font.PLAIN,
                MUTED
        );

        headingRow.add(icon);
        headingRow.add(heading);

        value.setAlignmentX(Component.LEFT_ALIGNMENT);

        cell.add(headingRow);
        cell.add(Box.createVerticalStrut(5));
        cell.add(value);

        return cell;
    }

    private void showEmptyDetails() {

        if (messageContentHolder == null) {
            return;
        }

        messageContentHolder.removeAll();

        JPanel empty = new JPanel(
                new GridBagLayout()
        );

        empty.setOpaque(false);

        JLabel text = label(
                "Select a conversation to view details.",
                13,
                Font.PLAIN,
                MUTED
        );

        empty.add(text);

        messageContentHolder.add(
                empty,
                BorderLayout.CENTER
        );

        messageContentHolder.revalidate();
        messageContentHolder.repaint();
    }

    private JComponent createMessageSection(
            String title,
            String emoji,
            String body,
            Color accent,
            String footer
    ) {

        JPanel section = new JPanel();
        section.setOpaque(false);

        section.setLayout(
                new BoxLayout(
                        section,
                        BoxLayout.Y_AXIS
                )
        );

        JPanel heading = new JPanel(
                new BorderLayout()
        );

        heading.setOpaque(false);

        JLabel titleLabel = label(
                title,
                13,
                Font.BOLD,
                GOLD_LIGHT
        );

        JSeparator line = new JSeparator();

        line.setForeground(
                new Color(
                        214,
                        160,
                        66,
                        80
                )
        );

        heading.add(
                titleLabel,
                BorderLayout.WEST
        );

        heading.add(
                line,
                BorderLayout.CENTER
        );

        LuxuryPanel bubble = new LuxuryPanel(
                14,
                CARD_2
        );

        bubble.setAccent(accent);

        bubble.setLayout(
                new BorderLayout(12, 0)
        );

        bubble.setBorder(
                new EmptyBorder(
                        18,
                        18,
                        16,
                        18
                )
        );

        UiIcon quote = new UiIcon(emoji, accent, 22);
        quote.setPreferredSize(new Dimension(28, 28));

        JTextArea text =
                new JTextArea(body);

        text.setEditable(false);
        text.setOpaque(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setForeground(TEXT);

        text.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13
                )
        );

        text.setBorder(
                BorderFactory.createEmptyBorder()
        );

        bubble.add(quote, BorderLayout.WEST);
        bubble.add(text, BorderLayout.CENTER);

        if (footer != null
                && !footer.isBlank()) {

            JLabel footerLabel = label(
                    footer,
                    10,
                    Font.PLAIN,
                    MUTED
            );

            bubble.add(
                    footerLabel,
                    BorderLayout.SOUTH
            );
        }

        section.add(heading);
        section.add(Box.createVerticalStrut(10));
        section.add(bubble);

        return section;
    }

    // =========================================================
    // REFRESH
    // =========================================================

    public void refreshMessages() {

        List<SupportMessage> all =
                repository.loadMessages();

        updateCounters(all);

        conversationsPanel.removeAll();

        List<SupportMessage> filtered =
                filterMessages(all);

        if (filtered.isEmpty()) {

            JLabel empty = label(
                    "No messages found.",
                    13,
                    Font.PLAIN,
                    MUTED
            );

            empty.setAlignmentX(
                    Component.CENTER_ALIGNMENT
            );

            conversationsPanel.add(
                    Box.createVerticalStrut(30)
            );

            conversationsPanel.add(empty);

        } else {

            for (SupportMessage message : filtered) {

                conversationsPanel.add(
                        createConversationCard(message)
                );

                conversationsPanel.add(
                        Box.createVerticalStrut(10)
                );
            }
        }

        conversationsPanel.revalidate();
        conversationsPanel.repaint();

        updateFilterSelection();
    }

    private List<SupportMessage> filterMessages(
            List<SupportMessage> messages
    ) {

        List<SupportMessage> result =
                new ArrayList<>();

        for (SupportMessage message : messages) {

            boolean include =
                    switch (currentFilter) {

                        case "LIVE" ->
                                message.getMessageType()
                                        == SupportMessage.MessageType.LIVE_CHAT;

                        case "TICKETS" ->
                                message.getMessageType()
                                        != SupportMessage.MessageType.LIVE_CHAT;

                        case "UNREAD" ->
                                !message.isRead();

                        case "RESOLVED" ->
                                message.getStatus()
                                        == SupportMessage.MessageStatus.RESOLVED;

                        default -> true;
                    };

            if (include) {
                result.add(message);
            }
        }

        return result;
    }

    private void updateCounters(
            List<SupportMessage> messages
    ) {

        int live = 0;
        int tickets = 0;
        int unread = 0;
        int resolved = 0;

        for (SupportMessage message : messages) {

            if (message.getMessageType()
                    == SupportMessage.MessageType.LIVE_CHAT) {

                live++;

            } else {

                tickets++;
            }

            if (!message.isRead()) {
                unread++;
            }

            if (message.getStatus()
                    == SupportMessage.MessageStatus.RESOLVED) {

                resolved++;
            }
        }

        allCountLabel.setText(
                String.valueOf(messages.size())
        );

        liveCountLabel.setText(
                String.valueOf(live)
        );

        ticketCountLabel.setText(
                String.valueOf(tickets)
        );

        unreadCountLabel.setText(
                String.valueOf(unread)
        );

        resolvedCountLabel.setText(
                String.valueOf(resolved)
        );
    }

    // =========================================================
    // OPEN MESSAGE
    // =========================================================

    private void openMessage(
            SupportMessage message
    ) {

        selectedMessage = message;

        if (!message.isRead()) {

            message.setRead(true);

            repository.updateMessage(message);
        }

        showMessageDetails(message);
        refreshMessages();
    }

    private void showMessageDetails(
            SupportMessage message
    ) {

        selectedMessage = message;

        selectedCustomerName.setText(
                message.getCustomerName()
        );

        selectedType.setText(
                typeText(message)
        );

        selectedType.setForeground(
                typeColor(message)
        );

        selectedSubject.setText(
                message.getSubject()
        );

        receivedValue.setText(
                message.getFormattedDateTime()
        );

        statusValue.setText(
                message.getStatus().toString()
        );

        statusValue.setForeground(
                switch (message.getStatus()) {
                    case RESOLVED -> GREEN;
                    case IN_PROGRESS -> BLUE;
                    default -> GOLD_LIGHT;
                }
        );

        readValue.setText(
                message.isRead()
                        ? "YES"
                        : "NO"
        );

        messageContentHolder.removeAll();

        JPanel content = new JPanel();

        content.setOpaque(false);

        content.setLayout(
                new BoxLayout(
                        content,
                        BoxLayout.Y_AXIS
                )
        );

        content.add(
                createMessageSection(
                        "CUSTOMER MESSAGE",
                        "QUOTE",
                        message.getMessage(),
                        GOLD,
                        message.getFormattedDateTime()
                )
        );

        if (message.hasAdminReply()) {

            content.add(
                    Box.createVerticalStrut(18)
            );

            content.add(
                    createMessageSection(
                            "VELORA SUPPORT REPLY",
                            "QUOTE",
                            message.getAdminReply(),
                            GREEN,
                            "Replied: "
                            + message.getFormattedReplyDateTime()
                            + "  DONE"
                    )
            );
        }

        content.add(Box.createVerticalGlue());

        messageContentHolder.add(
                content,
                BorderLayout.NORTH
        );

        messageContentHolder.revalidate();
        messageContentHolder.repaint();
    }

    // =========================================================
    // REPLY
    // =========================================================

    private void replyToSelectedMessage() {

        if (selectedMessage == null) {

            VeloraNotificationDialog.showWarning(
                    this,
                    "No Conversation Selected",
                    "Please select a customer conversation before continuing."
            );

            return;
        }

        String reply =
                VeloraNotificationDialog.showReplyDialog(
                        this,
                        selectedMessage.getCustomerName()
                );

        if (reply == null) {
            return;
        }

        if (reply.isBlank()) {

            VeloraNotificationDialog.showWarning(
                    this,
                    "Reply Cannot Be Empty",
                    "Please write a reply before sending it to the customer."
            );

            return;
        }

        selectedMessage.setAdminReply(reply);

        selectedMessage.setReplyDateTime(
                java.time.LocalDateTime.now()
        );

        selectedMessage.setRead(true);

        if (selectedMessage.getStatus()
                == SupportMessage.MessageStatus.OPEN) {

            selectedMessage.setStatus(
                    SupportMessage.MessageStatus.IN_PROGRESS
            );
        }

        boolean updated =
                repository.updateMessage(
                        selectedMessage
                );

        if (!updated) {

            VeloraNotificationDialog.showError(
                    this,
                    "Unable to Save Reply",
                    "The reply could not be saved to the support storage file. Please try again."
            );

            return;
        }

        showMessageDetails(selectedMessage);
        refreshMessages();

        VeloraNotificationDialog.showSuccess(
                this,
                "Reply Sent Successfully",
                "Your reply has been saved and delivered to "
                + selectedMessage.getCustomerName()
                + "."
        );
    }

    // =========================================================
    // RESOLVED
    // =========================================================

    private void markSelectedResolved() {

        if (selectedMessage == null) {

            VeloraNotificationDialog.showWarning(
                    this,
                    "No Conversation Selected",
                    "Please select a customer conversation before continuing."
            );

            return;
        }

        selectedMessage.setStatus(
                SupportMessage.MessageStatus.RESOLVED
        );

        selectedMessage.setRead(true);

        boolean updated =
                repository.updateMessage(
                        selectedMessage
                );

        if (!updated) {

            VeloraNotificationDialog.showError(
                    this,
                    "Unable to Update Message",
                    "The selected message could not be updated in the support storage file."
            );

            return;
        }

        showMessageDetails(selectedMessage);

        VeloraNotificationDialog.showSuccess(
                this,
                "Conversation Resolved",
                "This support conversation has been marked as resolved and saved successfully."
        );

        refreshMessages();
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private String typeText(
            SupportMessage message
    ) {

        return switch (
                message.getMessageType()
        ) {

            case LIVE_CHAT ->
                    "● LIVE CHAT";

            case BILLING_ISSUE ->
                    "● BILLING ISSUE";

            case RENTAL_ISSUE ->
                    "● RENTAL ISSUE";

            case TECHNICAL_ISSUE ->
                    "● TECHNICAL ISSUE";

            case EMERGENCY ->
                    "● EMERGENCY";

            default ->
                    "● SUPPORT TICKET";
        };
    }

    private Color typeColor(
            SupportMessage message
    ) {

        return switch (
                message.getMessageType()
        ) {

            case LIVE_CHAT -> GREEN;
            case BILLING_ISSUE -> GOLD;
            case RENTAL_ISSUE -> BLUE;
            case TECHNICAL_ISSUE -> PURPLE;
            case EMERGENCY -> RED;
            default -> GOLD_LIGHT;
        };
    }

    private String shorten(
            String text,
            int max
    ) {

        if (text == null) {
            return "";
        }

        if (text.length() <= max) {
            return text;
        }

        return text.substring(0, max) + "...";
    }

    private JLabel label(
            String text,
            int size,
            int style,
            Color color
    ) {

        JLabel label =
                new JLabel(text);

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

    private void addMouseListenerRecursively(
            Component component,
            MouseAdapter listener
    ) {

        component.addMouseListener(listener);

        if (component instanceof Container container) {

            for (Component child
                    : container.getComponents()) {

                addMouseListenerRecursively(
                        child,
                        listener
                );
            }
        }
    }

    // =========================================================
    // CUSTOM VECTOR ICONS - NO EMOJI FONT DEPENDENCY
    // =========================================================

    private static void drawIcon(
            Graphics2D g,
            String type,
            int x,
            int y,
            int s
    ) {
        switch (type) {

            case "ALL" -> {
                g.drawRoundRect(x + 2, y + 4, s - 6, s - 8, 5, 5);
                g.drawLine(x + 6, y + s - 4, x + 3, y + s - 1);
                g.drawRoundRect(x + 6, y + 1, s - 6, s - 8, 5, 5);
            }

            case "LIVE" -> {
                g.drawArc(x + 2, y + 2, s - 4, s - 4, 25, 130);
                g.drawArc(x + 2, y + 2, s - 4, s - 4, 205, 130);
                g.drawRoundRect(x, y + s / 2 - 3, 4, 8, 3, 3);
                g.drawRoundRect(x + s - 4, y + s / 2 - 3, 4, 8, 3, 3);
            }

            case "TICKET" -> {
                g.drawRoundRect(x + 1, y + 3, s - 2, s - 6, 4, 4);
                g.drawLine(x + s / 2, y + 5, x + s / 2, y + s - 5);
            }

            case "UNREAD" -> {
                g.drawRoundRect(x + 1, y + 3, s - 2, s - 6, 4, 4);
                g.drawLine(x + 2, y + 4, x + s / 2, y + s / 2);
                g.drawLine(x + s - 2, y + 4, x + s / 2, y + s / 2);
            }

            case "RESOLVED", "CHECK" -> {
                g.drawOval(x + 1, y + 1, s - 2, s - 2);
                g.drawLine(x + 5, y + s / 2, x + s / 2 - 1, y + s - 5);
                g.drawLine(x + s / 2 - 1, y + s - 5, x + s - 4, y + 5);
            }

            case "SHIELD" -> {
                Path2D shield = new Path2D.Double();
                shield.moveTo(x + s / 2.0, y + 1);
                shield.lineTo(x + s - 2, y + 5);
                shield.lineTo(x + s - 4, y + s - 5);
                shield.lineTo(x + s / 2.0, y + s - 1);
                shield.lineTo(x + 4, y + s - 5);
                shield.lineTo(x + 2, y + 5);
                shield.closePath();
                g.draw(shield);
                g.drawLine(x + 6, y + s / 2, x + s / 2 - 1, y + s - 6);
                g.drawLine(x + s / 2 - 1, y + s - 6, x + s - 5, y + 5);
            }

            case "QUOTE" -> {
                g.setFont(new Font("Serif", Font.BOLD, Math.max(18, s + 4)));
                g.drawString("\u201C", x, y + s);
            }

            case "REFRESH" -> {
                g.drawArc(x + 2, y + 2, s - 4, s - 4, 35, 285);
                Path2D arrow = new Path2D.Double();
                arrow.moveTo(x + s - 5, y + 2);
                arrow.lineTo(x + s - 1, y + 5);
                arrow.lineTo(x + s - 6, y + 7);
                arrow.closePath();
                g.fill(arrow);
            }

            case "CHEVRON" -> {
                g.drawLine(x + 3, y + 5, x + s / 2, y + s - 4);
                g.drawLine(x + s / 2, y + s - 4, x + s - 3, y + 5);
            }

            case "CALENDAR" -> {
                g.drawRoundRect(x + 1, y + 3, s - 2, s - 5, 4, 4);
                g.drawLine(x + 1, y + 7, x + s - 1, y + 7);
                g.drawLine(x + 5, y + 1, x + 5, y + 5);
                g.drawLine(x + s - 5, y + 1, x + s - 5, y + 5);
            }

            case "EYE" -> {
                Path2D eye = new Path2D.Double();
                eye.moveTo(x + 1, y + s / 2.0);
                eye.curveTo(x + s / 4.0, y + 2, x + 3.0 * s / 4.0, y + 2, x + s - 1, y + s / 2.0);
                eye.curveTo(x + 3.0 * s / 4.0, y + s - 2, x + s / 4.0, y + s - 2, x + 1, y + s / 2.0);
                g.draw(eye);
                g.fillOval(x + s / 2 - 2, y + s / 2 - 2, 4, 4);
            }

            case "SEND" -> {
                Path2D send = new Path2D.Double();
                send.moveTo(x + 1, y + 2);
                send.lineTo(x + s - 1, y + s / 2.0);
                send.lineTo(x + 1, y + s - 2);
                send.lineTo(x + 5, y + s / 2.0);
                send.closePath();
                g.draw(send);
                g.drawLine(x + 5, y + s / 2, x + s - 4, y + s / 2);
            }

            default -> g.drawOval(x + 2, y + 2, s - 4, s - 4);
        }
    }

    private static final class UiIcon extends JComponent {

        private final String type;
        private final Color color;
        private final int iconSize;

        UiIcon(String type, Color color, int iconSize) {
            this.type = type;
            this.color = color;
            this.iconSize = iconSize;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int s = Math.min(
                    iconSize,
                    Math.min(getWidth(), getHeight()) - 2
            );

            int x = (getWidth() - s) / 2;
            int y = (getHeight() - s) / 2;

            g.setColor(color);
            g.setStroke(new BasicStroke(
                    1.8f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));

            drawIcon(g, type, x, y, s);

            g.dispose();
        }
    }

    // =========================================================
    // SWING ICON WRAPPER - USED BY BUTTONS
    // =========================================================

    private static final class VectorIcon implements Icon {

        private final String type;
        private final Color color;
        private final int size;

        VectorIcon(String type, Color color, int size) {
            this.type = type;
            this.color = color;
            this.size = size;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component c, Graphics raw, int x, int y) {
            Graphics2D g = (Graphics2D) raw.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g.setColor(color);
            g.setStroke(new BasicStroke(
                    1.8f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));

            drawIcon(g, type, x, y, size);
            g.dispose();
        }
    }

    // =========================================================
    // LUXURY PANEL
    // =========================================================

    private static class LuxuryPanel
            extends JPanel {

        private final int radius;
        private final Color fill;
        private Color accent;

        LuxuryPanel(
                int radius,
                Color fill
        ) {

            this.radius = radius;
            this.fill = fill;

            setOpaque(false);
        }

        void setAccent(Color accent) {
            this.accent = accent;
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

            int w = getWidth();
            int h = getHeight();

            // Soft cinematic shadow.
            g.setColor(new Color(0, 0, 0, 78));

            g.fillRoundRect(
                    5,
                    7,
                    Math.max(0, w - 10),
                    Math.max(0, h - 10),
                    radius,
                    radius
            );

            RoundRectangle2D shape =
                    new RoundRectangle2D.Double(
                            0.5,
                            0.5,
                            Math.max(0, w - 1),
                            Math.max(0, h - 1),
                            radius,
                            radius
                    );

            Color brighter =
                    new Color(
                            Math.min(fill.getRed() + 9, 255),
                            Math.min(fill.getGreen() + 11, 255),
                            Math.min(fill.getBlue() + 14, 255)
                    );

            g.setPaint(
                    new GradientPaint(
                            0,
                            0,
                            brighter,
                            w,
                            h,
                            fill
                    )
            );

            g.fill(shape);

            // Very subtle top gold light sweep.
            g.setPaint(
                    new GradientPaint(
                            0,
                            0,
                            new Color(214, 160, 66, 18),
                            w,
                            0,
                            new Color(214, 160, 66, 0)
                    )
            );

            g.fill(shape);

            g.setColor(PANEL_BORDER);
            g.setStroke(new BasicStroke(1f));
            g.draw(shape);

            if (accent != null) {

                g.setPaint(
                        new GradientPaint(
                                0,
                                8,
                                accent,
                                0,
                                h - 8,
                                new Color(
                                        accent.getRed(),
                                        accent.getGreen(),
                                        accent.getBlue(),
                                        75
                                )
                        )
                );

                g.fillRoundRect(
                        0,
                        8,
                        3,
                        Math.max(0, h - 16),
                        3,
                        3
                );
            }

            g.dispose();

            super.paintComponent(raw);
        }
    }

    // =========================================================
    // FILTER TILE
    // =========================================================

    private static final class FilterTile
            extends JPanel {

        private final String filter;
        private boolean selected;

        FilterTile(String filter) {

            this.filter = filter;

            setOpaque(false);
        }

        void setSelected(boolean selected) {

            this.selected = selected;

            repaint();
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

            Color fill =
                    selected
                            ? new Color(36, 30, 20)
                            : new Color(8, 17, 25);

            g.setColor(fill);

            g.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    14,
                    14
            );

            g.setColor(
                    selected
                            ? GOLD
                            : new Color(
                                    214,
                                    160,
                                    66,
                                    55
                            )
            );

            g.drawRoundRect(
                    0,
                    0,
                    getWidth() - 1,
                    getHeight() - 1,
                    14,
                    14
            );

            if (selected) {

                for (int i = 6; i >= 1; i--) {

                    g.setColor(
                            new Color(
                                    214,
                                    160,
                                    66,
                                    8 + ((6 - i) * 4)
                            )
                    );

                    g.drawRoundRect(
                            i,
                            i,
                            Math.max(0, getWidth() - (i * 2) - 1),
                            Math.max(0, getHeight() - (i * 2) - 1),
                            12,
                            12
                    );
                }

                g.setColor(
                        new Color(
                                214,
                                160,
                                66,
                                150
                        )
                );

                g.setStroke(
                        new BasicStroke(1.8f)
                );

                g.drawRoundRect(
                        2,
                        2,
                        getWidth() - 5,
                        getHeight() - 5,
                        12,
                        12
                );
            }

            g.dispose();

            super.paintComponent(raw);
        }
    }

    // =========================================================
    // CONVERSATION CARD
    // =========================================================

    private final class ConversationCard
            extends JPanel {

        private final SupportMessage message;
        private boolean hovered;

        ConversationCard(
                SupportMessage message
        ) {

            this.message = message;
            setOpaque(false);

            addMouseListener(new MouseAdapter() {

                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
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

            boolean selected =
                    selectedMessage != null
                    && selectedMessage.getMessageId()
                            .equals(message.getMessageId());

            int w = getWidth();
            int h = getHeight();

            // Soft shadow.
            g.setColor(new Color(0, 0, 0, 70));
            g.fillRoundRect(
                    4,
                    6,
                    Math.max(0, w - 8),
                    Math.max(0, h - 8),
                    16,
                    16
            );

            // Card body.
            Color top =
                    selected
                            ? new Color(18, 28, 38)
                            : hovered
                            ? new Color(14, 24, 34)
                            : new Color(8, 17, 25);

            Color bottom =
                    selected
                            ? new Color(8, 16, 24)
                            : new Color(6, 13, 20);

            g.setPaint(
                    new GradientPaint(
                            0,
                            0,
                            top,
                            0,
                            h,
                            bottom
                    )
            );

            g.fillRoundRect(
                    0,
                    0,
                    w,
                    h,
                    16,
                    16
            );

            // Gold glow for selected card.
            if (selected) {

                for (int i = 7; i >= 1; i--) {
                    int alpha = 6 + (7 - i) * 3;

                    g.setColor(
                            new Color(
                                    GOLD.getRed(),
                                    GOLD.getGreen(),
                                    GOLD.getBlue(),
                                    alpha
                            )
                    );

                    g.drawRoundRect(
                            i,
                            i,
                            Math.max(0, w - (i * 2) - 1),
                            Math.max(0, h - (i * 2) - 1),
                            16,
                            16
                    );
                }
            }

            g.setColor(
                    selected
                            ? GOLD
                            : hovered
                            ? new Color(214, 160, 66, 115)
                            : PANEL_BORDER
            );

            g.setStroke(
                    new BasicStroke(
                            selected ? 1.8f : 1.0f
                    )
            );

            g.drawRoundRect(
                    0,
                    0,
                    w - 1,
                    h - 1,
                    16,
                    16
            );

            if (selected) {

                g.setPaint(
                        new GradientPaint(
                                0,
                                0,
                                new Color(214, 160, 66, 220),
                                0,
                                h,
                                new Color(214, 160, 66, 50)
                        )
                );

                g.fillRoundRect(
                        0,
                        12,
                        3,
                        Math.max(0, h - 24),
                        3,
                        3
                );
            }

            g.dispose();

            super.paintComponent(raw);
        }
    }

    // =========================================================
    // AVATAR
    // =========================================================

    private static final class AvatarIcon
            extends JComponent {

        AvatarIcon() {
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

            int s =
                    Math.min(
                            getWidth(),
                            getHeight()
                    ) - 4;

            int x =
                    (getWidth() - s) / 2;

            int y =
                    (getHeight() - s) / 2;

            g.setColor(
                    new Color(
                            214,
                            160,
                            66,
                            30
                    )
            );

            g.fillOval(x, y, s, s);

            g.setColor(GOLD_LIGHT);

            g.setStroke(
                    new BasicStroke(2f)
            );

            g.drawOval(x, y, s, s);

            int cx =
                    getWidth() / 2;

            int cy =
                    getHeight() / 2;

            g.setColor(
                    new Color(
                            235,
                            235,
                            235
                    )
            );

            g.fillOval(
                    cx - 7,
                    cy - 13,
                    14,
                    14
            );

            g.fillRoundRect(
                    cx - 13,
                    cy + 3,
                    26,
                    15,
                    12,
                    12
            );

            g.dispose();
        }
    }

    // =========================================================
    // STATUS BADGE
    // =========================================================

    private static final class StatusBadge
            extends JComponent {

        private final String text;
        private final Color color;

        StatusBadge(
                String text,
                Color color
        ) {

            this.text = text;
            this.color = color;

            setPreferredSize(
                    new Dimension(92, 28)
            );

            setMinimumSize(
                    new Dimension(92, 28)
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
                    new Color(
                            color.getRed(),
                            color.getGreen(),
                            color.getBlue(),
                            28
                    )
            );

            g.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    10,
                    10
            );

            g.setColor(color);

            g.setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            9
                    )
            );

            FontMetrics fm =
                    g.getFontMetrics();

            int tx =
                    (getWidth()
                    - fm.stringWidth(text))
                    / 2;

            int ty =
                    (getHeight()
                    + fm.getAscent()
                    - fm.getDescent())
                    / 2;

            g.drawString(
                    text,
                    tx,
                    ty
            );

            g.dispose();
        }
    }

    // =========================================================
    // GOLD BUTTON
    // =========================================================

    private static final class GoldButton
            extends JButton {

        GoldButton(String text) {
            this(text, null);
        }

        GoldButton(String text, String iconType) {

            super(text);

            if (iconType != null) {
                setIcon(new VectorIcon(iconType, new Color(30, 20, 8), 16));
                setIconTextGap(8);
            }

            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);

            setForeground(
                    new Color(
                            30,
                            20,
                            8
                    )
            );

            setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            13
                    )
            );

            setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
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

            Color start =
                    getModel().isRollover()
                            ? new Color(248, 216, 155)
                            : GOLD_LIGHT;

            g.setPaint(
                    new GradientPaint(
                            0,
                            0,
                            start,
                            getWidth(),
                            getHeight(),
                            GOLD_DARK
                    )
            );

            g.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    12,
                    12
            );

            g.dispose();

            super.paintComponent(raw);
        }
    }

    // =========================================================
    // OUTLINE BUTTON
    // =========================================================

    private static final class OutlineButton
            extends JButton {

        OutlineButton(String text) {
            this(text, null);
        }

        OutlineButton(String text, String iconType) {

            super(text);

            if (iconType != null) {
                setIcon(new VectorIcon(iconType, GOLD_LIGHT, 16));
                setIconTextGap(8);
            }

            setOpaque(false);
            setContentAreaFilled(false);
            setFocusPainted(false);

            setForeground(GOLD_LIGHT);

            setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            13
                    )
            );

            setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );

            setBorder(
                    BorderFactory.createLineBorder(
                            new Color(
                                    214,
                                    160,
                                    66,
                                    130
                            )
                    )
            );
        }
    }
}
