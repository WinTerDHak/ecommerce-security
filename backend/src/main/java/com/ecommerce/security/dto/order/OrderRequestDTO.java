package com.ecommerce.security.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class OrderRequestDTO {
    
    @NotBlank(message = "Shipping street is required")
    @Pattern(regexp = "^[^<>]*$", message = "XSS attempt detected")
    private String shippingStreet;

    @NotBlank(message = "Shipping city is required")
    @Pattern(regexp = "^[^<>]*$", message = "XSS attempt detected")
    private String shippingCity;

    @NotBlank(message = "Shipping zip is required")
    @Pattern(regexp = "^[^<>]*$", message = "XSS attempt detected")
    private String shippingZip;

    @NotBlank(message = "Shipping country is required")
    @Pattern(regexp = "^[^<>]*$", message = "XSS attempt detected")
    private String shippingCountry;

    public String getShippingStreet() { return shippingStreet; }
    public void setShippingStreet(String shippingStreet) { this.shippingStreet = shippingStreet; }
    public String getShippingCity() { return shippingCity; }
    public void setShippingCity(String shippingCity) { this.shippingCity = shippingCity; }
    public String getShippingZip() { return shippingZip; }
    public void setShippingZip(String shippingZip) { this.shippingZip = shippingZip; }
    public String getShippingCountry() { return shippingCountry; }
    public void setShippingCountry(String shippingCountry) { this.shippingCountry = shippingCountry; }
}
