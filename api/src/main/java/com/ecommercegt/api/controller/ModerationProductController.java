package com.ecommercegt.api.controller;

import com.ecommercegt.api.model.Product;
import com.ecommercegt.api.service.ProductModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/moderation/products")
@PreAuthorize("hasRole('MODERATOR')")
@RequiredArgsConstructor
public class ModerationProductController {

    private final ProductModerationService svc;

    // DTOs
    public record ProductResponse(
            Long id,
            String name,
            String description,
            String imageUrl,
            BigDecimal price,
            Integer stock,
            String condition,
            String category,
            String status,
            Instant createdAt,
            Long ownerId
    ) {}

    private static ProductResponse toDto(com.ecommercegt.api.model.Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getImageUrl(),
                p.getPrice(),
                p.getStock(),
                p.getCondition() != null ? p.getCondition().name() : null,
                p.getCategory() != null ? p.getCategory().name() : null,
                p.getStatus()   != null ? p.getStatus().name()   : null,
                p.getCreatedAt(),
                (p.getOwner() != null ? p.getOwner().getId() : null) // no forzamos serializar el owner
        );
    }

    @GetMapping
    public List<ProductResponse> listByStatus(@RequestParam("status") Product.Status status) {
        return svc.listByStatus(status)
                .stream()
                .map(ModerationProductController::toDto)
                .collect(Collectors.toList());
    }

    @PatchMapping("/{id}/approve")
    public ProductResponse approve(@PathVariable Long id) {
        return toDto(svc.approve(id));
    }

    @PatchMapping("/{id}/reject")
    public ProductResponse reject(@PathVariable Long id) {
        return toDto(svc.reject(id));
    }

    @PatchMapping("/{id}/reopen")
    public ProductResponse reopen(@PathVariable Long id) {
        return toDto(svc.reopen(id));
    }
}