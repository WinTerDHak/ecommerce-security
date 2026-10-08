package com.ecommerce.security.controller;

import com.ecommerce.security.dto.order.OrderRequestDTO;
import com.ecommerce.security.entity.CartItem;
import com.ecommerce.security.entity.Order;
import com.ecommerce.security.entity.Product;
import com.ecommerce.security.entity.User;
import com.ecommerce.security.repository.CartItemRepository;
import com.ecommerce.security.repository.OrderRepository;
import com.ecommerce.security.repository.ProductRepository;
import com.ecommerce.security.repository.UserRepository;
import com.ecommerce.security.repository.CategoryRepository;
import com.ecommerce.security.entity.Category;
import com.ecommerce.security.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.sql.init.mode=never",
    "spring.datasource.url=jdbc:h2:mem:testdb-order",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private com.ecommerce.security.repository.AuditLogRepository auditLogRepository;

    @Autowired
    private com.ecommerce.security.security.jwt.JwtUtils jwtUtils;

    private User user1;
    private User user2;
    private Product product;
    private String token1;
    private String token2;
    private Order orderUser1;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        user1 = new User();
        user1.setFirstName("Alice");
        user1.setLastName("A");
        user1.setEmail("alice@test.local");
        user1.setPasswordHash("hash");
        user1.setRole(com.ecommerce.security.entity.Role.CUSTOMER);
        user1.setActive(true);
        user1 = userRepository.save(user1);

        user2 = new User();
        user2.setFirstName("Bob");
        user2.setLastName("B");
        user2.setEmail("bob@test.local");
        user2.setPasswordHash("hash");
        user2.setRole(com.ecommerce.security.entity.Role.CUSTOMER);
        user2.setActive(true);
        user2 = userRepository.save(user2);

        com.ecommerce.security.entity.Category category = new com.ecommerce.security.entity.Category();
        category.setName("Electronics");
        category = categoryRepository.save(category);

        product = new Product();
        product.setName("Prod");
        product.setPrice(new BigDecimal("100000"));
        product.setStockQuantity(10);
        product.setCategory(category);
        product = productRepository.save(product);

        CartItem cartItem = new CartItem();
        cartItem.setUser(user1);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItemRepository.save(cartItem);

        orderUser1 = new Order();
        orderUser1.setUser(user1);
        orderUser1.setOrderNumber("ORD-123");
        orderUser1.setTotalAmount(new BigDecimal("200000"));
        orderUser1.setStatus(com.ecommerce.security.entity.OrderStatus.PENDING);
        orderUser1.setShippingStreet("123 St");
        orderUser1.setShippingCity("City");
        orderUser1.setShippingZip("12345");
        orderUser1.setShippingCountry("Country");
        orderUser1 = orderRepository.save(orderUser1);

        token1 = generateToken(user1);
        token2 = generateToken(user2);
        // No need to save category because we don't have a categoryRepository autowired, wait, product requires category
    }

    private String generateToken(User user) {
        CustomUserDetails userDetails = CustomUserDetails.build(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        return jwtUtils.generateJwtToken(authentication);
    }
}

