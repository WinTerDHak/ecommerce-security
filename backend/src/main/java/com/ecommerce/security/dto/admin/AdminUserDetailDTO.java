package com.ecommerce.security.dto.admin;

public class AdminUserDetailDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String role;
    private boolean active;
    private String createdAt;
    private String updatedAt;
    private long orderCount;

    public AdminUserDetailDTO(Long id, String firstName, String lastName, String email, String phone, String role, boolean active, String createdAt, String updatedAt, long orderCount) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.orderCount = orderCount;
    }

    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public boolean isActive() { return active; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public long getOrderCount() { return orderCount; }
}
