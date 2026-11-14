package com.mycompany.refaccionaria.automotriz.service;

import com.mycompany.refaccionaria.automotriz.model.Rol;
import com.mycompany.refaccionaria.automotriz.model.Usuario;

public final class AppSession {

    private Usuario usuario;

    public AppSession(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public boolean isAuthenticated() {
        return usuario != null;
    }

    public boolean isAdmin() {
        return isAuthenticated() && usuario.getRol() == Rol.ADMIN;
    }

    public boolean isEmpleado() {
        return isAuthenticated() && usuario.getRol() == Rol.EMPLEADO;
    }

    public void logout() {
        usuario = null;
    }

    public void requireAuthenticated() {
        if (!isAuthenticated()) {
            throw new SecurityException("La sesión no está autenticada.");
        }
    }

    public void requireAdmin() {
        requireAuthenticated();
        if (!isAdmin()) {
            throw new SecurityException("La operación requiere permisos de administrador.");
        }
    }
}
