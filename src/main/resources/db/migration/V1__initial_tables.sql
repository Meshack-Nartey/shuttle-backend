-- ======================================
--  V1 — INITIAL TABLES (MATCHES ENTITIES)
-- ======================================

CREATE TABLE users (
                       user_id INT AUTO_INCREMENT PRIMARY KEY,
                       email VARCHAR(150) UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(30) NOT NULL,
                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE school (
                        school_id INT AUTO_INCREMENT PRIMARY KEY,
                        school_name VARCHAR(100) NOT NULL,
                        map_center_lat DECIMAL(10,8),
                        map_center_lon DECIMAL(11,8),
                        map_image_url TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE student (
                         student_id INT AUTO_INCREMENT PRIMARY KEY,
                         user_id INT NOT NULL UNIQUE,
                         student_id_number VARCHAR(20) UNIQUE NOT NULL,
                         first_name VARCHAR(50) NOT NULL,
                         last_name VARCHAR(50) NOT NULL,
                         school_id INT NOT NULL,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                         FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                         FOREIGN KEY (school_id) REFERENCES school(school_id) ON DELETE RESTRICT
);

CREATE TABLE driver (
                        driver_id INT AUTO_INCREMENT PRIMARY KEY,
                        user_id INT NOT NULL UNIQUE,
                        first_name VARCHAR(50),
                        last_name VARCHAR(50),
                        school_id INT NOT NULL,
                        car_number VARCHAR(20) UNIQUE,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                        FOREIGN KEY (school_id) REFERENCES school(school_id) ON DELETE RESTRICT
);

CREATE TABLE shuttle (
                         shuttle_id INT AUTO_INCREMENT PRIMARY KEY,
                         license_plate VARCHAR(20) UNIQUE NOT NULL,
                         capacity INT NOT NULL,
                         status VARCHAR(20) DEFAULT 'Available',
                         school_id INT NOT NULL,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                         FOREIGN KEY (school_id) REFERENCES school(school_id) ON DELETE RESTRICT
);

CREATE TABLE route (
                       route_id INT AUTO_INCREMENT PRIMARY KEY,
                       route_name VARCHAR(100) NOT NULL,
                       description TEXT,
                       school_id INT NOT NULL,

                       UNIQUE KEY uk_route_school (route_name, school_id),
                       FOREIGN KEY (school_id) REFERENCES school(school_id) ON DELETE CASCADE
);

CREATE TABLE route_stop (
                            route_stop_id INT AUTO_INCREMENT PRIMARY KEY,
                            route_id INT NOT NULL,
                            stop_name VARCHAR(100) NOT NULL,
                            latitude DECIMAL(10,8) NOT NULL,
                            longitude DECIMAL(11,8) NOT NULL,
                            stop_order INT NOT NULL,

                            UNIQUE KEY uk_route_stop_order (route_id, stop_order),
                            FOREIGN KEY (route_id) REFERENCES route(route_id) ON DELETE CASCADE
);

CREATE TABLE location_update (
                                 update_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 shuttle_id INT NOT NULL,
                                 latitude DECIMAL(10,8) NOT NULL,
                                 longitude DECIMAL(11,8) NOT NULL,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                 FOREIGN KEY (shuttle_id) REFERENCES shuttle(shuttle_id) ON DELETE CASCADE
);

CREATE INDEX idx_location_update ON location_update(shuttle_id, created_at);

CREATE TABLE student_reminder (
                                  reminder_id INT AUTO_INCREMENT PRIMARY KEY,
                                  student_id INT NOT NULL,
                                  route_id INT,
                                  target_stop_id INT NOT NULL,
                                  reminder_time_offset INT NOT NULL,
                                  is_active BOOLEAN DEFAULT TRUE,
                                  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

                                  FOREIGN KEY (student_id) REFERENCES student(student_id) ON DELETE CASCADE,
                                  FOREIGN KEY (target_stop_id) REFERENCES route_stop(route_stop_id) ON DELETE CASCADE
);

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

                               FOREIGN KEY (student_id) REFERENCES student(student_id) ON DELETE CASCADE,
                               FOREIGN KEY (shuttle_id) REFERENCES shuttle(shuttle_id) ON DELETE RESTRICT,
                               FOREIGN KEY (departure_stop_id) REFERENCES route_stop(route_stop_id) ON DELETE RESTRICT,
                               FOREIGN KEY (arrival_stop_id) REFERENCES route_stop(route_stop_id) ON DELETE RESTRICT
);

CREATE TABLE driver_session (
                                session_id INT AUTO_INCREMENT PRIMARY KEY,
                                driver_id INT NOT NULL,
                                shuttle_id INT NOT NULL,
                                route_id INT NOT NULL,
                                started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                ended_at TIMESTAMP NULL,

                                FOREIGN KEY (driver_id) REFERENCES driver(driver_id) ON DELETE CASCADE,
                                FOREIGN KEY (shuttle_id) REFERENCES shuttle(shuttle_id) ON DELETE CASCADE,
                                FOREIGN KEY (route_id) REFERENCES route(route_id) ON DELETE RESTRICT
);
