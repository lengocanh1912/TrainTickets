package t3h.edu.vn.traintickets.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDto implements Serializable {
    Long id;
    @NotBlank(message = "Tên đăng nhập không được phép rỗng")
    String username;
    @NotBlank(message = "Mật khẩu không được phép rỗng")
    @Min(value = 6, message = "Ít nhất có 6 ký tự")
    String password;
    String rePassword;
    String fullName;
    @Email(message = "Không đúng định dạng email")
    String email;
    @NotBlank(message = "Số điện thoại không được phép rỗng")
    @Pattern(regexp = "\\d{10}", message = "Số điện thoại phải là 10 chữ số")
    String phoneNumber;
    Boolean gender;
    String address;
    String role;

}

