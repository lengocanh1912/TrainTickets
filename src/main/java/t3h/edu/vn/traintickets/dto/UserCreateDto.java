package t3h.edu.vn.traintickets.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

@Data
@ToString
@Getter
@Setter
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
    String phoneNumber;
    Boolean gender;
    String address;
    String role;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotBlank(message = "Tên đăng nhập không được phép rỗng") String getUsername() {
        return username;
    }

    public void setUsername(@NotBlank(message = "Tên đăng nhập không được phép rỗng") String username) {
        this.username = username;
    }

    public @NotBlank(message = "Mật khẩu không được phép rỗng") @Min(value = 6, message = "Ít nhất có 6 ký tự") String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank(message = "Mật khẩu không được phép rỗng") @Min(value = 6, message = "Ít nhất có 6 ký tự") String password) {
        this.password = password;
    }

    public String getRePassword() {
        return rePassword;
    }

    public void setRePassword(String rePassword) {
        this.rePassword = rePassword;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public @Email(message = "Không đúng định dạng email") String getEmail() {
        return email;
    }

    public void setEmail(@Email(message = "Không đúng định dạng email") String email) {
        this.email = email;
    }

    public @NotBlank(message = "Số điện thoại không được phép rỗng") String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(@NotBlank(message = "Số điện thoại không được phép rỗng") String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean getGender() {
        return gender;
    }

    public void setGender(Boolean gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

