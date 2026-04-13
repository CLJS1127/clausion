package com.classpulse.api;

import com.classpulse.config.SecurityUtil;
import com.classpulse.domain.announcement.Announcement;
import com.classpulse.domain.announcement.AnnouncementAudienceService;
import com.classpulse.domain.announcement.AnnouncementRead;
import com.classpulse.domain.announcement.AnnouncementReadRepository;
import com.classpulse.domain.announcement.AnnouncementRepository;
import com.classpulse.domain.user.User;
import com.classpulse.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
@Transactional
public class AnnouncementFeedController {

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementReadRepository announcementReadRepository;
    private final UserRepository userRepository;
    private final AnnouncementAudienceService announcementAudienceService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list() {
        User currentUser = getCurrentUser();

        List<Map<String, Object>> result = announcementRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .filter(announcement -> announcementAudienceService.canUserView(announcement, currentUser))
                .map(announcement -> toMap(announcement, currentUser.getId()))
                .toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다: " + id));

        if (!announcementAudienceService.canUserView(announcement, currentUser)) {
            return ResponseEntity.status(403).build();
        }

        boolean alreadyRead = announcementReadRepository.findByAnnouncementIdAndUserId(id, currentUser.getId()).isPresent();
        if (!alreadyRead) {
            announcementReadRepository.save(AnnouncementRead.builder()
                    .announcementId(id)
                    .userId(currentUser.getId())
                    .build());
        }
        return ResponseEntity.noContent().build();
    }

    private User getCurrentUser() {
        Long userId = SecurityUtil.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
    }

    private Map<String, Object> toMap(Announcement announcement, Long userId) {
        boolean isRead = announcementReadRepository.findByAnnouncementIdAndUserId(announcement.getId(), userId).isPresent();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", announcement.getId());
        result.put("title", announcement.getTitle());
        result.put("content", announcement.getContent());
        result.put("targetType", announcement.getTargetType());
        result.put("targetCourseId", announcement.getTargetCourseId());
        result.put("isUrgent", announcement.getIsUrgent());
        result.put("authorId", announcement.getAuthorId());
        result.put("createdAt", announcement.getCreatedAt() != null ? announcement.getCreatedAt().toString() : null);
        result.put("isRead", isRead);
        return result;
    }
}
