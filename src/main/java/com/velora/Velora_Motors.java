package com.velora;

import com.velora.ui.LoginScreen;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Velora_Motors {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            LoginScreen screen = new LoginScreen();
            screen.setVisible(true);
            
            
            
        });
    }
}