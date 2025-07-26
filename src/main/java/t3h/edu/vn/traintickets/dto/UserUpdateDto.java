package t3h.edu.vn.traintickets.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
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

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
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
