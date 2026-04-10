-- username 컬럼 추가 (어드민용, nullable)
ALTER TABLE users ADD COLUMN IF NOT EXISTS username VARCHAR(50) UNIQUE;
