package com.ecommerce.security.controller;

import com.ecommerce.security.dto.product.ProductDTO;
import com.ecommerce.security.entity.Category;
import com.ecommerce.security.entity.Product;
import com.ecommerce.security.repository.CategoryRepository;
import com.ecommerce.security.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private Long validCategoryId;

    @BeforeEach
    void setup() {
        if(categoryRepository.count() == 0) {
             Category category = new Category();
             category.setName("Test Category");
             category.setDescription("Desc");
             category = categoryRepository.save(category);
             validCategoryId = category.getId();
        } else {
             validCategoryId = categoryRepository.findAll().get(0).getId();
        }
    }

    @Test
    @WithMockUser(username = "admin@ecommerce.local", roles = {"ADMIN"})
    void adminCanCreateProduct() throws Exception {
        ProductDTO dto = new ProductDTO();
        dto.setName("New Product");
        dto.setPrice(new BigDecimal("100000"));
        dto.setStockQuantity(10);
        dto.setCategoryId(validCategoryId);

        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Product"));
    }

    @Test
    @WithMockUser(username = "customer@ecommerce.local", roles = {"USER"})
    void customerCannotCreateProduct() throws Exception {
        ProductDTO dto = new ProductDTO();
        dto.setName("New Product");
        dto.setPrice(new BigDecimal("100000"));
        dto.setStockQuantity(10);
        dto.setCategoryId(validCategoryId);

        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedCannotCreateProduct() throws Exception {
        ProductDTO dto = new ProductDTO();
        dto.setName("New Product");
        dto.setPrice(new BigDecimal("100000"));
        dto.setStockQuantity(10);
        dto.setCategoryId(validCategoryId);

        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@ecommerce.local", roles = {"ADMIN"})
    void adminCannotCreateProductWithNegativePrice() throws Exception {
        ProductDTO dto = new ProductDTO();
        dto.setName("New Product");
        dto.setPrice(new BigDecimal("-10000"));
        dto.setStockQuantity(10);
        dto.setCategoryId(validCategoryId);

        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin@ecommerce.local", roles = {"ADMIN"})
    void adminCannotCreateProductWithNegativeStock() throws Exception {
        ProductDTO dto = new ProductDTO();
        dto.setName("New Product");
        dto.setPrice(new BigDecimal("10000"));
        dto.setStockQuantity(-1);
        dto.setCategoryId(validCategoryId);

        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin@ecommerce.local", roles = {"ADMIN"})
    void adminCannotCreateProductWithInvalidCategory() throws Exception {
        ProductDTO dto = new ProductDTO();
        dto.setName("New Product");
        dto.setPrice(new BigDecimal("10000"));
        dto.setStockQuantity(10);
        dto.setCategoryId(9999L); // non-existent

        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin@ecommerce.local", roles = {"ADMIN"})
    void adminCannotCreateProductWithXssName() throws Exception {
        ProductDTO dto = new ProductDTO();
        dto.setName("<script>alert(1)</script>");
        dto.setPrice(new BigDecimal("10000"));
        dto.setStockQuantity(10);
        dto.setCategoryId(validCategoryId);

        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin@ecommerce.local", roles = {"ADMIN"})
    void adminCanUpdateProduct() throws Exception {
        // First create a product directly
        Product product = new Product();
        product.setName("Old Name");
        product.setPrice(new BigDecimal("50000"));
        product.setStockQuantity(5);
        product.setCategory(categoryRepository.findById(validCategoryId).orElseThrow());
        product = productRepository.save(product);

        ProductDTO dto = new ProductDTO();
        dto.setName("Updated Name");
        dto.setPrice(new BigDecimal("75000"));
        dto.setStockQuantity(25);
        dto.setCategoryId(validCategoryId);
        dto.setActive(true);

        mockMvc.perform(put("/api/admin/products/" + product.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.stockQuantity").value(25));
    }

    @Test
    @WithMockUser(username = "customer@ecommerce.local", roles = {"USER"})
    void customerCannotUpdateProduct() throws Exception {
        ProductDTO dto = new ProductDTO();
        dto.setName("Updated Name");
        dto.setPrice(new BigDecimal("75000"));
        dto.setStockQuantity(25);
        dto.setCategoryId(validCategoryId);

        mockMvc.perform(put("/api/admin/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@ecommerce.local", roles = {"ADMIN"})
    void adminCanDeactivateProduct() throws Exception {
        Product product = new Product();
        product.setName("To Be Deactivated");
        product.setPrice(new BigDecimal("50000"));
        product.setStockQuantity(5);
        product.setCategory(categoryRepository.findById(validCategoryId).orElseThrow());
        product = productRepository.save(product);

        mockMvc.perform(delete("/api/admin/products/" + product.getId()))
                .andExpect(status().isNoContent());

        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertFalse(updated.isActive());
    }

    @Test
    @WithMockUser(username = "admin@ecommerce.local", roles = {"ADMIN"})
    void nonexistentProductReturns404() throws Exception {
        ProductDTO dto = new ProductDTO();
        dto.setName("Updated Name");
        dto.setPrice(new BigDecimal("75000"));
        dto.setStockQuantity(25);
        dto.setCategoryId(validCategoryId);

        mockMvc.perform(put("/api/admin/products/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }
}
