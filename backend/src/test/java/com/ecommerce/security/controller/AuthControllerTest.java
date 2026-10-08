package com.ecommerce.security.controller;

import com.ecommerce.security.dto.request.LoginRequest;
import com.ecommerce.security.dto.request.RegisterRequest;
import com.ecommerce.security.entity.User;
import com.ecommerce.security.repository.UserRepository;
import com.ecommerce.security.repository.RefreshTokenRepository;
import com.ecommerce.security.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
    "spring.sql.init.mode=never",
    "spring.datasource.url=jdbc:h2:mem:testdb-auth",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@ecommerce.local");
        request.setPassword("SecurePass123!@#");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("john.doe@ecommerce.local"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
                
        assertTrue(userRepository.existsByEmail("john.doe@ecommerce.local"));
    }

    @Test
    void testRegisterUser_WeakPassword_FailsValidation() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.weak@ecommerce.local");
        request.setPassword("weakpass"); // Fails @ValidPassword

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").exists());
    }

    @Test
    void testLoginUser_Success() throws Exception {
        // Pre-register user
        RegisterRequest register = new RegisterRequest();
        register.setFirstName("Jane");
        register.setLastName("Doe");
        register.setEmail("jane.doe@ecommerce.local");
        register.setPassword("SecurePass123!@#");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        // Login
        LoginRequest login = new LoginRequest();
        login.setEmail("jane.doe@ecommerce.local");
        login.setPassword("SecurePass123!@#");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void testLoginUser_WrongPassword() throws Exception {
        // Pre-register user
        RegisterRequest register = new RegisterRequest();
        register.setFirstName("Alice");
        register.setLastName("Smith");
        register.setEmail("alice@ecommerce.local");
        register.setPassword("SecurePass123!@#");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        // Login with wrong password
        LoginRequest login = new LoginRequest();
        login.setEmail("alice@ecommerce.local");
        login.setPassword("WrongPass123!@#");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }
}


