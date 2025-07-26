package t3h.edu.vn.traintickets.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import t3h.edu.vn.traintickets.dto.UserCreateDto;
import t3h.edu.vn.traintickets.dto.UserUpdateDto;
import t3h.edu.vn.traintickets.entities.User;
import t3h.edu.vn.traintickets.repository.UserRepository;

import java.security.Principal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static t3h.edu.vn.traintickets.utils.PasswordUtil.md5;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

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
//    public void sendResetPasswordLink(String email) {
//        User user = userRepository.findByEmail(email);
//        if (user == null) {
//            throw new IllegalArgumentException("Email không tồn tại");
//        }
//
//        String token = UUID.randomUUID().toString();
//        user.setResetToken(token);
//        user.setResetTokenExpiry(Instant.now().plus(1, ChronoUnit.HOURS));
//        userRepository.save(user);
//
//        String resetLink = "http://localhost:8080/reset-password?token=" + token;
//
//        // Gửi mail (viết sau nếu chưa có)
//        mailService.send(email, "Đặt lại mật khẩu", "Click vào link sau để đặt lại mật khẩu: " + resetLink);
//    }

//    public void resetPassword(String token, String password, String confirmPassword) {
//        User user = userRepository.findByResetToken(token);
//        if (user == null || user.getResetTokenExpiry().isBefore(Instant.now())) {
//            throw new IllegalArgumentException("Token không hợp lệ hoặc đã hết hạn");
//        }
//
//        if (!password.equals(confirmPassword)) {
//            throw new IllegalArgumentException("Mật khẩu không khớp");
//        }
//
//        user.setPassword(passwordEncoder.encode(password));
//        user.setResetToken(null);
//        user.setResetTokenExpiry(null);
//        userRepository.save(user);
//    }


}
