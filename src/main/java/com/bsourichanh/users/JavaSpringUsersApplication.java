package com.bsourichanh.users;

import com.bsourichanh.users.dto.UserCreationDto;
import com.bsourichanh.users.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JavaSpringUsersApplication {
    public static void main(String[] args) {
        SpringApplication.run(JavaSpringUsersApplication.class, args);
    }

    @Bean
    CommandLineRunner initDefaultUsers(UserService userService) {
        return args -> {
            userService.createUser(new UserCreationDto("Alice", "alice@example.com", "password123", "ROLE_USER"));
            userService.createUser(new UserCreationDto("Admin", "admin@example.com", "admin123", "ROLE_ADMIN"));
        };
    }
}

