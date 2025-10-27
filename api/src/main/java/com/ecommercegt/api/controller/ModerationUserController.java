package com.ecommercegt.api.controller;

import com.ecommercegt.api.model.User;
import com.ecommercegt.api.service.UserModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/moderation/users")
@PreAuthorize("hasRole('MODERATOR')")
@RequiredArgsConstructor
public class ModerationUserController {

    private final UserModerationService svc;

    // GET /api/moderation/users?role=COMMON
    @GetMapping
    public List<User> listCommon() {
        return svc.listCommonUsers();
    }

    // PATCH /api/moderation/users/{id}/suspend
    @PatchMapping("/{id}/suspend")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void suspend(@PathVariable Long id) {
        svc.suspend(id);
    }

    // PATCH /api/moderation/users/{id}/activate
    @PatchMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activate(@PathVariable Long id) {
        svc.activate(id);
    }
}