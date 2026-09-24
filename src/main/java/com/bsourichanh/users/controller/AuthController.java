package com.bsourichanh.users.controller;

import com.bsourichanh.users.dto.LoginRequest;
import com.bsourichanh.users.dto.LoginResponse;
import com.bsourichanh.users.entity.UserEntity;
import com.bsourichanh.users.repository.UserRepository;
import com.bsourichanh.users.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Validation par Spring Security
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            UserEntity user = userRepository.findByUsername(request.username())
                    .orElseThrow(() -> new BadCredentialsException("Utilisateur introuvable"));

            String token = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole());
            return ResponseEntity.ok(new LoginResponse(
                    token,
                    "Bearer",
                    user.getId(),
                    user.getUsername(),
                    user.getRole()
            ));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Identifiants incorrects"));
        }
    }
}
