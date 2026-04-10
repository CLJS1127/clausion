package com.classpulse.notification;

import com.classpulse.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * RabbitMQ consumer for notification messages.
 * Only active when RabbitMQ is enabled.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class NotificationConsumer {

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATIONS_QUEUE)
    public void handleNotification(Map<String, Object> message) {
        log.info("Received notification - userId={}, type={}, title={}",
                message.get("userId"),
                message.get("type"),
                message.get("title"));
    }

    @RabbitListener(queues = RabbitMQConfig.CHATBOT_QUEUE)
    public void handleChatbotMessage(Map<String, Object> message) {
        log.info("Received chatbot message - conversationId={}, messageId={}",
                message.get("conversationId"),
                message.get("messageId"));
    }

    @RabbitListener(queues = RabbitMQConfig.GAMIFICATION_QUEUE)
    public void handleGamificationEvent(Map<String, Object> message) {
        log.info("Received gamification event - studentId={}, eventType={}",
                message.get("studentId"),
                message.get("eventType"));
    }

    @RabbitListener(queues = RabbitMQConfig.CONSULTATION_QUEUE)
    public void handleConsultationUpdate(Map<String, Object> message) {
        log.info("Received consultation update - consultationId={}, updateType={}",
                message.get("consultationId"),
                message.get("updateType"));
    }
}
