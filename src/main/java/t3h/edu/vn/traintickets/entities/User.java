package t3h.edu.vn.traintickets.entities;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class User {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)// autoincrement
    private long id;

    @NotNull
    @Size(max = 25)
    @Column(name = "username", nullable = false, length = 25, unique = true)
    private String username;

    @NotNull
    @Size(max = 100)
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @NotNull
    @Size(max = 50)
    @Column(name = "fullname", nullable = false, length = 50)
    private String fullname;

    @NotNull
    @Size(max = 50)
    @Email
    @Column(name = "email", nullable = false, length = 50, unique = true)
    private String email;

    @NotNull
    @Size(max = 15)
    @Column(name = "phoneNumber", nullable = false, length = 15, unique = true)
    private String phoneNumber;

    @NotNull
    @Column(name = "gender", nullable = false)
    private Boolean gender;

    @NotNull
    @Size(max = 200)
    @Column(name = "address", nullable = false, length = 200)
    private String address;

    @NotNull
    @Size(max = 20)
    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @Column(name = "status")
    private Integer status = 1;

    @NotNull
    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;
}
