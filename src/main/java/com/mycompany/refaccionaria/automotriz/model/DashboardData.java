package com.mycompany.refaccionaria.automotriz.model;

import java.math.BigDecimal;
import java.util.List;

public record DashboardData(
        long totalProducts,
        long lowStockProducts,
        long availableProducts,
        long outOfStockProducts,
        long salesToday,
        BigDecimal revenueToday,
        List<Producto> lowStockItems
) {
}
