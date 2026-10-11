-- Create submission_settings table for managing submission deadlines
CREATE TABLE submission_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    deadline TIMESTAMP NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Ensure only one row exists
ALTER TABLE submission_settings ADD CONSTRAINT only_one_row CHECK (id = id);

-- Insert default row
INSERT INTO submission_settings (deadline, updated_at)
VALUES (NULL, CURRENT_TIMESTAMP);

