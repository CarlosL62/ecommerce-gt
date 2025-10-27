package com.ecommercegt.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {

    public enum Status { PLACED, SHIPPED, DELIVERED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Buyer who placed the order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal platformFee;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal sellerAmount;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant deliveryDueDate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = Status.PLACED;
        if (deliveryDueDate == null) deliveryDueDate = createdAt.plus(5, ChronoUnit.DAYS);
    }
}