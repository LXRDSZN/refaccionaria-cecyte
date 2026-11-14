/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.refaccionaria.automotriz;

import com.formdev.flatlaf.FlatLightLaf;
import com.mycompany.refaccionaria.automotriz.controller.LoginController;
import com.mycompany.refaccionaria.automotriz.service.AuthService;
import com.mycompany.refaccionaria.automotriz.view.LoginView;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class RefaccionariaAutomotriz {

    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            AuthService authService = new AuthService();
            try {
                authService.initializeFirstAdminFromEnvironment();
                LoginView.showWindow(new LoginController(authService));
            } catch (SQLException exception) {
                JOptionPane.showMessageDialog(
                        null,
                        "No fue posible inicializar la aplicación porque no se pudo conectar con MariaDB.",
                        "Error de conexión",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
