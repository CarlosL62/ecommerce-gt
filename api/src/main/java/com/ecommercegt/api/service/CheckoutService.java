package com.ecommercegt.api.service;

import com.ecommercegt.api.model.*;
import com.ecommercegt.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CartRepository carts;
    private final CartItemRepository cartItems;
    private final ProductRepository products;
    private final OrderRepository orders;
    private final PaymentRepository payments;
    private final OrderItemRepository orderItems;
    private final SavedCardRepository savedCards;
    private final UserRepository users;

    @Transactional
    public Order checkout(Long buyerId, Long savedCardId, String cardHolder, String cardNumber, String brand, boolean saveNewCard) {
        // 1) Load buyer & cart
        var buyer = users.findById(buyerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer not found"));

        var cart = carts.findByUser(buyer)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

        var items = cartItems.findByCart(cart);
        if (items.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");

        // 2) Validate stock & moderation
        BigDecimal subtotal = BigDecimal.ZERO;
        for (var ci : items) {
            if (ci.getProduct() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart item without product");
            }
            if (ci.getQuantity() < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid quantity for product in cart");
            }

            var p = products.findById(ci.getProduct().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

            if (p.getStatus() != Product.Status.APPROVED) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not approved: " + p.getName());
            }
            if (p.getStock() == null || p.getStock() < ci.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock for: " + p.getName());
            }

            subtotal = subtotal.add(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }

        // 3) Fees (5% platform / 95% seller)
        BigDecimal platformFee = subtotal.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal sellerAmount = subtotal.subtract(platformFee);

        // 4) Create order + items
        var order = Order.builder()
                .buyer(buyer)
                .status(Order.Status.PLACED) // mark as PLACED immediately
                .subtotal(subtotal)
                .platformFee(platformFee)
                .sellerAmount(sellerAmount)
                .build();

        order = orders.save(order);

        for (var ci : items) {
            var p = products.getReferenceById(ci.getProduct().getId());
            // decrement stock
            p.setStock(p.getStock() - ci.getQuantity());
            products.save(p);

            var lineTotal = p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            var oi = OrderItem.builder()
                    .order(order)
                    .product(p)
                    .unitPrice(p.getPrice())
                    .quantity(ci.getQuantity())
                    .lineTotal(lineTotal)
                    .build();
            oi = orderItems.save(oi);
            order.getItems().add(oi);
        }
        order = orders.save(order);

        // 5) Resolve card data (saved card or raw payload) and optionally save new card
        Long usedCardId = null;
        String usedHolder;
        String usedNumber;
        String usedBrand;

        if (savedCardId != null) {
            var saved = savedCards.findById(savedCardId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Saved card not found"));
            if (!saved.getOwner().getId().equals(buyer.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Card does not belong to buyer");
            }
            usedCardId = saved.getId();
            usedHolder = saved.getCardHolder();
            usedNumber = saved.getCardNumber();
            usedBrand  = saved.getBrand();
        } else {
            if (cardNumber == null || cardNumber.isBlank() || cardHolder == null || cardHolder.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Card data required");
            }
            usedHolder = cardHolder;
            usedNumber = cardNumber;
            usedBrand  = (brand != null && !brand.isBlank()) ? brand : "CARD";

            if (saveNewCard) {
                var newCard = com.ecommercegt.api.model.SavedCard.builder()
                        .owner(buyer)
                        .cardHolder(usedHolder)
                        .cardNumber(usedNumber)
                        .brand(usedBrand)
                        .build();
                newCard = savedCards.save(newCard);
                usedCardId = newCard.getId();
            }
        }

        // 6) Payment record (store full card number per project scope)
        var payment = Payment.builder()
                .order(order)
                .cardHolder(usedHolder)
                .cardNumber(usedNumber)
                .brand(usedBrand)
                .amount(subtotal)
                .cardId(usedCardId)
                .build();
        payments.save(payment);

        // 7) Clear cart
        cartItems.deleteAll(items);

        return order;
    }
}