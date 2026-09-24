package com.bsourichanh.users.security;

import com.bsourichanh.users.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("userSecurity")
public class UserSecurity {

    private final UserRepository userRepository;

    public UserSecurity(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isOwner(Authentication authentication, String userId) {
        if (authentication == null || userId == null) {
            return false;
        }
        return userRepository.findById(userId)
                .map(user -> user.getUsername().equals(authentication.getName()))
                .orElse(false);
    }
}
