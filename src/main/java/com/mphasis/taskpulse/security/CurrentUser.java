// security/CurrentUser.java
package com.mphasis.taskpulse.security;

import com.mphasis.taskpulse.entity.User;
import com.mphasis.taskpulse.exception.ResourceNotFoundException;
import com.mphasis.taskpulse.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Gives services the logged-in User entity, found from the email inside the JWT. */
@Component
public class CurrentUser {

    private final UserRepository userRepository;

    public CurrentUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User get() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged-in user not found"));
    }
}