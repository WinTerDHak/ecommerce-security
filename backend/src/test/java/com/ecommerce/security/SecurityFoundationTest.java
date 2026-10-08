package com.ecommerce.security.security;

import com.ecommerce.security.entity.Role;
import com.ecommerce.security.entity.User;
import com.ecommerce.security.security.jwt.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.sql.init.mode=never", 
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
public class SecurityFoundationTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testArgon2idPasswordHashing() {
        String rawPassword = "SecurePassword123!";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Verify it uses Argon2id format
        assertTrue(encodedPassword.startsWith("$argon2id$"));

        // Verify correct password matches
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));

        // Verify wrong password rejects
        assertFalse(passwordEncoder.matches("WrongPassword", encodedPassword));
    }

    @Test
    void testJwtGenerationAndValidation() {
        String email = "test@ecommerce.local";
        String token = jwtUtils.generateJwtTokenFromUsername(email);

        assertNotNull(token);
        assertTrue(jwtUtils.validateJwtToken(token));

        String extractedEmail = jwtUtils.getUserNameFromJwtToken(token);
        assertEquals(email, extractedEmail);
    }

    @Test
    void testInvalidJwtRejection() {
        String invalidToken = "eyJhbGciOiJIUzUxMiJ9.InvalidPayload.Signature";
        assertFalse(jwtUtils.validateJwtToken(invalidToken));
    }

    @Test
    void testProtectedEndpointRequiresAuthentication() throws Exception {
        // Without authentication
        mockMvc.perform(get("/api/test/customer"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "customer@ecommerce.local", roles = "CUSTOMER")
    void testCustomerCannotAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/test"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@ecommerce.local", roles = "ADMIN")
    void testAdminCanAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/test"))
                .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "customer@ecommerce.local", roles = "CUSTOMER")
    void testCustomerCanAccessProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/api/test/customer"))
                .andExpect(status().isOk());
    }

    @Test
    void testMissingJwtSecretFailsSafely() {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
                .withUserConfiguration(com.ecommerce.security.EcommerceSecurityApplication.class)
                .withPropertyValues("app.jwt.secret="); // Empty secret

        contextRunner.run(context -> {
            assertTrue(context.getStartupFailure() != null, "Context should fail to load due to missing/empty JWT secret");
        });
    }
}

@RestController
class DummyTestController {
    @GetMapping("/api/test/customer")
    public String customerEndpoint() {
        return "Customer OK";
    }

    @GetMapping("/api/admin/test")
    public String adminEndpoint() {
        return "Admin OK";
    }
}
