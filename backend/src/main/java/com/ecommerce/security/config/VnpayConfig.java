package com.ecommerce.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
public class VnpayConfig {

    @Value("${vnpay.tmn-code}")
    private String vnpayTmnCode;

    @Value("${vnpay.hash-secret}")
    private String vnpayHashSecret;

    @Value("${vnpay.url}")
    private String vnpayUrl;

    @Value("${vnpay.return-url}")
    private String vnpayReturnUrl;

    public boolean isConfigured() {
        return vnpayTmnCode != null && !vnpayTmnCode.isEmpty() && !"UNCONFIGURED".equals(vnpayTmnCode) && !vnpayTmnCode.contains("${");
    }

    public String getVnpayTmnCode() {
        return vnpayTmnCode;
    }

    public String getVnpayHashSecret() {
        return vnpayHashSecret;
    }

    public String getVnpayUrl() {
        return vnpayUrl;
    }

    public String getVnpayReturnUrl() {
        return vnpayReturnUrl;
    }
}

