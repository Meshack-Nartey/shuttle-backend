-- ===========================================================
--  SHUTTLE SMART — FULL INITIAL DATABASE SCHEMA (BASELINE)
-- ===========================================================

-- ==========================
-- 1. USERS TABLE
-- ==========================
CREATE TABLE users (
                       user_id INT AUTO_INCREMENT PRIMARY KEY,
                       email VARCHAR(150) UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role ENUM('ROLE_ADMIN','ROLE_STUDENT','ROLE_DRIVER') NOT NULL,
                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ==========================
-- 2. SCHOOL TABLE
-- ==========================
CREATE TABLE school (
                        school_id INT AUTO_INCREMENT PRIMARY KEY,
                        school_name VARCHAR(100) UNIQUE NOT NULL,
                        external_id VARCHAR(64) UNIQUE,
                        map_center_lat DECIMAL(10,8),
                        map_center_lon DECIMAL(11,8),
                        map_image_url TEXT,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ==========================
-- 3. STUDENT TABLE
-- ==========================
CREATE TABLE student (
                         student_id INT AUTO_INCREMENT PRIMARY KEY,
                         user_id INT NOT NULL UNIQUE,
                         username VARCHAR(50) NOT NULL UNIQUE,
                         student_id_number VARCHAR(20) UNIQUE NOT NULL,
                         first_name VARCHAR(50) NOT NULL,
                         last_name VARCHAR(50) NOT NULL,
                         school_id INT NOT NULL,
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT fk_student_user FOREIGN KEY (user_id)
                             REFERENCES users(user_id) ON DELETE CASCADE,

                         CONSTRAINT fk_student_school FOREIGN KEY (school_id)
                             REFERENCES school(school_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ==========================
-- 4. DRIVER TABLE
-- ==========================
CREATE TABLE driver (
                        driver_id INT AUTO_INCREMENT PRIMARY KEY,
                        user_id INT NOT NULL UNIQUE,
                        first_name VARCHAR(50),
                        last_name VARCHAR(50),
                        school_id INT NOT NULL,
                        car_number VARCHAR(20) UNIQUE,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

                        CONSTRAINT fk_driver_user FOREIGN KEY (user_id)
                            REFERENCES users(user_id) ON DELETE CASCADE,

                        CONSTRAINT fk_driver_school FOREIGN KEY (school_id)
                            REFERENCES school(school_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ==========================
-- 5. SHUTTLE TABLE
-- ==========================
CREATE TABLE shuttle (
                         shuttle_id INT AUTO_INCREMENT PRIMARY KEY,
                         license_plate VARCHAR(20) UNIQUE NOT NULL,
                         external_id VARCHAR(64) UNIQUE,
                         capacity INT NOT NULL,
                         status VARCHAR(20) DEFAULT 'Available',
                         school_id INT NOT NULL,
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT fk_shuttle_school FOREIGN KEY (school_id)
                             REFERENCES school(school_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ==========================
-- 6. ROUTE TABLE
-- ==========================
CREATE TABLE route (
                       route_id INT AUTO_INCREMENT PRIMARY KEY,
                       route_name VARCHAR(100) NOT NULL,
                       description TEXT,
                       external_id VARCHAR(50) UNIQUE,
                       school_id INT NOT NULL,
                       polyline_forward JSON NOT NULL,
                       polyline_backward JSON NOT NULL,

                       UNIQUE KEY uk_route_name_school (route_name, school_id),

                       CONSTRAINT fk_route_school FOREIGN KEY (school_id)
                           REFERENCES school(school_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ==========================
-- 7. ROUTE STOP
-- ==========================
CREATE TABLE route_stop (
                            route_stop_id INT AUTO_INCREMENT PRIMARY KEY,
                            route_id INT NOT NULL,
                            stop_name VARCHAR(100) NOT NULL,
                            latitude DECIMAL(10,8) NOT NULL,
                            longitude DECIMAL(11,8) NOT NULL,
                            stop_order INT NOT NULL,
                            direction ENUM('FORWARD','BACKWARD') NOT NULL,

                            UNIQUE KEY uk_route_stop_order (route_id, stop_order, direction),

                            CONSTRAINT fk_route_stop FOREIGN KEY (route_id)
                                REFERENCES route(route_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ==========================
-- 8. LOCATION UPDATES
-- ==========================
CREATE TABLE location_update (
                                 update_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 shuttle_id INT NOT NULL,
                                 latitude DECIMAL(10,8) NOT NULL,
                                 longitude DECIMAL(11,8) NOT NULL,
                                 created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT fk_location_shuttle FOREIGN KEY (shuttle_id)
                                     REFERENCES shuttle(shuttle_id) ON DELETE CASCADE
);

CREATE INDEX idx_location_shuttle_time ON location_update(shuttle_id, created_at DESC);


-- ==========================
-- 9. STUDENT REMINDER
-- ==========================
CREATE TABLE student_reminder (
                                  reminder_id INT AUTO_INCREMENT PRIMARY KEY,
                                  student_id INT NOT NULL,
                                  route_id INT,
                                  target_stop_id INT NOT NULL,
                                  reminder_time_offset INT NOT NULL,
                                  is_active BOOLEAN DEFAULT TRUE,
                                  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_reminder_student FOREIGN KEY (student_id)
                                      REFERENCES student(student_id) ON DELETE CASCADE,

                                  CONSTRAINT fk_reminder_stop FOREIGN KEY (target_stop_id)
                                      REFERENCES route_stop(route_stop_id) ON DELETE CASCADE
);


-- ==========================
-- 10. TRIP ACTIVITY
-- ==========================
CREATE TABLE trip_activity (
                               trip_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               student_id INT NOT NULL,
                               shuttle_id INT NOT NULL,
                               departure_stop_id INT NOT NULL,
                               arrival_stop_id INT NOT NULL,
                               route_id INT NOT NULL,

                               estimated_time DATETIME,
                               actual_time DATETIME,

                               reminder_offset_minutes INT NULL,
                               reminder_scheduled_at DATETIME NULL,
                               notification_sent BOOLEAN DEFAULT FALSE,

                               status ENUM('UPCOMING','NOTIFIED','ONGOING','PAST') DEFAULT 'UPCOMING',

                               CONSTRAINT fk_trip_student FOREIGN KEY (student_id)
                                   REFERENCES student(student_id) ON DELETE CASCADE,

                               CONSTRAINT fk_trip_shuttle FOREIGN KEY (shuttle_id)
                                   REFERENCES shuttle(shuttle_id) ON DELETE RESTRICT,

                               CONSTRAINT fk_trip_depart FOREIGN KEY (departure_stop_id)
                                   REFERENCES route_stop(route_stop_id) ON DELETE RESTRICT,

                               CONSTRAINT fk_trip_arrive FOREIGN KEY (arrival_stop_id)
                                   REFERENCES route_stop(route_stop_id) ON DELETE RESTRICT,

                               CONSTRAINT fk_trip_route FOREIGN KEY (route_id)
                                   REFERENCES route(route_id) ON DELETE RESTRICT
);


-- ==========================
-- 11. DRIVER SESSION
-- ==========================
CREATE TABLE driver_session (
                                session_id INT AUTO_INCREMENT PRIMARY KEY,
                                driver_id INT NOT NULL,
                                shuttle_id INT NOT NULL,
                                route_id INT NULL,
                                started_at TIMESTAMP,
                                ended_at TIMESTAMP,

                                CONSTRAINT fk_session_driver FOREIGN KEY (driver_id)
                                    REFERENCES driver(driver_id) ON DELETE CASCADE,

                                CONSTRAINT fk_session_shuttle FOREIGN KEY (shuttle_id)
                                    REFERENCES shuttle(shuttle_id) ON DELETE CASCADE,

                                CONSTRAINT fk_session_route FOREIGN KEY (route_id)
                                    REFERENCES route(route_id) ON DELETE SET NULL
);


-- ==========================
-- 12. HIGH-PERFORMANCE INDEXES
-- ==========================

-- USERS
CREATE UNIQUE INDEX idx_users_email ON users(email);

-- SCHOOL
CREATE INDEX idx_school_name ON school(school_name);
CREATE UNIQUE INDEX idx_school_external_id ON school(external_id);

-- SHUTTLE
CREATE UNIQUE INDEX idx_shuttle_plate ON shuttle(license_plate);
CREATE UNIQUE INDEX idx_shuttle_external_id ON shuttle(external_id);
CREATE INDEX idx_shuttle_school ON shuttle(school_id);

-- DRIVER
CREATE INDEX idx_driver_school ON driver(school_id);
CREATE INDEX idx_driver_user ON driver(user_id);

-- STUDENT
CREATE UNIQUE INDEX idx_student_user ON student(user_id);
CREATE INDEX idx_student_school ON student(school_id);

-- ROUTE
CREATE INDEX idx_route_school ON route(school_id);
CREATE INDEX idx_route_name_school ON route(route_name, school_id);

-- ROUTE STOP
CREATE INDEX idx_route_stop_route ON route_stop(route_id);
CREATE INDEX idx_route_stop_order ON route_stop(route_id, stop_order, direction);

-- STUDENT REMINDER
CREATE INDEX idx_reminder_student ON student_reminder(student_id);

-- TRIP ACTIVITY
CREATE INDEX idx_trip_student ON trip_activity(student_id);
CREATE INDEX idx_trip_shuttle ON trip_activity(shuttle_id);

-- DRIVER SESSION
CREATE INDEX idx_session_driver ON driver_session(driver_id);
CREATE INDEX idx_session_shuttle ON driver_session(shuttle_id);
CREATE INDEX idx_session_route ON driver_session(route_id);

