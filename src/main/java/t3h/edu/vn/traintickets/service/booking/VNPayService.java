package t3h.edu.vn.traintickets.service.booking;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import t3h.edu.vn.traintickets.config.VnPayProperties;


import t3h.edu.vn.traintickets.entities.Order;
import t3h.edu.vn.traintickets.enums.OrderStatus;
import t3h.edu.vn.traintickets.repository.OrderRepository;
import t3h.edu.vn.traintickets.repository.TicketRepository;
import t3h.edu.vn.traintickets.service.OrderService;
import t3h.edu.vn.traintickets.service.pdf_qr.MailService;
import t3h.edu.vn.traintickets.service.pdf_qr.TicketPdfService;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VNPayService {

    @Autowired
    private OrderService orderService;
    @Autowired
    private TicketPdfService ticketPdfService;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private MailService mailService;

    private final OrderRepository orderRepository;
    private final VnPayProperties vnPayProperties;

    public VNPayService(OrderRepository orderRepository, VnPayProperties vnPayProperties) {
        this.orderRepository = orderRepository;
        this.vnPayProperties = vnPayProperties;
    }

    public String createVNPayPayment(Order order, HttpServletRequest request) {
        try {
            // === 1. Chuẩn bị dữ liệu ===
            String orderType = "billpayment";

            // Số tiền (VNPay yêu cầu nhân 100, không có số thập phân)
            long amount = order.getFinalAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .longValueExact();

            // Tạo mã giao dịch duy nhất
            String transCode = order.getId() + "_" + System.currentTimeMillis();
            order.setTransactionCode(transCode);
            orderRepository.save(order);

            String vnp_TxnRef = order.getTransactionCode();
            String vnp_IpAddr = getClientIp(request);
            String vnp_CreateDate = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String vnp_ExpireDate = LocalDateTime.now()
                    .plusMinutes(10)
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            // === 2. Build VNPay Parameters ===
            Map<String, String> vnp_Params = new TreeMap<>(); // TreeMap tự động sort
            vnp_Params.put("vnp_Version", "2.1.0");
            vnp_Params.put("vnp_Command", "pay");
            vnp_Params.put("vnp_TmnCode", vnPayProperties.getTmnCode());
            vnp_Params.put("vnp_Amount", String.valueOf(amount));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", "Thanh_toan_don_hang_" + vnp_TxnRef);
            vnp_Params.put("vnp_OrderType", orderType);
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", vnPayProperties.getReturnUrl());
//            vnp_Params.put("vnp_IpnUrl", vnPayProperties.getIpnUrl());
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

            // Optional: Thêm ngân hàng cụ thể
            // vnp_Params.put("vnp_BankCode", "NCB");

            // === 3. Debug Input ===
            System.out.println("\n========== VNPay Input Params ==========");
            vnp_Params.forEach((k, v) -> System.out.println(k + " = " + v));
            System.out.println("========================================\n");

            // === 4. Build HashData & Query String ===
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
                String fieldName = entry.getKey();
                String fieldValue = entry.getValue();

                if (fieldValue != null && !fieldValue.isEmpty()) {
                    //HashData:
                    if (hashData.length() > 0) {
                        hashData.append('&');
                    }
                    hashData.append(fieldName)
                            .append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));

                    //Query:
                    if (query.length() > 0) {
                        query.append('&');
                    }
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8))
                            .append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                }
            }

            // === 5. Generate Secure Hash ===
            String vnp_SecureHash = hmacSHA512(vnPayProperties.getHashSecret(), hashData.toString());

            // === 6. Append Hash to Query ===
            query.append("&vnp_SecureHash=").append(vnp_SecureHash);

            // === 7. Build Final URL ===
            String paymentUrl = vnPayProperties.getUrl() + "?" + query.toString();

            // === 8. Debug Output ===
            System.out.println("========== Hash Generation ==========");
            System.out.println("HashData: " + hashData);
            System.out.println("SecureHash: " + vnp_SecureHash);
            System.out.println("=====================================");
            System.out.println("\n========== Final Payment URL ==========");
            System.out.println(paymentUrl);
            System.out.println("========================================\n");

            return paymentUrl;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("❌ Lỗi khi tạo URL thanh toán VNPay: " + e.getMessage(), e);
        }
    }


    public int processVNPayResponse(HttpServletRequest request) {
        try {
            Map<String, String> fields = new HashMap<>();
            Enumeration<String> params = request.getParameterNames();

            while (params.hasMoreElements()) {
                String fieldName = params.nextElement();
                String fieldValue = request.getParameter(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    fields.put(fieldName, fieldValue);
                }
            }

            String secureHash = fields.remove("vnp_SecureHash");
            fields.remove("vnp_SecureHashType");

            // ✅ PHẢI URL-ENCODE VALUE
            String hashData = fields.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> e.getKey() + "=" +
                            URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));

            String calculatedHash =
                    hmacSHA512(vnPayProperties.getHashSecret(), hashData);

            System.out.println("VNPay HashData: " + hashData);
            System.out.println("VNPay SecureHash: " + secureHash);
            System.out.println("CalculatedHash: " + calculatedHash);

            if (!calculatedHash.equalsIgnoreCase(secureHash)) return -1;

            if (!"00".equals(fields.get("vnp_ResponseCode"))) return 0;
            if (!"00".equals(fields.get("vnp_TransactionStatus"))) return 0;

            Order order = orderRepository
                    .findByTransactionCode(fields.get("vnp_TxnRef"));

            if (order == null) return -1;

            long vnpAmount = Long.parseLong(fields.get("vnp_Amount"));
            long orderAmount = order.getFinalAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .longValueExact();

            if (vnpAmount != orderAmount) return -1;

            if (order.getStatus() == OrderStatus.PAID) return 1;

//            String email =
//                    orderRepository.findUserEmailByTransactionCode(
//                            fields.get("vnp_TxnRef")
//                    );
//            mailService.sendTicketsByOrder(
//                    order.getId(),
//                    email
//            );

            return 1;

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }


    private String hmacSHA512(String key, String data) throws Exception {
        Mac hmac512 = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmac512.init(secretKey);
        byte[] hash = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder result = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            result.append(String.format("%02x", b & 0xff));
        }
        return result.toString();
    }


    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-FORWARDED-FOR");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        // Fix cho localhost IPv6
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }
        return ip;
    }


    public boolean validateCallback(HttpServletRequest request) {
        try {
            Map<String, String> fields = new HashMap<>();
            for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
                String fieldName = params.nextElement();
                String fieldValue = request.getParameter(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    fields.put(fieldName, fieldValue);
                }
            }

            String vnp_SecureHash = fields.remove("vnp_SecureHash");
            fields.remove("vnp_SecureHashType");

            String hashData = fields.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> {
                        try {
                            return e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8);
                        } catch (Exception ex) {
                            return e.getKey() + "=" + e.getValue();
                        }
                    })
                    .collect(Collectors.joining("&"));

            String calculatedHash = hmacSHA512(vnPayProperties.getHashSecret(), hashData);
            return calculatedHash.equals(vnp_SecureHash);

        } catch (Exception e) {
            return false;
        }
    }
}