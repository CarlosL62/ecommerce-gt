package com.ecommercegt.api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a single item inside a user's cart.
 * Stores a reference to the product and the quantity chosen,
 * along with a snapshot of the product's price at the moment it was added.
 */
@Entity
@Table(name = "cart_items")
@Getter @Setter @NoArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The cart this item belongs to */
    @ManyToOne(optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    /** The product added to the cart */
    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Quantity of this product in the cart (must be >= 1) */
    @Min(1)
    @Column(nullable = false)
    private Integer quantity;

    /** Snapshot of the product's price when added */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Calculates the total cost for this item (unitPrice * qty).
     * This value is not persisted in the database.
     */
    @Transient
    public BigDecimal getLineTotal() {
        BigDecimal price = unitPrice != null
                ? unitPrice
                : (product != null && product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO);
        int q = (quantity != null && quantity > 0) ? quantity : 0;
        return price.multiply(BigDecimal.valueOf(q));
    }

    // Temporal
    @PrePersist
    @PreUpdate
    private void ensureDefaults() {
        if (quantity == null || quantity < 1) quantity = 1;
        if (unitPrice == null && product != null) unitPrice = product.getPrice();
    }
}
