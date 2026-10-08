package com.ecommerce.security.dto.admin;

import jakarta.validation.constraints.NotNull;

public class AdminUserStatusUpdateRequestDTO {

    @NotNull(message = "Active status must be provided")
    private Boolean active;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
