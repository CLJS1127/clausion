-- ClassPulse Twin - Seed data for badges
INSERT INTO badges (name, emoji, description, category, requirement_json) VALUES
('빠른 복습러', '🏆', '복습 3일 연속 완료', 'REVIEW', '{"type": "streak", "target": "review", "days": 3}'),
('코드 전사', '⚡', '코드 제출 10회 이상', 'CODE', '{"type": "count", "target": "code_submission", "count": 10}'),
('회고 마니아', '📝', '회고 7일 연속 작성', 'STREAK', '{"type": "streak", "target": "reflection", "days": 7}'),
('상담 참여자', '💬', '상담 3회 이상 참여', 'SOCIAL', '{"type": "count", "target": "consultation", "count": 3}'),
('불꽃 스트릭', '🔥', '14일 연속 학습', 'STREAK', '{"type": "streak", "target": "activity", "days": 14}'),
('Twin 마스터', '💎', '전체 이해도 80% 이상 달성', 'REVIEW', '{"type": "score", "target": "mastery", "threshold": 80}'),
('코드 리뷰어', '🔍', 'AI 피드백 100% 반영 5회', 'CODE', '{"type": "count", "target": "code_feedback_applied", "count": 5}'),
('스터디 리더', '👑', '스터디 그룹 생성 후 활동', 'SOCIAL', '{"type": "action", "target": "study_group_create"}');
