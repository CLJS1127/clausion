package com.classpulse.domain.announcement;

import com.classpulse.domain.course.Course;
import com.classpulse.domain.course.CourseEnrollment;
import com.classpulse.domain.course.CourseEnrollmentRepository;
import com.classpulse.domain.course.CourseRepository;
import com.classpulse.domain.user.User;
import com.classpulse.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AnnouncementAudienceService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;

    public Set<Long> resolveRecipientIds(Announcement announcement) {
        return resolveRecipientIds(announcement.getTargetType(), announcement.getTargetCourseId());
    }

    public Set<Long> resolveRecipientIds(String targetType, Long targetCourseId) {
        String normalized = normalizeTargetType(targetType);
        LinkedHashSet<Long> recipientIds = new LinkedHashSet<>();

        switch (normalized) {
            case "STUDENT_ONLY" -> userRepository.findByRole(User.Role.STUDENT)
                    .stream()
                    .map(User::getId)
                    .forEach(recipientIds::add);
            case "INSTRUCTOR_ONLY" -> userRepository.findByRole(User.Role.INSTRUCTOR)
                    .stream()
                    .map(User::getId)
                    .forEach(recipientIds::add);
            case "COURSE" -> {
                if (targetCourseId == null) {
                    return recipientIds;
                }
                Course course = courseRepository.findById(targetCourseId).orElse(null);
                if (course != null && course.getCreatedBy() != null) {
                    recipientIds.add(course.getCreatedBy().getId());
                }
                List<CourseEnrollment> activeEnrollments = enrollmentRepository.findByCourseIdAndStatus(targetCourseId, "ACTIVE");
                activeEnrollments.stream()
                        .map(enrollment -> enrollment.getStudent().getId())
                        .forEach(recipientIds::add);
            }
            case "ALL" -> {
                userRepository.findByRole(User.Role.STUDENT)
                        .stream()
                        .map(User::getId)
                        .forEach(recipientIds::add);
                userRepository.findByRole(User.Role.INSTRUCTOR)
                        .stream()
                        .map(User::getId)
                        .forEach(recipientIds::add);
            }
            default -> {
            }
        }

        return recipientIds;
    }

    public boolean canUserView(Announcement announcement, User user) {
        if (user == null || user.getRole() == User.Role.OPERATOR) {
            return false;
        }

        String normalized = normalizeTargetType(announcement.getTargetType());
        return switch (normalized) {
            case "STUDENT_ONLY" -> user.getRole() == User.Role.STUDENT;
            case "INSTRUCTOR_ONLY" -> user.getRole() == User.Role.INSTRUCTOR;
            case "COURSE" -> canUserViewCourseAnnouncement(announcement.getTargetCourseId(), user);
            case "ALL" -> user.getRole() == User.Role.STUDENT || user.getRole() == User.Role.INSTRUCTOR;
            default -> false;
        };
    }

    public long countRecipients(Announcement announcement) {
        return resolveRecipientIds(announcement).size();
    }

    private boolean canUserViewCourseAnnouncement(Long courseId, User user) {
        if (courseId == null) {
            return false;
        }

        if (user.getRole() == User.Role.INSTRUCTOR) {
            return courseRepository.findById(courseId)
                    .map(course -> course.getCreatedBy() != null && course.getCreatedBy().getId().equals(user.getId()))
                    .orElse(false);
        }

        if (user.getRole() == User.Role.STUDENT) {
            return enrollmentRepository.findByCourseIdAndStatus(courseId, "ACTIVE")
                    .stream()
                    .anyMatch(enrollment -> enrollment.getStudent().getId().equals(user.getId()));
        }

        return false;
    }

    private String normalizeTargetType(String targetType) {
        return targetType == null ? "ALL" : targetType.trim().toUpperCase(Locale.ROOT);
    }
}
