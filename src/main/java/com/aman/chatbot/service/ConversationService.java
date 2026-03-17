package com.aman.chatbot.service;

import com.aman.chatbot.entity.Conversation;
import com.aman.chatbot.repository.ConversationRepository;
import com.aman.chatbot.repository.MessageRepository;
import com.aman.chatbot.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationService conversationService;
    private final AuthUtil authUtil;
    private final MessageRepository messageRepository;

    public Conversation createConversation(Long userId) {
        Conversation conversation = Conversation.builder()
                .createdAt(LocalDateTime.now())
                .title("New Chat")
                .userId(userId)
                .build();

        return conversationRepository.save(conversation);
    }

    public List<Conversation> getUserConversations(Long userId) {
        return conversationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void deleteConversation(Long conversationId, Long userId) {

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // 🔐 SECURITY CHECK
        if (!conversation.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        // 🧹 DELETE MESSAGES FIRST
        messageRepository.deleteByConversationId(conversationId);

        // 🗑 DELETE CONVERSATION
        conversationRepository.delete(conversation);
    }
}