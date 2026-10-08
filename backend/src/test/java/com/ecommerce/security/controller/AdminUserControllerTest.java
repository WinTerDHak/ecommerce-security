package com.ecommerce.security.controller;

import com.ecommerce.security.dto.admin.AdminUserStatusUpdateRequestDTO;
import com.ecommerce.security.entity.Role;
import com.ecommerce.security.entity.User;
import com.ecommerce.security.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long customerId;
    private Long adminId;

    @BeforeEach
    void setUp() {
        if (userRepository.findByEmail("customer_test@ecommerce.local").isEmpty()) {
            User user = new User();
            user.setEmail("customer_test@ecommerce.local");
            user.setFirstName("Cust");
            user.setLastName("Omer");
            user.setPasswordHash("hashed");
            user.setRole(Role.CUSTOMER);
            user.setActive(true);
            userRepository.save(user);
        }
        customerId = userRepository.findByEmail("customer_test@ecommerce.local").get().getId();
        
        // ensure original admin exists
        if (userRepository.findByEmail("admin@ecommerce.local").isEmpty()) {
            User user = new User();
            user.setEmail("admin@ecommerce.local");
            user.setFirstName("Admin");
            user.setLastName("Istrator");
            user.setPasswordHash("hashed");
            user.setRole(Role.ADMIN);
            user.setActive(true);
            userRepository.save(user);
        }
        adminId = userRepository.findByEmail("admin@ecommerce.local").get().getId();
    }

    @Test
    @WithMockUser(username = "admin@ecommerce.local", roles = {"ADMIN"})
    void adminCanListUsers() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "admin@ecommerce.local", roles = {"ADMIN"})
    void adminCanViewUserDetail() throws Exception {
        mockMvc.perform(get("/api/admin/users/" + customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("customer_test@ecommerce.local"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @WithMockUser(username = "customer@ecommerce.local", roles = {"CUSTOMER"})
    void customerCannotListUsers() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "customer@ecommerce.local", roles = {"CUSTOMER"})
    void customerCannotViewUserDetail() throws Exception {
        mockMvc.perform(get("/api/admin/users/" + customerId))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedCannotAccessUsers() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@ecommerce.local", roles = {"ADMIN"})
    void adminCanDisableCustomer() throws Exception {
        AdminUserStatusUpdateRequestDTO request = new AdminUserStatusUpdateRequestDTO();
        request.setActive(false);

        mockMvc.perform(put("/api/admin/users/" + customerId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@ecommerce.local", roles = {"ADMIN"})
    void nonexistentUserReturns404() throws Exception {
        mockMvc.perform(get("/api/admin/users/999999"))
                .andExpect(status().isNotFound());
    }
}
