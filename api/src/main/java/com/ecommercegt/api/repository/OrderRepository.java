package com.ecommercegt.api.repository;

import com.ecommercegt.api.model.Order;
import com.ecommercegt.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByBuyerOrderByCreatedAtDesc(User buyer);
}