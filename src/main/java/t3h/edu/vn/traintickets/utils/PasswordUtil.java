package t3h.edu.vn.traintickets.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {

    public static boolean checkPassword(String rawPassword, String encodedPassword) {
        // Kiểm tra nếu là mật khẩu đã được mã hóa bằng BCrypt
        if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$")) {
            return new BCryptPasswordEncoder().matches(rawPassword, encodedPassword);
        }
        // Nếu là mật khẩu MD5
        return md5(rawPassword).equalsIgnoreCase(encodedPassword);
    }

    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));

            // Dùng StringBuilder để tạo chuỗi hex
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));  // %02x là chuẩn hex
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}

