package com.velora.ui;

import com.velora.authentication.Customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class SupportPanel extends JPanel {

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

    private static final Color TEXT = new Color(243, 244, 247);
    private static final Color MUTED = new Color(157, 164, 175);
    private static final Color SOFT_TEXT = new Color(196, 202, 211);

    private static final Color GREEN = new Color(86, 207, 114);
    private static final Color BLUE = new Color(82, 156, 255);
    private static final Color RED = new Color(235, 93, 98);

    private static final Color PANEL_BORDER =
            new Color(214, 160, 66, 72);

    // =========================================================
    // DATA
    // =========================================================

    private final Customer customer;

    private final SupportMessageRepository messageRepository =
            new SupportMessageRepository();

    private JTextField subjectField;
    private JComboBox<String> categoryBox;
    private JTextArea messageArea;

    private JPanel chatMessagesPanel;
    private JTextField chatInput;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SupportPanel(Customer customer) {

        this.customer = customer;

        setOpaque(false);
        setLayout(new BorderLayout(0, 10));

        setBorder(
                new EmptyBorder(
                        12,
                        16,
                        12,
                        16
                )
        );

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);

        SwingUtilities.invokeLater(
                this::loadSupportConversation
        );
    }

    // =========================================================
    // HEADER
    // =========================================================

    private JComponent createHeader() {

        JPanel header = new JPanel(
                new BorderLayout()
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

        JLabel title = label(
                "Support Center",
                30,
                Font.BOLD,
                TEXT
        );

        JLabel sub = label(
                "Premium assistance for rentals, payments, returns, and vehicle issues.",
                12,
                Font.PLAIN,
                MUTED
        );

        left.add(title);
        left.add(Box.createVerticalStrut(5));
        left.add(sub);

        StatusPill online = new StatusPill(
                "Support Online",
                GREEN
        );

        header.add(left, BorderLayout.WEST);
        header.add(online, BorderLayout.EAST);

        return header;
    }

    // =========================================================
    // BODY
    // =========================================================

    private JComponent createBody() {

        JPanel body = new JPanel(
                new BorderLayout(12, 12)
        );

        body.setOpaque(false);

        JPanel topCards = new JPanel(
                new GridLayout(1, 3, 12, 0)
        );

        topCards.setOpaque(false);

        topCards.add(
                infoCard(
                        "Call Support",
                        "+970 59 123 4567",
                        "Available daily from 9 AM to 10 PM",
                        "CALL"
                )
        );

        topCards.add(
                infoCard(
                        "Email Support",
                        "support@velora.com",
                        "We usually reply within 24 hours",
                        "MAIL"
                )
        );

        topCards.add(
                infoCard(
                        "Emergency Help",
                        "Roadside Assistance",
                        "For urgent vehicle problems",
                        "WARN"
                )
        );

        body.add(
                topCards,
                BorderLayout.NORTH
        );

        JPanel center = new JPanel(
                new GridBagLayout()
        );

        center.setOpaque(false);

        JComponent ticketCard = createTicketCard();
        JComponent rightSide = createRightSide();

        GridBagConstraints leftGbc =
                new GridBagConstraints();

        leftGbc.gridx = 0;
        leftGbc.gridy = 0;
        leftGbc.weightx = 0.5;
        leftGbc.weighty = 0.0;
        leftGbc.fill = GridBagConstraints.HORIZONTAL;
        leftGbc.anchor = GridBagConstraints.NORTH;
        leftGbc.insets = new Insets(0, 0, 0, 6);

        GridBagConstraints rightGbc =
                new GridBagConstraints();

        rightGbc.gridx = 1;
        rightGbc.gridy = 0;
        rightGbc.weightx = 0.5;
        rightGbc.weighty = 0.0;
        rightGbc.fill = GridBagConstraints.HORIZONTAL;
        rightGbc.anchor = GridBagConstraints.NORTH;
        rightGbc.insets = new Insets(0, 6, 0, 0);

        center.add(ticketCard, leftGbc);
        center.add(rightSide, rightGbc);

        GridBagConstraints fillerGbc =
                new GridBagConstraints();

        fillerGbc.gridx = 0;
        fillerGbc.gridy = 1;
        fillerGbc.gridwidth = 2;
        fillerGbc.weighty = 1.0;
        fillerGbc.fill = GridBagConstraints.VERTICAL;

        center.add(Box.createVerticalGlue(), fillerGbc);

        body.add(center, BorderLayout.CENTER);

        return body;
    }

    // =========================================================
    // INFO CARDS
    // =========================================================

    private JComponent infoCard(
            String title,
            String value,
            String desc,
            String icon
    ) {

        LuxuryPanel card = new LuxuryPanel(
                18,
                CARD
        );

        card.setLayout(
                new BorderLayout(14, 0)
        );

        card.setBorder(
                new EmptyBorder(
                        13,
                        14,
                        13,
                        14
                )
        );

        SupportIcon iconLabel =
                new SupportIcon(icon);

        iconLabel.setPreferredSize(
                new Dimension(58, 58)
        );

        JPanel text = new JPanel();
        text.setOpaque(false);

        text.setLayout(
                new BoxLayout(
                        text,
                        BoxLayout.Y_AXIS
                )
        );

        text.add(
                label(
                        title,
                        12,
                        Font.BOLD,
                        TEXT
                )
        );

        text.add(Box.createVerticalStrut(5));

        text.add(
                label(
                        value,
                        17,
                        Font.BOLD,
                        GOLD_LIGHT
                )
        );

        text.add(Box.createVerticalStrut(5));

        text.add(
                label(
                        desc,
                        10,
                        Font.PLAIN,
                        MUTED
                )
        );

        card.add(iconLabel, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);

        return card;
    }

    // =========================================================
    // TICKET CARD
    // =========================================================

    private JComponent createTicketCard() {

        LuxuryPanel card = new LuxuryPanel(
                18,
                CARD
        );

        card.setLayout(
                new BorderLayout(0, 14)
        );

        card.setPreferredSize(
                new Dimension(100, 520)
        );

        card.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        520
                )
        );

        card.setBorder(
                new EmptyBorder(
                        16,
                        16,
                        16,
                        16
                )
        );

        // ---------------------------------------------------------
        // HEADER - stays fixed at the top, so the form never drops
        // ---------------------------------------------------------

        JPanel heading = new JPanel(
                new BorderLayout(12, 0)
        );

        heading.setOpaque(false);

        heading.setPreferredSize(
                new Dimension(10, 52)
        );

        heading.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        52
                )
        );

        JPanel headingText = new JPanel();
        headingText.setOpaque(false);

        headingText.setLayout(
                new BoxLayout(
                        headingText,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel title = label(
                "Open Support Ticket",
                20,
                Font.BOLD,
                TEXT
        );

        JLabel sub = label(
                "Send the issue details and our support team will contact you.",
                11,
                Font.PLAIN,
                MUTED
        );

        headingText.add(title);
        headingText.add(Box.createVerticalStrut(5));
        headingText.add(sub);

        SectionIcon ticketIcon =
                new SectionIcon(
                        "TICKET",
                        GOLD_LIGHT
                );

        ticketIcon.setPreferredSize(
                new Dimension(40, 40)
        );

        heading.add(
                headingText,
                BorderLayout.CENTER
        );

        heading.add(
                ticketIcon,
                BorderLayout.EAST
        );

        card.add(
                heading,
                BorderLayout.NORTH
        );

        // ---------------------------------------------------------
        // FORM - starts immediately under the header
        // ---------------------------------------------------------

        JPanel form = new JPanel();
        form.setOpaque(false);

        form.setLayout(
                new BoxLayout(
                        form,
                        BoxLayout.Y_AXIS
                )
        );

        subjectField = new DarkTextField(
                "Ticket subject"
        );

        subjectField.setPreferredSize(
                new Dimension(100, 44)
        );

        subjectField.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        44
                )
        );

        subjectField.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        form.add(fieldTitle("Subject"));
        form.add(subjectField);
        form.add(Box.createVerticalStrut(9));

        categoryBox = new DarkComboBox<>(
                new String[]{
                    "Problem with rental",
                    "Payment issue",
                    "Car pickup problem",
                    "Late return question",
                    "Account problem",
                    "Other"
                }
        );

        styleCombo(categoryBox);

        categoryBox.setPreferredSize(
                new Dimension(100, 44)
        );

        categoryBox.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        44
                )
        );

        categoryBox.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        form.add(fieldTitle("Category"));
        form.add(categoryBox);
        form.add(Box.createVerticalStrut(9));

        messageArea = new DarkTextArea(
                "Describe your issue here..."
        );

        JScrollPane messageScroll =
                new JScrollPane(messageArea);

        messageScroll.setBorder(
                BorderFactory.createLineBorder(
                        new Color(
                                214,
                                160,
                                66,
                                82
                        )
                )
        );

        messageScroll.setOpaque(false);

        messageScroll
                .getViewport()
                .setOpaque(false);

        messageScroll.setPreferredSize(
                new Dimension(100, 120)
        );

        messageScroll.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        120
                )
        );

        messageScroll.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        form.add(fieldTitle("Message"));
        form.add(messageScroll);
        form.add(Box.createVerticalStrut(12));

        JButton send = new GoldButton(
                "Submit Ticket",
                "SEND"
        );

        send.setPreferredSize(
                new Dimension(220, 42)
        );

        send.addActionListener(
                e -> submitTicket()
        );

        JPanel sendHolder = new JPanel(
                new FlowLayout(
                        FlowLayout.CENTER,
                        0,
                        0
                )
        );

        sendHolder.setOpaque(false);

        sendHolder.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        sendHolder.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        44
                )
        );

        sendHolder.add(send);

        form.add(sendHolder);
        form.add(Box.createVerticalGlue());

        card.add(
                form,
                BorderLayout.CENTER
        );

        return card;
    }

    // =========================================================
    // RIGHT SIDE
    // =========================================================

    private JComponent createRightSide() {

        JPanel right = new JPanel();

        right.setOpaque(false);

        right.setLayout(
                new BoxLayout(
                        right,
                        BoxLayout.Y_AXIS
                )
        );

        JComponent faqCard = createFaqCard();
        JComponent chatCard = createChatCard();

        faqCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        chatCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        faqCard.setPreferredSize(
                new Dimension(100, 300)
        );

        faqCard.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        300
                )
        );

        chatCard.setPreferredSize(
                new Dimension(100, 390)
        );

        chatCard.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        390
                )
        );

        right.add(faqCard);
        right.add(Box.createVerticalStrut(12));
        right.add(chatCard);

        return right;
    }

    // =========================================================
    // FAQ
    // =========================================================

    private JComponent createFaqCard() {

        LuxuryPanel card = new LuxuryPanel(
                18,
                CARD
        );

        card.setLayout(new BorderLayout());

        card.setBorder(
                new EmptyBorder(
                        16,
                        16,
                        16,
                        16
                )
        );

        JPanel list = new JPanel();
        list.setOpaque(false);

        list.setLayout(
                new BoxLayout(
                        list,
                        BoxLayout.Y_AXIS
                )
        );

        JPanel heading = new JPanel(
                new BorderLayout()
        );

        heading.setOpaque(false);

        JLabel title = label(
                "Frequently Asked Questions",
                20,
                Font.BOLD,
                TEXT
        );

        SectionIcon faqIcon =
                new SectionIcon("FAQ", GOLD_LIGHT);

        faqIcon.setPreferredSize(
                new Dimension(40, 40)
        );

        heading.add(title, BorderLayout.WEST);
        heading.add(faqIcon, BorderLayout.EAST);

        heading.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        list.add(heading);
        list.add(Box.createVerticalStrut(8));

        list.add(
                faqRow(
                        "How can I extend my rental?",
                        "Go to My Rentals and request an extension before return time."
                )
        );

        list.add(Box.createVerticalStrut(5));

        list.add(
                faqRow(
                        "What happens if I return late?",
                        "A late fee may be added based on delay duration."
                )
        );

        list.add(Box.createVerticalStrut(5));

        list.add(
                faqRow(
                        "Can I cancel a reservation?",
                        "Yes, before pickup time based on the rental policy."
                )
        );

        list.add(Box.createVerticalStrut(5));

        list.add(
                faqRow(
                        "How do I pay my invoice?",
                        "Open Billing & Invoices and choose Pay or Download Invoice."
                )
        );

        JPanel topHolder = new JPanel(new BorderLayout());
        topHolder.setOpaque(false);
        topHolder.add(list, BorderLayout.NORTH);

        card.add(topHolder, BorderLayout.CENTER);

        return card;
    }

    private JComponent faqRow(
            String question,
            String answer
    ) {

        FAQRow row = new FAQRow();

        row.setLayout(
                new BorderLayout(10, 0)
        );

        row.setBorder(
                new EmptyBorder(
                        10,
                        12,
                        10,
                        12
                )
        );

        row.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        54
                )
        );

        row.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        SectionIcon icon =
                new SectionIcon("FAQ_SMALL", GOLD);

        icon.setPreferredSize(
                new Dimension(24, 24)
        );

        JPanel text = new JPanel();
        text.setOpaque(false);

        text.setLayout(
                new BoxLayout(
                        text,
                        BoxLayout.Y_AXIS
                )
        );

        text.add(
                label(
                        question,
                        11,
                        Font.BOLD,
                        GOLD_LIGHT
                )
        );

        text.add(Box.createVerticalStrut(3));

        text.add(
                label(
                        answer,
                        10,
                        Font.PLAIN,
                        MUTED
                )
        );

        row.add(icon, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);

        return row;
    }

    // =========================================================
    // CHAT
    // =========================================================

    private JComponent createChatCard() {

        LuxuryPanel card = new LuxuryPanel(
                18,
                CARD
        );

        card.setLayout(
                new BorderLayout(0, 12)
        );

        card.setBorder(
                new EmptyBorder(
                        16,
                        16,
                        16,
                        16
                )
        );

        JPanel chatHeader = new JPanel(
                new BorderLayout()
        );

        chatHeader.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);

        titleBox.setLayout(
                new BoxLayout(
                        titleBox,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel title = label(
                "Live Chat & Support Replies",
                20,
                Font.BOLD,
                TEXT
        );

        JLabel subtitle = label(
                "Your conversation with Velora Support",
                10,
                Font.PLAIN,
                MUTED
        );

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(3));
        titleBox.add(subtitle);

        JButton refreshReplies =
                new OutlineButton(
                        "Refresh",
                        "REFRESH"
                );

        refreshReplies.setPreferredSize(
                new Dimension(92, 32)
        );

        refreshReplies.addActionListener(
                e -> loadSupportConversation()
        );

        chatHeader.add(
                titleBox,
                BorderLayout.WEST
        );

        chatHeader.add(
                refreshReplies,
                BorderLayout.EAST
        );

        card.add(
                chatHeader,
                BorderLayout.NORTH
        );

        chatMessagesPanel = new JPanel();

        chatMessagesPanel.setOpaque(false);

        chatMessagesPanel.setLayout(
                new BoxLayout(
                        chatMessagesPanel,
                        BoxLayout.Y_AXIS
                )
        );

        JScrollPane scroll =
                new JScrollPane(
                        chatMessagesPanel
                );

        scroll.setBorder(
                BorderFactory.createEmptyBorder()
        );

        scroll.setOpaque(false);

        scroll
                .getViewport()
                .setOpaque(false);

        scroll
                .getVerticalScrollBar()
                .setUnitIncrement(16);

        card.add(
                scroll,
                BorderLayout.CENTER
        );

        JPanel bottom = new JPanel(
                new BorderLayout(10, 0)
        );

        bottom.setOpaque(false);

        chatInput = new DarkTextField(
                "Write a quick message..."
        );

        JButton send = new GoldButton(
                "Send",
                "SEND"
        );

        send.setPreferredSize(
                new Dimension(92, 40)
        );

        send.addActionListener(
                e -> sendChatMessage()
        );

        bottom.add(
                chatInput,
                BorderLayout.CENTER
        );

        bottom.add(
                send,
                BorderLayout.EAST
        );

        card.add(
                bottom,
                BorderLayout.SOUTH
        );

        return card;
    }

    // =========================================================
    // SUBMIT TICKET
    // =========================================================

    private void submitTicket() {

        String subject =
                subjectField
                        .getText()
                        .trim();

        String message =
                messageArea
                        .getText()
                        .trim();

        if (subject.isBlank()
                || subject.equals(
                        "Ticket subject"
                )) {

            VeloraNotificationDialog.showWarning(
                    this,
                    "Missing Ticket Subject",
                    "Please enter a ticket subject before submitting."
            );

            return;
        }

        if (message.isBlank()
                || message.equals(
                        "Describe your issue here..."
                )) {

            VeloraNotificationDialog.showWarning(
                    this,
                    "Missing Ticket Message",
                    "Please describe your issue before submitting the ticket."
            );

            return;
        }

        String ticketId =
                "SUP-"
                + DateTimeFormatter
                        .ofPattern(
                                "yyyyMMdd-HHmmss"
                        )
                        .format(
                                LocalDateTime.now()
                        );

        String category =
                String.valueOf(
                        categoryBox
                                .getSelectedItem()
                );

        SupportMessage.MessageType messageType =
                getMessageTypeFromCategory(
                        category
                );

        String customerName =
                customer == null
                        ? "Customer"
                        : customer.getFullName();

        SupportMessage supportMessage =
                new SupportMessage(
                        ticketId,
                        customerName,
                        customerEmail(),
                        messageType,
                        subject,
                        message
                );

        messageRepository.saveMessage(
                supportMessage
        );

        loadSupportConversation();

        VeloraNotificationDialog.showSuccess(
                this,
                "Ticket Submitted Successfully",
                "Ticket ID: "
                + ticketId
                + " | "
                + category
        );

        subjectField.setText(
                "Ticket subject"
        );

        subjectField.setForeground(
                MUTED
        );

        messageArea.setText(
                "Describe your issue here..."
        );

        messageArea.setForeground(
                MUTED
        );
    }

    // =========================================================
    // CATEGORY → TYPE
    // =========================================================

    private SupportMessage.MessageType getMessageTypeFromCategory(
            String category
    ) {

        if (category == null) {
            return SupportMessage.MessageType.SUPPORT_TICKET;
        }

        return switch (category) {

            case "Problem with rental",
                 "Car pickup problem",
                 "Late return question"
                    -> SupportMessage.MessageType.RENTAL_ISSUE;

            case "Payment issue"
                    -> SupportMessage.MessageType.BILLING_ISSUE;

            case "Account problem"
                    -> SupportMessage.MessageType.TECHNICAL_ISSUE;

            default
                    -> SupportMessage.MessageType.SUPPORT_TICKET;
        };
    }

    // =========================================================
    // SEND CHAT
    // =========================================================

    private void sendChatMessage() {

        String msg =
                chatInput
                        .getText()
                        .trim();

        if (msg.isBlank()
                || msg.equals(
                        "Write a quick message..."
                )) {

            return;
        }

        String messageId =
                "CHAT-"
                + DateTimeFormatter
                        .ofPattern(
                                "yyyyMMdd-HHmmss"
                        )
                        .format(
                                LocalDateTime.now()
                        );

        String customerName =
                customer == null
                        ? "Customer"
                        : customer.getFullName();

        SupportMessage chatMessage =
                new SupportMessage(
                        messageId,
                        customerName,
                        customerEmail(),
                        SupportMessage.MessageType.LIVE_CHAT,
                        "Live Chat",
                        msg
                );

        messageRepository.saveMessage(
                chatMessage
        );

        chatInput.setText("");

        loadSupportConversation();
    }

    // =========================================================
    // LOAD CONVERSATION
    // =========================================================

    private void loadSupportConversation() {

        if (chatMessagesPanel == null) {
            return;
        }

        chatMessagesPanel.removeAll();

        String customerName =
                customer == null
                        ? "Customer"
                        : customer.getFullName();

        String customerEmail =
                customerEmail();

        List<SupportMessage> allMessages =
                messageRepository.loadMessages();

        chatMessagesPanel.add(
                createWelcomeBubble()
        );

        chatMessagesPanel.add(
                Box.createVerticalStrut(10)
        );

        boolean foundAny = false;

        for (SupportMessage supportMessage
                : allMessages) {

            boolean sameCustomerByEmail =
                    !customerEmail.isBlank()
                    && customerEmail.equalsIgnoreCase(
                            supportMessage
                                    .getCustomerEmail()
                    );

            boolean sameCustomerByName =
                    customerName != null
                    && customerName.equalsIgnoreCase(
                            supportMessage
                                    .getCustomerName()
                    );

            if (!sameCustomerByEmail
                    && !sameCustomerByName) {

                continue;
            }

            foundAny = true;

            if (supportMessage.getMessageType()
                    == SupportMessage.MessageType.LIVE_CHAT) {

                chatMessagesPanel.add(
                        createMessageBubble(
                                firstName(),
                                supportMessage.getMessage(),
                                supportMessage
                                        .getFormattedDateTime(),
                                true,
                                null
                        )
                );

                chatMessagesPanel.add(
                        Box.createVerticalStrut(8)
                );

                if (supportMessage.hasAdminReply()) {

                    chatMessagesPanel.add(
                            createMessageBubble(
                                    "Velora Support",
                                    supportMessage.getAdminReply(),
                                    supportMessage
                                            .getFormattedReplyDateTime(),
                                    false,
                                    null
                            )
                    );

                    chatMessagesPanel.add(
                            Box.createVerticalStrut(8)
                    );
                }

            } else if (supportMessage.hasAdminReply()) {

                chatMessagesPanel.add(
                        createMessageBubble(
                                "Velora Support",
                                supportMessage.getAdminReply(),
                                supportMessage
                                    .getFormattedReplyDateTime(),
                                false,
                                supportMessage.getStatus()
                        )
                );

                chatMessagesPanel.add(
                        Box.createVerticalStrut(8)
                );
            }
        }

        if (!foundAny) {

            JLabel empty = label(
                    "No previous support messages yet.",
                    11,
                    Font.PLAIN,
                    MUTED
            );

            empty.setAlignmentX(
                    Component.CENTER_ALIGNMENT
            );

            chatMessagesPanel.add(
                    Box.createVerticalStrut(18)
            );

            chatMessagesPanel.add(empty);
        }

        chatMessagesPanel.revalidate();
        chatMessagesPanel.repaint();
    }

    private JComponent createWelcomeBubble() {

        MessageBubble bubble = new MessageBubble(
                false,
                GREEN
        );

        bubble.setLayout(
                new BorderLayout(10, 0)
        );

        bubble.setBorder(
                new EmptyBorder(
                        12,
                        12,
                        12,
                        12
                )
        );

        SectionIcon icon =
                new SectionIcon(
                        "HEADSET",
                        GREEN
                );

        icon.setPreferredSize(
                new Dimension(28, 28)
        );

        JPanel text = new JPanel();
        text.setOpaque(false);

        text.setLayout(
                new BoxLayout(
                        text,
                        BoxLayout.Y_AXIS
                )
        );

        text.add(
                label(
                        "Velora Support",
                        11,
                        Font.BOLD,
                        GREEN
                )
        );

        text.add(Box.createVerticalStrut(3));

        text.add(
                label(
                        "Hello "
                        + firstName()
                        + ", how can we help you today?",
                        11,
                        Font.PLAIN,
                        TEXT
                )
        );

        bubble.add(icon, BorderLayout.WEST);
        bubble.add(text, BorderLayout.CENTER);

        return bubble;
    }

    private JComponent createMessageBubble(
            String sender,
            String message,
            String time,
            boolean customerMessage,
            SupportMessage.MessageStatus status
    ) {

        Color accent =
                customerMessage
                        ? GOLD
                        : GREEN;

        MessageBubble bubble =
                new MessageBubble(
                        customerMessage,
                        accent
                );

        bubble.setLayout(
                new BorderLayout(10, 0)
        );

        bubble.setBorder(
                new EmptyBorder(
                        12,
                        12,
                        10,
                        12
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

        JPanel top = new JPanel(
                new BorderLayout()
        );

        top.setOpaque(false);

        JLabel senderLabel = label(
                sender,
                11,
                Font.BOLD,
                accent
        );

        JLabel timeLabel = label(
                time == null ? "" : time,
                9,
                Font.PLAIN,
                MUTED
        );

        top.add(
                senderLabel,
                BorderLayout.WEST
        );

        top.add(
                timeLabel,
                BorderLayout.EAST
        );

        JTextArea messageText =
                new JTextArea(message);

        messageText.setEditable(false);
        messageText.setOpaque(false);

        messageText.setLineWrap(true);
        messageText.setWrapStyleWord(true);

        messageText.setForeground(TEXT);

        messageText.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        11
                )
        );

        messageText.setBorder(null);

        text.add(top);
        text.add(Box.createVerticalStrut(5));
        text.add(messageText);

        if (status != null) {

            text.add(Box.createVerticalStrut(7));

            StatusPill statusPill =
                    new StatusPill(
                            status.toString(),
                            statusColor(status)
                    );

            statusPill.setAlignmentX(
                    Component.LEFT_ALIGNMENT
            );

            text.add(statusPill);
        }

        bubble.add(text, BorderLayout.CENTER);

        return bubble;
    }

    private Color statusColor(
            SupportMessage.MessageStatus status
    ) {

        return switch (status) {
            case RESOLVED -> GREEN;
            case IN_PROGRESS -> BLUE;
            default -> GOLD_LIGHT;
        };
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private String customerEmail() {

        if (customer == null
                || customer.getEmail() == null) {

            return "";
        }

        return customer.getEmail().trim();
    }

    private JLabel fieldTitle(String text) {

        JLabel label = label(
                text,
                11,
                Font.BOLD,
                GOLD_LIGHT
        );

        label.setBorder(
                new EmptyBorder(
                        0,
                        2,
                        6,
                        0
                )
        );

        label.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        return label;
    }

    private String firstName() {

        String name =
                customer == null
                        ? "Customer"
                        : customer.getFullName();

        if (name == null
                || name.isBlank()) {

            return "Customer";
        }

        return name.split(" ")[0];
    }

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

    // =========================================================
    // COMBO STYLE
    // =========================================================

    private void styleCombo(
            JComboBox<String> combo
    ) {

        combo.setForeground(TEXT);

        combo.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        combo.setFocusable(false);
        combo.setOpaque(false);

        combo.setBorder(
                BorderFactory.createEmptyBorder()
        );

        combo.setRenderer(
                new DefaultListCellRenderer() {

                    @Override
                    public Component getListCellRendererComponent(
                            JList<?> list,
                            Object value,
                            int index,
                            boolean isSelected,
                            boolean cellHasFocus
                    ) {

                        JLabel label =
                                (JLabel) super
                                        .getListCellRendererComponent(
                                                list,
                                                value,
                                                index,
                                                isSelected,
                                                cellHasFocus
                                        );

                        label.setFont(
                                new Font(
                                        "Segoe UI",
                                        Font.BOLD,
                                        12
                                )
                        );

                        label.setBorder(
                                new EmptyBorder(
                                        9,
                                        14,
                                        9,
                                        14
                                )
                        );

                        if (isSelected) {

                            label.setBackground(
                                    new Color(
                                            93,
                                            58,
                                            23
                                    )
                            );

                            label.setForeground(
                                    GOLD_LIGHT
                            );

                        } else {

                            label.setBackground(
                                    new Color(
                                            3,
                                            9,
                                            15
                                    )
                            );

                            label.setForeground(TEXT);
                        }

                        return label;
                    }
                }
        );

        combo.setUI(
                new BasicComboBoxUI() {

                    @Override
                    protected JButton createArrowButton() {

                        JButton button =
                                new JButton();

                        button.setIcon(
                                new VectorIcon(
                                        "CHEVRON",
                                        GOLD_LIGHT,
                                        14
                                )
                        );

                        button.setBorder(
                                BorderFactory
                                        .createEmptyBorder()
                        );

                        button.setFocusPainted(false);
                        button.setOpaque(false);

                        button.setContentAreaFilled(
                                false
                        );

                        return button;
                    }
                }
        );
    }

    // =========================================================
    // VECTOR ICONS
    // =========================================================

    private static void drawIcon(
            Graphics2D g,
            String type,
            int x,
            int y,
            int s
    ) {

        switch (type) {

            case "SEND" -> {

                Path2D send =
                        new Path2D.Double();

                send.moveTo(x + 1, y + 2);

                send.lineTo(
                        x + s - 1,
                        y + s / 2.0
                );

                send.lineTo(
                        x + 1,
                        y + s - 2
                );

                send.lineTo(
                        x + 5,
                        y + s / 2.0
                );

                send.closePath();

                g.draw(send);

                g.drawLine(
                        x + 5,
                        y + s / 2,
                        x + s - 4,
                        y + s / 2
                );
            }

            case "REFRESH" -> {

                g.drawArc(
                        x + 2,
                        y + 2,
                        s - 4,
                        s - 4,
                        35,
                        285
                );

                Path2D arrow =
                        new Path2D.Double();

                arrow.moveTo(
                        x + s - 5,
                        y + 2
                );

                arrow.lineTo(
                        x + s - 1,
                        y + 5
                );

                arrow.lineTo(
                        x + s - 6,
                        y + 7
                );

                arrow.closePath();

                g.fill(arrow);
            }

            case "CHEVRON" -> {

                g.drawLine(
                        x + 3,
                        y + 5,
                        x + s / 2,
                        y + s - 4
                );

                g.drawLine(
                        x + s / 2,
                        y + s - 4,
                        x + s - 3,
                        y + 5
                );
            }

            case "TICKET" -> {

                g.drawRoundRect(
                        x + 1,
                        y + 3,
                        s - 2,
                        s - 6,
                        4,
                        4
                );

                g.drawLine(
                        x + s / 2,
                        y + 5,
                        x + s / 2,
                        y + s - 5
                );
            }

            case "FAQ", "FAQ_SMALL" -> {

                g.drawOval(
                        x + 2,
                        y + 2,
                        s - 4,
                        s - 4
                );

                g.drawArc(
                        x + s / 3,
                        y + s / 4,
                        s / 3,
                        s / 3,
                        0,
                        200
                );

                g.drawLine(
                        x + s / 2,
                        y + s / 2,
                        x + s / 2,
                        y + s / 2 + 3
                );

                g.fillOval(
                        x + s / 2 - 1,
                        y + s - 6,
                        3,
                        3
                );
            }

            case "HEADSET" -> {

                g.drawArc(
                        x + 2,
                        y + 2,
                        s - 4,
                        s - 4,
                        20,
                        140
                );

                g.drawArc(
                        x + 2,
                        y + 2,
                        s - 4,
                        s - 4,
                        200,
                        140
                );

                g.drawRoundRect(
                        x,
                        y + s / 2 - 2,
                        4,
                        7,
                        2,
                        2
                );

                g.drawRoundRect(
                        x + s - 4,
                        y + s / 2 - 2,
                        4,
                        7,
                        2,
                        2
                );
            }

            default -> {

                g.drawOval(
                        x + 2,
                        y + 2,
                        s - 4,
                        s - 4
                );
            }
        }
    }

    private static final class VectorIcon
            implements Icon {

        private final String type;
        private final Color color;
        private final int size;

        VectorIcon(
                String type,
                Color color,
                int size
        ) {

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
        public void paintIcon(
                Component c,
                Graphics raw,
                int x,
                int y
        ) {

            Graphics2D g =
                    (Graphics2D) raw.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g.setColor(color);

            g.setStroke(
                    new BasicStroke(
                            1.8f,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND
                    )
            );

            drawIcon(
                    g,
                    type,
                    x,
                    y,
                    size
            );

            g.dispose();
        }
    }

    private static final class SectionIcon
            extends JComponent {

        private final String type;
        private final Color color;

        SectionIcon(
                String type,
                Color color
        ) {

            this.type = type;
            this.color = color;

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
                            color.getRed(),
                            color.getGreen(),
                            color.getBlue(),
                            22
                    )
            );

            g.fillOval(
                    x,
                    y,
                    s,
                    s
            );

            g.setColor(color);

            g.setStroke(
                    new BasicStroke(
                            1.8f,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND
                    )
            );

            drawIcon(
                    g,
                    type,
                    x + 5,
                    y + 5,
                    Math.max(8, s - 10)
            );

            g.dispose();
        }
    }

    // =========================================================
    // SUPPORT ICON
    // =========================================================

    private static final class SupportIcon
            extends JComponent {

        private final String type;

        SupportIcon(String type) {
            this.type = type;
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

            g.setStroke(
                    new BasicStroke(
                            2.2f,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND
                    )
            );

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            g.setColor(
                    new Color(
                            214,
                            160,
                            66,
                            24
                    )
            );

            g.fillOval(
                    cx - 25,
                    cy - 25,
                    50,
                    50
            );

            g.setColor(GOLD);

            switch (type) {

                case "CALL" -> {

                    g.drawArc(
                            cx - 17,
                            cy - 17,
                            34,
                            34,
                            135,
                            110
                    );

                    g.drawLine(
                            cx - 14,
                            cy - 4,
                            cx - 7,
                            cy + 5
                    );

                    g.drawLine(
                            cx + 7,
                            cy + 5,
                            cx + 14,
                            cy - 4
                    );

                    g.drawRoundRect(
                            cx - 18,
                            cy + 4,
                            8,
                            13,
                            4,
                            4
                    );

                    g.drawRoundRect(
                            cx + 10,
                            cy + 4,
                            8,
                            13,
                            4,
                            4
                    );
                }

                case "MAIL" -> {

                    g.drawRoundRect(
                            cx - 18,
                            cy - 12,
                            36,
                            24,
                            5,
                            5
                    );

                    g.drawLine(
                            cx - 18,
                            cy - 10,
                            cx,
                            cy + 3
                    );

                    g.drawLine(
                            cx + 18,
                            cy - 10,
                            cx,
                            cy + 3
                    );
                }

                default -> {

                    Path2D triangle =
                            new Path2D.Double();

                    triangle.moveTo(
                            cx,
                            cy - 20
                    );

                    triangle.lineTo(
                            cx + 20,
                            cy + 16
                    );

                    triangle.lineTo(
                            cx - 20,
                            cy + 16
                    );

                    triangle.closePath();

                    g.draw(triangle);

                    g.drawLine(
                            cx,
                            cy - 8,
                            cx,
                            cy + 5
                    );

                    g.fillOval(
                            cx - 2,
                            cy + 10,
                            4,
                            4
                    );
                }
            }

            g.dispose();
        }
    }

    // =========================================================
    // PANELS
    // =========================================================

    private static class LuxuryPanel
            extends JPanel {

        private final int radius;
        private final Color fill;

        LuxuryPanel(
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

            int w = getWidth();
            int h = getHeight();

            g.setColor(
                    new Color(
                            0,
                            0,
                            0,
                            72
                    )
            );

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

            g.setPaint(
                    new GradientPaint(
                            0,
                            0,
                            new Color(13, 23, 33),
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
                                    0
                            )
                    )
            );

            g.fill(shape);

            g.setColor(PANEL_BORDER);
            g.draw(shape);

            g.dispose();

            super.paintComponent(raw);
        }
    }

    private static final class FAQRow
            extends JPanel {

        FAQRow() {
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

            g.setColor(
                    new Color(
                            9,
                            18,
                            27,
                            220
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

            g.setColor(
                    new Color(
                            214,
                            160,
                            66,
                            45
                    )
            );

            g.drawRoundRect(
                    0,
                    0,
                    getWidth() - 1,
                    getHeight() - 1,
                    12,
                    12
            );

            g.dispose();

            super.paintComponent(raw);
        }
    }

    private static final class MessageBubble
            extends JPanel {

        private final boolean customerMessage;
        private final Color accent;

        MessageBubble(
                boolean customerMessage,
                Color accent
        ) {

            this.customerMessage =
                    customerMessage;

            this.accent = accent;

            setOpaque(false);

            setMaximumSize(
                    new Dimension(
                            Integer.MAX_VALUE,
                            120
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

            Color fill =
                    customerMessage
                            ? new Color(21, 19, 14)
                            : new Color(8, 20, 20);

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
                    new Color(
                            accent.getRed(),
                            accent.getGreen(),
                            accent.getBlue(),
                            95
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

            g.setColor(accent);

            g.fillRoundRect(
                    customerMessage
                            ? getWidth() - 3
                            : 0,
                    8,
                    3,
                    Math.max(
                            0,
                            getHeight() - 16
                    ),
                    3,
                    3
            );

            g.dispose();

            super.paintComponent(raw);
        }
    }

    // =========================================================
    // STATUS PILL
    // =========================================================

    private static final class StatusPill
            extends JComponent {

        private final String text;
        private final Color color;

        StatusPill(
                String text,
                Color color
        ) {

            this.text = text;
            this.color = color;

            int width =
                    Math.max(
                            92,
                            text.length() * 8 + 26
                    );

            setPreferredSize(
                    new Dimension(width, 28)
            );

            setMaximumSize(
                    new Dimension(width, 28)
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
                            25
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
                            10
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

            g.drawString(text, tx, ty);

            g.dispose();
        }
    }

    // =========================================================
    // BUTTONS
    // =========================================================

    private static final class GoldButton
            extends JButton {

        GoldButton(String text) {
            this(text, null);
        }

        GoldButton(
                String text,
                String iconType
        ) {

            super(text);

            if (iconType != null) {

                setIcon(
                        new VectorIcon(
                                iconType,
                                new Color(
                                        30,
                                        20,
                                        8
                                ),
                                15
                        )
                );

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
                            12
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

            g.setPaint(
                    new GradientPaint(
                            0,
                            0,
                            getModel().isRollover()
                                    ? new Color(
                                            250,
                                            219,
                                            158
                                    )
                                    : GOLD_LIGHT,
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

    private static final class OutlineButton
            extends JButton {

        OutlineButton(
                String text,
                String iconType
        ) {

            super(text);

            if (iconType != null) {

                setIcon(
                        new VectorIcon(
                                iconType,
                                GOLD_LIGHT,
                                14
                        )
                );

                setIconTextGap(7);
            }

            setOpaque(false);
            setContentAreaFilled(false);
            setFocusPainted(false);

            setForeground(GOLD_LIGHT);

            setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            11
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
                                    110
                            )
                    )
            );
        }
    }

    // =========================================================
    // INPUTS
    // =========================================================

    private static final class DarkTextField
            extends JTextField {

        private final String placeholder;

        DarkTextField(String placeholder) {

            this.placeholder = placeholder;

            setText(placeholder);
            setOpaque(false);
            setForeground(MUTED);
            setCaretColor(GOLD_LIGHT);

            setFont(
                    new Font(
                            "Segoe UI",
                            Font.PLAIN,
                            12
                    )
            );

            setBorder(
                    new EmptyBorder(
                            0,
                            14,
                            0,
                            14
                    )
            );

            addFocusListener(
                    new FocusAdapter() {

                        @Override
                        public void focusGained(
                                FocusEvent e
                        ) {

                            if (getText()
                                    .equals(
                                            placeholder
                                    )) {

                                setText("");
                                setForeground(TEXT);
                            }
                        }

                        @Override
                        public void focusLost(
                                FocusEvent e
                        ) {

                            if (getText()
                                    .isBlank()) {

                                setText(placeholder);
                                setForeground(MUTED);
                            }
                        }
                    }
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
                            3,
                            9,
                            15
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

            g.setColor(
                    new Color(
                            214,
                            160,
                            66,
                            isFocusOwner()
                                    ? 145
                                    : 72
                    )
            );

            g.drawRoundRect(
                    0,
                    0,
                    getWidth() - 1,
                    getHeight() - 1,
                    10,
                    10
            );

            g.dispose();

            super.paintComponent(raw);
        }
    }

    private static final class DarkTextArea
            extends JTextArea {

        private final String placeholder;

        DarkTextArea(String placeholder) {

            this.placeholder = placeholder;

            setText(placeholder);
            setOpaque(false);
            setForeground(MUTED);
            setCaretColor(GOLD_LIGHT);

            setFont(
                    new Font(
                            "Segoe UI",
                            Font.PLAIN,
                            12
                    )
            );

            setLineWrap(true);
            setWrapStyleWord(true);

            setBorder(
                    new EmptyBorder(
                            12,
                            14,
                            12,
                            14
                    )
            );

            addFocusListener(
                    new FocusAdapter() {

                        @Override
                        public void focusGained(
                                FocusEvent e
                        ) {

                            if (getText()
                                    .equals(
                                            placeholder
                                    )) {

                                setText("");
                                setForeground(TEXT);
                            }
                        }

                        @Override
                        public void focusLost(
                                FocusEvent e
                        ) {

                            if (getText()
                                    .isBlank()) {

                                setText(placeholder);
                                setForeground(MUTED);
                            }
                        }
                    }
            );
        }
    }

    private static final class DarkComboBox<E>
            extends JComboBox<E> {

        DarkComboBox(E[] items) {

            super(items);

            setOpaque(false);

            setBackground(
                    new Color(
                            3,
                            9,
                            15
                    )
            );

            setForeground(TEXT);

            setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            12
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
                    new Color(
                            3,
                            9,
                            15
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

            g.setColor(
                    new Color(
                            214,
                            160,
                            66,
                            120
                    )
            );

            g.drawRoundRect(
                    0,
                    0,
                    getWidth() - 1,
                    getHeight() - 1,
                    10,
                    10
            );

            g.dispose();

            super.paintComponent(raw);
        }
    }
}
