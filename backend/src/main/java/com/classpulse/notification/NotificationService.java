package com.classpulse.notification;

import com.classpulse.domain.user.User;
import com.classpulse.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 알림 서비스
 * 알림 생성, 조회, 읽음 처리 및 RabbitMQ 실시간 전송을 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final MessagePublisher messagePublisher;

    /**
     * 알림을 생성하고 RabbitMQ로 실시간 전송합니다.
     */
    @Transactional
    public Notification createNotification(Long userId, String type, String title,
                                            String message, Map<String, Object> data) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .dataJson(data)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);

        // Send via RabbitMQ
        messagePublisher.publishNotification(userId, type, title, message, data);

        log.info("알림 생성 - userId={}, type={}, title={}", userId, type, title);
        return saved;
    }

    /**
     * 사용자의 전체 알림을 조회합니다.
     */
    public List<Map<String, Object>> getNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    /**
     * 읽지 않은 알림만 조회합니다.
     */
    public List<Map<String, Object>> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    /**
     * 특정 알림을 읽음 처리합니다.
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    /**
     * 사용자의 모든 알림을 읽음 처리합니다.
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        for (Notification n : unread) {
            n.setIsRead(true);
        }
        notificationRepository.saveAll(unread);
        log.info("전체 읽음 처리 - userId={}, count={}", userId, unread.size());
    }

    /**
     * 읽지 않은 알림 수를 반환합니다.
     */
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    // ── RabbitMQ 전송 ──────────────────────────────────────────────────

    /**
     * 읽지 않은 알림 수를 RabbitMQ + SSE로 전송합니다.
     */
    public void sendUnreadCountUpdate(Long userId) {
        try {
            long count = getUnreadCount(userId);
            messagePublisher.publishNotification(
                    userId, "UNREAD_COUNT", "unread-count",
                    String.valueOf(count),
                    Map.of("unreadCount", count)
            );
        } catch (Exception e) {
            log.warn("읽지않은 수 전송 실패 - userId={}: {}", userId, e.getMessage());
        }
    }

    // ── Helper ──────────────────────────────────────────────────────────

    private Map<String, Object> toMap(Notification n) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", n.getId());
        map.put("type", n.getType());
        map.put("title", n.getTitle());
        map.put("message", n.getMessage());
        map.put("data", n.getDataJson());
        map.put("isRead", n.getIsRead());
        map.put("createdAt", n.getCreatedAt() != null ? n.getCreatedAt().toString() : null);
        return map;
    }
}
