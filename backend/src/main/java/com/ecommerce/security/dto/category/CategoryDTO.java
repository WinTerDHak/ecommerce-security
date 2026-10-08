package com.ecommerce.security.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CategoryDTO {
    private Long id;
    
    @NotBlank
    @Pattern(regexp = "^[^<>]*$", message = "XSS attempt detected")
    private String name;
    
    @Pattern(regexp = "^[^<>]*$", message = "XSS attempt detected")
    private String description;

    public CategoryDTO() {}

    public CategoryDTO(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
