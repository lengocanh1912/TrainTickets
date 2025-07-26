package t3h.edu.vn.traintickets.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import t3h.edu.vn.traintickets.config.VnPayProperties;
import t3h.edu.vn.traintickets.entities.Order;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class VNPayService {


    private final VnPayProperties vnPayProperties;

    public VNPayService(VnPayProperties vnPayProperties) {
        this.vnPayProperties = vnPayProperties;
    }

    public String createVNPayPayment(Order order, HttpServletRequest request) {
        try {
            String orderType = "billpayment";
            long amount = (long) (order.getTotalAmount() * 100); // nhân 100 để VNPay xử lý tiền theo đơn vị nhỏ nhất

            String vnp_TxnRef = String.valueOf(order.getId());
            String vnp_IpAddr = request.getRemoteAddr();
            String vnp_TmnCode = vnPayProperties.getTmnCode();

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", "2.1.0");
            vnp_Params.put("vnp_Command", "pay");
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(amount));
            vnp_Params.put("vnp_CurrCode", "VND");
//            vnp_Params.put("vnp_BankCode", "NCB");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang: " + vnp_TxnRef);
            vnp_Params.put("vnp_OrderType", orderType);
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", vnPayProperties.getReturnUrl());
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

//            vnp_Params.put("vnp_Version", "2.1.0");
//            vnp_Params.put("vnp_Command", "pay");
//            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
//            vnp_Params.put("vnp_Amount", String.valueOf(order.getTotalAmount() * 100));
//            vnp_Params.put("vnp_CurrCode", "VND");
//            vnp_Params.put("vnp_TxnRef", String.valueOf(order.getId()));
//            vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang: " + order.getId());
//            vnp_Params.put("vnp_Locale", "vn");
//            vnp_Params.put("vnp_ReturnUrl", vnPayProperties.getReturnUrl());
//            vnp_Params.put("vnp_IpAddr", getIpAddress(request));
//            vnp_Params.put("vnp_CreateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
//            vnp_Params.put("vnp_BankCode", "VNPAYQR"); // 💥 BẮT BUỘC để show mã QR


            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            vnp_Params.put("vnp_CreateDate", now.format(formatter));

            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            for (String fieldName : fieldNames) {
                String value = vnp_Params.get(fieldName);
                if (value != null && !value.isEmpty()) {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII)).append('&');
                    query.append(fieldName).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII)).append('&');
                }
            }

            // Bỏ ký tự & cuối cùng
            if (hashData.length() > 0) hashData.setLength(hashData.length() - 1);
            if (query.length() > 0) query.setLength(query.length() - 1);

            String vnp_SecureHash = hmacSHA512(vnPayProperties.getHashSecret(), hashData.toString());
            query.append("&vnp_SecureHash=").append(vnp_SecureHash);

            return vnPayProperties.getUrl() + "?" + query.toString();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo URL thanh toán VNPay", e);
        }
    }

