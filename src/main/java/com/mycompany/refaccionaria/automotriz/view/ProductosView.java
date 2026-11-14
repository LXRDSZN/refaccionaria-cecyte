package com.mycompany.refaccionaria.automotriz.view;

import com.mycompany.refaccionaria.automotriz.model.Producto;
import com.mycompany.refaccionaria.automotriz.service.AppSession;
import com.mycompany.refaccionaria.automotriz.service.ProductoService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class ProductosView extends JPanel {

    private static final Color BACKGROUND = new Color(0xF4, 0xF6, 0xFA);
    private static final Color TEXT = new Color(0x1F, 0x29, 0x37);
    private static final Color MUTED = new Color(0x64, 0x7, 0x8D);
    private static final Color PRIMARY = new Color(0x25, 0x63, 0xEB);
    private static final Color DANGER = new Color(0xDC, 0x26, 0x26);

    private final AppSession session;
    private final ProductoService service;
    private final Runnable onDataChanged;
    private final JTextField searchField = new JTextField();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Código", "Nombre", "Marca", "Categoría", "Precio", "Stock", "Stock mínimo"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JButton editButton = new JButton("Editar");
    private final JButton deleteButton = new JButton("Eliminar");

    public ProductosView(AppSession session, ProductoService service, Runnable onDataChanged) {
        this.session = session;
        this.service = service;
        this.onDataChanged = onDataChanged;
        buildUi();
        refresh();
    }

    public void refresh() {
        loadProducts(searchField.getText());
    }

    private void buildUi() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(30, 34, 30, 34));

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        JPanel titles = new JPanel(new GridBagLayout());
        titles.setOpaque(false);
        GridBagConstraints titleConstraints = new GridBagConstraints();
        titleConstraints.gridx = 0;
        titleConstraints.anchor = GridBagConstraints.WEST;

        JLabel title = new JLabel("Gestión de Productos");
        title.setForeground(TEXT);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        titles.add(title, titleConstraints);

        JLabel subtitle = new JLabel("Administra las refacciones automotrices registradas.");
        subtitle.setForeground(MUTED);
        titleConstraints.gridy = 1;
        titleConstraints.insets = new Insets(6, 0, 0, 0);
        titles.add(subtitle, titleConstraints);
        header.add(titles, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        if (session.isAdmin()) {
            JButton newButton = primaryButton("Nuevo producto");
            newButton.addActionListener(event -> openEditor(null));
            actions.add(newButton);
            editButton.addActionListener(event -> editSelected());
            editButton.setEnabled(false);
            actions.add(editButton);
            deleteButton.setForeground(Color.WHITE);
            deleteButton.setBackground(DANGER);
            deleteButton.setEnabled(false);
            deleteButton.addActionListener(event -> deleteSelected());
            actions.add(deleteButton);
        }
        JButton refreshButton = new JButton("Actualizar");
        refreshButton.addActionListener(event -> refresh());
        actions.add(refreshButton);
        header.add(actions, BorderLayout.EAST);
        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setOpaque(false);
        searchField.setToolTipText("Buscar por código, nombre, marca o categoría");
        searchField.addActionListener(event -> refresh());
        JButton searchButton = new JButton("Buscar");
        searchButton.addActionListener(event -> refresh());
        searchPanel.add(new JLabel("Buscar:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        JPanel topPanel = new JPanel(new BorderLayout(0, 16));
        topPanel.setOpaque(false);
        topPanel.add(header, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        table.getSelectionModel().addListSelectionListener(event -> {
            boolean selected = table.getSelectedRow() >= 0;
            editButton.setEnabled(selected);
            deleteButton.setEnabled(selected && session.isAdmin());
        });
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY);
        button.setFocusPainted(false);
        return button;
    }

    private void loadProducts(String term) {
        new SwingWorker<List<Producto>, Void>() {
            @Override
            protected List<Producto> doInBackground() throws SQLException {
                return service.search(session, term);
            }

            @Override
            protected void done() {
                try {
                    fillTable(get());
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError("La consulta fue interrumpida.");
                } catch (ExecutionException exception) {
                    showError("No fue posible consultar los productos.");
                }
            }
        }.execute();
    }

    private void fillTable(List<Producto> products) {
        tableModel.setRowCount(0);
        products.forEach(product -> tableModel.addRow(new Object[]{
            product.getId(), product.getCodigo(), product.getNombre(), product.getMarca(),
            product.getCategoria(), product.getPrecio(), product.getStock(), product.getStockMinimo()
        }));
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        openEditor(readSelected(row));
    }

    private Producto readSelected(int row) {
        return new Producto(
                (Integer) tableModel.getValueAt(row, 0),
                (String) tableModel.getValueAt(row, 1),
                (String) tableModel.getValueAt(row, 2),
                (String) tableModel.getValueAt(row, 3),
                (String) tableModel.getValueAt(row, 4),
                new BigDecimal(tableModel.getValueAt(row, 5).toString()),
                (Integer) tableModel.getValueAt(row, 6),
                (Integer) tableModel.getValueAt(row, 7)
        );
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0 || !session.isAdmin()) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(
                this,
                "¿Eliminar el producto seleccionado?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION
        );
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        int productId = (Integer) tableModel.getValueAt(row, 0);
        runAction(() -> service.delete(session, productId), "Producto eliminado correctamente.");
    }

    private void openEditor(Producto existing) {
        if (!session.isAdmin()) {
            return;
        }
        ProductDialog dialog = new ProductDialog(
                SwingUtilities.getWindowAncestor(this),
                existing
        );
        dialog.setVisible(true);
        if (dialog.getProduct() == null) {
            return;
        }
        Producto product = dialog.getProduct();
        if (existing != null) {
            product.setId(existing.getId());
        }
        runAction(() -> {
            if (existing == null) {
                service.create(session, product);
            } else {
                service.update(session, product);
            }
        }, existing == null ? "Producto creado correctamente." : "Producto actualizado correctamente.");
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
                    JOptionPane.showMessageDialog(ProductosView.this, successMessage, "Operación exitosa",
                            JOptionPane.INFORMATION_MESSAGE);
                    refresh();
                    onDataChanged.run();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError("La operación fue interrumpida.");
                } catch (ExecutionException exception) {
                    showError(userMessage(exception.getCause()));
                }
            }
        }.execute();
    }

    private String userMessage(Throwable cause) {
        if (cause instanceof IllegalArgumentException || cause instanceof IllegalStateException) {
            return cause.getMessage();
        }
        return "No fue posible completar la operación.";
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @FunctionalInterface
    private interface SqlAction {
        void execute() throws Exception;
    }

    private static final class ProductDialog extends JDialog {

        private final JTextField codeField = new JTextField();
        private final JTextField nameField = new JTextField();
        private final JTextField brandField = new JTextField();
        private final JTextField categoryField = new JTextField();
        private final JTextField priceField = new JTextField();
        private final JSpinner stockSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        private final JSpinner minimumSpinner = new JSpinner(new SpinnerNumberModel(5, 0, Integer.MAX_VALUE, 1));
        private Producto product;

        private ProductDialog(java.awt.Window owner, Producto existing) {
            super(owner, existing == null ? "Nuevo producto" : "Editar producto", ModalityType.APPLICATION_MODAL);
            buildUi();
            if (existing != null) {
                codeField.setText(existing.getCodigo());
                nameField.setText(existing.getNombre());
                brandField.setText(existing.getMarca());
                categoryField.setText(existing.getCategoria());
                priceField.setText(existing.getPrecio().toPlainString());
                stockSpinner.setValue(existing.getStock());
                minimumSpinner.setValue(existing.getStockMinimo());
            }
            pack();
            setLocationRelativeTo(owner);
        }

        private void buildUi() {
            JPanel form = new JPanel(new GridBagLayout());
            form.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));
            addField(form, 0, "Código", codeField);
            addField(form, 1, "Nombre", nameField);
            addField(form, 2, "Marca", brandField);
            addField(form, 3, "Categoría", categoryField);
            addField(form, 4, "Precio", priceField);
            addField(form, 5, "Stock inicial", stockSpinner);
            addField(form, 6, "Stock mínimo", minimumSpinner);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton cancel = new JButton("Cancelar");
            cancel.addActionListener(event -> dispose());
            JButton save = new JButton("Guardar");
            save.setBackground(PRIMARY);
            save.setForeground(Color.WHITE);
            save.addActionListener(event -> save());
            buttons.add(cancel);
            buttons.add(save);

            add(form, BorderLayout.CENTER);
            add(buttons, BorderLayout.SOUTH);
        }

        private void addField(JPanel panel, int row, String label, java.awt.Component component) {
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = row;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = new Insets(5, 0, 5, 12);
            panel.add(new JLabel(label), constraints);
            constraints.gridx = 1;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.insets = new Insets(5, 0, 5, 0);
            component.setPreferredSize(new java.awt.Dimension(250, 32));
            panel.add(component, constraints);
        }

        private void save() {
            try {
                BigDecimal price = new BigDecimal(priceField.getText().trim());
                product = new Producto(
                        codeField.getText().trim(), nameField.getText().trim(), brandField.getText().trim(),
                        categoryField.getText().trim(), price, (Integer) stockSpinner.getValue(),
                        (Integer) minimumSpinner.getValue()
                );
                dispose();
            } catch (NumberFormatException exception) {
                JOptionPane.showMessageDialog(this, "El precio debe ser numérico.", "Datos inválidos",
                        JOptionPane.WARNING_MESSAGE);
            }
        }

        private Producto getProduct() {
            return product;
        }
    }
}
