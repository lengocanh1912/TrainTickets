package t3h.edu.vn.traintickets.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import t3h.edu.vn.traintickets.entities.Message;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // ✅ THÊM JOIN FETCH để load User ngay từ đầu
    @Query("SELECT m FROM Message m " +
            "JOIN FETCH m.sender " +
            "JOIN FETCH m.conversation " +
            "WHERE m.conversation.id = :conversationId " +
            "ORDER BY m.createdAt ASC")
    List<Message> findByConversationIdOrderByCreatedAtAsc(@Param("conversationId") Long conversationId);

    @Query("""
            SELECT m FROM Message m
            WHERE m.conversation.id = :conversationId
            AND m.sender.id <> :userId
            AND m.status = 'SENT'
            """)
    List<Message> findUnreadMessages(Long conversationId, Long userId);



    Message findFirstByConversationIdOrderByCreatedAtDesc(Long conversationId);

    @Query("""
                SELECT m FROM Message m
                WHERE m.conversation.id = :conversationId
                ORDER BY m.createdAt DESC
            """)
    List<Message> findLastMessage(
            @Param("conversationId") Long conversationId,
            Pageable pageable
    );

    @Query("""
    SELECT COUNT(m)
    FROM Message m
    JOIN m.conversation c
    WHERE c.user.id = :userId
      AND m.sender.id <> :userId
      AND m.status <> 'READ'
""")
    int countUnreadByUser(@Param("userId") Long userId);


}
