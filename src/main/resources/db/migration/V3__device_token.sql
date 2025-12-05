-- Flyway migration: create device_token table
CREATE TABLE device_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    token VARCHAR(255) NOT NULL,
    platform VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_seen DATETIME NULL,

    CONSTRAINT fk_device_token_student FOREIGN KEY (student_id)
        REFERENCES student(student_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Unique constraint on token to avoid duplicates
CREATE UNIQUE INDEX uk_device_token_token ON device_token(token);

-- Index for lookups when sending notifications
CREATE INDEX idx_device_token_student_active ON device_token(student_id, is_active);

