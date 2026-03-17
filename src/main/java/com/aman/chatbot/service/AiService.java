package com.aman.chatbot.service;

import org.springframework.stereotype.Service;

@Service
public class AiService {

    public String generateResponse(String userMessage) {

        // 🔥 For now simple stub
        return "Echo: " + userMessage;

        // Later: AI integration
    }
}