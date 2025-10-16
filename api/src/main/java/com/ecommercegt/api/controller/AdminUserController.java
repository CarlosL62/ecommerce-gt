package com.ecommercegt.api.controller;

import com.ecommercegt.api.model.User;
import com.ecommercegt.api.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final UserRepository users;

    public AdminUserController(UserRepository users) {
        this.users = users;
    }

    @GetMapping
    public List<User> listAll(@RequestParam(required = false) Boolean active) {
        if (active == null) {
            return users.findAll();
        } else {
            return users.findAll().stream()
                    .filter(u -> u.isActive() == active)
                    .toList();
        }
    }

    @PatchMapping("/{id}/suspend")
    public void suspendUser(@PathVariable Long id) {
        var user = users.findById(id).orElseThrow();
        user.setActive(false);
        users.save(user);
    }

    @PatchMapping("/{id}/activate")
    public void activateUser(@PathVariable Long id) {
        var user = users.findById(id).orElseThrow();
        user.setActive(true);
        users.save(user);
    }
}
