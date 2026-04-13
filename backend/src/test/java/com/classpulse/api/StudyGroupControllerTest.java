package com.classpulse.api;

import com.classpulse.ai.StudyGroupMatcherAi;
import com.classpulse.domain.course.Course;
import com.classpulse.domain.course.CourseRepository;
import com.classpulse.domain.studygroup.StudyGroup;
import com.classpulse.domain.studygroup.StudyGroupMember;
import com.classpulse.domain.studygroup.StudyGroupMessage;
import com.classpulse.domain.studygroup.StudyGroupMessageRepository;
import com.classpulse.domain.studygroup.StudyGroupRepository;
import com.classpulse.domain.user.User;
import com.classpulse.domain.user.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudyGroupControllerTest {

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private StudyGroupMessageRepository messageRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserService userService;

    @Mock
    private StudyGroupMatcherAi studyGroupMatcherAi;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private StudyGroupController controller;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

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
    void leave_deletesGroupWhenOnlyLeaderWouldRemain() {
        User leader = user(1L, "Leader");
        User member = user(2L, "Member");
        StudyGroup group = group(100L, leader, leaderMember(leader), member("MEMBER", member, LocalDateTime.now()));

        authenticateAs(member.getId());
        when(userService.findById(member.getId())).thenReturn(member);
        when(studyGroupRepository.findById(group.getId())).thenReturn(Optional.of(group));

        var response = controller.leave(group.getId());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(group.getMembers()).extracting(m -> m.getStudent().getId()).containsExactly(leader.getId());
        verify(studyGroupRepository).delete(group);
        verify(studyGroupRepository, never()).save(group);
        verify(messageRepository, never()).save(any());

        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/group-chat/100"), payloadCaptor.capture());
        assertThat(payloadCaptor.getValue())
                .containsEntry("groupId", 100L)
                .containsEntry("messageType", "ROOM_DELETED");
    }

    @Test
    void leave_transfersLeadershipWhenLeaderLeavesAndGroupSurvives() {
        User leader = user(1L, "Leader");
        User memberA = user(2L, "Member A");
        User memberB = user(3L, "Member B");
        StudyGroupMember leaderMember = leaderMember(leader);
        StudyGroupMember firstJoined = member("MEMBER", memberA, LocalDateTime.of(2026, 4, 1, 9, 0));
        StudyGroupMember laterJoined = member("MEMBER", memberB, LocalDateTime.of(2026, 4, 2, 9, 0));
        StudyGroup group = group(101L, leader, leaderMember, firstJoined, laterJoined);

        authenticateAs(leader.getId());
        when(userService.findById(leader.getId())).thenReturn(leader);
        when(studyGroupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(studyGroupRepository.save(group)).thenReturn(group);

        var response = controller.leave(group.getId());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(group.getCreatedBy()).isEqualTo(memberA);
        assertThat(firstJoined.getRole()).isEqualTo("LEADER");
        assertThat(group.getMembers()).hasSize(2);
        verify(studyGroupRepository, never()).delete(group);
        verify(studyGroupRepository).save(group);
    }

    @Test
    void leave_keepsGroupWhenAtLeastTwoMembersRemain() {
        User leader = user(1L, "Leader");
        User memberA = user(2L, "Member A");
        User memberB = user(3L, "Member B");
        User memberC = user(4L, "Member C");
        StudyGroup group = group(
                102L,
                leader,
                leaderMember(leader),
                member("MEMBER", memberA, LocalDateTime.of(2026, 4, 1, 9, 0)),
                member("MEMBER", memberB, LocalDateTime.of(2026, 4, 2, 9, 0)),
                member("MEMBER", memberC, LocalDateTime.of(2026, 4, 3, 9, 0))
        );

        authenticateAs(memberB.getId());
        when(userService.findById(memberB.getId())).thenReturn(memberB);
        when(studyGroupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(studyGroupRepository.save(group)).thenReturn(group);

        var response = controller.leave(group.getId());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(group.getMembers()).hasSize(3);
        verify(studyGroupRepository, never()).delete(group);
        verify(studyGroupRepository).save(group);
    }

    private void authenticateAs(Long userId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null)
        );
    }

    private StudyGroup group(Long id, User creator, StudyGroupMember... members) {
        StudyGroup group = StudyGroup.builder()
                .id(id)
                .course(Course.builder().id(10L).title("Algorithms").build())
                .createdBy(creator)
                .name("Test Group")
                .members(new ArrayList<>(List.of(members)))
                .build();
        for (StudyGroupMember member : group.getMembers()) {
            member.setStudyGroup(group);
        }
        return group;
    }

    private StudyGroupMember leaderMember(User user) {
        return member("LEADER", user, LocalDateTime.of(2026, 3, 31, 9, 0));
    }

    private StudyGroupMember member(String role, User user, LocalDateTime joinedAt) {
        return StudyGroupMember.builder()
                .id(user.getId())
                .student(user)
                .role(role)
                .joinedAt(joinedAt)
                .build();
    }

    private User user(Long id, String name) {
        return User.builder()
                .id(id)
                .name(name)
                .email(name.toLowerCase().replace(" ", ".") + "@test.com")
                .passwordHash("hashed")
                .role(User.Role.STUDENT)
                .build();
    }
}