//    public String createVNPayPayment(Order order, HttpServletRequest request) {
//        try {
//            String vnp_TxnRef = String.valueOf(order.getId());
//            String vnp_IpAddr = getIpAddress(request);
//            String vnp_TmnCode = vnPayProperties.getTmnCode();
//            String vnp_ReturnUrl = vnPayProperties.getReturnUrl();
//            String orderType = "billpayment";
//            long amount = (long) (order.getTotalAmount() * 100); // VNPay dùng đơn vị nhỏ nhất (VND * 100)
//
//            // 1. Tạo map chứa tham số
//            Map<String, String> vnp_Params = new HashMap<>();
//            vnp_Params.put("vnp_Version", "2.1.0");
//            vnp_Params.put("vnp_Command", "pay");
//            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
//            vnp_Params.put("vnp_Amount", String.valueOf(amount));
//            vnp_Params.put("vnp_CurrCode", "VND");
//            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
//            vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang #" + vnp_TxnRef);
//            vnp_Params.put("vnp_OrderType", orderType);
//            vnp_Params.put("vnp_Locale", "vn");
//            vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
//            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
//            vnp_Params.put("vnp_BankCode", "VNPAYQR"); // 🔥 BẮT BUỘC để tạo mã QR
//
//            String createDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
//            vnp_Params.put("vnp_CreateDate", createDate);
//
//            // 2. Sắp xếp và ký hash
//            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
//            Collections.sort(fieldNames);
//
//            StringBuilder hashData = new StringBuilder();
//            StringBuilder query = new StringBuilder();
//
//            for (String field : fieldNames) {
//                String value = vnp_Params.get(field);
//                if (value != null && !value.isEmpty()) {
//                    hashData.append(field).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII)).append('&');
//                    query.append(field).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII)).append('&');
//                }
//            }
//
//            // Bỏ dấu & cuối
//            if (hashData.length() > 0) hashData.setLength(hashData.length() - 1);
//            if (query.length() > 0) query.setLength(query.length() - 1);
//
//            String secureHash = hmacSHA512(vnPayProperties.getHashSecret(), hashData.toString());
//            query.append("&vnp_SecureHash=").append(secureHash);
//
//            String paymentUrl = vnPayProperties.getUrl() + "?" + query;
//
//            // DEBUG LOG
//            System.out.println("🔍 [DEBUG] VNPay URL: " + paymentUrl);
//
//            return paymentUrl;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException("❌ Lỗi khi tạo URL thanh toán VNPay", e);
//        }
//    }

    private String hmacSHA512(String key, String data) throws Exception {
        javax.crypto.Mac hmac512 = javax.crypto.Mac.getInstance("HmacSHA512");
        javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmac512.init(secretKey);
        byte[] hash = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    public int processVNPayResponse(HttpServletRequest request) {
        Map<String, String> vnpParams = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();

        while (paramNames.hasMoreElements()) {
            String param = paramNames.nextElement();
            if (param.startsWith("vnp_")) {
                vnpParams.put(param, request.getParameter(param));
            }
        }

        String responseCode = vnpParams.get("vnp_ResponseCode");
        if ("00".equals(responseCode)) {
            // Giao dịch thành công
            return 1;
        } else {
            // Giao dịch thất bại
            return 0;
        }
    }


    private String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

//    public String createVNPayUrl(Long amount, String orderInfo, String returnUrl) {
//        String vnp_Version = "2.1.0";
//        String vnp_Command = "pay";
//        String orderType = "billpayment";
//        String vnp_TxnRef = String.valueOf(System.currentTimeMillis());
//        String vnp_IpAddr = "127.0.0.1"; // Hoặc lấy IP người dùng thật
//
//        String vnp_TmnCode = "VNPAYDEMO"; // TMN code test
//        String vnp_HashSecret = "SECRETKEY"; // Thay bằng key thật
//        String vnp_Url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
//
//        Map<String, String> vnp_Params = new HashMap<>();
//        vnp_Params.put("vnp_Version", vnp_Version);
//        vnp_Params.put("vnp_Command", vnp_Command);
//        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
//        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100)); // nhân 100 vì VNPAY dùng đơn vị nhỏ nhất
//        vnp_Params.put("vnp_CurrCode", "VND");
//        // KHÔNG TRUYỀN vnp_BankCode => sẽ hiện giao diện chọn phương thức
//        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
//        vnp_Params.put("vnp_OrderInfo", orderInfo);
//        vnp_Params.put("vnp_OrderType", orderType);
//        vnp_Params.put("vnp_Locale", "vn");
//        vnp_Params.put("vnp_ReturnUrl", returnUrl);
//        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
//
//        // Ngày giờ hiện tại định dạng yyyyMMddHHmmss
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
//        String vnp_CreateDate = LocalDateTime.now().format(formatter);
//        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
//
//        // Bước 1: sắp xếp map theo key tăng dần
//        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
//        Collections.sort(fieldNames);
//
//        StringBuilder hashData = new StringBuilder();
//        StringBuilder query = new StringBuilder();
//        for (String fieldName : fieldNames) {
//            String value = vnp_Params.get(fieldName);
//            if ((value != null) && (value.length() > 0)) {
//                hashData.append(fieldName).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII)).append('&');
//                query.append(fieldName).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII)).append('&');
//            }
//        }
//        // Xoá ký tự `&` cuối cùng
//        if (hashData.length() > 0) hashData.setLength(hashData.length() - 1);
//        if (query.length() > 0) query.setLength(query.length() - 1);
//
//        // Bước 2: tạo chuỗi hash sử dụng SHA256
//        String secureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
//
//        // Bước 3: tạo URL
//        String paymentUrl = vnp_Url + "?" + query + "&vnp_SecureHash=" + secureHash;
//
//        return paymentUrl;
//    }

//    public static String hmacSHA512(String key, String data) {
//        try {
//            Mac hmac512 = Mac.getInstance("HmacSHA512");
//            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
//            hmac512.init(secretKey);
//            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
//            return bytesToHex(bytes);
//        } catch (Exception e) {
//            throw new RuntimeException("Lỗi khi tạo HMAC SHA512", e);
//        }
//    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }


}
