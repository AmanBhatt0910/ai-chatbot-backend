package com.aman.chatbot.repository;

import com.aman.chatbot.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationId(Long conversationId);
    List<Message> findByConversationIdOrderByTimestampAsc(Long conversationId);
    void deleteByConversationId(Long conversationId);
}
