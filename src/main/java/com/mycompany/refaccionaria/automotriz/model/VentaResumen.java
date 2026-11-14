package com.mycompany.refaccionaria.automotriz.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VentaResumen(
        int id,
        LocalDateTime fecha,
        String nombreUsuario,
        BigDecimal total
) {
}
