package com.shuttlebackend.services;

import com.shuttlebackend.dtos.RegisterDriverRequest;
import com.shuttlebackend.entities.Driver;
import com.shuttlebackend.entities.User;
import com.shuttlebackend.repositories.DriverRepository;
import com.shuttlebackend.repositories.SchoolRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class DriverService {

    private final DriverRepository driverRepo;
    private final UserService userService;
    private final SchoolRepository schoolRepo;

    /**
     * Register a driver (admin or public depending on your policies).
     * Driver does NOT contain a carNumber — shuttles are separate entities.
     */
    public Driver registerDriver(RegisterDriverRequest dto) {

        if (dto.getEmail() != null && userService.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        User user = userService.createUser(dto.getEmail(), dto.getPassword(), "ROLE_DRIVER");

        Driver driver = new Driver();
        driver.setUser(user);
        driver.setFirstName(dto.getFirstName());
        driver.setLastName(dto.getLastName());
        driver.setSchool(
                schoolRepo.findById(dto.getSchoolId().intValue())
                        .orElseThrow(() -> new RuntimeException("School not found"))
        );

        return driverRepo.save(driver);
    }

    /**
     * Find the Driver by the user's email.
     * Used later when creating a driver session or fetching driver info.
     */
    public Optional<Driver> findDriverByUserEmail(String email) {
        return driverRepo.findByUser_Email(email);
    }


    public Optional<Driver> findDriverByEmail(String email) {
        return driverRepo.findByUser_Email(email);
    }

}
