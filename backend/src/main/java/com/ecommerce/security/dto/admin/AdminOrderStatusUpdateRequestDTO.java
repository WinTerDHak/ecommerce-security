package com.ecommerce.security.dto.admin;

import jakarta.validation.constraints.NotBlank;

public class AdminOrderStatusUpdateRequestDTO {
    
    @NotBlank(message = "Status cannot be blank")
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
