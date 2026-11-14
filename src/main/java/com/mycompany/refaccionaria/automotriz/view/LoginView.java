package com.mycompany.refaccionaria.automotriz.view;

import com.formdev.flatlaf.FlatLightLaf;
import com.mycompany.refaccionaria.automotriz.controller.LoginController;
import com.mycompany.refaccionaria.automotriz.controller.MainController;
import com.mycompany.refaccionaria.automotriz.service.AppSession;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

public class LoginView extends JFrame {

    private static final Color NAVY = new Color(0x15, 0x23, 0x38);
    private static final Color PRIMARY = new Color(0x25, 0x63, 0xEB);
    private static final Color TEXT = new Color(0x1F, 0x29, 0x37);
    private static final Color MUTED = new Color(0x64, 0x7, 0x8D);
    private static final Color ERROR = new Color(0xDC, 0x26, 0x26);

    private final LoginController controller;
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JLabel feedbackLabel = new JLabel(" ");
    private final JButton loginButton = new JButton("Iniciar sesión");

    public LoginView(LoginController controller) {
        this.controller = controller;
        configureWindow();
        buildUi();
    }

    public static void showWindow(LoginController controller) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> new LoginView(controller).setVisible(true));
    }

    private void configureWindow() {
        setTitle("AUTO PARTS | Acceso");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setIconImage(ResourceImages.loadImage("/images/logo/auto-parts-logo.png"));
        setMinimumSize(new Dimension(760, 500));
        setSize(900, 600);
        setLocationRelativeTo(null);
    }

    private void buildUi() {
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.setBackground(NAVY);
        root.add(buildBrandPanel());
        root.add(buildLoginPanel());
        setContentPane(root);
        getRootPane().setDefaultButton(loginButton);
    }

    private JPanel buildBrandPanel() {
        ImageBackgroundPanel panel = new ImageBackgroundPanel(
                "/images/login/autoparts-background.png",
                new Color(0x15, 0x23, 0x38, 190)
        );
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(48, 48, 48, 48));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(8, 0, 8, 0);

        JLabel icon = new JLabel(ResourceImages.scaledIcon("/images/logo/auto-parts-logo.png", 210, 130));
        constraints.gridy = 0;
        panel.add(icon, constraints);

        JLabel title = new JLabel("AUTO PARTS");
        title.setForeground(Color.WHITE);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 34));
        constraints.gridy = 1;
        panel.add(title, constraints);

        JLabel subtitle = new JLabel("<html>Sistema de gestión<br>para tu refaccionaria</html>");
        subtitle.setForeground(new Color(0xCB, 0xD5, 0xE1));
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        constraints.gridy = 2;
        panel.add(subtitle, constraints);

        JLabel detail = new JLabel("Inventario · Ventas · Control");
        detail.setForeground(new Color(0x94, 0xA3, 0xB8));
        detail.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        constraints.gridy = 3;
        constraints.insets = new Insets(32, 0, 8, 0);
        panel.add(detail, constraints);

        return panel;
    }

    private JPanel buildLoginPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(new Color(0xF4, 0xF6, 0xFA));
        outer.setBorder(BorderFactory.createEmptyBorder(36, 36, 36, 36));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2, 0xE8, 0xF0)),
                BorderFactory.createEmptyBorder(32, 36, 32, 36)
        ));
        card.setPreferredSize(new Dimension(360, 390));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(6, 0, 6, 0);

        JLabel heading = new JLabel("Iniciar sesión");
        heading.setForeground(TEXT);
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        constraints.gridy = 0;
        constraints.insets = new Insets(0, 0, 4, 0);
        card.add(heading, constraints);

        JLabel description = new JLabel("Ingresa tus credenciales para continuar");
        description.setForeground(MUTED);
        description.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        constraints.gridy = 1;
        constraints.insets = new Insets(0, 0, 24, 0);
        card.add(description, constraints);

        constraints.gridy = 2;
        constraints.insets = new Insets(5, 0, 5, 0);
        card.add(labelFor("Usuario"), constraints);
        constraints.gridy = 3;
        usernameField.setPreferredSize(new Dimension(280, 38));
        card.add(usernameField, constraints);

        constraints.gridy = 4;
        card.add(labelFor("Contraseña"), constraints);
        constraints.gridy = 5;
        passwordField.setPreferredSize(new Dimension(280, 38));
        card.add(passwordField, constraints);

        feedbackLabel.setForeground(ERROR);
        feedbackLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        constraints.gridy = 6;
        constraints.insets = new Insets(12, 0, 4, 0);
        card.add(feedbackLabel, constraints);

        loginButton.setBackground(PRIMARY);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.setPreferredSize(new Dimension(280, 40));
        loginButton.addActionListener(event -> authenticate());
        constraints.gridy = 7;
        constraints.insets = new Insets(12, 0, 0, 0);
        card.add(loginButton, constraints);

        outer.add(card);
        return outer;
    }

    private JLabel labelFor(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        return label;
    }

    private void authenticate() {
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();
        if (username.isBlank() || password.length == 0) {
            feedbackLabel.setText("Completa usuario y contraseña.");
            return;
        }

        setLoading(true);
        new SwingWorker<Optional<AppSession>, Void>() {
            @Override
            protected Optional<AppSession> doInBackground() throws SQLException {
                return controller.authenticate(username, password);
            }

            @Override
            protected void done() {
                try {
                    Optional<AppSession> session = get();
                    if (session.isEmpty()) {
                        feedbackLabel.setText("No se pudieron validar las credenciales.");
                        passwordField.setText("");
                        return;
                    }

                    MainView mainView = new MainView(
                            new MainController(session.get(), controller.getAuthService())
                    );
                    mainView.setVisible(true);
                    dispose();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    feedbackLabel.setText("La operación fue interrumpida.");
                } catch (ExecutionException exception) {
                    feedbackLabel.setText("No fue posible conectar con la base de datos.");
                } finally {
                    setLoading(false);
                }
            }
        }.execute();
    }

    private void setLoading(boolean loading) {
        loginButton.setEnabled(!loading);
        loginButton.setText(loading ? "Validando..." : "Iniciar sesión");
        usernameField.setEnabled(!loading);
        passwordField.setEnabled(!loading);
    }
}
