package com.ecommerce.security.dto.admin;

public class AdminUserListDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private boolean active;
    private String createdAt;

    public AdminUserListDTO(Long id, String name, String email, String role, boolean active, String createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public boolean isActive() { return active; }
    public String getCreatedAt() { return createdAt; }
}
