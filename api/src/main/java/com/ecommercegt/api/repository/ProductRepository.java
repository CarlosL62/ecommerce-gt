package com.ecommercegt.api.repository;

import com.ecommercegt.api.model.Product;
import com.ecommercegt.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByOwnerOrderByCreatedAtDesc(User owner);
    List<Product> findAllByStatusOrderByCreatedAtDesc(Product.Status status);
}
