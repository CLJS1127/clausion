package com.classpulse.api;

import com.classpulse.domain.studygroup.StudyGroup;
import com.classpulse.domain.studygroup.StudyGroupMember;
import com.classpulse.domain.studygroup.StudyGroupMessage;
import com.classpulse.domain.studygroup.StudyGroupMessageRepository;
import com.classpulse.domain.studygroup.StudyGroupRepository;
import com.classpulse.domain.user.User;
import com.classpulse.domain.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupChatControllerTest {

    @Mock
    private StudyGroupMessageRepository messageRepository;

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private UserService userService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private GroupChatController controller;

    @BeforeEach
    void stubMessageRepository() {
        lenient().when(messageRepository.save(any(StudyGroupMessage.class))).thenAnswer(invocation -> {
            StudyGroupMessage message = invocation.getArgument(0);
            message.setId(999L);
            if (message.getCreatedAt() == null) {
                message.setCreatedAt(LocalDateTime.now());
            }
            return message;
        });
    }

    @Test
    void sendMessage_ignoresBlankTextWithoutFile() {
        User sender = User.builder()
                .id(1L)
                .name("Sender")
                .email("sender@test.com")
                .passwordHash("hashed")
                .role(User.Role.STUDENT)
                .build();

        StudyGroupMember member = StudyGroupMember.builder()
                .id(1L)
                .student(sender)
                .role("LEADER")
                .joinedAt(LocalDateTime.now())
                .build();

        StudyGroup group = StudyGroup.builder()
                .id(10L)
                .createdBy(sender)
                .members(List.of(member))
                .build();
        member.setStudyGroup(group);

        Principal principal = () -> "1";
        GroupChatController.ChatMessageRequest request =
                new GroupChatController.ChatMessageRequest("   ", null, null, null, null);

        when(userService.findById(1L)).thenReturn(sender);
        when(studyGroupRepository.findById(10L)).thenReturn(Optional.of(group));

        controller.sendMessage(10L, request, principal);

        verify(messageRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(messagingTemplate, never()).convertAndSend(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.<Object>any()
        );
    }
}
