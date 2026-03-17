package com.aman.chatbot.service;

import com.aman.chatbot.dto.ChatMessage;
import com.aman.chatbot.entity.Message;
import com.aman.chatbot.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    public List<Message> getMessages(Long conversationId) {
        return messageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
    }

    public Message saveMessage(ChatMessage chatMessage) {

        Message message = Message.builder()
                .senderId(chatMessage.getSenderId())
                .conversationId(chatMessage.getConversationId())
                .content(chatMessage.getContent())
                .type(chatMessage.getType())
                .timestamp(LocalDateTime.now())
                .build();

        return messageRepository.save(message);
    }
}
