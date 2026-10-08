package com.ecommerce.security.dto.product;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public class ProductDTO {
    private Long id;
    
    @NotBlank
    @Pattern(regexp = "^[^<>]*$", message = "XSS attempt detected")
    private String name;
    
    @Pattern(regexp = "^[^<>]*$", message = "XSS attempt detected")
    private String description;
    
    @NotNull
    @Min(0)
    private BigDecimal price;
    
    @NotNull
    @Min(0)
    private Integer stockQuantity;
    
    private Long categoryId;
    private String categoryName;
    private boolean active = true;

    public ProductDTO() {}

    public ProductDTO(Long id, String name, String description, BigDecimal price, Integer stockQuantity, Long categoryId, String categoryName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public ProductDTO(Long id, String name, String description, BigDecimal price, Integer stockQuantity, Long categoryId, String categoryName, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
