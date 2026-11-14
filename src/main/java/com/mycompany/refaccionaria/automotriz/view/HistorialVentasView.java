package com.mycompany.refaccionaria.automotriz.view;

import com.mycompany.refaccionaria.automotriz.model.DetalleVentaResumen;
import com.mycompany.refaccionaria.automotriz.model.VentaResumen;
import com.mycompany.refaccionaria.automotriz.service.AppSession;
import com.mycompany.refaccionaria.automotriz.service.VentaService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class HistorialVentasView extends JPanel {

    private static final Color BACKGROUND = new Color(0xF4, 0xF6, 0xFA);
    private static final Color TEXT = new Color(0x1F, 0x29, 0x37);
    private static final Color MUTED = new Color(0x64, 0x7, 0x8D);
    private static final Color PRIMARY = new Color(0x25, 0x63, 0xEB);

    private final AppSession session;
    private final VentaService service;
    private final JTextField saleIdField = new JTextField();
    private final JTextField fromField = new JTextField();
    private final JTextField toField = new JTextField();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Número de venta", "Fecha", "Usuario vendedor", "Total"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    public HistorialVentasView(AppSession session, VentaService service) {
        this.session = session;
        this.service = service;
        buildUi();
        refresh();
    }

    public void refresh() {
        loadSales();
    }

    private void buildUi() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(30, 34, 30, 34));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Historial de Ventas");
        title.setForeground(TEXT);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        header.add(title, BorderLayout.NORTH);
        JLabel subtitle = new JLabel("Consulta operaciones históricas sin modificar sus registros.");
        subtitle.setForeground(MUTED);
        subtitle.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        header.add(subtitle, BorderLayout.CENTER);
        JPanel filters = new JPanel(new GridLayout(1, 7, 8, 0));
        filters.setOpaque(false);
        filters.add(new JLabel("Venta #"));
        filters.add(saleIdField);
        filters.add(new JLabel("Desde"));
        filters.add(fromField);
        filters.add(new JLabel("Hasta"));
        filters.add(toField);
        JButton searchButton = new JButton("Buscar");
        searchButton.setBackground(PRIMARY);
        searchButton.setForeground(Color.WHITE);
        searchButton.addActionListener(event -> loadSales());
        filters.add(searchButton);
        JPanel top = new JPanel(new BorderLayout(0, 16));
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(filters, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton detailButton = new JButton("Ver detalle");
        detailButton.addActionListener(event -> showSelectedDetails());
        JButton refreshButton = new JButton("Actualizar");
        refreshButton.addActionListener(event -> refresh());
        actions.add(detailButton);
        actions.add(refreshButton);
        center.add(actions, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
    }

    private void loadSales() {
        Integer saleId;
        LocalDate from;
        LocalDate to;
        try {
            saleId = parseSaleId();
            from = parseDate(fromField.getText());
            to = parseDate(toField.getText());
            if (from != null && to != null && from.isAfter(to)) {
                throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la final.");
            }
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
            return;
        }

        Integer selectedSaleId = saleId;
        LocalDate selectedFrom = from;
        LocalDate selectedTo = to;
        new SwingWorker<List<VentaResumen>, Void>() {
            @Override
            protected List<VentaResumen> doInBackground() throws Exception {
                return service.findSummaries(session, selectedSaleId, selectedFrom, selectedTo);
            }

            @Override
            protected void done() {
                try {
                    fillTable(get());
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError("La consulta fue interrumpida.");
                } catch (ExecutionException exception) {
                    showError("No fue posible consultar el historial.");
                }
            }
        }.execute();
    }

    private Integer parseSaleId() {
        String value = saleIdField.getText().trim();
        if (value.isBlank()) {
            return null;
        }
        try {
            int id = Integer.parseInt(value);
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("El número de venta no es válido.");
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Las fechas deben usar el formato AAAA-MM-DD.");
        }
    }

    private void fillTable(List<VentaResumen> sales) {
        tableModel.setRowCount(0);
        sales.forEach(sale -> tableModel.addRow(new Object[]{
            sale.id(), sale.fecha().toString().replace('T', ' '), sale.nombreUsuario(), sale.total()
        }));
    }

    private void showSelectedDetails() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError("Selecciona una venta.");
            return;
        }
        int saleId = (Integer) tableModel.getValueAt(row, 0);
        new SwingWorker<List<DetalleVentaResumen>, Void>() {
            @Override
            protected List<DetalleVentaResumen> doInBackground() throws Exception {
                return service.findDetails(session, saleId);
            }

            @Override
            protected void done() {
                try {
                    List<DetalleVentaResumen> details = get();
                    if (details.isEmpty()) {
                        showError("La venta no tiene detalles disponibles.");
                        return;
                    }
                    new SaleDetailDialog(
                            javax.swing.SwingUtilities.getWindowAncestor(HistorialVentasView.this), details
                    ).setVisible(true);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError("La consulta fue interrumpida.");
                } catch (ExecutionException exception) {
                    showError("No fue posible consultar el detalle.");
                }
            }
        }.execute();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static final class SaleDetailDialog extends JDialog {

        private SaleDetailDialog(java.awt.Window owner, List<DetalleVentaResumen> details) {
            super(owner, "Detalle de venta #" + details.get(0).ventaId(), ModalityType.APPLICATION_MODAL);
            setLayout(new BorderLayout(0, 12));
            setSize(700, 420);
            setLocationRelativeTo(owner);

            DetalleVentaResumen first = details.get(0);
            JPanel summary = new JPanel(new GridLayout(2, 3, 12, 4));
            summary.setBorder(BorderFactory.createEmptyBorder(14, 14, 0, 14));
            summary.add(new JLabel("Venta #" + first.ventaId()));
            summary.add(new JLabel("Fecha: " + first.fecha().toString().replace('T', ' ')));
            summary.add(new JLabel("Vendedor: " + first.nombreUsuario()));
            summary.add(new JLabel("Total: $" + first.totalVenta().toPlainString()));
            add(summary, BorderLayout.NORTH);

            DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"Código", "Producto", "Cantidad", "Precio unitario", "Subtotal"}, 0
            );
            details.forEach(detail -> model.addRow(new Object[]{
                detail.codigoProducto(), detail.nombreProducto(), detail.cantidad(),
                detail.precioUnitario(), detail.subtotal()
            }));
            add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
            JButton close = new JButton("Cerrar");
            close.addActionListener(event -> dispose());
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            footer.add(close);
            add(footer, BorderLayout.SOUTH);
        }
    }
}
