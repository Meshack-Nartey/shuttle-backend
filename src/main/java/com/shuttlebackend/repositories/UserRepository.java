package com.shuttlebackend.repositories;

import com.shuttlebackend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("select u.email as email, u.password as password, u.role as role from User u where u.email = :email")
    Optional<UserAuthView> findAuthByEmail(String email);

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    interface UserAuthView {
        String getEmail();
        String getPassword();
        String getRole();
    }
}
