package com.example.demo.api.dto;

public class CurrentUserResponse {
    private final Long id;
    private final String email;
    private final String firstName;
    private final String lastName;

    public CurrentUserResponse(Long id, String email, String firstName, String lastName) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
}
