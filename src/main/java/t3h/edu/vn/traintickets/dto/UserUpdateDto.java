package t3h.edu.vn.traintickets.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto implements Serializable {
        Long id;
        @NotBlank(message = "Tên đăng nhập không được phép rỗng")
        String username;
        String fullname;
        @Email(message = "Không đúng định dạng email")
        String email;
        @NotBlank(message = "Số điện thoại không được phép rỗng")
        String phoneNumber;
        Boolean gender;
        String address;
        String role;
        String birthday;

}
