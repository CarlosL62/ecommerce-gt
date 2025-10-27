package com.ecommercegt.api.controller;

import com.ecommercegt.api.model.Product;
import com.ecommercegt.api.model.User;
import com.ecommercegt.api.repository.ProductRepository;
import com.ecommercegt.api.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@PreAuthorize("hasRole('COMMON')")
public class ProductController {

    private final ProductRepository products;
    private final UserRepository users;

    public ProductController(ProductRepository products, UserRepository users) {
        this.products = products;
        this.users = users;
    }

    // DTOs
    public record CreateProductRequest(
            String name,
            String description,
            String imageUrl,
            BigDecimal price,
            Integer stock,
            String condition,
            String category
    ) {}

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
            Long ownerId
    ) {}

    private User me(Authentication auth){
        return users.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private ProductResponse toDto(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getImageUrl(),
                p.getPrice(),
                p.getStock(),
                p.getCondition().name(),
                p.getCategory().name(),
                p.getStatus().name(),
                p.getOwner().getId()
        );
    }

    // COMMON creates product in IN_REVIEW
    @PostMapping
    public ProductResponse create(Authentication auth, @RequestBody CreateProductRequest req) {
        // --- basic validations aligned with front rules ---
        if (req.name == null || req.name.trim().length() < 3 || req.name.trim().length() > 80)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre inválido");
        if (req.description == null || req.description.trim().length() < 10 || req.description.trim().length() > 500)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Descripción inválida");
        if (req.imageUrl == null || req.imageUrl.trim().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Imagen requerida");
        if (req.price == null || req.price.signum() <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Precio inválido");
        if (req.stock == null || req.stock < 1)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock inválido");
        if (req.condition == null || req.category == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Condición y categoría requeridas");

        Product.Condition cond;
        Product.Category cat;
        try {
            cond = Product.Condition.valueOf(req.condition);
            cat  = Product.Category.valueOf(req.category);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Condición o categoría inválida");
        }

        var user = me(auth);
        var p = new Product();
        p.setOwner(user);
        p.setName(req.name.trim());
        p.setDescription(req.description.trim());
        p.setImageUrl(req.imageUrl.trim());
        p.setPrice(req.price);
        p.setStock(req.stock);
        p.setCondition(cond);
        p.setCategory(cat);
        p.setStatus(Product.Status.IN_REVIEW);

        return toDto(products.save(p));
    }

    // COMMON: list own products
    @GetMapping("/mine")
    public List<ProductResponse> myProducts(Authentication auth) {
        var user = me(auth);
        return products.findAllByOwnerOrderByCreatedAtDesc(user).stream().map(this::toDto).toList();
    }

    // Public catalog: only APPROVED
    @GetMapping("/catalog")
    public List<ProductResponse> catalog() {
        return products.findAllByStatusOrderByCreatedAtDesc(Product.Status.APPROVED)
                .stream().map(this::toDto).toList();
    }
}
