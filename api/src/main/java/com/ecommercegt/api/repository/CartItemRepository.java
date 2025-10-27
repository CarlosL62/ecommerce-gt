package com.ecommercegt.api.repository;

import com.ecommercegt.api.model.Cart;
import com.ecommercegt.api.model.CartItem;
import com.ecommercegt.api.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CartItem entity.
 * Provides CRUD operations and finders by Cart and (Cart, Product).
 */
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCart(Cart cart);
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}
