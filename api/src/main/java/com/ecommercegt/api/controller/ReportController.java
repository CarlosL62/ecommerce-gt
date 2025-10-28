package com.ecommercegt.api.controller;

import com.ecommercegt.api.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN')")
public class ReportController {

    private final ReportService service;

    // 1) Top 10 productos más vendidos (intervalo)
    @GetMapping("/top-products")
    public List<ReportService.ProductTop> topProducts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return service.topProducts(from, to, Math.max(1, Math.min(limit, 50)));
    }

    // 2) Top 5 clientes que más ganancias por compras han generado (mayor gasto como compradores)
    @GetMapping("/top-customers-spend")
    public List<ReportService.CustomerSpend> topCustomersBySpend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return service.topCustomersBySpend(from, to, Math.max(1, Math.min(limit, 50)));
    }

    // 3) Top 5 clientes que más productos han vendido (vendedores)
    @GetMapping("/top-sellers-units")
    public List<ReportService.CustomerSold> topSellersByUnits(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return service.topSellersByUnits(from, to, Math.max(1, Math.min(limit, 50)));
    }

    // 4) Top 10 clientes que más pedidos han realizado (compradores por número de órdenes)
    @GetMapping("/top-customers-orders")
    public List<ReportService.CustomerOrdersCount> topCustomersByOrders(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return service.topCustomersByOrders(from, to, Math.max(1, Math.min(limit, 50)));
    }

    // 5) Top 10 clientes que más productos tienen a la venta (listados activos)
    @GetMapping("/top-sellers-active-listings")
    public List<ReportService.CustomerActiveListings> topSellersByActiveListings(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return service.topSellersByActiveListings(Math.max(1, Math.min(limit, 50)));
    }
}
