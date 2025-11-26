ALTER TABLE shuttle_backend_new.driver_session
ADD COLUMN route_id INT NULL,
ADD CONSTRAINT fk_driver_session_route FOREIGN KEY (route_id) REFERENCES shuttle_backend_new.route(route_id) ON DELETE SET NULL;

