-- 1. Make route_id NOT NULL
ALTER TABLE trip_activity
    MODIFY COLUMN route_id INT NOT NULL;

-- 2. Add reminder fields
ALTER TABLE trip_activity
    ADD COLUMN reminder_offset_minutes INT NULL,
    ADD COLUMN reminder_scheduled_at DATETIME NULL,
    ADD COLUMN notification_sent BOOLEAN DEFAULT FALSE;

-- 3. Convert status to ENUM for consistency
ALTER TABLE trip_activity
    MODIFY COLUMN status ENUM('UPCOMING', 'NOTIFIED', 'ONGOING', 'PAST')
    DEFAULT 'UPCOMING';

-- 4. Ensure FK constraint for route_id exists
ALTER TABLE trip_activity
    ADD CONSTRAINT fk_trip_activity_route
        FOREIGN KEY (route_id)
            REFERENCES route(route_id)
            ON DELETE RESTRICT;
