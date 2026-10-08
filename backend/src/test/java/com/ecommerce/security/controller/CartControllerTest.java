package com.ecommerce.security.controller;

import com.ecommerce.security.dto.cart.CartItemRequestDTO;
import com.ecommerce.security.entity.CartItem;
import com.ecommerce.security.entity.Product;
import com.ecommerce.security.entity.User;
import com.ecommerce.security.repository.CartItemRepository;
import com.ecommerce.security.repository.ProductRepository;
import com.ecommerce.security.repository.UserRepository;
import com.ecommerce.security.entity.Category;
import com.ecommerce.security.repository.CategoryRepository;
import com.ecommerce.security.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.sql.init.mode=never",
    "spring.datasource.url=jdbc:h2:mem:testdb-cart",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private com.ecommerce.security.security.jwt.JwtUtils jwtUtils;

    private User user1;
    private User user2;
    private Product product;
    private CartItem cartItemUser1;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        user1 = new User();
        user1.setFirstName("User");
        user1.setLastName("One");
        user1.setEmail("user1@test.local");
        user1.setPasswordHash("hash");
        user1.setRole(com.ecommerce.security.entity.Role.CUSTOMER);
        user1.setActive(true);
        user1 = userRepository.save(user1);

        user2 = new User();
        user2.setFirstName("User");
        user2.setLastName("Two");
        user2.setEmail("user2@test.local");
        user2.setPasswordHash("hash");
        user2.setRole(com.ecommerce.security.entity.Role.CUSTOMER);
        user2.setActive(true);
        user2 = userRepository.save(user2);

        product = new Product();
        Category category = new Category();
        category.setName("Electronics");
        category = categoryRepository.save(category);

        product.setName("Test Product");
        product.setCategory(category);
        product.setPrice(new BigDecimal("10000"));
        product.setStockQuantity(100);
        product = productRepository.save(product);

        cartItemUser1 = new CartItem();
        cartItemUser1.setUser(user1);
        cartItemUser1.setProduct(product);
        cartItemUser1.setQuantity(1);
        cartItemUser1 = cartItemRepository.save(cartItemUser1);
    }

    private String generateToken(User user) {
        CustomUserDetails userDetails = CustomUserDetails.build(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        return jwtUtils.generateJwtToken(authentication);
    }

    @Test
    void testUpdateCartItem_OwnItem_Success() throws Exception {
        CartItemRequestDTO request = new CartItemRequestDTO();
        request.setProductId(product.getId());
        request.setQuantity(2);

        String token1 = generateToken(user1);

        mockMvc.perform(put("/api/cart/items/" + cartItemUser1.getId())
                .header("Authorization", "Bearer " + token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateCartItem_OtherUserItem_IDOR_Fails() throws Exception {
        CartItemRequestDTO request = new CartItemRequestDTO();
        request.setProductId(product.getId());
        request.setQuantity(5);

        String token2 = generateToken(user2);

        mockMvc.perform(put("/api/cart/items/" + cartItemUser1.getId())
                .header("Authorization", "Bearer " + token2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden()); // SecurityException returns 403
    }
}


