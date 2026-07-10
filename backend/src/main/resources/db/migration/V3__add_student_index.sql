-- V3__add_student_index.sql
-- student_index was moved into V1__init_schema.sql (combined during refactor).
-- This file is intentionally left as a no-op so Flyway history stays consistent
-- on fresh installs. If you ran V1+V2 before this was added, run manually:
-- ALTER TABLE users ADD COLUMN IF NOT EXISTS student_index CHAR(6) UNIQUE;
-- ALTER TABLE users ADD CONSTRAINT IF NOT EXISTS chk_student_index_format
--     CHECK (student_index IS NULL OR student_index ~ '^\d{6}$');
SELECT 1;
