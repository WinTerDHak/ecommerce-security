package com.ecommerce.security.controller;

import com.ecommerce.security.dto.payment.PaymentUrlResponseDTO;
import com.ecommerce.security.security.CustomUserDetails;
import com.ecommerce.security.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/orders/{orderId}/pay")
    public ResponseEntity<PaymentUrlResponseDTO> createPayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId,
            HttpServletRequest request) {
        
        String ipAddress = request.getRemoteAddr();
        PaymentUrlResponseDTO response = paymentService.createPayment(userDetails.getId(), orderId, ipAddress);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/vnpay/return")
    public ResponseEntity<String> vnpayReturn(@RequestParam Map<String, String> params) {
        boolean isValid = paymentService.verifyReturn(params);
        if (isValid) {
            return ResponseEntity.ok("Payment successful or processed correctly (check order status)");
        } else {
            return ResponseEntity.badRequest().body("Payment failed or invalid signature");
        }
    }

    @GetMapping("/vnpay/ipn")
    public ResponseEntity<Map<String, String>> vnpayIpn(@RequestParam Map<String, String> params) {
        Map<String, String> response = paymentService.processIpn(params);
        return ResponseEntity.ok(response);
    }
}
