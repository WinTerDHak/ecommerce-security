package com.ecommerce.security.service;

import com.ecommerce.security.dto.product.ProductDTO;
import com.ecommerce.security.entity.Category;
import com.ecommerce.security.entity.Product;
import com.ecommerce.security.entity.User;
import com.ecommerce.security.repository.CategoryRepository;
import com.ecommerce.security.repository.ProductRepository;
import com.ecommerce.security.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, UserRepository userRepository, AuditLogService auditLogService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public List<ProductDTO> getAllProducts() {
        return productRepository.findByActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getAdminAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> searchProducts(String keyword) {
        return productRepository.searchActiveProducts(keyword).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::mapToDTO)
                .orElse(null);
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO dto, String adminEmail, String ipAddress) {
        User admin = userRepository.findByEmail(adminEmail).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Admin not found"));
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Category not found"));

        Product p = new Product();
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setPrice(dto.getPrice());
        p.setStockQuantity(dto.getStockQuantity());
        p.setCategory(category);
        p.setActive(true);
        // imageUrl will be handled locally on frontend, so we can leave it null or set to a placeholder
        
        Product saved = productRepository.save(p);
        
        auditLogService.logEvent(admin.getId(), "ADMIN_PRODUCT_CREATE", "PRODUCT", saved.getId(), "Created product: " + saved.getName(), ipAddress);
        return mapToDTO(saved);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO dto, String adminEmail, String ipAddress) {
        User admin = userRepository.findByEmail(adminEmail).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Admin not found"));
        Product p = productRepository.findById(id).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Product not found"));
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Category not found"));

        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setPrice(dto.getPrice());
        p.setStockQuantity(dto.getStockQuantity());
        p.setCategory(category);
        if(dto.isActive() != p.isActive()) {
             p.setActive(dto.isActive());
        }

        Product saved = productRepository.save(p);
        auditLogService.logEvent(admin.getId(), "ADMIN_PRODUCT_UPDATE", "PRODUCT", saved.getId(), "Updated product: " + saved.getName(), ipAddress);
        return mapToDTO(saved);
    }

    @Transactional
    public void deactivateProduct(Long id, String adminEmail, String ipAddress) {
        User admin = userRepository.findByEmail(adminEmail).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Admin not found"));
        Product p = productRepository.findById(id).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Product not found"));
        p.setActive(false);
        productRepository.save(p);
        auditLogService.logEvent(admin.getId(), "ADMIN_PRODUCT_DEACTIVATE", "PRODUCT", p.getId(), "Deactivated product: " + p.getName(), ipAddress);
    }

    private ProductDTO mapToDTO(Product product) {
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.isActive()
        );
    }
}
