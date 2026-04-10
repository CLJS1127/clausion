-- ClassPulse Twin - Core Schema
-- Users and profiles
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL, -- STUDENT, INSTRUCTOR, OPERATOR, ADMIN
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE student_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    track_name VARCHAR(100),
    goal TEXT,
    current_level VARCHAR(50)
);

CREATE TABLE instructor_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    department VARCHAR(100),
    bio TEXT
);

-- Courses
CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE course_weeks (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES courses(id),
    week_no INT NOT NULL,
    title VARCHAR(255),
    summary TEXT
);

CREATE TABLE course_enrollments (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES courses(id),
    student_id BIGINT NOT NULL REFERENCES users(id),
    enrolled_at TIMESTAMP DEFAULT NOW(),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    UNIQUE(course_id, student_id)
);

-- Curriculum Skills
CREATE TABLE curriculum_skills (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES courses(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    difficulty VARCHAR(20) DEFAULT 'MEDIUM',
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE skill_prerequisites (
    skill_id BIGINT NOT NULL REFERENCES curriculum_skills(id),
    prerequisite_skill_id BIGINT NOT NULL REFERENCES curriculum_skills(id),
    PRIMARY KEY (skill_id, prerequisite_skill_id)
);

-- Student Twin
CREATE TABLE student_twin (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id),
    course_id BIGINT NOT NULL REFERENCES courses(id),
    mastery_score DECIMAL(5,2) DEFAULT 0,
    execution_score DECIMAL(5,2) DEFAULT 0,
    retention_risk_score DECIMAL(5,2) DEFAULT 0,
    motivation_score DECIMAL(5,2) DEFAULT 0,
    consultation_need_score DECIMAL(5,2) DEFAULT 0,
    overall_risk_score DECIMAL(5,2) DEFAULT 0,
    ai_insight TEXT,
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(student_id, course_id)
);

CREATE TABLE skill_mastery_snapshot (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id),
    course_id BIGINT NOT NULL REFERENCES courses(id),
    skill_id BIGINT NOT NULL REFERENCES curriculum_skills(id),
    understanding_score DECIMAL(5,2) DEFAULT 0,
    practice_score DECIMAL(5,2) DEFAULT 0,
    confidence_score DECIMAL(5,2) DEFAULT 0,
    forgetting_risk_score DECIMAL(5,2) DEFAULT 0,
    source_type VARCHAR(50),
    captured_at TIMESTAMP DEFAULT NOW()
);

-- Learning Activities
CREATE TABLE reflections (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id),
    course_id BIGINT NOT NULL REFERENCES courses(id),
    content TEXT NOT NULL,
    stuck_point TEXT,
    self_confidence_score INT DEFAULT 3,
    emotion_summary JSONB,
    ai_analysis_json JSONB,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE review_tasks (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id),
    course_id BIGINT NOT NULL REFERENCES courses(id),
    skill_id BIGINT REFERENCES curriculum_skills(id),
    title VARCHAR(255) NOT NULL,
    reason_summary TEXT,
    scheduled_for DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE questions (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES courses(id),
    skill_id BIGINT REFERENCES curriculum_skills(id),
    question_type VARCHAR(30) NOT NULL,
    difficulty VARCHAR(20) DEFAULT 'MEDIUM',
    content TEXT NOT NULL,
    answer TEXT,
    explanation TEXT,
    generation_reason TEXT,
    approval_status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT NOW()
);

-- Consultation
CREATE TABLE consultations (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id),
    instructor_id BIGINT NOT NULL REFERENCES users(id),
    course_id BIGINT NOT NULL REFERENCES courses(id),
    scheduled_at TIMESTAMP NOT NULL,
    status VARCHAR(20) DEFAULT 'SCHEDULED',
    notes TEXT,
    summary_text TEXT,
    cause_analysis TEXT,
    action_plan_json JSONB,
    briefing_json JSONB,
    video_room_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW(),
    completed_at TIMESTAMP
);

-- Recommendations
CREATE TABLE recommendations (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id),
    course_id BIGINT NOT NULL REFERENCES courses(id),
    recommendation_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    reason_summary TEXT,
    trigger_event VARCHAR(100),
    evidence_payload JSONB,
    expected_outcome TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Async Jobs
CREATE TABLE async_jobs (
    id BIGSERIAL PRIMARY KEY,
    job_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    input_payload JSONB,
    result_payload JSONB,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    completed_at TIMESTAMP
);

-- Indexes
CREATE INDEX idx_student_twin_student_course ON student_twin(student_id, course_id);
CREATE INDEX idx_skill_mastery_snapshot_student ON skill_mastery_snapshot(student_id, skill_id, captured_at DESC);
CREATE INDEX idx_review_tasks_student ON review_tasks(student_id, scheduled_for, status);
CREATE INDEX idx_reflections_student ON reflections(student_id, created_at DESC);
CREATE INDEX idx_consultations_student ON consultations(student_id, scheduled_at DESC);
CREATE INDEX idx_recommendations_student ON recommendations(student_id, created_at DESC);
CREATE INDEX idx_questions_course ON questions(course_id, approval_status);
CREATE INDEX idx_course_enrollments_student ON course_enrollments(student_id);
