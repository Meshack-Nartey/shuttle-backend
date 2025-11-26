package com.shuttlebackend.services;

import com.shuttlebackend.entities.User;
import com.shuttlebackend.repositories.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(String email, String rawPassword, String roleStr) {
        if (email != null && userRepo.existsByEmail(email)) {
            throw new RuntimeException("Email already in use");
        }
        User u = new User();
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setRole(roleStr);
        return userRepo.save(u);
    }

    public Optional<User> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public void changePassword(Integer userId, String oldPassword, String newPassword) {
        User u = userRepo.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (!passwordEncoder.matches(oldPassword, u.getPassword())) {
            throw new RuntimeException("Old password incorrect");
        }
        u.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(u);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserRepository.UserAuthView av = userRepo.findAuthByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        GrantedAuthority authority = new SimpleGrantedAuthority(av.getRole());
        return org.springframework.security.core.userdetails.User.builder()
                .username(av.getEmail())
                .password(av.getPassword())
                .authorities(List.of(authority))
                .build();
    }
}
