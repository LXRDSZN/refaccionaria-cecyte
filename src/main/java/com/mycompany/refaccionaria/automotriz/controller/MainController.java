package com.mycompany.refaccionaria.automotriz.controller;

import com.mycompany.refaccionaria.automotriz.service.AppSession;
import com.mycompany.refaccionaria.automotriz.service.AuthService;

public class MainController {

    private final AppSession session;
    private final AuthService authService;

    public MainController(AppSession session, AuthService authService) {
        this.session = session;
        this.authService = authService;
    }

    public AppSession getSession() {
        return session;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public void logout() {
        session.logout();
    }
}
