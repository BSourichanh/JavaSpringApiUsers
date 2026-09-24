package com.bsourichanh.users.controller;

import com.bsourichanh.users.dto.UserCreationDto;
import com.bsourichanh.users.dto.UserDto;
import com.bsourichanh.users.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserCreationDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwner(authentication, #id)")
    public ResponseEntity<UserDto> getUser(@PathVariable String id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        return userService.deleteUser(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    /**
     * Endpoint appelé par le service de jeux pour valider qu'un X-UserId existe.
     * HTTP 200 + true  → utilisateur valide
     * HTTP 404 + false → utilisateur inconnu
     */
    @GetMapping("/{id}/valid")
    public ResponseEntity<Boolean> isUserValid(@PathVariable String id) {
        boolean valid = userService.isUserValid(id);
        return valid
                ? ResponseEntity.ok(true)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
    }
}
