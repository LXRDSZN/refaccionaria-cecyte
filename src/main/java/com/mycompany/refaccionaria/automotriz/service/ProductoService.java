package com.mycompany.refaccionaria.automotriz.service;

import com.mycompany.refaccionaria.automotriz.dao.ProductoDAO;
import com.mycompany.refaccionaria.automotriz.model.Producto;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ProductoService {

    private final ProductoDAO productoDAO;

    public ProductoService() {
        this(new ProductoDAO());
    }

    public ProductoService(ProductoDAO productoDAO) {
        this.productoDAO = productoDAO;
    }

    public List<Producto> findAll(AppSession session) throws SQLException {
        session.requireAuthenticated();
        return productoDAO.findAll();
    }

    public List<Producto> search(AppSession session, String term) throws SQLException {
        session.requireAuthenticated();
        return term == null || term.trim().isEmpty() ? productoDAO.findAll() : productoDAO.search(term);
    }

    public Producto create(AppSession session, Producto producto) throws SQLException {
        session.requireAdmin();
        validate(producto);
        if (productoDAO.findByCodigo(producto.getCodigo().trim()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un producto con ese código.");
        }
        return productoDAO.save(producto);
    }

    public void update(AppSession session, Producto producto) throws SQLException {
        session.requireAdmin();
        if (producto == null || producto.getId() == null) {
            throw new IllegalArgumentException("El producto seleccionado no es válido.");
        }
        validate(producto);
        productoDAO.findByCodigo(producto.getCodigo().trim()).ifPresent(existing -> {
            if (!existing.getId().equals(producto.getId())) {
                throw new IllegalArgumentException("Ya existe un producto con ese código.");
            }
        });
        productoDAO.update(producto);
    }

    public void delete(AppSession session, int productoId) throws SQLException {
        session.requireAdmin();
        if (productoDAO.hasSales(productoId)) {
            throw new IllegalStateException("No se puede eliminar un producto que tiene ventas asociadas.");
        }
        productoDAO.delete(productoId);
    }

    public void adjustStock(AppSession session, int productoId, int newStock) throws SQLException {
        session.requireAdmin();
        if (newStock < 0) {
            throw new IllegalArgumentException("La existencia no puede ser negativa.");
        }
        productoDAO.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("El producto no existe."));
        try (java.sql.Connection connection = com.mycompany.refaccionaria.automotriz.config.DatabaseConnection.getConnection()) {
            productoDAO.updateStock(connection, productoId, newStock);
        }
    }

    private void validate(Producto producto) {
        if (producto == null
                || isBlank(producto.getCodigo())
                || isBlank(producto.getNombre())
                || isBlank(producto.getMarca())
                || isBlank(producto.getCategoria())) {
            throw new IllegalArgumentException("Completa todos los campos del producto.");
        }
        if (producto.getCodigo().trim().length() > 50
                || producto.getNombre().trim().length() > 150
                || producto.getMarca().trim().length() > 100
                || producto.getCategoria().trim().length() > 100) {
            throw new IllegalArgumentException("Uno de los campos supera su longitud permitida.");
        }
        if (producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio debe ser mayor o igual a cero.");
        }
        if (producto.getStock() < 0 || producto.getStockMinimo() < 0) {
            throw new IllegalArgumentException("Las existencias no pueden ser negativas.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
