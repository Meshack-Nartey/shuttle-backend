-- Create table to persist issued refresh tokens
CREATE TABLE shuttle_backend_new.refresh_token (
    id INT AUTO_INCREMENT PRIMARY KEY,
    jti VARCHAR(255) NOT NULL UNIQUE,
    user_email VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP,
    revoked TINYINT(1) DEFAULT 0
);

-- Create table to store blacklisted access token JTIs
CREATE TABLE shuttle_backend_new.blacklisted_token (
    id INT AUTO_INCREMENT PRIMARY KEY,
    jti VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP
);

