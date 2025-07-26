package t3h.edu.vn.traintickets.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import t3h.edu.vn.traintickets.entities.User;


import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String userName);

    List<User> findByStatusLessThan(Integer statusIsLessThan);
    @Query(value = "select u from User u where u.status = ?1")
    List<User> getAllByStatus(Integer status);

    Page<User> findByStatus(Integer status, Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
            "u.username LIKE %:keyword% OR " +
            "u.fullname LIKE %:keyword% OR " +
            "u.email LIKE %:keyword% OR " +
            "u.phoneNumber LIKE %:keyword%")
    List<User> findByUsernameOrFullnameOrEmailOrPhoneNumber(@Param("keyword") String keyword);

    boolean existsByEmail(String email); // <- THÊM DÒNG NÀY

    boolean existsByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByResetToken(String resetToken);


}
