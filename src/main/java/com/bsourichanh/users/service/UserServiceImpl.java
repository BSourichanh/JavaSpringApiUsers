package com.bsourichanh.users.service;

import com.bsourichanh.users.dto.UserCreationDto;
import com.bsourichanh.users.dto.UserDto;
import com.bsourichanh.users.entity.UserEntity;
import com.bsourichanh.users.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDto createUser(UserCreationDto dto) {
        Optional<UserEntity> existing = userRepository.findByUsername(dto.username());
        if (existing.isPresent()) {
            return toDto(existing.get());
        }

        String rawPassword = (dto.password() != null && !dto.password().isBlank())
                ? dto.password()
                : "defaultPassword123!";
        String hashedPassword = passwordEncoder.encode(rawPassword);
        String role = (dto.role() != null && !dto.role().isBlank())
                ? dto.role()
                : "ROLE_USER";

        UserEntity entity = new UserEntity(
                UUID.randomUUID().toString(),
                dto.username(),
                dto.email(),
                hashedPassword,
                role
        );
        return toDto(userRepository.save(entity));
    }

    @Override
    public Optional<UserDto> getUserById(String id) {
        return userRepository.findById(id).map(this::toDto);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public boolean deleteUser(String id) {
        if (!userRepository.existsById(id)) return false;
        userRepository.deleteById(id);
        return true;
    }

    @Override
    public boolean isUserValid(String id) {
        return userRepository.existsById(id);
    }

    private UserDto toDto(UserEntity e) {
        return new UserDto(e.getId(), e.getUsername(), e.getEmail());
    }
}
