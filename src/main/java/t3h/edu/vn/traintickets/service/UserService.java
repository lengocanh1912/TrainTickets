package t3h.edu.vn.traintickets.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import t3h.edu.vn.traintickets.dto.UserCreateDto;
import t3h.edu.vn.traintickets.dto.UserPasswordDto;
import t3h.edu.vn.traintickets.dto.UserUpdateDto;
import t3h.edu.vn.traintickets.entities.User;
import t3h.edu.vn.traintickets.repository.UserRepository;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static t3h.edu.vn.traintickets.utils.PasswordUtil.md5;

@Service
@RequiredArgsConstructor
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MailService mailService;

    //
    public Page paging(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<User> users = userRepository.findAll(pageable);
        return users;
    }

    public User findByUserName(String userName) throws Exception {
        return  userRepository.findByUsername(userName);
    }

    public Long getUserIdFromPrincipal(Principal principal) {
        if (principal == null) return null;

        String username = principal.getName(); // lấy username từ security context
        User user = userRepository.findByUsername(username);
        return user != null ? user.getId() : null;
    }

    public List<User> getAll() {
        List<User> userModels = null;
        try {
            userModels = userRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return userModels;
    }

    public void createUser(UserCreateDto userCreateDto) {
        try {
            // Kiểm tra mật khẩu nhập lại
            if (!userCreateDto.getPassword().equals(userCreateDto.getRePassword())) {
                throw new IllegalArgumentException("Mật khẩu nhập lại không khớp.");
            }

            // Tạo đối tượng User
            User user = new User();

            // Gán từng field cụ thể thay vì BeanUtils để rõ ràng & tránh lỗi field tên khác nhau
            user.setUsername(userCreateDto.getUsername());
            user.setPassword(passwordEncoder.encode(userCreateDto.getPassword())); // mã hóa bằng BCrypt
            user.setFullname(userCreateDto.getFullName());
            user.setEmail(userCreateDto.getEmail());
            user.setPhoneNumber(userCreateDto.getPhoneNumber());
            user.setGender(userCreateDto.getGender() != null ? userCreateDto.getGender() : false);
            user.setAddress(userCreateDto.getAddress());
            user.setRole(userCreateDto.getRole());

            user.setStatus(1); // mặc định active
            user.setCreatedAt(Instant.now());

            userRepository.save(user);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo người dùng: " + e.getMessage(), e);
        }
    }


    public User findById(Long id) {
        User user = null;
        try {
            user = userRepository.findById(id).orElse(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    public void updateUser(UserUpdateDto userUpdateDto) {
        try {
            User userModel = userRepository.findById(userUpdateDto.getId())
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            // Chỉ cập nhật các field thay đổi
            userModel.setUsername(userUpdateDto.getUsername());
            userModel.setFullname(userUpdateDto.getFullname());
            userModel.setEmail(userUpdateDto.getEmail());
            userModel.setPhoneNumber(userUpdateDto.getPhoneNumber());
            userModel.setGender(userUpdateDto.getGender());
            userModel.setAddress(userUpdateDto.getAddress());
            userModel.setRole(userUpdateDto.getRole());
            userModel.setUpdatedAt(Instant.now());

            userRepository.save(userModel);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi cập nhật user: " + e.getMessage(), e);
        }
    }

    public void deleteById(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            userRepository.deleteById(id);
        } else {
            throw new RuntimeException("Không tìm thấy người dùng với ID = " + id);
        }
    }

    public List<User> searchUsers(String keyword) {
        List<User> userlist = null ;
        try {
            userlist = userRepository.findByUsernameOrFullnameOrEmailOrPhoneNumber(keyword);
        }
        catch (Exception e ){
            throw new RuntimeException(e);
        }
        return userlist;
    }

    public void updatePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        user.setPassword(passwordEncoder.encode(newPassword)); // tự mã hóa bằng bcrypto

        userRepository.save(user);
    }

    public void register(UserCreateDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }

        if (!dto.getPassword().equals(dto.getRePassword())) {
            throw new IllegalArgumentException("Mật khẩu không khớp");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setFullname(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setGender(dto.getGender());
        user.setAddress(dto.getAddress());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole("CUSTOMER"); // hoặc tùy theo context
        user.setStatus(1); // nếu có trạng thái kích hoạt
        user.setCreatedAt(Instant.now()); // 👈 Thêm dòng này

        userRepository.save(user);
    }

    public void sendResetPasswordLink(String email) {

        User user = userRepository.findByEmail(email);
        if (user == null) return; // tránh lộ email

        String token = UUID.randomUUID().toString();

        user.setResetToken(token);
        user.setResetTokenExpiry(
                Instant.now().plus(15, ChronoUnit.MINUTES)
        );

        userRepository.save(user);

        String link =
                "http://localhost:8081/trainticket/reset-password?token=" + token;

        mailService.sendSimpleMail(
                email,
                "Đặt lại mật khẩu",
                """
                Bạn đã yêu cầu đặt lại mật khẩu.
    
                Click link dưới đây để đặt lại mật khẩu (hiệu lực 15 phút):
                %s
    
                Nếu không phải bạn, hãy bỏ qua email này.
                """.formatted(link)
        );
    }

    public void resetPassword(String token, String password, String confirmPassword) {
        User user = userRepository.findByResetToken(token);
        if (user == null || user.getResetTokenExpiry().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Token không hợp lệ hoặc đã hết hạn");
        }

        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Mật khẩu không khớp");
        }

        user.setPassword(passwordEncoder.encode(password));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhoneNumber(phone);
    }

    public void updateAvatar(User user, MultipartFile file) {
        try {
            if (file.isEmpty()) return;

            // thư mục lưu
            String uploadDir = "uploads/avatars/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            // tên file
            String filename = "user_" + user.getId() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir + filename);

            Files.write(path, file.getBytes());

            // lưu path vào DB
            user.setAvatar("/" + uploadDir + filename);
            user.setUpdatedAt(Instant.now());

            userRepository.save(user);

        } catch (Exception e) {
            throw new RuntimeException("Upload avatar failed", e);
        }
    }

    public void updateProfile(UserUpdateDto dto) {

        User user = userRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // Email
        if (!user.getEmail().equals(dto.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Email đã được sử dụng");
            }
            user.setEmail(dto.getEmail());
        }

        user.setFullname(dto.getFullname());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setAddress(dto.getAddress());

        // ===== VALIDATE BIRTHDAY =====
        if (dto.getBirthday() != null && !dto.getBirthday().isBlank()) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            LocalDate birthday;
            try {
                birthday = LocalDate.parse(dto.getBirthday(), formatter);
            } catch (DateTimeParseException e) {
                throw new RuntimeException("Ngày sinh phải có định dạng dd/MM/yyyy");
            }

            if (birthday.isAfter(LocalDate.now())) {
                throw new RuntimeException("Ngày sinh không hợp lệ");
            }

            if (birthday.isBefore(LocalDate.of(1900, 1, 1))) {
                throw new RuntimeException("Ngày sinh quá xa");
            }

            user.setBirthday(birthday);
        }
        // =============================

        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }

    public void changePassword(
            String username,
            String currentPassword,
            String newPassword,
            String confirmPassword
    ) {

        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User không tồn tại");
        }


        // 1. Check mật khẩu hiện tại
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng");
        }

        // 2. Check mật khẩu mới
        if (newPassword.length() < 6) {
            throw new RuntimeException("Mật khẩu mới tối thiểu 6 ký tự");
        }

        // 3. Check confirm
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Mật khẩu nhập lại không khớp");
        }

        // 4. Không cho trùng mật khẩu cũ
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("Mật khẩu mới không được trùng mật khẩu cũ");
        }

        // 5. Encode & save
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(Instant.now());

        userRepository.save(user);
    }


    public void sendOtpToCurrentEmail(String username, String currentEmail) {

        User user = userRepository.findByUsername(username);

        // 🔐 Bảo mật: đảm bảo đúng email đang dùng
        if (!user.getEmail().equalsIgnoreCase(currentEmail)) {
            throw new RuntimeException("Email không khớp với tài khoản");
        }

        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);

        user.setEmailOtp(otp);
        user.setEmailOtpExpiry(Instant.now().plus(5, ChronoUnit.MINUTES));
        userRepository.save(user);

        mailService.sendSimpleMail(
                user.getEmail(), // 🔥 GỬI VỀ EMAIL HIỆN TẠI
                "Xác minh tài khoản",
                "Mã OTP của bạn là: " + otp + " (hết hạn sau 5 phút)"
        );
    }

    @Transactional
    public void confirmEmailOTP(String username, String otp) {

        User user = userRepository.findByUsername(username);

        if (user.getEmailOtp() == null) {
            throw new RuntimeException("Chưa yêu cầu OTP");
        }

        if (user.getEmailOtpExpiry().isBefore(Instant.now())) {
            throw new RuntimeException("OTP đã hết hạn");
        }

        if (!user.getEmailOtp().equals(otp.trim())) {
            throw new RuntimeException("OTP không đúng");
        }

        if (user.getPendingEmail() == null) {
            throw new RuntimeException("Không có email chờ xác nhận");
        }

        user.setPendingEmail(null);
        user.setEmailOtp(null);
        user.setEmailOtpExpiry(null);

        user.setUpdatedAt(Instant.now());

        userRepository.save(user);
    }

    @Transactional
    public void changePasswordAdmin(String username, UserPasswordDto dto) {

        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Không tìm thấy người dùng");
        }

        if (!dto.getNewPassword().equals(dto.getRePassword())) {
            throw new RuntimeException("Mật khẩu nhập lại không khớp");
        }

        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu mới không được trùng mật khẩu cũ");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

}
