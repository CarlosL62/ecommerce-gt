package com.ecommercegt.api.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommercegt.api.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface ReportRepository extends JpaRepository<Order, Long> {

    // 1) Top productos más vendidos (unidades + revenue) en intervalo
    @Query("""
      select p.id, p.name, sum(oi.quantity) as units, sum(oi.quantity * oi.unitPrice) as revenue
      from OrderItem oi
        join oi.order o
        join oi.product p
      where o.createdAt >= :from and o.createdAt < :to
      group by p.id, p.name
      order by units desc
    """)
    List<Object[]> topProducts(@Param("from") Instant from, @Param("to") Instant to);

    // 2) Top clientes (compradores) por gasto total en intervalo
    @Query("""
      select u.id, u.name, count(distinct o.id) as ordersCount, sum(oi.quantity) as items, sum(oi.quantity * oi.unitPrice) as spent
      from OrderItem oi
        join oi.order o
        join o.buyer u
      where o.createdAt >= :from and o.createdAt < :to
      group by u.id, u.name
      order by spent desc
    """)
    List<Object[]> topCustomersBySpend(@Param("from") Instant from, @Param("to") Instant to);

    // 3) Top vendedores por unidades vendidas en intervalo
    @Query("""
      select u.id, u.name, sum(oi.quantity) as itemsSold, sum(oi.quantity * oi.unitPrice) as revenue
      from OrderItem oi
        join oi.order o
        join oi.product p
        join p.owner u
      where o.createdAt >= :from and o.createdAt < :to
      group by u.id, u.name
      order by itemsSold desc
    """)
    List<Object[]> topSellersByUnits(@Param("from") Instant from, @Param("to") Instant to);

    // 4) Top compradores por número de pedidos en intervalo
    @Query("""
      select u.id, u.name, count(o) as ordersCount
      from Order o
        join o.buyer u
      where o.createdAt >= :from and o.createdAt < :to
      group by u.id, u.name
      order by ordersCount desc
    """)
    List<Object[]> topCustomersByOrders(@Param("from") Instant from, @Param("to") Instant to);

    // 5) Top clientes con más productos activos a la venta (status = APPROVED)
    @Query("""
      select u.id, u.name, count(p) as activeProducts
      from Product p
        join p.owner u
      where p.status = 'APPROVED'
      group by u.id, u.name
      order by activeProducts desc
    """)
    List<Object[]> topSellersByActiveListings();
}