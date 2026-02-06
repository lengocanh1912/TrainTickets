package t3h.edu.vn.traintickets.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import t3h.edu.vn.traintickets.entities.Conversation;
import t3h.edu.vn.traintickets.enums.ConversationStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c " +
            "JOIN FETCH c.user " +
            "WHERE c.user.id = :userId " +
            "AND c.status = :status")
    Optional<Conversation> findByUserIdAndStatus(@Param("userId") Long userId,
                                                 @Param("status") ConversationStatus status);
    @Query("""
    SELECT c FROM Conversation c
    WHERE c.user.id = :userId
    AND c.status IN (:statuses)
    ORDER BY c.createdAt DESC
""")
    List<Conversation> findLatestForUser(
            @Param("userId") Long userId,
            @Param("statuses") List<ConversationStatus> statuses
    );

    @Query("""
    SELECT c FROM Conversation c
    WHERE
        (c.admin.id = :adminId AND c.status IN ('OPEN','CLOSED'))
        OR
        (c.admin IS NULL AND c.status = 'WAITING')
    ORDER BY c.lastMessageAt DESC
""")
    List<Conversation> findAdminInbox(@Param("adminId") Long adminId);

    List<Conversation> findByAdmin_IdAndStatus(Long adminId, ConversationStatus status);

    List<Conversation> findByAdminIsNullAndStatusOrderByCreatedAtAsc(ConversationStatus status);

    @Query("""
    SELECT COALESCE(SUM(c.unreadForUser), 0)
    FROM Conversation c
    WHERE c.user.id = :userId
""")
    int sumUnreadConversationForUser(@Param("userId") Long userId);


    @Query("""
    SELECT COALESCE(SUM(c.unreadForAdmin), 0)
    FROM Conversation c
    WHERE c.admin.id = :adminId
       OR (c.admin IS NULL AND c.status = 'WAITING')
""")
    int sumUnreadConversationForAdmin(Long adminId);

}