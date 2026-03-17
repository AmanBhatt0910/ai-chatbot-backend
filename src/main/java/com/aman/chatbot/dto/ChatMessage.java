package com.aman.chatbot.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    private Long senderId;
    private Long conversationId;
    private String content;
    private String type; // USER or AI
    private LocalDateTime timestamp;
}