package com.bsourichanh.users.service;

import com.bsourichanh.users.dto.UserCreationDto;
import com.bsourichanh.users.dto.UserDto;

import com.bsourichanh.users.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDto createUser(UserCreationDto dto);
    Optional<UserDto> getUserById(String id);
    Optional<UserEntity> getUserByUsername(String username);
    List<UserDto> getAllUsers();
    boolean deleteUser(String id);
    boolean isUserValid(String id);
}
