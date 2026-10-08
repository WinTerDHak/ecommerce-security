package com.ecommerce.security.controller;

import com.ecommerce.security.entity.Order;
import com.ecommerce.security.entity.OrderStatus;
import com.ecommerce.security.entity.Payment;
import com.ecommerce.security.entity.PaymentStatus;
import com.ecommerce.security.entity.User;
import com.ecommerce.security.repository.OrderRepository;
import com.ecommerce.security.repository.PaymentRepository;
import com.ecommerce.security.repository.UserRepository;
import com.ecommerce.security.repository.AuditLogRepository;
import com.ecommerce.security.security.CustomUserDetails;
import com.ecommerce.security.util.HashHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
    "spring.sql.init.mode=never",
    "spring.datasource.url=jdbc:h2:mem:testdb-payment",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "vnpay.tmn-code=TESTTMN",
    "vnpay.hash-secret=TESTSECRETKEY12345678901234567890",
    "vnpay.url=http://sandbox.vnpayment.vn",
    "vnpay.return-url=http://localhost/return",
    "vnpay.ipn-url=http://localhost/ipn"
})
@AutoConfigureMockMvc
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private com.ecommerce.security.security.jwt.JwtUtils jwtUtils;

    private User user1;
    private User user2;
    private Order orderUser1;
    private String token1;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
paymentRepository.deleteAll();
        orderRepository.deleteAll();
        userRepository.deleteAll();

        user1 = new User();
        user1.setFirstName("Alice");
        user1.setLastName("A");
        user1.setEmail("alice@test.local");
        user1.setPasswordHash("hash");
        user1.setRole(com.ecommerce.security.entity.Role.CUSTOMER);
        user1.setActive(true);
        user1 = userRepository.save(user1);

        user2 = new User();
        user2.setFirstName("Bob");
        user2.setLastName("B");
        user2.setEmail("bob@test.local");
        user2.setPasswordHash("hash");
        user2.setRole(com.ecommerce.security.entity.Role.CUSTOMER);
        user2.setActive(true);
        user2 = userRepository.save(user2);

        orderUser1 = new Order();
        orderUser1.setUser(user1);
        orderUser1.setOrderNumber("ORD-123");
        orderUser1.setTotalAmount(new BigDecimal("500000"));
        orderUser1.setStatus(OrderStatus.PENDING);
        orderUser1.setShippingStreet("123 St");
        orderUser1.setShippingCity("City");
        orderUser1.setShippingZip("12345");
        orderUser1.setShippingCountry("Country");
        orderUser1 = orderRepository.save(orderUser1);

        CustomUserDetails userDetails = CustomUserDetails.build(user1);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        token1 = jwtUtils.generateJwtToken(authentication);
    }

    @Test
    void testCreatePayment_Unauthenticated_Rejected() throws Exception {
        mockMvc.perform(post("/api/payments/orders/" + orderUser1.getId() + "/pay"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreatePayment_Success() throws Exception {
        mockMvc.perform(post("/api/payments/orders/" + orderUser1.getId() + "/pay")
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentUrl").exists());
    }

    @Test
    void testCreatePayment_OtherUser_IDOR_Fails() throws Exception {
        CustomUserDetails userDetails = CustomUserDetails.build(user2);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        String token2 = jwtUtils.generateJwtToken(authentication);

        mockMvc.perform(post("/api/payments/orders/" + orderUser1.getId() + "/pay")
                .header("Authorization", "Bearer " + token2))
                .andExpect(status().isForbidden());
    }

    @Test
    void testIpn_Success_UpdatesOrder() throws Exception {
        Payment payment = new Payment();
        payment.setOrder(orderUser1);
        payment.setAmount(new BigDecimal("500000"));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentRef("TXN123");
        payment = paymentRepository.save(payment);

        String hashData = "vnp_Amount=50000000&vnp_ResponseCode=00&vnp_TxnRef=TXN123";
        String secureHash = HashHelper.hmacSHA512("TESTSECRETKEY12345678901234567890", hashData);

        mockMvc.perform(get("/api/payments/vnpay/ipn")
                .param("vnp_Amount", "50000000")
                .param("vnp_ResponseCode", "00")
                .param("vnp_TxnRef", "TXN123")
                .param("vnp_SecureHash", secureHash))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RspCode").value("00"));

        Order updatedOrder = orderRepository.findById(orderUser1.getId()).get();
        assertEquals(OrderStatus.PAID, updatedOrder.getStatus());
        assertEquals(PaymentStatus.SUCCESS, paymentRepository.findById(payment.getId()).get().getStatus());
    }

    @Test
    void testIpn_InvalidSignature_Rejected() throws Exception {
        Payment payment = new Payment();
        payment.setOrder(orderUser1);
        payment.setAmount(new BigDecimal("500000"));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentRef("TXN123");
        paymentRepository.save(payment);

        mockMvc.perform(get("/api/payments/vnpay/ipn")
                .param("vnp_Amount", "50000000")
                .param("vnp_ResponseCode", "00")
                .param("vnp_TxnRef", "TXN123")
                .param("vnp_SecureHash", "invalid_signature_here"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RspCode").value("97")); // Signature fail code

        Order updatedOrder = orderRepository.findById(orderUser1.getId()).get();
        assertEquals(OrderStatus.PENDING, updatedOrder.getStatus()); // Untouched
    }
}

