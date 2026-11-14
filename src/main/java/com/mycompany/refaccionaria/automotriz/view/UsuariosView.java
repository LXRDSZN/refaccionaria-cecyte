package com.mycompany.refaccionaria.automotriz.view;

import com.mycompany.refaccionaria.automotriz.model.Rol;
import com.mycompany.refaccionaria.automotriz.model.Usuario;
import com.mycompany.refaccionaria.automotriz.service.AppSession;
import com.mycompany.refaccionaria.automotriz.service.AuthService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class UsuariosView extends JPanel {

    private static final Color BACKGROUND = new Color(0xF4, 0xF6, 0xFA);
    private static final Color TEXT = new Color(0x1F, 0x29, 0x37);
    private static final Color MUTED = new Color(0x64, 0x7, 0x8D);
    private static final Color PRIMARY = new Color(0x25, 0x63, 0xEB);

    private final AppSession session;
    private final AuthService authService;
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Nombre de usuario", "Rol"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JButton editButton = new JButton("Editar");
    private final JButton passwordButton = new JButton("Cambiar contraseña");

    public UsuariosView(AppSession session, AuthService authService) {
        this.session = session;
        this.authService = authService;
        buildUi();
        refresh();
    }

    public void refresh() {
        new SwingWorker<List<Usuario>, Void>() {
            @Override
            protected List<Usuario> doInBackground() throws SQLException {
                return authService.findUsers(session);
            }

            @Override
            protected void done() {
                try {
                    fillTable(get());
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError("La consulta fue interrumpida.");
                } catch (ExecutionException exception) {
                    showError("No fue posible consultar los usuarios.");
                }
            }
        }.execute();
    }

    private void buildUi() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(30, 34, 30, 34));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Gestión de Usuarios");
        title.setForeground(TEXT);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        header.add(title, BorderLayout.NORTH);
        JLabel subtitle = new JLabel("Administra usuarios y roles sin exponer contraseñas.");
        subtitle.setForeground(MUTED);
        subtitle.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        header.add(subtitle, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        table.getSelectionModel().addListSelectionListener(event -> {
            boolean selected = table.getSelectedRow() >= 0;
            editButton.setEnabled(selected);
            passwordButton.setEnabled(selected);
        });
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton newButton = new JButton("Nuevo usuario");
        newButton.setBackground(PRIMARY);
        newButton.setForeground(Color.WHITE);
        newButton.addActionListener(event -> createUser());
        editButton.setEnabled(false);
        editButton.addActionListener(event -> editUser());
        passwordButton.setEnabled(false);
        passwordButton.addActionListener(event -> changePassword());
        JButton refreshButton = new JButton("Actualizar");
        refreshButton.addActionListener(event -> refresh());
        actions.add(newButton);
        actions.add(editButton);
        actions.add(passwordButton);
        actions.add(refreshButton);
        add(actions, BorderLayout.SOUTH);
    }

    private void fillTable(List<Usuario> users) {
        tableModel.setRowCount(0);
        users.forEach(user -> tableModel.addRow(new Object[]{
            user.getId(), user.getNombreUsuario(), user.getRol()
        }));
    }

    private void createUser() {
        UserDialog dialog = new UserDialog(
                javax.swing.SwingUtilities.getWindowAncestor(this), null
        );
        dialog.setVisible(true);
        if (dialog.getUsername() == null) {
            return;
        }
        runAction(() -> authService.createUser(
                session, dialog.getUsername(), dialog.getPassword(), dialog.getRole()
        ), "Usuario creado correctamente.");
    }

    private void editUser() {
        Usuario selected = selectedUser();
        if (selected == null) {
            return;
        }
        UserDialog dialog = new UserDialog(
                javax.swing.SwingUtilities.getWindowAncestor(this), selected
        );
        dialog.setVisible(true);
        if (dialog.getUsername() == null) {
            return;
        }
        Usuario updated = new Usuario(
                selected.getId(), dialog.getUsername(), selected.getPasswordHash(), dialog.getRole()
        );
        runAction(() -> authService.updateUser(session, updated), "Usuario actualizado correctamente.");
    }

    private void changePassword() {
        Usuario selected = selectedUser();
        if (selected == null) {
            return;
        }
        JPasswordField password = new JPasswordField();
        JPasswordField confirmation = new JPasswordField();
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 6));
        panel.add(new JLabel("Nueva contraseña:"));
        panel.add(password);
        panel.add(new JLabel("Confirmar contraseña:"));
        panel.add(confirmation);
        int answer = JOptionPane.showConfirmDialog(
                this, panel, "Cambiar contraseña", JOptionPane.OK_CANCEL_OPTION
        );
        if (answer != JOptionPane.OK_OPTION) {
            return;
        }
        char[] newPassword = password.getPassword();
        char[] confirmationValue = confirmation.getPassword();
        if (!java.util.Arrays.equals(newPassword, confirmationValue)) {
            java.util.Arrays.fill(newPassword, '\0');
            java.util.Arrays.fill(confirmationValue, '\0');
            showError("Las contraseñas no coinciden.");
            return;
        }
        java.util.Arrays.fill(confirmationValue, '\0');
        runAction(() -> authService.changePassword(session, selected.getId(), newPassword),
                "Contraseña actualizada correctamente.");
    }

    private Usuario selectedUser() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError("Selecciona un usuario.");
            return null;
        }
        return new Usuario(
                (Integer) tableModel.getValueAt(row, 0),
                (String) tableModel.getValueAt(row, 1),
                "",
                Rol.valueOf(tableModel.getValueAt(row, 2).toString())
        );
    }

    private void runAction(SqlAction action, String successMessage) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                action.execute();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(UsuariosView.this, successMessage, "Operación exitosa",
                            JOptionPane.INFORMATION_MESSAGE);
                    refresh();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError("La operación fue interrumpida.");
                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause();
                    showError(cause instanceof IllegalArgumentException || cause instanceof IllegalStateException
                            ? cause.getMessage() : "No fue posible completar la operación.");
                }
            }
        }.execute();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @FunctionalInterface
    private interface SqlAction {
        void execute() throws Exception;
    }

    private static final class UserDialog extends JDialog {

        private final JTextField usernameField = new JTextField();
        private final JPasswordField passwordField = new JPasswordField();
        private final JPasswordField confirmationField = new JPasswordField();
        private final JRadioButton adminRadio = new JRadioButton("ADMIN");
        private final JRadioButton employeeRadio = new JRadioButton("EMPLEADO");
        private final boolean editing;
        private String username;
        private char[] password;
        private Rol role;

        private UserDialog(java.awt.Window owner, Usuario existing) {
            super(owner, existing == null ? "Nuevo usuario" : "Editar usuario", ModalityType.APPLICATION_MODAL);
            editing = existing != null;
            buildUi();
            if (existing != null) {
                usernameField.setText(existing.getNombreUsuario());
                (existing.getRol() == Rol.ADMIN ? adminRadio : employeeRadio).setSelected(true);
                passwordField.setVisible(false);
                confirmationField.setVisible(false);
            }
            pack();
            setLocationRelativeTo(owner);
        }

        private void buildUi() {
            JPanel form = new JPanel(new GridLayout(0, 1, 0, 6));
            form.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));
            form.add(new JLabel("Nombre de usuario:"));
            form.add(usernameField);
            if (!editing) {
                form.add(new JLabel("Contraseña:"));
                form.add(passwordField);
                form.add(new JLabel("Confirmar contraseña:"));
                form.add(confirmationField);
            }
            form.add(new JLabel("Rol:"));
            ButtonGroup group = new ButtonGroup();
            group.add(adminRadio);
            group.add(employeeRadio);
            employeeRadio.setSelected(true);
            JPanel roles = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            roles.add(adminRadio);
            roles.add(employeeRadio);
            form.add(roles);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton cancel = new JButton("Cancelar");
            cancel.addActionListener(event -> dispose());
            JButton save = new JButton("Guardar");
            save.setBackground(PRIMARY);
            save.setForeground(Color.WHITE);
            save.addActionListener(event -> save());
            actions.add(cancel);
            actions.add(save);
            add(form, BorderLayout.CENTER);
            add(actions, BorderLayout.SOUTH);
        }

        private void save() {
            username = usernameField.getText().trim();
            role = adminRadio.isSelected() ? Rol.ADMIN : Rol.EMPLEADO;
            if (username.isBlank()) {
                showWarning("El nombre de usuario es obligatorio.");
                username = null;
                return;
            }
            if (!editing) {
                password = passwordField.getPassword();
                char[] confirmation = confirmationField.getPassword();
                if (!java.util.Arrays.equals(password, confirmation)) {
                    java.util.Arrays.fill(password, '\0');
                    java.util.Arrays.fill(confirmation, '\0');
                    password = null;
                    showWarning("Las contraseñas no coinciden.");
                    return;
                }
                java.util.Arrays.fill(confirmation, '\0');
            }
            dispose();
        }

        private void showWarning(String message) {
            JOptionPane.showMessageDialog(this, message, "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        }

        private String getUsername() {
            return username;
        }

        private char[] getPassword() {
            return password;
        }

        private Rol getRole() {
            return role;
        }
    }
}
