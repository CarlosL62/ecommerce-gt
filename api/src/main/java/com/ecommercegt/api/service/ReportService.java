package com.ecommercegt.api.service;

import com.ecommercegt.api.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    // === Inline DTOs (records) ===
    public record ProductTop(Long productId, String productName, Long unitsSold, BigDecimal revenue) {}
    public record CustomerSpend(Long customerId, String customerName, Long ordersCount, Long itemsCount, BigDecimal totalSpent) {}
    public record CustomerSold(Long sellerId, String sellerName, Long itemsSold, BigDecimal revenue) {}
    public record CustomerOrdersCount(Long customerId, String customerName, Long ordersCount) {}
    public record CustomerActiveListings(Long sellerId, String sellerName, Long activeProducts) {}

    private final ReportRepository repo;

    // --- date helpers ---
    private Instant startOfDay(LocalDate d) { return d.atStartOfDay().toInstant(ZoneOffset.UTC); }
    private Instant startOfNextDay(LocalDate d) { return d.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC); }
    private Instant from(LocalDate f) { return startOfDay(f != null ? f : LocalDate.now().minusDays(29)); } // default: last 30 days
    private Instant to(LocalDate t) { return startOfNextDay(t != null ? t : LocalDate.now()); }

    // 1) Top 10 productos más vendidos (intervalo)
    public List<ProductTop> topProducts(LocalDate f, LocalDate t, int limit) {
        var rows = repo.topProducts(from(f), to(t));
        var list = rows.stream().map(r -> new ProductTop(
                asLong(r[0]), asString(r[1]), asLong(r[2]), asBigDecimal(r[3])
        )).collect(Collectors.toList());
        return list.size() > limit ? list.subList(0, limit) : list;
    }

    // 2) Top 5 clientes (compradores) por gasto total
    public List<CustomerSpend> topCustomersBySpend(LocalDate f, LocalDate t, int limit) {
        var rows = repo.topCustomersBySpend(from(f), to(t));
        var list = rows.stream().map(r -> new CustomerSpend(
                asLong(r[0]), asString(r[1]), asLong(r[2]), asLong(r[3]), asBigDecimal(r[4])
        )).collect(Collectors.toList());
        return list.size() > limit ? list.subList(0, limit) : list;
    }

    // 3) Top 5 clientes (vendedores) por unidades vendidas
    public List<CustomerSold> topSellersByUnits(LocalDate f, LocalDate t, int limit) {
        var rows = repo.topSellersByUnits(from(f), to(t));
        var list = rows.stream().map(r -> new CustomerSold(
                asLong(r[0]), asString(r[1]), asLong(r[2]), asBigDecimal(r[3])
        )).collect(Collectors.toList());
        return list.size() > limit ? list.subList(0, limit) : list;
    }

    // 4) Top 10 clientes (compradores) por número de pedidos
    public List<CustomerOrdersCount> topCustomersByOrders(LocalDate f, LocalDate t, int limit) {
        var rows = repo.topCustomersByOrders(from(f), to(t));
        var list = rows.stream().map(r -> new CustomerOrdersCount(
                asLong(r[0]), asString(r[1]), asLong(r[2])
        )).collect(Collectors.toList());
        return list.size() > limit ? list.subList(0, limit) : list;
    }

    // 5) Top 10 clientes con más productos activos a la venta
    public List<CustomerActiveListings> topSellersByActiveListings(int limit) {
        var rows = repo.topSellersByActiveListings();
        var list = rows.stream().map(r -> new CustomerActiveListings(
                asLong(r[0]), asString(r[1]), asLong(r[2])
        )).collect(Collectors.toList());
        return list.size() > limit ? list.subList(0, limit) : list;
    }

    // --- type coercion helpers ---
    private static Long asLong(Object o) {
        if (o == null) return null;
        if (o instanceof Long l) return l;
        if (o instanceof Integer i) return i.longValue();
        if (o instanceof java.math.BigInteger bi) return bi.longValue();
        if (o instanceof java.math.BigDecimal bd) return bd.longValue();
        return Long.valueOf(o.toString());
    }

    private static String asString(Object o) { return o == null ? null : o.toString(); }

    private static BigDecimal asBigDecimal(Object o) {
        if (o == null) return null;
        if (o instanceof BigDecimal bd) return bd;
        if (o instanceof Long l) return BigDecimal.valueOf(l);
        if (o instanceof Integer i) return BigDecimal.valueOf(i);
        if (o instanceof java.math.BigInteger bi) return new BigDecimal(bi);
        return new BigDecimal(o.toString());
    }
}
