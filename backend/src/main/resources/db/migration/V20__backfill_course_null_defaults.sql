-- V20: Lombok @Builder.Default 누락으로 NULL이 저장된 강의 필드 백필
-- (Course.builder()가 필드 초기값을 무시해 approval_status/status/max_capacity가 NULL로 저장되던 버그의 데이터 정리)
UPDATE courses SET approval_status = 'APPROVED' WHERE approval_status IS NULL;
UPDATE courses SET status = 'ACTIVE' WHERE status IS NULL;
UPDATE courses SET max_capacity = 30 WHERE max_capacity IS NULL;
UPDATE curriculum_skills SET difficulty = 'MEDIUM' WHERE difficulty IS NULL;
