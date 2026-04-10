-- ClassPulse Twin - Seed demo course and curriculum skills
-- This creates a default course so that all course-dependent features work out of the box.

-- Create a demo instructor user (id=1 if table is empty, otherwise uses BIGSERIAL)
-- We use ON CONFLICT to avoid duplicates if users already exist
INSERT INTO users (email, password_hash, name, role)
VALUES ('instructor@classpulse.com', '$2a$10$dummyhashforinstructorplaceholder000', '김교수', 'INSTRUCTOR')
ON CONFLICT (email) DO NOTHING;

-- Create the demo course, owned by the instructor
INSERT INTO courses (title, description, status, created_by)
VALUES (
  'Python 프로그래밍 기초',
  'Python 기초 문법부터 알고리즘까지 체계적으로 학습하는 과정입니다. 변수, 조건문, 반복문, 함수, 자료구조, 알고리즘 등을 다룹니다.',
  'ACTIVE',
  (SELECT id FROM users WHERE email = 'instructor@classpulse.com')
);

-- Course weeks
INSERT INTO course_weeks (course_id, week_no, title, summary)
SELECT c.id, w.week_no, w.title, w.summary
FROM (SELECT id FROM courses WHERE title = 'Python 프로그래밍 기초' LIMIT 1) c,
(VALUES
  (1, '변수와 자료형', '변수 선언, 기본 자료형(int, float, str, bool), 형변환'),
  (2, '조건문과 반복문', 'if/elif/else, for, while, break/continue'),
  (3, '함수와 모듈', '함수 정의, 매개변수, 반환값, 모듈 import'),
  (4, '리스트와 튜플', '리스트 생성, 인덱싱, 슬라이싱, 리스트 컴프리헨션'),
  (5, '딕셔너리와 집합', '딕셔너리 활용, 집합 연산'),
  (6, '문자열 처리', '문자열 메서드, 포매팅, 정규표현식 기초'),
  (7, '파일 입출력', '파일 읽기/쓰기, CSV, JSON 처리'),
  (8, '재귀 함수', '재귀 개념, 재귀 vs 반복, 분할정복'),
  (9, '정렬 알고리즘', '버블정렬, 선택정렬, 삽입정렬, 내장 정렬'),
  (10, '탐색 알고리즘', '선형탐색, 이진탐색, 해시 탐색')
) AS w(week_no, title, summary);

-- Curriculum skills
INSERT INTO curriculum_skills (course_id, name, description, difficulty)
SELECT c.id, s.name, s.description, s.difficulty
FROM (SELECT id FROM courses WHERE title = 'Python 프로그래밍 기초' LIMIT 1) c,
(VALUES
  ('변수와 자료형', '변수 선언 및 기본 자료형 이해', 'EASY'),
  ('조건문', 'if/elif/else 조건 분기 처리', 'EASY'),
  ('반복문', 'for/while 루프 활용', 'EASY'),
  ('함수 기초', '함수 정의와 호출, 매개변수와 반환값', 'MEDIUM'),
  ('리스트 컴프리헨션', '리스트 컴프리헨션 문법과 활용', 'MEDIUM'),
  ('딕셔너리 활용', '딕셔너리 생성, 접근, 메서드 활용', 'MEDIUM'),
  ('재귀 함수', '재귀 호출 원리와 기저 조건 설정', 'HARD'),
  ('정렬 알고리즘', '기본 정렬 알고리즘 구현과 비교', 'HARD'),
  ('이진 탐색', '이진 탐색 알고리즘 구현', 'HARD'),
  ('클로저', '클로저 개념과 활용 패턴', 'HARD')
) AS s(name, description, difficulty);
