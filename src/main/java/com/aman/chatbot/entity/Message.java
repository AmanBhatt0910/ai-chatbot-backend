package com.aman.chatbot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long senderId;

    private Long conversationId;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime timestamp;

    private String type;
    // User or AI
}
