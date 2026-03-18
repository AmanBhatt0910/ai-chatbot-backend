package com.aman.chatbot.service;

import com.aman.chatbot.entity.Conversation;
import com.aman.chatbot.repository.ConversationRepository;
import com.aman.chatbot.repository.MessageRepository;
import com.aman.chatbot.util.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
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

    @Transactional
    public void deleteConversation(Long conversationId, Long userId) {

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (!conversation.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        messageRepository.deleteByConversationId(conversationId);

        conversationRepository.delete(conversation);
    }

    public void setCategory(Long conversationId, String category) {
        Conversation convo = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        convo.setCategory(category);
        conversationRepository.save(convo);
    }
}