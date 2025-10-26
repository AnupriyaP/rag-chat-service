package com.northbay.ragchat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Entity representing a chat session in the RAG Chat Service.
 * <p>
 * A chat session groups multiple messages exchanged between a user
 * and the system (or assistant). It includes session metadata such
 * as title, owner, and favorite status.
 */
@Entity
@Table(name = "chat_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String owner;
    private Boolean favorite;

    //private OffsetDateTime createdAt = OffsetDateTime.now();
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();  // automatically set timestamp when saving
    }


    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages;
}
