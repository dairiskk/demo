// src/main/java/com/example/demo/auth/AuthController.java
package com.example.demo.auth;

import com.example.demo.auth.dto.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static com.example.demo.auth.AuthDtos.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterRequest req) {
        users.findByEmail(req.getEmail()).ifPresent(x -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        });

        User u = new User();
        u.setEmail(req.getEmail());
        u.setPassword(encoder.encode(req.getPassword()));

        users.save(u);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest req) {
        var u = users.findByEmail(req.email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials"));
        if (!encoder.matches(req.password, u.getPassword()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
        return new AuthResponse(jwt.generate(u.getEmail()));
    }
}
