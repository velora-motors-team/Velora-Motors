package com.velora.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.Color;
import javax.swing.UIManager;

/** Application-wide FlatLaf theme tokens for the Velora luxury interface. */
public final class VeloraTheme {
    private VeloraTheme() {}

    public static void install() {
        FlatDarkLaf.setup();
        UIManager.put("Component.arc", 12);
        UIManager.put("Button.arc", 14);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("CheckBox.arc", 5);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 1);
        UIManager.put("Component.focusColor", new Color(214, 168, 91));
        UIManager.put("Component.borderColor", new Color(255, 215, 150, 35));
        UIManager.put("TextField.background", new Color(8, 14, 20));
        UIManager.put("TextField.foreground", new Color(248, 248, 248));
        UIManager.put("PasswordField.background", new Color(8, 14, 20));
        UIManager.put("PasswordField.foreground", new Color(248, 248, 248));
        UIManager.put("CheckBox.icon.focusedBorderColor", new Color(214, 168, 91));
        UIManager.put("CheckBox.icon.selectedBackground", new Color(214, 168, 91));
        UIManager.put("CheckBox.icon.checkmarkColor", new Color(20, 15, 10));
        UIManager.put("ToolTip.background", new Color(15, 18, 24));
        UIManager.put("ToolTip.foreground", Color.WHITE);
    }
}
