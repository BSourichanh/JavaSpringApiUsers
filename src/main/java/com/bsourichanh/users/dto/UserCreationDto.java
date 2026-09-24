package com.bsourichanh.users.dto;

public record UserCreationDto(
        String username,
        String email,
        String password,
        String role
) {
    public UserCreationDto(String username, String email) {
        this(username, email, null, null);
    }

    public UserCreationDto(String username, String email, String password) {
        this(username, email, password, null);
    }
}
