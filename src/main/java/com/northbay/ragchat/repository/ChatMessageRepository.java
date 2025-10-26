package com.northbay.ragchat.repository;

import com.northbay.ragchat.entity.ChatMessage;
import com.northbay.ragchat.entity.ChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
/**
 * Repository interface for performing database operations on {@link ChatMessage} entities.
 * <p>
 * Extends {@link JpaRepository} to provide CRUD and pagination support.
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findBySession(ChatSession session, Pageable pageable);
}
