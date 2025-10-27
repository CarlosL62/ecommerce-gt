package com.ecommercegt.api.service;

import com.ecommercegt.api.model.Role;
import com.ecommercegt.api.model.User;
import com.ecommercegt.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserModerationService {

    private final UserRepository users;

    @Transactional(readOnly = true)
    public List<User> listCommonUsers() {
        return users.findByRole(Role.COMMON);
    }

    @Transactional
    public void suspend(Long userId) {
        var u = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no existe"));
        u.setActive(false);
        users.save(u);
    }

    @Transactional
    public void activate(Long userId) {
        var u = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no existe"));
        u.setActive(true);
        users.save(u);
    }
}