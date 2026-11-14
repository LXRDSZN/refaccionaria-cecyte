package com.mycompany.refaccionaria.automotriz.view;

import com.mycompany.refaccionaria.automotriz.model.Producto;
import com.mycompany.refaccionaria.automotriz.service.AppSession;
import com.mycompany.refaccionaria.automotriz.service.ProductoService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class InventarioView extends JPanel {

    private static final Color BACKGROUND = new Color(0xF4, 0xF6, 0xFA);
    private static final Color TEXT = new Color(0x1F, 0x29, 0x37);
    private static final Color MUTED = new Color(0x64, 0x7, 0x8D);
    private static final Color PRIMARY = new Color(0x25, 0x63, 0xEB);

    private final AppSession session;
    private final ProductoService service;
    private final Runnable onDataChanged;
    private final JLabel totalLabel = new JLabel("0");
    private final JLabel availableLabel = new JLabel("0");
    private final JLabel lowStockLabel = new JLabel("0");
    private final JLabel emptyLabel = new JLabel("0");
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Código", "Producto", "Categoría", "Stock actual", "Stock mínimo", "Estado"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JButton adjustButton = new JButton("Ajustar existencias");

    public InventarioView(AppSession session, ProductoService service, Runnable onDataChanged) {
        this.session = session;
        this.service = service;
        this.onDataChanged = onDataChanged;
        buildUi();
        refresh();
    }

    public void refresh() {
        new SwingWorker<List<Producto>, Void>() {
            @Override
            protected List<Producto> doInBackground() throws SQLException {
                return service.findAll(session);
            }

            @Override
            protected void done() {
                try {
                    fillTable(get());
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError("La consulta fue interrumpida.");
                } catch (ExecutionException exception) {
                    showError("No fue posible consultar el inventario.");
                }
            }
        }.execute();
    }

    private void buildUi() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(30, 34, 30, 34));

        JPanel top = new JPanel(new BorderLayout(0, 18));
        top.setOpaque(false);
        JLabel title = new JLabel("Control de Inventario");
        title.setForeground(TEXT);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        top.add(title, BorderLayout.NORTH);

        JPanel metrics = new JPanel(new GridLayout(1, 4, 12, 0));
        metrics.setOpaque(false);
        metrics.add(metricPanel("Total de productos", totalLabel));
        metrics.add(metricPanel("Disponibles", availableLabel));
        metrics.add(metricPanel("Stock bajo", lowStockLabel));
        metrics.add(metricPanel("Agotados", emptyLabel));
        top.add(metrics, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        JPanel tablePanel = new JPanel(new BorderLayout(0, 10));
        tablePanel.setOpaque(false);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        if (session.isAdmin()) {
            adjustButton.setBackground(PRIMARY);
            adjustButton.setForeground(Color.WHITE);
            adjustButton.addActionListener(event -> adjustSelected());
            actions.add(adjustButton);
        }
        JButton refreshButton = new JButton("Actualizar");
        refreshButton.addActionListener(event -> refresh());
        actions.add(refreshButton);
        tablePanel.add(actions, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);
    }

    private JPanel metricPanel(String title, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2, 0xE8, 0xF0)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(MUTED);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        valueLabel.setForeground(TEXT);
        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        return panel;
    }

    private void fillTable(List<Producto> products) {
        tableModel.setRowCount(0);
        int lowStock = 0;
        int empty = 0;
        int available = 0;
        for (Producto product : products) {
            String status;
            if (product.getStock() == 0) {
                status = "Sin existencias";
                empty++;
            } else if (product.getStock() <= product.getStockMinimo()) {
                status = "Stock bajo";
                lowStock++;
            } else {
                status = "Disponible";
                available++;
            }
            tableModel.addRow(new Object[]{
                product.getCodigo(), product.getNombre(), product.getCategoria(),
                product.getStock(), product.getStockMinimo(), status
            });
        }
        totalLabel.setText(String.valueOf(products.size()));
        availableLabel.setText(String.valueOf(available));
        lowStockLabel.setText(String.valueOf(lowStock));
        emptyLabel.setText(String.valueOf(empty));
    }

    private void adjustSelected() {
        if (!session.isAdmin() || table.getSelectedRow() < 0) {
            return;
        }
        int row = table.getSelectedRow();
        String code = tableModel.getValueAt(row, 0).toString();
        new SwingWorker<Producto, Void>() {
            @Override
            protected Producto doInBackground() throws SQLException {
                return service.findAll(session).stream()
                        .filter(product -> product.getCodigo().equals(code))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("El producto no existe."));
            }

            @Override
            protected void done() {
                try {
                    showAdjustDialog(get());
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError("La consulta fue interrumpida.");
                } catch (ExecutionException exception) {
                    showError("No fue posible cargar el producto seleccionado.");
                }
            }
        }.execute();
    }

    private void showAdjustDialog(Producto product) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(
                product.getStock(), 0, Integer.MAX_VALUE, 1
        ));
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 8));
        panel.add(new JLabel("Producto: " + product.getNombre()));
        panel.add(new JLabel("Existencia actual: " + product.getStock()));
        panel.add(new JLabel("Nueva existencia:"));
        panel.add(spinner);
        int answer = JOptionPane.showConfirmDialog(
                this, panel, "Ajustar existencias", JOptionPane.OK_CANCEL_OPTION
        );
        if (answer != JOptionPane.OK_OPTION) {
            return;
        }

        int newStock = (Integer) spinner.getValue();
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws SQLException {
                service.adjustStock(session, product.getId(), newStock);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(InventarioView.this,
                            "Existencias actualizadas correctamente.", "Operación exitosa",
                            JOptionPane.INFORMATION_MESSAGE);
                    refresh();
                    onDataChanged.run();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError("La operación fue interrumpida.");
                } catch (ExecutionException exception) {
                    showError("No fue posible actualizar las existencias.");
                }
            }
        }.execute();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
