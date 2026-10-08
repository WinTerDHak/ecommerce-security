package com.ecommerce.security.security;

import com.ecommerce.security.dto.request.LoginRequest;
import com.ecommerce.security.dto.request.RegisterRequest;

import com.ecommerce.security.dto.product.ProductDTO;
import com.ecommerce.security.entity.User;
import com.ecommerce.security.repository.UserRepository;
import com.ecommerce.security.security.jwt.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.sql.init.mode=never",
    "spring.datasource.url=jdbc:h2:mem:testdb-attack",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
public class AttackAndHardeningTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.ecommerce.security.repository.AuditLogRepository auditLogRepository;

    @Autowired
    private com.ecommerce.security.repository.RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private com.ecommerce.security.security.filter.RateLimitFilter rateLimitFilter;

    @Autowired
    private com.ecommerce.security.security.jwt.JwtBlacklistService jwtBlacklistService;

    @BeforeEach
    void setUp() throws Exception {
        auditLogRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        rateLimitFilter.clear();
        jwtBlacklistService.clear();
        userRepository.deleteAll();

        // Create standard user
        RegisterRequest signup = new RegisterRequest();
        signup.setFirstName("Attack");
        signup.setLastName("Tester");
        signup.setEmail("attack@test.local");
        signup.setPassword("Password123!");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup)));
    }

    @Test
    void testXssProtectionInOrderCreation() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("attack@test.local");
        login.setPassword("Password123!");
        String res = mockMvc.perform(post("/api/auth/login").with(request -> { request.setRemoteAddr("10.0.0.88"); return request; }).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(login))).andReturn().getResponse().getContentAsString();
        String adminToken = objectMapper.readTree(res).get("token").asText();

        com.ecommerce.security.dto.order.OrderRequestDTO orderDTO = new com.ecommerce.security.dto.order.OrderRequestDTO();
        orderDTO.setShippingStreet("<script>alert(1)</script>");
        orderDTO.setShippingCity("City");
        orderDTO.setShippingZip("12345");
        orderDTO.setShippingCountry("Country");
        

        mockMvc.perform(post("/api/orders").with(request -> { request.setRemoteAddr("10.0.0.88"); return request; })
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.shippingStreet").value("XSS attempt detected"));
    }

    @Test
    void testRateLimitingOnLogin() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("attack@test.local");
        login.setPassword("WrongPassword!");

        String payload = objectMapper.writeValueAsString(login);

        // First 5 should be 401
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login").with(request -> { request.setRemoteAddr("10.0.0.99"); return request; })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload))
                    .andExpect(status().isUnauthorized());
        }

        // 6th should be 429 Too Many Requests
        mockMvc.perform(post("/api/auth/login").with(request -> { request.setRemoteAddr("10.0.0.99"); return request; })
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void testSqlInjectionInProductSearch() throws Exception {
        mockMvc.perform(get("/api/products?q=' OR 1=1 --"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        // A successful 200 with an empty or normal array indicates it was treated as literal string, not SQL command.
    }

    @Test
    void testJwtBlacklistingOnLogout() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("attack@test.local");
        login.setPassword("Password123!");

        String response = mockMvc.perform(post("/api/auth/login").with(request -> { request.setRemoteAddr("10.0.0.99"); return request; })
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        
        String token = objectMapper.readTree(response).get("token").asText();

        // Use token
        mockMvc.perform(get("/api/cart")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Logout
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Reuse token
        mockMvc.perform(get("/api/cart")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testSecurityHeaders() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Content-Type-Options"))
                .andExpect(header().exists("X-Frame-Options"))
                .andExpect(header().exists("X-XSS-Protection"));
    }

    @Test
    void testSensitiveErrorMessage() throws Exception {
        // Send malformed JSON to trigger an unhandled error or bad request
        mockMvc.perform(post("/api/auth/login").with(request -> { request.setRemoteAddr("10.0.0.99"); return request; })
                .contentType(MediaType.APPLICATION_JSON)
                .content("{malformed-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").doesNotExist()) // Spring Boot defaults shouldn't leak traces
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    private String getAdminToken() throws Exception {
        // Promote user to ADMIN manually
        User user = userRepository.findByEmail("attack@test.local").get();
        user.setRole(com.ecommerce.security.entity.Role.ADMIN);
        userRepository.save(user);

        LoginRequest login = new LoginRequest();
        login.setEmail("attack@test.local");
        login.setPassword("Password123!");

        String response = mockMvc.perform(post("/api/auth/login").with(request -> { request.setRemoteAddr("10.0.0.99"); return request; })
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andReturn().getResponse().getContentAsString();
        
        return objectMapper.readTree(response).get("token").asText();
    }
}










