package com.mycompany.refaccionaria.automotriz.view;

import com.mycompany.refaccionaria.automotriz.model.DashboardData;
import com.mycompany.refaccionaria.automotriz.model.Producto;
import com.mycompany.refaccionaria.automotriz.service.AppSession;
import com.mycompany.refaccionaria.automotriz.service.DashboardService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class DashboardView extends JPanel {

    private static final Color BACKGROUND = new Color(0xF4, 0xF6, 0xFA);
    private static final Color TEXT = new Color(0x1F, 0x29, 0x37);
    private static final Color MUTED = new Color(0x64, 0x7, 0x8D);

    private final AppSession session;
    private final DashboardService service;
    private final JLabel totalProductsLabel = new JLabel("—");
    private final JLabel lowStockLabel = new JLabel("—");
    private final JLabel salesTodayLabel = new JLabel("—");
    private final JLabel revenueTodayLabel = new JLabel("—");
    private final JLabel availableLabel = new JLabel("—");
    private final JLabel outOfStockLabel = new JLabel("—");
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Código", "Producto", "Marca", "Stock actual", "Stock mínimo", "Estado"}, 0
    );

    public DashboardView(AppSession session, DashboardService service) {
        this.session = session;
        this.service = service;
        buildUi();
        refresh();
    }

    public void refresh() {
        new SwingWorker<DashboardData, Void>() {
            @Override
            protected DashboardData doInBackground() throws Exception {
                return service.load(session);
            }

            @Override
            protected void done() {
                try {
                    render(get());
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError("La consulta fue interrumpida.");
                } catch (ExecutionException exception) {
                    showError("No fue posible cargar el dashboard.");
                }
            }
        }.execute();
    }

    private void buildUi() {
        setLayout(new BorderLayout(0, 20));
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(30, 34, 30, 34));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Dashboard");
        title.setForeground(TEXT);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        header.add(title, BorderLayout.NORTH);
        JLabel subtitle = new JLabel("Información actualizada desde MariaDB.");
        subtitle.setForeground(MUTED);
        subtitle.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        header.add(subtitle, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setOpaque(false);
        JPanel metrics = new JPanel(new GridLayout(1, session.isAdmin() ? 4 : 2, 12, 0));
        metrics.setOpaque(false);
        if (session.isAdmin()) {
            metrics.add(metric("Productos registrados", totalProductsLabel));
            metrics.add(metric("Stock bajo", lowStockLabel));
            metrics.add(metric("Ventas del día", salesTodayLabel));
            metrics.add(metric("Ingresos del día", revenueTodayLabel));
        } else {
            metrics.add(metric("Productos disponibles", availableLabel));
            metrics.add(metric("Productos agotados", outOfStockLabel));
        }
        content.add(metrics, BorderLayout.NORTH);

        JPanel lowStockPanel = new JPanel(new BorderLayout(0, 8));
        lowStockPanel.setBackground(Color.WHITE);
        lowStockPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2, 0xE8, 0xF0)),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
        JLabel lowTitle = new JLabel("Productos que requieren reabastecimiento");
        lowTitle.setForeground(TEXT);
        lowTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        lowStockPanel.add(lowTitle, BorderLayout.NORTH);
        JTable table = new JTable(tableModel);
        table.setRowHeight(29);
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        lowStockPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        content.add(lowStockPanel, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    private JPanel metric(String title, JLabel value) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2, 0xE8, 0xF0)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(MUTED);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        value.setForeground(TEXT);
        value.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(value, BorderLayout.CENTER);
        return panel;
    }

    private void render(DashboardData data) {
        totalProductsLabel.setText(String.valueOf(data.totalProducts()));
        lowStockLabel.setText(String.valueOf(data.lowStockProducts()));
        salesTodayLabel.setText(String.valueOf(data.salesToday()));
        revenueTodayLabel.setText("$" + data.revenueToday().setScale(2).toPlainString());
        availableLabel.setText(String.valueOf(data.availableProducts()));
        outOfStockLabel.setText(String.valueOf(data.outOfStockProducts()));
        tableModel.setRowCount(0);
        data.lowStockItems().forEach(product -> tableModel.addRow(new Object[]{
            product.getCodigo(), product.getNombre(), product.getMarca(),
            product.getStock(), product.getStockMinimo(), status(product)
        }));
    }

    private String status(Producto product) {
        if (product.getStock() == 0) {
            return "Sin existencias";
        }
        if (product.getStock() <= product.getStockMinimo()) {
            return "Stock bajo";
        }
        return "Disponible";
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
