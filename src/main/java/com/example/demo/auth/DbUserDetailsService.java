// src/main/java/com/example/demo/auth/DbUserDetailsService.java
package com.example.demo.auth;

import com.example.demo.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class DbUserDetailsService implements UserDetailsService {
    private final UserRepository repo;
    public DbUserDetailsService(UserRepository repo) { this.repo = repo; }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var u = repo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Not found"));
        // Simple single-role user; expand as needed
        return org.springframework.security.core.userdetails.User
                .withUsername(u.getEmail())
                .password(u.getPassword()) // already bcrypt’ed
                .authorities("ROLE_USER")
                .build();
    }
}
