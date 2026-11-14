package com.mycompany.refaccionaria.automotriz.controller;

import com.mycompany.refaccionaria.automotriz.service.AppSession;
import com.mycompany.refaccionaria.automotriz.service.AuthService;
import java.sql.SQLException;
import java.util.Optional;

public class LoginController {

    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    public Optional<AppSession> authenticate(String nombreUsuario, char[] password) throws SQLException {
        return authService.authenticate(nombreUsuario, password);
    }

    public AuthService getAuthService() {
        return authService;
    }
}
