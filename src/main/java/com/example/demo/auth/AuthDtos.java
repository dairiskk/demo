// src/main/java/com/example/demo/auth/AuthDtos.java
package com.example.demo.auth;

public class AuthDtos {
    public static class LoginRequest { public String email; public String password; }
    public static class AuthResponse { public String token; public AuthResponse(String t){this.token=t;} }
}
