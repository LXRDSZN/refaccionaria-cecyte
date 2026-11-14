package com.mycompany.refaccionaria.automotriz.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Venta {

    private Integer id;
    private LocalDateTime fecha;
    private BigDecimal total;
    private int usuarioId;

    public Venta() {
    }

    public Venta(Integer id, LocalDateTime fecha, BigDecimal total, int usuarioId) {
        this.id = id;
        this.fecha = fecha;
        this.total = total;
        this.usuarioId = usuarioId;
    }

    public Venta(BigDecimal total, int usuarioId) {
        this(null, null, total, usuarioId);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }
}
