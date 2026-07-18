//package com.velora;
//
//import com.velora.authentication.Customer;
//import com.velora.ui.ManagerDashboard;
//
//import javax.swing.*;
//import java.awt.*;
//
//public class Velora_Motors {
//
//    public static void main(String[] args) {
//        System.setProperty("sun.java2d.uiScale", "1.0");
//
//        SwingUtilities.invokeLater(() -> {
//            try {
//                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//            } catch (Exception ignored) {
//            }
//
//            Customer admin = new Customer(
//                    "System Admin",
//                    "admin@velora.com",
//                    "",
//                    Customer.Role.MANAGER
//            );
//
//            ManagerDashboard dashboard = new ManagerDashboard(admin);
//
//            Rectangle screen = GraphicsEnvironment
//                    .getLocalGraphicsEnvironment()
//                    .getMaximumWindowBounds();
//
//            dashboard.setBounds(screen);
//            dashboard.setMinimumSize(new Dimension(1280, 720));
//            dashboard.setExtendedState(JFrame.MAXIMIZED_BOTH);
//            dashboard.setVisible(true);
//        });
//    }
//}

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
