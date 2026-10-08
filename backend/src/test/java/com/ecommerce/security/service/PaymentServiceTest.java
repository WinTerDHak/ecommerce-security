package com.ecommerce.security.service;

import com.ecommerce.security.config.VnpayConfig;
import com.ecommerce.security.dto.payment.PaymentUrlResponseDTO;
import com.ecommerce.security.entity.Order;
import com.ecommerce.security.entity.OrderStatus;
import com.ecommerce.security.entity.Payment;
import com.ecommerce.security.entity.PaymentStatus;
import com.ecommerce.security.entity.User;
import com.ecommerce.security.repository.OrderRepository;
import com.ecommerce.security.repository.PaymentRepository;
import com.ecommerce.security.util.HashHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    private AuditLogService auditLogService;
    private VnpayConfig vnpayConfig;
    private PaymentService paymentService;

    private User user;
    private Order order;
    private Payment payment;
    private String testSecret = "TEST_HASH_SECRET_VERY_LONG_STRING_1234567890";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);

        order = new Order();
        order.setId(10L);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("100000"));
        order.setOrderNumber("ORD123");

        payment = new Payment();
        payment.setId(100L);
        payment.setOrder(order);
        payment.setAmount(new BigDecimal("100000"));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentRef("REF123");

        auditLogService = new AuditLogService(null, null) {
            @Override
            public void logEvent(Long userId, String action, String entityType, Long entityId, String details, String ipAddress) {
                // Fake log
            }
        };

        vnpayConfig = new VnpayConfig() {
            @Override public boolean isConfigured() { return true; }
            @Override public String getVnpayTmnCode() { return "TESTCODE"; }
            @Override public String getVnpayHashSecret() { return testSecret; }
            @Override public String getVnpayUrl() { return "http://sandbox.vnpayment.vn/paymentv2/vpcpay.html"; }
            @Override public String getVnpayReturnUrl() { return "http://localhost:8081/api/payments/vnpay/return"; }
        };
        paymentService = new PaymentService(paymentRepository, orderRepository, vnpayConfig, auditLogService);
    }

    @Test
    void testCreatePayment_Success() {
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrder_Id(10L)).thenReturn(Optional.empty());

        PaymentUrlResponseDTO response = paymentService.createPayment(1L, 10L, "127.0.0.1");

        assertNotNull(response.getPaymentUrl());
        assertTrue(response.getPaymentUrl().contains("vnp_Amount=10000000")); // 100.00 * 100
        assertTrue(response.getPaymentUrl().contains("vnp_TmnCode=TESTCODE"));
        
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testCreatePayment_OtherUser_IDOR() {
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        SecurityException exception = assertThrows(SecurityException.class, () -> {
            paymentService.createPayment(2L, 10L, "127.0.0.1"); // user 2 tries to pay user 1's order
        });
        assertEquals("Access denied: You do not own this order", exception.getMessage());
    }

    @Test
    void testProcessIpn_ValidSignature_Success() throws Exception {
        when(paymentRepository.findByPaymentRef("REF123")).thenReturn(Optional.of(payment));

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "10000000"); // 100.00 * 100
        params.put("vnp_TxnRef", "REF123");
        params.put("vnp_ResponseCode", "00");

        // Compute valid hash
        String hashData = "vnp_Amount=10000000&vnp_ResponseCode=00&vnp_TxnRef=REF123";
        String secureHash = HashHelper.hmacSHA512(testSecret, hashData);
        params.put("vnp_SecureHash", secureHash);

        Map<String, String> response = paymentService.processIpn(params);

        assertEquals("00", response.get("RspCode"));
        assertEquals("Confirm Success", response.get("Message"));
        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals(OrderStatus.PAID, order.getStatus());
        
        verify(paymentRepository, times(1)).save(payment);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void testProcessIpn_InvalidSignature() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "10000000");
        params.put("vnp_TxnRef", "REF123");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_SecureHash", "invalid_hash_data");

        Map<String, String> response = paymentService.processIpn(params);

        assertEquals("97", response.get("RspCode"));
        assertEquals("Invalid signature", response.get("Message"));
    }

    @Test
    void testProcessIpn_InvalidAmount() {
        when(paymentRepository.findByPaymentRef("REF123")).thenReturn(Optional.of(payment));

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "999900"); // Wrong amount, should be 10000
        params.put("vnp_TxnRef", "REF123");
        params.put("vnp_ResponseCode", "00");

        String hashData = "vnp_Amount=999900&vnp_ResponseCode=00&vnp_TxnRef=REF123";
        String secureHash = HashHelper.hmacSHA512(testSecret, hashData);
        params.put("vnp_SecureHash", secureHash);

        Map<String, String> response = paymentService.processIpn(params);

        assertEquals("04", response.get("RspCode"));
        assertEquals("Invalid amount", response.get("Message"));
    }
}

