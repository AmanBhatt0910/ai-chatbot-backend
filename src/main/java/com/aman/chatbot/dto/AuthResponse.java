package com.aman.chatbot.dto;

import com.aman.chatbot.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;

    // Included so the frontend authStore can populate user state immediately
    // after login/register without a separate /api/users/me call
    private UserPayload user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPayload {
        private Long id;
        private String username;
        private String email;
    }

    // Convenience constructor to build from a User entity
    public static AuthResponse of(String token, User user) {
        return new AuthResponse(
                token,
                new UserPayload(user.getId(), user.getUsername(), user.getEmail())
        );
    }
}