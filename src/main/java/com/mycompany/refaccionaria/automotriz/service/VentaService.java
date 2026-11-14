package com.mycompany.refaccionaria.automotriz.service;

import com.mycompany.refaccionaria.automotriz.dao.VentaDAO;
import com.mycompany.refaccionaria.automotriz.model.DetalleVenta;
import com.mycompany.refaccionaria.automotriz.model.Venta;
import com.mycompany.refaccionaria.automotriz.model.DetalleVentaResumen;
import com.mycompany.refaccionaria.automotriz.model.VentaResumen;
import java.time.LocalDate;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VentaService {

    private final VentaDAO ventaDAO;

    public VentaService() {
        this(new VentaDAO());
    }

    public VentaService(VentaDAO ventaDAO) {
        this.ventaDAO = ventaDAO;
    }

    public Venta registerSale(AppSession session, List<DetalleVenta> details) throws SQLException {
        session.requireAuthenticated();
        validateDetails(details);
        return ventaDAO.registerSale(new Venta(null, session.getUsuario().getId()), details);
    }

    public List<VentaResumen> findSummaries(AppSession session, Integer saleId, LocalDate from, LocalDate to)
            throws SQLException {
        session.requireAdmin();
        return ventaDAO.findSummaries(saleId, from, to);
    }

    public List<DetalleVentaResumen> findDetails(AppSession session, int saleId) throws SQLException {
        session.requireAdmin();
        return ventaDAO.findDetails(saleId);
    }

    private void validateDetails(List<DetalleVenta> details) {
        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException("Agrega al menos un producto a la venta.");
        }
        Set<Integer> productIds = new HashSet<>();
        for (DetalleVenta detail : details) {
            if (detail == null || detail.getProductoId() <= 0 || detail.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad de cada producto debe ser mayor que cero.");
            }
            if (!productIds.add(detail.getProductoId())) {
                throw new IllegalArgumentException("No se permiten productos repetidos en el carrito.");
            }
        }
    }
}
