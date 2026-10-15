// config/DataSeeder.java
package com.mphasis.taskpulse.config;

import com.mphasis.taskpulse.entity.User;
import com.mphasis.taskpulse.enums.Role;
import com.mphasis.taskpulse.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("manager@taskpulse.com")) {
            User manager = new User();
            manager.setName("Default Manager");
            manager.setEmail("manager@taskpulse.com");
            manager.setPassword(passwordEncoder.encode("Manager@123"));
            manager.setRole(Role.MANAGER);
            userRepository.save(manager);
            System.out.println(">>> Default manager created: manager@taskpulse.com / Manager@123");
        }
    }
}