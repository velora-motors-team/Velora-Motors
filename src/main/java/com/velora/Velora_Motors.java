package com.velora;

import com.velora.ui.LuxuryLoginScreen;
import com.velora.ui.VeloraTheme;
import java.awt.EventQueue;
import javax.swing.UIManager;

public final class Velora_Motors {
    private Velora_Motors() {}

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        EventQueue.invokeLater(() -> {
            VeloraTheme.install();
            LuxuryLoginScreen screen = new LuxuryLoginScreen();
            screen.setVisible(true);
            screen.getGraphicsConfiguration().getDevice().setFullScreenWindow(screen);
        });
    }

}
