package com.mycompany.refaccionaria.automotriz.view;

import com.mycompany.refaccionaria.automotriz.controller.LoginController;
import com.mycompany.refaccionaria.automotriz.controller.MainController;
import com.mycompany.refaccionaria.automotriz.model.Rol;
import com.mycompany.refaccionaria.automotriz.service.ProductoService;
import com.mycompany.refaccionaria.automotriz.service.VentaService;
import com.mycompany.refaccionaria.automotriz.service.DashboardService;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

public class MainView extends JFrame {

    private static final Color TOPBAR = new Color(0x15, 0x23, 0x38);
    private static final Color SIDEBAR = new Color(0x22, 0x34, 0x4C);
    private static final Color SELECTED = new Color(0x38, 0x55, 0x76);
    private static final Color BACKGROUND = new Color(0xF4, 0xF6, 0xFA);
    private static final Color TEXT = new Color(0x1F, 0x29, 0x37);
    private static final Color MUTED = new Color(0x64, 0x7, 0x8D);
    private static final Color BORDER = new Color(0xE2, 0xE8, 0xF0);

    private final MainController controller;
    private final ProductoService productoService = new ProductoService();
    private final VentaService ventaService = new VentaService();
    private final DashboardService dashboardService = new DashboardService();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(cardLayout);
    private final JPanel sidebar = new JPanel(new GridBagLayout());
    private ProductosView productosView;
    private InventarioView inventarioView;
    private VentasView ventasView;
    private HistorialVentasView historialView;
    private UsuariosView usuariosView;
    private DashboardView dashboardView;

    public MainView(MainController controller) {
        this.controller = controller;
        configureWindow();
        buildUi();
    }

