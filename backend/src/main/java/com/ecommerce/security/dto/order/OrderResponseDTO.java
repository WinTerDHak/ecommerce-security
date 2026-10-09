package com.ecommerce.security.dto.order;

import com.ecommerce.security.entity.OrderStatus;
import java.math.BigDecimal;

public class OrderResponseDTO {
    private Long id;
    private String orderNumber;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String createdAt;
    private String shippingStreet;
    private String shippingCity;
    private String shippingZip;
    private String shippingCountry;

    public OrderResponseDTO(Long id, String orderNumber, BigDecimal totalAmount, OrderStatus status, String createdAt, 
                            String shippingStreet, String shippingCity, String shippingZip, String shippingCountry) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.shippingStreet = shippingStreet;
        this.shippingCity = shippingCity;
        this.shippingZip = shippingZip;
        this.shippingCountry = shippingCountry;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public String getShippingStreet() { return shippingStreet; }
    public void setShippingStreet(String shippingStreet) { this.shippingStreet = shippingStreet; }
    public String getShippingCity() { return shippingCity; }
    public void setShippingCity(String shippingCity) { this.shippingCity = shippingCity; }
    public String getShippingZip() { return shippingZip; }
    public void setShippingZip(String shippingZip) { this.shippingZip = shippingZip; }
    public String getShippingCountry() { return shippingCountry; }
    public void setShippingCountry(String shippingCountry) { this.shippingCountry = shippingCountry; }
}
