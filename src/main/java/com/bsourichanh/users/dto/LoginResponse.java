package com.bsourichanh.users.dto;

public record LoginResponse(
        String token,
        String type,
        String userId,
        String username,
        String role
) {
    public LoginResponse(String token) {
        this(token, "Bearer", null, null, null);
    }
}
