package com.mycompany.refaccionaria.automotriz.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class VentaResumen {
    private final int id;
    private final LocalDateTime fecha;
    private final String nombreUsuario;
    private final BigDecimal total;

    public VentaResumen(int id, LocalDateTime fecha, String nombreUsuario, BigDecimal total) {
        this.id = id;
        this.fecha = fecha;
        this.nombreUsuario = nombreUsuario;
        this.total = total;
    }

    public int id() { return id; }
    public LocalDateTime fecha() { return fecha; }
    public String nombreUsuario() { return nombreUsuario; }
    public BigDecimal total() { return total; }
}
