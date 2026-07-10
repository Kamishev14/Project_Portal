-- V2__seed_data.sql
-- Seed a default professor account (password: professor123)
-- BCrypt hash for "professor123"
INSERT INTO users (id, name, email, password, role)
VALUES (
    gen_random_uuid(),
    'Prof. Johnson',
    'professor@uni.edu',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'PROFESSOR'
);
