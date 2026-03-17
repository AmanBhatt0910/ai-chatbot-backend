package com.aman.chatbot.controller;

import com.aman.chatbot.dto.ChatMessage;
import com.aman.chatbot.entity.Conversation;
import com.aman.chatbot.entity.Message;
import com.aman.chatbot.entity.User;
import com.aman.chatbot.repository.ConversationRepository;
import com.aman.chatbot.service.AiService;
import com.aman.chatbot.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final MessageService messageService;
    private final AiService aiService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationRepository conversationRepository;

    @MessageMapping("/chat")
    public void handleChat(
            ChatMessage chatMessage,
            java.security.Principal principal
    ) {

        // ✅ ALWAYS use Principal (Spring injects it correctly)
        if (principal == null) {
            throw new RuntimeException("Unauthorized: No WebSocket user");
        }

        System.out.println("Principal: " + principal);

        UsernamePasswordAuthenticationToken auth =
                (UsernamePasswordAuthenticationToken) principal;

        User user = (User) auth.getPrincipal();
        Long userId = user.getId();

        Conversation conversation;

        // ✅ AUTO CREATE CONVERSATION
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
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));

            if (!conversation.getUserId().equals(userId)) {
                throw new RuntimeException("Unauthorized");
            }
        }

        // ✅ USER MESSAGE
        chatMessage.setSenderId(userId);
        chatMessage.setType("USER");

        Message savedUserMessage = messageService.saveMessage(chatMessage);

        messagingTemplate.convertAndSend(
                "/topic/conversations/" + chatMessage.getConversationId(),
                savedUserMessage
        );

        // ✅ AI RESPONSE
        String aiResponse = aiService.generateResponse(chatMessage.getContent());

        ChatMessage aiMessage = ChatMessage.builder()
                .senderId(0L)
                .conversationId(chatMessage.getConversationId())
                .content(aiResponse)
                .type("AI")
                .timestamp(LocalDateTime.now())
                .build();

        Message savedAiMessage = messageService.saveMessage(aiMessage);

        messagingTemplate.convertAndSend(
                "/topic/conversations/" + chatMessage.getConversationId(),
                savedAiMessage
        );
    }
}