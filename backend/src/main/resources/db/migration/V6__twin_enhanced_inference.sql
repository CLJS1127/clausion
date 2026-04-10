-- V6: Enhanced twin inference - trend tracking, score history

ALTER TABLE student_twin ADD COLUMN trend_direction VARCHAR(20);
ALTER TABLE student_twin ADD COLUMN trend_explanation TEXT;
ALTER TABLE student_twin ADD COLUMN data_conflicts_json JSONB;
ALTER TABLE student_twin ADD COLUMN inference_source VARCHAR(50);

CREATE TABLE twin_score_history (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id),
    course_id BIGINT NOT NULL REFERENCES courses(id),
    mastery_score DECIMAL(5,2),
    execution_score DECIMAL(5,2),
    retention_risk_score DECIMAL(5,2),
    motivation_score DECIMAL(5,2),
    consultation_need_score DECIMAL(5,2),
    overall_risk_score DECIMAL(5,2),
    inference_source VARCHAR(50),
    captured_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_twin_history_lookup ON twin_score_history(student_id, course_id, captured_at DESC);
