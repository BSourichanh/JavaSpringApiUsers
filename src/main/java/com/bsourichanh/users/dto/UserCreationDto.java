package com.bsourichanh.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreationDto(
        @NotBlank(message = "Le nom d'utilisateur est obligatoire")
        String username,

        @NotBlank(message = "L'adresse email est obligatoire")
        @Email(message = "Format d'email invalide")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 6, message = "Le mot de passe doit comporter au moins 6 caractères")
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
