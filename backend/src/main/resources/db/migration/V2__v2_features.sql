-- ClassPulse Twin - v2 Features (chatbot, gamification, study groups, code analysis, notifications)

-- Chatbot conversations
CREATE TABLE conversations (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id),
    course_id BIGINT REFERENCES courses(id),
    title VARCHAR(255),
    twin_context_json JSONB,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE chat_messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES conversations(id),
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    inline_cards_json JSONB,
    token_count INT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Gamification
CREATE TABLE student_gamification (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id),
    course_id BIGINT NOT NULL REFERENCES courses(id),
    level INT DEFAULT 1,
    current_xp INT DEFAULT 0,
    next_level_xp INT DEFAULT 100,
    level_title VARCHAR(50) DEFAULT '초보 학습자',
    streak_days INT DEFAULT 0,
    last_activity_date DATE,
    total_xp_earned INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(student_id, course_id)
);

CREATE TABLE badges (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    emoji VARCHAR(10),
    description TEXT,
    category VARCHAR(50),
    requirement_json JSONB,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE student_badges (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id),
    badge_id BIGINT NOT NULL REFERENCES badges(id),
    earned_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(student_id, badge_id)
);

CREATE TABLE xp_events (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id),
    course_id BIGINT NOT NULL REFERENCES courses(id),
    event_type VARCHAR(50) NOT NULL,
    xp_amount INT NOT NULL,
    source_id BIGINT,
    source_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW()
);

-- Study Groups
CREATE TABLE study_groups (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES courses(id),
    name VARCHAR(255),
    description TEXT,
    max_members INT DEFAULT 5,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE study_group_members (
    id BIGSERIAL PRIMARY KEY,
    study_group_id BIGINT NOT NULL REFERENCES study_groups(id),
    student_id BIGINT NOT NULL REFERENCES users(id),
    role VARCHAR(20) DEFAULT 'MEMBER',
    strength_summary VARCHAR(255),
    complement_note VARCHAR(255),
    match_score DECIMAL(3,2),
    joined_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(study_group_id, student_id)
);

-- Code Analysis
CREATE TABLE code_submissions (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id),
    course_id BIGINT NOT NULL REFERENCES courses(id),
    skill_id BIGINT REFERENCES curriculum_skills(id),
    code_content TEXT NOT NULL,
    language VARCHAR(30) DEFAULT 'javascript',
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE code_feedbacks (
    id BIGSERIAL PRIMARY KEY,
    submission_id BIGINT NOT NULL REFERENCES code_submissions(id),
    line_number INT,
    end_line_number INT,
    severity VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    suggestion TEXT,
    twin_linked BOOLEAN DEFAULT FALSE,
    twin_skill_id BIGINT REFERENCES curriculum_skills(id),
    created_at TIMESTAMP DEFAULT NOW()
);

-- Notifications
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT,
    data_json JSONB,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_conversations_student ON conversations(student_id, updated_at DESC);
CREATE INDEX idx_chat_messages_conv ON chat_messages(conversation_id, created_at);
CREATE INDEX idx_xp_events_student ON xp_events(student_id, created_at DESC);
CREATE INDEX idx_code_submissions_student ON code_submissions(student_id, created_at DESC);
CREATE INDEX idx_code_feedbacks_submission ON code_feedbacks(submission_id);
CREATE INDEX idx_notifications_user ON notifications(user_id, is_read, created_at DESC);
