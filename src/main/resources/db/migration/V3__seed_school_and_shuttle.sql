INSERT INTO school (school_name, external_id)
VALUES ('Kwame Nkrumah University of Science and Technology', 'KNUST562');

-- Capture generated school_id (assumed 1)
INSERT INTO shuttle (license_plate, capacity, status, school_id, external_id)
VALUES
    ('ABC-1234', 100, 'Available', 1, 'KNUST-SS-01'),
    ('DEF-5678', 20, 'Available', 1, 'KNUST-SS-02');
