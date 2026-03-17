package com.aman.chatbot.repository;

import com.aman.chatbot.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByUserIdOrderByCreatedAtDesc(Long userId);
}