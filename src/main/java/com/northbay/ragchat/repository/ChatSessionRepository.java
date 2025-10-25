package com.northbay.ragchat.repository;

import com.northbay.ragchat.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    List<ChatSession> findByOwner(String owner);
    List<ChatSession> findByFavoriteTrue();
}
