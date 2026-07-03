/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.velora;


import com.velora.ui.LoginScreen;
import com.velora.ui.ManagerDashboard;
import javax.swing.SwingUtilities;

public class VeloraMotors {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ManagerDashboard().setVisible(true));
    }
}
