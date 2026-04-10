package com.classpulse.domain.chatbot;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByStudentIdAndStatusOrderByUpdatedAtDesc(Long studentId, String status);
    List<Conversation> findByStudentIdOrderByUpdatedAtDesc(Long studentId);
    List<Conversation> findByStudentIdAndCourseIdOrderByUpdatedAtDesc(Long studentId, Long courseId);
}
