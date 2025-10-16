package com.ecommercegt.api.controller;

import com.ecommercegt.api.model.User;
import com.ecommercegt.api.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository repo;

    public UserController(UserRepository repo) {
        this.repo = repo;
    }

}
