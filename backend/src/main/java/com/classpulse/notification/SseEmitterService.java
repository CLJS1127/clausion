package com.classpulse.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages SSE emitters for real-time push to frontend clients.
 */
@Slf4j
@Service
public class SseEmitterService {

    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30 minutes

    /** userId -> list of active emitters (a user may have multiple tabs) */
    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    /** conversationId -> list of active chatbot stream emitters */
    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> chatbotEmitters = new ConcurrentHashMap<>();

    // ── Notification SSE ───────────────────────────────────────────────

    public SseEmitter createEmitter(Long userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(e -> removeEmitter(userId, emitter));

        log.debug("SSE emitter created for userId={}", userId);
        return emitter;
    }

    public void sendToUser(Long userId, Map<String, Object> payload) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(payload));
            } catch (IOException e) {
                removeEmitter(userId, emitter);
            }
        }
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                userEmitters.remove(userId);
            }
        }
    }

    // ── Chatbot stream SSE ─────────────────────────────────────────────

    public SseEmitter createChatbotEmitter(Long conversationId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        chatbotEmitters.computeIfAbsent(conversationId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeChatbotEmitter(conversationId, emitter));
        emitter.onTimeout(() -> removeChatbotEmitter(conversationId, emitter));
        emitter.onError(e -> removeChatbotEmitter(conversationId, emitter));

        log.debug("SSE chatbot emitter created for conversationId={}", conversationId);
        return emitter;
    }

    public void sendToChatbotStream(Long conversationId, Map<String, Object> payload) {
        CopyOnWriteArrayList<SseEmitter> emitters = chatbotEmitters.get(conversationId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("chatbot-message")
                        .data(payload));
            } catch (IOException e) {
                removeChatbotEmitter(conversationId, emitter);
            }
        }
    }

    private void removeChatbotEmitter(Long conversationId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = chatbotEmitters.get(conversationId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                chatbotEmitters.remove(conversationId);
            }
        }
    }
}
