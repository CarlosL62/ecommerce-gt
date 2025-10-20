package com.ecommercegt.api.controller;

import com.ecommercegt.api.model.Role;
import com.ecommercegt.api.model.User;
import com.ecommercegt.api.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AdminUserController(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
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

        // Prevent suspending ADMIN users
        if (user.getRole() == Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede suspender a un usuario ADMIN");
        }

        user.setActive(false);
        users.save(user);
    }

    @PatchMapping("/{id}/activate")
    public void activateUser(@PathVariable Long id) {
        var user = users.findById(id).orElseThrow();
        user.setActive(true);
        users.save(user);
    }

    // DTO for worker creation
    public record CreateWorkerRequest(String name, String email, String password, String role) {}

    // Partial update DTO
    public record UpdateWorkerRequest(String name, String email, String role) {}

    @PostMapping
    public User createWorker(@RequestBody CreateWorkerRequest req) {
        Role role;
        try {
            role = Role.valueOf(req.role());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol inválido");
        }

        // Allow only roles other than COMMON
        if (role == Role.COMMON) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede crear un usuario con rol COMMON");
        }

        if (users.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo ya está en uso");
        }

        var user = new User();
        user.setName(req.name());
        user.setEmail(req.email());
        user.setPassword(encoder.encode(req.password()));
        user.setRole(role);
        return users.save(user);
    }

    @PatchMapping("/{id}")
    public User updateWorker(@PathVariable Long id, @RequestBody UpdateWorkerRequest req) {
        var user = users.findById(id).orElseThrow();

        // Update fields if provided
        if (req.name() != null) {
            user.setName(req.name());
        }
        if (req.email() != null) {
            // Check for email uniqueness
            if (users.existsByEmail(req.email()) && !user.getEmail().equals(req.email())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo ya está en uso");
            }
            user.setEmail(req.email());
        }
        if (req.role() != null) {
            Role newRole;
            try {
                newRole = Role.valueOf(req.role());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol inválido");
            }
            // Update role (optional). Do not allow downgrading ADMIN
            if (user.getRole() == Role.ADMIN && newRole != Role.ADMIN) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede cambiar el rol de un usuario ADMIN");
            }
            user.setRole(newRole);
        }
        return users.save(user);
    }
}
