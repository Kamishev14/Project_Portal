ALTER TABLE projects
    ADD COLUMN preferred_professor_id UUID REFERENCES users(id) ON DELETE SET NULL;

CREATE INDEX idx_projects_preferred_professor_id
    ON projects(preferred_professor_id);

CREATE TABLE project_participants (
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (project_id, user_id)
);

CREATE INDEX idx_project_participants_user_id
    ON project_participants(user_id);

