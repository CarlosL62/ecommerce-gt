package com.ecommercegt.api.service;

import com.ecommercegt.api.model.Cart;
import com.ecommercegt.api.model.CartItem;
import com.ecommercegt.api.model.Product;
import com.ecommercegt.api.model.User;
import com.ecommercegt.api.repository.CartItemRepository;
import com.ecommercegt.api.repository.CartRepository;
import com.ecommercegt.api.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Business logic for the shopping cart.
 * - Create or find a cart for the user
 * - Add/merge items
 * - Update item quantity
 * - Remove items and clear cart
 */
@Service
public class CartService {

    private final CartRepository carts;
    private final CartItemRepository items;
    private final ProductRepository products;

    public CartService(CartRepository carts, CartItemRepository items, ProductRepository products) {
        this.carts = carts;
        this.items = items;
        this.products = products;
    }

    /**
     * Get existing cart for user or create a new one.
     */
    @Transactional
    public Cart getOrCreate(User user) {
        return carts.findByUser(user).orElseGet(() -> {
            var c = new Cart();
            c.setUser(user);
            return carts.save(c);
        });
    }

    /**
     * Add a product to the cart (merge if the same product already exists).
     * Validates: qty >= 1, product exists, APPROVED status, sufficient stock.
     */
    @Transactional
    public Cart addItem(User user, Long productId, Integer qty) {
        int reqQty = (qty == null || qty < 1) ? 1 : qty;

        var product = products.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        if (product.getStatus() != Product.Status.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto no disponible");
        }
        Integer stock = product.getStock();
        if (stock == null || stock < reqQty) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock insuficiente");
        }

        var cart = getOrCreate(user);

        // Try to merge with existing line for the same product (repository lookup)
        var existingOpt = items.findByCartAndProduct(cart, product);
        if (existingOpt.isPresent()) {
            var existing = existingOpt.get();
            int newQty = (existing.getQuantity() == null ? 0 : existing.getQuantity()) + reqQty;
            if (stock == null || stock < newQty) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock insuficiente");
            }
            existing.setQuantity(newQty);
            existing.setUnitPrice(product.getPrice()); // refresh price snapshot
            items.save(existing);
        } else {
            var ci = new CartItem();
            ci.setCart(cart);
            ci.setProduct(product);
            ci.setQuantity(reqQty);
            ci.setUnitPrice(product.getPrice()); // snapshot
            items.save(ci);
            cart.getItems().add(ci);
        }

        return carts.save(cart);
    }

    /**
     * Update quantity for a specific cart item.
     * Validates: qty >= 1, item belongs to user's cart, sufficient stock.
     */
    @Transactional
    public Cart updateQty(User user, Long itemId, int qty) {
        int reqQty = (qty < 1) ? 1 : qty;

        var cart = carts.findByUser(user).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carrito no existe"));

        var item = cart.getItems().stream()
                .filter(ci -> ci.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ítem no existe"));

        var product = item.getProduct();
        Integer stock = product.getStock();
        if (stock == null || stock < reqQty) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock insuficiente");
        }

        item.setQuantity(reqQty);
        item.setUnitPrice(product.getPrice());
        items.save(item);

        return carts.save(cart);
    }

    /**
     * Remove a single item from the user's cart.
     */
    @Transactional
    public void removeItem(User user, Long itemId) {
        var cart = carts.findByUser(user).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carrito no existe"));
        var toRemove = cart.getItems().stream()
                .filter(ci -> ci.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ítem no existe"));
        cart.getItems().remove(toRemove);
        items.delete(toRemove);
        carts.save(cart);
    }

    /**
     * Clear all items from the user's cart.
     */
    @Transactional
    public void clear(User user) {
        var cart = carts.findByUser(user).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carrito no existe"));
        items.deleteAll(cart.getItems());
        cart.getItems().clear();
        carts.save(cart);
    }
}
