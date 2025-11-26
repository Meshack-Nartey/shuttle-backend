CREATE TABLE shuttle_backend_new.driver_session (
                                                    session_id INT AUTO_INCREMENT PRIMARY KEY,
                                                    driver_id INT NOT NULL,
                                                    shuttle_id INT NOT NULL,
                                                    started_at TIMESTAMP,
                                                    ended_at TIMESTAMP,

                                                    CONSTRAINT fk_driver_session_driver
                                                        FOREIGN KEY (driver_id) REFERENCES shuttle_backend_new.driver(driver_id)
                                                            ON DELETE CASCADE,

                                                    CONSTRAINT fk_driver_session_shuttle
                                                        FOREIGN KEY (shuttle_id) REFERENCES shuttle_backend_new.shuttle(shuttle_id)
                                                            ON DELETE CASCADE
);
