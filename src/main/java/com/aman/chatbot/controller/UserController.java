package com.aman.chatbot.controller;

import com.aman.chatbot.dto.UserResponse;
import com.aman.chatbot.entity.User;
import com.aman.chatbot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse getProfile(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}