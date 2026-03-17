package com.aman.chatbot.service;

import com.aman.chatbot.dto.AuthResponse;
import com.aman.chatbot.dto.LoginRequest;
import com.aman.chatbot.dto.RegisterRequest;
import com.aman.chatbot.entity.User;
import com.aman.chatbot.repository.UserRepository;
import com.aman.chatbot.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername());

        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {

        System.out.println(request.getIdentifier());

        User user = userRepository
                .findByUsernameOrEmail(request.getIdentifier(), request.getIdentifier())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Invalid username/email or password"
                        ));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid username/email or password"
            );
        }

        String token = jwtUtil.generateToken(user.getUsername());

        return new AuthResponse(token);
    }
}