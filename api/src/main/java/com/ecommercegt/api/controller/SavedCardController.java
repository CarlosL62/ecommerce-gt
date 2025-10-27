package com.ecommercegt.api.controller;

import com.ecommercegt.api.model.SavedCard;
import com.ecommercegt.api.repository.UserRepository;
import com.ecommercegt.api.service.SavedCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
@PreAuthorize("hasRole('COMMON')")
@RequiredArgsConstructor
public class SavedCardController {

    private final SavedCardService service;
    private final UserRepository users;

    // Helper method to resolve current user ID by email from Principal
    private Long currentUserId(Principal principal) {
        var u = users.findByEmail(principal.getName()).orElseThrow();
        return u.getId();
    }

    // List all saved cards for the current user
    @GetMapping
    public List<SavedCard> list(Principal principal) {
        return service.listForUser(currentUserId(principal));
    }

    // Create a new saved card for the current user
    @PostMapping
    public SavedCard create(@RequestBody SavedCard payload, Principal principal) {
        return service.create(currentUserId(principal), payload);
    }

    // Delete a saved card (must belong to the user)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Principal principal) {
        service.delete(currentUserId(principal), id);
    }
}
