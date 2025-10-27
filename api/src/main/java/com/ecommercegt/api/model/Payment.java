package com.ecommercegt.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity @Table(name = "payments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One payment per order in this simple model
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    // Card info (for this project, we store the full number as security validation is out of scope)
    @Column(nullable = false)
    private String cardHolder;

    @Column(nullable = false, length = 19)
    private String cardNumber;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private Instant paidAt;

    // Optional link to a saved card (user can store multiple cards)
    // We keep the snapshot fields (cardHolder, cardLast4, brand) for historical accuracy
    // even if the saved card changes later. This avoids mutating past payments.
    @Column(name = "card_id")
    private Long cardId; // nullable: present only if checkout used a saved card

    @PrePersist
    public void onCreate() {
        if (paidAt == null) paidAt = Instant.now();
    }
}