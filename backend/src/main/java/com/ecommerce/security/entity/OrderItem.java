package com.ecommerce.security.entity;

import jakarta.persistence.*;



import java.math.BigDecimal;

@Entity
@Table(name = "order_items")


public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String productName; // Snapshot

    @Column(nullable = false)
    private BigDecimal unitPrice; // Snapshot

    @Column(nullable = false)
    private Integer quantity;

    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public Order getOrder() { return this.order; }
    public void setOrder(Order order) { this.order = order; }
    public Product getProduct() { return this.product; }
    public void setProduct(Product product) { this.product = product; }
    public String getProductName() { return this.productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public BigDecimal getUnitPrice() { return this.unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public Integer getQuantity() { return this.quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
