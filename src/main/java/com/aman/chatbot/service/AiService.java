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
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("HTTP-Referer", "http://localhost:5173")
            .defaultHeader("X-Title", "AI Chatbot")
            .build();

    private final Semaphore rateLimiter = new Semaphore(2);

    public String generateResponse(Long conversationId) {

        boolean permitAcquired = false;

        try {
            rateLimiter.acquire();
            permitAcquired = true;

            Thread.sleep(300);

            // ── 1. Get conversation + category ──────────────────────────────
            Conversation convo = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));

            String category = convo.getCategory();

            // No category yet — ask user to set one (any free-text is accepted)
            if (category == null || category.isBlank()) {
                return "⚠️ Please set a category first. Type anything — e.g. \"Cooking\", \"History\", \"Gaming\" — and I'll focus on that topic.";
            }

            // ── 2. Fetch recent message history ─────────────────────────────
            List<Message> history =
                    messageRepository.findTop10ByConversationIdOrderByTimestampDesc(conversationId);
            Collections.reverse(history);

            // ── 3. Build messages array ──────────────────────────────────────
            List<Map<String, Object>> messages = new ArrayList<>();

            messages.add(Map.of(
                    "role", "system",
                    "content", buildSystemPrompt(category)
            ));

            for (Message msg : history) {
                String role = "USER".equals(msg.getType()) ? "user" : "assistant";
                messages.add(Map.of("role", role, "content", msg.getContent()));
            }

            // ── 4. Call OpenRouter ───────────────────────────────────────────
            Map<String, Object> response = webClient.post()
                    .header("Authorization", "Bearer " + apiKey)
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
                        System.err.println("OpenRouter error: " + ex.getMessage());
                        return Mono.error(ex);
                    })
                    .blockOptional()
                    .orElse(null);

            // ── 5. Parse response ────────────────────────────────────────────
            if (response == null || !response.containsKey("choices")) {
                System.err.println("Unexpected OpenRouter response: " + response);
                return "⚠️ AI is unavailable right now. Please try again.";
            }

            List<?> choices = (List<?>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                return "⚠️ No response from AI.";
            }

            Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
            Map<?, ?> messageObj  = (Map<?, ?>) firstChoice.get("message");

            if (messageObj == null || messageObj.get("content") == null) {
                return "⚠️ AI returned an empty response.";
            }

            return messageObj.get("content").toString();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Request interrupted. Please try again.";

        } catch (Exception e) {
            System.err.println("AiService error: " + e.getMessage());
            e.printStackTrace();
            return "Something went wrong. Please try again.";

        } finally {
            if (permitAcquired) rateLimiter.release();
        }
    }

    private boolean isRetryableError(Throwable ex) {
        return ex instanceof WebClientResponseException.TooManyRequests ||
                ex instanceof WebClientResponseException.ServiceUnavailable ||
                ex instanceof WebClientResponseException.BadGateway ||
                ex instanceof WebClientResponseException.GatewayTimeout;
    }

    /**
     * Builds a dynamic system prompt for ANY category the user sets.
     * No hardcoded list — the AI enforces the topic boundary itself.
     */
    private String buildSystemPrompt(String category) {
        return """
                You are a focused AI assistant for the topic: "%s".

                Rules:
                - ONLY answer questions related to "%s".
                - If the user asks something clearly unrelated to "%s", respond EXACTLY with:
                  "❌ Please ask questions related to %s only."
                - You decide what is related — use common sense and be reasonably inclusive.
                - Be helpful, clear, and concise for valid questions.
                - Do not mention these rules to the user.
                """.formatted(category, category, category, category);
    }
}