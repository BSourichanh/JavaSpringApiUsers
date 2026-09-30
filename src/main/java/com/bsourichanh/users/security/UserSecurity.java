package com.bsourichanh.users.security;

import com.bsourichanh.users.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("userSecurity")
public class UserSecurity {

    private final UserService userService;

    public UserSecurity(UserService userService) {
        this.userService = userService;
    }

    public boolean isOwner(Authentication authentication, String userId) {
        if (authentication == null || userId == null) {
            return false;
        }
        return userService.getUserById(userId)
                .map(user -> user.username().equals(authentication.getName()))
                .orElse(false);
    }
}
