package com.mycompany.refaccionaria.automotriz.model;

import java.math.BigDecimal;
import java.util.List;

public final class DashboardData {
    private final long totalProducts;
    private final long lowStockProducts;
    private final long availableProducts;
    private final long outOfStockProducts;
    private final long salesToday;
    private final BigDecimal revenueToday;
    private final List<Producto> lowStockItems;

    public DashboardData(long totalProducts, long lowStockProducts, long availableProducts,
            long outOfStockProducts, long salesToday, BigDecimal revenueToday,
            List<Producto> lowStockItems) {
        this.totalProducts = totalProducts;
        this.lowStockProducts = lowStockProducts;
        this.availableProducts = availableProducts;
        this.outOfStockProducts = outOfStockProducts;
        this.salesToday = salesToday;
        this.revenueToday = revenueToday;
        this.lowStockItems = lowStockItems;
    }

    public long totalProducts() { return totalProducts; }
    public long lowStockProducts() { return lowStockProducts; }
    public long availableProducts() { return availableProducts; }
    public long outOfStockProducts() { return outOfStockProducts; }
    public long salesToday() { return salesToday; }
    public BigDecimal revenueToday() { return revenueToday; }
    public List<Producto> lowStockItems() { return lowStockItems; }
}
