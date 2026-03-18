package com.aman.chatbot.service;

import com.aman.chatbot.entity.Conversation;
import com.aman.chatbot.entity.Message;
import com.aman.chatbot.repository.ConversationRepository;
import com.aman.chatbot.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Semaphore;

@Service
@RequiredArgsConstructor
public class AiService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://openrouter.ai/api/v1/chat/completions")
            .build();

    private final Semaphore rateLimiter = new Semaphore(2);

    public String generateResponse(Long conversationId) {

        boolean permitAcquired = false;

        try {
            rateLimiter.acquire();
            permitAcquired = true;

            Thread.sleep(300);

            // ✅ STEP 1: Get conversation + category
            Conversation convo = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));

            String category = convo.getCategory();

            if (category == null || category.isBlank()) {
                return "⚠️ Please select a category first (e.g., Fitness, Tech, Finance)";
            }

            // ✅ STEP 2: Fetch messages
            List<Message> history =
                    messageRepository.findTop10ByConversationIdOrderByTimestampDesc(conversationId);

            Collections.reverse(history);

            // ✅ STEP 3: Build messages
            List<Map<String, Object>> messages = new ArrayList<>();

            messages.add(Map.of(
                    "role", "system",
                    "content", buildSystemPrompt(category)
            ));

            for (Message msg : history) {
                messages.add(Map.of(
                        "role", msg.getType().equals("USER") ? "user" : "assistant",
                        "content", msg.getContent()
                ));
            }

            // ✅ STEP 4: Call AI
            Map<String, Object> response = webClient.post()
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(Map.of(
                            "model", model,
                            "messages", messages,
                            "temperature", 0.7
                    ))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .timeout(Duration.ofSeconds(30))
                    .retryWhen(
                            Retry.backoff(2, Duration.ofSeconds(2))
                                    .maxBackoff(Duration.ofSeconds(15))
                                    .filter(this::isRetryableError)
                    )
                    .onErrorResume(ex -> {
                        ex.printStackTrace();
                        return Mono.error(ex);
                    })
                    .blockOptional()
                    .orElse(null);

            // ✅ STEP 5: Parse response
            if (response == null || !response.containsKey("choices")) {
                System.err.println("FULL RESPONSE: " + response);
                return "⚠️ AI is busy right now. Please try again.";
            }

            List<?> choices = (List<?>) response.get("choices");

            if (choices.isEmpty()) {
                return "⚠️ No response from AI.";
            }

            Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
            Map<?, ?> messageObj = (Map<?, ?>) firstChoice.get("message");

            if (messageObj == null || messageObj.get("content") == null) {
                return "⚠️ Empty AI response.";
            }

            return messageObj.get("content").toString();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Request interrupted. Please try again.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Something went wrong. Please try again.";

        } finally {
            if (permitAcquired) {
                rateLimiter.release();
            }
        }
    }

    private boolean isRetryableError(Throwable ex) {
        return ex instanceof WebClientResponseException.TooManyRequests ||
                ex instanceof WebClientResponseException.ServiceUnavailable ||
                ex instanceof WebClientResponseException.BadGateway ||
                ex instanceof WebClientResponseException.GatewayTimeout;
    }

    // 🔥 STRONG CATEGORY CONTROL (AI handles restriction)
    private String buildSystemPrompt(String category) {
        return """
                You are an AI assistant strictly limited to the category: %s.

                Rules:
                - ONLY answer questions related to %s.
                - If the question is unrelated, respond EXACTLY with:
                  "❌ Please ask questions related to %s only."
                - Do not answer anything outside the category.
                - Be helpful, clear, and concise for valid questions.
                """.formatted(category, category, category);
    }
}