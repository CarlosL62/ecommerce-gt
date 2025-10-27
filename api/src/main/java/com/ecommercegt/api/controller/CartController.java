package com.ecommercegt.api.controller;

import com.ecommercegt.api.model.User;
import com.ecommercegt.api.repository.UserRepository;
import com.ecommercegt.api.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@PreAuthorize("isAuthenticated()") // keep it simple while wiring; we can switch to hasRole('COMMON') later
public class CartController {

    private final CartService cartService;
    private final UserRepository users;

    public CartController(CartService cartService, UserRepository users) {
        this.cartService = cartService;
        this.users = users;
    }

    private User me(Authentication auth){
        return users.findByEmail(auth.getName())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED));
    }

    public record ProductMini(Long id, String name, String imageUrl, java.math.BigDecimal price) {}
    public record CartItemDTO(Long id, ProductMini product, Integer qty, java.math.BigDecimal lineTotal) {}
    public record CartDTO(java.util.List<CartItemDTO> items, java.math.BigDecimal subtotal) {}
    public record AddItemRequest(Long productId, Integer qty) {}
    public record UpdateQtyRequest(Integer qty) {}

    private CartDTO toDto(com.ecommercegt.api.model.Cart cart){
        var list = cart.getItems().stream().map(ci -> new CartItemDTO(
                ci.getId(),
                new ProductMini(
                        ci.getProduct().getId(),
                        ci.getProduct().getName(),
                        ci.getProduct().getImageUrl(),
                        ci.getProduct().getPrice()
                ),
                ci.getQuantity(),
                ci.getLineTotal()
        )).toList();
        return new CartDTO(list, cart.getSubtotal());
    }

    @GetMapping("/cart")
    public CartDTO getCart(Authentication auth){
        var cart = cartService.getOrCreate(me(auth));
        return toDto(cart); // debería devolver [] y subtotal 0 la primera vez
    }

    // Add item to cart
    @PostMapping("/cart/items")
    public CartDTO add(Authentication auth, @RequestBody AddItemRequest req){
        var user = me(auth);
        var qty = (req.qty() != null && req.qty() > 0) ? req.qty() : 1;
        var cart = cartService.addItem(user, req.productId(), qty);
        return toDto(cart);
    }

    // Update item quantity in cart
    @PatchMapping("/cart/items/{id}")
    public CartDTO patchQty(Authentication auth, @PathVariable Long id, @RequestBody UpdateQtyRequest req){
        var user = me(auth);
        if (req.qty() == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "qty is required");
        }
        var cart = cartService.updateQty(user, id, req.qty());
        return toDto(cart);
    }

    // Remove item from cart
    @DeleteMapping("/cart/items/{id}")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(Authentication auth, @PathVariable Long id){
        cartService.removeItem(me(auth), id);
    }

    // Clear cart
    @DeleteMapping("/cart")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(Authentication auth){
        cartService.clear(me(auth));
    }
}
