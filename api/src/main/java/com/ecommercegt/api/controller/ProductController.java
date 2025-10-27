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

    // Centralized validation + parsing to remove duplication
    private record Validated(
            String name,
            String description,
            String imageUrl,
            BigDecimal price,
            Integer stock,
            Product.Condition condition,
            Product.Category category
    ) {}

    private Validated validate(CreateProductRequest req) {
        if (req == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos requeridos");
        var name = (req.name == null ? "" : req.name.trim());
        var description = (req.description == null ? "" : req.description.trim());
        var imageUrl = (req.imageUrl == null ? "" : req.imageUrl.trim());
        if (name.length() < 3 || name.length() > 80)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre inválido");
        if (description.length() < 10 || description.length() > 500)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Descripción inválida");
        if (imageUrl.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Imagen requerida");
        if (req.price == null || req.price.signum() <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Precio inválido");
        if (req.stock == null || req.stock < 1)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock inválido");
        if (req.condition == null || req.category == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Condición y categoría requeridas");
        try {
            var cond = Product.Condition.valueOf(req.condition);
            var cat  = Product.Category.valueOf(req.category);
            return new Validated(name, description, imageUrl, req.price, req.stock, cond, cat);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Condición o categoría inválida");
        }
    }

    private Product requireOwned(Authentication auth, Long id) {
        var user = me(auth);
        var p = products.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        if (!p.getOwner().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes editar este producto");
        return p;
    }

    // COMMON creates product in IN_REVIEW
    @PostMapping
    public ProductResponse create(Authentication auth, @RequestBody CreateProductRequest req) {
        var v = validate(req);

        var user = me(auth);
        var p = new Product();
        p.setOwner(user);
        p.setName(v.name());
        p.setDescription(v.description());
        p.setImageUrl(v.imageUrl());
        p.setPrice(v.price());
        p.setStock(v.stock());
        p.setCondition(v.condition());
        p.setCategory(v.category());
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

    // COMMON: update own product and send to review again
    @PutMapping("/{id}")
    public ProductResponse update(Authentication auth, @PathVariable Long id, @RequestBody CreateProductRequest req) {
        var v = validate(req);

        var p = requireOwned(auth, id);
        p.setName(v.name());
        p.setDescription(v.description());
        p.setImageUrl(v.imageUrl());
        p.setPrice(v.price());
        p.setStock(v.stock());
        p.setCondition(v.condition());
        p.setCategory(v.category());
        p.setStatus(Product.Status.IN_REVIEW);
        return toDto(products.save(p));
    }

    // PATCH variante: mismo comportamiento, permite compatibilidad con front
    @PatchMapping("/{id}")
    public ProductResponse patch(Authentication auth, @PathVariable Long id, @RequestBody CreateProductRequest req) {
        return update(auth, id, req);
    }
}
