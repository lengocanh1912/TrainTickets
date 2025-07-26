package t3h.edu.vn.traintickets.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 25)
    @NotNull
    @Column(name = "username", nullable = false, length = 25)
    private String username;

    @Size(max = 100)
    @NotNull
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Size(max = 50)
    @NotNull
    @Column(name = "fullname", nullable = false, length = 50)
    private String fullname;

    @Size(max = 50)
    @NotNull
    @Column(name = "email", nullable = false, length = 50)
    private String email;

    @Size(max = 15)
    @NotNull
    @Column(name = "phoneNumber", nullable = false, length = 15)
    private String phoneNumber;

    @NotNull
    @Column(name = "gender", nullable = false)
    private Boolean gender = false;

    @Size(max = 200)
    @NotNull
    @Column(name = "address", nullable = false, length = 200)
    private String address;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Size(max = 20)
    @NotNull
    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @ColumnDefault("1")
    @Column(name = "status")
    private Integer status;

    @NotNull
    @Column(name = "createdAt", nullable = false)
    private Instant createdAt;

    @Column(name = "updatedAt")
    private Instant updatedAt;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private Instant resetTokenExpiry;

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public Instant getResetTokenExpiry() {
        return resetTokenExpiry;
    }

    public void setResetTokenExpiry(Instant resetTokenExpiry) {
        this.resetTokenExpiry = resetTokenExpiry;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @Size(max = 25) @NotNull String getUsername() {
        return username;
    }

    public void setUsername(@Size(max = 25) @NotNull String username) {
        this.username = username;
    }

    public @Size(max = 100) @NotNull String getPassword() {
        return password;
    }

    public void setPassword(@Size(max = 100) @NotNull String password) {
        this.password = password;
    }

    public @Size(max = 50) @NotNull String getFullname() {
        return fullname;
    }

    public void setFullname(@Size(max = 50) @NotNull String fullname) {
        this.fullname = fullname;
    }

    public @Size(max = 50) @NotNull String getEmail() {
        return email;
    }

    public void setEmail(@Size(max = 50) @NotNull String email) {
        this.email = email;
    }

    public @Size(max = 15) @NotNull String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(@Size(max = 15) @NotNull String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public @NotNull Boolean getGender() {
        return gender;
    }

    public void setGender(@NotNull Boolean gender) {
        this.gender = gender;
    }

    public @Size(max = 200) @NotNull String getAddress() {
        return address;
    }

    public void setAddress(@Size(max = 200) @NotNull String address) {
        this.address = address;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public @Size(max = 20) @NotNull String getRole() {
        return role;
    }

    public void setRole(@Size(max = 20) @NotNull String role) {
        this.role = role;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public @NotNull Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(@NotNull Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}