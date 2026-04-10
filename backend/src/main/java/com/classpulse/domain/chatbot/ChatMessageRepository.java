package com.classpulse.domain.chatbot;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
    List<ChatMessage> findTop10ByConversationIdOrderByCreatedAtDesc(Long conversationId);
    long countByConversationStudentIdAndRole(Long studentId, String role);
}
