package com.ecommerce.security.service;

import com.ecommerce.security.config.VnpayConfig;
import com.ecommerce.security.dto.payment.PaymentUrlResponseDTO;
import com.ecommerce.security.entity.*;
import com.ecommerce.security.repository.OrderRepository;
import com.ecommerce.security.repository.PaymentRepository;
import com.ecommerce.security.util.HashHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final VnpayConfig vnpayConfig;
    private final AuditLogService auditLogService;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository,
                          VnpayConfig vnpayConfig, AuditLogService auditLogService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.vnpayConfig = vnpayConfig;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public PaymentUrlResponseDTO createPayment(Long userId, Long orderId, String ipAddress) {
        if (!vnpayConfig.isConfigured()) {
            throw new IllegalStateException("VNPAY is not configured. Payment cannot be processed.");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new SecurityException("Access denied: You do not own this order");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Order is not eligible for payment");
        }

        // Prevent duplicate payment creation if already SUCCESS
        Optional<Payment> existingPaymentOpt = paymentRepository.findByOrder_Id(orderId);
        if (existingPaymentOpt.isPresent() && existingPaymentOpt.get().getStatus() == PaymentStatus.SUCCESS) {
            throw new IllegalArgumentException("Order is already paid");
        }

        String paymentRef = existingPaymentOpt
                .map(Payment::getPaymentRef)
                .orElse(UUID.randomUUID().toString().replace("-", ""));

        Payment payment = existingPaymentOpt.orElse(new Payment());
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentRef(paymentRef);
        paymentRepository.save(payment);

        auditLogService.logEvent(userId, "PAYMENT_INITIATED", "Order", order.getId(), "Payment initiated for order " + order.getOrderNumber(), ipAddress);

        String paymentUrl = buildVnpayUrl(payment, ipAddress);
        return new PaymentUrlResponseDTO(paymentUrl);
    }

    private String buildVnpayUrl(Payment payment, String ipAddress) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_OrderInfo = "Payment for order " + payment.getOrder().getOrderNumber();
        String orderType = "other";
        String vnp_TxnRef = payment.getPaymentRef();
        String vnp_IpAddr = ipAddress != null ? ipAddress : "127.0.0.1";
        String vnp_TmnCode = vnpayConfig.getVnpayTmnCode();

        long amount = payment.getAmount().multiply(new BigDecimal(100)).longValue();
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnpayConfig.getVnpayReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        try {
            for (String fieldName : fieldNames) {
                String fieldValue = vnp_Params.get(fieldName);
                if (fieldValue != null && fieldValue.length() > 0) {
                    //Build hash data
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    //Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append('&');
                    hashData.append('&');
                }
            }
            query.setLength(query.length() - 1);
            hashData.setLength(hashData.length() - 1);

            String queryUrl = query.toString();
            String vnp_SecureHash = HashHelper.hmacSHA512(vnpayConfig.getVnpayHashSecret(), hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

            return vnpayConfig.getVnpayUrl() + "?" + queryUrl;
        } catch (Exception e) {
            throw new RuntimeException("Failed to build VNPAY URL", e);
        }
    }

    @Transactional
    public Map<String, String> processIpn(Map<String, String> params) {
        Map<String, String> response = new HashMap<>();
        
        if (!vnpayConfig.isConfigured()) {
            response.put("RspCode", "99");
            response.put("Message", "VNPAY is not configured");
            return response;
        }
        
        try {
            if (!verifySignature(params)) {
                response.put("RspCode", "97");
                response.put("Message", "Invalid signature");
                return response;
            }

            String txnRef = params.get("vnp_TxnRef");
            Optional<Payment> paymentOpt = paymentRepository.findByPaymentRef(txnRef);
            if (paymentOpt.isEmpty()) {
                response.put("RspCode", "01");
                response.put("Message", "Order not found");
                return response;
            }

            Payment payment = paymentOpt.get();
            long vnpAmount = Long.parseLong(params.get("vnp_Amount"));
            long expectedAmount = payment.getAmount().multiply(new BigDecimal(100)).longValue();

            if (vnpAmount != expectedAmount) {
                response.put("RspCode", "04");
                response.put("Message", "Invalid amount");
                return response;
            }

            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                response.put("RspCode", "02");
                response.put("Message", "Order already confirmed");
                return response;
            }

            String responseCode = params.get("vnp_ResponseCode");
            if ("00".equals(responseCode)) {
                payment.setStatus(PaymentStatus.SUCCESS);
                Order order = payment.getOrder();
                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);
                auditLogService.logEvent(order.getUser().getId(), "PAYMENT_SUCCESS", "Order", order.getId(), "VNPAY payment successful", "IPN");
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                auditLogService.logEvent(payment.getOrder().getUser().getId(), "PAYMENT_FAILED", "Order", payment.getOrder().getId(), "VNPAY payment failed with code " + responseCode, "IPN");
            }
            paymentRepository.save(payment);

            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");
            return response;

        } catch (Exception e) {
            response.put("RspCode", "99");
            response.put("Message", "Unknown error");
            return response;
        }
    }

    public boolean verifyReturn(Map<String, String> params) {
        if (!vnpayConfig.isConfigured()) {
            return false;
        }
        if (!verifySignature(params)) {
            return false;
        }
        String responseCode = params.get("vnp_ResponseCode");
        return "00".equals(responseCode);
    }

    private boolean verifySignature(Map<String, String> params) {
        String vnp_SecureHash = params.get("vnp_SecureHash");
        if (vnp_SecureHash == null) {
            return false;
        }

        Map<String, String> hashParams = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!entry.getKey().equals("vnp_SecureHash") && !entry.getKey().equals("vnp_SecureHashType")) {
                hashParams.put(entry.getKey(), entry.getValue());
            }
        }

        List<String> fieldNames = new ArrayList<>(hashParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        try {
            for (String fieldName : fieldNames) {
                String fieldValue = hashParams.get(fieldName);
                if (fieldValue != null && fieldValue.length() > 0) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    hashData.append('&');
                }
            }
            if (hashData.length() > 0) {
                hashData.setLength(hashData.length() - 1);
            }
            String computedHash = HashHelper.hmacSHA512(vnpayConfig.getVnpayHashSecret(), hashData.toString());
            return computedHash.equals(vnp_SecureHash);
        } catch (Exception e) {
            return false;
        }
    }
}
