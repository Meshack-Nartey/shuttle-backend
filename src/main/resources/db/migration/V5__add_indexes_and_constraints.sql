-- ================================================
-- V5 — HIGH PERFORMANCE INDEX OPTIMIZATION
-- ================================================

-- ===================================
-- USERS TABLE
-- ===================================
-- Email lookup during login
CREATE UNIQUE INDEX idx_users_email ON users(email);


-- ===================================
-- SCHOOL TABLE
-- ===================================
-- Searching schools by name
CREATE INDEX idx_school_name ON school(school_name);
-- External ID lookups (your frontend uses these)
CREATE UNIQUE INDEX idx_school_external_id ON school(external_id);


-- ===================================
-- SHUTTLE TABLE
-- ===================================
-- Speed up shuttle license plate lookups
CREATE UNIQUE INDEX idx_shuttle_plate ON shuttle(license_plate);
-- Fast shuttle external ID lookup
CREATE UNIQUE INDEX idx_shuttle_external_id ON shuttle(external_id);

-- Optimize queries by school_id
CREATE INDEX idx_shuttle_school ON shuttle(school_id);


-- ===================================
-- DRIVER TABLE
-- ===================================
CREATE INDEX idx_driver_school ON driver(school_id);
CREATE INDEX idx_driver_user ON driver(user_id);


-- ===================================
-- STUDENT TABLE
-- ===================================
-- Student login / profile fetch
CREATE UNIQUE INDEX idx_student_user ON student(user_id);
-- Filter students in a school
CREATE INDEX idx_student_school ON student(school_id);


-- ===================================
-- ROUTE TABLE
-- ===================================
CREATE INDEX idx_route_school ON route(school_id);
CREATE INDEX idx_route_name_school ON route(route_name, school_id);


-- ===================================
-- ROUTE STOP TABLE
-- ===================================
CREATE INDEX idx_route_stop_route ON route_stop(route_id);
CREATE INDEX idx_route_stop_order ON route_stop(route_id, stop_order);


-- ===================================
-- LOCATION UPDATE
-- ===================================
-- Critical for fast ETA calculations
CREATE INDEX idx_location_shuttle_time ON location_update(shuttle_id, created_at DESC);


-- ===================================
-- STUDENT REMINDER TABLE
-- ===================================
CREATE INDEX idx_reminder_student ON student_reminder(student_id);


-- ===================================
-- TRIP ACTIVITY TABLE
-- ===================================
CREATE INDEX idx_trip_student ON trip_activity(student_id);
CREATE INDEX idx_trip_shuttle ON trip_activity(shuttle_id);


-- ===================================
-- DRIVER SESSION TABLE
-- ===================================
CREATE INDEX idx_session_driver ON driver_session(driver_id);
CREATE INDEX idx_session_shuttle ON driver_session(shuttle_id);
CREATE INDEX idx_session_route ON driver_session(route_id);
