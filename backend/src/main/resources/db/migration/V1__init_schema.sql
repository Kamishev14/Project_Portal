-- V1__init_schema.sql
-- Uses VARCHAR for status/role columns so Hibernate's @Enumerated(STRING) works
-- cleanly without needing custom PostgreSQL enum types.

CREATE TABLE users (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL DEFAULT 'STUDENT',
    student_index   CHAR(6)      UNIQUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT chk_student_index_format
        CHECK (student_index IS NULL OR student_index ~ '^\d{6}$')
);

CREATE TABLE projects (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(200) NOT NULL,
    description     TEXT         NOT NULL,
    status          VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    queue_number    INT          UNIQUE,
    student_id      UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE reviews (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id      UUID         NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    professor_id    UUID         NOT NULL REFERENCES users(id),
    comment         TEXT,
    status          VARCHAR(30)  NOT NULL,
    reviewed_at     TIMESTAMP    NOT NULL DEFAULT now()
);

-- Auto-update updated_at on projects
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER projects_updated_at
    BEFORE UPDATE ON projects
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Indexes
CREATE INDEX idx_projects_student_id ON projects(student_id);
CREATE INDEX idx_projects_status     ON projects(status);
CREATE INDEX idx_reviews_project_id  ON reviews(project_id);
CREATE INDEX idx_users_student_index ON users(student_index) WHERE student_index IS NOT NULL;
