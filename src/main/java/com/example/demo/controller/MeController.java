package com.example.demo.controller;

import com.example.demo.api.dto.CurrentUserResponse;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class MeController {

    private final UserRepository users;

    public MeController(UserRepository users) {
        this.users = users;
    }

    // Protected: Security config should require auth for everything except /auth/** and /h2-console/**
    @GetMapping("/me")
    public CurrentUserResponse me(Authentication auth) {
        // auth.getName() is the username set in your JWT filter -> the email
        String email = auth.getName();

        User u = users.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        return new CurrentUserResponse(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName());
    }
}
