package com.mycompany.refaccionaria.automotriz.service;

import com.mycompany.refaccionaria.automotriz.dao.UsuarioDAO;
import com.mycompany.refaccionaria.automotriz.model.Rol;
import com.mycompany.refaccionaria.automotriz.model.Usuario;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UsuarioDAO usuarioDAO;

    public AuthService() {
        this(new UsuarioDAO());
    }

    public AuthService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public Optional<AppSession> authenticate(String nombreUsuario, char[] password) throws SQLException {
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty() || password == null || password.length == 0) {
            clear(password);
            return Optional.empty();
        }

        try {
            Optional<Usuario> usuario = usuarioDAO.findByUsername(nombreUsuario.trim());
            if (!usuario.isPresent() || !BCrypt.checkpw(new String(password), usuario.get().getPasswordHash())) {
                return Optional.empty();
            }
            return Optional.of(new AppSession(usuario.get()));
        } finally {
            clear(password);
        }
    }

    public Optional<Usuario> initializeFirstAdminFromEnvironment() throws SQLException {
        if (usuarioDAO.count() > 0) {
            return Optional.empty();
        }

        String username = System.getenv("INITIAL_ADMIN_USER");
        String password = System.getenv("INITIAL_ADMIN_PASSWORD");
        if (username == null || username.trim().isEmpty() || password == null || password.length() < MIN_PASSWORD_LENGTH) {
            return Optional.empty();
        }

        char[] passwordChars = password.toCharArray();
        try {
            Usuario admin = new Usuario(username.trim(), BCrypt.hashpw(password, BCrypt.gensalt()), Rol.ADMIN);
            return Optional.of(usuarioDAO.save(admin));
        } finally {
            clear(passwordChars);
        }
    }

    public Usuario createUser(AppSession session, String nombreUsuario, char[] password, Rol rol)
            throws SQLException {
        session.requireAdmin();
        validateUsername(nombreUsuario);
        validatePassword(password);
        if (rol == null) {
            throw new IllegalArgumentException("El rol es obligatorio.");
        }

        try {
            String passwordHash = BCrypt.hashpw(new String(password), BCrypt.gensalt());
            return usuarioDAO.save(new Usuario(nombreUsuario.trim(), passwordHash, rol));
        } finally {
            clear(password);
        }
    }

    public List<Usuario> findUsers(AppSession session) throws SQLException {
        session.requireAdmin();
        return usuarioDAO.findAll();
    }

    public void changePassword(AppSession session, int targetUserId, char[] newPassword) throws SQLException {
        session.requireAdmin();
        validatePassword(newPassword);
        try {
            usuarioDAO.updatePassword(targetUserId, BCrypt.hashpw(new String(newPassword), BCrypt.gensalt()));
        } finally {
            clear(newPassword);
        }
    }

    public void updateUser(AppSession session, Usuario usuario) throws SQLException {
        session.requireAdmin();
        if (usuario == null || usuario.getId() == null || usuario.getRol() == null) {
            throw new IllegalArgumentException("Los datos del usuario son obligatorios.");
        }
        validateUsername(usuario.getNombreUsuario());

        Usuario current = usuarioDAO.findById(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));
        if (current.getRol() == Rol.ADMIN && usuario.getRol() != Rol.ADMIN && usuarioDAO.countAdmins() <= 1) {
            throw new IllegalStateException("Debe existir al menos un administrador.");
        }
        usuarioDAO.updateIdentityAndRole(usuario);
    }

    public void requireAdmin(AppSession session) {
        session.requireAdmin();
    }

    public void requireAuthenticated(AppSession session) {
        session.requireAuthenticated();
    }

    private void validateUsername(String nombreUsuario) {
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty() || nombreUsuario.trim().length() > 50) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio y debe tener hasta 50 caracteres.");
        }
    }

    private void validatePassword(char[] password) {
        if (password == null || password.length < MIN_PASSWORD_LENGTH) {
            clear(password);
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
        }
    }

    private void clear(char[] value) {
        if (value != null) {
            Arrays.fill(value, '\0');
        }
    }
}
