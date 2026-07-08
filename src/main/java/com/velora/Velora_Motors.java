//package com.velora;
//
//import com.velora.ui.LoginScreen;
//import javax.swing.SwingUtilities;
//import javax.swing.UIManager;
//
//public class Velora_Motors {
//
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            try {
//                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//            } catch (Exception ignored) {
//            }
//
//            LoginScreen screen = new LoginScreen();
//            screen.setVisible(true);
//            
//            
//            
//        });
//    }
//}

//package com.velora;
//
//import com.velora.authentication.Customer;
//import com.velora.ui.CustomerDashboard;
//import javax.swing.SwingUtilities;
//import javax.swing.UIManager;
//
//public class Velora_Motors {
//
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            try {
//                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//            } catch (Exception ignored) {
//            }
//
//            Customer demoCustomer = new Customer(
//                    "Omar Al-Khatib",
//                    "omar@velora.com",
//                    "0599000000",
//                    Customer.Role.CUSTOMER
//            );
//
//            CustomerDashboard screen = new CustomerDashboard(demoCustomer);
//            screen.setVisible(true);
//        });
//    }
//}

//package com.velora;
//
//import com.velora.ui.VehicleCatalog;
//import javax.swing.SwingUtilities;
//import javax.swing.UIManager;
//
//public class Velora_Motors {
//
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            try {
//                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//            } catch (Exception ignored) {
//            }
//
//            VehicleCatalog screen = new VehicleCatalog();
//            screen.setVisible(true);
//        });
//    }
//}

//package com.velora;
//
//import com.velora.ui.LoginScreen;
//import javax.swing.SwingUtilities;
//import javax.swing.UIManager;
//public class Velora_Motors {
//
//   public static void main(String[] args) {
//      SwingUtilities.invokeLater(() -> {
//          try {             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//           } catch (Exception ignored) {
//          }
//
//           LoginScreen screen = new LoginScreen();
//           screen.setVisible(true);
//       });
//    }
//}

package com.velora;

import com.velora.authentication.Customer;
import com.velora.ui.ManagerDashboard;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Velora_Motors {

    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale", "1.0");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            Customer admin = new Customer(
                    "System Admin",
                    "admin@velora.com",
                    "",
                    Customer.Role.MANAGER
            );

            ManagerDashboard dashboard = new ManagerDashboard(admin);
            dashboard.setVisible(true);
        });
    }
}
