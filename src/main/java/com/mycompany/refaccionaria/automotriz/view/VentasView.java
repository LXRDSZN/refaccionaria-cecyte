package com.mycompany.refaccionaria.automotriz.view;

import com.mycompany.refaccionaria.automotriz.model.DetalleVenta;
import com.mycompany.refaccionaria.automotriz.model.Producto;
import com.mycompany.refaccionaria.automotriz.model.Venta;
import com.mycompany.refaccionaria.automotriz.service.AppSession;
import com.mycompany.refaccionaria.automotriz.service.ProductoService;
import com.mycompany.refaccionaria.automotriz.service.VentaService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class VentasView extends JPanel {

    private static final Color BACKGROUND = new Color(0xF4, 0xF6, 0xFA);
    private static final Color TEXT = new Color(0x1F, 0x29, 0x37);
    private static final Color MUTED = new Color(0x64, 0x7, 0x8D);
    private static final Color PRIMARY = new Color(0x25, 0x63, 0xEB);
    private static final Color DANGER = new Color(0xDC, 0x26, 0x26);

    private final AppSession session;
    private final ProductoService productoService;
    private final VentaService ventaService;
    private final Runnable onDataChanged;
    private final JTextField searchField = new JTextField();
    private final JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));
    private final JLabel totalLabel = new JLabel("$0.00");
    private final DefaultTableModel productsModel = new DefaultTableModel(
            new Object[]{"Código", "Producto", "Precio", "Stock"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultTableModel cartModel = new DefaultTableModel(
            new Object[]{"Código", "Producto", "Cantidad", "Precio unitario", "Subtotal"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable productsTable = new JTable(productsModel);
    private final JTable cartTable = new JTable(cartModel);
    private final List<CartLine> cart = new ArrayList<>();
    private List<Producto> availableProducts = new ArrayList<>();

    public VentasView(AppSession session, ProductoService productoService,
            VentaService ventaService, Runnable onDataChanged) {
        this.session = session;
        this.productoService = productoService;
        this.ventaService = ventaService;
        this.onDataChanged = onDataChanged;
        buildUi();
        refreshProducts();
    }

    public void refresh() {
        refreshProducts();
    }

    private void buildUi() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(30, 34, 30, 34));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Registrar venta");
        title.setForeground(TEXT);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        header.add(title, BorderLayout.NORTH);
        JLabel subtitle = new JLabel("Selecciona productos, valida existencias y confirma la operación.");
        subtitle.setForeground(MUTED);
        subtitle.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        header.add(subtitle, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        JPanel productsPanel = buildProductsPanel();
        JPanel cartPanel = buildCartPanel();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, productsPanel, cartPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);
        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel buildProductsPanel() {
        JPanel panel = sectionPanel("Productos disponibles");
        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setOpaque(false);
        searchField.setToolTipText("Buscar por código, nombre, marca o categoría");
        searchField.addActionListener(event -> refreshProducts());
        JButton searchButton = new JButton("Buscar");
        searchButton.addActionListener(event -> refreshProducts());
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        panel.add(searchPanel, BorderLayout.NORTH);

        productsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productsTable.setRowHeight(29);
        productsTable.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        productsTable.getSelectionModel().addListSelectionListener(event -> updateQuantityLimit());
        panel.add(new JScrollPane(productsTable), BorderLayout.CENTER);

        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addPanel.setOpaque(false);
        addPanel.add(new JLabel("Cantidad:"));
        quantitySpinner.setPreferredSize(new Dimension(75, 30));
        addPanel.add(quantitySpinner);
        JButton addButton = new JButton("Agregar");
        addButton.setBackground(PRIMARY);
        addButton.setForeground(Color.WHITE);
        addButton.addActionListener(event -> addSelectedProduct());
        addPanel.add(addButton);
        panel.add(addPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildCartPanel() {
        JPanel panel = sectionPanel("Carrito de venta");
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cartTable.setRowHeight(29);
        cartTable.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        panel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(0, 10));
        bottom.setOpaque(false);
        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setOpaque(false);
        JLabel totalTitle = new JLabel("Total general");
        totalTitle.setForeground(TEXT);
        totalTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        totalLabel.setForeground(TEXT);
        totalLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        totalPanel.add(totalTitle, BorderLayout.WEST);
        totalPanel.add(totalLabel, BorderLayout.EAST);
        bottom.add(totalPanel, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton removeButton = new JButton("Quitar");
        removeButton.addActionListener(event -> removeSelectedLine());
        JButton clearButton = new JButton("Vaciar carrito");
        clearButton.setForeground(DANGER);
        clearButton.addActionListener(event -> clearCart());
        JButton confirmButton = new JButton("Confirmar venta");
        confirmButton.setBackground(PRIMARY);
        confirmButton.setForeground(Color.WHITE);
        confirmButton.addActionListener(event -> confirmSale());
        actions.add(removeButton);
        actions.add(clearButton);
        actions.add(confirmButton);
        bottom.add(actions, BorderLayout.SOUTH);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel sectionPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2, 0xE8, 0xF0)),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(14, 14, 14, 14),
                        BorderFactory.createTitledBorder(title)
                )
        ));
        return panel;
    }

    private void refreshProducts() {
        new SwingWorker<List<Producto>, Void>() {
            @Override
            protected List<Producto> doInBackground() throws SQLException {
                return productoService.search(session, searchField.getText()).stream()
                        .filter(product -> product.getStock() > 0)
                        .collect(Collectors.toList());
            }

            @Override
            protected void done() {
                try {
                    fillProducts(get());
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError("La consulta fue interrumpida.");
                } catch (ExecutionException exception) {
                    showError("No fue posible consultar los productos disponibles.");
                }
            }
        }.execute();
    }

    private void fillProducts(List<Producto> products) {
        availableProducts = products;
        productsModel.setRowCount(0);
        products.forEach(product -> productsModel.addRow(new Object[]{
            product.getCodigo(), product.getNombre(), product.getPrecio(), product.getStock()
        }));
        updateQuantityLimit();
    }

    private void updateQuantityLimit() {
        int row = productsTable.getSelectedRow();
        int max = row < 0 ? 1 : (Integer) productsModel.getValueAt(row, 3);
        quantitySpinner.setModel(new SpinnerNumberModel(1, 1, Math.max(max, 1), 1));
    }

    private void addSelectedProduct() {
        int row = productsTable.getSelectedRow();
        if (row < 0) {
            showError("Selecciona un producto.");
            return;
        }
        Producto selected = productFromRow(row);
        int quantity = (Integer) quantitySpinner.getValue();
        CartLine existing = cart.stream()
                .filter(line -> line.product.getId().equals(selected.getId()))
                .findFirst()
                .orElse(null);
        int newQuantity = quantity + (existing == null ? 0 : existing.quantity);
        if (newQuantity > selected.getStock()) {
            showError("La cantidad supera las existencias disponibles.");
            return;
        }
        if (existing == null) {
            cart.add(new CartLine(selected, quantity));
        } else {
            existing.quantity = newQuantity;
        }
        fillCart();
    }

    private Producto productFromRow(int row) {
        return availableProducts.get(row);
    }

    private void fillCart() {
        cartModel.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        for (CartLine line : cart) {
            BigDecimal subtotal = line.product.getPrecio().multiply(BigDecimal.valueOf(line.quantity));
            total = total.add(subtotal);
            cartModel.addRow(new Object[]{
                line.product.getCodigo(), line.product.getNombre(), line.quantity,
                line.product.getPrecio(), subtotal
            });
        }
        totalLabel.setText("$" + total.setScale(2).toPlainString());
    }

    private void removeSelectedLine() {
        int row = cartTable.getSelectedRow();
        if (row >= 0) {
            cart.remove(row);
            fillCart();
        }
    }

    private void clearCart() {
        if (cart.isEmpty()) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(
                this, "¿Vaciar todos los productos del carrito?", "Confirmar", JOptionPane.YES_NO_OPTION
        );
        if (answer == JOptionPane.YES_OPTION) {
            cart.clear();
            fillCart();
        }
    }

    private void confirmSale() {
        if (cart.isEmpty()) {
            showError("Agrega al menos un producto antes de confirmar.");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(
                this, "¿Confirmar la venta y descontar el inventario?",
                "Confirmar venta", JOptionPane.YES_NO_OPTION
        );
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        List<DetalleVenta> details = cart.stream()
                .map(line -> new DetalleVenta(
                        line.product.getId(),
                        line.quantity,
                        line.product.getPrecio(),
                        line.product.getPrecio().multiply(BigDecimal.valueOf(line.quantity))
                ))
                .collect(Collectors.toList());
        new SwingWorker<Venta, Void>() {
            @Override
            protected Venta doInBackground() throws SQLException {
                return ventaService.registerSale(session, details);
            }

            @Override
            protected void done() {
                try {
                    Venta sale = get();
                    JOptionPane.showMessageDialog(
                            VentasView.this,
                            "Venta #" + sale.getId() + " registrada por $" + sale.getTotal().setScale(2).toPlainString(),
                            "Venta registrada",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    cart.clear();
                    fillCart();
                    refreshProducts();
                    onDataChanged.run();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError("La operación fue interrumpida.");
                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause();
                    if (cause instanceof IllegalArgumentException || cause instanceof IllegalStateException) {
                        showError(cause.getMessage());
                    } else {
                        showError("No fue posible registrar la venta. No se guardaron cambios parciales.");
                    }
                }
            }
        }.execute();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static final class CartLine {

        private final Producto product;
        private int quantity;

        private CartLine(Producto product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
    }
}