    private void configureWindow() {
        setTitle("AUTO PARTS | Sistema de Refaccionaria Automotriz");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 640));
        setSize(1180, 760);
        setLocationRelativeTo(null);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);
        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildContent(), BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(16, 0));
        topBar.setBackground(TOPBAR);
        topBar.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        JLabel brand = new JLabel("AUTO PARTS");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        topBar.add(brand, BorderLayout.WEST);

        JPanel userPanel = new JPanel(new GridBagLayout());
        userPanel.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 0;
        constraints.gridx = 0;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(0, 0, 1, 12);

        JLabel userName = new JLabel(controller.getSession().getUsuario().getNombreUsuario());
        userName.setForeground(Color.WHITE);
        userName.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        userPanel.add(userName, constraints);

        JLabel role = new JLabel(controller.getSession().getUsuario().getRol().name());
        role.setForeground(new Color(0x93, 0xC5, 0xFD));
        role.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        constraints.gridy = 1;
        userPanel.add(role, constraints);

        JButton logoutButton = new JButton("Cerrar sesión");
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setOpaque(false);
        logoutButton.setContentAreaFilled(false);
        logoutButton.setBorder(BorderFactory.createLineBorder(new Color(0x64, 0x7, 0x8D)));
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(event -> logout());
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridheight = 2;
        constraints.insets = new Insets(0, 0, 0, 0);
        userPanel.add(logoutButton, constraints);

        topBar.add(userPanel, BorderLayout.EAST);
        return topBar;
    }

    private JPanel buildSidebar() {
        sidebar.setBackground(SIDEBAR);
        sidebar.setBorder(BorderFactory.createEmptyBorder(18, 12, 18, 12));
        sidebar.setPreferredSize(new Dimension(220, 0));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(3, 0, 3, 0);

        addNavigationButton("Dashboard", "dashboard", constraints, true);
        if (controller.getSession().isAdmin()) {
            addNavigationButton("Productos", "products", constraints, false);
            addNavigationButton("Inventario", "inventory", constraints, false);
            addNavigationButton("Ventas", "sales", constraints, false);
            addNavigationButton("Historial de ventas", "history", constraints, false);
            addNavigationButton("Usuarios", "users", constraints, false);
        } else if (controller.getSession().getUsuario().getRol() == Rol.EMPLEADO) {
            addNavigationButton("Consultar productos", "products", constraints, false);
            addNavigationButton("Inventario", "inventory", constraints, false);
            addNavigationButton("Ventas", "sales", constraints, false);
        }

        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.weighty = 1;
        sidebar.add(new JPanel(), constraints);
        return sidebar;
    }

    private void addNavigationButton(String label, String card, GridBagConstraints constraints, boolean selected) {
        JButton button = new JButton(label);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setForeground(Color.WHITE);
        button.setBackground(selected ? SELECTED : SIDEBAR);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(11, 14, 11, 14));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(event -> cardLayout.show(contentCards, card));
        constraints.gridy = GridBagConstraints.RELATIVE;
        sidebar.add(button, constraints);
    }

    private JPanel buildContent() {
        contentCards.setBackground(BACKGROUND);
        dashboardView = new DashboardView(controller.getSession(), dashboardService);
        contentCards.add(dashboardView, "dashboard");
        productosView = new ProductosView(controller.getSession(), productoService, this::refreshDataCards);
        inventarioView = new InventarioView(controller.getSession(), productoService, this::refreshDataCards);
        ventasView = new VentasView(
                controller.getSession(), productoService, ventaService, this::refreshDataCards
        );
        contentCards.add(productosView, "products");
        contentCards.add(inventarioView, "inventory");
        contentCards.add(ventasView, "sales");
        if (controller.getSession().isAdmin()) {
            historialView = new HistorialVentasView(controller.getSession(), ventaService);
            usuariosView = new UsuariosView(controller.getSession(), controller.getAuthService());
            contentCards.add(historialView, "history");
            contentCards.add(usuariosView, "users");
        } else {
            contentCards.add(buildModuleCard("Historial de Ventas", "Módulo no disponible para este rol."), "history");
            contentCards.add(buildModuleCard("Gestión de Usuarios", "Módulo no disponible para este rol."), "users");
        }
        return contentCards;
    }

    private void refreshDataCards() {
        productosView.refresh();
        inventarioView.refresh();
        ventasView.refresh();
        dashboardView.refresh();
        if (historialView != null) {
            historialView.refresh();
        }
    }

    private JPanel buildDashboardCard() {
        JPanel panel = baseContentPanel();
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Dashboard");
        title.setForeground(TEXT);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        header.add(title, BorderLayout.NORTH);

        JLabel description = new JLabel("Resumen general de la operación de AUTO PARTS");
        description.setForeground(MUTED);
        description.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        header.add(description, BorderLayout.CENTER);
        panel.add(header, BorderLayout.NORTH);

        JPanel welcome = new JPanel(new GridBagLayout());
        welcome.setBackground(Color.WHITE);
        welcome.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(28, 30, 28, 30)
        ));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(4, 0, 4, 0);

        JLabel welcomeTitle = new JLabel("Bienvenido, " + controller.getSession().getUsuario().getNombreUsuario());
        welcomeTitle.setForeground(TEXT);
        welcomeTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        constraints.gridy = 0;
        welcome.add(welcomeTitle, constraints);

        JTextArea message = new JTextArea(
                "Tu sesión está activa con permisos de "
                + controller.getSession().getUsuario().getRol().name()
                + ". Selecciona un módulo del menú lateral para comenzar."
        );
        message.setForeground(MUTED);
        message.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        message.setEditable(false);
        message.setOpaque(false);
        message.setRows(2);
        message.setColumns(58);
        constraints.gridy = 1;
        welcome.add(message, constraints);

        panel.add(welcome, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildModuleCard(String titleText, String descriptionText) {
        JPanel panel = baseContentPanel();
        JLabel title = new JLabel(titleText);
        title.setForeground(TEXT);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        panel.add(title, BorderLayout.NORTH);

        JLabel description = new JLabel(descriptionText);
        description.setForeground(MUTED);
        description.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        panel.add(description, BorderLayout.CENTER);
        return panel;
    }

    private JPanel baseContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 24));
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 34, 30, 34));
        return panel;
    }

    private void logout() {
        controller.logout();
        dispose();
        new LoginView(new LoginController(controller.getAuthService())).setVisible(true);
    }
}
