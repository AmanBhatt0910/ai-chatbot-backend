package com.aman.chatbot.config;

import com.aman.chatbot.entity.User;
import com.aman.chatbot.repository.UserRepository;
import com.aman.chatbot.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {

                String token = authHeader.substring(7);

                String username = jwtUtil.extractUsername(token);

                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                null
                        );

                accessor.setUser(auth);
            }
        }

        return message;
    }
}