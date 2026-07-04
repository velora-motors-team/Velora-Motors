package com.velora.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.Color;
import javax.swing.UIManager;

public final class VeloraTheme {

    private VeloraTheme() {
    }

    public static final Color BACKGROUND = new Color(4, 8, 14);
    public static final Color PANEL = new Color(6, 11, 18);
    public static final Color CARD = new Color(8, 14, 22);
    public static final Color CARD_LIGHT = new Color(13, 22, 34);
    public static final Color FIELD = new Color(7, 13, 20);
    public static final Color FIELD_FOCUS = new Color(10, 18, 28);

    public static final Color GOLD = new Color(214, 168, 91);
    public static final Color GOLD_LIGHT = new Color(236, 203, 157);
    public static final Color GOLD_DARK = new Color(165, 118, 62);

    public static final Color TEXT = new Color(248, 248, 248);
    public static final Color MUTED = new Color(170, 176, 186);
    public static final Color SOFT_MUTED = new Color(125, 135, 150);

    public static final Color SUCCESS = new Color(70, 220, 135);
    public static final Color DANGER = new Color(210, 55, 65);
    public static final Color DANGER_DARK = new Color(95, 14, 24);
    public static final Color BLUE_TEXT = new Color(106, 140, 192);

    public static final int ARC_SMALL = 10;
    public static final int ARC_MEDIUM = 16;
    public static final int ARC_LARGE = 22;

    public static void install() {
        FlatDarkLaf.setup();

        UIManager.put("Component.arc", ARC_MEDIUM);
        UIManager.put("Button.arc", ARC_MEDIUM);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("CheckBox.arc", 6);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.trackArc", 999);
        UIManager.put("Table.arc", 14);

        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 1);
        UIManager.put("Component.focusColor", GOLD);
        UIManager.put("Component.borderColor", new Color(255, 215, 150, 45));
        UIManager.put("Component.disabledBorderColor", new Color(255, 215, 150, 22));

        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("Label.foreground", TEXT);

        UIManager.put("Button.background", GOLD);
        UIManager.put("Button.foreground", new Color(25, 18, 10));
        UIManager.put("Button.hoverBackground", GOLD_LIGHT);
        UIManager.put("Button.pressedBackground", GOLD_DARK);
        UIManager.put("Button.default.background", GOLD);
        UIManager.put("Button.default.foreground", new Color(25, 18, 10));
        UIManager.put("Button.borderColor", new Color(255, 225, 175, 75));
        UIManager.put("Button.disabledBackground", new Color(20, 30, 45));
        UIManager.put("Button.disabledText", SOFT_MUTED);

        UIManager.put("TextField.background", FIELD);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.inactiveForeground", MUTED);
        UIManager.put("TextField.placeholderForeground", SOFT_MUTED);
        UIManager.put("TextField.caretForeground", GOLD);
        UIManager.put("TextField.selectionBackground", new Color(214, 168, 91, 95));
        UIManager.put("TextField.selectionForeground", TEXT);

        UIManager.put("PasswordField.background", FIELD);
        UIManager.put("PasswordField.foreground", TEXT);
        UIManager.put("PasswordField.inactiveForeground", MUTED);
        UIManager.put("PasswordField.placeholderForeground", SOFT_MUTED);
        UIManager.put("PasswordField.caretForeground", GOLD);
        UIManager.put("PasswordField.selectionBackground", new Color(214, 168, 91, 95));
        UIManager.put("PasswordField.selectionForeground", TEXT);

        UIManager.put("CheckBox.background", BACKGROUND);
        UIManager.put("CheckBox.foreground", TEXT);
        UIManager.put("CheckBox.icon.background", new Color(8, 14, 20));
        UIManager.put("CheckBox.icon.borderColor", new Color(214, 168, 91, 120));
        UIManager.put("CheckBox.icon.focusedBorderColor", GOLD);
        UIManager.put("CheckBox.icon.selectedBackground", GOLD);
        UIManager.put("CheckBox.icon.selectedBorderColor", GOLD_LIGHT);
        UIManager.put("CheckBox.icon.checkmarkColor", new Color(20, 15, 10));

        UIManager.put("Table.background", new Color(9, 16, 27));
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.selectionBackground", new Color(35, 55, 82));
        UIManager.put("Table.selectionForeground", TEXT);
        UIManager.put("Table.gridColor", new Color(35, 47, 65));
        UIManager.put("TableHeader.background", new Color(13, 23, 38));
        UIManager.put("TableHeader.foreground", GOLD_LIGHT);

        UIManager.put("ScrollPane.background", BACKGROUND);
        UIManager.put("ScrollPane.border", null);
        UIManager.put("ScrollBar.thumb", new Color(60, 72, 92));
        UIManager.put("ScrollBar.track", new Color(8, 14, 22));

        UIManager.put("ToolTip.background", new Color(15, 18, 24));
        UIManager.put("ToolTip.foreground", Color.WHITE);
        UIManager.put("ToolTip.borderColor", new Color(214, 168, 91, 85));

        UIManager.put("OptionPane.background", CARD);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("OptionPane.buttonAreaBorder", null);
    }

    public static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
}
