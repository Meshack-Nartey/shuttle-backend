-- sql
-- 1. SCHOOL ENTITY
CREATE TABLE School (
                        school_id INT AUTO_INCREMENT PRIMARY KEY,
                        school_name VARCHAR(100) UNIQUE NOT NULL,
                        map_center_lat DECIMAL(10,8),
                        map_center_lon DECIMAL(11,8),
                        map_image_url TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 2. SHUTTLE & DRIVER ENTITIES
CREATE TABLE Shuttle (
                         shuttle_id INT AUTO_INCREMENT PRIMARY KEY,
                         license_plate VARCHAR(20) UNIQUE NOT NULL,
                         capacity INT NOT NULL,
                         status VARCHAR(20) DEFAULT 'Available' NOT NULL,
                         school_id INT NOT NULL,
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         CONSTRAINT fk_school_shuttle
                             FOREIGN KEY (school_id) REFERENCES School(school_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE Driver (
                        car_number VARCHAR(20) PRIMARY KEY,
                        password_hash VARCHAR(255) NOT NULL,
                        shuttle_id INT,
                        school_id INT NOT NULL,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT fk_shuttle_driver
                            FOREIGN KEY (shuttle_id) REFERENCES Shuttle(shuttle_id) ON DELETE SET NULL,
                        CONSTRAINT fk_school_driver
                            FOREIGN KEY (school_id) REFERENCES School(school_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 3. STUDENT ENTITY
CREATE TABLE Student (
                         student_id INT AUTO_INCREMENT PRIMARY KEY,
                         first_name VARCHAR(50) NOT NULL,
                         last_name VARCHAR(50) NOT NULL,
                         email VARCHAR(100) UNIQUE NOT NULL,
                         password_hash VARCHAR(255) NOT NULL,
                         school_id INT NOT NULL,
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         CONSTRAINT fk_school_student
                             FOREIGN KEY (school_id) REFERENCES School(school_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 4. ROUTE ENTITIES
CREATE TABLE Route (
                       route_id INT AUTO_INCREMENT PRIMARY KEY,
                       route_name VARCHAR(100) NOT NULL,
                       description TEXT,
                       school_id INT NOT NULL,
                       CONSTRAINT fk_school_route
                           FOREIGN KEY (school_id) REFERENCES School(school_id) ON DELETE CASCADE,
                       UNIQUE KEY uk_route_name_school (route_name, school_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE Route_Stop (
                            route_stop_id INT AUTO_INCREMENT PRIMARY KEY,
                            route_id INT NOT NULL,
                            stop_name VARCHAR(100) NOT NULL,
                            latitude DECIMAL(10,8) NOT NULL,
                            longitude DECIMAL(11,8) NOT NULL,
                            stop_order INT NOT NULL,
                            CONSTRAINT fk_route_stop
                                FOREIGN KEY (route_id) REFERENCES Route(route_id) ON DELETE CASCADE,
                            UNIQUE KEY uk_route_stop_order (route_id, stop_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 5. REAL-TIME TRACKING & ACTIVITY
CREATE TABLE Location_Update (
                                 update_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 shuttle_id INT NOT NULL,
                                 latitude DECIMAL(10,8) NOT NULL,
                                 longitude DECIMAL(11,8) NOT NULL,
                                 created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                 CONSTRAINT fk_shuttle_location
                                     FOREIGN KEY (shuttle_id) REFERENCES Shuttle(shuttle_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE INDEX idx_location_shuttle_time ON Location_Update (shuttle_id, created_at);


CREATE TABLE Driver_Message (
                                message_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                driver_car_number VARCHAR(20) NOT NULL,
                                shuttle_id INT NOT NULL,
                                message_text TEXT NOT NULL,
                                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                CONSTRAINT fk_driver_message
                                    FOREIGN KEY (driver_car_number) REFERENCES Driver(car_number) ON DELETE CASCADE,
                                CONSTRAINT fk_shuttle_message
                                    FOREIGN KEY (shuttle_id) REFERENCES Shuttle(shuttle_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE Student_Reminder (
                                  reminder_id INT AUTO_INCREMENT PRIMARY KEY,
                                  student_id INT NOT NULL,
                                  route_id INT,
                                  target_stop_id INT NOT NULL,
                                  reminder_time_offset INT NOT NULL,
                                  is_active BOOLEAN DEFAULT TRUE NOT NULL,
                                  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                  CONSTRAINT fk_student_reminder
                                      FOREIGN KEY (student_id) REFERENCES Student(student_id) ON DELETE CASCADE,
                                  CONSTRAINT fk_route_stop_reminder
                                      FOREIGN KEY (target_stop_id) REFERENCES Route_Stop(route_stop_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE Trip_Activity (
                               trip_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               student_id INT NOT NULL,
                               shuttle_id INT NOT NULL,
                               departure_stop_id INT NOT NULL,
                               arrival_stop_id INT NOT NULL,
                               route_id INT,
                               estimated_time DATETIME,
                               actual_time DATETIME,
                               status VARCHAR(20) DEFAULT 'Upcoming' NOT NULL,
                               CONSTRAINT fk_student_trip
                                   FOREIGN KEY (student_id) REFERENCES Student(student_id) ON DELETE CASCADE,
                               CONSTRAINT fk_shuttle_trip
                                   FOREIGN KEY (shuttle_id) REFERENCES Shuttle(shuttle_id) ON DELETE RESTRICT,
                               CONSTRAINT fk_departure_stop
                                   FOREIGN KEY (departure_stop_id) REFERENCES Route_Stop(route_stop_id) ON DELETE RESTRICT,
                               CONSTRAINT fk_arrival_stop
                                   FOREIGN KEY (arrival_stop_id) REFERENCES Route_Stop(route_stop_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

