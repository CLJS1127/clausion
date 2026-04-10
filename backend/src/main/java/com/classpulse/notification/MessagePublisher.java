package com.classpulse.notification;

import com.classpulse.config.RabbitMQConfig;
import com.classpulse.domain.chatbot.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Message publisher for real-time messaging.
 * Uses RabbitMQ when available, falls back to SSE-only when not.
 */
@Slf4j
@Component
public class MessagePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final SseEmitterService sseEmitterService;
    private final boolean rabbitEnabled;

    @Autowired
    public MessagePublisher(@Autowired(required = false) RabbitTemplate rabbitTemplate,
                            SseEmitterService sseEmitterService) {
        this.rabbitTemplate = rabbitTemplate;
        this.sseEmitterService = sseEmitterService;
        this.rabbitEnabled = (rabbitTemplate != null);
        if (!rabbitEnabled) {
            log.info("RabbitMQ not available - using SSE-only mode for messaging");
        }
    }

    public void publishNotification(Long userId, String type, String title,
                                     String message, Map<String, Object> data) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", userId);
        payload.put("type", type);
        payload.put("title", title);
        payload.put("message", message);
        payload.put("data", data);
        payload.put("timestamp", LocalDateTime.now().toString());

        if (rabbitEnabled) {
            String routingKey = "user." + userId;
            rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATIONS_EXCHANGE, routingKey, payload);
            log.debug("Published notification via RabbitMQ - userId={}, type={}", userId, type);
        }

        sseEmitterService.sendToUser(userId, payload);
    }

    public void publishChatbotResponse(Long conversationId, ChatMessage chatMessage) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("conversationId", conversationId);
        payload.put("messageId", chatMessage.getId());
        payload.put("role", chatMessage.getRole());
        payload.put("content", chatMessage.getContent());
        payload.put("inlineCards", chatMessage.getInlineCardsJson());
        payload.put("tokenCount", chatMessage.getTokenCount());
        payload.put("createdAt", chatMessage.getCreatedAt() != null ? chatMessage.getCreatedAt().toString() : null);

        if (rabbitEnabled) {
            rabbitTemplate.convertAndSend(RabbitMQConfig.CHATBOT_EXCHANGE, "conversation", payload);
            log.debug("Published chatbot response via RabbitMQ - conversationId={}", conversationId);
        }

        sseEmitterService.sendToChatbotStream(conversationId, payload);
    }

    public void publishGamificationEvent(Long studentId, String eventType, Map<String, Object> data) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("studentId", studentId);
        payload.put("eventType", eventType);
        payload.put("data", data);
        payload.put("timestamp", LocalDateTime.now().toString());

        if (rabbitEnabled) {
            String routingKey = "student." + studentId;
            rabbitTemplate.convertAndSend(RabbitMQConfig.GAMIFICATION_EXCHANGE, routingKey, payload);
            log.debug("Published gamification event via RabbitMQ - studentId={}, eventType={}", studentId, eventType);
        }
    }

    public void publishConsultationUpdate(Long consultationId, String updateType, Map<String, Object> data) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("consultationId", consultationId);
        payload.put("updateType", updateType);
        payload.put("data", data);
        payload.put("timestamp", LocalDateTime.now().toString());

        if (rabbitEnabled) {
            rabbitTemplate.convertAndSend(RabbitMQConfig.CONSULTATION_EXCHANGE, "update", payload);
            log.debug("Published consultation update via RabbitMQ - consultationId={}", consultationId);
        }
    }
}
