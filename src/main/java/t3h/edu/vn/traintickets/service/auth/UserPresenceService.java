package t3h.edu.vn.traintickets.service.auth;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserPresenceService {

    private final Map<Long, Integer> sessions = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;

    public UserPresenceService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public synchronized void online(Long userId) {
        int count = sessions.getOrDefault(userId, 0) + 1;
        sessions.put(userId, count);

        // 🔥 chỉ bắn event khi ONLINE LẦN ĐẦU
        if (count == 1) {
            broadcastPresence(userId, true);
        }

        log();
    }

    public synchronized void offline(Long userId) {
        Integer count = sessions.get(userId);
        if (count == null) return;

        if (count <= 1) {
            sessions.remove(userId);
            // 🔥 chỉ bắn event khi OFFLINE THẬT
            broadcastPresence(userId, false);
        } else {
            sessions.put(userId, count - 1);
        }

        log();
    }

    public boolean isOnline(Long userId) {
        return sessions.containsKey(userId);
    }

    private void broadcastPresence(Long userId, boolean online) {
        messagingTemplate.convertAndSend(
                "/topic/presence",
                Map.of(
                        "userId", userId,
                        "online", online
                )
        );
    }

    private void log() {
        System.out.println("🟢 ONLINE USERS = " + sessions);
    }
}


