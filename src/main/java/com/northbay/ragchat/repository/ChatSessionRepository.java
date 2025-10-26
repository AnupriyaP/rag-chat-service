package com.northbay.ragchat.repository;

import com.northbay.ragchat.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for managing {@link ChatSession} entities.
 * <p>
 * Extends {@link JpaRepository} to provide CRUD operations and custom
 * query methods for retrieving chat sessions by owner or favorite status.
 */
@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    List<ChatSession> findByOwner(String owner);
    List<ChatSession> findByFavoriteTrue();
}
