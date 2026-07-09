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
//
package com.velora;

import com.velora.ui.LoginScreen;
import com.velora.authentication.Customer;
import com.velora.ui.CustomerDashboard;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import javax.swing.*;

public class Velora_Motors {

    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale", "1.0");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            ///////
            new LoginScreen().setVisible(true);
            Customer customer = new Customer(
                    "Omar Al-Khatib",
                    "omar@velora.com",
                    "0599000000",
                    Customer.Role.CUSTOMER
            );

            CustomerDashboard dashboard = new CustomerDashboard(customer);

            Rectangle screen = GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getMaximumWindowBounds();

            dashboard.setBounds(screen);
            dashboard.setMinimumSize(new Dimension(1280, 720));
            dashboard.setExtendedState(JFrame.MAXIMIZED_BOTH);
            dashboard.setVisible(true);
        });
    }
}
