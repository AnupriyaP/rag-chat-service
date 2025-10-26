package com.northbay.ragchat.controller;

import com.northbay.ragchat.api.ChatSessionsApi;
import com.northbay.ragchat.model.ChatSessionCreateRequest;
import com.northbay.ragchat.model.ChatSessionDTO;
import com.northbay.ragchat.model.ChatSessionUpdateRequest;
import com.northbay.ragchat.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j; // ✅ NEW IMPORT
/**
 * REST controller for managing chat sessions.
 * Supports creating, renaming, listing and deleting chat sessions.
 *
 * All endpoints require API key authentication via 'X-API-KEY' header.**/
@Slf4j
@RestController
public class ChatSessionsController implements ChatSessionsApi {

    private final ChatService chatService;

    public ChatSessionsController(ChatService chatService) {
        this.chatService = chatService;
        log.info("ChatSessionsController initialized."); // ✅ LOGGED
    }

    /**
     * Create a new chat session.
     *
     * @param request the session creation request
     * @return HTTP 201 with created session in body and Location header
     */
    @Override
    public ResponseEntity<ChatSessionDTO> createChatSession(
            @Valid ChatSessionCreateRequest request) {
        log.info("Creating new chat session for owner: {}", request.getOwner()); // ✅ LOGGED
        return ResponseEntity.status(201).body(chatService.createChatSession(request));
    }

    /**
     * Get all sessions for the user (or all sessions if system).
     */
    @Override
    public ResponseEntity<List<ChatSessionDTO>> listChatSessions(String owner) {
        return ResponseEntity.ok(chatService.listChatSessions(owner));
    }

    /**
     * Rename a session.
     */
    @Override
    public ResponseEntity<ChatSessionDTO> updateChatSession(
            Integer id, @Valid ChatSessionUpdateRequest request) {
        return ResponseEntity.ok(chatService.updateChatSession(id.longValue(), request));
    }

    /**
     * Delete a session.
     */
    @Override
    public ResponseEntity<Void> deleteChatSession(Integer id) {
        chatService.deleteChatSession(id.longValue());
        return ResponseEntity.noContent().build();
    }

    /**
     * lists a favourite session
     * @return
     */
    @Override
    public ResponseEntity<List<ChatSessionDTO>> listFavoriteSessions() {
        return ResponseEntity.ok(chatService.listFavoriteSessions());
    }
}
