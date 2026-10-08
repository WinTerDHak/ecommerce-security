package com.ecommerce.security.dto.payment;

public class PaymentUrlResponseDTO {
    private String paymentUrl;

    public PaymentUrlResponseDTO(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public String getPaymentUrl() { return paymentUrl; }
    public void setPaymentUrl(String paymentUrl) { this.paymentUrl = paymentUrl; }
}
