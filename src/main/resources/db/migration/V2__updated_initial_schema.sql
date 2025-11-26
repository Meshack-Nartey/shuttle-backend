-- ===========================================================
--  SHUTTLE SMART — INITIAL DATABASE SCHEMA (FULL RESET)
-- ===========================================================

-- ==========================
-- 1. AUTHENTICATION (USERS)
-- ==========================

CREATE TABLE users (
                       user_id INT AUTO_INCREMENT PRIMARY KEY,

    -- login credentials
                       email VARCHAR(150) UNIQUE,
                       password VARCHAR(255) NOT NULL,

    -- who is the user?
                       role ENUM('ROLE_ADMIN', 'ROLE_STUDENT', 'ROLE_DRIVER') NOT NULL,

                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ==========================
-- 2. SCHOOL
-- ==========================

CREATE TABLE school (
                        school_id INT AUTO_INCREMENT PRIMARY KEY,
                        school_name VARCHAR(100) UNIQUE NOT NULL,
                        map_center_lat DECIMAL(10,8),
                        map_center_lon DECIMAL(11,8),
                        map_image_url TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ==========================
-- 3. STUDENT (links to USER)
-- ==========================

CREATE TABLE student (
                         student_id INT AUTO_INCREMENT PRIMARY KEY,

                         user_id INT NOT NULL UNIQUE,

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
-- 4. DRIVER (links to USER)
-- ==========================

CREATE TABLE driver (
                        driver_id INT AUTO_INCREMENT PRIMARY KEY,

                        user_id INT NOT NULL UNIQUE,

                        car_number VARCHAR(20) UNIQUE NOT NULL,
                        first_name VARCHAR(50),
                        last_name VARCHAR(50),

                        school_id INT NOT NULL,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

                        CONSTRAINT fk_driver_user FOREIGN KEY (user_id)
                            REFERENCES users(user_id) ON DELETE CASCADE,

                        CONSTRAINT fk_driver_school FOREIGN KEY (school_id)
                            REFERENCES school(school_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ==========================
-- 5. SHUTTLE
-- ==========================

CREATE TABLE shuttle (
                         shuttle_id INT AUTO_INCREMENT PRIMARY KEY,
                         license_plate VARCHAR(20) UNIQUE NOT NULL,
                         capacity INT NOT NULL,
                         status VARCHAR(20) DEFAULT 'Available',
                         school_id INT NOT NULL,
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT fk_shuttle_school FOREIGN KEY (school_id)
                             REFERENCES school(school_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ==========================
-- 6. ROUTE
-- ==========================

CREATE TABLE route (
                       route_id INT AUTO_INCREMENT PRIMARY KEY,

                       route_name VARCHAR(100) NOT NULL,
                       description TEXT,

                       school_id INT NOT NULL,

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

                            UNIQUE KEY uk_route_stop_order (route_id, stop_order),

                            CONSTRAINT fk_route_stop FOREIGN KEY (route_id)
                                REFERENCES route(route_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ==========================
-- 8. LOCATION UPDATE (real-time tracking)
-- ==========================

CREATE TABLE location_update (
                                 update_id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                 shuttle_id INT NOT NULL,
                                 latitude DECIMAL(10,8) NOT NULL,
                                 longitude DECIMAL(11,8) NOT NULL,
                                 created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT fk_location_shuttle FOREIGN KEY (shuttle_id)
                                     REFERENCES shuttle(shuttle_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_location_update ON location_update(shuttle_id, created_at);


-- ==========================
-- 9. STUDENT REMINDERS
-- ==========================

CREATE TABLE student_reminder (
                                  reminder_id INT AUTO_INCREMENT PRIMARY KEY,

                                  student_id INT NOT NULL,
                                  route_id INT,
                                  target_stop_id INT NOT NULL,
                                  reminder_time_offset INT NOT NULL, -- minutes before arrival
                                  is_active BOOLEAN DEFAULT TRUE,
                                  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_reminder_student FOREIGN KEY (student_id)
                                      REFERENCES student(student_id) ON DELETE CASCADE,

                                  CONSTRAINT fk_reminder_route_stop FOREIGN KEY (target_stop_id)
                                      REFERENCES route_stop(route_stop_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ==========================
-- 10. TRIP ACTIVITY (history)
-- ==========================

CREATE TABLE trip_activity (
                               trip_id BIGINT AUTO_INCREMENT PRIMARY KEY,

                               student_id INT NOT NULL,
                               shuttle_id INT NOT NULL,
                               departure_stop_id INT NOT NULL,
                               arrival_stop_id INT NOT NULL,
                               route_id INT,

                               estimated_time DATETIME,
                               actual_time DATETIME,
                               status VARCHAR(20) DEFAULT 'Upcoming',

                               CONSTRAINT fk_trip_student FOREIGN KEY (student_id)
                                   REFERENCES student(student_id) ON DELETE CASCADE,

                               CONSTRAINT fk_trip_shuttle FOREIGN KEY (shuttle_id)
                                   REFERENCES shuttle(shuttle_id) ON DELETE RESTRICT,

                               CONSTRAINT fk_depart_stop FOREIGN KEY (departure_stop_id)
                                   REFERENCES route_stop(route_stop_id) ON DELETE RESTRICT,

                               CONSTRAINT fk_arrival_stop FOREIGN KEY (arrival_stop_id)
                                   REFERENCES route_stop(route_stop_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

