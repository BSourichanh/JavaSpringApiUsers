package com.bsourichanh.users;

import com.bsourichanh.users.dto.UserCreationDto;
import com.bsourichanh.users.dto.UserDto;
import com.bsourichanh.users.entity.UserEntity;
import com.bsourichanh.users.repository.UserRepository;
import com.bsourichanh.users.security.JwtService;
import com.bsourichanh.users.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JavaSpringUsersApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
        assertThat(passwordEncoder).isNotNull();
        assertThat(jwtService).isNotNull();
    }

    @Test
    @DisplayName("5.1 - Le mot de passe doit être hashé avec BCrypt en base de données")
    void shouldHashPasswordWithBCrypt() {
        String rawPassword = "SuperSecretPassword123!";
        UserCreationDto dto = new UserCreationDto("bob_security", "bob@sec.com", rawPassword, "ROLE_USER");
        UserDto created = userService.createUser(dto);

        UserEntity savedEntity = userRepository.findById(created.id()).orElseThrow();

        assertThat(savedEntity.getPassword()).isNotEqualTo(rawPassword);
        assertThat(savedEntity.getPassword()).startsWith("$2a$");
        assertThat(passwordEncoder.matches(rawPassword, savedEntity.getPassword())).isTrue();
    }

    @Test
    @DisplayName("5.1 - POST /users est public (inscription libre)")
    void shouldAllowRegistrationWithoutAuth() throws Exception {
        String json = """
                {
                    "username": "charlie_test",
                    "email": "charlie@test.com",
                    "password": "PasswordCharlie!"
                }
                """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("5.1 - GET /users sans token requiert une authentification (401 Unauthorized)")
    void shouldBlockAccessToProtectedEndpointsWithoutToken() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("5.2 - POST /auth/login avec identifiants valides délivre un token JWT valide")
    void shouldAuthenticateAndReturnJwtToken() throws Exception {
        String username = "alice_jwt";
        String password = "AlicePassword123!";
        userService.createUser(new UserCreationDto(username, "alice@jwt.com", password, "ROLE_USER"));

        String loginJson = """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(username, password);

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andReturn();

        JsonNode responseNode = objectMapper.readTree(result.getResponse().getContentAsString());
        String token = responseNode.get("token").asText();

        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo(username);
        assertThat(jwtService.extractRole(token)).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("5.2 - POST /auth/login avec mauvais mot de passe renvoie 401 Unauthorized")
    void shouldRejectLoginWithWrongPassword() throws Exception {
        String username = "david_jwt";
        userService.createUser(new UserCreationDto(username, "david@jwt.com", "CorrectPassword123!", "ROLE_USER"));

        String badLoginJson = """
                {
                    "username": "%s",
                    "password": "WrongPassword!"
                }
                """.formatted(username);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badLoginJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("5.3 - GET /users est réservé à ROLE_ADMIN (403 pour ROLE_USER, 200 pour ROLE_ADMIN)")
    void shouldEnforceAdminRoleOnGetAllUsers() throws Exception {
        // Token utilisateur standard (ROLE_USER)
        UserDto normalUser = userService.createUser(new UserCreationDto("normal_user", "normal@test.com", "Pass123!", "ROLE_USER"));
        String userToken = jwtService.generateToken(normalUser.id(), normalUser.username(), "ROLE_USER");

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        // Token administrateur (ROLE_ADMIN)
        UserDto adminUser = userService.createUser(new UserCreationDto("admin_user", "admin@test.com", "AdminPass123!", "ROLE_ADMIN"));
        String adminToken = jwtService.generateToken(adminUser.id(), adminUser.username(), "ROLE_ADMIN");

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("5.3 - DELETE /users/{id} est réservé à ROLE_ADMIN")
    void shouldEnforceAdminRoleOnDeleteUser() throws Exception {
        UserDto target = userService.createUser(new UserCreationDto("target_user", "target@test.com", "Pass123!", "ROLE_USER"));

        // ROLE_USER tente de supprimer -> 403 Forbidden
        UserDto normalUser = userService.createUser(new UserCreationDto("user_deleter", "deleter@test.com", "Pass123!", "ROLE_USER"));
        String userToken = jwtService.generateToken(normalUser.id(), normalUser.username(), "ROLE_USER");

        mockMvc.perform(delete("/users/" + target.id())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        // ROLE_ADMIN tente de supprimer -> 204 No Content
        UserDto adminUser = userService.createUser(new UserCreationDto("admin_deleter", "adm_del@test.com", "AdminPass123!", "ROLE_ADMIN"));
        String adminToken = jwtService.generateToken(adminUser.id(), adminUser.username(), "ROLE_ADMIN");

        mockMvc.perform(delete("/users/" + target.id())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("5.3 - GET /users/{id} : ROLE_USER peut voir son profil mais pas celui d'un tiers")
    void shouldAllowSelfProfileAccessOnly() throws Exception {
        UserDto user1 = userService.createUser(new UserCreationDto("user_one", "one@test.com", "Pass123!", "ROLE_USER"));
        UserDto user2 = userService.createUser(new UserCreationDto("user_two", "two@test.com", "Pass123!", "ROLE_USER"));

        String token1 = jwtService.generateToken(user1.id(), user1.username(), "ROLE_USER");

        // user1 consulte son propre profil -> 200 OK
        mockMvc.perform(get("/users/" + user1.id())
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user_one"));

        // user1 consulte le profil de user2 -> 403 Forbidden
        mockMvc.perform(get("/users/" + user2.id())
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isForbidden());
    }
}
