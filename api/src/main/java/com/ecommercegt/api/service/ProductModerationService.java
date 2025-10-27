package com.ecommercegt.api.service;

import com.ecommercegt.api.model.Product;
import com.ecommercegt.api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductModerationService {

    private final ProductRepository products;

    @Transactional(readOnly = true)
    public List<Product> listByStatus(Product.Status status) {
        return products.findAllByStatusOrderByCreatedAtDesc(status);
    }

    @Transactional
    public Product approve(Long id) {
        var p = products.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no existe"));
        p.setStatus(Product.Status.APPROVED);
        return products.save(p);
    }

    @Transactional
    public Product reject(Long id) {
        var p = products.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no existe"));
        p.setStatus(Product.Status.REJECTED);
        return products.save(p);
    }

    @Transactional
    public Product reopen(Long id) {
        var p = products.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no existe"));
        p.setStatus(Product.Status.IN_REVIEW);
        return products.save(p);
    }
}