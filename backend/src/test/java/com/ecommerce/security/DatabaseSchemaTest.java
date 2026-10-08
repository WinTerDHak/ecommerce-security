package com.ecommerce.security;

import com.ecommerce.security.entity.CartItem;
import com.ecommerce.security.entity.Category;
import com.ecommerce.security.entity.Product;
import com.ecommerce.security.entity.User;
import com.ecommerce.security.entity.Role;
import com.ecommerce.security.repository.CartItemRepository;
import com.ecommerce.security.repository.CategoryRepository;
import com.ecommerce.security.repository.ProductRepository;
import com.ecommerce.security.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase(replace = org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE)
class DatabaseSchemaTest {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CartItemRepository cartItemRepository;

    @Test
    void allEntitiesMapped() {
        assertNotNull(userRepository);
        assertNotNull(cartItemRepository);
    }

    @Test
    void cartItemUniqueConstraintWorks() {
        User user = new User();
        user.setEmail("test@ecommerce.local");
        user.setPasswordHash("hash");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(Role.CUSTOMER);
        user = userRepository.saveAndFlush(user);

        Category cat = new Category();
        cat.setName("Cat");
        cat = categoryRepository.saveAndFlush(cat);

        Product p = new Product();
        p.setName("Prod");
        p.setPrice(BigDecimal.TEN);
        p.setStockQuantity(10);
        p.setCategory(cat);
        p = productRepository.saveAndFlush(p);

        CartItem item1 = new CartItem();
        item1.setUser(user);
        item1.setProduct(p);
        item1.setQuantity(1);
        cartItemRepository.saveAndFlush(item1);

        CartItem item2 = new CartItem();
        item2.setUser(user);
        item2.setProduct(p);
        item2.setQuantity(2);

        assertThrows(DataIntegrityViolationException.class, () -> {
            cartItemRepository.saveAndFlush(item2);
        });
    }
}

