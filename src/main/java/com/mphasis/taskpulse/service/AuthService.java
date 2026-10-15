// service/AuthService.java
package com.mphasis.taskpulse.service;

import com.mphasis.taskpulse.dto.LoginRequest;
import com.mphasis.taskpulse.dto.LoginResponse;
import com.mphasis.taskpulse.entity.User;
import com.mphasis.taskpulse.exception.ResourceNotFoundException;
import com.mphasis.taskpulse.repository.UserRepository;
import com.mphasis.taskpulse.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest req) {
        // Throws BadCredentialsException (-> 401) if email/password are wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));

        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return new LoginResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}