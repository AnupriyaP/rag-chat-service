package com.northbay.ragchat.dto;

import java.time.OffsetDateTime;
import java.util.List;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object (DTO) representing a chat session.
 * <p>
 * Used to transfer session data between the backend and clients
 * without exposing entity details. A session contains metadata
 * such as title, owner, and favorite status, as well as a list of
 * associated chat messages.
 */
public class ChatSessionDTO {
    private Long id;

    @NotBlank(message = "Title is required")

    private String title;
    private boolean favorite;
    private String owner;
    private OffsetDateTime createdAt;
    private List<ChatMessageDTO> messages;

    // getters & setters
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getTitle(){return title;} public void setTitle(String title){this.title=title;}
    public boolean isFavorite(){return favorite;} public void setFavorite(boolean favorite){this.favorite=favorite;}
    public String getOwner(){return owner;} public void setOwner(String owner){this.owner=owner;}
    public OffsetDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(OffsetDateTime createdAt){this.createdAt=createdAt;}
    public List<ChatMessageDTO> getMessages(){return messages;} public void setMessages(List<ChatMessageDTO> messages){this.messages=messages;}
}
