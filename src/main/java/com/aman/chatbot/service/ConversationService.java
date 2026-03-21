package com.aman.chatbot.service;

import com.aman.chatbot.entity.Conversation;
import com.aman.chatbot.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;

    /**
     * These are UI suggestions shown as quick-pick chips.
     * They are NOT constraints — users can type any category they want.
     * The AI will adapt to whatever category is set.
     * Add or remove suggestions freely; the AI handles any value.
     */
    private static final List<String> CATEGORY_SUGGESTIONS = List.of(
            "Fitness",
            "Tech",
            "Finance",
            "Cooking",
            "History",
            "Gaming",
            "Science",
            "Travel"
    );

    public List<String> getAvailableCategories() {
        return CATEGORY_SUGGESTIONS;
    }

    public Conversation createConversation(Long userId) {
        Conversation conversation = Conversation.builder()
                .userId(userId)
                .createdAt(LocalDateTime.now())
                .title("New Chat")
                .build();
        return conversationRepository.save(conversation);
    }

    public List<Conversation> getUserConversations(Long userId) {
        return conversationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void deleteConversation(Long id, Long userId) {
        conversationRepository.deleteById(id);
    }

    public void setCategory(Long id, String category) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        conversation.setCategory(category);
        conversation.setTitle(category); // update sidebar title to match category
        conversationRepository.save(conversation);
    }
}