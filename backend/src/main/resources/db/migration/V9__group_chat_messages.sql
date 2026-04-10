CREATE TABLE study_group_messages (
    id         BIGSERIAL PRIMARY KEY,
    study_group_id BIGINT    NOT NULL REFERENCES study_groups(id) ON DELETE CASCADE,
    sender_id      BIGINT    NOT NULL REFERENCES users(id),
    content        TEXT      NOT NULL,
    message_type   VARCHAR(20) DEFAULT 'TEXT',
    created_at     TIMESTAMP   DEFAULT NOW()

);

CREATE INDEX idx_sgm_group_created ON study_group_messages(study_group_id, created_at DESC);
