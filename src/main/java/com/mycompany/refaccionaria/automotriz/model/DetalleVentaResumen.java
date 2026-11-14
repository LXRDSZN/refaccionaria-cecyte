package com.mycompany.refaccionaria.automotriz.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class DetalleVentaResumen {
    private final int ventaId;
    private final LocalDateTime fecha;
    private final String nombreUsuario;
    private final String codigoProducto;
    private final String nombreProducto;
    private final int cantidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal subtotal;
    private final BigDecimal totalVenta;

    public DetalleVentaResumen(int ventaId, LocalDateTime fecha, String nombreUsuario,
            String codigoProducto, String nombreProducto, int cantidad,
            BigDecimal precioUnitario, BigDecimal subtotal, BigDecimal totalVenta) {
        this.ventaId = ventaId;
        this.fecha = fecha;
        this.nombreUsuario = nombreUsuario;
        this.codigoProducto = codigoProducto;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
        this.totalVenta = totalVenta;
    }

    public int ventaId() { return ventaId; }
    public LocalDateTime fecha() { return fecha; }
    public String nombreUsuario() { return nombreUsuario; }
    public String codigoProducto() { return codigoProducto; }
    public String nombreProducto() { return nombreProducto; }
    public int cantidad() { return cantidad; }
    public BigDecimal precioUnitario() { return precioUnitario; }
    public BigDecimal subtotal() { return subtotal; }
    public BigDecimal totalVenta() { return totalVenta; }
}
