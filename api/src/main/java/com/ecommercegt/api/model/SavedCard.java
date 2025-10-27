package com.ecommercegt.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "saved_cards")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SavedCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Card owner (the user who owns this card)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // Full card data (for this project, we store full numbers)
    @Column(nullable = false)
    private String cardHolder;

    @Column(nullable = false, length = 19)
    private String cardNumber;

    @Column(nullable = false)
    private String brand; // e.g. VISA, MASTERCARD

    // Optional friendly name to identify the card (like “My VISA”)
    @Column(length = 64)
    private String label;

    // Optional expiration data
    @Column
    private Integer expMonth;

    @Column
    private Integer expYear;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = Instant.now();
    }
}