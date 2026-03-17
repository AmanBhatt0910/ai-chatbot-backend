package com.aman.chatbot.controller;

import com.aman.chatbot.dto.ChatMessage;
import com.aman.chatbot.entity.Conversation;
import com.aman.chatbot.entity.Message;
import com.aman.chatbot.entity.User;
import com.aman.chatbot.repository.ConversationRepository;
import com.aman.chatbot.service.AiService;
import com.aman.chatbot.service.MessageService;
import com.aman.chatbot.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final MessageService messageService;
    private final AiService aiService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationRepository conversationRepository;

    @MessageMapping("/chat")
    public void handleChat(ChatMessage chatMessage, Principal principal) {

        // 🔐 Extract user from WebSocket principal
        UsernamePasswordAuthenticationToken auth =
                (UsernamePasswordAuthenticationToken) principal;

        User user = (User) auth.getPrincipal();
        Long userId = user.getId();

        Conversation conversation;

        // ✅ AUTO-CREATE CONVERSATION
        if (chatMessage.getConversationId() == null) {

            conversation = Conversation.builder()
                    .createdAt(LocalDateTime.now())
                    .title("New Chat")
                    .userId(userId)
                    .build();

            conversation = conversationRepository.save(conversation);

            chatMessage.setConversationId(conversation.getId());

        } else {

            conversation = conversationRepository
                    .findById(chatMessage.getConversationId())
                    .orElseThrow();

            if (!conversation.getUserId().equals(userId)) {
                throw new RuntimeException("Unauthorized");
            }
        }

        // FORCE senderId (backend-controlled)
        chatMessage.setSenderId(userId);
        chatMessage.setType("USER");

        // 1. Save USER message
        Message savedUserMessage = messageService.saveMessage(chatMessage);

        // 2. Broadcast USER message
        messagingTemplate.convertAndSend(
                "/topic/conversations/" + chatMessage.getConversationId(),
                savedUserMessage
        );

        // 3. Generate AI response
        String aiResponse = aiService.generateResponse(chatMessage.getContent());

        // 4. Create AI message
        ChatMessage aiMessage = ChatMessage.builder()
                .senderId(0L) // AI
                .conversationId(chatMessage.getConversationId())
                .content(aiResponse)
                .type("AI")
                .timestamp(LocalDateTime.now())
                .build();

        // 5. Save AI message
        Message savedAiMessage = messageService.saveMessage(aiMessage);

        // 6. Broadcast AI message
        messagingTemplate.convertAndSend(
                "/topic/conversations/" + chatMessage.getConversationId(),
                savedAiMessage
        );
    }
}