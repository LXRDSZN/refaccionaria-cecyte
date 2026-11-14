package com.mycompany.refaccionaria.automotriz.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DetalleVentaResumen(
        int ventaId,
        LocalDateTime fecha,
        String nombreUsuario,
        String codigoProducto,
        String nombreProducto,
        int cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal,
        BigDecimal totalVenta
) {
}
