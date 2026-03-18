package com.aman.chatbot.repository;

import com.aman.chatbot.entity.Message;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationId(Long conversationId);
    List<Message> findByConversationIdOrderByTimestampAsc(Long conversationId);
    List<Message> findTop10ByConversationIdOrderByTimestampDesc(Long conversationId);

    @Modifying
    @Transactional
    void deleteByConversationId(Long conversationId);
}
