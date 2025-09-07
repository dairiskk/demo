// src/main/java/com/example/demo/controller/UserController.java
package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository repo;

    public UserController(UserRepository repo) {
        this.repo = repo;
    }

    // GET /api/users
    @GetMapping
    public List<User> all() {
        return repo.findAll();
    }

    // GET /api/users/{id}
    @GetMapping("/{id}")
    public User one(@PathVariable Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    // POST /api/users
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@Valid @RequestBody User user) {
        // check unique email
        repo.findByEmail(user.getEmail()).ifPresent(u -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        });
        return repo.save(user);
    }

    // PUT /api/users/{id}
    // partial update: only non-null fields applied
    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @RequestBody User incoming) {
        User existing = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (incoming.getFirstName() != null) {
            existing.setFirstName(incoming.getFirstName());
        }
        if (incoming.getLastName() != null) {
            existing.setLastName(incoming.getLastName());
        }

        if (incoming.getEmail() != null && !incoming.getEmail().equals(existing.getEmail())) {
            repo.findByEmail(incoming.getEmail()).ifPresent(u -> {
                if (!u.getId().equals(existing.getId())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
                }
            });
            existing.setEmail(incoming.getEmail());
        }

        if (incoming.getPassword() != null) {
            existing.setPassword(incoming.getPassword());
        }

        return repo.save(existing);
    }

    // DELETE /api/users/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        repo.deleteById(id);
    }

    // Optional: GET /api/users/search?email=alice@example.com
    @GetMapping("/search")
    public User byEmail(@RequestParam String email) {
        return repo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
