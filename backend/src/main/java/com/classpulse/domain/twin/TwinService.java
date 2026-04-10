package com.classpulse.domain.twin;

import com.classpulse.domain.course.Course;
import com.classpulse.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TwinService {

    private final StudentTwinRepository twinRepository;
    private final SkillMasterySnapshotRepository snapshotRepository;

    public StudentTwin getOrCreateTwin(User student, Course course) {
        return twinRepository.findByStudentIdAndCourseId(student.getId(), course.getId())
                .orElseGet(() -> {
                    StudentTwin twin = StudentTwin.builder()
                            .student(student)
                            .course(course)
                            .masteryScore(BigDecimal.ZERO)
                            .executionScore(BigDecimal.ZERO)
                            .retentionRiskScore(BigDecimal.ZERO)
                            .motivationScore(BigDecimal.ZERO)
                            .consultationNeedScore(BigDecimal.ZERO)
                            .overallRiskScore(BigDecimal.ZERO)
                            .build();
                    return twinRepository.save(twin);
                });
    }

    public StudentTwin getTwin(Long studentId, Long courseId) {
        return twinRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("Twin을 찾을 수 없습니다"));
    }

    public List<StudentTwin> getStudentTwins(Long studentId) {
        return twinRepository.findByStudentId(studentId);
    }

    @Transactional
    public StudentTwin updateTwin(StudentTwin twin) {
        return twinRepository.save(twin);
    }

    public List<SkillMasterySnapshot> getTwinHistory(Long studentId, Long courseId) {
        return snapshotRepository.findByStudentIdAndCourseIdOrderByCapturedAtDesc(studentId, courseId);
    }

    public List<StudentTwin> getRiskStudents(Long courseId, BigDecimal threshold) {
        return twinRepository.findByCourseIdAndOverallRiskScoreGreaterThan(courseId, threshold);
    }

    @Transactional
    public SkillMasterySnapshot saveSnapshot(SkillMasterySnapshot snapshot) {
        return snapshotRepository.save(snapshot);
    }
}
