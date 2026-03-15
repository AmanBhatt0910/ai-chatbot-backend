package com.aman.chatbot.controller;

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
    public User getProfile(Authentication authentication) {
        String username = authentication.getName();
        return userService.getUserByUsername(username);
    }
}