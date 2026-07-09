package com.velora;

import com.velora.ui.LoginScreen;

import javax.swing.*;

public class Velora_Motors {

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
}
