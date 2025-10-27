package com.ecommercegt.api.controller;

import com.ecommercegt.api.model.Order;
import com.ecommercegt.api.model.OrderItem;
import com.ecommercegt.api.model.User;
import com.ecommercegt.api.repository.OrderRepository;
import com.ecommercegt.api.repository.UserRepository;
import com.ecommercegt.api.service.CheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
//@PreAuthorize("hasRole('COMMON')")
@RequiredArgsConstructor
public class OrderController {

    private final CheckoutService checkout;
    private final OrderRepository orders;
    private final UserRepository users;

    // Incoming payload for checkout
    public record CheckoutRequest(Long savedCardId, String cardHolder, String cardNumber, String brand, boolean save) {}

    // Flat DTOs for responses
    public record OrderLineDTO(
            Long productId,
            String name,
            String imageUrl,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal lineTotal
    ) {}

    public record OrderSummary(
            Long id,
            String status,
            String createdAt,
            String deliveryDueDate,
            BigDecimal subtotal,
            List<OrderLineDTO> items
    ) {}

    @PreAuthorize("hasRole('COMMON')")
    @PostMapping("/checkout")
    public OrderSummary doCheckout(@RequestBody CheckoutRequest req, Principal principal) {
        Long buyerId = users.findByEmail(principal.getName()).orElseThrow().getId();
        Order o = checkout.checkout(buyerId, req.savedCardId(), req.cardHolder(), req.cardNumber(), req.brand(), req.save());
        return toSummary(o);
    }

    @PreAuthorize("hasRole('COMMON')")
    @GetMapping("/mine")
    public List<OrderSummary> myOrders(Principal principal) {
        User user = users.findByEmail(principal.getName()).orElseThrow();
        return orders.findAllByBuyerOrderByCreatedAtDesc(user).stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    // ---- mapping helpers ----
    private OrderSummary toSummary(Order o) {
        List<OrderLineDTO> lines = (o.getItems() == null) ? List.of() : o.getItems().stream()
                .map(this::toLine)
                .collect(Collectors.toList());
        return new OrderSummary(
                o.getId(),
                o.getStatus() != null ? o.getStatus().name() : null,
                o.getCreatedAt() != null ? o.getCreatedAt().toString() : null,
                o.getDeliveryDueDate() != null ? o.getDeliveryDueDate().toString() : null,
                o.getSubtotal(),
                lines
        );
    }

    private OrderLineDTO toLine(OrderItem it) {
        var p = it.getProduct();
        return new OrderLineDTO(
                p != null ? p.getId() : null,
                p != null ? p.getName() : null,
                p != null ? p.getImageUrl() : null,
                it.getUnitPrice(),
                it.getQuantity(),
                it.getLineTotal()
        );
    }

    @PreAuthorize("hasRole('LOGISTICS')")
    @GetMapping
    public List<OrderSummary> listByStatus(@RequestParam(name = "status", required = false) String status) {
        if (status == null || status.isBlank()) {
            return orders.findAll().stream().map(this::toSummary).toList();
        }
        Order.Status s = Order.Status.valueOf(status.toUpperCase());
        return orders.findAll().stream()
                .filter(o -> o.getStatus() == s)
                .map(this::toSummary)
                .toList();
    }

    @PatchMapping("/{id}/ship")
    @PreAuthorize("hasRole('LOGISTICS')")
    public OrderSummary markShipped(@PathVariable Long id) {
        var o = orders.findById(id).orElseThrow();
        if (o.getStatus() != Order.Status.DELIVERED && o.getStatus() != Order.Status.SHIPPED) {
            o.setStatus(Order.Status.SHIPPED);
            orders.save(o);
        }
        return toSummary(o);
    }

    @PatchMapping("/{id}/deliver")
    @PreAuthorize("hasRole('LOGISTICS')")
    public OrderSummary markDelivered(@PathVariable Long id) {
        var o = orders.findById(id).orElseThrow();
        if (o.getStatus() != Order.Status.DELIVERED) {
            o.setStatus(Order.Status.DELIVERED);
            orders.save(o);
        }
        return toSummary(o);
    }
}