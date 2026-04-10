package com.classpulse.notification;

import com.classpulse.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * RabbitMQ consumer for group chat messages.
 * Receives messages from the group chat exchange and broadcasts to STOMP subscribers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class GroupChatConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = RabbitMQConfig.GROUP_CHAT_QUEUE)
    public void handleGroupChatMessage(Map<String, Object> payload) {
        Object groupId = payload.get("groupId");
        log.debug("RabbitMQ group chat message received - groupId={}", groupId);

        // Broadcast to all STOMP subscribers of this group's topic
        messagingTemplate.convertAndSend("/topic/group-chat/" + groupId, payload);
    }
}
