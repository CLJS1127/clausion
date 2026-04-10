CREATE TABLE action_plans (
    id BIGSERIAL PRIMARY KEY,
    consultation_id BIGINT NOT NULL REFERENCES consultations(id),
    student_id BIGINT NOT NULL REFERENCES users(id),
    course_id BIGINT NOT NULL REFERENCES courses(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    due_date DATE,
    linked_skill_id BIGINT REFERENCES curriculum_skills(id),
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT NOW(),
    completed_at TIMESTAMP
);

CREATE INDEX idx_action_plans_student ON action_plans(student_id, status, due_date);
CREATE INDEX idx_action_plans_consultation ON action_plans(consultation_id);
