package com.ecommerce.security.controller;

import com.ecommerce.security.dto.product.ProductDTO;
import com.ecommerce.security.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAdminAllProducts());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO productDTO, Authentication authentication, HttpServletRequest request) {
        String adminEmail = authentication.getName();
        String ipAddress = request.getRemoteAddr();
        ProductDTO created = productService.createProduct(productDTO, adminEmail, ipAddress);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO productDTO, Authentication authentication, HttpServletRequest request) {
        String adminEmail = authentication.getName();
        String ipAddress = request.getRemoteAddr();
        ProductDTO updated = productService.updateProduct(id, productDTO, adminEmail, ipAddress);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateProduct(@PathVariable Long id, Authentication authentication, HttpServletRequest request) {
        String adminEmail = authentication.getName();
        String ipAddress = request.getRemoteAddr();
        productService.deactivateProduct(id, adminEmail, ipAddress);
        return ResponseEntity.noContent().build();
    }
}
