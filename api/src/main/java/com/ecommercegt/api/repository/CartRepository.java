package com.ecommercegt.api.repository;

import com.ecommercegt.api.model.Cart;
import com.ecommercegt.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for Cart entity.
 * Provides basic CRUD and a finder by owner (User).
 */
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User owner);
}
