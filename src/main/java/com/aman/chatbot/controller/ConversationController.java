package com.aman.chatbot.controller;

import com.aman.chatbot.entity.Conversation;
import com.aman.chatbot.entity.Message;
import com.aman.chatbot.repository.ConversationRepository;
import com.aman.chatbot.service.ConversationService;
import com.aman.chatbot.service.MessageService;
import com.aman.chatbot.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final MessageService messageService;
    private final AuthUtil authUtil;

    @PostMapping
    public Conversation createConversation() {

        Long userId = authUtil.getCurrentUserId();

        return conversationService.createConversation(userId);
    }

    @GetMapping
    public List<Conversation> getUserConversations() {

        Long userId = authUtil.getCurrentUserId();

        return conversationService.getUserConversations(userId);
    }

    @GetMapping("/{id}/messages")
    public List<Message> getMessages(@PathVariable Long id) {

        Long userId = authUtil.getCurrentUserId();

        // OPTIONAL: move this validation into service later
        return messageService.getMessages(id);
    }

    // ✅ ADD DELETE API HERE
    @DeleteMapping("/{id}")
    public String deleteConversation(@PathVariable Long id) {

        Long userId = authUtil.getCurrentUserId();

        conversationService.deleteConversation(id, userId);

        return "Conversation deleted successfully";
    }
}