package com.mycompany.refaccionaria.automotriz.service;

import com.mycompany.refaccionaria.automotriz.dao.ProductoDAO;
import com.mycompany.refaccionaria.automotriz.dao.VentaDAO;
import com.mycompany.refaccionaria.automotriz.model.DashboardData;
import java.math.BigDecimal;
import java.sql.SQLException;

public class DashboardService {

    private final ProductoDAO productoDAO;
    private final VentaDAO ventaDAO;

    public DashboardService() {
        this(new ProductoDAO(), new VentaDAO());
    }

    public DashboardService(ProductoDAO productoDAO, VentaDAO ventaDAO) {
        this.productoDAO = productoDAO;
        this.ventaDAO = ventaDAO;
    }

    public DashboardData load(AppSession session) throws SQLException {
        session.requireAuthenticated();
        boolean admin = session.isAdmin();
        return new DashboardData(
                productoDAO.count(),
                productoDAO.countLowStock(),
                productoDAO.countAvailable(),
                productoDAO.countOutOfStock(),
                admin ? ventaDAO.countToday() : 0,
                admin ? ventaDAO.totalToday() : BigDecimal.ZERO,
                productoDAO.findLowStock()
        );
    }
}
